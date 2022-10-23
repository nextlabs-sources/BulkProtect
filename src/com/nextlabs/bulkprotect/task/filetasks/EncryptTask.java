package com.nextlabs.bulkprotect.task.filetasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import com.nextlabs.bulkprotect.datatype.Configuration;

import com.nextlabs.vfs.constant.RepositoryType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileUtil;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.UnexpectedPropertySetTypeException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;


import com.nextlabs.bulkprotect.App;
import com.nextlabs.bulkprotect.datatype.SkyDRMConfiguration;
import com.nextlabs.bulkprotect.datatype.actionparams.EncryptActionParams;
import com.nextlabs.common.io.IOUtils;
import com.nextlabs.common.shared.Constants.TokenGroupType;
import com.nextlabs.jtagger.Tagger;
import com.nextlabs.jtagger.TaggerFactory;
import com.nextlabs.nxl.NxlException;
import com.nextlabs.nxl.Rights;
import com.nextlabs.nxl.RightsManager;

public class EncryptTask extends AbstractFileTask<Long, Exception> {

	private static final Logger logger = LogManager.getLogger(EncryptTask.class);
	private final static String NXL_IDENTIFIER="NXLFMT@";
	protected FileObject target;
	protected EncryptActionParams params;
	private String rootDir;

	public EncryptTask(FileObject target, EncryptActionParams params, String rootDir) {
		super(target, params);
		this.target = target;
		this.params = params;
		this.rootDir = rootDir;
//		backup();
	}

