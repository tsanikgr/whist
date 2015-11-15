package com.tsanikgr.whist_multiplayer.views.adaptors;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;

class DragAdaptor implements EventListener {

	private DragListener dragListener = null;
	private ActorGestureListener flingListener = null;

	public DragAdaptor(final Actor actor, final float minX, final float maxX, final float minY, final float maxY, boolean enableFling) {

		dragListener = new DragListener() {

			float startClickX;
			float startClickY;
			float startImageX;
			float startImageY;

			@Override
			public void dragStart(InputEvent event, float x, float y, int pointer) {
				super.dragStart(event, x, y, pointer);
				startClickX = event.getStageX();
				startClickY = event.getStageY();
				startImageX = actor.getX() + actor.getWidth()/2f;
				startImageY = actor.getY() + actor.getHeight()/2f;
			}

			@Override
			public void drag(InputEvent event, float x, float y, int pointer) {
				super.drag(event, x, y, pointer);
				float dx = event.getStageX() - startClickX;
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
				if (startImageX + dx  - actor.getWidth()/2 < minX) {
					dx = 0;
					startClickX = event.getStageX();
					startImageX = minX + actor.getWidth()/2f;
				} else if (startImageX + dx  - actor.getWidth()/2 > maxX) {
					dx = 0;
					startClickX = event.getStageX();
					startImageX = maxX + actor.getWidth()/2f;
				}
				actor.addAction(moveTo(startImageX + dx - actor.getWidth()/2, startImageY + dy - actor.getHeight()/2, 0.0f));
			}
		};

		if (enableFling) {
			flingListener = new ActorGestureListener() {

				@Override
				public void fling(InputEvent event, float velocityX, float velocityY, int button) {
					super.fling(event, velocityX, velocityY, button);
					float a = 0.5f;
					if (velocityX * a < 20f) velocityX = 0f;
					if (velocityY * a < 20f) velocityY = 0f;

					Interpolation interp = Interpolation.exp5Out;
					float moveToX = actor.getX() + velocityX * a;
					float moveToY = actor.getY() + velocityY * a;

					if (moveToY < minY) moveToY = minY;
					else if (moveToY > maxY) moveToY = maxY;
					if (moveToX < minX) moveToX = minX;
					else if (moveToX > maxX) moveToX = maxX;

					actor.addAction(moveTo(moveToX, moveToY, 0.5f, interp));
				}
			};
		}
	}

	@Override
	public boolean handle(Event event) {
		boolean ret = false;
		if (dragListener != null) ret = dragListener.handle(event);
		if (flingListener != null) ret = flingListener.handle(event) || ret;
		return ret;
	}
}
