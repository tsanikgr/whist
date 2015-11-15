package com.tsanikgr.whist_multiplayer.myactors;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class PointRevealer extends RevealerAnimator {

	public class PointRevealerParams extends RevealerAnimatorParams {
	}

	public PointRevealer(Vector2 gameAreaPosition) {
		super(gameAreaPosition);
		initParams();
	}

	void initParams(){
		params = new PointRevealerParams();
		newParams = new PointRevealerParams();
	}

	public PointRevealerParams config(){
		return (PointRevealerParams)newParams;
	}

	@Override
	public void onStart() {
		RevealerAnimatorParams params = (RevealerAnimatorParams)this.params;
		float clickX = params.clickX;
		float clickY = params.clickY;

		super.onStart();
		if (params.initScale < 0) {
//			mask.setPosition(ix, iy);
//			mask.setSize(iw, ih);
		} else {
			clickX -= gameAreaPosition.x;
			clickY -= gameAreaPosition.y;
			params.mask.mask.setSize(iw * (params.reverse ? params.finalScale : params.initScale), ih * (params.reverse ? params.finalScale : params.initScale));
			params.mask.mask.setPosition(clickX - params.mask.mask.getWidth()/2f, clickY - params.mask.mask.getHeight()/2f);
		}

		float xFinal = clickX - iw * (params.reverse ? params.initScale : params.finalScale) / 2f;
		float yFinal = clickY - ih * (params.reverse ? params.initScale : params.finalScale) / 2f;
		float wFinal = iw * (params.reverse ? params.initScale : params.finalScale);
		float hFinal = ih * (params.reverse ? params.initScale : params.finalScale);

		params.mask.mask.addAction(lastAction(Actions.parallel(Actions.moveTo(xFinal, yFinal, params.duration, params.interpolation), Actions.sizeTo(wFinal, hFinal, params.duration, params.interpolation))));
	}
}