	@Override
	protected Long process() throws NxlException, IOException {
		long size = 0L;

		try {
			size = target.getContent().getSize();
			if(isProtectedFile(target))
			{
				logger.debug(String.format("This File is a protected file %s skipping protection for the file", target.toString()));
				return -1L;
			}
		} catch (Exception e) {
			logger.debug(String.format("Failed to get file size for %s", target.toString()));
			logger.debug(e.getMessage(), e);
		}

		SkyDRMConfiguration config = App.getConfiguration().getSkyDRMCfg();
		logger.debug(String.format("Starting Encryption for %s", target.toString()));
		Rights[] rights = Rights.fromStrings(new String[] {});
		Map<String, String[]> tagMap = params.getTags();
		tagMap.putAll(parseMetadataFromDirPath(rootDir, target.toString(), params));
		if (params.isTransferMetadata()) transferTags(tagMap);

		RightsManager manager = App.getRightsManager();

		String tempDirPath = "/mnt/temp";
	    File directory = new File(tempDirPath);
	    if (! directory.exists()){
	        directory.mkdir();
	    }
	    
	    String uuid = UUID.randomUUID().toString();
	    String tempFilePath = tempDirPath + "/" + uuid + "_" + getEncryptedFileName();
	    FileObject encryptedFile = VFS.getManager().resolveFile(tempFilePath);
	    
		long fileSize = target.getContent().getSize();
		try (InputStream is = target.getContent().getInputStream()) {
			try (OutputStream os = encryptedFile.getContent().getOutputStream()) {
				manager.encryptStream(is, os, getEncryptedFileName(), fileSize, null, rights, tagMap, config.getSystemBucketId(), TokenGroupType.TOKENGROUP_SYSTEMBUCKET);
				os.close();
			} catch (Exception e) {
				logger.error("Encryption failed. Deleting the nxl file...");
				encryptedFile.delete();
				encryptedFile.close();
				throw e;
			}
			is.close();
		}

		try {
			if (params.getRepoType() == RepositoryType.SHAREPOINT) {
				// copy temp file's content back to original file and delete temp file
				FileUtil.copyContent(encryptedFile, target);
				encryptedFile.delete();
				// rename original file if needed
				if (!getEncryptedFileName().equals(getFileName())) {
					FileObject renamedFile = target.getParent().resolveFile(getEncryptedFileName());
					target.moveTo(renamedFile);
				}
			} else {
				if (!params.isKeepOriginal()) deleteTargetFile();
				FileObject finalFile = target.getParent().resolveFile(getEncryptedFileName());
				encryptedFile.moveTo(finalFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.debug(String.format("Encryption for %s completed.", target.toString()));

		return size;
	}

	private String getFileName() {
		String[] targetFileDirSplit = target.toString().split("/");
		return targetFileDirSplit[targetFileDirSplit.length - 1];
	}

	private String getEncryptedFileName() {
		String[] targetFileDirSplit = target.toString().split("/");
		String targetFileName = targetFileDirSplit[targetFileDirSplit.length - 1];
		if (App.getConfiguration().getRuleParams().isHideNxlExt()) {
			return targetFileName;
		} else {
			return targetFileName + ".nxl";
		}
	}
	
	private void transferTags(Map<String, String[]> tagMap) throws IOException {
		Configuration.RuleParams cfg = App.getConfiguration().getRuleParams();
		try (InputStream is = target.getContent().getInputStream()) {
			Tagger tagger = TaggerFactory.getTagger(target.toString(), is);
			if (tagger != null) {
				tagger.getAllTags().forEach((k, v) -> {
					if (cfg.getClsKeys().length == 0 || Arrays.stream(cfg.getClsKeys()).anyMatch(k::equalsIgnoreCase)) {
						logger.info(String.format("Transfering file metadata for file %s: %s %s", target.toString(), k, v.toString()));
						Optional<String> delimiter = Arrays.stream(cfg.getDelimiters()).filter(d -> Pattern.compile(d).matcher(v.toString()).find()).findFirst();
						logger.debug(delimiter.isPresent() ? String.format("Using delimiter '%s'", delimiter.get()) : "Delimiter not found. Transferring value as a single string.");
						tagMap.put(k, cfg.isUseDelimiter() && delimiter.isPresent() ? v.toString().split(delimiter.get()) : new String[] { v.toString() });
					} else {
						logger.info(String.format("Skipping file metadata for file %s: %s %s. Classification key was not included in selection criteria.", target.toString(), k, v.toString()));
					}
				});
			}
		} catch (OpenXML4JException | XmlException | NoPropertySetStreamException | MarkUnsupportedException | UnexpectedPropertySetTypeException e) {
			logger.error("Failed to transfer tags to nxl file.");
			logger.debug(e.getMessage(), e);
		}
	}
	
    public static boolean isProtectedFile(FileObject fo) {
        if (fo.toString().endsWith(".nxl")) {
            return true;
        }
        InputStream input = null;
        try {
            input =  fo.getContent().getInputStream();
            byte[] bytes =null;
            bytes = new byte[7];
            input.read(bytes);
            String headerString = new String(bytes, StandardCharsets.UTF_8);
            if (NXL_IDENTIFIER.equalsIgnoreCase(headerString)) {
                    return true;
                }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(input);
        }
        return false;
    }
    
    public static Map<String, String[]> parseMetadataFromDirPath(String rootDir, String dirPath, EncryptActionParams params) {
        Map<String, String[]> attrMap = new HashMap<>();
        String[] pathSplit = (dirPath.substring(0, dirPath.lastIndexOf("/")+1) + "*").toLowerCase().split("(?=[^A-Za-z0-9_*])");
        String[] patternSplit = params.getFilePatternSplit();
        for (int i = 0; i < patternSplit.length; i++) {
        	if (!patternSplit[i].contains("skydrm_")) {
        		if (!patternSplit[i].equals(pathSplit[i])) {
                    break;
                }
        	} else {
        		if (!pathSplit[i].matches(patternSplit[i].charAt(0) + "[A-Za-z0-9]+")) {
                    break;
                } else {
                	attrMap.put(patternSplit[i].split("skydrm_")[1], new String[] { pathSplit[i].substring(1, pathSplit[i].length()) });
                }
        	}
        }
    	return attrMap;
    }
    
	private void deleteTargetFile() {
		try {
			target.delete();
		} catch (Exception e) {
			logger.error(String.format("Failed to delete file: %s", target.toString()));
			logger.debug(e.getMessage(), e);
		}
	}
}
