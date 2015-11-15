package com.tsanikgr.whist_multiplayer.assets;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Array;
import com.tsanikgr.whist_multiplayer.util.Log;
import com.tsanikgr.whist_multiplayer.IPerformance;

public class Font{
	private final Log log = new Log(this);
	private float fontScaling = 1.0f;
	private int[] supportedSizes = null;
	private String defaultFont = null;
	private String defaultFontExtension = null;
	private int nFontsLoaded = -1;
	private int totalFonts = 0;
	private boolean isReady = false;

	private int getBestMatch(float requestedSize) {
		if (supportedSizes == null) throw new RuntimeException("Need to set supported font sizes first");

		int s = (int) (supportedSizes[supportedSizes.length-1] * fontScaling);
		for (int supportedSize : supportedSizes) {
			if (requestedSize <= ((float) supportedSize)) {
				s = (int) (supportedSize * fontScaling);
				break;
			}
		}
		return s;
	}

	public float getFontScaling(float requestedSize) {
		return requestedSize*fontScaling/(float)getBestMatch(requestedSize);
	}

	public void setSupportedFontSizes(int[] supportedFontSizes, float fontScaling) {
		this.fontScaling = fontScaling;
		this.supportedSizes = new int[supportedFontSizes.length];
		System.arraycopy(supportedFontSizes,0,this.supportedSizes,0,supportedFontSizes.length);
	}

	public void setDefaultFont(String fontName, String fontExtension) {
		this.defaultFont = fontName;
		this.defaultFontExtension = fontExtension;
	}

	public synchronized void loadFontAssetsAsync(IAssets assets, final IPerformance performance, Array<String> fontAssetKeys) {
		if (nFontsLoaded != -1) throw new RuntimeException("Fonts already loading. loadFontAssetsAsync cannot be called in this manner yet");
		nFontsLoaded = 0;
		totalFonts = fontAssetKeys.size;

		for (final String fontKey : fontAssetKeys) {
			assets.loadAssetsAsync(fontKey, new AssetLoaderListener() {
				@Override
				public void onAssetsLoaded() {
					fontConfigLoaded(fontKey, performance);
				}
			});
		}
	}

	public BitmapFont getFont(IAssets assets, String font, float size) {
		if (!isReady){
			Long startTime = System.currentTimeMillis();
			log.w().append("Waiting for fonts to load. Requested font: [").append(font).append("], size [").append(size,1).append("]").append("get");
			while(!isReady) isReady = assets.getAssetManager().update();
			log.w().append("Fonts ready in [").append(System.currentTimeMillis() - startTime).append("]ms").print();
		}

		int s = getBestMatch(size);
//		if (getFontScaling(size) > 1.1f || getFontScaling(size) < 0.7f) log.i().append(" ~~~~~~~ Font size [").append(size,1).append("] has scaling ").append(getFontScaling(size),1).print();

		if (font == null) font = defaultFont;
		BitmapFont f = assets.getAssetManager().get(font + "_" + s + defaultFontExtension);
		if (f != null) return f;
		else {
			f = assets.getAssetManager().get(defaultFont + s + defaultFontExtension);
			log.w().append(" ~~~~~~~~~ Font [").append(font).append("] not found. Using default").print();
			return f;
		}
	}

	private synchronized void fontConfigLoaded(String name, IPerformance performance){
		log.i().append("Finished loading ").append(name).append("[").append(performance.tock(),0).append("ms)").print();
		performance.tick();
		nFontsLoaded++;
		if (nFontsLoaded == totalFonts) {
			log.i().append("All fonts loaded").print();
			isReady = true;
			nFontsLoaded = -1;
			totalFonts = 0;
		}
	}
}
