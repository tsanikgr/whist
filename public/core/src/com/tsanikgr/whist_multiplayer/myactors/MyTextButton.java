package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class MyTextButton extends TextButton implements MaskInterface {

	private Mask mask = null;
	private Color pressedColor = null;
	private Color checkedColor = null;
	private Color cachedColor = null;
	private int maxCharacters = -1;

	public MyTextButton(String text, Skin skin) {
		super(text, skin);
	}
	public MyTextButton(String text, Skin skin, String styleName) {
		super(text, skin, styleName);
	}
	public MyTextButton(String text, TextButtonStyle style) {
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

	@Override
	public void setColor(Color color) {
		super.setColor(color);
		if (cachedColor == null) cachedColor = new Color(1,1,1,1);
		cachedColor.set(color);
	}

	private void setTempColor(Color color) {
		if (color == null) return;
		super.setColor(color);
	}

	@Override
	public boolean isPressed() {
		boolean isPressed = super.isPressed();
		if (isPressed && pressedColor != null) setTempColor(pressedColor);
		else if (isChecked() && checkedColor != null) setTempColor(checkedColor);
		else setTempColor(cachedColor);
		return isPressed;
	}

	public void setPressedColor(Color color) {
		this.pressedColor = color;
	}

	public void setCheckedColor(Color color) {
		this.checkedColor = color;
	}

	public void setMaxCharacters(int maxCharacters) {
		this.maxCharacters = maxCharacters;
	}

	@Override
	public void setText(String newText) {
		if (maxCharacters != -1 && newText.length() > maxCharacters) super.setText(newText.subSequence(0,maxCharacters) + "..");
		else super.setText(newText);
	}
}
