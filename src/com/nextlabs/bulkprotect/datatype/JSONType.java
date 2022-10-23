package com.nextlabs.bulkprotect.datatype;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.util.Result;

public abstract class JSONType {

	private static final Logger logger = LogManager.getLogger(JSONType.class);
	protected static final ObjectMapper mapper = App.getJsonObjectMapper();

	public static <T extends JSONType> Result<T, IOException> parse(String json, Class<T> cls) {
		try {
			return Result.ok((mapper).readValue(json, cls));
		} catch (IOException e) {
			logger.error(String.format("JSON Parsing Error. Could not parse the following JSON to a %s class instance.", cls.getName()));
			logger.error(json);
			logger.debug(e.getMessage(), e);
			return Result.err(e);
		}
	}

	public static <T extends JSONType> Result<T, IOException> parse(InputStream jsonStream, Class<T> cls) {
		try {
			return Result.ok((mapper).readValue(jsonStream, cls));
		} catch (IOException e) {
			logger.error(String.format("JSON Parsing Error. Could not parse JSONStream to a %s class instance.", cls.getName()));
			logger.debug(e.getMessage(), e);
			return Result.err(e);
		}
	}

	public Result<String, JsonProcessingException> stringify() {
		try {
			return Result.ok((mapper).writeValueAsString(this));
		} catch (JsonProcessingException e) {
			logger.error(String.format("Could not convert the following object to JSON: %s", this));
			logger.debug(e.getMessage(), e);
			return Result.err(e);
		}
	}
	
	public Result<? extends JSONType, ? extends Exception> copy() {
		Result<String, JsonProcessingException> jsonString = this.stringify();
		return jsonString.isOk() ? JSONType.parse(jsonString.getValue(), this.getClass()) : Result.err(jsonString.getError());
	}
}