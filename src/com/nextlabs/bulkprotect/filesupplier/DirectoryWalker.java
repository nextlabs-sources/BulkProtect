package com.nextlabs.bulkprotect.filesupplier;

import java.util.*;
import java.util.function.Supplier;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nextlabs.bulkprotect.util.Result;

public class DirectoryWalker implements Supplier<Optional<Result<FileObject, Exception>>> {

	private static final Logger logger = LogManager.getLogger(DirectoryWalker.class);
	private String rootName;
	Queue<FileObject> fileQueue;

	public DirectoryWalker(FileObject root) throws FileSystemException {
		logger.debug("DirectoryWalker constructor begins");
		FileType type = null;
		try {
			 type = root.getType();
		} catch (Throwable t) {
			throw t;
		}
		if (type != FileType.FOLDER) {
			throw new FileSystemException("Provided root folder failed to connect or was not a directory!");
		} else {
			rootName = root.toString();
			fileQueue = new ArrayDeque<FileObject>();
			fileQueue.addAll(Arrays.asList(root.getChildren()));
		}
		logger.debug("DirectoryWalker constructor ends");
	}

	@Override
	public Optional<Result<FileObject, Exception>> get() {
		while (!fileQueue.isEmpty()) {
			FileObject child = fileQueue.poll();
			try {
				FileType type = child.getType();
				if (type == FileType.FILE) return Optional.of(Result.ok(child));
				else if (type == FileType.FOLDER) fileQueue.addAll(Arrays.asList(child.getChildren()));
			} catch (FileSystemException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return Optional.empty();
	}

	@Override
	public String toString() { return rootName; }
}
