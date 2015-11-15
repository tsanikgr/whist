package com.tsanikgr.whist_multiplayer.AI;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class WhistExecutorService{
	private final Log log = new Log(this);

	private static int MAX_THREADS;
	private final ThreadedGameModelPool gameModelPool;
	private final ThreadedArrayIntegerPool integerArrayPool;
	private final ValidateAiCallback callback;
	private static int SPAWN_THREAD_IF_CARDS_LEFT_MORE_THAN;
	private final ExecutorService executorService;
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(false);

	private final Array<Future<?>> tasks = new Array<>(20);
	private final boolean[] running;
	private volatile int currentThreads;
	private volatile boolean noMoreThreadsAllowed = false;

	WhistExecutorService(int maxThreads, int spawnThreadIfCardsLeftMoreThan, ThreadedGameModelPool gameModelPool, ThreadedArrayIntegerPool integerArrayPool, ValidateAiCallback callback){
		MAX_THREADS = maxThreads;
		this.gameModelPool = gameModelPool;
		this.integerArrayPool = integerArrayPool;
		this.callback = callback;
		executorService = Executors.newFixedThreadPool(MAX_THREADS);
		SPAWN_THREAD_IF_CARDS_LEFT_MORE_THAN = spawnThreadIfCardsLeftMoreThan;
		running = new boolean[MAX_THREADS];
		currentThreads = 0;
	}

	public void start(){
		noMoreThreadsAllowed = false;
		tasks.clear();
		for (int i = 0; i < MAX_THREADS; i++) running[i] = false;
	}

	public boolean execute(ThreadedGameModelPool.SimGameModel model, int card) {

		if (model.getState().getCardsLeft() < SPAWN_THREAD_IF_CARDS_LEFT_MORE_THAN) return false;
		if (executorService.isTerminated() || executorService.isShutdown()) return false;

		boolean ret = false;
		Lock rl = readWriteLock.readLock();
		rl.lock();
		if (currentThreads < MAX_THREADS && !noMoreThreadsAllowed) {
			rl.unlock();
			Lock wl = readWriteLock.writeLock();
			wl.lock();
			try {
				if (currentThreads < MAX_THREADS && !noMoreThreadsAllowed) {      //recheck state because another thread might have altered it
					for (int f = tasks.size - 1; f >= 0; f--) if (tasks.get(f).isDone()) tasks.removeIndex(f);
					currentThreads++; //write lock obtained, so this is an atomic operation
					int id = 0;
					for (int i = 0; i < MAX_THREADS; i++) if (!running[i]){
						id = i;
						break;
					}
					running[id] = true;
					model.threadId = id;
					tasks.add(newTask(model, card));
					ret = true;
				}
			} finally {
				wl.unlock();
			}
		} else rl.unlock();
		return ret;
	}

	private Future<?> newTask(final ThreadedGameModelPool.SimGameModel model, final int card) {
		return executorService.submit(new Runnable() {
			@Override
			public void run() {
//					log.i().append(" ### Spawned thread [").append(model.threadId).append("][").append(Thread.currentThread().getName()).append("]. Total threads (active/queued): ").append(activeThreads()).append(" / ").append(queuedThreads()).print();
				int threadId = model.threadId;
				callback.validateGameAction(model, model.getState().getCurrentPlayer(), false, card);
				Lock wl = readWriteLock.writeLock();
				wl.lock();
				try {
//						log.i().append(" ### Thread [").append(model.threadId).append("][").append(Thread.currentThread().getName()).append("] finished. Total threads (active/queued): ").append(activeThreads()).append(" / ").append(queuedThreads()).print();
					running[threadId] = false;
					currentThreads--; //write lock obtained, so this is an atomic operation
				} finally {
					wl.unlock();
				}
			}
		});
	}

	private int queuedThreads() {
		return ((ThreadPoolExecutor)executorService).getQueue().size();
	}

	private int activeThreads() {
		return ((ThreadPoolExecutor)executorService).getActiveCount();
	}

	void waitForThreads(int pollingMillis, int timeoutSeconds){
		long now = TimeUtils.millis();
		boolean logged = false;

		while (true) {
			try {
				Thread.sleep(pollingMillis);
			} catch (InterruptedException e) {
				log.w(e).print();
			}
			if (allThreadsDone()) break;
			else if (!logged) {
				log.i().append("Awaiting for threads. Timeout in ").append(timeoutSeconds).append(" seconds.........................").print();
				logged = true;
			}
			if (timeout(timeoutSeconds, TimeUtils.millis() - now)) break;
		}

		for (int i = 0; i < tasks.size; i++) {
			try {
				tasks.get(i).get();
			} catch (CancellationException e) {
				//ignore
			} catch (Exception e) {
				log.e(e).print();
			}
		}
		tasks.clear();
		log.i().print();
		if (activeThreads() != 0 || queuedThreads() != 0) log.e().append("Active threads: (").append(activeThreads()).append("), Queued threads: (").append(queuedThreads()).append(")").print();

		for (int i = 0; i < MAX_THREADS+1; i++) log.i().append("GameModelPool [").append(i).append("] (free/peak): ").append(gameModelPool.get(i).getFree()).append(" / ").append(gameModelPool.get(i).peak).print();
		for (int i = 0; i < MAX_THREADS+1; i++) log.i().append("ArrayIntePool [").append(i).append("] (free/peak): ").append(integerArrayPool.get(i).getFree()).append(" / ").append(integerArrayPool.get(i).peak).print();
	}

	private boolean allThreadsDone(){
		boolean ret = false;
		Lock rl = readWriteLock.readLock();
		rl.lock();
		if (currentThreads == 0) {
			rl.unlock();
			Lock wl = readWriteLock.writeLock();
			wl.lock();
			if (currentThreads == 0) {
				noMoreThreadsAllowed = true;
				ret = true;
			}
			wl.unlock();
		} else rl.unlock();
		return ret;
	}

	public boolean timeout(int timeoutSeconds, long elapsed) {
		if (elapsed > timeoutSeconds*1000){
			log.i().print();
			log.i().append("+++++++++++++++ TimeOut +++++++++++++++ ").print();
			log.i().print();
			int counter = 0;

			Lock wl = readWriteLock.writeLock();
			wl.lock();
			try {
				for (int t = tasks.size - 1; t >= 0; t--) if (tasks.get(t).cancel(true)) counter++;
				((ThreadPoolExecutor) executorService).purge();
			} finally {
				wl.unlock();
			}
			log.i().append("!!!!! ").append(counter).append(" tasks canceled !!!!!").print();
			return true;
		}
		return false;
	}
}