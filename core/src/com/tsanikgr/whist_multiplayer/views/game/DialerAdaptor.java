package com.tsanikgr.whist_multiplayer.views.game;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;

class DialerAdaptor implements EventListener {

	private final Actor dialer;
	private DragListener dragListener = null;
	private ActorGestureListener flingListener = null;
	private boolean flinged = false;
	private int maxBausen = -1;
	private final float dy;

	public DialerAdaptor(final Actor actor, final float minY, final float maxY, final int steps) {

		dialer = actor;
		dy = dialer.getHeight()/(float)steps;

		dragListener = new DragListener() {

			float startClickY;
			float startImageY;

			@Override
			public void dragStart(InputEvent event, float x, float y, int pointer) {
				super.dragStart(event, x, y, pointer);
				startClickY = event.getStageY();
				startImageY = dialer.getY() + dialer.getHeight()/2f;
			}

			@Override
			public void drag(InputEvent event, float x, float y, int pointer) {
				super.drag(event, x, y, pointer);
				float dy = event.getStageY() - startClickY;

				float numberHeight = (maxY - minY)/steps;

				if (startImageY + dy - dialer.getHeight()/2 < minY + (maxBausen != -1 ? numberHeight*(steps-maxBausen) : 0)) {
					dy = 0;
					startClickY = event.getStageY();
					startImageY = minY + (maxBausen != -1 ? numberHeight*(steps-maxBausen) : 0) + dialer.getHeight()/2f;
				} else if (startImageY + dy - dialer.getHeight()/2 > maxY) {
					dy = 0;
					startClickY = event.getStageY();
					startImageY = maxY + dialer.getHeight()/2f;
				}
				dialer.addAction(moveTo(dialer.getX(), startImageY + dy - dialer.getHeight()/2, 0.0f));
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);

				if (flinged) {
					flinged = false;
					return;
				}
				int nearest = Math.round((dialer.getY() - maxY)/dy);
				if (nearest < -maxBausen ) nearest = -maxBausen;
				else if (nearest > 0) nearest = 0;
				dialer.addAction(moveTo(dialer.getX(), nearest*dy+maxY, 0.1f, Interpolation.swingOut));
			}

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				dialer.clearActions();
				return super.touchDown(event, x, y, pointer, button);
			}
		};

		flingListener = new ActorGestureListener() {
			@Override
			public void fling(InputEvent event, float velocityX, float velocityY, int button) {
				super.fling(event, velocityX, velocityY, button);

				float a = 1f/8f;
				float moveToY = dialer.getY() + velocityY * a;

				int nearest = Math.round((moveToY - maxY)/dy);
				if (nearest < -maxBausen ) nearest = -maxBausen;
				else if (nearest > 0) nearest = 0;

				float duration;
				duration = 0.5f;
				dialer.addAction(moveTo(dialer.getX(), nearest*dy+maxY, duration, Interpolation.swingOut));

				flinged = true;
			}
		};
	}

	public void init(int maxBausen) {
		this.maxBausen = maxBausen;

//		float half = (float)Math.floor(maxBausen/3f);
//		float duration = Math.min(Math.abs(100f/half/dy),0.5f);
//		dialer.addAction(moveTo(dialer.getX(), -half * dy + maxY, duration, Interpolation.swingOut));
	}

	@Override
	public boolean handle(Event event) {
		boolean ret = false;
		if (dragListener != null) ret = dragListener.handle(event);
		if (flingListener != null) ret = flingListener.handle(event) || ret;
		return ret;
	}
}
