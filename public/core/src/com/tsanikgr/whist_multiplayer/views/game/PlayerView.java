package com.tsanikgr.whist_multiplayer.views.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.tsanikgr.whist_multiplayer.myactors.Card;
import com.tsanikgr.whist_multiplayer.myactors.Geometry;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;
import com.tsanikgr.whist_multiplayer.myactors.MyTextButton;
import com.tsanikgr.whist_multiplayer.views.View;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class PlayerView extends View {

	private static final String CARDS_P = "cards_p";
	private static final String BAUSEN_P = "bausen_p";
	public static final float DEALING_DELAY = 0.05f;
	public static final float DEALING_DURATION = 1.0f;
	private static final String BAUSEN_PROTOTYPE = "pl_p0_bausen";
	private int playerIndex;
	private CardGroup bausen;
	private CardGroup cards;
	private int lastUpdated;
	private MyTextButton nameLabel;
	private MyGroup nameLabelWrap;

	private static volatile MyImage bausenPrototype = null;

	private CardLayoutAdaptor bausenLayoutManager, cardLayoutAdaptor;
	private Geometry.PlaceHolder cardBounds;
	private Geometry.PlaceHolder bausenBounds;

	public PlayerView(String xmlFile, String name) {
		super(xmlFile, name);
	}

	@Override
	public void dispose() {
	}

	@Override
	protected void onAssetsLoaded(View view) {
		initActors();
	}

	private void initActors() {
		lastUpdated = -1;
		if (getName().substring(getName().length()-1).compareTo("0") == 0) playerIndex = 0;
		else if (getName().substring(getName().length()-1).compareTo("1") == 0) playerIndex = 1;
		else if (getName().substring(getName().length()-1).compareTo("2") == 0) playerIndex = 2;
		else if (getName().substring(getName().length()-1).compareTo("3") == 0) playerIndex = 3;

		nameLabelWrap = findActor("label_p" + playerIndex);
		nameLabelWrap.setTouchable(Touchable.disabled);
		nameLabel = (MyTextButton)nameLabelWrap.getChildren().get(0);
		nameLabel.setTouchable(Touchable.disabled);
		nameLabel.setMaxCharacters(10);
		if (playerIndex == 3) {
			nameLabel.padRight(nameLabel.getWidth()/3.7f);
			nameLabel.padLeft(nameLabel.getWidth()/15f);
		} else {
			nameLabel.padLeft(nameLabel.getWidth()/3.7f);
			nameLabel.padRight(nameLabel.getWidth()/15f);
		}
		nameLabel.getLabel().setAlignment(Align.center);
		Geometry.fixOrigin(nameLabelWrap);

		initialiseLayoutManagers();

		cards = new CardGroup(cardLayoutAdaptor);
		cards.setPosition(cardBounds.x+cardBounds.w/2f, cardBounds.y);

		bausen = new CardGroup(bausenLayoutManager);
		bausen.setPosition(bausenBounds.x + bausenBounds.w / 2f, bausenBounds.y);

		addActor(cards);
		addActor(bausen);

		if (playerIndex == 0) {
			bausenPrototype = findActor(BAUSEN_PROTOTYPE);
			bausenPrototype.remove();
		}

		bausen.setZIndex(0);
		cards.setZIndex(0);
		cards.setTouchable(Touchable.childrenOnly);
		bausen.setTouchable(Touchable.disabled);
	}

	public void reset() {
		cards.clearChildren();
		bausen.clearChildren();
	}

	public void throwCard(Card card){
//		log.i("Throwing card " + card, "throwCard");
		cards.removeActor(card);
		cards.layoutCards();
	}

	public void playDealingAnimation(Array<Card> cards) {
		this.cards.clearChildren();
		this.cards.add(cards);
		for (Actor card : this.cards.getChildren()) {
			card.setVisible(true);
			if (playerIndex == 0) {
				if (card instanceof Card) ((Card) card).setDisabled(false);
				card.setTouchable(Touchable.enabled);
			} else {
				card.setTouchable(Touchable.disabled);
			}
		}
	}

	private static final Color backColor = new Color(0f, 0f, 0f, 0f);

	@Override
	public Color getBackgroundColor() {
		return backColor;
	}

	@Override
	public void setBackground(MyImage prototypeBackground) {
	}

	public void setNameLabel(String name){
		nameLabel.setText(name);
//		nameLabel.pack();    //DON'T !!
	}

	public void setActivePlayer(boolean isActive) {
		nameLabel.setDisabled(isActive);
		nameLabelWrap.clearActions();
		if (isActive)
			nameLabelWrap.addAction(Actions.repeat(-1, Actions.sequence(Actions.scaleTo(1.2f, 1.2f, 1f, Interpolation.pow5In), Actions.scaleTo(1f, 1f, 1f, Interpolation.pow5Out), Actions.delay(0.75f))));
		else {
			nameLabelWrap.addAction(Actions.scaleTo(1f, 1f, 0.5f, Interpolation.pow5Out));
		}

		if (playerIndex == 0) {
			for (Actor card : cards.getChildren()) {
				for (Action action : card.getActions()) {
					if (action instanceof RepeatAction) {
						card.removeAction(action);
						card.setRotation(0f);
					}
				}
			}
		}
	}

	public void nudgeCards(){
		int sign = 1;
		for (Actor card : cards.getChildren()) {
			card.addAction(Actions.repeat(1, Actions.sequence(
					Actions.rotateTo(-3f * sign, 0.075f, Interpolation.fade),
					Actions.repeat(2, Actions.sequence(
							Actions.rotateTo(3f * sign, 0.075f, Interpolation.fade),
							Actions.rotateTo(-3f * sign, 0.075f, Interpolation.fade))),
					Actions.rotateTo(0f, 0.075f, Interpolation.fade)
			)));

			sign *= -1;
		}
	}

	public void updateBausen(int declared, int complete, boolean reset) {

		int n = Math.max(declared, complete);

		((BausenCardLayoutAdaptor)bausenLayoutManager).set(n);
		if (bausen.size() != n) {
			((BausenCardLayoutAdaptor)bausenLayoutManager).reset(reset);
			bausen.layoutCards();
		}

		animateWonBausen(declared, complete, reset);
	}

	private void animateWonBausen(int declared, int complete, boolean reset) {
		if (reset) lastUpdated = -1;

		Actor actor;
		for (int b = 0 ; b < bausen.size() ; b++) {
			actor = bausen.getChildren().get(b);
			if (b >= complete) {
//				actor.addAction(sequence(delay(1f),alpha(0.25f)));
				actor.addAction(sequence(delay(1f), Actions.color(BAUSEN_COLOUR_INCOMPLETE)));
			} else {
//				actor.addAction(sequence(delay(1f),alpha(1.0f)));
//				actor.addAction(sequence(delay(1f), Actions.color(BAUSEN_COLOUR_ACHIEVED)));
			}
			if (b == lastUpdated && b < declared) {
				actor.addAction(sequence(delay(1f), Actions.moveBy(0f, 30f)));
				actor.addAction(sequence(delay(1f), Actions.moveBy(0f, -30f, 1.8f, Interpolation.bounceOut)));
				actor.addAction(sequence(delay(1f), Actions.color(BAUSEN_COLOUR_ACHIEVED)));

//				actor.addAction(sequence(delay(1.5f), Actions.scaleTo(1.5f, 1.5f, 0.75f, Interpolation.sineIn)));
//				actor.addAction(sequence(delay(2.25f), Actions.scaleTo(1f, 1f, 0.75f, Interpolation.sineOut)));
			}
		}
		lastUpdated++;
	}

	private void initialiseLayoutManagers() {
		cardBounds = new Geometry.PlaceHolder();
		cardBounds.fromActor(findActor(CARDS_P +playerIndex));
		bausenBounds = new Geometry.PlaceHolder();
		bausenBounds.fromActor(findActor(BAUSEN_P + playerIndex));
		findActor(CARDS_P+ playerIndex).remove();
		findActor(BAUSEN_P + playerIndex).remove();

		cardLayoutAdaptor = new PlayerCardLayoutAdaptor();
		bausenLayoutManager = new BausenCardLayoutAdaptor();
	}

	private static final Color BAUSEN_COLOUR_INCOMPLETE = new Color(0.5f,0.5f,0.5f,1f);
	private static final Color BAUSEN_COLOUR_ACHIEVED = new Color(1f,1f,1f,1f);
	private static final Color BAUSEN_COLOUR_WRONG = new Color(0.8f, 0.4f, 0.4f, 1f);

	public void animateValidCards(Array<Card> validCards) {
		for (Actor card : validCards) card.addAction(Actions.sequence(Actions.color(Color.LIME, 0.4f, Interpolation.circleOut), Actions.color(Color.WHITE, 0.25f, Interpolation.circleOut)));
	}

	private class BausenCardLayoutAdaptor implements CardLayoutAdaptor {

		private boolean reset = false;
		int total;
		Actor i;

		@Override
		public void layoutCards() {
			float overlap = 4f/7f;
			float dx = bausenPrototype.getWidth()*(1-overlap);
			float offset = -(dx *(float)(total-1) + bausenPrototype.getWidth()) / 2f;

			if (reset) bausen.clearChildren();
			for (int b = 0; b < total; b++) {
				if (b < bausen.size()) i = bausen.getChildren().get(b);
				else {
					i = MyImage.fromPrototype(bausenPrototype);
					bausen.addActor(i);
					i.setPosition(0f,0f);
				}

				if (reset) {
					i.setColor(BAUSEN_COLOUR_INCOMPLETE);
					i.setPosition(0f, 0f);
					i.addAction(Actions.moveBy(offset, 0f, 1.8f, Interpolation.elasticOut));
					offset += dx;
				} else {
					if (b == total - 1) {
						i.setVisible(false);
						i.setPosition(offset + b*dx, 30f);
						i.addAction(sequence(delay(1f), Actions.color(BAUSEN_COLOUR_WRONG)));
						i.addAction(sequence(delay(1f), Actions.visible(true)));
						i.addAction(Actions.sequence(Actions.delay(1f), Actions.moveBy(0f, -30f, 1.8f, Interpolation.bounceOut)));
					} else {
						i.addAction(Actions.sequence(delay(1f), Actions.moveBy(-dx/2.0f, 0f, 1.8f, Interpolation.exp10Out)));
					}
				}
			}
			reset = false;
		}

		private void set(int total){
			this.total = total;
		}

		private void reset(boolean reset){
			this.reset = reset;
		}
	}

	public void setDealing(boolean dealing) {
		((PlayerCardLayoutAdaptor) cardLayoutAdaptor).setDealing(dealing, -1f, -1f);
	}

	public void setDealing(boolean dealing, float fromX, float fromY) {
		((PlayerCardLayoutAdaptor) cardLayoutAdaptor).setDealing(dealing, fromX, fromY);
	}

	private class PlayerCardLayoutAdaptor implements CardLayoutAdaptor {

		public static final float CARD_SPEED = 0.001f;
		boolean dealing = false;
		private float fromX;
		private float fromY;

		public void setDealing(boolean dealing, float fromX, float fromY) {
			this.dealing = dealing;
			if (Math.abs(fromX + 1) > 0.001) this.fromX = fromX;
			if (Math.abs(fromY + 1) > 0.001) this.fromY = fromY;
		}
		public static final float MIN_OVERLAP = 0.75f;
		@Override
		public void layoutCards() {
			int n = cards.size();
			if (n == 0) return;
			float cw = cards.getCard(0).getWidth();

			float overlap = MIN_OVERLAP;
			float w = cw + (n-1) * cw * overlap;
			if (w > cardBounds.w) {
				overlap = (cardBounds.w-cw)/(n-1)/cw;
				w = cw + (n-1) * cw * overlap;
			}

			Card card;

			Vector2 stageCoords = new Vector2(fromX, fromY);
			Vector2 localCoords = cards.stageToLocalCoordinates(stageCoords);
			float d;
			int i;
			for (int j = 0 ; j < cards.size() ; j++) {
				i = playerIndex != 1 ? j : cards.size() - 1 - j;
				card = cards.getCard(j);
				if (!dealing) {
					if (card.isDragging() && !card.isFlinged()) continue;
					d = CARD_SPEED * (float) (Math.pow(card.getX() - cw * (float) i * overlap - w / 2f, 2) + Math.pow(card.getY(), 2));
					card.addAction(Actions.moveTo(cw * (float) i * overlap - w / 2f, 0f, 0.5f, Interpolation.fade));
				} else {
					card.setPosition(localCoords.x-card.getWidth()/2f,localCoords.y-card.getHeight()/2f);
					card.addAction(Actions.sequence(Actions.delay(DEALING_DELAY*(1f+(float)playerIndex + i)), Actions.moveTo(cw * (float) i * overlap - w / 2f, 0f, DEALING_DURATION, Interpolation.fade)));
				}
			}
		}
	}
}
