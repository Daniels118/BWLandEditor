package it.ld.libgdx.utils;

public interface I18nSupport {
	public String get(String key);
    public String get(String key, Object... args);
}
