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

import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.libgdx.ui.components.MenuItemAction;
import it.ld.libgdx.ui.components.PopupMenu;
import it.ld.libgdx.ui.components.SmartWindow;

public class OutputWindow extends SmartWindow {
	private static boolean opened = false;
	
	private final MainApp app;
	
	private ScrollPane scrollPane;
	private Label textarea;
	
	private volatile boolean dataAvailable = false;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		OutputWindow window = (OutputWindow) SmartWindow.getSingleInstance("outputWindow");
		if (window == null) {
			window = new OutputWindow(app, skin, true);
			window.setDockingManager(app.getDockingManager());
			window.setSingleInstance("outputWindow");
			window.show(stage, stage.getWidth() - window.getWidth(), 28, false);
		}
		window.toFront();
	}
	
    public OutputWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("outputWindow.title"), skin, Attribute.RESIZABLE, Attribute.DOCKABLE);
		this.setAutodispose(autodispose);
		this.app = app;
		
		setMinSize(255, 180);
		setSize(650, 330);
		
		textarea = new Label("", skin, "mono");
		textarea.addListener(new ClickListener(Buttons.RIGHT) {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				PopupMenu.show1(getStage(), event.getStageX(), event.getStageY(), new MenuItemAction[] {
					new MenuItemAction("copy", I18n.tr("outputWindow.copy"), () -> {
						Gdx.app.getClipboard().setContents(textarea.getText().toString());
					}),
					new MenuItemAction("clear", I18n.tr("outputWindow.clear"), () -> {
						app.getStdOut().clear();
						textarea.setText("");
					})
				});
			}
		});
		
		scrollPane = new ScrollPane(textarea, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, false);
        scrollPane.setOverscroll(false, false);
        scrollPane.setForceScroll(false, true);
        scrollPane.setScrollbarsVisible(true);
		add(scrollPane).minSize(250, 160).grow();
		
		app.getStdOut().addConsumer(consumer);
		update();
	}
	
	private final Consumer<String[]> consumer = (lines) -> {
		dataAvailable = true;
	};
	
	@Override
	public void act(float delta) {
		if (dataAvailable) {
			update();
		}
		super.act(delta);
	}
	
	private void update() {
		String[] lines = app.getStdOut().getLines();
		String text = String.join("", lines).replaceAll("\r", "").replaceAll("\t", "    ");
		textarea.setText(text);
		scrollPane.invalidate();
		scrollPane.layout();
		scrollPane.setScrollPercentY(1f);
		dataAvailable = false;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		app.getStdOut().removeConsumer(consumer);
		super.dispose();
	}
	
	@Override
	public SmartWindow show(Stage stage, float x, float y, boolean modal) {
		opened = true;
		return super.show(stage, x, y, modal);
	}
	
	@Override
	public boolean remove() {
		opened = false;
		return super.remove();
	}
	
	public static boolean isOpen() {
		return opened;
	}
}
