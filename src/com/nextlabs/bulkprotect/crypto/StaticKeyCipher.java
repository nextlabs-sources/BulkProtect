package com.nextlabs.bulkprotect.crypto;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StaticKeyCipher {

	private static final Logger logger = LogManager.getLogger(StaticKeyCipher.class);

	private static SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode("nrIR48Mwwurswgu5h6Jtww==".getBytes()), "AES");
	
	private static IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode("WfBDR9SUuNz77Hug38nXHg==".getBytes()));

	public static CipherOutputStream wrap(OutputStream os) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			return new CipherOutputStream(os, cipher);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			// This is hard-coded not to fail, but will log if it somehow does anyway
			logger.error("Cipher initialization failed.");
			logger.debug(e.getMessage(), e);
		}
		return null;
	}

	public static CipherInputStream wrap(InputStream is) { 
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
			return new CipherInputStream(is, cipher);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			// This is hard-coded not to fail, but will log if it somehow does anyway
			logger.error("Cipher initialization failed.");
			logger.debug(e.getMessage(), e);
		}
		return null;
	}
}
