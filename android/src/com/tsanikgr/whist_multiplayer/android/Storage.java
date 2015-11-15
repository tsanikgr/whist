package com.tsanikgr.whist_multiplayer.android;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.provider.Settings;

import com.badlogic.gdx.utils.ObjectMap;
import com.tsanikgr.whist_multiplayer.IPlatformStorage;
import com.tsanikgr.whist_multiplayer.util.Log;

public class Storage implements IPlatformStorage {

	private final Log log = new Log(this);
	private final AndroidLauncher app;
	private static final String UTF8 = "utf-8";
	private final ObjectMap<String, SharedPreferences> prefs;
	private final ObjectMap<String, OnSharedPreferenceChangeListener> listeners;

	public Storage(AndroidLauncher app) {
		this.app = app;
		prefs = new ObjectMap<>();
		listeners = new ObjectMap<>();
	}

	@Override
	public void write(String key, String value) {
		SharedPreferences.Editor editor = getPrefs(key).edit();
		editor.putString(key, value);
		if (!editor.commit()) {
			log.e().append("Failed to save [").append(key).print();
		}
	}

	public void write(String prefs, String key, String value) {
		SharedPreferences.Editor editor = getPrefs(prefs).edit();
		editor.putString(key, value);
		if (!editor.commit()) {
			log.e().append("Failed to save [").append(key).append("] in [").append(prefs).print();
		}
	}

	@Override
	public String read(String key) {
		String s = getPrefs(key).getString(key, null);
		if (s == null) {
			log.w().append("Failed to load [").append(key).append("]").print();
		}
		return s;
	}

	public String read(String prefs, String key) {
		String s = getPrefs(prefs).getString(key, null);
		if (s == null) {
			log.w().append("Failed to load [").append(key).append("] from [").append(prefs).append("]").print();
		}
		return s;
	}

	@Override
	public boolean delete(String key) {
		SharedPreferences.Editor editor = getPrefs(key).edit();
		editor.remove(key);
		if (!editor.commit()) {
			log.e().append("Failed to delete [").append(key).append("]").print();
			return false;
		}
		return true;
	}

	private SharedPreferences getPrefs(String key) {
		if (prefs.containsKey(key)) return prefs.get(key);
		else {
			SharedPreferences sp = app.getSharedPreferences(key, 0);
			prefs.put(key, sp);
			return sp;
		}
	}

	@Override
	public void registerFileChangeListener(String prefs, final OnFileChangedListener listener) {
		OnSharedPreferenceChangeListener l;
		if (listeners.containsKey(prefs)) {
			l = listeners.get(prefs);
		} else {
			l = new OnSharedPreferenceChangeListener() {

				@Override
				public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
					listener.onFileChanged(key);
				}
			};
			listeners.put(prefs, l);
		}
		getPrefs(prefs).registerOnSharedPreferenceChangeListener(l);
	}

	@Override
	public void unregisterFileChangeListener(String prefs) {
		if (listeners.containsKey(prefs)) {
			getPrefs(prefs).unregisterOnSharedPreferenceChangeListener(listeners.get(prefs));
			listeners.remove(prefs);
		} else {
			log.w().append("No registered listener for [").append(prefs).append("], cannot remove.").print();
		}
	}

	@Override
	public byte[] getDeviceSpecificSalt() {
		try {
			return Settings.Secure.getString(app.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes(UTF8);
		} catch (Exception e) {
			log.w(e).append("Cannot get device specific salt").print();
			return null;
		}
	}
}
