package com.tsanikgr.whist_multiplayer.views.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.tsanikgr.whist_multiplayer.IDialogueListener;
import com.tsanikgr.whist_multiplayer.IMenuController;
import com.tsanikgr.whist_multiplayer.IResolution;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.models.SettingsModel;
import com.tsanikgr.whist_multiplayer.myactors.Animator;
import com.tsanikgr.whist_multiplayer.myactors.AutoRevealer;
import com.tsanikgr.whist_multiplayer.myactors.Mask;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;
import com.tsanikgr.whist_multiplayer.myactors.PointRevealer;
import com.tsanikgr.whist_multiplayer.views.Screen;
import com.tsanikgr.whist_multiplayer.views.View;

public class MenuScreen extends Screen {

	public static final String BACK = "back";
	public static final String MAIN_MENU = "main_menu";
	public static final String MAIN_MENU_VIEW = "main_menu_layer";
	public static final String NEW_GAME_VIEW = "new_game_layer";
	public static final String SETTINGS_VIEW = "settings";
	public static final String STATISTICS_VIEW = "statistics";
	public static final String COINS_VIEW = "coins";
	public static final String COMMON_VIEW = "common";
	public static final String USER_VIEW = "player";
	private static final String ACHIEVEMENTS_VIEW = "achievements_layer";
	private static final String LEADERBOARDS_VIEW = "leaderboards_layer";

	public static final String LOGOUT = "logout";
	private static final String BACKGROUND = "background";
	private static final String REVEAL_MASK_HIDE = "reveal_mask_hide";
	private static final String REVEAL_MASK = "reveal_mask";

	private MyImage splash = null;
	private MyImage backgroundImage = null;
	private Mask mask, maskReveal;
	private PointRevealer layersShowAnimator;
	private PointRevealer layersHideAnimator;

