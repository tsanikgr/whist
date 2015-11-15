package com.tsanikgr.whist_multiplayer.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AssetLoader {

	final private AssetManager assetManager;
	/**
	 * Key is used for querying loaded assets.
	 * Multiple asset files can be mapped to a single key.
	 */
	private final Map<String, List<AssetConfig>> assetsConfiguration = new HashMap<>();
	private final Map<String, Set<AssetConfig>> alreadyLoadedAssets = new HashMap<>();

	public AssetLoader(AssetManager assetManager, StageBuilderFileHandleResolver resolver) {
		this.assetManager = assetManager;

		assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		assetManager.setLoader(BitmapFont.class, new FreetypeFontLoader(resolver));
	}

	/**
	 * @param assetKey this key is used to load mapped assets.
	 * @param fileName asset file name
	 * @param type     asset type
	 */
	public void addAssetConfiguration(String assetKey, String fileName, Class<?> type) {
		AssetConfig config = new AssetConfig(fileName, type);
		List<AssetConfig> types = assetsConfiguration.get(assetKey);
		if (types == null) {
			types = new LinkedList<>();
			assetsConfiguration.put(assetKey, types);
		}
		types.add(config);
	}

	public void loadAssetsAsync(String assetKey, AssetLoaderListener listener) {
		LoadedCallbackManager loadedCallbackManager = new LoadedCallbackManager();
		loadedCallbackManager.addAssetsLoadedListener(listener);
		loadAssets(assetKey, loadedCallbackManager);
	}

	public void loadAssetsSync(final String assetKey) {
		//loadedCallbackmanager does not listen asset loading.
		loadAssets(assetKey, new LoadedCallbackManager() /*dummy*/);
		this.assetManager.finishLoading();
	}

	private void loadAssets(String screenName, LoadedCallbackManager loadedCallbackManager) {
		List<AssetConfig> list = assetsConfiguration.get(screenName);
		if (list != null) {
			Set<AssetConfig> alreadyLoadedSet = new HashSet<>();
			for (AssetConfig config : list) {
				String fileName = config.getFileName();
				loadedCallbackManager.addFile(fileName);
			}
			for (AssetConfig config : list) {
				Class<?> type = config.getType();
				String fileName = config.getFileName();
				if (assetManager.isLoaded(fileName)) {
					alreadyLoadedSet.add(config);
					loadedCallbackManager.finishedLoading(assetManager, fileName, type);
				} else {
					if (TextureAtlas.class.equals(type)) {
						TextureAtlasLoader.TextureAtlasParameter param = new TextureAtlasLoader.TextureAtlasParameter();
						param.loadedCallback = loadedCallbackManager;
						assetManager.load(fileName, TextureAtlas.class, param);

					} else if (Texture.class.equals(type)) {
						TextureLoader.TextureParameter param = new TextureLoader.TextureParameter();
						param.loadedCallback = loadedCallbackManager;
						assetManager.load(fileName, Texture.class, param);

					} else if (BitmapFont.class.equals(type)) {
//						BitmapFontLoader.BitmapFontParameter param = new BitmapFontLoader.BitmapFontParameter();
//						param.magFilter = Texture.TextureFilter.Linear;
//						param.minFilter = Texture.TextureFilter.Linear;
//						param.loadedCallback = loadedCallbackManager;
//						assetManager.load(fileName, BitmapFont.class, param);

						FreetypeFontLoader.FreeTypeFontLoaderParameter parameter = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
						parameter.fontFileName = fileName;
						parameter.fontParameters.size = getFontSize(screenName);
						parameter.fontParameters.genMipMaps = false;
						parameter.fontParameters.minFilter = Texture.TextureFilter.Linear;
						parameter.fontParameters.magFilter = Texture.TextureFilter.Linear;
						parameter.loadedCallback = loadedCallbackManager;
//						parameter.fontParameters.borderWidth = 3f;
//						parameter.fontParameters.borderColor = Color.ORANGE;
//						parameter.fontParameters.shadowOffsetY = 1;
//						parameter.fontParameters.shadowColor = new Color(0f,0f,0f,1f);
						assetManager.load(screenName+".otf", BitmapFont.class, parameter);

					} else if (Sound.class.equals(type)) {
						SoundLoader.SoundParameter param = new SoundLoader.SoundParameter();
						param.loadedCallback = loadedCallbackManager;
						assetManager.load(fileName, Sound.class, param);

					} else if (Music.class.equals(type)) {
						MusicLoader.MusicParameter param = new MusicLoader.MusicParameter();
						param.loadedCallback = loadedCallbackManager;
						assetManager.load(fileName, Music.class, param);
					} else {
						Gdx.app.log("AssetLoader", "Unrecognized asset type.");
					}
				}
			}

			if(!alreadyLoadedSet.isEmpty()){
				alreadyLoadedAssets.put(screenName, alreadyLoadedSet);
			}
		}
	}

	private String getFontName(String nameWithSize) {
		return nameWithSize.substring(0, nameWithSize.lastIndexOf("_"));
	}

	private int getFontSize(String string){
		return Integer.parseInt(string.substring(string.lastIndexOf("_") + 1, string.length()));
	}

	public Map<String, List<AssetConfig>> getAssetsConfiguration(){
		return  assetsConfiguration;
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	public void unloadAssets(String screenName, Set<String> excludedSet) {
		List<AssetConfig> list = assetsConfiguration.get(screenName);
		if (list != null) {
			Set<AssetConfig> alreadyLoadedSet = alreadyLoadedAssets.get(screenName);
			List<AssetConfig> loadList = new ArrayList<>();
			loadList.addAll(list);
			if(alreadyLoadedSet != null){
				removeAlreadyLoadedAssets(loadList, alreadyLoadedSet);
				alreadyLoadedAssets.remove(screenName);
			}
			for (AssetConfig config : loadList) {
				if (excludedSet != null && excludedSet.contains(config.getFileName())) {
					Gdx.app.log("AssetLoader", "Asset " + config.getFileName() + " will not be unloaded. (excluded)");
				} else {
					assetManager.unload(config.getFileName());
				}
			}
		}
	}

	private void removeAlreadyLoadedAssets(List<AssetConfig> list, Set<AssetConfig> alreadyLoadedSet) {
		for(AssetConfig assetConfig : alreadyLoadedSet){
			list.remove(assetConfig);
		}
	}

	public boolean isLoaded(String fileName) {
		return this.assetManager.isLoaded(fileName);
	}

	public void removeAssetConfiguration(String key) {
		this.assetsConfiguration.remove(key);
	}

	public static class AssetConfig {
		private final String fileName;
		private final Class<?> type;

		public AssetConfig(String fileName, Class<?> type) {
			super();
			this.fileName = fileName;
			this.type = type;
		}

		public String getFileName() {
			return fileName;
		}

		public Class<?> getType() {
			return type;
		}

		@Override
		public String toString() {
			return "AssetConfig [fileName=" + fileName + ", type=" + type + "]";
		}
	}

	public void resetAlreadyLoadedAssetMap() {
		this.alreadyLoadedAssets.clear();
	}
}
