package com.tsanikgr.whist_multiplayer.AI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.util.Log;

public abstract class AbstractWhistAI implements WhistAIInterface {

	final Log log = new Log(this);
	private final WhistAICallback callback;
	private float minimumDelay = 1f;
	private long startTime;

	AbstractWhistAI(WhistAICallback callback, float minimumDelay) {
		this.callback = callback;
		this.minimumDelay = minimumDelay;
	}

	@Override
	public void howManyToDeclare(final GameModel model, final int player, int restriction) {

		startTime = TimeUtils.millis();

		new Thread(){
			@Override
			public void run() {
				if (callback != null) {
					float dt;
					final int finalDeclaration = requestDeclaration(model);
					dt = (float)(TimeUtils.millis() - startTime)/1000f;
					if (dt < minimumDelay) {
						new Timer().scheduleTask(new Timer.Task() {
							@Override
							public void run() {
								callback.onDeclare(player, finalDeclaration);
							}
						}, minimumDelay - dt);
					} else {
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run() {
								callback.onDeclare(player, finalDeclaration);
							}
						});
					}
				}
			}
		}.start();
	}

	@Override
	public void whichCardToPlay(final GameModel model, final int player) {

		startTime = TimeUtils.millis();

		new Thread() {
			@Override
			public void run() {
				if(callback!=null)
				{
					float dt;
					final int finalCard = requestWhichCardToPlay(model);
					dt = (float) (TimeUtils.millis() - startTime) / 1000f;
					if (dt < minimumDelay) {
						new Timer().scheduleTask(new Timer.Task() {
							@Override
							public void run() {
								callback.onPlayCard(player, finalCard);
							}
						}, minimumDelay - dt);
					} else {
						Gdx.app.postRunnable(new Runnable() {
							@Override
							public void run() {
								callback.onPlayCard(player, finalCard);
							}
						});
					}
				}
			}
		}.start();
	}

	protected abstract int requestDeclaration(GameModel model);
	protected abstract int requestWhichCardToPlay(GameModel model);

}
