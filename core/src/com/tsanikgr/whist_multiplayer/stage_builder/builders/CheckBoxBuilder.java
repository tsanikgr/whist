package com.tsanikgr.whist_multiplayer.stage_builder.builders;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.ButtonModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.CheckBoxModel;

public class CheckBoxBuilder extends TextButtonBuilder {

    private TextureRegionDrawable checkBoxOff;
    private TextureRegionDrawable checkBoxOn;
    private TextureRegionDrawable checkBoxOver;

    public CheckBoxBuilder(IAssets assets, IResolution resolution, LocalizationService localizationService) {
        super(assets, resolution, localizationService);
    }

    @Override
    public Actor build(BaseActorModel model) {
        CheckBoxModel checkBoxModel = (CheckBoxModel) model;
        float size = checkBoxModel.getFontSize();
        BitmapFont font = assets.getFont( checkBoxModel.getFontName(),size);
        checkBoxModel.setFontScale(assets.getFontScaling(size));

        setTextures( checkBoxModel);
        String text = ( checkBoxModel.getText() == null) ? "" : getLocalizedString( checkBoxModel.getText()).replace("\\n", String.format("%n"));

        Color fontColor = Color.valueOf(checkBoxModel.getFontColor());
        CheckBox.CheckBoxStyle style = new CheckBox.CheckBoxStyle( checkBoxOff, checkBoxOn, font, fontColor);
        if ( this.checkBoxOver != null){
            style.checkboxOver = checkBoxOver;
        }
        CheckBox checkBox = new CheckBox( text, style);
        normalizeModelSize(checkBoxModel, checkBoxOff.getMinWidth(), checkBoxOff.getMinHeight());
        setBasicProperties(checkBoxModel, checkBox);
        setTextButtonProperties(checkBoxModel, font, checkBox);
        applyTableAlignment(checkBoxModel.getAlignment(), checkBox);

        return checkBox;

    }

    private void applyTableAlignment(String alignment, CheckBox checkBox) {
        if(alignment != null) {
            int numericAlignment = calculateAlignment(alignment);
            switch (numericAlignment) {
                case Align.left:
                    checkBox.left();
                    break;
                case Align.right:
                    checkBox.right();
                    break;
                case Align.center:
                    checkBox.center();
                    break;
                case Align.top:
                    checkBox.top();
                    break;
                case Align.bottom:
                    checkBox.bottom();
                    break;
                default:
                    checkBox.center();
            }
        }
    }

    @Override
    void setTextures(ButtonModel buttonModel) {
        CheckBoxModel checkBoxModel = (CheckBoxModel)buttonModel;
        if ( checkBoxModel.getTextureSrcCheckboxOff() != null){
            this.checkBoxOff = new TextureRegionDrawable( new TextureRegion( new Texture( checkBoxModel.getTextureSrcCheckboxOff())));
            this.checkBoxOn = new TextureRegionDrawable( new TextureRegion( new Texture( checkBoxModel.getTextureSrcCheckboxOn())));
            if ( checkBoxModel.getTextureSrcCheckboxOver() != null){
                this.checkBoxOver = new TextureRegionDrawable( new TextureRegion( new Texture( checkBoxModel.getTextureSrcCheckboxOver())));
            }

        }else{
            TextureAtlas textureAtlas = assets.getTextureAtlas( checkBoxModel.getAtlasName());
            this.checkBoxOff = new TextureRegionDrawable( textureAtlas.findRegion( checkBoxModel.getFrameCheckboxOff()));
            this.checkBoxOn = new TextureRegionDrawable( textureAtlas.findRegion( checkBoxModel.getFrameCheckboxOn()));
            if ( checkBoxModel.getFrameCheckboxOver() != null){
                this.checkBoxOver = new TextureRegionDrawable( textureAtlas.findRegion( checkBoxModel.getFrameCheckboxOver()));
            }

        }
    }
}
