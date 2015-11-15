package com.tsanikgr.whist_multiplayer.stage_builder.models;

public class TextButtonModel extends ButtonModel {

    private String text;
    private String fontName;
    private String fontColor;
    private float fontSize;
    private float fontScale;
    private float labelPadding;
    private float labelPaddingLeft;
    private float labelPaddingRight;
    private float labelPaddingTop;
    private float labelPaddingBottom;
    private String alignment;
    private boolean fontAutoScale;
    private boolean wrap;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public void setFontSize(float fontSize) {this.fontSize = fontSize;}

    public float getFontSize(){return fontSize;}

    public float getFontScale() {
        return fontScale;
    }

    public void setFontScale(float fontScale) {
        this.fontScale = fontScale;
    }

    public float getLabelPadding() {
        return labelPadding;
    }

    public void setLabelPadding(float labelPadding) {
        this.labelPadding = labelPadding;
    }

    public float getLabelPaddingLeft() {
        return labelPaddingLeft;
    }

    public void setLabelPaddingLeft(float labelPaddingLeft) {
        this.labelPaddingLeft = labelPaddingLeft;
    }

    public float getLabelPaddingRight() {
        return labelPaddingRight;
    }

    public void setLabelPaddingRight(float labelPaddingRight) {
        this.labelPaddingRight = labelPaddingRight;
    }

    public float getLabelPaddingTop() {
        return labelPaddingTop;
    }

    public void setLabelPaddingTop(float labelPaddingTop) {
        this.labelPaddingTop = labelPaddingTop;
    }

    public float getLabelPaddingBottom() {
        return labelPaddingBottom;
    }

    public void setLabelPaddingBottom(float labelPaddingBottom) {
        this.labelPaddingBottom = labelPaddingBottom;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public boolean isWrap() {
        return wrap;
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    public boolean isFontAutoScale() {
        return fontAutoScale;
    }

    public void setFontAutoScale(boolean fontAutoScale) {
        this.fontAutoScale = fontAutoScale;
    }
}
