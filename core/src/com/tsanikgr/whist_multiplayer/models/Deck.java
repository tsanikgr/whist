package com.tsanikgr.whist_multiplayer.models;

import com.badlogic.gdx.utils.Array;
import com.tsanikgr.whist_multiplayer.util.Log;

public class Deck {

	private final transient Log log = new Log(this);
	private final transient Array<Integer> cardsInDeck;
	private final transient Array<Integer> cardsDealt;

	private long seed;

	public Deck(){
		cardsInDeck = new Array<>();
		cardsDealt = new Array<>();
		seed = -1;
		reset();
	}

	/* Don't reset any of the actual cards here.
	This is called by the constructor during serialisation (on saving)
	We don't want to call card.resetCard() when saving
	 */
	public void reset() {
		cardsInDeck.clear();
		cardsDealt.clear();
		for (int i = 0 ; i < 52 ; i++) cardsInDeck.add(i);
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public Array<Integer> getCardsInDeck() {
		return cardsInDeck;
	}

	public synchronized int getLastCard() {
		if (seed == -1) log.w().append("Not shuffled").print();
		cardsDealt.add(cardsInDeck.peek());
		return cardsInDeck.pop();
	}
	public void copy(Deck deck) {
		seed = deck.seed;
		cardsInDeck.clear();
		cardsDealt.clear();
		cardsInDeck.addAll(deck.cardsInDeck);
		cardsDealt.addAll(deck.cardsDealt);
	}
	public long getSeed() {
		return seed;
	}
}
