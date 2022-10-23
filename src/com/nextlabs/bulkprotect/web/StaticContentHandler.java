package com.nextlabs.bulkprotect.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StaticContentHandler implements HttpHandler {
	
	private static final Logger logger = LogManager.getLogger(StaticContentHandler.class);
	protected String root;
	
	public StaticContentHandler(String root) { this.root = root; }

	@Override
	public void handle(HttpExchange t) throws IOException {
		URI uri = t.getRequestURI();
		String path = uri.getPath();
		String method = t.getRequestMethod();
		if (path.equals("/")) path = "/index.html";
		File file = new File(root+path).getCanonicalFile();
			
		try (OutputStream os = t.getResponseBody()) {
			if (!file.isFile()) {
				String resp = "404 (Not Found)\n";
				t.sendResponseHeaders(404,  resp.length());
				
				os.write(resp.getBytes());
			} else {
				Headers h = t.getResponseHeaders();
				String mime = getMIMEType(path);
				if (mime != null) h.set("Content-Type", mime);
				t.sendResponseHeaders(200, 0);
				try (FileInputStream fs = new FileInputStream(file)) {
					final byte[] buffer = new byte[0x10000];
					int count = 0;
					while ((count = fs.read(buffer)) >= 0) {
						os.write(buffer, 0, count);
					}
				}
			}
		}
	}
	
	private String getMIMEType(String s) {
		String[] sp = s.split("\\.");
		switch (sp[sp.length-1]) {
			case "html": return "text/html";
			case "css": return "text/css";
			case "js": return "application/javascript";
			case "ico": return "image/x-icon";
			case "json": return "application/json";
			default: return null;
		}
	}
}