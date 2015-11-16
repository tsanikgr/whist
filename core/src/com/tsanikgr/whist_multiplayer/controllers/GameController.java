package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Timer;
import com.tsanikgr.whist_multiplayer.Config;
import com.tsanikgr.whist_multiplayer.IGameController;
import com.tsanikgr.whist_multiplayer.models.AccomplishmentsModel;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.models.GameState;
import com.tsanikgr.whist_multiplayer.models.PlayerModel;
import com.tsanikgr.whist_multiplayer.myactors.Card;
import com.tsanikgr.whist_multiplayer.views.View;
import com.tsanikgr.whist_multiplayer.views.game.ChatView;
import com.tsanikgr.whist_multiplayer.views.game.GameScreen;
import com.tsanikgr.whist_multiplayer.views.game.GameView;
import com.tsanikgr.whist_multiplayer.views.game.PlayerView;
import com.tsanikgr.whist_multiplayer.views.menu.CommonView;

public class GameController extends ScreenController implements IGameController{

	final GameScreen screen;
	private GameModel gameModel;
	PlayerController playerController;
	GameStateController gameStateController;
	Dealer dealer;
	BoardController board;

	final private GameModel.WhistRoomConfig roomConfig;
	int pid = 0;
	private boolean needsSave;
	private boolean fromLoading;
	private boolean playing;

	public GameController() {
		screen = new GameScreen(this, pid);
		playing = false;
		needsSave = false;
		fromLoading = false;
		roomConfig = new GameModel.WhistRoomConfig();
	}

	@Override
	protected void init() {
		deactivate();
		fromLoading = tryToloadGameModel();
		screen.buildViews();
//		super.init();  //no child controllers yet
	}

	@Override
	protected void onAssetsLoaded(View view) {
		addButtonListenersRecursively(view);
	}

	@Override
	public boolean gameExists() {
		return gameModel() != null;
	}

	@Override
	public void setGameParams(GameModel.WhistRoomConfig roomConfig) {
		this.roomConfig.set(roomConfig);
		needsSave = true;
	}

	void activateLocalMultiCommon(){
		super.activate();
		screen.buildViews();
		screen.clearActions();
		screen.setVisible(true);
		getScreenDirector().addActor(screen);
		playing = true;
		clearEventListeners();
		addListeners();
	}

	@Override
	public void activate() {
		getCardController().getCard(0);  //make sure the CardController is ready
		activateLocalMultiCommon();
		if (gameModel == null) {
			gameModel = new GameModel();
			gameModel.getPlayer(0).setName(getStatisticsController().getStatistics().getName());
			log.i().append("------------- NEW GAME CREATED -------------").print();
			initLocalGame();
		} else if (fromLoading) initLocalGame();

		if (getState().getCardsLeft() == 0 && !newRound()) return;

		screen.hideChatButton();

		requestAction();
	}

	private void initLocalGame() {

		resetViews();
		initCommonControllers();
		addController(playerController = new PlayerController(gameModel.getPlayers(), pid, screen));

		if (!fromLoading) initNewGame();
		else initLocalLoadedGame();

		if (!getState().isDeclaringBausen()) {
			screen.setUnderOver(getState().getTotalBausen() - Math.min(getState().getRound(), 13));
			screen.animateDeck();
		}
		screen.set13sCover(getState().isPlay13s());
	}

	@Override
	public void deactivate() {
		super.deactivate();
		playing = false;
		if (playerController != null) playerController.deactivate();
		clearEventListeners();
	}

	void resetViews(){
		screen.resetViews();
	}

	void initCommonControllers(){
		getCardController().resetCards();
		addController(gameStateController = new GameStateController(gameModel().getState()));
		addController(dealer = new Dealer(gameModel().getDeck()));
		addController(board = new BoardController(gameModel().getBoard(), screen));
		board.init();

		gameStateController.setAttou(gameModel().getState().getAttou());
		screen.setFirstPlayerOffset(gameModel().getBoard().getFirstPlayerOffset());
		screen.setDeck(getCardController().getCard(52));
	}

