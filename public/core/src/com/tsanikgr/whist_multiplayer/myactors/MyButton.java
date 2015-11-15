package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class MyButton extends Button implements MaskInterface {

	private final Vector2 stageCoords = new Vector2();
	private Mask mask = null;
	private Color pressedColor = null;
	private Color checkedColor = null;
	private Color cachedColor = null;

	MyButton() {}
	public MyButton(Drawable up, Drawable down) {
		super(up, down);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (mask != null) mask.startMasking(batch);
		super.draw(batch, parentAlpha);
		if (mask != null) {
			if (isTransform()) mask.stopMasking(batch);
			else {
				stageCoords.set(0, 0);
				localToStageCoordinates(stageCoords);
				stageCoords.sub(getX(), getY());
				mask.stopMasking(batch, stageCoords);
			}
		}
	}

	@Override
	public void setMask(Mask mask){
		this.mask = mask;
	}

	@Override
	public void setColor(Color color) {
		if (color == null) return;
		if (cachedColor == null) cachedColor = new Color(1, 1, 1, 1);
		if (!color.equals(checkedColor) && !color.equals(pressedColor)) cachedColor.set(color);
		super.setColor(color);
	}

	@Override
	public boolean isPressed() {
		boolean isPressed = super.isPressed();
		if (isPressed && pressedColor != null) setColor(pressedColor);
		else if (isChecked() && checkedColor != null)	setColor(checkedColor);
		else if (!getColor().equals(cachedColor) && (getColor().equals(pressedColor) || getColor().equals(checkedColor))) setColor(cachedColor);
		return isPressed;
	}

	public void setPressedColor(Color color) {
		this.pressedColor = color;
	}

	public void setCheckedColor(Color color) {
		this.checkedColor = color;
	}
}
