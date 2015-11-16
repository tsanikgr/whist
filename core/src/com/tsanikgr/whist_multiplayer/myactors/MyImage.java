package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Scaling;

public class MyImage extends Image implements MaskInterface {

	private Mask mask = null;
	public MyImage(NinePatch patch) {
		super(patch);
	}
	public MyImage(TextureRegion textureRegion) {
		super(textureRegion);
	}
	public MyImage(Texture texture) {
		super(texture);
	}
	public MyImage(Skin skin, String drawableName) {
		super(skin, drawableName);
	}
	public MyImage(Drawable drawable) {
		super(drawable);
	}
	public MyImage(Drawable drawable, Scaling scaling) {
		super(drawable, scaling);
	}
	public MyImage(Drawable drawable, Scaling scaling, int align) {
		super(drawable, scaling, align);
	}
	public MyImage(NinePatchDrawable ninePatchDrawable) {
		super(ninePatchDrawable);
	}

	public MyImage() {
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

	public static MyImage fromPrototype(MyImage imagePrototype) {
		MyImage image = new MyImage(imagePrototype.getDrawable());
		image.setBounds(imagePrototype.getX(),
				imagePrototype.getY(),
				imagePrototype.getWidth(),
				imagePrototype.getHeight());
		image.setScaleX(imagePrototype.getScaleX());
		image.setScaleY(imagePrototype.getScaleY());
		image.setRotation(imagePrototype.getRotation());
		image.setOrigin(imagePrototype.getOriginX(), imagePrototype.getOriginY());
		return image;
	}
}
