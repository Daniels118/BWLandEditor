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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import it.ld.bw.info.TribeType;
import it.ld.bw.lhx.Command;
import it.ld.bw.lhx.LHXFile;
import it.ld.bw.lhx.Statement;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lndgui.EditMode;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.Settings;
import it.ld.bw.lndgui.Settings.ShowCellAttrOption;
import it.ld.bw.lndgui.Settings.ShowGridOption;
import it.ld.bw.lndgui.Settings.ShowObjectOption;
import it.ld.bw.lndgui.Tool;
import it.ld.bw.lndgui.gfx.Coord;
import it.ld.bw.lndgui.gfx.LHX3D;
import it.ld.bw.lndgui.gfx.Land3D;
import it.ld.bw.lndgui.gfx.Object3D;
import it.ld.bw.lndgui.gfx.OrbitCamera.Mode;
import it.ld.bw.lndgui.interfaces.ClipboardListener.DataType;
import it.ld.bw.lndgui.tools.Selection;
import it.ld.bw.lndgui.ui.View3D.ObjectSelectionCallback;
import it.ld.bw.lndgui.ui.View3D.SelectionCallback;
import it.ld.bw.serializer.FotFile;
import it.ld.libgdx.ui.components.Menu;
import it.ld.libgdx.ui.components.MenuItemAction;
import it.ld.libgdx.ui.components.MessageBox;
import it.ld.libgdx.ui.components.PopupMenu;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.ToolBar;
import it.ld.libgdx.ui.docking.DockContainer;
import it.ld.libgdx.ui.docking.DockingManager;
import it.ld.utils.UChangeListener;

public class UIScreen implements Screen {
	private final static float TEMP_MSG_DURATION = 2f;
	
	private final MainApp app;
	
	private LndFile land;
	private FotFile footpaths;
	private LHXFile lhx;
	
	private Stage stage;
	private DockingManager dockingManager;
    private final Skin skin;
    private final Table root;
    private final Menu menuBar;
    private MenuItemAction menuUndo, menuRedo;
    private final ToolBar mainToolBar;
    private final Container<Actor> leftToolbar = new Container<>();
    private final DockContainer leftPanel = new DockContainer("left-pane", Align.left);
    private final DockContainer topPanel = new DockContainer("top-pane", Align.top);
    private final DockContainer rightPanel = new DockContainer("right-pane", Align.right);
    private final DockContainer bottomPanel = new DockContainer("bottom-pane", Align.bottom);
    private final ToolBar sculptToolBar;
    private final ToolBar footpathToolBar;
    private final ToolBar scriptToolBar;
    private final Table statusbar;
    private final Label statusText;
    private final Label coordText;
    private final Label blocksText;
    private final Label heapText;
    
    private String statusMsg = "";
    private float tmpMsgTime = 0;
    
    private final View3D view3D;
    private CountryEditor countryEditor;
    private MaterialsEditor materialsEditor;
    
    private Button toolbarSave;
    private Button toolbarCopy;
    private Button toolbarCut;
    private Button toolbarPaste;
    private Button toolbarDelete;
    private Button toolbarUndo;
    private Button toolbarRedo;
    private Button toolbarSelect;
    
    private Button moveCellsButton;
    private Button sculptBrushButton;
    private Button oceanBrushButton;
    private Button lakeBrushButton;
    private Button countryPaintBrushButton;
    private Button soundPaintBrushButton;
    
    private Coord measurementStart;
    private final Vector3 tmpVec0 = new Vector3();
    private final Vector3 tmpVec1 = new Vector3();
    
    private Runnable toolDisableAction = null;
    private Runnable toolResetAction = null;
    
    private boolean allDisabled = false;
    private SmartWindow enabledWindow = null;
    private Tool prevTool = null;
    
    private boolean hasSelectedCells = false;
    private boolean hasSelectedObjects = false;
    private boolean canCopy = false;
    private boolean canCut = false;
    private boolean canPaste = false;
    private boolean canDelete = false;
    
    public UIScreen(MainApp app) {
    	this.app = app;
    	this.skin = app.getSkin();
    	this.view3D = new View3D(app);
    	view3D.setShowGrid((ShowGridOption) Settings.SHOW_GRID.getValue());
    	view3D.setHighlightSelectedCountry(Settings.HIGHLIGHT_SELECTED_COUNTRY.getBool());
		view3D.setOutlineSelectedCountry(Settings.OUTLINE_SELECTED_COUNTRY.getBool());
		view3D.setShowCellAttr((ShowCellAttrOption) Settings.SHOW_CELLS_ATTR.getValue());
		view3D.setShowObjects((ShowObjectOption) Settings.SHOW_OBJECTS.getValue());
		view3D.setShowVirtualObjects(Settings.SHOW_VIRTUAL_OBJECTS.getBool());
		view3D.setShowCells(Settings.SHOW_CELLS.getBool());
		view3D.setShowLinks(Settings.SHOW_LINKS.getBool());
    	view3D.setCompassVisible(Settings.SHOW_COMPASS.getBool());
		view3D.setPreventCameraUnderground(Settings.PREVENT_CAMERA_UNDERGROUND.getBool());
    	view3D.listeners.add(view3DChangeListener);
    	
    	menuBar = buildMenuBar();
        mainToolBar = buildMainToolBar();
        
        sculptToolBar = buildSculptToolBar();
        footpathToolBar = buildFootpathToolBar();
        scriptToolBar = buildScriptToolBar();
        
        Table content = new Table(skin);
        content.add(view3D).grow();
        
        statusbar = new Table(skin);
        statusbar.defaults().pad(1);
        statusText = new Label("", skin, "textfield");
        statusbar.add(statusText).growX();
        coordText = new Label("", skin, "textfield");
        coordText.setAlignment(Align.center);
        statusbar.add(coordText).width(170);
        blocksText = new Label("", skin, "textfield");
        blocksText.setAlignment(Align.right);
        statusbar.add(blocksText).width(140);
        heapText = new Label("", skin, "textfield");
        heapText.setAlignment(Align.right);
        statusbar.add(heapText).width(140);
        
        root = new Table();
        root.setFillParent(true);
        root.top().left();
        root.add(menuBar).colspan(3).growX();
        root.row();
        root.add(mainToolBar).colspan(3).growX().padBottom(2);
        root.row();
        
        root.add(leftToolbar).fill().top();
        root.add(leftPanel).maxWidth(800).fill().top();
        Table t = new Table();
        t.add(topPanel).colspan(2).maxHeight(800).growX().row();
        t.add(content).grow();
        t.add(rightPanel).maxWidth(800).fill().top();
        t.row();
        t.add(bottomPanel).colspan(2).maxHeight(800).growX().row();
        root.add(t).grow().padLeft(2);
        root.row();
        
        root.add(statusbar).colspan(3).height(24).growX().pad(2).row();
        
        leftToolbar.top().fill();
        leftToolbar.setActor(sculptToolBar);
        
        app.listeners.add(appChangeListener);
        Settings.listeners.add(settingsChangeListener);
	}
    
