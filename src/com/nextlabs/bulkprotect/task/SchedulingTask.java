package com.nextlabs.bulkprotect.task;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.datatype.Rule;
import com.nextlabs.bulkprotect.util.Result;

public class SchedulingTask extends AbstractTask {
	
	private static final Logger logger = LogManager.getLogger(SchedulingTask.class);
	private static final long freq = App.getTaskSchedulerFreq();
	private static Random rand = new Random();

	@Override
	public void run() {
		logger.debug("Running task scheduler...");
		List<Rule> rules = App.getConfiguration().getRules();
		for (Rule rule : rules) {
			long timeToNextExecution = rule.getNextScheduledTime() - System.currentTimeMillis();
			if (rule.isActive() && timeToNextExecution > 0L && timeToNextExecution < (freq	* 60000L)) {
				timeToNextExecution += rand.nextInt(60000);
				logger.debug(String.format("Scheduling rule %s to run in %d seconds.", rule.getName(), timeToNextExecution/1000L));
				es.schedule(() -> {
					Result<FileSetTask, Exception> dt = rule.createNewTask();
					if (dt.isOk()) es.submit(dt.getValue());
				}, timeToNextExecution , TimeUnit.MILLISECONDS);
			}
		}
	}
}
