package com.tsanikgr.whist_multiplayer.views.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.tsanikgr.whist_multiplayer.views.View;

public class ChatView extends View{

	public static final String CLOSE_BUTTON = "back_close_chat";
	public ChatView(String xmlFileAndName) {
		super(xmlFileAndName);
	}

	@Override
	public void dispose() {

	}

	@Override
	protected void onAssetsLoaded(View view) {
		setVisible(false);
	}

	@Override
	protected Color getBackgroundColor() {
		return null;
	}

	public void showAnimation() {
		getColor().a = 0f;
		setVisible(true);
		clearActions();
		addAction(Actions.fadeIn(1f, Interpolation.fade));
	}

	public void hideAnimation() {
		clearActions();
		addAction(Actions.sequence(Actions.fadeOut(1f, Interpolation.fade), Actions.run(new Runnable() {
			@Override
			public void run() {
				setVisible(false);
			}
		})));
	}
}
