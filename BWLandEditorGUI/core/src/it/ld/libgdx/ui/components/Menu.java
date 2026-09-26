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
package it.ld.libgdx.ui.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class Menu extends WidgetGroup {
	private final Skin skin;
	private final float buttonPadding = 5f;
	private boolean disabled = false;
	private PopupMenu activeMenu;
	
	private final Map<Integer, List<MenuItemAction>> shortcutMap = new HashMap<>();
	
	public Menu(Skin skin) {
		this.skin = skin;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
		for (Actor actor : this.getChildren()) {
			Button button = (Button)actor;
			button.setDisabled(disabled);
		}
	}
	
	public Runnable findShortcutAction(int keycode, boolean ctrlPressed, boolean shiftPressed, boolean altPressed) {
		List<MenuItemAction> keyActions = shortcutMap.get(keycode);
		if (keyActions != null) {
			for (MenuItemAction item : keyActions) {
				if (item.isShortcutCtrl() == ctrlPressed &&
					item.isShortcutShift() == shiftPressed &&
					item.isShortcutAlt() == altPressed) {
					if (item.action != null && (item.enableStatus == null || item.enableStatus.getStatus(item))) {
						return item.action;
					}
				}
			}
		}
    	return null;
	}
	
	public Button add(String item) {
		return this.add(item, new MenuItemAction[0]);
	}
	
	public Button add(String item, MenuItemAction...actions) {
		TextButton button = new TextButton(item, skin);
		button.padLeft(Math.max(button.getPadLeft(), buttonPadding));
		button.padRight(Math.max(button.getPadRight(), buttonPadding));
		button.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (!button.isDisabled()) {
					activeMenu = PopupMenu.show1(button, actions);
				}
			}
		});
		button.addListener(new InputListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				if (activeMenu != null && activeMenu.isVisible()) {
					activeMenu = PopupMenu.show1(button, actions);
				}
			}
		});
		addActor(button);
		for (MenuItemAction action : actions) {
			if (action.getShortcut() != null) {
				List<MenuItemAction> keyActions = shortcutMap.get(action.getShortcutKey());
				if (keyActions == null) {
					keyActions = new ArrayList<>(1);
					shortcutMap.put(action.getShortcutKey(), keyActions);
				}
				keyActions.add(action);
			}
		}
		return button;
	}
	
	@Override
	public float getPrefWidth() {
		float width = 0;
		for (Actor actor : this.getChildren()) {
			width += ((Button)actor).getPrefWidth();
		}
		return width;
	}
	
	@Override
	public float getMinWidth() {
		return getPrefWidth();
	}
	
	@Override
	public float getPrefHeight() {
		return this.getChildren().isEmpty() ? 0 : ((Button)this.getChild(0)).getPrefHeight();
	}
	
	@Override
	public void layout() {
		final float height = this.getHeight();
		float x = 0;
		for (Actor actor : this.getChildren()) {
			Button button = (Button)actor;
			float w = button.getPrefWidth();
			actor.setBounds(x, 0, w, height);
			x += w;
		}
	}
}
