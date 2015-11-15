package com.tsanikgr.whist_multiplayer.AI;

interface ValidateAiCallback {
	boolean validateGameAction(ThreadedGameModelPool.SimGameModel model, int player, boolean isDeclareResult, int result);
}