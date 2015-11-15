package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.Gdx;
import com.tsanikgr.whist_multiplayer.Config;
import com.tsanikgr.whist_multiplayer.IApplication;
import com.tsanikgr.whist_multiplayer.IApplicationBuild;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.ICardController;
import com.tsanikgr.whist_multiplayer.IGameController;
import com.tsanikgr.whist_multiplayer.IMenuController;
import com.tsanikgr.whist_multiplayer.IMultiplayerGame;
import com.tsanikgr.whist_multiplayer.IPerformance;
import com.tsanikgr.whist_multiplayer.IPlatformApplication;
import com.tsanikgr.whist_multiplayer.IPlatformStorage;
import com.tsanikgr.whist_multiplayer.IScreenDirector;
import com.tsanikgr.whist_multiplayer.ISettings;
import com.tsanikgr.whist_multiplayer.ISounds;
import com.tsanikgr.whist_multiplayer.IStatistics;
import com.tsanikgr.whist_multiplayer.IStorage;
import com.tsanikgr.whist_multiplayer.IUid;

public class AppController extends CompositeController implements IApplication {

	private final IPlatformApplication plattformApplication;
	private final IPlatformStorage platformStorage;
	private final IApplicationBuild buildInterface;
	private final IUid uuidInterface;
	private final IMultiplayerGame.Out multiplayerInterface;

	private Storage storage;
	private Sounds sounds;
	private Performance performance;
	private Settings settings;
	private Assets assets;
	private ScreenDirector screenDirector;
	private CardController cardController;
	private StatisticsController statisticsController;

	public AppController(IPlatformApplication plattformApplication, IPlatformStorage storage, IApplicationBuild buildInterface, IUid uuid, IMultiplayerGame.Out multiplayerInterface){
		this.plattformApplication = plattformApplication;
		this.platformStorage = storage;
		this.buildInterface = buildInterface;
		this.uuidInterface = uuid;
		this.multiplayerInterface = multiplayerInterface;
	}

	@Override
	public void create() {
		addController(storage = new Storage(platformStorage));
		addController(settings = new Settings());
		addController(performance = new Performance());
		addController(assets = new Assets());
		addController(sounds = new Sounds());
		addController(statisticsController = new StatisticsController());
		addController(screenDirector = new ScreenDirector());
		addController(cardController = new CardController());

		init();
	}

	@Override
	public void resize(int width, int height) {
		if (assets.getResolution().resize(width,height)) {
			assets.disposeController();
			assets.init();
			screenDirector.updateViewport(width, height);
		}
	}

	@Override
	public void render() {
		update(Gdx.graphics.getDeltaTime() * Config.GLOBAL_SPEED);
		performance.renderingEnded();
	}

	@Override
	public void pause() {
		screenDirector.pause();
	}

	@Override
	public void resume() {
		screenDirector.resume();
	}

	@Override
	public void dispose() {
		disposeController();
	}

	@Override
	public IStorage getStorage() {
		return storage;
	}

	@Override
	public ISettings getSettings() {
		return settings;
	}

	@Override
	public IPerformance getPerformance() {
		return performance;
	}

	@Override
	public IAssets getAssets() {
		return assets;
	}

	@Override
	public ISounds getSounds() {
		return sounds;
	}

	@Override
	public IPlatformApplication getOSinterface(){
		return plattformApplication;
	}

	@Override
	public IMultiplayerGame.Out getMultiplayer(){
		return multiplayerInterface;
	}

	@Override
	public IUid getUuidInterface(){
		return uuidInterface;
	}

	@Override
	public IApplicationBuild getBuildInterface(){
		return buildInterface;
	}

	@Override
	public IMenuController getMenuController() {
		if (screenDirector == null) return null;
		return screenDirector.getMenuController();
	}

	@Override
	public IGameController getGameController() {
		if (screenDirector == null) return null;
		return screenDirector.getGameController();
	}

	@Override
	public IScreenDirector getScreenDirector() {
		return screenDirector;
	}

	@Override
	public IStatistics getStatisticsController() {
		return statisticsController;
	}

	@Override
	public ICardController getCardController() {
		return cardController;
	}

	@Override
	public boolean isUiThread() {
		return assets != null && assets.isRenderThread();
	}

	@Override
	public IResolution getResolution()  {
		return assets.getResolution();
	}

	@Override
	public boolean isRenderThread(){
		return assets.isRenderThread();
	}
}
