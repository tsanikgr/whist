package com.tsanikgr.whist_multiplayer.android.google;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Invitations;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.tsanikgr.whist_multiplayer.IImageBytesLoadedInterface;
import com.tsanikgr.whist_multiplayer.IMultiplayerController;
import com.tsanikgr.whist_multiplayer.IMultiplayerGame;
import com.tsanikgr.whist_multiplayer.android.AndroidLauncher;
import com.tsanikgr.whist_multiplayer.android.R;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.models.MultiplayerMessage;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.util.ArrayList;

public class GoogleGames implements GameHelper.GameHelperListener, IMultiplayerGame.Out {
	private final Log log = new Log(this);
	private final static int RC_SELECT_PLAYERS = 10000;
	private final static int RC_INVITATION_INBOX = 10001;
	final static int RC_WAITING_ROOM = 10002;
	private final static int RC_ACHIEVEMENTS = 10003;
	private static final int RC_LEADERBOARDS = 10004;
	private static final int CLIENT_ALL = GameHelper.CLIENT_ALL;

	private final AndroidLauncher launcher;
	private IMultiplayerController multiplayerController;
	private GameHelper mHelper;
	private MultiplayerRoom multiplayerRoom;
	private final ImageDownloader downloader;

	private final int mRequestedClients;
	private boolean mDebugLog = false;

	public GoogleGames(AndroidLauncher launcher) { this(CLIENT_ALL, launcher); }

	private GoogleGames(int requestedClients, AndroidLauncher launcher) {
		mRequestedClients = requestedClients;
		this.launcher = launcher;
		multiplayerController = null;
		multiplayerRoom = null;
		downloader = new ImageDownloader(launcher);
	}

	public void setMultiplayerController(IMultiplayerController multiplayerController) {
		this.multiplayerController = multiplayerController;
	}

	public void onStart() {
		if (mHelper == null) {
			getGameHelper();
		}
		if (mHelper == null) return;
		mHelper.setup(this);
		mHelper.onStart(launcher);
	}

	public void onStop() {
		if (multiplayerRoom != null) multiplayerRoom.leaveRoomWithoutRefund();
		unregisterInvitationListener();
		mHelper.onStop();
	}

	public void onActivityResult(int request, int response, Intent data) {
		mHelper.onActivityResult(request, response, data);
		if (request == RC_SELECT_PLAYERS) onInvitesSelected(request, response, data);
		else if (request == RC_WAITING_ROOM && multiplayerRoom != null) multiplayerRoom.onWaitingRoomDone(request, response, data);
		else if (request == RC_INVITATION_INBOX) onInvitationFromInboxSelected(request, response, data);
		else if (request == RC_ACHIEVEMENTS) {}
		else if (request == RC_LEADERBOARDS) {}
		else if (response != Activity.RESULT_OK) {
//			BaseGameUtils.showActivityResultError(launcher,request,response, R.string.signin_other_error);
		}
	}

	@Override
	public void onSignInFailed() {
		if (multiplayerController != null) multiplayerController.signInFailed();
	}

	@Override
	public void onSignInSucceeded() {
		if (multiplayerController != null) multiplayerController.signInSucceeded();
	}

	@Override
	public boolean isSignedIn() {
		return mHelper.isSignedIn();
	}

	@Override
	public void beginUserInitiatedSignIn() {
		mHelper.beginUserInitiatedSignIn();
	}

	@Override
	public void signOut() {
		mHelper.signOut();
	}

	@Override
	public void requestOnlinePlayerImage(IImageBytesLoadedInterface listener, int imageHeight, int imageWidth) {
		if (!isSignedIn()) return;
		if (!Games.Players.getCurrentPlayer(getApiClient()).hasHiResImage()) return;
		downloader.downloadImage(Games.Players.getCurrentPlayer(getApiClient()).getHiResImageUri(), listener, imageWidth, imageHeight);
	}

	public void reconnectClient() {
		mHelper.reconnectClient();
	}

	@Override
	public boolean hasSignInError() {
		return mHelper.hasSignInError();
	}

	@Override
	public void sendMessage(MultiplayerMessage message) {
		if (multiplayerRoom == null) log.e().append("Multiplayer room is NULL, cannot send message").print();
		else multiplayerRoom.sendMessage(message);
	}

