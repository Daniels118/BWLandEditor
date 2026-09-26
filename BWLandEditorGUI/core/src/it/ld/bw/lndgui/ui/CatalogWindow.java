package it.ld.bw.lndgui.ui;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.gfx.Coord;
import it.ld.bw.lndgui.gfx.L3DModelManager.ModelInfo;
import it.ld.bw.lndgui.gfx.L3DPreviewManager;
import it.ld.bw.lndgui.gfx.Object3D;
import it.ld.libgdx.ui.components.GridView;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.GridView.GridModel;
import it.ld.libgdx.ui.components.GridView.State;
import it.ld.libgdx.ui.components.MessageBox.MessageType;
import it.ld.libgdx.ui.components.MessageBox;
import it.ld.libgdx.ui.events.RemoveEvent;

public class CatalogWindow extends SmartWindow {
	private final MainApp app;
	
	private List<ModelInfo> catalog;
	
	private final GridView<ModelInfo> grid;
	
	private final DragAndDrop dnd = new DragAndDrop();
	private Object3D obj3d;
	private ModelInstance tmpInstance;
	private float radius;
	
	public static CatalogWindow showSingleInstance(MainApp app, Stage stage, Skin skin) {
		CatalogWindow window = (CatalogWindow) SmartWindow.getSingleInstance("catalogWindow");
		if (window == null) {
			window = new CatalogWindow(app, skin, true);
			window.setDockingManager(app.getDockingManager());
			window.setSingleInstance("catalogWindow");
			window.show(stage, 54, 28, false);
			window.setSize(460, stage.getHeight() - 90);
		}
		window.toFront();
		return window;
	}
	
	public static CatalogWindow getInstance() {
		return (CatalogWindow) SmartWindow.getSingleInstance("catalogWindow");
	}
	
	public static void hideInstance() {
		CatalogWindow window = (CatalogWindow) SmartWindow.getSingleInstance("catalogWindow");
		if (window != null) {
			window.remove();
		}
	}
	
	public CatalogWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("catalogWindow.title"), skin, Attribute.RESIZABLE, Attribute.DOCKABLE);
		this.setAutodispose(autodispose);
		this.app = app;
		
		final TextureRegion waitIcon = skin.getRegion("icon-refresh");
		
		TextField searchField = new TextField("", skin);
		searchField.addListener(new InputListener() {
			@Override
			public boolean keyTyped(InputEvent event, char character) {
				filter(searchField.getText());
				return false;
			}
		});
		add(searchField).growX().pad(5);
		row();
		
		final L3DPreviewManager previewManager = app.getPreviewManager();
		grid = new GridView<ModelInfo>(skin)
		.setIconSize(128)
		.setMinCols(1)
		.setModel(new GridModel<ModelInfo>() {
			@Override
			public void render(int index, ModelInfo item, Container<Actor> container, Image image, Label label, State state) {
				TextureRegionDrawable preview = previewManager.getPreview(item, (model, texture) -> {
					Image img = grid.getImage(item);	//Don't use original Image since it could have been replaced
					if (img != null) {
						img.invalidate();
					}
				});
				if (preview.getRegion() == null) {
					preview.setRegion(waitIcon);
				}
				image.setDrawable(preview);
				label.setText(item.toString());
			}
		});
		grid.addListener(new DragListener() {
			@Override
			public void dragStart(InputEvent event, float x, float y, int pointer) {
				
			}
		});
		final View3D view3d = app.getView3D();
		dnd.addSource(new DragAndDrop.Source(grid) {
			@Override
			public Payload dragStart(InputEvent event, float x, float y, int pointer) {
				int index = grid.getItemIndexAt(x, y);
				if (index < 0) return null;
				ModelInfo item = grid.getItem(index);
				
				TextureRegionDrawable preview = previewManager.getPreview(item, null);
				if (preview.getRegion() == null) {
					preview.setRegion(waitIcon);
				}
				Image validImage = new Image(preview);
				validImage.setColor(1f, 1f, 1f, 0.5f);
				Image invalidImage = new Image(preview);
				invalidImage.setColor(1f, 0.3f, 0.3f, 0.5f);
				
				Payload payload = new Payload();
				payload.setObject(item);
				payload.setDragActor(validImage);
				payload.setValidDragActor(new Image());
				payload.setInvalidDragActor(invalidImage);
				
				obj3d = new Object3D(view3d.getLand3D(), item, 0);
				Vector3 halfSize = obj3d.boundingBox.getDimensions(new Vector3()).scl(0.5f);
				radius = (float)Math.sqrt(halfSize.x * halfSize.x + halfSize.z * halfSize.z);
				tmpInstance = obj3d.getModelInstance();
				
				return payload;
			}
			
			@Override
			public void dragStop(InputEvent event, float x, float y, int pointer, Payload payload, Target target) {
				view3d.getExtraObjects().remove(tmpInstance);
				tmpInstance = null;
				obj3d.close();
				obj3d = null;
			}
		});
		dnd.addTarget(new DragAndDrop.Target(view3d) {
			private boolean visible = false;
			
			@Override
			public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
				Vector3 pos = view3d.pickLandPos(x, y);
				if (pos == null) {
					if (visible) {
						view3d.getExtraObjects().remove(tmpInstance);
						visible = false;
					}
					return false;
				} else {
					if (!visible) {
						view3d.getExtraObjects().add(tmpInstance);
						visible = true;
					}
					pos.y = view3d.getLand().getHeight(pos.x, -pos.z);
					tmpInstance.transform.setTranslation(pos);
					obj3d.setOutOfBoundsError(!isDropAllowed(pos));
					return true;
				}
			}
			
			@Override
			public void drop(Source source, Payload payload, float x, float y, int pointer) {
				visible = false;
				Vector3 pos = view3d.pickLandPos(x, y);
				if (isDropAllowed(pos)) {
					ModelInfo entry = (ModelInfo) payload.getObject();
					app.createObject(entry, new Coord(pos));
				}
			}
		});
		add(grid).grow().pad(5).row();
		
		try {
			if (app.getModelManager() != null) {
				catalog = app.getModelManager().getCatalog();
			} else {
				MessageBox.show(getStage(), I18n.tr("catalogWindow.title"), I18n.tr("catalogWindow.cannotLoad"), MessageType.ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (catalog == null) {
			catalog = Collections.emptyList();
		}
		
		pack();
	}
	
	private boolean isDropAllowed(Vector3 pos) {
		if (pos == null) return false;
		float x = pos.x;
		float z = -pos.z;
		LndFile land = app.getLand();
		return land != null && x > radius && z > radius && x < land.getSideLen() - radius && z < land.getSideLen() - radius;
	}
	
	private void filter(String text) {
		Array<ModelInfo> entries = new Array<>(catalog.size());
		for (ModelInfo entry : catalog) {
			if (entry.toString().toLowerCase().contains(text)) {
				entries.add(entry);
			}
		}
		grid.setItems(entries);
	}
	
	@Override
	public float getMinWidth() {
		return 200f;
	}
	
	@Override
	public float getPrefWidth() {
		return 460f;
	}
	
	@Override
	public SmartWindow show(Stage stage, float x, float y, boolean modal) {
		filter("");
		return super.show(stage, x, y, modal);
	}
	
	@Override
	public boolean remove() {
		boolean removed = super.remove();
		if (removed) {
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
		if (obj3d != null) obj3d.close();
		super.dispose();
	}
}
