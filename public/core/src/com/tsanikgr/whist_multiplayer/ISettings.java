package com.tsanikgr.whist_multiplayer;

import com.badlogic.gdx.graphics.Color;
import com.tsanikgr.whist_multiplayer.models.SettingsModel;

public interface ISettings {
	void handleLanguageClick(String language);
	void handleCardColorClick(Color cardColor);

	void handleSoundChange(float volume);
	void handleAnimationSpeedChange(float animationSpeed);

	void handleVibrationClick(boolean checked);
	void handleDebugClick(boolean checked);
	void handleChatClick(boolean chat);

	void doVibrate(int ms);
	void doVibrate(int ms, long delay);
	SettingsModel getSettingsModel();
	Color getCardColor();
}
