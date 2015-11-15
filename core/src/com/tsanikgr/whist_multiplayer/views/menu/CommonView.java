package com.tsanikgr.whist_multiplayer.views.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.tsanikgr.whist_multiplayer.IDialogueListener;
import com.tsanikgr.whist_multiplayer.myactors.Geometry;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;
import com.tsanikgr.whist_multiplayer.myactors.MyLabel;
import com.tsanikgr.whist_multiplayer.myactors.MyTextButton;
import com.tsanikgr.whist_multiplayer.myactors.PointRevealer;
import com.tsanikgr.whist_multiplayer.views.View;

public class CommonView extends View {

	private static final String DIALOGUE = "dialogue";
	private static final String WAITING_SCREEN = "waiting_screen";
	private PointRevealer animator;
	private Dialogue dialogue;
	private LoadingDialogue loadingDialogue;

	public CommonView(String name) {
		super(name);
	}

	@Override
	public void dispose() {
	}

	public void setLoadingDialogueAnimator(PointRevealer animator) {
		this.animator = animator;
	}

	@Override
	protected void onAssetsLoaded(View view) {
		initActors();
	}

	private void initActors(){
		loadingDialogue = new LoadingDialogue((MyGroup)findActor(WAITING_SCREEN));
		fixOriginRecursively(this);
		dialogue = new Dialogue((MyGroup)findActor(DIALOGUE));

		setVisible(true);
		dialogue.setVisible(false);
		loadingDialogue.setVisible(false);
		addActor(loadingDialogue);
		addActor(dialogue);
	}

	@Override
	public Color getBackgroundColor() {
		return new Color(0f, 0f, 0f, 0.5f);
	}

	public View showDialogue(String title, String subtitle, boolean yesNoButton, IDialogueListener listener){
		dialogue.setTitle(title)
				.setSubtitle(subtitle)
				.setDialogueListener(listener)
				.animateShow(yesNoButton);
		return this;
	}

	@Override
	public void setBackground(MyImage prototypeBackground) {
		super.setBackground(prototypeBackground);
		dialogue.setBackground((MyImage) findActor("background"));
	}

	public View hideAnimationDialogue() {
		dialogue.hideAnimation();
		return this;
	}

	public View hideDialogue() {
		dialogue.setVisible(false);
		return this;
	}

	public View showLoadingDialogue(String text, float clickX, float clickY){
		loadingDialogue.show(text);
		if (Math.abs(clickX + 1f) < 0.0001 && Math.abs(clickY + 1f) < 0.0001) return this;

		animator.stopAnimation();

		animator.config()
				.setToReveal(loadingDialogue)
				.set(clickX, clickY);

		animator.startAnimation();
		return this;
	}

	public View hideLoadingDialogue(){
		loadingDialogue.setVisible(false);
		loadingDialogue.onHide();
		return this;
	}

	public void setLoadingDialogueBackDimensions(float w, float h, Vector2 gameAreaPosition) {
		loadingDialogue.updateBackgroundImagePosition(w,h,gameAreaPosition);
	}

	public static class Dialogue extends MyGroup {

		public static final String YES_BUTTON = "yes_button";
		public static final String NO_BUTTON = "no_button";
		public static final String OK_BUTTON = "ok_button";
		public static final int NO = 0;
		public static final int YES = 1;
		public static final int OK = 2;
		MyLabel title, subtitle;
		MyTextButton yesButton, noButton, okButton;
		IDialogueListener dialogueListener;
		private float maxTitleLength;
		private float maxSubtitleLength;
		private float topTitle, topSubtitle;

		Dialogue(MyGroup dialogue){
			super(dialogue);
			initActors();
			dialogueListener = null;
		}

		private void initActors(){
			title = findActor("dialogue_title");
			subtitle = findActor("dialogue_subtitle");
			title.setAlignment(Align.center);
			subtitle.setAlignment(Align.center);
			maxTitleLength = title.getWidth();
			maxSubtitleLength = title.getWidth();
			topTitle = title.getY() + title.getHeight();
			topSubtitle = subtitle.getY() + subtitle.getHeight();

			yesButton = findActor(YES_BUTTON);
			noButton = findActor(NO_BUTTON);
			okButton = new MyTextButton("OK", yesButton.getStyle());
			okButton.setPosition(noButton.getX(), noButton.getY());
			okButton.setSize(noButton.getWidth() * 2f, noButton.getHeight());
			okButton.setName(OK_BUTTON);
			addActor(okButton);
			okButton.setVisible(false);

			final ClickListener listener = new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					int result = -1;
					if (event.getListenerActor() == noButton) result = NO;
					if (event.getListenerActor() == yesButton) result = YES;
					if (event.getListenerActor() == okButton) result = OK;
					if (dialogueListener != null) dialogueListener.onDialogueResult(result);
				}
			};

