package com.tsanikgr.whist_multiplayer.stage_builder.builders;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.myactors.MyButton;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.ButtonModel;

public class ButtonBuilder extends ActorBuilder {

    TextureRegionDrawable up;
    TextureRegionDrawable down;
    TextureRegionDrawable disabled;
    TextureRegionDrawable checked;

    public ButtonBuilder(IAssets assets, IResolution resolution, LocalizationService localizationService) {
        super(assets, resolution, localizationService);
    }

    @Override
    public Actor build(BaseActorModel model) {
        ButtonModel buttonModel = (ButtonModel) model;
        setTextures(buttonModel);
        MyButton button = new MyButton(up, down);
        if (buttonModel.getFrameDisabled() != null) {
            button.getStyle().disabled = disabled;
        }
        if ( buttonModel.getFrameChecked() != null){
            button.getStyle().checked = checked;
        }
        normalizeModelSize(buttonModel, up.getMinWidth(), up.getMinHeight());
        setBasicProperties(model, button);
        return button;
    }

    void setTextures(ButtonModel buttonModel) {
        if (buttonModel.getTextureSrcUp() != null) {
            this.down = new TextureRegionDrawable(new TextureRegion(new Texture(buttonModel.getTextureSrcDown())));
            this.up = new TextureRegionDrawable(new TextureRegion(new Texture(buttonModel.getTextureSrcUp())));
            if (buttonModel.getTextureSrcDisabled() != null) {
                this.disabled = new TextureRegionDrawable(new TextureRegion(new Texture(buttonModel.getTextureSrcDisabled())));
            }
            if ( buttonModel.getTextureSrcChecked() != null){
                this.checked = new TextureRegionDrawable( new TextureRegion( new Texture( buttonModel.getTextureSrcChecked())));
            }
        } else {
            TextureAtlas textureAtlas = assets.getTextureAtlas(buttonModel.getAtlasName());
            this.down = new TextureRegionDrawable(textureAtlas.findRegion(buttonModel.getFrameDown()));
            this.up = new TextureRegionDrawable(textureAtlas.findRegion(buttonModel.getFrameUp()));
            if ( buttonModel.getFrameDisabled() != null) {
                this.disabled = new TextureRegionDrawable(textureAtlas.findRegion(buttonModel.getFrameDisabled()));
            }
            if ( buttonModel.getFrameChecked() != null) {
                this.checked = new TextureRegionDrawable( textureAtlas.findRegion( buttonModel.getFrameChecked()));
            }
        }
    }

}
