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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.gfx.Coord;
import it.ld.bw.lndgui.gfx.OrbitCamera;
import it.ld.libgdx.ui.components.ImageButton;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.utils.UChangeListener;

public class CameraInfoWindow extends SmartWindow {
	private Label positionLabel;
	private Label lookAtLabel;
	
	private OrbitCamera camera;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		CameraInfoWindow window = (CameraInfoWindow) SmartWindow.getSingleInstance("cameraInfo");
		if (window == null) {
			window = new CameraInfoWindow(app, skin);
			window.setDockingManager(app.getDockingManager());
			window.setSingleInstance("cameraInfo");
		}
		window.show(stage, stage.getWidth() - window.getWidth(), 28f, false);
		window.toFront();
	}
	
	public static void hideInstance() {
		CameraInfoWindow window = (CameraInfoWindow) SmartWindow.getSingleInstance("cameraInfo");
		if (window != null) {
			window.remove();
		}
	}
	
	private CameraInfoWindow(MainApp app, Skin skin) {
		super(I18n.tr("cameraInfo.title"), skin, Attribute.DOCKABLE, Attribute.AUTODISPOSE);
		
		defaults().padBottom(5);
		columnDefaults(1).width(200);
		columnDefaults(2).expandX().left();
		
		positionLabel = new Label("", skin);
		lookAtLabel = new Label("", skin);
		
		add(new Label(I18n.tr("cameraInfo.position"), skin)).left().padRight(10);
		add(positionLabel).left();
		Button copyPositionButton = new ImageButton(skin, "copy", I18n.tr("dialog.copy"), () -> {
			Gdx.app.getClipboard().setContents(positionLabel.getText().toString());
		});
		add(copyPositionButton).size(20).padLeft(5);
		row();
		
		add(new Label(I18n.tr("cameraInfo.lookAt"), skin)).left().padRight(10);
		add(lookAtLabel).left();
		Button copyLookAtButton = new ImageButton(skin, "copy", I18n.tr("dialog.copy"), () -> {
			Gdx.app.getClipboard().setContents(lookAtLabel.getText().toString());
		});
		add(copyLookAtButton).size(20).padLeft(5);
		row();
		
		Button getSetCommandsButton = new TextButton(I18n.tr("cameraInfo.getSetCommands"), skin);
		getSetCommandsButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				String cmds = String.format("set camera position to %s\r\n"
										  + "set camera focus to %s",
										  positionLabel.getText(),
										  lookAtLabel.getText());
				Gdx.app.getClipboard().setContents(cmds);
			}
		});
		add(getSetCommandsButton).colspan(3).growX();
		row();
		
		Button getMoveCommandsButton = new TextButton(I18n.tr("cameraInfo.getMoveCommands"), skin);
		getMoveCommandsButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				String cmds = String.format("move camera position to %s time 1.0\r\n"
										  + "move camera focus to %s time 1.0",
										  positionLabel.getText(),
										  lookAtLabel.getText());
				Gdx.app.getClipboard().setContents(cmds);
			}
		});
		add(getMoveCommandsButton).colspan(3).growX();
		row();
		
		add().colspan(3).expand();	//Filler
		
		pack();
		setMinSize(getPrefWidth(), getPrefHeight());
		invalidateHierarchy();
		
		camera = app.getView3D().getCamera();
		camera.listeners.add(cameraChangeListener);
		
		updatePosition();
		updatePivot();
	}
	
	private final UChangeListener cameraChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == OrbitCamera.Property.POSITION) {
				updatePosition();
			} else if (event.getProperty() == OrbitCamera.Property.PIVOT) {
				updatePivot();
			}
		}
	};
	
	private final Coord tmp = new Coord();
	
	private void updatePosition() {
		tmp.set(camera.getPosition());
		positionLabel.setText(tmp.toString());
	}
	
	private void updatePivot() {
		tmp.set(camera.getPivot());
		lookAtLabel.setText(tmp.toString());
	}
	
	@Override
	public void dispose() {
		camera.listeners.remove(cameraChangeListener);
		super.dispose();
	}
}
