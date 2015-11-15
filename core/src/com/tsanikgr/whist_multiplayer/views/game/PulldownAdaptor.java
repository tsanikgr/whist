package com.tsanikgr.whist_multiplayer.views.game;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;

class PulldownAdaptor implements EventListener {

	private static final float MOVE_ON_TOUCH = 25f;
	private DragListener dragListener = null;
	private ActorGestureListener flingListener = null;
	private boolean flinged = false;

	private final Interpolation interpolation = new Interpolation.ElasticOut(3f, 4, 5, 0.2f);

	public PulldownAdaptor(final Actor actor, final float minY, final float maxY) {

		dragListener = new DragListener() {

			float startClickY;
			float startImageY;

			@Override
			public void dragStart(InputEvent event, float x, float y, int pointer) {
				super.dragStart(event, x, y, pointer);
				startClickY = event.getStageY();
				startImageY = actor.getY() + actor.getHeight()/2f;
			}

			@Override
			public void drag(InputEvent event, float x, float y, int pointer) {
				super.drag(event, x, y, pointer);
				float dy = event.getStageY() - startClickY;
				if (startImageY + dy - actor.getHeight()/2 < minY) {
					dy = 0;
					startClickY = event.getStageY();
					startImageY = minY + actor.getHeight()/2f;
				} else if (startImageY + dy - actor.getHeight()/2 > maxY) {
					dy = 0;
					startClickY = event.getStageY();
					startImageY = maxY + actor.getHeight()/2f;
				}
				actor.addAction(moveTo(actor.getX(), startImageY + dy - actor.getHeight()/2, 0.0f));
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);

				if (flinged) {
					flinged = false;
					return;
				}
				float threshold = (maxY-minY)*2f/3f;

				if (actor.getY() < threshold + minY) ((PullDownInterface)actor).pullDown();
				else ((PullDownInterface)actor).pullUp();
			}
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				actor.clearActions();
				if (Math.abs(actor.getX() - maxY) < 0.0001) actor.addAction(Actions.moveBy(0f, -MOVE_ON_TOUCH,0.5f,Interpolation.swingOut));
				return super.touchDown(event, x, y, pointer, button);
			}
		};

		flingListener = new ActorGestureListener() {

			@Override
			public void fling(InputEvent event, float velocityX, float velocityY, int button) {
				super.fling(event, velocityX, velocityY, button);

				float a = 1f/2f;
				float moveToY = actor.getY() + velocityY * a;

				float threshold = (maxY-minY)*2f/3f;

				if (moveToY < threshold + minY) actor.addAction(moveTo(actor.getX(), minY, 2.5f, interpolation));
				else ((PullDownInterface)actor).pullUp();
				flinged = true;
			}
		};
	}

	@Override
	public boolean handle(Event event) {
		boolean ret = false;
		if (dragListener != null) ret = dragListener.handle(event);
		if (flingListener != null) ret = flingListener.handle(event) || ret;
		return ret;
	}
}
