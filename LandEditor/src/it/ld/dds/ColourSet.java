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

public class ColourSet {
	public Vec3[] points = new Vec3[16];
	public float[] weights = new float[16];
	public int[] remap = new int[16];
	public int count = 0;
	
	public ColourSet(int[] rgba) {
		//Find unique colors and their weights (occurrences count)
		for (int i = 0; i < 16; i++) {
			int color = rgba[i] & 0x00FFFFFF;
			for (int j = 0; ; j++) {
				if (j == i) {
					//Add the point
					points[count] = new Vec3(
							(float)((color >> 16) & 0xFF) / 255f,
							(float)((color >>  8) & 0xFF) / 255f,
							(float)((color      ) & 0xFF) / 255f
					);
					weights[count] = 1f;
					remap[i] = count;
					count++;
					break;
				}
				//Check for a match
				if (color == (rgba[j] & 0x00FFFFFF)) {
					//Get the index of the match
					int index = remap[j];
					//Map to this point and increase the weight
					weights[index]++;
					remap[i] = index;
					break;
				}
			}
		}
		//Square root the weights
		for (int i = 0; i < count; i++) {
			weights[i] = (float)Math.sqrt(weights[i]);
		}
	}
	
	public static int floatTo565(Vec3 colour) {
		// get the components in the correct range
		int r = Maths.floatToInt(31f * colour.x, 31);
		int g = Maths.floatToInt(63f * colour.y, 63);
		int b = Maths.floatToInt(31f * colour.z, 31);
		
		// pack into a single value
		return (r << 11) | (g << 5) | b;
	}
	
	public void writeColourBlock4(Vec3 start, Vec3 end, int[] indices, byte[] bdata, int blk) {
		// get the packed values
		int a = floatTo565(start);
		int b = floatTo565(end);
		// remap the indices
		int[] remapped = new int[16];
		if (a < b) {
			// swap a and b
			int t = a;
			a = b;
			b = t;
			for (int i = 0; i < 16; i++) {
				remapped[i] = (indices[i] ^ 0x1) & 0x3;
			}
		} else if (a == b) {
			// use index 0
			for (int i = 0; i < 16; i++) {
				remapped[i] = 0;
			}
		} else {
			// use the indices directly
			for (int i = 0; i < 16; i++) {
				remapped[i] = indices[i];
			}
		}
		//Write the block
		writeColourBlock(a, b, remapped, bdata, blk);
	}
	
	public static void writeColourBlock(int a, int b, int[] indices, byte[] bdata, int blk) {
		//Write the endpoints
		bdata[blk++] = (byte)(a & 0xff);
		bdata[blk++] = (byte)(a >> 8);
		bdata[blk++] = (byte)(b & 0xff);
		bdata[blk++] = (byte)(b >> 8);
		//Write the indices
		for (int i = 0; i < 4; i++) {
			int ind = 4 * i;
			bdata[blk++] = (byte) (indices[ind + 0] | (indices[ind + 1] << 2) | (indices[ind + 2] << 4) | (indices[ind + 3] << 6));
		}
	}
	
	public void remapIndices(int[] source, int[] target) {
		for (int i = 0; i < 16; i++) {
			int j = remap[i];
			if (j == -1) {
				target[i] = 3;
			} else {
				target[i] = source[j];
			}
		}
	}
}
