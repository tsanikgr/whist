package com.tsanikgr.whist_multiplayer.controllers;


import com.badlogic.gdx.audio.Sound;
import com.tsanikgr.whist_multiplayer.ISounds;
import com.tsanikgr.whist_multiplayer.util.Log;

public class Sounds extends Controller implements ISounds{
	private boolean enable;
	private float volume;
	private Sound deal, pop, tick, turn;

	public Sounds() {
		volume = 1.0f;
	}

	@Override
	public void init() {
		enable = true;
	}

	@Override
	public void disposeController() {
		if (deal == null) return;
		deal.dispose();
		pop.dispose();
		tick.dispose();
		turn.dispose();
	}

	@Override
	public void soundsLoaded(){
		deal = getAssets().getSound("deal.mp3");
		pop = getAssets().getSound("pop.mp3");
		tick = getAssets().getSound("tick.mp3");
		turn = getAssets().getSound("turn.mp3");
		Log.setErrorSound(getAssets().getSound("error.mp3"));
	}

	@Override
	public void playDealSound(){
		if (!enable || deal == null) return;
		deal.play(volume);
	}

	@Override
	public void playPopSound(){
		if (!enable || pop == null) return;
		pop.play(volume);
	}

	@Override
	public void playTickSound(){
		if (!enable || tick == null) return;
		tick.play(volume);
	}

	@Override
	public void playTurnSound(){
		if (!enable || turn == null) return;
		turn.play(volume);
	}

	@Override
	public void setVolume(float volume) {
		this.volume = volume*volume*volume; //if the volume is set linearly, low volume is really high in android

		enable = volume > 0.00001f;
	}
}
