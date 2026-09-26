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

import java.util.ListIterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.LandEditManager;
import it.ld.bw.lndgui.MainApp;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.utils.Edit;
import it.ld.utils.UChangeListener;

public class HistoryWindow extends SmartWindow {
	private final MainApp app;
	
	private ScrollPane scrollPane;
	private List<Edit> list;
	
	private boolean updating = false;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		HistoryWindow window = (HistoryWindow) SmartWindow.getSingleInstance("editHistory");
		if (window == null) {
			window = new HistoryWindow(app, skin, true);
			window.setDockingManager(app.getDockingManager());
			window.setSingleInstance("editHistory");
			window.show(stage, stage.getWidth() - window.getWidth(), stage.getHeight() - window.getHeight() - 62, false);
		}
		window.toFront();
	}
	
	public HistoryWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("historyWindow.title"), skin, Attribute.RESIZABLE, Attribute.DOCKABLE);
		this.setAutodispose(autodispose);
		this.app = app;
		
		setMinSize(165, 180);
		setSize(180, 250);
		
		list = new List<Edit>(skin) {
			@Override
		    protected GlyphLayout drawItem(Batch batch, BitmapFont font, int index, Edit item, float x, float y, float width) {
		    	Color oldColor = font.getColor();
		    	if (index > getSelectedIndex()) font.setColor(Color.GRAY);
				GlyphLayout r = super.drawItem(batch, font, index, item, x, y, width);
				font.setColor(oldColor);
				return r;
		    }
		};
		list.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (!updating) {
					updating = true;
					if (list.getItems().isEmpty()) return;
					int pos = list.getItems().size - 1 - list.getSelectedIndex();
					app.getEditManager().setPosition(pos);
					updating = false;
					update();
				}
			}
		});
		
		scrollPane = new ScrollPane(list, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // no X, yes Y
        scrollPane.setOverscroll(false, true);
		add(scrollPane).minSize(160, 160).grow();
		
		update();
		
		app.listeners.add(appChangeListener);
		pack();
	}
	
	private final UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.UNDO_HISTORY) {
				update();
			}
		}
	};
	
	private void update() {
		if (!updating) {
			updating = true;
			LandEditManager manager = app.getEditManager();
			java.util.List<Edit> history = manager.getHistory();
			Edit[] items = new Edit[history.size()];
			ListIterator<Edit> it = history.listIterator(history.size());
			int i = 0;
			while (it.hasPrevious()) {
				items[i++] = it.previous();
			}
			list.setItems(items);
			Gdx.app.postRunnable(() -> {
				int count = list.getItems().size;
				if (count > 0) {
					int index = count - 1 - manager.getPosition();
					list.setSelectedIndex(index);
					float itemHeight = list.getItemHeight();
					float y = list.getHeight() - index * itemHeight;
					scrollPane.scrollTo(0, y, list.getWidth(), itemHeight);
				}
			});
			updating = false;
		}
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		app.listeners.remove(appChangeListener);
		super.dispose();
	}
}
