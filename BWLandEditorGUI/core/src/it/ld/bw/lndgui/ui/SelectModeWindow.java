package it.ld.bw.lndgui.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.SelectionMode;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.ToolBar;

public class SelectModeWindow extends SmartWindow {
	public static void showSingleInstance(MainApp app, Stage stage, Actor anchor, int align, Skin skin) {
		SelectModeWindow window = (SelectModeWindow) SmartWindow.getSingleInstance("selectModeWindow");
		if (window == null) {
			window = new SelectModeWindow(app, skin, true);
			window.setSingleInstance("selectModeWindow");
		}
		float x = (stage.getWidth() - window.getWidth()) / 2f;
		float y = (stage.getHeight() - window.getHeight()) / 2f;
		if (anchor != null) {
			Vector2 pos = anchor.localToStageCoordinates(new Vector2(0, 0));
			x = pos.x;
			y = pos.y;
			if ((align & Align.left) != 0) x = pos.x - window.getWidth();
			if ((align & Align.right) != 0) x = pos.x + anchor.getWidth();
			if ((align & Align.top) != 0) y = pos.y + anchor.getHeight() - window.getHeight();
			if ((align & Align.bottom) != 0) y = pos.y - window.getHeight();
		}
		window.show(stage, x, y, false);
		window.toFront();
	}
	
	public static void hideInstance() {
		SelectModeWindow window = (SelectModeWindow) SmartWindow.getSingleInstance("selectModeWindow");
		if (window != null) {
			window.remove();
		}
	}
	
	
	public SelectModeWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("selectionMode.title"), skin);
		this.setAutodispose(autodispose);
		this.defaults().left().pad(2f);
		
		ToolBar toolbar = new ToolBar(skin, 32, true);
		for (SelectionMode mode : SelectionMode.values()) {
			toolbar.add("selection-" + mode.name().toLowerCase(), I18n.tr("selectionMode." + mode.name()), () -> app.setSelectionMode(mode), () -> mode == app.getSelectionMode());
		}
		add(toolbar).growX();
		
		pack();
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
}
