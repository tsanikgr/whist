package com.tsanikgr.whist_multiplayer.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.tsanikgr.whist_multiplayer.IPlatformStorage;
import com.tsanikgr.whist_multiplayer.util.Log;


public class PlatformStorage implements IPlatformStorage {

	private final Log log = new Log(this);
	protected static final String UTF8 = "utf-8";
	private final ObjectMap<String, OnFileChangedListener> listeners;

	public PlatformStorage() {
		listeners = new ObjectMap<>();
	}

	@Override
	public void write(String key, String value) {
		FileHandle file = Gdx.files.local(key);
		file.writeString(value, false);
		for (Entry<String, OnFileChangedListener> listener : listeners) {
			listener.value.onFileChanged(key);
		}
	}

	@Override
	public String read(String key) {
		FileHandle file;
		try {
			file = Gdx.files.local(key);
			if (file != null) return file.readString();
		} catch (GdxRuntimeException e) {
			// exception e ignored
			log.e().append("Cannot find ").append(key).append(" in permanent storage.").print();
			return null;
		}
		return null;
	}

	@Override
	public boolean delete(String key) {
		FileHandle file = Gdx.files.local(key);
		boolean success = file.delete();

		if (success) {
			for (ObjectMap.Entry<String, OnFileChangedListener> listener : listeners) {
				listener.value.onFileChanged(key);
			}
			return true;
		}else return false;
	}

	@Override
	public void registerFileChangeListener(String prefs, OnFileChangedListener listener) {
		if (!listeners.containsKey(prefs)) {
			listeners.put(prefs, listener);
		}
	}

	@Override
	public void unregisterFileChangeListener(String prefs) {
		if (listeners.containsKey(prefs)) {
			listeners.remove(prefs);
		} else {
			log.w().append("No registered listener for '").append(prefs).append("', cannot remove.").print();
		}
	}

	@Override
	public byte[] getDeviceSpecificSalt() {
		log.w().append("No device specific salt for desktop, need to wrapp listener").print();
		return null;
	}
}
