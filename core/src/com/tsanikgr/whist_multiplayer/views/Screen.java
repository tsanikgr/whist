package com.tsanikgr.whist_multiplayer.views;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.tsanikgr.whist_multiplayer.IScreenController;

public abstract class Screen extends View {

	protected final IScreenController controller;

	protected Screen(IScreenController controller, String xmlFileAndGroupName) {
		this(controller, xmlFileAndGroupName, xmlFileAndGroupName);
	}

	private Screen(IScreenController controller, String xmlFile, String name) {
		super(xmlFile, name);
		this.controller = controller;
	}

	@Override
	public void dispose(){
		for (int i = 0; i < getChildren().size; i++) {
			if (getChildren().get(i) instanceof View) ((View) getChildren().get(i)).dispose();
		}
	}

	@Override
	protected void onAssetsLoaded(View view) {
		areAllViewsReady();
	}

	public void buildViews() {
		if (isReady() || !compareAndSetBuilding()) return;
		setReady(true);   //screens are immediately ready, irrespective of whether their views are done building
		buildViewsProtected();
	}

	abstract protected void buildViewsProtected();

	protected <T extends View> T getView(String name, Class<T> klass) {
		return (T)getView(name);
	}

	protected <T extends View> T getView(String name) {
		T view = findActor(name);
		if (view != null && !view.isReady()) buildViewSync(name);
		return view;
	}

	protected void buildViewAsync(String viewName){
		View view = findActor(viewName);
		if (view == null) {
//			throw new NullPointerException("Need to call Screen.addActor(View) for [" + viewName + "] before trying to access it");
			return;
		}
		if (view.isReady() || !view.compareAndSetBuilding()) return;
		controller.buildViewAsync(view.xmlFile, viewName, view);
	}

	protected <T extends View> T buildViewSync(String viewName) {
		View view = findActor(viewName);
		if (view == null) throw new NullPointerException("Need to call Screen.addActor(View) for [" + viewName + "] before trying to access it");

		if (view.isReady()) return (T)view;

		if (view.compareAndSetBuilding()) {
			controller.buildViewSync(view.xmlFile, viewName, view);
		} else {
			String timerId = controller.getPerformance().tickNew();
			if (controller.isRenderThread()) {
				log.w().append("!!!!!! BLOCKING RENDER THREAD: Waiting for [").append(viewName).append("] to finish building.").print();
				log.w().append("!!!!!! Executing any UI thread runnables").print();
				while (view.isBuilding()) {
					controller.updateAssetManager();
					controller.executePendingUiThreadRunnables();
				}
			} else {
				log.w().append("!!!!!! BLOCKING BACKGROUND THREAD: Waiting for [").append(viewName).append("] to finish building.").print();
				while (view.isBuilding()) {
					controller.updateAssetManager();
				}
			}
			log.w().append("!!!!!!! CONTINUING: Blocked for ").append(controller.getPerformance().stop(timerId)).print();
		}
		return (T)view;
	}

	protected final boolean isViewReady(String view) {
		Actor a;
		for (int i = 0; i < getChildren().size; i++) {
			a = getChildren().get(i);
			if (!(a instanceof View)) continue;
			if (a.getName().compareTo(view) == 0 && ((View)a).isReady()) return true;
		}
		return false;
	}

	private boolean areAllViewsReady() {
		Actor view;
		for (int i = 0; i < getChildren().size; i++) {
			view = getChildren().get(i);
			if (!(view instanceof View)) continue;
			if (!((View)view).isReady()) {
				return false;
			}
		}
		finishedBuilding();
		return true;
	}
}
