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

import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.sin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

import it.ld.bw.lhx.Command;
import it.ld.bw.lhx.LHXFile;
import it.ld.bw.lhx.Statement;
import it.ld.bw.lnd.model.BulkUpdate;
import it.ld.bw.lnd.model.LH3DLandBlock;
import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lndgui.LandEditManager;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.SelectionMode;
import it.ld.bw.lndgui.Settings.ShowCellAttrOption;
import it.ld.bw.lndgui.Settings.ShowGridOption;
import it.ld.bw.lndgui.Settings.ShowObjectOption;
import it.ld.bw.lndgui.Tool;
import it.ld.bw.lndgui.gfx.BillboardQuad;
import it.ld.bw.lndgui.gfx.BillboardQuadAsset;
import it.ld.bw.lndgui.gfx.Block3D;
import it.ld.bw.lndgui.gfx.Bounds3D;
import it.ld.bw.lndgui.gfx.CompassBillboard;
import it.ld.bw.lndgui.gfx.Coord;
import it.ld.bw.lndgui.gfx.Cross3D;
import it.ld.bw.lndgui.gfx.LHX3D;
import it.ld.bw.lndgui.gfx.LandGrid;
import it.ld.bw.lndgui.gfx.LandMeasure;
import it.ld.bw.lndgui.gfx.Land3D;
import it.ld.bw.lndgui.gfx.Brush3D;
import it.ld.bw.lndgui.gfx.LandShader;
import it.ld.bw.lndgui.gfx.LandShader.LandAttribute;
import it.ld.bw.lndgui.gfx.MorphableShader;
import it.ld.bw.lndgui.gfx.MorphableShader.MorphableAttribute;
import it.ld.bw.lndgui.gfx.Object3D;
import it.ld.bw.lndgui.gfx.Ocean3D;
import it.ld.bw.lndgui.gfx.SkyShader;
import it.ld.bw.lndgui.tools.Brush;
import it.ld.bw.lndgui.tools.Selection;
import it.ld.bw.serializer.Footpath;
import it.ld.bw.serializer.FootpathLinkSave;
import it.ld.bw.serializer.FootpathNode;
import it.ld.bw.serializer.FotFile;
import it.ld.bw.serializer.MapCoords;
import it.ld.bw.lndgui.gfx.OrbitCamera;
import it.ld.bw.lndgui.gfx.Path3D;
import it.ld.bw.lndgui.gfx.Points3D;
import it.ld.bw.lndgui.gfx.Selection3D;
import it.ld.bw.lndgui.gfx.SelectionShader;
import it.ld.bw.lndgui.gfx.Sky;
import it.ld.libgdx.utils.DynamicShaderProvider;
import it.ld.libgdx.utils.DynamicShaderProvider.DynamicCreator;
import it.ld.utils.Listeners;
import it.ld.utils.UChangeListener;

public class View3D extends Widget implements Disposable {
	private static final float FIELD_OF_VIEW = 45f;
	private static final float VPAN_MAX_ANGLE = -0.1f;
	private static final float VPAN_SPEED = 0.001f;
	private static final float CAMERA_MAX_OCCLUSION_DISTANCE_PERCENT = 0.4f;
	private static final float BRUSH_SIZE_SPEED = 0.1f;
	
	public enum Property {SELECTION}
	public final Listeners listeners = new Listeners(this);
	
	private final MainApp app;
	
	private FrameBuffer fbo;
	private TextureRegion fboRegion;
	private boolean alphaEnabled = false;
	private boolean hovered = false;
	
	private ModelBatch modelBatch;
	private Environment environment;
	private LandShader landShader;
	private SkyShader skyShader;
	private SelectionShader selectionShader;
	
	private DynamicShaderProvider shaderProvider;
	
	private final OrbitCamera camera;
	private boolean preventCameraUnderground;
	
	private float mouseStartX;
	private float mouseStartY;
	private float mousePrevX;
	private float mousePrevY;
	private int cursorStartX;
	private int cursorStartY;
	private float rotationSpeed = 0.008f;
	private float zoomSpeedScroll = 0.15f;
	private float zoomSpeedDrag = zoomSpeedScroll / 4;
	private int downButton = -1;
	private boolean panning;
	private boolean dragging;
	private OrbitCamera parm = new OrbitCamera();
	private Vector3 startPoint;
	private Vector3 startPivot;
	private double startPitch;
	private Coord cursorCoord = null;
	
	private Sky sky;
	private Ocean3D ocean;
	private LndFile land;
	private Land3D land3D;
	private Bounds3D landBounds = new Bounds3D(0f, 0f, 0f, 100f);
	private List<Block3D> extraBlocks;
	
	private LandGrid grid;
	private ShowGridOption showGrid = ShowGridOption.NONE;
	private ShowCellAttrOption showCellAttr = ShowCellAttrOption.OFF;
	private ShowObjectOption showObjects = ShowObjectOption.ALL;
	private boolean showVirtualObjects = true;
	private boolean showLinks;
	private boolean showCells = false;
	private boolean highlightSelectedCountry = false;
	private boolean outlineSelectedCountry = false;
	
	private FotFile footpaths;
	private List<Path3D> paths3D = new ArrayList<>();
	private Cross3D obstaclePoints3D;
	private Points3D linkPoints3D;
	private boolean footpathsVisible = false;
	
	private Map<Object3D, List<Path3D>> links3D = new HashMap<>();
	
	private Brush brush;
	private Brush3D brush3D;
	
	private Map<Selection, Selection3D> selections = new IdentityHashMap<>();
	private Selection tmpSelection = new Selection();
	
	private LandMeasure measure;
	private boolean measureVisible = false;
	
	private Texture markerTexture;
	private BillboardQuadAsset markerAsset;
	private BillboardQuad tmpMarker;
	private boolean tmpMarkerVisible = false;
	
	private Texture compassTexture;
	private BillboardQuadAsset compassAsset;
	private CompassBillboard compassInstance;
	private boolean compassVisible = false;
	
	private CoordCallback pickCoordCallback = null;
	private boolean pickCoordCancelable = false;
	private Runnable pickCoordCancel = null;
	private CoordCallback mouseCoordCallback = null;
	private SelectionMode selectionMode = SelectionMode.NEW;
	private SelectionCallback cellSelectionCallback = null;
	private ObjectSelectionCallback objectSelectionCallback = null;
	private PickObjectCallback pickObjectCallback = null;
	private boolean pickObjectCancelable = false;
	private Runnable pickObjectCancel = null;
	
	private FrameCallback frameCallback = null;
	
	private LHXFile lhx = null;
	private LHX3D lhx3d = null;
	
	private final List<ModelInstance> extraObjects = new ArrayList<>();
	
	private Color backgroundColor = new Color(0.2f, 0.2f, 0.2f, 1f);
	private boolean skyEnabled = true;
	private boolean oceanEnabled = true;
	
