package com.nextlabs.bulkprotect.task.test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.vfs2.FileObject;

import com.nextlabs.bulkprotect.task.FileSetTask;
import com.nextlabs.bulkprotect.task.filetasks.DummyTask;
import com.nextlabs.vfs.RepositoryFileSystemManager;
import com.nextlabs.vfs.constant.AuthType;
import com.nextlabs.vfs.constant.RepositoryType;
import com.nextlabs.vfs.dto.RepositoryCredentials;

public class DirectoryTaskTest {
	
	private static final ScheduledExecutorService es = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors()*4);
	private static RepositoryFileSystemManager fsMgr;	
	
	public static void main(String[] args) throws Exception {
		fsMgr = new RepositoryFileSystemManager();
		fsMgr.init();
		RepositoryCredentials rc = new RepositoryCredentials("nextlabs", "thlee", "nextlabs3!", AuthType.CIFS);
		FileObject dir = fsMgr.resolveFile("//semakau/share/Users", rc, RepositoryType.SHARED_FOLDER);
//		RepositoryCredentials rc = new RepositoryCredentials("WORKGROUP", "theodore", "79E8110406b!", AuthType.CIFS);
//		FileObject dir = fsMgr.resolveFile("//192.168.1.64/ServerStore/jupyter", rc, RepositoryType.SHARED_FOLDER);
//		FileSetTask dt = new FileSetTask(dir, DummyTask.class);
//		es.submit(dt);
	}
}
