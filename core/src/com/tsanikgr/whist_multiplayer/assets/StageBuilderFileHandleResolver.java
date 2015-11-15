package com.tsanikgr.whist_multiplayer.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class StageBuilderFileHandleResolver implements FileHandleResolver {

	private static final String IMAGES_DIR = "images";
	private static final String FONTS_DIR = "fonts";
	private static final String SOUNDS_DIR = "sounds";

	private final int screenWidth;
	private final List<Vector2> supportedResolutions;

	public StageBuilderFileHandleResolver(int width, List<Vector2> supportedResolutions) {
		screenWidth = width;
		this.supportedResolutions = supportedResolutions;

		Collections.sort(this.supportedResolutions, new Comparator<Vector2>() {

			@Override
			public int compare(Vector2 o1, Vector2 o2) {
				return (int) (o1.x - o2.x);
			}
		});
	}

	@Override
	public FileHandle resolve(String fileName) {
		if (fileName.startsWith(IMAGES_DIR + "/")) return Gdx.files.internal(fileName);
		String path = generateFilePath(fileName);
		return Gdx.files.internal(path);
	}

	private String generateFilePath(String fileName) {
		StringBuilder sb = new StringBuilder();
		if (isFontFile(fileName)) {
			sb.append(FONTS_DIR);
			sb.append("/");
		} else if (isSoundFile(fileName)) {
			sb.append(SOUNDS_DIR);
			sb.append("/");
		} else {
			Vector2 bestRes = findBestResolution();
			sb.append(IMAGES_DIR);
			sb.append("/");
			sb.append((int) bestRes.x);
			sb.append("x");
			sb.append((int) bestRes.y);
			sb.append("/");
		}
		sb.append(fileName);
		return sb.toString();
	}

	public Vector2 findBestResolution() {
		int minDiff = Integer.MAX_VALUE;
		int bestResIndex = 0;
		for (int i = 0; i < supportedResolutions.size(); i++) {
			int diff = Math.abs(screenWidth - (int) supportedResolutions.get(i).x);
			if (diff < minDiff) {
				minDiff = diff;
				bestResIndex = i;
			}
		}

		return supportedResolutions.get(bestResIndex);
	}

	private boolean isSoundFile(String fileName) {
		String lowerCaseFileName = fileName.toLowerCase(Locale.ENGLISH);
		return lowerCaseFileName.endsWith(".mp3") || lowerCaseFileName.endsWith(".ogg") || lowerCaseFileName.endsWith(".wav");
	}

	private boolean isFontFile(String fileName) {
		String lowerCaseFileName = fileName.toLowerCase(Locale.ENGLISH);
		return lowerCaseFileName.endsWith(".ttf") || lowerCaseFileName.endsWith(".otf") || lowerCaseFileName.endsWith(".gen");
	}
}
