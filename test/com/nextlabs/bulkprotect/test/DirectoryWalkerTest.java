package com.nextlabs.bulkprotect.test;

import java.util.Optional;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nextlabs.bulkprotect.filesupplier.DirectoryWalker;
import com.nextlabs.bulkprotect.util.Result;
import com.nextlabs.vfs.RepositoryFileSystemManager;
import com.nextlabs.vfs.constant.AuthType;
import com.nextlabs.vfs.constant.RepositoryType;
import com.nextlabs.vfs.dto.RepositoryCredentials;

public class DirectoryWalkerTest {
	
	private static final Logger logger = LogManager.getLogger(DirectoryWalkerTest.class);
	private static RepositoryFileSystemManager fsMgr;

	public static void main(String[] args) throws Exception {
		fsMgr = new RepositoryFileSystemManager();
		fsMgr.init();
//		RepositoryCredentials rc = new RepositoryCredentials("WORKGROUP", "theodore", "79E8110406b!", AuthType.CIFS);
//		FileObject dir = fsMgr.resolveFile("//192.168.1.64/ServerStore/jupyter", rc, RepositoryType.SHARED_FOLDER);
		RepositoryCredentials rc = new RepositoryCredentials("nextlabstest-my", "jimmy.carter@nextlabstest.onmicrosoft.com", "123next!", AuthType.SHAREPOINT_ONLINE);
		FileObject dir = fsMgr.resolveFile("https://nextlabstest-my.sharepoint.com/personal/jimmy_carter_nextlabstest_onmicrosoft_com/Documents", rc, RepositoryType.SHAREPOINT);
		DirectoryWalker dw = new DirectoryWalker(dir);
//		for (Optional<FileObject> fileOption = dw.get(); fileOption.isPresent(); fileOption = dw.get()) {
//			FileObject file = fileOption.get();
//			logger.info(file.toString());
//		}
		for (Optional<Result<FileObject, Exception>> fileResult = dw.get(); fileResult.isPresent(); fileResult = dw.get()) {
			if (fileResult.get().isErr()) { break; }
			FileObject file = fileResult.get().getValue();
			logger.info(file.toString());
		}
	}

}
