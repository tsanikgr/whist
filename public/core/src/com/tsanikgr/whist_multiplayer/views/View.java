package com.tsanikgr.whist_multiplayer.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.SnapshotArray;
import com.tsanikgr.whist_multiplayer.myactors.Geometry;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class View extends MyGroup {
	final protected Log log = new Log(this);

	final String xmlFile;
	private boolean ready = false;
	private final AtomicBoolean building;

	public View(String xmlFileAndName) {
		this(xmlFileAndName, xmlFileAndName);
	}

	protected View(String xmlFile, String name) {
		setName(name);
		this.xmlFile = xmlFile;
		building = new AtomicBoolean(false);
	}

	public abstract void dispose();

	public boolean isReady(){
		return ready;
	}

	final public void onAssetsLoadedFinal(View view){
		log.i().append("VIEW [").append(view.getName()).append("] loaded").print();
		onAssetsLoaded(view);

		if (this == view) {
			setReady(true);
			finishedBuilding();
		}
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (!isReady()) return;
		super.draw(batch, parentAlpha);
	}

	@Override
	public void act(float delta) {
		if (!isReady()) return;
		super.act(delta);
	}

	protected abstract void onAssetsLoaded(View view);

	protected abstract Color getBackgroundColor();

	public void setBackground(MyImage prototypeBackground) {
		if (getBackgroundColor() == null) return;
		MyImage back = findActor("background");
		if (back != null) {
			back.setColor(getBackgroundColor());
			back.setZIndex(0);
			return;
		}
		back = new MyImage(prototypeBackground.getDrawable());
		back.setName("background");
		back.setPosition(prototypeBackground.getX(), prototypeBackground.getY());
		back.setSize(prototypeBackground.getWidth(), prototypeBackground.getHeight());
		back.setColor(getBackgroundColor());
		addActor(back);
		back.setZIndex(0);
	}

	protected void fixOriginRecursively(Group group) {
		if (group == null) return;
		SnapshotArray<Actor> localSnapshotArray = group.getChildren();
		for(Actor actor : localSnapshotArray) {
			if (actor instanceof Group) {
				fixOriginRecursively((Group) actor);
				Geometry.fixOrigin(actor);
			} else Geometry.fixOrigin(actor);
		}
	}

	void setReady(boolean ready) {
		this.ready = ready;
	}

	boolean compareAndSetBuilding() {
		boolean success = this.building.compareAndSet(false, true);
		if (!success) return false;      //already building
		if (isReady()) {                 //view is ready -> another thread built it in the mean-time
			this.building.set(false);
			return false;
		} else return true;
	}

	void finishedBuilding(){
		building.set(false);
	}

	boolean isBuilding() {
		return building.get();
	}

}
