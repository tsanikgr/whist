package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import org.jetbrains.annotations.NotNull;

public class Card extends MyButton implements Comparable<Card>{

	private static float CARD_Y_INIT = 0f, CARD_X_INIT = 0f;
	private static float CARD_W = 0, CARD_H = 0;
	private static int attouSuit = Suit.NONE;

	private final int suit;
	private final int rank;
	private final int index;
	private boolean flinged = false;
	private boolean dragging = false;

	public static class Suit{
		public static final int HEARTS = 0;
		public static final int SPADES = 1;
		public static final int DIAMODS = 2;
		public static final int CLUBS = 3;
		public static final int NONE = 4;
	}

	public Card(int index) {
		this(index, null, null);
	}

	public Card(final int index, Drawable up, Drawable down) {
		super(up, down);
		this.index = index;
		this.rank = getRank(index, false);
		this.suit = getSuit(index);

		setName(getStringName(index));
		setTransform(true);
	}

	public void resetCard() {
		remove();
		clearActions();

		setVisible(false);
		setScale(1.0f);
		setSize(Card.CARD_W, Card.CARD_H);
		setRotation(0f);
		setPosition(CARD_X_INIT, CARD_Y_INIT);
		Geometry.fixOrigin(this);
		setColor(Color.WHITE);
		setTouchable(Touchable.disabled);
		setDisabled(true);
	}

	private String getStringName(int index) {
		String name;
		if (index < 0) name = "card_";
		else if (index < 13) name = "card_h";
		else if (index < 26) name = "card_s";
		else if (index < 39) name = "card_d";
		else if (index < 52) name = "card_c";
		else name = "card_";

		if (index == 0) return name + "A";
		else return name + (index % 13 + 1);
	}

	public int getIndex(){
		return index;
	}

	public int getSuit(){
		return suit;
	}

	private int getRank(boolean Aas14) {
		if (Aas14 && rank == 1) return 14;
		else return rank;
	}

	public boolean isFlinged(){
		return flinged;
	}

	public void setFlinged(boolean flinged) {
		this.flinged = flinged;
	}

	public boolean isDragging() {
		return !flinged && dragging;
	}

	public void setDragging(boolean dragging){
		this.dragging = dragging;
	}

	@Override
	public int compareTo(@NotNull Card card) {
		int mRank = getRank(true);
		int cRank = card.getRank(true);

		int bestSuit = 0;
		if (attouSuit != Suit.NONE) {
			bestSuit = attouSuit;
			if (suit == attouSuit) mRank += 100;
			if (card.suit == attouSuit) cRank += 100;
		}
		int suit_difference = (suit - bestSuit + 4) % 4 - (card.suit - bestSuit + 4) % 4;
		return mRank - cRank + suit_difference*13;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Card) return compareTo((Card)obj) == 0;
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return getIndex();
	}

	/*********************************************************************************************/
	public static int getSuit(int index) {
		if (index < 0) return Suit.NONE;
		if (index < 13) return Suit.HEARTS;
		if (index < 26) return Suit.SPADES;
		if (index < 39) return Suit.DIAMODS;
		if (index < 52) return Suit.CLUBS;
		return Suit.NONE;
	}

	public static int getRank(int index, boolean Aas14){
		int rank = index % 13 + 1;
		if (Aas14 && rank == 1) return 14;
		return rank;
	}

	public static void setAttouSuit(int suit) {
		attouSuit = suit;
	}

	public static void setCardDimensions(float cardXinit, float cardYinit, float cardW, float cardH){
		CARD_X_INIT = cardXinit;
		CARD_Y_INIT = cardYinit;
		CARD_W = cardW;
		CARD_H = cardH;
	}

	public static float getCardWidth(){
		return CARD_W;
	}

	public static float getCardHeight(){
		return CARD_H;
	}
}