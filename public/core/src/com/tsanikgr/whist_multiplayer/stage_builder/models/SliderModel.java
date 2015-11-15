package com.tsanikgr.whist_multiplayer.stage_builder.models;

public class SliderModel extends BaseActorModel {

    private float minValue;
    private float maxValue;
    private float stepSize;
    private boolean vertical;
    private String frameBackground;
    private String frameKnob;
    private String textureSrcBackground;
    private String textureSrcKnob;
    private String atlasName;

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public String getTextureSrcKnob() {
        return textureSrcKnob;
    }

    public void setTextureSrcKnob(String textureSrcKnob) {
        this.textureSrcKnob = textureSrcKnob;
    }

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public float getStepSize() {
        return stepSize;
    }

    public void setStepSize(float stepSize) {
        this.stepSize = stepSize;
    }

    public String getFrameBackground() {
        return frameBackground;
    }

    public void setFrameBackground(String frameBackground) {
        this.frameBackground = frameBackground;
    }

    public String getFrameKnob() {
        return frameKnob;
    }

    public void setFrameKnob(String frameKnob) {
        this.frameKnob = frameKnob;
    }

    public String getTextureSrcBackground() {
        return textureSrcBackground;
    }

    public void setTextureSrcBackground(String textureSrcBackground) {
        this.textureSrcBackground = textureSrcBackground;
    }

    public String getAtlasName() {
        return atlasName;
    }

    public void setAtlasName(String atlasName) {
        this.atlasName = atlasName;
    }
}
