package com.nextlabs.bulkprotect.datatype;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration extends JSONType {
	List<Repository> repositories;
	List<Rule> rules;
	SkyDRMConfiguration skyDRMCfg;
	RuleParams ruleParams;

	int historyRetentionTime = 30;
	
	Map<String, Repository> repositoryMap;
	Map<String, Rule> ruleMap;

	public Configuration() {
		repositories = new ArrayList<Repository>();
		rules = new ArrayList<Rule>();
		skyDRMCfg = new SkyDRMConfiguration();
		ruleParams = new RuleParams();
		historyRetentionTime = 30;
	}

	public List<Repository> getRepositories() { return repositories; }

	public List<Rule> getRules() { return rules; }
	
	public SkyDRMConfiguration getSkyDRMCfg() { return skyDRMCfg; }

	public RuleParams getRuleParams() { return ruleParams; }

	public int getHistoryRetentionTime() { return historyRetentionTime; }
	
	@JsonIgnore
	public Map<String, Repository> getRepositoryMap() {
		if (this.repositoryMap == null) repositoryMap = repositories.stream().collect(Collectors.toMap(Repository::getId, x -> x));
		return repositoryMap;
	}
	
	@JsonIgnore
	public Map<String, Rule> getRuleMap() {
		if (this.ruleMap == null) ruleMap = rules.stream().collect(Collectors.toMap(Rule::getId, x -> x));
		return ruleMap;
	}

	@JsonIgnore
	public boolean validate() {
		boolean valid = true;
		
		// Check that all repository Id references refer to existing repositories
		if (rules.stream().anyMatch(r -> !getRepositoryMap().containsKey(r.getRepositoryId()))) valid = false;
		
		return valid;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public class RuleParams {
		String[] delimiters;
		String[] clsKeys;
		boolean useDelimiter;
		boolean hideNxlExt;

		public RuleParams() {
			delimiters = new String[]{","};
			clsKeys = new String[]{};
			useDelimiter = false;
			hideNxlExt = false;
		}

		public String[] getDelimiters() { return delimiters; }

		public String[] getClsKeys() { return clsKeys; }

		public boolean isUseDelimiter() { return useDelimiter; }
		
		public boolean isHideNxlExt() { return hideNxlExt; }
	}
}
