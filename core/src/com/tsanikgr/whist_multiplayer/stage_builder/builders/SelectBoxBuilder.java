package com.tsanikgr.whist_multiplayer.stage_builder.builders;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.SelectBoxModel;

public class SelectBoxBuilder extends ActorBuilder {

    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final String DELIMITER = ";";
    private static final int DEFAULT_PADDING_LEFT = 5;
    private static final int DEFAULT_PADDING_RIGHT = 5;

    public SelectBoxBuilder(IAssets assets, IResolution resolution, LocalizationService localizationService) {
        super(assets, resolution, localizationService);
    }

    @Override
    public Actor build(BaseActorModel model) {
        float positionMultiplier = resolution.getPositionMultiplier();

        SelectBoxModel selectBoxModel = (SelectBoxModel)model;
        selectBoxModel.setPaddingLeft((int) (selectBoxModel.getPaddingLeft() * positionMultiplier));
        selectBoxModel.setPaddingRight((int) (selectBoxModel.getPaddingRight() * positionMultiplier));

        TextureAtlas textureAtlas = assets.getTextureAtlas(selectBoxModel.getAtlasName());

        TextureRegionDrawable hScroll = new TextureRegionDrawable(textureAtlas.findRegion(selectBoxModel.getSelection()));
        TextureRegionDrawable hScrollKnob = new TextureRegionDrawable(textureAtlas.findRegion(selectBoxModel.getSelection()));
        TextureRegionDrawable vScroll = new TextureRegionDrawable(textureAtlas.findRegion(selectBoxModel.getSelection()));
        TextureRegionDrawable vScrollKnob = new TextureRegionDrawable(textureAtlas.findRegion(selectBoxModel.getSelection()));

        TextureRegionDrawable selection = new TextureRegionDrawable(textureAtlas.findRegion(selectBoxModel.getSelection()));

        TextureRegionDrawable selectBoxBackground = new TextureRegionDrawable(textureAtlas.findRegion(selectBoxModel.getSelectionBackground()));

        NinePatchDrawable drawable = new NinePatchDrawable();
        int patchSize = calculatePatchSize(positionMultiplier, selectBoxModel, selectBoxBackground);

        NinePatch n = new NinePatch(textureAtlas.findRegion(selectBoxModel.getBackground()), patchSize, patchSize, patchSize, patchSize);
        drawable.setPatch(n);
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle(drawable, hScroll, hScrollKnob, vScroll, vScrollKnob);

        float size = selectBoxModel.getFontSize();
        BitmapFont font = assets.getFont(selectBoxModel.getFontName(),size);
        Color fontColor = selectBoxModel.getFontColor()==null ? DEFAULT_COLOR : Color.valueOf(selectBoxModel.getFontColor());
        Color fontColorSelected = selectBoxModel.getFontColorSelected()==null ? DEFAULT_COLOR : Color.valueOf(selectBoxModel.getFontColorSelected());
        Color fontColorUnselected = selectBoxModel.getFontColorUnselected()==null ? DEFAULT_COLOR : Color.valueOf(selectBoxModel.getFontColorUnselected());

        selection.setLeftWidth(selectBoxModel.getPaddingLeft()==0 ? (int) (DEFAULT_PADDING_LEFT * positionMultiplier) : selectBoxModel.getPaddingLeft());
        selection.setRightWidth(selectBoxModel.getPaddingRight() == 0 ? (int) (DEFAULT_PADDING_RIGHT * positionMultiplier) : selectBoxModel.getPaddingRight());
        selection.setTopHeight(5 * positionMultiplier);
        selection.setBottomHeight(5 * positionMultiplier);

        String[] values = new String[0];
        String filterValues =  getLocalizedString(selectBoxModel.getValue());
        if (selectBoxModel.getValue() != null && filterValues.length() != 0) {
            values = filterValues.split(DELIMITER);
        }

        autoScaleFont(font, values, selectBoxModel.getMaxTextWidth() * positionMultiplier);

        List.ListStyle listStyle = new List.ListStyle(font, fontColorSelected, fontColorUnselected, selection);

        selectBoxBackground.setLeftWidth(selectBoxModel.getPaddingLeft());
        selectBoxBackground.setRightWidth(selectBoxModel.getPaddingRight());
        SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle(font, fontColor, selectBoxBackground, scrollPaneStyle, listStyle);


        SelectBox selectBox = new SelectBox(style);
        selectBox.setItems((Object[])values);
        selectBox.setName(selectBoxModel.getName());

        selectBox.setBounds(selectBoxModel.getX(), selectBoxModel.getY(), selectBoxBackground.getRegion().getRegionWidth(), selectBoxBackground.getRegion().getRegionHeight());

        normalizeModelSize(selectBoxModel, selectBoxBackground.getRegion().getRegionWidth(), selectBoxBackground.getRegion().getRegionHeight());
        setBasicProperties(selectBoxModel, selectBox);

        Gdx.app.log("SelectBoxBuilder", "font scaling is not yet supported");
//	    selectBox.getStyle().listStyle.font.setFontScale(label.getStyle().font.getScaleX() * App.font().getFontScaling(size));
        return selectBox;
    }

    private void autoScaleFont(BitmapFont font, String[] values, float maxWidth) {
        if (maxWidth <= 0) {
            return;
        }
        float max = 0;
        for (String value : values) {
            glyphLayout.setText(font, value);
            float textWidth =  glyphLayout.width;
            if (textWidth > max) {
                max = textWidth;
            }
        }
        if (max > maxWidth) {
            font.getData().setScale(font.getScaleX() * (maxWidth/max));
        }
    }

    /**
     * TODO Bu metod icinde sadece height kontrolu yapiliyor.
     * bottom ve top patch toplami nine-patch resminin yuksekliginden buyuk ise
     * patch size yuksekligi gecmeyecek sekilde guncelleniyor.
     * @param positionMultiplier
     * @param selectBoxModel
     * @param selectBoxBackground
     * @return patch size
     */
    private int calculatePatchSize(float positionMultiplier, SelectBoxModel selectBoxModel, TextureRegionDrawable selectBoxBackground) {
        int patchSize = (int) (positionMultiplier * selectBoxModel.getPatchSize());
        if (patchSize > (selectBoxBackground.getMinHeight() /2f)) {
            patchSize = (int) (selectBoxBackground.getMinHeight() /2f) - 2 ;
        }
        return patchSize;
    }

}
