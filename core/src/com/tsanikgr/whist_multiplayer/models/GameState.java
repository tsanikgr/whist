package com.tsanikgr.whist_multiplayer.models;

import com.tsanikgr.whist_multiplayer.myactors.Card;

public class GameState {

	private boolean declaringBausen;
	private int totalBausen;

	private int round, cardsLeft;
	private int currentPlayer, gameFirstPlayer, roundFirstPlayer, trickFirstPlayer;
	private int followSuit;
	private int attou;
	private boolean play13s, playWithRestriction;

	public GameState(){
		reset();
	}

	public void reset() {
		declaringBausen = false;
		totalBausen = 0;
		round = 0;
		cardsLeft = 0;
		currentPlayer = 0;
		roundFirstPlayer = 0;
		trickFirstPlayer = 0;
		gameFirstPlayer = 0;
		followSuit = Card.Suit.NONE;
		attou = -1;
		play13s = true;
		playWithRestriction = true;
	}

	public boolean isDeclaringBausen() {
		return declaringBausen;
	}
	public void setDeclaringBausen(boolean declaringBausen) {
		this.declaringBausen = declaringBausen;
	}
	public int getTotalBausen() {
		return totalBausen;
	}
	public void setTotalBausen(int totalBausen) {
		this.totalBausen = totalBausen;
	}
	public void addToTotalBausen(int declaration) {
		totalBausen += declaration;
	}
	public int getRound() {
		return round;
	}
	public void setRound(int round) {
		this.round = round;
	}
	public int nextRound() {
		this.round++;
		return round;
	}
	public int getCardsLeft() {
		return cardsLeft;
	}
	public void setCardsLeft(int cardsLeft) {
		this.cardsLeft = cardsLeft;
	}
	public void decrementCardsLeft() {
		cardsLeft--;
	}
//	public int getMiddleCards() {
//		return middleCards;
//	}
//	public void setMiddleCards(int middleCards) {
//		this.middleCards = middleCards;
//	}
//	public void incrementMiddleCards() {middleCards++;}
	public int getCurrentPlayer() {
		return currentPlayer;
	}
	public void setCurrentPlayer(int currentPlayer) {
		this.currentPlayer = currentPlayer;
	}
	public int getRoundFirstPlayer() {
		return roundFirstPlayer;
	}
	public void setRoundFirstPlayer(int roundFirstPlayer) {
		this.roundFirstPlayer = roundFirstPlayer;
	}
	public int getFollowSuit() {
		return followSuit;
	}
	public void setFollowSuit(int followSuit) {
		this.followSuit = followSuit;
	}
	public int getAttou() { return attou;}
	public void setAttou(int attou) {
		this.attou = attou;
	}
	public int getGameFirstPlayer() {
		return gameFirstPlayer;
	}
	public void setGameFirstPlayer(int gameFirstPlayer) {
		this.gameFirstPlayer = gameFirstPlayer;
	}
	public int getTrickFirstPlayer() {
		return trickFirstPlayer;
	}
	public void setTrickFirstPlayer(int trickFirstPlayer) {
		this.trickFirstPlayer = trickFirstPlayer;
	}
	public boolean isPlay13s() {
		return play13s;
	}
	public void setPlay13s(boolean play13s) {
		this.play13s = play13s;
	}
	public boolean isPlayWithRestriction() {
		return playWithRestriction;
	}
	public void setPlayWithRestriction(boolean playWithRestriction) {
		this.playWithRestriction = playWithRestriction;
	}

	public void copy(GameState state) {
		declaringBausen = state.declaringBausen;
		totalBausen = state.totalBausen;
		round = state.round;
		cardsLeft = state.cardsLeft;
		currentPlayer = state.currentPlayer;
		roundFirstPlayer = state.roundFirstPlayer;
		trickFirstPlayer = state.trickFirstPlayer;
		gameFirstPlayer = state.gameFirstPlayer;
		followSuit = state.followSuit;
		attou = state.attou;
		play13s = state.play13s;
		playWithRestriction = state.playWithRestriction;
	}
}
