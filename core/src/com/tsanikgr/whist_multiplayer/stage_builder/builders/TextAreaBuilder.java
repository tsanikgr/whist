package com.tsanikgr.whist_multiplayer.stage_builder.builders;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.TextAreaModel;

public class TextAreaBuilder extends ActorBuilder {

    public TextAreaBuilder(IAssets assets, IResolution resolution, LocalizationService localizationService) {
        super(assets, resolution, localizationService);
    }

    @Override
    public Actor build(BaseActorModel model) {
        TextAreaModel textAreaModel = (TextAreaModel)model;

        float size = textAreaModel.getFontSize();
        BitmapFont font = assets.getFont(textAreaModel.getFontName(),size);
        Color fontColor = Color.valueOf(textAreaModel.getFontColor());

        TextureAtlas textureAtlas = assets.getTextureAtlas(textAreaModel.getAtlasName());

        NinePatchDrawable cursor = createNinePatchDrawable(textAreaModel.getCursorImageName(), textureAtlas, textAreaModel.getCursorOffset());
        cursor.getPatch().setColor(fontColor);
        NinePatchDrawable selection = createNinePatchDrawable(textAreaModel.getSelectionImageName(), textureAtlas, textAreaModel.getSelectionOffset());
        NinePatchDrawable background = null;

        if(textAreaModel.getBackgroundImageName() != null){
            background = createNinePatchDrawable(textAreaModel.getBackgroundImageName(), textureAtlas, textAreaModel.getBackGroundOffset());
            background.setLeftWidth(textAreaModel.getPadding());
            background.setRightWidth(textAreaModel.getPadding());
            background.setBottomHeight(textAreaModel.getPadding());
            background.setTopHeight(textAreaModel.getPadding());
        }


        TextField.TextFieldStyle textAreaStyle = new TextField.TextFieldStyle(font, fontColor, cursor, selection, background);
        TextArea textArea = new TextArea(getLocalizedString(textAreaModel.getText()), textAreaStyle);
        textArea.setPasswordMode(textAreaModel.isPassword());
        textArea.setPasswordCharacter(textAreaModel.getPasswordChar().charAt(0));
        if(textAreaModel.getHint() != null) textArea.setMessageText(getLocalizedString(textAreaModel.getHint()));
        normalizeModelSize(model, model.getWidth(), model.getHeight());
        setBasicProperties(model, textArea);

        Gdx.app.log("TextAreaBuilder", "font scaling is not yet supported");
//	    textArea.getStyle().setFontScale(label.getStyle().font.getScaleX() * App.font().getFontScaling(size));
        return textArea;
    }

    protected void updateDrawableSize( TextureRegionDrawable textureRegionDrawable){
        float sizeMultiplier = resolution.getSizeMultiplier();
        textureRegionDrawable.setMinWidth( textureRegionDrawable.getMinWidth() * sizeMultiplier);
        textureRegionDrawable.setMinHeight( textureRegionDrawable.getMinHeight() * sizeMultiplier);
    }

    private NinePatchDrawable createNinePatchDrawable(String imageName, TextureAtlas textureAtlas ,int patchOffset) {
        NinePatchDrawable ninePatchDrawable = new NinePatchDrawable();
        NinePatch patch = new NinePatch(textureAtlas.findRegion(imageName), patchOffset, patchOffset, patchOffset, patchOffset);
        ninePatchDrawable.setPatch(patch);
        return ninePatchDrawable;
    }
}
