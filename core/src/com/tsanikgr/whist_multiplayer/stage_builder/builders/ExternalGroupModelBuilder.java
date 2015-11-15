package com.tsanikgr.whist_multiplayer.stage_builder.builders;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.ExternalGroupModel;
import com.tsanikgr.whist_multiplayer.util.Log;

public class ExternalGroupModelBuilder extends ActorBuilder {

	private final StageBuilder stageBuilder;

	public ExternalGroupModelBuilder(IAssets assets, IResolution resolution, LocalizationService localizationService, StageBuilder stageBuilder) {
		super(assets, resolution, localizationService);
		this.stageBuilder = stageBuilder;
	}

	@Override
	public Actor build(BaseActorModel model) {
		ExternalGroupModel externalGroupModel = (ExternalGroupModel) model;
		try {
			Group group = stageBuilder.buildGroup(externalGroupModel.getFileName(), null, null);
			updateGroupProperties( externalGroupModel, group);
			return group;
		} catch (Exception e) {
			new Log(this).e(e).append("Failed to build group from external file ").append(externalGroupModel.getFileName()).print();
			return null;
		}
	}

	private void updateGroupProperties(ExternalGroupModel model, Group group){
		group.setName( model.getName());
		model.setWidth( group.getWidth());
		model.setHeight( group.getHeight());
		Vector2 screenPos;
		if (model.getScreenAlignmentSupport() == null) {
			screenPos = calculateScreenPosition(model.getScreenAlignment(), model);
		}
		else {
			screenPos = calculateScreenPosition(model.getScreenAlignment(), model.getScreenAlignmentSupport(), model);
		}

		if(screenPos != null){
			group.setPosition(screenPos.x, screenPos.y);
		}else{
			group.setPosition(model.getX() * resolution.getPositionMultiplier(), model.getY() * resolution.getPositionMultiplier());
		}
	}
}
