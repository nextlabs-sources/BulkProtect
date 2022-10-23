package com.nextlabs.bulkprotect.task.filetasks;

import java.util.Random;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import com.nextlabs.bulkprotect.datatype.ActionParams;

public class DummyTask extends AbstractFileTask<Void, InterruptedException> {

	public DummyTask(FileObject target, ActionParams params) {
		super(target, null);
	}

	@Override
	protected Void process() throws FileSystemException {
		try {
			Random r = new Random();
			Thread.sleep((long) r.nextInt(10000));
			if (r.nextInt(10000) < 5000) throw new InterruptedException("Dummy task simulated failure.");
		} catch (InterruptedException e) {
			setError(e);
			return null;
		}
		return null;
	}
}
