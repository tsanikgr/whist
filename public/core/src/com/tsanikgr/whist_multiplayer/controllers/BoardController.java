package com.tsanikgr.whist_multiplayer.controllers;

import com.tsanikgr.whist_multiplayer.models.BoardModel;
import com.tsanikgr.whist_multiplayer.myactors.Card;
import com.tsanikgr.whist_multiplayer.views.game.GameScreen;

public class BoardController extends WhistGameController implements IBoardController{
	private final BoardModel boardModel;
	private final GameScreen screen;
	private int pid;

	public BoardController(BoardModel board, GameScreen screen){
		pid = 0;
		this.screen = screen;
		boardModel = board;
	}

	@Override
	protected void init() {
		screen.resetMiddleCards();
		screen.setFirstPlayerOffset(boardModel.getFirstPlayerOffset());
		Card card;
		for (int i = 0 ; i < 4 ; i++) if (getCard(i) != -1) {
			card = getCardController().getCard(getCard(i));
			screen.addMiddleCard(card);
		}
	}

	@Override
	protected void disposeController() {
	}

	private int getCard(int card){
		return boardModel.getCard(card);
	}

	private void setCard(int player, int card) {
		if (player > 4 || player < 0) throw new RuntimeException("Cannot set card of player " + player);
		boardModel.setCard(player, card);
	}

	public void reset(int pid){
		this.pid = pid;
		reset();
	}

	public void reset() {
		boardModel.reset();
		screen.resetMiddleCards();
	}

	public void setFirstPlayerOffset(int offset){
		boardModel.reset();
		boardModel.setFirstPlayerOffset(offset);
	}

	public void receiveCard(int player, int card) {

		setCard(player, card);
		screen.playerThrewCard(player, getCardController().getCard(card));
	}

	public int getFirstSuit(){
		if (getCard(boardModel.getFirstPlayerOffset()) != -1)
			return Card.getSuit(getCard(boardModel.getFirstPlayerOffset()));
		else
			return Card.Suit.NONE;
	}

	public int getWinner(int attouSuit) throws RuntimeException {
		if (getMiddleCardsNo() != 4) throw new RuntimeException("Can't get winner without 4 cards on the board");
		int winner = boardModel.getFirstPlayerOffset();
		for (int c =  0 ; c < 4 ; c++)
			if (c != winner && compareForWistWinner(getCard(winner),	getCard(c),	attouSuit) < 0) winner = c;
		return winner;
	}

	public void playWinAnimation(int winner) {
		screen.playerWinAnimation(winner);
	}

	public int getMiddleCardsNo() {
		int count = 0;
		for (int i = 0 ; i < 4 ; i++) if (getCard(i) != -1) count++;
		return count;
	}

	private int compareForWistWinner(int reference, int test, int attouSuit) {
		int mRank = Card.getRank(reference,true);
		int cRank = Card.getRank(test, true);
		if (attouSuit != Card.Suit.NONE) {
			if (Card.getSuit(reference) == attouSuit) mRank += 14;
			if (Card.getSuit(test) == attouSuit) cRank += 14;
			else if (Card.getSuit(reference) != Card.getSuit(test) && Card.getSuit(reference) != attouSuit) cRank -= 14;
		} else if (Card.getSuit(reference) != Card.getSuit(test)) cRank -= 14;
		return mRank - cRank;
	}
}