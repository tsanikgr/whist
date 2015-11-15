package com.tsanikgr.whist_multiplayer.stage_builder.builders;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.ImageModel;

public class ImageBuilder extends ActorBuilder {


	public ImageBuilder(IAssets assets, IResolution resolution, LocalizationService localizationService) {
		super(assets, resolution, localizationService);
	}

	@Override
	public Actor build(BaseActorModel model) {
		ImageModel imageModel = (ImageModel) model;
		MyImage image;
		if (imageModel.getTextureSrc() != null) {
			image = createFromTexture(imageModel);
		} else {
			image = createFromTextureAtlas(imageModel);
		}

		normalizeModelSize(imageModel,
				image.getDrawable().getMinWidth(),
				image.getDrawable().getMinHeight());


		setBasicProperties(model, image);

		if (ImageModel.TYPE_BACKGROUND.equals(imageModel.getType())) {
			updateBackgroundImagePosition(image);
		}

		return image;
	}

	private void updateBackgroundImagePosition(Image image) {
		Vector2 selectedResolution = assets.getResolver().findBestResolution();
		Vector2 backGroundSize = resolution.calculateBackgroundSize(selectedResolution.x, selectedResolution.y);
		image.setWidth(backGroundSize.x);
		image.setHeight(backGroundSize.y);

		Vector2 backGroundPosition = resolution.calculateBackgroundPosition(image.getWidth(), image.getHeight());
		Vector2 gameAreaPosition = resolution.getGameAreaPosition();
          /*
  		 * stage root position is always set to gameAreaPosition.
  		 * Since the bg image is also inside the root group, bg image position should be updated.
		 */
		image.setPosition(backGroundPosition.x - gameAreaPosition.x, backGroundPosition.y - gameAreaPosition.y);
	}

	private MyImage createFromTexture(ImageModel imageModel) {
		if(imageModel.getNinepatch()){
			NinePatchDrawable ninePatchDrawable = new NinePatchDrawable();

			NinePatch patch;
			if (imageModel.getNinepatchOffset() == 0) {

				patch = new NinePatch(new TextureRegion(assets.getTexture(getLocalizedString(imageModel.getTextureSrc()))),
						imageModel.getNinepatchOffsetLeft(), imageModel.getNinepatchOffsetRight(), imageModel.getNinepatchOffsetTop(), imageModel.getNinepatchOffsetBottom());
			} else {
				patch = new NinePatch(new TextureRegion(assets.getTexture(getLocalizedString(imageModel.getTextureSrc()))),
						imageModel.getNinepatchOffset(), imageModel.getNinepatchOffset(), imageModel.getNinepatchOffset(), imageModel.getNinepatchOffset());
			}

			ninePatchDrawable.setPatch(patch);
			return new MyImage(patch);
		}else{
			TextureRegion textureRegion = new TextureRegion(assets.getTexture(getLocalizedString(imageModel.getTextureSrc())));
			return new MyImage(textureRegion);
		}
	}

	private MyImage createFromTextureAtlas(ImageModel imageModel) {
		if(imageModel.getNinepatch()){
			if (imageModel.getNinepatchOffset() != 0) {
				return new MyImage(createNinePatchDrawable(imageModel.getFrame(), assets.getTextureAtlas(imageModel.getAtlasName()), imageModel.getNinepatchOffset()));
			}else{
				return new MyImage(createNinePatchDrawable(imageModel.getFrame(), assets.getTextureAtlas(imageModel.getAtlasName()), imageModel.getNinepatchOffsetLeft(),
						imageModel.getNinepatchOffsetRight(), imageModel.getNinepatchOffsetTop(), imageModel.getNinepatchOffsetBottom()));
			}
		}else{
			TextureAtlas textureAtlas = assets.getTextureAtlas(imageModel.getAtlasName());
			TextureAtlas.AtlasRegion atlasRegion = textureAtlas.findRegion(getLocalizedString(imageModel.getFrame()));
			return new MyImage(atlasRegion);
		}
	}

	private NinePatchDrawable createNinePatchDrawable(String imageName, TextureAtlas textureAtlas ,int patchOffset) {
		NinePatchDrawable ninePatchDrawable = new NinePatchDrawable();
		NinePatch patch = new NinePatch(textureAtlas.findRegion(imageName), patchOffset, patchOffset, patchOffset, patchOffset);
		ninePatchDrawable.setPatch(patch);
		return ninePatchDrawable;
	}

	private NinePatchDrawable createNinePatchDrawable(String imageName, TextureAtlas textureAtlas ,int patchOffsetLeft, int patchOffsetRight, int patchOffsetTop, int patchOffsetBottom) {
		NinePatchDrawable ninePatchDrawable = new NinePatchDrawable();
		NinePatch patch = new NinePatch(textureAtlas.findRegion(imageName), patchOffsetLeft, patchOffsetRight, patchOffsetTop, patchOffsetBottom);
		ninePatchDrawable.setPatch(patch);
		return ninePatchDrawable;
	}

}
