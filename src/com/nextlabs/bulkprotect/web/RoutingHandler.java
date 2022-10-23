package com.nextlabs.bulkprotect.web;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nextlabs.bulkprotect.App;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RoutingHandler implements HttpHandler {
	
	private static final ExecutorService es = App.getWebThreadPool();
	private static final Logger logger = LogManager.getLogger(RoutingHandler.class);
	
	private static final String staticContentPath = "./web/dist";

	@Override
	public void handle(HttpExchange t) throws IOException {
		URI uri = t.getRequestURI();
		String path = uri.getPath();
		String method = t.getRequestMethod();
		logger.debug(String.format("%s %s %s",t.getRemoteAddress(), method, path));
		String[] pathSplit = path.split("/");
		es.submit(() -> {
			try {
				switch(pathSplit.length) {
					case 1: (new StaticContentHandler(staticContentPath)).handle(t); break;
					case 2: 
						switch(pathSplit[1]) {
							case "execute": (new ExecuteHandler()).handle(t); break;
							case "config": (new ConfigHandler()).handle(t); break;
							case "clskeys": (new ClassificationKeysHandler()).handle(t); break;
							case "conntest": (new ConnectionResultHandler()).handle(t); break;
							default: (new StaticContentHandler(staticContentPath)).handle(t); break;
						} break;
					case 3: 
						switch(pathSplit[1]) {
							case "history": 
								switch (pathSplit[2]) {
									case "all": (new ExecHistoryHandler()).handle(t); break;
									default: (new StaticContentHandler("./logs")).handle(t); break;
								}
								break;
							default: (new StaticContentHandler(staticContentPath)).handle(t); break;
						} break;
					default: (new StaticContentHandler(staticContentPath)).handle(t); break;
				}
			} catch (IOException e) {
				logger.error("HTTPHandler Error");
				logger.debug(e.getMessage(), e);
			}
		});
	}
}
