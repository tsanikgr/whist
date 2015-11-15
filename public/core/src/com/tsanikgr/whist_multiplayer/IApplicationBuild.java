package com.tsanikgr.whist_multiplayer;

public interface IApplicationBuild {
	String getAppVersion();
	int getAppVersionCode();
	String getManifacturer();
	String getModel();
	String getOperatingSystem();
	String getPackageName();
	boolean isAmazon();
	boolean isDebug();
}