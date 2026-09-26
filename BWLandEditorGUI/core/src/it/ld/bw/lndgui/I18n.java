package it.ld.bw.lndgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

import it.ld.libgdx.utils.I18nSupport;

import java.util.Locale;
import java.util.MissingResourceException;

public final class I18n implements I18nSupport {
    private static I18n instance = new I18n();
	
	private static I18NBundle bundle;
    private static Locale locale;

    private I18n() {}
    
    public static I18n getInstance() {
    	return instance;
    }
    
    public static void init(Locale desiredLocale) {
        locale = desiredLocale != null ? desiredLocale : Locale.getDefault();

        FileHandle baseFileHandle = Gdx.files.internal("i18n/strings");
        bundle = I18NBundle.createBundle(baseFileHandle, locale);
    }
    
    public String get(String key) {
        return I18n.tr(key);
    }

    public String get(String key, Object... args) {
    	return I18n.tr(key, args);
    }
    
    public static String tr(String key) {
        ensureInit();
        try {
            return bundle.get(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public static String tr(String key, Object... args) {
        ensureInit();
        try {
            return bundle.format(key, args);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public static void setLocale(Locale newLocale) {
        init(newLocale);
    }

    public static Locale getLocale() {
        return locale;
    }

    private static void ensureInit() {
        if (bundle == null) {
            init(Locale.getDefault());
        }
    }
}

