package com.tsanikgr.whist_multiplayer.stage_builder.builders;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.xml.XmlModelBuilder;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.io.Serializable;
import java.util.Comparator;

abstract class ActorBuilder {

	final IAssets assets;
	final IResolution resolution;
	final LocalizationService localizationService;
	private static final int NO_ALIGN = 0;

	static final GlyphLayout glyphLayout = new GlyphLayout();

	ActorBuilder(IAssets assets, IResolution resolution, LocalizationService localizationService) {
		this.localizationService = localizationService;
		this.assets = assets;
		this.resolution = resolution;
	}

	static int calculateAlignment(String s) {
		try {
			if (s == null) {
				// IOS does not catch NullPointerException
				return Align.left;
			}
			String[] sArray = s.split("\\|");
			int result = NO_ALIGN;
			for (String val : sArray) {
				if ("left".equals(val)) {
					result |= Align.left;
				} else if ("right".equals(val)) {
					result |= Align.right;
				} else if ("top".equals(val)) {
					result |= Align.top;
				} else if ("bottom".equals(val)) {
					result |= Align.bottom;
				} else if ("center".equals(val)) {
					result |= Align.center;
				}
			}
			return result;
		} catch (Exception e) {
			new Log(null).w(e).print();
		}
		return Align.left;
	}

	public abstract Actor build(BaseActorModel model);

	/**
	 * Width & height properties are updated by normalizeModelSize method.
	 *
	 * @param model model
	 * @param actor actor
	 */
	void setBasicProperties(BaseActorModel model, Actor actor) {
		actor.setBounds(
				model.getX() * resolution.getPositionMultiplier(),
				model.getY() * resolution.getPositionMultiplier(),
				model.getWidth(),
				model.getHeight());

		actor.setWidth(model.getWidth());
		actor.setHeight(model.getHeight());

		if (Math.abs(model.getScale() - 1f) > 0.0000001) {
			actor.setScale(model.getScale(), model.getScale());
		} else {
			actor.setScaleX(model.getScaleX());
			actor.setScaleY(model.getScaleY());
		}
//		actor.setZIndex(model.getzIndex());
//		actor.setVisible(model.isVisible());

		if (model.getColor() != null) {
			actor.setColor(Color.valueOf(model.getColor()));
		}

		if (Math.abs(model.getRotation()) > 0.0000001) {
			actor.setOrigin(actor.getWidth() / 2, actor.getHeight() / 2);
			actor.setRotation(model.getRotation());
		}

		if (Math.abs(model.getOriginX()) > 0.0000001) {
			actor.setOriginX(model.getOriginX());
		}

		if (Math.abs(model.getOriginY()) > 0.0000001) {
			actor.setOriginY(model.getOriginY());
		}


		actor.setName(model.getName());
		Vector2 screenPos;
		if (model.getScreenAlignmentSupport() == null) {
			screenPos = calculateScreenPosition(model.getScreenAlignment(), model);
		}
		else {
			screenPos = calculateScreenPosition(model.getScreenAlignment(), model.getScreenAlignmentSupport(), model);
		}

		if (screenPos != null) {
			actor.setPosition(screenPos.x, screenPos.y);
		}

		setTouchable(actor, model);
	}

	private void setTouchable(Actor actor, BaseActorModel model) {
		switch (model.getTouchable()) {
			case enabled:
				actor.setTouchable(Touchable.enabled);
				break;
			case disabled:
				actor.setTouchable(Touchable.disabled);
				break;
			case childrenOnly:
				actor.setTouchable(Touchable.childrenOnly);
				break;
			default:
				actor.setTouchable(Touchable.enabled);
				break;
		}
	}

	Vector2 calculateScreenPosition(BaseActorModel.ScreenAlign screenAlign, BaseActorModel.ScreenAlign screenAlignSupport, BaseActorModel model) {
		if (screenAlign == null || screenAlignSupport == null) {
			return null;
		}

		float x = 0;
		float y = 0;

		if (screenAlign == BaseActorModel.ScreenAlign.top || screenAlign == BaseActorModel.ScreenAlign.bottom) {
			y = calculateScreenPosition(screenAlign, model).y;
		}
		else {
			x = calculateScreenPosition(screenAlign, model).x;
		}

		if (screenAlignSupport == BaseActorModel.ScreenAlign.top || screenAlignSupport == BaseActorModel.ScreenAlign.bottom) {
			y = calculateScreenPosition(screenAlignSupport, model).y;
		}
		else {
			x = calculateScreenPosition(screenAlignSupport, model).x;
		}

		return new Vector2(x, y);
	}

	Vector2 calculateScreenPosition(BaseActorModel.ScreenAlign screenAlign, BaseActorModel model) {
		if (screenAlign == null) {
			return null;
		}
		float y = model.getY() * resolution.getPositionMultiplier();
		float x = model.getX() * resolution.getPositionMultiplier();

		switch (screenAlign) {
			case top:
				//after building all actors stage position will be set to gameAreaPosition.
				y = resolution.getScreenHeight() - model.getScaledHeight() - resolution.getGameAreaPosition().y;
				y = y - model.getScreenPaddingTop() * resolution.getPositionMultiplier();
				break;
			case bottom:
				y = -resolution.getGameAreaPosition().y;
				y = y + model.getScreenPaddingBottom() * resolution.getPositionMultiplier();
				break;

			case left:
				x = -resolution.getGameAreaPosition().x;
				x = x + model.getScreenPaddingLeft() * resolution.getPositionMultiplier();
				break;

			case right:
				x = resolution.getScreenWidth() - model.getScaledWidth() - resolution.getGameAreaPosition().x;
				x = x - model.getScreenPaddingRight() * resolution.getPositionMultiplier();
				break;
			default:
				break;
		}
		return new Vector2(x, y);
	}

	/**
	 * Target screen resolution(800x480) may be smaller than selected asset resolution(1280x800) for
	 * device screen resolution 1280x800. sizeMultiplier in this case will be "1". If there is size
	 * information in layout xml file generated for 800x480 target screen resolution, size multiplier value "1" will
	 * not work correctly. Position multiplier (1280 / 800 = 1.6) must be used in such cases for providing correct scaling.
	 *
	 * @param defaultWidth  if width of the actor is not specified in layout file then defaultWidth is multiplied with sizeMultiplier
	 * @param defaultHeight if height of the actor is not specified in layout file then defaultHeight is multiplied with sizeMultiplier
	 */
	void normalizeModelSize(BaseActorModel model, float defaultWidth, float defaultHeight) {
		float width = model.getWidth();
		float height = model.getHeight();
		if (Math.abs(width) < 0.001) {
			model.setWidth(defaultWidth * resolution.getSizeMultiplier());
		} else {
			model.setWidth(width * resolution.getPositionMultiplier());
		}
		if (Math.abs(height) < 0.001) {
			model.setHeight(defaultHeight * resolution.getSizeMultiplier());
		} else {
			model.setHeight(height * resolution.getPositionMultiplier());
		}
	}

	String getLocalizedString(String s) {
		if (s == null) {
			return "";
		}
		if (s.startsWith(XmlModelBuilder.LOCALIZED_STRING_PREFIX)) {
			s = s.replace(XmlModelBuilder.LOCALIZED_STRING_PREFIX, "");
			return localizationService.getString(s);
		} else {
			return s;
		}
	}

	static final class ZIndexComparator implements Comparator<BaseActorModel>, Serializable {
		@Override
		public int compare(BaseActorModel model1, BaseActorModel model2) {
			return model1.getzIndex() - model2.getzIndex();
		}
	}
}