	@Override
	public ArrayList<String> getPlayerNames() {
		if (multiplayerRoom == null) return null;
		return multiplayerRoom.getPlayerNames();
	}

	@Override
	public void leaveGame() {
		if (multiplayerRoom != null) multiplayerRoom.leaveRoomWithoutRefund();
		else log.w().append("Multiplayer room is NULL. Ignoring leave request.").print();
		multiplayerRoom = null;
	}

	@Override
	public void unlockAchievement(String achievementId) {
		String packageName = launcher.getPackageName();
		try {
			int resId = launcher.getResources().getIdentifier("achievement_" + achievementId, "string", packageName);
			Games.Achievements.unlock(getApiClient(), launcher.getString(resId));
		} catch (Resources.NotFoundException e) {
			log.e().append("Achievement [").append(achievementId).append("] not found in resources").print();
		}
	}

	@Override
	public void showAchievements() {
		if (isSignedIn()) launcher.startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), RC_ACHIEVEMENTS);
		else beginUserInitiatedSignIn();
	}

	@Override
	public void showLeaderboards() {
		if (isSignedIn()) launcher.startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient()), RC_LEADERBOARDS);
		else beginUserInitiatedSignIn();
	}

	@Override
	public void postScoreToLeaderboards(long score) {
		Games.Leaderboards.submitScore(getApiClient(), launcher.getString(R.string.leaderboard_highscores), score);
	}

	@Override
	public void postELOToLeaderboards(long ELO) {
		Games.Leaderboards.submitScore(getApiClient(), launcher.getString(R.string.leaderboard_elos), ELO);
	}

	private GoogleApiClient getApiClient() {
		return mHelper.getApiClient();
	}

	private GameHelper getGameHelper() {
		if (mHelper == null) {
			mHelper = new GameHelper(launcher, mRequestedClients);
			mHelper.enableDebugLog(mDebugLog);
		}
		return mHelper;
	}

	protected void showAlert(String message) {
		mHelper.makeSimpleDialog(message).show();
	}

	protected void showAlert(String title, String message) {
		mHelper.makeSimpleDialog(title, message).show();
	}

	protected void enableDebugLog(boolean enabled) {
		mDebugLog = true;
		if (mHelper != null) {
			mHelper.enableDebugLog(enabled);
		}
	}

	protected GameHelper.SignInFailureReason getSignInError() {
		return mHelper.getSignInError();
	}

	@Override
	public void registerInvitationListener(){
		if (mHelper == null) return;
		unregisterInvitationListener();
		if (!isSignedIn()) return;
		Games.Invitations.registerInvitationListener(getApiClient(), new OnInvitationReceivedListener() {
			@Override
			public void onInvitationReceived(Invitation invitation) {
				if (multiplayerController == null) return;
				multiplayerController.onInvitationReceived(invitationToWhistConfig(invitation));
			}
			@Override
			public void onInvitationRemoved(String invitationId) {
			}
		});
	}

	public void unregisterInvitationListener(){
		if (mHelper == null) return;
		try {
			if (isSignedIn()) Games.Invitations.unregisterInvitationListener(getApiClient());
		} catch (Exception e) {
			log.e(e).print();
		}
	}

	private RoomConfig createRoomConfig(IMultiplayerGame.In gameController, Bundle autoMatchCriteria, ArrayList<String> invitees) {
		if (multiplayerRoom != null) multiplayerRoom.leaveRoomWithoutRefund();
		multiplayerRoom = new MultiplayerRoom(launcher, getApiClient(), multiplayerController, gameController);
		GameModel.WhistRoomConfig config = gameController.getWhistConfig();
		RoomConfig.Builder builder = RoomConfig.builder(multiplayerRoom)
				.setMessageReceivedListener(multiplayerRoom)
				.setRoomStatusUpdateListener(multiplayerRoom)
				.setVariant(config.toVariant());

		if (config.getInvitationId() != null) builder.setInvitationIdToAccept(config.getInvitationId());
		if (autoMatchCriteria != null) builder.setAutoMatchCriteria(autoMatchCriteria);
		if (invitees != null) builder.addPlayersToInvite(invitees);
		return builder.build();
	}

	@Override
	public void startQuickGame(IMultiplayerGame.In gameController) {
		Bundle am = RoomConfig.createAutoMatchCriteria(1, 3, 0);
		Games.RealTimeMultiplayer.create(getApiClient(), createRoomConfig(gameController, am, null));
		launcher.keepScreenOn(true);
	}

	private GameModel.WhistRoomConfig invitationToWhistConfig(Invitation invitation) {
		GameModel.WhistRoomConfig config = GameModel.WhistRoomConfig.fromVariant(invitation.getVariant());
		config.setInviterName(invitation.getInviter().getDisplayName());
		config.setInvitationId(invitation.getInvitationId());
//		config.setInviterImage();

		return config;
	}

	@Override
	public void invitePlayers(GameModel.WhistRoomConfig roomConfig) {
		Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(getApiClient(), 1, 3);
		launcher.startActivityForResult(intent, RC_SELECT_PLAYERS);
	}

	@Override
	public void seeInvitations(){
		Intent intent = Games.Invitations.getInvitationInboxIntent(getApiClient());
		launcher.startActivityForResult(intent, RC_INVITATION_INBOX);
	}

	@Override
	public boolean hasAppLaunchInvite() {
		return mHelper != null && mHelper.hasInvitation();
	}

	@Override
	public GameModel.WhistRoomConfig getAppLaunchInvitation() {
		final Invitation inv = mHelper.getInvitation();
		mHelper.clearInvitation();
		if (inv == null) return null;
		return invitationToWhistConfig(inv);
	}

	@Override
	public void checkForMorePendingInvitations() {
		PendingResult<Invitations.LoadInvitationsResult> invitationsPendingResult = Games.Invitations.loadInvitations(getApiClient(), Multiplayer.SORT_ORDER_SOCIAL_AGGREGATION);
		invitationsPendingResult.setResultCallback(new ResultCallback<Invitations.LoadInvitationsResult>() {
			@Override
			public void onResult(Invitations.LoadInvitationsResult loadInvitationsResult) {
				if (multiplayerController != null && loadInvitationsResult.getInvitations().getCount() != 0) {
					Invitation invitation = loadInvitationsResult.getInvitations().get(0);
					multiplayerController.onInvitationReceived(invitationToWhistConfig(invitation));
				}
				loadInvitationsResult.getInvitations().release();
			}
		});
	}

	private void onInvitesSelected(int request, int response, Intent data){
		if (response == Activity.RESULT_OK) {
			multiplayerController.onInvitesSelected(data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS),
					data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0),
					data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0));
		} else multiplayerController.onInvitesSelected(null, -1, -1);
	}

	@Override
	public void invitePlayersAndStartGame(IMultiplayerGame.In gameController, ArrayList<String> invitees, int minAutomatchPlayers, int maxAutomatchPlayers) {
		Bundle autoMatchCriteria;
		if (minAutomatchPlayers > 0) autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutomatchPlayers, maxAutomatchPlayers, 0);
		else autoMatchCriteria = null;
		Games.RealTimeMultiplayer.create(getApiClient(), createRoomConfig(gameController, autoMatchCriteria, invitees));
		launcher.keepScreenOn(true);
	}

	@Override
	public void onInvitationAccepted(IMultiplayerGame.In gameController){
		if (multiplayerController == null) return;
		Games.RealTimeMultiplayer.join(getApiClient(), createRoomConfig(gameController, null, null));
		launcher.keepScreenOn(true);
	}

	@Override
	public void onInvitationRejected(GameModel.WhistRoomConfig config){
		Games.RealTimeMultiplayer.declineInvitation(getApiClient(), config.getInvitationId());
	}

	private void onInvitationFromInboxSelected(int request, int response, Intent data) {
		if (response != Activity.RESULT_OK) {
			multiplayerController.onInvitationReceived(null);
			return; // canceled
		}
		final Invitation invitation =	data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);
		if (invitation == null || invitation.getInvitationId() == null) return;
		multiplayerController.onInvitationReceived(invitationToWhistConfig(invitation));
	}
}