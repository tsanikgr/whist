package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.SnapshotArray;
import com.tsanikgr.whist_multiplayer.util.Log;

public class Geometry {

	public static class PlaceHolder {
		public float x, y, w, h;

		public PlaceHolder() {
			x = 0; y = 0; w = 0; h = 0;
		}

		public PlaceHolder(Actor actor) {
			fromActor(actor);
		}

		public void fromActor(Actor actor) {
			x = actor.getX();
			y = actor.getY();
			w = actor.getWidth();
			h = actor.getHeight();
		}

		public void applyTo(Actor actor) {
			actor.setBounds(x,y,w,h);
		}
	}

	private static final Bounds actorBounds = new Bounds();
	private static float groupX, groupY;

	public static class Bounds {
		public float minx, miny, maxx, maxy;
		public Bounds() { reset(); }
		public void reset() { minx = 10000000f; miny = 10000000f; maxx = -10000000f; maxy = -10000000f; groupX = 0f; groupY = 0f;}
		public float width() { return maxx - minx; }
		public float height(){ return maxy - miny; }
	}

	public static void fixOrigin(Actor a, Actor... exclude) {
		Bounds bounds;
		if (a instanceof MyGroup) bounds = sizeFromChildren((MyGroup)a);
		else {
			bounds = new Bounds();
			getBounds(a, bounds, exclude);
		}
		a.setOrigin(bounds.minx + bounds.width()/2f - a.getX(), bounds.miny + bounds.height()/2f - a.getY());
	}

	private static Bounds sizeFromChildren(MyGroup group, Actor... exclude) {
		Bounds bounds = new Bounds();
		getBounds(group, bounds, exclude);
		//move children before moving the group
		float dx = group.getX() - bounds.minx;
		float dy = group.getY() - bounds.miny;
		for (int i = 0; i < group.getChildren().size; i++) group.getChildren().get(i).moveBy(dx,dy);
		group.setBounds(bounds.minx, bounds.miny, bounds.width(), bounds.height());
		return bounds;
	}

	public static void alignActors(int alignX, int alignY, Actor reference, Actor... actorsToAlign) {

		Bounds ref = new Bounds(), toAlign = new Bounds();

		getBounds(reference, ref, actorsToAlign);
		for (Actor actor : actorsToAlign) {
			getBounds(actor, toAlign);

			if ((alignX & Align.right) != 0)
				toAlign.minx = ref.width() - toAlign.width() + ref.minx;
			else if ((alignX & Align.left) != 0)
				toAlign.minx = ref.minx;
			else if ((alignX & Align.center) != 0)
				toAlign.minx = (ref.width() - toAlign.width()) / 2f + ref.minx;

			if ((alignY & Align.top) != 0)
				toAlign.miny = ref.height() - toAlign.height() + ref.miny;
			else if ((alignY & Align.bottom) != 0)
				toAlign.miny = ref.miny;
			else if ((alignY & Align.center) != 0)
				toAlign.miny = (ref.height() - toAlign.height()) / 2f + ref.miny;

			actor.setPosition(toAlign.minx, toAlign.miny);
		}
	}

	public static synchronized void getBounds(Actor actor, Bounds bounds, Actor... exclude) {
		if (actor instanceof MyGroup) {
			actorBounds.reset();
			getGroupBounds((MyGroup) actor, bounds, exclude);
		} else getActorBounds(actor, bounds);
	}

	private static void getActorBounds(Actor actor, Bounds bounds) {
		bounds.minx = actor.getX();
		bounds.miny = actor.getY();
		bounds.maxx = actor.getX() + actor.getWidth();
		bounds.maxy = actor.getY() + actor.getHeight();
	}

	private static void getGroupBounds(MyGroup group, Bounds bounds, Actor... exclude) {
		if (group == null) return;
		SnapshotArray<Actor> localSnapshotArray = group.getChildren();
		Actor localActor;

		groupX += group.getX();
		groupY += group.getY();

		int i = 0;
		boolean found;
		while (i < localSnapshotArray.size) {
			localActor = localSnapshotArray.get(i);
			found =false;
			for (Actor ex : exclude) if (localActor == ex) {
				found = true;
				break;
			}
			if (found) {
				i++;
				continue;
			}

			if (localActor instanceof MyGroup) {
				getGroupBounds((MyGroup) localActor, bounds, exclude);
				groupX -= localActor.getX();
				groupY -= localActor.getY();
			} else {
				getActorBounds(localActor, actorBounds);
				if (actorBounds.minx + groupX < bounds.minx)	bounds.minx = actorBounds.minx + groupX;
				if (actorBounds.miny + groupY < bounds.miny) bounds.miny = actorBounds.miny + groupY;
				if (actorBounds.maxx + groupX > bounds.maxx) bounds.maxx = actorBounds.maxx + groupX;
				if (actorBounds.maxy + groupY > bounds.maxy) bounds.maxy = actorBounds.maxy + groupY;
			}
			i++;
		}
	}

	public static boolean splitLabelToMultiLine(MyLabel label, float maxOneLineLength) {
		if (label.getWidth() <= maxOneLineLength) return false;
		String text = label.getText().toString();
		String oneLineTemp;
		int lastSpacePosition = text.length() + 1;

		do {
			lastSpacePosition = text.lastIndexOf(" ", lastSpacePosition-1);
			oneLineTemp = text.substring(0,lastSpacePosition);
			label.setText(oneLineTemp);
			label.pack();
		} while (lastSpacePosition != -1 && label.getWidth() > maxOneLineLength);

		if (lastSpacePosition == -1 || lastSpacePosition == text.length()) {
			new Log(null).w().append("No spaces found - cannot split label to multiple lines").print();
			return false;
		}

		label.setText(oneLineTemp + "\n" + text.subSequence(lastSpacePosition + 1, text.length()));
		label.pack();
		return true;
	}
}
