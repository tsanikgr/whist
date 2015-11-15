package com.tsanikgr.whist_multiplayer.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class MD5 {
	public static boolean checkMD5(String md5, InputStream is) {
		if (md5 == null || md5.equals("") || is == null) {
			return false;
		}

		String calculatedDigest = calculateMD5(is);
		return calculatedDigest != null && calculatedDigest.equalsIgnoreCase(md5);
	}

	private static String calculateMD5(InputStream is) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			new Log(null).w(e).print();
			return null;
		}

		byte[] buffer = new byte[8192];
		int read;
		try {
			while ((read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			String output = bigInt.toString(16);
			// Fill to 32 chars
			output = String.format("%32s", output).replace(' ', '0');
			return output;
		} catch (IOException e) {
			throw new RuntimeException("Unable to process file for MD5", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				new Log(null).w(e).print();
			}
		}
	}
}
