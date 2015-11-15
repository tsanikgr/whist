package com.tsanikgr.whist_multiplayer;

public interface IPlatformApplication{
	boolean isAndroid();
	void shareText(String s);
	void rateApp();
	void keepScreenOn(boolean keepOn);
	void executePendingUiThreadRunnables();
	void setMultiplayerControllerAndAttemptToConnect(IMultiplayerController multiplayerController);
}
