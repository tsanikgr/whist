package com.tsanikgr.whist_multiplayer.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.tsanikgr.whist_multiplayer.IResolution;

public class ResolutionHelper implements IResolution {
	private static final int TARGET_WIDTH = 1920;
	private static final int TARGET_HEIGHT = 1080;

	private float targetAspectRatio;
	private float screenWidth;
	private float screenHeight;
	private Vector2 gameAreaBounds;
	private Vector2 gameAreaPosition;
	private float targetWidth;
	private float targetHeight;
	private float targetAssetSizeRatio;
	private final IAssets assets;

	public ResolutionHelper(IAssets assets){
		this.assets = assets;
		resize(TARGET_WIDTH, TARGET_HEIGHT, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public boolean resize(int screenWidth, int screenHeight) {

		if (Math.abs(this.screenWidth - screenWidth) < 0.0001 && Math.abs(this.screenHeight - screenHeight) < 0.0001) return false;
		if (Math.abs(this.screenHeight - screenWidth) < 0.0001 && Math.abs(this.screenWidth - screenHeight) < 0.0001) return false;

		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		assets.setResolver(new StageBuilderFileHandleResolver(screenWidth, assets.getSupportedResolutions()));

		if (screenHeight > screenWidth) {
			resize(targetWidth, targetHeight, screenWidth, screenHeight);
		} else {
			resize(targetHeight, targetWidth, screenWidth, screenHeight);
		}

		return true;
	}

	private void resize(float targetWidth, float targetHeight, float screenWidth, float screenHeight) {
		this.targetWidth = targetWidth;
		this.targetHeight = targetHeight;
		this.targetAspectRatio = targetWidth / targetHeight;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.gameAreaBounds = calculateGameAreaBounds(targetAspectRatio, screenWidth, screenHeight);
		this.gameAreaPosition = calculateGameAreaPosition(targetAspectRatio, screenWidth, screenHeight);
		this.targetAssetSizeRatio = gameAreaBounds.x / (int) assets.getResolver().findBestResolution().x;
	}

	/**
	 * @param targetAspectRatio
	 * @param screenWidth       device screen width in pixels
	 * @param screenHeight      device screen height in pixels
	 * @return width and height of max area having aspect ratio of targetAspectRatio
	 */
	private Vector2 calculateGameAreaBounds(float targetAspectRatio, float screenWidth, float screenHeight) {
		Vector2 result = new Vector2();
		float deviceAspectRatio = screenWidth / screenHeight;
		if (targetAspectRatio > deviceAspectRatio) {
			result.x = screenWidth;
			result.y = screenWidth / targetAspectRatio;
		} else {
			result.x = targetAspectRatio * screenHeight;
			result.y = screenHeight;
		}
		return result;
	}

	/**
	 * @param targetAspectRatio
	 * @param screenWidth
	 * @param screenHeight
	 * @return x, y coordinates where the game area will be placed on screen. bottom left corner is (0,0)
	 */
	private Vector2 calculateGameAreaPosition(float targetAspectRatio, float screenWidth, float screenHeight) {
		Vector2 area = calculateGameAreaBounds(targetAspectRatio, screenWidth, screenHeight);
		Vector2 pos = new Vector2();
		pos.x = (screenWidth - area.x) * 0.5f;
		pos.y = (screenHeight - area.y) * 0.5f;
		return pos;
	}

	/**
	 * Converts virtual coordinates to screen coordinates.
	 *
	 * @param x x pos on virtual stage
	 * @param y y pos on virtual stage
	 * @return screen coordinates
	 */
	public Vector2 toScreenCoordinates(float x, float y) {
		Vector2 result = new Vector2();
		result.x = x + this.gameAreaPosition.x;
		result.y = y + this.gameAreaPosition.y;
		return result;
	}

	/**
	 * Background images may not have the same aspect ratio or size as the screen.
	 * This method calculates the min bounds for a background image that covers the screen in fullscreen.
	 *
	 * @param backgroundWidth
	 * @param backgroundHeight
	 * @return fullscreen image bounds with respecting background aspect ratio.
	 */
	@Override
	public Vector2 calculateBackgroundSize(float backgroundWidth, float backgroundHeight) {
		Vector2 result = new Vector2();
		float backgroundAspectRatio;
		float screenAspectRatio = this.screenWidth / this.screenHeight;
		float horizontalWidth;
		float verticalWidth;
		// portrait orientation
		if(screenHeight>screenWidth){
			horizontalWidth = Math.min(backgroundHeight, backgroundWidth);
			verticalWidth = Math.max(backgroundHeight, backgroundWidth);
		} else {
			// landscape orientation
			horizontalWidth = Math.max(backgroundHeight, backgroundWidth);
			verticalWidth = Math.min(backgroundHeight, backgroundWidth);
		}

		backgroundAspectRatio = horizontalWidth / verticalWidth;

		if (backgroundAspectRatio > screenAspectRatio) {
			result.x = this.screenHeight * backgroundAspectRatio;
			result.y = this.screenHeight;
		} else {
			result.x = this.screenWidth;
			result.y = this.screenWidth / backgroundAspectRatio;
		}
		return result;
	}

	@Override
	public Vector2 calculateBackgroundPosition(float backgroundWidth, float backgroundHeight) {
		Vector2 bgSize = calculateBackgroundSize(backgroundWidth, backgroundHeight);
		Vector2 result = new Vector2();
		result.x = (this.screenWidth - bgSize.x) * 0.5f;
		result.y = (this.screenHeight - bgSize.y) * 0.5f;
		return result;
	}

	@Override
	public float getPositionMultiplier() {
		return this.gameAreaBounds.x / this.targetWidth;
	}

	@Override
	public float getSizeMultiplier() {
		return targetAssetSizeRatio;
	}

	@Override
	public Vector2 getGameAreaBounds() {
		return gameAreaBounds;
	}

	@Override
	public Vector2 getGameAreaPosition() {
		return gameAreaPosition;
	}

	public float getTargetAspectRatio() {
		return targetAspectRatio;
	}

	@Override
	public float getScreenWidth() {
		return screenWidth;
	}

	@Override
	public float getScreenHeight() {
		return screenHeight;
	}

	public float getTargetWidth() {
		return targetWidth;
	}

	public float getTargetHeight() {
		return targetHeight;
	}
}
