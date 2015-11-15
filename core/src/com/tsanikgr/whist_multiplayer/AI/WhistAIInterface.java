package com.tsanikgr.whist_multiplayer.AI;

import com.tsanikgr.whist_multiplayer.models.GameModel;

public interface WhistAIInterface {
	void howManyToDeclare(GameModel model, int player, int restriction);
	void whichCardToPlay(GameModel model, int player);

	interface WhistAICallback {
		void onDeclare(int player, int declaration);
		void onPlayCard(int player, int card);
	}
}
