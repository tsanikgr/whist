package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Timer;
import com.tsanikgr.whist_multiplayer.Config;
import com.tsanikgr.whist_multiplayer.ISettings;
import com.tsanikgr.whist_multiplayer.models.SettingsModel;

public class Settings extends Controller implements ISettings{

	private static final String PREFERRED_LANGUAGE = "prlg";
	private SettingsModel settingsModel;
	private final Timer delayedVibrationTimer = new Timer();

	public Settings() {
		settingsModel = new SettingsModel();
	}

	@Override
	public void init() {
		load();
	}

	@Override
	public void disposeController() {
	}

	@Override
	public SettingsModel getSettingsModel(){
		if (settingsModel == null) load();
		return settingsModel;
	}
	@Override
	public Color getCardColor() {
		return getSettingsModel().getCardColor();
	}

	private void load() {
		settingsModel = SettingsModel.deserialise(getStorage().load("settings"), SettingsModel.class);
		if (settingsModel != null) log.i().append("Settings loaded succesfully").print();
		else {
			log.w().append("Failed to load settings, setting default").print();
			settingsModel = new SettingsModel();
		}
		apply();
		if (getMenuController() != null)
			getMenuController().updateSettingsView(settingsModel);
	}

	private void save() {
		getStorage().save("settings", getSettingsModel().serialise());
		getMenuController().updateSettingsView(settingsModel);
	}

	private void apply(){
		getSettingsModel();
//		getAssets().getLocalization().setPreferredLanguage(settingsModel.getLanguage());
		getSounds().setVolume(settingsModel.getSoundVolume());
		Config.GLOBAL_SPEED = settingsModel.getAnimationSpeed();
		//vibration is handled herein
		//chat is handled herein
		Config.setDebugFromSettings(settingsModel.isDebug());
	}

	@Override
	public void handleSoundChange(float volume) {
		getSettingsModel().setSoundVolume(volume);
		getSounds().setVolume(settingsModel.getSoundVolume());
		save();
	}

	@Override
	public void handleAnimationSpeedChange(float animationSpeed) {
		getSettingsModel().setAnimationSpeed(animationSpeed);
		Config.GLOBAL_SPEED = settingsModel.getAnimationSpeed();
		save();
	}

	@Override
	public void handleVibrationClick(boolean vibration) {
		getSettingsModel().setVibration(vibration);
		Gdx.input.vibrate(200);
		save();
	}

	@Override
	public void handleDebugClick(boolean checked) {
		getSettingsModel().setDebug(checked);
		Config.setDebugFromSettings(settingsModel.isDebug());
		save();
	}

	@Override
	public void handleChatClick(boolean chat) {
		getSettingsModel().setChat(chat);
		save();
	}

	@Override
	public void handleLanguageClick(String language) {
		//TODO
		language = language.substring(language.lastIndexOf("_")+1, language.length());
//		if (getAssets().getLocalization().setPreferredLanguage(language))
//			getStorage().save(PREFERRED_LANGUAGE, language);

		getMenuController().updateSettingsView(getSettingsModel());
	}

	@Override
	public void handleCardColorClick(Color cardColor) {
		if (cardColor.equals(getSettingsModel().getCardColor())) return;
		getSettingsModel().setCardColor(cardColor);
		getCardController().setCardColor(cardColor, true);
		save();
	}

	@Override
	public void doVibrate(int ms) {
		if (getSettingsModel().isVibration()) Gdx.input.vibrate(ms);
	}

	@Override
	public void doVibrate(final int ms, long delayMillis) {
		if (!getSettingsModel().isVibration()) return;
		delayedVibrationTimer.scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				Gdx.input.vibrate(ms);
			}
		}, (float)delayMillis/1000f);
	}
}
