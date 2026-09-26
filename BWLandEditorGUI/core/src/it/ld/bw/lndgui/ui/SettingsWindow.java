package it.ld.bw.lndgui.ui;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Array;

import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;

import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.Settings;
import it.ld.bw.lndgui.Settings.Category;
import it.ld.bw.lndgui.Settings.Type;
import it.ld.bw.lndgui.interfaces.FolderChooser.FolderChooserCallback;
import it.ld.libgdx.ui.components.MessageBox;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.Spinner;
import it.ld.libgdx.ui.components.TextSlider;
import it.ld.libgdx.ui.components.MessageBox.MessageType;
import it.ld.utils.UChangeListener;

public class SettingsWindow extends SmartWindow {
	private TextField searchField;
	private List<Settings.Category> categoryList;
	private Label categoryTitle;
	private Table categoryPanel = new Table();
	
	private Map<Settings, Actor> settingsToField = new HashMap<>();
	
	private Map<Category, java.util.List<Settings>> filteredSettings;
	
	public static SettingsWindow showSingleInstance(MainApp app, Stage stage, Skin skin, boolean modal) {
		SettingsWindow window = (SettingsWindow) SmartWindow.getSingleInstance("mainSettings");
		if (window == null) {
			window = new SettingsWindow(app, skin);
			window.setSingleInstance("mainSettings");
		}
		window.show(stage, modal);
		window.toFront();
		return window;
	}
	
	public static void hideInstance() {
		SettingsWindow window = (SettingsWindow) SmartWindow.getSingleInstance("mainSettings");
		if (window != null) {
			window.remove();
		}
	}
	
