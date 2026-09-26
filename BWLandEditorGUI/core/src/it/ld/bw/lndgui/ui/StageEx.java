package it.ld.bw.lndgui.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

public class StageEx extends Stage {
	public StageEx() {
		super();
	}
	
	public StageEx(Viewport viewport) {
		super(viewport);
	}
	
	public StageEx(Viewport viewport, Batch batch) {
		super(viewport, batch);
	}
	
	/**This method is invoked whenever an unhandled exception reaches the stage.
	 * The default implementation will throw the exception again, usually crashing the application.
	 * Override this method to catch the exception and handle it in a different way.
	 * @param e
	 * @throws RuntimeException
	 */
	public void onException(Exception e) throws RuntimeException {
		if (e instanceof RuntimeException) {
			throw (RuntimeException)e;
		} else {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		try {
			return super.touchDown(screenX, screenY, pointer, button);
		} catch (RuntimeException e) {
			onException(e);
			return false;
		}
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		try {
			return super.touchDragged(screenX, screenY, pointer);
		} catch (Exception e) {
			onException(e);
			return false;
		}
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		try {
			return super.touchUp(screenX, screenY, pointer, button);
		} catch (RuntimeException e) {
			onException(e);
			return false;
		}
	}
	
	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		try {
			return super.touchCancelled(screenX, screenY, pointer, button);
		} catch (RuntimeException e) {
			onException(e);
			return false;
		}
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		try {
			return super.mouseMoved(screenX, screenY);
		} catch (RuntimeException e) {
			onException(e);
			return false;
		}
	}
	
	@Override
	public boolean scrolled(float amountX, float amountY) {
		try {
			return super.scrolled(amountX, amountY);
		} catch (RuntimeException e) {
			onException(e);
			return false;
		}
	}
	
	@Override
	public boolean keyDown(int keyCode) {
		try {
			return super.keyDown(keyCode);
		} catch (RuntimeException e) {
			onException(e);
			return false;
		}
	}
	
	@Override
	public boolean keyUp(int keyCode) {
		try {
			return super.keyUp(keyCode);
		} catch (RuntimeException e) {
			onException(e);
			return false;
		}
	}
	
	@Override
	public boolean keyTyped(char character) {
		try {
			return super.keyTyped(character);
		} catch (RuntimeException e) {
			onException(e);
			return false;
		}
	}
}
