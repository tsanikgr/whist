package com.tsanikgr.whist_multiplayer.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.tsanikgr.whist_multiplayer.IScreenController;
import com.tsanikgr.whist_multiplayer.myactors.Geometry;
import com.tsanikgr.whist_multiplayer.myactors.MyImage;

public class LoadingView extends View {
	public static final float ANIMATION_DURATION = 0f;

	private static final String SPLASH = "splash";
	public static final String LOADING_VIEW = "loading_view";
	private ShapeRenderer shapeRenderer;
	private static final Color PROGRESS_COLOR = new Color(1f,0.5f,0f,1f);
	private static final float INIT_SCALE = 0.1f;
	private float assetManagerProgress = 0f;
	private final IScreenController controller;
	private MyImage splash;

	public LoadingView(IScreenController controller) {
		super(LOADING_VIEW);
		this.controller = controller;
		shapeRenderer = new ShapeRenderer();
		loadSplash();
		setReady(true);
	}

	private void loadSplash() {
		splash = new MyImage(new Texture(Gdx.files.internal("images/splash.png")));
		splash.setName(SPLASH);
		float finalScale = Gdx.graphics.getWidth()/splash.getWidth();
		Geometry.fixOrigin(splash);
//		splash.setScale(INIT_SCALE);
		addActor(splash);
		splash.setScale(finalScale);
//		splash.addAction(Actions.scaleTo(finalScale, finalScale, ANIMATION_DURATION, Interpolation.swingOut));
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		Gdx.gl.glClearColor(0.17f, 0.53f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		controller.alignToScreen(Align.center, Align.center, splash);
		super.draw(batch, parentAlpha);
		renderAssetManagerProgress();
	}

	private void renderAssetManagerProgress() {
		if (shapeRenderer == null) return;

		float p = assetManagerProgress * Gdx.graphics.getWidth();
		this.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		this.shapeRenderer.setColor(PROGRESS_COLOR);
		this.shapeRenderer.rect(0.5f * (Gdx.graphics.getWidth() - p), 50, p, 10);
		this.shapeRenderer.end();
	}

	public void setAssetManagerProgress(float progress){
		this.assetManagerProgress = progress;
	}

	@Override
	public void dispose() {
		if (shapeRenderer == null) return;
		shapeRenderer.dispose();
		shapeRenderer = null;
	}

	@Override
	protected void onAssetsLoaded(View group) {
	}
	@Override

	protected Color getBackgroundColor() {
		return null;
	}
}
