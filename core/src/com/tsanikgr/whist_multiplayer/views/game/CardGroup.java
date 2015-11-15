package com.tsanikgr.whist_multiplayer.views.game;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Sort;
import com.tsanikgr.whist_multiplayer.myactors.Card;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;

public class CardGroup extends MyGroup implements CardLayoutAdaptor {

	private final CardLayoutAdaptor cardLayoutAdaptor;

	public CardGroup(CardLayoutAdaptor cardLayoutAdaptor) {
		this.cardLayoutAdaptor = cardLayoutAdaptor;
	}

	public void add(Array<Card> cards) {
		for (Card card : cards) addActor(card);
		sortCards();
		if (cardLayoutAdaptor != null) cardLayoutAdaptor.layoutCards();
	}

	private void sortCards() {
		Sort.instance().sort(getChildren());
	}

	public int size(){
		return getChildren().size;
	}

	public Card getCard(int index) {
		return (Card)getChildren().get(index);
	}

	@Override
	public void layoutCards() {
		if (cardLayoutAdaptor != null) cardLayoutAdaptor.layoutCards();
	}
}
