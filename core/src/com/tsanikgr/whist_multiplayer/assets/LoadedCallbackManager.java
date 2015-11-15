package com.tsanikgr.whist_multiplayer.assets;

import com.badlogic.gdx.assets.AssetLoaderParameters.LoadedCallback;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.util.ArrayList;
import java.util.List;

class LoadedCallbackManager implements LoadedCallback {

	private final Log log = new Log(this);

	private final List<AssetLoaderListener> listeners = new ArrayList<>();
	private final List<String> files = new ArrayList<>();

	public LoadedCallbackManager() {
	}

	public void addAssetsLoadedListener(AssetLoaderListener listener) {
		if (listeners.contains(listener)) return;
		listeners.add(listener);
	}

	public void addFile(String fileName) {
		if (files.contains(fileName)) return;
		log.i().append("Tracking [").append(fileName).append("] loading...").print();
		files.add(fileName);
	}

	@Override
	public void finishedLoading(AssetManager assetManager, String fileName, Class type) {
		if (type == BitmapFont.class) fileName = fileName.replaceAll("_[0-9]*.", ".");
		files.remove(fileName);
		if (files.size() == 0) {
			for (AssetLoaderListener listener : listeners) {
				if (listener != null) listener.onAssetsLoaded();
			}
		}
	}
}
