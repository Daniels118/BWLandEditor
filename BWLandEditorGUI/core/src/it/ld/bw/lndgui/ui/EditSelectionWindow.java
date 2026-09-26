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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import it.ld.bw.info.TribeType;
import it.ld.bw.lhx.Command;
import it.ld.bw.lhx.Statement;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.gfx.Object3D;
import it.ld.libgdx.ui.components.ListTable;
import it.ld.libgdx.ui.components.SmartWindow;

public class EditSelectionWindow extends SmartWindow {
	private Table lists = new Table();
	
	private ListTable<Option<Command>> cmdList;
	private ListTable<Option<Object>> typeList;
	private ListTable<Option<Integer>> townList;
	private ListTable<Option<TribeType>> tribeList;
	
	private EventListener callback;
	
	public EditSelectionWindow(MainApp app, Skin skin, String title) {
		super(title, skin, Attribute.RESIZABLE, Attribute.AUTODISPOSE);
		
		cmdList = createList(skin, "editSelectionWindow.command");
		typeList = createList(skin, "editSelectionWindow.type");
		townList = createList(skin, "editSelectionWindow.town");
		tribeList = createList(skin, "editSelectionWindow.tribe");
		
		add(lists).row();
		
		TextButton okButton = new TextButton(I18n.tr("dialog.ok"), skin);
		okButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (callback != null) {
					event.handle();
					callback.handle(new Event());
					remove();
				}
			}
		});
		add(okButton).right();
		
		pack();
	}
	
	private <T> ListTable<Option<T>> createList(final Skin skin, final String titleKey) {
		ListTable<Option<T>> list = new ListTable<Option<T>>(skin)
		.setCellPadding(0);
		list.setModel(new ListTable.DefaultModel<Option<T>>(skin) {
			@Override
			public String[] getHeaderNames() {
				return new String[] {I18n.tr(titleKey)};
			}
			
			private final ChangeListener changeListener = new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					int row = list.getItemIndex(actor);
					list.getItem(row).checked = ((CheckBox)actor).isChecked();
					list.refresh(row);
				}
			};
			
			@Override
			public Actor createField(int row, int col) {
				CheckBox field = new CheckBox("", skin);
				field.align(Align.left);
				field.addListener(changeListener);
				return field;
			}
			
			@Override
			public void render(int row, Option<T> item, Cell<Container<Actor>>[] fields) {
				CheckBox field = ((CheckBox)fields[0].getActor().getActor());
				//Content
				field.setText(item.toString());
				field.setChecked(item.checked);
			}
		})
		.setHeaderVisible(true)
		.setRowsBackground(null, null);
		return list;
	}
	
	public EditSelectionWindow setCallback(EventListener callback) {
		this.callback = callback;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public EditSelectionWindow setSampleObjects(List<Object3D> objects) {
		Map<Command, Option<Command>> commands = new HashMap<>();
		Map<Object, Option<Object>> types = new HashMap<>();
		Map<Integer, Option<Integer>> towns = new HashMap<>();
		Map<TribeType, Option<TribeType>> tribes = new HashMap<>();
		if (!objects.isEmpty()) {
			Object3D obj0 = objects.get(0);
			Statement rStmt = obj0.getStatement();
	    	Command sharedCmd = rStmt.getCommand();
	    	Object sharedType = rStmt.getType();
	    	int sharedTown = rStmt.getCommand().town >= 0 ? rStmt.getTown() : -2;
	    	TribeType sharedTribe = rStmt.getEffectiveTribe();
	    	//
	    	for (Object3D obj : objects) {
	    		Statement stmt = obj.getStatement();
	    		Command cmd = stmt.getCommand();
	    		Object type = stmt.getType();
	    		int town = stmt.getCommand().town >= 0 ? stmt.getTown() : -2;
	    		TribeType tribe = stmt.getEffectiveTribe();
	    		//
	    		if (!commands.containsKey(cmd)) {
	    			commands.put(cmd, new Option<>(cmd));
	    		}
	    		if (type != null && !types.containsKey(type)) {
	    			types.put(type, new Option<>(type));
	    		}
	    		if (town >= -1 && !towns.containsKey(town)) {
	    			towns.put(town, new Option<>(town));
	    		}
	    		if (tribe != null && !tribes.containsKey(tribe)) {
	    			tribes.put(tribe, new Option<>(tribe));
	    		}
	    		//
	    		if (cmd != sharedCmd) {
	    			sharedCmd = null;
	    			sharedType = null;
	    		}
	    		if (!Objects.equals(type, sharedType)) {
	    			sharedType = null;
	    		}
	    		if (town != sharedTown) {
	    			sharedTown = -2;
	    		}
	    		if (tribe != sharedTribe) {
	    			sharedTribe = null;
	    		}
	    	}
	    	//
	    	if (sharedCmd != null) {
	    		//commands.get(sharedCmd).checked = true;
	    	} else {
	    		types.clear();
	    	}
	    	/*if (sharedType != null) {
	    		types.get(sharedType).checked = true;
	    	}
	    	if (sharedTown != -2) {
	    		towns.get(sharedTown).checked = true;
	    	}
	    	if (sharedTribe != null) {
	    		tribes.get(sharedTribe).checked = true;
	    	}*/
		}
		cmdList.setItems(new Array<Option<Command>>(commands.values().toArray(new Option[0])));
		typeList.setItems(new Array<Option<Object>>(types.values().toArray(new Option[0])));
		townList.setItems(new Array<Option<Integer>>(towns.values().toArray(new Option[0])));
		tribeList.setItems(new Array<Option<TribeType>>(tribes.values().toArray(new Option[0])));
		
		lists.clear();
		if (!commands.isEmpty()) lists.add(cmdList).minSize(180, 160).prefSize(200, 320).grow().pad(5);
		if (!types.isEmpty()) lists.add(typeList).minSize(200, 160).prefSize(270, 320).grow().pad(5);
		if (!towns.isEmpty()) lists.add(townList).minSize(50, 160).prefSize(80, 320).grow().pad(5);
		if (!tribes.isEmpty()) lists.add(tribeList).minSize(100, 160).prefSize(130, 320).grow().pad(5);
		
		pack();
    	return this;
	}
	
	public Set<Command> getSelectedCommands() {
		if (cmdList.getItems().isEmpty()) return null;
		Set<Command> values = new HashSet<>();
		for (Option<Command> entry : cmdList.getItems()) {
			if (entry.checked) {
				values.add(entry.value);
			}
		}
		return values;
	}
	
	public Set<Object> getSelectedTypes() {
		if (typeList.getItems().isEmpty()) return null;
		Set<Object> values = new HashSet<>();
		for (Option<Object> entry : typeList.getItems()) {
			if (entry.checked) {
				values.add(entry.value);
			}
		}
		return values;
	}
	
	public Set<Integer> getSelectedTowns() {
		if (townList.getItems().isEmpty()) return null;
		Set<Integer> values = new HashSet<>();
		for (Option<Integer> entry : townList.getItems()) {
			if (entry.checked) {
				values.add(entry.value);
			}
		}
		return values;
	}
	
	public Set<TribeType> getSelectedTribes() {
		if (tribeList.getItems().isEmpty()) return null;
		Set<TribeType> values = new HashSet<>();
		for (Option<TribeType> entry : tribeList.getItems()) {
			if (entry.checked) {
				values.add(entry.value);
			}
		}
		return values;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
	
	private static class Option<T> {
		public final T value;
		public boolean checked = true;
		
		public Option(T value) {
			this.value = value;
		}
		
		@Override
		public int hashCode() {
			return value.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Option)) return false;
			Option<?> other = (Option<?>) obj;
			return this.value.equals(other.value);
		}
		
		@Override
		public String toString() {
			return value.toString();
		}
	}
}
