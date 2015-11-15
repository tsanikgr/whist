package com.tsanikgr.whist_multiplayer;

import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.models.MultiplayerMessage;

import java.util.ArrayList;

public interface IMultiplayerGame {

	interface Out{
		boolean isSignedIn();
		void beginUserInitiatedSignIn();
		void signOut();
		boolean hasSignInError();
		void requestOnlinePlayerImage(IImageBytesLoadedInterface listener, int imageWidth, int imageHeight);
		void registerInvitationListener();

		void startQuickGame(IMultiplayerGame.In gameController);
		void invitePlayersAndStartGame(IMultiplayerGame.In gameController, ArrayList<String> invitees, int minAutomatchPlayers, int maxAutomatchPlayers);
		void onInvitationAccepted(IMultiplayerGame.In gameController);
		void onInvitationRejected(GameModel.WhistRoomConfig config);

		void invitePlayers(GameModel.WhistRoomConfig roomConfig);
		void seeInvitations();
		boolean hasAppLaunchInvite();
		GameModel.WhistRoomConfig getAppLaunchInvitation();
		void checkForMorePendingInvitations();

		void sendMessage(MultiplayerMessage message);
		ArrayList<String> getPlayerNames();
		void leaveGame();

		void showAchievements();
		void unlockAchievement(String achievementId);
		void showLeaderboards();
		void postScoreToLeaderboards(long score);
		void postELOToLeaderboards(long ELO);
	}

	interface In{
		boolean onMessageReceived(MultiplayerMessage message);
		void startGame(int playerId);
		void cancelGame(boolean refundPlayer, String message);
		void setMultiplayerGameParams(GameModel.WhistRoomConfig roomConfig);
		GameModel.WhistRoomConfig getWhistConfig();
		String getPlayerNickname();
		void setPlayerNickname(int fromPlayer, String string);
	}
}
