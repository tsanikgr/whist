package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.utils.Array;
import com.tsanikgr.whist_multiplayer.Config;
import com.tsanikgr.whist_multiplayer.IMultiplayerGame;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.models.GameState;
import com.tsanikgr.whist_multiplayer.models.MultiplayerMessage;

import java.util.ArrayList;

public class MultiplayerGameController extends GameController implements IMultiplayerGame.In {

	private boolean playingMultiplayer = false;
	private boolean gameCancelled;

	private final GameModel.WhistRoomConfig roomConfigMulti;
	private GameModel multiplayerGameModel;
	private boolean betPlaced = false;

	public MultiplayerGameController() {
		super();
		gameCancelled = false;
		roomConfigMulti = new GameModel.WhistRoomConfig();
	}

	@Override
	public void activate() {
		activateLocalMultiCommon();

		playingMultiplayer = true;
		multiplayerGameModel = new GameModel();
		log.i().append("------------- NEW MULTIPLAYER GAME CREATED -------------").print();
	}

	@Override
	public void startGame(int playerId) {
		getStatisticsController().placeBet(roomConfigMulti.bet);
		betPlaced = true;
		pid = playerId;
		screen.setPid(pid);
		if (!initGame()) return;
		if (pid == 0) shuffle(-1);
		else showLoadingDialogue("Starting game",-1,-1);

		screen.showChatButton();
	}

	private void seedReceived(long seed) {
		hideLoadingDialogue();
		if (pid == 0) log.w().append("Seed was sent to first player as well").append("seedReceived").print();
		else {
			log.i().append("Seed received: ").append(seed).print();
			shuffle(seed);
		}
	}

	private void shuffle(long seed) {
		/* set seed and shuffle*/
		if (pid != 0) {
			dealer.shuffle(seed);
		} else {
			dealer.setNewSeed();
			getMultiplayer().sendMessage(MultiplayerMessage.build(multiplayerGameModel.getOrder(),
					0,
					MultiplayerMessage.Type.SEED,
					dealer.getSeed()));
			dealer.shuffle();
		}

		int firstPlayer = dealer.getFirstPlayer();
		gameStateController.newGame(firstPlayer);
		gameModel().setGameParams(roomConfigMulti, Config.STARTING_ROUND_CHECKED);
		screen.set13sCover(roomConfigMulti.has13s);
		newRound();
		requestAction();
	}

	private boolean initGame() {

		resetViews();
		initCommonControllers();
		addController(playerController = new MultiplayerPlayerController(multiplayerGameModel.getPlayers(), pid, screen));
		playerController.reset();
		board.reset(pid);

		if (!setMultiplayerNames()) return false;
		screen.set13sCover(getState().isPlay13s());
		return true;
	}

	private boolean setMultiplayerNames() {
		ArrayList<String> names = getMultiplayer().getPlayerNames();
		if (names == null) return false;

		Array<Integer> aiPlayers = new Array<>(3);
		for (int p = names.size(); p < 4; p++) aiPlayers.add(p);
		((MultiplayerPlayerController)playerController).setAIPlayers(aiPlayers);
		playerController.setNames(names);
		return true;
	}

	@Override
	public boolean onMessageReceived(MultiplayerMessage message) {
		boolean ret = true;
		if (message.getOrder() == 0) seedReceived(message.getMessage());
		else if (!validateGameAction(message.getFromPlayer(), message.isDeclare(), (int) message.getMessage())) ret = false;
		message.done();

		return ret;
	}

