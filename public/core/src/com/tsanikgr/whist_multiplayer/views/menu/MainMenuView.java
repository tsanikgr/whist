package com.tsanikgr.whist_multiplayer.views.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.myactors.Geometry;
import com.tsanikgr.whist_multiplayer.myactors.Mask;
import com.tsanikgr.whist_multiplayer.myactors.MyButton;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;
import com.tsanikgr.whist_multiplayer.myactors.MyTextButton;
import com.tsanikgr.whist_multiplayer.myactors.PointRevealer;
import com.tsanikgr.whist_multiplayer.views.View;

public class MainMenuView extends View {

	public static final String NEW_GAME = "local_game";
	public static final String ONLINE_GAME = "online_game";
	public static final String SHARE = "button_share";
	public static final String RATE = "button_rate";
	public static final String TUTORIAL = "button_tutorial";
	public static final String SETTINGS = "button_settings";
	private static final String USER_LAYER = "player";
	public static final String ACHIEVEMENTS = "button_achievements";
	public static final String LEADERBOARDS = "button_leaderboards";
	private static final String STAR_MASK = "main_screen_star_mask";
	private static final String LOGO_TITLE = "logo_title";

	private MyTextButton onlineGame;
	private MyButton achievements, leaderboards;
	private MyImage star;
	private PointRevealer revealer1, revealer2;

	public MainMenuView(String xmlFile, String name) {
		super(xmlFile, name);
	}

	@Override
	public void dispose() {

	}

	@Override
	protected void onAssetsLoaded(View view) {
		initActors();
	}

	private void initActors() {
		star = findActor(STAR_MASK);
		star.remove();
//		fixOriginRecursively(this);         //this produces a mysterious error with the Point Revealers
		MyTextButton newGame = findActor(NEW_GAME);
		Geometry.fixOrigin(newGame);
		onlineGame = findActor(ONLINE_GAME);
		Geometry.fixOrigin(onlineGame);
		Geometry.fixOrigin(findActor(LOGO_TITLE));
		MyButton settings = findActor(SETTINGS);
		MyButton rate = findActor(RATE);
		MyButton share = findActor(SHARE);
		MyButton tutorial = findActor(TUTORIAL);
		achievements = findActor(ACHIEVEMENTS);
		leaderboards = findActor(LEADERBOARDS);
		achievements.setPressedColor(AchievementsView.backColor);
		leaderboards.setPressedColor(LeaderBoardsView.backColor);
		settings.setPressedColor(SettingsView.backColor);
		onlineGame.setTouchable(Touchable.disabled);
		leaderboards.setVisible(false);
		achievements.setVisible(false);
	}

	public void setStarMask(IResolution resolution) {
		MyImage star2 = new MyImage(star.getDrawable());
		star2.setBounds(star.getX(), star.getY(), star.getWidth(), star.getHeight());
		Mask maskStar1 = new Mask(star, true, resolution.getGameAreaPosition());
		Mask maskStar2 = new Mask(star2, true, resolution.getGameAreaPosition());

		Vector2 achievementsPos = new Vector2(achievements.getWidth()/2, achievements.getHeight()/2 * 13/10);
		achievements.localToStageCoordinates(achievementsPos);
		Vector2 leaderboardsPos = new Vector2(leaderboards.getWidth()/2, leaderboards.getHeight()/2 * 13/10);
		leaderboards.localToStageCoordinates(leaderboardsPos);

		revealer1 = new PointRevealer(resolution.getGameAreaPosition());
		revealer2 = new PointRevealer(resolution.getGameAreaPosition());
		revealer1.config()
				.setToReveal(achievements)
				.setDuration(1.75f)
				.setInitScale(0f)
				.setFinalScale(1f)
				.setInterpolation(Interpolation.pow4In)
				.setMask(maskStar1)
				.set(achievementsPos.x, achievementsPos.y);

		revealer2.config()
				.setToReveal(leaderboards)
				.setDuration(1.75f)
				.setInitScale(0f)
				.setFinalScale(1f)
				.setInterpolation(Interpolation.pow4In)
				.setMask(maskStar2)
				.set(leaderboardsPos.x, leaderboardsPos.y);
	}

	@Override
	public Color getBackgroundColor() {
		return new Color(0.4f, 0.4f, 0.4f, 1f);
	}

	public void startShowAnimation() {
		MyGroup mainScreenGroup = findActor("main_screen");

		leaderboards.setVisible(false);
		achievements.setVisible(false);
		int i = 0;
		for (Actor actor : mainScreenGroup.getChildren()) {
			if (actor == achievements || actor == leaderboards) continue;
			if (actor instanceof Table) ((Table) actor).setTransform(true);
			actor.addAction(Actions.scaleTo(0.85f,0.85f,0f));
			actor.addAction(Actions.sequence(Actions.delay(0.8f + 0.2f * i), Actions.scaleTo(actor.getScaleX(), actor.getScaleY(), 1f, Interpolation.bounceOut)));
			i++;
		}

		revealer1.startAnimation();
		revealer2.startAnimation();

//		new Timer().scheduleTask(new Timer.Task() {
//			@Override
//			public void run() {
//				revealer1.startAnimation();
//				revealer2.startAnimation();
//			}
//		}, 0.5f);
	}

	public void enableMultiplayer(boolean enable) {
		if (enable) onlineGame.setTouchable(Touchable.enabled);
		else {
			onlineGame.setTouchable(Touchable.disabled);
			onlineGame.setColor(1f,1f,1f,0.5f);
		}
	}
}
