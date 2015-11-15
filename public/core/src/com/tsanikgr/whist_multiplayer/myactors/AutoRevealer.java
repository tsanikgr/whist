package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class AutoRevealer extends RevealerAnimator {

	public class AutoRevealerAnimatorParams extends RevealerAnimatorParams {
	}

	public AutoRevealer(Vector2 gameAreaPosition) {
		super(gameAreaPosition);
		params = new AutoRevealerAnimatorParams();
		newParams = new AutoRevealerAnimatorParams();
	}

	public AutoRevealerAnimatorParams config(){
		return (AutoRevealerAnimatorParams)newParams;
	}

	@Override
	public void onStart() {
		AutoRevealerAnimatorParams params = (AutoRevealerAnimatorParams)this.params;
		super.onStart();

		float x = params.toReveal.getX();
		float y = params.toReveal.getY();
		float w = params.toReveal.getWidth();
		float h = params.toReveal.getHeight();
		float xfinal = x + w / 2f - iw * (params.reverse? params.initScale : params.finalScale) / 2f;
		float yfinal = y + h / 2f - ih * (params.reverse? params.initScale : params.finalScale) / 2f;

		if ((params.reverse? params.finalScale : params.initScale) < 0) {
			params.mask.mask.setPosition(ix, iy);
			params.mask.mask.setSize(iw, ih);
		} else {
			params.mask.mask.setPosition(x + w / 2f - iw * (params.reverse ? params.finalScale : params.initScale) / 2f, y + h / 2f - ih * (params.reverse ? params.finalScale : params.initScale) / 2f);
			params.mask.mask.setSize(iw * (params.reverse ? params.finalScale : params.initScale), ih * (params.reverse ? params.finalScale : params.initScale));
		}
		params.mask.mask.addAction(lastAction(Actions.parallel(Actions.moveTo(xfinal, yfinal, params.duration, params.interpolation), Actions.sizeTo(iw * (params.reverse ? params.initScale : params.finalScale), ih * (params.reverse ? params.initScale : params.finalScale), params.duration, params.interpolation))));
	}
}