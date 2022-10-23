package com.nextlabs.bulkprotect.web;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.datatype.SkyDRMClassificationProfile;
import com.nextlabs.bulkprotect.datatype.SkyDRMConfiguration;
import com.nextlabs.common.shared.Constants.TokenGroupType;
import com.nextlabs.nxl.NxlException;
import com.nextlabs.nxl.RightsManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ClassificationKeysHandler implements HttpHandler {
	
	private static final Logger logger = LogManager.getLogger(ClassificationKeysHandler.class);

	@Override
	public void handle(HttpExchange t) throws IOException { 
		try (OutputStream os = t.getResponseBody()) {
			String resp = getClassificationProfile().stringify().getValue();
			logger.debug(resp);
			t.sendResponseHeaders(200, resp.length());
			os.write(resp.getBytes());
		}
	}
	
	private SkyDRMClassificationProfile getClassificationProfile() {
		RightsManager rm = App.getRightsManager();
		SkyDRMConfiguration cfg = App.getConfiguration().getSkyDRMCfg();
		try {
			return new SkyDRMClassificationProfile(rm.getClassification(cfg.getSystemBucketId(), TokenGroupType.TOKENGROUP_SYSTEMBUCKET, null));
		} catch (NxlException e) {
			logger.error("Failed to retrieve classification profile from SkyDRM");
			logger.debug(e.getMessage(), e);
			return new SkyDRMClassificationProfile();
		}
	}

}
