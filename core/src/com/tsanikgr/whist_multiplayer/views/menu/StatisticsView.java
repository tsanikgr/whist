package com.tsanikgr.whist_multiplayer.views.menu;

import com.badlogic.gdx.graphics.Color;
import com.tsanikgr.whist_multiplayer.IStatistics;
import com.tsanikgr.whist_multiplayer.models.StatisticsModel;
import com.tsanikgr.whist_multiplayer.myactors.MyLabel;
import com.tsanikgr.whist_multiplayer.views.View;

public class StatisticsView extends View implements IStatistics.IStatisticsObserver {

	public static final Color backColor = new Color(179f/255f, 136f/255f, 255f/255f, 1f);

	private MyLabel[] finish;
	private MyLabel[] perc;
	private MyLabel games;
	private MyLabel worseScore;
	private MyLabel bestScore;
	private MyLabel averageScore;
	private MyLabel winnings;
	private MyLabel careerHighCoins;
	private MyLabel coins;
	private MyLabel eloHigh;
	private MyLabel eloLow;
	private MyLabel elo;

	public StatisticsView(String name) {
		super(name);
	}

	@Override
	public void dispose() {
	}

	@Override
	protected void onAssetsLoaded(View view) {
		fixOriginRecursively(this);
		finish = new MyLabel[4];
		perc = new MyLabel[4];
		for (int i = 1 ; i < 5 ; i++) {
			finish[i-1] = findActor("input_finish_" + i);
			perc[i-1] = findActor("input_perc_" + i);
		}
		games = findActor("input_games");
		worseScore = findActor("input_worse_score");
		bestScore = findActor("input_best_score");
		averageScore = findActor("input_average_score");
		winnings = findActor("input_winnings");
		careerHighCoins = findActor("input_career_high_coins");
		coins = findActor("input_coins");
		eloHigh = findActor("input_elo_high");
		eloLow = findActor("input_elo_low");
		elo = findActor("input_elo");
	}

	@Override
	public Color getBackgroundColor() {
		return backColor;
	}

	@Override
	public void onStatisticsChanged(StatisticsModel statistics) {
		for (int i = 0 ; i < 4; i++) {
			finish[i].setText(Integer.toString(statistics.getFinishPositionCount(i)));
			if (statistics.getGamesCompleted() == 0) perc[i].setText("");
			else perc[i].setText(String.format("(%.1f%%)",(float)(statistics.getFinishPositionCount(i)*100f/statistics.getGamesCompleted())));
		}
		coins.setText(Integer.toString(statistics.getCoins()));
		games.setText(Integer.toString(statistics.getGamesCompleted()));
		worseScore.setText(Integer.toString(statistics.getWorseScore()));
		bestScore.setText(Integer.toString(statistics.getMaximumScore()));
		if (statistics.getAverageScore() == -1) averageScore.setText("-");
		else averageScore.setText(Integer.toString(statistics.getAverageScore()));
		winnings.setText(Integer.toString(statistics.getWinnings()));
		careerHighCoins.setText(Integer.toString(statistics.getMaximumCoins()));
		elo.setText(Integer.toString(statistics.getELOScore()));
		eloLow.setText(Integer.toString(statistics.getMinimumELO()));
		eloHigh.setText(Integer.toString(statistics.getMaximumELO()));
	}
}
