package com.tsanikgr.whist_multiplayer.views.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.myactors.Card;
import com.tsanikgr.whist_multiplayer.myactors.Geometry;
import com.tsanikgr.whist_multiplayer.myactors.MyButton;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;
import com.tsanikgr.whist_multiplayer.myactors.MyLabel;
import com.tsanikgr.whist_multiplayer.views.View;

public class DeclareView extends View {

	private static final String DECLARE_LAYER_ROUND = "declare_layer_round";
	private static final String DECLARE_LAYER_TITLE = "declare_layer_title";
	private static final String DECLARE_LAYER_SCORE = "declare_layer_score_p";
	private static final String DECLARE_LAYER_NAME = "declare_layer_name_p";

	private static final String START = "start";
	private static final String ATTOU = "attou";
	private static final String DIALER = "dialer";

	private MyLabel round;
	private MyButton start;
	private Geometry.PlaceHolder attouPlaceholder;
	private Card attou;
	private BausenDialer dialer;

	private MyLabel title;
	private MyLabel[] declarationNames;
	private MyLabel[] declarations;
	private int pid;

	public DeclareView(String xmlFile, String name) {
		super(xmlFile, name);
	   pid = 0;
	}

	@Override
	public void dispose() {
	}

	@Override
	protected void onAssetsLoaded(View view) {
		initActors();
		pid = 0;
		setTouchable(Touchable.childrenOnly);
	}

	private int pid2View(int pid) {
		return (pid - this.pid + 4) % 4;
	}

	private void initActors() {
		Geometry.fixOrigin(this);
		title = findActor(DECLARE_LAYER_TITLE);
		round = findActor(DECLARE_LAYER_ROUND);
		declarationNames = new MyLabel[4];
		declarations = new MyLabel[4];

		for (int i = 0 ; i < 4 ; i++) {
			declarations[i] = findActor(DECLARE_LAYER_SCORE + i);
			declarationNames[i] = findActor(DECLARE_LAYER_NAME + i);
			declarationNames[i].setMaxCharacters(10);
		}

		start = findActor(START);
		attouPlaceholder = new Geometry.PlaceHolder(findActor(ATTOU));
		findActor(ATTOU).remove();
		attou = null;
		dialer = new BausenDialer((MyGroup)findActor(DIALER));
		addActor(dialer);
	}

	public void update(int player, GameModel model, Card attou, int round, int notAllowed) {
		dialer.init(round, notAllowed);
		setPlayerName(model.getPlayer(player).getName());
		setRound(round);
		for (int i = 0 ; i < 4 ; i++) {
			setPlayerDeclaration(i, model.getPlayer(i).getName(), model.getPlayer(i).getDeclaredNo(round), i == player ? 1.0f : 0.5f);
		}
		setAttou(attou);

		if (pid2View(player) != 0) {
			dialer.getColor().a = 0.1f;
			start.getColor().a = 0.1f;
			dialer.setTouchable(Touchable.disabled);
			start.setTouchable(Touchable.disabled);
		} else {
			dialer.getColor().a = 1.0f;
			start.getColor().a = 1.0f;
			dialer.setTouchable(Touchable.childrenOnly);
			start.setTouchable(Touchable.enabled);
		}
	}

	private void setRound(int round) {
		this.round.setText("ROUND " + round);
	}

	public Card getAttou(){
		return attou;
	}

	private void setAttou(Card attou) {
		if (this.attou == attou) return;
		if (this.attou != null) {
			removeActor(this.attou);
			this.attou = null;
		}
		if (attou == null) return;

		this.attou = attou;
		this.attou.resetCard();
		attouPlaceholder.applyTo(this.attou);
		addActor(this.attou);
		this.attou.setVisible(true);
		this.attou.setDisabled(false);
	}

	public void updateName(int player, String name) {
		if (validateTitle(name).compareTo(title.getText().toString()) == 0) {
			setPlayerName(name);
		}
		declarationNames[player].setText(name);
		declarationNames[player].pack();
	}

	private void setPlayerName(String name) {
		title.setText(validateTitle(name) + " playing");
		title.pack();
	}

	private String validateTitle(String name) {
		if (name.length() > 10) name = name.substring(0,9) + (name.substring(9,10).compareTo(" ") == 0 ? "" : name.substring(9,10)) + "..";
		return name;
	}

	private void setPlayerDeclaration(int playerIndex, String name, int declaration, float alpha) {

		playerIndex = pid2View(playerIndex);

		declarationNames[playerIndex].setText(name);
		if (declaration == - 1) declarations[playerIndex].setText("");
		else declarations[playerIndex].setText(declaration + "");

		declarationNames[playerIndex].pack();
		declarations[playerIndex].pack();
		declarationNames[playerIndex].getColor().a = alpha;
		declarations[playerIndex].getColor().a = alpha;
	}

	public void startShowAnimation() {
		setVisible(true);
		getColor().a = 0f;
		addAction(Actions.scaleTo(0f, 0f));
		float d = 0.5f;
		addAction(Actions.parallel(Actions.fadeIn(d, Interpolation.pow2Out), Actions.scaleTo(1.0f, 1.0f, d, Interpolation.swingOut)));
	}

	public void hideWithAnimation() {
		addAction(Actions.sequence(Actions.parallel(
				Actions.fadeOut(0.5f, Interpolation.pow2In),
				Actions.scaleTo(0.0f,0.0f,0.5f,Interpolation.pow2In)),
				Actions.run(new Runnable() {
			@Override
			public void run() {
				if (attou != null) {
					removeActor(attou);
					attou = null;
				}
				setVisible(false);
			}
		})));
	}

	private static final Color backColor = new Color(0f, 0f, 0f, 0.75f);

	@Override
	public Color getBackgroundColor() {
		return backColor;
	}

	@Override
	public void setBackground(MyImage prototypeBackground) {
		super.setBackground(prototypeBackground);
		Actor back = findActor("background");
		back.setSize(back.getWidth() * 4f, back.getHeight() * 4f);
		dialer.remove();
		Geometry.alignActors(Align.center, Align.center, this, back);
		addActor(dialer);
	}

	public int getDeclaration() {
		return dialer.getSelection();
	}
	public void highLightNotAllowed() {
		dialer.highLightNotAllowed();
	}
	public void reset(int pid) {
		this.pid = pid;
	}
}
