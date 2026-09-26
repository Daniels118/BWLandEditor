package it.ld.bw.lndgui;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import it.ld.utils.Listeners;
import it.ld.utils.UChangeListener.EventType;

public enum Settings {
	/**enum*/
	LANGUAGE(Category.UI, Type.OPTION, Language.getDefault(), (Object[]) Language.getAvailable()),
	/**boolean*/
	AUTOSAVE(Category.EDITING, Type.BOOL, false),
	/**int*/
	AUTOSAVE_INT(Category.EDITING, Type.INT, 10, 1, 60),
	/**int*/
	MAX_UNDO(Category.EDITING, Type.INT, 50, 1, 1000),	//Important: some functions rely on undo, so at least 1 is required
	/**int*/
	HTTP_PORT(Category.INTEGRATIONS, Type.INT, 8086, 0, (int)Short.MAX_VALUE),
	/**int*/
	EXT_EDITOR_PORT(Category.INTEGRATIONS, Type.INT, 8085, 0, (int)Short.MAX_VALUE),
	/**{@link java.io.File File} / {@link String}*/
	GAME_DIR(Category.GAME, Type.FOLDER, ""),
	/**enum*/
	SHOW_CELLS_ATTR(Category.VIEW3D, Type.OPTION, ShowCellAttrOption.OFF, (Object[])ShowCellAttrOption.values()),
	/**enum*/
	SHOW_GRID(Category.VIEW3D, Type.OPTION, ShowGridOption.EVERYWHERE, (Object[])ShowGridOption.values()),
	/**boolean*/
	SHOW_CELLS(Category.VIEW3D, Type.BOOL, false),
	/**enum*/
	SHOW_OBJECTS(Category.VIEW3D, Type.OPTION, ShowObjectOption.ALL, (Object[])ShowObjectOption.values()),
	/**boolean*/
	SHOW_VIRTUAL_OBJECTS(Category.VIEW3D, Type.BOOL, true),
	/**boolean*/
	SHOW_LINKS(Category.VIEW3D, Type.BOOL, true),
	/**boolean*/
	SHOW_COMPASS(Category.VIEW3D, Type.BOOL, true),
	/**boolean*/
	PREVENT_CAMERA_UNDERGROUND(Category.VIEW3D, Type.BOOL, true),
	/**boolean*/
	HIGHLIGHT_SELECTED_COUNTRY(Category.VIEW3D, Type.BOOL, true),
	/**boolean*/
	OUTLINE_SELECTED_COUNTRY(Category.VIEW3D, Type.BOOL, true),
	/**int*/
	MAX_RECENT_FILES(Category.MISC, Type.INT, 5, 0, 15),
	/**enum*/
	META_STORE(Category.MISC, Type.OPTION, MetaStore.EMBEDDED, (Object[])MetaStore.values()),
	/**float*/
	IMG_SAVE_QUALITY(Category.MISC, Type.FLOAT, 0.8f, 0.2f, 1f, 0.05f),
	/**boolean*/
	AUTORECOVERY(Category.MISC, Type.BOOL, true),
	/**int*/
	AUTORECOVERY_INT(Category.MISC, Type.INT, 1, 1, 10),
	/**boolean*/
	CONSOLE_POPUP(Category.MISC, Type.BOOL, true);
	
	public enum Category {
		UI, EDITING, INTEGRATIONS, VIEW3D, GAME, MISC;
		
		private List<Settings> settings = new ArrayList<>();
		private String text;
		
		public Settings[] getSettings() {
			return settings.toArray(new Settings[settings.size()]);
		}
		
		@Override
		public String toString() {
			if (this.text == null) {
				this.text = I18n.tr("settings.categories." + this.name());
			}
			return this.text;
		}
	}
	
	public enum Type {
		INT, FLOAT, BOOL, STRING, FOLDER, FILE, OPTION
	}
	
	public enum MetaStore {
		EMBEDDED, EXTERNAL;
		
