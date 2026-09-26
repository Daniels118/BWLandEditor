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
package it.ld.bw.lndgui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.utils.Null;

import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import it.ld.bw.info.TribeType;
import it.ld.bw.lhx.Command;
import it.ld.bw.lhx.LHXCoord;
import it.ld.bw.lhx.LHXFile;
import it.ld.bw.lhx.Statement;
import it.ld.bw.lnd.model.BulkUpdate;
import it.ld.bw.lnd.model.LH3DLandBlock;
import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.SimpleMap;
import it.ld.bw.lnd.tools.CountryPreviewGenerator;
import it.ld.bw.lnd.tools.HeightMapTool;
import it.ld.bw.lnd.tools.LandTool;
import it.ld.bw.lnd.tools.LandTool.CountryMatch;
import it.ld.bw.lnd.tools.ShadowTool;
import it.ld.bw.lndgui.gfx.Block3D;
import it.ld.bw.lndgui.gfx.Coord;
import it.ld.bw.lndgui.gfx.L3DModelManager;
import it.ld.bw.lndgui.gfx.L3DPreviewManager;
import it.ld.bw.lndgui.gfx.Object3D;
import it.ld.bw.lndgui.gfx.L3DModelManager.ModelInfo;
import it.ld.bw.lndgui.interfaces.ClipboardListener;
import it.ld.bw.lndgui.interfaces.ExternalEditor;
import it.ld.bw.lndgui.interfaces.OS;
import it.ld.bw.lndgui.interfaces.ClipboardListener.DataType;
import it.ld.bw.lndgui.tools.CountryBrush;
import it.ld.bw.lndgui.tools.SculptBrush;
import it.ld.bw.lndgui.tools.SculptBrush.Mode;
import it.ld.bw.lndgui.tools.SoundBrush;
import it.ld.bw.lndgui.ui.ImportHeightMapWindow;
import it.ld.bw.lndgui.ui.ImportLandWindow;
import it.ld.bw.lndgui.ui.OutputWindow;
import it.ld.bw.lndgui.ui.SettingsWindow;
import it.ld.bw.lndgui.ui.EditSelectionWindow;
import it.ld.bw.lndgui.ui.UIScreen;
import it.ld.bw.lndgui.ui.View3D;
import it.ld.bw.lndgui.ui.View3D.CoordCallback;
import it.ld.bw.serializer.FotFile;
import it.ld.libgdx.ui.components.MessageBox;
import it.ld.libgdx.ui.components.PopupMenu;
import it.ld.libgdx.ui.components.Prompt;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.docking.DockingManager;
import it.ld.libgdx.ui.components.Prompt.PromptListener;
import it.ld.libgdx.ui.components.MessageBox.Choice;
import it.ld.libgdx.ui.components.MessageBox.MessageType;
import it.ld.libgdx.ui.components.MessageBox.Options;
import it.ld.libgdx.utils.ShaderLoader;
import it.ld.libgdx.utils.Utils;
import it.ld.utils.ConsumerOutputStream;
import it.ld.utils.ControlledList;
import it.ld.utils.ControlledList.ListControllerAdapter;
import it.ld.utils.Listeners;
import it.ld.utils.PlaceHolderEdit;
import it.ld.utils.UChangeListener;
import it.ld.utils.UChangeListener.EventType;
import it.ld.utils.UndoableEdit;

public class MainApp extends Game {
	public static final String APP_NAME = "Black & White Land Editor";
	
	public enum Property {
		SMALL_BUMP, SKY_TEXTURE, LHX, LAND, FOOTPATHS, LAND_CHANGED, LHX_CHANGED, UNDO_HISTORY,
		EDIT_MODE, SELECTION_MODE, ACTIVE_TOOL,
		SCULPT_BRUSHES, SCULPT_BRUSH, COUNTRY_BRUSHES, COUNTRY_BRUSH, OCEAN_BRUSH, LAKE_BRUSH, SOUND_BRUSH,
		CLIPBOARD
	}
	public final Listeners listeners = new Listeners(this);
	
	private final OS os;
	private final ExternalEditor externalEditor;
	private final ConsumerOutputStream uiOut;
	
	private final AssetManager assetManager = new AssetManager();
	private Skin skin;
	
	private UIScreen uiScreen;
	
	private File fileToOpen;
	private LHXFile lhx;
	private boolean changingLHX = false;
	private LndFile land;
	private boolean changingLand = false;
	private FotFile footpaths;
	private float timeFromLastLandSave;
	private float timeFromLastLHXSave;
	private boolean allowAutosave = true;
	
	private final RecoveryManager recoveryManager = new RecoveryManager(this);
	private final LandEditManager editManager = new LandEditManager(this);
	private final MetaManager metaManager = new MetaManager(this);
	
	private final Map<Integer, Integer> countryMappingCache = new HashMap<>();
	
	private boolean unsavedLandChanges = false;
	private boolean unsavedLHXChanges = false;
	private EditMode editMode = EditMode.SCULPT;
	private SelectionMode selectionMode = SelectionMode.NEW;
	
	private Map<EditMode, Tool> activeTool = new HashMap<>();
	
	private ControlledList<SculptBrush> sculptBrushes = new ControlledList<SculptBrush>(new ListControllerAdapter<SculptBrush>() {
		/*public boolean beforeAdd(int index, SculptBrush brush) {
			for (SculptBrush tmp : sculptBrushes) {
				if (tmp.getName().equalsIgnoreCase(brush.getName())) {
					throw new IllegalArgumentException(I18n.tr("brushes.add.exists"));
				}
			}
			return true;
		};*/
		
		public void afterAdd(int index, SculptBrush brush) {
			brush.setLand(land);
			brush.listeners.add(sculptBrushChangeListener);
			listeners.notify(EventType.ADD, Property.SCULPT_BRUSHES, null, brush, index);
			if (sculptBrush == null) {
				setSculptBrush(brush);
			}
		};
		
		public boolean beforeRemove(int index, SculptBrush brush) {
			if (brush == sculptBrush) {
				if (sculptBrushes.size() > 2) {
					int newIndex = MathUtils.clamp(index, 0, sculptBrushes.size() - 2);
					setSculptBrush(sculptBrushes.get(newIndex));
				} else {
					setSculptBrush(null);
				}
			}
			return true;
		};
		
		public void afterRemove(int index, SculptBrush brush) {
			brush.close();
			brush.listeners.remove(sculptBrushChangeListener);
			listeners.notify(EventType.REMOVE, Property.SCULPT_BRUSHES, brush, null, index);
		};
	});
	
