package com.nextlabs.bulkprotect.crypto.test;

import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Random {

	public static void main(String[] args) throws Exception {
		KeyGenerator keygen = KeyGenerator.getInstance("AES");
		SecretKey key = keygen.generateKey();
		
		System.out.println(key.getClass());
		System.out.println(key.getAlgorithm());
		System.out.println(new String(Base64.getEncoder().encode(key.getEncoded())));
	}

}
