package com.nextlabs.bulkprotect.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.crypto.StaticKeyCipher;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ExecHistoryHandler implements HttpHandler {
	
	private static final Logger logger = LogManager.getLogger(ExecHistoryHandler.class);
	
	private static final Path resultsFilePath = App.getExecHistoryJSONPath();

	@Override
	public void handle(HttpExchange t) throws IOException {
		try (OutputStream os = t.getResponseBody()) {
			if (Files.exists(resultsFilePath)) {
				t.sendResponseHeaders(200, 0);
				try(FileChannel ch = FileChannel.open(resultsFilePath, StandardOpenOption.READ); InputStream is = StaticKeyCipher.wrap(Channels.newInputStream(ch))) {
					final byte[] buffer = new byte[0x10000];
					for (int count = is.read(buffer); count >= 0; count = is.read(buffer)) {
						os.write(buffer, 0, count);
					}
				}
			} else {
				String resp = "[]";
				t.sendResponseHeaders(200, resp.length());
				os.write(resp.getBytes());
			}
		}
	}
}
