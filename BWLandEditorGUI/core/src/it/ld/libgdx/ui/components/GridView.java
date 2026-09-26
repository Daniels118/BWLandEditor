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

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SnapshotArray;

import it.ld.libgdx.ui.events.BridgedEvent;
import it.ld.utils.MathUtils;

public class GridView<E> extends WidgetGroup {
    private final Skin skin;
	private final Table content;
    private final ScrollPane scrollPane;

    private final Array<E> items = new Array<>();
    private GridModel<E> model = new DefaultGridModel<>();
    private ArraySelection<E> selection = new ArraySelection<>(items);
    
    private float iconWidth = 96f;
    private float iconHeight = 96f;
    private float labelHeight;
    private float pad = 4f;
    private int minCols = 1;
    private boolean textWrapEnabled = true;
    private boolean unwrapSelectedEnabled = true;

    private int lastCols = -1;
    private float lastWidth = -1f;
    
    private Drawable selectedBackground;

    public GridView(Skin skin) {
    	this.skin = skin;
    	selectedBackground = skin.getDrawable("selection");
    	labelHeight = skin.getFont("default-font").getLineHeight() + 1;
    	
    	addListener(new InputListener() {
    		@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (getStage() != null) getStage().setKeyboardFocus(GridView.this);
				return false;
			}
    		
    		public boolean keyDown (InputEvent event, int keycode) {
				if (items.isEmpty()) return false;
				int index;
				switch (keycode) {
				case Keys.A:
					if (UIUtils.ctrl() && selection.getMultiple()) {
						selection.clear();
						selection.addAll(items);
						return true;
					}
					break;
				case Keys.HOME:
					setSelectedIndex(0);
					return true;
				case Keys.END:
					setSelectedIndex(items.size - 1);
					return true;
				case Keys.DOWN:
					index = items.indexOf(getSelected(), false) + content.getColumns();
					if (index < items.size) setSelectedIndex(index);
					return true;
				case Keys.UP:
					index = items.indexOf(getSelected(), false) - content.getColumns();
					if (index < 0) index = 0;
					setSelectedIndex(index);
					return true;
				case Keys.RIGHT:
					index = items.indexOf(getSelected(), false) + 1;
					if (index < items.size) setSelectedIndex(index);
					return true;
				case Keys.LEFT:
					index = items.indexOf(getSelected(), false) - 1;
					if (index < 0) index = 0;
					setSelectedIndex(index);
					return true;
				case Keys.ESCAPE:
					if (getStage() != null) getStage().setKeyboardFocus(null);
					return true;
				}
				return false;
			}
		});
    	
