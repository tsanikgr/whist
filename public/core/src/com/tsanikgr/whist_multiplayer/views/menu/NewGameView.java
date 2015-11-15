package com.tsanikgr.whist_multiplayer.views.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.myactors.Geometry;
import com.tsanikgr.whist_multiplayer.myactors.MyButton;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;
import com.tsanikgr.whist_multiplayer.myactors.MyLabel;
import com.tsanikgr.whist_multiplayer.myactors.MySlider;
import com.tsanikgr.whist_multiplayer.views.View;

public class NewGameView extends View {

	private static final Color backColor = new Color(107f/255f, 194f/255f, 237f/255f, 1f);

	private static final String NEW_GAME_BET = "local_multi_common_bet";
	private static final String NEW_GAME_DIFFICULTY = "local_multi_common_difficulty";
	public static final String SLIDER = "bet_slider";
	public static final String ROUND13_CHECKBOX = "checkbox_round13s";
	public static final String SKIP_ROUNDS_CHECKBOX = "checkbox_restriction";
	public static final String ROUND13_LINK = "local_multi_common_round13s_label";
	public static final String SKIP_ROUNDS_LINK = "local_multi_common_restriction_label";
	public static final String START_BUTTON = "start";
	private static final String ICON_EASY = "icons_easy";
	private static final String ICON_MEDIUM = "icons_medium";
	private static final String ICON_HARD = "icons_hard";
	private static final String ICON_VERY_HARD = "icons_very_hard";
	private static final String ICON_EXPERT = "icons_expert";
	private static final String LOCAL_GROUP = "local";
	private static final String MULTIPLAYER_GROUP = "multiplayer";
	public static final String QUICK_GAME = "quick_game";
	public static final String INVTITATIONS = "invitations";
	public static final String INVITE = "invite";

	private MyGroup localGroup, multiplayerGroup;
	private MyButton round13sCheckbox, skipRoundsCheckbox;

	private MySlider slider;
	private MyImage[] difficultyIcons;
	private MyLabel newGameBet, newGameDifficulty;

	private boolean skipRounds, play13s;
	private int difficulty, bet;
	private EventListener eventListener = null;

	public NewGameView(String name) {
		super(name);
	}

	@Override
	public void dispose() {

	}

	public void setEventListener(EventListener eventListener) {
		this.eventListener = eventListener;
	}

	@Override
	protected void onAssetsLoaded(View view) {
		initActors();
	}

	private static final float[] STEPS = {1,2,3,4,5,6,7,8,9};
	private float betInitY;
	private float difInitY;
	private void initActors() {
		fixOriginRecursively(this);
		difficultyIcons = new MyImage[5];

		difficultyIcons[0] = findActor(ICON_EASY);
		difficultyIcons[1] = findActor(ICON_MEDIUM);
		difficultyIcons[2] = findActor(ICON_HARD);
		difficultyIcons[3] = findActor(ICON_VERY_HARD);
		difficultyIcons[4] = findActor(ICON_EXPERT);
		newGameDifficulty = findActor(NEW_GAME_DIFFICULTY);
		newGameBet = findActor(NEW_GAME_BET);

		round13sCheckbox = findActor(ROUND13_CHECKBOX);
		skipRoundsCheckbox = findActor(SKIP_ROUNDS_CHECKBOX);
		MyButton startButton = findActor(START_BUTTON);
		newGameBet = findActor(NEW_GAME_BET);
		newGameDifficulty = findActor(NEW_GAME_DIFFICULTY);

		multiplayerGroup = findActor(MULTIPLAYER_GROUP);
		localGroup = findActor(LOCAL_GROUP);

		multiplayerGroup.setVisible(false);

		betInitY = newGameBet.getY();
		difInitY = newGameDifficulty.getY();

		initSlider();
		initCheckboxes();
	}

