package com.nextlabs.bulkprotect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextlabs.bulkprotect.crypto.StaticKeyCipher;
import com.nextlabs.bulkprotect.datatype.Configuration;
import com.nextlabs.bulkprotect.datatype.ConnectionVerificationResult;
import com.nextlabs.bulkprotect.datatype.JSONType;
import com.nextlabs.bulkprotect.task.ConnectionVerificationTask;
import com.nextlabs.bulkprotect.task.LogManagementTask;
import com.nextlabs.bulkprotect.task.SchedulingTask;
import com.nextlabs.bulkprotect.util.Result;
import com.nextlabs.bulkprotect.web.RoutingHandler;
import com.nextlabs.bulkprotect.web.authentication.StaticAuthenticator;
import com.nextlabs.nxl.NxlException;
import com.nextlabs.nxl.RightsManager;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import javax.net.ssl.*;

public class App {

	private static final Logger logger = LogManager.getLogger(App.class);

	private static final int HTTP_PORT = 80;
	private static final int HTTPS_PORT = 443;
	private static final int WS_PORT = 8081;

	// 4 Threads per logical core, hard-coded
	private static final ScheduledExecutorService es = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);
	private static final ExecutorService webpool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 128);
	private static final ObjectMapper jm = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	private static final EventFeedServer ws = new EventFeedServer(WS_PORT);
	private static Configuration cfg = new Configuration();
	private static RightsManager rm;

	private static final Path keystoreFilePath = Paths.get("", "conf", "keystore.jks");
	private static final char[] keystorePass = "password".toCharArray();

	private static final Path configFilePath = Paths.get("", "store", "config.json.encrypted").toAbsolutePath();
	private static final Path execHistoryJSONPath = Paths.get("", "store", "history.json.encrypted").toAbsolutePath();
	private static final Path execHistoryLogFilesPath = Paths.get("", "logs", "history").toAbsolutePath();
	
	private static ConnectionVerificationResult prevConnTestResult = new ConnectionVerificationResult();
	
	private static final long connVerificationFreq = 30L, taskSchedulerFreq = 3L;

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException {
		initSystemProperties();
		
		logger.debug("Loading configuration...");
		loadConfigFromJson();
		
		initRightsManager();

		initWebServers();

		es.scheduleAtFixedRate(new LogManagementTask(), 3600L - ((long) (System.currentTimeMillis() / 1000) % 3600), 3600L, TimeUnit.SECONDS);
		logger.debug("Scheduled log manager to run every hour...");
		
		es.scheduleAtFixedRate(new ConnectionVerificationTask(), 0L, connVerificationFreq, TimeUnit.MINUTES);
		logger.debug(String.format("Scheduled connection verifier to run every %d minutes...", connVerificationFreq));
	
		es.scheduleAtFixedRate(new SchedulingTask(), 0, taskSchedulerFreq, TimeUnit.MINUTES);
		logger.debug("Scheduled task scheduler to run every 3 minutes...");
	}

	public static EventFeedServer getEventServer() { return ws; }

	public static ScheduledExecutorService getExecutorService() { return es; }

	public static ObjectMapper getJsonObjectMapper() { return jm; }
	
	public static Configuration getConfiguration() { return cfg; }
	
	public static Path getConfigFilePath() { return configFilePath; }
	
	public static Path getExecHistoryJSONPath() { return execHistoryJSONPath; }
	
	public static Path getExecHistoryLogFilesPath() { return execHistoryLogFilesPath; }
	
	public static ConnectionVerificationResult getPrevConnTestResult() { return prevConnTestResult; }
	
	public static RightsManager getRightsManager() { return rm; }
	
	public static long getTaskSchedulerFreq() { return taskSchedulerFreq; }
	
	public static ExecutorService getWebThreadPool() { return webpool; }

	public static void setConfiguration(Configuration config) {
		logger.debug("Updating global configuration...");
		cfg = config;
		initRightsManager();
	}
	
	public static void setPrevConnTestResult(ConnectionVerificationResult result) { prevConnTestResult = result; }

	private static void initSystemProperties() throws IOException {
		logger.debug("Setting system properties...");
		String confPath = "/mnt/RPT_conf";
		File confDir = new File(confPath);
		if (!confDir.exists()) {
			confDir.mkdir();
		}
		File propertiesFile = new File(confDir, "jcifs.properties");
		FileInputStream fis = new FileInputStream(propertiesFile);
		Properties prop = new Properties();
		prop.load(fis);
		for (String key : prop.stringPropertyNames()) {
			if((key.equals("enable_debugging") || key.startsWith("jcifs.")) && prop.getProperty(key) != null && !prop.getProperty(key).trim().isEmpty()) {
				System.setProperty(key, prop.getProperty(key));
				logger.debug(key + "=" + prop.getProperty(key));
			}
		}
		if (System.getProperty("enable_debugging") == null) {
			System.setProperty("enable_debugging", "false");
		}
	}
	
	private static void loadConfigFromJson() throws IOException {
		if (Files.exists(configFilePath)) {
			try (FileChannel ch = FileChannel.open(configFilePath, StandardOpenOption.READ); InputStream is = StaticKeyCipher.wrap(Channels.newInputStream(ch))) {
				Result<Configuration, IOException> parseResult = JSONType.parse(is, Configuration.class);
				if (parseResult.isOk()) {
				  setConfiguration(parseResult.getValue());
				  logger.info("Successfully loaded configuration from JSON file");
//				  logger.debug(parseResult.getValue().stringify().getValue());
        }
			} catch (IOException e) {
			  logger.error("Could not parse config JSON file. The config file may be corrupted.");
				logger.debug(e.getMessage(), e);
				throw e;
			}
		}
	}
	
	private static void initRightsManager() {
		logger.debug("Initializing SkyDRM Rights Manager...");
		try {
			rm = new RightsManager(cfg.getSkyDRMCfg().getRouterUrl(), cfg.getSkyDRMCfg().getAppId(), cfg.getSkyDRMCfg().getAppKey());
		} catch (NxlException e) {
			logger.error("Failed to initialize SkyDRM Rights Manager.");
			logger.debug(e.getMessage(), e);
		}
	}

	private static void initWebServers() throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
		// If keystore file can be found in config path, start https instance
		if (Files.exists(keystoreFilePath)) {
			logger.debug(String.format("Binding HTTPS Server to port %d...", HTTPS_PORT));
			HttpsServer server = HttpsServer.create(new InetSocketAddress(HTTPS_PORT), 0);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			KeyStore ks = KeyStore.getInstance("JKS");
			try (FileChannel ch = FileChannel.open(keystoreFilePath, StandardOpenOption.READ); InputStream is = Channels.newInputStream(ch)) {
				ks.load(is, keystorePass);
			} catch (CertificateException e) { throw e; }
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, keystorePass);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
				public void configure(HttpsParameters params) {
					try {
						// Initialise the SSL context
						SSLEngine engine = sslContext.createSSLEngine();
						params.setNeedClientAuth(false);
						params.setCipherSuites(engine.getEnabledCipherSuites());
						params.setProtocols(engine.getEnabledProtocols());

						// Get the default parameters
						SSLParameters defaultSSLParameters = sslContext.getDefaultSSLParameters();
						params.setSSLParameters(defaultSSLParameters);
					} catch (Exception e) {
						logger.debug(e.getMessage(), e);
					}
				}
			});

			HttpContext ctx = server.createContext("/", new RoutingHandler());
			ctx.setAuthenticator(new StaticAuthenticator());
			server.setExecutor(webpool);
			server.start();
			logger.debug(String.format("HTTPS Server started."));

			// Initialize the event server with WSS using the same sslContext
			ws.setWebSocketFactory(new DefaultSSLWebSocketServerFactory( sslContext ));
			ws.start();
			logger.debug("Started Secure Websocket Server.");
		} else {
			logger.debug(String.format("Binding HTTP Server to port %d...", HTTP_PORT));
			HttpServer server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
			HttpContext ctx = server.createContext("/", new RoutingHandler());
			ctx.setAuthenticator(new StaticAuthenticator());
			server.setExecutor(webpool);
			server.start();
			logger.debug(String.format("HTTP Server started."));

			ws.start();
			logger.debug("Websocket server started.");
		}
	}
}
