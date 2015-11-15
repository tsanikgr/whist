package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

public abstract class CompositeController extends Controller{

	private final Array<Controller> controllers;

	CompositeController() {
		super();
		controllers = new Array<>();
	}

	Array<Controller> getChildren() {
		return controllers;
	}

	void addController(Controller controller){
		if (controllers.contains(controller, true)) {
			log.w().append("+C+ Controller [").append(controller.getClass().getSimpleName()).append("] is already a child.").print();
			return;
		}
		controllers.add(controller);
		controller.parent = this;
	}

	void removeController(Controller controller) {
		controllers.removeValue(controller, true);
		controller.parent = null;
	}

	void clearControllers() {
		controllers.clear();
	}

	@Override
	protected void update(float dt){
		if (!Gdx.graphics.isContinuousRendering() && dt > 0.1f) dt = 0f;
		for (int i = 0; i < controllers.size; i++) {
			if (!controllers.get(i).active) continue;
			controllers.get(i).update(dt);
		}
	}

	@Override
	protected void init(){
		for (int i = 0; i < controllers.size; i++) controllers.get(i).init();
	}

	@Override
	protected void disposeController() {
		for (int i = 0; i < controllers.size; i++) controllers.get(i).disposeController();
		controllers.clear();
	}
}