	private void initLocalLoadedGame(){
		playerController.layoutCards();
		playerController.layoutBausenFromLoading(gameModel());
		if (getState().isDeclaringBausen()) animateDeclareView();
		else screen.setAttou(getAttou());
		screen.fillToRound(gameModel());

		fromLoading = false;
	}

	private void initNewGame(){
		getStatisticsController().placeBet(roomConfig.bet);
		gameStateController.newGame();

		gameModel().setGameParams(roomConfig, Config.STARTING_ROUND_CHECKED);
		screen.set13sCover(roomConfig.has13s);

		playerController.reset();
		board.reset(0);
		for (int i = 0 ; i < 4 ; i++) screen.setPlayerName(i, gameModel().getPlayer(i).getName());
	}

	boolean newRound() {
		if (!gameStateController.newRound()) return false;
		playerController.newRound(getState().getRound());
		screen.setRound(getState().getRound());
		board.reset();
		animateDeclareView();
//		screen.updateDeclareView(gameModel(), getAttou(), gameStateController.getRestriction());
		return true;
	}

	private void newTrick(int firstPlayer) {
		gameStateController.newTrick(firstPlayer);
		board.setFirstPlayerOffset(getState().getCurrentPlayer());
		screen.setFirstPlayerOffset(getState().getCurrentPlayer());
	}

	void requestAction(){
		if (getState().isDeclaringBausen()) screen.updateDeclareView(gameModel(), getAttou(), gameStateController.getRestriction());
		playerController.requestAction(gameModel());
	}

	@Override
	public boolean validateGameAction(int player, boolean isDeclareResult, int result) {
		boolean success;
		if (player == -1) player = pid;
		/** just return if we are not showing the game screen - never requestAction */
		if (!playing && (!Config.PLAY_IN_BACKGROUND && isLocalGame())) return false;
		if (player != getState().getCurrentPlayer()) return false;

		if (isDeclareResult) success = validateDeclaration(player, result);
		else success = validatePlay(player, result);

		/** return if it is not a valid move */
		if (!success) {
			requestAction();
			return false;
		}

		needsSave = true;
		gameModel().incrementOrder();
		if (getState().getCardsLeft() == 0) endOfRound();
		else requestAction();
		return true;
	}

	private boolean validateDeclaration(int player, int declaration) {

		if (!getState().isDeclaringBausen()) return false;
		int restriction = gameStateController.getRestriction();
		if (!(declaration >= 0 && declaration <= Math.min(getState().getRound(),13) && restriction != declaration)){
			screen.highLightNotAllowed();
			return false;
		}

		/** Advance state */
		playerController.doDeclare(player, getState().getRound(), declaration);
		gameStateController.incrementTotalBausen(declaration);
		screen.updateScore(getState().getRound(), getState().getCurrentPlayer(), declaration, -1, false);
		if (gameStateController.isTrickLastPlayer()) onLastPlayerDeclared();
		else gameStateController.incrementCurrentPlayer();
		return true;
	}

	private boolean validatePlay(int player, int card) {

		if (getState().isDeclaringBausen()) return false;
		if (!isValidThrow(player, card)) {
			if (player == pid) {
				Array<Card> cards = Pools.obtain(Array.class);
				cards.clear();
				for (int c : getPlayer(player).getCards()) if (isValidThrow(player, c)) cards.add(getCardController().getCard(c));
				playerController.animateValidCards(player, cards);
				cards.clear();
				Pools.free(cards);
			}
			return false;
		}

		/** Advance state */
		board.receiveCard(player, card);
		playerController.playCard(player, card);
		if (board.getMiddleCardsNo() == 1) gameStateController.setFollowSuit(board.getFirstSuit());
		if (gameStateController.isTrickLastPlayer()) onLastPlayerPlayed();
		else gameStateController.incrementCurrentPlayer();
		return true;
	}

	private void onLastPlayerDeclared(){
		gameStateController.onLastPlayerDeclared();
		screen.updateDeclareView(gameModel(), getAttou(), -1);
		hideDeclareView();
		newTrick(getState().getTrickFirstPlayer());
	}

