package com.nextlabs.bulkprotect.datatype;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import com.nextlabs.bulkprotect.datatype.actionparams.DecryptActionParams;
import com.nextlabs.bulkprotect.datatype.actionparams.EncryptActionParams;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.EventFeedServer;
import com.nextlabs.bulkprotect.crypto.StaticKeyCipher;
import com.nextlabs.bulkprotect.filesupplier.DirectoryWalker;
import com.nextlabs.bulkprotect.filesupplier.FileSupplierFilter;
import com.nextlabs.bulkprotect.interfaces.FutureEvent;
import com.nextlabs.bulkprotect.interfaces.TaskFactory;
import com.nextlabs.bulkprotect.predicate.FileExtensionCriteria;
import com.nextlabs.bulkprotect.task.FileSetTask;
import com.nextlabs.bulkprotect.task.filetasks.AbstractFileTask;
import com.nextlabs.bulkprotect.task.filetasks.DecryptTask;
import com.nextlabs.bulkprotect.task.filetasks.DummyTask;
import com.nextlabs.bulkprotect.task.filetasks.EncryptTask;
import com.nextlabs.bulkprotect.util.Result;
import com.nextlabs.vfs.RepositoryFileSystemManager;

@SuppressWarnings("rawtypes")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Rule extends JSONType implements FutureEvent, TaskFactory {
	
	@JsonIgnore
	private static final Logger logger = LogManager.getLogger(Rule.class);
	@JsonIgnore
	private static final EventFeedServer ws = App.getEventServer();
	
	String name;
	String repositoryId;
	String subdirectory;
	String action;
	ActionParams params;
	String[] fileExtCriteria;
	boolean fileExtCriteriaIncl;
	boolean active;
	String execFreq;
	int execTime;
	int execMinute;
	int execHour;
	int execDay;
	String id;
	
	Repository repository;
	
	public Rule() {}

	

	public String getExecFreq() { return execFreq; }

	public int getExecTime() { return execTime; }
	
	public int getExecMinute() { return execMinute; }
	
	public int getExecHour() { return execHour; }

	public int getExecDay() { return execDay; }
	
	public String getId() { return id == null ? UUID.randomUUID().toString() : id; }
	
	public String getRepositoryId() { return repositoryId; }

	public void setName(String name) { this.name = name; }

	public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }

	public void setSubdirectory(String subdirectory) { this.subdirectory = subdirectory; }

	public void setAction(String action) { this.action = action; }

	public void setParams(ActionParams params) { this.params = params; }

	public void setFileExtCriteria(String[] fileExtCriteria) { this.fileExtCriteria = fileExtCriteria; }

	public void setFileExtCriteriaIncl(boolean fileExtCriteriaIncl) { this.fileExtCriteriaIncl = fileExtCriteriaIncl; }

	public void setActive(boolean active) { this.active = active; }

	public void setExecFreq(String execFreq) { this.execFreq = execFreq; }

	public void setExecTime(int execTime) { this.execTime = execTime; }
	
	public void setExeMinute(int execMinute) { this.execMinute = execMinute; }
	
	public void setExecHour(int execHour) { this.execHour = execHour; }

	public void setExecDay(int execDay) { this.execDay = execDay; }
	
	public void setId (String id) { this.id = id == null ? UUID.randomUUID().toString() : id; }
	
	public void setRepository(Repository repo) { this.repository = repo; }

	public String getName() { return name; }

	public String getSubdirectory() { return subdirectory; }

	public String getAction() { return action; }

	public ActionParams getParams() {
		if (params == null) {
			params = getNewActionParamsInstance();
		}
		if (action.equals("Rights Protect")) {
			((EncryptActionParams) params).setRepoType(App.getConfiguration().getRepositoryMap().get(repositoryId).getRepoType());
			((EncryptActionParams) params).setFilePattern(((EncryptActionParams) params).getFilePattern());
		}
		return params;
	}

	public String[] getFileExtCriteria() { return fileExtCriteria; }
	
	public boolean getFileExtCriteriaIncl() { return fileExtCriteriaIncl; }
	
	public boolean isActive() { return active; }
	
	public String getFullDirectory() {
		try { return getRepository().getRootUrl() + subdirectory; } catch (Exception e) { logger.debug(e.getMessage(), e); }
		return this.repository.getRootUrl() + subdirectory;
	}
	
	public Repository getRepository() {
		try {return App.getConfiguration().getRepositoryMap().get(repositoryId); } catch (Exception e) { logger.debug(e.getMessage(), e); }
		return this.repository;
	}
	
	@JsonIgnore
	public FileExtensionCriteria getFileExtensionCriteria() { return new FileExtensionCriteria(fileExtCriteria, fileExtCriteriaIncl); }
	
	public Class<? extends AbstractFileTask> getFileTaskClass() {
		switch (action) {
			case "Rights Protect": return EncryptTask.class;
			case "Remove Protection": return DecryptTask.class;
			default: return DummyTask.class;
		}
		// TODO: Handle this properly, and for more actions
	}

	@JsonIgnore
	public ActionParams getNewActionParamsInstance() {
		switch (action) {
			case "Rights Protect": return new EncryptActionParams();
			case "Remove Protection": return new DecryptActionParams();
			default: return null;
		}
	}

	@JsonIgnore
	@Override
	public Result<FileSetTask, Exception> createNewTask() {
		logger.debug("Creating new task");
		String logFilename = "RightsProtectionToolExecLog_" + getName().replace(" ", "_") + "-" + getLogfileDateTimeString();
		ThreadContext.put("logFilename", logFilename);
		try {
			logger.debug("Initializing RepositoryFileSystemManager");
			RepositoryFileSystemManager fsMgr = new RepositoryFileSystemManager();
			if (System.getProperty("enable_debugging").equals("true")) {
				fsMgr.printSingletonContextProperties();
				logger.debug("getFullDirectory" + "=" + getFullDirectory());
				logger.debug("getDomain" + "=" + getRepository().getRepositoryCredentials().getDomain());
				logger.debug("getUserName" + "=" + getRepository().getRepositoryCredentials().getUserName());
				logger.debug("getPassword" + "=" + getRepository().getRepositoryCredentials().getPassword());
				logger.debug("getRepoType" + "=" + getRepository().getRepoType().getName());
			}
			FileObject fo = fsMgr.resolveFile(getFullDirectory(), getRepository().getRepositoryCredentials(), getRepository().getRepoType());
			logger.debug("Initializing DirectoryWalker");
			DirectoryWalker directoryWalker = new DirectoryWalker(fo);
			logger.debug("DirectoryWalker successfully initialized. Initializing FileSupplierFilter");
			Supplier<Optional<Result<FileObject, Exception>>> dw = new FileSupplierFilter(directoryWalker, getFileExtensionCriteria());
			return Result.ok(new FileSetTask(dw, getFileTaskClass(), getParams(), new RuleExecutionResult(this, logFilename), directoryWalker.toString()));
		} catch (FileSystemException e) {
			ws.broadcastError("Failed to initialize task. Directory could not be accessed.");
			logger.error(String.format("%s - Failed to initialize directory walker", getFullDirectory()));
			logger.error(e.getMessage(), e);
			RuleExecutionResult r = new RuleExecutionResult(this, logFilename);
			r.start();
			r.fail("Failed to initialize task. Directory could not be accessed.");
			ws.broadcastData(r);
			writeResultToFile(r);
			return Result.err(e);
		} catch (Throwable t) {
			t.printStackTrace();
			ws.broadcastError("Failed to initialize task. Directory could not be accessed.");
			logger.error(String.format("%s - Failed to initialize directory walker", getFullDirectory()));
			logger.error(t.getMessage(), t);
			RuleExecutionResult r = new RuleExecutionResult(this, logFilename);
			r.start();
			r.fail("Failed to initialize task. Directory could not be accessed.");
			ws.broadcastData(r);
			writeResultToFile(r);
			return Result.err(new Exception(t));
		}
	}

	int prevExecTime = -1;

	@Override
	public long getNextScheduledTime() {
		boolean execTimeNotPassed = (ZonedDateTime.now().getHour() * 60 + ZonedDateTime.now().getMinute()) < execTime;
		if (execFreq.equals("Minutes")) {
			int currentTime = ZonedDateTime.now().getHour() * 60 + ZonedDateTime.now().getMinute();
			if (prevExecTime == -1 || currentTime - prevExecTime >= execMinute) {
				prevExecTime = currentTime;
			}
			return ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()).plusMinutes(prevExecTime + execMinute).toEpochSecond() * 1000L;
		}
		else if (execFreq.equals("Hourly")) {
			int currentTime = ZonedDateTime.now().getHour() * 60 + ZonedDateTime.now().getMinute();
			if (prevExecTime == -1 || currentTime - prevExecTime >= execHour * 60) {
				prevExecTime = currentTime;
			}
			return ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()).plusMinutes(prevExecTime + execHour * 60).toEpochSecond() * 1000L;
		}
		else if (execFreq.equals("Daily")) {
			long addDay = execTimeNotPassed ? 0L : 1L;
			return ZonedDateTime.now().plusDays(addDay).toLocalDate().atStartOfDay(ZoneId.systemDefault()).plusMinutes(execTime).toEpochSecond() * 1000L;
		}
		else if (execFreq.equals("Weekly")) {
			if (ZonedDateTime.now().getDayOfWeek() == DayOfWeek.of(execDay) && execTimeNotPassed)
				return ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()).plusMinutes(execTime).toEpochSecond() * 1000L;
			else return ZonedDateTime.now().with(TemporalAdjusters.next(DayOfWeek.of(execDay))).toLocalDate().atStartOfDay(ZoneId.systemDefault()).plusMinutes(execTime).toEpochSecond() * 1000L;
		}
		else if (execFreq.equals("Monthly")) {
			TemporalAdjuster dateAdjuster = ZonedDateTime.now().getDayOfMonth() < execDay || ZonedDateTime.now().getDayOfMonth() == execDay && execTimeNotPassed ? TemporalAdjusters.firstDayOfMonth() : TemporalAdjusters.firstDayOfNextMonth();
			return ZonedDateTime.now().with(dateAdjuster).plusDays(execDay-1L).toLocalDate().atStartOfDay(ZoneId.systemDefault()).plusMinutes(execTime).toEpochSecond() * 1000L;
		}
		return 0L;
	}
	
	public Result<Void, FileSystemException> testSubdirectory() {
		RepositoryFileSystemManager fsMgr;
		try {
			fsMgr = new RepositoryFileSystemManager();
			FileObject dir = fsMgr.resolveFile(getFullDirectory(), getRepository().getRepositoryCredentials(), getRepository().getRepoType());
			FileType type = null;
			try {
				type = dir.getType();
			} catch (Throwable t) {
				throw t;
			}
			if (type != FileType.FOLDER) throw new FileSystemException("Connection failed or directory path was incorrect.");
			return Result.ok(null);
		} catch (FileSystemException e) {
			return Result.err(e);
		}
	}
	
	@JsonIgnore
	@Override
	public Result<? extends JSONType, ? extends Exception> copy() {
		this.repository = getRepository();
		return super.copy();
	}
	
	@JsonIgnore
	public void writeResultToFile(RuleExecutionResult result) {
		Path resultsFilePath = App.getExecHistoryJSONPath();
		ObjectMapper jm = App.getJsonObjectMapper();
		try {
			JsonNode root;
			if (!Files.exists(resultsFilePath)) Files.createFile(resultsFilePath);
			try (FileChannel ch = FileChannel.open(resultsFilePath, StandardOpenOption.READ);) {
				try (InputStream is = StaticKeyCipher.wrap(Channels.newInputStream(ch))) { root = jm.readTree(is); }
				if (!(root instanceof ArrayNode)) root = jm.createArrayNode();
				((ArrayNode) root).insert(0, jm.valueToTree(result));
			}
			try(FileChannel ch = FileChannel.open(resultsFilePath, StandardOpenOption.WRITE); OutputStream os = StaticKeyCipher.wrap(Channels.newOutputStream(ch)); FileLock lock = ch.lock();) {
				String jsonString = root.toString();
				os.write(jsonString.getBytes());
			}
			logger.debug("Successfully wrote execution result to file");
		} catch (IOException e) {
			logger.error("Rule execution result could not be recorded!");
			logger.error(e.getMessage(), e);
		}	
	}

	@JsonIgnore
  protected static String getLogfileDateTimeString() {
	  return (new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss")).format(new Date());
  }
}
