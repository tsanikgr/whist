package com.tsanikgr.whist_multiplayer.models;

import com.badlogic.gdx.utils.TimeUtils;

public class StatisticsModel extends JsonSerialisable<StatisticsModel> {

	private static final int FREE_COINS = 100;
	private static final int STARTING_ELO = 1400;
	private static final int STARTING_COINS = 2000;
	public static final String DEFAULT_NICKNAME = "";

	private String name;
	private int coins;
	private int maximumCoins;
	private int winnings;
	private int ELOScore;
	private int maximumELO;
	private int minimumELO;
	private int gamesCompleted;
	private int gamesAbandoned;
	private int[] finishPosition;
	private int bestWinStreak;
	private int currentWinStreak;
	private int averageScoreSUM;
	private int maximumScore;
	private int worseScore;
	private long lastFreeCoinsTimestamp;

	public StatisticsModel() {
		super(StatisticsModel.class);
		setDefaults();
	}

	private StatisticsModel setDefaults() {
		name = DEFAULT_NICKNAME;
		coins = STARTING_COINS;
		maximumCoins = STARTING_COINS;
		winnings = 0;
		ELOScore = STARTING_ELO;
		minimumELO = STARTING_ELO;
		maximumELO = STARTING_ELO;
		gamesCompleted = 0;
		gamesAbandoned = 0;
		finishPosition = new int[4];
		for (int i = 0 ; i < 4 ; i++) finishPosition[i] = 0;
		bestWinStreak = 0;
		currentWinStreak = 0;
		averageScoreSUM = 0;
		maximumScore = 0;
		worseScore = 0;
		lastFreeCoinsTimestamp = TimeUtils.millis();
		return this;
	}

	public boolean update(GameModel model, int forPlayer) {

		int bet = model.getRoomConfig().bet;
		int difficulty = model.getRoomConfig().difficulty;
		if (!model.isGameCompleted()) {
			log.w().append("Statistics not updated, game has not ended.").print();
			return false;
		}

		int score = model.getFinalScore(forPlayer);

		int position = 4;
		for (int i = 0; i < 4; i++) {
			if (i == forPlayer) continue;
			if (score >= model.getFinalScore(i)) position--;
		}
		finishPosition[position-1]++;

		switch (position) {
			case 1:
				coins += bet*2;
				winnings += bet*2;
				currentWinStreak++;
				ELOScore += difficulty*10/2;
				break;
			case 2:
				coins += Math.round(bet*1.5f);
				winnings += Math.round(bet*1.5f);
				currentWinStreak = 0;
				ELOScore += difficulty*5/2;
				break;
			case 3:
				coins += Math.round(bet*0.5f);
				winnings += Math.round(bet*0.5f);
				currentWinStreak = 0;
				ELOScore -= difficulty*5/2;
				break;
			case 4:
				coins += 0;
				currentWinStreak = 0;
				ELOScore -= difficulty*10/2;
				break;
			default: break;
		}

		gamesCompleted++;
		averageScoreSUM += score;

		maximumCoins = Math.max(coins,maximumCoins);
		bestWinStreak = Math.max(bestWinStreak,currentWinStreak);
		maximumScore = Math.max(maximumScore,score);
		worseScore = Math.min(worseScore, score);
		maximumELO = Math.max(maximumELO, ELOScore);
		minimumELO = Math.min(minimumELO, ELOScore);
		return true;
	}

	public boolean depositBet(int bet){
		if (bet > coins) return false;
		coins -= bet;
		return true;
	}

	public boolean refundBet(int bet) {
		if (bet < 0) return false;
		coins += bet;
		return true;
	}

	public void incrementAbandondedGames(){
		gamesAbandoned++;
	}

	public String getName() {
		return name;
	}
	public int getCoins() {
		return coins;
	}
	public int getMaximumCoins() {
		return maximumCoins;
	}
	public int getWinnings() {
		return winnings;
	}
	public int getELOScore() {
		return ELOScore;
	}
	public int getMaximumELO() {
		return maximumELO;
	}
	public int getMinimumELO() {
		return minimumELO;
	}
	public int getGamesCompleted() {
		return gamesCompleted;
	}
	public int getGamesAbandoned() {
		return gamesAbandoned;
	}
	public int getFinishPositionCount(int position) {
		return finishPosition[position];
	}
	public int getBestWinStreak() {
		return bestWinStreak;
	}
	public int getCurrentWinStreak() {
		return currentWinStreak;
	}
	public int getMaximumScore() {
		return maximumScore;
	}
	public int getWorseScore() {
		return worseScore;
	}
	public int getAverageScore(){
		if (gamesCompleted == 0) return -1;
		else return averageScoreSUM/gamesCompleted;
	}

	public int getScorePercent() {
		int eloPerc;

		float m = (float)STARTING_ELO;
		float b = 200f;
		eloPerc = (int)(-((float)ELOScore - m) / b / Math.sqrt(1f + Math.pow(((float)ELOScore - m)/b,2f))*50f + 50f); //sigmoid
		eloPerc = Math.max(Math.min(eloPerc,100),1);
		return eloPerc;
	}

	public int getRemainingHours(){
		return 23 - (int)Math.floor(millisToHours(TimeUtils.millis() - lastFreeCoinsTimestamp));
	}

	public int getRemainingMinutes() {
		long dt = TimeUtils.millis()-lastFreeCoinsTimestamp;
		return 59 - (int)Math.floor(millisToMinutes(dt) - Math.floor(millisToHours(dt)) * 60f);
	}

	public boolean validateRefill() {
		if (millisToHours(TimeUtils.millis()-lastFreeCoinsTimestamp) >= 23.99f) {
			lastFreeCoinsTimestamp = TimeUtils.millis();
			coins += FREE_COINS;
			return true;
		}
		return false;
	}

	private float millisToHours(long l) {
		return (float)l/1000f/60f/60f;
	}

	private float millisToMinutes(long l) {
		return (float)l/1000f/60f;
	}

	public void setName(String name) {
		this.name = name;
	}
}
