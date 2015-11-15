package com.tsanikgr.whist_multiplayer.android.google;

import com.google.android.gms.games.multiplayer.Participant;
import com.tsanikgr.whist_multiplayer.IMultiplayerGame;

import java.util.ArrayList;

interface IMultiplayerRoom {
	IMultiplayerGame.In getGameController();
	int participantToInt(String senderParticipantId) throws MultiplayerRoom.ParticipantNotFoundException;
	Participant getParticipant(int index) throws MultiplayerRoom.ParticipantNotFoundException;
	Participant getParticipant(String participantId) throws MultiplayerRoom.ParticipantNotFoundException;
	String getRoomId() throws MultiplayerRoom.NotConnectedToRoomException;
	int getPlayerIndex() throws MultiplayerRoom.ParticipantNotFoundException;
	ArrayList<Participant> getParticipants()throws MultiplayerRoom.ParticipantNotFoundException;
}
