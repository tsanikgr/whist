package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Timer;
import com.tsanikgr.whist_multiplayer.AI.AiConfig;
import com.tsanikgr.whist_multiplayer.AI.GameSimulator;
import com.tsanikgr.whist_multiplayer.AI.WhistAIInterface;
import com.tsanikgr.whist_multiplayer.Config;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.models.GameState;
import com.tsanikgr.whist_multiplayer.models.PlayerModel;
import com.tsanikgr.whist_multiplayer.myactors.Card;
import com.tsanikgr.whist_multiplayer.views.game.GameScreen;

import java.util.ArrayList;

public class PlayerController extends WhistGameController implements IPlayerController{

	private static final float REMINDER_INTERVAL = 8f;
	static final int ACTIVE_PLAYER_VIBRATION_DELAY = 500;
	static final int ACTIVE_VIBRATION_LENGTH = 40;

	private final PlayerModel[] players;
	private final GameScreen screen;
	final GameSimulator simulator;
	final int pid;

	private final Timer turnReminder = new Timer();
	private final Timer.Task reminderTask = new Timer.Task() {
		@Override
		public void run() {
			getSounds().playTurnSound();
			screen.nudgePlayerCards(pid);
		}
	};
	private final boolean[] nicknameSet = new boolean[4];

	public PlayerController(PlayerModel[] players, int pid, GameScreen screen) {
		this.screen = screen;
		this.pid = pid;
		this.players = players;
		for (int i = 0; i < 4; i++) {
			screen.setPlayerName(i, players[i].getName());
			nicknameSet[i] = false;
		}
		simulator = new GameSimulator(new AiConfig(), getWhistAiCallback(),Config.AUTOPLAY_DELAY);
	}

	@Override
	protected void init() {
	}

	@Override
	protected void disposeController() {
		cancelAI();
	}

	@Override
	void deactivate() {
		super.deactivate();
		setActivePlayer(-1);
		cancelAI();
		cancelReminder();
	}

	private WhistAIInterface.WhistAICallback getWhistAiCallback(){
		return new WhistAIInterface.WhistAICallback() {
			@Override
			public void onDeclare(int player, int declaration) {
				getGameController().validateGameAction(player, true, declaration);
			}
			@Override
			public void onPlayCard(int player, int card) {
				getGameController().validateGameAction(player, false, card);
			}
		};
	}

	public void requestAction(GameModel model) {
		int currentPlayer = model.getState().getCurrentPlayer();
		boolean declaringBausen = model.getState().isDeclaringBausen();

		setActivePlayer(currentPlayer);
		if (model.getState().getCurrentPlayer() != 0 || Config.AUTOPLAY) {
			if (declaringBausen) simulator.howManyToDeclare(model, currentPlayer, getRestriction(model));
			else simulator.whichCardToPlay(model, currentPlayer);
		} else {
			getSettings().doVibrate(ACTIVE_VIBRATION_LENGTH, ACTIVE_PLAYER_VIBRATION_DELAY);
		}
	}

	int getRestriction(GameModel model){
		GameState state = model.getState();
		int restriction = state.getCurrentPlayer() == (state.getTrickFirstPlayer()+3)%4 ? state.getRound() - state.getTotalBausen() : -1;
		if (state.getRound() == 14) restriction--;

		return Math.max(restriction,-1);
	}

	public void reset() {
		screen.resetPlayerViews();
		for (int i = 0 ; i < 4 ; i++) players[i].reset();
	}

	public void newRound(int round) {
		//update models
		for (int p = 0; p < 4; p++) {
			players[p].setDeclaredNo(-1, round);
			players[p].setAchievedNo(-1, round);
		}
		//update UI
		resetBausen();
		dealCards(round);
	}

	private void dealCards(int round) {
		Array<Integer> cards = Pools.obtain(Array.class);
		for (int i = 0; i < 4; i++) {
			cards.clear();
			getDealer().deal(Math.min(round, 13), cards);
			players[i].receiveCards(cards);
		}
		cards.clear();
		Pools.free(cards);
		layoutCards();

		getSounds().playDealSound();
	}

	public int getDeclaredNo(int player, int round) { return players[player].getDeclaredNo(round); }

	public void doDeclare(int currentPlayer, int round, int declaration) {
		players[currentPlayer].setDeclaredNo(declaration, round);
	}

	public void winsTrick(int winner, int round) {
		players[winner].incrementAchievedNo(round);
		layoutBausen(winner, false, round);
	}

	public void layoutCards() {
		Array<Card> cards = Pools.obtain(Array.class);
		for (int i = 0 ; i < 4; i++) {
			cards.clear();
			for (int c : players[i].getCards()) cards.add(getCardController().getCard(c));
			screen.playDealingAnimation(i, cards);
		}
		cards.clear();
		Pools.free(cards);
	}

	public void layoutBausen(int player, boolean reset, int round){
		if (reset) players[player].setAchievedNo(0, round);
		screen.updateBausen(player, players[player].getDeclaredNo(round), players[player].getAchievedNo(round), reset);
	}

	private void resetBausen(){
		screen.resetBausen();
	}

	public void playCard(int player, int card) {
		players[player].throwCard(card);
		screen.throwCard(player, getCardController().getCard(card));
	}

	public void calculateRoundScore(int round) {
		int score;
		PlayerModel player;
		for (int i = 0; i < 4; i++) {
			player = players[i];
			score = 0;
			if (round > 1 && player.getScore(round-1) != -1) score += player.getScore(round-1);
			score += player.getDeclaredNo(round) == player.getAchievedNo(round) ? 10 : 0;
			score += player.getDeclaredNo(round) == player.getAchievedNo(round) ?
					player.getDeclaredNo(round) * 5 :
					-Math.abs(player.getDeclaredNo(round)-player.getAchievedNo(round)) * 5;

			player.setScore(score, round);
		}
	}

	public void layoutBausenFromLoading(GameModel gameModel) {
		int round = gameModel.getState().getRound();
		int declared, achieved;
		for (int i = 0 ; i < 4 ; i++) {
			declared = gameModel.getPlayer(i).getDeclaredNo(round);
			achieved = gameModel.getPlayer(i).getAchievedNo(round);
			screen.updateBausen(i, declared, 0, true);
			for (int j = 0 ; j < achieved ; j++) screen.updateBausen(i,declared,achieved,false);
		}
	}

	private void cancelAI() {
		if (simulator != null) simulator.stop();
	}

	public void setNames(ArrayList<String> names) {
		for (int i = 0 ; i < names.size() ; i++) {
			if (nicknameSet[i]) continue;
			players[i].setName(names.get(i));
			screen.setPlayerName(i, names.get(i));
		}
		for (int i = names.size() ; i < 4; i++) {
			if (nicknameSet[i]) continue;
			screen.setPlayerName(i, players[i].getName());
		}
		setName(pid, getStatisticsController().getName());
	}

	public void setName(int playerIndex, String nickname) {
		nicknameSet[playerIndex] = true;
		players[playerIndex].setName(nickname);
		screen.setPlayerName(playerIndex, nickname);
	}

	public void animateValidCards(int player, Array<Card> cards) {
		screen.animateValidCards(player, cards);
	}

	public void setActivePlayer(int player) {
		screen.setActivePlayer(player);
		if (player == pid) {
			if (reminderTask.isScheduled()) return;
			turnReminder.scheduleTask(reminderTask,REMINDER_INTERVAL,REMINDER_INTERVAL);
		} else cancelReminder();
	}

	private void cancelReminder(){
		reminderTask.cancel();
		turnReminder.clear();
	}
}
