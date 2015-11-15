package com.tsanikgr.whist_multiplayer.stage_builder.builders;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.myactors.MyLabel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.LabelModel;

public class LabelBuilder extends ActorBuilder {

    private static final Color DEFAULT_LABEL_COLOR = Color.WHITE;

    public LabelBuilder(IAssets assets, IResolution resolution, LocalizationService localizationService) {
        super(assets, resolution, localizationService);
    }

    @Override
    public Actor build(BaseActorModel model) {
        LabelModel labelModel = (LabelModel) model;
        Color color = labelModel.getFontColor() == null ? DEFAULT_LABEL_COLOR : Color.valueOf(labelModel.getFontColor());
        float size = labelModel.getFontSize();
        Label.LabelStyle style = new Label.LabelStyle(assets.getFont(labelModel.getFontName(),size), color);
        MyLabel label = new MyLabel(getLocalizedString(labelModel.getText()).replace("\\n", String.format("%n")), style);

        normalizeModelSize(labelModel, 0, 0);
        setBasicProperties(model, label);

        labelModel.setFontScale(assets.getFontScaling(size));

        label.setAlignment(calculateAlignment(labelModel.getAlignment()));
        label.setWrap(labelModel.isWrap());
        if (labelModel.isFontAutoScale()) {
            autoScaleLabel(label);
        } else if (Math.abs(labelModel.getFontScale() - 1f) > 0.0000001) {
            label.setFontScale(label.getFontScaleX() * labelModel.getFontScale());
        } else if (Math.abs(labelModel.getLabelScale()) > 0.0000001) {
            float scaleLabelWidth = labelModel.getLabelScale() * resolution.getPositionMultiplier();
            scaleLabel(label, scaleLabelWidth);
        }

        return label;
    }

    private void autoScaleLabel(Label label) {
        scaleLabel(label, label.getWidth());
    }

    private static void scaleLabel(Label label, float labelWidth){
        float labelTextWidth = label.getGlyphLayout().width /label.getFontScaleX();
        float scaleDownFactor = labelWidth / labelTextWidth;
        if (labelTextWidth > labelWidth) {
            label.setFontScale(label.getFontScaleX() * scaleDownFactor);
        }
    }

}
