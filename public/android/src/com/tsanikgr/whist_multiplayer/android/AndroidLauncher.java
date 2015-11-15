package com.tsanikgr.whist_multiplayer.android;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.tsanikgr.whist_multiplayer.Config;
import com.tsanikgr.whist_multiplayer.IApplication;
import com.tsanikgr.whist_multiplayer.IMultiplayerController;
import com.tsanikgr.whist_multiplayer.IPlatformApplication;
import com.tsanikgr.whist_multiplayer.IUid;
import com.tsanikgr.whist_multiplayer.android.google.GoogleGames;
import com.tsanikgr.whist_multiplayer.controllers.AppController;
import com.tsanikgr.whist_multiplayer.util.Log;


public class AndroidLauncher extends AndroidApplication implements IPlatformApplication {

	private static final Log log = new Log(AndroidLauncher.class);
	private static final int KEYBOARD_HEIGHT_THRESHOLD = 150;
	private static final int CHECK_KEYBOARD_DELAY = 500;

	private IApplication app;
	private GoogleGames googleGames;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (BuildConfig.IS_DEBUG) Config.setDebug();
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = true;
		config.useAccelerometer = false;
		config.useCompass = false;

		Storage storage = new Storage(this);
		AndroidBuild buildInterface = new AndroidBuild(this);
		buildInterface.setDebug(false);
		IUid uuid = new UIDAndroid(storage, this);

		/** Fire the engines! */
		app = new AppController(this, storage, buildInterface, uuid, googleGames = new GoogleGames(this));
		initialize(app, config);
		addKeyboardListener();

//		facebook.initialize(this, savedInstanceState);


	}

	@Override
	public void onDestroy() {
//		facebook.onDestroy();
		googleGames.onStop();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
//		facebook.onPause();
		if (googleGames != null) googleGames.unregisterInvitationListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
//		facebook.onResume();
//		googleGames.reconnectClient();
		if (googleGames != null) googleGames.registerInvitationListener();
		Gdx.graphics.requestRendering();
	}

	@Override
	public void onSaveInstanceState(Bundle paramBundle) {
		super.onSaveInstanceState(paramBundle);
//		facebook.onSaveInstanceState(paramBundle);
	}

	@Override
	protected void onActivityResult(int request, int response, Intent data) {
		super.onActivityResult(request, response, data);
		googleGames.onActivityResult(request, response, data);
	}

	private void addKeyboardListener() {
		final View rootView = graphics.getView();
		final Rect layoutChangeRect = new Rect();

		rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(CHECK_KEYBOARD_DELAY);
						} catch (InterruptedException e) {
							log.e(e).print();
						}
						rootView.getWindowVisibleDisplayFrame(layoutChangeRect);
						if (Math.abs(rootView.getHeight() - layoutChangeRect.height()) < KEYBOARD_HEIGHT_THRESHOLD) {
							if (app != null && app.getMenuController() != null)
								Gdx.app.postRunnable(new Runnable() {
									@Override
									public void run() {
										app.getMenuController().onKeyboardHidden();
									}
								});
//							log.i().append("keyboard closed").print();
						} /*else log.i().append("keyboard opened").print();*/
					}
				}).start();
			}
		});
	}

	/**
	 * *************************************************************************
	 * ***************** OSApplicationInterface implementation
	 ****************************************************************************************** */
	@Override
	public void setMultiplayerControllerAndAttemptToConnect(IMultiplayerController multiplayerController) {
		googleGames.setMultiplayerController(multiplayerController);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				googleGames.onStart();
			}
			});

	}

	@Override
	public boolean isAndroid() {
		return true;
	}

	@Override
	public void keepScreenOn(final boolean keepOn) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (keepOn) {
					getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				} else {
					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
			}
		});
	}

	@Override
	public void shareText(String shareBody) {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Play Whist");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
	}

	@Override
	public void rateApp(){
		Uri uri = Uri.parse("market://details?id=" + getPackageName());
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
					Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
					Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		} else {
			goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
					Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
					Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		}
		try {
			startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
		}
	}

	@Override
	public void executePendingUiThreadRunnables() {
		if (!app.isUiThread()) {
			log.e().append("Function called from a thread OTHER than the UI thread").print();
			return;
		}
		synchronized (getRunnables()) {
			getExecutedRunnables().clear();
			getExecutedRunnables().addAll(getRunnables());
			getRunnables().clear();
		}

		for (int i = 0; i < getExecutedRunnables().size; i++) {
			try {
				log.w().append("Runnable executing").print();
				getExecutedRunnables().get(i).run();
			} catch (Exception e) {
				log.e(e).print();
			}
		}
	}
}