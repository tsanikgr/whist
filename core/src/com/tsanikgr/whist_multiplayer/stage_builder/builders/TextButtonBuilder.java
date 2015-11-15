package com.tsanikgr.whist_multiplayer.stage_builder.builders;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.myactors.MyTextButton;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.TextButtonModel;

public class TextButtonBuilder extends ButtonBuilder {


	public TextButtonBuilder(IAssets assets, IResolution resolution, LocalizationService localizationService) {
		super(assets, resolution, localizationService);
	}

	@Override
	public Actor build(BaseActorModel model) {
		TextButtonModel textButtonModel = (TextButtonModel) model;
		float size = textButtonModel.getFontSize();
		BitmapFont font = assets.getFont(textButtonModel.getFontName(),size);
		textButtonModel.setFontScale(assets.getFontScaling(size));

		setTextures(textButtonModel);
		TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(up, down, up, font);

		if (textButtonModel.getFontColor() != null) {
			style.fontColor = Color.valueOf(textButtonModel.getFontColor());
		}
		if (textButtonModel.getFrameDisabled() != null) {
			style.disabled = disabled;
		}
		if ( textButtonModel.getFrameChecked() != null){
			style.checked = checked;
		}

		MyTextButton textButton = new MyTextButton(getLocalizedString(textButtonModel.getText()).replace("\\n", String.format("%n")), style);
		normalizeModelSize(textButtonModel, up.getMinWidth(), up.getMinHeight());
		setBasicProperties(textButtonModel, textButton);
		setTextButtonProperties(textButtonModel, font, textButton);

		return textButton;
	}

	void setTextButtonProperties(TextButtonModel textButtonModel, BitmapFont font, TextButton textButton) {
		float positionMultiplier = resolution.getPositionMultiplier();
		textButton.padBottom(textButtonModel.getLabelPaddingBottom() * positionMultiplier);
		textButton.padTop(textButtonModel.getLabelPaddingTop() * positionMultiplier);
		textButton.padRight(textButtonModel.getLabelPaddingRight() * positionMultiplier);
		textButton.padLeft(textButtonModel.getLabelPaddingLeft() * positionMultiplier);
		Label label = textButton.getLabel();
		label.setWrap(textButtonModel.isWrap());
		if (textButtonModel.getAlignment() != null) {
			int alignment = calculateAlignment(textButtonModel.getAlignment());
			label.setAlignment(alignment);
		}
//		Cell labelCell = textButton.getLabelCell();
		if (textButtonModel.isFontAutoScale()) {
			autoScaleTextButton(textButton);
		} else /*if (textButtonModel.getFontScale() != 1f)*/ {
//			labelCell.height(textButton.getHeight());
//			labelCell.bottom();
			label.setFontScale(font.getScaleX() * textButtonModel.getFontScale());
			label.setAlignment(Align.center);
		}
	}

	private void autoScaleTextButton(TextButton textButton) {
		Label label = textButton.getLabel();
		float textButtonWidth = textButton.getWidth() - textButton.getPadLeft() - textButton.getPadRight();
		float labelWidth = label.getWidth();
		if (labelWidth > textButtonWidth) {
			float scaleDownFactor = textButtonWidth / labelWidth;
			label.setFontScale(label.getStyle().font.getScaleX() * scaleDownFactor);
			label.setWidth(label.getWidth() * scaleDownFactor);
		}
	}
}