package com.tsanikgr.whist_multiplayer.android;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.tsanikgr.whist_multiplayer.IApplicationBuild;
import com.tsanikgr.whist_multiplayer.util.Log;

public class AndroidBuild implements IApplicationBuild {
	private String appVersion = "-1";
	private int appVersionCode = -1;
	private boolean debug = false;
	private boolean isAmazon = false;
	private final String manifacturer;
	private final String model;
	private final String operationSystem;
	private String packageName = "";

	public AndroidBuild(Activity activity){
		this.manifacturer = Build.MANUFACTURER;
		this.model = Build.MODEL;
		this.operationSystem = Build.VERSION.RELEASE;
		try {
			PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
			this.appVersion = packageInfo.versionName;
			this.appVersionCode = packageInfo.versionCode;
			this.packageName = packageInfo.packageName;
		} catch (PackageManager.NameNotFoundException localNameNotFoundException) {
			new Log(this).e(localNameNotFoundException).append("Could not get package details").print();
		}
	}

	@Override
	public String getAppVersion() {
		return this.appVersion;
	}

	@Override
	public int getAppVersionCode() {
		return this.appVersionCode;
	}

	@Override
	public String getManifacturer() {
		return this.manifacturer;
	}

	@Override
	public String getModel() {
		return this.model;
	}

	@Override
	public String getOperatingSystem() {
		return this.operationSystem;
	}

	@Override
	public String getPackageName() {
		return this.packageName;
	}

	@Override
	public boolean isAmazon() {
		return this.isAmazon;
	}

	@Override
	public boolean isDebug() {
		return this.debug;
	}

	public void setAmazon(boolean isAmazon) {
		this.isAmazon = isAmazon;
	}

	public void setDebug(boolean isDebug) {
		this.debug = isDebug;
	}
}