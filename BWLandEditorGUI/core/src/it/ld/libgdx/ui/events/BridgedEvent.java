package it.ld.libgdx.ui.events;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Stage;

public abstract class BridgedEvent<E extends Event> extends Event {
	private final E source;
	
	public BridgedEvent(E source) {
		this.source = source;
	}
	
	public E getSource() {
		return this.source;
	}
	
	@Override
	public void cancel() {
		source.cancel();
	}
	
	@Override
	public boolean getBubbles() {
		return source.getBubbles();
	}
	
	@Override
	public Actor getListenerActor() {
		return source.getListenerActor();
	}
	
	@Override
	public Stage getStage() {
		return source.getStage();
	}
	
	@Override
	public Actor getTarget() {
		return source.getTarget();
	}
	
	@Override
	public boolean isCancelled() {
		return source.isCancelled();
	}
	
	@Override
	public boolean isCapture() {
		return source.isCapture();
	}
	
	@Override
	public boolean isHandled() {
		return source.isHandled();
	}
	
	@Override
	public boolean isStopped() {
		return source.isStopped();
	}
	
	@Override
	public void reset() {
		source.reset();
	}
	
	@Override
	public void setBubbles(boolean bubbles) {
		source.setBubbles(bubbles);
	}
	
	@Override
	public void setCapture(boolean capture) {
		source.setCapture(capture);
	}
	
	@Override
	public void setListenerActor(Actor listenerActor) {
		source.setListenerActor(listenerActor);
	}
	
	@Override
	public void setStage(Stage stage) {
		source.setStage(stage);
	}
	
	@Override
	public void setTarget(Actor targetActor) {
		source.setTarget(targetActor);
	}
	
	@Override
	public void stop() {
		source.stop();
	}
}
