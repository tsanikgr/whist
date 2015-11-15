package com.tsanikgr.whist_multiplayer.stage_builder.builders;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;

import java.lang.reflect.Method;
import java.util.Map;

public class CustomWidgetBuilder extends ActorBuilder {

   public CustomWidgetBuilder(IAssets assets, IResolution resolution, LocalizationService localizationService) {
      super(assets, resolution, localizationService);
   }

   @Override
   public Actor build(com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel model) {
      try {
         com.tsanikgr.whist_multiplayer.stage_builder.models.CustomWidgetModel customWidgetModel = (com.tsanikgr.whist_multiplayer.stage_builder.models.CustomWidgetModel) model;
         localizeAttributes(customWidgetModel);
         Class<?> klass = Class.forName(customWidgetModel.getKlass());
         Object customWidget = klass.newInstance();
         Class<?>[] buildMethodParameterTypes = {
               Map.class,
               IAssets.class,
               IResolution.class,
               LocalizationService.class
         };

         Method buildMethod = klass.getMethod("build", buildMethodParameterTypes);
         setBasicProperties(model, (Actor) customWidget);
         buildMethod.invoke(
               customWidget,
               customWidgetModel.getAttributeMap(),
               this.assets,
               this.resolution,
               this.localizationService);

         return (Actor) customWidget;
      } catch (Exception e) {
         Gdx.app.log("GdxWidgets", "Failed to create custom widget.", e);
         return null;
      }

   }

   private void localizeAttributes(com.tsanikgr.whist_multiplayer.stage_builder.models.CustomWidgetModel customWidgetModel) {
      for (Map.Entry<String,String> mapEntry : customWidgetModel.getAttributeMap().entrySet()) {
         customWidgetModel.addAttribute(mapEntry.getKey(), getLocalizedString(mapEntry.getValue()));
      }
   }

}
