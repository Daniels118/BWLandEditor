package it.ld.bw.lndgui;

import java.util.Locale;

public class Language {
	private static Language defaultLang = new Language(Locale.US);
	private static Language[] available = new Language[] {
		defaultLang,
		new Language(Locale.GERMAN),
		new Language(Locale.FRANCE),
		new Language(Locale.ITALIAN),
		new Language(Locale.forLanguageTag("es"))
	};
	
	private Locale locale;
	
	private Language(Locale locale) {
		this.locale = locale;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	@Override
	public String toString() {
		return locale.getDisplayLanguage(locale);
	}
	
	
	public static Language getDefault() {
		return defaultLang;
	}
	
	public static Language[] getAvailable() {
		return available;
	}
}
