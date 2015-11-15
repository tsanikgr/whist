package com.tsanikgr.whist_multiplayer.controllers;

import com.tsanikgr.whist_multiplayer.IApplicationBuild;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.ICardController;
import com.tsanikgr.whist_multiplayer.IGameController;
import com.tsanikgr.whist_multiplayer.IMenuController;
import com.tsanikgr.whist_multiplayer.IPerformance;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.IScreenDirector;
import com.tsanikgr.whist_multiplayer.ISettings;
import com.tsanikgr.whist_multiplayer.ISounds;
import com.tsanikgr.whist_multiplayer.IStatistics;
import com.tsanikgr.whist_multiplayer.IStorage;
import com.tsanikgr.whist_multiplayer.IMultiplayerGame;
import com.tsanikgr.whist_multiplayer.IPlatformApplication;
import com.tsanikgr.whist_multiplayer.IUid;
import com.tsanikgr.whist_multiplayer.util.Log;

public abstract class Controller {
	protected final Log log = new Log(this);

	Controller parent = null;
	boolean active = true;

	protected Controller(){ }

	protected abstract void init();
	protected abstract void disposeController();
	protected void update(float dt) {}
	void activate() { active = true; }
	void deactivate() { active = false; }
	public void setParent(Controller parent) {
		this.parent = parent;
	}

	IAssets getAssets() {
		if (this instanceof IAssets) return (IAssets)this;
		if (parent != null) return parent.getAssets();
		return null;
	}

	public IPerformance getPerformance() {
		if (this instanceof IPerformance) return (IPerformance)this;
		if (parent != null) return parent.getPerformance();
		return null;
	}

	IStorage getStorage() {
		if (this instanceof IStorage) return (IStorage)this;
		if (parent != null) return parent.getStorage();
		return null;
	}

	ISounds getSounds() {
		if (this instanceof ISounds) return (ISounds)this;
		if (parent != null) return parent.getSounds();
		return null;
	}

	ISettings getSettings() {
		if (this instanceof ISettings) return (ISettings)this;
		if (parent != null) return parent.getSettings();
		return null;
	}

	IScreenDirector getScreenDirector(){
		if (this instanceof IScreenDirector) return (IScreenDirector)this;
		if (parent != null) return parent.getScreenDirector();
		return null;
	}

	IPlatformApplication getOSinterface(){
		if (this instanceof IPlatformApplication) return (IPlatformApplication)this;
		if (parent != null) return parent.getOSinterface();
		return null;
	}

	IMultiplayerGame.Out getMultiplayer(){
		if (this instanceof IMultiplayerGame.Out) return (IMultiplayerGame.Out)this;
		if (parent != null) return parent.getMultiplayer();
		return null;
	}

	IUid getUuidInterface(){
		if (this instanceof IUid) return (IUid)this;
		if (parent != null) return parent.getUuidInterface();
		return null;
	}

	IApplicationBuild getBuildInterface(){
		if (this instanceof IApplicationBuild) return (IApplicationBuild)this;
		if (parent != null) return parent.getBuildInterface();
		return null;
	}

	IMenuController getMenuController() {
		if (this instanceof IMenuController) return (IMenuController)this;
		if (parent != null) return parent.getMenuController();
		return null;
	}

	IGameController getGameController() {
		if (this instanceof IGameController) return (IGameController)this;
		if (parent != null) return parent.getGameController();
		return null;
	}

	IStatistics getStatisticsController() {
		if (this instanceof IStatistics) return (IStatistics)this;
		if (parent != null) return parent.getStatisticsController();
		return null;
	}

	ICardController getCardController() {
		if (this instanceof ICardController) return (ICardController)this;
		if (parent != null) return parent.getCardController();
		return null;
	}

	public IResolution getResolution()  {
		return parent.getResolution();
	}

	public boolean isRenderThread(){
		return parent.isRenderThread();
	}
}
