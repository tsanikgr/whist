package com.tsanikgr.whist_multiplayer.assets;

public interface PerformanceInterface {
	void tick();
	double tock();
	String tickNew();
	long tock(String timerName);
	float tockFloat(String timerName);
	long stop(String timerName);
	float stopFloat(String timerName);
	void renderingStarted();
	void renderingEnded(boolean inLoadingState);
	String getIdleTime();
	boolean isShowingFPS();
}
