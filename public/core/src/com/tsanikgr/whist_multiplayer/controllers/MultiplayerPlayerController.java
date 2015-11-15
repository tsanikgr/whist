package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.utils.Array;
import com.tsanikgr.whist_multiplayer.Config;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.models.PlayerModel;
import com.tsanikgr.whist_multiplayer.views.game.GameScreen;

public class MultiplayerPlayerController extends PlayerController {

	private Array<Integer> aiPlayerIds = null;

	public MultiplayerPlayerController(PlayerModel[] players, int pid, GameScreen screen) {
		super(players, pid, screen);
	}

	public void setAIPlayers(Array<Integer> aiPlayerIds) {
		this.aiPlayerIds = aiPlayerIds;
	}

	@Override
	public void requestAction(GameModel model) {
		int currentPlayer = model.getState().getCurrentPlayer();
		boolean declaringBausen = model.getState().isDeclaringBausen();

		setActivePlayer(currentPlayer);

		if (model.getState().getCurrentPlayer() == pid) {
			getSettings().doVibrate(ACTIVE_VIBRATION_LENGTH, ACTIVE_PLAYER_VIBRATION_DELAY);
			if (Config.AUTOPLAY) {
				if (declaringBausen) simulator.howManyToDeclare(model, currentPlayer, getRestriction(model));
				else simulator.whichCardToPlay(model, currentPlayer);
			}
		}

		if (!isHost()) return;
		for (int i = 0; i < aiPlayerIds.size ; i++) {
			if (currentPlayer != aiPlayerIds.get(i)) continue;
			if (declaringBausen) simulator.howManyToDeclare(model, currentPlayer, getRestriction(model));
			else simulator.whichCardToPlay(model, currentPlayer);
		}
	}

	private boolean isHost() {
		if (pid == 0) return true;
		if (aiPlayerIds.size == 3) return true;
		for (int i = 1; i < 4 ; i++) if (aiPlayerIds.contains(i, false)) return i == pid;
		return false;
	}

	public boolean isAiPlayer(int player) {
		return aiPlayerIds != null && aiPlayerIds.contains(player, true);
	}
}
