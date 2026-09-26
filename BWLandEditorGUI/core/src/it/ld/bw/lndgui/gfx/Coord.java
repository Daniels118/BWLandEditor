package it.ld.bw.lndgui.gfx;

import java.util.Locale;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import it.ld.bw.lhx.LHXCoord;
import it.ld.bw.lnd.model.Vec3f;

/**
 * Helper class used to hold game-space coordinates, and avoid confusion with LibGDX coordinate system. 
 */
public class Coord {
	public float x;
	public float y;
	public float z;
	
	public Coord() {}
	
	public Coord(Vector3 v) {
		this(v.x, v.y, -v.z);
	}
	
	public Coord(Vector2 v) {
		this(v.x, 0f, -v.y);
	}
	
	public Coord(Coord v) {
		this(v.x, v.y, v.z);
	}
	
	public Coord(LHXCoord v) {
		this(v.x, 0f, v.z);
	}
	
	public Coord(float x, float z) {
		this(x, 0f, z);
	}
	
	public Coord(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public Coord clone() {
		return new Coord(this);
	}
	
	public Coord set(Coord v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
		return this;
	}
	
	public Coord set(Vector3 v) {
		this.x = v.x;
		this.y = v.y;
		this.z = -v.z;
		return this;
	}
	
	public Coord set(LHXCoord v) {
		this.x = v.x;
		this.y = 0;
		this.z = v.z;
		return this;
	}
	
	public float dst(Coord c) {
		float dx = c.x - this.x;
		float dy = c.y - this.y;
		float dz = c.z - this.z;
		return (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
	
	public float dst2(Coord c) {
		float dx = c.x - this.x;
		float dy = c.y - this.y;
		float dz = c.z - this.z;
		return dx*dx + dy*dy + dz*dz;
	}
	
	public Vec3f toVec3f() {
		return new Vec3f(x, y, z);
	}
	
	public Vector3 toVector3() {
		return new Vector3(x, y, -z);
	}
	
	public Vector3 toVector3(Vector3 out) {
		return out.set(x, y, -z);
	}
	
	public Vector2 toVector2() {
		return new Vector2(x, -z);
	}
	
	public Vector2 toVector2(Vector2 out) {
		return out.set(x, -z);
	}
	
	public LHXCoord toLHXCoord() {
		return new LHXCoord(x, z);
	}
	
	public static Coord from(Vec3f v) {
		return new Coord(v.x, v.y, v.z);
	}
	
	public static Coord from(Vector3 v) {
		return new Coord(v.x, v.y, -v.z);
	}
	
	public static Coord from(Vector2 v) {
		return new Coord(v.x, 0f, -v.y);
	}
	
	@Override
    public String toString() {
    	return String.format(Locale.US, "[%.2f, %.2f, %.2f]", x, y, z);
    }
}
