package com.nextlabs.bulkprotect.task.filetasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileUtil;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.datatype.SkyDRMConfiguration;
import com.nextlabs.bulkprotect.datatype.actionparams.DecryptActionParams;
import com.nextlabs.common.shared.Constants.TokenGroupType;
import com.nextlabs.nxl.NxlException;
import com.nextlabs.nxl.NxlFile;
import com.nextlabs.nxl.RightsManager;

public class DecryptTask extends AbstractFileTask<Long, Exception> {
	
	private static final Logger logger = LogManager.getLogger(DecryptTask.class);
	protected FileObject target;

	public DecryptTask(FileObject target, DecryptActionParams params) {
		super(target, params);
		this.target = target;
//		backup();
	}

	@Override
	protected Long process() throws NxlException, IOException {
		long size = 0L;
		
		try {
			size = target.getContent().getSize();
			if(!EncryptTask.isProtectedFile(target))
			{
				logger.debug(String.format("This File is not a protected file %s skipping decryption for the file", target.toString()));
				return size;
			}
		} catch (Exception e) {
			logger.debug(String.format("Failed to get file size for %s", target.toString()));
			logger.debug(e.getMessage(), e);
		}
		
		SkyDRMConfiguration config = App.getConfiguration().getSkyDRMCfg();
		logger.debug(String.format("Starting Decryption for %s", target.toString()));
		
		RightsManager manager = App.getRightsManager();
		
		String tempDirPath = "/mnt/temp";
	    File directory = new File(tempDirPath);
	    if (! directory.exists()){
	        directory.mkdir();
	    }
	    
	    String uuid = UUID.randomUUID().toString();
	    String tempFilePath = tempDirPath + "/" + uuid + "_" + getDecryptedFileName();
	    FileObject decryptedFile = VFS.getManager().resolveFile(tempFilePath);
		
		long originalContentLength;
		try (InputStream is = target.getContent().getInputStream()) {
			originalContentLength = manager.getOriginalContentLength(NxlFile.parse(is));
		}
		logger.debug(String.format("Original File Size: %d", originalContentLength));
		try (InputStream is = target.getContent().getInputStream()) {
			try (OutputStream os = decryptedFile.getContent().getOutputStream()) {
				manager.decryptPartial(is, os, null, 0L, originalContentLength, config.getSystemBucketId(), TokenGroupType.TOKENGROUP_SYSTEMBUCKET);
			}
		}
		
		try {
			// copy temp file's content back to original file and delete temp file
			FileUtil.copyContent(decryptedFile, target);
			decryptedFile.delete();
			// rename original file if needed
			if (!getDecryptedFileName().equals(getFileName())) {
				FileObject renamedFile = target.getParent().resolveFile(getDecryptedFileName());
				target.moveTo(renamedFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.debug(String.format("Decryption for %s completed.", target.toString()));
		return size;
	}

	private String getFileName() {
		String[] targetFileDirSplit = target.toString().split("/");
		return targetFileDirSplit[targetFileDirSplit.length - 1];
	}
	
	private String getDecryptedFileName() {
		// TODO: get decrypted file name properly
		String[] targetFileDirSplit = target.toString().split("/");
		String targetFileName = targetFileDirSplit[targetFileDirSplit.length-1];
		return targetFileName.endsWith(".nxl") ? targetFileName.substring(0, targetFileName.length()-4) : targetFileName;
	}
}
