package com.tsanikgr.whist_multiplayer.util;

public interface ICrypto {
	String encrypt(String message);
	String decrypt(String message);
	void changePassword(String newPassword);
	boolean isReady();
}
