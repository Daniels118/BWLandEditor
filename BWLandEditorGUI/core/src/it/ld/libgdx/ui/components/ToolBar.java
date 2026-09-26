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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SnapshotArray;

import it.ld.utils.MathUtils;

public class ToolBar extends WidgetGroup {
	private Skin skin;
	private float buttonSize;
	private float groupSpacing = 4f;
	
	private boolean horizontal = true;
	private float maxWidth = 0;
	private boolean disabled = false;
	
	private ButtonsGroup currentGroup;
	
	public ToolBar(Skin skin, float buttonSize, boolean horizontal) {
		this.skin = skin;
		this.buttonSize = buttonSize;
		this.horizontal = horizontal;
		currentGroup = new ButtonsGroup(skin, buttonSize, horizontal);
		addActor(currentGroup);
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
		for (Actor actor : this.getChildren()) {
			ButtonsGroup group = (ButtonsGroup)actor;
			group.setDisabled(disabled);
		}
	}
	
	public void setHorizontal(boolean value) {
		if (this.horizontal != value) {
			this.horizontal = value;
			for (Actor actor : this.getChildren()) {
				((ButtonsGroup)actor).setHorizontal(value);
			}
			this.invalidateHierarchy();
		}
	}
	
	public boolean isHorizontal() {
		return this.horizontal;
	}
	
	public void setGroupSpacing(float value) {
		if (value != this.groupSpacing) {
			this.groupSpacing = value;
			this.invalidate();
		}
	}
	
	public Button add(String iconName, Runnable action) {
	    return add(iconName, null, true, action, (StateChecker)null);
	}
	
	public Button add(String iconName, String tooltip, Runnable action) {
	    return add(iconName, tooltip, true, action, (StateChecker)null);
	}
	
	public Button add(String iconName, boolean enabled, Runnable action) {
	    return add(iconName, null, enabled, action, (StateChecker)null);
	}
	
	public Button add(String iconName, String tooltip, boolean enabled, Runnable action) {
		return add(iconName, tooltip, enabled, action, (StateChecker)null);
	}
	
	public Button add(String iconName, String tooltip, Runnable action, StateChecker state) {
		return add(iconName, tooltip, true, action, state);
	}
	
	public Button add(String iconName, String tooltip, Runnable action, MultiState state, Color[] stateColors) {
		return add(iconName, tooltip, true, action, state, stateColors);
	}
	
	public Button add(String iconName, String tooltip, boolean enabled, Runnable action, StateChecker state) {
	    Button button = currentGroup.add(iconName, action, state);
	    button.setDisabled(!enabled);
	    if (tooltip != null && !tooltip.isEmpty()) {
	    	button.addListener(new TextTooltip(tooltip, skin));
	    }
	    return button;
	}
	
	public Button add(String iconName, String tooltip, boolean enabled, Runnable action, MultiState state, Color[] stateColors) {
	    Button button = currentGroup.add(iconName, action, state, stateColors);
	    button.setDisabled(!enabled);
	    if (tooltip != null && !tooltip.isEmpty()) {
	    	button.addListener(new TextTooltip(tooltip, skin));
	    }
	    return button;
	}
	
	public void add(Actor actor) {
		currentGroup.addActor(actor);
	}
	
	public void add(Actor actor, float width) {
		currentGroup.addActor(new Cell(actor, width));
	}
	
	public void setTooltip(Button button, String text) {
		for (EventListener listener : button.getListeners()) {
			if (listener instanceof TextTooltip) {
				TextTooltip tooltip = (TextTooltip)listener;
				button.removeListener(tooltip);
				tooltip.hide();
				break;
			}
		}
		if (text != null && !text.isEmpty()) {
			button.addListener(new TextTooltip(text, skin));
		}
	}
	
	public ButtonsGroup getCurrentGroup() {
		return this.currentGroup;
	}
	
	public ButtonsGroup addGroup() {
		this.currentGroup = new ButtonsGroup(skin, buttonSize, horizontal);
		this.addActor(currentGroup);
		return this.currentGroup;
	}
	
	public ButtonsGroup addFiller() {
		this.addActor(new ButtonsGroup(skin, 0, horizontal));
		return this.addGroup();
	}
	
	@Override
	public float getMinWidth() {
		return buttonSize;
	}
	
	public void setMaxWidth(float maxWidth) {
		this.maxWidth = maxWidth;
	}
	
	@Override
	public float getMaxWidth() {
		return this.maxWidth;
	}
	
	@Override
	public float getWidth() {
		float width = super.getWidth();
		if (maxWidth > 0 && width > maxWidth) width = maxWidth;
		return width;
	}
	
