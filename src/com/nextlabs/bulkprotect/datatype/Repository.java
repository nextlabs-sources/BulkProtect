package com.nextlabs.bulkprotect.datatype;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nextlabs.vfs.constant.AuthType;
import com.nextlabs.vfs.constant.RepositoryType;
import com.nextlabs.vfs.dto.RepositoryCredentials;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository extends JSONType {
	String repoName, rootUrl, domain, username, password, id;
	RepositoryType repoType;
	AuthType authType;
	
	public Repository() {}

	public String getRepoName() { return repoName; }

	public String getRootUrl() { return rootUrl; }

	public String getDomain() { return domain; }

	public String getUsername() { return username; }

	public String getPassword() { return password; }
	
	public String getId() { return id == null ? UUID.randomUUID().toString() : id; }

	public RepositoryType getRepoType() { return repoType; }

	public AuthType getAuthType() { return authType; }

	public void setRepoName(String repoName) { this.repoName = repoName; }

	public void setRootUrl(String rootUrl) { this.rootUrl = rootUrl; }

	public void setDomain(String domain) { this.domain = domain; }

	public void setUsername(String username) { this.username = username; }

	public void setPassword(String password) { this.password = password; }

	public void setRepoType(RepositoryType repoType) { this.repoType = repoType; }

	public void setAuthType(AuthType authType) { this.authType = authType; }
	
	public void setId (String id) { this.id = id == null ? UUID.randomUUID().toString() : id; }
	
	@JsonIgnore
	public RepositoryCredentials getRepositoryCredentials() { return new RepositoryCredentials(domain, username, password, authType); }
//	
//	@JsonIgnore
//	public String getValidRootUrl(String rootUrl) {
//		
//	}
}
