package com.nextlabs.bulkprotect.datatype.actionparams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nextlabs.bulkprotect.datatype.ActionParams;
import com.nextlabs.bulkprotect.datatype.JSONType;
import com.nextlabs.vfs.constant.RepositoryType;

public class EncryptActionParams extends ActionParams {
	boolean keepOriginal;
	boolean transferMetadata;
	RepositoryType repoType;
	String filePattern;
	String[] filePatternSplit;
	List<Classification> classifications;
	
	public EncryptActionParams() {
		this.classifications = new ArrayList<Classification>();
	}

	public boolean isKeepOriginal() { return keepOriginal; }

	public boolean isTransferMetadata() { return transferMetadata; }
	
	public RepositoryType getRepoType() { return repoType; }
	
	public String getFilePattern() { return filePattern; }
	
	public String[] getFilePatternSplit() { return filePatternSplit; }

	public List<Classification> getClassifications() { return classifications; }
	
	public void setKeepOriginal(boolean keepOriginal) { this.keepOriginal = keepOriginal; }

	public void setTransferMetadata(boolean transferMetadata) { this.transferMetadata = transferMetadata; }
	
	public void setRepoType(RepositoryType repoType) { this.repoType = repoType; }
	
	public void setFilePattern(String filePattern) {
		if (filePattern == null || filePattern.isEmpty()) {
			this.filePattern = "";
			this.filePatternSplit = new String[0];
		} else {
			this.filePattern = filePattern;
			if (repoType == RepositoryType.SHARED_FOLDER && !this.filePattern.startsWith("file:")) {
				filePatternSplit = ("file:" + this.filePattern).toLowerCase().split("(?=[^A-Za-z0-9_*])");
			} else if (repoType == RepositoryType.AZURE_FILE_STORAGE && !this.filePattern.startsWith("null:")) {
				filePatternSplit = ("null:" + this.filePattern).toLowerCase().split("(?=[^A-Za-z0-9_*])");
			} else {
				filePatternSplit = this.filePattern.toLowerCase().split("(?=[^A-Za-z0-9_*])");
			}
		}
	}

	public void setClassifications(List<Classification> classifications) { this.classifications = classifications; }

	protected static class Classification extends JSONType {
		String key; String[] value;

		public Classification() {}
		
		public String getKey() { return key; }
		public String[] getValue() { return value; }
		public void setKey(String key) { this.key = key; }
		public void setValue(String[] value) { this.value = value; }
	}
	
	@JsonIgnore
	public Map<String, String[]> getTags() {
		return classifications.stream().collect(Collectors.toMap(Classification::getKey, Classification::getValue));
	}
}
