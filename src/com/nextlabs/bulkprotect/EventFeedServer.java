package com.nextlabs.bulkprotect;

import java.net.InetSocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nextlabs.bulkprotect.datatype.JSONType;

public class EventFeedServer extends WebSocketServer {

	private static final Logger logger = LogManager.getLogger(EventFeedServer.class);
	private static ObjectMapper jm = App.getJsonObjectMapper();

	public EventFeedServer(int port) {
		super(new InetSocketAddress(port));
		logger.debug(String.format("Binding WebSocket server to port %d", port));
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) { logger.debug(String.format("%s has disconnected.", conn.toString())); }

	@Override
	public void onError(WebSocket conn, Exception e) {
		logger.debug(String.format("%s has thrown an error", conn));
		logger.debug(e.getMessage(), e);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		// ignore
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) { logger.debug(String.format("New websocket connection: %s", handshake.getResourceDescriptor())); }

	@Override
	public void onStart() { logger.debug("Successfully started websocket server!"); }
	
	@Override
	public void broadcast(String s) {
		super.broadcast(s);
//		logger.debug(String.format("Broadcasting WebSocket Message: %s",s));
	}

	public void broadcastData(JSONType dataObj) {
		ObjectNode node = jm.createObjectNode();
		node.put("msgType", "data");
		node.set("data", jm.valueToTree(dataObj));
		node.put("type", dataObj.getClass().getSimpleName());
		broadcast(node.toString());
	}

	public void broadcastMessage(String message) { broadcast("message", message); }

	public void broadcastSuccess(String message) { broadcast("success", message); }

	public void broadcastWarning(String message) { broadcast("warning", message); }

	public void broadcastInfo(String message) { broadcast("info", message); }

	public void broadcastError(String message) { broadcast("error", message); }

	public void broadcastLoading(String message) { broadcast("loading", message); }

	private void broadcast(String messageType, String message) {
		ObjectNode node = jm.createObjectNode();
		node.put("msgType", messageType);
		node.put("msg", message);
		broadcast(node.toString());
	}
}
