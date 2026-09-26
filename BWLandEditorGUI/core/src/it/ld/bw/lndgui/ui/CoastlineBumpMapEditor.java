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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.SimpleMap;
import it.ld.bw.lnd.tools.SimpleMapTool;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.libgdx.ui.components.MessageBox;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.utils.Utils;
import it.ld.utils.UChangeListener;

public class CoastlineBumpMapEditor extends SmartWindow {
	private LndFile land;
	
	private Texture texture;
	private Image preview;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		CoastlineBumpMapEditor editor = (CoastlineBumpMapEditor) SmartWindow.getSingleInstance("coastlineBumpMapEditor");
		if (editor == null) {
			editor = new CoastlineBumpMapEditor(app, skin, true);
			editor.setSingleInstance("coastlineBumpMapEditor");
			editor.show(stage);
		} else {
			editor.toFront();
		}
	}
	
	public CoastlineBumpMapEditor(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("coastlineBumpMapEditor.title"), skin);
		this.setAutodispose(autodispose);
		this.land = app.getLand();
		
		this.preview = new Image();
		preview.setScaling(Scaling.fit);
		preview.setAlign(Align.top);
		preview.setSize(SimpleMap.width, SimpleMap.height);
		
		this.add(preview).top().pad(10f);
		
		Table tools = new Table();
		
		TextButton importButton = new TextButton(I18n.tr("coastlineBumpMapEditor.import"), skin);
		importButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				app.getOS().getFileChooser().chooseFile(
		            new NativeFileChooserConfiguration() {{
		                title = I18n.tr("dialog.exportMaterial.title");
		                directory = Gdx.files.absolute(System.getProperty("user.home"));
		                nameFilter = (dir, name) ->
	                	name.endsWith(".lnd" ) ||
	                	name.endsWith(".png" ) ||
	                	name.endsWith(".gif" ) ||
	                	name.endsWith(".jpg" ) ||
	                	name.endsWith(".gpeg") ||
	                	name.endsWith(".bmp" ) ||
	                	name.endsWith(".raw" );
	                mimeFilter = "All supported formats/lnd,png,gif,jpg,jpeg,bmp,raw";
		                intent = NativeFileChooserIntent.OPEN;
		            }},
		            new NativeFileChooserCallback() {
		                @Override
		                public void onFileChosen(FileHandle file) {
		                    try {
		                    	String ext = file.name().substring(file.name().lastIndexOf('.') + 1).toLowerCase();
			                	if ("lnd".equals(ext)) {
			                		LndFile srcLand = LndFile.load(file.file(), false);
			                		land.setCoastlineBumpMap(srcLand.getCoastlineBumpMap().clone());
				                } else {
				                	SimpleMap newMap = SimpleMapTool.importSimpleMap(file.file());
				                	land.setCoastlineBumpMap(newMap);
				                }
							} catch (Exception e) {
								e.printStackTrace();
								MessageBox.show(getStage(), I18n.tr("dialog.importImage.title"), e);
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
		tools.add(importButton).fillX().top().padBottom(5f).row();
		
		TextButton exportButton = new TextButton(I18n.tr("coastlineBumpMapEditor.export"), skin);
		exportButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				app.getOS().getFileChooser().chooseFile(
		            new NativeFileChooserConfiguration() {{
		                title = I18n.tr("dialog.exportMaterial.title");
		                directory = Gdx.files.absolute(System.getProperty("user.home"));
		                nameFilter = (dir, name) -> name.endsWith(".png") || name.endsWith(".raw");
		                mimeFilter = "Images/png,raw";
		                intent = NativeFileChooserIntent.SAVE;
		            }},
		            new NativeFileChooserCallback() {
		                @Override
		                public void onFileChosen(FileHandle file) {
		                    try {
								SimpleMapTool.exportSimpleMap(land.getCoastlineBumpMap(), file.file());
							} catch (IOException e) {
								e.printStackTrace();
								MessageBox.show(getStage(), I18n.tr("dialog.exportImage.title"), e);
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
		tools.add(exportButton).fillX().top().padBottom(5f).row();
		
		TextButton invertButton = new TextButton(I18n.tr("coastlineBumpMapEditor.invert"), skin);
		invertButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				SimpleMap newMap = land.getCoastlineBumpMap().clone();
				newMap.invert();
				land.setCoastlineBumpMap(newMap);
			}
		});
		tools.add(invertButton).fillX().top().padBottom(5f).row();
		
		this.add(tools).top().pad(10f, 0f, 10f, 10f);
		
		updatePreview();
		land.listeners.add(landChangeListener);
		
		pack();
	}
	
	private final UChangeListener landChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.CHANGE && event.getProperty() == LndFile.Property.COASTLINE_BUMPMAP) {
				updatePreview();
			}
		}
	};
	
	private void updatePreview() {
		if (this.texture != null) {
			texture.dispose();
		}
		byte[] alpha = land.getCoastlineBumpMap().getPixels();
		byte[] data = new byte[SimpleMap.width * SimpleMap.height * 4];
		for (int src = 0, dst = 0; src < alpha.length; src++) {
			data[dst++] = alpha[src];
			data[dst++] = alpha[src];
			data[dst++] = alpha[src];
			data[dst++] = (byte)255;
		}
		this.texture = Utils.toTexture(data, SimpleMap.width, SimpleMap.height, true);
		preview.setDrawable(new TextureRegionDrawable(texture));
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		land.listeners.remove(landChangeListener);
		if (texture != null) {
			texture.dispose();
			texture = null;
		}
		super.dispose();
	}
}
