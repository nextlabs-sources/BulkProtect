package com.nextlabs.bulkprotect.web;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nextlabs.bulkprotect.App;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ConnectionResultHandler implements HttpHandler {
	
	public static final Logger logger = LogManager.getLogger(ConnectionResultHandler.class);

	@Override
	public void handle(HttpExchange t) throws IOException {
		t.getResponseHeaders().set("Content-Type", "application/json");
		try (OutputStream os = t.getResponseBody()) {
			String resp = App.getPrevConnTestResult().stringify().getValue();
			t.sendResponseHeaders(200, resp.length());
			os.write(resp.getBytes());
		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
		}
	}
}
