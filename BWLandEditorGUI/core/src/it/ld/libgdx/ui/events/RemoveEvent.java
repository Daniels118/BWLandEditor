package it.ld.libgdx.ui.events;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;

public class RemoveEvent extends Event {
	public RemoveEvent(Actor target) {
		this.setTarget(target);
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "(" + getTarget() + ")";
	}
}
