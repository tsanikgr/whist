package com.tsanikgr.whist_multiplayer.views.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.tsanikgr.whist_multiplayer.myactors.Geometry;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.myactors.MyTextButton;

public class BausenDialer extends MyGroup {

	private static final String DIALER_CENTER = "lb_center";
	private static final String DIALER_BOTTOM = "lb_bottom";
	private static final String NOT_ALLOWED = "not_allowed";

	private final float dy;
	private final float dyNotAllowed;
	private int maxBausen = 1;
	private int notAllowed = -1;

	private final MyTextButton prototype;
	private final MyGroup notAllowedLabel;
	private final MyGroup numbers = new MyGroup();

	public BausenDialer(MyGroup group){
		super(group);

		prototype = findActor(DIALER_CENTER);
		findActor(DIALER_BOTTOM).setVisible(false);
		prototype.setVisible(false);
//		prototype.pack();

		notAllowedLabel = findActor(NOT_ALLOWED);
		notAllowedLabel.setVisible(false);
//		((Label)notAllowedLabel.getChildren().get(0)).pack();
//		Geometry.sizeFromChildren(notAllowedLabel);
		Geometry.fixOrigin(notAllowedLabel);

		dy = prototype.getY() - findActor(DIALER_BOTTOM).getY();
		dyNotAllowed = prototype.getY() - notAllowedLabel.getY();

		numbers.setSize(prototype.getWidth(), dy * 13);
		numbers.setPosition(prototype.getX(), prototype.getY());
		numbers.setTouchable(Touchable.childrenOnly);
		prototype.setX(0);
		prototype.remove();
		findActor(DIALER_BOTTOM).remove();

		MyTextButton label;
		for (int i = 0 ; i < 14 ; i++) {
			numbers.addActor(label = new MyTextButton(i + "", prototype.getStyle()));
			label.setPosition(0, dy * i);
			label.setSize(prototype.getWidth(),prototype.getHeight());
			label.getLabel().setFontScale(prototype.getLabel().getFontScaleX());
			label.getLabel().setFontScaleY(prototype.getLabel().getFontScaleY());
//			label.pack();
			Geometry.alignActors(Align.center, 0, prototype, label);
		}

		addScrollAdaptor();

		numbers.addActor(notAllowedLabel);
		addActor(numbers);
		notAllowedLabel.setPosition(notAllowedLabel.getX() - numbers.getX(), -dyNotAllowed);
		init(1,-1);
	}

	private DialerAdaptor listener;

	private void addScrollAdaptor() {
		float minY = numbers.getY();
		float maxY = -numbers.getY()+13*dy;
		numbers.addListener(listener = new DialerAdaptor(numbers, -maxY, minY, 13));
	}

	public void init(int round, int notAllowed) {
		this.notAllowed = notAllowed;
		this.maxBausen = Math.min(round,13);

		if (notAllowed == -1) {
			notAllowedLabel.setVisible(false);
			if (getSelection() < 0 || getSelection() > round) setSelection(0);
		} else {
			notAllowedLabel.setY(notAllowed * dy - dyNotAllowed);
			setSelection(notAllowed);
		}

		listener.init(maxBausen);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		float pos;
		for (int i = 0 ; i < 14 ; i ++) {
			pos = Math.abs((numbers.getY() - prototype.getY()) / dy) - (float) i;
			numbers.getChildren().get(i).getColor().a = Math.min(Math.max(0, 1f - Math.abs(pos) / 1.5f), 1f);
			if (Math.abs(pos) > 1.5 || i > maxBausen) {
				numbers.getChildren().get(i).setTouchable(Touchable.disabled);
				numbers.getChildren().get(i).setVisible(false);
			} else {
				numbers.getChildren().get(i).setVisible(true);
				numbers.getChildren().get(i).setTouchable(Touchable.enabled);
			}
		}

		if (notAllowed != -1) {
			pos = Math.abs((numbers.getY() - prototype.getY()) / dy ) - (float) (notAllowed);
			notAllowedLabel.getColor().a = Math.min(Math.max(0, 1f - Math.abs(pos) / 1f), 1f);
			if (Math.abs(pos) > 1f || pos > maxBausen) {
				notAllowedLabel.setVisible(false);
				notAllowedLabel.setTouchable(Touchable.disabled);
			}
			else {
				notAllowedLabel.setVisible(true);
				notAllowedLabel.setTouchable(Touchable.enabled);
			}
		} else {
			notAllowedLabel.setVisible(false);
			notAllowedLabel.setTouchable(Touchable.disabled);
		}

		super.draw(batch, parentAlpha);
	}
	public int getSelection() {
		float pos = Math.abs((numbers.getY() - prototype.getY()) / dy);
		return Math.round(pos);
	}
	public void setSelection(int selection) {
		if (getSelection() == selection) return;
		numbers.addAction(Actions.moveTo(numbers.getX(), - selection * dy + prototype.getY(),0.5f,Interpolation.pow2));
	}

	public void highLightNotAllowed() {
		float d = 0.07f;
		notAllowedLabel.addAction(Actions.sequence(Actions.rotateTo(5f, d / 2f, Interpolation.fade),
				Actions.repeat(3, Actions.sequence(Actions.rotateTo(-5f, d, Interpolation.fade), Actions.rotateTo(5f, d, Interpolation.fade))),
				Actions.rotateTo(0f, d / 2f, Interpolation.fade)));
		notAllowedLabel.addAction(Actions.sequence(Actions.scaleTo(6f, 6f,  d * 7f / 2f, Interpolation.fade),
				Actions.scaleTo(1f, 1f, d * 7f / 2f, Interpolation.fade)));
	}
}