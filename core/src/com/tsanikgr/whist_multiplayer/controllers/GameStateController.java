package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.tsanikgr.whist_multiplayer.models.GameState;
import com.tsanikgr.whist_multiplayer.myactors.Card;

import java.util.Random;

public class GameStateController extends WhistGameController implements IGameStateController{

	private final GameState state;

	public GameStateController(GameState state) {
		this.state = state;
	}

	@Override
	protected void init() {
	}

	@Override
	protected void disposeController() {

	}

	public void newGame(){
		newGame(new Random().nextInt(4));
	}

	public void newGame(int firstPlayer) {
		state.reset();
		state.setGameFirstPlayer(firstPlayer);
		state.setRoundFirstPlayer(previousPlayer(state.getGameFirstPlayer()));
	}

	public boolean newRound() {
		int round = state.nextRound();
		log.i().append("________________________________________________________________________________________").print();
		log.i().append("Dealing round: ").append(state.getRound()).print();
		if (round > (state.isPlay13s() ? 14 : 13)) {
			return false;
		}
		getDealer().shuffle();
		state.setCardsLeft(Math.min(round, 13));
		state.setRoundFirstPlayer(nextPlayer(state.getRoundFirstPlayer()));
		state.setTrickFirstPlayer(state.getRoundFirstPlayer());
		state.setCurrentPlayer(state.getTrickFirstPlayer());
		state.setTotalBausen(0);
		state.setDeclaringBausen(true);
		if (state.getRound() < 13) {
			Array<Integer> cards = Pools.obtain(Array.class);
			getDealer().deal(1, cards);
			setAttou(cards);
			cards.clear();
			Pools.free(cards);
		} else if (state.getRound() == 13) setAttou(getDealer().getNewAttou());
		else setAttou(-1);

		return true;
	}

	public void newTrick(int firstPlayer){
		state.setTrickFirstPlayer(firstPlayer);
		state.setFollowSuit(Card.Suit.NONE);
		state.setCurrentPlayer(state.getTrickFirstPlayer());
	}

	public void incrementTotalBausen(int declaration) {
		state.addToTotalBausen(declaration);
	}

	public void onLastPlayerDeclared() {
		state.setDeclaringBausen(false);
	}

	public boolean isTrickLastPlayer(){
		return state.getCurrentPlayer() == (state.getTrickFirstPlayer()+3)%4;
	}

	private int previousPlayer(int player) {
		return (player + 4 - 1)%4;
	}

	public void incrementCurrentPlayer() {
		state.setCurrentPlayer(nextPlayer(state.getCurrentPlayer()));
	}

	private int nextPlayer(int currentPlayer) {
		return (currentPlayer+1)%4;
	}

	void setAttou(int attou){
		state.setAttou(attou);
		setAttouSuit();
	}

	private void setAttou(Array<Integer> attou) {
		if (attou != null) state.setAttou(attou.get(0));
		else state.setAttou(-1);
		setAttouSuit();
	}

	private void setAttouSuit() {
		if (state.getAttou() == -1) Card.setAttouSuit(Card.Suit.NONE);
		else Card.setAttouSuit(Card.getSuit(state.getAttou()));
	}

	public int getRestriction(){
		int restriction = isTrickLastPlayer() ? state.getRound() - state.getTotalBausen() : -1;
		if (state.getRound() == 14) restriction--;

		return Math.max(restriction,-1);
	}

	public void setFollowSuit(int firstSuit) {
		state.setFollowSuit(firstSuit);
	}
}
