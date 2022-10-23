package com.nextlabs.bulkprotect.util;

public interface Retriever<T> {
	
	public T retrieve() throws Exception;

}
