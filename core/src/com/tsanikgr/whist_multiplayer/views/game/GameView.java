package com.tsanikgr.whist_multiplayer.views.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.tsanikgr.whist_multiplayer.myactors.Card;
import com.tsanikgr.whist_multiplayer.myactors.Geometry;
import com.tsanikgr.whist_multiplayer.views.View;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class GameView extends View {

	private static final int WIN_DY = 110;
	private static final int WIN_DX = 290;

	public static final float FLING_DURATION = 0.5f;
	private static final String DECK_PACK = "deck_pack";
	private static final String DECK_ATTOU = "deck_attou";
	private static final String MIDDLE_CARDS = "middle_cards_card";
	public static final String CHAT_BUTTON = "chat";

	private Geometry.PlaceHolder attouPlaceholder;
	private Geometry.PlaceHolder deckPlacehoder;
	private Geometry.PlaceHolder[] middleCardsPlaceholder;
	private Card attou;
	private Card deck;
	private CardGroup middleCards;
	private int firstPlayerOffset;
	private int pid;
	private final Vector2 tmpPos = new Vector2();

	public GameView(String xmlFile, String name) {
		super(xmlFile, name);
		pid = 0;
	}

	@Override
	public void dispose() {
	}

	@Override
	protected void onAssetsLoaded(View view) {
		initActors();
	}

	private void initActors() {
		attouPlaceholder = new Geometry.PlaceHolder(findActor(DECK_ATTOU));
		deckPlacehoder = new Geometry.PlaceHolder(findActor(DECK_PACK));
		attou = null;
		deck = null;
		middleCardsPlaceholder = new Geometry.PlaceHolder[4];
		for (int i = 0 ; i < 4 ; i++)
			middleCardsPlaceholder[i] = new Geometry.PlaceHolder(findActor(MIDDLE_CARDS + i));

		firstPlayerOffset = 0;
		middleCards = new CardGroup(new CardLayoutAdaptor() {
			@Override
			public void layoutCards() {
				GameView.this.layoutCards();
			}
		});
		addActor(middleCards);
		removePlaceholders();
	}

	void setMiddleCardsOrigin(float x, float y){
		middleCards.setOrigin(x, y);
	}

	public void setDeck(Card deck) {
		if (this.deck != null) deck.remove();
		this.deck = deck;
		deckPlacehoder.applyTo(this.deck);
		addActor(this.deck);
	}

	private void removePlaceholders() {
		findActor(DECK_ATTOU).remove();
		findActor(DECK_PACK).remove();
		for (int i = 0 ; i < 4 ; i++) findActor(MIDDLE_CARDS + i).remove();
	}

	private static final Color backColor = new Color(27f/255f, 94f/255f, 32f/255f, 1f);

	@Override
	public Color getBackgroundColor() {
		return backColor;
	}

	public void reset(int pid) {
		this.pid = pid;
		reset();
	}

	public void reset() {
		if (deck != null) deck.resetCard();
		if (attou != null) attou.resetCard();
		attou = null;
	}

	public void animateDeck(float positionMultiplier) {
		deck.resetCard();
		deckPlacehoder.applyTo(deck);
		addActor(deck);
		deck.setVisible(true);
		float dy = 100 * positionMultiplier;
		deck.moveBy(0f, dy);
		deck.addAction(Actions.sequence(Actions.moveBy(0f, -dy, 0.5f, Interpolation.pow2Out), Actions.rotateBy(45f, 0.5f, Interpolation.fade)));
	}

	public void setAttou(Card attou) {
		if (this.attou != null) {
			removeActor(this.attou);
			this.attou = null;
		}
		if (attou == null) return;
		this.attou = attou;
		addActor(attou);
		deck.toFront();
		attou.setVisible(true);
		attou.setDisabled(false);
		attou.addAction(Actions.moveBy(attouPlaceholder.x - attou.getX(), 0f, 0.8f, Interpolation.pow2Out));
		attou.addAction(Actions.moveBy(0f, attouPlaceholder.y - attou.getY(), 0.8f, Interpolation.pow2In));
		attou.addAction(Actions.rotateBy(90f, 0.8f, Interpolation.fade));
		attou.addAction(Actions.sizeTo(Card.getCardWidth(), Card.getCardHeight(), 0.8f, Interpolation.pow2Out));
	}

	private void layoutCards() {

		if (middleCards.size() == 0) return;

		int index = (middleCards.size()-1 + firstPlayerOffset - pid + 4)%4;
		Card card = middleCards.getCard(middleCards.size() - 1);

		float dx = middleCardsPlaceholder[index].x - card.getX();
		float dy = middleCardsPlaceholder[index].y - card.getY();

		card.addAction(Actions.moveBy(dx, dy, FLING_DURATION, Interpolation.fade));

		if (index == 0) card.addAction(Actions.rotateBy(180f, FLING_DURATION, Interpolation.fade));
		else if (index == 1) card.addAction(Actions.rotateBy(90f, FLING_DURATION, Interpolation.fade));
//		else if (index == 2) card.addAction(Actions.rotateBy(90f, FLING_DURATION, Interpolation.fade));
		else if (index == 3) card.addAction(Actions.rotateBy(-90f, FLING_DURATION, Interpolation.fade));
	}

	public void setFirstPlayerOffset(int firstPlayerOffset) {
		this.firstPlayerOffset = firstPlayerOffset;
	}

	public CardGroup getMiddleCards() {
		return middleCards;
	}

	public void addMiddleCard(Card card) {
		card.resetCard();
		card.setVisible(true);
		card.setDisabled(false);
		card.clearActions();
		middleCards.addActor(card);
		middleCards.layoutCards();
	}

	void resetMiddleCards(){
		middleCards.clear();
		middleCards.setScale(1.0f);
		middleCards.setPosition(0f, 0f);
		middleCards.getColor().a = 1.0f;
	}
	public void playerThrewCard(int player, Card card) {
		if (middleCards.size() >= 4) resetMiddleCards();
		tmpPos.set(0f, 0f);
		tmpPos.set(card.localToStageCoordinates(tmpPos));
		card.setTouchable(Touchable.disabled);
		card.setDisabled(false);
		middleCards.addActor(card);
		card.setPosition(tmpPos.x, tmpPos.y);
		card.clearActions();
		middleCards.layoutCards();
	}

	public void playWinAnimation(int winner, float positionMultiplier) {
		winner = (winner - pid + 4)%4;

		int dirx, diry;
		switch(winner){
			case 0:
				dirx = 0;
				diry = -WIN_DY;
				break;
			case 1:
				dirx = -WIN_DX;
				diry = 0;
				break;
			case 2:
				dirx = 0;
				diry = (int)(WIN_DY*1.3f);
				break;
			case 3:
				dirx = WIN_DX;
				diry = 0;
				break;
			default:
				dirx = 0;
				diry = 0;
				break;
		}

		dirx *= positionMultiplier;
		diry *= positionMultiplier;

		middleCards.addAction(sequence(delay(0.5f), Actions.alpha(0.5f, 2f, Interpolation.fade)));
		middleCards.addAction(sequence(delay(0.5f), Actions.moveBy(dirx, diry, 2f, Interpolation.fade)));
		middleCards.addAction(sequence(delay(0.5f), Actions.scaleTo(0.5f, 0.5f, 2f, Interpolation.fade)));
	}

	public void showChatButton() {
		findActor(CHAT_BUTTON).setVisible(true);
	}

	public void hideChatButton(){
		findActor(CHAT_BUTTON).setVisible(false);
	}
}
