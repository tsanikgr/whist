package com.tsanikgr.whist_multiplayer.android.google;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.tsanikgr.whist_multiplayer.models.MultiplayerMessage;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

class Messenger {

	private final Log log = new Log(this);
	private static final float RETRY_DELAY_SECONDS = 1f;
	private static final int MAX_RETRY_ATTEMPTS = 10;
	private static final int KEEP_IN_OUTBOX = 10;
	private static final float HEARTBEAT_SECONDS = 15f;    //sends dropped message request after this many seconds, if no other message was sent/received
	private static final float HEARTBEAT_CHECKING_SECONDS = HEARTBEAT_SECONDS/5f;  //time between hearbeat checks
																											//useful when the user switches activities, or at the start of the game,
																											//so that he doesn't have to wait 15 seconds
	private final MyReliableMessageCallback messageCallback;
	private final ArrayList<MultiplayerMessage> inbox;
	private final LinkedList<MultiplayerMessage> outbox;

	private final GoogleApiClient apiClient;
	private final IMultiplayerRoom multiplayerRoom;

	private final AtomicBoolean hearbeatInitialised;
	private Timer heartbeatTimer = null;
	private int lastMessageOrder;
	private long lastMessageTimestamp;

	public Messenger(GoogleApiClient apiClient, IMultiplayerRoom multiplayerRoom) {
		this.apiClient = apiClient;
		this.multiplayerRoom = multiplayerRoom;
		this.inbox = new ArrayList<>(3);
		this.outbox = new LinkedList<>();
		lastMessageOrder = -1;
		messageCallback = new MyReliableMessageCallback();
		hearbeatInitialised = new AtomicBoolean(false);
	}

	public void sendMessage(MultiplayerMessage message) throws MultiplayerRoom.ParticipantNotFoundException {
		if (message.isBroadcast()) broadcastMessage(message);
		else sendMessage(getParticipant(message.getRecepient()).getParticipantId(), message);

		updateOutbox(message);
		if (!message.isSpecialOrder()) lastMessageOrder++;

		if (hearbeatInitialised.compareAndSet(false, true)) initHearbeatTimer();
	}

