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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

public class PopupMenu extends Table {
	public static final MenuItemAction separator = new MenuItemAction(null, null, null);
	
	public static Skin skin;
	
	private static PopupMenu currentPopup;
	
	private final LabelStyle labelNormal;
	private final LabelStyle shortcutNormal;
	private final LabelStyle labelOver;
	private final LabelStyle shortcutOver;
	private final LabelStyle disabledStyle;
	
	public static void init(Skin skin) {
		PopupMenu.skin = skin;
	}
	
	public PopupMenu(Skin skin) {
		super(skin);
		this.setBackground("window-border-bg");
		this.defaults().grow();
		labelNormal = new LabelStyle(skin.get(LabelStyle.class));
		labelNormal.background = skin.getDrawable("button");
		labelNormal.fontColor = Color.WHITE;
		shortcutNormal = new LabelStyle(skin.get(LabelStyle.class));
		shortcutNormal.background = skin.getDrawable("button");
		shortcutNormal.fontColor = Color.GRAY;
		
		labelOver = new LabelStyle(skin.get(LabelStyle.class));
		labelOver.background = skin.getDrawable("button-over");
		labelOver.fontColor = Color.WHITE;
		shortcutOver = new LabelStyle(skin.get(LabelStyle.class));
		shortcutOver.background = skin.getDrawable("button-over");
		shortcutOver.fontColor = Color.GRAY;
		
		disabledStyle = new LabelStyle(skin.get(LabelStyle.class));
		disabledStyle.background = skin.getDrawable("button");
		disabledStyle.fontColor = Color.GRAY;
	}
	
	public PopupMenu show(Actor anchor, MenuItemAction[] items) {
		return show(anchor, 0, 0, items);
	}
	
	public PopupMenu show(Actor anchor, float x, float y, MenuItemAction[] items) {
		final Stage stage = anchor.getStage();
		Vector2 pos = new Vector2(x, y);
	    anchor.localToStageCoordinates(pos);
	    return show(stage, pos.x, pos.y, items);
	}
	
	public PopupMenu show(Stage stage, float x, float y, MenuItemAction[] items) {
	    if (currentPopup != null) {
	    	hide();
	    }
	    clear();
	    if (items.length == 0) return this;
	    int cols = 2;
	    boolean hasCheckBoxes = false;
	    for (MenuItemAction item : items) {
	    	if (item.checkStatus != null) {
	    		hasCheckBoxes = true;
	    		cols = 3;
	    		break;
	    	}
	    }
	    
	    Drawable separatorDrawable = getSkin().newDrawable("white", Color.GRAY);
	    
	    boolean prevWasSeparator = true;
	    for (MenuItemAction item : items) {
	    	if (item == null || item.label == null) {
	    		if (!prevWasSeparator) {
	    			add(new Image(separatorDrawable)).colspan(cols).height(1).fillX().pad(3, 1, 3, 1).row();
	    			prevWasSeparator = true;
	    		}
	    	} else {
	    		boolean enabled = item.action != null && (item.enableStatus == null || item.enableStatus.getStatus(item));
	    		
	    		boolean checked = false;
	    		if (hasCheckBoxes) {
		    		if (item.checkStatus != null) {
		    			checked = item.checkStatus.getStatus(item);
		        	}
	    		}
	    		final ImageButton checkbox = new ImageButton(skin, checked ? "check-trans" : "transparent", null, null);
	    		checkbox.setDisabled(!enabled || item.checkStatus == null);
	    		
	    		Label button = new Label(item.label, enabled ? labelNormal : disabledStyle);
	    		Label shortcut = new Label(item.getShortcut(), enabled ? shortcutNormal : disabledStyle);
		        button.setAlignment(Align.left);
		        
		        if (enabled) {
			        InputListener inputListener = new InputListener() {
			            @Override
			            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			            	hide();
			                Gdx.app.postRunnable(() -> {
			                	try {
			                		item.action.run();
			                	} catch (Exception e) {
			                		e.printStackTrace();
			                		MessageBox.show(stage, item.label, e);
			                	}
			                });
			                return true;
						}
			            
			            @Override
			            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
			            	button.setStyle(labelOver);
			            	shortcut.setStyle(shortcutOver);
			            	super.enter(event, x, y, pointer, fromActor);
			            }
			            
			            @Override
			            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
			            	button.setStyle(labelNormal);
			            	shortcut.setStyle(shortcutNormal);
			            	super.exit(event, x, y, pointer, toActor);
			            }
			        };
			        checkbox.addListener(inputListener);
		        	button.addListener(inputListener);
		        	shortcut.addListener(inputListener);
		        }
		        