	@Override
	public float getPrefWidth() {
		if (horizontal) {
			return 0;
		}
		return buttonSize;
	}
	
	private float getAvailableWidth() {
		Actor parent = this.getParent();
		while (parent != null && parent.getWidth() <= 0) {
			parent = parent.getParent();
		}
		if (parent != null) {
			return parent.getWidth();
		}
		return 0;
	}
	
	@Override
	public float getMinHeight() {
		if (horizontal) {
			final float width = getAvailableWidth();
			float x = 0;
			float y = 0;
			SnapshotArray<Actor> children = this.getChildren();
			int fillerIndex = children.size;
			float prevh = 0;
			for (int i = 0; i < children.size; i++) {
				Actor actor = children.get(i);
				ButtonsGroup group = (ButtonsGroup)actor;
				if (group.buttonSize == 0) {
					fillerIndex = i;
					continue;
				}
				float w = group.getPrefWidth();
				float h = group.getPrefHeight();
				if (x > 0 && x + w > width) {
					x = 0;
					y += prevh;
					if (!horizontal) y += groupSpacing;
					if (fillerIndex < children.size) fillerIndex = i - 1;
				}
				x += w;
				if (horizontal) x += groupSpacing;
				prevh = h;
			}
			return y + prevh;
		}
		return buttonSize;
	}
	
	@Override
	public float getPrefHeight() {
		return buttonSize;
	}
	
	@Override
	public void layout() {
		final float width = Math.min(this.getWidth(), getAvailableWidth());
		final float height = this.getHeight();
		float x = 0;
		float y = height;
		SnapshotArray<Actor> children = this.getChildren();
		int fillerIndex = children.size;
		float prevh = 0;
		for (int i = 0; i < children.size; i++) {
			Actor actor = children.get(i);
			ButtonsGroup group = (ButtonsGroup)actor;
			if (group.buttonSize == 0) {
				fillerIndex = i;
				continue;
			}
			float w = group.getPrefWidth();
			float h = group.getPrefHeight();
			if (x > 0 && x + w > width) {
				x = 0;
				y -= prevh;
				if (!horizontal) y -= groupSpacing;
				if (fillerIndex < children.size) fillerIndex = i - 1;
			}
			actor.setBounds(x, y - h, w, h);
			x += w;
			if (horizontal) x += groupSpacing;
			prevh = h;
		}
		final float shift = horizontal ? (width - x) : y;
		if (shift > 0) {
			for (int i = fillerIndex + 1; i < children.size; i++) {
				Actor actor = children.get(i);
				if (horizontal) {
					actor.setX(actor.getX() + shift);
				} else {
					actor.setY(actor.getY() - shift);
				}
			}
		}
	}
	
	
	public static class ButtonsGroup extends WidgetGroup {
		private final Skin skin;
		private float buttonSize;
		private boolean horizontal;
		private boolean disabled = false;
		
		public ButtonsGroup(Skin skin, float buttonSize, boolean horizontal) {
			this.skin = skin;
			this.buttonSize = buttonSize;
			this.horizontal = horizontal;
		}
		
		public boolean isDisabled() {
			return disabled;
		}
		
		public void setDisabled(boolean disabled) {
			this.disabled = disabled;
			for (Actor actor : this.getChildren()) {
				TButton button = (TButton)actor;
				button.refresh();
			}
		}
		
		private void setHorizontal(boolean value) {
			this.horizontal = value;
		}
		
		public TButton add(String iconName, Runnable action, StateChecker state) {
		    TButton button = new TButton(skin, this, iconName, state, action);
		    this.addActor(button);
			return button;
		}
		
		public TButton add(String iconName, Runnable action, MultiState state, Color[] stateColors) {
			TButton button = new TButton(skin, this, iconName, state, stateColors, action);
		    this.addActor(button);
			return button;
		}
		
		@Override
		public float getMinWidth() {
			return horizontal ? getPrefWidth() : buttonSize;
		}
		
		@Override
		public float getPrefWidth() {
			if (!horizontal) return buttonSize;
			float r = 0f;
			for (Actor actor : getChildren()) {
				float w = buttonSize;
				if (actor instanceof TButton) {
					w = buttonSize;
				} else if (actor instanceof Layout) {
					w = ((Layout)actor).getPrefWidth();
				}
				if (w == 0) w = buttonSize;
				r += w;
			}
			return r;
		}
		
		@Override
		public float getMaxWidth() {
			return horizontal ? getPrefWidth() : 0;
		}
		
