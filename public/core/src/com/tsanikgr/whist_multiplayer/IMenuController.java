package com.tsanikgr.whist_multiplayer;

import com.tsanikgr.whist_multiplayer.models.SettingsModel;

public interface IMenuController extends IScreenController{
	void hideNewGameView();
	void onSplashScreenDone();
	void onKeyboardHidden();
	void requestOnlinePlayerImage();
	boolean checkForNickName(String pendingButtonClick);
	void onCoinsClicked(float clickX, float clickY);
	void updateSettingsView(SettingsModel settingsModel);
	void onOnlineGameClicked(float stageX, float stageY);
}
