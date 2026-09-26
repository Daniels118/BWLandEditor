package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Bounds2D {
	private final Vector2 low = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);
	private final Vector2 high = new Vector2(-Float.MAX_VALUE, -Float.MAX_VALUE);
	
	public Bounds2D() {}
	
	public Bounds2D(float x, float y, float size) {
		low.x = x - size;
		low.y = y - size;
		high.x = x + size;
		high.y = y + size;
	}
	
	public Vector2 getLow() {
		return low;
	}
	
	public void setLow(Vector2 low) {
		this.low.set(low);
	}
	
	public Vector2 getHigh() {
		return high;
	}
	
	public void setHigh(Vector2 high) {
		this.high.set(high);
	}
	
	public void update(float x, float y) {
		low.x = Math.min(x, low.x);
		low.y = Math.min(y, low.y);
		high.x = Math.max(x, high.x);
		high.y = Math.max(y, high.y);
	}
	
	public Vector2 getCenter() {
		return low.cpy().add(high).scl(0.5f);
	}
	
	public Vector2 getSize() {
		return high.cpy().sub(low);
	}
	
	public float getWidth() {
		return high.x - low.x;
	}
	
	public float getHeight() {
		return high.y - low.y;
	}
	
	public boolean contains(Vector3 pos) {
		return low.x <= pos.x && pos.x <= high.x &&
				low.y <= pos.z && pos.z <= high.y;
	}
}