	public MenuScreen(IMenuController controller) {
		super(controller, MAIN_MENU);
		addActor(new MainMenuView(MAIN_MENU, MAIN_MENU_VIEW));
		addActor(new NewGameView(NEW_GAME_VIEW));
		addActor(new CoinsView(COINS_VIEW));
		addActor(new StatisticsView(STATISTICS_VIEW));
		addActor(new SettingsView(SETTINGS_VIEW));
		addActor(new AchievementsView(ACHIEVEMENTS_VIEW));
		addActor(new LeaderBoardsView(LEADERBOARDS_VIEW));
		addActor(new UserView(MAIN_MENU, USER_VIEW));
		addActor(new CommonView(COMMON_VIEW));
		for (int i = 0; i < getChildren().size; i++) getChildren().get(i).setVisible(false);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected void buildViewsProtected() {
		buildViewSync(MAIN_MENU_VIEW);  //needs sync to make sure revealers are ready

		buildViewAsync(USER_VIEW);
//		buildViewAsync(COMMON_VIEW);
//		buildViewAsync(NEW_GAME_VIEW);
//		buildViewAsync(COINS_VIEW);
//		buildViewAsync(STATISTICS_VIEW);
//		buildViewAsync(SETTINGS_VIEW);
//		buildViewAsync(ACHIEVEMENTS_VIEW);
//		buildViewAsync(LEADERBOARDS_VIEW);

		initAnimators();
	}

	private void showWithAnimation(final View view, float stageX, float stageY) {

		view.setTouchable(Touchable.enabled);

		layersShowAnimator.stopAnimation();
		layersHideAnimator.stopAnimation();
		layersShowAnimator.config()
				.setToReveal(view)
				.set(stageX, stageY);
		layersHideAnimator.config().set(stageX, stageY);
		layersShowAnimator.startAnimation();

		getView(MAIN_MENU_VIEW).toBack();
		getView(USER_VIEW).toFront();
		if (isViewReady(COMMON_VIEW)) getView(COMMON_VIEW).toFront();
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}

	private void hideWithAnimation(final View view, float stageX, float stageY) {

		if (!view.isVisible()) return;

		restoreMainMenuIfRequired(view);
		view.setTouchable(Touchable.disabled);

		layersShowAnimator.stopAnimation();
		layersHideAnimator.stopAnimation();
		layersHideAnimator.config()
				.setToReveal(view)
				.set(stageX, stageY)
				.setListener(new Animator.AnimatorFinishedListener() {
					@Override
					public void onAnimationFinished(boolean completed) {
						view.setVisible(false);
					}
				});
		layersHideAnimator.startAnimation();


		getView(USER_VIEW).toFront();
		View common = getView(COMMON_VIEW);
		if (common != null) common.toFront();
	}

	private void restoreMainMenuIfRequired(View toHide) {
		boolean found = false;
		Actor actor;
		for (int i = getChildren().size-1 ; i >= 0; i--) {
			actor = getChildren().get(i);
			if (!(actor instanceof View)) continue;
			if (actor.getName().compareTo(USER_VIEW) == 0) continue;
			if (actor.getName().compareTo(COMMON_VIEW) == 0) continue;
			if (actor == toHide) continue;
			if (!actor.isVisible()) continue;
			found = true;
			break;
		}
		if (!found) {
			getView(MAIN_MENU_VIEW).setVisible(true);
			getView(MAIN_MENU_VIEW).toBack();
		}
	}

	@Override
	protected void onAssetsLoaded(View view) {
		view.setVisible(false);
		if (view.getName().compareTo(MAIN_MENU_VIEW) == 0) initMainMenuView((MainMenuView) view);

		view.setBackground(backgroundImage);
		if (view.getName().compareTo(COMMON_VIEW) == 0) initCommonView((CommonView) view);
		if (view.getName().compareTo(NEW_GAME_VIEW) == 0) ((NewGameView)view).setEventListener(controller);
		super.onAssetsLoaded(view);
	}

	private void initMainMenuView(MainMenuView mainMenuView) {
		maskReveal = new Mask(mainMenuView.findActor(REVEAL_MASK_HIDE), true, controller.getResolution().getGameAreaPosition());
		mask = new Mask(mainMenuView.findActor(REVEAL_MASK), true, controller.getResolution().getGameAreaPosition());
		backgroundImage = mainMenuView.findActor(BACKGROUND);
		backgroundImage.remove();

		mainMenuView.setStarMask(controller.getResolution());
	}

	public void enableMultiplayer(boolean enable) {
		getView(MAIN_MENU_VIEW, MainMenuView.class).enableMultiplayer(enable);
	}

	private PointRevealer getLoadingDialogueAnimator() {
		PointRevealer animator = new PointRevealer(controller.getResolution().getGameAreaPosition());
		animator.config()
				.setMask(mask)
				.setDuration(1.2f)
				.setInitScale(0.05f)
				.setFinalScale(2.5f)
				.setInterpolation(Interpolation.pow2Out)
				.setMoveToFront(true)
				.setListener(new Animator.AnimatorFinishedListener() {
					@Override
					public void onAnimationFinished(boolean completed) {
					}
				});
		return animator;
	}

	private void initCommonView(CommonView view) {
		view.setLoadingDialogueAnimator(getLoadingDialogueAnimator());
		IResolution resolution = controller.getResolution();
		float w, h;
		w = resolution.getScreenWidth();
		h = resolution.getScreenHeight();
		view.setLoadingDialogueBackDimensions(w, h, resolution.getGameAreaPosition());
		view.setVisible(true);
	}

	@Override
	protected Color getBackgroundColor() {
		return null;
	}

	public void goBack(float stageX, float stageY) {
		View currentView = getTopView(true);
		if (currentView == null) return;
		hideWithAnimation(currentView, stageX, stageY);
	}

	public void show(String view, float stageX, float stageY) {
		showWithAnimation(getView(view), stageX, stageY);
	}

	public boolean isOnTop(String view, boolean exculdeUser) {
		return getView(view) == getTopView(exculdeUser);
	}

	private View getTopView(boolean excludeUser) {
		Actor view;
		for (int i = getChildren().size - 1; i >= 0 ; i--) {
			view = getChildren().get(i);
			if (!view.isVisible()) continue;
			if (!(view instanceof View)) continue;
			if (view.getName().compareTo(COMMON_VIEW) == 0) continue;
			if (excludeUser && view.getName().compareTo(USER_VIEW) == 0) continue;
			return (View)view;
		}
		return null;
	}

	public void askForNickname() {
		getView(USER_VIEW, UserView.class).askForNickname();
	}

	public void chooseMode(boolean isLocalGame) {
		getView(NEW_GAME_VIEW, NewGameView.class).chooseMode(isLocalGame);
	}

	public void setRoomConfigFromUI(GameModel.WhistRoomConfig roomConfig) {
		getView(NEW_GAME_VIEW, NewGameView.class).setRoomConfigFromUI(roomConfig);
	}

	public void removeView(String view, float stageX, float stageY) {
		if (!isViewReady(view)) return;
		View v = getView(view);
		if (v == null) return;
		if (isOnTop(view, true)) hideWithAnimation(v,stageX,stageY);
		else v.setVisible(false);
	}

	public void setSplash(MyImage splash) {
		this.splash = splash;
		getParent().addActor(splash);
	}

	private void initAnimators() {
		layersShowAnimator = new PointRevealer(controller.getResolution().getGameAreaPosition());
		layersHideAnimator = new PointRevealer(controller.getResolution().getGameAreaPosition());
		layersShowAnimator.config()
				.setMask(mask)
				.setDuration(1f)
				.setInitScale(0.05f)
				.setFinalScale(3.2f)
				.setInterpolation(Interpolation.pow2Out)
				.setMoveToFront(true);
//				.setListener(new Animator.AnimatorFinishedListener() {
//					@Override
//					public void onAnimationFinished(boolean completed) {
//						getView(MAIN_MENU_VIEW).setVisible(false);
//					}
//				});
		layersHideAnimator.config()
				.setMask(maskReveal)
				.setDuration(0.7f)
				.setInitScale(0.05f)
				.setFinalScale(3f)
				.setInterpolation(Interpolation.pow2In)
				.setReverse(true)
				.setMoveToFront(true);
	}

	public void revealAnimation() {
		IResolution resolution = controller.getResolution();
//		splash.setPosition(-resolution.getGameAreaPosition().x, -resolution.getGameAreaPosition().y);
//		splash.setSize(resolution.getScreenWidth(), resolution.getScreenHeight());
		splash.toFront();

		AutoRevealer autoRevealer = new AutoRevealer(resolution.getGameAreaPosition());
		autoRevealer.config()
				.setDuration(1f)
				.setInitScale(0.05f)
				.setFinalScale(1.7f)
				.setInterpolation(Interpolation.pow3In)
				.setMask(maskReveal)
				.setToReveal(splash)
				.setReverse(true)
				.setListener(new Animator.AnimatorFinishedListener() {
					@Override
					public void onAnimationFinished(boolean completed) {
						splash.setVisible(false);
						((IMenuController)controller).onSplashScreenDone();
					}
				});
		autoRevealer.startAnimation();
	}

	public View showLoadingDialogue(String text, float clickX, float clickY) {
		return getView(COMMON_VIEW, CommonView.class).showLoadingDialogue(text, clickX, clickY);
	}

	public View hideLoadingDialogue() {
		return getView(COMMON_VIEW, CommonView.class).hideLoadingDialogue();
	}

	public View hideAnimationDialogue() {
		return getView(COMMON_VIEW, CommonView.class).hideAnimationDialogue();
	}

	public View showDialogue(String title, String subtitle, boolean hasYesNoButtons){
		return showDialogue(title, subtitle, hasYesNoButtons, null);
	}

	public View showDialogue(String title, String subtitle, boolean hasYesNoButtons, IDialogueListener listener) {

		CommonView view = getView(COMMON_VIEW, CommonView.class);
		if (listener == null) controller.getActiveScreenController().addButtonListenersRecursively(view);
		else controller.removeButtonListenersRecursively(view);

		view.showDialogue(title, subtitle, hasYesNoButtons, listener);
		return view;
	}

	public View hideDialogue() {
		return getView(COMMON_VIEW, CommonView.class).hideDialogue();
	}

	public void startShowAnimation() {
		getView(MAIN_MENU_VIEW, MainMenuView.class).startShowAnimation();
	}

	public void updateSettings(SettingsModel settingsModel) {
		if (((View)findActor(SETTINGS_VIEW)).isReady())
			getView(SETTINGS_VIEW, SettingsView.class).updateView(settingsModel);
	}

	public void setPlayerImage(byte[] image) {
		if (mask == null) {
			log.e().append("Main menu not ready yet, cannot reveal profile picture").print();
			return;
		}
		MyImage newMaskImage = new MyImage(((MyImage)mask.getMask()).getDrawable());
		Mask newMask = new Mask(newMaskImage, true, controller.getResolution().getGameAreaPosition());
		getView(USER_VIEW, UserView.class).setPlayerImage(image, newMask, controller.getResolution());
	}

	public void removePlayerImage() {
		if (mask == null) return;
		MyImage newMaskImage = new MyImage(((MyImage)mask.getMask()).getDrawable());
		Mask newMask = new Mask(newMaskImage, true, controller.getResolution().getGameAreaPosition());
		getView(USER_VIEW, UserView.class).removePlayerImage(newMask, controller.getResolution());
	}

	@Override
	protected <T extends View> T getView(String name, Class<T> klass) {
		//make sure the main menu view is ready
		if (name.compareTo(MAIN_MENU_VIEW) != 0) super.getView(MAIN_MENU_VIEW);
		return super.getView(name, klass);
	}
	@Override
	protected <T extends View> T getView(String name) {
		//make sure the main menu view is ready
		if (name.compareTo(MAIN_MENU_VIEW) != 0) super.getView(MAIN_MENU_VIEW);
		return super.getView(name);
	}
}
