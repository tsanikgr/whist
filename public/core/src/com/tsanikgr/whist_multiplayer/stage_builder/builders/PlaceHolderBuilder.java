package com.tsanikgr.whist_multiplayer.stage_builder.builders;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.PlaceHolderModel;

public class PlaceHolderBuilder extends ActorBuilder {

	public PlaceHolderBuilder(IAssets assets, IResolution resolution, LocalizationService localizationService) {
		super(assets, resolution, localizationService);
	}
	@Override
	public Actor build(BaseActorModel model) {
		PlaceHolderModel placeHolderModel = (PlaceHolderModel) model;

		Actor actor = new Actor();

		normalizeModelSize(placeHolderModel,
				placeHolderModel.getWidth(),
				placeHolderModel.getHeight());

		setBasicProperties(placeHolderModel, actor);

		return actor;
	}
}
