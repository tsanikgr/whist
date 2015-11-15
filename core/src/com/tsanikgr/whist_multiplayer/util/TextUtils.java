package com.tsanikgr.whist_multiplayer.util;

class TextUtils {
	public static boolean isNullOrEmpty(String paramString) {
		return paramString == null || "".equals(paramString.replaceAll("\\s", ""));
	}
}
