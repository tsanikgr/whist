package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.assets.AssetLoaderListener;
import com.tsanikgr.whist_multiplayer.views.game.GameScreen;
import com.tsanikgr.whist_multiplayer.views.LoadingView;
import com.tsanikgr.whist_multiplayer.views.View;
import com.tsanikgr.whist_multiplayer.views.menu.MenuScreen;

public class LoadingController extends ScreenController {

	private static final String SOUNDS_KEY = "sounds";
	private LoadingView view;
	private final long startTime;

	LoadingController() {
		view = new LoadingView(this);
		startTime = System.currentTimeMillis();
	}

	@Override
	protected void onAssetsLoaded(View view) {} //not applicable here

	@Override
	protected void init() {
		getScreenDirector().addActor(view);
		loadAssets();
	}

	private void loadAssets() {
		IAssets assets = getAssets();
		addFontAssets(assets);
		addTextureAssets(assets);
		addSoundAssets(assets);
		getPerformance().tick();

		assets.loadFontAssetsAsync();
		assets.loadAssetsAsync(MenuScreen.MAIN_MENU, getAssetLoaderListener(MenuScreen.MAIN_MENU));
		assets.loadAssetsAsync(SOUNDS_KEY, getAssetLoaderListener(SOUNDS_KEY));

//		assets.loadAssetsAsync(MenuScreen.COMMON_VIEW, getAssetLoaderListener(MenuScreen.COMMON_VIEW));
	}

	private void loadAfterSplashDone(){
//		log.i().append("LOADING >>> Started preloading").print();
//		getAssets().loadAssetsAsync(GameScreen.CARDS, getAssetLoaderListener(GameScreen.CARDS));
	}

	private void addFontAssets(IAssets assets) {
		String name;
//		int[] supportedSizes = {40, 50, 60, 70};
		int[] supportedSizes = {70};
		float fontScaling = assets.getResolution().getPositionMultiplier();

		assets.setDefaultFont("MyriadPro-Bold", ".otf");
		assets.setSupportedFontSizes(supportedSizes, fontScaling);
		for (int supportedSize : supportedSizes) {
			name = "MyriadPro-Bold_" + ((int) (supportedSize * fontScaling));
			assets.addAssetConfiguration(name, "MyriadPro-Bold.otf", BitmapFont.class);
			name = "MyriadPro-Regular_" + ((int) (supportedSize * fontScaling));
			assets.addAssetConfiguration(name, "MyriadPro-Regular.otf", BitmapFont.class);
		}
	}

	private void addTextureAssets(IAssets assets){
		String atlas = ".atlas";
		assets.addAssetConfiguration(MenuScreen.COMMON_VIEW, MenuScreen.COMMON_VIEW + atlas, TextureAtlas.class);
		assets.addAssetConfiguration(MenuScreen.MAIN_MENU, MenuScreen.MAIN_MENU + atlas,TextureAtlas.class);
		assets.addAssetConfiguration(MenuScreen.NEW_GAME_VIEW, MenuScreen.NEW_GAME_VIEW + atlas,TextureAtlas.class);
		assets.addAssetConfiguration(MenuScreen.SETTINGS_VIEW, MenuScreen.SETTINGS_VIEW + atlas,TextureAtlas.class);
		assets.addAssetConfiguration(MenuScreen.STATISTICS_VIEW, MenuScreen.STATISTICS_VIEW + atlas,TextureAtlas.class);
		assets.addAssetConfiguration(MenuScreen.COINS_VIEW, MenuScreen.COINS_VIEW + atlas,TextureAtlas.class);
		assets.addAssetConfiguration(GameScreen.GAME_SCREEN, GameScreen.GAME_SCREEN + atlas, TextureAtlas.class);
		assets.addAssetConfiguration(GameScreen.CARDS, GameScreen.CARDS + atlas, TextureAtlas.class);
		assets.addAssetConfiguration(GameScreen.CHAT_VIEW, GameScreen.CHAT_VIEW + atlas, TextureAtlas.class);
	}

	private void addSoundAssets(IAssets assets) {
		assets.addAssetConfiguration(SOUNDS_KEY, "pop.mp3", Sound.class);
		assets.addAssetConfiguration(SOUNDS_KEY, "tick.mp3", Sound.class);
		assets.addAssetConfiguration(SOUNDS_KEY, "turn.mp3", Sound.class);
		assets.addAssetConfiguration(SOUNDS_KEY, "deal.mp3", Sound.class);
		assets.addAssetConfiguration(SOUNDS_KEY, "error.mp3", Sound.class);
	}

	private AssetLoaderListener getAssetLoaderListener(final String name) {
		return new AssetLoaderListener() {
			@Override
			public void onAssetsLoaded() {
				if (name.compareTo(SOUNDS_KEY) == 0) getSounds().soundsLoaded();
				log.i().append("Finished loading asset [").append(name).append("] in ").append(getPerformance().tock()).append("ms").print();
				getPerformance().tick();
			}
		};
	}

	@Override
	protected void disposeController() {
		if (view == null) return;
		view.dispose();
		view = null;
	}

	@Override
	protected void update(float dt) {
		view.setAssetManagerProgress(getAssets().getAssetManager().getProgress());
		if (getAssets().getAssetManager().update()) {
			if ((System.currentTimeMillis() - startTime) > LoadingView.ANIMATION_DURATION * 1000) {
				disposeController();
				getScreenDirector().goToMenu();
				loadAfterSplashDone();
			}
		}
	}

	@Override
	public boolean handleBack() {
		return true;
	}

	@Override
	public boolean handle(Event event) {
		return true;
	}
}
