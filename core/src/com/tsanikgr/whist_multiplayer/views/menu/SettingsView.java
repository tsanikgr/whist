package com.tsanikgr.whist_multiplayer.views.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.tsanikgr.whist_multiplayer.models.SettingsModel;
import com.tsanikgr.whist_multiplayer.myactors.Geometry;
import com.tsanikgr.whist_multiplayer.myactors.MyButton;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;
import com.tsanikgr.whist_multiplayer.myactors.MySlider;
import com.tsanikgr.whist_multiplayer.views.View;

public class SettingsView extends View {

	public static final Color backColor = new Color(1f, 0.72f, 0.05f, 1.0f);
	private static final String LANGUAGE_SELECTED = "language_selected";
	public static final String LANGUAGE_PREFIX = "language_";
	public static final String CHECKBOX_VIBRATION = "checkbox_vibration";
	public static final String CHECKBOX_DEBUG = "checkbox_debug";
	public static final String CHECKBOX_CHAT = "checkbox_chat";
	public static final String SLIDER_ANIMATION_SPEED = "slider_animation";
	public static final String SLIDER_SOUND = "slider_sound";
	public static final String CARD_COLOR_PREFIX = "color_";
	public static final String CARD_COLOR_LABEL = "cards_label";
	private static final String LANGUAGE_LABEL = "language_label";
	private static final String[] CARD_COLORS = {"purple", "red", "yellow", "orange", "green", "cyan"};
	private static final Color languageButtonDisabledColor = new Color(0.4f,0.4f,0.4f,1.0f);

	private MyImage languageSelected;
	private Array<MyButton> languageButtons;
	private Array<MyButton> cardColorButtons;
	private MySlider soundSlider;
	private MySlider animationSpeedSlider;
	private MyButton vibrationCheckbox;
	private MyButton debugCheckbox;
	private MyButton chatCheckbox;

	private float languageSelectedOffsetX;
	private float languageSelectedOffsetY;
	private float lastSoundSliderValue = 1;
	private EventListener eventListener;

	public SettingsView(String name) {
		super(name);
	}
	@Override

	public void dispose() {
	}

	@Override
	protected void onAssetsLoaded(View view) {
		MyButton colorButton;
		cardColorButtons = new Array<>();
		for (String color : CARD_COLORS) {
			colorButton = findActor(CARD_COLOR_PREFIX+color);
			cardColorButtons.add(colorButton);
		}
		vibrationCheckbox = findActor(CHECKBOX_VIBRATION);
		debugCheckbox = findActor(CHECKBOX_DEBUG);
		chatCheckbox = findActor(CHECKBOX_CHAT);

		initSliders();
	}

	public void setEventListener(EventListener listener) {
		this.eventListener = listener;
	}

	private static final float[] ANIMATION_SPEED_STEPS = {0.8f, 1.0f, 1.2f, 1.4f, 1.6f, 1.8f, 2.0f, 2.2f, 2.4f};
	private static final float[] SOUND_VOLUME_STEPS = {0, 0.125f, 0.125f*2f, 0.125f*3f, 0.125f*4f, 0.125f*5f, 0.125f*6f, 0.125f*7f, 0.125f*8f };
	private void initSliders() {

		soundSlider = findActor(SLIDER_SOUND);

		soundSlider.setRange(0f, 1f);
		soundSlider.setStepSize(0.125f);
		soundSlider.setSnapToValues(SOUND_VOLUME_STEPS, 0.125f / 2f);
		soundSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (!(actor instanceof MySlider)) return;
				float value = ((MySlider) actor).getValue();
				if (lastSoundSliderValue != value) {
					MySlider.SliderEvent e = new MySlider.SliderEvent(soundSlider, value);
					if (eventListener != null) eventListener.handle(e);
				}
				lastSoundSliderValue = value;
			}
		});
		soundSlider.setSize(soundSlider.getStyle().background.getMinWidth(), soundSlider.getStyle().background.getMinHeight());

		animationSpeedSlider = findActor(SLIDER_ANIMATION_SPEED);

		animationSpeedSlider.setRange(0.8f, 2.4f);
		animationSpeedSlider.setStepSize(0.2f);
		animationSpeedSlider.setSnapToValues(ANIMATION_SPEED_STEPS, 0.1f);
		animationSpeedSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (!(actor instanceof MySlider)) return;
				float value = ((MySlider) actor).getValue();
				if (lastSoundSliderValue != value) {
					MySlider.SliderEvent e = new MySlider.SliderEvent(animationSpeedSlider, value);
					if (eventListener != null) eventListener.handle(e);
				}
				lastSoundSliderValue = value;
			}
		});
		soundSlider.setSize(animationSpeedSlider.getStyle().background.getMinWidth(), animationSpeedSlider.getStyle().background.getMinHeight());
	}

	public void initialiseLanguageButtons(String[] languageKeys){
		languageSelected = findActor(LANGUAGE_SELECTED);
		languageSelected.setTouchable(Touchable.disabled);
		languageButtons = new Array<>();
		MyButton languageButton;
		for (String languageKey : languageKeys) {
			languageButton = findActor(LANGUAGE_PREFIX + languageKey);
			languageButtons.add(languageButton);
			languageButton.setCheckedColor(languageButtonDisabledColor);
			//TODO:
			languageButton.getColor().a = 0.3f;
//			languageButton.setTouchable(Touchable.disabled);
		}
		findActor(LANGUAGE_LABEL).getColor().a = 0.3f;

		languageSelectedOffsetX = languageSelected.getX() - languageButtons.get(0).getX();
		languageSelectedOffsetY = languageSelected.getY() - languageButtons.get(0).getY();
	}

	public void updateView(SettingsModel settingsModel) {
		//TODO
		//updateLanguage(settingsModel.getLanguage());
		updateLanguage("en");
		updateCardColor(settingsModel.getCardColor());
		vibrationCheckbox.setChecked(settingsModel.isVibration());
		debugCheckbox.setChecked(settingsModel.isDebug());
		chatCheckbox.setChecked(settingsModel.isChat());

		animationSpeedSlider.setValue(settingsModel.getAnimationSpeed());
		soundSlider.setValue(settingsModel.getSoundVolume());
	}

	private void updateLanguage(String language) {
		MyButton button;
		for (int i = 0 ; i < languageButtons.size; i++) {
			button = languageButtons.get(i);
			if (button.getName().compareTo(LANGUAGE_PREFIX + language) == 0) {
				languageSelected.setPosition(button.getX() + languageSelectedOffsetX, button.getY() + languageSelectedOffsetY);
				Geometry.alignActors(Align.center, Align.center, button, languageSelected);
				button.setChecked(false);
			} else {
				button.setChecked(true);
			}
		}
	}

	private void updateCardColor(Color cardColor) {
		MyButton button;
		for (int i = 0 ; i < cardColorButtons.size ; i++) {
			button = cardColorButtons.get(i);
			if (button.getColor().equals(cardColor)) button.setChecked(true);
			else button.setChecked(false);
		}
	}

	@Override
	public Color getBackgroundColor() {
		return backColor;
	}
}
