package com.tsanikgr.whist_multiplayer.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.tsanikgr.whist_multiplayer.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class LocalizationService {

	//TODO NOT YET IMPLEMENTED PROPERLY

	private final Log log = new Log(this);
	private static final String[] DEFAULT_LANGUAGE_KEYS = {"en", "fr", "gr", "it", "jp", "cn"};
	private static final String DEFAULT_LANGUAGE = "en";

	private Map<String, String> stringMap = null;
	private final List<String> availableLanguageNames = new ArrayList<>();
	private String[] languageKeys = DEFAULT_LANGUAGE_KEYS;
	private String preferredLanguage = "";

	public static String[] getDefaultLanguageKeys() {
		String[] copy = new String[DEFAULT_LANGUAGE_KEYS.length];
		System.arraycopy(DEFAULT_LANGUAGE_KEYS,0,copy,0,DEFAULT_LANGUAGE_KEYS.length);
		return copy;
	}

	public LocalizationService() {
		//TODO
		//load language from storage, see SettingsController
		String language = Locale.getDefault().getLanguage();
		if (!setPreferredLanguage(language)) {
			log.w().append("Language >").append(language).append("< is not supported. Using default ").append(DEFAULT_LANGUAGE).print();
			setPreferredLanguage(DEFAULT_LANGUAGE);
		} else log.i().append("Preferred language is set to ").append(language).print();
	}

	public String getString(String string) {
		return string;
	}


	public String getString(String string, Object... varArgs) {
		return String.format(string, varArgs);
	}

	//	@Override
	//	public String getString(String string) {
	//		if (stringMap != null) {
	//			return (String) stringMap.get(string);
	//		}
	//		return null;
	//	}
	//
	//	@Override
	//	public String getString(String string, Object... varArgs) {
	//		String str = getString(string);
	//		if (str == null) {
	//			return null;
	//		}
	//		return String.format(string, varArgs);
	//	}


	private void initialize(Locale paramLocale) {
		stringMap = new HashMap<>();
		String str1 = paramLocale.getLanguage();
		String str2 = "strings/strings-" + str1 + ".xml";
		FileHandle localFileHandle = Gdx.files.internal(str2);
		if (!localFileHandle.exists()) {
			localFileHandle = Gdx.files.internal("strings/strings.xml");
		}
		try {
			XmlPullParser localXmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
			localXmlPullParser.setInput(localFileHandle.read(), null);
			int j;
			for (int i = localXmlPullParser.getEventType(); i != 1; i = j) {
				if ((i == 2) && (localXmlPullParser.getName().equals("string"))) {
					stringMap.put(localXmlPullParser.getAttributeValue(null, "name"), localXmlPullParser.nextText());
				}
				j = localXmlPullParser.next();
			}
		} catch (XmlPullParserException localXmlPullParserException) {
			Gdx.app.log("LocalisationManager", "Failed to parse strings file.", localXmlPullParserException);
		} catch (IOException localIOException) {
			Gdx.app.log("LocalisationManager", "Failed to read strings file.", localIOException);
		}
	}

	public List<String> getAvailableLanguageNames() {
		return availableLanguageNames;
	}

	private int getLanguageIndex(String language) {
		for (int i = 0; i < languageKeys.length; i++) {
			if (languageKeys[i].equals(language)) return i;
		}
		return -1;
	}

	public String[] getLanguageKeys() {
		String[] copy = new String[languageKeys.length];
		System.arraycopy(languageKeys, 0, copy, 0, languageKeys.length);
		return copy;
	}

	public String getPreferredLanguage() {
		return preferredLanguage;
	}

	private int getPreferredLanguageIndex() {
		return getLanguageIndex(preferredLanguage);
	}

	public String getPreferredLanguageName() {
		return availableLanguageNames.get(getPreferredLanguageIndex());
	}

	public void initializeLanguages(String[] languages) {
		languageKeys = languages;
		availableLanguageNames.clear();
		for (String str : languages) {
			availableLanguageNames.add(getString(str));
		}
	}

	private boolean setPreferredLanguage(String preferred) {
		if (getLanguageIndex(preferred) < 0) return false;
		if (preferredLanguage.equals(preferred)) return false;
		preferredLanguage = preferred;
		initialize(new Locale(preferred));
		return true;
	}
}
