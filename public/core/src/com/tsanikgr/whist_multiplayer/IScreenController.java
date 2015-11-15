package com.tsanikgr.whist_multiplayer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;

public interface IScreenController extends EventListener{
	void alignToScreen(int alignX, int alignY, Actor... actorsToAlign);
	void addButtonListenersRecursively(Group group);
	void removeButtonListenersRecursively(Group group);
	void buildViewAsync(String name, String groupName, MyGroup view);
	void buildViewSync(String xmlFile, String groupName, MyGroup view);
	boolean handleBack();

	boolean isShowingDialogue();
	boolean isShowingloadingDialogue();
	void showLoadingDialogue(String text, float clickX, float clickY);
	void hideLoadingDialogue();
	void showDialogue(String title, String subtitle, boolean hasYesNoButtons);
	void showDialogue(String title, String subtitle, boolean hasYesNoButtons, IDialogueListener listener);
	void hideAnimationDialogue();
	void hideDialogue();

	IPerformance getPerformance();
	IResolution getResolution();
	IScreenController getActiveScreenController();
	void updateAssetManager();
	boolean isRenderThread();
	void executePendingUiThreadRunnables();
}