	private void sendMessage(final String receipientId, final MultiplayerMessage multiplayerMessage) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				try {
					log.i().append("################## SENDING MESSAGE ####################").print();
					byte[] message = multiplayerMessage.toByte();
					int tokenId = Games.RealTimeMultiplayer.sendReliableMessage(apiClient, messageCallback, message,
							getRoomId(), receipientId);
					multiplayerMessage.sentWithTokenId(tokenId);
					lastMessageTimestamp = System.currentTimeMillis();
					log.i().append("####################################################################").print();
				} catch (MultiplayerRoom.NotConnectedToRoomException e) {
					log.e(e).print();
				}
			}
		});
	}

	private void broadcastMessage(final MultiplayerMessage multiplayerMessage){
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				try {
					log.i().append("################## BROADCASTING MESSAGE ####################").print();
					int playerIndex = multiplayerRoom.getPlayerIndex();
					ArrayList<Participant> participants = multiplayerRoom.getParticipants();
					final byte[] message = multiplayerMessage.toByte();
					for (int i = 0; i < multiplayerRoom.getParticipants().size(); i++) {
						if (playerIndex == i) continue;
						if (participants.get(i) == null || !participants.get(i).isConnectedToRoom())
							continue;
						int tokenId = Games.RealTimeMultiplayer.sendReliableMessage(apiClient, messageCallback, message, multiplayerRoom.getRoomId(), participants.get(i).getParticipantId());
						multiplayerMessage.sentWithTokenId(tokenId);
					}
					lastMessageTimestamp = System.currentTimeMillis();
					log.i().append("####################################################################").print();
				} catch (MultiplayerRoom.NotConnectedToRoomException | MultiplayerRoom.ParticipantNotFoundException e) {
					log.e(e).print();
				}
			}
		});
	}

	public void onRealTimeMessageReceived(final RealTimeMessage realTimeMessage) {
		//run it on the ui thread:
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (multiplayerRoom.getGameController() == null) return;
				MultiplayerMessage message = decodeMessage(realTimeMessage);

				if (message ==null) return;
				if (message.isSpecialOrder()) processSpecialOrderMessage(message);
				else if (message.getOrder() > lastMessageOrder + 1) addToInboxAndRequestDropped(message);
				else if (message.getOrder() < lastMessageOrder + 1) {
					log.w().append("Already received message with order [").append(message.getOrder()).append("]. Ignoring").print();
					message.done();
				}
				else if (!processCorrectOrderMessage(message)) return;
				lastMessageTimestamp = System.currentTimeMillis();
				log.i().append("####################################################################").print();
			}
		});
	}

	private MultiplayerMessage decodeMessage(RealTimeMessage realTimeMessage) {
		log.i().append("################## Multiplayer message received. ####################").print();
		MultiplayerMessage message = null;
		try {
			message = MultiplayerMessage.fromByte(realTimeMessage.getMessageData(), multiplayerRoom.participantToInt(realTimeMessage.getSenderParticipantId()));
		} catch (MultiplayerRoom.ParticipantNotFoundException e){
			log.e(e).append("Participant not found").print();
		}catch (Exception e) {
			log.e(e).append("Could not decode message").print();
		}
		if (message == null) log.e().append("Message is null.").print();
		return message;
	}

	private boolean processCorrectOrderMessage(MultiplayerMessage message) {
		log.i().append("Message in correct order.").print();
		if (!multiplayerRoom.getGameController().onMessageReceived(message)) return false;
		lastMessageOrder++;
		checkInboxForOutstanding();
		return true;
	}

	private void processSpecialOrderMessage(MultiplayerMessage message) {
		log.w().append("Special order message received").print();
		if (message.getType() == MultiplayerMessage.Type.DROPPED_MESSAGE_REQUEST) processDroppedMessageRequest(message);
		else if (message.getType() == MultiplayerMessage.Type.HANDSHAKE) processHandshake(message);

		message.done();
	}

	private void processHandshake(MultiplayerMessage message) {
		log.i().append("Handshake received").print();
		multiplayerRoom.getGameController().setPlayerNickname(message.getFromPlayer(), message.getString());
	}

	private void processDroppedMessageRequest(MultiplayerMessage message) {
		try{
			MultiplayerMessage droppedMessage = getFromOutbox(message.getMessage());
			if (droppedMessage != null) {
				log.w().append("Resending message with order [").append(droppedMessage.getOrder()).append("]").print();
				if (droppedMessage.getMessage() == 0) {
					log.w().append("Dropped request is for seed. Sending nickname again.").print();
					sendMessage(MultiplayerMessage.buildHandshake(multiplayerRoom.getPlayerIndex(), multiplayerRoom.getGameController().getPlayerNickname()));
				}
				sendMessage(getParticipant(message.getFromPlayer()).getParticipantId(), droppedMessage);
			} else {
				log.w().append("Ignoring dropped message request, not in outbox.").print();
			}
		} catch (MultiplayerRoom.ParticipantNotFoundException e) {
			log.e(e).print();
		}
	}

	private void addToInboxAndRequestDropped(MultiplayerMessage message) {
		if (!inboxContains(message)) {
			inbox.add(message);
			log.w().append("Message with order [").append(message.getOrder()).append("] received, ").
					append("was expecting [").append(lastMessageOrder + 1).print();
			log.w().append("-> Adding to inbox").print();
		}
		if (!inbox.isEmpty()) requestDroppedMessage(lastMessageOrder + 1);
	}

	private boolean inboxContains(MultiplayerMessage message) {
		for (MultiplayerMessage m : inbox) if (m.getOrder() == message.getOrder()) {
			log.e().append("Inbox already contains message with order ").append(m.getOrder()).append(". Ignoring").print();
			message.done();
			return true;
		}
		return false;
	}

	private MultiplayerMessage getFromOutbox(long order) {
		Iterator<MultiplayerMessage> it = outbox.iterator();
		MultiplayerMessage message;
		while(it.hasNext()) {
			message = it.next();
			if (message.getOrder() == order) return message;
		}
		return null;
	}

	private void requestDroppedMessage(int order) {
		try {
			log.w().append("Requesting dropped message").print();
			sendMessage(MultiplayerMessage.buildDroppedMessageRequest(multiplayerRoom.getPlayerIndex(), order));
		} catch (MultiplayerRoom.ParticipantNotFoundException e) {
			log.e(e).print();
		}
	}

	private void checkInboxForOutstanding() {
		if (multiplayerRoom.getGameController() == null) return;
		boolean found = true;
		while (!inbox.isEmpty() && found) {
			found = false;
			for (MultiplayerMessage m : inbox) {
				if (m.getOrder() == lastMessageOrder + 1) {
					log.i().append("Found outstanding message. Processing").print();
					found = true;
					lastMessageOrder++;
					multiplayerRoom.getGameController().onMessageReceived(m);
					inbox.remove(m);
					break;
				}
			}
		}
		if (!inbox.isEmpty()) requestDroppedMessage(lastMessageOrder + 1);
	}

	private void updateOutbox(MultiplayerMessage message){
		if (message.isSpecialOrder()) return;
		outbox.offer(message);
		if (outbox.size() > KEEP_IN_OUTBOX) outbox.poll().done();
	}

	private Participant getParticipant(int index) throws MultiplayerRoom.ParticipantNotFoundException {
		return multiplayerRoom.getParticipant(index);
	}

	private Participant getParticipant(String participantId) throws MultiplayerRoom.ParticipantNotFoundException {
		return multiplayerRoom.getParticipant(participantId);
	}

	private String getRoomId() throws MultiplayerRoom.NotConnectedToRoomException {
		return multiplayerRoom.getRoomId();
	}

	private void initHearbeatTimer() {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
//				if (heartbeatTimer != null) return;
				log.w().append("Init hearbeat").print();
				lastMessageTimestamp = System.currentTimeMillis();
				heartbeatTimer = new Timer();
				heartbeatTimer.scheduleTask(new Timer.Task() {
					@Override
					public void run() {
						if (System.currentTimeMillis() - lastMessageTimestamp > HEARTBEAT_SECONDS * 1000f) {
							log.w().append("Heartbeat requesting dropped message").print();
							requestDroppedMessage(lastMessageOrder + 1);
							lastMessageTimestamp = System.currentTimeMillis();
						}
						if (lastMessageOrder == -1) {
							log.w().append("Heartbeat requesting dropped message").print();
							requestDroppedMessage(0);
							lastMessageTimestamp = System.currentTimeMillis();
						}

					}
				}, HEARTBEAT_CHECKING_SECONDS, HEARTBEAT_CHECKING_SECONDS);
			}
		});
	}

	public void cancelHearbeat(){
		if (heartbeatTimer != null) {
			log.w().append("Heartbeat stopped").print();
			heartbeatTimer.stop();
			heartbeatTimer.clear();
		}
	}

	private MultiplayerMessage getFromOutboxUsingTokenId(int tokenId) {
		for (MultiplayerMessage message : outbox) {
			if (message.hasTokenId(tokenId)) return message;
		}
		return null;
	}

	private class MyReliableMessageCallback implements RealTimeMultiplayer.ReliableMessageSentCallback {

		final Timer retryTimer = new Timer();
		@Override
		public void onRealTimeMessageSent(int statusCode, final int tokenId, final String recipientParticipantId) {
			final MultiplayerMessage message = getFromOutboxUsingTokenId(tokenId);

			if (statusCode == GamesStatusCodes.STATUS_OK) {
				if (message == null) log.i().append("Message with tokenId [").append(tokenId).append("] successfully sent (not in outbox)").print();
				else log.i().append("Message ").append(MultiplayerMessage.Type.toString(message.getType())).append(" with order [").append(message.getOrder()).append("] successfully sent").print();
				return;
			}
			if (statusCode == GamesStatusCodes.STATUS_REAL_TIME_MESSAGE_SEND_FAILED) {
				if (message != null) {
					log.w().append("Sending message failed. Retrying.").print();
					retry(recipientParticipantId, message);
				} else log.w().append("Sending message failed. Cannot retry, NOT in outbox").print();
			}
			if (statusCode == GamesStatusCodes.STATUS_REAL_TIME_ROOM_NOT_JOINED) {
				if (message != null) {
					log.w().append("Sending message failed (player has not yet joined). Retrying in 1s.").print();
					retry(recipientParticipantId, message);
				} else log.w().append("Sending message failed (player has not yet joined). Cannot retry, NOT in outbox").print();
			}
		}

		public void retry(final String recipientParticipantId, final MultiplayerMessage message) {
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					retryTimer.scheduleTask(new Timer.Task() {
						@Override
						public void run() {
							try {
								if (message.getRetryAttempts() > MAX_RETRY_ATTEMPTS) {
//				               leaveRoomWithRefund("Opponent not responding.");
									log.e().append("Maximum retry attempts").print();
									return;
								}
								if ((getParticipant(recipientParticipantId) != null) && !getParticipant(recipientParticipantId).isConnectedToRoom()) {
									log.e().append("Stop retrying, participantId not found in room.").print();
									return;
								}

								sendMessage(recipientParticipantId, message);
								message.incrementRetryAttempts();
								log.w().append("Retrying message with order [").append(message.getOrder()).append("] for the ").append(message.getRetryAttempts()).append(" time").print();
							} catch (MultiplayerRoom.ParticipantNotFoundException e) {
								log.e(e).print();
							}
						}
					}, RETRY_DELAY_SECONDS);
				}
			});
		}
	}
}
