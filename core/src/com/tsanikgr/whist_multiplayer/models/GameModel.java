package com.tsanikgr.whist_multiplayer.models;

import com.badlogic.gdx.utils.Pool;

public class GameModel extends JsonSerialisable<GameModel> implements Pool.Poolable{

	private final PlayerModel[] players;
	private final GameState state;
	private final Deck deck;
	private final BoardModel board;
	private final WhistRoomConfig roomConfig;

	private int order;

	public GameModel(){
		super(GameModel.class);
		state = new GameState();
		deck = new Deck();
		players = new PlayerModel[4];
		board = new BoardModel();
		roomConfig = new WhistRoomConfig();

		for (int i = 0 ; i < 4 ; i++) players[i] = new PlayerModel(i);
		order = 0;
	}

	public void setGameParams(WhistRoomConfig roomConfig, int startingRoundIfChecked) {
		this.roomConfig.set(roomConfig);
		if (this.roomConfig.skipRounds) state.setRound(startingRoundIfChecked);
		else state.setRound(0);
		state.setPlay13s(roomConfig.has13s);
	}

	public int getOrder(){
		return order;
	}

	public void incrementOrder(){
		order++;
	}

	public void resetOrder(){
		order = 0;
	}

	public PlayerModel getPlayer(int i) {
		return players[i];
	}

	public GameState getState() {
		return state;
	}

	public Deck getDeck() {
		return deck;
	}

	public BoardModel getBoard() { return board; }

	public GameModel copy(GameModel prototype, boolean ignoreDeck) {
		for (int i = 0 ; i < 4 ;i++) players[i].copy(prototype.players[i]);
		state.copy(prototype.state);
		if (!ignoreDeck) deck.copy(prototype.deck);
		board.copy(prototype.board);
		return this;
	}

	@Override
	public void reset() {
		//do nothing to save time
		order = 0;
	}

	public WhistRoomConfig getRoomConfig() {
		return roomConfig;
	}

	public int getFinalScore(int player) {
		return getPlayer(player).getScore(state.isPlay13s() ? 14 : 13);
	}
	public boolean isGameCompleted() {
		return state.getRound() == (state.isPlay13s() ? 15 : 14);
	}
	public PlayerModel[] getPlayers() {
		return players;
	}

	/** ********************************************************************************************/
	public static class WhistRoomConfig {
		public int bet = -1;
		public int difficulty = -1;
		public boolean has13s = true;
		public boolean skipRounds = false;
		private String inviterName = null;
		private String invitationId;
		private byte[] inviterImage;

		public WhistRoomConfig(){}

		public WhistRoomConfig(int bet, int difficulty, boolean skipRounds, boolean has13s) {
			this.bet = bet;
			this.difficulty = difficulty;
			this.skipRounds = skipRounds;
			this.has13s = has13s;
		}

		public void set(WhistRoomConfig roomConfig) {
			this.bet = roomConfig.bet;
			this.difficulty = betToDifficulty(roomConfig.bet);
			this.skipRounds = roomConfig.skipRounds;
			this.has13s = roomConfig.has13s;
			this.invitationId = roomConfig.invitationId;
			this.inviterName = roomConfig.inviterName;
			this.inviterImage = roomConfig.inviterImage;
		}

		public static WhistRoomConfig fromVariant(int variant) {
			WhistRoomConfig rc = new WhistRoomConfig();
			if ((variant & 0x40000000) != 0) {
				rc.has13s = true;
				variant -= 0x40000000;
			}
			if ((variant & 0x20000000) != 0) {
				rc.skipRounds = true;
				variant -= 0x20000000;
			}
			rc.bet = variant;
			return rc;
		}

		public int toVariant() {
			int variant;
			variant = bet;
			variant |= (has13s ? 0x40000000 : 0x00000000);
			variant |= (skipRounds ? 0x20000000 : 0x00000000);
			return variant;
		}

		private int betToDifficulty(int bet) {
			//TODO assign different AI difficulties for different bets
			switch(bet) {
				default:
					return 4;
			}
		}

		public void setInviterName(String inviterName) {
			this.inviterName = inviterName;
		}
		public void setInvitationId(String invitationId) { this.invitationId = invitationId; }
		public String getInviterName() {
			return inviterName;
		}

		public String getInvitationId() {
			return invitationId;
		}
		public byte[] getInviterImage() {
			return inviterImage;
		}
		public void setInviterImage(byte[] inviterImage) {
			this.inviterImage = inviterImage;
		}
	}
}
