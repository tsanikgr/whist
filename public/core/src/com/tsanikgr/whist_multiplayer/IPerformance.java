package com.tsanikgr.whist_multiplayer;

public interface IPerformance {
	void tick();
	long tock();
	String tickNew();
	long tock(String timerName);
	float tockFloat(String timerName);
	long stop(String timerName);
	float stopFloat(String timerName);
	void renderingStarted();
	void renderingEnded();
	String getIdleTime();
	String getFps();
}
