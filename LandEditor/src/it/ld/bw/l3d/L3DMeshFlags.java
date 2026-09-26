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
package it.ld.bw.l3d;

public final class L3DMeshFlags {
	public static final int HAS_BONES = 1 << 8;
	public static final int HAS_DOOR_POSITION = 1 << 11;
	public static final int PACKED = 1 << 12;
	public static final int NO_DRAW = 1 << 13;
	public static final int CONTAINS_LANDSCAPE_FEATURE = 1 << 15;
	public static final int CONTAINS_UV2 = 1 << 18;
	public static final int CONTAINS_NAME_DATA = 1 << 19;
	public static final int CONTAINS_EXTRA_METRICS = 1 << 20;
	public static final int CONTAINS_EBONE = 1 << 21;
	public static final int CONTAINS_TNL_DATA = 1 << 22;
	public static final int CONTAINS_NEW_EP = 1 << 23;
	
	private L3DMeshFlags() {}
}
