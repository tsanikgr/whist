package com.tsanikgr.whist_multiplayer;

import com.tsanikgr.whist_multiplayer.controllers.IWhistGameController;
import com.tsanikgr.whist_multiplayer.models.GameModel;

public interface IGameController extends IScreenController, IWhistGameController {
	boolean gameExists();
	void setGameParams(GameModel.WhistRoomConfig roomConfig);
	boolean validateGameAction(int player, boolean isDeclare, int result);
	void saveAndExit();
}
