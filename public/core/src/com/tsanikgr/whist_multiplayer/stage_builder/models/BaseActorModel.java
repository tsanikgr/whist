package com.tsanikgr.whist_multiplayer.stage_builder.models;

import java.util.Locale;

public abstract class BaseActorModel {

	public enum ScreenAlign {
		top, bottom, left, right
	}

	public enum Touchable {
		enabled, disabled, childrenOnly
	}

	private String name;
	private float x;
	private float y;
	private float width;
	private float height;
	private float originX;
	private float originY;
	private float scaleX = 1f;
	private float scaleY = 1f;
	private float scale = 1f;
	private int zIndex;
	private boolean isVisible;
	private String color;
	private float rotation;
	private ScreenAlign screenAlignment = null;
	private ScreenAlign screenAlignmentSupport = null;
	/**
	 * used only if screen alignment is "top"
	 */
	private float screenPaddingTop;
	/**
	 * used only if screen alignment is "bottom"
	 */
	private float screenPaddingBottom;
	/**
	 * used only if screen alignment is "right"
	 */
	private float screenPaddingRight;
	/**
	 * used only if screen alignment is "left"
	 */
	private float screenPaddingLeft;

	/**
	 * enabled, disabled, childrenOnly
	 * default value enabled
	 */
	private Touchable touchable = Touchable.enabled;


	@Override
	public String toString() {
		return this.name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public int getzIndex() {
		return zIndex;
	}

	public void setzIndex(int zIndex) {
		this.zIndex = zIndex;
	}

	public float getScaleX() {
		return scaleX;
	}

	public void setScaleX(float scaleX) {
		this.scaleX = scaleX;
	}

	public float getScaleY() {
		return scaleY;
	}

	public void setScaleY(float scaleY) {
		this.scaleY = scaleY;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean visible) {
		isVisible = visible;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public float getOriginX() {
		return originX;
	}

	public void setOriginX(float originX) {
		this.originX = originX;
	}

	public float getOriginY() {
		return originY;
	}

	public void setOriginY(float originY) {
		this.originY = originY;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public ScreenAlign getScreenAlignment() {
		return screenAlignment;
	}

	public ScreenAlign getScreenAlignmentSupport() {
		return screenAlignmentSupport;
	}

	public void setScreenAlignment(String s) {
		if (s != null) {
			if (s.indexOf('|') > 0) {
				int index = s.indexOf('|');
				String arr[] = {s.substring(0, index), s.substring(index + 1, s.length())};
				this.screenAlignment = ScreenAlign.valueOf(arr[0].toLowerCase(Locale.ENGLISH));
				this.screenAlignmentSupport = ScreenAlign.valueOf(arr[1].toLowerCase(Locale.ENGLISH));
			}
			else {
				this.screenAlignment = ScreenAlign.valueOf(s.toLowerCase(Locale.ENGLISH));
			}
		}
	}

	public void setScreenAlignment(ScreenAlign screenAlignment) {
		this.screenAlignment = screenAlignment;
	}

	public float getScreenPaddingTop() {
		return screenPaddingTop;
	}

	public void setScreenPaddingTop(float screenPaddingTop) {
		this.screenPaddingTop = screenPaddingTop;
	}

	public float getScreenPaddingBottom() {
		return screenPaddingBottom;
	}

	public void setScreenPaddingBottom(float screenPaddingBottom) {
		this.screenPaddingBottom = screenPaddingBottom;
	}

	public float getScreenPaddingRight() {
		return screenPaddingRight;
	}

	public void setScreenPaddingRight(float screenPaddingRight) {
		this.screenPaddingRight = screenPaddingRight;
	}

	public float getScreenPaddingLeft() {
		return screenPaddingLeft;
	}

	public void setScreenPaddingLeft(float screenPaddingLeft) {
		this.screenPaddingLeft = screenPaddingLeft;
	}

	public float getScaledHeight() {
		if (Math.abs(this.scaleY - 1f) > 0.00001) {
			return this.height * scaleY;
		}

		if (Math.abs(this.scale - 1f) > 0.00001) {
			return this.height * scale;
		}
		return this.height;
	}

	public float getScaledWidth() {
		if (Math.abs(this.scaleX - 1f) > 0.00001) {
			return this.width * scaleX;
		}

		if (Math.abs(this.scale - 1f) > 0.00001) {
			return this.width * scale;
		}
		return this.width;
	}

	public Touchable getTouchable() {
		return touchable;
	}

	public void setTouchable(Touchable touchable) {
		this.touchable = touchable;
	}

	public void setTouchable(String value) {
		if (value != null) {
			this.touchable = Touchable.valueOf(value);
		}
	}



}
