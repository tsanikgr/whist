package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Timer;
import com.tsanikgr.whist_multiplayer.Config;
import com.tsanikgr.whist_multiplayer.IGameController;
import com.tsanikgr.whist_multiplayer.IMenuController;
import com.tsanikgr.whist_multiplayer.IMultiplayerGame;
import com.tsanikgr.whist_multiplayer.IScreenController;
import com.tsanikgr.whist_multiplayer.IScreenDirector;
import com.tsanikgr.whist_multiplayer.IStageBuilderListener;
import com.tsanikgr.whist_multiplayer.myactors.Mask;
import com.tsanikgr.whist_multiplayer.myactors.MyGroup;
import com.tsanikgr.whist_multiplayer.myactors.MyLabel;
import com.tsanikgr.whist_multiplayer.stage_builder.builders.StageBuilder;
import com.tsanikgr.whist_multiplayer.views.Screen;
import com.tsanikgr.whist_multiplayer.views.menu.MenuScreen;
import com.tsanikgr.whist_multiplayer.views.menu.UserView;

import java.util.HashMap;

public class ScreenDirector extends CompositeController implements IScreenDirector {
	private static final float FADE_DURATION = 1f;

	private LoadingController loadingController;
	private MenuController menuController;
	private GameController gameController;

	private final InputMultiplexer multiplexer = new InputMultiplexer();
	private StageBuilder stageBuilder = null;
	private CompositeStageBuilderListener stageBuilderListener;
	private Stage stage = null;
	private MyGroup root = null;
	private ShapeRenderer debugRenderer = null;
	private MyLabel fpsLabel = null;
	private MyLabel idleLabel = null;


	@Override
	protected void init() {
		clearControllers();
		createStage();
		addInputProcessors();
		Gdx.graphics.setContinuousRendering(Config.IS_CONTINIOUS_RENDERING);

		addController(loadingController = new LoadingController());
		addController(menuController = new MenuController());
		addController(gameController = new GameController());
		super.init();
	}

	@Override
	protected void disposeController() {
		Gdx.input.setInputProcessor(null);
		Gdx.input.setCatchBackKey(false);
		gameController.save();

		super.disposeController();
		stage.dispose();
		Mask.disposeMaskingFrameBuffer();
		if (debugRenderer != null) {
			debugRenderer.dispose();
			debugRenderer = null;
		}
	}

	@Override
	protected void update(float dt) {
		getAssets().setRenderThreadId(Thread.currentThread().getId());
		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.update(dt);
		stage.act(dt);
		stage.draw();
		if (Config.SHOW_FPS) addDebugOverlay();
		else removeDebugOverlay();
		if (Config.DRAW_DEBUG) drawDebug();
	}

	void updateViewport(int width, int height) {
		Mask.disposeMaskingFrameBuffer();
		log.w().append("Resizing to [").append(width).append("x").append(height).append("]").print();
		init();
	}

	public void pause() {
		gameController.save();
	}

	public void resume() {
	}

	@Override
	public void goToMenu() {
		Controller activeController = getActiveController();
		if (activeController == menuController) return;
		log.i().append("=== Goind to menu screen ===").print();
		if (activeController != null) activeController.deactivate();
		menuController.activate();
		if (activeController == gameController)
			switchScreensWithFadeAnimation(menuController.screen, gameController.screen);
	}

	@Override
	public void goToGame() {
		Controller activeController = getActiveController();
		if (activeController == gameController) return;
		log.i().append("=== Goind to game screen ===").print();
		if (activeController != null) activeController.deactivate();
		gameController.activate();
		if (activeController == menuController)
			switchScreensWithFadeAnimation(gameController.screen, menuController.screen);
	}

	private void switchScreensWithFadeAnimation(Screen entering, Screen exiting) {
		exiting.addAction(Actions.delay(FADE_DURATION, Actions.visible(false)));
		entering.getColor().a = 0;
		entering.addAction(Actions.fadeIn(FADE_DURATION, Interpolation.fade));
	}

	private ScreenController getActiveController(){
		for (int i = 0; i < getChildren().size; i++)
			if (getChildren().get(i) instanceof ScreenController && getChildren().get(i).active) return (ScreenController)getChildren().get(i);
		return null;
	}

	/***********************************************************************************************/

	private void createStage() {
		stageBuilder = new StageBuilder(getAssets(), getOSinterface());
		stageBuilder.setStageBuilderListener(stageBuilderListener = new CompositeStageBuilderListener());
		stage = stageBuilder.build(null, getResolution().getScreenWidth(), getResolution().getScreenHeight(), true);
		root = stage.getRoot().findActor(StageBuilder.ROOT_NAME);
		root.setSize(getResolution().getGameAreaBounds().x, getResolution().getGameAreaBounds().y);
		stage.setActionsRequestRendering(true);
	}

