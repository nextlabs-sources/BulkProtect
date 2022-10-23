package com.nextlabs.bulkprotect.task;

import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.EventFeedServer;
import com.nextlabs.bulkprotect.util.Result;

public abstract class AbstractTask<T, E extends Throwable> implements RunnableFuture<Result<T, E>> {

	private static final Logger logger = LogManager.getLogger(AbstractTask.class);
	protected static final EventFeedServer ws = App.getEventServer();
	protected static final ScheduledExecutorService es = App.getExecutorService();

	private T result;
	private E error;
	private boolean done = false;

	protected void set(T result) {
		this.result = result;
		done = true;
	}

	protected void setError(E error) {
		this.error = error;
		done = true;
	}

	public AbstractTask() {}

	// No cancellation mechanism by default, but subclasses can implement
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) { return false; }

	@Override
	public boolean isCancelled() { return false; }

	@Override
	public boolean isDone() { return done; }

	@Override
	public Result<T, E> get() {
		while (!done) {
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				return Result.err(error);
			}
		}
		return error == null ? Result.ok(result) : Result.err(error);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result<T, E> get(long timeout, TimeUnit unit) {
		long timeoutTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeout, unit);
		while (!done && System.currentTimeMillis() < timeoutTime) {
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				return Result.err(error);
			}
		}
		if (!done) setError((E) new TimeoutException("Timeout exceeded."));
		return error == null ? Result.ok(result) : Result.err(error);
	}
}
