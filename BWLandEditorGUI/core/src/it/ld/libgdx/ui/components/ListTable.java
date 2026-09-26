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

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.SnapshotArray;

import it.ld.libgdx.ui.events.BridgedEvent;

public class ListTable<E> extends WidgetGroup {
	private final Skin skin;
	
	private float cellPadTop = 1;
	private float cellPadLeft = 3;
	private float cellPadBottom = 1;
	private float cellPadRight = 5;
	private float scrollAmount = 24f;
	private float prefWidth = 0f;
	private float prefHeight = 0f;
	
	private String[] headerNames;
	private int[] alignments;
	
	private boolean headerVisible = false;
	private TableModel<E> model;
	
	private final Table headerTable;
    private final Table contentTable;
    private final ScrollPane scrollPane;
    private final ArrayList<Container<Actor>> fields = new ArrayList<>();
    private Actor extraRowActor;
    
    private Drawable headerBackground;
    private Drawable oddBackground;
    private Drawable evenBackground;
    private Drawable selectedBackground;
    
    private final Array<E> items = new Array<>();
    
    private ArraySelection<E> selection = new ArraySelection<>(items);
    
    private boolean buildingRows = false;
    
    public ListTable(Skin skin) {
        this.skin = skin;
        
        addListener(new InputListener() {
        	@Override
        	public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        		Stage stage = getStage();
				if (stage != null) {
					stage.setScrollFocus(scrollPane);
				}
        	}
        	
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				Stage stage = getStage();
				if (stage != null) {
					Actor focusedActor = stage.getKeyboardFocus();
					if (focusedActor == null || !ListTable.this.isAscendantOf(focusedActor)) {
						stage.setKeyboardFocus(ListTable.this);
					}
				}
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
					index = items.indexOf(getSelected(), true) + 1;
					if (index < items.size) setSelectedIndex(index);
					return true;
				case Keys.UP:
					index = items.indexOf(getSelected(), true) - 1;
					if (index < 0) index = 0;
					setSelectedIndex(index);
					return true;
				case Keys.PAGE_DOWN:
					if (items.size > 0) {
						int visibleRows = getVisibleRows();
						index = Math.min(items.indexOf(getSelected(), true) + visibleRows, items.size - 1);
						setSelectedIndex(index);
					}
					return true;
				case Keys.PAGE_UP:
					if (items.size > 0) {
						int visibleRows = getVisibleRows();
						index = Math.max(0, items.indexOf(getSelected(), true) - visibleRows);
						setSelectedIndex(index);
					}
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
				if (buildingRows) return;
				//Handle selection changed event
				for (int i = 0; i < items.size; i++) {
			    	E item = items.get(i);
			    	boolean isSelected = selection.contains(item);
		    		int offset = ((headerVisible ? 1 : 0) + i) * headerNames.length;
			        for (int col = 0; col < headerNames.length; col++) {
			    		@SuppressWarnings("unchecked")
						Container<Label> container = (Container<Label>)contentTable.getChild(offset + col);
			    		if (isSelected) {
			    			container.setBackground(selectedBackground, false);
			    		} else {
			    			container.setBackground((i & 1) == 0 ? evenBackground : oddBackground, false);
			    		}
			    	}
				}
				// Scroll to item
				if (selection.size() == 1) {
					int index = getSelectedIndex();
					scrollTo(index);
				}
			}
        });
        selection.setActor(this);
        
        headerBackground = skin.getDrawable("grey");
        selectedBackground = skin.getDrawable("selection");
        
        headerTable = new Table(skin);
        headerTable.top().left();
        
        contentTable = new Table(skin);
        contentTable.top().left();
        
        scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // no X, yes Y
        scrollPane.setOverscroll(false, false);
        scrollPane.setCancelTouchFocus(false);
        scrollPane.addCaptureListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                event.cancel();
            	scrollPane.setScrollY(scrollPane.getScrollY() + amountY * scrollAmount);
                return true;
            }
        });
        
        addActor(headerTable);
        addActor(scrollPane);
        
        setTransform(false);
        //
        setModel(new DefaultModel<E>(skin));
    }
    
    public void setExtraRow(Actor actor) {
    	if (actor != this.extraRowActor) {
    		this.extraRowActor = actor;
    		rebuildRows();
    	}
    }
    
	public ListTable<E> setModel(TableModel<E> model) {
    	this.model = model;
    	
    	this.headerNames = model.getHeaderNames();
        
    	this.alignments = new int[headerNames.length];
        for (int i = 0; i < alignments.length; i++) {
			alignments[i] = Align.left;
		}
        this.alignments = model.getAlignments(alignments);
        
        rebuildRows();
        refresh();
    	return this;
    }
    
    public TableModel<E> getModel() {
    	return this.model;
    }
    
    public ListTable<E> setCellPadding(float pad) {
        return setCellPadding(pad, pad, pad, pad);
    }
    
    public ListTable<E> setCellPadding(float top, float left, float bottom, float right) {
        this.cellPadTop = top;
        this.cellPadLeft = left;
        this.cellPadBottom = bottom;
        this.cellPadRight = right;
        refresh();
        return this;
    }
    
    public float getScrollAmount() {
    	return scrollAmount;
    }
    
    public void setScrollAmount(float amount) {
    	this.scrollAmount = amount;
    }
    
    public boolean isHeaderVisible() {
    	return headerVisible;
    }
    
    public ListTable<E> setHeaderVisible(boolean visible) {
    	if (visible != headerVisible) {
    		headerVisible = visible;
    		refresh();
    	}
    	return this;
    }
    
    public ListTable<E> setHeaderBackground(Drawable background) {
        this.headerBackground = background;
        rebuildHeader();
        return this;
    }
    
    public Drawable getHeaderBackground() {
    	return this.headerBackground;
    }
    
    public ListTable<E> setRowsBackground(Drawable odd, Drawable even) {
        this.oddBackground = odd;
        this.evenBackground = even;
        rebuildRows();
        return this;
    }
    
    public Drawable getOddBackground() {
    	return this.oddBackground;
    }
    
    public Drawable getEvenBackground() {
    	return this.evenBackground;
    }
    
    public int getVisibleRows() {
    	if (items.size == 0) return 0;
		return (int)(getHeight() / getField(0, 0).getHeight());
    }
    
    private int downRow = -1;
	private int downCol = -1;
	
	public int getTouchRow() {
		return downRow;
	}
	
	public int getTouchCol() {
		return downCol;
	}
    
    private final InputListener fieldListener = new InputListener() {
    	private final Vector2 tmpCoords = new Vector2();
    	
    	@Override
    	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
    		event.toCoordinates(contentTable, tmpCoords);
        	downRow = getItemIndexInContent(tmpCoords.y);
        	downCol = getColumnAt(tmpCoords.x);
    		return true;
    	};
    	
    	@Override
    	public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            event.toCoordinates(contentTable, tmpCoords);
        	int row = getItemIndexInContent(tmpCoords.y);
        	int col = getColumnAt(tmpCoords.x);
        	if (row == downRow && col == downCol) {
	        	E item = items.get(row);
	        	selection.choose(item);
	        	fire(new ItemClickEvent<E>(event, row, col, item));
        	}
        	downRow = -1;
        	downCol = -1;
        }
    };
    
    public SnapshotArray<E> getItems() {
		return new SnapshotArray<>(this.items);
	}
    
    public E getItem(int index) {
		return this.items.get(index);
	}
    
    public void setItems(Array<E> newItems) {
    	items.clear();
        items.addAll(newItems);
        //
        final int fieldsCount = items.size * headerNames.length;
        fields.clear();
        /*for (int fieldIndex = fields.size() - 1; fieldIndex >= fieldsCount; fieldIndex--) {
        	fields.remove(fieldIndex);
        }*/
        int row = 0;
        for (int fieldIndex = fields.size(); fieldIndex < fieldsCount; fieldIndex++) {
        	for (int col = 0; col < headerNames.length; col++) {
        		Actor actor = model.createField(row, col);
        		if (actor instanceof Label) {
        			((Label)actor).setAlignment(alignments[col]);
        		}
	    		Container<Actor> container = new Container<>(actor).fill();
	    		container.pad(cellPadTop, cellPadLeft, cellPadBottom, cellPadRight);
	    		container.addCaptureListener(fieldListener);
        		fields.add(container);
        	}
        	row++;
        }
        //
        selection.validate();
        refresh();
    }

    public void clearItems() {
        items.clear();
        selection.clear();
        refresh();
    }
    
    public void refresh() {
    	rebuildHeader();
    	rebuildRows();
    	updateHeadersWidth();
    	contentTable.invalidateHierarchy();
        scrollPane.layout();
    }
    
    public void refresh(E item) {
    	int row = items.indexOf(item, true);
    	if (row >= 0) {
    		refresh(row);
    	}
    }
    
    public void refresh(int row) {
    	rebuildRow(row);
    	updateHeadersWidth();
    	contentTable.invalidateHierarchy();
        scrollPane.layout();
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
		return selected.size == 0 ? -1 : items.indexOf(selected.first(), true);
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

    private void updateHeadersWidth() {
    	if (headerVisible) {
        	@SuppressWarnings("rawtypes")
			Array<Cell> cells = headerTable.getCells();
        	if (contentTable.getCells().size <= (extraRowActor == null ? 0 : 1)) {
        		for (int col = 0; col < headerNames.length; col++) {
	        		Cell<?> cell = cells.get(col);
	        		@SuppressWarnings("unchecked")
					Label label = ((Container<Label>)cell.getActor()).getActor();
        			cell.minWidth(label.getMinWidth() + 2);
        			cell.prefWidth(label.getPrefWidth() + 2);
        			cell.maxWidth(Float.POSITIVE_INFINITY);
	        	}
        	} else {
        		if (contentTable.needsLayout()) {
        			contentTable.layout();
        		}
	        	for (int col = 0; col < headerNames.length; col++) {
	        		cells.get(col).width(contentTable.getColumnWidth(col));
	        	}
        	}
        }
    }
    
    private void rebuildHeader() {
    	headerTable.clear();
    	if (headerVisible) {
	    	for (int col = 0; col < headerNames.length; col++) {
	    		String name = headerNames[col];
	    		Label label = new Label(name, skin);
	    		label.setAlignment(alignments[col]);
	    		Container<Label> container = new Container<>(label).fillX();
	    		container.setBackground(headerBackground, false);
	    		container.pad(cellPadTop, cellPadLeft, cellPadBottom, cellPadRight);
	    		headerTable.add(container).fillX();
	    	}
	    	Container<Label> filler = new Container<>(new Label("", skin));
	    	filler.setBackground(headerBackground);
	    	headerTable.add(filler).expand().fill();
    	}
    	updateHeadersWidth();
    }
    
    private void rebuildRows() {
    	buildingRows = true;
    	contentTable.clearChildren();
    	//Header
    	if (headerVisible) {
	    	Color transparent = new Color(0f, 0f, 0f, 0f);
    		for (int col = 0; col < headerNames.length; col++) {
	    		String name = headerNames[col];
	    		Label label = new Label(name, skin);
	    		label.setColor(transparent);
	    		label.setAlignment(alignments[col]);
	    		Container<Label> container = new Container<>(label).fillX().pad(cellPadTop, cellPadLeft, cellPadBottom, cellPadRight);
	    		contentTable.add(container).fillX().height(0f);
	    	}
	    	contentTable.row();
    	}
    	
    	@SuppressWarnings("unchecked")
		Cell<Container<Actor>>[] rowFields = (Cell<Container<Actor>>[]) new Cell<?>[headerNames.length];
    	int fieldIndex = 0;
        for (int row = 0; row < items.size; row++) {
            final E item = items.get(row);
            boolean isSelected = selection.contains(item);
            for (int col = 0; col < headerNames.length; col++, fieldIndex++) {
            	Container<Actor> container = fields.get(fieldIndex);
            	if (isSelected) {
            		container.setBackground(selectedBackground, false);
                } else {
                	container.setBackground((row & 1) == 0 ? evenBackground : oddBackground, false);
                }
            	rowFields[col] = contentTable.add(container).fill();
            }
            model.render(row, item, rowFields);
            contentTable.row();
        }
        
        if (extraRowActor != null) {
        	contentTable.add(extraRowActor).colspan(headerNames.length).fillX().row();
        }
        buildingRows = false;
    }
    
    private void rebuildRow(int row) {
    	if (row < 0 || row >= items.size) throw new ArrayIndexOutOfBoundsException();
    	@SuppressWarnings("unchecked")
		Cell<Container<Actor>>[] rowFields = (Cell<Container<Actor>>[]) new Cell<?>[headerNames.length];
    	@SuppressWarnings("rawtypes")
		Array<Cell> cells = contentTable.getCells();
    	int cellIndex = ((headerVisible ? 1 : 0) + row) * headerNames.length;
    	final E item = items.get(row);
    	boolean isSelected = selection.items().contains(item);
        for (int col = 0; col < headerNames.length; col++, cellIndex++) {
        	@SuppressWarnings("unchecked")
			Cell<Container<Actor>> cell = cells.get(cellIndex);
        	Container<Actor> container = cell.getActor();
        	if (isSelected) {
        		container.setBackground(selectedBackground, false);
            }
        	rowFields[col] = cell;
        }
        model.render(row, item, rowFields);
    }
    
    public float getColumnPrefWidth(int columnIndex) {
    	return contentTable.getColumnPrefWidth(columnIndex);
    }
    
    private int getItemIndexInContent(float y) {
    	int index = contentTable.getRow(y);
    	if (headerVisible) index--;
    	if (index < 0 || index >= items.size) return -1;
    	return index;
    }
    
    public int getItemIndex(Actor actor) {
    	while (actor != null) {
    		Actor parent = actor.getParent();
    		if (parent == contentTable) {
    			SnapshotArray<Actor> cells = contentTable.getChildren();
    			for (int i = 0; i < cells.size; i++) {
    				if (cells.get(i) == actor) {
    					int row = i / this.headerNames.length;
    					if (headerVisible) row--;
    					return row;
    				}
    			}
    		}
    		actor = parent;
    	}
    	return -1;
    }
    
    private final Vector2 tmpCoords = new Vector2();
    
    public Container<Actor> getField(int row, int col) {
    	if (row < 0 || col < 0 || row >= items.size || col >= headerNames.length) return null;
    	if (headerVisible) row++;
    	int index = row * headerNames.length + col;
    	return fields.get(index);
    }
    
    public int getItemIndexAt(float y) {
    	tmpCoords.set(0, y);
    	this.localToDescendantCoordinates(contentTable, tmpCoords);
    	return getItemIndexInContent(tmpCoords.y);
    }
    
    public int getColumnAt(float x) {
    	if (!headerVisible && items.size == 0) return 0;
    	@SuppressWarnings("rawtypes")
		Array<Cell> cells = contentTable.getCells();
    	for (int col = headerNames.length - 1; col >= 0; col--) {
    		if (x >= cells.get(col).getActor().getX()) return col;
    	}
    	return 0;
    }
    
    public void scrollTo(int row) {
    	int offset = ((headerVisible ? 1 : 0) + row) * headerNames.length;
    	Actor ref = contentTable.getChild(offset);
    	scrollPane.scrollTo(0, ref.getY() + ref.getHeight(), 0, ref.getHeight(), false, false);
    }
    
    public void setPrefWidth(float v) {
    	this.prefWidth = v;
    	invalidate();
    }
    
    public void setPrefHeight(float v) {
    	this.prefHeight = v;
    	invalidate();
    }
    
    @Override
    public float getPrefWidth() {
    	if (prefWidth > 0) return prefWidth;
    	return contentTable.getPrefWidth() + scrollPane.getScrollBarWidth();
    }
    
    @Override
    public float getPrefHeight() {
    	if (prefHeight > 0) return prefHeight;
    	return (headerVisible ? headerTable.getPrefHeight() : 0) + contentTable.getPrefHeight() + scrollPane.getScrollBarHeight();
    }
    
    @Override
    public float getMinWidth() {
    	return contentTable.getMinWidth() + scrollPane.getScrollBarWidth();
    }
    
    @Override
    public float getMinHeight() {
    	return 60f;
    }
    
    @Override
    public void layout() {
    	float w = getWidth();
        float h = getHeight();
        
        float headerH = headerTable.isVisible() ? headerTable.getPrefHeight() : 0f;
        
        if (headerTable.isVisible()) {
            headerTable.setBounds(0, h - headerH, w, headerH);
        } else {
            headerTable.setBounds(0, h, w, 0);
        }
        
        scrollPane.setBounds(0, 0, w, h - headerH);
        
        scrollPane.layout();
        updateHeadersWidth();
        headerTable.layout();
    }
    
    
    public interface TableModel<T> {
    	public String[] getHeaderNames();
    	public int[] getAlignments(int[] alignments);
    	public Actor createField(int row, int col);
    	public void render(int row, T item, Cell<Container<Actor>>[] fields);
    }
    
    
    public static class DefaultModel<T> implements TableModel<T> {
		private final Skin skin;
		
    	public DefaultModel(Skin skin) {
			this.skin = skin;
		}
    	
    	@Override
		public String[] getHeaderNames() {
			return new String[] {""};
		}
		
		@Override
		public int[] getAlignments(int[] alignments) {
			return alignments;
		}
		
		@Override
		public Actor createField(int row, int col) {
			return new Label("", skin);
		}
    	
    	@Override
		public void render(int row, T item, Cell<Container<Actor>>[] fields) {
			Actor actor = fields[0].getActor();
			if (actor instanceof Label) {
				((Label) actor).setText(String.valueOf(item));
			}
		}
    }
    
    
    public static abstract class ItemClickListener<T> implements EventListener {
    	@SuppressWarnings("unchecked")
		@Override
    	public boolean handle(Event event) {
    		if (!(event instanceof ItemClickEvent)) return false;
    		ItemClickEvent<T> evt = (ItemClickEvent<T>)event;
    		clicked(evt, evt.getRow(), evt.getCol(), evt.getItem());
    		return false;
    	}
    	
    	public abstract void clicked(ItemClickEvent<T> event, int row, int col, T item);
    }
    
    
    public static class ItemClickEvent<T> extends BridgedEvent<InputEvent> {
    	private final int row;
    	private final int col;
    	private final T item;
    	
    	public ItemClickEvent(InputEvent source, int row, int col, T item) {
			super(source);
			this.row = row;
			this.col = col;
			this.item = item;
		}
    	
    	public int getRow() {
    		return this.row;
    	}
    	
    	public int getCol() {
    		return this.col;
    	}
    	
    	public T getItem() {
    		return this.item;
    	}
    }
}