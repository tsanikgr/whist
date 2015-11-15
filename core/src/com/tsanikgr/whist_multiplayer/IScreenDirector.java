package com.tsanikgr.whist_multiplayer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;

public interface IScreenDirector {
	void goToMenu();
	void goToGame();
	void addActor(Actor actor);
	boolean removeActor(Actor actor);
	<T extends Actor> T findActor(String name);
	MyGroup buildGroup(String xmlFile, String groupName, MyGroup groupToFill);
	void buildGroupAsync(String xmlFile, String groupName, MyGroup groupToFill, IStageBuilderListener listener);
	void unfocusAll();

	IScreenController getActiveScreenController();
	IGameController setLocal();
	IMultiplayerGame.In setOnline();
}
