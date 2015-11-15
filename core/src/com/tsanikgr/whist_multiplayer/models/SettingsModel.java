package com.tsanikgr.whist_multiplayer.models;

import com.badlogic.gdx.graphics.Color;

public class SettingsModel extends JsonSerialisable<SettingsModel>{
	private static final Color DEFAULT_CARD_COLOR = new Color(0.44f, 0.74f, 1f, 1f);

	private boolean debug;
	private boolean vibration;
	private boolean chat;
	private float soundVolume;
	private float animationSpeed;
	private Color cardColor;

	public SettingsModel() {
		super(SettingsModel.class);
		setDefaults();
	}

	private void setDefaults() {
		cardColor = DEFAULT_CARD_COLOR;
		soundVolume = 1.0f;
		animationSpeed = 1.2f;
		vibration = true;
		debug = false;
		chat = true;
	}

	public float getSoundVolume() {
		return soundVolume;
	}
	public void setSoundVolume(float volume) {
		this.soundVolume = volume;
	}
	public boolean isChat() {
		return chat;
	}
	public void setChat(boolean onOrOff) {
		chat = onOrOff;
	}
	public boolean isVibration() {
		return vibration;
	}
	public void setVibration(boolean vibration) {
		this.vibration = vibration;
	}
	public float getAnimationSpeed(){
		return animationSpeed;
	}
	public void setAnimationSpeed(float animationSpeed) {
		this.animationSpeed = animationSpeed;
	}
	public Color getCardColor(){
		return cardColor;
	}
	public void setCardColor(Color cardColor) {
		this.cardColor = cardColor;
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