		private String text;
		
		@Override
		public String toString() {
			if (this.text == null) {
				this.text = I18n.tr("settings.META_STORE." + this.name());
			}
			return this.text;
		}
	}
	
	public enum ShowGridOption {
		NONE, SEA, EVERYWHERE;
		
		private String text;
		
		@Override
		public String toString() {
			if (this.text == null) {
				this.text = I18n.tr("settings.SHOW_GRID." + this.name());
			}
			return this.text;
		}
	}
	
	public enum ShowCellAttrOption {
		OFF, PROPS, SOUNDS;
		
		private String text;
		
		@Override
		public String toString() {
			if (this.text == null) {
				this.text = I18n.tr("settings.SHOW_CELLS_ATTR." + this.name());
			}
			return this.text;
		}
	}
	
	public enum ShowObjectOption {
		OFF, ALL, BUILT, PLANNED;
		
		private String text;
		
		@Override
		public String toString() {
			if (this.text == null) {
				this.text = I18n.tr("settings.SHOW_OBJECTS." + this.name());
			}
			return this.text;
		}
	}
	
	public static final Listeners listeners = new Listeners(Settings.class);
	
	private static final String FILENAME = "BWLandEditorGUI_settings";
	
	public final Category category;
	public final Type type;
	public final Object defaultValue;
	public Object[] options;
	
	private String text;
	private Object value;
	
	private Settings(Category category, Type type, Object defaultValue, Object...options) {
		this.category = category;
		this.type = type;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		this.options = options;
		category.settings.add(this);
	}
	
	public String getLocalizedName() {
		if (this.text == null) {
			this.text = I18n.tr("settings." + this.name());
		}
		return this.text;
	}
	
	public static Map<Category, List<Settings>> search(String query) {
		query = query.trim().toLowerCase();
		Map<Category, List<Settings>> r = new LinkedHashMap<>();
		for (Category category : Category.values()) {
			List<Settings> categorySettings = null;
			for (Settings setting : category.settings) {
				if (query.isEmpty() || setting.getLocalizedName().toLowerCase().contains(query)) {
					if (categorySettings == null) {
						categorySettings = new LinkedList<>();
						r.put(setting.category, categorySettings);
					}
					categorySettings.add(setting);
				}
			}
			
		}
		return r;
	}
	
	public static void load() {
		Preferences prefs = Gdx.app.getPreferences(FILENAME);
		for (Category category : Category.values()) {
			for (Settings setting : category.settings) {
				switch (setting.type) {
					case BOOL:
						setting.setValue(prefs.getBoolean(setting.name(), setting.getBool()));
						break;
					case FILE:
					case FOLDER:
					case STRING:
						setting.setValue(prefs.getString(setting.name(), setting.getString()));
						break;
					case FLOAT:
						setting.setValue(prefs.getFloat(setting.name(), setting.getFloat()));
						break;
					case INT:
						setting.setValue(prefs.getInteger(setting.name(), setting.getInt()));
						break;
					case OPTION:
						setting.setValueByName(prefs.getString(setting.name(), setting.getValueName()));
						break;
				}
			}
		}
	}
	
	public static void save() {
		Preferences prefs = Gdx.app.getPreferences(FILENAME);
		for (Category category : Category.values()) {
			for (Settings setting : category.settings) {
				switch (setting.type) {
					case BOOL:
						prefs.putBoolean(setting.name(), setting.getBool());
						break;
					case FILE:
					case FOLDER:
					case STRING:
						prefs.putString(setting.name(), setting.getString());
						break;
					case FLOAT:
						prefs.putFloat(setting.name(), setting.getFloat());
						break;
					case INT:
						prefs.putInteger(setting.name(), setting.getInt());
						break;
					case OPTION:
						prefs.putString(setting.name(), setting.getValueName());
						break;
				}
			}
		}
		prefs.flush();
	}
	
	public Object getValue() {
		return this.value;
	}
	
