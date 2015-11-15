package com.tsanikgr.whist_multiplayer.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.tsanikgr.whist_multiplayer.Config;
import com.tsanikgr.whist_multiplayer.IDialogueListener;
import com.tsanikgr.whist_multiplayer.IImageBytesLoadedInterface;
import com.tsanikgr.whist_multiplayer.IMenuController;
import com.tsanikgr.whist_multiplayer.models.GameModel;
import com.tsanikgr.whist_multiplayer.models.SettingsModel;
import com.tsanikgr.whist_multiplayer.models.StatisticsModel;
import com.tsanikgr.whist_multiplayer.myactors.MyButton;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;
import com.tsanikgr.whist_multiplayer.myactors.MySlider;
import com.tsanikgr.whist_multiplayer.myactors.MyTextField;
import com.tsanikgr.whist_multiplayer.views.LoadingView;
import com.tsanikgr.whist_multiplayer.views.View;
import com.tsanikgr.whist_multiplayer.views.menu.CoinsView;
import com.tsanikgr.whist_multiplayer.views.menu.CommonView;
import com.tsanikgr.whist_multiplayer.views.menu.MainMenuView;
import com.tsanikgr.whist_multiplayer.views.menu.MenuScreen;
import com.tsanikgr.whist_multiplayer.views.menu.NewGameView;
import com.tsanikgr.whist_multiplayer.views.menu.SettingsView;
import com.tsanikgr.whist_multiplayer.views.menu.StatisticsView;
import com.tsanikgr.whist_multiplayer.views.menu.UserView;

public class MenuController extends ScreenController implements IMenuController {

	final MenuScreen screen;
	private final GameModel.WhistRoomConfig roomConfig;
	private boolean splashDone = false;
	private String pendingButton = null;

	private final MultiplayerController multiplayerController;

	public MenuController() {
		screen = new MenuScreen(this);
		roomConfig = new GameModel.WhistRoomConfig();

		addController(multiplayerController = new MultiplayerController());
		addEventListener(new MenuScreenClickListener());
		addEventListener(new SettingsClickListener());
		addEventListener(getNewGameViewListener());
		addEventListener(getSettingsSliderListener());
		deactivate();
	}

	@Override
	protected void init() {
		super.init();
		screen.setVisible(false);
		getScreenDirector().addActor(screen);
		getOSinterface().setMultiplayerControllerAndAttemptToConnect(multiplayerController);
	}

	@Override
	void activate() {
		super.activate();
		boolean fromSplash = checkForSplashScreen();
		screen.buildViews();
		screen.clearActions();
		screen.setVisible(true);
		getScreenDirector().addActor(screen);
		if (fromSplash) screen.revealAnimation();
		screen.startShowAnimation();
	}

	private boolean checkForSplashScreen() {
		View loadingView = getScreenDirector().findActor(LoadingView.LOADING_VIEW);
		if (loadingView == null) return false;
		screen.setSplash((MyImage) loadingView.getChildren().get(0));
		loadingView.remove();
		loadingView.clear();
		return true;
	}

