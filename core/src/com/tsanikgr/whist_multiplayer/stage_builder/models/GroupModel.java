package com.tsanikgr.whist_multiplayer.stage_builder.models;

import java.util.ArrayList;
import java.util.List;

public class GroupModel extends BaseActorModel {

    private List<BaseActorModel> children = new ArrayList<>();

    public List<BaseActorModel> getChildren() {
        return children;
    }

    public void setChildren(List<BaseActorModel> children) {
        this.children = children;
    }
}