	public View3D(MainApp app) {
		this.app = app;
		DefaultShader.Config config = new DefaultShader.Config();
		config.numBones = 64;
		shaderProvider = new DynamicShaderProvider(config);
		AssetManager manager = app.getAssetManager();
		manager.finishLoading();
		//
		shaderProvider.add(new DynamicCreator() {
			@Override
			public Shader create(Renderable renderable) {
				if (renderable.material.has(MorphableAttribute.MORPHABLE)) {
					return new MorphableShader(renderable);
				}
				return null;
			}
		});
		//
		landShader = new LandShader(manager.get("shaders/land.glsl", ShaderProgram.class));
		landShader.init();
		shaderProvider.add(landShader);
		//
		skyShader = new SkyShader(manager.get("shaders/sky.glsl", ShaderProgram.class));
		skyShader.init();
		shaderProvider.add(skyShader);
		//
		selectionShader = new SelectionShader(manager.get("shaders/selection.glsl", ShaderProgram.class));
		selectionShader.init();
		shaderProvider.add(selectionShader);
		//
		modelBatch = new ModelBatch(shaderProvider);
		//
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.6f, 0.6f, 0.6f, 1f, -1f, -1f));
		//
		sky = new Sky();
		ocean = new Ocean3D(app.getSkyTexture(), 0.5f, 1f / 500f);
		//
		markerTexture = manager.get("icons/marker.png", Texture.class);
		markerAsset = new BillboardQuadAsset(markerTexture, 4f, 10f, true, true);
		tmpMarker = new BillboardQuad(markerAsset);
		//
		measure = new LandMeasure();
		addSelection(tmpSelection);
		//
		compassTexture = manager.get("textures/compass.png", Texture.class);
		compassAsset = new BillboardQuadAsset(compassTexture, 1f, 1f, false, false);
		compassInstance = new CompassBillboard(compassAsset);
		/*Model model = manager.get("models/something.g3db");
        ModelInstance instance = new ModelInstance(model);
        instance.transform.setToTranslation(0f, 0f, 0f);
        instances.add(instance);*/
        
        //
        camera = new OrbitCamera(FIELD_OF_VIEW, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.setMaxPitch(MathUtils.HALF_PI);
        camera.setMinRadius(1f);
		camera.lookAt(0f, 0f, 0f);
		camera.update();
		//
		addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (cursorCoord != null && this.getTapCount() == 2) {
					camera.flyTo(cursorCoord.toVector3(), 80f);
				}
			}
		});
		addListener(new InputListener() {
			private Tool prevTool;
			
			@Override
			public boolean mouseMoved(InputEvent event, float x, float y) {
				if (event.isHandled()) return false;
				event.getStage().setScrollFocus(View3D.this);
				View3D.this.mouseMoved(x, y);
				return true;
			}
			
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            	event.getStage().setKeyboardFocus(View3D.this);
            	event.getStage().setScrollFocus(View3D.this);
                return mouseDown(x, y, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
            	mouseDragged(x, y);
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            	mouseUp(x, y, pointer, button);
            	event.getStage().setScrollFocus(View3D.this);
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
            	if (event.isHandled()) return false;
            	return mouseScrolled(amountX, amountY);
            }
            
            @Override
        	public boolean keyDown(InputEvent event, int keycode) {
    			if (keycode == Keys.SPACE) {
        			prevTool = app.getActiveTool();
        			if (prevTool != Tool.PAN) {
        				app.setActiveTool(Tool.PAN);
        			}
        		}
        		return false;
        	}
        	
        	@Override
        	public boolean keyUp(InputEvent event, int keycode) {
        		if (prevTool != null) {
	        		if (keycode == Keys.ALT_LEFT || keycode == Keys.SPACE) {
	        			app.setActiveTool(prevTool);
	        			prevTool = null;
	        		}
        		}
        		if (keycode == Keys.ESCAPE) {
        			if (pickCoordCallback != null && pickCoordCancelable) {
        				cancelPickCoord();
        			}
        			if (pickObjectCallback != null && pickObjectCancelable) {
        				cancelPickObject();
        			}
        		}
        		return false;
        	}
            
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            	hovered = true;
            	//event.getStage().setKeyboardFocus(View3D.this);
            	event.getStage().setScrollFocus(View3D.this);
            	updateCursor();
            }
            
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            	Actor over = event.getStage().hit(event.getStageX(), event.getStageY(), false);
            	if (over != View3D.this) {
	            	hovered = false;
	            	event.getStage().setScrollFocus(null);
	            	Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
            	}
            }
        });
		//
		app.listeners.add(appChangeListener);
		LandEditManager editManager = app.getEditManager();
		editManager.listeners.add(editChangeListener);
	}
	
	private final UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.SMALL_BUMP) {
				if (land3D != null) {
					land3D.setSmallBump(app.getSmallBump());
				}
			} else if (event.getProperty() == MainApp.Property.SKY_TEXTURE) {
				ocean.setTexture(app.getSkyTexture());
			}
		}
	};
	
	private final UChangeListener editChangeListener = new UChangeListener() {
		private boolean prevAutoSortEnabled;
		
		@Override
		public void onChange(UEvent event) {
			if (lhx3d != null) {
				//We need to disable automatic sorting while applying an undo/redo action, otherwise the statements could be moved while applying changes
				if (event.getProperty() == LandEditManager.Property.BEFORE_UNDO || event.getProperty() == LandEditManager.Property.BEFORE_REDO) {
					prevAutoSortEnabled = lhx3d.isAutoSortEnabled();
					lhx3d.setAutoSortEnabled(false);
				} else if (event.getProperty() == LandEditManager.Property.AFTER_UNDO || event.getProperty() == LandEditManager.Property.AFTER_REDO) {
					lhx3d.setAutoSortEnabled(prevAutoSortEnabled);
				}
			}
		}
	};
	
	private void updateCursor() {
		if (hovered) {
			if (pickCoordCallback != null || pickObjectCallback != null) {
				Gdx.graphics.setSystemCursor(cursorCoord != null ? SystemCursor.Crosshair : SystemCursor.Arrow);
			} else {
				Gdx.graphics.setSystemCursor(brush != null && cursorCoord != null ? SystemCursor.None : SystemCursor.Arrow);
			}
		}
	}
	
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	
	public void setBackgroundColor(Color color) {
		this.backgroundColor.set(color);
	}
	
	public boolean isAlphaEnabled() {
		return alphaEnabled;
	}
	
	/**Enable or disable the alpha channel in the frame buffer. Enabling alpha is useful if you want to take a screenshot of an object
	 * with a transparent background (sky and ocean should be disabled too).
	 * @param enabled
	 */
	public void setAlphaEnabled(boolean enabled) {
		if (enabled != this.alphaEnabled) {
			this.alphaEnabled = enabled;
			if (fbo != null) {
				fbo.dispose();
				fbo = null;
			}
		}
	}
	
	public boolean isSkyEnabled() {
		return skyEnabled;
	}
	
	public void setSkyEnabled(boolean enabled) {
		this.skyEnabled = enabled;
	}
	
	public boolean isOceanEnabled() {
		return oceanEnabled;
	}
	
	public void setOceanEnabled(boolean enabled) {
		this.oceanEnabled = enabled;
	}
	
	/**Returns a list of extra objects to be displayed on the 3D view.
	 * Objects can be added to or removed from the returned list.
	 * @return
	 */
	public List<ModelInstance> getExtraObjects() {
		return extraObjects;
	}
	
	/**Returns how fast the mouse wheel wil zoom in and out.
	 * @return
	 */
	public float getZoomSpeed() {
		return zoomSpeedScroll;
	}

	/**Sets how fast the mouse wheel wil zoom in and out.
	 * @param speed
	 */
	public void setZoomSpeed(float speed) {
		this.zoomSpeedScroll = speed;
		this.zoomSpeedDrag = speed / 4;
	}
	
	public Land3D getLand3D() {
		return this.land3D;
	}
	
	public LndFile getLand() {
		return this.land;
	}
	
	public void setLand(LndFile land) {
		if (land != this.land) {
			if (this.land != null) {
				hideLinks();
				grid.dispose();
				grid = null;
				land3D.listeners.remove(land3DChangeListener);
				land3D.dispose();
				land3D = null;
				this.land.listeners.remove(landChangeListener);
			}
			this.land = land;
			if (this.land != null) {
				grid = new LandGrid(0f, -land.getSideLen(), land.getBlocksPerSide(), LH3DLandBlock.BLOCK_SIZE, Color.WHITE);
				land3D = new Land3D(land);
		    	land3D.setSmallBump(app.getSmallBump());
		    	land3D.listeners.add(land3DChangeListener);
		    	this.land.listeners.add(landChangeListener);
		    	landBounds = getBounds();
		        camera.setMaxRadius(land3D.land.getSideLen() * 1.41f);
			}
			if (brush3D != null) brush3D.setLand(land3D);
			for (Selection3D selection3d : selections.values()) {
				selection3d.setLand(land3D);
			}
			tmpMarkerVisible = false;
		}
	}
	
	public void setFootpaths(FotFile footpaths) {
		if (this.footpaths != null) {
			for (Path3D path3D : paths3D) {
				path3D.dispose();
			}
			paths3D.clear();
			obstaclePoints3D.dispose();
			obstaclePoints3D = null;
			linkPoints3D.dispose();
			linkPoints3D = null;
		}
		this.footpaths = footpaths;
		if (footpaths != null) {
			for (Footpath footpath : footpaths.getFootpaths()) {
				List<Coord> points = new ArrayList<>(footpath.getNodes().size());
				for (FootpathNode node : footpath.getNodes()) {
					MapCoords coords = node.getCoords();
					points.add(new Coord(coords.getX(), coords.getAltitude(), coords.getZ()));
				}
				Path3D path3D = new Path3D(land3D, points, Color.GREEN);
				paths3D.add(path3D);
			}
			
			List<Coord> obstaclePoints = new ArrayList<>(footpaths.getFootpathLinkSaves().size());
			List<Coord> linkPoints = new ArrayList<>(footpaths.getFootpathLinkSaves().size());
			for (FootpathLinkSave link : footpaths.getFootpathLinkSaves()) {
				MapCoords coords = link.getCoords();
				Coord linkCoord = new Coord(coords.getX(), coords.getAltitude(), coords.getZ());
				if (link.getLink() == null) {
					obstaclePoints.add(linkCoord);
				} else {
					linkPoints.add(linkCoord);
					List<Footpath> paths = link.getLink().getFootpaths();
					for (Footpath footpath : paths) {
						List<Coord> points2 = new ArrayList<>(footpath.getNodes().size());
						points2.add(linkCoord);
						
						MapCoords mapCoords2 = footpath.getNodes().get(0).getCoords();
						MapCoords mapCoords3 = footpath.getNodes().get(footpath.getNodes().size() - 1).getCoords();
						Coord coords2 = new Coord(mapCoords2.getX(), mapCoords2.getAltitude(), mapCoords2.getZ());
						Coord coords3 = new Coord(mapCoords3.getX(), mapCoords3.getAltitude(), mapCoords3.getZ());
						if (linkCoord.dst2(coords2) < linkCoord.dst2(coords3)) {
							points2.add(coords2);
						} else {
							points2.add(coords3);
						}
						paths3D.add(new Path3D(land3D, points2, Color.BLUE));
					}
				}
			}
			obstaclePoints3D = new Cross3D(land3D, obstaclePoints, Color.RED);
			linkPoints3D = new Points3D(land3D, linkPoints, Color.CYAN);
		}
	}
	
	public void setLHX(LHXFile lhxFile) {
		if (lhxFile != this.lhx) {
			if (this.lhx != null) {
				lhx3d.listeners.remove(lhx3DChangeListener);
				clearSelectedObjects();
				lhx3d.close();
				lhx3d = null;
			}
			this.lhx = lhxFile;
			if (lhx != null) {
				lhx3d = new LHX3D(app.getModelManager(), lhx, land3D);
				lhx3d.listeners.add(lhx3DChangeListener);
			}
		}
	}
	
	public LHX3D getLHX3D() {
		return this.lhx3d;
	}
	
	public boolean isPreventCameraUnderground() {
		return preventCameraUnderground;
	}
	
	public void setPreventCameraUnderground(boolean value) {
		this.preventCameraUnderground = value;
	}
	
	public ShowGridOption getShowGrid() {
		return showGrid;
	}
	
	public void setShowGrid(ShowGridOption showGrid) {
		this.showGrid = showGrid;
	}
	
	public ShowCellAttrOption getShowCellAttr() {
		return showCellAttr;
	}
	
	public void setShowCellAttr(ShowCellAttrOption showCellAttr) {
		this.showCellAttr = showCellAttr;
	}
	
	public void setShowObjects(ShowObjectOption showObjects) {
		this.showObjects = showObjects;
	}
	
	public void setShowVirtualObjects(boolean showVirtualObjects) {
		this.showVirtualObjects = showVirtualObjects;
	}
	
	public boolean isShowCells() {
		return showCells;
	}
	
	public void setShowCells(boolean visible) {
		this.showCells = visible;
	}
	
	public boolean isShowLinks() {
		return showLinks;
	}
	
	public void setShowLinks(boolean visible) {
		if (visible != this.showLinks) {
			this.showLinks = visible;
		}
	}
	
	public boolean isHighlightSelectedCountry() {
		return highlightSelectedCountry;
	}
	
	public void setHighlightSelectedCountry(boolean value) {
		this.highlightSelectedCountry = value;
	}
	
	public boolean isOutlineSelectedCountry() {
		return outlineSelectedCountry;
	}
	
	public void setOutlineSelectedCountry(boolean value) {
		this.outlineSelectedCountry = value;
	}
	
	public boolean isCompassVisible() {
		return compassVisible;
	}
	
	public void setCompassVisible(boolean visible) {
		this.compassVisible = visible;
	}
	
	public boolean isFootpathsVisible() {
		return footpathsVisible;
	}
	
	public void setFootpathsVisible(boolean visible) {
		this.footpathsVisible = visible;
	}
	
	private final UChangeListener land3DChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.CHANGE && event.getProperty() == Land3D.Property.SELECTION) {
				listeners.notify(EventType.CHANGE, Property.SELECTION, null, null, -1, event);
			}
		}
	};
	
	private Map<Object3D, List<Object3D>> selectedObjects = new IdentityHashMap<>();
	
	private void clearSelectedObjects() {
		for (Entry<Object3D, List<Object3D>> e : selectedObjects.entrySet()) {
			Object3D selectedObject = e.getKey();
			List<Object3D> linkedObjects = e.getValue();
			selectedObject.listeners.remove(objectChangeListener);
			for (Object3D obj : linkedObjects) {
				obj.listeners.remove(objectChangeListener);
			}
		}
		selectedObjects.clear();
		hideLinks();
	}
	
	private final UChangeListener lhx3DChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.CHANGE && event.getProperty() == LHX3D.Property.SELECTION) {
				clearSelectedObjects();
				List<Object3D> objects = lhx3d.getSelected();
				//
				for (Object3D selectedObject : objects) {
					Statement stmt = selectedObject.getStatement();
					if (stmt != null) {
						List<Object3D> linkedObjects = selectedObject.getChildren();
						showLinks(selectedObject);
						for (Object3D obj : linkedObjects) {
							obj.listeners.add(objectChangeListener);
						}
						selectedObjects.put(selectedObject, linkedObjects);
						selectedObject.listeners.add(objectChangeListener);
					}
				}
				listeners.notify(EventType.CHANGE, Property.SELECTION, null, null, -1, event);
			}
		}
	};
	
	private final UChangeListener objectChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			Object3D object = (Object3D)event.getSource();
			showLinks(object);
			List<Object3D> linkedObjects = selectedObjects.get(object);
			if (linkedObjects != null) {
				for (Object3D obj : linkedObjects) {
					showLinks(obj);
				}
			}
		}
	};
	
	public void showLinks(Object3D object) {
		if (object == null) return;
		Command cmd = object.getStatement().getCommand();
		if (cmd == Command.CREATE_STREAM || cmd == Command.CREATE_STREAM_POINT) return;
		List<Path3D> links = links3D.get(object);
		if (links == null) {
			links = new ArrayList<>();
			links3D.put(object, links);
		} else {
			for (Path3D link : links) {
				link.dispose();
			}
			links.clear();
		}
		//
		Object3D parent = object.getParent();
		if (parent != null) {
			List<Coord> points = new ArrayList<>(2);
			Coord p = cmd == Command.CREATE_VILLAGER_POS ? parent.getDoorPosition() : parent.getPosition();
			if (p != null) {
				p.y += 0.5f;
				points.add(p);
				p = object.getPosition();
				if (p != null) {
					p.y += 0.5f;
					points.add(p);
					links.add(new Path3D(land3D, points, Color.WHITE));
				}
			}
		}
		//
		List<Object3D> children = object.getChildren();
		for (Object3D child : children) {
			List<Coord> points = new ArrayList<>(2);
			Coord p = cmd == Command.CREATE_ABODE ? object.getDoorPosition() : object.getPosition();
			p.y += 0.5f;
			points.add(p);
			p = child.getPosition();
			if (p != null) {
				p.y += 0.5f;
				points.add(p);
				links.add(new Path3D(land3D, points, Color.LIGHT_GRAY));
			}
		}
	}
	
	public void hideLinks() {
		for (List<Path3D> links : links3D.values()) {
  			for (Path3D link : links) {
  				link.dispose();
  			}
  			links.clear();
  		}
		links3D.clear();
	}
	
	public void hideLinks(Object3D obj) {
		List<Path3D> links = links3D.remove(obj);
		if (links != null) {
  			for (Path3D link : links) {
  				link.dispose();
  			}
  			links.clear();
  		}
	}
	
	public void setExtraBlocks(List<LH3DLandBlock> blocks) {
		if (extraBlocks != null) {
			for (Block3D block : extraBlocks) {
				block.dispose();
			}
			extraBlocks = null;
		}
		if (blocks != null && land3D != null) {
			extraBlocks = new ArrayList<Block3D>(blocks.size());
			for (LH3DLandBlock block : blocks) {
				Block3D block3D = new Block3D(land3D, block);
				extraBlocks.add(block3D);
			}
		}
	}
	
	private final UChangeListener landChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LndFile.Property.BLOCKS_PER_SIDE) {
				if (grid != null) grid.dispose();
				grid = new LandGrid(0f, -land.getSideLen(), land.getBlocksPerSide(), LH3DLandBlock.BLOCK_SIZE, Color.WHITE);
			}
		}
	};
	
	private Bounds3D getBounds() {
		Bounds3D bounds = land3D.getBounds();
		if (!bounds.isInitialized()) {
			bounds = new Bounds3D();
			bounds.update(0f, 0f, 0f);
			bounds.update(land.getSideLen(), land.getMaxHeight(), -land.getSideLen());
		}
		return bounds;
	}
	
	public OrbitCamera getCamera() {
		return camera;
	}
	
	public void flyTo(float x, float z, float radius) {
		float h = land != null ? land.getHeight(x, z) : 0f;
		camera.flyTo(new Vector3(x, h, -z), radius);
	}
	
	public void resetView() {
		if (land3D != null) {
			Vector3 center = landBounds.getCenter();
	        camera.setPivot(center.x, center.z);
	        camera.setYaw(0f);
	        camera.setPitch(-0.5f);
	        float size = Math.max(landBounds.getWidth(), landBounds.getDepth());
	        camera.setRadius(size * 1.1f);
	        camera.setZoomFromRadius();
		}
	}
	
	public void viewFromTop() {
		Vector3 center = landBounds.getCenter();
        camera.setPivot(center.x, center.z);
        camera.setYaw(0f);
        camera.setPitch(-MathUtils.PI / 2);
        float size = Math.max(landBounds.getWidth(), landBounds.getDepth());
        camera.setRadius(size * 1.2f);
        camera.setZoomFromRadius();
	}
	
	public void setCountryToHighlight(LNDCountry country) {
		setCountryToHighlight(country != null ? country.getIndex() : -1);
	}
	
	public void setCountryToHighlight(int index) {
		this.land3D.getMaterialAttribute().countryToHighlight = index;
	}
	
	public void showTmpMarker(Coord coord) {
		Vector3 pos = coord.toVector3();
		float elevation = land3D.getHeight(pos);
		if (pos.y == 0) {
			pos.y += elevation;
		} else {
			pos.y = Math.max(elevation, pos.y);
		}
		tmpMarker.setPosition(pos);
		tmpMarkerVisible = true;
	}
	
	public void hideTmpMarker() {
		tmpMarkerVisible = false;
	}
	
	public void setBrush(Brush brush) {
		if (this.brush != null) {
			this.brush3D.dispose();
			this.brush3D = null;
		}
		this.brush = brush;
		if (brush != null) {
			this.brush3D = new Brush3D(land3D, brush, camera);
		}
		updateCursor();
	}
	
	public Brush getBrush() {
		return this.brush;
	}
	
	public void addSelection(Selection selection) {
		if (!selections.containsKey(selection)) {
			selections.put(selection, new Selection3D(land3D, selection));
		}
	}
	
	public boolean removeSelection(Selection selection) {
		Selection3D selection3d = selections.remove(selection);
		if (selection3d == null) return false;
		selection3d.dispose();
		return true;
	}
	
	public void setMeasureVisible(boolean v) {
		this.measureVisible = v;
	}
	
	public boolean isMeasureVisible() {
		return this.measureVisible;
	}
	
	public void setMeasureStart(Coord p) {
		this.measure.setStart(p.toVector3());
	}
	
	public Coord getCursorCoord() {
		return cursorCoord;
	}
	
	public void pickCoord(CoordCallback callback) {
		pickCoord(callback, true, null);
	}
	
	public void pickCoord(CoordCallback callback, boolean cancelable) {
		pickCoord(callback, cancelable, null);
	}
	
	public void pickCoord(CoordCallback callback, Runnable onCancel) {
		pickCoord(callback, true, onCancel);
	}
	
	public void pickCoord(CoordCallback callback, boolean cancelable, Runnable onCancel) {
		this.pickCoordCallback = callback;
		this.pickCoordCancelable = cancelable;
		this.pickCoordCancel = onCancel;
		updateCursor();
	}
	
	public void cancelPickCoord() {
		this.pickCoordCallback = null;
		updateCursor();
		if (pickCoordCancel != null) {
			Gdx.app.postRunnable(pickCoordCancel);
			pickCoordCancel = null;
		}
	}
	
	public void pickObject(PickObjectCallback callback, boolean cancelable, Runnable onCancel) {
		this.pickObjectCallback = callback;
		this.pickObjectCancelable = cancelable;
		this.pickObjectCancel = onCancel;
		updateCursor();
	}
	
	public void cancelPickObject() {
		this.pickObjectCallback = null;
		updateCursor();
		if (pickObjectCancel != null) {
			Gdx.app.postRunnable(pickObjectCancel);
			pickObjectCancel = null;
		}
	}
	
	public boolean isPicking() {
		return pickCoordCallback != null || pickObjectCallback != null;
	}
	
	public void enableCellSelection(SelectionCallback callback, SelectionMode mode) {
		this.cellSelectionCallback = callback;
		this.selectionMode = mode;
	}
	
	public void disableCellSelection() {
		this.cellSelectionCallback = null;
		tmpSelection.clear();
	}
	
	public void enableObjectSelection(ObjectSelectionCallback callback, SelectionMode mode) {
		this.objectSelectionCallback = callback;
		this.selectionMode = mode;
	}
	
	public void disableObjectSelection() {
		this.objectSelectionCallback = null;
		tmpSelection.clear();
	}
	
	public void setSelectionMode(SelectionMode mode) {
		this.selectionMode = mode;
	}
	
	public void clearSelectCells() {
		land3D.clearSelection();
	}
	
	public void selectCells(Selection selection, SelectionMode mode) {
		land3D.select(selection, mode);
	}
	
	public boolean hasSelectedCells() {
		if (land3D == null) return false;
		return land3D.hasSelectedCells();
	}
	
	public boolean isWithinSelectedCells(Coord coord) {
		if (land3D == null) return false;
		return land3D.isWithinSelection(coord);
	}
	
	public boolean hasSelectedObjects() {
		if (lhx3d == null) return false;
		return lhx3d.hasSelectedObjects();
	}
	
	public List<Block3D> getSelectedBlocks() {
		if (land3D == null) return Collections.emptyList();
		return land3D.getSelectedBlocks();
	}
	
	public LndFile getSelectedCellsAsNewLand(boolean removeResources, boolean allowReorder) {
		if (land3D == null) return null;
		LndFile newLand = land.clone();
		try (BulkUpdate<LH3DLandBlock> cellsUpdate = newLand.getLandBlocksForUpdate()) {
			for (LH3DLandBlock block : cellsUpdate.data) {
				Block3D block3D = land3D.getBlock(block.getBlockX(), block.getBlockZ());
				if (block3D.hasSelectedCells()) {
					try (BulkUpdate<byte[]> selUpdate = block3D.updateSelection()) {
						for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
							for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
								if (selUpdate.data[cx][cz] == 0) {
									LH3DLandCell cell = block.getCell(cx, cz);
									cell.set(LH3DLandCell.EMPTY);
									selUpdate.data[cx][cz] = 0;
								}
							}
						}
					}
					cellsUpdate.setChanged(block.getIndex(), LH3DLandBlock.Property.CELLS);
				} else {
					int i = block.getIndex();
					newLand.removeBlock(block);
					cellsUpdate.setRemoved(block, i);
				}
			}
		}
		if (removeResources) {
			for (int i = newLand.getCountries().size() - 1; i >= 0; i--) {
				if (!newLand.isCountryInUse(i)) {
					newLand.getCountries().remove(i);
				} else if (!allowReorder) {
					break;
				}
			}
			for (int i = newLand.getMaterials().size() - 1; i >= 0; i--) {
				if (!newLand.isMaterialInUse(i)) {
					newLand.getMaterials().remove(i);
				} else if (!allowReorder) {
					break;
				}
			}
		}
		return newLand;
	}
	
	public void setMouseCoordCallback(CoordCallback callback) {
		this.mouseCoordCallback = callback;
	}
	
	public void getRenderedImage(FrameCallback callback) {
		this.frameCallback = callback;
	}
	
	public boolean isCapturingFrame() {
		return this.frameCallback != null;
	}
	
	private void ensureFbo(int w, int h) {
        w = Math.max(1, w);
        h = Math.max(1, h);

        if (fbo != null && fbo.getWidth() == w && fbo.getHeight() == h) return;

        if (fbo != null) fbo.dispose();

        fbo = new FrameBuffer(alphaEnabled ? Pixmap.Format.RGBA8888 : Pixmap.Format.RGB888, w, h, true);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);
    }
	
	@Override
	public float getPrefWidth() {
	    return 100f;
	}

	@Override
	public float getPrefHeight() {
	    return 100f;
	}
	
	private Vector3 tmpVec = new Vector3();
	
	private void handleKeyboardNavigation(float delta) {
		if (!Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
	        if (Gdx.input.isKeyPressed(Keys.Q)) {
	        	camera.rotate(0.8f * delta, 0f);
	        } else if (Gdx.input.isKeyPressed(Keys.E)) {
	        	camera.rotate(-0.8f * delta, 0f);
	        }
	        if (Gdx.input.isKeyPressed(Keys.R)) {
	        	camera.rotate(0f, 0.75f * delta);
	        } else if (Gdx.input.isKeyPressed(Keys.F)) {
	        	camera.rotate(0f, -0.75f * delta);
	        }
	        if (Gdx.input.isKeyPressed(Keys.NUMPAD_SUBTRACT)) {
	        	camera.zoom(delta, 8f * zoomSpeedDrag);
	        } else if (Gdx.input.isKeyPressed(Keys.NUMPAD_ADD)) {
	        	camera.zoom(-delta, 8f * zoomSpeedDrag);
	        }
	        if (Gdx.input.isKeyPressed(Keys.A)) {
	        	float speed = camera.getRadius() * 0.5f;
	        	float dx = (float)Math.cos(camera.getYaw()) * speed * delta;
	        	float dz = (float)Math.sin(camera.getYaw()) * speed * delta;
	        	camera.getPivot(tmpVec);
	        	float x = MathUtils.clamp(tmpVec.x - dx, 0f, land3D.land.getSideLen());
				float z = MathUtils.clamp(tmpVec.z - dz, -land3D.land.getSideLen(), 0f);
	        	camera.setPivot(x, z);
	        } else if (Gdx.input.isKeyPressed(Keys.D)) {
	        	float speed = camera.getRadius() * 0.5f;
	        	float dx = (float)Math.cos(camera.getYaw()) * speed * delta;
	        	float dz = (float)Math.sin(camera.getYaw()) * speed * delta;
	        	camera.getPivot(tmpVec);
	        	float x = MathUtils.clamp(tmpVec.x + dx, 0f, land3D.land.getSideLen());
				float z = MathUtils.clamp(tmpVec.z + dz, -land3D.land.getSideLen(), 0f);
				camera.setPivot(x, z);
	        }
	        if (Gdx.input.isKeyPressed(Keys.W)) {
	        	float speed = camera.getRadius() * 0.5f;
	        	float dx = -(float)Math.sin(camera.getYaw()) * speed * delta;
	        	float dz = (float)Math.cos(camera.getYaw()) * speed * delta;
	        	camera.getPivot(tmpVec);
	        	float x = MathUtils.clamp(tmpVec.x - dx, 0f, land3D.land.getSideLen());
				float z = MathUtils.clamp(tmpVec.z - dz, -land3D.land.getSideLen(), 0f);
				camera.setPivot(x, z);
	        } else if (Gdx.input.isKeyPressed(Keys.S)) {
	        	float speed = camera.getRadius() * 0.5f;
	        	float dx = -(float)Math.sin(camera.getYaw()) * speed * delta;
	        	float dz = (float)Math.cos(camera.getYaw()) * speed * delta;
	        	camera.getPivot(tmpVec);
	        	float x = MathUtils.clamp(tmpVec.x + dx, 0f, land3D.land.getSideLen());
				float z = MathUtils.clamp(tmpVec.z + dz, -land3D.land.getSideLen(), 0f);
				camera.setPivot(x, z);
	        }
        }
	}
	
	@Override
    public void act(float delta) {
        super.act(delta);
        landShader.act(delta);
    	selectionShader.act(delta);
        if (this.hasKeyboardFocus()) {
        	if (brush != null && brush.isStarted()) {
	        	brush.apply(delta, cursorCoord, camera.getYaw(), Gdx.input.isKeyPressed(Keys.SHIFT_LEFT));
	        }
        	handleKeyboardNavigation(delta);
	        if (measureVisible && cursorCoord != null) {
	        	measure.setEnd(cursorCoord.toVector3(tmpVec));
	        }
        }
        if (land3D != null) {
        	land3D.act(delta);
        }
        camera.act(delta);
        if (lhx3d != null) {
			for (Object3D obj : lhx3d.getObjects()) {
				obj.act(delta);
			}
        }
        render();
	}
	
	private void render() {
		this.validate();
		final int w = Math.round(getWidth());
		final int h = Math.round(getHeight());
        ensureFbo(w, h);
        //Update objects
		camera.viewportWidth = w;
        camera.viewportHeight = h;
        camera.update();
        ocean.update(camera);
        compassInstance.update(camera);
        tmpMarker.update(camera);
        //
        fbo.begin();
		Gdx.gl.glViewport(0, 0, w, h);
		Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		//Pass 1: sky
		Gdx.gl.glColorMask(true, true, true, false);
		if (skyEnabled) {
			modelBatch.begin(camera);
			modelBatch.render(sky);
			modelBatch.end();
		}
		//Pass 2: ocean and grid
		if (oceanEnabled) {
			modelBatch.begin(camera);
			modelBatch.render(ocean, environment);
			if (showGrid != ShowGridOption.NONE && grid != null) {
				modelBatch.render(grid);
			}
			modelBatch.end();
		}
		Gdx.gl.glColorMask(true, true, true, true);
		//Pass 3: land and objects
		modelBatch.begin(camera);
		if (land3D != null) {
			LandAttribute attr = land3D.getMaterialAttribute();
			attr.showGrid = this.showGrid;
			attr.showCellsAttr = this.showCellAttr;
			attr.showCells = this.showCells;
			attr.highlightSelectedCountry = this.highlightSelectedCountry;
			attr.outlineSelectedCountry = this.outlineSelectedCountry;
			modelBatch.render(land3D, environment);
		}
		if (extraBlocks != null) {
			modelBatch.render(extraBlocks, environment);
		}
		if (lhx3d != null) {
			for (Object3D obj : lhx3d.getObjects()) {
				if (isVisible(obj)) {
					if (obj.isSprite()) {
						obj.lookAt(camera.getPosition(), camera.up);
						modelBatch.render(obj, (Environment)null);
					} else {
						modelBatch.render(obj, environment);
					}
				}
			}
		}
		modelBatch.render(extraObjects, environment);
		modelBatch.end();
        //Pass 4: UI
  		modelBatch.begin(camera);
  		if (compassVisible) {
  			modelBatch.render(compassInstance.getInstance());
  		}
  		if (tmpMarkerVisible) {
			modelBatch.render(tmpMarker.getInstance());
		}
  		if (brush != null && pickCoordCallback == null && pickObjectCallback == null && cursorCoord != null) {
			modelBatch.render(brush3D);
		}
  		if (footpathsVisible) {
	  		for (Path3D path3d : paths3D) {
	  			if (!path3d.isNull()) {
	  				modelBatch.render(path3d);
	  			}
	  		}
	  		if (obstaclePoints3D != null && !obstaclePoints3D.isNull()) {
	  			modelBatch.render(obstaclePoints3D);
	  		}
	  		if (linkPoints3D != null && !linkPoints3D.isNull()) {
	  			modelBatch.render(linkPoints3D);
	  		}
  		}
  		for (Selection3D selection3d : selections.values()) {
  			if (!selection3d.isNull()) {
  				modelBatch.render(selection3d);
  			}
  		}
  		if (showLinks) {
	  		for (List<Path3D> links : links3D.values()) {
	  			for (Path3D link : links) {
	  				modelBatch.render(link);
	  			}
	  		}
  		}
  		if (measureVisible) {
        	modelBatch.render(measure);
        }
  		modelBatch.end();
        //Take a screenshot if required
  		if (frameCallback != null) {
        	final FrameCallback callback = this.frameCallback;
        	this.frameCallback = null;
        	final Pixmap renderedFrame = Pixmap.createFromFrameBuffer(0, 0, fbo.getWidth(), fbo.getHeight());
    		try {
    			callback.frameReady(renderedFrame);
    		} finally {
    			renderedFrame.dispose();
    		}
        }
  		//
        fbo.end();
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(fboRegion, getX(), getY(), getWidth(), getHeight());
	}
	
	private boolean mouseScrolled(float amountX, float amountY) {
		if (!panning) {
			camera.zoom(amountY, zoomSpeedScroll);
			return true;
		}
		return false;
	}
	
	private boolean mouseDown(float x, float y, int button) {
		if (downButton == -1) {
			downButton = button;
		}
		Vector3 landPos = pickLandPos(x, y);
		if (landPos != null) {
			if (cursorCoord == null) cursorCoord = new Coord();
			cursorCoord.set(landPos);
			if (mouseCoordCallback != null) {
				mouseCoordCallback.onCoord(cursorCoord, button);
			}
			if ((cellSelectionCallback != null || objectSelectionCallback != null) && button == Buttons.LEFT) {
				tmpSelection.clear();
				tmpSelection.add(cursorCoord.clone());
			}
		} else {
			cursorCoord = null;
		}
		//
		mouseStartX = x;
		mouseStartY = y;
		mousePrevX = mouseStartX;
		mousePrevY = mouseStartY;
		cursorStartX = Gdx.input.getX();
		cursorStartY = Gdx.input.getY();
		//
		parm.set(camera);
		//
		Ray ray = getPickRay(x, y);
		startPoint = pickLandPos(ray);
		startPivot = camera.getPivot();
		startPitch = ray.direction.y;
		//
		if (brush != null && button == Buttons.LEFT &&
				!Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Keys.ALT_LEFT)
				&& pickCoordCallback == null && pickObjectCallback == null) {
        	app.getEditManager().begin(brush.getDescription());
			brush.begin(cursorCoord);
        }
		if (button == Buttons.MIDDLE || button == Buttons.LEFT && Gdx.input.isKeyPressed(Keys.SPACE)) {
			panning = true;
		}
		return true;
	}
	
	private void mouseMoved(float x, float y) {
		Vector3 landPos = pickLandPos(x, y);
		if (landPos != null) {
			if (cursorCoord == null) cursorCoord = new Coord();
			cursorCoord.set(landPos);
			if (brush != null) {
				brush3D.setPosition(landPos.x, landPos.z);
			}
			if (mouseCoordCallback != null) {
				mouseCoordCallback.onCoord(cursorCoord, -1);
			}
		} else {
			cursorCoord = null;
		}
		updateCursor();
	}
	
	private boolean mouseDragged(float x, float y) {
		dragging = true;
		Vector3 landPos = pickLandPos(x, y);
		if (landPos != null) {
			if (cursorCoord == null) cursorCoord = new Coord();
			cursorCoord.set(landPos);
			if (mouseCoordCallback != null) {
				mouseCoordCallback.onCoord(cursorCoord, downButton);
			}
		} else {
			cursorCoord = null;
		}
		//
		final float deltaX = x - mouseStartX;
		final float deltaY = y - mouseStartY;
		final float dx = x - mousePrevX;
		final float dy = y - mousePrevY;
		//
		boolean shouldZoom = Gdx.input.isButtonPressed(Buttons.LEFT) && Gdx.input.isButtonPressed(Buttons.RIGHT) && app.getActiveTool() == Tool.ORBIT;
		boolean shouldModifyBrush = brush != null && Gdx.input.isButtonPressed(Buttons.RIGHT) && (Gdx.input.isKeyPressed(Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Keys.ALT_RIGHT));
		boolean shouldSelect = ((cellSelectionCallback != null || objectSelectionCallback != null) && Gdx.input.isButtonPressed(Buttons.LEFT) && !Gdx.input.isButtonPressed(Buttons.RIGHT));
		boolean shouldOrbit = false;
		boolean shouldPan = false;
		if (!shouldZoom && !shouldModifyBrush) {
			shouldOrbit = Gdx.input.isButtonPressed(Buttons.RIGHT) || (downButton == Buttons.LEFT && app.getActiveTool() == Tool.ORBIT);
			shouldPan = Gdx.input.isButtonPressed(Buttons.MIDDLE) || (downButton == Buttons.LEFT && app.getActiveTool() == Tool.PAN);
		}
		//
		if (shouldZoom) {
			camera.zoom(dy, zoomSpeedDrag);
		} else if (shouldOrbit) {
			camera.rotate(dx * rotationSpeed, dy * rotationSpeed);
			if (preventCameraUnderground && land3D != null) {
				//Avoid camera from going underground
				float minElevation = land3D.getHeight(camera.position);
				if (camera.position.y < minElevation) {
					float hDist = Vector2.dst(camera.getPivot().x, camera.getPivot().z, camera.position.x, camera.position.z);
					float newPitch = -(float)Math.atan2(minElevation - camera.getPivot().y, hDist);
					if (newPitch < camera.getPitch()) {
						camera.setPitch(newPitch);
						camera.update();
					}
				}
				//Check for close occlusions between camera and pivot point
				float hDist = Vector2.dst(camera.getPivot().x, camera.getPivot().z, camera.position.x, camera.position.z);
				float maxDist = hDist * CAMERA_MAX_OCCLUSION_DISTANCE_PERCENT;
				Vector3 tmpPos = new Vector3();
				Vector3 maxElevationPos = new Vector3();
				Vector3 hDir = new Vector3(camera.direction.x, 0f, camera.direction.z).nor();
				for (float dist = 0; dist <= maxDist; dist += 10f) {
					tmpPos.set(hDir).scl(dist).add(camera.position);
					float h = land3D.getHeight(tmpPos);
					if (h > maxElevationPos.y) {
						maxElevationPos.set(tmpPos.x, h, tmpPos.z);
					}
				}
				hDist = Vector2.dst(camera.getPivot().x, camera.getPivot().z, maxElevationPos.x, maxElevationPos.z);
				float newPitch = -(float)Math.atan2(maxElevationPos.y - camera.getPivot().y, hDist);
				if (newPitch < camera.getPitch()) {
					camera.setPitch(newPitch);
					camera.update();
				}
			}
		} else if (shouldPan) {
			if (startPitch > VPAN_MAX_ANGLE) {
				//Y pan
				float sinYaw = sin(camera.getYaw());
				float cosYaw = cos(camera.getYaw());
				float r = camera.getRadius();
				float newx = startPivot.x - cosYaw * deltaX * VPAN_SPEED * r;
				float newz = startPivot.z - sinYaw * deltaX * VPAN_SPEED * r;
				newx = MathUtils.clamp(newx, landBounds.getLow().x, landBounds.getHigh().x);
				newz = MathUtils.clamp(newz, landBounds.getLow().z, landBounds.getHigh().z);
				float newy = Math.max(0, startPivot.y - deltaY * VPAN_SPEED * r);
				camera.setPivot(newx, newy, newz);
			} else {
				//XZ Pan
				if (startPoint != null) {
					Vector3 w = getWorldFromScreen(parm, x, y, startPoint.y);
					if (w != null) {
						float newx = startPivot.x - (w.x - startPoint.x);
						float newz = startPivot.z - (w.z - startPoint.z);
						newx = MathUtils.clamp(newx, 0f, land3D.land.getSideLen());
						newz = MathUtils.clamp(newz, -land3D.land.getSideLen(), 0f);
						camera.setPivot(newx, newz);
					}
				}
			}
		} else if (shouldModifyBrush) {
			brush.setSize(MathUtils.clamp(brush.getSize() - Gdx.input.getDeltaY() * BRUSH_SIZE_SPEED, 10f, 500f));
			brush.setAngleOffset(brush.getAngleOffset() - Gdx.input.getDeltaX() * rotationSpeed);
			Gdx.input.setCursorPosition(cursorStartX, cursorStartY);
		} else if (shouldSelect) {
			if (cursorCoord != null && tmpSelection.isAcceptable(cursorCoord)) {
				tmpSelection.add(cursorCoord.clone());
			}
		}
		//
		if (brush != null && landPos != null) {
			brush3D.setPosition(landPos.x, landPos.z);
		}
		mousePrevX = x;
		mousePrevY = y;
		return true;
	}
	
	private boolean mouseUp(float x, float y, int pointer, int button) {
		panning = false;
		if (brush != null && brush.isStarted()) {
			brush.end();
			app.getEditManager().end();
		}
		Vector3 landPos = pickLandPos(x, y);
		if (landPos != null) {
			if (cursorCoord == null) cursorCoord = new Coord();
			cursorCoord.set(landPos);
			if ((cellSelectionCallback != null || objectSelectionCallback != null) && button == Buttons.LEFT && tmpSelection.isAcceptable(cursorCoord)) {
				tmpSelection.add(cursorCoord.clone());
			}
		} else {
			cursorCoord = null;
		}
		
		if (cellSelectionCallback != null && button == Buttons.LEFT) {
			tmpSelection.closeLoop();
			selectCells(tmpSelection, selectionMode);
			cellSelectionCallback.onSelect(tmpSelection, land3D);
			tmpSelection.clear();
		}
		
		if (objectSelectionCallback != null && pickObjectCallback == null && button == Buttons.LEFT && lhx3d != null) {
			if (dragging) {
				tmpSelection.closeLoop();
				lhx3d.select(tmpSelection, selectionMode);
				objectSelectionCallback.onSelect(lhx3d);
				tmpSelection.clear();
			} else {
				Object3D obj = pickObject(x, y);
				if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
					obj.setSelected(!obj.isSelected());
				} else {
					lhx3d.select(obj, selectionMode);
				}
				objectSelectionCallback.onSelect(lhx3d);
			}
		}
		
		if (land3D != null) {
			if (button == Buttons.LEFT && pickCoordCallback != null && cursorCoord != null) {
				final Coord coord = new Coord(cursorCoord);
				final CoordCallback callback = pickCoordCallback;
				pickCoordCallback = null;
				pickCoordCancel = null;
				Gdx.app.postRunnable(() -> callback.onCoord(coord, button));
				return true;
			}
			if (button == Buttons.LEFT && pickObjectCallback != null) {
				Object3D object = pickObject(x, y);
				if (object != null) {
					final PickObjectCallback callback = pickObjectCallback;
					pickObjectCallback = null;
					pickObjectCancel = null;
					Gdx.app.postRunnable(() -> callback.onObject(object, button));
				}
				return true;
			}
			if (dragging) {
				Vector3 pos = pickLandPos(new Ray(camera.position, camera.direction));
				if (pos != null) {
					final float newRadius = camera.getPosition().dst(pos);
					camera.setPivot(pos);
					camera.setRadius(newRadius);
				}
			}
		}
		if (button == downButton) {
			dragging = false;
			downButton = -1;
		}
		return true;
	}
	
	public Iterable<Object3D> getAllObjects() {
		return lhx3d.getObjects();
	}
	
	public void setSelectedObjects(List<Object3D> objects) {
		if (lhx3d == null) return;
		lhx3d.setSelectedObjects(objects);
	}
	
	public void setSelectedStatements(List<Statement> statements) {
		if (lhx3d == null) return;
		lhx3d.setSelectedStatements(statements);
	}
	
	public void setSelectedObject(@Null Statement stmt) {
		if (lhx3d == null) return;
		lhx3d.select(stmt, SelectionMode.NEW);
	}
	
	public void setSelectedObject(@Null Object3D obj) {
		if (lhx3d == null) return;
		lhx3d.select(obj, SelectionMode.NEW);
	}
	
	public List<Object3D> getSelectedObjects() {
		if (lhx3d == null) return Collections.emptyList();
		return lhx3d.getSelected();
	}
	
	public List<Object3D> getObjects(Selection sel) {
		List<Object3D> res = new LinkedList<>();
		Coord coord = new Coord();
		for (Object3D obj : lhx3d.getObjects()) {
			coord.set(obj.getPosition());
			if (sel.isInside(coord)) {
				res.add(obj);
			}
		}
		return new ArrayList<>(res);
	}
	
	private boolean isVisible(Object3D obj) {
		if (obj.isVirtual()) {
			return showVirtualObjects;
		} else {
			switch (showObjects) {
				case ALL:
					return true;
				case BUILT:
					return !obj.getStatement().getCommand().planned;
				case OFF:
					return false;
				case PLANNED:
					return obj.getStatement().getCommand().planned;
			}
		}
		return true;
	}
	
	public Object3D pickObject(float x, float y) {
		if (lhx3d == null) return null;
		Ray ray = this.getPickRay(x, y);
		Vector3 intersection = new Vector3();
		float minDist = Float.MAX_VALUE;
		Object3D res = null;
		for (Object3D obj : lhx3d.getObjects()) {
			if (isVisible(obj) && obj.intersect(ray, intersection)) {
				float dist = intersection.dst2(ray.origin);
				if (dist < minDist) {
					minDist = dist;
					res = obj;
				}
			}
		}
		return res;
	}
	
	public Vector3 pickLandPos(Ray ray) {
		if (land3D == null) return null;
		return land3D.pickPos(ray);
	}
	
	public Vector3 pickLandPos(float touchX, float touchY) {
		if (land3D == null) return null;
		Ray ray = getPickRay(touchX, touchY);
		return land3D.pickPos(ray);
	}
	
	public Vector3 getWorldFromScreen(float mouseX, float mouseY, float y) {
		return getWorldFromScreen(camera, mouseX,  mouseY, y);
	}
	
	public Ray getPickRay(float touchX, float touchY) {
		return getPickRay(camera, touchX, touchY);
	}
	
	@Override
	public void dispose() {
		hideLinks();
		if (extraBlocks != null) {
			for (Block3D block : extraBlocks) {
				block.dispose();
			}
			extraBlocks = null;
		}
		setFootpaths(null);
		setLand(null);
		setLHX(null);
		ocean.dispose();
		sky.dispose();
		if (grid != null) grid.dispose();
		measure.dispose();
		if (brush3D != null) brush3D.dispose();
		for (Selection3D selection3d : selections.values()) {
			selection3d.dispose();
		}
		selections.clear();
		if (land != null) {
			land.listeners.remove(landChangeListener);
		}
		//
		fbo.dispose();
		modelBatch.dispose();
		shaderProvider.dispose();
		compassAsset.dispose();
		markerAsset.dispose();
		LandEditManager editManager = app.getEditManager();
		editManager.listeners.remove(editChangeListener);
		app.listeners.remove(appChangeListener);
	}
	
	
	public static Vector3 getWorldFromScreen(Camera camera, float mouseX, float mouseY, float y) {
		Ray ray = getPickRay(camera, mouseX, mouseY);
		float distance = (ray.origin.y - y) / -ray.direction.y;
		if (distance <= 0) return null;
        //World pos is eye + direction*distance
        return ray.direction.scl(distance).add(ray.origin);
	}
	
	public static Vector3 getScreenDirection(Camera camera, float mouseX, float mouseY) {
		if (camera instanceof OrthographicCamera) {
			return new Vector3(camera.direction).nor();
		} else {
			//Get the coords of the point on the near plane
			Vector3 nearPlanePoint = unproject(camera, new Vector3(mouseX, mouseY, 0));
			//Create a direction vector from the eye to the point on the near plane 
			return nearPlanePoint.sub(camera.position).nor();
		}
	}
	
	public static Vector3 unproject(Camera camera, Vector3 touchCoords) {
		touchCoords.x = touchCoords.x / camera.viewportWidth * 2f - 1f;
		touchCoords.y = touchCoords.y / camera.viewportHeight * 2f - 1f;
		touchCoords.z = touchCoords.z * 2f - 1;
		touchCoords.prj(camera.invProjectionView);
		return touchCoords;
	}
	
	public static Ray getPickRay(Camera camera, float touchX, float touchY) {
		Ray ray = new Ray(new Vector3(), new Vector3());
		unproject(camera, ray.origin.set(touchX, touchY, 0f));
		unproject(camera, ray.direction.set(touchX, touchY, 1f));
		ray.direction.sub(ray.origin).nor();
		return ray;
	}
	
	
	public interface CoordCallback {
		public void onCoord(Coord coord, int button);
	}
	
	public interface SelectionCallback {
		public void onSelect(Selection tmpSelection, Land3D land3D);
	}
	
	public interface ObjectSelectionCallback {
		public void onSelect(LHX3D proc);
	}
	
	public interface PickObjectCallback {
		public void onObject(Object3D object, int button);
	}
	
	public interface FrameCallback {
		public void frameReady(Pixmap frame);
	}
}
