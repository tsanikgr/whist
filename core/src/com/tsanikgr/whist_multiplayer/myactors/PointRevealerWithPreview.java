package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

public class PointRevealerWithPreview extends PointRevealer {

	private float finalScale;
	private float initScale;
	private float duration;
	private Interpolation interpolation;

	private boolean isClicked = false;
	private boolean isTouchUp = false;
	private long touchDownTime;

	public PointRevealerWithPreview(Vector2 gameAreaPosition) {
		super(gameAreaPosition);
	}

	public class PointRevealerWithPreviewParams extends PointRevealerParams {
		private Interpolation.Elastic peekInterpolation = new Interpolation.ElasticOut(2, 10, 7, 0.6f);
		private float peekDuration = 1.0f;
		private float peekFinalScale = 1.0f;
		private float peekInitScale = 0.01f;

		public PointRevealerWithPreviewParams(){
		}

		public PointRevealerWithPreviewParams setPeekInterpolation(Interpolation.Elastic peekInterpolation) {
			this.peekInterpolation = peekInterpolation;
			return this;
		}
		public PointRevealerWithPreviewParams setPeekDuration(float peekDuration) {
			this.peekDuration = peekDuration;
			return this;
		}
		public PointRevealerWithPreviewParams setPeekInitScale(float peekInitScale){
			this.peekInitScale = peekInitScale;
			return this;
		}
		public PointRevealerWithPreviewParams setPeekFinalScale(float peekFinalScale) {
			this.peekFinalScale = peekFinalScale;
			return this;
		}

		@Override
		protected PointRevealerWithPreviewParams setFrom(AnimatorParams prototype1) {
			super.setFrom(prototype1);
			PointRevealerWithPreviewParams prototype = (PointRevealerWithPreviewParams)prototype1;
			peekInterpolation = prototype.peekInterpolation;
			peekDuration = prototype.peekDuration;
			peekFinalScale = prototype.peekFinalScale;
			peekInitScale = prototype.peekInitScale;
			return this;
		}
	}

	@Override
	public void initParams(){
		params = new PointRevealerWithPreviewParams();
		newParams = new PointRevealerWithPreviewParams();
	}

	@Override
	public PointRevealerWithPreviewParams config(){
		return (PointRevealerWithPreviewParams)newParams;
	}

	@Override
	public void onTouchDown(float x, float y) {
		if (!isRunning()) {
			PointRevealerWithPreviewParams params = (PointRevealerWithPreviewParams)this.params;
			finalScale = params.finalScale;
			initScale = params.initScale;
			duration = params.duration;
			interpolation = params.interpolation;

			params.initScale = params.peekInitScale;
			params.finalScale = params.peekFinalScale;
			params.duration = params.peekDuration;
			params.interpolation = params.peekInterpolation;

			isClicked = false;
			isTouchUp = false;

			touchDownTime = System.currentTimeMillis();
			super.onTouchDown(x, y);
		}
	}

	@Override
	public void onTouchUp(float x, float y) {
		PointRevealerWithPreviewParams params = (PointRevealerWithPreviewParams)this.params;
		animationRunning = false;

		params.initScale = -1f;
		params.interpolation = interpolation;
		if (System.currentTimeMillis() - touchDownTime < 200) {
			isClicked = true;

			params.finalScale = finalScale;
			params.duration = duration;
			startAnimation();
		} else {
			isTouchUp = true;

			params.finalScale = 0;
			params.duration = 0.2f;
			startAnimation();
		}
	}

	@Override
	public void onStop() {
		PointRevealerWithPreviewParams params = (PointRevealerWithPreviewParams)this.params;
		animationCompleted = isClicked;

		if (isTouchUp) params.toReveal.setVisible(false);
		if (isTouchUp || isClicked) super.onStop();

		isClicked = false;
		isTouchUp = false;
		params.duration = duration;
		params.finalScale = finalScale;
		params.initScale = initScale;
	}
}
