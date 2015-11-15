package com.tsanikgr.whist_multiplayer.android;

import android.provider.Settings;

import com.tsanikgr.whist_multiplayer.IUid;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.util.UUID;

public class UIDAndroid implements IUid {

	private final Log log = new Log(this);
	private final Storage storage;
	private final AndroidLauncher app;

	public UIDAndroid(Storage storage, AndroidLauncher app) {
		this.storage = storage;
		this.app = app;
	}

	@Override
	public String getDeviceIdentifier() {
		try {
			return Settings.Secure.getString(app.getContentResolver(), Settings.Secure.ANDROID_ID);
		} catch (Exception e) {
			log.w(e).append("Could not get unique device identifier!").print();
			return getInstallationIdentifier();
		}
	}

	@Override
	public String getInstallationIdentifier() {
		String gid = storage.read("id", "did");
		if (gid == null) {
			gid = UUID.randomUUID().toString();
			storage.write("id", "did", gid);
		}
		return gid;
	}

}
