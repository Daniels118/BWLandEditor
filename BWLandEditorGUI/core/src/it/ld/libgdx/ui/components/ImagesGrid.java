package it.ld.libgdx.ui.components;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import it.ld.libgdx.ui.events.BridgedEvent;
import it.ld.utils.MathUtils;

public class ImagesGrid extends WidgetGroup {
    private final Table content;
    private final ScrollPane scroll;

    private final Array<Drawable> items = new Array<>();
    private Styler styler = null;
    
    private float cellSize = 96f;
    private float pad = 4f;
    private int minCols = 1;

    private int lastCols = -1;
    private float lastWidth = -1f;
    
    private int selectedIndex = -1;
    
    private Drawable selectedBackground;

    public ImagesGrid(Skin skin) {
    	selectedBackground = skin.getDrawable("window-border-bg");
    	
    	content = new Table();
        content.top().left();
        content.defaults().pad(pad);

        scroll = new ScrollPane(content, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);	//only vertical
        scroll.setForceScroll(false, true);
        scroll.addListener(new InputListener() {
        	@Override
        	public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        		event.getStage().setScrollFocus(scroll);
        	}
        	
        	@Override
        	public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        		event.getStage().setScrollFocus(null);
        	}
        });
        
        setTouchable(Touchable.childrenOnly);
        addActor(scroll);
    }
    
    public Styler getStyler() {
    	return this.styler;
    }
    
    public void setStyler(Styler styler) {
    	this.styler = styler;
    	rebuildIfNeeded(true);
    }
    
    public int getSelectedIndex() {
    	return this.selectedIndex;
    }
    
    public void setSelectedIndex(int index) {
    	if (this.selectedIndex != index) {
	    	if (this.selectedIndex >= 0) {
	    		Container<?> container = (Container<?>) content.getChild(this.selectedIndex);
	    		container.setBackground(null, false);
	    	}
	    	this.selectedIndex = index;
			if (this.selectedIndex >= 0) {
				Container<?> container = (Container<?>) content.getChild(this.selectedIndex);
				container.setBackground(selectedBackground, false);
	    	}
			fire(new ChangeEvent());
    	}
    }
    
    public ImagesGrid setCellSize(float cell) {
        this.cellSize = cell;
        invalidateHierarchy();
        return this;
    }
    
    public float getCellSize() {
    	return cellSize;
    }

    public ImagesGrid setPadding(float pad) {
        this.pad = pad;
        content.defaults().pad(pad);
        invalidateHierarchy();
        return this;
    }
    
    public float getPadding() {
        return this.pad;
    }

    public ImagesGrid setMinCols(int minCols) {
        this.minCols = Math.max(1, minCols);
        invalidateHierarchy();
        return this;
    }
    
    public int getMinCols() {
        return this.minCols;
    }
    
    public void setItems(Array<Drawable> drawables) {
    	items.clear();
        if (drawables != null) items.addAll(drawables);
        //
        int toSelect = selectedIndex;
        if (selectedIndex >= items.size) {
        	toSelect = -1;
        } else if (selectedIndex == -1 && !items.isEmpty()) {
        	toSelect = 0;
        }
        selectedIndex = -1;
        lastCols = -1;	//force rebuild
        rebuildIfNeeded(true);
        setSelectedIndex(toSelect);
    }

    public void clearItems() {
        items.clear();
        lastCols = -1;
        rebuildIfNeeded(true);
    }
    
    @Override
    public void layout() {
        scroll.setBounds(0, 0, getWidth(), getHeight());
        rebuildIfNeeded(false);
    }
    
    private float getCellSpacing() {
    	return cellSize + pad * 2;
    }
    
    @Override
    public float getMinWidth() {
    	return content.getPadLeft() + getCellSpacing() + pad + content.getPadRight() + scroll.getScrollBarWidth() + 28f;
    }
    
    @Override
    public float getMinHeight() {
    	return content.getPadTop() + getCellSpacing() + content.getPadBottom() + 12f;
    }
    
    private int getPreferredCols() {
    	return Math.max(1, (int)Math.ceil(Math.sqrt(items.size) * 4 / 3));
    }
    
    @Override
    public float getPrefWidth() {
    	if (items.isEmpty()) return getMinWidth();
    	int cols = getPreferredCols();
        return content.getPadLeft() + getCellSpacing() * cols + pad + content.getPadRight() + scroll.getScrollBarWidth() + 28f;
    }

    @Override
    public float getPrefHeight() {
    	if (items.isEmpty()) return getMinHeight();
    	int cols = getPreferredCols();
    	int rows = MathUtils.ceilDiv(items.size, cols);
        return (cellSize + pad * 2) * rows + 12f;
    }

    private void rebuildIfNeeded(boolean force) {
        float w = scroll.getWidth();
        if (w <= 1f) return; //too early
        if (!force && Math.abs(w - lastWidth) < 0.5f) return;	//Avoid micro resize
        lastWidth = w;
        
        float step = cellSize + pad * 2f;
        int cols = Math.max(minCols, (int) (w / step));
        if (!force && cols == lastCols) return;

        lastCols = cols;

        content.clearChildren();

        for (int i = 0; i < items.size; i++) {
            Container<?> element = makeElement(items.get(i), i);
            content.add(element);
            if ((i + 1) % cols == 0) content.row();
        }

        content.invalidateHierarchy();
    }

    private Container<?> makeElement(Drawable icon, int index) {
        ImageButton.ImageButtonStyle st = new ImageButton.ImageButtonStyle();
        st.imageUp = icon;
        ImageButton button = new ImageButton(st);
        button.getImage().setScaling(Scaling.fit);
        button.getImageCell().align(Align.center);
        if (styler != null) {
        	styler.process(index, icon, button);
        }
        if (button.isDisabled()) {
        	button.setColor(1f, 1f, 1f, 0.3f);
        } else {
        	button.addListener(clickListener);
        }
        Container<?> container = new Container<>(button)
        .pad(selectedBackground.getTopHeight(), selectedBackground.getLeftWidth(),
				selectedBackground.getBottomHeight(), selectedBackground.getRightWidth())
        .size(cellSize);
        return container;
    }
    
    private final ClickListener clickListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
    		Actor actor = event.getTarget();
    		while (actor.getParent() != content) {
    			actor = actor.getParent();
    		}
        	int index = content.getChildren().indexOf(actor, true);
    		setSelectedIndex(index);
    		fire(new ImageClickEvent(event, index));
        }
    };
    
    
    public static class Styler {
		public void process(int index, Drawable item, ImageButton button) {}
	}
    
    
    public static abstract class ImageClickListener implements EventListener {
    	@Override
    	public boolean handle(Event event) {
    		if (!(event instanceof ImageClickEvent)) return false;
    		imageClicked((ImageClickEvent) event);
    		return false;
    	}
    	
    	public abstract void imageClicked(ImageClickEvent event);
    }
    
    
    public static class ImageClickEvent extends BridgedEvent<InputEvent> {
    	private final int index;
    	
    	public ImageClickEvent(InputEvent source, int index) {
			super(source);
			this.index = index;
		}
    	
    	public int getIndex() {
    		return this.index;
    	}
    }
}