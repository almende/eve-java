/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Utility to encrypt text/passwords
 * 
 * WARNING: NOT SAFE TO USE IN A PRODUCTION ENVIRONMENT!
 * Why? Well, currently the master password is put in the code...
 * 
 * Sources:
 * http://docs.oracle.com/javase/1.4.2/docs/guide/security/jce/JCERefGuide.html#
 * PBEEx
 * http://stackoverflow.com/questions/1132567/encrypt-password-in-configuration-
 * files-java
 */
public final class EncryptionUtil {
	
	/**
	 * Instantiates a new encryption util.
	 */
	private EncryptionUtil() {
	}
	
	/**
	 * master password:
	 * FIXME: do not store the master password in the code
	 */
	private static final char[]	P	= ("This is our secret master p......d, "
											+ "which should definetely NOT be stored in the code!")
											.toCharArray();
	
	private static final String	ENC	= "PBEWithMD5AndDES";
	
	/** salt */
	private static final byte[]	S	= { (byte) 0xc7, (byte) 0x73, (byte) 0x21,
			(byte) 0x8c, (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99 };
	
	/** Iteration count */
	private static final int	C	= 20;
	
	/**
	 * Encrypt a string.
	 * 
	 * @param text
	 *            the text
	 * @return encryptedText
	 * @throws InvalidKeyException
	 *             the invalid key exception
	 * @throws InvalidAlgorithmParameterException
	 *             the invalid algorithm parameter exception
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws InvalidKeySpecException
	 *             the invalid key spec exception
	 * @throws NoSuchPaddingException
	 *             the no such padding exception
	 * @throws IllegalBlockSizeException
	 *             the illegal block size exception
	 * @throws BadPaddingException
	 *             the bad padding exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	public static String encrypt(final String text) throws InvalidKeyException,
			InvalidAlgorithmParameterException, NoSuchAlgorithmException,
			InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException,
			UnsupportedEncodingException {
		final PBEParameterSpec pbeParamSpec = new PBEParameterSpec(S, C);
		final PBEKeySpec pbeKeySpec = new PBEKeySpec(P);
		final SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ENC);
		final SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
		
		final Cipher pbeCipher = Cipher.getInstance(ENC);
		pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
		
		final byte[] encryptedText = pbeCipher.doFinal(text.getBytes("UTF-8"));
		
		String encodedString = new String(Base64.encodeBase64(encryptedText));
		String safeString = encodedString.replace('+','-').replace('/','_');
		return safeString;
	}
	
	/**
	 * Decrypt an encrypted string.
	 * 
	 * @param encryptedText
	 *            the encrypted text
	 * @return text
	 * @throws InvalidKeyException
	 *             the invalid key exception
	 * @throws InvalidAlgorithmParameterException
	 *             the invalid algorithm parameter exception
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws InvalidKeySpecException
	 *             the invalid key spec exception
	 * @throws NoSuchPaddingException
	 *             the no such padding exception
	 * @throws IllegalBlockSizeException
	 *             the illegal block size exception
	 * @throws BadPaddingException
	 *             the bad padding exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	public static String decrypt(final String encryptedText)
			throws InvalidKeyException, InvalidAlgorithmParameterException,
			NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, UnsupportedEncodingException {
		final PBEParameterSpec pbeParamSpec = new PBEParameterSpec(S, C);
		final PBEKeySpec pbeKeySpec = new PBEKeySpec(P);
		final SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ENC);
		final SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
		
		final Cipher pbeCipher = Cipher.getInstance(ENC);
		pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
		
		final byte[] text = pbeCipher.doFinal(Base64
				.decodeBase64(encryptedText.getBytes()));
		return new String(text, "UTF-8").intern();
	}
}