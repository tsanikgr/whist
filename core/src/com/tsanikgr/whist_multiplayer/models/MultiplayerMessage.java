package com.tsanikgr.whist_multiplayer.models;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MultiplayerMessage implements Pool.Poolable{

	private final static Log log = new Log(null);
	private static final int BROADCAST = -1;
	private static final int SPECIAL_ORDER = Short.MAX_VALUE;

	private int order;
	private int fromPlayer;
	private int toPlayer;
	private int type;
	private long message;
	private int retryAttempts;
	private String stringMessage;
	private final Array<Integer> sentTonkenIds;

	public static class Type {
		/* maximum of 64 types of messages (6 bits) */
		public static final int HANDSHAKE = 0;
		public static final int DECLARE = 1;
		public static final int CARD = 2;
		public static final int SEED = 3;
		public static final int DROPPED_MESSAGE_REQUEST = 4;
		public static final int CHAT = 5;

		public static boolean hasStringMessage(int type) {
			return type == HANDSHAKE || type == CHAT;
		}
		public static String toString(int type) {
			switch (type) {
				case  HANDSHAKE: return "Handshake";
				case DECLARE: return "Declare";
				case CARD: return "Card";
				case SEED: return "Seed";
				case DROPPED_MESSAGE_REQUEST: return "Dropped message request";
				case CHAT: return "Chat";
				default: return "UNKNOWN (" + type + ")";
			}
		}
	}

	private static final Pool<MultiplayerMessage> pool = new Pool<MultiplayerMessage>(4) {
		@Override
		protected MultiplayerMessage newObject() {
			return new MultiplayerMessage();
		}
	};

	private MultiplayerMessage(){
		sentTonkenIds = new Array<>();
		reset();
	}

	/** Remember to call MultiplayerMessage.done() to put the event back to the pool */
	public static MultiplayerMessage build(int order, int fromPlayer, int type, long message) {
		return pool.obtain().set(order, fromPlayer, type, message);
	}

	public static MultiplayerMessage buildChat(int fromPlayer, String message) {
		return pool.obtain().set(SPECIAL_ORDER, fromPlayer, Type.CHAT, message);
	}

	public static MultiplayerMessage buildHandshake(int fromPlayer, String message) {
		return pool.obtain().set(SPECIAL_ORDER, fromPlayer, Type.HANDSHAKE, message);
	}

	public static MultiplayerMessage buildDroppedMessageRequest(int fromPlayer, long order) {
		return pool.obtain().set(SPECIAL_ORDER, fromPlayer, MultiplayerMessage.Type.DROPPED_MESSAGE_REQUEST, order);
	}

	public static MultiplayerMessage fromByte(byte[] message, int fromPlayer) {
		MultiplayerMessage m = pool.obtain();

		ByteBuffer buffer = ByteBuffer.wrap(message);

		m.order = buffer.getShort();
		if (m.order == 0) {
			m.message = 0;
			m.message = buffer.getLong();
			m.type = Type.SEED;
			log.i().append("From ").append(m.fromPlayer).append(": [").append(m.order).append("] ").append(Type.toString(m.type)).append(": ").append(m.message).print();
		} else {
			byte playerAndType = buffer.get();
//			log.w().append("Decoding player and type: ").append(String.format("%8s", Integer.toBinaryString(playerAndType & 0xFF)).replace(' ', '0')).print();
			m.fromPlayer = playerAndType & 0x03;
			m.type = (playerAndType >> 2) & 0x3F;
			log.i().append("From ").append(m.fromPlayer).append(": [").append(m.order).append("] ").append(Type.toString(m.type)).append(": ").print();
			if (Type.hasStringMessage(m.type)) {
				short stringLength = buffer.getShort();
				byte[] stringBytes = new byte[stringLength];
				buffer.get(stringBytes);
				m.stringMessage = new String( stringBytes, Charset.forName("UTF-8"));
				log.i().append("String: ").append(m.stringMessage).append(" (size: ").append(stringLength).append(")").print();
			} else {
				m.message = buffer.get();
				log.i().append("Message: ").append(m.message).print();
			}
		}
		return m;
	}

	public byte[] toByte() {
		// order 2
		// Order == 0: + 8 (seed)
		// Has String? + 1 (fromPlayer, 2bits + type, 6bits) + 2 (String length) + String length
		// !Has string + 1 (fromPlayer, 2bits + type, 6bits) + 1 (message) + 0 (toPlayer)

		short stringLength = 0;
		byte[] byteString = null;
		int size = 2;

		if (order == 0) size += 8;
		else {
			size += 1;
			if (!Type.hasStringMessage(type)) size += 1;
			else {
				byteString = stringMessage.getBytes(Charset.forName("UTF-8"));
				stringLength = (short)byteString.length;
				size += 2 + stringLength;
			}
		}

		ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.putShort((short)order);
		if (order == 0) {
			buffer.putLong(message);
			log.i().append("From ").append(fromPlayer).append(": [").append(order).append("] ").append(Type.toString(type)).append(": ").append(message).print();
		} else {
			byte b =  (byte) ((fromPlayer & 0x03) | ((type << 2) & 0xFC));
//			log.w().append("Encoded player and type: ").append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0')).print();
			buffer.put(b);
			if (Type.hasStringMessage(type)) {
				log.i().append("From ").append(fromPlayer).append(": [").append(order).append("] ").append(Type.toString(type)).append(": ").append(stringMessage).append(" (length: ").append(stringLength).append(")").print();
				buffer.putShort(stringLength);
				buffer.put(byteString);
			} else {
				buffer.put((byte)message);
				log.i().append("From ").append(fromPlayer).append(": [").append(order).append("] ").append(Type.toString(type)).append(": ").append(message).print();
			}
		}
		return buffer.array();
	}

	private MultiplayerMessage set(int order, int fromPlayer, int type, long message) {
		this.order = order;
		this.fromPlayer = fromPlayer;
		this.type = type;
		this.message = message;
		return this;
	}

	private MultiplayerMessage set(int order, int fromPlayer, int type, String message) {
		if (!Type.hasStringMessage(type)) throw new RuntimeException("Trying to create a Multiplayer message containing a string using an invalid type: (type = " + type + ")");
		this.order = order;
		this.fromPlayer = fromPlayer;
		this.type = type;
		this.stringMessage = message;
		return this;
	}

	public void done(){
		if (stringMessage == null) log.i().append("Freeing up message [").append(order).append("] ").append(Type.toString(type)).append(": ").append(message).print();
		else log.i().append("Freeing up message [").append(order).append("] ").append(Type.toString(type)).append(": ").append(stringMessage).print();
		pool.free(this);
	}

	@Override
	public void reset() {
		order = -1;
		fromPlayer = -1;
		toPlayer = -1;
		type = -1;
		message = -1;
		stringMessage = null;
		retryAttempts = 0;
		sentTonkenIds.clear();
	}

	public long getMessage() {
		return message;
	}
	public String getString() {return stringMessage;}
	public int getOrder(){
		return order;
	}
	public int getFromPlayer() {
		return fromPlayer;
	}
	public int getRecepient(){
		return toPlayer;
	}
	public int getType() {
		return type;
	}
	public int getRetryAttempts() {
		return retryAttempts;
	}

	public boolean isDeclare() {
		return type == Type.DECLARE;
	}
	public boolean isBroadcast(){
		return toPlayer == BROADCAST;
	}
	public boolean isSpecialOrder() {return order == SPECIAL_ORDER;}
	public void incrementRetryAttempts() {
		retryAttempts++;
	}

	public void sentWithTokenId(int tokenId) {
		sentTonkenIds.add(tokenId);
	}

	public boolean hasTokenId(int tokenId) {
		return sentTonkenIds.contains(tokenId, false);
	}
}
