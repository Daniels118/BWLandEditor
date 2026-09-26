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

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import it.ld.bw.lndgui.BuildInfo;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.libgdx.ui.components.SmartWindow;

public class AboutWindow extends SmartWindow {
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		AboutWindow window = (AboutWindow) SmartWindow.getSingleInstance("aboutWindow");
		if (window == null) {
			window = new AboutWindow(app, skin, true);
			window.setSingleInstance("aboutWindow");
		}
		window.show(stage, true);
		window.toFront();
	}
	
	public static void hideInstance() {
		AboutWindow window = (AboutWindow) SmartWindow.getSingleInstance("aboutWindow");
		if (window != null) {
			window.remove();
		}
	}
	
	public AboutWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("aboutWindow.title", MainApp.APP_NAME), skin);
		this.setAutodispose(autodispose);
		
		add(new Image(skin.getDrawable("tool-sculpt"), Scaling.fit)).width(64).fill().top().pad(10);
		
		Table table = new Table();
		table.defaults().align(Align.topLeft);
		
		table.add(new Label(I18n.tr("aboutWindow.developer") + " Daniels118", skin)).row();
		table.add(new Label(I18n.tr("aboutWindow.version") + " " + BuildInfo.VERSION, skin)).row();
		table.add(new Label(I18n.tr("aboutWindow.license") + " GNU General Public License 3", skin)).row();
		
		add(table).expandX().align(Align.topLeft).padTop(10).pad(10, 0, 10, 10).row();
		
		add().colspan(2).minHeight(44).expandX();
		
		pack();
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
}
