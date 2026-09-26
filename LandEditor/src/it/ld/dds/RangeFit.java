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

public class RangeFit {
	private final ColourSet m_colours;
	private final Vec3 m_metric;
	
	private float m_besterror;
	private Vec3 m_start;
	private Vec3 m_end;
	
	public RangeFit(ColourSet colours, Vec3 metric) {
		this.m_colours = colours;
		this.m_metric = metric != null ? metric : new Vec3(1f);
		m_besterror = Float.MAX_VALUE;
		
		final int count = colours.count;
		final Vec3[] values = colours.points;
		final float[] weights = colours.weights;
		
		// get the covariance matrix
		Sym3x3 covariance = Maths.computeWeightedCovariance(count, values, weights);
		
		// compute the principle component
		Vec3 principle = Maths.computePrincipleComponent(covariance);
		
		// get the min and max range as the codebook endpoints
		Vec3 start = new Vec3(0.0f);
		Vec3 end = new Vec3(0.0f);
		if (count > 0) {
			float min, max;
			// compute the range
			start = end = values[0];
			min = max = values[0].dot(principle);
			for (int i = 1; i < count; i++) {
				float val = values[i].dot(principle);
				if (val < min) {
					start = values[i];
					min = val;
				} else if (val > max) {
					end = values[i];
					max = val;
				}
			}
		}
		//Clamp the output to [0, 1]
		final Vec3 one = new Vec3(1f);
		final Vec3 zero = new Vec3(0f);
		start = Vec3.min(one, Vec3.max(zero, start));
		end = Vec3.min(one, Vec3.max(zero, end));
		//Clamp to the grid and save
		final Vec3 grid = new Vec3(31f, 63f, 31f);
		final Vec3 gridrcp = new Vec3(1f / 31f, 1f / 63f, 1f / 31f);
		final Vec3 half = new Vec3(0.5f);
		m_start = Vec3.truncate(grid.mul(start).add(half)).mul(gridrcp);
		m_end = Vec3.truncate(grid.mul(end).add(half)).mul(gridrcp);
	}
	
	public void compress(byte[] bdata, int blk) {
		// cache some values
		final int count = m_colours.count;
		final Vec3[] values = m_colours.points;
		// create a codebook
		Vec3[] codes = new Vec3[4];
		codes[0] = m_start;
		codes[1] = m_end;
		codes[2] = m_start.mul(2f / 3f).add(m_end.mul(1f / 3f));
		codes[3] = m_start.mul(1f / 3f).add(m_end.mul(2f / 3f));
		// match each point to the closest code
		int[] closest = new int[16];
		float error = 0f;
		for (int i = 0; i < count; i++) {
			// find the closest code
			float dist = Float.MAX_VALUE;
			int idx = 0;
			for (int j = 0; j < 4; j++) {
				float d = m_metric.mul(values[i].sub(codes[j])).lengthSquared();
				if (d < dist) {
					dist = d;
					idx = j;
				}
			}
			// save the index
			closest[i] = idx;
			// accumulate the error
			error += dist;
		}
		// save this scheme if it wins
		if (error < m_besterror) {
			// remap the indices
			int[] indices = new int[16];
			m_colours.remapIndices(closest, indices);
			// save the block
			m_colours.writeColourBlock4(m_start, m_end, indices, bdata, blk);
			// save the error
			m_besterror = error;
		}
	}
}
