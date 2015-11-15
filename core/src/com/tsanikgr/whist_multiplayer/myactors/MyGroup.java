package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class MyGroup extends Group implements MaskInterface {

	private Mask mask = null;

	protected MyGroup(Group group) {

		if (group == null) return;
		while (group.getChildren().size != 0) addActor(group.getChildren().get(0));

		setName(group.getName());
		setPosition(group.getX(), group.getY());
		setSize(group.getWidth(), group.getHeight());
		setScale(group.getScaleX(), group.getScaleY());
		setColor(group.getColor());
		setCullingArea(group.getCullingArea());
		setOrigin(group.getOriginX(), group.getOriginY());
		setRotation(group.getRotation());
		setTransform(group.isTransform());
//		setZIndex(group.getZIndex());
		setUserObject(group.getUserObject());

		group.remove();
		group.setVisible(false);
	}

	public MyGroup() {
		super();
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (mask != null) mask.startMasking(batch);
		super.draw(batch, parentAlpha);
		if (mask != null) mask.stopMasking(batch);
	}

	@Override
	public void setMask(Mask mask){
		this.mask = mask;
	}

	public void nudgeAnimation() {
		nudgeAnimation(this);
	}

	private void nudgeAnimation(Group group) {
		if (group == null) return;
		for (Actor actor : group.getChildren()) {
			if (actor == null) continue;
			if (actor instanceof Group) nudgeAnimation((Group)actor);
			else {
				Action nudge = Actions.repeat(3,
						Actions.sequence(
								Actions.rotateBy(4f, 0.03f, Interpolation.sine),
								Actions.rotateBy(-8f, 0.06f, Interpolation.sine),
								Actions.rotateBy(4f, 0.03f, Interpolation.sine)));
				actor.addAction(nudge);
			}
		}
	}

//	public void setSizeFromChildren(){
//		Geometry.Bounds bounds = new Geometry.Bounds();
//		Geometry.getBoundsFromChildren(this, bounds);
//		setSize(bounds.maxx-bounds.minx, bounds.maxy-bounds.miny);
//	}

	//	public void show(AbstractScreen screen) {
//		this.screen = screen;
//		setVisible(true);
//		if (getStage() == null) {
//			Log log = new Log(((Object)this).getClass().getSimpleName());
//			log.w("actor " + getName() + " has no stage, assigning current", "show");
//			screen.getRoot().addActor(this);
//		}
////		getColor().a = 0;
////		addAction(fadeIn(0.3f));
//	}

//	public void hide() {
//		addAction(sequence(fadeOut(0.3f), run(new Runnable() {
//			@Override
//			public void run() {
//				setVisible(false);
//				remove();
//				screen = null;
//			}
//		})));
//	}
}
