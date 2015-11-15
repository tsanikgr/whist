package com.tsanikgr.whist_multiplayer.models;

public class BoardModel {

	private final int[] cards;
	private int firstPlayerOffset;

	public BoardModel(){
		cards = new int[4];
		for (int i = 0; i < 4; i++) cards[i] = -1;
		firstPlayerOffset = 0;
	}
	public int getCard(int card) {
		return cards[card];
	}

	public int getFirstPlayerOffset() {
		return firstPlayerOffset;
	}
	public void setFirstPlayerOffset(int firstPlayerOffset) {
		this.firstPlayerOffset = firstPlayerOffset;
	}
	public void reset() {
		for (int i = 0; i < 4; i++) cards[i] = -1;
		firstPlayerOffset = 0;
	}

	public void copy(BoardModel board) {
		System.arraycopy(board.cards, 0, cards, 0, 4);
		firstPlayerOffset = board.firstPlayerOffset;
	}

	public void setCard(int player, int card) {
		cards[player] = card;
	}
}
