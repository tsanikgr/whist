package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class MyLabel extends Label implements MaskInterface {

	private Mask mask = null;
	private int maxCharacters = -1;
	public MyLabel(CharSequence text, Skin skin) {
		super(text, skin);
	}
	public MyLabel(CharSequence text, Skin skin, String styleName) {
		super(text, skin, styleName);
	}
	public MyLabel(CharSequence text, Skin skin, String fontName, Color color) {
		super(text, skin, fontName, color);
	}
	public MyLabel(CharSequence text, Skin skin, String fontName, String colorName) {
		super(text, skin, fontName, colorName);
	}
	public MyLabel(CharSequence text, LabelStyle style) {
		super(text, style);
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

	public void setMaxCharacters(int maxCharacters) {
		this.maxCharacters = maxCharacters;
	}

	@Override
	public void setText(CharSequence newText) {
		if (maxCharacters != -1 && newText.length() > maxCharacters) {
			super.setText(newText.subSequence(0,maxCharacters) + "..");
		}
		else super.setText(newText);
	}
}
