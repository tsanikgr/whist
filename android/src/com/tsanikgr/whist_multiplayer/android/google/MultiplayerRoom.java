package com.tsanikgr.whist_multiplayer.android.google;

import android.app.Activity;
import android.content.Intent;

import com.badlogic.gdx.Gdx;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.tsanikgr.whist_multiplayer.IMultiplayerController;
import com.tsanikgr.whist_multiplayer.IMultiplayerGame;
import com.tsanikgr.whist_multiplayer.android.AndroidLauncher;
import com.tsanikgr.whist_multiplayer.models.MultiplayerMessage;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MultiplayerRoom implements RoomUpdateListener, RoomStatusUpdateListener, RealTimeMessageReceivedListener, IMultiplayerRoom {

	private final Log log = new Log(this);
	private final AndroidLauncher launcher;
	private final GoogleApiClient apiClient;
	private final IMultiplayerController multiplayerController;
	private final Messenger messenger;
	private IMultiplayerGame.In gameController;
	private String mRoomId = null;
	private ArrayList<Participant> mParticipants = null;
	private String mMyId = null;
	private boolean mPlaying = false;
	private final static int MIN_PLAYERS = 2;


	public MultiplayerRoom(AndroidLauncher launcher, GoogleApiClient apiClient, IMultiplayerController multiplayerController, IMultiplayerGame.In controller) {
		this.launcher = launcher;
		this.apiClient = apiClient;
		this.gameController = controller;
		this.multiplayerController = multiplayerController;
		this.messenger = new Messenger(apiClient, this);
		mRoomId = null;
	}

	/** ********************************************************************************************/
	@Override
	public void onRealTimeMessageReceived(final RealTimeMessage realTimeMessage) {
		messenger.onRealTimeMessageReceived(realTimeMessage);
	}

	public void sendMessage(MultiplayerMessage message) {
		try {
			messenger.sendMessage(message);
		} catch (ParticipantNotFoundException e) {
			log.e(e).print();
		}
	}

	/*************            RoomUpdateListener Implementation      ******************************/
	private boolean roomConnected = false;
	@Override
	public void onRoomConnected(int statusCode, Room room) {
		if (statusCode != GamesStatusCodes.STATUS_OK) {
			leaveRoomWithRefund("Room connection failed.");
			return;
		}
		log.i().append("Room connected").print();
		roomConnected = true;
		updateRoom(room);
	}

	@Override
	public void onJoinedRoom(int statusCode, Room room) {
		if (statusCode != GamesStatusCodes.STATUS_OK) {
			leaveRoomWithRefund("Unable to join room. Please try again.");
			return;
		}
		log.i().append("Joined room").print();
		updateRoom(room);
		mRoomId = room.getRoomId();
		Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(apiClient, room, 2);
		launcher.startActivityForResult(i, GoogleGames.RC_WAITING_ROOM);
	}

	@Override
	public void onRoomCreated(int statusCode, Room room) {
		if (statusCode != GamesStatusCodes.STATUS_OK) {
			leaveRoomWithRefund("Room could not be created. Please try again.");
			return;
		}
		log.i().append("Room created").print();
		updateRoom(room);
		mRoomId = room.getRoomId();
		Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(apiClient, room, 2);
		launcher.startActivityForResult(i, GoogleGames.RC_WAITING_ROOM);
	}

	@Override
	public void onLeftRoom(int i, String s) {
		mPlaying = false;
	}

	/********************       RoomStatusUpdateListener implementation       *********************/
	@Override
	public void onConnectedToRoom(Room room) {
		mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(apiClient));
		mRoomId = room.getRoomId();
		updateRoom(room);
		log.i().print();
		log.i().append("-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_").print();
		log.i().append("                  << CONNECTED TO ROOM>>").print();
	}

	@Override
	public void onPeersConnected(Room room, List<String> peers) {
		if (mPlaying) {
			//add new player to ongoing game
		} else if (shouldStartGame(room)) {
		}
	}

	@Override
	public void onDisconnectedFromRoom(Room room) {
		leaveRoomWithRefund("You were disconnected from the room.");
	}

	@Override
	public void onPeersDisconnected(Room room, List<String> peers) {
		if (mPlaying) {
			// do game-specific handling of this -- remove player's avatar
			// from the screen, etc. If not enough players are left for
			// the game to go on, end the game and leave the room.
		} else if (shouldCancelGame(room)) {
			leaveRoomWithRefund("Peers disconnected. Please try again.");
		}
		updateRoom(room);
	}

	@Override
	public void onPeerLeft(Room room, List<String> peers) {
		// peer left -- see if game should be canceled
		if (!mPlaying && shouldCancelGame(room)) {
			leaveRoomWithRefund("Peer left. Please try again.");
		}
		updateRoom(room);
	}

	@Override
	public void onPeerDeclined(Room room, List<String> peers) {
		// peer declined invitation -- see if game should be canceled
		if (!mPlaying && shouldCancelGame(room)) leaveRoomWithRefund("Peer declined. Please try again");
		updateRoom(room);
	}

	@Override
	public void onRoomConnecting(Room room) {
		updateRoom(room);
	}
	@Override
	public void onRoomAutoMatching(Room room) {
		updateRoom(room);
	}
	@Override
	public void onPeerInvitedToRoom(Room room, List<String> list) {
		updateRoom(room);
	}
	@Override
	public void onPeerJoined(Room room, List<String> list) {
		updateRoom(room);
	}
	@Override
	public void onP2PConnected(String s) {}
	@Override
	public void onP2PDisconnected(String s) {}

	/***********************     IMultiplayerRoom implementation *******************/
	@Override
	public IMultiplayerGame.In getGameController() {
		return gameController;
	}

	@Override
	public int participantToInt(String participantId) throws ParticipantNotFoundException{
		for (int i = 0; i < safe(mParticipants).size(); i++)
			if (getParticipant(i).getParticipantId().compareTo(participantId) == 0) return i;
		throw new ParticipantNotFoundException();
	}

	@Override
	public Participant getParticipant(int index) throws ParticipantNotFoundException {
		if (safe(mParticipants).size() <= index || mParticipants.get(index) == null) throw new ParticipantNotFoundException();
		return mParticipants.get(index);
	}

	@Override
	public Participant getParticipant(String participantId) throws ParticipantNotFoundException {
		return mParticipants.get(participantToInt(participantId));
	}

	@Override
	public String getRoomId() throws NotConnectedToRoomException {
		if (mRoomId == null) throw new NotConnectedToRoomException();
		return mRoomId;
	}

	@Override
	public int getPlayerIndex() throws ParticipantNotFoundException {
		return participantToInt(mMyId);
	}

	@Override
	public ArrayList<Participant> getParticipants() throws ParticipantNotFoundException {
		if (mParticipants == null) throw new ParticipantNotFoundException();
		return mParticipants;
	}

	/**********************      Helper Functions      **************************/


	private void updateRoom(Room room) {
		if (room != null) {
			mParticipants = room.getParticipants();
			log.i().append("Participants size: ").append(safe(mParticipants).size()).print();
		}
		if (gameController == null) return;
		try {
			if (mPlaying) {
				//add new player to ongoing game
			} else if (shouldStartGame(room) && roomConnected) {
				log.i().append("@@@@@@@@@@@@@@@@@@ Starting game. @@@@@@@@@@@@@@@@@@").print();
				log.i().append("Participant id is [").append(participantToInt(mMyId)).append("]").print();
				multiplayerController.goToGame();
				gameController.startGame(participantToInt(mMyId));
				mPlaying = true;

				//send handshake
				messenger.sendMessage(MultiplayerMessage.buildHandshake(participantToInt(mMyId), gameController.getPlayerNickname()));
			}
		} catch (ParticipantNotFoundException e) {
			log.e(e).print();
		}
	}

	// returns whether there are enough players to start the game
	private boolean shouldStartGame(Room room) {
		int connectedPlayers = 0;
		for (Participant p : room.getParticipants()) if (p.isConnectedToRoom()) connectedPlayers++;
		return connectedPlayers >= MIN_PLAYERS;
	}

	private boolean shouldCancelGame(Room room) {
		// TODO (Can check a participant's status with Participant.getStatus())
		return false;
	}

	void onWaitingRoomDone(int request, int response, Intent data) {
		if (response == Activity.RESULT_OK) {
		} else if (response == Activity.RESULT_CANCELED) {
			// Waiting room was dismissed with the back button.
			leaveRoomWithRefund("Game canceled");
		} else if (response == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
			// player wants to leave the room.
			leaveRoomWithRefund("Game canceled");
		} else if (response == GamesActivityResultCodes.RESULT_INVALID_ROOM){
			leaveRoomWithRefund("Game canceled");
		}
		Gdx.graphics.requestRendering();
	}

	public ArrayList<String> getPlayerNames() {
		ArrayList<String> names = new ArrayList<>(4);

		for (Participant participant : safe(mParticipants)) names.add(participant.getDisplayName());
		return names;
	}

	public void leaveRoomWithoutRefund(){
		messenger.cancelHearbeat();

		IMultiplayerGame.In gameControllerLocal = gameController;
		gameController = null;
		if (gameControllerLocal == null) return;

		if (mRoomId != null) Games.RealTimeMultiplayer.leave(apiClient, this, mRoomId);
		gameControllerLocal.cancelGame(false, "");

		mRoomId = null;
		mPlaying = false;
		launcher.keepScreenOn(false);
		log.i().append("$$$$$$$$$$$$$$$ Left room ***NO*** refund $$$$$$$$$$$$$$$$$$").print();
	}

	private void leaveRoomWithRefund(String message) {
		messenger.cancelHearbeat();

		IMultiplayerGame.In gameControllerLocal = gameController;
		gameController = null;
		if (gameControllerLocal == null) return;

		if (mRoomId != null) Games.RealTimeMultiplayer.leave(apiClient, this, mRoomId);
		gameControllerLocal.cancelGame(true, message);

		mRoomId = null;
		mPlaying = false;
		launcher.keepScreenOn(false);
		log.i().append("$$$$$$$$$$$$$$$ Left room WITH refund $$$$$$$$$$$$$$$$$$").print();
	}

	public static class ParticipantNotFoundException extends Exception {
	}

	public static class NotConnectedToRoomException extends Exception {
	}

	private static <T> List<T> safe(List<T> list) {
		return list == null ? Collections.<T>emptyList() : list;
	}
}
