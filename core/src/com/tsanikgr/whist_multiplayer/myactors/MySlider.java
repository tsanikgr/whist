package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;

public class MySlider extends Slider implements MaskInterface {

	public static class SliderEvent extends Event {
		public final int bet;
		public final int difficulty;
		public final float value;

		public SliderEvent(Actor listenerActor, int bet, int difficulty) {
			this.bet = bet;
			this.difficulty = difficulty;
			this.value = -1f;
			setListenerActor(listenerActor);
		}

		public SliderEvent(Actor listenerActor, float value) {
			this.value = value;
			this.bet = -1;
			this.difficulty = -1;
			setListenerActor(listenerActor);
		}
	}

	private Mask mask = null;
	public MySlider(float min, float max, float stepSize, boolean vertical, SliderStyle style) {
		super(min, max, stepSize, vertical, style);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (mask != null) mask.startMasking(batch);
		super.draw(batch, parentAlpha);
		if (mask != null) mask.stopMasking(batch);
	}

	@Override
	public void setMask(Mask mask){
		this.mask = mask;
	}

}
