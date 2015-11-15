package com.tsanikgr.whist_multiplayer;

import com.badlogic.gdx.math.Vector2;

public interface IResolution {
	boolean resize(int screenWidth, int screenHeight);
	Vector2 calculateBackgroundSize(float backgroundWidth, float backgroundHeight);
	Vector2 calculateBackgroundPosition(float backgroundWidth, float backgroundHeight);
	float getPositionMultiplier();
	float getSizeMultiplier();
	Vector2 getGameAreaBounds();
	Vector2 getGameAreaPosition();
	float getScreenWidth();
	float getScreenHeight();
}
