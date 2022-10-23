package com.nextlabs.bulkprotect.task.filetasks;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.EventFeedServer;
import com.nextlabs.bulkprotect.datatype.ActionParams;
import com.nextlabs.bulkprotect.task.AbstractTask;
import com.nextlabs.vfs.RepositoryFileSystemManager;

public abstract class AbstractFileTask<T, E extends Throwable> extends AbstractTask<T, E> {
	
	private static final Logger logger = LogManager.getLogger(AbstractFileTask.class);
	protected static final EventFeedServer ws = App.getEventServer();
	
	protected FileObject file, backupFile;
	protected RepositoryFileSystemManager fsMgr;
	
	public AbstractFileTask(FileObject target, ActionParams params) {
		file = target;
		this.fsMgr = (RepositoryFileSystemManager) target.getFileSystem().getFileSystemManager();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			if (!isDone()) {
				logger.info(String.format("Starting processing for %s", file.toString()));
				ws.broadcastLoading(String.format("Processing %s...", file.toString()));
				set(process());
				logger.info(String.format("Completed processing for %s", file.toString()));
				ws.broadcastLoading(String.format("Completed processing for %s", file.toString()));
			}
		} catch (Exception e) {
			setError((E) e);
			logger.error(String.format("An error occurred during the processing of %s", file.toString()));
			logger.debug(e.getMessage(), e);
			if (backupFile != null) rollback();
		} finally {
			if (backupFile != null) cleanup();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void backup() {
		logger.info(String.format("Backing up %s", file.toString()));
		try {
			if (file.getType() == FileType.FILE) {
				backupFile = fsMgr.resolveFile(file.toString() + ".bak");
				backupFile.copyFrom(file, new AllFileSelector());
			}
		} catch (FileSystemException e) { setError((E) e); }
	}
	
	protected void rollback() {
		try {
			file.copyFrom(backupFile, new AllFileSelector());
		} catch (FileSystemException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	protected void cleanup() {
		try {
			backupFile.delete();
			file.close();
			backupFile.close();
		} catch (FileSystemException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public FileObject getTargetFile() {
		return file;
	}
	
	protected abstract T process() throws Exception;
}