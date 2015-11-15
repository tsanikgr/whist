package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.tsanikgr.whist_multiplayer.IDialogueListener;
import com.tsanikgr.whist_multiplayer.IScreenController;
import com.tsanikgr.whist_multiplayer.IStageBuilderListener;
import com.tsanikgr.whist_multiplayer.myactors.Geometry;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.views.View;

import static com.badlogic.gdx.utils.Align.bottom;
import static com.badlogic.gdx.utils.Align.left;
import static com.badlogic.gdx.utils.Align.right;
import static com.badlogic.gdx.utils.Align.top;

public abstract class ScreenController extends CompositeController implements IScreenController {

	final private Array<EventListener> eventListeners;
	boolean showingDialogue = false;
	boolean showingLoadingDialogue = false;

	ScreenController() {
		eventListeners = new Array<>();
	}

	protected abstract void onAssetsLoaded(View view);

	@Override
	public void buildViewAsync(String name, String groupName, MyGroup view){
		getScreenDirector().buildGroupAsync(name, groupName, view, new IStageBuilderListener() {
			@Override
			public void onGroupBuilded(String fileName, MyGroup view) {
				ScreenController.this.onViewBuilded((View) view);
			}
			@Override
			public void onGroupBuildFailed(String fileName, Exception e) {
				log.e(e).append("Menu screen building failed").print();
			}
		});
	}

	@Override
	public void buildViewSync(String xmlFile, String groupName, MyGroup view) {
		if (view == null) throw new RuntimeException("Need to specify a group to fill for [" + groupName);
		getScreenDirector().buildGroup(xmlFile,groupName, view);
		onViewBuilded((View)view);
	}

	private void onViewBuilded(View view) {

		view.onAssetsLoadedFinal(view);
		Actor g = view.getParent();
		while(g instanceof View) {
			((View) g).onAssetsLoadedFinal(view);
			g = g.getParent();
		}

		onAssetsLoaded(view);
		log.i().append("CONTROLLER: view [").append(view.getName()).append("] loaded").print();
	}

	@Override
	public boolean handle(Event event) {
		if (!active) return false;
		boolean handled = false;
		for (int i = 0 ; i < eventListeners.size ; i ++) {
			if (eventListeners.get(i).handle(event)) handled = true;
		}
		return handled;
	}

	void addEventListener(EventListener eventListener) {
		if (eventListeners.contains(eventListener,true)) return;
		eventListeners.add(eventListener);
	}

	protected boolean removeEventListener(EventListener eventListener) {
		return eventListeners.removeValue(eventListener,true);
	}

	void clearEventListeners(){
		eventListeners.clear();
	}

	@Override
	public void alignToScreen(int alignX, int alignY, Actor... actorsToAlign){
		Geometry.Bounds ref = new Geometry.Bounds(), toAlign = new Geometry.Bounds();

		ref.minx = getResolution().getGameAreaPosition().x;
		ref.miny = getResolution().getGameAreaPosition().y;
		ref.maxx = ref.minx +  getResolution().getGameAreaBounds().x;
		ref.maxy = ref.miny +  getResolution().getGameAreaBounds().y;

		for (Actor actor : actorsToAlign) {
			Geometry.getBounds(actor, toAlign);

			if ((alignX & right) != 0)
				toAlign.minx = ref.width() - toAlign.width() + ref.minx;
			else if ((alignX & left) != 0)
				toAlign.minx = ref.minx;
			else if ((alignX & Align.center) != 0)
				toAlign.minx = (ref.width() - toAlign.width()) / 2f + ref.minx;

			if ((alignY & top) != 0)
				toAlign.miny = ref.height() - toAlign.height() + ref.miny;
			else if ((alignY & bottom) != 0)
				toAlign.miny = ref.miny;
			else if ((alignY & Align.center) != 0)
				toAlign.miny = (ref.height() - toAlign.height()) / 2f + ref.miny;

			actor.setPosition(toAlign.minx, toAlign.miny);
		}
	}

	@Override
	public void addButtonListenersRecursively(Group group) {
		if (group == null) return;
		removeButtonListenersRecursively(group);

		SnapshotArray<Actor> localSnapshotArray = group.getChildren();
		for(Actor actor : localSnapshotArray) {
			if ((actor instanceof TextButton) || (actor instanceof Button)) actor.addListener(this);
			else if (actor instanceof Group) addButtonListenersRecursively((Group) actor);
		}
	}

	@Override
	public void removeButtonListenersRecursively(Group group) {
		if (group == null) return;
		SnapshotArray<Actor> localSnapshotArray = group.getChildren();
		for(Actor actor : localSnapshotArray) {
			if ((actor instanceof TextButton) || (actor instanceof Button)) {
				for (int el = actor.getListeners().size-1 ; el >= 0 ; el--)
					if (actor.getListeners().get(el) instanceof ScreenController) actor.removeListener(actor.getListeners().get(el));
			} else if (actor instanceof Group) addButtonListenersRecursively((Group) actor);
		}
	}

	@Override
	public void showLoadingDialogue(String text, float clickX, float clickY){
		if (getMenuController() != null) {
			getMenuController().showLoadingDialogue(text, clickX, clickY);
		}
	}

	@Override
	public void hideLoadingDialogue() {
		if (getMenuController() != null) {
			getMenuController().hideLoadingDialogue();
		}
	}

	@Override
	public void hideAnimationDialogue(){
		if (getMenuController() != null) {
			getMenuController().hideAnimationDialogue();
		}
	}

	@Override
	public void showDialogue(String title, String subtitle, boolean hasYesNoButtons) {
		if (getMenuController() != null) {
			getMenuController().showDialogue(title, subtitle, hasYesNoButtons);
		}
	}

	@Override
	public void showDialogue(String title, String subtitle, boolean hasYesNoButtons, IDialogueListener listener) {
		if (getMenuController() != null) {
			getMenuController().showDialogue(title, subtitle, hasYesNoButtons, listener);
		}
	}

	@Override
	public void hideDialogue() {
		if (getMenuController() != null) {
			getMenuController().hideDialogue();
		}
	}

	@Override
	public boolean isShowingDialogue() {
		return getMenuController().isShowingDialogue();
	}

	@Override
	public boolean isShowingloadingDialogue() {
		return getMenuController().isShowingloadingDialogue();
	}

	@Override
	public IScreenController getActiveScreenController(){
		return getScreenDirector().getActiveScreenController();
	}

	@Override
	public void executePendingUiThreadRunnables() {
		getOSinterface().executePendingUiThreadRunnables();
	}

	@Override
	public void updateAssetManager() {
		getAssets().getAssetManager().update();
	}
}
