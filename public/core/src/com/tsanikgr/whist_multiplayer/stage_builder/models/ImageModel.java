package com.tsanikgr.whist_multiplayer.stage_builder.models;

/**
 * An image's texture can be loaded in two different ways:
 * 1. From an atlas file with the specified frame name
 * 2. From file system (individual file)
 */
public class ImageModel extends BaseActorModel {

    public static final String TYPE_BACKGROUND = "background";
    private String atlasName;
    private String frame;
    //TODO textureSrc relative mi? relative ise nereye gore relative? javadoc'u netlestir.
    private String textureSrc;
    private String type;

    private boolean ninepatch;
    private int ninepatchOffset;
    private int ninepatchOffsetLeft;
    private int ninepatchOffsetRight;
    private int ninepatchOffsetTop;
    private int ninepatchOffsetBottom;


    public String getAtlasName() {
        return atlasName;
    }

    public void setAtlasName(String atlasName) {
        this.atlasName = atlasName;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }

    public String getTextureSrc() {
        return textureSrc;
    }

    public void setTextureSrc(String textureSrc) {
        this.textureSrc = textureSrc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getNinepatch() {return ninepatch; }

    public void setNinepatch(boolean ninepatch) { this.ninepatch = ninepatch; }

    public int getNinepatchOffset() { return ninepatchOffset;  }

    public void setNinepatchOffset(int ninepatchOffset) {  this.ninepatchOffset = ninepatchOffset;    }

    public int getNinepatchOffsetLeft() {
        return ninepatchOffsetLeft;
    }

    public void setNinepatchOffsetLeft(int ninepatchOffsetLeft) {
        this.ninepatchOffsetLeft = ninepatchOffsetLeft;
    }

    public int getNinepatchOffsetRight() {
        return ninepatchOffsetRight;
    }

    public void setNinepatchOffsetRight(int ninepatchOffsetRight) {
        this.ninepatchOffsetRight = ninepatchOffsetRight;
    }

    public int getNinepatchOffsetTop() {
        return ninepatchOffsetTop;
    }

    public void setNinepatchOffsetTop(int ninepatchOffsetTop) {
        this.ninepatchOffsetTop = ninepatchOffsetTop;
    }

    public int getNinepatchOffsetBottom() {
        return ninepatchOffsetBottom;
    }

    public void setNinepatchOffsetBottom(int ninepatchOffsetBottom) {
        this.ninepatchOffsetBottom = ninepatchOffsetBottom;
    }
}