    public void showIntroWindow() {
    	IntroWindow.showSingleInstance(app, stage, skin);
    }
    
    public void showError(String title, Throwable e) {
		MessageBox.show(stage, title, e);
	}
    
    public void showError(String title, Throwable e, Runnable callback) {
		MessageBox.show(stage, title, e, callback == null ? null : ((choice) -> callback.run()));
	}
    
    public Stage getStage() {
    	return this.stage;
    }
    
    public DockingManager getDockingManager() {
    	return dockingManager;
    }
    
    public View3D getView3D() {
    	return this.view3D;
    }
    
    public void setLand(LndFile lndFile) {
    	if (this.land != null) {
    		this.land.listeners.remove(landChangeListener);
    	}
    	this.land = lndFile;
    	if (this.land != null) {
	    	this.view3D.setLand(land);
	    	this.view3D.resetView();
	    	this.land.listeners.add(landChangeListener);
    	}
    	updateBlocksStatus();
    	toolbarSave.setDisabled(true);
    }
    
    public void setFootpaths(FotFile fotFile) {
    	this.footpaths = fotFile;
    	this.view3D.setFootpaths(footpaths);
    }
    
    public void setLHX(LHXFile lhxFile) {
    	this.lhx = lhxFile;
    	this.view3D.setLHX(lhx);
    }
    
