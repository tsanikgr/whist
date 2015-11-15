package com.tsanikgr.whist_multiplayer.AI;

import com.tsanikgr.whist_multiplayer.models.GameModel;

public class ProbabilisticWhistAI extends AbstractWhistAI {

	public ProbabilisticWhistAI(WhistAICallback callback, float minimumDelay) {
		super(callback, minimumDelay);
	}
	@Override
	protected int requestDeclaration(GameModel model) {
		return 0;
	}
	@Override
	protected int requestWhichCardToPlay(GameModel model) {
		return 0;
	}
}
