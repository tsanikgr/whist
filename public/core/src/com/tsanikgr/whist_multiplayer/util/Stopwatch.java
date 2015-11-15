package com.tsanikgr.whist_multiplayer.util;

import com.badlogic.gdx.utils.Array;

class Stopwatch {

	private boolean started;
	private long initTime;
	private long timeRemaining;

	private int lastSecond;
	private int last100Milly;

	private final Array<StopwatchListener> listeners;
	private final Array<Long> laps;
	public long getTimeRemaining() {
		return timeRemaining;
	}

	public interface StopwatchListener {
		void onSecondChanged(long timeRemaining);
		void on100MillyChanged(long timeRemaining);
		void onTimeExpired();
	}

	public Stopwatch(StopwatchListener listener){
		laps = new Array<>();
		listeners = new Array<>();
		listeners.add(listener);
		started = false;
	}

	public void addListener(StopwatchListener listener) {
		listeners.add(listener);
	}

	public void clearListeners(){
		listeners.clear();
	}

	public void init(long initTime) {
		this.initTime = initTime;
		timeRemaining = initTime;
		lastSecond = -1;
		last100Milly = -1;
		started = false;
		laps.clear();
	}

	public void start() {
		started = true;
	}

	public void lap(){
		laps.add(initTime - timeRemaining);
	}

	public Array<Long> getLaps(){
		return laps;
	}

	public void stop() {
		started = false;
	}

	public void update(float delta) {
		if (!started) return;
		timeRemaining -= (long)(delta*1000f);

		checkForSecondChanged();
		checkFor100MillyChanged();
		if (timeRemaining < 0) for (StopwatchListener stopwatchListener : listeners) stopwatchListener.onTimeExpired();
	}

	private void checkForSecondChanged() {
		if (lastSecond != (int)Math.ceil(timeRemaining/1000f)) {
			for (StopwatchListener stopwatchListener : listeners)
				stopwatchListener.onSecondChanged(timeRemaining);
			lastSecond = (int)Math.ceil(timeRemaining/1000f);
		}
	}

	private void checkFor100MillyChanged() {
		if (last100Milly != (int)((timeRemaining%1000)/100)) {
			for (StopwatchListener stopwatchListener : listeners)
				stopwatchListener.on100MillyChanged(timeRemaining);
			last100Milly = (int) ((timeRemaining % 1000) / 100);
		}
	}
}
