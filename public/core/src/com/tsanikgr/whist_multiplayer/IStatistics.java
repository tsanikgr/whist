package com.tsanikgr.whist_multiplayer;

import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.models.StatisticsModel;

public interface IStatistics {

	interface IStatisticsObserver{
		void onStatisticsChanged(StatisticsModel statisticsModel);
	}

	void addObserver(IStatisticsObserver observer);
	void removeObserver(IStatisticsObserver observer);

	boolean isNameSet();
	StatisticsModel getStatistics();
	void setName(String text);
	StatisticsModel update(GameModel gameModel, int pid);
	StatisticsModel incrementAbandoned(GameModel gameModel, int pid);
	boolean hasEnoughtCoins(int coins);
	boolean placeBet(int bet);
	StatisticsModel refundBet(int bet);

	void unlockAchievement(int achievementId);
	void pushAccomplishments();
	String getName();
}
