package it.ld.libgdx.ui.docking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.events.RemoveEvent;

public class DockContainer extends Table {
    private final String id;
    private final int align;
    private boolean vertical;
    private final Table content = new Table();
    private final ContainerResizeHandle resizeHandle;
    
    private final List<Float> sizes = new ArrayList<>();
    private float splitterSize = 4f;
    private float minWindowSize = 80f;
    
    private final Map<SmartWindow, WinAttr> windowsAttributes = new HashMap<>();
    private final List<SmartWindow> windows = new ArrayList<>();
	
    public DockContainer(String id, int align) {
        this.id = id;
        this.align = align;
        this.vertical = align == Align.left || align == Align.right;
        content.defaults().grow().top().left();
        
        resizeHandle = new ContainerResizeHandle(this);
        Cell<?> handleCell = null;
        if (align == Align.right || align == Align.bottom) {
        	handleCell = add(resizeHandle);
        	if (!vertical) row();
        }
        add(content).grow();
        if (align == Align.left || align == Align.top) {
        	if (!vertical) row();
        	handleCell = add(resizeHandle);
        }
        if (vertical) {
        	handleCell.growY().minWidth(splitterSize).maxWidth(splitterSize).width(splitterSize);
        } else {
        	handleCell.growX().minHeight(splitterSize).maxHeight(splitterSize).height(splitterSize);
        }
    }

    public String getId() {
        return id;
    }
    
    public int getAlign() {
    	return align;
    }
    
    public boolean isVertical() {
    	return vertical;
    }
    
    public boolean isEmpty() {
        return windows.isEmpty();
    }

    public List<SmartWindow> getWindows() {
        return windows;
    }

    public boolean canDock(SmartWindow window) {
        return window != null;
    }
    
    public static boolean isDocked(SmartWindow window) {
    	return window.getParent() != null && window.getParent().getParent() instanceof DockContainer;
    }
    
    public static DockContainer getContainer(SmartWindow window) {
    	return (DockContainer) window.getParent().getParent();
    }
    
    private boolean docking = false;
    private boolean rebuilding = false;
    private boolean needsRescaling = true;
    
    public void dock(SmartWindow window) {
    	dock(window, 999);
    }
    
    public void dock(SmartWindow window, int index) {
        if (!canDock(window)) return;
        docking = true;
        if (isDocked(window)) {
            getContainer(window).undock(window, 0, 0);
        } else if (window.getParent() != null) {
        	window.getParent().removeActor(window);
        }
        if (!windowsAttributes.containsKey(window)) {
            windowsAttributes.put(window, new WinAttr(window));
            index = Math.min(index, windows.size());
            windows.add(index, window);
            
            float preferredSize = vertical ? window.getHeight() : window.getWidth();
            if (preferredSize <= 0) preferredSize = 180f;
            sizes.add(index, preferredSize);
            
            window.setResizable(false);
            window.setMaxSize(Float.MAX_VALUE, Float.MAX_VALUE);
            window.addListener(closeListener);
        }
        needsRescaling = true;
        rebuild();
        docking = false;
    }

    public boolean undock(SmartWindow window, float mouseX, float mouseY) {
    	docking = true;
        WinAttr attr = windowsAttributes.remove(window);
        if (attr != null) {
        	int index = windows.indexOf(window);
        	windows.remove(index);
        	sizes.remove(index);
        	Vector2 pos = window.localToStageCoordinates(new Vector2(0, 0));
        	pos.y = pos.y - (attr.height - window.getHeight());
        	if (mouseX > pos.x + (attr.width - 40)) {
        		pos.x = mouseX - (attr.width - 40);
        	}
            getStage().addActor(window);
            window.removeListener(closeListener);
            needsRescaling = true;
            rebuild();
            window.setResizable(attr.resizable);
            window.setMovable(attr.moveable);
            window.setMinSize(attr.minWidth, attr.minHeight);
            window.setMaxSize(attr.maxWidth, attr.maxHeight);
            window.setSize(attr.width, attr.height);
            window.setPosition(pos.x, pos.y);
        }
        docking = false;
        return attr != null;
    }
    
    private final EventListener closeListener = new EventListener() {
    	@Override
    	public boolean handle(Event event) {
    		if (event instanceof RemoveEvent) {
    			if (docking || rebuilding) {
    				event.cancel();
    			} else {
    				Actor window = event.getTarget();
    				int index = windows.indexOf(window);
    				windowsAttributes.remove(window);
    				windows.remove(window);
    				sizes.remove(index);
    				window.removeListener(closeListener);
    				needsRescaling = true;
    				Gdx.app.postRunnable(() -> rebuild());
    			}
    			return true;
    		}
    		return false;
    	}
    };
    
