package com.tsanikgr.whist_multiplayer.AI;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.util.Log;

class ThreadedGameModelPool {
	private final Log log = new Log(this);
	private final GameModelPool[] pools;
	private static int MAX_THREADS;
	private final int poolInitCapacity;
	private final int poolMaxCapacity;

	ThreadedGameModelPool(int maxThreads, int poolInitCapacity, int poolMaxCapacity) {
		MAX_THREADS = maxThreads;
		this.poolInitCapacity = poolInitCapacity;
		this.poolMaxCapacity = poolMaxCapacity;
		pools = new GameModelPool[maxThreads + 1];
		for (int i = 0; i < maxThreads + 1; i++) {
			pools[i] = new GameModelPool(i);
		}
	}

	public SimGameModel fromPrototype(SimGameModel model) { return pools[model.threadId].fromPrototype(model); }

	public SimGameModel obtain() { return pools[MAX_THREADS].obtain(); }

	public void free(SimGameModel model) { pools[model.threadId].free(model); }

	public GameModelPool get(int i) { return pools[i];	}


	class GameModelPool extends Pool<SimGameModel> {
		final int id;
		int lastPeak = 0;

		@Override
		protected SimGameModel newObject() { return new SimGameModel(); }

		GameModelPool(int id){
			super(poolInitCapacity, poolMaxCapacity);
			this.id = id;
			lastPeak = 0;
		}

		SimGameModel fromPrototype(SimGameModel prototype) { return obtain().copy(prototype,true); }

		@Override
		public SimGameModel obtain() {
			if (peak > (float)lastPeak*1.5f) {
				lastPeak = peak;
				log.i().append(peak).append(" GameModels in Pool [").append(id).append("] <<<<<<<<<<<<<<<<<<<<<<<<<").print();
			}
			return super.obtain();
		}

		@Override
		public void free(SimGameModel model) {
			if (model == null) log.e().append("Freed GameModel is null");
			else super.free(model);
		}
	}

	static class SimGameModel extends GameModel {
		final Array<Integer> knownCards;
		int startingCard;
		int threadId;

		SimGameModel() {
			knownCards = new Array<>();
			startingCard = -1;
			threadId = MAX_THREADS;
		}

		@Override
		public void reset() {
			super.reset();
			knownCards.clear();
			startingCard = -1;
			threadId = MAX_THREADS;
		}

		@Override
		public SimGameModel copy(GameModel prototype, boolean ignoreDeck) {
			super.copy(prototype, ignoreDeck);
			if (prototype instanceof SimGameModel) {
				knownCards.addAll(((SimGameModel) prototype).knownCards);
				startingCard = ((SimGameModel) prototype).startingCard;
				threadId = ((SimGameModel) prototype).threadId;
			}
			return this;
		}
	}
}
