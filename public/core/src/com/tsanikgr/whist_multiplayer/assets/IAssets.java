package com.tsanikgr.whist_multiplayer.assets;


import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.tsanikgr.whist_multiplayer.IResolution;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAssets {
	AssetManager getAssetManager();
	StageBuilderFileHandleResolver getResolver();
	IResolution getResolution();
	LocalizationService getLocalization();
	void setResolver(StageBuilderFileHandleResolver stageBuilderFileHandleResolver);

	void addAssetConfiguration(String key, String fileName, Class<?> type);
	void removeAssetConfiguration(String key);
	void loadAssetsSync(String key);
	void loadAssetsAsync(String key, AssetLoaderListener listener);
	void unloadAssets(String key);
	void unloadAssets(String key, Set<String> exludedSet);
	void resetAlreadyLoadedAssetsMap();
	Map<String, List<AssetLoader.AssetConfig>> getAssetsConfiguration();
	TextureAtlas getTextureAtlas(String atlasName);
	Texture getTexture(String textureName);

	void setSupportedFontSizes(int[] supportedSizes, float fontScaling);
	void setDefaultFont(String fontName, String fontExtension);
	void loadFontAssetsAsync();
	BitmapFont getFont(String fontName, float size);
	float getFontScaling(float requestedSize);

	Sound getSound(String soundName);

	List<Vector2> getSupportedResolutions();
	boolean isRenderThread();
	void setRenderThreadId(long id);
}
