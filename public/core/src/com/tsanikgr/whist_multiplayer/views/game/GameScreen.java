package com.tsanikgr.whist_multiplayer.views.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.tsanikgr.whist_multiplayer.Config;
import com.tsanikgr.whist_multiplayer.IGameController;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.myactors.Card;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;
import com.tsanikgr.whist_multiplayer.views.Screen;
import com.tsanikgr.whist_multiplayer.views.View;

public class GameScreen extends Screen {
	public static final String CARDS = "cards";
	public static final String GAME_SCREEN = "game";
	public static final String DECLARE_BUTTON = "start";
	public static final String RESIGN_BUTTON = "resign";
	private static final String PLAYER_VIEW = "player";
	private static final String DECLARE_VIEW = "declare_layer";
	private static final String SCORE_VIEW = "score_layer";
	private static final String GAME_VIEW = "game_layer";
	public static final String CHAT_VIEW = "chat_view";
	private static final String BACKGROUND = "background";
	public static final String BACK_BUTTON = "back";

	private int pid;
	private MyImage backgroundImage;

	public GameScreen(IGameController controller, int pid) {
		super(controller, GAME_SCREEN);
		addActor(new GameView(GAME_SCREEN, GAME_VIEW));
		for (int i = 0; i < 4; i++) addActor(new PlayerView(GAME_SCREEN, PLAYER_VIEW + i));
		addActor(new DeclareView(GAME_SCREEN, DECLARE_VIEW));
		findActor(PLAYER_VIEW + 0).toFront();
		addActor(new ScoreView(GAME_SCREEN, SCORE_VIEW));
		addActor(new ChatView(CHAT_VIEW));
		this.pid = pid;
	}

	@Override
	protected void buildViewsProtected() {
		buildViewAsync(GAME_VIEW);
	}

	@Override
	protected void onAssetsLoaded(View view) {
//		fixOriginRecursively(view);
		if (view.getName().compareTo(GAME_VIEW) == 0) {
			whenGameViewReady((GameView) view);
		} else if (view.getName().compareTo(DECLARE_VIEW) == 0) {
			view.setBackground(backgroundImage);
			view.setVisible(false);
		} else if (view.getName().compareTo(SCORE_VIEW) == 0) {
			((ScoreView)view).addScoreDragListener(controller.getResolution().getScreenHeight());
		}

		super.onAssetsLoaded(view);
	}

	private void whenGameViewReady(GameView view) {
		backgroundImage = view.findActor(BACKGROUND);
		backgroundImage.remove();
		view.setTouchable(Touchable.childrenOnly);
//		view.setBackground(backgroundImage);
		view.setMiddleCardsOrigin(controller.getResolution().getScreenWidth() / 2f, controller.getResolution().getScreenHeight() / 2f);

		for (int i = 0; i < 4; i++) buildViewAsync(PLAYER_VIEW + i);
		buildViewAsync(SCORE_VIEW);
		buildViewAsync(DECLARE_VIEW);
//		buildViewAsync(CHAT_VIEW);
	}

	@Override
	protected Color getBackgroundColor() {
		return null;
	}

	public void updateDeclareView(GameModel model, Card attou, int restriction) {
		int player = model.getState().getCurrentPlayer();
		int round = model.getState().getRound();
		declareView().update(player, model, attou, round, restriction);
	}

	public void showDeclareWithAnimation() {
		declareView().setVisible(true);
		declareView().startShowAnimation();
	}

	public void hideDeclareWithAnimation() {
		gameView().setAttou(declareView().getAttou());
		gameView().animateDeck(controller.getResolution().getPositionMultiplier());
		declareView().hideWithAnimation();
	}

	public void pullDownScore() {
		scoreView().pullDown();
	}

	public void setUnderOver(int underOver) {
		scoreView().setUnderOver(underOver);
	}

	public void animateDeck() {
		gameView().animateDeck(controller.getResolution().getPositionMultiplier());
	}

	public void set13sCover(boolean has13s) {
		scoreView().set13sCover(has13s);
	}

	public void resetViews() {
		scoreView().reset(pid);
		gameView().reset(pid);
		declareView().reset(pid);
		declareView().setVisible(false);
	}

	public void setDeck(Card card) {
		gameView().setDeck(card);
	}

	public void setAttou(Card attou) {
		gameView().setAttou(attou);
	}

