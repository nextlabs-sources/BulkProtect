package com.nextlabs.bulkprotect.datatype;

import java.util.HashMap;
import java.util.Map;

public class RuleExecutionResult extends JSONType {
	private long startTime, completionTime, volumeProcessed;
	private int total, processed, skipped, failed;
	boolean succeeded = true;
	Rule rule;
	String details;
	String logFilename;
	Map<String, Integer> extMap;
	
	public RuleExecutionResult() {}

	public RuleExecutionResult(Rule rule, String logFilename) {
		this.rule = (Rule) rule.copy().getValue();
		this.logFilename = logFilename;
	}

	public void start() { startTime = System.currentTimeMillis(); }

	public void finish() { completionTime = System.currentTimeMillis(); }

	public void incProcessed() { processed++; total++; }

	public void incSkipped() { skipped++; total++; }

	public void incFailed() { failed++; total++; }
	
	public void incVolume(long size) { volumeProcessed += size; }

	public void fail(String details) {
		succeeded = false;
		finish();
	}

	public long getStartTime() { return startTime; }

	public long getCompletionTime() { return completionTime; }

	public int getTotal() { return total; }

	public int getProcessed() { return processed; }

	public int getSkipped() { return skipped; }

	public int getFailed() { return failed; }

	public boolean isSucceeded() { return succeeded; }
	
	public long getVolumeProcessed() { return volumeProcessed; }
	
	public Rule getRule() { return rule; }

	public String getDetails() { return details; }
	
	public String getLogFilename() { return logFilename; }

	public Map<String, Integer> getExtMap() { return this.extMap == null ? new HashMap<>() : this.extMap; }

	public void setExtMap(Map<String, Integer> extMap) { this.extMap = extMap; }
}
