package com.tsanikgr.whist_multiplayer.AI;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.tsanikgr.whist_multiplayer.util.Log;

class ThreadedArrayIntegerPool {
	private final Log log = new Log(this);
	private final IntegerArrayPool[] pools;
	private final int poolInitCapacity;
	private final int poolMaxCapacity;

	ThreadedArrayIntegerPool(int maxThreads, int poolInitCapacity, int poolMaxCapacity) {
		this.poolInitCapacity = poolInitCapacity;
		this.poolMaxCapacity = poolMaxCapacity;

		pools = new IntegerArrayPool[maxThreads + 1];
		for (int i = 0; i < maxThreads + 1; i++) {
			pools[i] = new IntegerArrayPool(i);
		}
	}

	public Array<Integer> obtainSync(int threadId) {
		return pools[threadId].obtainSync();
	}

	public void free(Array<Integer> array, int threadId) {
		pools[threadId].free(array);
	}

	public IntegerArrayPool get(int i) {
		return pools[i];
	}

	class IntegerArrayPool extends Pool<Array<Integer>> {
		private final int id;
		private long lastPeak = 0;

		public IntegerArrayPool(int id) {
			super(poolInitCapacity, poolMaxCapacity);
			this.id = id;
			lastPeak = 0;
		}
		@Override
		protected Array<Integer> newObject() {
			return new Array<>();
		}

		private Array<Integer> obtainSync() {
			if (peak > (float) lastPeak * 1.5f) {
				lastPeak = peak;
				log.i().append(peak).append(" Arrays<Integer> in Pool [").append(id).append("] <<<<<<<<<<<<<<<<<<<<<<<<<<<<").print();
			}
			return super.obtain();
		}

		@Override
		public void free(Array<Integer> array) {
			if (array == null) log.e().append("Freed Array<Integer> is null");
			else {
				array.clear();
				super.free(array);
			}
		}
	}
}