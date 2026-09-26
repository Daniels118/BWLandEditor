/* Copyright (c) 2024-2026 Daniele Lombardi / Daniels118
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
 * 
 *                             DISCLAIMER
 * This class is a porting from C++ libsquish by Simon Brown published
 * under MIT License.
 */
package it.ld.dds;

public class Vec3 {
	public float x;
	public float y;
	public float z;
	
	public Vec3() {}
	
	public Vec3(float s) {
		this.x = s;
		this.y = s;
		this.z = s;
	}
	
	public Vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void selfAdd(Vec3 v) {
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
	}
	
	public Vec3 add(Vec3 v) {
		return new Vec3(this.x + v.x, this.y + v.y, this.z + v.z);
	}
	
	public Vec3 sub(Vec3 v) {
		return new Vec3(this.x - v.x, this.y - v.y, this.z - v.z);
	}
	
	public Vec3 mul(float s) {
		return new Vec3(this.x * s, this.y * s, this.z * s);
	}
	
	public Vec3 mul(Vec3 v) {
		return new Vec3(this.x * v.x, this.y * v.y, this.z * v.z);
	}
	
	public void selfDiv(float s) {
		this.x /= s;
		this.y /= s;
		this.z /= s;
	}
	
	public Vec3 div(float s) {
		return new Vec3(this.x / s, this.y / s, this.z / s);
	}
	
	public Vec3 div(Vec3 v) {
		return new Vec3(this.x / v.x, this.y / v.y, this.z / v.z);
	}
	
	public float dot(Vec3 v) {
		return this.x * v.x + this.y * v.y + this.z * v.z;
	}
	
	public static Vec3 min(Vec3 a, Vec3 b) {
		return new Vec3(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z));
	}
	
	public static Vec3 max(Vec3 a, Vec3 b) {
		return new Vec3(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));
	}
	
	public float lengthSquared() {
		return this.dot(this);
	}
	
	public static Vec3 truncate(Vec3 v) {
		return new Vec3(
				(float) (v.x > 0 ? Math.floor(v.x) : Math.ceil(v.x)),
				(float) (v.y > 0 ? Math.floor(v.y) : Math.ceil(v.y)),
				(float) (v.z > 0 ? Math.floor(v.z) : Math.ceil(v.z))
			);
	}
}
