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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;

import it.ld.bw.lnd.model.LH3DLandCell.Sound;
import it.ld.bw.lndgui.I18n;
import it.ld.libgdx.ui.components.GridView;
import it.ld.libgdx.ui.components.GridView.GridModel;
import it.ld.libgdx.ui.components.GridView.ItemClickEvent;
import it.ld.libgdx.ui.components.GridView.ItemClickListener;
import it.ld.libgdx.ui.components.GridView.State;
import it.ld.libgdx.ui.components.SmartWindow;

public class SoundPicker extends SmartWindow {
	private final GridView<Sound> grid;
	
	private EventListener callback;
	
	private Styler styler;
	
	public SoundPicker(Skin skin, boolean autodispose) {
		this(skin, autodispose, false);
	}
	
	public SoundPicker(Skin skin, boolean autodispose, boolean multiselect) {
		super(I18n.tr("soundPicker.title"), skin);
		this.setAutodispose(autodispose);
		
		final Array<Sound> sounds = new Array<Sound>(Sound.values());
		
		grid = new GridView<Sound>(skin);
		grid.setMinCols(7);
		grid.setForceScroll(false);
		grid.setTextWrapEnabled(true);
		grid.setPadding(2);
		grid.setIconSize(64);
		grid.setItems(sounds);
		grid.setModel(new GridModel<Sound>() {
			private final Drawable[] images;
			private final String[] labels;
			
			{
				images = new Drawable[sounds.size];
				labels = new String[sounds.size];
				for (int i = 0; i < sounds.size; i++) {
					Sound sound = sounds.get(i);
					images[i] = skin.getDrawable("sound-" + sound.name().toLowerCase());
					labels[i] = I18n.tr("cell.sounds." + sound.name());
				}
			}
			
			@Override
			public void render(int index, Sound item, Container<Actor> container, Image image, Label label, State state) {
				image.setDrawable(images[index]);
				label.setText(labels[index]);
				if (styler != null) {
					styler.process(index, item, state);
				}
			}
		});
		add(grid).grow().pad(5).row();
		
		if (multiselect) {
			TextButton selectButton = new TextButton(I18n.tr("dialog.select"), skin);
			selectButton.setDisabled(true);
			selectButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if (callback != null) {
						event.handle();
						callback.handle(new Event());
						remove();
					}
				}
			});
			add(selectButton).right().pad(0, 0, 5, 5);
			
			grid.getSelection().setMultiple(true);
			grid.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if (selectButton != null) {
						selectButton.setDisabled(grid.getSelection().isEmpty());
					}
				}
			});
		} else {
			grid.addListener(new ItemClickListener<Sound>() {
				@Override
				public void clicked(ItemClickEvent<Sound> event, int index, Sound item) {
					if (callback != null) {
						event.handle();
						callback.handle(new Event());
						remove();
					}
				}
			});
		}
		
		pack();
	}
	
	public SoundPicker setStyler(Styler styler) {
		this.styler = styler;
		this.grid.refresh();
		return this;
	}
	
	public SoundPicker setCallback(EventListener callback) {
		this.callback = callback;
		return this;
	}
	
	public ArraySelection<Sound> getSelection() {
		return grid.getSelection();
	}
	
	public int getSelectedIndex() {
		return this.grid.getSelectedIndex();
	}
	
	public Sound getSelected() {
		return this.grid.getSelected();
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	
	public static class Styler {
		public void process(int index, Sound item, State state) {}
	}
}
