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

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;

import it.ld.libgdx.ui.docking.DockingManager;
import it.ld.libgdx.ui.events.RemoveEvent;

public class SmartWindow extends Window implements Disposable {
	private final int MOVE = 32;
	
	public enum Attribute {RESIZABLE, DETACHABLE, AUTODISPOSE, DOCKABLE}
	
	private static final Map<String, SmartWindow> singleInstances = new HashMap<>();
	
	private String instanceName;
	private boolean autodispose;
	private boolean disposed;
	private boolean dockable;
	private DockingManager dockingManager;
	
	private final float border;
	private float minWidth;
	private float minHeight;
	private float maxWidth = Float.MAX_VALUE;
	private float maxHeight = Float.MAX_VALUE;
	
	private boolean forceModal;
	private Image modalShadow;
	private float shadowAlpha = 0.5f;
    
    private boolean disabled = false;
    private boolean keepWithinStage = false;
    
    private SystemCursor desiredCursor = null;

    public SmartWindow(String title, Skin skin, Attribute...attributes) {
        this(title, skin, 7f, attributes);
    }

    public SmartWindow(String title, Skin skin, float border, Attribute...attributes) {
        super(title, skin);
        
        Drawable closeIcon = skin.getDrawable("icon-close");
        ImageButton closeButton = new ImageButton(closeIcon, closeIcon);
        closeButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				event.handle();
				remove();
			}
		});
        
        this.getTitleTable().add(closeButton).top().right().pad(border, 10, 0, 0);
        this.border = border;
        this.minWidth = border * 2;
        this.minHeight = border * 2;
        
        setKeepWithinStage(true);
        
        this.clearListeners();
        addCaptureListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (disabled && event.getTarget() != getTitleLabel()) {
					if (event instanceof FocusEvent) {
						if (!((FocusEvent)event).isFocused()) {
							return false;	//Allow "lost focus" events
						}
					} else if (event instanceof RemoveEvent) {
						return false;
					}
					event.cancel();
				}
				if (!disabled && (event instanceof InputEvent)) {
					InputEvent inputEvent = (InputEvent)event;
					if (inputEvent.getType() == InputEvent.Type.touchDown) {
						toFront();
					}
				}
				return false;
			}
        });
        
        addListener(new InputListener() {
        	private Actor startParent;
			private float mouseStartX;
			private float mouseStartY;
			private float winStartX;
			private float winStartY;
			private float startWidth;
			private float startHeight;
			
			private void updateEdge(float x, float y) {
				float border = getResizeBorder();
				float width = getWidth();
				float height = getHeight();
				edge = 0;
				if (isResizable()) {
					if (x >= 0 && x <= border) {
						edge |= Align.left;
					} else if (x >= width - border && x <= width) {
						edge |= Align.right;
					}
					if (y >= 0 && y <= border) {
						edge |= Align.bottom;
					} else if (y >= height - border && y <= height) {
						edge |= Align.top;
					}
				}
				if (isMovable() && edge == 0 && y >= height - getTitleLabel().getHeight() && y <= height && x >= 0 && x <= width - 30) {
					edge = MOVE;
				}
			}
			
			public boolean mouseMoved(InputEvent event, float x, float y) {
				if (!isResizable()) return false;
                if (isInside(SmartWindow.this, x, y, SmartWindow.this.border)) {
                	if (desiredCursor != null) {
                		resetCursor();
                	}
                } else {
                	desiredCursor = cursorFor(SmartWindow.this, x, y, SmartWindow.this.border);
                	Gdx.graphics.setSystemCursor(desiredCursor);
                }
                return false;
			}

			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (button == 0) {
					desiredCursor = cursorFor(SmartWindow.this, x, y, SmartWindow.this.border);
                	Gdx.graphics.setSystemCursor(desiredCursor);
					updateEdge(x, y);
					dragging = edge != 0;
					startParent = getParent();
					mouseStartX = event.getStageX();
					mouseStartY = event.getStageY();
					winStartX = getX();
					winStartY = getY();
					startWidth = getWidth();
					startHeight = getHeight();
				}
				return edge != 0;
			}

			public void touchDragged(InputEvent event, float x, float y, int pointer) {
				if (!dragging) return;
				Gdx.graphics.setSystemCursor(desiredCursor);
				if (startParent != getParent()) {
					startParent = getParent();
					winStartX = getX();
					winStartY = getY();
				}
				
				float dx = event.getStageX() - mouseStartX;
				float dy = event.getStageY() - mouseStartY;
				
				float width = getWidth();
				float height = getHeight();
				float windowX = getX();
				float windowY = getY();

				float minWidth = getMinWidth();
				float maxWidth = getMaxWidth();
				float minHeight = getMinHeight();
				float maxHeight = getMaxHeight();
				
				Stage stage = getStage();
				boolean clampPosition = keepWithinStage && stage != null && getParent() == stage.getRoot();

				if ((edge & MOVE) != 0) {
					windowX = winStartX + dx;
					windowY = winStartY + dy;
				}
				if ((edge & Align.left) != 0) {
					if (startWidth - dx < minWidth) dx = startWidth - minWidth;
					if (startWidth - dx > maxWidth) dx = maxWidth - startWidth;
					if (clampPosition && winStartX + dx < 0) dx = -winStartX;
					width = startWidth - dx;
					windowX = winStartX + dx;
				}
				if ((edge & Align.bottom) != 0) {
					if (startHeight - dy < minHeight) dy = startHeight - minHeight;
					if (startHeight - dy > maxHeight) dy = maxHeight - startHeight;
					if (clampPosition && winStartY + dy < 0) dy = -winStartY;
					height = startHeight - dy;
					windowY = winStartY + dy;
				}
				if ((edge & Align.right) != 0) {
					if (startWidth + dx < minWidth) dx = minWidth - startWidth;
					if (startWidth + dx > maxWidth) dx = startWidth - maxWidth;
					if (clampPosition && winStartX + startWidth + dx > stage.getWidth()) dx = stage.getWidth() - winStartX - startWidth;
					width = startWidth + dx;
				}
				if ((edge & Align.top) != 0) {
					if (startHeight + dy < minHeight) dy = minHeight - startHeight;
					if (startHeight + dy > maxHeight) dy = startHeight - maxHeight;
					if (clampPosition && winStartY + startHeight + dy > stage.getHeight()) dy = stage.getHeight() - winStartY - startHeight;
					height = startHeight + dy;
				}
				setBounds(Math.round(windowX), Math.round(windowY), Math.round(width), Math.round(height));
			}
			
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				dragging = false;
				resetCursor();
			}
			
			@Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				if (!dragging && !isInside(SmartWindow.this, x, y, 0)) {
					resetCursor();
				}
            }
		});
        
        if (contains(attributes, Attribute.DOCKABLE)) {
        	dockable = true;
        	addListener(new InputListener() {
        		@Override
        		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        			if (dragging) {
        				setKeepWithinStage(false);
        				return true;
        			}
        			return false;
        		}
        		
        		@Override
        		public void touchDragged(InputEvent event, float x, float y, int pointer) {
        			if (dragging) {
        				if (dockable && dockingManager != null) {
        				    dockingManager.updateDrag(SmartWindow.this, event.getStageX(), event.getStageY());
        				}
        			}
        		}
        		
        		@Override
        		public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        			if (dockable && dockingManager != null) {
        			    dockingManager.endDrag(SmartWindow.this, event.getStageX(), event.getStageY());
        			}
        			setKeepWithinStage(true);
        		}
        	});
        }
        
        if (contains(attributes, Attribute.RESIZABLE)) {
        	setResizable(true);
        	setResizeBorder((int)border);
        }
        
        autodispose = contains(attributes, Attribute.AUTODISPOSE);
    }
    
    public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
		getTitleLabel().setColor(1f, 1f, 1f, disabled ? 0.5f : 1f);
	}
	
	@Override
	public void setResizable(boolean isResizable) {
		if (isResizable != this.isResizable()) {
			super.setResizable(isResizable);
			setStyle(getSkin().get(isResizable ? "resizable" : "default", WindowStyle.class));
		}
	}
	
	public float getResizeBorder() {
		return border;
	}
    
    public float getMinWidth() {
    	return minWidth;
    }
    
    public void setMinWidth(float v) {
    	this.minWidth = v;
    	if (v > this.getWidth()) this.setWidth(v);
    }
    
    public float getMaxWidth() {
    	return maxWidth;
    }
    
    public void setMaxWidth(float v) {
    	this.maxWidth = v;
    	if (v < this.getWidth()) this.setWidth(v);
    }
    
    public float getMinHeight() {
    	return minHeight;
    }
    
    public void setMinHeight(float v) {
    	this.minHeight = v;
    	if (v > this.getHeight()) this.setHeight(v);
    }
    
    public float getMaxHeight() {
    	return maxHeight;
    }
    
    public void setMaxHeight(float v) {
    	this.maxHeight = v;
    	if (v < this.getHeight()) this.setHeight(v);
    }
    
    public void setMinSize(float w, float h) {
    	setMinWidth(w);
    	setMinHeight(h);
    }
    
    public void setMaxSize(float w, float h) {
    	setMaxWidth(w);
    	setMaxHeight(h);
    }
    
    protected boolean isForceModal() {
		return forceModal;
	}

    protected void setForceModal(boolean forceModal) {
		this.forceModal = forceModal;
	}
    
    public float getShadowAlpha() {
    	return this.shadowAlpha;
    }
    
    public void setShadowAlpha(float shadowAlpha) {
    	this.shadowAlpha = shadowAlpha;
    	if (modalShadow != null) {
    		modalShadow.getColor().a = shadowAlpha;
    	}
    }
    
    public void setAutodispose(boolean autodispose) {
    	this.autodispose = autodispose;
    }
    
    public String getInstanceName() {
    	return this.instanceName;
    }
    
    public void setSingleInstance(String name) {
    	if (singleInstances.containsKey(name)) throw new IllegalStateException("A single instance window named " + name + " already exists");
    	this.instanceName = name;
    	singleInstances.put(name, this);
    }
    
    public static SmartWindow getSingleInstance(String name) {
    	return singleInstances.get(name);
    }
    
    public SmartWindow show(Stage stage) {
		return show(stage, false);
    }
    
    public SmartWindow show(Stage stage, boolean modal) {
    	float x = (stage.getWidth() - getWidth()) / 2;
		float y = (stage.getHeight() - getHeight()) / 2;
		return show(stage, x, y, modal);
    }
    
    public SmartWindow show(Stage stage, float x, float y, boolean modal) {
    	modal |= forceModal;
    	super.setModal(modal);
    	if (modal && modalShadow == null) {
    		modalShadow = new Image(getSkin().newDrawable("white", new Color(0f, 0f, 0f, shadowAlpha)));
    		modalShadow.setScaling(Scaling.fill);
    		modalShadow.setFillParent(true);
    		addListener(new ClickListener() {
    			@Override
    			public void clicked(InputEvent event, float x, float y) {
    				if (x < 0 || x > getWidth() || y < 0 || y > getHeight()) {
    					flash();
    				}
    			}
    		});
    		stage.addActor(modalShadow);
    	}
    	setPosition(Math.round(x), Math.round(y));
	    stage.addActor(this);
	    return this;
    }
    
    public void flash() {
    	Drawable bg = getSkin().newDrawable("white", new Color(1f, 0.5f, 0f, 1f));
		Table titleTable = getTitleTable();
		titleTable.addAction(Actions.sequence(
			new Action() {
				@Override
				public boolean act(float delta) {
					titleTable.setColor(Color.CLEAR);
					titleTable.setBackground(bg);
					return true;
				}
			},
			Actions.color(Color.WHITE, 0.1f, Interpolation.smooth),
		    Actions.color(Color.CLEAR, 0.1f, Interpolation.smooth),
		    Actions.color(Color.WHITE, 0.1f, Interpolation.smooth),
		    Actions.color(Color.CLEAR, 0.1f, Interpolation.smooth),
		    Actions.color(Color.WHITE, 0.1f, Interpolation.smooth),
		    Actions.color(Color.CLEAR, 0.1f, Interpolation.smooth),
		    Actions.color(Color.WHITE, 0.1f, Interpolation.smooth),
		    Actions.color(Color.CLEAR, 0.1f, Interpolation.smooth),
		    new Action() {
				@Override
				public boolean act(float delta) {
					titleTable.setBackground((Drawable)null);
					return true;
				}
			}
		));
    }

    private void resetCursor() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        desiredCursor = null;
    }
    
    private static boolean isInside(Window w, float x, float y, float b) {
    	float width = w.getWidth();
        float height = w.getHeight();
        return x > b && x < width - b && y > b && y < height - b;
    }
    
    private static SystemCursor cursorFor(Window w, float x, float y, float b) {
        float width = w.getWidth();
        float height = w.getHeight();
        if (x >= 0 && x <= width && y >= 0 && y <= height) {
	        boolean left   = x <= b;
	        boolean right  = x >= width - b;
	        boolean bottom = y <= b;
	        boolean top    = y >= height - b;
	
	        if (left && top)     return SystemCursor.NWSEResize;
	        if (right && top)    return SystemCursor.NESWResize;
	        if (left && bottom)  return SystemCursor.NESWResize;
	        if (right && bottom) return SystemCursor.NWSEResize;
	
	        if (left || right)   return SystemCursor.HorizontalResize;
	        if (top || bottom)   return SystemCursor.VerticalResize;
        }
        return SystemCursor.Arrow;
    }
    
    @Override
    public void setKeepWithinStage(boolean keepWithinStage) {
    	this.keepWithinStage = keepWithinStage;
    	super.setKeepWithinStage(keepWithinStage);
    }
    
    @Override
    public void keepWithinStage() {
    	if (Gdx.graphics.getWidth() > 0 && Gdx.graphics.getHeight() > 0) {
    		super.keepWithinStage();
    	}
    }
    
    public boolean isDockable() {
        return dockable;
    }

    public void setDockable(boolean dockable) {
        this.dockable = dockable;
    }

    public void setDockingManager(DockingManager dockingManager) {
        this.dockingManager = dockingManager;
    }

    public DockingManager getDockingManager() {
        return dockingManager;
    }
    
    public void detachForDocking() {
        Group parent = getParent();
        if (parent != null) {
            parent.removeActor(this);
        }
        if (modalShadow != null) {
            modalShadow.remove();
            modalShadow = null;
        }
    }
    
    @Override
    protected void setParent(Group parent) {
    	boolean newResizable = isResizable() && (parent == null || parent.getParent() == null);
    	setResizable(newResizable);
    	super.setParent(parent);
    }
    
    @Override
	public boolean remove() {
    	if (this.getStage() != null) {
    		RemoveEvent e = new RemoveEvent(this);
    		fire(e);
    		if (!e.isCancelled()) {
	    		super.remove();
				if (modalShadow != null) {
					modalShadow.remove();
					modalShadow = null;
				}
				if (autodispose) {
					dispose();
				}
				return true;
    		}
		}
		return false;
	}

	@Override
	public void dispose() {
		this.disposed = true;
		if (instanceName != null) singleInstances.remove(instanceName);
	}
	
	public boolean isDisposed() {
		return this.disposed;
	}
	
	
	private static boolean contains(Attribute[] attributes, Attribute val) {
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i] == val) return true;
		}
		return false;
	}
}
