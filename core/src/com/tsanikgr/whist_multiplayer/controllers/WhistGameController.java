package com.tsanikgr.whist_multiplayer.controllers;

public abstract class WhistGameController extends Controller implements IWhistGameController {

	@Override
	public IDealer getDealer() {
		return ((IWhistGameController)parent).getDealer();
	}

	@Override
	public IBoardController getBoardController() {
		return ((IWhistGameController)parent).getBoardController();
	}

	@Override
	public IGameStateController getGameStateController(){
		return ((IWhistGameController)parent).getGameStateController();
	}

	@Override
	public IPlayerController getPlayerController(){
		return ((IWhistGameController)parent).getPlayerController();
	}

}
