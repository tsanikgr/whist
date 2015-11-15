package com.tsanikgr.whist_multiplayer.util;

import com.badlogic.gdx.utils.TimeUtils;

public class Timer {
	private long start;
	public Timer(){
		start = TimeUtils.millis();
	}
	public void tick() {
		start = TimeUtils.millis();
	}
	public long tock(){
		return TimeUtils.millis() - start;
	}
	public float tockFloat(){
		return (float)(TimeUtils.millis() - start)/1000f;
	}
}