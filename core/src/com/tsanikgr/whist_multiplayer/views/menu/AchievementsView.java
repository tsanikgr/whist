package com.tsanikgr.whist_multiplayer.views.menu;

import com.badlogic.gdx.graphics.Color;
import com.tsanikgr.whist_multiplayer.views.View;

public class AchievementsView extends View {

	public static final Color backColor = new Color(1f, 0.74f, 0f, 1f);
	public AchievementsView(String name) {
		super(name);
	}
	@Override
	public void dispose() {

	}
	@Override
	protected void onAssetsLoaded(View view) {

	}

	@Override
	public Color getBackgroundColor() {
		return backColor;
	}
}
