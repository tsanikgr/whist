package com.tsanikgr.whist_multiplayer.stage_builder.builders;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.myactors.MySlider;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.SliderModel;

public class SliderBuilder extends ActorBuilder {

    private TextureRegionDrawable background;
    private TextureRegionDrawable knob;

    public SliderBuilder(IAssets assets, IResolution resolution, LocalizationService localizationService) {
        super(assets, resolution, localizationService);
    }

    @Override
    public Actor build(BaseActorModel model) {
        SliderModel sliderModel = (SliderModel) model;
        setTextures(sliderModel);
        MySlider slider = new MySlider( sliderModel.getMinValue(),
              sliderModel.getMaxValue(),
              sliderModel.getStepSize(),
              sliderModel.isVertical(),
              new Slider.SliderStyle( background, knob));
        updateDrawableSize( knob);
        normalizeModelSize( sliderModel, background.getMinWidth(), background.getMinHeight());
        updateDrawableSize( background);
        setBasicProperties( model, slider);
        return slider;
    }

    private void setTextures(SliderModel sliderModel){
        if ( sliderModel.getTextureSrcBackground() != null) {
            this.background = new TextureRegionDrawable( new TextureRegion(new Texture( sliderModel.getTextureSrcBackground())));
            this.knob = new TextureRegionDrawable( new TextureRegion( new Texture( sliderModel.getTextureSrcKnob())));
        } else {
            TextureAtlas textureAtlas = assets.getTextureAtlas(sliderModel.getAtlasName());
            this.background = new TextureRegionDrawable(textureAtlas.findRegion( sliderModel.getFrameBackground()));
            this.knob = new TextureRegionDrawable(textureAtlas.findRegion(sliderModel.getFrameKnob()));
        }
    }

    private void updateDrawableSize(TextureRegionDrawable textureRegionDrawable){
        float sizeMultiplier = resolution.getSizeMultiplier();
        textureRegionDrawable.setMinWidth( textureRegionDrawable.getMinWidth() * sizeMultiplier);
        textureRegionDrawable.setMinHeight( textureRegionDrawable.getMinHeight() * sizeMultiplier);

    }
}
