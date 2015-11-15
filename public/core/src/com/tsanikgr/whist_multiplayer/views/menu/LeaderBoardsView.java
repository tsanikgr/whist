package com.tsanikgr.whist_multiplayer.views.menu;

import com.badlogic.gdx.graphics.Color;
import com.tsanikgr.whist_multiplayer.views.View;

public class LeaderBoardsView extends View {

	public static final Color backColor = new Color(0.79f, 0.85f, 0.17f, 1f);
	public LeaderBoardsView(String name) {
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
