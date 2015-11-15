package com.tsanikgr.whist_multiplayer.stage_builder.xml;

import com.badlogic.gdx.files.FileHandle;
import com.tsanikgr.whist_multiplayer.stage_builder.models.BaseActorModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.ButtonModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.CardModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.CheckBoxModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.CustomWidgetModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.ExternalGroupModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.GroupModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.ImageModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.LabelModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.PlaceHolderModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.SelectBoxModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.SliderModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.TextAreaModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.TextButtonModel;
import com.tsanikgr.whist_multiplayer.stage_builder.models.TextFieldModel;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class XmlModelBuilder {

	private static final String IMAGE_TAG = "Image";
	private static final String BUTTON_TAG = "Button";
	private static final String TEXT_BUTTON_TAG = "TextButton";
	private static final String LABEL_TAG = "Label";
	private static final String SELECT_BOX_TAG = "SelectBox";
	private static final String GROUP_TAG = "Group";
	private static final String SLIDER_TAG = "Slider";
	private static final String TEXT_FIELD_TAG = "TextField";
	private static final String TEXT_AREA_TAG = "TextArea";
	private static final String CHECKBOX_TAG = "CheckBox";
	private static final String PLACEHOLDER_TAG = "PlaceHolder";
	public static final String LOCALIZED_STRING_PREFIX = "@string/";
	private static final float SMALL_TEXT_SIZE = 48.0f;
	private static final String CARD_TAG = "Card";

	public List<BaseActorModel> buildModels(FileHandle fileHandle) throws Exception {
		InputStream is = fileHandle.read();
		try {
			XmlPullParser xmlParser = XmlHelper.getXmlParser(is);
			return buildModels(xmlParser, null);
		} finally {
			is.close();
		}
	}

	public List<BaseActorModel> buildModels(FileHandle fileHandle, String groupName) throws Exception {
		InputStream is = fileHandle.read();
		try {
			XmlPullParser xmlParser = XmlHelper.getXmlParser(is);
			return buildModels(xmlParser, groupName);
		} finally {
			is.close();
		}
	}

	private List<BaseActorModel> buildModels(XmlPullParser xmlParser, String groupName) throws Exception {

		List<BaseActorModel> modelList = new LinkedList<>();
		int eventType = xmlParser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
				case XmlPullParser.START_TAG:
					BaseActorModel model = processXmlStartTag(xmlParser, groupName);
					if (model != null) {
						modelList.add(model);
					}
					break;
				case XmlPullParser.END_TAG:
					if (GROUP_TAG.equalsIgnoreCase(xmlParser.getName())) {
						eventType = XmlPullParser.END_DOCUMENT;
					}
					break;
				default:
					break;
			}
			if (eventType != XmlPullParser.END_DOCUMENT) {
				eventType = xmlParser.next();
			}
		}
		return modelList;
	}

	private BaseActorModel processXmlStartTag(XmlPullParser xmlParser, String groupName) throws Exception {
		String tagName = xmlParser.getName();
		BaseActorModel model;

		if (groupName != null) {
			String name = XmlHelper.readStringAttribute(xmlParser, "name");
			if (!GROUP_TAG.equalsIgnoreCase(tagName)) return null;
			if (!name.equalsIgnoreCase(groupName)) return buildGroupModel(xmlParser,groupName);
		}

		if (IMAGE_TAG.equalsIgnoreCase(tagName)) {
			model = buildImageModel(xmlParser);
		} else if (BUTTON_TAG.equalsIgnoreCase(tagName)) {
			model = buildButtonModel(xmlParser);
		} else if (TEXT_BUTTON_TAG.equalsIgnoreCase(tagName)) {
			model = buildTextButtonModel(xmlParser);
		} else if (TEXT_FIELD_TAG.equalsIgnoreCase(tagName)) {
			model = buildTextFieldModel(xmlParser);
		} else if (TEXT_AREA_TAG.equalsIgnoreCase(tagName)) {
			model = buildTextAreaModel(xmlParser);
		} else if (LABEL_TAG.equalsIgnoreCase(tagName)) {
			model = buildLabelModel(xmlParser);
		} else if (SELECT_BOX_TAG.equalsIgnoreCase(tagName)) {
			model = buildSelectBoxModel(xmlParser);
		} else if ( SLIDER_TAG.equalsIgnoreCase(tagName)) {
			model = buildSliderModel( xmlParser);
		} else if ( GROUP_TAG.equalsIgnoreCase(tagName)) {
			model = buildGroupModel(xmlParser, null);
		} else if ( CHECKBOX_TAG.equalsIgnoreCase( tagName)){
			model = buildCheckBoxModel( xmlParser);
		} else if (PLACEHOLDER_TAG.equalsIgnoreCase(tagName)) {
			model = buildPlaceholderModel(xmlParser);
		} else if (CARD_TAG.equalsIgnoreCase((tagName))) {
			model = buildCardModel(xmlParser);
		} else if (isCustomWidget(tagName)) {
			model = buildCustomWidget(xmlParser, tagName);
		} else{
			model = buildExternalGroupModel(xmlParser, tagName);
		}
		return model;
	}

	private BaseActorModel buildExternalGroupModel(XmlPullParser xmlParser, String tagName) {
		ExternalGroupModel model = new ExternalGroupModel();
		setBaseModelParameters(model, xmlParser);
		model.setFileName(tagName + ".xml");
		return model;
	}

	private BaseActorModel buildCustomWidget(XmlPullParser xmlParser, String klass) {
		CustomWidgetModel model = new CustomWidgetModel();
		setBaseModelParameters(model, xmlParser);
		model.setKlass(klass);
		int numberOfAttributes = xmlParser.getAttributeCount();
		for (int i = 0; i < numberOfAttributes; i++) {
			model.addAttribute(
					xmlParser.getAttributeName(i),
					xmlParser.getAttributeValue(i));
		}
		return model;
	}


	private boolean isCustomWidget(String tagName) {
		return tagName.contains(".");
	}

	private BaseActorModel buildGroupModel(XmlPullParser xmlParser, String groupName) throws Exception {

		if (groupName == null) {
			GroupModel group = new GroupModel();
			setBaseModelParameters(group, xmlParser);
			xmlParser.next();
			List<BaseActorModel> subModels = buildModels(xmlParser, null);
			group.setChildren(subModels);
			return group;
		} else {
			xmlParser.next();
			List<BaseActorModel> subModels = buildModels(xmlParser, groupName);
			if (subModels.size() > 0) return subModels.get(0);
			else return null;
		}
	}

	private BaseActorModel buildImageModel(XmlPullParser xmlParser) {
		ImageModel image = new ImageModel();
		setBaseModelParameters(image, xmlParser);
		image.setAtlasName(XmlHelper.readStringAttribute(xmlParser, "atlas"));
		image.setFrame(XmlHelper.readStringAttribute(xmlParser, "frame"));
		image.setTextureSrc(XmlHelper.readStringAttribute(xmlParser, "src"));
		image.setType(XmlHelper.readStringAttribute(xmlParser, "type"));
		image.setNinepatch(XmlHelper.readBooleanAttribute(xmlParser, "ninepatch", false));
		image.setNinepatchOffset(XmlHelper.readIntAttribute(xmlParser, "ninepatchOffset", 0));
		image.setNinepatchOffsetLeft(XmlHelper.readIntAttribute(xmlParser, "npLeft", 0));
		image.setNinepatchOffsetRight(XmlHelper.readIntAttribute(xmlParser, "npRight", 0));
		image.setNinepatchOffsetTop(XmlHelper.readIntAttribute(xmlParser, "npTop", 0));
		image.setNinepatchOffsetBottom(XmlHelper.readIntAttribute(xmlParser, "npBottom", 0));
		return image;
	}

	private BaseActorModel buildLabelModel(XmlPullParser xmlParser) {
		LabelModel label = new LabelModel();
		setBaseModelParameters(label, xmlParser);
		label.setText(XmlHelper.readStringAttribute(xmlParser, "text"));
		label.setFontName(XmlHelper.readStringAttribute(xmlParser, "fontName"));
		label.setFontColor(XmlHelper.readStringAttribute(xmlParser, "fontColor"));
		label.setWrap(XmlHelper.readBooleanAttribute(xmlParser, "wrap", false));
		label.setAlignment(XmlHelper.readStringAttribute(xmlParser, "align"));
		label.setShadow(XmlHelper.readBooleanAttribute(xmlParser, "shadow", false));
		label.setShadowColor(XmlHelper.readStringAttribute(xmlParser, "shadowColor"));
		label.setFontScale(XmlHelper.readFloatAttribute(xmlParser, "fontScale", 1f));
		label.setFontSize(XmlHelper.readFloatAttribute(xmlParser, "fontSize", SMALL_TEXT_SIZE));
		label.setFontAutoScale(XmlHelper.readBooleanAttribute(xmlParser, "fontAutoScale", false));
		label.setLabelScale(XmlHelper.readFloatAttribute(xmlParser, "labelScale", 0));
		return label;
	}

	private BaseActorModel buildSelectBoxModel(XmlPullParser xmlParser) {
		SelectBoxModel selectBoxModel = new SelectBoxModel();
		setBaseModelParameters(selectBoxModel, xmlParser);
		selectBoxModel.setName(XmlHelper.readStringAttribute(xmlParser, "name"));
		selectBoxModel.setValue(XmlHelper.readStringAttribute(xmlParser, "value"));
		selectBoxModel.setFontName(XmlHelper.readStringAttribute(xmlParser, "font"));
		selectBoxModel.setFontColor(XmlHelper.readStringAttribute(xmlParser, "fontColor"));
		selectBoxModel.setFontSize(XmlHelper.readFloatAttribute(xmlParser, "fontSize", SMALL_TEXT_SIZE));
		selectBoxModel.setFontColorSelected(XmlHelper.readStringAttribute(xmlParser, "fontColorSelected"));
		selectBoxModel.setFontColorUnselected(XmlHelper.readStringAttribute(xmlParser, "fontColorUnselected"));
		selectBoxModel.setAtlasName(XmlHelper.readStringAttribute(xmlParser, "atlas"));
		selectBoxModel.setBackground(XmlHelper.readStringAttribute(xmlParser, "background"));
		selectBoxModel.setSelection(XmlHelper.readStringAttribute(xmlParser, "selection"));
		selectBoxModel.setSelectionBackground(XmlHelper.readStringAttribute(xmlParser, "selectionBackground"));
		selectBoxModel.setPaddingLeft(XmlHelper.readIntAttribute(xmlParser, "paddingLeft", 1));
		selectBoxModel.setPaddingRight(XmlHelper.readIntAttribute(xmlParser, "paddingRight", 1));
		selectBoxModel.setPatchSize(XmlHelper.readIntAttribute(xmlParser, "patchSize", 1));
		selectBoxModel.setMaxTextWidth(XmlHelper.readIntAttribute(xmlParser, "maxTextWidth", 0));
		selectBoxModel.setHorizontalScrollDisabled(XmlHelper.readBooleanAttribute(xmlParser, "horizontalScrollDisabled", false));
		selectBoxModel.setVerticalScrollDisabled(XmlHelper.readBooleanAttribute(xmlParser, "verticalScrollDisabled", false));
		return selectBoxModel;
	}

	private BaseActorModel buildButtonModel(XmlPullParser xmlParser) {
		ButtonModel button = new ButtonModel();
		setBaseModelParameters(button, xmlParser);
		setButtonModelProperties(button, xmlParser);
		return button;
	}

	private CardModel buildCardModel(XmlPullParser xmlParser) {
		CardModel card = new CardModel();
		setBaseModelParameters(card, xmlParser);
		setButtonModelProperties(card, xmlParser);
		return card;
	}

	private void setButtonModelProperties(ButtonModel button, XmlPullParser xmlParser) {
		button.setAtlasName(XmlHelper.readStringAttribute(xmlParser, "atlas"));
		button.setFrameUp(XmlHelper.readStringAttribute(xmlParser, "frameUp"));
		button.setFrameDown(XmlHelper.readStringAttribute(xmlParser, "frameDown"));
		button.setFrameDisabled(XmlHelper.readStringAttribute(xmlParser, "frameDisabled"));
		button.setFrameChecked( XmlHelper.readStringAttribute(xmlParser, "frameChecked"));
		button.setTextureSrcUp(XmlHelper.readStringAttribute(xmlParser, "textureSrcUp"));
		button.setTextureSrcDown(XmlHelper.readStringAttribute(xmlParser, "textureSrcDown"));
		button.setTextureSrcDisabled(XmlHelper.readStringAttribute(xmlParser, "textureSrcDisabled"));
		button.setTextureSrcChecked(XmlHelper.readStringAttribute(xmlParser, "textureSrcChecked"));
	}

	private BaseActorModel buildSliderModel( XmlPullParser xmlPullParser){
		SliderModel sliderModel = new SliderModel();
		setBaseModelParameters(sliderModel, xmlPullParser);
		setSliderModelProperties(sliderModel, xmlPullParser);
		return sliderModel;
	}

	private void setSliderModelProperties( SliderModel sliderModel, XmlPullParser xmlPullParser){
		sliderModel.setAtlasName( XmlHelper.readStringAttribute(xmlPullParser, "atlas"));
		sliderModel.setFrameBackground( XmlHelper.readStringAttribute(xmlPullParser, "frameBackground"));
		sliderModel.setFrameKnob( XmlHelper.readStringAttribute(xmlPullParser, "frameKnob"));
		sliderModel.setTextureSrcBackground( XmlHelper.readStringAttribute(xmlPullParser, "textureSrcBackground"));
		sliderModel.setTextureSrcKnob( XmlHelper.readStringAttribute(xmlPullParser, "textureSrcKnob"));
		sliderModel.setMinValue( XmlHelper.readFloatAttribute(xmlPullParser, "minValue", 0));
		sliderModel.setMaxValue(XmlHelper.readFloatAttribute(xmlPullParser, "maxValue", 1));
		sliderModel.setStepSize(XmlHelper.readFloatAttribute(xmlPullParser, "stepSize", 0.1f));
	}

	private BaseActorModel buildTextFieldModel(XmlPullParser xmlParser) {
		TextFieldModel textFieldModel = new TextFieldModel();
		setBaseModelParameters(textFieldModel, xmlParser);
		textFieldModel.setAtlasName( XmlHelper.readStringAttribute(xmlParser, "atlas"));
		textFieldModel.setText(XmlHelper.readStringAttribute(xmlParser, "text"));
		textFieldModel.setBackgroundImageName(XmlHelper.readStringAttribute(xmlParser, "backgroundImage"));
		textFieldModel.setSelectionImageName(XmlHelper.readStringAttribute(xmlParser, "selectionImage"));
		textFieldModel.setCursorImageName(XmlHelper.readStringAttribute(xmlParser, "cursorImage"));
		textFieldModel.setBackGroundOffset(XmlHelper.readIntAttribute(xmlParser, "backgroundOffset", 0));
		textFieldModel.setSelectionOffset(XmlHelper.readIntAttribute(xmlParser, "selectionOffset", 0));
		textFieldModel.setCursorOffset(XmlHelper.readIntAttribute(xmlParser, "cursorOffset", 0));
		textFieldModel.setFontName(XmlHelper.readStringAttribute(xmlParser, "fontName"));
		textFieldModel.setFontColor(XmlHelper.readStringAttribute(xmlParser, "fontColor"));
		textFieldModel.setFontSize(XmlHelper.readFloatAttribute(xmlParser, "fontSize", SMALL_TEXT_SIZE));
		textFieldModel.setPassword(XmlHelper.readBooleanAttribute(xmlParser, "password", false));
		textFieldModel.setPasswordChar(XmlHelper.readStringAttribute(xmlParser, "passwordChar", "*"));
		textFieldModel.setHint(XmlHelper.readStringAttribute(xmlParser, "hint"));
		textFieldModel.setPadding(XmlHelper.readFloatAttribute(xmlParser, "padding", 0.0f));
		return textFieldModel;
	}
	private BaseActorModel buildTextAreaModel(XmlPullParser xmlParser) {
		TextAreaModel textAreaModel = new TextAreaModel();
		setBaseModelParameters(textAreaModel, xmlParser);
		textAreaModel.setAtlasName( XmlHelper.readStringAttribute(xmlParser, "atlas"));
		textAreaModel.setText(XmlHelper.readStringAttribute(xmlParser, "text"));
		textAreaModel.setBackgroundImageName(XmlHelper.readStringAttribute(xmlParser, "backgroundImage"));
		textAreaModel.setSelectionImageName(XmlHelper.readStringAttribute(xmlParser, "selectionImage"));
		textAreaModel.setCursorImageName(XmlHelper.readStringAttribute(xmlParser, "cursorImage"));
		textAreaModel.setBackGroundOffset(XmlHelper.readIntAttribute(xmlParser, "backgroundOffset", 0));
		textAreaModel.setSelectionOffset(XmlHelper.readIntAttribute(xmlParser, "selectionOffset", 0));
		textAreaModel.setCursorOffset(XmlHelper.readIntAttribute(xmlParser, "cursorOffset", 0));
		textAreaModel.setFontName(XmlHelper.readStringAttribute(xmlParser, "fontName"));
		textAreaModel.setFontColor(XmlHelper.readStringAttribute(xmlParser, "fontColor"));
		textAreaModel.setFontSize(XmlHelper.readFloatAttribute(xmlParser, "fontSize", SMALL_TEXT_SIZE));
		textAreaModel.setPassword(XmlHelper.readBooleanAttribute(xmlParser, "password", false));
		textAreaModel.setPasswordChar(XmlHelper.readStringAttribute(xmlParser, "passwordChar", "*"));
		textAreaModel.setHint(XmlHelper.readStringAttribute(xmlParser, "hint"));
		textAreaModel.setPadding(XmlHelper.readFloatAttribute(xmlParser, "padding", 0.0f));
		return textAreaModel;
	}

	private BaseActorModel buildTextButtonModel(XmlPullParser xmlParser) {
		TextButtonModel textButton = new TextButtonModel();
		setBaseModelParameters(textButton, xmlParser);
		setButtonModelProperties(textButton, xmlParser);
		setTextButtonModelProperties(textButton, xmlParser);
		return textButton;
	}

	private BaseActorModel buildCheckBoxModel( XmlPullParser xmlPullParser){
		CheckBoxModel checkBoxModel = new CheckBoxModel();
		setBaseModelParameters( checkBoxModel, xmlPullParser);
		setButtonModelProperties(checkBoxModel, xmlPullParser);
		setTextButtonModelProperties(checkBoxModel, xmlPullParser);
		setCheckBoxModelProperties(checkBoxModel, xmlPullParser);
		return checkBoxModel;
	}

	private BaseActorModel buildPlaceholderModel( XmlPullParser xmlPullParser) {
		PlaceHolderModel placeHolderModel = new PlaceHolderModel();
		setBaseModelParameters(placeHolderModel, xmlPullParser);

		return placeHolderModel;
	}

	private void setCheckBoxModelProperties(CheckBoxModel checkBoxModel, XmlPullParser xmlPullParser) {
		checkBoxModel.setFrameCheckboxOff( XmlHelper.readStringAttribute(xmlPullParser, "frameCheckBoxOff"));
		checkBoxModel.setFrameCheckboxOn(XmlHelper.readStringAttribute(xmlPullParser, "frameCheckBoxOn"));
		checkBoxModel.setFrameCheckboxOver( XmlHelper.readStringAttribute(xmlPullParser, "frameCheckBoxOver"));
		checkBoxModel.setTextureSrcCheckboxOff(XmlHelper.readStringAttribute(xmlPullParser, "textureSrcCheckBoxOff"));
		checkBoxModel.setTextureSrcCheckboxOn(XmlHelper.readStringAttribute(xmlPullParser, "textureSrcCheckBoxOn"));
		checkBoxModel.setTextureSrcCheckboxOver(XmlHelper.readStringAttribute(xmlPullParser, "textureSrcCheckBoxOver"));
	}

	private void setTextButtonModelProperties( TextButtonModel textButton, XmlPullParser xmlParser) {
		textButton.setText(XmlHelper.readStringAttribute(xmlParser, "text"));
		textButton.setFontName(XmlHelper.readStringAttribute(xmlParser, "fontName"));
		textButton.setFontColor(XmlHelper.readStringAttribute(xmlParser, "fontColor"));
		textButton.setFontScale(XmlHelper.readFloatAttribute(xmlParser, "fontScale", 1.0f));
		textButton.setFontSize(XmlHelper.readFloatAttribute(xmlParser, "fontSize", SMALL_TEXT_SIZE));
		textButton.setLabelPadding(XmlHelper.readFloatAttribute(xmlParser, "labelPadding", 0.0f));
		textButton.setLabelPaddingLeft(XmlHelper.readFloatAttribute(xmlParser, "labelPaddingLeft", 0.0f));
		textButton.setLabelPaddingRight(XmlHelper.readFloatAttribute(xmlParser, "labelPaddingRight", 0.0f));
		textButton.setLabelPaddingTop(XmlHelper.readFloatAttribute(xmlParser, "labelPaddingTop", 0.0f));
		textButton.setLabelPaddingBottom(XmlHelper.readFloatAttribute(xmlParser, "labelPaddingBottom", 0.0f));
		textButton.setAlignment(XmlHelper.readStringAttribute(xmlParser, "align"));
		textButton.setWrap(XmlHelper.readBooleanAttribute(xmlParser, "wrap", false));
		textButton.setFontAutoScale(XmlHelper.readBooleanAttribute(xmlParser, "fontAutoScale", false));
	}

	private void setBaseModelParameters(BaseActorModel model, XmlPullParser xmlParser) {
		model.setName(XmlHelper.readStringAttribute(xmlParser, "name"));
		model.setX(XmlHelper.readFloatAttribute(xmlParser, "x", 0.0f));
		model.setY(XmlHelper.readFloatAttribute(xmlParser, "y", 0.0f));
		model.setWidth(XmlHelper.readFloatAttribute(xmlParser, "width", 0.0f));
		model.setHeight(XmlHelper.readFloatAttribute(xmlParser, "height", 0.0f));
		model.setOriginX(XmlHelper.readFloatAttribute(xmlParser, "originX", 0.0f));
		model.setOriginY(XmlHelper.readFloatAttribute(xmlParser, "originY", 0.0f));
		model.setzIndex(XmlHelper.readIntAttribute(xmlParser, "zIndex", 0));
		model.setScale(XmlHelper.readFloatAttribute(xmlParser, "scale", 1));
		model.setScaleX(XmlHelper.readFloatAttribute(xmlParser, "scaleX", 1));
		model.setScaleY(XmlHelper.readFloatAttribute(xmlParser, "scaleY", 1));
		model.setVisible(Boolean.valueOf(XmlHelper.readStringAttribute(xmlParser, "visible", "true")));
		model.setColor(XmlHelper.readStringAttribute(xmlParser, "color", null));
		model.setRotation(XmlHelper.readFloatAttribute(xmlParser, "rotation", 0.0f));
		model.setScreenAlignment(XmlHelper.readStringAttribute(xmlParser, "screenAlign", null));
		model.setScreenPaddingTop(XmlHelper.readFloatAttribute(xmlParser, "screenPaddingTop", 0.0f));
		model.setScreenPaddingBottom(XmlHelper.readFloatAttribute(xmlParser, "screenPaddingBottom", 0.0f));
		model.setScreenPaddingLeft(XmlHelper.readFloatAttribute(xmlParser, "screenPaddingLeft", 0.0f));
		model.setScreenPaddingRight(XmlHelper.readFloatAttribute(xmlParser, "screenPaddingRight", 0.0f));
		model.setTouchable(XmlHelper.readStringAttribute(xmlParser, "touchable", "enabled"));
	}

}
