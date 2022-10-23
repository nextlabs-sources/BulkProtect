package com.nextlabs.bulkprotect.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import com.nextlabs.bulkprotect.filesupplier.FileSupplierFilter;
import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.EventFeedServer;
import com.nextlabs.bulkprotect.crypto.StaticKeyCipher;
import com.nextlabs.bulkprotect.datatype.ActionParams;
import com.nextlabs.bulkprotect.datatype.RuleExecutionResult;
import com.nextlabs.bulkprotect.datatype.actionparams.EncryptActionParams;
import com.nextlabs.bulkprotect.task.filetasks.AbstractFileTask;
import com.nextlabs.bulkprotect.task.filetasks.EncryptTask;
import com.nextlabs.bulkprotect.util.FileSkippedException;
import com.nextlabs.bulkprotect.util.Result;

@SuppressWarnings("rawtypes")
public class FileSetTask extends AbstractTask<RuleExecutionResult, Exception> {
	
	private static final Logger logger = LogManager.getLogger(FileSetTask.class);
	private static Set<String> current = Collections.synchronizedSet(new HashSet<String>());
	protected static final EventFeedServer ws = App.getEventServer();

	private final ExecutorService des;
	
	protected Supplier<Optional<Result<FileObject, Exception>>> dw;
	protected Class<? extends AbstractFileTask> fileTaskClass;
	protected ActionParams fileTaskParams;
	
	private Queue<Runnable> fileTaskQueue;
	private int queueSize = Runtime.getRuntime().availableProcessors()*32;
	private long lastQueueActivity = Long.MAX_VALUE;
	private long timeout = 3600000L;
	
	private RuleExecutionResult result;
	
	private String rootDir;
	
	public FileSetTask(Supplier<Optional<Result<FileObject, Exception>>> fileSupplier, Class<? extends AbstractFileTask> fileTaskClass, ActionParams params, RuleExecutionResult result, String rootDir) {
		this.dw = fileSupplier;
		this.fileTaskClass = fileTaskClass;
		this.fileTaskParams = params;
		
		this.fileTaskQueue = new ArrayDeque<Runnable>(queueSize);
		this.result = result;
		this.des = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
		
		this.rootDir = rootDir;
	}

	@Override
	public void run() {
		prestart();
		if (!isDone()) {
			process();
			finish();
		}
	}
	
	protected void prestart() {
		ThreadContext.put("logFilename", result.getLogFilename());
		result.start();
		synchronized (current) {
			if (current.contains(dw.toString())) setError(new ConcurrentModificationException(String.format("Another task is currently operating on the file source %s", dw.toString())));
			else current.add(dw.toString());
		}
		
	}
	
	protected void process() {
		for (Optional<Result<FileObject, Exception>> fileResult = dw.get(); fileResult.isPresent(); fileResult = dw.get()) {
			if (fileResult.get().isErr()) {
				if (fileResult.get().getError() instanceof FileSkippedException) { result.incSkipped(); continue; }
				else { setError(fileResult.get().getError()); break; }
			}
			FileObject file = fileResult.get().getValue();
			addNewTask(newFileTask(file));
			logger.info(String.format("Submitted %s to be processed by %s", file.toString(), fileTaskClass.getName()));
		}
	}
	
	protected void finish() {
		while (!fileTaskQueue.isEmpty()) {
			Runnable r = fileTaskQueue.poll();
			if (r instanceof AbstractFileTask) handleFileTaskResult((AbstractFileTask) r);
		}
		result.finish();
		set(result);
		result.setExtMap(((FileSupplierFilter) dw).getExtCount());
		ws.broadcastSuccess(String.format("Rule execution on %s has completed successfully.", dw.toString()));
		ws.broadcastData(result);
		result.getRule().writeResultToFile(result);
		synchronized (current) { current.remove(dw.toString()); }
		this.des.shutdownNow();
	}
	
	@SuppressWarnings("unchecked")
	private Runnable newFileTask(FileObject file) {
		try {
			if (fileTaskClass.equals(EncryptTask.class)) {
				return new EncryptTask(file, (EncryptActionParams) fileTaskParams, rootDir);
			} else {
				return (AbstractFileTask<Object, ? extends Throwable>) fileTaskClass.getConstructors()[0].newInstance(file, fileTaskParams);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			logger.error(String.format("%s - Failed to instantiate task.", file.toString()));
			logger.debug(e.getMessage(), e);
			return () -> {};
		}
	}
	
	private void addNewTask(Runnable task) {
		while (fileTaskQueue.size() >= queueSize) {
			Runnable r = fileTaskQueue.poll();
			if (r instanceof AbstractFileTask) {
				if (((AbstractFileTask) r).isDone()) handleFileTaskResult((AbstractFileTask) r);
				else {
					if ((System.currentTimeMillis() - lastQueueActivity) > timeout) {
						setError(new TimeoutException("Rule execution timed out after 1 hour of inactivity."));
						fileTaskQueue.clear();
						finish();
						break;
					}
					fileTaskQueue.add(r);
					try { Thread.sleep(300L); } catch (InterruptedException e) {}
				}
			}
		}
		this.des.submit(task);
		fileTaskQueue.add(task);
	}
	
	private void handleFileTaskResult(AbstractFileTask task) {
		Result r = task.get();
		if (r.isOk()) {
			if (r.getValue() instanceof Long && ((long) r.getValue()) == -1L) {
				result.incSkipped();
			} else {
				result.incProcessed();
				// Somewhat hackish way to aggregate volume of processed files. 
				if (r.getValue() instanceof Long) result.incVolume((long) r.getValue());
			}
		}
		else if (r.isErr()) {
			result.incFailed();
			logger.error(String.format("Error while processing file: %s", task.getTargetFile().toString()));
			logger.error(r.getError().getMessage(), r.getError());
		}
	}
	
	@Override
	protected void setError(Exception e) {
		super.setError(e);
		logger.error(e.getMessage(), e);
		result.fail(e.toString());
		ws.broadcastError(String.format("Rule execution on %s failed. Check rule execution history for more details...", dw.toString()));
		ws.broadcastData(result);
		result.getRule().writeResultToFile(result);
	}
}
