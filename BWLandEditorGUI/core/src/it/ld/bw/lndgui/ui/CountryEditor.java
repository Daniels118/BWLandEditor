/* Copyright (c) 2026 Daniele Lombardi / Daniels118
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.ld.bw.lndgui.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LNDMapMaterial;
import it.ld.bw.lnd.model.LNDMaterial;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.tools.CountryTool;
import it.ld.bw.lnd.tools.CountryTool.CtrlPoint;
import it.ld.bw.lnd.tools.LandTool;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.Settings;
import it.ld.libgdx.ui.components.GridView.State;
import it.ld.libgdx.ui.components.ListTable;
import it.ld.libgdx.ui.components.ListTable.ItemClickListener;
import it.ld.libgdx.ui.components.MenuItemAction;
import it.ld.libgdx.ui.components.ListTable.ItemClickEvent;
import it.ld.libgdx.ui.components.MessageBox;
import it.ld.libgdx.ui.components.PictureBox;
import it.ld.libgdx.ui.components.PopupMenu;
import it.ld.libgdx.ui.components.Prompt;
import it.ld.libgdx.ui.components.Prompt.PromptListener;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.events.RemoveEvent;
import it.ld.libgdx.utils.UndoHelper;
import it.ld.libgdx.utils.Utils;
import it.ld.utils.MathUtils;
import it.ld.utils.UChangeListener;

public class CountryEditor extends SmartWindow {
	private static final int THUMBW = 48;
	private static final int THUMBH = 64;
	
	private ListTable<LNDCountry> countryList;
	private boolean updatingList = false;
	
	private final MainApp app;
	private LndFile land;
	private LNDCountry country = null;
	
	private PictureBox preview;
	private PictureBox mixerPreview;
	private CheckBox shadeUnselectedCheckbox;
	private CheckBox outlineSelectedCheckbox;
	private Label statusBar;
	
	private ArrayList<int[]> materials;
	
	private CountryTool tool;
	
	private MaterialsEditor materialsEditor;
	private CountryPicker countryPicker;
	
	private boolean showNoise = true;
	
	private boolean isDraggingMixer = false;
	private Array<Drawable> thumbs;
	private Texture atlasTexture;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		CountryEditor window = (CountryEditor) SmartWindow.getSingleInstance("countryEditor");
		if (window == null) {
			window = new CountryEditor(app, skin, true);
			window.setDockingManager(app.getDockingManager());
			window.setSingleInstance("countryEditor");
			window.show(stage, 54, 28, false);
		} else {
			window.toFront();
		}
	}
	
	public CountryEditor(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("countryEditor.title"), skin, Attribute.RESIZABLE, Attribute.DOCKABLE);
		this.setAutodispose(autodispose);
		this.app = app;
		
		add(new Label(I18n.tr("countryEditor.countries.label"), skin)).left().pad(0, 5, 0, 5);
		add(new Label(I18n.tr("countryEditor.preview.label"), skin)).left().pad(0, 5, 0, 5);
		add(new Label(I18n.tr("countryEditor.mixer.label"), skin)).left().pad(0, 0, 0, 5);
		row();
		
		countryList = new ListTable<LNDCountry>(skin)
		.setModel(new ListTable.DefaultModel<LNDCountry>(skin) {
			@Override
			public String[] getHeaderNames() {
				return new String[] {
					"",	//Preview image, no header name required
					I18n.tr("countryEditor.countries.name"),
					I18n.tr("countryEditor.countries.used")
				};
			}
			
			@Override
			public Actor createField(int row, int col) {
				if (col == 0) {
					Image image = new Image();
					image.setSize(THUMBW, THUMBH);
					return image;
				}
				return super.createField(row, col);
			}
			
			@Override
			public int[] getAlignments(int[] alignments) {
				alignments[2] = Align.right;
				return alignments;
			}
			
			@Override
			public void render(int row, LNDCountry country, Cell<Container<Actor>>[] fields) {
				fields[0].width(THUMBW);
				fields[1].expandX();
				Image fImage = ((Image)fields[0].getActor().getActor());
				Label fName = ((Label)fields[1].getActor().getActor());
				Label fUsed = ((Label)fields[2].getActor().getActor());
				//
				int total = land.getTotalCells();
				int used = country.getIndex() >= 0 ? land.getCellsPerCountry(country.getIndex()) : -1;
				int percent = Math.round((float)used / total * 100f);
				String pUsed = used == -1 ? "?" : ((used > 0 && percent == 0) ? "<1%" : percent + "%");
				//Content
				//fImage.setDrawable(row < thumbs.size ? thumbs.get(row) : null);
				fImage.setDrawable(thumbs.get(row));
				fName.setText(String.valueOf(country));
				fUsed.setText(pUsed);
				//Style
				Color textColor = used == 0 ? Color.GRAY : Color.WHITE;
				fName.setColor(row >= LndFile.MAX_COUNTRIES ? Color.RED : textColor);
				fUsed.setColor(textColor);
			}
		})
		.setHeaderVisible(true)
		.setRowsBackground(null, skin.newDrawable("white", new Color(1f, 1f, 1f, 0.05f)));
		
		countryList.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (!updatingList) {
					setCountry(countryList.getSelected());
				}
			}
		});
		countryList.addListener(new ItemClickListener<LNDCountry>() {
			@Override
			public void clicked(ItemClickEvent<LNDCountry> event, int row, int col, LNDCountry item) {
				InputEvent click = event.getSource();
				if (click.getButton() == Buttons.RIGHT) {
					int index = countryList.getSelectedIndex();
					//
					LinkedList<MenuItemAction> actions = new LinkedList<>();
					actions.add(new MenuItemAction("replaceOccurrences", I18n.tr("countryEditor.countries.replaceOccurrences"), () -> {
						countryPicker
						.setStyler(new CountryPicker.Styler() {
							@Override
							public void process(int i, LNDCountry item, State state) {
								state.setDisabled(i == index || i >= 16);
							}
						})
						.setCallback(new EventListener() {
							@Override
							public boolean handle(Event event) {
								int newIndex = countryPicker.getSelectedIndex();
								if (newIndex != -1) {
									app.getEditManager().begin(I18n.tr("action.replaceCountry"));
									LandTool.replaceCountry(land, index, newIndex);
									app.getEditManager().end();
								}
								return true;
							}
						})
						.show(getStage());
                    }));
					actions.add(new MenuItemAction("copyFrom", I18n.tr("countryEditor.countries.copyFrom"), () -> {
                    	countryPicker
                    	.setCallback(new EventListener() {
							@Override
							public boolean handle(Event event) {
								LNDCountry src = countryPicker.getSelected();
								if (src != null) {
									app.getEditManager().begin(I18n.tr("action.editCountry"));
									country.setTerrainType(src.getTerrainType());
									country.setMapMaterials(src.getMapMaterialsForRead());
									country.setName(src.getName());
									app.getEditManager().end();
								}
								return true;
							}
						})
                    	.show(getStage());
                    }));
					actions.add(new MenuItemAction("duplicate", I18n.tr("countryEditor.countries.duplicate"), () -> {
        				LNDCountry newCountry = country.clone();
						land.getCountries().add(newCountry);
                    }));
					actions.add(new MenuItemAction("importFrom", I18n.tr("countryEditor.countries.importFrom"), () -> {
						app.getOS().getFileChooser().chooseFile(
				            new NativeFileChooserConfiguration() {{
				                title = I18n.tr("dialog.importCountry.title");
				                directory = Gdx.files.absolute(System.getProperty("user.home"));
				                nameFilter = (dir, name) -> name.endsWith(".lnd") || name.endsWith(".lndc");
				                mimeFilter = "All supported formats/lndc,lnd";
				                intent = NativeFileChooserIntent.OPEN;
				            }},
				            new NativeFileChooserCallback() {
				                @Override
				                public void onFileChosen(FileHandle file) {
				                	try {
				                		String ext = file.name().substring(file.name().lastIndexOf('.') + 1).toLowerCase();
					                	if ("lndc".equals(ext)) {
					                		LandTool.importCountry(file.file(), land, country.getIndex());
					                	} else if ("lnd".equals(ext)) {
					                		LndFile srcLand = LndFile.load(file.file(), false);
					                		CountryPicker tmpPicker = new CountryPicker(skin, srcLand, true);
					                		tmpPicker.setCallback(new EventListener() {
												@Override
												public boolean handle(Event event) {
													int srcCountry = tmpPicker.getSelectedIndex();
													if (srcCountry != -1) {
														LandTool.importCountry(srcLand, srcCountry, land, index);
													}
													return true;
												}
											})
					                		.show(getStage());
						                }
				                	} catch (Exception e) {
										e.printStackTrace();
										MessageBox.show(getStage(), I18n.tr("countryEditor.countries.importFrom"), e);
									}
				                }

				                @Override
				                public void onCancellation() {}

				                @Override
				                public void onError(Exception exception) {
				                    exception.printStackTrace();
				                }
				            }
				        );
                    }));
					actions.add(new MenuItemAction("setTerrainType", I18n.tr("countryEditor.countries.setTerrainType"), () -> {
						Prompt.show(getStage(), skin, I18n.getInstance(), I18n.tr("countryEditor.setTerrainType.title"), I18n.tr("countryEditor.setTerrainType.message"), String.valueOf(country.getTerrainType()), new PromptListener() {
							@Override
							public void confirm(String value) {
								try {
									country.setTerrainType(Integer.parseInt(value));
								} catch (Exception e) {
									MessageBox.show(getStage(), I18n.tr("dialog.exportCountry.title"), e);
								}
							}
						});
                    }));
					actions.add(new MenuItemAction("rename", I18n.tr("countryEditor.countries.rename"), () -> {
						Prompt.show(getStage(), skin, I18n.getInstance(), I18n.tr("countryEditor.rename.title"), I18n.tr("countryEditor.rename.message"), country.getName(), new PromptListener() {
							@Override
							public void confirm(String value) {
								app.getEditManager().begin(I18n.tr("action.renameCountry"));
								country.setName(value);
								app.getEditManager().end();
							}
						});
                    }));
					actions.add(PopupMenu.separator);
					actions.add(new MenuItemAction("export", I18n.tr("countryEditor.countries.export"), () -> {
						app.getOS().getFileChooser().chooseFile(
				            new NativeFileChooserConfiguration() {{
				                title = I18n.tr("dialog.exportCountry.title");
				                directory = Gdx.files.absolute(System.getProperty("user.home"));
				                nameFilter = (dir, name) -> name.endsWith(".lndc");
				                mimeFilter = "Landscape country files/lndc";
				                intent = NativeFileChooserIntent.SAVE;
				            }},
				            new NativeFileChooserCallback() {
				                @Override
				                public void onFileChosen(FileHandle file) {
				                    try {
				                    	LandTool.exportCountry(land, index, file.file());
									} catch (IOException e) {
										e.printStackTrace();
										MessageBox.show(getStage(), I18n.tr("dialog.exportCountry.title"), e);
									}
				                }
				                
				                @Override
				                public void onCancellation() {}

				                @Override
				                public void onError(Exception exception) {
				                    exception.printStackTrace();
				                }
				            }
				        );
                    }));
					actions.add(PopupMenu.separator);
					if (land.isCountryInUse(country.getIndex())) {
						actions.add(new MenuItemAction("swap", I18n.tr("countryEditor.countries.swap"), null));
					} else {
						actions.add(new MenuItemAction("swap", I18n.tr("countryEditor.countries.swap"), () -> {
							final LNDCountry country1 = country;
							final int index1 = country1.getIndex();
							countryPicker
							.setStyler(new CountryPicker.Styler() {
								@Override
								public void process(int i, LNDCountry item, State state) {
									state.setDisabled(i == index || land.isCountryInUse(item.getIndex()));
								}
							})
							.setCallback(new EventListener() {
								@Override
								public boolean handle(Event event) {
									LNDCountry country2 = countryPicker.getSelected();
									if (country2 != null && !land.isCountryInUse(country2.getIndex())) {
										app.getEditManager().begin(I18n.tr("action.swapCountries"));
										final int index2 = country2.getIndex();
										land.getCountries().remove(country1);
										land.getCountries().remove(country2);
										if (index1 < index2) {
											land.getCountries().add(index1, country2);
											land.getCountries().add(index2, country1);
										} else {
											land.getCountries().add(index2, country1);
											land.getCountries().add(index1, country2);
										}
										app.getEditManager().end();
									}
									return true;
								}
							})
							.show(getStage());
						}));
					}
					if (land.isCountryInUse(country.getIndex())) {
						actions.add(new MenuItemAction("removeCountry", I18n.tr("countryEditor.countries.remove"), null));
					} else {
						actions.add(new MenuItemAction("removeCountry", I18n.tr("countryEditor.countries.remove"), () -> {
							if (!land.isCountryInUse(country.getIndex())) {
								app.getEditManager().begin(I18n.tr("action.removeCountry"));
								land.getCountries().remove(country.getIndex());
								app.getEditManager().end();
							}
						}));
					}
					PopupMenu.show1(getStage(), click.getStageX(), click.getStageY(), actions.toArray(new MenuItemAction[0]));
				}
			}
		});
		add(countryList).top().left().grow().pad(0, 5, 10, 5);
		
		preview = new PictureBox();
		preview.setKeepContentOnResize(true);
		add(preview).prefSize(256, 750).grow().pad(0, 5, 10, 5);
		
		mixerPreview = new PictureBox();
		mixerPreview.setKeepContentOnResize(true);
		mixerPreview.addListener(mixerInputListener);
		add(mixerPreview).prefSize(256, 750).grow().pad(0, 0, 10, 5);
		
		row();
		
		add(createCountryPanel(app, skin)).growX().pad(0, 5, 10, 5);
		add(createNoisePanel(skin)).left().top().pad(0, 5, 10, 5);
		add(createOptionsPanel(skin)).left().top().colspan(2).pad(0, 0, 10, 5);
		
		row();
		
		statusBar = new Label(I18n.tr("countryEditor.messages.default"), skin);
		add(statusBar).colspan(3).left();
		
		this.setLand(app.getLand());
		app.listeners.add(appChangeListener);
		Settings.listeners.add(settingsChangeListener);
		pack();
		
		UndoHelper.attachGroup(this, app.getEditManager(), I18n.tr("action.editCountry"))
			.attachActors(mixerPreview);
	}
	
	private final UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.LAND) {
				setLand(app.getLand());
			}
		}
	};
	
	private final UChangeListener settingsChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == Settings.HIGHLIGHT_SELECTED_COUNTRY) {
				shadeUnselectedCheckbox.setChecked(Settings.HIGHLIGHT_SELECTED_COUNTRY.getBool());
			} else if (event.getProperty() == Settings.OUTLINE_SELECTED_COUNTRY) {
				outlineSelectedCheckbox.setChecked(Settings.OUTLINE_SELECTED_COUNTRY.getBool());
			}
		}
	};
	
	private Actor createCountryPanel(MainApp app, Skin skin) {
		Table countryPanel = new Table();
		
		TextButton addCountryButton = new TextButton(I18n.tr("countryEditor.countries.add"), skin);
		addCountryButton.addListener(new ClickListener() {
			@Override
			public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
				materialsEditor.pick(event.getTarget(), 0, 0, new EventListener() {
					@Override
					public boolean handle(Event event2) {
						int matId = materialsEditor.getSelectedIndex();
						if (matId != -1) {
							LNDCountry country = LandTool.createCountry(matId, "");
							land.getCountries().add(country);
						}
						return true;
					}
				});
			}
		});
		countryPanel.add(addCountryButton).growX().padRight(4);
		
		TextButton importCountryButton = new TextButton(I18n.tr("countryEditor.countries.import"), skin);
		importCountryButton.addListener(new ClickListener() {
			@Override
			public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
				app.getOS().getFileChooser().chooseFile(
		            new NativeFileChooserConfiguration() {{
		                title = I18n.tr("dialog.importCountry.title");
		                directory = Gdx.files.absolute(System.getProperty("user.home"));
		                nameFilter = (dir, name) -> name.endsWith(".lnd") || name.endsWith(".lndc");
		                mimeFilter = "All supported formats/lndc,lnd";
		                intent = NativeFileChooserIntent.OPEN;
		            }},
		            new NativeFileChooserCallback() {
		                @Override
		                public void onFileChosen(FileHandle file) {
		                	try {
		                		String ext = file.name().substring(file.name().lastIndexOf('.') + 1).toLowerCase();
			                	if ("lndc".equals(ext)) {
			                		LNDCountry newCountry = LandTool.importCountry(file.file(), land);
			                		setCountry(newCountry);
			                	} else if ("lnd".equals(ext)) {
			                		LndFile srcLand = LndFile.load(file.file(), false);
			                		CountryPicker tmpPicker = new CountryPicker(skin, srcLand, true, true);
			                		tmpPicker.setCallback(new EventListener() {
										@Override
										public boolean handle(Event event) {
											app.getEditManager().begin(I18n.tr("action.importCountries"));
											ArraySelection<LNDCountry> srcCountries = tmpPicker.getSelection();
											for (LNDCountry srcCountry : srcCountries) {
												LNDCountry newCountry = LandTool.importCountry(srcLand, srcCountry.getIndex(), land);
												setCountry(newCountry);
											}
											app.getEditManager().end();
											return true;
										}
									})
			                		.show(getStage());
				                }
		                	} catch (Exception e) {
								e.printStackTrace();
								MessageBox.show(getStage(), I18n.tr("dialog.importCountry.title"), e);
							}
		                }

		                @Override
		                public void onCancellation() {}

		                @Override
		                public void onError(Exception exception) {
		                    exception.printStackTrace();
		                }
		            }
		        );
			}
		});
		countryPanel.add(importCountryButton).growX();
		
		return countryPanel;
	}
	
	private Actor createNoisePanel(Skin skin) {
		Table noisePanel = new Table();
		
		CheckBox noiseCheckbox = new CheckBox(I18n.tr("countryEditor.preview.showNoise"), skin);
		noiseCheckbox.getLabelCell().padLeft(5);
		noiseCheckbox.setChecked(showNoise);
		noiseCheckbox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				showNoise = noiseCheckbox.isChecked();
				updatePreview();
			}
		});
		noisePanel.add(noiseCheckbox).left().top();
		
		TextButton changeNoiseButton = new TextButton(I18n.tr("countryEditor.changeNoiseMap"), skin);
		changeNoiseButton.addListener(new ClickListener() {
			@Override
			public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
				NoiseMapEditor.showSingleInstance(app, getStage(), skin);
			}
		});
		noisePanel.add(changeNoiseButton).padLeft(5);
		
		return noisePanel;
	}
	
	private Actor createOptionsPanel(Skin skin) {
		Table optionsPanel = new Table();
		
		shadeUnselectedCheckbox = new CheckBox(I18n.tr("countryEditor.shadeUnselected"), skin);
		shadeUnselectedCheckbox.getLabelCell().padLeft(5);
		shadeUnselectedCheckbox.setChecked(Settings.HIGHLIGHT_SELECTED_COUNTRY.getBool());
		shadeUnselectedCheckbox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Settings.HIGHLIGHT_SELECTED_COUNTRY.setValue(shadeUnselectedCheckbox.isChecked());
			}
		});
		optionsPanel.add(shadeUnselectedCheckbox).left().top();
		
		outlineSelectedCheckbox = new CheckBox(I18n.tr("countryEditor.outlineSelected"), skin);
		outlineSelectedCheckbox.getLabelCell().padLeft(5);
		outlineSelectedCheckbox.setChecked(Settings.OUTLINE_SELECTED_COUNTRY.getBool());
		outlineSelectedCheckbox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Settings.OUTLINE_SELECTED_COUNTRY.setValue(outlineSelectedCheckbox.isChecked());
			}
		});
		optionsPanel.add(outlineSelectedCheckbox).left().top().padLeft(10);
		
		return optionsPanel;
	}
	
	private final InputListener mixerInputListener = new InputListener() {
		private int startElevation;
		private CtrlPoint dragPoint;
		private CtrlPoint prevPoint;
		private CtrlPoint nextPoint;
		private int prevPointElevation;
		private int nextPointElevation;
		private int prevElevation;
		
		@Override
		public boolean mouseMoved(InputEvent event, float x, float y) {
			if (tool == null) return false;
			int elevation = getElevation(y);
			CtrlPoint point = tool.getPointNear(elevation, 2);
			Gdx.graphics.setSystemCursor(point != null ? SystemCursor.VerticalResize : SystemCursor.Arrow);
			return true;
		}
		
		@Override
		public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
			Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
			super.exit(event, x, y, pointer, toActor);
		}
		
		@Override
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			if (tool == null) return false;
			isDraggingMixer = false;
			startElevation = getElevation(y);
			prevElevation = startElevation;
			dragPoint = tool.getPointNear(startElevation, 2);
			if (dragPoint != null) {
				prevPointElevation = dragPoint.getElevation();
			} else {
				prevPoint = tool.getPointBelow(startElevation);
				nextPoint = prevPoint.getNext();
				prevPointElevation = prevPoint != null ? prevPoint.getElevation() : -1;
				nextPointElevation = nextPoint != null ? nextPoint.getElevation() : -1;
			}
			return true;
		}
		
		@Override
		public void touchDragged(InputEvent event, float x, float y, int pointer) {
			if (tool == null) return;
			event.cancel();
			int elevation = getElevation(y);
			if (elevation != prevElevation) {
				isDraggingMixer = true;
				int dy = elevation - startElevation;
				if (dragPoint != null) {
					dragPoint.setElevation(prevPointElevation + dy);
				} else if (prevPoint != null && nextPoint != null) {
					int e = prevPoint.getElevation();
					boolean r = prevPoint.setElevation(prevPointElevation + dy);
					if (r) {
						r = nextPoint.setElevation(nextPointElevation + dy);
						if (!r) {	//If cannot change next point then rollback
							prevPoint.setElevation(e);
						}
					}
				}
				prevElevation = elevation;
			}
			Gdx.graphics.setSystemCursor(SystemCursor.VerticalResize);
		}
		
		@Override
		public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
			if (tool == null) return;
			if (!isDraggingMixer && button == Buttons.RIGHT) {
				final int elevation = getElevation(y);
				LinkedList<MenuItemAction> actions = new LinkedList<>();
				if (tool.canSplit()) {
					actions.add(new MenuItemAction("splitUp", I18n.tr("countryEditor.mixer.splitUp"), () -> {
						materialsEditor.pick(getStage(), new EventListener() {
    						@Override
    						public boolean handle(Event event2) {
    							Integer matId = materialsEditor.getSelectedIndex();
    							if (matId != null) {
    								tool.splitUp(matId);
    							}
    							return true;
    						}
    					});
                    }));
					actions.add(new MenuItemAction("splitDown", I18n.tr("countryEditor.mixer.splitDown"), () -> {
						materialsEditor.pick(getStage(), new EventListener() {
    						@Override
    						public boolean handle(Event event2) {
    							Integer matId = materialsEditor.getSelectedIndex();
    							if (matId != null) {
    								tool.splitDown(matId);
    							}
    							return true;
    						}
    					});
                    }));
				}
				if (tool.canInsertMaterial(elevation)) {
					actions.add(new MenuItemAction("insertMaterial", I18n.tr("countryEditor.mixer.insertMaterial"), () -> {
                    	materialsEditor.pick(getStage(), new EventListener() {
    						@Override
    						public boolean handle(Event event2) {
    							Integer matId = materialsEditor.getSelectedIndex();
    							if (matId != null) {
    								tool.insertMaterial(elevation, matId);
    							}
    							return true;
    						}
    					});
                    }));
				}
				if (tool.canChangeMaterial(elevation)) {
					int oldMat = tool.getPointBelow(elevation).getFirstMaterial();
					actions.add(new MenuItemAction("changeMaterial", I18n.tr("countryEditor.mixer.changeMaterial"), () -> {
                    	materialsEditor.pick(getStage(), oldMat, new EventListener() {
    						@Override
    						public boolean handle(Event event2) {
    							Integer matId = materialsEditor.getSelectedIndex();
    							if (matId != null) {
    								tool.changeMaterial(elevation, matId);
    							}
    							return true;
    						}
    					});
                    }));
				}
				if (tool.canRemoveMaterial(elevation)) {
					actions.add(new MenuItemAction("removeMaterial", I18n.tr("countryEditor.mixer.removeMaterial"), () -> {
                    	tool.removeMaterial(elevation);
                    }));
				}
				if (!actions.isEmpty()) {
					PopupMenu.show1(mixerPreview, x, y, actions.toArray(new MenuItemAction[0]));
				}
			}
			if (isDraggingMixer) {
				isDraggingMixer = false;
				updateThumbs(true);			//Now we can refresh the thumbs
			}
		}
	};
	
	private void updateThumbs(boolean refresh) {
		if (isDraggingMixer) return;	//Avoid refreshing thumbnails while dragging control points
		//Cleanup
		if (atlasTexture != null) {
			atlasTexture.dispose();
		}
		//
		List<LNDCountry> countries = land.getCountries();
		int count = countries.size();
		int radix = Math.max(1, (int)Math.ceil(Math.sqrt(count)));
		int nx = Math.max(1, count / radix);
		int ny = Math.max(1, MathUtils.ceilDiv(count, nx));
		Pixmap sheet = new Pixmap(nx * THUMBW, ny * THUMBH, Pixmap.Format.RGB888);
		for (int i = 0; i < count; i++) {
			Pixmap pixmap = Utils.toPixmap(app.getDefaultCountryPreviewGenerator().generatePreview(countries.get(i), 256, 512), 256, 512);
			int x = (i % nx) * THUMBW;
	        int y = (i / nx) * THUMBH;
	        sheet.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), x, y, THUMBW, THUMBH);
	        pixmap.dispose();
		}
		
		atlasTexture = new Texture(sheet);
	    sheet.dispose();

	    thumbs = new Array<>(count);
	    for (int i = 0; i < count; i++) {
	        int x = (i % nx) * THUMBW;
	        int y = (i / nx) * THUMBH;
	        thumbs.add(new TextureRegionDrawable(new TextureRegion(atlasTexture, x, y, THUMBW, THUMBH)));
	    }
	    
	    if (refresh) countryList.refresh();
	}
	
	private int getElevation(float y) {
		return MathUtils.clamp(Math.round(y / mixerPreview.getHeight() * 255), 0, 255);
	}
	
	private final UChangeListener landListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LndFile.Property.COUNTRIES) {
				if (event.getType() == EventType.ADD) {
					if (event.isLast()) {
						updateThumbs(false);
						updateCountryList();
						countryList.setSelectedIndex(event.getIndex());
					}
				} else if (event.getType() == EventType.REMOVE) {
					if (event.getOldValue() == country) {
						setCountry(null);
					}
					if (event.getIndex() < 0) {
						thumbs.clear();
					} else {
						thumbs.removeIndex(event.getIndex());
					}
					if (event.isLast()) {
						updateCountryList();
					}
				} else if (event.getType() == EventType.CHANGE) {
					if (event.isItemChange(LNDCountry.Property.NAME)) {
						countryList.refresh(event.getIndex());
					} else if (event.isItemChange(LNDCountry.Property.MAP_MATERIALS) && event.isLast()) {
						updateThumbs(true);	//This will also force refresh, updating aspect based on stats if required
					}
				}
			} else if (event.getProperty() == LndFile.Property.MATERIALS) {
				if (event.getType() == EventType.ADD) {
					LNDMaterial material = (LNDMaterial) event.getNewValue();
					materials.add(material.getIntARGB());
					if (event.isLast()) {
						updateThumbs(true);
					}
				} else if (event.getType() == EventType.REMOVE) {
					if (event.getIndex() < 0) {
						materials.clear();
					} else {
						materials.remove(event.getIndex());
					}
					if (event.isLast()) {
						updateThumbs(true);
					}
				} else if (event.getType() == EventType.CHANGE) {
					LNDMaterial material = (LNDMaterial) event.getNewValue();
					materials.set(event.getIndex(), material.getIntARGB());
					if (event.isItemChange(LNDMaterial.Property.TEXELS)) {
						if (country.usesMaterial(material.getIndex())) {
							updateSelectedCountryView();
						}
						if (event.isLast()) {
							updateThumbs(true);
						}
					}
				}
			} else if (event.getProperty() == LndFile.Property.BLOCKS && event.isLast()) {
				countryList.refresh();
			} else if (event.getProperty() == LndFile.Property.NOISE_MAP && event.isLast()) {
				updatePreview();
				updateThumbs(true);
			}
		}
	};
	
	private void updateCountryList() {
		updatingList = true;
		int prevSelected = country != null ? country.getIndex() : 0;
		Array<LNDCountry> items = new Array<>();
		if (land != null) {
			for (LNDCountry country : land.getCountries()) {
				items.add(country);
			}
		}
		countryList.setItems(items);
		updatingList = false;
		countryList.setSelectedIndex(Math.min(prevSelected, (land != null ? land.getCountries().size() : 0) - 1));
	}
	
	public CountryEditor setLand(LndFile land) {
		//Cleanup
		if (this.land != null) {
			this.land.listeners.remove(landListener);
		}
		this.setCountry(null);
		if (this.materialsEditor != null) {
			this.materialsEditor.dispose();
			this.materialsEditor = null;
		}
		if (this.countryPicker != null) {
			this.countryPicker.dispose();
			this.countryPicker = null;
		}
		//Set fields
		this.land = land;
		if (land != null) {
			this.land.listeners.add(landListener);
			this.materialsEditor = new MaterialsEditor(app, getSkin(), false).setLand(land);
			this.countryPicker = new CountryPicker(getSkin(), land, false);
			this.materials = new ArrayList<>(land.getMaterials().size());
			for (LNDMaterial material : land.getMaterials()) {
				this.materials.add(material.getIntARGB());
			}
			updateThumbs(false);
		}
		updateCountryList();
		
		setCountry(countryList.getSelected());
		return this;
	}
	
	private final UChangeListener selectedCountryChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			updateSelectedCountryView();
		}
	};
	
	public void setCountry(LNDCountry country) {
		if (country != this.country) {
			//Remove previous listeners
			if (this.country != null) {
				this.country.listeners.remove(selectedCountryChangeListener);
				this.tool = null;
			}
			//Update fields
			this.country = country;
			if (this.land != null) {
				this.app.getView3D().setCountryToHighlight(country);
			}
			//Attach new listeners
			if (this.country != null) {
				country.listeners.add(selectedCountryChangeListener);
				this.tool = new CountryTool(country);
			}
			//
			this.updateSelectedCountryView();
		}
	}
	
	private void updateSelectedCountryView() {
		updatePreview();
		updateMixer();
	}
	
	private void updatePreview() {
		if (this.country != null) {
			preview.paint(app.getDefaultCountryPreviewGenerator().generatePreview(this.country, preview.getPixmapWidth(), preview.getPixmapHeight(), showNoise));
		} else {
			preview.clear(Color.BLACK);
		}
		preview.refresh();
	}
	
	private void updateMixer() {
		Pixmap mixerPixmap = mixerPreview.getPixmap();
		mixerPreview.clear(Color.BLACK);
		if (this.country != null) {
			final int h1 = mixerPixmap.getHeight() - 1;
			final int w1 = mixerPixmap.getWidth() - 1;
			final int halfW = mixerPixmap.getWidth() / 2;
			LNDMapMaterial[] mapMaterials = this.country.getMapMaterialsForRead();
			Color col0 = new Color();
			Color col1 = new Color();
			Color mixed = new Color();
			//Draw mixer materials
			for (int y = 0; y < mixerPixmap.getHeight(); y++) {
				int ty = y & 0xFF;
				final int altitude = (int)(((float)(h1 - y) / h1) * 255.99f);
				final LNDMapMaterial mapMaterial = mapMaterials[altitude];
				float blend = mapMaterial.getBlend();
				int mat0Id = mapMaterial.getFirstMaterialIndex();
				int mat1Id = mapMaterial.getSecondMaterialIndex();
				int[] mat0 = this.materials.get(mat0Id);
				int[] mat1 = this.materials.get(mat1Id);
				for (int x = 0; x < mixerPixmap.getWidth(); x++) {
					int tx = x & 0xFF;
					int rgb0 = mat0[ty * LNDMaterial.width + tx];
					int rgb1 = mat1[ty * LNDMaterial.width + tx];
					Color.argb8888ToColor(col0, rgb0);
					Color.argb8888ToColor(col1, rgb1);
					mixed.set(col0).lerp(col1, blend);
					mixed.a = 1f;
					mixerPixmap.drawPixel(x, y, Color.rgba8888(x < halfW ? col0 : col1));
				}
			}
			//Draw lines at control points
			mixerPixmap.setColor(Color.GRAY);
			for (CtrlPoint point = tool.getFirstPoint().getNext(); point.getNext() != null; point = point.getNext()) {
				int y = h1 - (int)(point.getElevation() / 255.99f * h1);
				mixerPixmap.drawLine(0, y, w1, y);
			}
			//Draw blend lines
			mixerPixmap.setColor(Color.RED);
			CtrlPoint point = tool.getFirstPoint();
			int prevX = Math.round(point.getBlend() * w1);
			int prevY = h1 - (int)(point.getElevation() / 255.99f * h1);
			for (point = point.getNext(); point != null; point = point.getNext()) {
				int x = Math.round(point.getBlend() * w1);
				int y = h1 - (int)(point.getElevation() / 255.99f * h1);
				if (point.getPrev().getFirstMaterial() != point.getPrev().getSecondMaterial()) {
					mixerPixmap.drawLine(prevX, prevY, x, y);
				}
				prevX = x;
				prevY = y;
			}
		}
		mixerPreview.refresh();
	}
	
	@Override
	protected void sizeChanged() {
		super.sizeChanged();
		if (preview != null && mixerPreview != null) {
			updateSelectedCountryView();
		}
	}
	
	@Override
	public boolean remove() {
		boolean removed = super.remove();
		if (removed) {
			setLand(null);
			if (materialsEditor != null) {
				materialsEditor.dispose();
				materialsEditor = null;
			}
			this.notify(new RemoveEvent(this), false);
		}
		return removed;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		Settings.listeners.remove(settingsChangeListener);
		app.listeners.remove(appChangeListener);
		if (this.atlasTexture != null) {
			this.atlasTexture.dispose();
			this.atlasTexture = null;
		}
		super.dispose();
	}
}
