package com.tsanikgr.whist_multiplayer.views.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.utils.Align;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.myactors.Geometry;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;
import com.tsanikgr.whist_multiplayer.myactors.MyLabel;
import com.tsanikgr.whist_multiplayer.views.View;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;

public class ScoreView extends View implements PullDownInterface{

	private static final String SCORE_BUTTON = "score_button";
	private static final String BUTTON_ROUND_LABEL = "button_round_label";
	private static final String BUTTON_OVER_UNDER = "button_over_under";

	private static final String SCORE_TABLE = "score_table";
	private static final String NAMES = "score_table_name_p";
	private static final String DECLARATIONS = "score_table_declared_p";
	private static final String SCORES = "score_table_score_p";
	private static final String NO_13_S = "score_table_no_13s";
	private static final String ROUNDS_GROUP = "rounds_labels";
	private static final String NAMES_PLACEHOLDERS = "names_back_";
	private static Color WRONG_COLOUR;
	private static Color CORRECT_COLOUR;

	private MyGroup scoreTable;
	private MyLabel roundLabel;
	private Container overUnderLabelContainer;
	private MyGroup scoreButton;
	private MyLabel[] names;
	private MyLabel[][] scores;
	private MyLabel[][] declarations;
	private Geometry.PlaceHolder[] namePlaceholders;
	private MyImage no13sCover;
	private int pid = 0;

	private float dy;


	public ScoreView(String xmlFile, String name) {
		super(xmlFile, name);
		pid = 0;
	}

	@Override
	public void dispose() {
	}

	@Override
	protected void onAssetsLoaded(View view) {
		initActors();
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (getY() >= minY-1) scoreTable.setVisible(false);
		else scoreTable.setVisible(true);
		super.draw(batch, parentAlpha);
	}

	private void initActors() {
		setTouchable(Touchable.childrenOnly);

		scoreTable = findActor(SCORE_TABLE);

		names = new MyLabel[4];
		scores = new MyLabel[4][14];
		declarations = new MyLabel[4][14];
		namePlaceholders = new Geometry.PlaceHolder[4];
		no13sCover = findActor(NO_13_S);

		MyGroup roundsLabels = findActor(ROUNDS_GROUP);
		dy = roundsLabels.getChildren().get(1).getY() - roundsLabels.getChildren().get(0).getY();

		for (int i = 0 ; i < 4; i++) {
			names[i] = findActor(NAMES+i);
			names[i].setMaxCharacters(12);
			names[i].setAlignment(Align.center);
			declarations[i][0] = findActor(DECLARATIONS+i);
			declarations[i][0].setVisible(false);
			scores[i][0] = findActor(SCORES+i);
			scores[i][0].setVisible(false);
			for (int j = 1 ; j < 14 ; j++) {
				declarations[i][j] = null;
				scores[i][j] = null;
			}
			namePlaceholders[i] = new Geometry.PlaceHolder(findActor(NAMES_PLACEHOLDERS +i));
		}
		CORRECT_COLOUR = scores[0][0].getStyle().fontColor;
		WRONG_COLOUR = scores[2][0].getStyle().fontColor;
		for (int i = 0 ; i < 4; i++)
			scores[i][0].getStyle().fontColor = Color.WHITE;

		scoreButton = findActor(SCORE_BUTTON);
		roundLabel = scoreButton.findActor(BUTTON_ROUND_LABEL);
		MyLabel overUnderLabel = scoreButton.findActor(BUTTON_OVER_UNDER);
		overUnderLabelContainer = new Container(overUnderLabel);
		overUnderLabelContainer.setBounds(overUnderLabel.getX(), overUnderLabel.getY(), overUnderLabel.getWidth(), overUnderLabel.getHeight());
		overUnderLabelContainer.setTransform(true);
		overUnderLabel.setPosition(0f, 0f);
		overUnderLabel.getStyle().fontColor = Color.WHITE;
		scoreButton.addActor(overUnderLabelContainer);
	}

	public void setPlayerName(int player, String name) {
		names[player].setText(name);
		names[player].pack();
		namePlaceholders[player].applyTo(names[player]);
//		Geometry.alignActors(Align.center, 0, namePlaceholders[player], names[player]);
	}

	public void updateScore(GameModel model, int round){
		for (int index = 0 ; index < 4 ; index++) {
			updateScore(round, index, model.getPlayer(index).getDeclaredNo(round), model.getPlayer(index).getScore(round), model.getPlayer(index).isWorseScore(round));
		}
	}

	public void updateScore(int round, int player, int declaredNo, int score, boolean worse){

		if (declaredNo == -1) {
			clearRound(player,round);
			return;
		}

		player = pid2View(player);
		round--;

		if (declarations[player][round] == null) declarations[player][round] = getNewDeclarationLabel(player,round);
		if (scores[player][round] == null) scores[player][round] = getNewScoreLabel(player, round);

		declarations[player][round].setText(Integer.toString(declaredNo));
		declarations[player][round].setVisible(true);

		if (score != -1) {
			scores[player][round].setText(Integer.toString(score));
			scores[player][round].setVisible(true);
			if (worse) scores[player][round].setColor(WRONG_COLOUR);
			else scores[player][round].setColor(CORRECT_COLOUR);
		} else scores[player][round].setVisible(false);
	}

