package com.tsanikgr.whist_multiplayer.stage_builder.models;

public class SelectBoxModel extends BaseActorModel {
    private String name;

    private String value;
    private String fontName;
    private String fontColor;
    private String fontColorSelected;
    private String fontColorUnselected;

    private String atlasName;
    private String background;
    private String selection;
    private String selectionBackground;

    private boolean horizontalScrollDisabled;
    private boolean verticalScrollDisabled;

    private int paddingLeft;
    private int paddingRight;
    private int patchSize;
    private int maxTextWidth;
    private float fontSize;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontColorSelected() {
        return fontColorSelected;
    }

    public void setFontColorSelected(String fontColorSelected) {
        this.fontColorSelected = fontColorSelected;
    }

    public String getAtlasName() {
        return atlasName;
    }

    public void setAtlasName(String atlasName) {
        this.atlasName = atlasName;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String getSelectionBackground() {
        return selectionBackground;
    }

    public void setSelectionBackground(String selectionBackground) {
        this.selectionBackground = selectionBackground;
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(int paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public int getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(int paddingRight) {
        this.paddingRight = paddingRight;
    }

    public String getFontColorUnselected() {
        return fontColorUnselected;
    }

    public void setFontColorUnselected(String fontColorUnselected) {
        this.fontColorUnselected = fontColorUnselected;
    }

    public int getPatchSize() {
        return patchSize;
    }

    public void setPatchSize(int patchSize) {
        this.patchSize = patchSize;
    }

    public int getMaxTextWidth() {
        return maxTextWidth;
    }

    public void setMaxTextWidth(int maxTextWidth) {
        this.maxTextWidth = maxTextWidth;
    }

    public boolean isVerticalScrollDisabled() {
        return verticalScrollDisabled;
    }

    public void setVerticalScrollDisabled(boolean verticalScrollDisabled) {
        this.verticalScrollDisabled = verticalScrollDisabled;
    }

    public boolean isHorizontalScrollDisabled() {
        return horizontalScrollDisabled;
    }

    public void setHorizontalScrollDisabled(boolean horizontalScrollDisabled) {
        this.horizontalScrollDisabled = horizontalScrollDisabled;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public float getFontSize() {
        return fontSize;
    }
}
