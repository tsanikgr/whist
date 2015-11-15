package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.AssetLoader;
import com.tsanikgr.whist_multiplayer.assets.AssetLoaderListener;
import com.tsanikgr.whist_multiplayer.assets.Font;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.assets.IdentityLocalizationService;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.assets.ResolutionHelper;
import com.tsanikgr.whist_multiplayer.assets.StageBuilderFileHandleResolver;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Assets extends Controller implements IAssets {

	private StageBuilderFileHandleResolver resolver;
	private IResolution resolution;
	private AssetManager manager;
	private AssetLoader assetLoader;
	private LocalizationService localization;

	private Font font;
	private Long renderThreadId;

	@Override
	public void init(){
		renderThreadId = Thread.currentThread().getId();

		List<Vector2> supportedResolutions;
		supportedResolutions = getSupportedResolutions();
		resolver = new StageBuilderFileHandleResolver(Gdx.graphics.getWidth(), supportedResolutions);
		resolution = new ResolutionHelper(this);

		manager = new AssetManager(resolver);
		assetLoader = new AssetLoader(manager, resolver);
		localization = new IdentityLocalizationService();

		font = new Font();
	}

	@Override
	protected void update(float dt) {
		super.update(dt);
		if (!manager.update()) Gdx.graphics.requestRendering();
	}

	@Override
	public void disposeController() {
		resetAlreadyLoadedAssetsMap();
		getAssetsConfiguration().clear();
		manager.dispose();
	}

	@Override
	public List<Vector2> getSupportedResolutions() {
		List<Vector2> supportedScreenResolutions = new LinkedList<>();
		supportedScreenResolutions.add(new Vector2(1366, 768));
		supportedScreenResolutions.add(new Vector2(1920, 1080));
		return supportedScreenResolutions;
	}

	@Override
	public IResolution getResolution() {	return resolution; }

	@Override
	public StageBuilderFileHandleResolver getResolver() { return resolver; }

	@Override
	public AssetManager getAssetManager() { return manager; }

	@Override
	public LocalizationService getLocalization() {
		return localization;
	}

	@Override
	public void setResolver(StageBuilderFileHandleResolver stageBuilderFileHandleResolver) {
		resolver = stageBuilderFileHandleResolver;
	}

	@Override
	public void addAssetConfiguration(String key, String fileName, Class<?> type) {
		assetLoader.addAssetConfiguration(key, fileName, type);
	}

	@Override
	public void removeAssetConfiguration(String key) {
		assetLoader.removeAssetConfiguration(key);
	}

	@Override
	public void loadAssetsSync(String key) {
		assetLoader.loadAssetsSync(key);
	}

	@Override
	public void loadAssetsAsync(String key, AssetLoaderListener listener) {
		assetLoader.loadAssetsAsync(key, listener);
	}

	@Override
	public Map<String, List<AssetLoader.AssetConfig>> getAssetsConfiguration() {
		return assetLoader.getAssetsConfiguration();
	}

	@Override
	public void unloadAssets(String key) {
		assetLoader.unloadAssets(key, null);
	}

	@Override
	public void unloadAssets(String key, Set<String> exludedSet) {
		assetLoader.unloadAssets(key, exludedSet);
	}

	@Override
	public void resetAlreadyLoadedAssetsMap() {
		assetLoader.resetAlreadyLoadedAssetMap();
	}

	@Override
	public void loadFontAssetsAsync() {
		Array<String> fontConfigKeys = new Array<>();
		for (String configKey : getAssetsConfiguration().keySet())
			for (AssetLoader.AssetConfig config : getAssetsConfiguration().get(configKey))
				if (config.getType() == BitmapFont.class) fontConfigKeys.add(configKey);
		font.loadFontAssetsAsync(this, getPerformance(), fontConfigKeys);
	}

	@Override
	public BitmapFont getFont(String font, float size) { return this.font.getFont(this, font, size); }

	@Override
	public float getFontScaling(float requestedSize) { return font.getFontScaling(requestedSize); }

	@Override
	public void setDefaultFont(String fontName, String fontExtension) { font.setDefaultFont(fontName, fontExtension); }

	@Override
	public void setSupportedFontSizes(int[] supportedSizes, float fontScaling) { font.setSupportedFontSizes(supportedSizes, fontScaling); }

	@Override
	public TextureAtlas getTextureAtlas(String atlasName) {
		loadLazy(atlasName);
		return manager.get(atlasName, TextureAtlas.class);
	}

	@Override
	public Texture getTexture(String textureName) {
		loadLazy(textureName);
		return manager.get(textureName, Texture.class);
	}

	@Override
	public Sound getSound(String soundName) {
		return manager.get(soundName, Sound.class);
	}

	private boolean loadingLazy = false;

	private void loadLazy(final String what) throws GdxRuntimeException {

		if (manager.isLoaded(what)) return;
		String id = getPerformance().tickNew();
		loadingLazy = true;

		Log.LogEntry log = this.log.w().append("Lazy initialisation of ").append(what);

		if (!isRenderThread()) {
			log.append(" started asynchronously").print();
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					loadAssetsAsync(what.substring(0, what.lastIndexOf(".")), new AssetLoaderListener() {
						@Override
						public void onAssetsLoaded() {
							loadingLazy = false;
						}
					});
				}
			});
		} else {
			log.append(" started synchronously").print();
			loadAssetsSync(what.substring(0, what.lastIndexOf(".")));
			loadingLazy = false;
		}
		while(loadingLazy) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				this.log.e(e).print();
			}
		}
		loadingLazy = false;
//		loadAssetsSync(what.substring(0, what.lastIndexOf(".")));
		if (!manager.isLoaded(what)) {
			throw new GdxRuntimeException(what + " is not in assetsConfiguration (LoadingController)");
		}
		this.log.w().append("Lazy initialisation of [").append(what).append("] took [").append(getPerformance().stop(id)).append("ms]").print();
	}

	@Override
	public void setRenderThreadId(long renderThreadId){
		this.renderThreadId = renderThreadId;
	}

	@Override
	public boolean isRenderThread() {
		return Thread.currentThread().getId() == renderThreadId;
	}
}
