package com.tsanikgr.whist_multiplayer;

public interface ISounds {
	void setVolume(float volume);
	void soundsLoaded();
	void playDealSound();
	void playPopSound();
	void playTickSound();
	void playTurnSound();
}
