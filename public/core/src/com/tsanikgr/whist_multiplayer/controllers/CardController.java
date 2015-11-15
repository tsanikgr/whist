package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.tsanikgr.whist_multiplayer.ICardController;
import com.tsanikgr.whist_multiplayer.IStageBuilderListener;
import com.tsanikgr.whist_multiplayer.myactors.Card;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.views.View;
import com.tsanikgr.whist_multiplayer.views.game.CardLayoutAdaptor;
import com.tsanikgr.whist_multiplayer.views.game.GameScreen;
import com.tsanikgr.whist_multiplayer.views.game.GameView;

import java.util.LinkedHashMap;

public class CardController extends Controller implements ICardController{

	private static final float FLING_COEF = 0.25f;
	private static final float TAP_OFFSET = 50f;
	private Vector2 tmpPos = new Vector2();

	private final LinkedHashMap<Integer, Card> cards = new LinkedHashMap<>(52);
	private boolean buildingCards = false;
	private boolean ready = false;

	public CardController() {
	}

	@Override
	protected void init() {
	}

	@Override
	protected void disposeController() {
		cards.clear();
		buildingCards = false;
	}

	@Override
	public void loadAsync(){
		createCards(true);
	}

	private synchronized void createCards(boolean async){
		if (ready || buildingCards) return;
		buildingCards = true;

		final View cards = new View(GameScreen.CARDS) {
			@Override
			public void dispose() {}
			@Override
			protected void onAssetsLoaded(View view) {
				fixOriginRecursively(view);
			}
			@Override
			protected Color getBackgroundColor() {return null;}
		};

		if (!async) {
			getScreenDirector().buildGroup(GameScreen.CARDS, GameScreen.CARDS, cards);
			initFromGroup(cards, false);
			buildingCards = false;
			ready = true;
		}
		else getScreenDirector().buildGroupAsync(GameScreen.CARDS, GameScreen.CARDS, cards, new IStageBuilderListener() {
			@Override
			public void onGroupBuildFailed(String fileName, Exception e) {
				log.e(e).append("Building cards failed").print();
				buildingCards = false;
				ready = false;
			}
			@Override
			public void onGroupBuilded(String fileName, MyGroup group) {
				initFromGroup(cards, true);
//				buildingCards = false;
//				ready = true;
			}
		});
	}

	private void initFromGroup(View group, boolean async) {
		ActorGestureListener flingListener = new CardFlingListener();
		DragListener dragListener = new CardDragListener();
		Card card;
		SnapshotArray<Actor> cardGroup = ((MyGroup) group.getChildren().get(0)).getChildren();
		for (int i = 51 ; i >= 0 ; i--) {
			card = (Card) cardGroup.get(i);
			cards.put(card.getIndex() + 1, card);
			card.setPressedColor(new Color(0.7f, 0.7f, 1.0f, 1.0f));

			if (i == 51) Card.setCardDimensions((getResolution().getScreenWidth() - card.getWidth()) / 2f,
					(getResolution().getScreenHeight() - card.getHeight()) / 2f,
					card.getWidth(),
					card.getHeight());
			card.resetCard();

			// order matters:
			card.addListener(flingListener);
			card.addListener(dragListener);
		}

		Card deckCard = new Card(52);
		cards.put(53, deckCard);
		deckCard.resetCard();

		setCardColor(getSettings().getCardColor(), async);
	}

	@Override
	public void setCardColor(final Color color, boolean async) {
		buildingCards = true;
		ready = false;
		if (async) new Thread(new SetCardColorRunnable(color, true)).start();
		else {
			if (!getAssets().isRenderThread()) throw new RuntimeException("Cannot build cards in sync from a background thread.");
			new SetCardColorRunnable(color, false).run();
		}
	}

	class SetCardColorRunnable implements Runnable {
		private final Color color;
		private boolean async;

		SetCardColorRunnable(Color color, boolean async) {
			this.color = color;
			this.async = async;
		}

		@Override
		public void run() {
			TextureAtlas.AtlasRegion textureRegion = getAssets().getTextureAtlas("cards.atlas").findRegion("cards_back");
			Texture texture = textureRegion.getTexture();
			int width = textureRegion.getRegionWidth();
			int height = textureRegion.getRegionHeight();

			if (!texture.getTextureData().isPrepared()) texture.getTextureData().prepare();
			final Pixmap regionPixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
			final Pixmap pixmap = texture.getTextureData().consumePixmap();

			int colorInt;
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					colorInt = pixmap.getPixel(textureRegion.getRegionX() + x, textureRegion.getRegionY() + y);
					regionPixmap.drawPixel(x, y, colorInt);
				}
			}

