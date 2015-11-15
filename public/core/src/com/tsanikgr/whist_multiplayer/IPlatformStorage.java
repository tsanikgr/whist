package com.tsanikgr.whist_multiplayer;

public interface IPlatformStorage {

	interface OnFileChangedListener{
		void onFileChanged(String key);
	}

	byte[] getDeviceSpecificSalt();
	void write(String key, String value);
	String read(String key);
	boolean delete(String key);

	void registerFileChangeListener(String preferences, final OnFileChangedListener listener);
	void unregisterFileChangeListener(String preferences);
}
