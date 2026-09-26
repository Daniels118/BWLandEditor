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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
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
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.libgdx.ui.components.MessageBox;
import it.ld.libgdx.ui.components.SmartWindow;

public class TextureViewer extends SmartWindow {
	private Image preview;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin, String title, Texture texture) {
		TextureViewer editor = (TextureViewer) SmartWindow.getSingleInstance("textureViewer");
		if (editor == null) {
			editor = new TextureViewer(app, skin, title, texture, true);
			editor.setSingleInstance("textureViewer");
			editor.show(stage);
		} else {
			editor.getTitleLabel().setText(title);
			editor.setTexture(texture);
			editor.toFront();
		}
	}
	
	public TextureViewer(MainApp app, Skin skin, String title, Texture texture, boolean autodispose) {
		super(title, skin, SmartWindow.Attribute.RESIZABLE);
		this.setAutodispose(autodispose);
		
		this.preview = new Image(texture);
		preview.setScaling(Scaling.fit);
		preview.setAlign(Align.topLeft);
		preview.setSize(texture.getWidth(), texture.getHeight());
		
		this.add(preview).top().pad(10f);
		
		Table tools = new Table();
		
		TextButton exportButton = new TextButton(I18n.tr("textureViewer.export"), skin);
		exportButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				app.getOS().getFileChooser().chooseFile(
		            new NativeFileChooserConfiguration() {{
		                title = I18n.tr("dialog.exportImage.title");
		                directory = Gdx.files.absolute(System.getProperty("user.home"));
		                nameFilter = (dir, name) -> name.endsWith(".png");
		                mimeFilter = "PNG image/png";
		                intent = NativeFileChooserIntent.SAVE;
		            }},
		            new NativeFileChooserCallback() {
		                @Override
		                public void onFileChosen(FileHandle file) {
		                    try {
		                    	TextureData textureData = texture.getTextureData();
		                    	if (!textureData.isPrepared()) textureData.prepare();
		                    	Pixmap pixmap = textureData.consumePixmap();
		                    	PixmapIO.writePNG(file, pixmap);
		                    	if (textureData.disposePixmap()) pixmap.dispose();
							} catch (Exception e) {
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
		
		this.add(tools).top().pad(10f, 0f, 10f, 10f);
		pack();
	}
	
	public void setTexture(Texture texture) {
		preview.setDrawable(new TextureRegionDrawable(texture));
		preview.setSize(texture.getWidth(), texture.getHeight());
		pack();
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
}