	@Override
	public void unfocusAll() {
		if (stage != null) {
			stage.setKeyboardFocus(null);
			stage.unfocusAll();
			Gdx.input.setOnscreenKeyboardVisible(false);
			Gdx.graphics.requestRendering();
		}
	}

	@Override
	public IScreenController getActiveScreenController() {
		return getActiveController();
	}

	@Override
	public IGameController setLocal() {
		if ((gameController != null) && !(gameController instanceof MultiplayerGameController)) return gameController;
		if (gameController != null) {
			gameController.deactivate();
			gameController.disposeController();
			removeController(gameController);
		}

		addController(gameController = new GameController());
		gameController.init();
		return gameController;
	}

	@Override
	public IMultiplayerGame.In setOnline() {
		if ((gameController != null) && (gameController instanceof MultiplayerGameController)) return (MultiplayerGameController)gameController;
		if (gameController != null) {
			gameController.deactivate();
			gameController.disposeController();
			removeController(gameController);
		}

		addController(gameController = new MultiplayerGameController());
		gameController.init();
		return (MultiplayerGameController)gameController;
	}

	@Override
	public void addActor(Actor actor) {
		root.addActor(actor);
		if (findActor(MenuScreen.COMMON_VIEW) != null) findActor(MenuScreen.COMMON_VIEW).toFront();
	}

	@Override
	public boolean removeActor(Actor actor) {
		return removeActor(root, actor);
	}

	@Override
	public <T extends Actor> T findActor(String name) {
		return root.findActor(name);
	}

	private boolean removeActor(Group root, Actor actor){
		if (root.removeActor(actor)) return true;
		for (Actor a : root.getChildren()) {
			if (a instanceof Group && removeActor((Group)a, actor)) return true;
		}
		return false;
	}

	@Override
	public MyGroup buildGroup(String xmlFile, String groupName, MyGroup groupToFill) {
		xmlFile = prepareGroupBuild(xmlFile);
		if (xmlFile == null) return null;

		MyGroup group = null;
		String timerId = getPerformance().tickNew();
		try {
			log.i().append("+++").append(xmlFile).append("::").append(groupName).append("+++ building started SYNC").print();
			group = stageBuilder.buildGroup(xmlFile, groupName, groupToFill);
			log.i().append("+++").append(xmlFile).append("::").append(groupName).append("+++ was built in [").append(getPerformance().stop(timerId)).append("ms]").print();
		} catch (Exception e) {
			getPerformance().stop(timerId);
			log.e(e).append("Cannot build group from [").append(xmlFile).append("]").print();
		}
		return group;
	}

	@Override
	public void buildGroupAsync(String xmlFile, String groupName, MyGroup groupToFill, IStageBuilderListener listener) {
		xmlFile = prepareGroupBuild(xmlFile);
		if (xmlFile == null) return;

		final String timerId = getPerformance().tickNew();
		stageBuilderListener.addListener(xmlFile,groupName,listener,timerId);
		log.i().append(">>> ").append(xmlFile).append("::").append(groupName).append(" >>> building ASYNC").print();
		stageBuilder.buildGroupAsync(xmlFile, groupName, groupToFill);
	}

	private String prepareGroupBuild(String xmlFile){
		if (xmlFile == null) {
			log.e().append("xmlFile name cannot be null").print();
			return null;
		}
		if (xmlFile.substring(xmlFile.length() - 4).compareTo(".xml") != 0) xmlFile += ".xml";
		return xmlFile;
	}

	/***********************************************************************************************/

	private void drawDebug() {
		if (debugRenderer == null) {
			debugRenderer = new ShapeRenderer();
		}

		debugRenderer.setProjectionMatrix(stage.getCamera().combined);

		debugRenderer.begin(ShapeRenderer.ShapeType.Line);
		debugRenderer.setColor(Color.YELLOW);
		Vector2 gameAreaBounds = getResolution().getGameAreaBounds();
		Vector2 gameAreaPosition = getResolution().getGameAreaPosition();
		debugRenderer.rect(gameAreaPosition.x, gameAreaPosition.y, gameAreaBounds.x, gameAreaBounds.y);
		debugRenderer.rect(gameAreaPosition.x + 1, gameAreaPosition.y + 1, gameAreaBounds.x - 2, gameAreaBounds.y - 2);
		debugRenderer.end();
		stage.setDebugAll(true);
		stage.setDebugInvisible(true);
//		stage.setDebugUnderMouse(true);
	}

	Timer.Task timerTask = new Timer.Task() {
		@Override
		public void run() {
			fpsLabel.setText(Gdx.graphics.getFramesPerSecond() + " fps");
			idleLabel.setText(getPerformance().getIdleTime() + "% idle");
		}
	};

