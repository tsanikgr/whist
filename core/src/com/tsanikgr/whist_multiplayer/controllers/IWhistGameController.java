package com.tsanikgr.whist_multiplayer.controllers;

public interface IWhistGameController {
	IDealer getDealer();
	IBoardController getBoardController();
	IGameStateController getGameStateController();
	IPlayerController getPlayerController();
}
