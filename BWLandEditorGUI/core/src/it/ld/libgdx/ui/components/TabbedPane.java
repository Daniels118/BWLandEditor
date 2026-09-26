package it.ld.libgdx.ui.components;

import java.util.Objects;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.SnapshotArray;

public class TabbedPane extends Table {
	private Skin skin;
	private int tabsAlign;
	private Table tabsPanel = new Table();
	private Stack contentPanel = new Stack();
	private ButtonGroup<Button> tabsGroup = new ButtonGroup<>();
	
	private int selectedIndex = -1;
	
	public TabbedPane(Skin skin, int tabsAlign) {
		this.skin = skin;
		this.tabsAlign = tabsAlign;
		tabsGroup.setMinCheckCount(1);
		tabsGroup.setMaxCheckCount(1);
		if (tabsAlign == Align.top || tabsAlign == Align.bottom) {
			tabsPanel.defaults().padRight(2);
		} else if (tabsAlign == Align.left || tabsAlign == Align.right) {
			tabsPanel.defaults().padBottom(2);
		}
		if (tabsAlign == Align.top) {
			add(tabsPanel).left().row();
			add(contentPanel).grow();
		} else if (tabsAlign == Align.bottom) {
			add(contentPanel).grow().row();
			add(tabsPanel).left();
		} else if (tabsAlign == Align.left) {
			add(tabsPanel).top();
			add(contentPanel).grow();
		} else if (tabsAlign == Align.right) {
			add(contentPanel).grow();
			add(tabsPanel).top();
		}
	}
	
	@Override
	public void clearChildren() {
		tabsPanel.clearChildren();
		contentPanel.clearChildren();
		tabsGroup.clear();
		selectedIndex = -1;
	}
	
	public TabbedPane add(String label, Actor content) {
		return add(label, content, null);
	}
	
	public TabbedPane add(String label, Actor content, Object tag) {
		Button button = new TextButton(label, skin, "toggle");
		button.setUserObject(tag);
		return add(button, content);
	}
	
	public TabbedPane add(Button button, Actor panel) {
		if (tabsAlign == Align.left || tabsAlign == Align.right) {
			if (button instanceof TextButton) {
				button.setRotation(-90f);
			}
		}
		tabsGroup.add(button);
		if (tabsPanel.getChildren().isEmpty()) {
			selectedIndex = 0;
			button.setChecked(true);
		}
		button.addListener(tabChangeListener);
		tabsPanel.add(button);
		if (tabsAlign == Align.left || tabsAlign == Align.right) {
			tabsPanel.row();
		}
		panel.setVisible(button.isChecked());
		contentPanel.addActor(panel);
		return this;
	}
	
	private final ChangeListener tabChangeListener = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			Actor button = event.getTarget();
			selectedIndex = tabsPanel.getChildren().indexOf(button, true);
			SnapshotArray<Actor> panels = contentPanel.getChildren();
			for (int i = 0; i < panels.size; i++) {
				panels.get(i).setVisible(i == selectedIndex);
			}
			//
			TabbedPane.this.fire(new ChangeEvent());
		}
	};
	
	public void showPanel(int index) {
		((Button)tabsPanel.getChild(index)).setChecked(true);
	}
	
	public boolean showPanelByTag(Object tag) {
		for (Actor tab : tabsPanel.getChildren()) {
			if (Objects.equals(tag, tab.getUserObject())) {
				((Button)tab).setChecked(true);
				return true;
			}
		}
		return false;
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	public Object getSelectedTag() {
		if (selectedIndex < 0 || selectedIndex >= tabsPanel.getChildren().size) return null;
		return getTag(selectedIndex);
	}
	
	public Button getTab(int index) {
		return (Button)tabsPanel.getChild(index);
	}
	
	public Object getTag(int index) {
		return tabsPanel.getChild(index).getUserObject();
	}
	
	public Actor getPanel(int index) {
		return contentPanel.getChild(index);
	}
}
