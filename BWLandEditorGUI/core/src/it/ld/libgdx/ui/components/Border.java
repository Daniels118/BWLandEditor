package it.ld.libgdx.ui.components;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class Border<E extends Actor> extends WidgetGroup {
	private Table table;
	private NinePatchDrawable background;
	
	public Border(Skin skin, E actor) {
		table = new Table();
		background = new NinePatchDrawable(skin.getPatch("textfield"));
		table.setBackground(background);
		table.add(actor);
		this.addActor(table);
	}
	
	@SuppressWarnings("unchecked")
	public E getActor() {
		return (E)table.getChild(0);
	}
	
	@Override
	public void layout() {
		table.setSize(getWidth(), getHeight());
	}
}
