package com.nextlabs.bulkprotect.web.authentication;

import com.sun.net.httpserver.BasicAuthenticator;

public class StaticAuthenticator extends BasicAuthenticator {

	public StaticAuthenticator() { super(""); }

	@Override
	public boolean checkCredentials(String username, String password) {
		return username.equalsIgnoreCase("administrator") && password.equals("123next!");
	}
}
