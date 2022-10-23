package com.nextlabs.bulkprotect.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.EventFeedServer;
import com.nextlabs.bulkprotect.crypto.StaticKeyCipher;
import com.nextlabs.bulkprotect.datatype.Configuration;
import com.nextlabs.bulkprotect.datatype.JSONType;
import com.nextlabs.bulkprotect.util.Result;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ConfigHandler implements HttpHandler {
	
	private static final Logger logger = LogManager.getLogger(ConfigHandler.class);
	private static final EventFeedServer ws = App.getEventServer();
	private static Path configFilePath = App.getConfigFilePath();

	@Override
	public void handle(HttpExchange t) throws IOException {
		String method = t.getRequestMethod();
		if (method.equals("GET")) handleGet(t);
		else if (method.equals("POST")) handlePost(t);
	}
	
	private void handleGet(HttpExchange t) throws IOException {
		t.getResponseHeaders().set("Content-Type", "application/json");
		try (OutputStream os = t.getResponseBody()) {
			if (Files.exists(configFilePath)) {
				String configJson = App.getConfiguration().stringify().getValue();
				t.sendResponseHeaders(200, configJson.length());
				os.write(configJson.getBytes());
			} else {
				String resp = (new Configuration()).stringify().getValue();
				t.sendResponseHeaders(200, resp.length());
				os.write(resp.getBytes());
			}
		}
	}
	
	private void handlePost(HttpExchange t) throws IOException {
		boolean success = false;
		Result<Configuration, IOException> config;
		
		try (InputStream is = t.getRequestBody();) { config = JSONType.parse(is, Configuration.class); }
		
		if (config != null && config.isOk() && config.getValue().validate()) {
			// Create the file if it doesn't exist
			if (!Files.exists(configFilePath)) Files.createFile(configFilePath);
			Configuration oldConfig = App.getConfiguration();
			App.setConfiguration(config.getValue());
			// This is a HttpHandler, so acquire lock on config.json for concurrent file access
			try(FileChannel ch = FileChannel.open(configFilePath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING); 
					OutputStream os = StaticKeyCipher.wrap(Channels.newOutputStream(ch)); FileLock lock = ch.lock();) {
				String configJson = config.getValue().stringify().getValue();
				os.write(configJson.getBytes());
				logger.debug("Successfully saved configuration to file.");
				ws.broadcastSuccess("Configuration saved.");
				success = true;
			} catch (IOException e) {
				logger.debug(e.getMessage(), e);
				App.setConfiguration(oldConfig);
			}
		}
		if (success) { 
			t.sendResponseHeaders(200, 0);
			t.getResponseBody().close();
		} else {
			String resp = "500 Internal Server Error";
			t.sendResponseHeaders(500, resp.length());
			try (OutputStream os = t.getResponseBody()) {os.write(resp.getBytes()); }
		}
	}
}
