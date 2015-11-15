package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.TimeUtils;
import com.tsanikgr.whist_multiplayer.util.Timer;
import com.tsanikgr.whist_multiplayer.IPerformance;

import java.util.HashMap;
import java.util.UUID;

public class Performance extends Controller implements IPerformance {

	private static final int SAMPLING_SIZE = 30;
	private long startTime;
	private long lastStartTime;
	private float tTotalSum;
	private float idle;
	private float averageFPS;
	private float tWorkingSum;
	private int f = 0;


	@Override
	public void init() {
//		dtRequired = (int)(1f/throttleFPS * 1000f);
//		dynamicAdjust = 0;
	}

	@Override
	public void disposeController() {
	}

	@Override
	protected void update(float dt) {
		super.update(dt);
		renderingStarted();
	}

	private final HashMap<String, Timer> timers = new HashMap<>();
	private final Timer defaultTimer = new Timer();

	@Override
	public void tick(){
		defaultTimer.tick();
	}

	@Override
	public long tock(){
		return defaultTimer.tock();
	}

	@Override
	public String tickNew() {
		String s = UUID.randomUUID().toString();
		tick(s);
		return s;
	}

	private void tick(String timerName) {
		Timer timer = timers.get(timerName);
		if (timer != null) timer.tick();
		else {
			timer = Pools.obtain(Timer.class);
			timers.put(timerName, timer);
			timer.tick();
		}
	}

	@Override
	public long tock(String timerName) {
		Timer timer = timers.get(timerName);
		if (timer != null) return timer.tock();
		else {
			log.e().append(timerName).append(": Timer does not exist").print();
			return 0;
		}
	}

	@Override
	public float tockFloat(String timerName) {
		Timer timer = timers.get(timerName);
		if (timer != null) return timer.tockFloat();
		else {
			log.e().append(timerName).append(": Timer does not exist").print();
			return 0f;
		}
	}

	@Override
	public long stop(String timerName) {
		Timer timer = timers.remove(timerName);
		long dt = 0;
		if (timer != null) {
			dt = timer.tock();
			Pools.free(timer);
		} else {
			log.e().append(timerName).append(": Timer does not exist").print();
		}
		return dt;
	}

	@Override
	public float stopFloat(String timerName) {
		Timer timer = timers.remove(timerName);
		float dt = 0f;
		if (timer != null) {
			dt = timer.tockFloat();
			Pools.free(timer);
		} else {
			log.e().append(timerName).append(": Timer does not exist").print();
		}
		return dt;
	}

	@Override
	public void renderingStarted() {

		startTime = TimeUtils.millis();
		tTotalSum += startTime - lastStartTime;
		lastStartTime = startTime;

		if (f == SAMPLING_SIZE) {
			averageFPS = 1000f / tTotalSum * SAMPLING_SIZE;
			idle = (tTotalSum - tWorkingSum) / tTotalSum * 100f;
			f = 0;
			tTotalSum = 0f;
			tWorkingSum = 0f;
//			if (doThrottle) {
//				dynamicAdjust += (long) ((1000f / averageFPS - dtRequired) * dynamicDumping);
//			}
		}
		f++;

	}

	@Override
	public void renderingEnded() {
		long dt = TimeUtils.millis() - startTime;
		tWorkingSum += dt;
//		if (doThrottle && !inLoadingState && (dt < dtRequired - dynamicAdjust)) {
//			try {
//				Thread.sleep(dtRequired - dt - dynamicAdjust);
//			} catch (InterruptedException e) {
//				log.e("",e,"renderingEnded");
//			}
//		}
	}

	@Override
	public String getIdleTime() {
		return String.format("%.2f",idle);
	}

	@Override
	public String getFps(){ return String.format("%d", Math.round(averageFPS)); }
}
