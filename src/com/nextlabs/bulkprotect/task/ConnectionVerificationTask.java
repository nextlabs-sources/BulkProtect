package com.nextlabs.bulkprotect.task;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.EventFeedServer;
import com.nextlabs.bulkprotect.datatype.Configuration;
import com.nextlabs.bulkprotect.datatype.ConnectionVerificationResult;
import com.nextlabs.bulkprotect.datatype.Rule;
import com.nextlabs.bulkprotect.datatype.SkyDRMConfiguration;
import com.nextlabs.bulkprotect.util.Result;
import com.nextlabs.nxl.NxlException;
import com.nextlabs.nxl.RightsManager;

public class ConnectionVerificationTask extends AbstractTask<ConnectionVerificationResult, Exception> {
	
	private static final Logger logger = LogManager.getLogger(ConnectionVerificationTask.class);
	
	private static final EventFeedServer ws = App.getEventServer();
	
	private static final RightsManager rm = App.getRightsManager();
	
	Configuration cfg;
	ConnectionVerificationResult result;

	@Override
	public void run() {
		try {
			long startTime = System.currentTimeMillis();
			logger.debug("Verifying connections...");

			cfg = App.getConfiguration();
			result = new ConnectionVerificationResult();

			for (Rule rule : cfg.getRules()) {
				logger.debug(String.format("Testing connection to %s...", rule.getFullDirectory()));
				Result<Void, FileSystemException> res = rule.testSubdirectory();
				logger.debug(String.format("Adding connection result to %s...", rule.getFullDirectory()));
				result.addResult(result.new RuleConnectionResult(rule.getFullDirectory(), rule.getName(), res.isOk()));
			}
			result.setSkydrmConnection(testSkyDRMConnection());
			result.setVerificationTime(System.currentTimeMillis());

			ws.broadcastData(result);
			set(result);

			App.setPrevConnTestResult(result);

			logger.debug(String.format("Connection Verification Task completed in %d ms.", System.currentTimeMillis() - startTime));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	private boolean testSkyDRMConnection() {
		SkyDRMConfiguration config = cfg.getSkyDRMCfg();
		try {
			RightsManager manager = new RightsManager(config.getRouterUrl(), config.getAppId(), config.getAppKey());
			manager.listProjects(true, config.getSystemBucketId());
		} catch (NxlException e) {
			logger.error("Failed to connect to SkyDRM!");
			logger.debug(e.getMessage(), e);
			return false;
		}
		return true;
	}
}