	@Override
	public void onSplashScreenDone() {
		splashDone = true;
		checkForNickName(null);
		new Timer().scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				getCardController().loadAsync();
			}
		}, 0.2f);
	}

	@Override
	protected void disposeController() {
		super.disposeController();
		screen.dispose();
	}

	@Override
	void deactivate() {
		super.deactivate();
	}

	@Override
	public boolean handleBack() {
		return handleBack(-1,-1);
	}

	private boolean handleBack(float stageX, float stageY) {
//		if (screen.isOnTop(MenuScreen.SETTINGS_VIEW, true)) getSettings().save();
		if (showingDialogue || showingLoadingDialogue) return true;
		if (screen.isOnTop(MenuScreen.MAIN_MENU_VIEW,true)) return false;
		screen.goBack(stageX, stageY);
		return true;
	}

	@Override
	protected void onAssetsLoaded(View view){
		screen.addActor(view);
		addButtonListenersRecursively(view);
		if (view.getName().compareTo(MenuScreen.MAIN_MENU_VIEW) == 0) {
			view.setVisible(true);
			if (getMultiplayer() != null) {
				screen.enableMultiplayer(true);
				multiplayerController.onMainMenuAssetsLoaded();
			} else screen.enableMultiplayer(false);
		} else if (view.getName().compareTo(MenuScreen.USER_VIEW) == 0) {
			view.setVisible(true);
			initNickNameTextFieldListeners(view);
			getStatisticsController().addObserver((UserView) view);
		} else if (view.getName().compareTo(MenuScreen.NEW_GAME_VIEW) == 0) {
			screen.setRoomConfigFromUI(roomConfig);
		} else if (view.getName().compareTo(MenuScreen.COINS_VIEW) == 0) {
			getStatisticsController().addObserver((CoinsView)view);
		} else if (view.getName().compareTo(MenuScreen.STATISTICS_VIEW) == 0) {
			getStatisticsController().addObserver((StatisticsView)view);
		} else if (view.getName().compareTo(MenuScreen.SETTINGS_VIEW) == 0) {
			((SettingsView)view).initialiseLanguageButtons(getAssets().getLocalization().getLanguageKeys());
			((SettingsView)view).setEventListener(this);
			((SettingsView)view).updateView(getSettings().getSettingsModel());
		}
	}

	private void goTo(String view, float stageX, float stageY) {
		if (!screen.isOnTop(view,true)) screen.show(view, stageX, stageY);
		else screen.goBack(stageX, stageY);
	}

	private void onNewGameClicked(float stageX, float stageY) {
		if (!checkForNickName("local")) return;
		if (getScreenDirector().setLocal().gameExists()) getScreenDirector().goToGame();
		else {
			goTo(MenuScreen.NEW_GAME_VIEW, stageX, stageY);
			screen.chooseMode(true);
		}
	}

	@Override
	public void onOnlineGameClicked(float stageX, float stageY) {
		if (getMultiplayer() == null) return; // desktop mode
		if (!checkForNickName("online")) return;
		if (!multiplayerController.onOnlineGameClicked()) return;
		goTo(MenuScreen.NEW_GAME_VIEW, stageX, stageY);
		screen.chooseMode(false);
	}

	private void onStartButtonClicked(float stageX, float stageY) {
		if (!getAndValidateRoomConfig()) return;
		getScreenDirector().setLocal().setGameParams(roomConfig);
		screen.removeView(MenuScreen.NEW_GAME_VIEW, -1, -1);
		getScreenDirector().goToGame();
	}

	private void onInviteClicked(float stageX, float stageY) {
		if (!getAndValidateRoomConfig()) return;
		multiplayerController.onInviteClicked(roomConfig);
		showLoadingDialogue("Loading friends from Google", stageX, stageY);
	}

	private void onInvitationsClicked(float stageX, float stageY) {
		multiplayerController.onInvitationsClicked();
		showLoadingDialogue("Loading invitations", stageX, stageY);
	}

	private void onQuickGameClicked(float stageX, float stageY) {
		if (!getAndValidateRoomConfig()) return;
		multiplayerController.onQuickGameClicked(roomConfig);
		showLoadingDialogue("Joining waiting room", stageX, stageY);
	}

	private void onLogoutClicked(float stageX, float stageY) {
		multiplayerController.onLogoutClicked();
		screen.removeView(MenuScreen.NEW_GAME_VIEW, stageX, stageY);
		screen.removePlayerImage();
	}

	private void onStatsClicked(float stageX, float stageY) {
		goTo(MenuScreen.STATISTICS_VIEW, stageX, stageY);
	}

	private void onLeaderboardsClicked(float stageX, float stageY) {
		if (!checkForNickName("leaderboards")) return;
		multiplayerController.onLeaderboardsClicked();
	}

	private void onAchievementsClicked(float stageX, float stageY) {
		if (!checkForNickName("achievements")) return;
		multiplayerController.onAchievementsClicked();
	}

	private void onSettingsClicked(float stageX, float stageY) {
		goTo(MenuScreen.SETTINGS_VIEW, stageX, stageY);
	}

	private void onTutorialClicked(float stageX, float stageY) {
		showDialogue("The tutorial is not ready yet.", "Please check back soon.", false);
	}

	private void onRateClicked(float stageX, float stageY) {
		getOSinterface().rateApp();
	}

	private void onShareClicked(float stageX, float stageY) {
		if (!getOSinterface().isAndroid()) return;
//		showLoadingDialogue("", stageX, stageY);
		getOSinterface().shareText("I am challenging you to a game of whist: https://goo.gl/Ia5dRN");
	}

	@Override
	public void onCoinsClicked(float stageX, float stageY) {
		goTo(MenuScreen.COINS_VIEW, stageX, stageY);
	}

	@Override
	public void showLoadingDialogue(String text, float clickX, float clickY) {
		if (showingDialogue) hideLoadingDialogue();
		View lv = screen.showLoadingDialogue(text, clickX, clickY);
		getScreenDirector().addActor(lv);
		lv.toFront();
		showingLoadingDialogue = true;
	}

	@Override
	public void hideLoadingDialogue() {
		if (!showingLoadingDialogue) return;
		screen.addActor(((CommonView) getScreenDirector().findActor(MenuScreen.COMMON_VIEW)).hideLoadingDialogue());
		showingLoadingDialogue = false;
	}

	@Override
	public void showDialogue(String title, String subtitle, boolean hasYesNoButtons, IDialogueListener listener) {
		if (showingDialogue) hideDialogue();
		View lv = screen.showDialogue(title, subtitle, hasYesNoButtons, listener);
		getScreenDirector().addActor(lv);
		lv.toFront();
		showingDialogue = true;
	}

	@Override
	public void showDialogue(String title, String subtitle, boolean hasYesNoButtons) {
		if (showingDialogue) hideDialogue();
		View lv = screen.showDialogue(title, subtitle, hasYesNoButtons);
		getScreenDirector().addActor(lv);
		lv.toFront();
		showingDialogue = true;
	}

	@Override
	public void hideAnimationDialogue() {
		if (!showingDialogue) return;
		screen.addActor(((CommonView) getScreenDirector().findActor(MenuScreen.COMMON_VIEW)).hideAnimationDialogue());
		showingDialogue = false;
	}

	@Override
	public void hideDialogue() {
		if (!showingDialogue) return;
		screen.addActor(((CommonView) getScreenDirector().findActor(MenuScreen.COMMON_VIEW)).hideDialogue());
		showingDialogue = false;
	}

	@Override
	public boolean isShowingDialogue() {
		return showingDialogue;
	}

	@Override
	public boolean isShowingloadingDialogue() {
		return showingLoadingDialogue;
	}

	@Override
	public void hideNewGameView() {
		screen.removeView(MenuScreen.NEW_GAME_VIEW, -1, -1);
	}

	@Override
	public boolean checkForNickName(String pendingButtonClick) {
		if (pendingButtonClick == null) pendingButtonClick = pendingButton;
		if (!getStatisticsController().isNameSet()) {
			this.pendingButton = pendingButtonClick;
			showDialogue("Please choose a nickname", "You can always change it later", false,
					new IDialogueListener() {
						@Override
						public void onDialogueResult(int result) {
							screen.askForNickname();
							hideDialogue();
						}
					});
			return false;
		}
		pendingButton = null;
		return true;
	}

	@Override
	public void onKeyboardHidden() {
		TextField nameField;
		if (!splashDone) return;
		if (screen == null || screen.getStage() == null)  return;
		try {	nameField = (TextField) screen.getStage().getKeyboardFocus(); } catch (ClassCastException e) { return; }
		if (nameField == null || nameField.getName().compareTo(UserView.PLAYER_NAME) != 0) return;

		Gdx.graphics.setContinuousRendering(Config.IS_CONTINIOUS_RENDERING);
		getScreenDirector().unfocusAll();

		if (nameField.getText().length() == 0 || nameField.getText().compareTo("nickname") == 0) {
			nameField.setText(getStatisticsController().getStatistics().getName());
			pendingButton = null;
			return;
		}

		getStatisticsController().setName(nameField.getText());
		onNicknameValidated();
	}

	private boolean isSignedIn() {
		return getMultiplayer() != null && getMultiplayer().isSignedIn();
	}

	private void onNicknameValidated(){
		if (pendingButton == null) return;
		String pending = pendingButton;
		pendingButton = null;

		if (getMultiplayer().hasAppLaunchInvite()) {
			getMultiplayer().beginUserInitiatedSignIn();
			return;
		}
		if (multiplayerController.checkIfLaunchedFromInvitation()) return;
		if (pending.compareTo("local") == 0) onNewGameClicked(-1, -1);
		else if (pending.compareTo("online") == 0) onOnlineGameClicked(-1, -1);
		else if (pending.compareTo("achievements") == 0) onAchievementsClicked(-1, -1);
		else if (pending.compareTo("leaderboards") == 0) onLeaderboardsClicked(-1, -1);
	}

	private boolean getAndValidateRoomConfig() {
		screen.setRoomConfigFromUI(roomConfig);
		if (!getStatisticsController().hasEnoughtCoins(roomConfig.bet)) {
			showDialogue("Not enough coins", "You can either buy, or play for a smaller bet", false);
			goTo(MenuScreen.COINS_VIEW, -1, -1);
			return false;
		}
		return true;
	}

	@Override
	public void requestOnlinePlayerImage() {
		if (!isSignedIn()) return;
		int height = (int)Math.ceil(280 * getResolution().getScreenHeight()/1080);
		getMultiplayer().requestOnlinePlayerImage(new IImageBytesLoadedInterface() {
			@Override
			public void onImageLoaded(byte[] image) {
				if (!isSignedIn()) return;    //in case he logs out before this callback is received
				if (screen != null) screen.setPlayerImage(image);
			}
		}, -1, height);
	}

	private void initNickNameTextFieldListeners(View view) {
		final MyTextField nameField = view.findActor(UserView.PLAYER_NAME);
		nameField.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				Gdx.graphics.setContinuousRendering(true);
				if (nameField.getText().compareTo(StatisticsModel.DEFAULT_NICKNAME) == 0) {
					nameField.setText("nickname");
					nameField.setSelection(0, nameField.getText().length());
				}
			}
		});
		nameField.setTextFieldListener(new TextField.TextFieldListener() {
			@Override
			public void keyTyped(TextField textField, char c) {
				if (c == '\n' || c == '\r') {
					if (getOSinterface().isAndroid()) Gdx.input.setOnscreenKeyboardVisible(false);
					else onKeyboardHidden();
				}
			}
		});
	}

	@Override
	public void updateSettingsView(SettingsModel settingsModel) {
		screen.updateSettings(settingsModel);
	}

	private EventListener getNewGameViewListener() {
		return new EventListener() {
			@Override
			public boolean handle(Event event) {
				String name = event.getListenerActor().getName();
				if (name.compareTo(NewGameView.SLIDER) == 0) {
					roomConfig.bet = ((MySlider.SliderEvent)event).bet;
					roomConfig.difficulty = ((MySlider.SliderEvent)event).difficulty;
					getSounds().playTickSound();
					return true;
				} else if (name.compareTo(NewGameView.ROUND13_LINK) == 0) {
					roomConfig.has13s = ((MyButton)((InputEvent)event).getRelatedActor()).isChecked();
					getSounds().playTickSound();
				} else if (name.compareTo(NewGameView.SKIP_ROUNDS_LINK) == 0) {
					roomConfig.skipRounds = ((MyButton) ((InputEvent) event).getRelatedActor()).isChecked();
					getSounds().playTickSound();
				}
				return false;
			}
		};
	}

	private class MenuScreenClickListener extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {

			boolean playPopSound = true;
			String name = event.getListenerActor().getName();
			if (name.compareTo(MainMenuView.NEW_GAME) == 0) {
				onNewGameClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(MainMenuView.ONLINE_GAME) == 0) {
				onOnlineGameClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(UserView.COINS) == 0) {
				onCoinsClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(UserView.STATS) == 0) {
				onStatsClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(MainMenuView.SETTINGS) == 0) {
				onSettingsClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(MainMenuView.SHARE) == 0) {
				onShareClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(MainMenuView.RATE) == 0) {
				onRateClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(MainMenuView.TUTORIAL) == 0) {
				onTutorialClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(MainMenuView.ACHIEVEMENTS) == 0) {
				onAchievementsClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(MainMenuView.LEADERBOARDS) == 0) {
				onLeaderboardsClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(MenuScreen.BACK) == 0) {
				handleBack(event.getStageX(), event.getStageY());
			} else if (name.compareTo(NewGameView.START_BUTTON) == 0) {
				onStartButtonClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(NewGameView.INVITE) == 0) {
				onInviteClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(NewGameView.INVTITATIONS) == 0) {
				onInvitationsClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(NewGameView.QUICK_GAME) == 0) {
				onQuickGameClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(MenuScreen.LOGOUT) == 0) {
				onLogoutClicked(event.getStageX(), event.getStageY());
			} else if (name.compareTo(CommonView.Dialogue.OK_BUTTON) == 0) {
				hideAnimationDialogue();
			} else if (name.compareTo(NewGameView.ROUND13_CHECKBOX) == 0) {
				roomConfig.has13s = ((MyButton)event.getListenerActor()).isChecked();
				getSounds().playTickSound();
				playPopSound = false;
			} else if (name.compareTo(NewGameView.SKIP_ROUNDS_CHECKBOX) == 0) {
				roomConfig.skipRounds = ((MyButton)event.getListenerActor()).isChecked();
				getSounds().playTickSound();
				playPopSound = false;
			} else playPopSound = false;

			if (playPopSound) getSounds().playPopSound();
		}
	}

	private EventListener getSettingsSliderListener() {
		return new EventListener() {
			@Override
			public boolean handle(Event event) {
				String name = event.getListenerActor().getName();
				if (name.compareTo(SettingsView.SLIDER_SOUND) == 0) {
					getSettings().handleSoundChange(((MySlider.SliderEvent) event).value);
					getSounds().playTickSound();
					return true;
				} else if (name.compareTo(SettingsView.SLIDER_ANIMATION_SPEED) == 0) {
					getSettings().handleAnimationSpeedChange(((MySlider.SliderEvent) event).value);
					getSounds().playTickSound();
					return true;
				}
				return false;
			}
		};
	}

	private class SettingsClickListener extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			boolean playPopSound = true;
			Actor actor = event.getListenerActor();
			String name = actor.getName();

			if (name.compareTo(SettingsView.CHECKBOX_VIBRATION) == 0) {
				getSettings().handleVibrationClick(((MyButton) actor).isChecked());
			} else if (name.compareTo(SettingsView.CHECKBOX_CHAT) == 0) {
				getSettings().handleChatClick(((MyButton) actor).isChecked());
			} else if (name.compareTo(SettingsView.CHECKBOX_DEBUG) == 0) {
				getSettings().handleDebugClick(((MyButton) actor).isChecked());
			} else if (name.startsWith(SettingsView.LANGUAGE_PREFIX)) {
				getSettings().handleLanguageClick(name);
			} else if (name.startsWith(SettingsView.CARD_COLOR_PREFIX)) {
				if (actor.getColor().equals(getSettings().getCardColor())) ((MyButton)actor).setChecked(true);
				else getSettings().handleCardColorClick(actor.getColor());
			} else {
				playPopSound = false;
			}

			if (playPopSound) getSounds().playPopSound();
		}
	}
}
