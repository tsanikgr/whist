package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.utils.Array;

public interface IDealer {
	void shuffle();
	void deal(int nCards, Array<Integer> cards);
	int getNewAttou();
}
