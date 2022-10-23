package com.nextlabs.bulkprotect.util;

public class Result<T, E extends Throwable> {

	private T value;
	private E err;

	public static <T, E extends Throwable> Result<T, E> ok(T value) { return new Result<T, E>(value); }

	public static <T, E extends Throwable> Result<T, E> err(E err) { return new Result<T, E>(err); }

	private Result(T v) { this.value = v; }

	private Result(E e) { this.err = e; }

	public T getValue() { return value; }

	public E getError() { return err; }

	public boolean isOk() { return this.err == null; }

	public boolean isErr() { return this.err != null; }
}
