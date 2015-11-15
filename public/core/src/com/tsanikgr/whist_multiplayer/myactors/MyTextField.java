package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class MyTextField extends TextField implements MaskInterface {

	private Mask mask = null;

	public MyTextField(String text, TextFieldStyle style) {
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

}
