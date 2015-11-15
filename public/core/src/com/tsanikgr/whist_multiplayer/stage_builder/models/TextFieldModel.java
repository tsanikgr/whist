package com.tsanikgr.whist_multiplayer.stage_builder.models;

public class TextFieldModel extends ButtonModel {

	private String text;
	private String fontName;
	private String fontColor;
	private float fontSize;
	private float fontScale;
	private String cursorImageName;
	private String selectionImageName;
	private String backgroundImageName;
	private int backGroundOffset;
	private int cursorOffset;
	private int selectionOffset;
	private boolean password;
	private String passwordChar = "*";
	private String hint;
	private float padding;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFontName() {
		return fontName;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	public String getFontColor() {
		return fontColor;
	}

	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}

	public void setFontSize(float fontSize) {this.fontSize = fontSize;}

	public float getFontSize(){return fontSize;}

	public String getCursorImageName() {
		return cursorImageName;
	}

	public void setCursorImageName(String cursorImageName) {
		this.cursorImageName = cursorImageName;
	}

	public String getSelectionImageName() {
		return selectionImageName;
	}

	public void setSelectionImageName(String selectionImageName) {
		this.selectionImageName = selectionImageName;
	}

	public String getBackgroundImageName() {
		return backgroundImageName;
	}

	public void setBackgroundImageName(String backgroundImageName) {
		this.backgroundImageName = backgroundImageName;
	}

	public int getBackGroundOffset() {
		return backGroundOffset;
	}

	public void setBackGroundOffset(int backGroundOffset) {
		this.backGroundOffset = backGroundOffset;
	}

	public int getCursorOffset() {
		return cursorOffset;
	}

	public void setCursorOffset(int cursorOffset) {
		this.cursorOffset = cursorOffset;
	}

	public int getSelectionOffset() {
		return selectionOffset;
	}

	public void setSelectionOffset(int selectionOffset) {
		this.selectionOffset = selectionOffset;
	}

	public boolean isPassword() {
		return password;
	}

	public void setPassword(boolean password) {
		this.password = password;
	}

	public String getPasswordChar() {
		return passwordChar;
	}

	public void setPasswordChar(String passwordChar) {
		this.passwordChar = passwordChar;
	}

	public String getHint() {
		return hint;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	public float getPadding() {
		return padding;
	}

	public void setPadding(float padding) {
		this.padding = padding;
	}

	public float getFontScale() {
		return fontScale;
	}
	public void setFontScale(float fontScale) {
		this.fontScale = fontScale;
	}
}