    public SmartWindow getWindow(float stageX, float stageY) {
    	Vector2 pos = content.stageToLocalCoordinates(new Vector2(stageX, stageY));
    	for (SmartWindow window : windows) {
    		if (pos.x >= window.getX() && pos.x < window.getX() + window.getWidth() &&
    				pos.y >= window.getY() && pos.y < window.getY() + window.getHeight()) {
    			return window;
    		}
    	}
    	return null;
    }
    
    public SmartWindow getWindow(int index) {
    	if (index < 0 || index >= windows.size()) return null;
    	return windows.get(index);
    }
    
    public SmartWindow getLastWindow() {
    	return windows.isEmpty() ? null : windows.get(windows.size() - 1);
    }
    
    public int getIndex(SmartWindow window) {
    	return content.getChildren().indexOf(window, true);
    }
    
    private void rescale() {
    	if (!needsRescaling) return;
    	if (windows.isEmpty()) return;
    	final float containerSize = (vertical ? getHeight() : getWidth()) - splitterSize;
    	final float splittersSize = (windows.size() - 1) * splitterSize;
    	float totalCurrentSize = 0;
    	float shrinkableSize = 0;
    	float expandableSize = 0;
    	float nonShrinkableSize = 0;
    	float nonExpandableSize = 0;
    	for (int i = 0; i < windows.size(); i++) {
        	SmartWindow window = windows.get(i);
        	float size = sizes.get(i);
        	float minSize = Math.max(0f, vertical ? window.getMinHeight() : window.getMinWidth());
        	float maxSize = Math.max(0f, vertical ? window.getMaxHeight() : window.getMaxWidth());
        	totalCurrentSize += size;
        	if (size > minSize) {
        		shrinkableSize += size;
        	} else {
        		nonShrinkableSize += size;
        	}
        	if (size < maxSize) {
        		expandableSize += size;
        	} else {
        		nonExpandableSize += size;
        	}
        }
        for (int j = 0; j < 1; j++) {
        	float scale;
        	if (totalCurrentSize < containerSize) {
        		if (expandableSize == 0) break;
        		scale = (containerSize - splittersSize - nonExpandableSize) / expandableSize;
        	} else {
        		if (shrinkableSize == 0) break;
        		scale = (containerSize - splittersSize - nonShrinkableSize) / shrinkableSize;
        	}
	        totalCurrentSize = 0;
	        shrinkableSize = 0;
	    	expandableSize = 0;
	        for (int i = 0; i < windows.size(); i++) {
	        	SmartWindow window = windows.get(i);
	        	float minSize = Math.max(0f, vertical ? window.getMinHeight() : window.getMinWidth());
	        	float maxSize = Math.max(0f, vertical ? window.getMaxHeight() : window.getMaxWidth());
	        	float size = MathUtils.clamp(sizes.get(i) * scale, minSize, maxSize);
	        	totalCurrentSize += size;
	        	if (size > minSize) {
	        		shrinkableSize += size;
	        	} else {
	        		nonShrinkableSize += size;
	        	}
	        	if (size < maxSize) {
	        		expandableSize += size;
	        	} else {
	        		nonExpandableSize += size;
	        	}
	        	sizes.set(i, size);
	        }
        }
        needsRescaling = false;
    }

    private void rebuild() {
    	rebuilding = true;
    	content.clearChildren();
    	rescale();
        for (int i = 0; i < windows.size(); i++) {
            SmartWindow window = windows.get(i);
            if (vertical) {
            	content.add(window).growX().prefHeight(sizes.get(i)).row();
                if (i < windows.size() - 1) {
                	content.add(new WindowResizeHandle(this, i))
                        .growX().minHeight(splitterSize).maxHeight(splitterSize).height(splitterSize)
                        .row();
                }
            } else {
            	content.add(window).growY().prefWidth(sizes.get(i));
                if (i < windows.size() - 1) {
                	content.add(new WindowResizeHandle(this, i))
                        .growY().minWidth(splitterSize).maxWidth(splitterSize).width(splitterSize);
                }
            }
        }
        if (windows.size() <= 1) {
    		resizeHandle.reset();
    	}
        content.invalidateHierarchy();
        rebuilding = false;
    }
    