    	this.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				//Handle selection changed event
				for (int i = 0; i < items.size; i++) {
			    	E item = items.get(i);
					@SuppressWarnings("unchecked")
					Container<Table> container = (Container<Table>) content.getChild(i);
					boolean isSelected = selection.contains(item);
			    	container.setBackground(isSelected ? selectedBackground : null, false);
			    	Label label = (Label) container.getActor().getChild(1);
			    	label.setEllipsis(!isSelected || !unwrapSelectedEnabled);
			    	label.setWrap(textWrapEnabled);
				}
				// Scroll to item
				if (selection.size() == 1) {
					int index = getSelectedIndex();
					Actor ref = content.getChild(index);
					scrollPane.scrollTo(0, ref.getY() + ref.getHeight(), 0, ref.getHeight(), false, false);
				}
			}
    	});
    	this.selection.setActor(this);
    	
    	content = new Table();
        content.top().left().defaults().pad(pad);

        scrollPane = new ScrollPane(content, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);	//only vertical
        scrollPane.setForceScroll(false, true);
        scrollPane.addListener(new InputListener() {
        	@Override
        	public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        		event.getStage().setScrollFocus(scrollPane);
        	}
        	
        	@Override
        	public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        		event.getStage().setScrollFocus(null);
        	}
        });
        
        setTouchable(Touchable.childrenOnly);
        addActor(scrollPane);
    }
    
    public void setForceScroll(boolean y) {
    	scrollPane.setForceScroll(false, y);
    }
    
    public GridModel<E> getModel() {
    	return this.model;
    }
    
    public GridView<E> setModel(GridModel<E> model) {
    	this.model = model;
    	rebuildIfNeeded(true, false);
    	return this;
    }
    
    public ArraySelection<E> getSelection() {
    	return this.selection;
    }
    
    public void setSelection(ArraySelection<E> selection) {
    	this.selection = selection;
    	this.selection.setActor(this);
    }
    
    public int getSelectedIndex() {
    	ObjectSet<E> selected = selection.items();
		return selected.size == 0 ? -1 : items.indexOf(selected.first(), false);
    }
    
    /** Sets the selection to only the selected index.
	 * @param index -1 to clear the selection. */
	public void setSelectedIndex(int index) {
		if (index < -1 || index >= items.size)
			throw new IllegalArgumentException("index must be >= -1 and < " + items.size + ": " + index);
		if (index == -1) {
			selection.clear();
		} else {
			selection.set(items.get(index));
		}
	}
    
    public E getSelected() {
    	return selection.first();
    }
    
    public void setSelected(E item) {
    	if (item == null) {
    		selection.clear();
    	} else {
    		selection.set(item);
    	}
    }
    
    public int getColumnAt(float x) {
    	if (items.size == 0) return 0;
    	@SuppressWarnings("rawtypes")
		Array<Cell> cells = content.getCells();
    	for (int col = 0; col < content.getColumns(); col++) {
    		Actor cell = cells.get(col).getActor();
    		if (x > cell.getX() && x < cell.getX() + cell.getWidth()) return col;
    	}
    	return -1;
    }
    
    private int getItemIndexInContent(float x, float y) {
    	int row = content.getRow(y);
    	if (row < 0) return -1;
    	int col = getColumnAt(x);
    	if (col < 0) return -1;
    	int index = row * content.getColumns() + col;
    	if (index >= items.size) return -1;
    	return index;
    }
    
    private final Vector2 tmpCoords = new Vector2();
    
    public int getItemIndexAt(float x, float y) {
    	tmpCoords.set(x, y);
    	this.localToDescendantCoordinates(content, tmpCoords);
    	return getItemIndexInContent(tmpCoords.x, tmpCoords.y);
    }
    
    public GridView<E> setIconSize(float size) {
        return setIconSize(size, size);
    }
    
    public GridView<E> setIconSize(float width, float height) {
        this.iconWidth = width;
        this.iconHeight = height;
        invalidateHierarchy();
        return this;
    }
    
    public float getIconWidth() {
    	return iconWidth;
    }
    
    public float getIconHeight() {
    	return iconHeight;
    }

    public GridView<E> setPadding(float pad) {
        this.pad = pad;
        content.defaults().pad(pad);
        invalidateHierarchy();
        return this;
    }
    
    public float getPadding() {
        return this.pad;
    }

    public GridView<E> setMinCols(int minCols) {
        this.minCols = Math.max(1, minCols);
        invalidateHierarchy();
        return this;
    }
    
    public int getMinCols() {
        return this.minCols;
    }
    
    public boolean isTextWrapEnabled() {
		return textWrapEnabled;
	}

	public void setTextWrapEnabled(boolean textWrapEnabled) {
		this.textWrapEnabled = textWrapEnabled;
		rebuildIfNeeded(true, true);
	}

	public boolean isUnwrapSelectedEnabled() {
		return unwrapSelectedEnabled;
	}

	public void setUnwrapSelectedEnabled(boolean unwrapSelectedEnabled) {
		this.unwrapSelectedEnabled = unwrapSelectedEnabled;
		rebuildIfNeeded(true, true);
	}
    
	public SnapshotArray<E> getItems() {
		return new SnapshotArray<>(this.items);
	}
	
	public E getItem(int index) {
		return this.items.get(index);
	}
	
    public void setItems(Array<E> newItems) {
    	this.items.clear();
        if (newItems != null) items.addAll(newItems);
        //
        selection.validate();
        lastCols = -1;	//force rebuild
        rebuildIfNeeded(true, false);
    }

    public void clearItems() {
        items.clear();
        lastCols = -1;
        rebuildIfNeeded(true, false);
    }
    
    public Container<Actor> getContainer(E item) {
    	int index = items.indexOf(item, true);
    	return getContainer(index);
    }
    
    public Image getImage(E item) {
    	int index = items.indexOf(item, true);
    	return getImage(index);
    }
    
    public Label getLabel(E item) {
    	int index = items.indexOf(item, true);
    	return getLabel(index);
    }
    
    @SuppressWarnings("unchecked")
	public Container<Actor> getContainer(int index) {
    	if (index < 0 || index >= items.size) return null;
    	return (Container<Actor>) content.getChild(index);
    }
    
    @SuppressWarnings("unchecked")
	public Image getImage(int index) {
    	if (index < 0 || index >= items.size) return null;
    	Container<Table> container = (Container<Table>) content.getChild(index);
    	return (Image) container.getActor().getChild(0);
    }
    
    @SuppressWarnings("unchecked")
    public Label getLabel(int index) {
    	if (index < 0 || index >= items.size) return null;
    	Container<Table> container = (Container<Table>) content.getChild(index);
    	return (Label) container.getActor().getChild(1);
    }
    
    public void refresh() {
    	rebuildIfNeeded(true, true);
    }
    
    @Override
    public void layout() {
        scrollPane.setBounds(0, 0, getWidth(), getHeight());
        rebuildIfNeeded(false, true);
    }
    
    private float getHSpacing() {
    	return iconWidth + pad * 2;
    }
    
    private float getVSpacing() {
    	return iconHeight + labelHeight + pad * 2;
    }
    
    @Override
    public float getMinWidth() {
    	return content.getPadLeft() + getHSpacing() + pad + content.getPadRight() + scrollPane.getScrollBarWidth() + 28f;
    }
    
    @Override
    public float getMinHeight() {
    	return content.getPadTop() + getVSpacing() + content.getPadBottom() + 12f;
    }
    
    private int getPreferredCols() {
    	return Math.max(minCols, (int)Math.ceil(Math.sqrt(items.size) * 4 / 3));
    }
    
    @Override
    public float getPrefWidth() {
    	if (items.isEmpty()) return getMinWidth();
    	int cols = getPreferredCols();
        return content.getPadLeft() + getHSpacing() * cols + pad + content.getPadRight() + scrollPane.getScrollBarWidth() + 28f;
    }

    @Override
    public float getPrefHeight() {
    	if (items.isEmpty()) return getMinHeight();
    	int cols = getPreferredCols();
    	int rows = MathUtils.ceilDiv(items.size, cols);
        return (iconHeight + labelHeight + pad * 2) * rows + 12f;
    }
    
    private void rebuildIfNeeded(boolean force, boolean reuseItems) {
        float w = scrollPane.getWidth();
        if (w <= 1f) return; //too early
        if (!force && Math.abs(w - lastWidth) < 0.5f) return;	//Avoid micro resize
        lastWidth = w;
        
        float step = iconWidth + pad * 2f;
        int cols = Math.max(minCols, (int) (w / step));
        if (!force && cols == lastCols) return;
        
        lastCols = cols;
        
        Array<Actor> oldElements = reuseItems ? new Array<>(content.getChildren()) : null;
        
        content.clearChildren();
        
        ObjectSet<E> selected = selection.items();
        for (int i = 0; i < items.size; i++) {
            E item = items.get(i);
        	Container<?> element = reuseItems && i < oldElements.size ? ((Container<?>)oldElements.get(i)) : createElement();
        	updateElement(item, i, element);
            if (selected.contains(item)) {
            	element.setBackground(selectedBackground, false);
        	}
            content.add(element).width(iconWidth).minHeight(iconHeight).top();
            if ((i + 1) % cols == 0) content.row();
        }
        
        content.invalidateHierarchy();
    }
    
    private State state = new State();
    
    private Container<?> createElement() {
        Table table = new Table();
    	
    	Container<Actor> container = new Container<>(table);
    	container.pad(Math.max(2, selectedBackground.getTopHeight()),
    				  Math.max(2, selectedBackground.getLeftWidth()),
    				  Math.max(2, selectedBackground.getBottomHeight()),
    				  Math.max(2, selectedBackground.getRightWidth()));
    	
    	Image image = new Image();
        image.setAlign(Align.center);
        image.setScaling(Scaling.contain);
        image.setSize(iconWidth, iconHeight);
        table.add(image).size(iconWidth, iconHeight).row();
        
        Label label = new Label("", skin);
        label.setAlignment(Align.center);
        label.setWrap(textWrapEnabled);
        label.setEllipsis(true);
        table.add(label).width(iconWidth).fillX();
        
        return container;
    }
    
    @SuppressWarnings("unchecked")
	private void updateElement(E item, int index, Container<?> container) {
    	Table table = (Table) container.getActor();
    	Image image = (Image) table.getChild(0);
    	Label label = (Label) table.getChild(1);
    	
    	state.disabled = false;
        model.render(index, item, (Container<Actor>)container, image, label, state);
        
        if (state.disabled) {
        	container.setColor(1f, 1f, 1f, 0.3f);
        } else {
        	container.addListener(inputListener);
        }
    }

	private final InputListener inputListener = new InputListener() {
    	@Override
    	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
    		getStage().setScrollFocus(scrollPane);
    		return true;
    	};
    	
        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
    		Actor actor = event.getTarget();
    		while (actor.getParent() != content) {
    			actor = actor.getParent();
    		}
        	int index = content.getChildren().indexOf(actor, true);
    		E item = items.get(index);
        	selection.choose(item);
    		fire(new ItemClickEvent<E>(event, index, item));
        }
    };
    
    
    public static class State {
    	private boolean disabled = false;
    	
    	public void setDisabled(boolean disabled) {
    		this.disabled = disabled;
    	}
    }
    
    
    public static interface GridModel<T> {
		public void render(int index, T item, Container<Actor> container, Image image, Label label, State state);
	}
    
    
    public static class DefaultGridModel<T> implements GridModel<T> {
		public void render(int index, T item, Container<Actor> container, Image image, Label label, State state) {
			label.setText(String.valueOf(item));
		}
	}
    
    
    public static abstract class ItemClickListener<T> implements EventListener {
    	@SuppressWarnings("unchecked")
		@Override
    	public boolean handle(Event event) {
    		if (!(event instanceof ItemClickEvent)) return false;
    		ItemClickEvent<T> evt = (ItemClickEvent<T>)event;
    		clicked(evt, evt.getIndex(), evt.getItem());
    		return false;
    	}
    	
    	public abstract void clicked(ItemClickEvent<T> event, int index, T item);
    }
    
    
    public static class ItemClickEvent<T> extends BridgedEvent<InputEvent> {
    	private final int index;
    	private final T item;
    	
    	public ItemClickEvent(InputEvent source, int index, T item) {
			super(source);
			this.index = index;
			this.item = item;
		}
    	
    	public int getIndex() {
    		return this.index;
    	}
    	
    	public T getItem() {
    		return this.item;
    	}
    }
}