			textureRegion = getAssets().getTextureAtlas("cards.atlas").findRegion("cards_back_Color");
			Color tempColor = new Color(1,0,1,0);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					colorInt = pixmap.getPixel(textureRegion.getRegionX() + x, textureRegion.getRegionY() + y);
					Color.rgba8888ToColor(tempColor, colorInt);
					tempColor.mul(color);
					tempColor.set(tempColor.a, tempColor.b, tempColor.g, tempColor.r);   //convert to ABRG
					regionPixmap.drawPixel(x, y, tempColor.toIntBits());
				}
			}

			textureRegion = getAssets().getTextureAtlas("cards.atlas").findRegion("cards_back_front");
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					colorInt = pixmap.getPixel(textureRegion.getRegionX() + x, textureRegion.getRegionY() + y);
					regionPixmap.drawPixel(x,y,colorInt);
				}
			}

			if (async) Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run() {
						createDrawable(regionPixmap, pixmap);
					}
				});
			else createDrawable(regionPixmap, pixmap);
		}

		private void createDrawable(Pixmap regionPixmap, Pixmap pixmap){
			TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(new PixmapTextureData(regionPixmap, regionPixmap.getFormat(), false, true, true))));
			pixmap.dispose();
			if (cards.get(1).getStyle().disabled != null)
				((TextureRegionDrawable) cards.get(1).getStyle().disabled).getRegion().getTexture().dispose();
			for (int i = 1; i < 54; i++) cards.get(i).getStyle().disabled = drawable;
			buildingCards = false;
			ready = true;
		}
	}

	@Override
	public Card getCard(int index) {
		createCards(false);
		while(!ready) {
			getAssets().getAssetManager().update();
			getOSinterface().executePendingUiThreadRunnables();
		}
		return cards.get(index+1);
	}

	@Override
	public void resetCards(){
		if (cards.isEmpty()) return;
		for (int i = 0; i < cards.size(); i++) if (cards.get(i) != null) cards.get(i).resetCard();
	}

	@Override
	public Card newCardFromPrototype(int index) {
		Card card = new Card(index, cards.get(index).getStyle().up, cards.get(index).getStyle().down);
		card.resetCard();
		return card;
	}

	public class CardDragListener extends DragListener{
		float initDx, initDy;

		@Override
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			Card card;

			if (pointer != 0) return super.touchDown(event, x, y, pointer, button);

			if (event.getListenerActor() instanceof Card) card = (Card)event.getListenerActor();
			else return false;

			card.setDragging(true);
			card.addAction(Actions.moveBy(0f, getResolution().getPositionMultiplier() * TAP_OFFSET, 0.1f, Interpolation.fade));
			initDx = x;
			initDy = y - getResolution().getPositionMultiplier() * TAP_OFFSET;
			return super.touchDown(event, x, y, pointer, button);
		}

		@Override
		public void drag(InputEvent event, float x, float y, int pointer) {
			super.drag(event, x, y, pointer);

			if (pointer != 0) return;

			Card card;
			if (event.getListenerActor() instanceof Card) card = (Card)event.getListenerActor();
			else return;

			card.addAction(Actions.moveTo(card.getX() - initDx + x, card.getY() - initDy + y, 0.04f));
		}

		@Override
		public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
			super.touchUp(event, x, y, pointer, button);

			if (pointer != 0) return;

			Card card;
			if (event.getListenerActor() instanceof Card) card = (Card)event.getListenerActor();
			else return;

			card.setDragging(false);
			if (card.isFlinged()) {
				card.setFlinged(false);
				return;
			}

			boolean success = false;
			tmpPos.set(0f, 0f);
			tmpPos = card.localToStageCoordinates(tmpPos);
			if (tmpPos.y > getResolution().getPositionMultiplier() * 120f &&
					Math.abs(tmpPos.x - getResolution().getScreenWidth()/2f) < getResolution().getScreenWidth()/4)
				if (getGameController().validateGameAction(-1, false, card.getIndex())) success = true;

			if (!success && card.getParent() instanceof CardLayoutAdaptor) ((CardLayoutAdaptor) card.getParent()).layoutCards();
		}
	}

	/*********************************************************************************************/

	private class CardFlingListener extends ActorGestureListener{
		@Override
		public void fling(InputEvent event, float velocityX, float velocityY, int button) {
			super.fling(event, velocityX, velocityY, button);
			Card card;
			if (event.getListenerActor() instanceof Card) card = (Card)event.getListenerActor();
			else return;

			if (velocityY > 30f) {
				card.setFlinged(true);
				card.setDragging(false);
				if (getGameController().validateGameAction(-1, false, card.getIndex())) {
					card.addAction(Actions.moveBy(velocityX * FLING_COEF, velocityY * FLING_COEF, GameView.FLING_DURATION, Interpolation.pow3Out));
					card.addAction(Actions.moveBy(-velocityX * FLING_COEF, -velocityY * FLING_COEF, GameView.FLING_DURATION, Interpolation.fade));
				} else if (card.getParent() instanceof CardLayoutAdaptor) ((CardLayoutAdaptor) card.getParent()).layoutCards();
			}
		}
	}
}
