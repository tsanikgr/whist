package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.utils.Array;
import com.tsanikgr.whist_multiplayer.models.Deck;

import java.lang.RuntimeException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class Dealer extends WhistGameController implements IDealer{
	private final Deck deck;
	private Random random;

	public Dealer(Deck deck) {
		this.deck = deck;
		setNewSeed();
	}

	@Override
	protected void init() {
	}

	@Override
	protected void disposeController() {
	}

	public void setNewSeed() {
		setNewSeed(seedUniquifier() ^ System.nanoTime());
	}

	private void setNewSeed(long seed) {
		deck.setSeed(seed);
		random = new Random(seed);
	}

	public long getSeed(){
		return deck.getSeed();
	}

	public void shuffle(long seed){
		setNewSeed(seed);
		shuffle();
	}

	@Override
	public void shuffle() {
		deck.reset();
		getCardController().resetCards();
		Array<Integer> cards = deck.getCardsInDeck();

		/** Hidden shuffling implementation **/
		/** All you have to do is shuffle the integers in the cards array */
		throw new RuntimeException("You have to implement you own deck shuffling.");
	}

	@Override
	public synchronized void deal(int nCards, Array<Integer> cardsToDeal){
		if (nCards > deck.getCardsInDeck().size) {
			log.e().append("Attempting to deal more cards than in deck").print();
			return;
		}
		cardsToDeal.clear();
		for (int i = 0 ; i < nCards ; i++) cardsToDeal.add(deck.getLastCard());
	}

	private static final AtomicLong seedUniquifier = new AtomicLong(8682522807148012L);
	private static long seedUniquifier() {
		for (;;) {
			long current = seedUniquifier.get();
			long next = current * 181783497276652981L;
			if (seedUniquifier.compareAndSet(current, next))
				return next;
		}
	}

	@Override
	public int getNewAttou() {
		return random.nextInt(52);
	}
	public int getFirstPlayer() {
		return random.nextInt(4);
	}
}
