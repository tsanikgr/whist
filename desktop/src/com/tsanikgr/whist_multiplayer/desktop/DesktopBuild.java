package com.tsanikgr.whist_multiplayer.desktop;

import com.tsanikgr.whist_multiplayer.IApplicationBuild;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DesktopBuild implements IApplicationBuild {
	private int appVersionCode;
	private String packageName = "com.tsanikgr.whist_multiplayer";

	public DesktopBuild() {
		BufferedReader localBufferedReader = null;
		try {
			localBufferedReader = new BufferedReader(new FileReader("pom.xml"));
			Matcher localMatcher;
			String str2 = "";
			do {
				do {
					String str1 = localBufferedReader.readLine();
					if (str1 == null) {
						break;
					}
					str2 = str1.trim();
				} while ((!str2.startsWith("<app.version.code>")) && (!str2.startsWith("<app.version.code.next>")));
				localMatcher = Pattern.compile("-?\\d+").matcher(str2);
			} while (!localMatcher.find());
			this.appVersionCode = Integer.valueOf(localMatcher.group());
		} catch (Exception localException) {
			//local exception ignored
			new Log(this).e().append("Failed to read version info from pom.xml").print();
		} finally {
			try {
				if (localBufferedReader != null) localBufferedReader.close();
			} catch (IOException ignored) {
				new Log(this).w(ignored).print();
			}
		}
	}

	@Override
	public String getAppVersion() {
		return "0.1";
	}

	@Override
	public int getAppVersionCode() {
		return this.appVersionCode;
	}

	@Override
	public String getManifacturer() {
		return "";
	}

	@Override
	public String getModel() {
		return "";
	}

	@Override
	public String getOperatingSystem() {
		return "WINDOWS 8";
	}

	@Override
	public String getPackageName() {
		return this.packageName;
	}

	@Override
	public boolean isAmazon() {
		return false;
	}

	@Override
	public boolean isDebug() {
		return true;
	}

	public void setAppVersionCode(int paramInt) {
		this.appVersionCode = paramInt;
	}

	public void setPackageName(String paramString) {
		this.packageName = paramString;
	}
}