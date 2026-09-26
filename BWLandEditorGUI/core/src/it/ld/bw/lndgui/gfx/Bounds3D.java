/* Copyright (c) 2026 Daniele Lombardi / Daniels118
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class Bounds3D {
	private final Vector3 low = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	private final Vector3 high = new Vector3(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
	
	public Bounds3D() {}
	
	public Bounds3D(float x, float y, float z, float size) {
		low.x = x - size;
		low.y = y - size;
		low.z = z - size;
		high.x = x + size;
		high.y = y + size;
		high.z = z + size;
	}
	
	public boolean isInitialized() {
		return low.x != Float.MAX_VALUE;
	}
	
	public Vector3 getLow() {
		return low;
	}
	
	public void setLow(Vector3 low) {
		this.low.set(low);
	}
	
	public Vector3 getHigh() {
		return high;
	}
	
	public void setHigh(Vector3 high) {
		this.high.set(high);
	}
	
	public void update(float x, float y, float z) {
		low.x = Math.min(x, low.x);
		low.y = Math.min(y, low.y);
		low.z = Math.min(z, low.z);
		high.x = Math.max(x, high.x);
		high.y = Math.max(y, high.y);
		high.z = Math.max(z, high.z);
	}
	
	public Vector3 getCenter() {
		return low.cpy().add(high).scl(0.5f);
	}
	
	public Vector3 getSize() {
		return high.cpy().sub(low);
	}
	
	public float getWidth() {
		return high.x - low.x;
	}
	
	public float getHeight() {
		return high.y - low.y;
	}
	
	public float getDepth() {
		return high.z - low.z;
	}
	
	public boolean contains(Vector3 pos) {
		return low.x <= pos.x && pos.x <= high.x &&
				low.y <= pos.y && pos.y <= high.y &&
				low.z <= pos.z && pos.z <= high.z;
	}
	
	public boolean containsXZ(Vector3 pos) {
		return low.x <= pos.x && pos.x <= high.x &&
				low.z <= pos.z && pos.z <= high.z;
	}
	
	public boolean intersect(Ray ray, Vector3 first, Vector3 second) {
        final float EPS = 1e-6f;

        float tMin = Float.NEGATIVE_INFINITY;
        float tMax = Float.POSITIVE_INFINITY;

        // X
        if (!slab(ray.origin.x, ray.direction.x, low.x, high.x, EPS)) return false;
        tMin = tmpMin; tMax = tmpMax;

        // Y
        if (!slab(ray.origin.y, ray.direction.y, low.y, high.y, EPS, tMin, tMax)) return false;
        tMin = tmpMin; tMax = tmpMax;

        // Z
        if (!slab(ray.origin.z, ray.direction.z, low.z, high.z, EPS, tMin, tMax)) return false;
        tMin = tmpMin; tMax = tmpMax;

        if (tMax < 0f) return false;

        float tEnter = Math.max(tMin, 0f);

        first.x  = ray.origin.x + ray.direction.x * tEnter;
        first.y  = ray.origin.y + ray.direction.y * tEnter;
        first.z  = ray.origin.z + ray.direction.z * tEnter;

        second.x = ray.origin.x + ray.direction.x * tMax;
        second.y = ray.origin.y + ray.direction.y * tMax;
        second.z = ray.origin.z + ray.direction.z * tMax;

        return true;
    }

    // temporanei interni (attenzione: non thread-safe se usi lo stesso Bounds3D da più thread)
    private float tmpMin, tmpMax;

    private boolean slab(float o, float d, float min, float max, float eps) {
        // primo asse: parte con [-inf, +inf]
        return slab(o, d, min, max, eps, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    }

    private boolean slab(float o, float d, float min, float max, float eps, float tMin, float tMax) {
        float newTMin = tMin;
        float newTMax = tMax;

        if (Math.abs(d) < eps) {
            if (o < min || o > max) return false;
        } else {
            float invD = 1f / d;
            float t1 = (min - o) * invD;
            float t2 = (max - o) * invD;
            if (t1 > t2) { float tmp = t1; t1 = t2; t2 = tmp; }

            newTMin = Math.max(newTMin, t1);
            newTMax = Math.min(newTMax, t2);
            if (newTMin > newTMax) return false;
        }

        tmpMin = newTMin;
        tmpMax = newTMax;
        return true;
    }
	
	@Override
	public String toString() {
		return "{" + low + ", " + high + "}";
	}
}
