package com.tsanikgr.whist_multiplayer.desktop;

import com.tsanikgr.whist_multiplayer.IUid;

import java.util.UUID;

public class UIDDesktop implements IUid {

	public UIDDesktop() {}

	@Override
	public String getDeviceIdentifier() {
//		String did = storage.read("did");
//		if (did == null) {
//			did = UUID.randomUUID().toString();
//			storage.write("did", did);
//		}
//		return did;

		return UUID.randomUUID().toString();
	}

	@Override
	public String getInstallationIdentifier() {

		return UUID.randomUUID().toString();

//		String did = storage.read("did");
//		if (did == null) {
//			did = UUID.randomUUID().toString();
//			storage.write("did", did);
//		}
//		return did;
	}

}
