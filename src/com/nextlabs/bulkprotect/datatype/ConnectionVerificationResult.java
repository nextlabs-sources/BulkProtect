package com.nextlabs.bulkprotect.datatype;

import java.util.ArrayList;
import java.util.List;

public class ConnectionVerificationResult extends JSONType {

	private List<RuleConnectionResult> results;
	private long verificationTime;
	private boolean skydrmConnection;

	public ConnectionVerificationResult() { this.results = new ArrayList<RuleConnectionResult>(); }

	public void addResult(RuleConnectionResult newResult) { this.results.add(newResult); }

	public List<RuleConnectionResult> getResults() { return results; }

	public long getVerificationTime() { return verificationTime; }

	public boolean isSkydrmConnection() { return skydrmConnection; }

	public void setResults(List<RuleConnectionResult> results) { this.results = results; }

	public void setVerificationTime(long l) { this.verificationTime = l; }

	public void setSkydrmConnection(boolean skydrmConnection) { this.skydrmConnection = skydrmConnection; }

	public class RuleConnectionResult extends JSONType {
		String fullDirectory, ruleName;
		boolean result;
		
		public RuleConnectionResult() {}
		
		public RuleConnectionResult(String fullDirectory, String ruleName, boolean result) {
			this.fullDirectory = fullDirectory;
			this.ruleName = ruleName;
			this.result = result;
		}

		public String getFullDirectory() { return fullDirectory; }

		public String getRuleName() { return ruleName; }

		public boolean isResult() { return result; }

		public void setFullDirectory(String fullDirectory) { this.fullDirectory = fullDirectory; }

		public void setRuleName(String ruleName) { this.ruleName = ruleName; }

		public void setConnectionResult(boolean connectionResult) { result = connectionResult; }
	}
}
