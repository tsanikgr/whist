package com.tsanikgr.whist_multiplayer.views.menu;

import com.badlogic.gdx.graphics.Color;
import com.tsanikgr.whist_multiplayer.IStatistics;
import com.tsanikgr.whist_multiplayer.models.StatisticsModel;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.myactors.MyLabel;
import com.tsanikgr.whist_multiplayer.views.View;

public class CoinsView extends View implements IStatistics.IStatisticsObserver {

	public static final Color backColor = new Color(77f/255f, 182f/255f, 172f/255f, 1.0f);
	private static final String BACKGROUND_REFILL_LABEL = "label_refill";
	private static final String COINS_COINS_LABEL = "coins_coins_label";

	private MyLabel coinsLabel;
	private MyLabel refill;

	public CoinsView(String name) {
		super(name);
	}

	@Override
	public void dispose() {
	}

	@Override
	protected void onAssetsLoaded(View view) {
		fixOriginRecursively(this);
		coinsLabel = ((MyGroup)findActor("coins")).findActor(COINS_COINS_LABEL);
		refill = ((MyGroup)findActor("label")).findActor(BACKGROUND_REFILL_LABEL);
	}

	@Override
	public void onStatisticsChanged(StatisticsModel stats) {
		coinsLabel.setText(Integer.toString(stats.getCoins()));
		int lastHours = stats.getRemainingHours();
		int lastMinutes = stats.getRemainingMinutes();
		refill.setText(lastHours + "h " + lastMinutes + "m");
	}

	@Override
	public Color getBackgroundColor() {
		return backColor;
	}
}
