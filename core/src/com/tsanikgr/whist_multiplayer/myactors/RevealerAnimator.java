package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public abstract class RevealerAnimator extends Animator {

	float ix, iy, iw, ih;

	public class RevealerAnimatorParams extends AnimatorParams {
		Mask mask = null;
		float initScale = 0.1f;
		float finalScale = 1f;
		float duration = 0.8f;
		Interpolation interpolation = Interpolation.pow4In;
		boolean reverse = false;
		Actor toReveal;

		public RevealerAnimatorParams(){}

		public RevealerAnimatorParams setReverse(boolean reverse){
			this.reverse = reverse;
			return this;
		}

		public RevealerAnimatorParams setDuration(float duration) {
			this.duration = duration;
			return this;
		}
		public RevealerAnimatorParams setInitScale(float initScale) {
			this.initScale = initScale;
			return this;
		}
		public RevealerAnimatorParams setFinalScale(float finalScale) {
			this.finalScale = finalScale;
			return this;
		}
		public RevealerAnimatorParams setInterpolation(Interpolation interpolation) {
			this.interpolation = interpolation;
			return this;
		}

		public RevealerAnimatorParams setMask(Mask mask) {
			this.mask = mask;
			return this;
		}
		public RevealerAnimatorParams setToReveal(Actor toReveal) {
			this.toReveal = toReveal;
			return this;
		}

		@Override
		AnimatorParams setFrom(AnimatorParams prototype1) {
			super.setFrom(prototype1);
			RevealerAnimatorParams prototype = (RevealerAnimatorParams)prototype1;
			mask = prototype.mask;
			initScale = prototype.initScale;
			finalScale = prototype.finalScale;
			duration = prototype.duration;
			interpolation = prototype.interpolation;
			reverse = prototype.reverse;
			toReveal = prototype.toReveal;
			return this;
		}
	}

	RevealerAnimator(Vector2 gameAreaPosition) {
		super(gameAreaPosition);
	}

	@Override
	protected void onStart(){
		RevealerAnimatorParams params = (RevealerAnimatorParams)this.params;

		ix = params.mask.mask.getX();
		iy = params.mask.mask.getY();
		iw = params.mask.mask.getWidth();
		ih = params.mask.mask.getHeight();

		params.mask.mask.setVisible(false);
		params.toReveal.getStage().getRoot().addActor(params.mask.mask);

		params.toReveal.setVisible(true);
		if (params.moveToFront) params.toReveal.toFront();
		params.mask.enableMask();
		params.mask.mask.clearActions();
		if (params.toReveal instanceof MaskInterface) ((MaskInterface)params.toReveal).setMask(params.mask);
	}

	@Override
	protected void onStop() {
		RevealerAnimatorParams params = (RevealerAnimatorParams)this.params;

		if (params.toReveal instanceof MaskInterface) ((MaskInterface)params.toReveal).setMask(null);
		params.mask.disableMask();
		params.mask.mask.setSize(iw,ih);
		params.mask.mask.clearActions();
	}
}
