package it.ld.bw.lnd.model;

public class Vec3f {
	public float x;
	public float y;
	public float z;
	
	public Vec3f() {}
	
	public Vec3f(float x, float y, float z) {
		set(x, y, z);
	}
	
	public Vec3f(Vec3f v) {
		set(v);
	}
	
	public Vec3f set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vec3f set(Vec3f v) {
		return set(v.x, v.y, v.z);
	}
	
	public Vec3f cpy() {
		return new Vec3f(this);
	}
	
	public Vec3f add(Vec3f v) {
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
		return this;
	}
	
	public Vec3f sub(Vec3f v) {
		this.x -= v.x;
		this.y -= v.y;
		this.z -= v.z;
		return this;
	}
	
	public float len2() {
		return x * x + y * y + z * z;
	}
	
	public float len() {
		return (float)Math.sqrt(x * x + y * y + z * z);
	}
	
	public Vec3f scl(float scalar) {
		return this.set(this.x * scalar, this.y * scalar, this.z * scalar);
	}
	
	public Vec3f nor() {
		final float len2 = this.len2();
		if (len2 == 0f || len2 == 1f) return this;
		return this.scl(1f / (float)Math.sqrt(len2));
	}
	
	public Vec3f crs(final Vec3f vector) {
		return this.set(y * vector.z - z * vector.y, z * vector.x - x * vector.z, x * vector.y - y * vector.x);
	}
	
	public float dot(final Vec3f vector) {
		return x * vector.x + y * vector.y + z * vector.z;
	}
	
	public float dst(final Vec3f vector) {
		final float a = vector.x - x;
		final float b = vector.y - y;
		final float c = vector.z - z;
		return (float)Math.sqrt(a * a + b * b + c * c);
	}
	
	public float dst2(Vec3f point) {
		final float a = point.x - x;
		final float b = point.y - y;
		final float c = point.z - z;
		return a * a + b * b + c * c;
	}
	
	@Override
	public int hashCode() {
		return (int)x * 512 + (int)z;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vec3f)) return false;
		Vec3f other = (Vec3f)obj;
		return this.x == other.x && this.y == other.y && this.z == other.z;
	}
	
	@Override
	public String toString() {
		return "[" + x + ", " + y + ", " + z + "]";
	}
}