		@Override
		public float getMinHeight() {
			return buttonSize;
		}
		
		@Override
		public float getPrefHeight() {
			return horizontal ? buttonSize : (this.getChildren().size * buttonSize);
		}
		
		public int getColsFor(float width) {
			return buttonSize != 0 ? Math.max(1, (int) Math.floor(width / buttonSize)) : 1;
		}
		
		public int getRowsFor(float width) {
			final int cols = getColsFor(width);
			return MathUtils.ceilDiv(this.getChildren().size, cols);
		}
		
		@Override
		public void layout() {
			final float width = this.getWidth();
			final float height = this.getHeight();
			final int cols = getColsFor(width);
			final int rows = getRowsFor(width);
			final float buttonWidth = rows == 1 ? buttonSize : width / cols;
			final float buttonHeight = cols == 1 ? buttonSize : height / rows;
			float x = 0;
			float y = height - buttonHeight;
			for (Actor actor : this.getChildren()) {
				float w = buttonWidth;
				float h = buttonHeight;
				if (actor instanceof TButton) {
					//
				} else if (actor instanceof Layout) {
					w = ((Layout)actor).getPrefWidth();
					h = ((Layout)actor).getPrefHeight();
				}
				if (w == 0) w = buttonWidth;
				if (h == 0) h = buttonHeight;
				if (!horizontal && x > 0 && x + w > width) {
					x = 0;
					y -= buttonHeight;
				}
				actor.setBounds(x, y + (buttonHeight - h) / 2, w, h);
				x += w;
			}
		}
	}
	
	private static class TButton extends Button {
		private static final Color DISABLED_COLOR = new Color(1f, 1f, 1f, 0.3f);
		
		private final ButtonsGroup group;
		private final StateChecker state;
		private final MultiState multistate;
		private final Color[] stateColors;
		private final Image img;
		
		public TButton(Skin skin, ButtonsGroup group, String icon, StateChecker state, Runnable action) {
			this(skin, group, icon, state, null, null, action);
		}
		
		public TButton(Skin skin, ButtonsGroup group, String icon, MultiState multistate, Color[] stateColors, Runnable action) {
			this(skin, group, icon, null, multistate, stateColors, action);
		}
		
		public TButton(Skin skin, ButtonsGroup group, String icon, StateChecker state, MultiState multistate, Color[] stateColors, Runnable action) {
			super(new Image(skin.getDrawable(icon)), skin, "toggle");
			this.group = group;
			this.state = state;
			this.multistate = multistate;
			this.stateColors = stateColors;
			img = (Image)this.getChild(0);
			img.setScaling(Scaling.fit);
			this.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if (isDisabled()) {
						event.cancel();
					} else {
			    		event.handle();
			    		action.run();
		    		}
				}
			});
		}
		
		public void refresh() {
			img.setColor(isDisabled() ? DISABLED_COLOR : Color.WHITE);
		}
		
		@Override
		public void setDisabled(boolean isDisabled) {
			super.setDisabled(isDisabled);
			refresh();
		}
		
		@Override
		public boolean isDisabled() {
			return super.isDisabled() || (group != null && group.disabled);
		}
		
		@Override
		public boolean isChecked() {
			if (state != null) {
				return state.isChecked();
			}
			if (multistate != null) {
				return multistate.getState() != 0;
			}
			return false;
		}
		
		@Override
		protected Drawable getBackgroundDrawable() {
			if (isDisabled()) {
				return getStyle().up;
			}
			return super.getBackgroundDrawable();
		}
		
		@Override
		public Color getColor() {
			if (multistate == null || multistate.getState() == 0) return Color.WHITE;
			return stateColors[MathUtils.clamp(multistate.getState() - 1, 0, stateColors.length - 1)];
		}
	}
	
	private static class Cell extends WidgetGroup {
		private float width;
		
		public Cell(Actor actor, float width) {
			this.addActor(actor);
			this.width = width;
		}
		
		@Override
		public float getMinWidth() {
			return width;
		}
		
		@Override
		public float getMaxWidth() {
			return width;
		}
		
		@Override
		public float getPrefWidth() {
			return width;
		}
		
		@Override
		public void layout() {
			Actor actor = this.getChild(0);
			float h = getHeight();
			if (actor instanceof Layout) h = ((Layout)actor).getPrefHeight();
			if (h == 0) h = getHeight();
			actor.setBounds(0f, (getHeight() - h) / 2, getWidth(), h);
		}
	}
	
	
	public interface StateChecker {
    	public boolean isChecked();
    }
	
	public interface MultiState {
    	public int getState();
    }
}
