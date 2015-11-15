package com.tsanikgr.whist_multiplayer.AI;

public class AiConfig {
	private static final int EXECUTOR_TIMEOUT_SECONDS = 5;
	private static final int MAX_SIMS_PER_CARD = 18000;
	private static final int MAX_EXECUTOR_THREADS = 2; //(+1 more, the primary worker, + 1 for the UI)
	private static final int MAX_FILTERED_CARDS = 2;
	private static final int FILTER_ABOVE_ROUND = 4;
	private static final int SPAWN_THREAD_IF_CARDS_LEFT_MORE_THAN = 4;

	private int executorTimeoutSeconds;
	private int maxSimsPerCard;
	private int maxThreads;
	private int maxFilteredCards;
	private int filterAboveRound;
	private int spawnThreadIfCardsLeftMoreThan;

	public AiConfig(){
		setDefaults();
	}

	private void setDefaults() {
		executorTimeoutSeconds = EXECUTOR_TIMEOUT_SECONDS;
		maxSimsPerCard = MAX_SIMS_PER_CARD;
		maxThreads = MAX_EXECUTOR_THREADS;
		maxFilteredCards = MAX_FILTERED_CARDS;
		filterAboveRound = FILTER_ABOVE_ROUND;
		spawnThreadIfCardsLeftMoreThan = SPAWN_THREAD_IF_CARDS_LEFT_MORE_THAN;
	}

	public int getExecutorTimeoutSeconds() {
		return executorTimeoutSeconds;
	}
	public void setExecutorTimeoutSeconds(int executorTimeoutSeconds) {
		this.executorTimeoutSeconds = executorTimeoutSeconds;
	}
	public int getMaxSimsPerCard() {
		return maxSimsPerCard;
	}
	public void setMaxSimsPerCard(int maxSimsPerCard) {
		this.maxSimsPerCard = maxSimsPerCard;
	}
	public int getMaxThreads() {
		return maxThreads;
	}
	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}
	public int getMaxFilteredCards() {
		return maxFilteredCards;
	}
	public void setMaxFilteredCards(int maxFilteredCards) {
		this.maxFilteredCards = maxFilteredCards;
	}
	public int getFilterAboveRound() {
		return filterAboveRound;
	}
	public void setFilterAboveRound(int filterAboveRound) {
		this.filterAboveRound = filterAboveRound;
	}
	public int getSpawnThreadIfCardsLeftMoreThan() {
		return spawnThreadIfCardsLeftMoreThan;
	}
	public void setSpawnThreadIfCardsLeftMoreThan(int spawnThreadIfCardsLeftMoreThan) {
		this.spawnThreadIfCardsLeftMoreThan = spawnThreadIfCardsLeftMoreThan;
	}
}