	private MyLabel getNewScoreLabel(int player, int round) {

		MyLabel label = new MyLabel("",scores[player][0].getStyle());
		label.setFontScale(scores[player][0].getFontScaleX());
		label.setPosition(scores[player][0].getX(), scores[player][0].getY() + dy * round);
		label.setSize(scores[player][0].getWidth(), scores[player][0].getHeight());
		scoreTable.addActor(label);
		return label;
	}

	private MyLabel getNewDeclarationLabel(int player, int round) {
		MyLabel label = new MyLabel("", declarations[player][0].getStyle());
		label.setFontScale(declarations[player][0].getFontScaleX());
		label.setPosition(declarations[player][0].getX(), declarations[player][0].getY() + dy * round);
		label.setSize(declarations[player][0].getWidth(), declarations[player][0].getHeight());
		scoreTable.addActor(label);
		return label;
	}

	private void clearRound(int player, int round){
		round--;
		player = pid2View(player);

		if (scores[player][round] != null) {
			if (round == 0) scores[player][round].setVisible(false);
			else {
				scores[player][round].remove();
				scores[player][round] = null;
			}
		}
		if (declarations[player][round] != null) {
			if (round == 0) declarations[player][round].setVisible(false);
			else {
				declarations[player][round].remove();
				declarations[player][round] = null;
			}
		}
	}

	public void reset(int pid){
		this.pid = pid;
		reset();
	}

	private void reset() {
		for (int i = 0 ; i < 4; i++) for (int j = 1 ; j < 15 ; j++)
				clearRound(i,j);
	}

	public void set13sCover(boolean has13s) {
		no13sCover.setVisible(!has13s);
	}

	public void setRound(int round) {
		if (round == 14) roundLabel.setText("Round 13*");
		else roundLabel.setText("Round "+round);

		roundLabel.pack();
		Geometry.alignActors(Align.center, 0, scoreButton, roundLabel);
		setUnderOver(0);
	}

	public void setUnderOver(int underOver){
		MyLabel overUnderLabel = (MyLabel)overUnderLabelContainer.getActor();
		if (underOver < 0) overUnderLabel.setText("under " + underOver * -1);
		else if (underOver == 0) overUnderLabel.setText("");
		else overUnderLabel.setText("over " + underOver);

		overUnderLabel.pack();
		overUnderLabelContainer.pack();
		Geometry.alignActors(Align.center, 0, scoreButton, overUnderLabelContainer);
		Geometry.fixOrigin(overUnderLabelContainer);
		overUnderLabelContainer.setOriginY(overUnderLabelContainer.getHeight());
		overUnderLabelContainer.addAction(Actions.sequence(
				Actions.parallel(
						/*Actions.moveBy(0f, -overUnderLabel.getHeight(), 0.5f, Interpolation.fade),*/
						Actions.scaleTo(1.4f, 1.4f, 1.0f, Interpolation.fade)),
				Actions.delay(1.0f),
				Actions.parallel(
						/*Actions.moveBy(0f, overUnderLabel.getHeight(), 0.5f, Interpolation.fade),*/
						Actions.scaleTo(1.0f, 1.0f, 0.5f, Interpolation.fade))));
		overUnderLabel.addAction(Actions.color(Color.RED));
		overUnderLabel.addAction(Actions.color(overUnderTextColor, 3.0f, Interpolation.pow5In));
	}

	private static final Color backColor = new Color(0f, 0f, 0f, 0f);
	private static final Color overUnderTextColor = new Color(0.5f,0.5f,0.5f,1.0f);
	@Override
	public Color getBackgroundColor() {
		return backColor;
	}

	@Override
	public void setBackground(MyImage prototypeBackground) {
	}

	private float minY;
	private float maxY;

	void addScoreDragListener(float screenHeight) {
		minY = getY();
		maxY = screenHeight * (1f-1f/30f);
		scoreButton.setTouchable(Touchable.childrenOnly);
		addListener(new PulldownAdaptor(this, -maxY, minY));
	}

	public boolean isPulledDown() {
		return Math.abs(getY() - minY) > 0.0001;
	}

	@Override
	public void pullDown() {
		clearActions();
		addAction(moveTo(getX(), -maxY, 0.4f, Interpolation.swingOut));
	}

	@Override
	public void pullUp() {
		clearActions();
		addAction(moveTo(getX(), minY, 0.4f, Interpolation.swingOut));
	}

	public void fillToRound(GameModel gameModel) {

		pid = 0;
		setRound(gameModel.getState().getRound());
		int round = gameModel.getState().getRound();

		for (int i = 0 ; i < 4 ; i++) setPlayerName(pid2View(i), gameModel.getPlayer(i).getName());
		for (int i = 1 ; i <= round ; i++) updateScore(gameModel,i);
	}

	private int pid2View(int pid) {
		return (pid - this.pid + 4) % 4;
	}
}