		        if (hasCheckBoxes) {
		        	add(checkbox);
	    		}
		        add(button).left();
		        add(shortcut).right();
		        row();
		        prevWasSeparator = false;
	    	}
	    }
	    
	    pack();
	    
	    setPosition(x, y - getHeight());
	    
	    addListener(new InputListener() {
	        @Override
	        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
	            return true;
	        }
	    });
	    
	    stage.addActor(this);
	    currentPopup = this;
	    
	    stage.addCaptureListener(stageListener);
	    return this;
	}
	
	public boolean isVisible() {
		return getStage() != null;
	}
	
	public PopupMenu hide() {
		Stage stage = currentPopup.getStage();
		currentPopup.remove();
    	currentPopup = null;
    	stage.removeCaptureListener(stageListener);
    	return this;
	}
	
	private final InputListener stageListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (currentPopup != null) {
            	Actor hit = currentPopup.hit(x - currentPopup.getX(), y - currentPopup.getY(), false);
            	if (!belongsTo(hit, currentPopup)) {
	                event.stop();
            		hide();
            	}
            }
            return false;
        }
    };
    
    @Override
    public float getPrefWidth() {
    	float w = super.getPrefWidth();
    	return Math.max(150f, w);
    }
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		keepWithinStage();
		super.draw(batch, parentAlpha);
	}
	
	public void keepWithinStage() {
		Stage stage = getStage();
		if (stage == null) return;
		Camera camera = stage.getCamera();
		if (camera instanceof OrthographicCamera) {
			OrthographicCamera orthographicCamera = (OrthographicCamera)camera;
			float parentWidth = stage.getWidth();
			float parentHeight = stage.getHeight();
			if (getX(Align.right) - camera.position.x > parentWidth / 2 / orthographicCamera.zoom)
				setPosition(camera.position.x + parentWidth / 2 / orthographicCamera.zoom, getY(Align.right), Align.right);
			if (getX(Align.left) - camera.position.x < -parentWidth / 2 / orthographicCamera.zoom)
				setPosition(camera.position.x - parentWidth / 2 / orthographicCamera.zoom, getY(Align.left), Align.left);
			if (getY(Align.top) - camera.position.y > parentHeight / 2 / orthographicCamera.zoom)
				setPosition(getX(Align.top), camera.position.y + parentHeight / 2 / orthographicCamera.zoom, Align.top);
			if (getY(Align.bottom) - camera.position.y < -parentHeight / 2 / orthographicCamera.zoom)
				setPosition(getX(Align.bottom), camera.position.y - parentHeight / 2 / orthographicCamera.zoom, Align.bottom);
		} else if (getParent() == stage.getRoot()) {
			float parentWidth = stage.getWidth();
			float parentHeight = stage.getHeight();
			if (getX() < 0) setX(0);
			if (getRight() > parentWidth) setX(parentWidth - getWidth());
			if (getY() < 0) setY(0);
			if (getTop() > parentHeight) setY(parentHeight - getHeight());
		}
	}
	
	private static boolean belongsTo(Actor child, Actor parent) {
		while (child != null) {
			if (child == parent) return true;
			child = child.getParent();
		}
		return false;
	}
	
	
	public static PopupMenu show1(Stage stage, float x, float y, MenuItemAction[] items) {
		PopupMenu menu = new PopupMenu(skin);
		menu.show(stage, x, y, items);
		return menu;
	}
	
	public static PopupMenu show1(Actor anchor, MenuItemAction[] items) {
		return show1(anchor, 0, 0, items);
	}
	
	public static PopupMenu show1(Actor anchor, float x, float y, MenuItemAction[] items) {
		PopupMenu menu = new PopupMenu(skin);
		menu.show(anchor, x, y, items);
		return menu;
	}
}
