package it.ld.libgdx.ui.docking;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.List;

import it.ld.libgdx.ui.components.SmartWindow;

public class DockingManager {
	private static final float PREVIEW_MIN_SIZE = 150;
	
    private final Stage stage;
    private final List<DockContainer> containers = new ArrayList<>();
    private final DockPreview preview = new DockPreview();

    private float snapDistance = 32f;

    public DockingManager(Stage stage) {
        this.stage = stage;
        stage.addActor(preview);
    }

    public void addContainer(DockContainer container) {
        if (!containers.contains(container)) {
            containers.add(container);
        }
    }

    public void removeContainer(DockContainer container) {
        containers.remove(container);
    }

    public void updateDrag(SmartWindow window, float stageX, float stageY) {
    	if (window.getParent() != null) {
    		undock(window, stageX, stageY);
    	}
    	
        DockContainer target = findContainer(stageX, stageY, window);

        if (target == null) {
            preview.hide();
            return;
        }
        Vector2 pos = target.localToStageCoordinates(new Vector2(0, 0));
        int align = target.getAlign();
        float w = target.getWidth();
        float h = target.getHeight();
        Vector2 bl = pos.cpy();
        Vector2 tr = pos.cpy().add(w, h);
        SmartWindow ref = target.getWindow(stageX, stageY);
        if (ref == null) ref = target.getLastWindow();
        if (ref != null) {
        	if (target.isVertical()) {
        		Vector2 rPos = ref.localToStageCoordinates(new Vector2(0, ref.getHeight() / 2));
        		if (stageY <= rPos.y) {
        			pos.y = rPos.y - ref.getHeight();
        		} else {
        			pos.y = rPos.y;
        		}
        		h = ref.getHeight();
        		if (pos.y + h > tr.y) {
        			h = tr.y - pos.y;
        		}
        		if (pos.y < bl.y) {
        			h = pos.y + h - bl.y;
        			pos.y = bl.y;
        		}
        	} else {
        		Vector2 rPos = ref.localToStageCoordinates(new Vector2(ref.getWidth() / 2, 0));
        		if (stageX <= rPos.x) {
        			pos.x = rPos.x - ref.getWidth();
        		} else {
        			pos.x = rPos.x;
        		}
        		w = ref.getWidth();
        		if (pos.x + w > tr.x) {
        			w = tr.x - pos.x;
        		}
        		if (pos.x < bl.x) {
        			w = pos.x + w - bl.x;
        			pos.x = bl.x;
        		}
        	}
        } else {
	        if (target.isVertical()) {
	        	if (w < PREVIEW_MIN_SIZE) {
		        	if (align == Align.right) {
		        		pos.x = pos.x + w - PREVIEW_MIN_SIZE;
		        	}
		        	w = PREVIEW_MIN_SIZE;
	        	}
	        } else {
	        	if (h < PREVIEW_MIN_SIZE) {
		        	if (align == Align.top) {
		        		pos.y = pos.y + h - PREVIEW_MIN_SIZE;
		        	}
		        	h = PREVIEW_MIN_SIZE;
	        	}
	        }
        }
        preview.show(pos.x, pos.y, w, h);
        preview.toFront();
        window.toFront();
    }

    public void endDrag(SmartWindow window, float stageX, float stageY) {
        DockContainer target = findContainer(stageX, stageY, window);
        preview.hide();

        if (target == null) return;
        int index = 999;
        SmartWindow ref = target.getWindow(stageX, stageY);
        if (ref != null) {
        	index = target.getIndex(ref);
        	if (target.isVertical()) {
        		Vector2 pos = ref.localToStageCoordinates(new Vector2(0, ref.getHeight() / 2));
        		if (stageY <= pos.y) {
        			index++;
        		}
        	} else {
        		Vector2 pos = ref.localToStageCoordinates(new Vector2(ref.getWidth() / 2, 0));
        		if (stageX > pos.x) {
        			index++;
        		}
        	}
        }
        dock(window, target, index);
    }

    public void dock(SmartWindow window, DockContainer container, int index) {
        if (!container.canDock(window)) return;
        container.dock(window, index);
    }

    public void undock(SmartWindow window, float mouseX, float mouseY) {
        if (DockContainer.isDocked(window)) {
            DockContainer.getContainer(window).undock(window, mouseX, mouseY);
        } else if (window.getParent() != null) {
        	Vector2 pos = window.localToStageCoordinates(new Vector2(0, 0));
        	stage.addActor(window);
        	window.setMovable(true);
            window.setResizable(true);
            window.setPosition(pos.x, pos.y);
        }
    }

    private DockContainer findContainer(float stageX, float stageY, SmartWindow window) {
    	DockContainer result = null;
    	for (DockContainer container : containers) {
            if (!container.isVisible()) continue;
            if (!container.canDock(window)) continue;
            
            Vector2 pos = container.localToStageCoordinates(new Vector2(0, 0));
            int align = container.getAlign();
            float w = container.getWidth();
            float h = container.getHeight();
            if (container.isVertical()) {
            	if (w < snapDistance) {
    	        	if (align == Align.right) {
    	        		pos.x = pos.x + w - snapDistance;
    	        	}
    	        	w = snapDistance;
            	}
            	pos.y += PREVIEW_MIN_SIZE / 2;
            	h -= PREVIEW_MIN_SIZE;
            } else {
            	if (h < snapDistance) {
    	        	if (align == Align.top) {
    	        		pos.y = pos.y + h - snapDistance;
    	        	}
    	        	h = snapDistance;
            	}
            	pos.x += PREVIEW_MIN_SIZE / 2;
            	w -= PREVIEW_MIN_SIZE;
            }
            
            if (stageX >= pos.x && stageX <= pos.x + w && stageY >= pos.y && stageY <= pos.y + h) {
            	if (result != null) return null;	//When 2 targets overlap avoid to choose one
            	result = container;
            }
        }
        return result;
    }

    public float getSnapDistance() {
        return snapDistance;
    }

    public void setSnapDistance(float snapDistance) {
        this.snapDistance = snapDistance;
    }
}