			yesButton.addListener(listener);
			noButton.addListener(listener);
			okButton.addListener(listener);
		}

		private Dialogue setTitle(String titleText) {
			title.setText(titleText);
			title.pack();
			Geometry.splitLabelToMultiLine(title, maxTitleLength);
			title.setY(topTitle - title.getHeight());
			Geometry.alignActors(Align.center, 0, okButton, title);
			return this;
		}

		private Dialogue setSubtitle(String subtitleText) {
			subtitle.setText(subtitleText);
			subtitle.pack();
			Geometry.splitLabelToMultiLine(subtitle, maxSubtitleLength);
			subtitle.setY(topSubtitle - subtitle.getHeight());
			Geometry.alignActors(Align.center, 0, okButton, subtitle);
			return this;
		}

		private Dialogue setDialogueListener(IDialogueListener listener){
			dialogueListener = listener;
			return this;
		}

		private void setYesNoButtons(){
			okButton.setVisible(false);
			yesButton.setVisible(true);
			noButton.setVisible(true);
		}

		private void setOKButton(){
			okButton.setVisible(true);
			yesButton.setVisible(false);
			noButton.setVisible(false);
		}

		final static float d = 0.5f;
		private void animateShow(boolean yesNoButtons){
			if (yesNoButtons) setYesNoButtons();
			else setOKButton();

			getColor().a = 0.0f;
			setScale(0f);

			addAction(Actions.fadeIn(d, Interpolation.fade));
			addAction(Actions.scaleTo(1f, 1f, d, Interpolation.swingOut));

			setVisible(true);
		}

		private void hideAnimation() {
			addAction(Actions.fadeOut(d, Interpolation.fade));
			addAction(Actions.sequence(Actions.scaleTo(0f, 0f, d, Interpolation.pow2In), Actions.visible(false)));
		}

		private void setBackground(MyImage back) {
			back.setSize(back.getWidth() * 4f, back.getHeight() * 4f);
			Geometry.alignActors(Align.center, Align.center, this, back);
			addActor(back);
			back.setZIndex(0);
		}
	}

	private class LoadingDialogue extends MyGroup{
		public static final String WAITING_SCREEN_TITLE = "waiting_screen_title";
		MyLabel text;
		MyImage[] dots;

		private LoadingDialogue(MyGroup loading) {
			super(loading);
			initActors();
		}

		private void show(String text) {
			this.text.setText(text);
			this.text.pack();
			Geometry.alignActors(Align.center,0,findActor("waiting_screen_back"),this.text);
			setVisible(true);

			for (int i = 0; i < 4; i++) {
				dots[i].clearActions();
				dots[i].setScale(1.0f);
				dots[i].setVisible(false);
			}
			animate();
		}

		private void initActors(){
			this.text = findActor(WAITING_SCREEN_TITLE);
			dots = new MyImage[4];
			for (int i = 0; i < 4; i++) {
				dots[i] = findActor("dots_"+i);
				Geometry.fixOrigin(dots[i]);
			}
		}

		private void animate() {
			for (int i = 0; i < 4; i++) {
				dots[i].clearActions();
				dots[i].setScale(1.0f);
				dots[i].setVisible(false);
				dots[i].addAction(Actions.sequence(Actions.delay(i * 0.1f), Actions.sequence(Actions.visible(true),Actions.repeat(-1, Actions.sequence(Actions.fadeIn(0.4f, Interpolation.fade), Actions.fadeOut(0.4f, Interpolation.fade))))));
				dots[i].addAction(Actions.sequence(Actions.delay(i * 0.1f), Actions.sequence(Actions.visible(true), Actions.repeat(-1, Actions.sequence(Actions.scaleTo(1.5f, 1.5f, 0.4f, Interpolation.fade), Actions.scaleTo(1f, 1f, 0.4f, Interpolation.fade))))));
			}
		}
		private void onHide(){
			for (int i = 0; i < 4; i++) dots[i].clearActions();
		}

		public void updateBackgroundImagePosition(float w, float h, Vector2 gameAreaPosition) {
			MyImage image = findActor("waiting_screen_back");
			image.setSize(w,h);
			image.setBounds(-gameAreaPosition.x, -gameAreaPosition.y, w, h);
		}
	}
}
