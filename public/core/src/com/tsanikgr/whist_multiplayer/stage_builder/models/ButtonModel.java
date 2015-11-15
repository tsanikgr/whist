package com.tsanikgr.whist_multiplayer.stage_builder.models;

public class ButtonModel extends BaseActorModel {
    private String atlasName;
    private String frameUp;
    private String frameDown;
    private String frameDisabled;
    private String frameChecked;
    private String textureSrcUp;
    private String textureSrcDown;
    private String textureSrcDisabled;
    private String textureSrcChecked;

    public String getAtlasName() {
        return atlasName;
    }

    public void setAtlasName(String atlasName) {
        this.atlasName = atlasName;
    }

    public String getFrameUp() {
        return frameUp;
    }

    public void setFrameUp(String frameUp) {
        this.frameUp = frameUp;
    }

    public String getFrameDown() {
        return frameDown;
    }

    public void setFrameDown(String frameDown) {
        this.frameDown = frameDown;
    }

    public String getFrameDisabled() {
        return frameDisabled;
    }

    public void setFrameDisabled(String frameDisabled) {
        this.frameDisabled = frameDisabled;
    }

    public String getTextureSrcUp() {
        return textureSrcUp;
    }

    public void setTextureSrcUp(String textureSrcUp) {
        this.textureSrcUp = textureSrcUp;
    }

    public String getTextureSrcDown() {
        return textureSrcDown;
    }

    public void setTextureSrcDown(String textureSrcDown) {
        this.textureSrcDown = textureSrcDown;
    }

    public String getTextureSrcDisabled() {
        return textureSrcDisabled;
    }

    public void setTextureSrcDisabled(String textureSrcDisabled) {
        this.textureSrcDisabled = textureSrcDisabled;
    }

    public String getFrameChecked() {
        return frameChecked;
    }

    public void setFrameChecked(String frameChecked) {
        this.frameChecked = frameChecked;
    }

    public String getTextureSrcChecked() {
        return textureSrcChecked;
    }

    public void setTextureSrcChecked(String textureSrcChecked) {
        this.textureSrcChecked = textureSrcChecked;
    }
}
