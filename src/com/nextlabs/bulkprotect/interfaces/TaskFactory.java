package com.nextlabs.bulkprotect.interfaces;

import com.nextlabs.bulkprotect.task.AbstractTask;
import com.nextlabs.bulkprotect.util.Result;

public interface TaskFactory<T extends Result<AbstractTask<?, ? extends Throwable>, Exception>> {

	public T createNewTask();
	
}
