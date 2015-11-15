package com.tsanikgr.whist_multiplayer.controllers;

import com.tsanikgr.whist_multiplayer.IDialogueListener;
import com.tsanikgr.whist_multiplayer.IMultiplayerController;
import com.tsanikgr.whist_multiplayer.IMultiplayerGame;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.views.menu.CommonView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiplayerController extends Controller implements IMultiplayerController {

	private String pendingButton = null;
	private final AtomicBoolean canPerformOnSignInActions = new AtomicBoolean(false);

	@Override
	protected void init() {
	}

	@Override
	protected void disposeController() {
	}

	public boolean onOnlineGameClicked() {
		return isSignedIn("online", "Signing in to play online");
	}

	public void onInviteClicked(GameModel.WhistRoomConfig roomConfig) {
		getScreenDirector().setOnline().setMultiplayerGameParams(roomConfig);
		getMultiplayer().invitePlayers(roomConfig);
	}

	public void onInvitationsClicked() {
		getMultiplayer().seeInvitations();
	}

	public void onQuickGameClicked(GameModel.WhistRoomConfig roomConfig) {
		IMultiplayerGame.In controller = getScreenDirector().setOnline();
		controller.setMultiplayerGameParams(roomConfig);
		getMultiplayer().startQuickGame(controller);
	}

	public void onLogoutClicked() {
		if (getMultiplayer() == null) return; // desktop mode
		if (getMultiplayer().isSignedIn()) {
			getMultiplayer().signOut();
			getMenuController().showDialogue("You have been logged out.", "", false);
			canPerformOnSignInActions.set(true);
		} else getMenuController().showDialogue("You are not currently signed in.", "", false);
	}

	public void onLeaderboardsClicked() {
		if (getMultiplayer() == null) return; // desktop mode
		if (!isSignedIn("leaderboards", "Signing in to see leaderboards")) return;
		getMultiplayer().showLeaderboards();
	}
	public void onAchievementsClicked() {
		if (getMultiplayer() == null) return; // desktop mode
		if (!isSignedIn("achievements", "Signing in to see achievements")) return;
		getMultiplayer().showAchievements();
	}

	@Override
	public void goToGame(){
		getMenuController().hideLoadingDialogue();
		getMenuController().hideNewGameView();
		getScreenDirector().goToGame();
	}

	private boolean isSignedIn(String pendingButtonClick, String message) {
		if (getMultiplayer().isSignedIn()) return true;
		pendingButton = pendingButtonClick;
		getMenuController().showLoadingDialogue(message, -1, -1);
		getMultiplayer().beginUserInitiatedSignIn();
		return false;
	}

	@Override
	public void signInFailed() {
		if (pendingButton == null) return;
		pendingButton = null;
		getMenuController().hideLoadingDialogue();
		getMenuController().showDialogue("Could not sign in to play online", "Please try again", false);
		if (getMultiplayer().hasSignInError()) log.e().append("Has sign in error.").print();
	}

	@Override
	public void signInSucceeded() {
		onSignedInAndMainMenuAssetsLoaded();
	}

	synchronized boolean checkIfLaunchedFromInvitation() {
		if (!getMenuController().checkForNickName(null)) return false;    //this is true only if the player is automatically signed in, and has manually deleted the statistics preference file
		boolean signedIn = getMultiplayer() != null && getMultiplayer().isSignedIn();
		boolean hasInvitation = signedIn && processInvitations(getMultiplayer().getAppLaunchInvitation());
		if (signedIn && !hasInvitation) getMultiplayer().checkForMorePendingInvitations();
		return hasInvitation;
	}

	@Override
	public void onInvitesSelected(ArrayList<String> inviteeIds, int minAutomatchPlayers, int maxAutomatchPlayers) {
		getMenuController().hideLoadingDialogue();
		getMultiplayer().invitePlayersAndStartGame(getScreenDirector().setOnline(), inviteeIds, minAutomatchPlayers, maxAutomatchPlayers);
		if (inviteeIds != null) getMenuController().showLoadingDialogue("Joining waiting room", -1, -1);
	}

	@Override
	public void onInvitationReceived(GameModel.WhistRoomConfig roomConfig) {
		//maybe here display a pop-up, non-clickable notification
		//or notify the invitee that this player is already in another online game
		getMenuController().hideLoadingDialogue();
		if (roomConfig == null) return;
		if (getMenuController().getActiveScreenController() instanceof MultiplayerGameController) return;
		if (getMenuController().getActiveScreenController() instanceof GameController)
			((GameController)getMenuController().getActiveScreenController()).saveAndExit();
		processInvitations(roomConfig);
	}

	private boolean processInvitations(final GameModel.WhistRoomConfig invitationConfig) {
		if (invitationConfig == null) return false;

		if (!getStatisticsController().hasEnoughtCoins(invitationConfig.bet)) {
			getMenuController().showDialogue("Cannot accept invitation from " + invitationConfig.getInviterName(), "Their bet is " + invitationConfig.bet + ", you have " + getStatisticsController().getStatistics().getCoins(), false);
			getMenuController().onCoinsClicked(-1,-1);
			return false;
		}

		getMenuController().showDialogue("Accept the invitation from " + invitationConfig.getInviterName() + "?", "The bet is " + invitationConfig.bet, true, new IDialogueListener() {
			@Override
			public void onDialogueResult(int result) {
				getMenuController().hideDialogue();
				getSounds().playPopSound();
				if (result == CommonView.Dialogue.YES) {
					getScreenDirector().setOnline().setMultiplayerGameParams(invitationConfig);
					getMultiplayer().onInvitationAccepted(getScreenDirector().setOnline());
					getMenuController().showLoadingDialogue("Joining game...", -1, -1);
				} else {
					getMultiplayer().onInvitationRejected(invitationConfig);
					getMultiplayer().checkForMorePendingInvitations();
				}
			}
		});
		return true;
	}

	void onMainMenuAssetsLoaded() {
		canPerformOnSignInActions.set(true);
		onSignedInAndMainMenuAssetsLoaded();
	}

	private void onSignedInAndMainMenuAssetsLoaded() {
		if (!getMultiplayer().isSignedIn() || !canPerformOnSignInActions.compareAndSet(true, false)) return;
//		Gdx.app.postRunnable(new Runnable() {
//			@Override
//			public void run() {
				getMenuController().hideLoadingDialogue();
				getMultiplayer().registerInvitationListener();
				getMenuController().requestOnlinePlayerImage();
				getStatisticsController().pushAccomplishments();

				if (checkIfLaunchedFromInvitation()) {
					pendingButton = null;
					return;
				}

				if (pendingButton == null) return;     //automatic sign-in on startup
				String pending = pendingButton;
				pendingButton = null;

				if (pending.compareTo("online") == 0) getMenuController().onOnlineGameClicked(-1, -1);
				else if (pending.compareTo("achievements") == 0) onAchievementsClicked();
				else if (pending.compareTo("leaderboards") == 0) onLeaderboardsClicked();
			}
//		});
//	}
}