	@Override
	public boolean validateGameAction(int player, boolean isDeclareResult, int result) {
		if (player == -1) player = pid;
		log.i().append(getState().isDeclaringBausen() ? "DECLARING" : "PLAYING").print();
		log.i().append("Current player: ").append(getState().getCurrentPlayer()).print();
		log.i().append("Validating move from [").append(player).append("]:").append(isDeclareResult? "DECLARING " : "playing").append(result).print();
		if (!super.validateGameAction(player, isDeclareResult, result)) return false;
		log.i().append("valid").print();

		if (!playingMultiplayer) return true;
		if ((player == pid) || (pid == 0 && ((MultiplayerPlayerController)playerController).isAiPlayer(player))){
			MultiplayerMessage message = MultiplayerMessage.build(multiplayerGameModel.getOrder(),
					player,
					isDeclareResult ? MultiplayerMessage.Type.DECLARE : MultiplayerMessage.Type.CARD,
					result);
			getMultiplayer().sendMessage(message);
		}
		return true;
	}

	@Override
	protected void endOfGame() {
		if (!playingMultiplayer) super.endOfGame();
		else {
			updateStatistics();
			log.i().append(" >>>>> End of multiplayer game").print();
			multiplayerGameModel = null;
			playerController.deactivate();
			screen.pullDownScore();
			if (getMultiplayer() != null) getMultiplayer().leaveGame();
		}
	}

	@Override
	void onResignButtonPressed(){
		if (gameModel() != null)
			showDialogue("Are you sure you want to resign?",
					"You will loose your bet (" + roomConfigMulti.bet + ") coins.",
					true);
		else handleBack();
	}

	@Override
	public synchronized void cancelGame(boolean refundPlayer, String message) {
		getMenuController().hideLoadingDialogue();
		playingMultiplayer = false;
		gameCancelled = true;

		if (multiplayerGameModel == null) return;
		if (refundPlayer) {
			if (roomConfigMulti.bet != -1 && betPlaced) {
				getStatisticsController().refundBet(roomConfigMulti.bet);
			}
			showDialogue(message,
					roomConfigMulti.bet != -1 ? "Your bet (" + roomConfigMulti.bet + ") was refunded." : "Your bet was refunded.",
					false);
		}

		multiplayerGameModel = null;
		playerController.deactivate();
		playerController = null;
	}

	@Override
	protected void onOKButtonPressed(){
		if (gameCancelled) {
			gameCancelled = false;
			hideAnimationDialogue();
			getScreenDirector().goToMenu();
			return;
		}
		if (getMultiplayer() != null) getMultiplayer().leaveGame();
		playingMultiplayer = false;
	}

	@Override
	protected void onResignConfirmed(){
		super.onResignConfirmed();
		if (playingMultiplayer && getMultiplayer() != null) getMultiplayer().leaveGame();
		if (playingMultiplayer) {
			playingMultiplayer = false;
			multiplayerGameModel = null;
			playerController.deactivate();
			playerController = null;
		}
		hideAnimationDialogue();
		getScreenDirector().goToMenu();
	}

	@Override
	protected boolean isLocalGame() {
		return !playingMultiplayer && super.isLocalGame();
	}

	@Override
	public void setMultiplayerGameParams(GameModel.WhistRoomConfig roomConfig) {
		roomConfigMulti.set(roomConfig);
	}

	@Override
	public GameModel.WhistRoomConfig getWhistConfig() {
		return roomConfigMulti;
	}

	@Override
	public String getPlayerNickname() {
		return getStatisticsController().getName();
	}

	@Override
	public void setPlayerNickname(int playerIndex, String nickname){
		playerController.setName(playerIndex, nickname);
	}

	@Override
	protected boolean needsSave() {
		return !playingMultiplayer && super.needsSave();
	}

	@Override
	public boolean handleBack() {
		if (playingMultiplayer) {
			onResignButtonPressed();
			return true;
		}
		if (!super.handleBack()) {
			if (multiplayerGameModel == null) {
				getScreenDirector().goToMenu();
			}
			//go back to waiting screen
		}
		return true;
	}

	@Override
	protected boolean backButtonEnabled(){
		return false;
	}

	@Override
	GameModel gameModel() {
		if (playingMultiplayer) return multiplayerGameModel;
		return super.gameModel();
	}

	private GameState getState() {
		return gameModel().getState();
	}

	@Override
	protected boolean tryToloadGameModel() {
		return false;
	}
}