	private void addDebugOverlay(){
		if (fpsLabel != null) return;
		if (loadingController.active) return;

		fpsLabel = new MyLabel("00",new Label.LabelStyle(getAssets().getFont(null, 30), Color.WHITE));
		fpsLabel.setFontScale(fpsLabel.getStyle().font.getScaleX() * getAssets().getFontScaling(30));
		idleLabel = new MyLabel("00",new Label.LabelStyle(getAssets().getFont(null, 30), Color.WHITE));
		idleLabel.setFontScale(fpsLabel.getStyle().font.getScaleX() * getAssets().getFontScaling(30));

		stage.getRoot().addActor(fpsLabel);
		stage.getRoot().addActor(idleLabel);
		fpsLabel.pack();
		idleLabel.pack();
		fpsLabel.setPosition(20, getResolution().getScreenHeight() - fpsLabel.getHeight() - 20);
		idleLabel.setPosition(20, getResolution().getScreenHeight() - fpsLabel.getHeight() * 2 - 10);
		fpsLabel.setTouchable(Touchable.disabled);
		idleLabel.setTouchable(Touchable.disabled);
		new Timer().scheduleTask(timerTask, 0.2f, 0.2f);
	}

	private void removeDebugOverlay(){
		if (fpsLabel == null) return;
		if (loadingController.active) return;

		timerTask.cancel();
		fpsLabel.remove();
		idleLabel.remove();
		fpsLabel = null;
		idleLabel = null;
	}

	/***********************************************************************************************/

	private void addInputProcessors() {
		Gdx.input.setCatchBackKey(true);
		multiplexer.addProcessor(new InputAdapter() {
			@Override
			public boolean keyDown(int keycode) {
				if (checkIfNicknameEntered(keycode)) return true;
				return (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) && handleBackKey();
			}

			private boolean handleBackKey() {
				unfocusAll();
				getSounds().playPopSound();
				for (int i = 0; i < getChildren().size; i++)
					if (getChildren().get(i) instanceof ScreenController &&
							getChildren().get(i).active &&
							((ScreenController) getChildren().get(i)).handleBack()) return true;
				Gdx.app.exit();
				return true;
			}

			private boolean checkIfNicknameEntered(int keycode) {
				if (keycode != Input.Keys.ESCAPE && keycode != Input.Keys.BACK) return false;
				Actor nameField = stage.getKeyboardFocus();
				if (nameField != null && nameField.getName().compareTo(UserView.PLAYER_NAME) == 0) {
					if (getOSinterface().isAndroid()) Gdx.input.setOnscreenKeyboardVisible(false);
					else getMenuController().onKeyboardHidden();
					return true;
				}
				return false;
			}
		});
		multiplexer.addProcessor(stage);
		Gdx.input.setInputProcessor(multiplexer);
	}

	/***********************************************************************************************/

	private class CompositeStageBuilderListener implements IStageBuilderListener {

		final HashMap<String, IStageBuilderListener> listeners = new HashMap<>();
		final HashMap<String, String> listenerTimerIds = new HashMap<>();

		public void addListener(String fileName, String groupName, IStageBuilderListener listener, String timerId) {
			if (listeners.containsKey(fileName + groupName)) log.e().append("StageBuilderListeners already contains this key.").print();
			listeners.put(fileName + groupName, listener);
			listenerTimerIds.put(fileName + groupName,timerId);
		}

		@Override
		public void onGroupBuilded(final String fileName, final MyGroup group) {
//			addButtonListenersRecursively(group);
//			fixOriginRecursively(root);
			String timerId = listenerTimerIds.get(fileName+group.getName());
			log.i().append(">>> ").append(fileName).append("::").append(group.getName()).append(" >>> was built in ").append(getPerformance().stop(timerId != null ? timerId : "n/a")).append("ms").print();
			if (timerId == null) return;

			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					listeners.get(fileName + group.getName()).onGroupBuilded(fileName, group);
					listeners.remove(fileName + group.getName());
					listenerTimerIds.remove(fileName + group.getName());
				}
			});
		}

		@Override
		public void onGroupBuildFailed(final String fileName,final Exception e) {
			String timerId = listenerTimerIds.get(fileName);
			log.e().append(">>>").append(fileName).append("<<< FAILED building after [").append(getPerformance().stop(timerId != null ? timerId : "n/a")).append("ms]").print();
			if (timerId == null) return;

			throw new RuntimeException("Group " + fileName + "failed to build");

//			Gdx.app.postRunnable(new Runnable() {
//				@Override
//				public void run() {
//					listeners.get(fileName).onGroupBuildFailed(fileName, e);
//					listeners.remove(fileName);
//					listenerTimerIds.remove(fileName);
//				}
//			});
		}
	}

	@Override
	public IMenuController getMenuController() {
		return menuController;
	}

	@Override
	public IGameController getGameController() {
		return gameController;
	}

}
