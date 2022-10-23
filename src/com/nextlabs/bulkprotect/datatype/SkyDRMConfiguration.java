package com.nextlabs.bulkprotect.datatype;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class SkyDRMConfiguration extends JSONType {
	String routerUrl;
	int appId;
	String appKey;
	String systemBucketId;
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public SkyDRMConfiguration() {
		routerUrl = "https://example-skydrm.com/router";
		appId = 1;
		appKey = "0123456789ABCDEF0123456789ABCDEF";
		systemBucketId = "0123456789ABCDEF0123456789ABCDEF_system";
	}

	public String getRouterUrl() { return routerUrl; }

	public int getAppId() { return appId; }

	public String getAppKey() { return appKey; }

	public String getSystemBucketId() { return systemBucketId; }

	public void setRouterUrl(String routerUrl) { this.routerUrl = routerUrl; }

	public void setAppId(int appId) { this.appId = appId; }

	public void setAppKey(String appKey) { this.appKey = appKey; }

	public void setSystemBucketId(String systemBucketId) { this.systemBucketId = systemBucketId; }
}