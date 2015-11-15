package com.tsanikgr.whist_multiplayer;

import com.tsanikgr.whist_multiplayer.util.Log;

public class Config {

	private static int debugClickCounter = 0;
	public static void setDebug() {
		RELEASE = false;
		LOG_ENABLED = true;
		Log.setLevelToInfo();

		LOADING = true;
		AUTOPLAY = true;
		AUTOPLAY_DELAY = 0.7f;
		GLOBAL_SPEED = 1.5f;
		PLAY_IN_BACKGROUND = false;
		STARTING_ROUND_CHECKED = 4;

		DRAW_DEBUG = false;
		SHOW_FPS = true;
		SOUND_ON_ERROR = true;
		IS_CONTINIOUS_RENDERING = false;

		ENABLE_CRYPTOGRAPHY = true;
	}

	public static void setDebugFromSettings(boolean isDebug){
		LOG_ENABLED = true;
		if (isDebug) Log.setLevelToInfo();
		else Log.setLevelToWarning();

		debugClickCounter++;
		AUTOPLAY = debugClickCounter > 5;
		if (debugClickCounter == 10) {
			AUTOPLAY = false;
			debugClickCounter = 0;
		}
		SHOW_FPS = isDebug;
		SOUND_ON_ERROR = isDebug;
	}

	/** REMOVE THE FOLLOWING BEFORE PUBLISHING ONLINE: */
	private static final byte[] DEFAULT_SALT = {
			/* hidden implementation */ };

	public static byte[] getDefaultSalt(){
		byte[] copy = new byte[DEFAULT_SALT.length];
		System.arraycopy(DEFAULT_SALT, 0, copy, 0, DEFAULT_SALT.length);
		return copy;
	}

	private static String getStoragePassword(){
		/* hidden implementation */
	}
	public static String getIvSeparator(){
		/* hidden implementation */
	}

	//default values for release:
	public static boolean RELEASE = true;

	public static int STARTING_ROUND_CHECKED = 4;
	public static boolean LOADING = true;
	public static boolean AUTOPLAY = false;
	public static float AUTOPLAY_DELAY = 0.7f;
	public static boolean PLAY_IN_BACKGROUND = false;

	public static boolean SHOW_FPS = false;
	public static boolean LOG_ENABLED = true;
	public static boolean SOUND_ON_ERROR = false;
	public static boolean IS_CONTINIOUS_RENDERING = false;
	public static boolean DRAW_DEBUG = false;
	public static float GLOBAL_SPEED = 1.25f;

	public static boolean ENABLE_CRYPTOGRAPHY = true;
	public static final String STORAGE_PASSWORD = getStoragePassword();
}