	public int getOrdinal() {
		return ((Enum<?>)this.value).ordinal();
	}
	
	public Enum<?> getEnum() {
		return (Enum<?>)this.value;
	}
	
	public int getInt() {
		return (Integer)this.value;
	}
	
	public float getFloat() {
		return (Float)this.value;
	}
	
	public boolean getBool() {
		return (Boolean)this.value;
	}
	
	public String getString() {
		return (String)this.value;
	}
	
	public File getFile() {
		return this.value != null && !((String)this.value).isEmpty() ? new File((String)this.value) : null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> void setValueByName(String name) {
		if (this.type != Type.OPTION) throw new RuntimeException("Method allowed only for option type");
		Class<?> clazz = options[0].getClass();
		if (clazz.isEnum()) {
			try {
				setValue(Enum.valueOf((Class<T>) clazz, name));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			for (Object option : this.options) {
				if (name.equals(String.valueOf(option))) {
					setValue(option);
					break;
				}
			}
		}
	}
	
	public String getValueName() {
		if (this.type != Type.OPTION) throw new RuntimeException("Method allowed only for option type");
		Class<?> clazz = options[0].getClass();
		if (clazz.isEnum()) {
			return ((Enum<?>)this.value).name();
		} else {
			return String.valueOf(this.value);
		}
	}
	
	public void setValue(Object value) {
		if (value == null) throw new IllegalArgumentException("Null not allowed");
		boolean changed = false;
		switch (this.type) {
			case BOOL:
				changed = (boolean)value != (boolean)this.value;
				break;
			case FLOAT:
				if (value instanceof Integer) {
					value = (float)(Integer)value;
				}
				changed = (float)value != (float)this.value;
				break;
			case INT:
				changed = (int)value != (int)this.value;
				break;
			case FILE:
			case FOLDER:
				if (value instanceof File) {
					value = ((File)value).getAbsolutePath();
				}
			case STRING:
				changed = !((String)value).equals(this.value);
				break;
			case OPTION:
			default:
				changed = value != this.value;
				break;
		}
		if (changed) {
			Object oldValue = this.value;
			this.value = value;
			listeners.notify(EventType.CHANGE, this, oldValue, this.value);
		}
	}
	
	public void toggle() {
		if (this.type != Type.BOOL) throw new RuntimeException("Only boolean values can be toggled");
		Object oldValue = this.value;
		this.value = !(Boolean)value;
		listeners.notify(EventType.CHANGE, this, oldValue, this.value);
	}
	
	public void cycle() {
		if (this.type != Type.OPTION) throw new RuntimeException("Only option values can be cycled");
		Object oldValue = this.value;
		for (int i = 0; i < this.options.length; i++) {
			if (this.options[i] == this.value) {
				this.value = options[(i + 1) % this.options.length];
				break;
			}
		}
		listeners.notify(EventType.CHANGE, this, oldValue, this.value);
	}
	
	public Object[] getOptions() {
		return this.options;
	}
	
	public float getMinFloat() {
		return options.length >= 1 ? (Float)options[0] : Float.NEGATIVE_INFINITY;
	}
	
	public float getMaxFloat() {
		return options.length >= 2 ? (Float)options[1] : Float.POSITIVE_INFINITY;
	}
	
	public float getMinInt() {
		return options.length >= 1 ? (Integer)options[0] : Integer.MIN_VALUE;
	}
	
	public float getMaxInt() {
		return options.length >= 2 ? (Integer)options[1] : Integer.MAX_VALUE;
	}
	
	public String getMimeFilter() {
		return options.length >= 1 ? (String)options[0] : "All files/*,*";
	}
	
	@Override
	public String toString() {
		return name() + "=" + value;
	}
	
	
	public static class Option {
		public final String id;
		
		public Option(String id) {
			this.id = id;
		}
		
		@Override
		public String toString() {
			return super.toString();
		}
	}
}