	public void fillToRound(GameModel gameModel) {
		scoreView().fillToRound(gameModel);
	}

	public void setPlayerName(int player, String name) {
		playerView(pid2View(player)).setNameLabel(name);
		scoreView().setPlayerName(pid2View(player), name);
		declareView().updateName(player, name);
	}

	public void setRound(int round) {
		scoreView().setRound(round);
	}

	public void highLightNotAllowed() {
		declareView().highLightNotAllowed();
	}

	public void updateScore(int round, int player, int declaration, int score, boolean worse) {
		scoreView().updateScore(round, player, declaration, score, worse);
	}

	public void updateScore(GameModel gameModel, int round) {
		scoreView().updateScore(gameModel, round);
	}

	public void resetGameView() {
		gameView().reset();
	}

	public int getDeclaration() {
		return declareView().getDeclaration();
	}

	public boolean isScoreViewPulledDown() {
		return scoreView().isPulledDown();
	}

	public void pullScoreUp() {
		scoreView().pullUp();
	}

	private int pid2View(int pid) {
		return (pid - this.pid + 4) % 4;
	}

	public void setActivePlayer(int currentPlayer) {
		for (int pl = 0; pl < 4; pl++) playerView(pid2View(pl)).setActivePlayer(pl == currentPlayer);
	}

	public void nudgePlayerCards(int player) {
		playerView(pid2View(player)).nudgeCards();
	}

	public void resetPlayerViews() {
		for (int i = 0 ; i < 4; i++) playerView(i).reset();
	}

	public void playDealingAnimation(int player, Array<Card> cards) {
		playerView(pid2View(player)).setDealing(true, 0.5f * controller.getResolution().getScreenWidth(), 0.5f * controller.getResolution().getScreenHeight());
		playerView(pid2View(player)).playDealingAnimation(cards);
		playerView(pid2View(player)).setDealing(false);
	}

	public void updateBausen(int winner, int declaredNo, int achievedNo, boolean reset) {
		playerView(pid2View(winner)).updateBausen(declaredNo, achievedNo, reset);
	}

	public void resetBausen() {
		for (int i = 0 ; i < 4; i++) playerView(i).updateBausen(0, 0, true);
	}

	public void throwCard(int player, Card card) {
		playerView(pid2View(player)).throwCard(card);
	}

	public void animateValidCards(int player, Array<Card> cards) {
		playerView(pid2View(player)).animateValidCards(cards);
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public void setFirstPlayerOffset(int offset) {
		gameView().setFirstPlayerOffset(offset);
	}

	public CardGroup getMiddleCards() {
		return gameView().getMiddleCards();
	}

	private GameView gameView() {
		return getView(GAME_VIEW);
	}

	private PlayerView playerView(int player) {
		return getView(PLAYER_VIEW +player);
	}

	private DeclareView declareView(){
		return getView(DECLARE_VIEW);
	}

	private ScoreView scoreView(){
		return getView(SCORE_VIEW);
	}

	public void addMiddleCard(Card card) {
		gameView().addMiddleCard(card);
	}

	public void resetMiddleCards() {
		gameView().resetMiddleCards();
	}

	public void playerThrewCard(int player, Card card) {
		gameView().playerThrewCard(player, card);
	}

	public void playerWinAnimation(int winner) {
		gameView().playWinAnimation(winner, controller.getResolution().getPositionMultiplier());
	}

	@Override
	protected <T extends View> T getView(String name, Class<T> klass) {
		//make sure game view is ready
		if (name.compareTo(GAME_VIEW) != 0) super.getView(GAME_VIEW);
		return super.getView(name, klass);
	}
	@Override
	protected <T extends View> T getView(String name) {
		//make sure game view is ready
		if (name.compareTo(GAME_VIEW) != 0) super.getView(GAME_VIEW);
		return super.getView(name);
	}

	public void showChatButton() {
		getView(GAME_VIEW, GameView.class).showChatButton();
	}

	public void hideChatButton() {
		getView(GAME_VIEW, GameView.class).hideChatButton();
	}

	public void showChatView(){
		//TODO
		if (Config.RELEASE) return;

		getView(CHAT_VIEW, ChatView.class).showAnimation();
	}

	public void hideChatView(){
		getView(CHAT_VIEW, ChatView.class).hideAnimation();
	}
}