	private void onLastPlayerPlayed(){
		newTrick(findWinner(Card.getSuit(getState().getAttou())));
		getState().decrementCardsLeft();
	}

	private void endOfRound() {
		playerController.calculateRoundScore(getState().getRound());
		screen.updateScore(gameModel(), getState().getRound());

		checkForAchievementsAtEndOfRound();

		playerController.setActivePlayer(-1);
		save();

		new Timer().scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				screen.resetGameView();
				if (!newRound()) endOfGame();
				else requestAction();
			}
		}, 2f);
	}

	private void checkForAchievementsAtEndOfRound() {
		int achieved = getPlayer(pid).getAchievedNo(getState().getRound());
		int declared = getPlayer(pid).getDeclaredNo(getState().getRound());

		if (achieved == declared) {
			if (achieved == 4) getStatisticsController().unlockAchievement(AccomplishmentsModel.FISH);
			if (achieved == 5) getStatisticsController().unlockAchievement(AccomplishmentsModel.LARGE);
			if (achieved == 6) getStatisticsController().unlockAchievement(AccomplishmentsModel.HUGE);
//			if (achieved == 7) getStatisticsController().unlockAchievement(AccomplishmentsModel.BOSS);
//			if (achieved == 8) getStatisticsController().unlockAchievement(AccomplishmentsModel.SUPERSTAR);
		}
	}

	void endOfGame(){
		updateStatistics();
		clearSavedGame();
		log.i().append(" >>>>> End of game, deleting saved game").print();
		needsSave = false;
		gameModel = null;
		playing = false;
		screen.pullDownScore();

		getStatisticsController().unlockAchievement(AccomplishmentsModel.NOOB);
	}

	void updateStatistics() {
		getStatisticsController().update(gameModel(), pid);
	}

	private void onDeclareButtonPressed(){
		if (getState().isDeclaringBausen()) validateGameAction(getState().getCurrentPlayer(), true, screen.getDeclaration());
	}

	private int findWinner(int attouSuit){
		int winner = board.getWinner(attouSuit);
		board.playWinAnimation(winner);
		playerController.winsTrick(winner, getState().getRound());

		if (winner == pid) getStatisticsController().unlockAchievement(AccomplishmentsModel.PLAYA);
		return winner;
	}

	private void animateDeclareView() {
		new Timer().scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				screen.showDeclareWithAnimation();
			}
		}, PlayerView.DEALING_DELAY * getState().getRound() + PlayerView.DEALING_DURATION * 1f / 3f);
	}

	private void addListeners() {
		addEventListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				boolean playSound = true;
				String name = event.getListenerActor().getName();
				if (name == null) return;
				if (name.compareTo(GameScreen.DECLARE_BUTTON) == 0) {
					onDeclareButtonPressed();
				} else if (name.compareTo(GameScreen.RESIGN_BUTTON) == 0) {
					onResignButtonPressed();
				} else if (name.compareTo(CommonView.Dialogue.YES_BUTTON) == 0) {
					hideDialogue();
					onResignConfirmed();
				} else if (name.compareTo(CommonView.Dialogue.NO_BUTTON) == 0) {
					hideAnimationDialogue();
				} else if (name.compareTo(CommonView.Dialogue.OK_BUTTON) == 0) {
					hideDialogue();
					onOKButtonPressed();
				} else if (name.compareTo(GameScreen.BACK_BUTTON) == 0) {
					handleBack();
				} else if (name.compareTo(GameView.CHAT_BUTTON) == 0) {
					onChatButtonPressed();
				} else if (name.compareTo(ChatView.CLOSE_BUTTON) == 0) {
					screen.hideChatView();
				} else playSound = false;

				if (playSound) getSounds().playPopSound();
			}
		});
	}

	private void onChatButtonPressed() {
		screen.showChatView();
	}

	void onOKButtonPressed() {
	}

	void onResignButtonPressed() {
		if (gameModel() != null)
			showDialogue("Are you sure you want to resign?",
					"You will loose your bet (" + roomConfig.bet + ") coins.",
					true);
		else handleBack();
	}

	void onResignConfirmed(){
		if (gameModel() != null) getStatisticsController().incrementAbandoned(gameModel(), pid);
		needsSave = false;
		if (isLocalGame()) {
			log.i().append(" >>>>> deleting saved game").print();
			clearSavedGame();
			gameModel = null;
		}
		getScreenDirector().goToMenu();
		clearEventListeners();
	}

	boolean isLocalGame() {
		return true;
	}

	private void hideDeclareView(){
		new Timer().scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				screen.hideDeclareWithAnimation();
				for (int i = 0; i < 4; i++)
					playerController.layoutBausen(i, true, getState().getRound());
			}
		}, 0.5f);
		screen.setUnderOver(getState().getTotalBausen() - Math.min(getState().getRound(), 13));
	}

	@Override
	public boolean handleBack() {
		if (getMenuController().isShowingDialogue()) {
			hideAnimationDialogue();
			return true;
		}

		if (screen.isScoreViewPulledDown() && gameModel() != null) {
			screen.pullScoreUp();
			return true;
		}

		if (backButtonEnabled()) {
			save();
			getScreenDirector().goToMenu();

			return true;
		}
		return false;
	}

	@Override
	public void saveAndExit() {
		save();
		getScreenDirector().goToMenu();
	}

	boolean backButtonEnabled(){
		return true;
	}

	private Card getAttou() {
		Card attou = null;
		int a = getState().getAttou();
		int round = getState().getRound();

		if (round == 14 || a == -1) return null;

		if (round < 13) attou = getCardController().getCard(a);
		else if (round == 13) attou = getCardController().newCardFromPrototype(a);

		return attou;
	}

	GameModel gameModel(){
		return gameModel;
	}

	private GameState getState() {
		return gameModel().getState();
	}

	private PlayerModel getPlayer(int player){
		return gameModel().getPlayer(player);
	}

	boolean needsSave(){
		return needsSave;
	}

	public void save(){
		if (!needsSave() || gameModel == null) return;
		log.i().append(">>>>>>>>>>>>>> SAVING <<<<<<<<<<<<<<").print();
		getStorage().save("savedGame",gameModel.serialise());      //only local game
		needsSave = false;
	}

	private void clearSavedGame() {
		getStorage().delete("savedGame");
	}

	private boolean isValidThrow(int player, int card) {
		PlayerModel playerModel = getPlayer(player);
		if (playerModel.getCards().size != getState().getCardsLeft()) return false;
		if (!playerModel.hasCard(card)) return false;
		if (getState().getFollowSuit() == Card.Suit.NONE || Card.getSuit(card) == getState().getFollowSuit()) return true;
		return !hasSuit(player, getState().getFollowSuit());
	}

	private boolean hasSuit(int player, int suit) {
		for (int i = 0 ; i < getPlayer(player).getCards().size ; i++) if (Card.getSuit(getPlayer(player).getCards().get(i)) == suit) return true;
		return false;
	}

	@Override
	protected void disposeController() {
		super.disposeController();
	}

	@Override
	protected void update(float dt) {
		super.update(dt);
	}

	@Override
	public IDealer getDealer() {
		return dealer;
	}

	@Override
	public IBoardController getBoardController() {
		return board;
	}

	@Override
	public IGameStateController getGameStateController() {
		return gameStateController;
	}

	@Override
	public IPlayerController getPlayerController() {
		return playerController;
	}

	boolean tryToloadGameModel(){
		if (!Config.LOADING) return false;

		String jsonStr = getStorage().load("savedGame");
		if (jsonStr == null) return false;
		try {
			gameModel = GameModel.deserialise(jsonStr, GameModel.class);
		} catch (Exception exception) {
			log.e(exception).append("Cannot desirialise saved GameModel").print();
			return false;
		}
		if (gameModel != null) {
			log.i().append("------------- Last game LOADED -------------").print();
			roomConfig.set(gameModel.getRoomConfig());
			return true;
		}
		return false;
	}
}
