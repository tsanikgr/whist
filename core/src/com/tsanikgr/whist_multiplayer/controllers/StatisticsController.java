package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.tsanikgr.whist_multiplayer.IStatistics;
import com.tsanikgr.whist_multiplayer.models.AccomplishmentsModel;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.models.StatisticsModel;

public class StatisticsController extends Controller implements IStatistics {

	private static final String STATISTICS = "statistics";
	private static final String ACCOMPLISHMENTS_MODEL = "accomplishments";

	private AccomplishmentsModel accomplishmentsModel;
	private StatisticsModel statistics;

	private int lastHours;
	private int lastMinutes;
	private Timer.Task task;

	private final Array<IStatisticsObserver> observers;

	public StatisticsController() {
		observers = new Array<>(3);
	}

	@Override
	protected void init() {
		load();
		startTimer();
	}

	@Override
	protected void disposeController() {
		if (task != null) task.cancel();
		task = null;
	}

	@Override
	public StatisticsModel getStatistics(){
		if (statistics == null) load();
		return statistics;
	}

	private void load(){
		String load = getStorage().load(STATISTICS);
		statistics = StatisticsModel.deserialise(load, StatisticsModel.class);
		if (statistics == null) statistics = new StatisticsModel();

		load = getStorage().load(ACCOMPLISHMENTS_MODEL);
		accomplishmentsModel = AccomplishmentsModel.deserialise(load, AccomplishmentsModel.class);
		if (accomplishmentsModel == null) accomplishmentsModel = new AccomplishmentsModel();

		notifyObservers();
	}

	private void save(){
		getStorage().save(STATISTICS, getStatistics().serialise());
		notifyObservers();
	}

	@Override
	public void addObserver(IStatisticsObserver observer) {
		observers.add(observer);
		if (observer != null) observer.onStatisticsChanged(getStatistics());
	}

	@Override
	public void removeObserver(IStatisticsObserver observer) {
		observers.removeValue(observer,true);
	}

	private void notifyObservers(){
		if (getMenuController() == null) return;
		for (IStatisticsObserver observer : observers) if (observer != null) observer.onStatisticsChanged(getStatistics());

//		getMenuController().updateStatistics(getStatistics());
	}

	@Override
	public void setName(String name) {
		getStatistics().setName(name);
		save();
	}

	@Override
	public String getName() {
		return getStatistics().getName();
	}

	@Override
	public StatisticsModel update(GameModel gameModel, int pid) {
		if (getStatistics().update(gameModel, pid)) save();

		checkForAccomplishments(gameModel);

		return statistics;
	}

	@Override
	public StatisticsModel incrementAbandoned(GameModel gameModel, int pid) {
		getStatistics().incrementAbandondedGames();
		save();
		return statistics;
	}

	@Override
	public boolean hasEnoughtCoins(int coins) {
		return getStatistics().getCoins() >= coins;
	}

	@Override
	public boolean placeBet(int bet) {
		if (!getStatistics().depositBet(bet)) return false;
		save();
		return true;
	}

	@Override
	public StatisticsModel refundBet(int bet) {
		getStatistics().refundBet(bet);
		save();
		return statistics;
	}

	@Override
	public boolean isNameSet() {
		return getStatistics().getName().compareTo(StatisticsModel.DEFAULT_NICKNAME) != 0;
	}

	private void validateRefill(){
		if (getStatistics().validateRefill()) save();
	}

	private void startTimer(){
		lastHours = getStatistics().getRemainingHours();
		lastMinutes = statistics.getRemainingMinutes();
		validateRefill();

		task = new Timer().scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				lastMinutes--;
				if (lastMinutes < 0) {
					lastMinutes = 59;
					lastHours--;
					if (lastHours < 0) {
						validateRefill();
						lastHours = statistics.getRemainingHours();
						lastMinutes = statistics.getRemainingMinutes();
					}
				}
			}
		}, 60f, 60f);
	}

	private void checkForAccomplishments(GameModel model) {
		boolean needsPush = false;

		if (getStatistics().getMaximumScore() >= 200 && accomplishmentsModel.unlockAchievement(AccomplishmentsModel.PRO)) needsPush = true;
		if (statistics.getGamesCompleted() == 10 && accomplishmentsModel.unlockAchievement(AccomplishmentsModel.APPRENTICE)) needsPush = true;
		if (accomplishmentsModel.newHighScore(statistics.getMaximumScore())) needsPush = true;
		if (accomplishmentsModel.newHighELO(statistics.getMaximumELO())) needsPush = true;

		if (needsPush) pushAccomplishments();
	}

	@Override
	public void unlockAchievement(int achievementId) {
		if (accomplishmentsModel.unlockAchievement(achievementId)) pushAccomplishments();
	}

	@Override
	/** pushes the accomplishments online if possible, otherwise saves to local storage for latter processing */
	public void pushAccomplishments() {
		if (accomplishmentsModel == null) return;
		if (accomplishmentsModel.isOutboxEmpty()) return;

		if (getMultiplayer() != null && getMultiplayer().isSignedIn()) {
			String achievementId;
			while ((achievementId = accomplishmentsModel.getNextAchievementAndClearFromOutbox()) != null)
				getMultiplayer().unlockAchievement(achievementId);

			long score;
			if ((score = accomplishmentsModel.getMaxELOAndClearFromOutbox()) > 0)
				getMultiplayer().postELOToLeaderboards(score);
			if ((score = accomplishmentsModel.getMaxScoreAndClearFromOutbox()) > 0)
				getMultiplayer().postScoreToLeaderboards(score);
		}

		getStorage().save(ACCOMPLISHMENTS_MODEL, accomplishmentsModel.serialise());
	}
}