    private final UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.LAND) {
				setLand(app.getLand());
			} else if (event.getProperty() == MainApp.Property.FOOTPATHS) {
				setFootpaths(app.getFootpaths());
			} else if (event.getProperty() == MainApp.Property.LHX) {
				setLHX(app.getLHX());
			} else if (event.getProperty() == MainApp.Property.LAND_CHANGED || event.getProperty() == MainApp.Property.LHX_CHANGED) {
				toolbarSave.setDisabled(!app.hasUnsavedChanges());
			} else if (event.getProperty() == MainApp.Property.UNDO_HISTORY) {
				undoHistoryChanged();
			} else if (event.getProperty() == MainApp.Property.EDIT_MODE) {
				editModeChanged();
			} else if (event.getProperty() == MainApp.Property.SELECTION_MODE) {
				view3D.setSelectionMode(app.getSelectionMode());
			} else if (event.getProperty() == MainApp.Property.ACTIVE_TOOL) {
				activeToolChanged();
			} else if (event.getProperty() == MainApp.Property.SCULPT_BRUSH && app.getActiveTool() == Tool.SCULPT) {
				view3D.setBrush(app.getSculptBrush());
			} else if (event.getProperty() == MainApp.Property.COUNTRY_BRUSH && app.getActiveTool() == Tool.COUNTRY_PAINT) {
				view3D.setBrush(app.getCountryBrush());
			} else if (event.getProperty() == MainApp.Property.CLIPBOARD) {
				updateClipboardDependentButtons();
			}
		}
	};
	
	private final UChangeListener settingsChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == Settings.SHOW_GRID) {
				view3D.setShowGrid((ShowGridOption) Settings.SHOW_GRID.getValue());
			} else if (event.getProperty() == Settings.HIGHLIGHT_SELECTED_COUNTRY) {
				view3D.setHighlightSelectedCountry(Settings.HIGHLIGHT_SELECTED_COUNTRY.getBool());
			} else if (event.getProperty() == Settings.OUTLINE_SELECTED_COUNTRY) {
				view3D.setOutlineSelectedCountry(Settings.OUTLINE_SELECTED_COUNTRY.getBool());
			} else if (event.getProperty() == Settings.SHOW_CELLS_ATTR) {
				view3D.setShowCellAttr((ShowCellAttrOption) Settings.SHOW_CELLS_ATTR.getValue());
			} else if (event.getProperty() == Settings.SHOW_OBJECTS) {
				view3D.setShowObjects((ShowObjectOption) Settings.SHOW_OBJECTS.getValue());
			} else if (event.getProperty() == Settings.SHOW_VIRTUAL_OBJECTS) {
				view3D.setShowVirtualObjects(Settings.SHOW_VIRTUAL_OBJECTS.getBool());
			} else if (event.getProperty() == Settings.SHOW_CELLS) {
				view3D.setShowCells(Settings.SHOW_CELLS.getBool());
			} else if (event.getProperty() == Settings.SHOW_LINKS) {
				view3D.setShowLinks(Settings.SHOW_LINKS.getBool());
			} else if (event.getProperty() == Settings.SHOW_COMPASS) {
				view3D.setCompassVisible(Settings.SHOW_COMPASS.getBool());
			} else if (event.getProperty() == Settings.PREVENT_CAMERA_UNDERGROUND) {
				view3D.setPreventCameraUnderground(Settings.PREVENT_CAMERA_UNDERGROUND.getBool());
			}
		}
	};
	
	private void undoHistoryChanged() {
		String undoDescr = app.getEditManager().getUndoDescription();
		String redoDescr = app.getEditManager().getRedoDescription();
		menuUndo.label = I18n.tr("menu.edit.undo", undoDescr);
		menuRedo.label = I18n.tr("menu.edit.redo", redoDescr);
		mainToolBar.setTooltip(toolbarUndo, I18n.tr("toolbar.undo", undoDescr));
		mainToolBar.setTooltip(toolbarRedo, I18n.tr("toolbar.redo", redoDescr));
		toolbarUndo.setDisabled(!app.getEditManager().canUndo());
		toolbarRedo.setDisabled(!app.getEditManager().canRedo());
	}
	
	private ShowObjectOption prevShowObject;
	private Boolean prevShowVirtualObject;
	
	private void editModeChanged() {
		view3D.clearSelectCells();
		view3D.setFootpathsVisible(app.getEditMode() == EditMode.FOOTPATH);
		if (app.getEditMode() != EditMode.OBJ_PLACEMENT) {
			CatalogWindow.hideInstance();
			LHXTreeWindow.hideInstance();
			LHXScriptWindow.hideInstance();
			ObjectAttrWindow.hideInstance();
			if (prevShowObject != null) {
				Settings.SHOW_OBJECTS.setValue(prevShowObject);
			}
			if (prevShowVirtualObject != null) {
				Settings.SHOW_VIRTUAL_OBJECTS.setValue(prevShowVirtualObject);
			}
		}
		if (app.getEditMode() != EditMode.SCRIPT) {
			CameraInfoWindow.hideInstance();
		}
		switch (app.getEditMode()) {
			case SCULPT:
				leftToolbar.setActor(sculptToolBar);
				break;
			case FOOTPATH:
				leftToolbar.setActor(footpathToolBar);
				break;
			case OBJ_PLACEMENT:
				leftToolbar.setActor(null);
				leftPanel.dock(CatalogWindow.showSingleInstance(app, stage, skin));
				leftPanel.dock(LHXTreeWindow.showSingleInstance(app, stage, skin));
				ObjectAttrWindow.showSingleInstance(app, stage, skin);
				prevShowObject = (ShowObjectOption) Settings.SHOW_OBJECTS.getValue();
				if (Settings.SHOW_OBJECTS.getValue() == ShowObjectOption.OFF) {
					Settings.SHOW_OBJECTS.setValue(ShowObjectOption.ALL);
				}
				if (prevShowVirtualObject == null) {
					prevShowVirtualObject = Settings.SHOW_VIRTUAL_OBJECTS.getBool();
					Settings.SHOW_VIRTUAL_OBJECTS.setValue(true);
				} else {
					prevShowVirtualObject = Settings.SHOW_VIRTUAL_OBJECTS.getBool();
				}
				break;
			case SCRIPT:
				CameraInfoWindow.showSingleInstance(app, stage, skin);
				break;
		}
		updateClipboardDependentButtons();
	}
	
	private void activeToolChanged() {
		if (toolResetAction != null) {
			toolResetAction.run();
			toolResetAction = null;
		}
		if (toolDisableAction != null) {
			toolDisableAction.run();
			toolDisableAction = null;
		}
		switch (app.getActiveTool()) {
			case MEASURE_DISTANCE:
				measureDistance();
				toolResetAction = () -> measureDistance();
				toolDisableAction = () -> stopMeasureDistance();
				break;
			case PICK_COORD:
				pickCoord();
				toolDisableAction = () -> stopPickCoord();
				break;
			case SELECT:
				EditMode mode = app.getEditMode();
				if (mode == EditMode.SCULPT) {
					view3D.enableCellSelection(selectionCallback, app.getSelectionMode());
					SelectModeWindow.showSingleInstance(app, stage, toolbarSelect, Align.bottom, skin);
					toolDisableAction = () -> {
						SelectModeWindow.hideInstance();
						view3D.disableCellSelection();
					};
				} else {
					view3D.enableObjectSelection(objectSelectionCallback, app.getSelectionMode());
					SelectModeWindow.showSingleInstance(app, stage, toolbarSelect, Align.bottom, skin);
					toolDisableAction = () -> {
						SelectModeWindow.hideInstance();
						view3D.disableObjectSelection();
					};
				}
				break;
			case SCULPT:
				view3D.setBrush(app.getSculptBrush());
				SculptOptionsWindow.showSingleInstance(app, stage, sculptBrushButton, skin);
				toolDisableAction = () -> {
					SculptOptionsWindow.hideInstance();
					view3D.setBrush(null);
				};
				break;
			case OCEAN:
				view3D.setBrush(app.getOceanBrush());
				WaterOptionsWindow.showSingleInstance(app, I18n.tr("oceanOptions.title"), app.getOceanBrush(), stage, oceanBrushButton, skin);
				toolDisableAction = () -> {
					WaterOptionsWindow.hideInstance();
					view3D.setBrush(null);
				};
				break;
			case LAKE:
				view3D.setBrush(app.getLakeBrush());
				WaterOptionsWindow.showSingleInstance(app, I18n.tr("lakeOptions.title"), app.getLakeBrush(), stage, lakeBrushButton, skin);
				toolDisableAction = () -> {
					WaterOptionsWindow.hideInstance();
					view3D.setBrush(null);
				};
				break;
			case COUNTRY_PAINT:
				view3D.setBrush(app.getCountryBrush());
				CountryPaintOptionsWindow.showSingleInstance(app, stage, countryPaintBrushButton, skin);
				toolDisableAction = () -> {
					CountryPaintOptionsWindow.hideInstance();
					view3D.setBrush(null);
				};
				break;
			case SOUND_PAINT:
				final Object prevShowCellAttr = Settings.SHOW_CELLS_ATTR.getValue();
				Settings.SHOW_CELLS_ATTR.setValue(ShowCellAttrOption.SOUNDS);
				view3D.setBrush(app.getSoundBrush());
				SoundPaintOptionsWindow.showSingleInstance(app, stage, soundPaintBrushButton, skin);
				toolDisableAction = () -> {
					SoundPaintOptionsWindow.hideInstance();
					view3D.setBrush(null);
					Settings.SHOW_CELLS_ATTR.setValue(prevShowCellAttr);
				};
				break;
			default:
		}
	}
	
	private final UChangeListener landChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LndFile.Property.BLOCKS) {
				if (event.getType() == EventType.ADD || event.getType() == EventType.REMOVE) {
					updateBlocksStatus();
				}
			} else if (event.getProperty() == LndFile.Property.MAX_BLOCKS) {
				updateBlocksStatus();
			}
		}
	};
	
	private final UChangeListener view3DChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == View3D.Property.SELECTION) {
				updateSelectionDependentButtons();
			}
		}
	};
	
	private final SelectionCallback selectionCallback = new SelectionCallback() {
		@Override
		public void onSelect(Selection tmpSelection, Land3D land3D) {
			if (app.getEditMode() != EditMode.SCULPT) {
				land3D.clearSelection();
			}
		}
	};
	
	private final ObjectSelectionCallback objectSelectionCallback = new ObjectSelectionCallback() {
		@Override
		public void onSelect(LHX3D lhx3d) {
			if (app.getEditMode() != EditMode.OBJ_PLACEMENT) {
				lhx3d.clearSelection();
			}
		}
	};
	
	private void updateSelectionDependentButtons() {
		hasSelectedCells = view3D.hasSelectedCells();
		hasSelectedObjects = view3D.hasSelectedObjects();
		canCopy = hasSelectedCells || hasSelectedObjects;
		canCut = hasSelectedCells || hasSelectedObjects;
		canDelete = hasSelectedCells || hasSelectedObjects;
		toolbarCopy.setDisabled(!canCopy);
		toolbarCut.setDisabled(!canCut);
		toolbarDelete.setDisabled(!canDelete);
		moveCellsButton.setDisabled(!hasSelectedCells);
	}
	
	private void updateClipboardDependentButtons() {
		DataType type = app.getOS().getClipboardContentType();
		canPaste = type != null;
		toolbarPaste.setDisabled(!canPaste);
	}
	
	public void showStatusMessage(String msg) {
		statusMsg = msg;
		if (tmpMsgTime <= 0) {
			statusText.setText(statusMsg);
		}
	}
	
	public void showTempStatusMessage(String msg) {
		statusText.setText(msg);
		tmpMsgTime = TEMP_MSG_DURATION;
	}
	
	private void updateBlocksStatus() {
		if (land != null) {
			blocksText.setText(I18n.tr("statusbar.usedBlocks", land.getNumBlocks(), land.getMaxBlocks()));
			blocksText.setColor(land.getNumBlocks() < land.getMaxBlocks() ? Color.WHITE : Color.RED);
		} else {
			blocksText.setText("");
			blocksText.setColor(Color.WHITE);
		}
	}
	
	private void updateCoordStatus(Coord coord) {
		coordText.setText(coord == null ? "" : String.format(Locale.US, "%#.1f, %#.1f, %#.1f", coord.x, coord.y, coord.z));
	}
	
	private void updateMsgStatus(float delta) {
    	if (tmpMsgTime > 0) {
        	tmpMsgTime -= delta;
        	if (tmpMsgTime <= 0) {
        		statusText.setText(statusMsg);
        	}
        }
    }
	
	private float heapUpdateDelay = 0;
	
	private void updateHeapStatus(float dt) {
		heapUpdateDelay -= dt;
		if (heapUpdateDelay < 0) {
			float used = Gdx.app.getJavaHeap() / (1024 * 1024);
			float max = Runtime.getRuntime().maxMemory() / (1024 * 1024);
			String unit = "MB";
			if (max >= 1024) {
				used /= 1024f;
				max /= 1024f;
				unit = "GB";
			}
			DecimalFormat df = new DecimalFormat("#.#");
			heapText.setText(I18n.tr("statusbar.heap", df.format(used), df.format(max), unit));
			heapUpdateDelay = 3f;
		}
	}
    
    @Override
    public void show() {
        stage = new StageEx(new ScreenViewport()) {
        	@Override
        	public void onException(Exception e) {
        		app.disableAutosave();
        		showError(I18n.tr("dialog.fatalError"), e);
        	}
        };
        stage.addCaptureListener(new InputListener() {
        	@Override
        	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        		if (enabledWindow != null) {
        			Actor actor = event.getTarget();
        			while (actor != null && !(actor instanceof SmartWindow)) {
        				actor = actor.getParent();
        			}
        			if (actor instanceof SmartWindow && actor != enabledWindow && event.getTarget() != ((SmartWindow)actor).getTitleLabel()) {
        				enabledWindow.flash();
        			}
        		}
        		return false;
        	}
        	
        	@Override
        	public boolean keyDown(InputEvent event, int keycode) {
        		if (keycode == Keys.ESCAPE && toolResetAction != null) {
        			toolResetAction.run();
        		}
        		return false;
        	}
        });
        
        dockingManager = new DockingManager(stage);
        dockingManager.addContainer(leftPanel);
        dockingManager.addContainer(topPanel);
        dockingManager.addContainer(rightPanel);
        dockingManager.addContainer(bottomPanel);
        
        view3D.addListener(new InputListener() {
        	private boolean dragObjects = false;
        	private boolean dragging = false;
        	
        	@Override
        	public boolean keyUp(InputEvent event, int keycode) {
        		Actor focus = stage.getKeyboardFocus();
        		if (focus instanceof TextField) return false;
        		if (menuBar.isDisabled()) return false;
        		Runnable action = menuBar.findShortcutAction(keycode,
        				Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT),
        				Gdx.input.isKeyPressed(Keys.SHIFT_LEFT), Gdx.input.isKeyPressed(Keys.ALT_LEFT));
        		if (action != null && !isModalWindowActive()) {
        			Gdx.app.postRunnable(action);
        			return true;
        		}
        		return false;
        	}
        	
        	@Override
        	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        		if (view3D.isPicking()) return false;
        		try {
        			Tool tool = app.getActiveTool();
	        		if (tool == Tool.MOVE_CELLS && button == Buttons.LEFT) {
	        			Coord coord = view3D.getCursorCoord();
	        			if (coord != null && view3D.isWithinSelectedCells(coord)) {
	        				app.beginDragLand();
	        				event.handle();
	        			}
	        		} else if (tool == Tool.POINTER && button == Buttons.LEFT) {
	        			Object3D obj = view3D.pickObject(x, y);
	        			if (!Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
	        				List<Object3D> selObjects = view3D.getSelectedObjects();
		        			if (!selObjects.contains(obj)) {
		            			view3D.setSelectedObject(obj);
		        			}
	        			}
	        			dragObjects = obj != null;
	        			event.handle();
	        		}
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        		dragging = false;
        		return true;
        	}
        	
        	@Override
        	public void touchDragged(InputEvent event, float x, float y, int pointer) {
        		Coord coord = view3D.getCursorCoord();
        		updateCoordStatus(coord);
        		if (dragObjects) {
        			if (!dragging) {
        				if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
        					List<Object3D> selObjects = view3D.getSelectedObjects();
        					List<Statement> newStatements = new ArrayList<>(selObjects.size());
        					for (Object3D obj : selObjects) {
        						Statement stmt = obj.getStatement().clone();
        						newStatements.add(stmt);
        					}
        					app.getLHX().getStatements().addAll(newStatements);
        					view3D.setSelectedStatements(newStatements);
        				}
    					app.beginDragObjects();
        			}
        			event.handle();
        		}
        		dragging = true;
        	}
        	
        	@Override
        	public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        		Coord coord = view3D.getCursorCoord();
        		updateCoordStatus(coord);
        		if (button == Buttons.LEFT) {
        			Tool tool = app.getActiveTool();
        			if (!dragging && tool == Tool.POINTER) {
        				Object3D obj = view3D.pickObject(x, y);
        				if (obj != null) {
	        				if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
	        					obj.setSelected(!obj.isSelected());
	        				} else {
	        					view3D.setSelectedObject(obj);
	        				}
	        			}
	        		}
        			if (dragObjects) {
	        			dragObjects = false;
	        			event.handle();
	        		}
	        		dragging = false;
        		} else if (button == Buttons.RIGHT && !dragging) {
        			Ray ray = view3D.getPickRay(x, y);
        			List<Object3D> objects = view3D.getSelectedObjects();
        			boolean overSelection = false;
        			for (Object3D obj : objects) {
        				if (obj.intersect(ray)) {
        					overSelection = true;
        					break;
        				}
        			}
        			if (!overSelection) {
        				Object3D obj = view3D.pickObject(x, y);
            			objects.clear();
            			if (obj != null) {
	        				objects.add(obj);
            			}
    	        		view3D.setSelectedObject(obj);
        			}
        			showView3DContextMenu(event, objects);
        		}
        	}
        	
        	@Override
        	public boolean mouseMoved(InputEvent event, float x, float y) {
        		Coord coord = view3D.getCursorCoord();
        		updateCoordStatus(coord);
        		if (measurementStart != null) {
	        		if (coord != null) {
	        			measurementStart.toVector3(tmpVec0);
						coord.toVector3(tmpVec1);
						float d = tmpVec0.dst(tmpVec1);
						showStatusMessage(I18n.tr("measureDistance.result", d));
					} else {
						showStatusMessage("");
					}
        		}
        		return false;
        	}
        });
        Gdx.input.setInputProcessor(stage);
        stage.addActor(root);
        //
        updateClipboardDependentButtons();
        HistoryWindow.showSingleInstance(app, stage, skin);
    }
    
    private void showView3DContextMenu(InputEvent event, List<Object3D> objects) {
    	LinkedList<MenuItemAction> actions = new LinkedList<>();
    	if (objects.isEmpty()) {
    		if (this.canPaste) {
    			actions.add(new MenuItemAction("objects.copy", I18n.tr("toolbar.paste"), () -> app.paste()));
    		}
    	} else {
	    	Object3D obj0 = objects.get(0);
	    	boolean hasParent = false;
	    	boolean hasChildren = false;
	    	Command cmd = obj0.getStatement().getCommand();
	    	Object type = obj0.getStatement().getType();
	    	int town = cmd.town >= 0 ? obj0.getStatement().getTown() : -2;
	    	TribeType tribe = obj0.getStatement().getEffectiveTribe();
	    	for (Object3D obj : objects) {
	    		if (obj.getParent() != null && obj.getParent().getStatement() != null) {
	    			hasParent = true;
	    		}
	    		if (!obj.getChildren().isEmpty()) {
	    			hasChildren = true;
	    		}
	    		if (obj.getStatement().getCommand() != cmd) {
	    			cmd = null;
	    			type = null;
	    		}
	    		if (!Objects.equals(obj.getStatement().getType(), type)) {
	    			type = null;
	    		}
	    		if (obj.getStatement().getTown() != town) {
	    			town = -2;
	    		}
	    		if (obj.getStatement().getEffectiveTribe() != tribe) {
	    			tribe = null;
	    		}
	    	}
	    	//
	    	if (objects.size() > 1 && (cmd == null || type == null || town == -2 || tribe == null)) {
				actions.add(new MenuItemAction("objects.refineSelection", I18n.tr("objects.refineSelection"), () -> app.refineSelection()));
	    	}
			actions.add(new MenuItemAction("objects.extendSelection", I18n.tr("objects.extendSelection"), () -> app.extendSelection()));
			if (hasParent || hasChildren) {
				actions.add(PopupMenu.separator);
				if (hasParent) {
		    		actions.add(new MenuItemAction("objects.selectParents", I18n.tr("objects.selectParents"), () -> app.selectParents()));
		    		actions.add(new MenuItemAction("objects.selectSiblings", I18n.tr("objects.selectSiblings"), () -> app.selectSiblings()));
		    	}
				if (hasChildren) {
		    		actions.add(new MenuItemAction("objects.selectChildren", I18n.tr("objects.selectChildren"), () -> app.selectChildren()));
		    		actions.add(new MenuItemAction("objects.selectDescendants", I18n.tr("objects.selectDescendants"), () -> app.selectDescendants()));
		    	}
			}
	    	actions.add(PopupMenu.separator);
	    	if (objects.size() == 1) {
	    		Object3D obj = objects.get(0);
	    		actions.add(new MenuItemAction("objects.revealInTreeView", I18n.tr("objects.revealInTreeView"), () -> {
	    			if (obj.getStatement().getCommand() == Command.CREATE_STREAM_POINT) {
	    				revealInTreeView(obj.getParent());
	    			} else {
	    				revealInTreeView(obj);
	    			}
	    		}));
	    		actions.add(PopupMenu.separator);
	    	}
	    	actions.add(new MenuItemAction("objects.copy", I18n.tr("toolbar.copy"), () -> app.copySelectedObjects()));
	    	actions.add(new MenuItemAction("objects.copy", I18n.tr("toolbar.cut"), () -> app.cutSelectedObjects()));
			actions.add(new MenuItemAction("objects.delete", I18n.tr("toolbar.delete"), () -> app.deleteSelectedObjects()));
    	}
    	if (!actions.isEmpty()) {
    		PopupMenu.show1(getStage(), event.getStageX(), event.getStageY(), actions.toArray(new MenuItemAction[0]));
		}
    }
    
    private void revealInTreeView(Object3D object) {
    	LHXTreeWindow.showSingleInstance(app, stage, skin).showObject(object);
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        updateMsgStatus(delta);
        updateHeapStatus(delta);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void hide() {}
	
	private Menu buildMenuBar() {
		Menu r = new Menu(skin);
	    
	    r.add(I18n.tr("menu.file"),
    		new MenuItemAction("file.new",  I18n.tr("menu.file.new"),  "Ctrl+N", () -> app.newProject()),
            new MenuItemAction("file.open", I18n.tr("menu.file.open"), "Ctrl+O", () -> app.openLandDialog()),
            new MenuItemAction("file.more", I18n.tr("menu.file.more"), () -> IntroWindow.showSingleInstance(app, stage, skin)),
            PopupMenu.separator,
            new MenuItemAction("file.save", I18n.tr("menu.file.save"), "Ctrl+S", () -> app.save(), (e) -> app.hasUnsavedChanges()),
            new MenuItemAction("file.saveLandAs", I18n.tr("menu.file.saveLandAs"), () -> app.saveLandAsDialog()),
            new MenuItemAction("file.saveLHXAs", I18n.tr("menu.file.saveLHXAs"), () -> app.saveLHXAsDialog()),
            PopupMenu.separator,
            new MenuItemAction("file.importLand", I18n.tr("menu.file.importLand"), () -> app.importLandDialog()),
            new MenuItemAction("file.importHeightMap", I18n.tr("menu.file.importHeightMap"), () -> app.importHeightMapDialog()),
            new MenuItemAction("file.exportHeightMap", I18n.tr("menu.file.exportHeightMap"), () -> app.exportHeightMapDialog()),
            PopupMenu.separator,
            new MenuItemAction("file.exit", I18n.tr("menu.file.exit"), () -> app.exit())
	    );
	    
	    r.add(I18n.tr("menu.edit"),
	    	menuUndo = new MenuItemAction("edit.undo",  I18n.tr("menu.edit.undo"),  "Ctrl+Z", () -> app.getEditManager().undo(), (e) -> app.getEditManager().canUndo()),
	        menuRedo = new MenuItemAction("edit.redo", I18n.tr("menu.edit.redo"), "Ctrl+SHIFT+Z", () -> app.getEditManager().redo(), (e) -> app.getEditManager().canRedo()),
	        PopupMenu.separator,
	        new MenuItemAction("edit.copy", I18n.tr("menu.edit.copy"), "Ctrl+C", () -> app.copy(), (e) -> canCopy),
	        new MenuItemAction("edit.cut", I18n.tr("menu.edit.cut"), "Ctrl+X", () -> app.cut(), (e) -> canCut),
	        new MenuItemAction("edit.paste", I18n.tr("menu.edit.paste"), "Ctrl+V", () -> app.paste(), (e) -> canPaste),
	        new MenuItemAction("edit.delete", I18n.tr("menu.edit.delete"), "Del", () -> app.delete(), (e) -> canDelete),
	        PopupMenu.separator,
	        new MenuItemAction("edit.blocksGrid", I18n.tr("menu.edit.blocksGrid"), () -> app.resizeGridDialog()),
	        new MenuItemAction("edit.maxBlocks", I18n.tr("menu.edit.maxBlocks"), () -> app.editMaxBlocksDialog()),
	        new MenuItemAction("edit.maxAltitude", I18n.tr("menu.edit.maxAltitude"), () -> app.editMaxAltitudeDialog()),
		    new MenuItemAction("edit.updateShadows", I18n.tr("menu.edit.updateShadows"), () -> app.updateShadows()),
		    PopupMenu.separator,
		    new MenuItemAction("edit.lhxSettings", I18n.tr("menu.edit.lhxSettings"), () -> LandAttrWindow.showSingleInstance(app, stage, skin)),
		    PopupMenu.separator,
            new MenuItemAction("edit.settings", I18n.tr("menu.edit.settings"), () -> SettingsWindow.showSingleInstance(app, stage, skin, false))
	    );
	    
	    r.add(I18n.tr("menu.view"),
		    new MenuItemAction("view.reset", I18n.tr("menu.view.reset"), "Ctrl+Numpad 0", () -> view3D.resetView()),
		    new MenuItemAction("view.top", I18n.tr("menu.view.top"), "Ctrl+Numpad 5", () -> view3D.viewFromTop()),
		    new MenuItemAction("view.flyTo", I18n.tr("menu.view.flyTo"), "Ctrl+G", () -> app.flyTo()),
		    PopupMenu.separator,
            new MenuItemAction("view.perspective", I18n.tr("menu.view.perspective"), () -> setCameraMode(Mode.PERSPECTIVE),
            		e -> getCameraMode() != Mode.PERSPECTIVE, e -> getCameraMode() == Mode.PERSPECTIVE),
            new MenuItemAction("view.orthographic", I18n.tr("menu.view.orthographic"), () -> setCameraMode(Mode.ORTHOGRAPHIC),
            		e -> getCameraMode() != Mode.ORTHOGRAPHIC, e -> getCameraMode() == Mode.ORTHOGRAPHIC)
		);
	    
	    r.add(I18n.tr("menu.tools"),
	    	new MenuItemAction("countryEditor.open", I18n.tr("menu.tools.countryEditor"), () -> CountryEditor.showSingleInstance(app, stage, skin)),
            new MenuItemAction("materialsEditor.open", I18n.tr("menu.tools.materialsEditor"), () -> MaterialsEditor.showSingleInstance(app, stage, skin)),
            new MenuItemAction("landGenerator.open", I18n.tr("menu.tools.landGenerator"), () -> LandGeneratorWindow.showSingleInstance(app, stage, skin)),
            new MenuItemAction("noiseMapEditor.open", I18n.tr("menu.tools.noiseMapEditor"), () -> NoiseMapEditor.showSingleInstance(app, stage, skin)),
            new MenuItemAction("coastlineBumpMapEditor.open", I18n.tr("menu.tools.coastlineBumpMapEditor"), () -> CoastlineBumpMapEditor.showSingleInstance(app, stage, skin)),
            PopupMenu.separator,
            new MenuItemAction("smallBumpViewer.open", I18n.tr("menu.tools.smallBumpViewer"), () ->
            	TextureViewer.showSingleInstance(app, stage, skin, I18n.tr("menu.tools.smallBumpViewer"), app.getSmallBump())),
            new MenuItemAction("skyTextureViewer.open", I18n.tr("menu.tools.skyTextureViewer"), () ->
            	TextureViewer.showSingleInstance(app, stage, skin, I18n.tr("menu.tools.skyTextureViewer"), app.getSkyTexture())),
            new MenuItemAction("modelViewer.open", I18n.tr("menu.tools.modelViewer"), () -> ModelViewerWindow.showSingleInstance(app, stage, skin))
	    );
	    
	    r.add(I18n.tr("menu.windows"),
	    	new MenuItemAction("blockInspector.open", I18n.tr("menu.windows.blockInspector"), () -> BlockInspectorWindow.showSingleInstance(app, stage, skin)),
	    	PopupMenu.separator,
	    	new MenuItemAction("objectAttr.open", I18n.tr("menu.windows.objectAttr"), () -> ObjectAttrWindow.showSingleInstance(app, stage, skin)),
	    	new MenuItemAction("catalogWindow.open", I18n.tr("menu.windows.catalogWindow"), () -> CatalogWindow.showSingleInstance(app, stage, skin)),
	    	new MenuItemAction("lhxTreeWindow.open", I18n.tr("menu.windows.lhxTreeWindow"), () -> LHXTreeWindow.showSingleInstance(app, stage, skin)),
	    	new MenuItemAction("lhxScript.open", I18n.tr("menu.windows.lhxScript"), () -> LHXScriptWindow.showSingleInstance(app, stage, skin)),
	    	PopupMenu.separator,
            new MenuItemAction("cameraInfo.open", I18n.tr("menu.windows.cameraInfo"), () -> CameraInfoWindow.showSingleInstance(app, stage, skin)),
            PopupMenu.separator,
	    	new MenuItemAction("editHistory.open", I18n.tr("menu.windows.history"), () -> HistoryWindow.showSingleInstance(app, stage, skin)),
	    	new MenuItemAction("consoleWindow.open", I18n.tr("menu.windows.console"), () -> OutputWindow.showSingleInstance(app, stage, skin))
	    );
	    
	    r.add(I18n.tr("menu.help"),
		    new MenuItemAction("guide.open", I18n.tr("menu.help.guide"), "F1", () -> app.openUserGuide()),
		    new MenuItemAction("about.open", I18n.tr("menu.help.about"), () -> AboutWindow.showSingleInstance(app, stage, skin))
		);
	    
		return r;
	}
	
	private ToolBar buildMainToolBar() {
	    ToolBar r = new ToolBar(skin, 36, true);
	    r.add("file-new", I18n.tr("toolbar.new"), () -> app.newProject());
	    r.add("file-open", I18n.tr("toolbar.open"), () -> app.openLandDialog());
	    toolbarSave = r.add("file-save", I18n.tr("toolbar.save"), false, () -> app.save());
	    
	    r.addGroup();
	    toolbarUndo = r.add("undo", I18n.tr("toolbar.undo"), false, () -> app.getEditManager().undo());
	    toolbarRedo = r.add("redo", I18n.tr("toolbar.redo"), false, () -> app.getEditManager().redo());
	    
	    r.addGroup();
	    toolbarCopy = r.add("copy", I18n.tr("toolbar.copy"), false, () -> app.copy());
	    toolbarCut = r.add("cut", I18n.tr("toolbar.cut"), false, () -> app.cut());
	    toolbarPaste = r.add("paste", I18n.tr("toolbar.paste"), false, () -> app.paste());
	    toolbarDelete = r.add("tool-delete", I18n.tr("toolbar.delete"), false, () -> app.delete());
	    
	    r.addGroup();
	    r.add("tool-grid", I18n.tr("toolbar.grid"), () -> Settings.SHOW_GRID.cycle(),
	    		() -> Settings.SHOW_GRID.getOrdinal(),
	    		new Color[] {Color.WHITE, Color.GREEN});
	    r.add("tool-layers", I18n.tr("toolbar.cellsAttr"), () -> Settings.SHOW_CELLS_ATTR.cycle(),
	    		() -> Settings.SHOW_CELLS_ATTR.getOrdinal(),
	    		new Color[] {Color.WHITE, Color.GREEN});
	    r.add("mode-objects", I18n.tr("toolbar.objects"), () -> Settings.SHOW_OBJECTS.cycle(),
	    		() -> Settings.SHOW_OBJECTS.getOrdinal(),
	    		new Color[] {Color.WHITE, Color.GREEN, Color.BLUE});
	    r.add("gears", I18n.tr("toolbar.virtualObjects"), () -> Settings.SHOW_VIRTUAL_OBJECTS.toggle(), () -> Settings.SHOW_VIRTUAL_OBJECTS.getBool());
	    r.add("tool-links", I18n.tr("toolbar.links"), () -> Settings.SHOW_LINKS.toggle(), () -> Settings.SHOW_LINKS.getBool());
	    r.add("tool-compass", I18n.tr("toolbar.compass"), () -> Settings.SHOW_COMPASS.toggle(), () -> Settings.SHOW_COMPASS.getBool());
	    
	    r.addGroup();
	    addTool(r, "tool-pointer", Tool.POINTER);
		toolbarSelect = r.add("tool-select", I18n.tr("tool.SELECT"), () -> {
	    	app.setActiveTool(Tool.SELECT);
	    	SelectModeWindow.showSingleInstance(app, stage, toolbarSelect, Align.bottom, skin);
		}, () -> app.getActiveTool() == Tool.SELECT);
		
		r.addGroup();
	    addTool(r, "tool-orbit", Tool.ORBIT);
		addTool(r, "tool-pan", Tool.PAN);
		
		r.addGroup();
		addTool(r, "tool-measure", Tool.MEASURE_DISTANCE);
		addTool(r, "tool-location", Tool.PICK_COORD);
		r.add("tool-camera", I18n.tr("toolbar.screenshot"), () -> app.saveScreenshot());
		
		//TODO remove this before build
		r.add("tool-magic-hat", "Test", () -> {
			MessageBox.show(stage, "Test", "This is just a test");
		});
	    
	    r.addFiller();
	    r.add("mode-sculpt", I18n.tr("toolbar.editmode.sculpt"), () -> app.setEditMode(EditMode.SCULPT), () -> app.getEditMode() == EditMode.SCULPT);
	    r.add("mode-footpath", I18n.tr("toolbar.editmode.footpath"), () -> app.setEditMode(EditMode.FOOTPATH), () -> app.getEditMode() == EditMode.FOOTPATH);
	    r.add("mode-objects", I18n.tr("toolbar.editmode.objPlacement"), () -> app.setEditMode(EditMode.OBJ_PLACEMENT), () -> app.getEditMode() == EditMode.OBJ_PLACEMENT);
	    r.add("mode-flow", I18n.tr("toolbar.editmode.script"), () -> app.setEditMode(EditMode.SCRIPT), () -> app.getEditMode() == EditMode.SCRIPT);
	    return r;
	}
	
	private ToolBar buildSculptToolBar() {
		ToolBar r = new ToolBar(skin, 52, false);
		
		moveCellsButton = addTool(r, "tool-move", Tool.MOVE_CELLS);
		moveCellsButton.setDisabled(true);
		
		r.addGroup();
		
		sculptBrushButton = addTool(r, "tool-sculpt", Tool.SCULPT);
		sculptBrushButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (event.isCancelled()) return;
				SculptOptionsWindow.showSingleInstance(app, stage, event.getListenerActor(), skin);
			}
		});
		
		oceanBrushButton = addTool(r, "tool-ocean", Tool.OCEAN);
		oceanBrushButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (event.isCancelled()) return;
				WaterOptionsWindow.showSingleInstance(app, I18n.tr("oceanOptions.title"), app.getOceanBrush(), stage, event.getListenerActor(), skin);
			}
		});
		
		lakeBrushButton = addTool(r, "tool-lake", Tool.LAKE);
		lakeBrushButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (event.isCancelled()) return;
				WaterOptionsWindow.showSingleInstance(app, I18n.tr("lakeOptions.title"), app.getLakeBrush(), stage, event.getListenerActor(), skin);
			}
		});
		
		r.addGroup();
		
		countryPaintBrushButton = addTool(r, "climate-regions", Tool.COUNTRY_PAINT);
		countryPaintBrushButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (event.isCancelled()) return;
				CountryPaintOptionsWindow.showSingleInstance(app, stage, event.getListenerActor(), skin);
			}
		});
		
		soundPaintBrushButton = addTool(r, "env-sounds", Tool.SOUND_PAINT);
		soundPaintBrushButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (event.isCancelled()) return;
				SoundPaintOptionsWindow.showSingleInstance(app, stage, event.getListenerActor(), skin);
			}
		});
		
	    r.addGroup();
	    
	    return r;
	}
	
	private ToolBar buildFootpathToolBar() {
		ToolBar r = new ToolBar(skin, 36, false);
		
	    r.addGroup();
	    
	    return r;
	}
	
	private ToolBar buildScriptToolBar() {
		ToolBar r = new ToolBar(skin, 36, false);
		
	    r.addGroup();
	    
	    return r;
	}
	
	private Button addTool(ToolBar toolbar, String iconName, Tool tool) {
		return toolbar.add(iconName, I18n.tr("tool."+tool.name()), () -> app.setActiveTool(tool), () -> app.getActiveTool() == tool);
	}
	
	public void disableAllExcept3DViewAnd(SmartWindow window) {
		allDisabled = true;
		menuBar.setDisabled(true);
		mainToolBar.setDisabled(true);
		sculptToolBar.setDisabled(true);
		scriptToolBar.setDisabled(true);
		for (SmartWindow tmpWindow : getWindows()) {
			tmpWindow.setDisabled(tmpWindow != window);
		}
		prevTool = app.getActiveTool();
		app.setActiveTool(Tool.NONE);
		enabledWindow = window;
	}
	
	public void enableAll() {
		if (allDisabled) {
			menuBar.setDisabled(false);
			mainToolBar.setDisabled(false);
			sculptToolBar.setDisabled(false);
			scriptToolBar.setDisabled(false);
			for (SmartWindow window : getWindows()) {
				window.setDisabled(false);
			}
			app.setActiveTool(prevTool);
			prevTool = null;
			enabledWindow = null;
			allDisabled = false;
		}
	}
	
	public Mode getCameraMode() {
		return view3D.getCamera().getMode();
	}
	
	public void setCameraMode(Mode mode) {
		view3D.getCamera().setMode(mode);
	}
	
	public void measureDistance() {
		showStatusMessage(I18n.tr("measureDistance.hint1"));
		measurementStart = null;
		view3D.cancelPickCoord();
		view3D.setMeasureVisible(false);
		view3D.pickCoord((coord0, button0) -> {
			measurementStart = coord0;
			view3D.setMeasureStart(coord0);
			view3D.setMeasureVisible(true);
			view3D.pickCoord((coord1, button1) -> {
				stopMeasureDistance();
				measureDistance();
			}, false);
		}, false);
	}
	
	public void stopMeasureDistance() {
		view3D.cancelPickCoord();
		view3D.setMeasureVisible(false);
		measurementStart = null;
		showStatusMessage("");
	}
	
	public void pickCoord() {
		showStatusMessage(I18n.tr("pickCoord.hint1"));
		view3D.cancelPickCoord();
		view3D.pickCoord((coord, button) -> {
			if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
				app.getExternalEditor().insertCoord(coord);
				showTempStatusMessage(I18n.tr("pickCoord.sent"));
			} else {
				String val = String.format(Locale.US, "[%.2f, %.2f]", coord.x, coord.z);
				Gdx.app.getClipboard().setContents(val);
				showTempStatusMessage(I18n.tr("pickCoord.copied"));
			}
			pickCoord();
		}, false);
	}
	
	public void stopPickCoord() {
		view3D.cancelPickCoord();
		showStatusMessage("");
	}
	
	private List<SmartWindow> getWindows() {
		List<SmartWindow> res = new LinkedList<>();
		for (Actor actor : stage.getRoot().getChildren()) {
			if (actor instanceof SmartWindow) {
				res.add((SmartWindow)actor);
			}
		}
		return res;
	}
	
	private boolean isModalWindowActive() {
		Actor focus = stage.getKeyboardFocus();
		for (Actor a = focus; a != null; a = a.getParent()) {
			if (a instanceof Window) {
				Window w = (Window) a;
				if (w.isModal() && w.isVisible()) {
					return true;
				}
			}
		}
		Array<Actor> children = stage.getRoot().getChildren();
		for (int i = children.size - 1; i >= 0; i--) {
			Actor a = children.get(i);
			if (a instanceof Window) {
				Window w = (Window) a;
				if (w.isModal() && w.isVisible()) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
    public void dispose() {
    	app.listeners.remove(appChangeListener);
        if (this.countryEditor != null) {
        	this.countryEditor.dispose();
        }
        if (this.materialsEditor != null) {
        	this.materialsEditor.dispose();
        }
        stage.dispose();
        skin.dispose();
    }
}