	private SettingsWindow(MainApp app, Skin skin) {
		super(I18n.tr("settings.title"), skin, Attribute.RESIZABLE);
		
		filteredSettings = Settings.search("");
		searchField = new TextField("", skin);
		searchField.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				filteredSettings = Settings.search(searchField.getText());
				categoryList.setItems(new Array<>(filteredSettings.keySet().toArray()));
				categoryList.fire(new ChangeEvent());
			}
		});
		add(searchField).fillX().pad(10, 10, 10, 10);
		categoryTitle = new Label("", skin);
		add(categoryTitle).left().pad(10, 0, 10, 10);
		row();
		
		categoryPanel.defaults().top().left().padBottom(2);
		categoryList = new List<>(skin);
		categoryList.addListener(new ChangeListener() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				categoryPanel.clear();
				Category category = categoryList.getSelected();
				if (category == null) {
					categoryTitle.setText("");
					settingsToField.clear();
				} else {
					categoryTitle.setText(category.toString());
					java.util.List<Settings> settings = filteredSettings.get(category);
					for (Settings tSetting : settings) {
						final Settings setting = tSetting;
						categoryPanel.add(new Label(setting.getLocalizedName(), skin)).left().padRight(10);
						Actor field = null;
						switch (setting.type) {
							case BOOL:
								final CheckBox checkbox = new CheckBox("", skin);
								checkbox.setChecked(setting.getBool());
								checkbox.addListener(new ChangeListener() {
									@Override
									public void changed(ChangeEvent event, Actor actor) {
										setting.setValue(checkbox.isChecked());
									}
								});
								field = checkbox;
								break;
							case FILE:
								final Table container = new Table();
								final TextField textfield = new TextField(setting.getString(), skin);
								textfield.addListener(new FocusListener() {
									@Override
									public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
										if (!event.isFocused()) {
											setting.setValue(textfield.getText());
										}
									}
								});
								container.add(textfield).expandX().fillX();
								final TextButton button = new TextButton("...", skin);
								button.addListener(new ClickListener() {
									public void clicked(InputEvent event, float x, float y) {
										app.getOS().getFileChooser().chooseFile(
								            new NativeFileChooserConfiguration() {{
								                title = I18n.tr("dialog.open");
								                directory = Gdx.files.absolute(System.getProperty("user.home"));
								                mimeFilter = setting.getMimeFilter();
								                intent = NativeFileChooserIntent.OPEN;
								            }},
								            new NativeFileChooserCallback() {
								                @Override
								                public void onFileChosen(FileHandle file) {
								                	String path = file.file().getAbsolutePath();
								                	textfield.setText(path);
								                	setting.setValue(path);
								                }

								                @Override
								                public void onCancellation() {}

								                @Override
								                public void onError(Exception exception) {
								                    exception.printStackTrace();
								                }
								            }
								        );
									};
								});
								container.add(button);
								field = container;
								break;
							case FLOAT:
								float min1 = setting.getOptions().length >= 1 ? (float)setting.getOptions()[0] : Float.MIN_VALUE;
								float max1 = setting.getOptions().length >= 2 ? (float)setting.getOptions()[1] : Float.MAX_VALUE;
								float step1 = setting.getOptions().length >= 3 ? (float)setting.getOptions()[2] : 0.1f;
								if (min1 != Float.MIN_VALUE && max1 != Float.MAX_VALUE) {
									final TextSlider slider1 = new TextSlider(skin, setting.getFloat(), min1, max1, step1);
									slider1.addCaptureListener(new InputListener() {
										public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
											return true;
										};
										
										public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
											setting.setValue(slider1.getValue());
										};
									});
									field = slider1;
								} else {
									final Spinner spinner1 = new Spinner(skin, setting.getFloat(), min1, max1, step1);
									spinner1.addListener(new FocusListener() {
										@Override
										public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
											if (!event.isFocused()) {
												setting.setValue(spinner1.getValue());
											}
										}
									});
									spinner1.addListener(new InputListener() {
										public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
											return true;
										};
										
										public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
											setting.setValue(spinner1.getValue());
										};
									});
									field = spinner1;
								}
								break;
							case FOLDER:
								final Table container2 = new Table();
								final TextField textfield4 = new TextField(setting.getString(), skin);
								textfield4.addListener(new FocusListener() {
									@Override
									public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
										if (!event.isFocused()) {
											setting.setValue(textfield4.getText());
										}
									}
								});
								container2.add(textfield4).expandX().fillX();
								final TextButton button2 = new TextButton("...", skin);
								button2.addListener(new ClickListener() {
									public void clicked(InputEvent event, float x, float y) {
										app.getOS().getFolderChooser().chooseFolder(getStage(), skin, I18n.tr("dialog.selectFolder"), setting.getFile(),
								            new FolderChooserCallback() {
								                @Override
								                public void onFolderChosen(FileHandle file) {
								                	String path = file.file().getAbsolutePath();
								                	textfield4.setText(path);
								                	setting.setValue(path);
								                }

								                @Override
								                public void onCancellation() {}

								                @Override
								                public void onError(Exception exception) {
								                    exception.printStackTrace();
								                }
								            }
								        );
									};
								});
								container2.add(button2);
								field = container2;
								break;
							case INT:
								int min2 = setting.getOptions().length >= 1 ? (int)setting.getOptions()[0] : Integer.MIN_VALUE;
								int max2 = setting.getOptions().length >= 2 ? (int)setting.getOptions()[1] : Integer.MAX_VALUE;
								final Spinner spinner2 = new Spinner(skin, setting.getInt(), min2, max2, 1);
								spinner2.setIntegerOnly(true);
								spinner2.addListener(new FocusListener() {
									@Override
									public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
										if (!event.isFocused()) {
											setting.setValue(spinner2.getIntValue());
										}
									}
								});
								spinner2.addListener(new InputListener() {
									public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
										return true;
									};
									
									public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
										setting.setValue(spinner2.getIntValue());
									};
								});
								field = spinner2;
								break;
							case OPTION:
								SelectBox selectbox = new SelectBox(skin);
								selectbox.setItems(setting.getOptions());
								selectbox.setSelected(setting.getValue());
								selectbox.addListener(new ChangeListener() {
									@Override
									public void changed(ChangeEvent event, Actor actor) {
										setting.setValue(selectbox.getSelected());
									}
								});
								field = selectbox;
								break;
							case STRING:
								final TextField textfield5 = new TextField(setting.getString(), skin);
								textfield5.addListener(new FocusListener() {
									@Override
									public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
										if (!event.isFocused()) {
											setting.setValue(textfield5.getText());
										}
									}
								});
								field = textfield5;
								break;
						}
						Cell<Actor> cell = categoryPanel.add(field).expandX();
						if (setting.type != Type.BOOL && !(field instanceof Spinner)) cell.fillX();
						categoryPanel.row();
						settingsToField.put(setting, field);
					}
				}
				categoryPanel.add().colspan(2).expand();	//Filler
				pack();
			}
		});
		categoryList.setItems(Category.values());
		add(categoryList).expandY().fill().pad(0, 10, 10, 10);
		add(categoryPanel).minSize(300, 300).expand().fill().pad(0, 0, 10, 10);
		row();
		
		pack();
		
		Settings.listeners.add(settingsChengeListener);
	}
	
	private final UChangeListener settingsChengeListener = new UChangeListener() {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.CHANGE && event.getProperty() instanceof Settings) {
				Settings setting = (Settings)event.getProperty();
				Actor field = settingsToField.get(event.getProperty());
				if (field != null) {
					TextField textField;
					TextSlider slider;
					Spinner spinner;
					switch (setting.type) {
						case BOOL:
							CheckBox checkbox = (CheckBox)field;
							checkbox.setChecked(setting.getBool());
							break;
						case FILE:
							textField = (TextField)((Table)field).getChild(0);
							textField.setText(setting.getString());
							break;
						case FLOAT:
							if (field instanceof Spinner) {
								spinner = (Spinner)field;
								spinner.setValue(setting.getFloat());
							} else if (field instanceof TextSlider) {
								slider = (TextSlider)field;
								slider.setValue(setting.getFloat());
							} else {
								throw new RuntimeException("Unsupported field type");
							}
							break;
						case FOLDER:
							textField = (TextField)((Table)field).getChild(0);
							textField.setText(setting.getString());
							break;
						case INT:
							spinner = (Spinner)field;
							spinner.setValue(setting.getInt());
							break;
						case OPTION:
							SelectBox selectbox = (SelectBox)field;
							selectbox.setSelected(setting.getValue());
							break;
						case STRING:
							textField = (TextField)field;
							textField.setText(setting.getString());
							break;
					}
				}
				if (setting == Settings.LANGUAGE) {
					MessageBox.show(getStage(), I18n.tr("settings.title"), I18n.tr("settings.thisChangeNotice"), MessageType.INFO);
				}
			}
		}
	};
	
	public void search(Settings setting) {
		searchField.setText(setting.getLocalizedName());
		searchField.fire(new ChangeListener.ChangeEvent());
	}
	
	@Override
	public void dispose() {
		Settings.listeners.remove(settingsChengeListener);
		Settings.save();
		super.dispose();
	}
}
