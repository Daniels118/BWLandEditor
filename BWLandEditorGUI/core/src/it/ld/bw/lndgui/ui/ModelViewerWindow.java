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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.gfx.L3DModelManager.ModelInfo;
import it.ld.bw.lndgui.gfx.Object3D;
import it.ld.bw.lndgui.gfx.OrbitCamera;
import it.ld.bw.lndgui.tools.ObjExporter;
import it.ld.libgdx.ui.components.MessageBox;
import it.ld.libgdx.ui.components.SmartWindow;

public class ModelViewerWindow extends SmartWindow {
	private final TextField searchField;
	private final ScrollPane scrollPane;
	private final List<ModelInfo> list;
	private final View3D view3d;
	
	private ArrayList<ModelInfo> catalog;
	private ModelInfo modelInfo;
	private Object3D obj3d;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		ModelViewerWindow window = (ModelViewerWindow) SmartWindow.getSingleInstance("modelViewer");
		if (window == null) {
			window = new ModelViewerWindow(app, skin, true);
			window.setDockingManager(app.getDockingManager());
			window.setSingleInstance("modelViewer");
			window.show(stage);
		}
		window.toFront();
	}
	
	public ModelViewerWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("modelViewer.title"), skin, Attribute.RESIZABLE, Attribute.DOCKABLE);
		this.setAutodispose(autodispose);
		
		setMinSize(165, 180);
		
		searchField = new TextField("", skin);
		searchField.addListener(new InputListener() {
			@Override
			public boolean keyTyped(InputEvent event, char character) {
				filter(searchField.getText());
				return false;
			}
		});
		add(searchField).fillX();
		
		Table buttons = new Table();
		buttons.defaults().padRight(5);
		
		TextButton openButton = new TextButton(I18n.tr("modelViewer.open"), skin);
		openButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				app.getOS().getFileChooser().chooseFile(
			            new NativeFileChooserConfiguration() {{
			                title = I18n.tr("dialog.openModel.title");
			                directory = Gdx.files.absolute(System.getProperty("user.home"));
			                nameFilter = (dir, name) -> name.toLowerCase().endsWith(".l3d" );
			                mimeFilter = "L3D files/l3d";
			                intent = NativeFileChooserIntent.OPEN;
			            }},
			            new NativeFileChooserCallback() {
			                @Override
			                public void onFileChosen(FileHandle file) {
			                    try {
			                    	ModelInfo modelInfo = app.getModelManager().getModelInfo(file.file(), false, null, null, null);
			                    	setModel(modelInfo);
								} catch (Exception e) {
									e.printStackTrace();
									MessageBox.show(getStage(), I18n.tr("dialog.openModel.title"), e);
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
		buttons.add(openButton);
		
		TextButton saveImageButton = new TextButton(I18n.tr("modelViewer.saveImage"), skin);
		saveImageButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				app.getOS().getFileChooser().chooseFile(
		            new NativeFileChooserConfiguration() {{
		                title = I18n.tr("dialog.exportImage.title");
		                directory = Gdx.files.absolute(System.getProperty("user.home"));
		                nameFilter = (dir, name) -> name.endsWith(".jpg") || name.endsWith(".png");
		                mimeFilter = "Images/jpg,png";
		                intent = NativeFileChooserIntent.SAVE;
		            }},
		            new NativeFileChooserCallback() {
		                @Override
		                public void onFileChosen(FileHandle file) {
		                	view3d.getRenderedImage((Pixmap pixmap) -> {
								try {
									app.getOS().getImageWriter().write(file.file(), pixmap);
								} catch (IOException e) {
									e.printStackTrace();
									MessageBox.show(getStage(), I18n.tr("dialog.exportImage.title"), e);
								}
							});
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
		buttons.add(saveImageButton);
		
		TextButton exportButton = new TextButton(I18n.tr("modelViewer.export"), skin);
		exportButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				app.getOS().getFileChooser().chooseFile(
		            new NativeFileChooserConfiguration() {{
		                title = I18n.tr("dialog.exportModel.title");
		                directory = Gdx.files.absolute(System.getProperty("user.home"));
		                nameFilter = (dir, name) -> name.endsWith(".obj");
		                mimeFilter = "Wavefront (.obj)/obj";
		                intent = NativeFileChooserIntent.SAVE;
		            }},
		            new NativeFileChooserCallback() {
		                @Override
		                public void onFileChosen(FileHandle file) {
		                	try {
		                		ObjExporter.export(modelInfo.model, file.file());
							} catch (Exception e) {
								e.printStackTrace();
								MessageBox.show(getStage(), I18n.tr("dialog.exportModel.title"), e);
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
		buttons.add(exportButton);
		
		add(buttons).right();
		
		row();
		
		list = new List<>(skin);
		list.setItems();
		list.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				ModelInfo entry = list.getSelected();
				if (entry != null) {
					setModel(entry);
					scrollTo(list.getSelectedIndex());
				}
			}
		});
		scrollPane = new ScrollPane(list);
		scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // no X, yes Y
        scrollPane.setOverscroll(false, true);
		add(scrollPane).fillX().growY().prefHeight(400);
		
		
		view3d = new View3D(app);
		view3d.setAlphaEnabled(true);
		view3d.setOceanEnabled(false);
		view3d.setSkyEnabled(false);
		view3d.setBackgroundColor(new Color(0f, 0f, 0f, 0f));
		add(view3d).prefSize(500).grow().row();
		
		/*LHXFile lhx = new LHXFile();
		LHXParser parser = new LHXParser();
		try {
			lhx.getStatements().addAll(parser.parse("CREATE_VILLAGER_POS(\"0,0\", \"0,0\", \"NORSE_FORESTER\", 21)", false));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		view3d.setLHX(lhx);*/
		
		/*try {
			Model model = app.getModelManager().getVillager("GREEK_HOUSEWIFE");
			setModel(model);
		} catch (Exception e) {
			app.showError(I18n.tr("modelViewer.title"), e);
		}*/
		
		try {
			catalog = app.getModelManager().getCatalog();
		} catch (Exception e) {
			e.printStackTrace();
		}
		filter("");
		
		pack();
	}
	
	private void scrollTo(int row) {
    	float itemHeight = list.getItemHeight();
		float y = row * itemHeight;
    	scrollPane.scrollTo(0, list.getHeight() - y, 0, itemHeight, false, false);
    }
	
	private void filter(String text) {
		text = text.toLowerCase();
		final Array<ModelInfo> entries = new Array<>(catalog.size());
		for (ModelInfo e : catalog) {
			if (e.toString().toLowerCase().contains(text)) {
				entries.add(e);
			}
		}
		list.setItems(entries);
	}
	
	public void setModel(ModelInfo modelInfo) {
		if (this.modelInfo != null) {
			view3d.getExtraObjects().clear();
			obj3d.close();
			obj3d = null;
		}
		this.modelInfo = modelInfo;
		if (modelInfo != null) {
			obj3d = new Object3D(modelInfo, 0);
			if (!obj3d.getState(0)) {
				obj3d.enableStates(-1, -1);
			}
			ModelInstance inst = obj3d.getModelInstance();
			view3d.getExtraObjects().add(inst);
			BoundingBox bb = inst.calculateBoundingBox(new BoundingBox());
			OrbitCamera camera = view3d.getCamera();
			camera.setPivot(bb.getCenter(new Vector3()));
			float fovTan = MathUtils.tanDeg(camera.getFieldOfView() / 2f);
			float halfw = bb.getWidth() * 0.5f;
			float halfh = bb.getHeight() * 0.5f;
			float halfd = bb.getDepth() * 0.5f;
			float wDist = halfw / fovTan;
			float hDist = halfh / fovTan;
			float dDist = halfd / fovTan;
			float maxDist = Math.max(Math.max(wDist, hDist), dDist);
			camera.setRadius(maxDist + halfd);
		}
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		setModel(null);
		view3d.dispose();
		super.dispose();
	}
}
