package com.tsanikgr.whist_multiplayer;

import com.tsanikgr.whist_multiplayer.models.GameModel;

import java.util.ArrayList;

public interface IMultiplayerController {
	void signInFailed();
	void signInSucceeded();
	void onInvitationReceived(GameModel.WhistRoomConfig roomConfig);
	void onInvitesSelected(ArrayList<String> inviteeIds, int minAutomatchPlayers, int maxAutomatchPlayers);
	void goToGame();
}
