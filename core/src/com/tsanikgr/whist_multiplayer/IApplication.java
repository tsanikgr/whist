package com.tsanikgr.whist_multiplayer;

import com.badlogic.gdx.ApplicationListener;

public interface IApplication extends ApplicationListener {
	IMenuController getMenuController();
	IScreenDirector getScreenDirector();
	boolean isUiThread();
}
