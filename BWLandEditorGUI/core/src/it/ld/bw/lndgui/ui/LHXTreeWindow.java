package it.ld.bw.lndgui.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;

import it.ld.bw.lhx.Command;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.gfx.Coord;
import it.ld.bw.lndgui.gfx.LHX3D;
import it.ld.bw.lndgui.gfx.Object3D;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.utils.UChangeListener;

public class LHXTreeWindow extends SmartWindow {
	private final MainApp app;
	
	private LHX3D lhx3d;
	
	private TextField searchField;
	private ScrollPane scrollPane;
	private final Tree<Object3DNode, Object3D> tree;
	
	private final Map<Object3D, Object3DNode> nodeMap = new HashMap<>();
	
	public static LHXTreeWindow showSingleInstance(MainApp app, Stage stage, Skin skin) {
		LHXTreeWindow window = (LHXTreeWindow) SmartWindow.getSingleInstance("lhxTreeWindow");
		if (window == null) {
			window = new LHXTreeWindow(app, skin, true);
			window.setDockingManager(app.getDockingManager());
			window.setSingleInstance("lhxTreeWindow");
			window.show(stage, 54, 28, false);
			window.setSize(460, stage.getHeight() - 90);
		}
		window.toFront();
		return window;
	}
	
	public static LHXTreeWindow getInstance() {
		return (LHXTreeWindow) SmartWindow.getSingleInstance("lhxTreeWindow");
	}
	
	public static void hideInstance() {
		LHXTreeWindow window = (LHXTreeWindow) SmartWindow.getSingleInstance("lhxTreeWindow");
		if (window != null) {
			window.remove();
		}
	}
	
