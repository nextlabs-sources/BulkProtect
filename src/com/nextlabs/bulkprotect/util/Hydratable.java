package com.nextlabs.bulkprotect.util;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Hydratable<T> {
	
	private static final Logger logger = LogManager.getLogger(Hydratable.class);
	
	private Optional<T> value;
	private Long lastUpdateTime;
	private Retriever<? extends T> retriever;

	public Hydratable(Retriever<? extends T> retriever) {
		unset();
		this.retriever = retriever;
	}

	public Hydratable(T initialValue, Retriever<? extends T> retriever) {
		set(initialValue);
		this.retriever = retriever;
	}

	public Optional<T> get() { return value; }
	
	public T getHydrated() throws Exception {
		if (value.isEmpty()) {
			Result<T, ? extends Exception> res = hydrate();
			if (res.isOk()) return res.getValue();
			else throw res.getError();
		} else {
			return value.get();
		}
	}
	
	public T getHydrated(long updateTimeout) throws Exception {
		if ((System.currentTimeMillis() - updateTimeout) <= updateTimeout) return getHydrated();
		else {
			Result<T, ? extends Exception> res = hydrate();
			if (res.isOk()) return res.getValue();
			else throw res.getError();
		}
	}

	public void set(T value) {
		this.value = Optional.of(value);
		this.lastUpdateTime = System.currentTimeMillis();
	}

	public Long getLastUpdateTime() { return lastUpdateTime; }

	public void unset() {
		this.value = Optional.empty();
		this.lastUpdateTime = null;
	}
	
	public Result<T, ? extends Exception> hydrate() {
		try {
			T val = this.retriever.retrieve();
			set(val);
			return Result.ok(val);
		} catch (Exception e) {
			logger.error("Failed to retrieve hydratable value");
			logger.debug(e.getMessage(), e);
			return Result.err(e);
		}
	}
}