    public void resizeBetween(int index, float delta) {
        if (index < 0 || index >= sizes.size() - 1) return;
        rescale();

        float a = sizes.get(index);
        float b = sizes.get(index + 1);
        
        if (vertical) delta = -delta;
        
        float newA = a + delta;
        float newB = b - delta;

        if (newA < minWindowSize) {
            float diff = minWindowSize - newA;
            newA += diff;
            newB -= diff;
        }

        if (newB < minWindowSize) {
            float diff = minWindowSize - newB;
            newB += diff;
            newA -= diff;
        }

        if (newA < minWindowSize || newB < minWindowSize) return;

        sizes.set(index, newA);
        sizes.set(index + 1, newB);
        
        @SuppressWarnings("rawtypes")
		Array<Cell> cells = content.getCells();
        if (vertical) {
        	cells.get(index * 2).prefHeight(newA);
        	cells.get(index * 2 + 2).prefHeight(newB);
        } else {
        	cells.get(index * 2).prefWidth(newA);
        	cells.get(index * 2 + 2).prefWidth(newB);
        }
        
        content.invalidate();
        this.invalidate();
    }
    
    @Override
    public void validate() {
    	if (vertical) {
    		if (content.getWidth() < content.getMinWidth()) {
    			resizeHandle.reset();
    		}
    	} else {
    		if (content.getHeight() < content.getMinHeight()) {
    			resizeHandle.reset();
    		}
    	}
    	super.validate();
    }
    
    @Override
    protected void sizeChanged() {
    	needsRescaling = true;
    	super.sizeChanged();
    }
    
    @Override
    public String toString() {
    	return id;
    }
    
    
    private static class WinAttr {
    	public final boolean resizable;
    	public final boolean moveable;
    	public final float width;
    	public final float height;
    	public final float minWidth;
    	public final float minHeight;
    	public final float maxWidth;
    	public final float maxHeight;
    	
    	public WinAttr(SmartWindow window) {
    		resizable = window.isResizable();
			moveable = window.isMovable();
			width = window.getWidth();
			height = window.getHeight();
			minWidth = window.getMinWidth();
			minHeight = window.getMinHeight();
			maxWidth = window.getMaxWidth();
			maxHeight = window.getMaxHeight();
		}
    }
    
    
    private static class WindowResizeHandle extends Widget {
        private float startX;
        private float startY;

        public WindowResizeHandle(DockContainer container, int index) {
        	final boolean vertical = container.isVertical();
        	
            addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                	Gdx.graphics.setSystemCursor(vertical ? SystemCursor.VerticalResize : SystemCursor.HorizontalResize);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
                }

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (button != Input.Buttons.LEFT) return false;
                    startX = event.getStageX();
                    startY = event.getStageY();
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    float delta = vertical ? event.getStageY() - startY : event.getStageX() - startX;
                    container.resizeBetween(index, delta);
                    Gdx.graphics.setSystemCursor(vertical ? SystemCursor.VerticalResize : SystemCursor.HorizontalResize);
                    startX = event.getStageX();
                    startY = event.getStageY();
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
                }
            });
        }
    }
    
    
    public class ContainerResizeHandle extends Widget {
        private final DockContainer container;
    	private float startX;
        private float startY;
        
        public ContainerResizeHandle(DockContainer container) {
        	this.container = container;
        	final int align = container.getAlign();
        	final boolean vertical = container.isVertical();
            
            addListener(new InputListener() {
            	private Table table;
            	private Cell<?> cell;
            	
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                	Gdx.graphics.setSystemCursor(vertical ? SystemCursor.HorizontalResize : SystemCursor.VerticalResize);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
                }

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (button != Input.Buttons.LEFT) return false;
                    if (!(container.getParent() instanceof Table)) return false;
                    table = (Table)container.getParent();
                    cell = table.getCell(container);
                    startX = event.getStageX();
                    startY = event.getStageY();
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    if (vertical) {
                    	float delta = event.getStageX() - startX;
                    	if (align == Align.right) delta = -delta;
                    	float w = Math.max(cell.getActorWidth() + delta, container.getMinWidth());
                    	cell.width(w);
                    	Gdx.graphics.setSystemCursor(SystemCursor.HorizontalResize);
                	} else {
                		float delta = event.getStageY() - startY;
                		if (align == Align.top) delta = -delta;
                        float h = Math.max(cell.getActorHeight() + delta, container.getMinHeight());
                		cell.height(h);
                		Gdx.graphics.setSystemCursor(SystemCursor.VerticalResize);
                	}
                    table.invalidate();
                    startX = event.getStageX();
                    startY = event.getStageY();
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
                }
            });
        }
        
        public void reset() {
        	Table table = (Table)container.getParent();
            Cell<?> cell = table.getCell(container);
            if (vertical) {
            	cell.width(container.getPrefWidth());
        	} else {
        		cell.height(container.getPrefHeight());
        	}
            table.invalidate();
        }
    }
}