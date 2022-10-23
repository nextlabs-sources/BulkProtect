package com.nextlabs.bulkprotect.datatype;

import java.util.HashMap;
import java.util.Map;

public class SkyDRMClassificationProfile extends JSONType {
	Map<String, String[]> tags;

	public SkyDRMClassificationProfile() { this.tags = new HashMap<String, String[]>(); }
	
	public SkyDRMClassificationProfile(Map<String, String[]> tags) { this.tags = tags; }
	
	public Map<String, String[]> getTags() { return tags; }
	
	public void setTags(Map<String, String[]> tags) { this.tags = tags; }
}
