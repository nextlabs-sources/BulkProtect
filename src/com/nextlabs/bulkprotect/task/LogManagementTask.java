package com.nextlabs.bulkprotect.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.crypto.StaticKeyCipher;
import com.nextlabs.bulkprotect.datatype.RuleExecutionResult;

public class LogManagementTask extends AbstractTask<Void, Exception> {
	
	public static final Logger logger = LogManager.getLogger(LogManagementTask.class);
	public static final ObjectMapper jm = App.getJsonObjectMapper();
	
	public static final Path resultsFilePath = App.getExecHistoryJSONPath();
	public static final Path logFilesPath = App.getExecHistoryLogFilesPath();

	@Override
	public void run() {
		logger.debug("Running log management task...");
		// Parse the original file
		RuleExecutionResult[] results = new RuleExecutionResult[] {};
		if (Files.exists(resultsFilePath)) {
			try(FileChannel ch = FileChannel.open(resultsFilePath, StandardOpenOption.READ); InputStream is = StaticKeyCipher.wrap(Channels.newInputStream(ch))) {
				results = jm.readValue(is, RuleExecutionResult[].class);
			} catch (IOException e) {
				logger.error("Failed to parse execution history file!");
				logger.debug(e.getMessage(), e);
				return;
			}
		}
		
		// Compute cutoff time in ms
		long cutoff = ZonedDateTime.now().minusDays(App.getConfiguration().getHistoryRetentionTime()).toEpochSecond() * 1000L;
		
		// Filter the original exec history file and rewrite entries corresponding to rule executions completed after the cutoff time
		List<RuleExecutionResult> newResults = Arrays.stream(results).filter(e -> e.getCompletionTime() > cutoff).collect(Collectors.toList());
		String newResultsJson = null;
		try {
			newResultsJson = jm.writeValueAsString(newResults);
		} catch (JsonProcessingException e) {
			logger.error("Failed to serialize filtered rule execution results.");
			logger.debug(e.getMessage(), e);
		}
		
		// Will only overwrite the old file if JSON Serialization of new results succeeds
		if (newResultsJson != null) {
			try(FileChannel ch = FileChannel.open(resultsFilePath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
					OutputStream os = StaticKeyCipher.wrap(Channels.newOutputStream(ch)); FileLock lock = ch.lock();) {
				os.write(newResultsJson.getBytes());
			} catch (ClosedChannelException e) {
				// Channel will be closed because JSON Mapper has closed it, hence ignore this exception
			} catch (IOException e) {
				logger.error("Failed to write filtered rule execution history file");
				logger.debug(e.getMessage(), e);
				return;
			}
		}
		
		// Delete the expired log files
		Arrays.stream(results).filter(e -> e.getCompletionTime() <= cutoff).forEach(f -> {
			try {
				logger.debug(String.format("Deleting log file %s...", f.getLogFilename()));
				Files.deleteIfExists(logFilesPath.resolve(f.getLogFilename()));
				logger.debug(String.format("Successfully deleted log file %s...", f.getLogFilename()));
			} catch (IOException e) {
				logger.error(String.format("Could not delete log file %s", f.getLogFilename()));
				logger.debug(e.getMessage(), e);
			}
		});
		logger.debug("Log management task completed successfully.");
	}
}
