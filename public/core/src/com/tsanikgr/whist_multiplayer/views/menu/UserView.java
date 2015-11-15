package com.tsanikgr.whist_multiplayer.views.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.IStatistics;
import com.tsanikgr.whist_multiplayer.models.StatisticsModel;
import com.tsanikgr.whist_multiplayer.myactors.Animator;
import com.tsanikgr.whist_multiplayer.myactors.Geometry;
import com.tsanikgr.whist_multiplayer.myactors.Mask;
import com.tsanikgr.whist_multiplayer.myactors.MyButton;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;
import com.tsanikgr.whist_multiplayer.myactors.MyLabel;
import com.tsanikgr.whist_multiplayer.myactors.MyTextField;
import com.tsanikgr.whist_multiplayer.myactors.PointRevealer;
import com.tsanikgr.whist_multiplayer.views.View;

public class UserView extends View implements IStatistics.IStatisticsObserver {

	private static final String PLAYER_ICON = "player_icon";
	public static final String PLAYER_NAME = "player_name";
	private static final String COINS_COINS_LABEL = "coins_coins_label";
	private static final String SCORE_SCORE_LABEL = "score_score_label";
	private static final String SCORE_PERCENT_LABEL = "score_percent_label";
	private static final String COINS_REFILL_LABEL = "coins_refill_label";
	private static final String ONLINE_IMAGE_GROUP = "online_image_group";

	public static final String COINS = "coins_icon";
	private static final String COINS_LABEL = "coins_label";
	public static final String STATS = "score";
	private static final String USER_SCORE = "score_label";

	private MyTextField nameField;
	private MyLabel scorePercent;
	private MyLabel score;
	private MyLabel refill;
	private MyLabel coins;

	private MyGroup onlineImageGroup;
	private MyImage offlineImage, onlineImage;

	public UserView(String xmlFile, String name) {
		super(xmlFile, name);
	}

	@Override
	public void dispose() {

	}

	@Override
	protected void onAssetsLoaded(View view) {
		fixOriginRecursively(view);
		initActors();
	}


	private void initActors() {
		scorePercent = findActor(SCORE_PERCENT_LABEL); //top 19%
		score = findActor(SCORE_SCORE_LABEL); //1250
		refill = findActor(COINS_REFILL_LABEL); //12h 20m
		coins = findActor(COINS_COINS_LABEL); //400

		nameField = findActor(PLAYER_NAME);
		nameField.setMaxLength(12);
		nameField.setAlignment(Align.center);
		nameField.setOnlyFontChars(true);
		nameField.setTextFieldFilter(new TextField.TextFieldFilter() {
			@Override
			public boolean acceptChar(TextField textField, char c) {
				return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
			}
		});
		nameField.setBlinkTime(0.6f);
		nameField.setText(StatisticsModel.DEFAULT_NICKNAME);
		nameField.setMessageText("<nickname>");
		nameField.getStyle().messageFontColor = new Color(1.0f,0.5f,0.5f,1.0f);

		MyButton stats = findActor(STATS);
		MyButton coinsButton = findActor(COINS);
		MyLabel userScore = findActor(USER_SCORE);
		MyLabel userCoins = findActor(COINS_LABEL);

		offlineImage = findActor(PLAYER_ICON);
		onlineImageGroup = findActor(ONLINE_IMAGE_GROUP);
		onlineImageGroup.setVisible(false);
		Geometry.fixOrigin(onlineImageGroup);
		onlineImage = null;

		coinsButton.setPressedColor(CoinsView.backColor);
		stats.setPressedColor(StatisticsView.backColor);
	}

	@Override
	public void onStatisticsChanged(StatisticsModel stats) {
		nameField.setText(stats.getName());
//		nameField.pack();
//		Geometry.alignActors(Align.center,0,findActor(PLAYER_ICON),nameField);
		scorePercent.setText("top " + stats.getScorePercent() + "%");
		score.setText(Integer.toString(stats.getELOScore()));
		int lastHours = stats.getRemainingHours();
		int lastMinutes = stats.getRemainingMinutes();
		refill.setText(lastHours + "h " + lastMinutes + "m");
		coins.setText(Integer.toString(stats.getCoins()));
	}

	@Override
	public Color getBackgroundColor() {
		return null;
	}

	public void askForNickname() {
		if (nameField.getStage() != null) nameField.getStage().setKeyboardFocus(nameField);
		nameField.getOnscreenKeyboard().show(true);
		nameField.setText("<nickname>");
		nameField.setSelection(0, nameField.getText().length());
	}

	public void setPlayerImage(byte[] image, final Mask mask, final IResolution resolution) {
		if (image == null) return;

		final Pixmap pixmap = new Pixmap(image, 0, image.length);
		final int width = pixmap.getWidth();
		final int height = pixmap.getHeight();

		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (onlineImage != null) disposeOnlineImage();
				onlineImage = new MyImage(new TextureRegion(new Texture(pixmap), 0, 0, width, height));
				onlineImageGroup.addActor(onlineImage);
				onlineImage.setZIndex(0);
				onlineImage.setSize(width * onlineImageGroup.getHeight() / height, onlineImageGroup.getHeight());
				if (onlineImage.getWidth() > onlineImageGroup.getWidth())
					Geometry.alignActors(Align.right, 0, onlineImageGroup, onlineImage);
				else Geometry.alignActors(Align.center, 0, onlineImageGroup, onlineImage);
				onlineImageGroup.setVisible(true);

				PointRevealer revealer = new PointRevealer(resolution.getGameAreaPosition());
				revealer.config()
						.setToReveal(onlineImageGroup)
						.setDuration(2f)
						.setInitScale(0.001f)
						.setFinalScale(0.2f)
						.setInterpolation(Interpolation.pow5In)
						.setMask(mask)
						.set(onlineImageGroup.getX() + onlineImageGroup.getOriginX(), onlineImageGroup.getY() + onlineImageGroup.getOriginY())
						.setListener(new Animator.AnimatorFinishedListener() {
							@Override
							public void onAnimationFinished(boolean completedSuccesfully) {
								offlineImage.setVisible(false);
							}
						});

				revealer.startAnimation();
			}
		});
	}

	private void disposeOnlineImage(){
		onlineImage.remove();
		((TextureRegionDrawable)onlineImage.getDrawable()).getRegion().getTexture().dispose();
		onlineImage = null;
	}

	public void removePlayerImage(final Mask mask, IResolution resolution) {
		if (onlineImage == null) return;
		PointRevealer revealer = new PointRevealer(resolution.getGameAreaPosition());
		revealer.config()
				.setToReveal(onlineImageGroup)
				.setDuration(2f)
				.setInitScale(0.001f)
				.setFinalScale(0.2f)
				.setReverse(true)
				.setInterpolation(Interpolation.pow5In)
				.setMask(mask)
				.set(onlineImageGroup.getX() + onlineImageGroup.getOriginX(), onlineImageGroup.getY() + onlineImageGroup.getOriginY())
				.setListener(new Animator.AnimatorFinishedListener() {
					@Override
					public void onAnimationFinished(boolean completedSuccesfully) {
						onlineImageGroup.setVisible(false);
						disposeOnlineImage();
					}
				});

		revealer.startAnimation();
		offlineImage.setVisible(true);
		offlineImage.getColor().a = 0;
		offlineImage.addAction(Actions.fadeIn(2f, Interpolation.pow5In));
	}
}
