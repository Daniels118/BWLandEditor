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
package it.ld.bw.lhx;

import java.util.Locale;

public class LHXCoord {
	public final float x;
	public final float z;
	
	private final int hash;
	
	public LHXCoord(float x, float z) {
		this.x = x;
		this.z = z;
		hash = computeHash(x, z);
	}
	
	public LHXCoord(String expr) {
		String[] parts = expr.split(",");
		if (parts.length == 2) {
			this.x = Float.parseFloat(parts[0]);
			this.z = Float.parseFloat(parts[1]);
			hash = computeHash(x, z);
		} else {
			throw new IllegalArgumentException("Invalid coordinate string: " + expr);
		}
	}
	
	public float dst(float x, float z) {
		float dx = this.x - x;
		float dz = this.z - z;
		return (float)Math.sqrt(dx * dx + dz * dz);
	}
	
	public float dst2(float x, float z) {
		float dx = this.x - x;
		float dz = this.z - z;
		return dx * dx + dz * dz;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LHXCoord)) return false;
		LHXCoord other = (LHXCoord)obj;
		return Math.abs(this.x - other.x) < 0.01f && Math.abs(this.z - other.z) < 0.01f;
	}
	
	@Override
	public String toString() {
		return String.format(Locale.US, "\"%.2f,%.2f\"", x, z);
	}
	
	
	private static int computeHash(float x, float z) {
		return Float.hashCode(Math.round(x * 100f) + Math.round(z * 100f));
	}
}
