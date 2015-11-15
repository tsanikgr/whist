package com.tsanikgr.whist_multiplayer.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.tsanikgr.whist_multiplayer.Config;
import com.tsanikgr.whist_multiplayer.IApplication;
import com.tsanikgr.whist_multiplayer.IMultiplayerController;
import com.tsanikgr.whist_multiplayer.IPlatformApplication;
import com.tsanikgr.whist_multiplayer.controllers.AppController;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.io.File;
import java.io.IOException;

public class DesktopLauncher {

	public static void main (String[] arg) {
		if (arg.length > 0) {
			updateResources();
			return;
		}

		Config.setDebug();
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Gdx Game";
		cfg.height = (int) (768/1.2);
		cfg.width = (int) (1366/1.2);
		cfg.addIcon("images/ic_launcher_16.png", Files.FileType.Internal);
		cfg.addIcon("images/ic_launcher_32.png", Files.FileType.Internal);
		cfg.addIcon("images/ic_launcher_64.png", Files.FileType.Internal);
		cfg.addIcon("images/ic_launcher_128.png", Files.FileType.Internal);
		cfg.addIcon("images/ic_launcher_256.png", Files.FileType.Internal);
		cfg.addIcon("images/ic_launcher_512.png", Files.FileType.Internal);
//		cfg.useHDPI = true;

		PlatformApplication pa = new PlatformApplication();
		IApplication app = new AppController(pa,
				new PlatformStorage(),
				new DesktopBuild(),
				new UIDDesktop(),
				null);

		pa.init(app,new LwjglApplication(app, cfg));
	}

	private static void updateResources(){
		TexturePacker.Settings settings = new TexturePacker.Settings();
		settings.alias = true;
		settings.alias=true;
		settings.alphaThreshold=0;
		settings.debug=false;
		settings.duplicatePadding=true;
		settings.edgePadding=false;
		settings.fast=false;
		// TODO:
		// Consider using mipmaps here
		settings.filterMag= Texture.TextureFilter.Linear;
		settings.filterMin= Texture.TextureFilter.Linear;
		settings.format= Pixmap.Format.RGBA8888;
		settings.ignoreBlankImages=true;
		settings.jpegQuality=1.0f;
		settings.outputFormat= "png";
		settings.paddingX=2;
		settings.paddingY=2;
		settings.pot=true;
		settings.rotation=false;
		settings.stripWhitespaceX=false;
		settings.stripWhitespaceY=false;
		settings.wrapX= Texture.TextureWrap.ClampToEdge;
		settings.wrapY= Texture.TextureWrap.ClampToEdge;
		settings.minHeight=16;
		settings.minWidth=16;
		settings.maxWidth = 2048;
		settings.maxHeight = 2048;

		File file = new File("raw_resources/images");
		String[] names = file.list();
		for(String name : names) {
			if (new File("raw_resources/images/" + name).isDirectory()) {
				TexturePacker.process(settings, "raw_resources/images/" + name + "/1366x768/", "images/1366x768/", name);
				TexturePacker.process(settings, "raw_resources/images/" + name + "/1920x1080/", "images/1920x1080/", name);
			}
		}

		Process p;
		try {
			p = Runtime.getRuntime().exec("/Users/nikos/Code/android/Whist/desktop/resources/raw_resources/copyToAndroid.command");
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			new Log(DesktopLauncher.class).e(e).print();
		}
	}

	private static class PlatformApplication implements IPlatformApplication {

		private IApplication app;
		private LwjglApplication lwjglApplication;

		public void init(IApplication app, LwjglApplication lwjglApplication) {
			this.app = app;
			this.lwjglApplication = lwjglApplication;
		}

		@Override
		public boolean isAndroid() {
			return false;
		}

//		public void keepScreenOn(boolean keepOn) {
//		}

		@Override
		public void shareText(String s) {

		}
		@Override
		public void rateApp() {

		}

		@Override
		public void keepScreenOn(boolean keepOn) {
		}

		@Override
		public void executePendingUiThreadRunnables() {
			if (app == null || lwjglApplication == null) return;
			if (!app.isUiThread()) {
				new Log(this).e().append("Function called from a thread OTHER than the UI thread").print();
				return;
			}
			lwjglApplication.executeRunnables();
		}
		@Override
		public void setMultiplayerControllerAndAttemptToConnect(IMultiplayerController multiplayerController) {
		}
	}
}

