package com.nextlabs.bulkprotect.datatype;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;

import com.nextlabs.bulkprotect.util.Hydratable;

public class HydratableFileMetadata {
	private FileObject file;
	private Hydratable<Long> lastModifiedTime;
	private Hydratable<Long> size;
	private Hydratable<FileType> type;
	
	public HydratableFileMetadata(FileObject file) {
		this.lastModifiedTime = new Hydratable<>(() -> file.getContent().getLastModifiedTime());
		this.size = new Hydratable<>(() -> file.getContent().getSize());
		this.type = new Hydratable<>(file::getType);
	}
}