	public LHXTreeWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("lhxTreeWindow.title"), skin, Attribute.RESIZABLE, Attribute.DOCKABLE);
		this.setAutodispose(autodispose);
		this.app = app;
		
		searchField = new TextField("", skin);
		searchField.addListener(new InputListener() {
			@Override
			public boolean keyTyped(InputEvent event, char character) {
				if (lhx3d != null) {
					rebuildFilteredTree(lhx3d.getRootObjects(), searchField.getText());
				}
				return false;
			}
		});
		add(searchField).growX().pad(5, 5, 0, 5);
		row();
		
		tree = new Tree<>(skin);
		tree.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Selection<Object3DNode> sel = tree.getSelection();
				List<Object3D> objects = new ArrayList<>(sel.size());
				for (Object3DNode node : sel) {
					objects.add(node.getValue());
				}
				app.getView3D().setSelectedObjects(objects);
			}
		});
		tree.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (this.getTapCount() == 2) {
					Selection<Object3DNode> sel = tree.getSelection();
					if (sel.size() == 1) {
						Object3DNode node = sel.getLastSelected();
						Object3D obj = node.getValue();
						Coord pos = obj.getPosition();
						if (pos != null) {
							Vector3 size = obj.boundingBox.getDimensions(new Vector3());
							float r = Math.max(size.x, size.z);
							app.flyTo(pos.x, pos.z, 40f + r);
						}
					}
				}
			}
		});
		tree.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				event.getStage().setKeyboardFocus(tree);
				return false;
			}
			
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Keys.LEFT) {
					Selection<Object3DNode> sel = tree.getSelection();
					if (sel.size() == 1) {
						Object3DNode node = sel.getLastSelected();
						node.setExpanded(false);
					}
					return true;
				} else if (keycode == Keys.RIGHT) {
					Selection<Object3DNode> sel = tree.getSelection();
					if (sel.size() == 1) {
						Object3DNode node = sel.getLastSelected();
						node.setExpanded(true);
					}
					return true;
				} else if (keycode == Keys.DOWN) {
					Object3DNode node = tree.getSelectedNode();
					if (node != null) {
						if (node.hasChildren() && node.isExpanded()) {
							Object3DNode next = node.getChildren().get(0);
							tree.getSelection().set(next);
						} else {
							while (node != null) {
								Object3DNode parent = node.getParent();
								Array<Object3DNode> children = parent != null ? parent.getChildren() : tree.getRootNodes();
								int index = children.indexOf(node, true);
								if (index < children.size - 1) {
									tree.getSelection().set(children.get(index + 1));
									break;
								} else {
									node = parent;
								}
							}
						}
					}
					return true;
				} else if (keycode == Keys.UP) {
					Object3DNode node = tree.getSelectedNode();
					if (node != null) {
						Object3DNode parent = node.getParent();
						Array<Object3DNode> children = parent != null ? parent.getChildren() : tree.getRootNodes();
						int index = children.indexOf(node, true);
						if (index > 0) {
							Object3DNode prev = children.get(index - 1);
							while (prev.hasChildren() && prev.isExpanded()) {
								children = prev.getChildren();
								prev = children.get(children.size - 1);
							}
							tree.getSelection().set(prev);
						} else if (parent != null) {
							tree.getSelection().set(parent);
						}
					}
					return true;
				}
				return false;
			}
			
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				getStage().setScrollFocus(tree);
			}
		});
		
		scrollPane = new ScrollPane(tree, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, false);
        scrollPane.setOverscroll(false, true);
		add(scrollPane).grow().pad(5);
		
		app.listeners.add(appChangeListener);
		setLHX3D(app.getView3D().getLHX3D());
		pack();
	}
	
	private final UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.CHANGE && event.getProperty() == MainApp.Property.LHX) {
				setLHX3D(app.getView3D().getLHX3D());
			}
		}
	};
	
	private void setLHX3D(LHX3D lhx3d) {
		if (this.lhx3d != null) {
			this.lhx3d.listeners.remove(lhx3dChangeListener);
		}
		for (Object3D obj : nodeMap.keySet()) {
			obj.listeners.remove(objectChangeListener);
		}
		nodeMap.clear();
		tree.clearChildren();
		searchField.setText("");
		this.lhx3d = lhx3d;
		if (lhx3d != null) {
			rebuildFilteredTree(lhx3d.getRootObjects(), searchField.getText());
			lhx3d.listeners.add(lhx3dChangeListener);
		}
	}
	
	private final UChangeListener lhx3dChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LHX3D.Property.SELECTION) {
				setSelected(lhx3d.getSelected());
			}
		}
	};
	
	private void rebuildFilteredTree(List<Object3D> roots, String text) {
	    String filter = text == null ? "" : text.trim().toLowerCase();
	    tree.clearChildren();
	    for (Object3D root : roots) {
		    Object3DNode filteredRoot = createFilteredNode(root, filter);
		    if (filteredRoot != null) {
		        tree.add(filteredRoot);
		        filteredRoot.setExpanded(true);
		    }
	    }
	}
	
	private Object3DNode createFilteredNode(Object3D object, String filter) {
	    boolean selfMatches = filter.isEmpty() || object.toString().toLowerCase().contains(filter);

	    Object3DNode node = createNodeWithoutChildren(object);

	    boolean hasMatchingChildren = false;

	    for (Object3D child : object.getChildren()) {
	    	if (child.getStatement() != null && child.getStatement().getCommand() == Command.CREATE_STREAM_POINT) continue;
	    	
	        Object3DNode childNode = createFilteredNode(child, filter);

	        if (childNode != null) {
	            node.add(childNode);
	            hasMatchingChildren = true;
	        }
	    }
	    
	    if (selfMatches || hasMatchingChildren) {
	        if (!filter.isEmpty()) {
	            node.setExpanded(true);
	        }
	        return node;
	    }
	    
	    return null;
	}
	
	private Object3DNode createNodeWithoutChildren(Object3D object) {
	    String style = object.isAnyError() ? "error" : "default";
		Label label = new Label(object.toString(), getSkin(), style);
	    Object3DNode node = new Object3DNode(label);
	    node.setValue(object);
	    nodeMap.put(object, node);
        object.listeners.add(objectChangeListener);
	    return node;
	}
	
	private final UChangeListener objectChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			Object3D obj = (Object3D)event.getSource();
			if (event.getProperty() == Object3D.Property.CHILDREN) {
				if (event.getType() == EventType.ADD || event.getType() == EventType.REMOVE) {
					refreshChildren(obj);
				}
			} else if (event.getProperty() == Object3D.Property.NAME) {
				refreshLabel(obj);
			} else if (event.getProperty() == Object3D.Property.SYNTAX_ERROR ||
					event.getProperty() == Object3D.Property.DUPLICATE_ID ||
					event.getProperty() == Object3D.Property.OUT_OF_BOUNDS) {
				refreshLabel(obj);
			}
		}
	};
	
	private void refreshLabel(Object3D object) {
	    Object3DNode node = nodeMap.get(object);
	    if (node == null) return;
	    Label label = node.getActor();
	    label.setText(object.toString());
	    label.setStyle(getSkin().get(object.isAnyError() ? "error" : "default", LabelStyle.class));
	}
	
	private void refreshChildren(Object3D object) {
	    Object3DNode node = nodeMap.get(object);
	    if (node == null) return;
	    boolean wasExpanded = node.isExpanded();
	    removeFromMapRecursive(node);
	    node.clearChildren();
	    for (Object3D child : object.getChildren()) {
	        Object3DNode childNode = createFilteredNode(child, searchField.getText());
	        if (childNode != null) {
	        	node.add(childNode);
	        }
	    }
	    node.setExpanded(wasExpanded);
	}
	
	private void removeFromMapRecursive(Object3DNode parent) {
	    for (Object3DNode child : parent.getChildren()) {
	        removeFromMapRecursive(child);
	        Object3D obj = child.getValue();
	        obj.listeners.remove(objectChangeListener);
	        nodeMap.remove(obj);
	    }
	}
	
	public void setSelected(List<Object3D> objects) {
		if (objects == null) objects = Collections.emptyList();
		IdentityHashMap<Object3D, Object3D> selectSet = new IdentityHashMap<>();
		for (Object3D obj : objects) {
			selectSet.put(obj, obj);
		}
		Selection<Object3DNode> selection = tree.getSelection();
		for (Entry<Object3D, Object3DNode> e : nodeMap.entrySet()) {
			Object3D obj = e.getKey();
			Object3DNode node = e.getValue();
			boolean mustSelect = selectSet.containsKey(obj);
			boolean isSelected = selection.contains(node);
			if (mustSelect && !isSelected) {
				selection.add(node);
			} else if (isSelected && !mustSelect) {
				selection.remove(node);
			}
		}
	}
	
	public boolean showObject(Object3D object) {
		final Object3DNode node = nodeMap.get(object);
		if (node != null) {
			Object3DNode parent = node.getParent();
			while (parent != null) {
				parent.setExpanded(true);
				parent = parent.getParent();
			}
			Gdx.app.postRunnable(() -> {	//Let the tree expand before scrolling
				Actor actor = node.getActor();
				scrollPane.scrollTo(actor.getX(), actor.getY() + actor.getHeight(), actor.getWidth(), actor.getHeight());
			});
			return true;
		}
		return false;
	}
	
	@Override
	public float getMinWidth() {
		return 200f;
	}
	
	@Override
	public float getPrefWidth() {
		return 460f;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		app.listeners.remove(appChangeListener);
		setLHX3D(null);
		super.dispose();
	}
	
	
	private static class Object3DNode extends Node<Object3DNode, Object3D, Label> {
		public Object3DNode(Label label) {
			super(label);
		}
		
		@Override
		public String toString() {
			return getValue().toString();
		}
	}
}
