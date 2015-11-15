package com.tsanikgr.whist_multiplayer.stage_builder.builders;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.GroupModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GroupBuilder extends ActorBuilder {
    private final Map<Class<? extends BaseActorModel>, ActorBuilder> builders;

    public GroupBuilder(Map<Class<? extends BaseActorModel>, ActorBuilder> builders, IAssets assets, IResolution resolution, LocalizationService localizationService) {
        super(assets, resolution, localizationService);
        this.builders = builders;
    }

    @Override
    public Actor build(BaseActorModel model) {
        GroupModel groupModel = (GroupModel) model;
        MyGroup group = new MyGroup();
        normalizeModelSize(model, model.getWidth(), model.getHeight());
        setBasicProperties(model, group);
        List<BaseActorModel> children = groupModel.getChildren();
        Collections.sort(children, new ZIndexComparator());
        for (BaseActorModel child : children) {
            Actor actor = builders.get(child.getClass()).build(child);
            group.addActor(actor);
        }
        return group;
    }
}