	private void initCheckboxes() {
		round13sCheckbox.setChecked(true);
		skipRoundsCheckbox.setChecked(false);
		MyLabel skipRoundsLink = findActor(SKIP_ROUNDS_LINK);
		MyLabel round13sLink = findActor(ROUND13_LINK);

		skipRoundsLink.setTouchable(Touchable.enabled);
		skipRoundsLink.addListener(new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				skipRoundsCheckbox.setChecked(!skipRoundsCheckbox.isChecked());
				if (eventListener != null) {
					event.setRelatedActor(skipRoundsCheckbox);
					eventListener.handle(event);
				}
				super.clicked(event, x, y);
			}
		});

		round13sLink.setTouchable(Touchable.enabled);
		round13sLink.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				round13sCheckbox.setChecked(!round13sCheckbox.isChecked());
				if (eventListener != null) {
					event.setRelatedActor(round13sCheckbox);
					eventListener.handle(event);
				}
				super.clicked(event, x, y);
			}
		});
	}


	private float lastSliderValue = 1;
	private void initSlider() {
		bet = 20;
		slider = findActor(SLIDER);
		slider.setRange(STEPS[0], STEPS[8]);
		slider.setSnapToValues(STEPS, 0.5f);
		slider.setStepSize(1.0f);
		slider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (!(actor instanceof MySlider)) return;
				onSliderChanged(((MySlider) actor).getValue());
			}
		});

		slider.setSize(slider.getStyle().background.getMinWidth(), slider.getStyle().background.getMinHeight());
		slider.toFront();
		onSliderChanged(1f);
	}

	private void onSliderChanged(float value) {

		for (int i = 0 ; i < 5; i++) difficultyIcons[i].setVisible(false);
		difficultyIcons[(int)((value-1f)/2f)].setVisible(true);
		difficultyIcons[(int)((value-1f)/2f)].clearActions();
		difficultyIcons[(int)((value-1f)/2f)].getColor().a = 0.5f;
		difficultyIcons[(int)((value-1f)/2f)].addAction(Actions.fadeIn(0.3f,Interpolation.fade));

		String text;
		float startX = newGameBet.getX() + newGameBet.getWidth();

		switch ((int)value-1){
			case 0: bet = 20;
				newGameBet.setText("20");
				text = "Easy";
				break;
			case 1: bet = 50;
				newGameBet.setText("50");
				text = "Easy";
				break;
			case 2: bet = 100;
				newGameBet.setText("100");
				text = "Medium";
				break;
			case 3: bet = 500;
				newGameBet.setText("500");
				text = "Medium";
				break;
			case 4: bet = 2500;
				newGameBet.setText("2500");
				text = "Hard";
				break;
			case 5: bet = 10000;
				newGameBet.setText("10K");
				text = "Hard";
				break;
			case 6: bet = 100000;
				newGameBet.setText("100K");
				text = "Pro";
				break;
			case 7: bet = 250000;
				newGameBet.setText("250K");
				text = "Pro";
				break;
			default: bet = 1000000;
				newGameBet.setText("1M");
				text = "Expert";
				break;
		}
		newGameBet.pack();
		newGameBet.setX(startX - newGameBet.getWidth());

		text += "(" + (int)value + "/9)";
		newGameDifficulty.setText(text);
		newGameDifficulty.pack();
		Geometry.alignActors(Align.right, 0, newGameBet, newGameDifficulty);

		animateSliderChanges((int) lastSliderValue - (int) value);
		if (lastSliderValue != value) {
			MySlider.SliderEvent event = new MySlider.SliderEvent(slider, bet, (int) value - 1);
			if (eventListener != null) eventListener.handle(event);
		}
		lastSliderValue = value;
	}

	private void animateSliderChanges(int bigger) {

		if (bigger == 0) return;

		ParallelAction betAnimation, difAnimation;
		ParallelAction action;

		betAnimation = Actions.parallel();
		difAnimation = Actions.parallel();

		Actor actor;
		float y;
		float d = 1.0f;
		for (int i = 0 ; i < 2 ; i++) {
			if (i == 0) {
				action = difAnimation;
				actor = newGameDifficulty;
				y = difInitY;
			}
			else {
				action = betAnimation;
				actor = newGameBet;
				y = betInitY;
			}
			actor.clearActions();

//			if (bigger > 0) {
//				action.addAction(Actions.sequence(Actions.color(Color.LIME, d/5f, Interpolation.fade), Actions.color(Color.WHITE, d/2f, Interpolation.fade)));
//				action.addAction(Actions.moveTo(actor.getX(), y + 15f));
//				action.addAction(Actions.moveBy(0f, -15f, d, Interpolation.fade));
//			} else {
				action.addAction(Actions.sequence(Actions.color(Color.RED, d / 5f, Interpolation.fade), Actions.color(Color.WHITE, d / 2f, Interpolation.fade)));
//				action.addAction(Actions.moveTo(actor.getX(),y-15f));
//				action.addAction(Actions.moveBy(0f, 15f, d, Interpolation.fade));
//			}
			action.addAction(Actions.alpha(0.5f));
			action.addAction(Actions.fadeIn(d, Interpolation.fade));
		}

		newGameBet.addAction(betAnimation);
		newGameDifficulty.addAction(difAnimation);
	}

	@Override
	public Color getBackgroundColor() {
		return backColor;
	}

	public void chooseMode(boolean isLocalGame) {
		localGroup.setVisible(isLocalGame);
		multiplayerGroup.setVisible(!isLocalGame);
	}

	public void setRoomConfigFromUI(GameModel.WhistRoomConfig roomConfig) {
		roomConfig.bet = bet;
		roomConfig.difficulty = (int)slider.getValue()-1;
		roomConfig.skipRounds = skipRoundsCheckbox.isChecked();
		roomConfig.has13s = round13sCheckbox.isChecked();
	}
}
