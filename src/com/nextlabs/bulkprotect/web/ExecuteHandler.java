package com.nextlabs.bulkprotect.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.EventFeedServer;
import com.nextlabs.bulkprotect.datatype.JSONType;
import com.nextlabs.bulkprotect.datatype.Rule;
import com.nextlabs.bulkprotect.task.FileSetTask;
import com.nextlabs.bulkprotect.util.Result;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ExecuteHandler implements HttpHandler {
	
	private static final ScheduledExecutorService es = App.getExecutorService();
	private static final EventFeedServer ws = App.getEventServer();
	
	private static final Logger logger = LogManager.getLogger(ExecuteHandler.class);

	@Override
	public void handle(HttpExchange t) throws IOException {
		try (InputStream is = t.getRequestBody();) {
			Result<Rule, IOException> ruleResult = JSONType.parse(is, Rule.class);
			if (ruleResult.isOk()) {
				ws.broadcastLoading("Initializing...");
				Rule rule = ruleResult.getValue();
//				logger.debug(rule.stringify().getValue());
				Result<FileSetTask, Exception> dt = rule.createNewTask();
				if (dt.isOk()) es.submit(dt.getValue());
			} else {
				logger.error("Rule JSON was invalid.");
				logger.debug(ruleResult.getError().getMessage(), ruleResult.getError());
			}
		}
		
		t.sendResponseHeaders(200, 0);
		t.getResponseBody().close();
	}
}