package com.tsanikgr.whist_multiplayer.stage_builder.builders;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.tsanikgr.whist_multiplayer.assets.IAssets;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.assets.LocalizationService;
import com.tsanikgr.whist_multiplayer.myactors.Card;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.CardModel;

public class CardBuilder extends ButtonBuilder {
	public CardBuilder(IAssets assets, IResolution resolution, LocalizationService localizationService) {
		super(assets, resolution, localizationService);
	}

	@Override
	public Card build(BaseActorModel model) {

		CardModel cardModel = (CardModel) model;
		setTextures(cardModel);

		Card card = fromName(cardModel.getName().substring(cardModel.getName().lastIndexOf("_")+1,cardModel.getName().length()), up, down);

		if (cardModel.getFrameDisabled() != null && card.getIndex() < 52) {
			card.getStyle().disabled = disabled;
		}

		if ( cardModel.getFrameChecked() != null && card.getIndex() < 52){
			card.getStyle().checked = checked;
		}

		normalizeModelSize(cardModel, up.getMinWidth(), up.getMinHeight());
		setBasicProperties(model, card);

		return card;
	}

	private Card fromName(String name, Drawable up, Drawable down) {
		int index;
		int rank = 0;
		int suit = 0;

		if (name.substring(0,1).compareTo("h")==0) suit = Card.Suit.HEARTS;
		else if (name.substring(0,1).compareTo("s")==0) suit = Card.Suit.SPADES;
		else if (name.substring(0,1).compareTo("d")==0) suit = Card.Suit.DIAMODS;
		else if (name.substring(0,1).compareTo("c")==0) suit = Card.Suit.CLUBS;
//		else if (name.substring(0,1).compareTo("p")==0) return new Card(52); //is created in card controller (= deck card)
		else if (name.substring(0,1).compareTo("b")==0) return new Card(53);
		else if (name.substring(0,1).compareTo("C")==0) return new Card(54);
		else if (name.substring(0,1).compareTo("f")==0) return new Card(55);

		if (name.substring(1,name.length()).compareTo("A") == 0) rank = 1;
		else if (name.substring(1,name.length()).compareTo("2") == 0) rank = 2;
		else if (name.substring(1,name.length()).compareTo("3") == 0) rank = 3;
		else if (name.substring(1,name.length()).compareTo("4") == 0) rank = 4;
		else if (name.substring(1,name.length()).compareTo("5") == 0) rank = 5;
		else if (name.substring(1,name.length()).compareTo("6") == 0) rank = 6;
		else if (name.substring(1,name.length()).compareTo("7") == 0) rank = 7;
		else if (name.substring(1,name.length()).compareTo("8") == 0) rank = 8;
		else if (name.substring(1,name.length()).compareTo("9") == 0) rank = 9;
		else if (name.substring(1,name.length()).compareTo("10") == 0) rank = 10;
		else if (name.substring(1,name.length()).compareTo("11") == 0) rank = 11;
		else if (name.substring(1,name.length()).compareTo("12") == 0) rank = 12;
		else if (name.substring(1,name.length()).compareTo("13") == 0) rank = 13;

		index = suit*13 + rank-1;

		return new Card(index, up, down);
	}
}
