package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.tsanikgr.whist_multiplayer.util.Log;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public abstract class Animator{
	private final Log log = new Log(this);

	boolean animationRunning = false;
	boolean animationCompleted = false;
	final Vector2 gameAreaPosition;
	AnimatorParams params = null;
	AnimatorParams newParams = null;

	Animator(Vector2 gameAreaPosition) {
		this.gameAreaPosition = gameAreaPosition;
	}

	public interface AnimatorFinishedListener{
		void onAnimationFinished(boolean completedSuccesfully);
	}

	public class AnimatorParams {
		AnimatorFinishedListener listener = null;
		boolean moveToFront = false;
		float clickX = 0;
		float clickY = 0;

		public AnimatorParams(){}
		public AnimatorParams setListener(AnimatorFinishedListener listener) {
			this.listener = listener;
			return this;
		}

		public AnimatorParams setMoveToFront(boolean moveToFront) {
			this.moveToFront = moveToFront;
			return this;
		}

		public AnimatorParams set(float clickX, float clickY) {
			if (Math.abs(clickX + 1f) > 0.000001) this.clickX = clickX;
			if (Math.abs(clickY + 1f) > 0.000001) this.clickY = clickY;
			return this;
		}

		AnimatorParams setFrom(AnimatorParams prototype) {
			listener = prototype.listener;
			moveToFront = prototype.moveToFront;
			clickX = prototype.clickX;
			clickY = prototype.clickY;
			return this;
		}
	}

	abstract protected void onStart();
	abstract protected void onStop();

	public void startAnimation(){
		if (isRunning()) {
			log.i().append("animator already running - stopping it before new one").print();
			stopAnimation();
		} else log.i().append("animation ").append(this.getClass().getSimpleName()).append(" started").print();

		if (newParams != null) params.setFrom(newParams);
		animationCompleted = false;
		animationRunning = true;

		Gdx.graphics.requestRendering();
		onStart();
	}

	public void stopAnimation() {
		if (!isRunning()) return;
		log.i().append("animation completed ").append(animationCompleted ? "succesfully" : "FAILED").print();

		onStop();
		if (params.listener != null) params.listener.onAnimationFinished(animationCompleted);
		animationRunning = false;
		animationCompleted = false;
	}

	Action lastAction(Action action) {
		if (action == null) return null;
		return sequence(action, run(new Runnable() {
			@Override
			public void run() {
				animationCompleted = true;
				stopAnimation();
			}
		}));
	}

	boolean isRunning() {
		return animationRunning;
	}

	private void onTouchDragged(float stageX, float stageY) {}
	void onTouchUp(float stageX, float stageY) {}
	void onTouchDown(float stageX, float stageY) {}
	private void onClicked(float stageX, float stageY) {}

	public static void animateOnClick(Actor clickActor, final Animator animator) {
		clickActor.addListener(new  ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				animator.onClicked(event.getStageX(), event.getStageY());
			}

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				animator.onTouchDown(event.getStageX(), event.getStageY());
				return super.touchDown(event, x, y, pointer, button);
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				animator.onTouchUp(event.getStageX(), event.getStageY());
			}

			@Override
			public void touchDragged(InputEvent event, float x, float y, int pointer) {
				super.touchDragged(event, x, y, pointer);
				animator.onTouchDragged(event.getStageX(), event.getStageY());
			}
		});
	}
}
