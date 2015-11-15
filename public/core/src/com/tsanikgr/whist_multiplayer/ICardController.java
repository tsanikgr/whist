package com.tsanikgr.whist_multiplayer;

import com.badlogic.gdx.graphics.Color;
import com.tsanikgr.whist_multiplayer.myactors.Card;

public interface ICardController {
	Card getCard(int index);
	void resetCards();
	Card newCardFromPrototype(int index);
	void loadAsync();
	void setCardColor(Color color, boolean async);
}