	private final UChangeListener sculptBrushChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			listeners.notify(EventType.CHANGE, Property.SCULPT_BRUSHES, event.getOldValue(), event.getNewValue(), event.getIndex(), event);
		}
	};
	
	private SculptBrush sculptBrush;
	
	private final SculptBrush oceanBrush;
	private final UChangeListener oceanBrushChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			listeners.notify(EventType.CHANGE, Property.OCEAN_BRUSH, event.getOldValue(), event.getNewValue(), event.getIndex(), event);
		}
	};
	
	private final SculptBrush lakeBrush;
	private final UChangeListener lakeBrushChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			listeners.notify(EventType.CHANGE, Property.LAKE_BRUSH, event.getOldValue(), event.getNewValue(), event.getIndex(), event);
		}
	};
	
	private ControlledList<CountryBrush> countryBrushes = new ControlledList<CountryBrush>(new ListControllerAdapter<CountryBrush>() {
		/*public boolean beforeAdd(int index, CountryBrush brush) {
			for (CountryBrush tmp : countryBrushes) {
				if (tmp.getName().equalsIgnoreCase(brush.getName())) {
					throw new IllegalArgumentException(I18n.tr("brushes.add.exists"));
				}
			}
			return true;
		};*/
		
		public void afterAdd(int index, CountryBrush brush) {
			brush.setLand(land);
			brush.listeners.add(countryBrushChangeListener);
			listeners.notify(EventType.ADD, Property.COUNTRY_BRUSHES, null, brush, index);
			if (countryBrush == null) {
				setCountryBrush(brush);
			}
		};
		
		public boolean beforeRemove(int index, CountryBrush brush) {
			if (brush == countryBrush) {
				if (countryBrushes.size() > 2) {
					int newIndex = MathUtils.clamp(index, 0, countryBrushes.size() - 2);
					setCountryBrush(countryBrushes.get(newIndex));
				} else {
					setCountryBrush(null);
				}
			}
			return true;
		};
		
		public void afterRemove(int index, CountryBrush brush) {
			brush.close();
			brush.listeners.remove(countryBrushChangeListener);
			listeners.notify(EventType.REMOVE, Property.COUNTRY_BRUSHES, brush, null, index);
		};
	});
	
	private final UChangeListener countryBrushChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			listeners.notify(EventType.CHANGE, Property.COUNTRY_BRUSHES, event.getOldValue(), event.getNewValue(), event.getIndex(), event);
		}
	};
	
	private CountryBrush countryBrush;
	
	private final SoundBrush soundBrush;
	private final UChangeListener soundBrushChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			listeners.notify(EventType.CHANGE, Property.SOUND_BRUSH, event.getOldValue(), event.getNewValue(), event.getIndex(), event);
		}
	};
	
	private Texture smallBump;
	private Texture skyTexture;
	
	private CountryPreviewGenerator countryPreviewGenerator;
	
	private L3DModelManager modelManager;
	private L3DPreviewManager previewManager;
	
	
	public MainApp(OS os, ExternalEditor externalEditor, ConsumerOutputStream uiOut) {
		this.os = os;
		this.externalEditor = externalEditor;
		this.uiOut = uiOut;
		
		os.setClipboardListener(clipboardListener);
		
		this.oceanBrush = new SculptBrush();
		oceanBrush.setMode(Mode.ABSOLUTE);
		oceanBrush.setElevation(LH3DLandCell.DEEP_WATER_HEIGHT);
		oceanBrush.setMaxDepth(LH3DLandCell.DEEP_WATER_ALTITUDE);
		oceanBrush.setFlow(LH3DLandCell.getMaxHeight(8) * 10);
		oceanBrush.setSmoothness(1f);
		oceanBrush.listeners.add(oceanBrushChangeListener);
		
		this.lakeBrush = new SculptBrush();
		lakeBrush.setLake(true);
		lakeBrush.setMode(Mode.ABSOLUTE);
		lakeBrush.setElevation(LH3DLandCell.DEEP_WATER_HEIGHT);
		lakeBrush.setMaxDepth(LH3DLandCell.DEEP_WATER_ALTITUDE);
		lakeBrush.setFlow(LH3DLandCell.getMaxHeight(8) * 10);
		lakeBrush.setSmoothness(1f);
		lakeBrush.listeners.add(lakeBrushChangeListener);
		
		this.soundBrush = new SoundBrush();
		soundBrush.listeners.add(soundBrushChangeListener);
		
		activeTool.put(EditMode.SCULPT, Tool.ORBIT);
		activeTool.put(EditMode.FOOTPATH, Tool.ORBIT);
		activeTool.put(EditMode.OBJ_PLACEMENT, Tool.POINTER);
		activeTool.put(EditMode.SCRIPT, Tool.ORBIT);
		
		InternalFileHandleResolver ifhr = new InternalFileHandleResolver();
		assetManager.setLoader(ShaderProgram.class, ".glsl", new ShaderLoader(ifhr));
		assetManager.load("shaders/land.glsl", ShaderProgram.class);
		assetManager.load("shaders/sky.glsl", ShaderProgram.class);
		assetManager.load("shaders/selection.glsl", ShaderProgram.class);
		assetManager.load("icons/marker.png", Texture.class);
		assetManager.load("textures/compass.png", Texture.class);
	}
	
	private final ClipboardListener clipboardListener = new ClipboardListener() {
		@Override
		public void contentsChanged(DataType type, Object data) {
			listeners.notify(EventType.CHANGE, Property.CLIPBOARD, null, data);
		}
	};
	
	private final UChangeListener settingsChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == Settings.LANGUAGE) {
				I18n.setLocale(((Language)Settings.LANGUAGE.getValue()).getLocale());
			} else if (event.getProperty() == Settings.GAME_DIR) {
				reloadGameFiles();
			} else if (event.getProperty() == Settings.MAX_UNDO) {
				if ((int)event.getNewValue() < (int)event.getOldValue()) {
					listeners.notify(EventType.CHANGE, Property.UNDO_HISTORY);
				}
			}
		}
	};
	
	public CountryPreviewGenerator getDefaultCountryPreviewGenerator() {
		return this.countryPreviewGenerator;
	}
	
	private void reloadGameFiles() {
		reloadGameTextures();
		reloadAllMeshes();
	}
	
	private void reloadAllMeshes() {
		if (!Settings.GAME_DIR.getString().isEmpty()) {
			try {
				if (modelManager != null) {
					modelManager.dispose();
					modelManager = null;
				}
				modelManager = new L3DModelManager(Settings.GAME_DIR.getFile(), getSkin());
			} catch (Exception e) {
				e.printStackTrace();
				showError("Error", e);
			}
		}
	}
	
	private void reloadGameTextures() {
		smallBump = reloadGameTexture(smallBump, "smallbumpa.raw");
		listeners.notify(EventType.CHANGE, Property.SMALL_BUMP);
		skyTexture = reloadGameTexture(skyTexture, "Sky.raw");
		listeners.notify(EventType.CHANGE, Property.SKY_TEXTURE);
	}
	
	private void disposeGameTextures() {
		if (smallBump != null && !smallBump.isManaged()) {
    		smallBump.dispose();
    		smallBump.getTextureData().disposePixmap();
    	}
    	if (skyTexture != null && !skyTexture.isManaged()) {
    		skyTexture.dispose();
    		skyTexture.getTextureData().disposePixmap();
    	}
	}
	
	private Texture reloadGameTexture(Texture texture, String path) {
		if (texture != null && !texture.isManaged()) {
			texture.getTextureData().disposePixmap();
			texture.dispose();
		}
		texture = null;
		//Try to load the texture from the game folder
		if (!Settings.GAME_DIR.getString().isEmpty()) {
	    	try {
	    		File file = new File(Settings.GAME_DIR.getFile(), "Data/Textures/" + path);
				texture = Utils.toTexture(Files.readAllBytes(file.toPath()), SimpleMap.width, SimpleMap.height, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
		//If fails, fallback to embedded copy
		if (texture == null) {
			try {
				texture = Utils.toTexture(Gdx.files.internal("textures/" + path).readBytes(), SimpleMap.width, SimpleMap.height, false);
			} catch (Exception e) {
				e.printStackTrace();
				texture = new Texture(1, 1, Format.RGBA8888);
			}
		}
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    	return texture;
	}
	
	public L3DModelManager getModelManager() {
		return modelManager;
	}
	
	public L3DPreviewManager getPreviewManager() {
		return previewManager;
	}
	
	public OS getOS() {
		return os;
	}
	
	public ExternalEditor getExternalEditor() {
		return this.externalEditor;
	}
	
	public ConsumerOutputStream getStdOut() {
		return this.uiOut;
	}
	
	public AssetManager getAssetManager() {
		return this.assetManager;
	}
	
	public Skin getSkin() {
		return this.skin;
	}
	
	/**Disables all UI components except the 3D view and an optional window.
	 * Useful when you want to give to the user the ability to interact with the 3D view, without breaking the intended workflow.
	 * @param window
	 */
	public void disableAllExcept3DViewAnd(SmartWindow window) {
		uiScreen.disableAllExcept3DViewAnd(window);
	}
	
	/**Enable the whole UI again after a call to {@linkplain #disableAllExcept3DViewAnd(SmartWindow)}.
	 * 
	 */
	public void enableAll() {
		uiScreen.enableAll();
	}
	
	public void showError(String title, Throwable e) {
		uiScreen.showError(title, e);
	}
	
	public void showError(String title, Throwable e, Runnable callback) {
		uiScreen.showError(title, e, callback);
	}
	
	public View3D getView3D() {
		return uiScreen.getView3D();
	}
	
	public DockingManager getDockingManager() {
		return uiScreen.getDockingManager();
	}
	
	private Stage getStage() {
		return uiScreen.getStage();
	}
	
	/**Used internally by the app loader to set the file to open when the UI is ready. Don't call this method.
	 * @param file
	 */
	public void setFileToOpen(File file) {
		this.fileToOpen = file;
	}
	
	/**Create a new empty project, asking to save the pending changes if any.
	 */
	public void newProject() {
		newProject(true, null);
	}
	
	/**Create a new empty project, asking to save the pending changes if any.
	 * @param callback called only if the new project has been set (i.e. the user hasn't canceled the operation).
	 */
	public void newProject(Runnable callback) {
		newProject(true, callback);
	}
	
	public boolean isGameDirValid() {
		File gameDir = Settings.GAME_DIR.getFile();
		if (gameDir == null) return false;
		File exe = new File(gameDir, "runblack.exe");
		return exe.isFile();
	}
	
	/**See {@link #getLHXLandscapeSafe(LHXFile)}.
	 * @return
	 */
	public File getLHXLandscapeSafe() {
		return getLHXLandscapeSafe(this.lhx);
	}
	
	/**Returns the real file pointed by the LOAD_LANDSCAPE statement.
	 * A check is done to verify that the file is a descendant of the game dir, to avoid potential directory traversal attacks.
	 * No check is done on the existence of the file.
	 * If the game dir is not set, returns null.
	 * @param lhxFile
	 * @return an absolute file, or null if the validation fails.
	 */
	public File getLHXLandscapeSafe(LHXFile lhxFile) {
		if (lhxFile != null) {
			File gameDir = Settings.GAME_DIR.getFile();
			String landscape = lhxFile.getLandscape();
			if (gameDir != null && landscape != null) {
				Path gamePath = gameDir.toPath().normalize();
				Path landPath = gamePath.resolve(landscape).normalize();
				if (landPath.startsWith(gamePath)) {
					return landPath.toFile();
				}
			}
		}
		return null;
	}
	
	/**
	 * @param askSavePending
	 * @param callback
	 * @return
	 */
	public boolean newProject(boolean askSavePending, Runnable callback) {
		if (hasUnsavedChanges() && askSavePending) {
			MessageBox.show(getStage(), I18n.tr("dialog.new"), I18n.tr("dialog.unsavedChanges"), MessageType.ASK, Options.YES_NO_CANCEL, (choice) -> {
				if (choice == Choice.YES) {
					save(() -> newProject(false, callback));
				} else if (choice == Choice.NO) {
					newProject(false, callback);
				}
			});
		} else {
			LndFile newLand = new LndFile(true);
			newLand.getCoastlineBumpMap().setPixels(Gdx.files.internal("textures/bumpmap.raw").readBytes());
			newLand.getNoiseMap().setPixels(Gdx.files.internal("textures/noisemap.raw").readBytes());
			setLand(newLand);
			setLHX(new LHXFile(), false);
			//
			if (callback != null) callback.run();
			return true;
		}
		return false;
	}
	
	/**Save the current project, asking the user the files location if needed.
	 */
	public void save() {
		save(null);
	}
	
	/**Save the current project, asking the user the files location if needed.
	 * @param callback called after all the files have been successfully saved.
	 */
	public void save(Runnable callback) {
		Runnable saveLandAfter = () -> {
			if (this.unsavedLandChanges) {
				try {
					if (land.getFile() != null) {
						saveLand();
						if (callback != null) callback.run();
					} else {
						saveLandAsDialog(callback);
					}
				} catch (Exception e) {
					e.printStackTrace();
					showError(I18n.tr("dialog.save"), e);
				}
			} else {
				 if (callback != null) callback.run();
			}
		};
		//
		if (this.unsavedLHXChanges) {
			if (lhx.getFile() != null) {
				try {
					saveLHX();
					saveLandAfter.run();
				} catch (Exception e) {
					e.printStackTrace();
					showError(I18n.tr("dialog.save"), e);
				}
			} else {
				saveLHXAsDialog(saveLandAfter);
			}
		} else {
			saveLandAfter.run();
		}
	}
	
	/**Saves the LHX script after prompting the user for the file location.
	 */
	public void saveLHXAsDialog() {
		saveLHXAsDialog(null);
	}
	
	/**Saves the landscape after prompting the user for the file location.
	 */
	public void saveLandAsDialog() {
		saveLandAsDialog(null);
	}
	
	/**Saves the landscape after prompting the user for the file location.
	 * @param callback called after the file has been successfully saved.
	 */
	public void saveLandAsDialog(Runnable callback) {
		os.getFileChooser().chooseFile(
            new NativeFileChooserConfiguration() {{
                title = I18n.tr("dialog.saveLandscape.title");
                directory = Gdx.files.absolute(System.getProperty("user.home"));
                nameFilter = (dir, name) -> name.endsWith(".lnd");
                mimeFilter = "Landscape files/lnd";
                intent = NativeFileChooserIntent.SAVE;
            }},
            new NativeFileChooserCallback() {
                @Override
                public void onFileChosen(FileHandle file) {
                    try {
						saveLand(file.file());
						if (callback != null) callback.run();
					} catch (Exception e) {
						e.printStackTrace();
						showError(I18n.tr("dialog.save"), e);
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
	
	/**Saves the LHX script after prompting the user for the file location.
	 * @param callback called after the file has been successfully saved.
	 */
	public void saveLHXAsDialog(Runnable callback) {
		os.getFileChooser().chooseFile(
            new NativeFileChooserConfiguration() {{
                title = I18n.tr("dialog.saveLHX.title");
                directory = Gdx.files.absolute(System.getProperty("user.home"));
                nameFilter = (dir, name) -> name.endsWith(".txt");
                mimeFilter = "Land init scripts/txt";
                intent = NativeFileChooserIntent.SAVE;
            }},
            new NativeFileChooserCallback() {
                @Override
                public void onFileChosen(FileHandle file) {
                    try {
						saveLHX(file.file());
						if (callback != null) callback.run();
					} catch (Exception e) {
						e.printStackTrace();
						showError(I18n.tr("dialog.save"), e);
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
	
	/**Prompt the user for a file location and then saves the heightmap to that file.
	 */
	public void exportHeightMapDialog() {
		exportHeightMapDialog(null);
	}
	
	/**Prompt the user for a file location and then saves the heightmap to that file.
	 * @param callback callback called after the file has been successfully saved.
	 */
	public void exportHeightMapDialog(Runnable callback) {
		os.getFileChooser().chooseFile(
            new NativeFileChooserConfiguration() {{
                title = I18n.tr("dialog.exportHeightMap");
                directory = Gdx.files.absolute(System.getProperty("user.home"));
                nameFilter = (dir, name) -> name.endsWith(".png");
                mimeFilter = "PNG images/png";
                intent = NativeFileChooserIntent.SAVE;
            }},
            new NativeFileChooserCallback() {
                @Override
                public void onFileChosen(FileHandle file) {
                    try {
                    	HeightMapTool tool = new HeightMapTool(land);
						tool.exportHeightMap(file.file());
						if (callback != null) callback.run();
					} catch (Exception e) {
						e.printStackTrace();
						showError(I18n.tr("dialog.exportHeightMap"), e);
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
	
	/**Prompt the user for a landscape file to import in the current project, without erasing the current landscape.
	 * The user is also asked how to map the imported materials to the existing materials, and then to choose a location where to drop the imported landscape.
	 */
	public void importLandDialog() {
		ImportLandWindow.showSingleInstance(this, getStage(), skin);
	}
	
	/**Prompt the user for a heightmap file to import in the current project, without erasing the current landscape, and
	 * then to choose a location where to drop the imported landscape.
	 */
	public void importHeightMapDialog() {
		ImportHeightMapWindow.showSingleInstance(this, getStage(), skin);
	}
	
	/**Prompt the user for a file to open. If there are pending changes, the user is asked to save them.
	 */
	public void openLandDialog() {
		openLandDialog(null);
	}
	
	/**Prompt the user for a file to open. If there are pending changes, the user is asked to save them.
	 * @param callback called after the file has been successfully opened.
	 */
	public void openLandDialog(Runnable callback) {
		openLandDialog(true, callback);
	}
	
	/**Prompt the user for a file to open. Optionally ask the user to save pending changes.
	 * @param askSavePending if true, and there are pending changes, the user is asked to save them.
	 * @param callback callback called after the file has been successfully opened.
	 */
	public void openLandDialog(boolean askSavePending, Runnable callback) {
		if (hasUnsavedChanges() && askSavePending) {
			MessageBox.show(getStage(), I18n.tr("dialog.open"), I18n.tr("dialog.unsavedChanges"), MessageType.ASK, Options.YES_NO_CANCEL, (choice) -> {
				if (choice == Choice.YES) {
					save(() -> openLandDialog(false, callback));
				} else if (choice == Choice.NO) {
					openLandDialog(false, callback);
				}
			});
		} else {
			os.getFileChooser().chooseFile(
	            new NativeFileChooserConfiguration() {{
	                title = I18n.tr("dialog.openLandscape.title");
	                directory = Gdx.files.absolute(System.getProperty("user.home"));
	                nameFilter = (dir, name) -> {
	                	String ext = name.replaceFirst("^.*\\.", "").toLowerCase();
	                	return "lnd".equals(ext) || "txt".equals(ext);
	                };
	                mimeFilter = "Landscape files/lnd,txt";
	                intent = NativeFileChooserIntent.OPEN;
	            }},
	            new NativeFileChooserCallback() {
	                @Override
	                public void onFileChosen(FileHandle file) {
	                    openFile(file.file());
	                    if (callback != null) callback.run();
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
    }
	
	/**Ask the user to enter a new grid size for the current landscape. Sizes other than 32 require a game mod.
	 */
	public void resizeGridDialog() {
		final int MIN = 32;
		final int MAX = 128;
		Prompt.show(getStage(), skin, I18n.getInstance(), I18n.tr("edit.blocksGrid.title"), I18n.tr("edit.blocksGrid.prompt", MIN, MAX),
				String.valueOf(land.getBlocksPerSide()),
		new PromptListener() {
			@Override
			public void confirm(String value) {
				try {
					int n = Integer.parseInt(value);
					if (n < MIN || n > MAX) throw new IllegalArgumentException(I18n.tr("messages.outOfRangeII", MIN, MAX));
					editManager.begin(I18n.tr("action.resizeBlocksGrid"));
					land.setBlocksPerSide(n);
					editManager.end();
				} catch (Exception e) {
					e.printStackTrace();
					showError(I18n.tr("edit.blocksGrid.title"), e);
				}
			}
		});
	}
	
	/**Ask the user to enter a new limit for the number of blocks for the current landscape. Limits other than 256 require a game mod.
	 */
	public void editMaxBlocksDialog() {
		final int MIN = 255;
		final int MAX = 16384;
		Prompt.show(getStage(), skin, I18n.getInstance(), I18n.tr("edit.maxBlocks.title"), I18n.tr("edit.maxBlocks.prompt", MIN, MAX),
				String.valueOf(land.getMaxBlocks()),
		new PromptListener() {
			@Override
			public void confirm(String value) {
				try {
					int n = Integer.parseInt(value);
					if (n < MIN || n > MAX) throw new IllegalArgumentException(I18n.tr("messages.outOfRangeII", MIN, MAX));
					land.setMaxBlocks(n);
				} catch (Exception e) {
					e.printStackTrace();
					showError(I18n.tr("edit.maxBlocks.title"), e);
				}
			}
		});
	}
	
	/**Ask the user to enter a new max altitude for the current landscape. Values other than 8 bits require a game mod.
	 */
	public void editMaxAltitudeDialog() {
		final int MIN = 8;
		final int MAX = 16;
		final Stage stage = getStage();
		Prompt.show(stage, skin, I18n.getInstance(), I18n.tr("edit.maxAltitude.title"), I18n.tr("edit.maxAltitude.prompt", MIN, MAX),
				String.valueOf(land.getAltitudeBits()),
		new PromptListener() {
			@Override
			public void confirm(String value) {
				try {
					final int n = Integer.parseInt(value);
					if (n < MIN || n > MAX) throw new IllegalArgumentException(I18n.tr("messages.outOfRangeII", MIN, MAX));
					final int newMaxAltitude = LH3DLandCell.getMaxAltitude(n);
					int maxAltitude = land.getMaxAltitude();
					if (maxAltitude <= newMaxAltitude) {
						setAltitudeBits(n);
					} else {
						MessageBox.show(stage, I18n.tr("edit.maxAltitude.title"), I18n.tr("edit.maxAltitude.confirmTruncation"), MessageType.WARN, Options.YES_NO, (choice) -> {
							if (choice == Choice.YES) {
								setAltitudeBits(n);
							}
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
					showError(I18n.tr("edit.maxAltitude.title"), e);
				}
			}
		});
	}
	
	/**Sets the max altitude bits for the current landscape. Values other than 8 require a game mod.
	 * @param bits
	 */
	public void setAltitudeBits(int bits) {
		editManager.begin(I18n.tr("action.editMaxAltitude"));
		land.setAltitudeBits(bits);
		editManager.end();
	}
	
	/**Close the program, asking the user to save any pending change.
	 * @return true if the program will close immediately (i.e. there are no prompts for the user).
	 */
	public boolean exit() {
		return exit(true, null);
	}
	
	/**Close the program, asking the user to save any pending change.
	 * @param callback called when the program is going to close (i.e. the user hasn't canceled the operation).
	 * @return true if the program will close immediately (i.e. there are no prompts for the user).
	 */
	public boolean exit(Runnable callback) {
		return exit(true, callback);
	}
	
	private boolean exitPromptActive = false;
	
	/**Close the program, optionally asking the user to save any pending change.
	 * @param askSavePending
	 * @param callback called when the program is going to close (i.e. the user hasn't canceled the operation).
	 * @return true if the program will close immediately (i.e. there are no prompts for the user).
	 */
	public boolean exit(boolean askSavePending, Runnable callback) {
		if (exitPromptActive) return false;
		if (hasUnsavedChanges() && askSavePending) {
			exitPromptActive = true;
			MessageBox.show(getStage(), I18n.tr("dialog.exit"), I18n.tr("dialog.unsavedChanges"), MessageType.ASK, Options.YES_NO_CANCEL, (choice) -> {
				exitPromptActive = false;
				if (choice == Choice.YES) {
					save(() -> exit(false, callback));
				} else if (choice == Choice.NO) {
					exit(false, callback);
				}
			});
		} else {
			setLHX(null, false);
			setLand(null);
			if (callback != null) callback.run();
			Gdx.app.exit();
			return true;
		}
		return false;
	}
	
	/**Opens the given file, optionally asking the user to save any pending change
	 * @param file the file to open.
	 * @param askSavePending
	 * @param callback called after the file has been successfully opened.
	 * @return
	 */
	public boolean openFile(File file, boolean askSavePending, Runnable callback) {
		String ext = file.getName().replaceFirst("^.*\\.", "").toLowerCase();
		if ("txt".equals(ext)) {
			return openLHX(file, askSavePending, false, callback);
		} else if ("lnd".equals(ext)) {
			return openLand(file, askSavePending, true, callback);
		} else {
			MessageBox.show(getStage(), I18n.tr("dialog.open"), I18n.tr("error.unsupportedFileFormat"), MessageType.ERROR);
			return false;
		}
	}
	
	/**Opens the given file, discarding any pending change without prompting the user.
	 * @param file
	 * @return
	 */
	public boolean openFile(File file) {
		String ext = file.getName().replaceFirst("^.*\\.", "").toLowerCase();
		if ("txt".equals(ext)) {
			return openLHX(file, true);
		} else if ("lnd".equals(ext)) {
			return openLand(file, true);
		} else {
			MessageBox.show(getStage(), I18n.tr("dialog.open"), I18n.tr("error.unsupportedFileFormat"), MessageType.ERROR);
			return false;
		}
	}
	
	/**Opens the given LHX file, optionally asking the user to save any pending change
	 * @param file the file to open.
	 * @param askSavePending
	 * @param ignoreGameDir
	 * @param callback called after the file has been successfully opened.
	 * @return
	 */
	public boolean openLHX(File file, boolean askSavePending, boolean ignoreGameDir, Runnable callback) {
		if (!ignoreGameDir && !isGameDirValid()) {
			MessageBox.show(getStage(), I18n.tr("dialog.open"), I18n.tr("messages.gameDirInvalidAsk"), MessageType.INFO, Options.YES_NO, choice -> {
				if (choice == Choice.YES) {
					SettingsWindow.showSingleInstance(this, getStage(), skin, true).search(Settings.GAME_DIR);
				} else {
					openLHX(file, askSavePending, true, callback);
				}
			});
			return false;
		}
		if (hasUnsavedChanges() && askSavePending) {
			MessageBox.show(getStage(), I18n.tr("dialog.open"), I18n.tr("dialog.unsavedChanges"), MessageType.ASK, Options.YES_NO_CANCEL, (choice) -> {
				if (choice == Choice.YES) {
					save(() -> openLHX(file, true, ignoreGameDir, callback));
				} else if (choice == Choice.NO) {
					openLHX(file, false, ignoreGameDir, callback);
				}
			});
		} else {
			if (openLHX(file, true)) {
				if (callback != null) callback.run();
			}
			return true;
		}
		return false;
	}
	
	/**Opens the given LHX file, discarding any pending change without prompting the user.
	 * @param file
	 * @param autoloadLand
	 * @return
	 */
	public boolean openLHX(File file, boolean autoloadLand) {
		try {
			setLHX(LHXFile.load(file), autoloadLand);
			RecentFiles.add(file);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			if (uiScreen == null) throw new RuntimeException(e);
			showError(I18n.tr("dialog.openLHXScript.title"), e);
			return false;
		}
	}
	
	/**Opens the given landscape file, optionally asking the user to save any pending change
	 * @param file the file to open.
	 * @param askSavePending
	 * @param clearLHX
	 * @param callback called after the file has been successfully opened.
	 * @return
	 */
	public boolean openLand(File file, boolean askSavePending, boolean clearLHX, Runnable callback) {
		if (hasUnsavedChanges() && askSavePending) {
			MessageBox.show(getStage(), I18n.tr("dialog.open"), I18n.tr("dialog.unsavedChanges"), MessageType.ASK, Options.YES_NO_CANCEL, (choice) -> {
				if (choice == Choice.YES) {
					save(() -> openLand(file, true, clearLHX, callback));
				} else if (choice == Choice.NO) {
					openLand(file, false, clearLHX, callback);
				}
			});
		} else {
			if (openLand(file, clearLHX)) {
				if (callback != null) callback.run();
			}
			return true;
		}
		return false;
	}
	
	/**Opens the given landscape file, discarding any pending change without prompting the user.
	 * @param file
	 * @param clearLHX
	 * @return
	 */
	public boolean openLand(File file, boolean clearLHX) {
		try {
			if (clearLHX) {
				setLHX(new LHXFile(), false);
			}
			setLand(LndFile.load(file, true));
			RecentFiles.add(file);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			if (uiScreen == null) throw new RuntimeException(e);
			showError(I18n.tr("dialog.openLandscape.title"), e);
			return false;
		}
	}
	
	/**Saves the current landscape to the given file.
	 * @param file
	 * @throws Exception
	 */
	public void saveLand(File file) throws Exception {
		land.setFile(file);
		saveLand();
	}
	
	/**Saves the landscape to its current location. If the location isn't set, will throw an exception. 
	 * @throws Exception
	 */
	public void saveLand() throws Exception {
		if (land.getFile() == null) throw new IllegalStateException("Landscape file not set");
		land.updateLowResTextures();
		metaManager.store();
		land.write(land.getFile());
		setUnsavedLandChanges(false);
		RecentFiles.add(land.getFile());
	}
	
	public boolean isChangingLand() {
		return this.changingLand;
	}
	
	public LndFile getLand() {
		return this.land;
	}
	
	/**Sets the given landscape, optionally asking the user to save any pending change.
	 * @param file
	 * @param askSavePending
	 * @param callback called after the landscape has been set (i.e. the user hasn't canceled the operation).
	 * @return
	 */
	public boolean setLand(LndFile file, boolean askSavePending, Runnable callback) {
		if (hasUnsavedChanges() && askSavePending) {
			MessageBox.show(getStage(), I18n.tr("dialog.exit"), I18n.tr("dialog.unsavedChanges"), MessageType.ASK, Options.YES_NO_CANCEL, (choice) -> {
				if (choice == Choice.YES) {
					save(() -> setLand(file, true, callback));
				} else if (choice == Choice.NO) {
					setLand(file, false, callback);
				}
			});
		} else {
			setLand(file);
			if (callback != null) callback.run();
			return true;
		}
		return false;
	}
	
	/**Saves the current LHX script to the given location.
	 * @param file
	 * @throws Exception
	 */
	public void saveLHX(File file) throws Exception {
		lhx.setFile(file);
		saveLHX();
	}
	
	/**Saves the LHX script to its current location. If the location isn't set, will throw an exception. 
	 * @throws Exception
	 */
	public void saveLHX() throws Exception {
		if (lhx.getFile() == null) throw new IllegalStateException("LHX file not set");
		lhx.write(lhx.getFile());
		setUnsavedLHXChanges(false);
		RecentFiles.add(lhx.getFile());
	}
	
	/**Sets the given LHX script, optionally asking the user to save any pending change.
	 * @param file
	 * @param askSavePending
	 * @param autoloadLand if true, loads the landscape specified in LOAD_LANDSCAPE statement.
	 * @param callback called after the script has been set (i.e. the user hasn't canceled the operation).
	 * @return
	 */
	public boolean setLHX(LHXFile file, boolean askSavePending, boolean autoloadLand, Runnable callback) {
		if (hasUnsavedChanges() && askSavePending) {
			MessageBox.show(getStage(), I18n.tr("dialog.exit"), I18n.tr("dialog.unsavedChanges"), MessageType.ASK, Options.YES_NO_CANCEL, (choice) -> {
				if (choice == Choice.YES) {
					save(() -> setLHX(file, true, autoloadLand, callback));
				} else if (choice == Choice.NO) {
					setLHX(file, false, autoloadLand, callback);
				}
			});
		} else {
			setLHX(file, autoloadLand);
			if (callback != null) callback.run();
			return true;
		}
		return false;
	}
	
	/**Sets the given LHX script, discarding any pending change.
	 * If the script contains a LOAD_LANDSCAPE statement, an attempt is made to open that landscape file too.
	 * @param lhxFile
	 * @param autoloadLand if true, loads the landscape specified in LOAD_LANDSCAPE statement.
	 */
	public void setLHX(LHXFile lhxFile, boolean autoloadLand) {
		if (lhxFile != this.lhx) {
			changingLHX = true;
			try {
				if (this.lhx != null) {
					this.lhx.listeners.remove(lhxChangeListener);
				}
				
				if (lhxFile != null) {
					File landFile = getLHXLandscapeSafe(lhxFile);
					if (landFile != null) {
						if (landFile.exists()) {
							openLand(landFile, false);
						} else {
							System.err.println("File "+landFile.getAbsolutePath()+" not found");
						}
					}
				}
				
				Object oldValue = this.lhx;
				this.lhx = lhxFile;
				timeFromLastLHXSave = 0f;
				if (this.lhx != null) {
					this.lhx.listeners.add(lhxChangeListener);
				}
				listeners.notify(EventType.CHANGE, Property.LHX, oldValue, this.lhx);
			} finally {
				setUnsavedLHXChanges(false);
				updateTitle();
				changingLHX = false;
			}
		}
	}
	
	public boolean isChangingLHX() {
		return this.changingLHX;
	}
	
	public LHXFile getLHX() {
		return this.lhx;
	}
	
	/**Sets the given landscape file, discarding any pending change.
	 * Also loads the footpaths next to the given landscape, if any.
	 * @param value
	 */
	public void setLand(LndFile value) {
		if (value != this.land) {
			changingLand = true;
			Object oldValue = land;
			if (this.land != null) {
				land.listeners.remove(landChangeListener);
				countryPreviewGenerator.close();
				sculptBrushes.clear();
				countryBrushes.clear();
				oceanBrush.setLand(null);
			}
			editManager.clear();
			this.land = value;
			timeFromLastLandSave = 0f;
			//
			if (this.land != null) {
				editManager.add(new PlaceHolderEdit(I18n.tr(land.getFile() != null ? "action.loadLand" : "action.newLand")), false);
				countryPreviewGenerator = new CountryPreviewGenerator(land);
				if (sculptBrushes.size() == 0) {
					sculptBrushes.add(new SculptBrush());
				}
				if (countryBrushes.size() == 0) {
					countryBrushes.add(new CountryBrush());
				}
				oceanBrush.setLand(land);
				lakeBrush.setLand(land);
				soundBrush.setLand(land);
				//
				land.listeners.add(landChangeListener);
			}
			listeners.notify(EventType.CHANGE, Property.LAND, oldValue, land);
			
			FotFile oldFoothpaths = this.footpaths;
			if (land != null && land.getFile() != null) {
				File fotFile = new File(land.getFile().getAbsolutePath().replaceAll("\\.lnd$", ".fot"));
				if (fotFile.exists()) {
					openFootpaths(fotFile);
				}
			}
			if (this.footpaths == oldFoothpaths) {
				setFootpaths(null);
			}
			
			setUnsavedLandChanges(false);
			updateTitle();
			changingLand = false;
		}
	}
	
	/**Opens the given footpaths file, discarding the previous one.
	 * @param file
	 * @return
	 */
	public boolean openFootpaths(File file) {
		try {
			setFootpaths(FotFile.load(file));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			if (uiScreen == null) throw new RuntimeException(e);
			showError(I18n.tr("dialog.openFootpaths.title"), e);
			return false;
		}
	}
	
	/**Sets the given footpaths file, discarding the previous one.
	 * @param fotFile
	 */
	public void setFootpaths(FotFile fotFile) {
		if (fotFile != this.footpaths) {
			/*if (fotFile != null) {
				try (PrintWriter out = new PrintWriter("fot.json")) {
				    out.println(fotFile.toJsonString());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}*/
			Object oldValue = this.footpaths;
			this.footpaths = fotFile;
			listeners.notify(EventType.CHANGE, Property.FOOTPATHS, oldValue, this.footpaths);
		}
	}
	
	public FotFile getFootpaths() {
		return this.footpaths;
	}
	
	private void updateTitle() {
		if (land == null || land.getFile() == null) {
			if (hasUnsavedChanges()) {
				Gdx.graphics.setTitle("*" + APP_NAME);
			} else {
				Gdx.graphics.setTitle(APP_NAME);
			}
		} else {
			String filename = land.getFile().getName().replaceAll("(?i)\\.lnd$", "");
			if (hasUnsavedChanges()) {
				Gdx.graphics.setTitle("*" + filename + " - " + APP_NAME);
			} else {
				Gdx.graphics.setTitle(filename + " - " + APP_NAME);
			}
		}
	}
	
	public RecoveryManager getRecoveryManager() {
		return recoveryManager;
	}
	
	public LandEditManager getEditManager() {
		return editManager;
	}
	
	private final UChangeListener landChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			setUnsavedLandChanges(true);
		}
	};
	
	private final UChangeListener lhxChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			setUnsavedLHXChanges(true);
		}
	};
	
	public boolean hasUnsavedLandChanges() {
		return this.unsavedLandChanges;
	}
	
	public boolean hasUnsavedLHXChanges() {
		return this.unsavedLHXChanges;
	}
	
	public boolean hasUnsavedChanges() {
		return this.unsavedLandChanges || this.unsavedLHXChanges;
	}
	
	public void setUnsavedLandChanges(boolean value) {
		if (this.unsavedLandChanges != value) {
			Object oldValue = this.unsavedLandChanges;
			this.unsavedLandChanges = value;
			updateTitle();
			if (!unsavedLandChanges) timeFromLastLandSave = 0;
			listeners.notify(EventType.CHANGE, Property.LAND_CHANGED, oldValue, this.unsavedLandChanges);
		}
	}
	
	public void setUnsavedLHXChanges(boolean value) {
		if (this.unsavedLHXChanges != value) {
			Object oldValue = this.unsavedLHXChanges;
			this.unsavedLHXChanges = value;
			updateTitle();
			if (!unsavedLHXChanges) timeFromLastLHXSave = 0;
			listeners.notify(EventType.CHANGE, Property.LHX_CHANGED, oldValue, this.unsavedLHXChanges);
		}
	}
	
	/**Disables the autosave function until the program is restarted. This method must be called whenever an unrecoverable error
	 * is detected, to avoid overwriting files with possibly broken data.
	 */
	public void disableAutosave() {
		this.allowAutosave = false;
		this.showStatusMessage(I18n.tr("messages.autosaveDisabled"));
	}
	
	/**Prompt the user for a file location where to save the image currently displayed in the 3D view.
	 */
	public void saveScreenshot() {
		getOS().getFileChooser().chooseFile(
            new NativeFileChooserConfiguration() {{
                title = I18n.tr("dialog.saveScreenshot.title");
                directory = Gdx.files.absolute(System.getProperty("user.home"));
                nameFilter = (dir, name) -> name.endsWith(".jpg") || name.endsWith(".png");
                mimeFilter = "Images/jpg,png";
                intent = NativeFileChooserIntent.SAVE;
            }},
            new NativeFileChooserCallback() {
                @Override
                public void onFileChosen(FileHandle file) {
					getView3D().getRenderedImage((Pixmap pixmap) -> {
						try {
							getOS().getImageWriter().write(file.file(), pixmap);
						} catch (IOException e) {
							e.printStackTrace();
							showError(I18n.tr("dialog.saveScreenshot.title"), e);
						}
					});
                }

                @Override
                public void onCancellation() {}

                @Override
                public void onError(Exception exception) {
                    exception.printStackTrace();
                    showError(I18n.tr("dialog.saveScreenshot.title"), exception);
                }
            }
        );
	}
	
	public Texture getSmallBump() {
		return this.smallBump;
	}
	
	public Texture getSkyTexture() {
		return this.skyTexture;
	}
	
	public void openUserGuide() {
		os.openURL("docs/index.htm");
	}
	
	public EditMode getEditMode() {
		return this.editMode;
	}
	
	public void setEditMode(EditMode mode) {
		if (mode == null) throw new IllegalArgumentException("Edit mode cannot be null");
		if (mode != this.editMode) {
			Object prevMode = this.editMode;
			Object prevTool = getActiveTool();
			this.editMode = mode;
			listeners.notify(EventType.CHANGE, Property.EDIT_MODE, prevMode, this.editMode);
			listeners.notify(EventType.CHANGE, Property.ACTIVE_TOOL, prevTool, getActiveTool());
		}
	}
	
	public SelectionMode getSelectionMode() {
		return selectionMode;
	}
	
	public void setSelectionMode(SelectionMode mode) {
		if (mode != this.selectionMode) {
			Object oldValue = this.selectionMode;
			this.selectionMode = mode;
			listeners.notify(EventType.CHANGE, Property.SELECTION_MODE, oldValue, this.selectionMode);
		}
	}
	
	/**Copy the current selection.
	 */
	public void copy() {
		switch (editMode) {
			case SCULPT:
				copySelectedCells();
				break;
			case FOOTPATH:
				
				break;
			case OBJ_PLACEMENT:
				copySelectedObjects();
				break;
			case SCRIPT:
				
				break;
		}
	}
	
	/**Cut the current selection.
	 */
	public void cut() {
		switch (editMode) {
			case SCULPT:
				cutSelectedCells();
				break;
			case FOOTPATH:
				
				break;
			case OBJ_PLACEMENT:
				cutSelectedObjects();
				break;
			case SCRIPT:
				
				break;
		}
	}
	
	/**Paste the content of the clipboard, if its format is supported.
	 */
	public void paste() {
		try {
			DataType type = os.getClipboardContentType();
			if (type == DataType.LAND) {
				LndFile srcLand = os.getClipboardLand();
				pasteLand(srcLand);
			} else if (type == DataType.OBJECTS) {
				List<Statement> statements = os.getClipboardObjects();
				pasteObjects(statements);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**Delete the current selection.
	 */
	public void delete() {
		boolean hasSelectedCells = getView3D().hasSelectedCells();
		boolean hasSelectedObjects = getView3D().hasSelectedObjects();
		if (hasSelectedCells && hasSelectedObjects) {
			editManager.begin(I18n.tr("action.delete"));
			deleteSelectedObjects();	//Objects must be deleted before landscape
			deleteSelectedCells();
			editManager.end();
		} else if (hasSelectedCells) {
			deleteSelectedCells();
		} else if (hasSelectedObjects) {
			deleteSelectedObjects();
		}
	}
	
	/**Paste the given landscape into the current landscape. The user must choose a location where to drop the landscape.
	 * @param srcLand
	 */
	public void pasteLand(LndFile srcLand) {
		pasteLand(srcLand, null, null);
	}
	
	/**Paste the given landscape into the current landscape. The user must choose a location where to drop the landscape.
	 * @param srcLand
	 * @param editDescription an optional description to be shown in the edit history for this operation. Can be null.
	 * @param sourceName an optional name to be shown in the country mapping dialog. Can be null.
	 */
	public void pasteLand(LndFile srcLand, String editDescription, String sourceName) {
		List<LNDCountry> srcCountries = srcLand.getCountries();
		List<LNDCountry> dstCountries = this.land.getCountries();
		boolean allMatched = true;
		CountryMatch[] matches = LandTool.matchCountries(srcLand, this.land);
		List<CountryMapping> mappings = new ArrayList<>(matches.length);
		for (CountryMatch match : matches) {
			LNDCountry srcCountry = srcCountries.get(match.srcCountry);
			if (match.distance == 0) {
				CountryMapping mapping = new CountryMapping(srcCountry);
				mapping.dstCountry = dstCountries.get(match.dstCountry);
				mappings.add(mapping);
			} else {
				LNDCountry dstCountry = getPreferredCountryMapping(srcCountry);
				if (dstCountry != null) {
					CountryMapping mapping = new CountryMapping(srcCountry);
					mapping.dstCountry = dstCountry;
					mappings.add(mapping);
				} else {
					allMatched = false;
				}
			}
		}
		if (allMatched) {
			if (editDescription == null) editDescription = I18n.tr("action.pasteLand");
			beginDropLand(srcLand, mappings, false, editDescription, DropEventType.ENTER, null);
		} else {
			if (sourceName == null) sourceName = "<clipboard>";
			ImportLandWindow.showSingleInstance(this, getStage(), getSkin())
				.setSrcLand(srcLand, sourceName)
				.setMappings(mappings);
		}
	}
	
	/**Returns a copy of the selected cells as a new landscape.
	 * @param removeResources if true, tries to remove countries and materials that aren't referenced by the selection.
	 * @param allowReorder if true, reorder countries and materials, allowing to remove all the unused resources.
	 * @return
	 */
	public LndFile getSelectedCellsAsNewLand(boolean removeResources, boolean allowReorder) {
		return uiScreen.getView3D().getSelectedCellsAsNewLand(removeResources, allowReorder);
	}
	
	/**Copies the selected cells to the system clipboard.
	 */
	public void copySelectedCells() {
		LndFile cpyLand = getSelectedCellsAsNewLand(true, true);
		os.setClipboardContent(cpyLand);
		listeners.notify(EventType.CHANGE, Property.CLIPBOARD, null, cpyLand);
	}
	
	/**Copies the selected cells to the system clipboard and remove them from the landscape.
	 */
	public void cutSelectedCells() {
		editManager.begin(I18n.tr("action.cutLand"));
		copySelectedCells();
		deleteSelectedCells();
		editManager.end();
	}
	
	/**Begin a drag operation fo the selected cells. The user must then choose a drop location.
	 */
	public void beginDragLand() {
		UndoableEdit dragLandEdit = editManager.begin(I18n.tr("action.moveLand"));
		LndFile cpyLand = getSelectedCellsAsNewLand(true, false);
		deleteSelectedCells();
		beginDropLand(cpyLand, null, false, "drop land", DropEventType.MOUSEUP, success -> {
			if (success) {
				editManager.end();
			} else {
				dragLandEdit.undo();
				editManager.cancel();
			}
			Gdx.app.postRunnable(() -> setActiveTool(Tool.SELECT));
		});
	}
	
	private boolean droppingLand = false;
	private boolean dropLandAlignBlocks = false;
	private List<LH3DLandBlock> dropLandBlocks;
	private float dropLandHalfW;
	private float dropLandHalfH;
	private int dropLandOffsetX;
	private int dropLandOffsetZ;
	private DropEventType dropEventType;
	private DropCallback dropCallback;
	private UndoableEdit dropLandEdit;
	
	/**Let the user choose a drop loaction for the given landscape.
	 * @param srcLand the landscape to drop.
	 * @param countryMappings an optional mapping for the source countries to the existing countries. Can be null.
	 * @param alignBlocks if true, will forece the alignment of source blocks with target blocks.
	 * @param editDescription a description to be shown in the edit history for this operation.
	 * @param dropEventType the action the user must do to end the drop operation.
	 * @param callback an optional callback to invoke after the drop completes successfully.
	 */
	public void beginDropLand(LndFile srcLand, @Null Iterable<CountryMapping> countryMappings, boolean alignBlocks, String editDescription, DropEventType dropEventType, @Null DropCallback callback) {
		droppingLand = true;
		this.dropLandAlignBlocks = alignBlocks;
		this.dropEventType = dropEventType;
		this.dropCallback = callback;
		this.dropLandEdit = editManager.begin(editDescription);
		final LndFile dstLand = this.land;
		//Import and map src countries to target ones
		int[] rawMappings = countryMappings == null ? null : new int[LndFile.MAX_COUNTRIES];
		if (countryMappings != null) {
			for (CountryMapping mapping : countryMappings) {
				if (mapping.srcCountry.getIndex() < rawMappings.length) {
					if (mapping.dstCountry != null) {
						rawMappings[mapping.srcCountry.getIndex()] = mapping.dstCountry.getIndex();
					} else {
						LNDCountry newCountry = LandTool.importCountry(srcLand, mapping.srcCountry.getIndex(), dstLand);
						rawMappings[mapping.srcCountry.getIndex()] = newCountry.getIndex();
					}
				}
			}
		}
		//
		LandTool landTool = new LandTool(dstLand);
		dropLandBlocks = landTool.convertBlocks(srcLand, rawMappings);
		int bxLow = Integer.MAX_VALUE;
		int bzLow = Integer.MAX_VALUE;
		int bxHigh = Integer.MIN_VALUE;
		int bzHigh = Integer.MIN_VALUE;
		for (LH3DLandBlock block : dropLandBlocks) {
			bxLow = Math.min(bxLow, block.getBlockX());
			bzLow = Math.min(bzLow, block.getBlockZ());
			bxHigh = Math.max(bxHigh, block.getBlockX());
			bzHigh = Math.max(bzHigh, block.getBlockZ());
		}
		int blockSpanX = bxHigh - bxLow + 1;
		int blockSpanZ = bzHigh - bzLow + 1;
		dropLandHalfW = LH3DLandBlock.BLOCK_SIZE * blockSpanX * 0.5f;
		dropLandHalfH = LH3DLandBlock.BLOCK_SIZE * blockSpanZ * 0.5f;
		getView3D().setExtraBlocks(dropLandBlocks);
		getView3D().setMouseCoordCallback(dropLandCoordCallback);
		if (dropEventType == DropEventType.ENTER) {
			showStatusMessage(I18n.tr("importLand.positioningHint"));
		}
		disableAllExcept3DViewAnd(null);
	}
	
	/**Cancel the current drop land operation. Any action in the same transaction is reverted.
	 */
	public void cancelDropLand() {
		endDropLand(false);
	}
	
	private void endDropLand(boolean success) {
		if (droppingLand) {
			droppingLand = false;
			getView3D().setExtraBlocks(null);
			getView3D().setMouseCoordCallback(null);
			enableAll();
			showStatusMessage("");
			if (success) {
				editManager.end();
			} else {
				dropLandEdit.undo();
				editManager.cancel();
			}
			if (dropCallback != null) {
				dropCallback.finished(success);
				dropCallback = null;
			}
		}
	}
	
	private final CoordCallback dropLandCoordCallback = new CoordCallback() {
		@Override
		public void onCoord(Coord coord, int button) {
			if (droppingLand) {
				final float step = dropLandAlignBlocks ? LH3DLandBlock.BLOCK_SIZE : LH3DLandCell.CELL_SIZE;
				dropLandOffsetX = Math.round(Math.round((coord.x - dropLandHalfW) / step) * step / LH3DLandCell.CELL_SIZE);
				dropLandOffsetZ = Math.round(Math.round((coord.z - dropLandHalfH) / step) * step / LH3DLandCell.CELL_SIZE);
				final float left = dropLandOffsetX * LH3DLandCell.CELL_SIZE;
				final float bottom = dropLandOffsetZ * LH3DLandCell.CELL_SIZE;
				for (LH3DLandBlock block : dropLandBlocks) {
					block.setMapX(left + block.getBlockX() * LH3DLandBlock.BLOCK_SIZE);
					block.setMapZ(bottom + block.getBlockZ() * LH3DLandBlock.BLOCK_SIZE);
				}
			}
		}
	};
	
	private final InputListener dropLandViewListener = new InputListener() {
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			return true;
		};
		
		public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
			if (droppingLand && dropEventType == DropEventType.MOUSEUP && button == Buttons.LEFT) {
				LandTool tool = new LandTool(land);
				boolean success = tool.importBlocks(dropLandBlocks, dropLandOffsetX, dropLandOffsetZ, null);
				endDropLand(success);
				event.handle();
			}
			if (draggingObjects && button == Buttons.LEFT) {
				endDropObjects();
				event.handle();
			}
		};
		
		public boolean keyUp(InputEvent event, int keycode) {
			if (droppingLand) {
				if ((keycode == Keys.ENTER || keycode == Keys.NUMPAD_ENTER) && dropEventType == DropEventType.ENTER) {
					LandTool tool = new LandTool(land);
					boolean success = tool.importBlocks(dropLandBlocks, dropLandOffsetX, dropLandOffsetZ, null);
					if (success) {
						endDropLand(true);
					} else {
						showTempStatusMessage(I18n.tr("importLand.outOfBounds"));
					}
					event.handle();
				} else if (keycode == Keys.ESCAPE) {
					cancelDropLand();
					event.handle();
				}
			}
			return false;
		};
	};
	
	public List<Object3D> getSelectedObjects() {
		return getView3D().getSelectedObjects();
	}
	
	/**Copy the selected objects to the system clipboard. The objects are copied as text LHX statements.
	 */
	public void copySelectedObjects() {
		List<Object3D> objects = getSelectedObjects();
		List<Statement> statements = new ArrayList<>(objects.size());
		for (Object3D obj3d : objects) {
			if (obj3d.getStatement() != null) {
				statements.add(obj3d.getStatement());
			}
		}
		os.setClipboardContent(statements);
		listeners.notify(EventType.CHANGE, Property.CLIPBOARD, null, statements);
	}
	
	/**Copy the selected objects to the system clipboard and removes them from the landscape. The objects are copied as text LHX statements.
	 */
	public void cutSelectedObjects() {
		editManager.begin(I18n.tr("action.cutObjects"));
		copySelectedObjects();
		deleteSelectedObjects();
		editManager.end();
	}
	
	/**Removes the selected objects from the landscape.
	 */
	public void deleteSelectedObjects() {
		List<Object3D> objects = getSelectedObjects();
		if (objects.isEmpty()) return;
		getView3D().setSelectedObject((Object3D)null);
		List<Statement> statements = new ArrayList<>(objects.size());
		for (Object3D obj3d : objects) {
			if (obj3d.getStatement() != null) {
				statements.add(obj3d.getStatement());
			}
		}
		lhx.getStatements().removeAll(statements);
	}
	
	/**Paste the given objects at the location the camera is pointing to.
	 * @param statements
	 */
	public void pasteObjects(List<Statement> statements) {
		editManager.begin(I18n.tr("action.pasteObjects"));
		int n = 0;
		float xSum = 0;
		float zSum = 0;
		for (Statement stmt : statements) {
			if (stmt.isCommand() && stmt.getCommand().position >= 0) {
				LHXCoord pos = stmt.getPosition();
				xSum += pos.x;
				zSum += pos.z;
				n++;
			}
		}
		float xAvg = xSum / n;
		float zAvg = zSum / n;
		Coord lookAt = new Coord(getView3D().getCamera().getPivot());
		for (Statement stmt : statements) {
			if (stmt.isCommand() && stmt.getCommand().position >= 0) {
				LHXCoord pos = stmt.getPosition();
				stmt.setPosition(pos.x - xAvg + lookAt.x, pos.z - zAvg + lookAt.z);
			}
		}
		lhx.getStatements().addAll(statements);
		getView3D().setSelectedStatements(statements);
		editManager.end();
	}
	
	private boolean draggingObjects = false;
	private Coord startCoord;
	private final Map<Object3D, Coord> objectsStartPosition = new IdentityHashMap<>();
	
	/**Begin a drag operation for the selected objects.
	 */
	public void beginDragObjects() {
		startCoord = getView3D().getCursorCoord().clone();
		if (startCoord == null) return;
		List<Object3D> objects = getSelectedObjects();
		if (objects.isEmpty()) return;
		String descr = objects.size() == 1 ? I18n.tr("action.moveObject", objects.get(0).toString()) : I18n.tr("action.moveObjects");
		editManager.begin(descr);
		objectsStartPosition.clear();
		for (Object3D obj : objects) {
			objectsStartPosition.put(obj, obj.getPosition());
		}
		draggingObjects = true;
		getView3D().setMouseCoordCallback(dragObjectsCoordCallback);
		disableAllExcept3DViewAnd(null);
	}
	
	private void endDropObjects() {
		if (draggingObjects) {
			draggingObjects = false;
			objectsStartPosition.clear();
			getView3D().setMouseCoordCallback(null);
			enableAll();
			showStatusMessage("");
			editManager.end();
		}
	}
	
	private final CoordCallback dragObjectsCoordCallback = new CoordCallback() {
		@Override
		public void onCoord(Coord coord, int button) {
			if (draggingObjects) {
				final float offsetX = coord.x - startCoord.x;
				final float offsetZ = coord.z - startCoord.z;
				for (Entry<Object3D, Coord> e : objectsStartPosition.entrySet()) {
					Object3D obj = e.getKey();
					Coord startPos = e.getValue();
					obj.setPosition(startPos.x + offsetX, startPos.z + offsetZ);
				}
			}
		}
	};
	
	/**Removes the selected cells from the landscape.
	 */
	public void deleteSelectedCells() {
		if (!uiScreen.getView3D().hasSelectedCells()) return;
		editManager.begin(I18n.tr("action.deleteLand"));
		List<Block3D> selBlocks = uiScreen.getView3D().getSelectedBlocks();
		List<LH3DLandBlock> blocksToRemove = new LinkedList<>();
		try (BulkUpdate<LH3DLandBlock> cellsUpdate = land.getLandBlocksForUpdate()) {
			for (Block3D block3D : selBlocks) {
				LH3DLandBlock block = block3D.block;
				boolean isEmpty = true;
				try (BulkUpdate<byte[]> selUpdate = block3D.updateSelection()) {
					for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
						for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
							LH3DLandCell cell = block.getCell(cx, cz);
							if (selUpdate.data[cx][cz] != 0) {
								cell.set(LH3DLandCell.EMPTY);
								selUpdate.data[cx][cz] = 0;
							} else if (cell.getAltitude() > 0) {
								isEmpty = false;
							}
						}
					}
				}
				if (isEmpty) {
					blocksToRemove.add(block);
				} else {
					cellsUpdate.setChanged(block.getIndex(), LH3DLandBlock.Property.CELLS);
				}
			}
		}
		if (!blocksToRemove.isEmpty()) {
			try (BulkUpdate<LH3DLandBlock> update = land.getLandBlocksForUpdate()) {
				for (LH3DLandBlock block : blocksToRemove) {
					update.setRemoved(block, block.getIndex());
					land.removeBlock(block);
				}
			}
		}
		editManager.end();
	}
	
	/**Prompt the user to reduce the selected objects by excluding the ones which don't match the chosen properties.
	 */
	public void refineSelection() {
		List<Object3D> objects = getSelectedObjects();
		EditSelectionWindow w = new EditSelectionWindow(this, skin, I18n.tr("editSelectionWindow.refine"));
		w.setSampleObjects(objects)
		.setCallback(new EventListener() {
			@Override
			public boolean handle(Event event) {
				Set<Command> commands = w.getSelectedCommands();
				Set<Object> types = w.getSelectedTypes();
				Set<Integer> towns = w.getSelectedTowns();
				Set<TribeType> tribes = w.getSelectedTribes();
				for (Object3D obj : objects) {
					Statement stmt = obj.getStatement();
					Command cmd = stmt.getCommand();
					TribeType tribe = stmt.getEffectiveTribe();
					if ((commands != null && !commands.contains(stmt.getCommand())) ||
							(cmd.type >= 0 && types != null && !types.contains(stmt.getType())) ||
							(cmd.town >= 0 && towns != null && !towns.contains(stmt.getTown())) ||
							(tribe != null && tribes != null && !tribes.contains(tribe))) {
						obj.setSelected(false);
					}
				}
				return true;
			}
		})
		.show(getStage(), true);
	}
	
	/**Prompt the user to extend the selected objects by including the ones which match the chosen properties.
	 */
	public void extendSelection() {
		List<Object3D> objects = getSelectedObjects();
		EditSelectionWindow w = new EditSelectionWindow(this, skin, I18n.tr("editSelectionWindow.extend"));
		w.setSampleObjects(objects)
		.setCallback(new EventListener() {
			@Override
			public boolean handle(Event event) {
				Set<Command> commands = w.getSelectedCommands();
				Set<Object> types = w.getSelectedTypes();
				Set<Integer> towns = w.getSelectedTowns();
				Set<TribeType> tribes = w.getSelectedTribes();
				List<Object3D> toSelect = new ArrayList<>(objects);
				for (Object3D obj : getView3D().getAllObjects()) {
					if (!obj.isSelected()) {
						Statement stmt = obj.getStatement();
						if ((commands == null || commands.isEmpty() || commands.contains(stmt.getCommand())) &&
							(types == null || types.isEmpty() || types.contains(stmt.getType())) &&
							(towns == null || towns.isEmpty() || towns.contains(stmt.getTown())) &&
							(tribes == null || tribes.isEmpty() || tribes.contains(stmt.getEffectiveTribe()))) {
							toSelect.add(obj);
						}
					}
				}
				getView3D().setSelectedObjects(toSelect);
				return true;
			}
		})
		.show(getStage(), true);
	}
	
	/**Adds the parents of the selected objects to the current selection.
	 */
	public void selectParents() {
		List<Object3D> objects = getSelectedObjects();
		List<Object3D> newSelection = new ArrayList<>(objects);
		for (Object3D obj : objects) {
			if (obj.getParent() != null && obj.getParent().getStatement() != null) {
				newSelection.add(obj.getParent());
			}
		}
		getView3D().setSelectedObjects(newSelection);
	}
	
	/**Adds the siblings of the selected objects to the current selection.
	 */
	public void selectSiblings() {
		List<Object3D> objects = getSelectedObjects();
		List<Object3D> newSelection = new ArrayList<>(objects);
		for (Object3D obj : objects) {
			if (obj.getParent() != null && obj.getParent().getStatement() != null) {
				newSelection.addAll(obj.getParent().getChildren());
			}
		}
		getView3D().setSelectedObjects(newSelection);
	}
	
	/**Adds the children of the selected objects to the current selection.
	 */
	public void selectChildren() {
		List<Object3D> objects = getSelectedObjects();
		List<Object3D> newSelection = new ArrayList<>(objects);
		for (Object3D obj : objects) {
			newSelection.addAll(obj.getChildren());
		}
		getView3D().setSelectedObjects(newSelection);
	}
	
	/**Adds the descendants of the selected objects to the current selection.
	 */
	public void selectDescendants() {
		List<Object3D> objects = getSelectedObjects();
		List<Object3D> newSelection = new ArrayList<>(objects);
		List<Object3D> prevChildren = objects;
		List<Object3D> newChildren;
		while (!prevChildren.isEmpty()) {
			newChildren = new ArrayList<>();
			for (Object3D obj : prevChildren) {
				newChildren.addAll(obj.getChildren());
			}
			newSelection.addAll(newChildren);
			prevChildren = newChildren;
		}
		getView3D().setSelectedObjects(newSelection);
	}
	
	/**Create a new object at the given position. The object type can be retrieved using the methods exposed by {@link it.ld.bw.lndgui.gfx.L3DModelManager}.
	 * @param modelInfo the kind of object to create.
	 * @param position
	 */
	public void createObject(ModelInfo modelInfo, Coord position) {
		Command cmd = modelInfo.command;
		editManager.begin(I18n.tr("action.addObject", cmd.objectDisplayName));
		try {
			Statement stmt = new Statement(cmd);
			stmt.setType(modelInfo.type);
			stmt.setPosition(position.x, position.z);
			stmt.setAge(modelInfo.getAge());
			if (cmd.id >= 0) {
				int id = lhx.findHighestId(cmd) + 1;
				stmt.setId(id);
			}
			if (cmd.parent >= 0) {
				if (cmd.parent == cmd.player) {
					Statement town = lhx.findClosest(Command.CREATE_TOWN, position.x, position.z, -1);
					if (town != null) {
						stmt.setPlayer(town.getPlayer());
					}
				} else if (cmd.parent == cmd.town) {
					Statement town = lhx.findClosest(Command.CREATE_TOWN, position.x, position.z, 1000f);
					if (town != null) {
						stmt.setTown(town.getId());
					}
				} else if (cmd.parent == cmd.flock) {
					Statement flock = lhx.findClosest(Command.CREATE_FLOCK, position.x, position.z, 500f);
					if (flock != null) {
						LHXCoord flockPos = flock.getPosition();
						int radius = flock.getArg("radius").getInt();
						if (flockPos.dst(position.x, position.z) <= radius) {
							stmt.setFlock(flock.getId());
						}
					}
				} else if (cmd.parent == cmd.forest) {
					Statement forest = lhx.findClosest(Command.CREATE_FOREST, position.x, position.z, 500f);
					if (forest != null) {
						stmt.setForest(forest.getId());
					}
				}
			}
			if (cmd == Command.CREATE_FLOCK) {
				stmt.setCoord("destination", position.x, position.z);
				stmt.setTown(-1);
			} else if (cmd == Command.CREATE_VILLAGER_POS) {
				Object3D home = getView3D().getLHX3D().findClosestFreeHome(position, 200f, modelInfo.isAdult());
				if (home != null) {
					stmt.setSecondaryCoords(home.getDoorPosition().toLHXCoord());
				}
			}
			lhx.getStatements().add(stmt);
			getView3D().setSelectedObject(stmt);
		} finally {
			editManager.end();
		}
	}
	
	public Tool getActiveTool() {
		return activeTool.get(editMode);
	}
	
	public void setActiveTool(Tool tool) {
		Tool current = activeTool.get(editMode);
		if (tool != current) {
			Object oldValue = current;
			activeTool.put(editMode, tool);
			listeners.notify(EventType.CHANGE, Property.ACTIVE_TOOL, oldValue, tool);
		}
	}
	
	public SculptBrush[] getSculptBrushes(SculptBrush.Mode mode) {
		if (mode == null) return sculptBrushes.toArray(new SculptBrush[0]);
		List<SculptBrush> res = new ArrayList<>(sculptBrushes.size());
		for (SculptBrush brush : sculptBrushes) {
			if (brush.getMode() == mode) {
				res.add(brush);
			}
		}
		return res.toArray(new SculptBrush[0]);
	}
	
	/**Returns the set of the defined sculpt brushes.
	 * @return
	 */
	public List<SculptBrush> getSculptBrushes() {
		return this.sculptBrushes;
	}
	
	/**Returns the currently selected sculpt brush.
	 * @return
	 */
	public SculptBrush getSculptBrush() {
		return this.sculptBrush;
	}
	
	/**Sets the current sculpt brush.
	 * @param brush
	 */
	public void setSculptBrush(SculptBrush brush) {
		if (brush != this.sculptBrush) {
			Object oldValue = this.sculptBrush;
			this.sculptBrush = brush;
			listeners.notify(EventType.CHANGE, Property.SCULPT_BRUSH, oldValue, this.sculptBrush);
		}
	}
	
	/**Create a new sculpt brush and adds it to the set.
	 * @return the new sculpt brush.
	 */
	public SculptBrush createSculptBrush() {
		SculptBrush brush = new SculptBrush();
		this.sculptBrushes.add(brush);
		return brush;
	}
	
	public SculptBrush getOceanBrush() {
		return oceanBrush;
	}
	
	public SculptBrush getLakeBrush() {
		return lakeBrush;
	}
	
	public SoundBrush getSoundBrush() {
		return soundBrush;
	}
	
	/**Returns the set of the defined country brushes.
	 * @return
	 */
	public List<CountryBrush> getCountryBrushes() {
		return this.countryBrushes;
	}
	
	/**Returns the currently selected country brush.
	 * @return
	 */
	public CountryBrush getCountryBrush() {
		return this.countryBrush;
	}
	
	/**Sets the current country brush.
	 * @param brush
	 */
	public void setCountryBrush(CountryBrush brush) {
		if (brush != this.countryBrush) {
			Object oldValue = this.countryBrush;
			this.countryBrush = brush;
			listeners.notify(EventType.CHANGE, Property.COUNTRY_BRUSH, oldValue, this.countryBrush);
		}
	}
	
	/**Create a new country brush and adds it to the set.
	 * @return the new country brush.
	 */
	public CountryBrush createCountryBrush() {
		CountryBrush brush = new CountryBrush();
		this.countryBrushes.add(brush);
		return brush;
	}
	
	/**Update the baked shadows on the whole map.
	 */
	public void updateShadows() {
		editManager.begin(I18n.tr("action.updateShadows"));
    	ShadowTool.updateShadows(land);
    	editManager.end();
	}
	
	/**Ask the user for map coordinates, and then move the camera to that location.
	 */
	public void flyTo() {
		Prompt.show(getStage(), skin, I18n.getInstance(), I18n.tr("dialog.flyTo.title"), I18n.tr("dialog.flyTo.message"), "", new PromptListener() {
			@Override
			public void confirm(String value) {
				try {
					String[] parts = value.split(",");
					if (parts.length != 2) throw new IllegalArgumentException("Expected 2 numbers separed by comma");
					float x = Float.parseFloat(parts[0]);
					float z = Float.parseFloat(parts[1]);
					getView3D().flyTo(x, z, 80f);
				} catch (Exception e) {
					showError(I18n.tr("dialog.flyTo.title"), e);
				}
			}
		});
	}
	
	/**Move the camera to the given coordinates.
	 * @param x
	 * @param z
	 * @param radius the camera orbit radius around the target location.
	 */
	public void flyTo(float x, float z, float radius) {
		getView3D().flyTo(x, z, radius);
	}
	
	/**Stores a mapping for the given country to be used as default when importing a landscape.
	 * @param src
	 * @param dst
	 */
	public void rememberCountryMapping(LNDCountry src, LNDCountry dst) {
		final int srcHash = LandTool.CountryKey.getHash(src);
		final int dstHash = LandTool.CountryKey.getHash(dst);
		countryMappingCache.put(srcHash, dstHash);
	}
	
	/**Removes the default mapping for the given country.
	 * @param src
	 * @return
	 */
	public boolean forgetCountryMapping(LNDCountry src) {
		final int srcHash = LandTool.CountryKey.getHash(src);
		return countryMappingCache.remove(srcHash) != null;
	}
	
	/**Clears all of the default country mappings.
	 */
	public void forgetCountryMappings() {
		countryMappingCache.clear();
	}
	
	/**Returns the default target country for the given input country, if any.
	 * @param src
	 * @return a country or null
	 */
	public LNDCountry getPreferredCountryMapping(LNDCountry src) {
		final int srcHash = LandTool.CountryKey.getHash(src);
		if (countryMappingCache.containsKey(srcHash)) {
			final int searchHash = countryMappingCache.get(srcHash);
			for (LNDCountry dst : land.getCountries()) {
				final int dstHash = LandTool.CountryKey.getHash(dst);
				if (dstHash == searchHash) return dst;
			}
		}
		return null;
	}
	
	/**Shows a permanent message on the status bar. The message stays visible until it is replaced by a new message.
	 * @param msg
	 */
	public void showStatusMessage(String msg) {
		this.uiScreen.showStatusMessage(msg);
	}
	
	/**Shows a temporary message on the status bar. The message stays visible for few seconds, then the previous permanent message is shown.
	 * @param msg
	 */
	public void showTempStatusMessage(String msg) {
		this.uiScreen.showTempStatusMessage(msg);
	}
	
	@Override
    public void create() {
    	this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
    	 
    	Settings.load();
    	Settings.listeners.add(settingsChangeListener);
    	
    	I18n.setLocale(((Language)Settings.LANGUAGE.getValue()).getLocale());
    	
    	PopupMenu.init(skin);
		MessageBox.init(I18n.getInstance(), skin);
		
		TooltipManager manager = TooltipManager.getInstance();
    	manager.animations = false;
    	manager.initialTime = 0.75f;
    	manager.resetTime = 0.5f;
    	manager.offsetX = 8f;
    	manager.offsetY = 8f;
    	manager.edgeDistance = 0f;
    	
    	previewManager = new L3DPreviewManager(this);
    	
    	reloadGameFiles();
    	RecentFiles.init();
    	
		uiScreen = new UIScreen(this);
    	
        uiScreen.show();
        uiScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiScreen.getView3D().addListener(dropLandViewListener);
        
        uiOut.addConsumer((lines) -> {
        	if (Settings.CONSOLE_POPUP.getBool() && !OutputWindow.isOpen()) {
        		uiOut.ltrim();
        		if (!uiOut.isEmpty()) {
        			Gdx.app.postRunnable(() -> OutputWindow.showSingleInstance(MainApp.this, getStage(), skin));
        		}
        	}
        });
        
        if (fileToOpen != null) {
        	if (!openFile(fileToOpen)) {
            	newProject(false, () -> uiScreen.showIntroWindow());
        	}
		} else {
			newProject(false, null);
			uiScreen.showIntroWindow();
		}
    }
	
	private void handleAutosave(float delta) {
		timeFromLastLandSave += delta;
    	if (unsavedLandChanges && land.getFile() != null
    			&& Settings.AUTOSAVE.getBool()
    			&& allowAutosave
    			&& timeFromLastLandSave >= Settings.AUTOSAVE_INT.getInt() * 60
    			&& !editManager.isTransactionActive()) {
    		try {
				saveLand();
				showTempStatusMessage(I18n.tr("messages.autosaved"));
			} catch (Exception e) {
				e.printStackTrace();
				showStatusMessage(e.getMessage());
			}
    	}
    	//
    	timeFromLastLHXSave += delta;
    	if (unsavedLHXChanges && lhx.getFile() != null
    			&& Settings.AUTOSAVE.getBool()
    			&& allowAutosave
    			&& timeFromLastLHXSave >= Settings.AUTOSAVE_INT.getInt() * 60
    			&& !editManager.isTransactionActive()) {
    		try {
				saveLHX();
				showTempStatusMessage(I18n.tr("messages.autosaved"));
			} catch (Exception e) {
				e.printStackTrace();
				showStatusMessage(e.getMessage());
			}
    	}
	}
	
    @Override
    public void render() {
    	try {
	    	float delta = Gdx.graphics.getDeltaTime();
	    	handleAutosave(delta);
	    	recoveryManager.act(delta);
	    	previewManager.act(delta);
	        uiScreen.render(delta);
    	} catch (Exception e) {
    		e.printStackTrace();
    		showError("Fatal error", e);
    	}
    }

    @Override
    public void resize(int width, int height) {
        uiScreen.resize(width, height);
    }

    @Override
    public void pause() {
        uiScreen.pause();
    }

    @Override
    public void resume() {
        uiScreen.resume();
    }

    @Override
    public void dispose() {
    	if (modelManager != null) modelManager.dispose();
    	if (countryPreviewGenerator != null) countryPreviewGenerator.close();
    	disposeGameTextures();
    	if (uiScreen != null) uiScreen.dispose();
    	if (skin != null) skin.dispose();
    	assetManager.dispose();
    	Settings.listeners.remove(settingsChangeListener);
    	Settings.save();
    }
}
