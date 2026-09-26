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

public class Maths {
	public static final float FLT_EPSILON = 1.19209290E-07F;
	
	private Maths() {}
	
	public static Sym3x3 computeWeightedCovariance(int n, Vec3[] points, float[] weights) {
		//Compute the centroid
		float total = 0.0f;
		Vec3 centroid = new Vec3(0.0f);
		for (int i = 0; i < n; i++) {
			total += weights[i];
			centroid.selfAdd(points[i].mul(weights[i]));
		}
		if (total > FLT_EPSILON) {
			centroid.selfDiv(total);
		}
		//Accumulate the covariance matrix
		Sym3x3 covariance = new Sym3x3(0.0f);
		for (int i = 0; i < n; i++) {
			Vec3 a = points[i].sub(centroid);
			Vec3 b = a.mul(weights[i]);
			covariance.m[0] += a.x * b.x;
			covariance.m[1] += a.x * b.y;
			covariance.m[2] += a.x * b.z;
			covariance.m[3] += a.y * b.y;
			covariance.m[4] += a.y * b.z;
			covariance.m[5] += a.z * b.z;
		}
		return covariance;
	}
	
	public static Vec3 computePrincipleComponent(Sym3x3 matrix) {
		// compute the cubic coefficients
		float c0 = matrix.m[0] * matrix.m[3] * matrix.m[5] 
			+ 2.0f*matrix.m[1] * matrix.m[2] * matrix.m[4] 
			- matrix.m[0] * matrix.m[4] * matrix.m[4] 
			- matrix.m[3] * matrix.m[2] * matrix.m[2] 
			- matrix.m[5] * matrix.m[1] * matrix.m[1];
		float c1 = matrix.m[0] * matrix.m[3] + matrix.m[0] * matrix.m[5] + matrix.m[3] * matrix.m[5]
			- matrix.m[1] * matrix.m[1] - matrix.m[2] * matrix.m[2] - matrix.m[4] * matrix.m[4];
		float c2 = matrix.m[0] + matrix.m[3] + matrix.m[5];

		// compute the quadratic coefficients
		float a = c1 - (1.0f / 3.0f) * c2 * c2;
		float b = (-2.0f / 27.0f) * c2 * c2 *c2 + (1.0f / 3.0f) * c1 * c2 - c0;
		
		// compute the root count check
		float Q = 0.25f * b * b + (1.0f / 27.0f) * a * a * a;
		
		// test the multiplicity
		if( FLT_EPSILON < Q ) {
			// only one root, which implies we have a multiple of the identity
	        return new Vec3(1.0f);
		} else if (Q < -FLT_EPSILON) {
			// three distinct roots
			float theta = (float) Math.atan2(Math.sqrt(-Q), -0.5f * b);
			float rho = (float) Math.sqrt(0.25f * b * b - Q);
			
			float rt = (float) Math.pow(rho, 1.0f / 3.0f);
			float ct = (float) Math.cos(theta / 3.0f);
			float st = (float) Math.sin(theta / 3.0f);
			
			float l1 = (1f / 3f) * c2 + 2f * rt * ct;
			float l2 = (1f / 3f) * c2 - rt * (ct + (float) Math.sqrt(3f) * st);
			float l3 = (1f / 3f) * c2 - rt * (ct - (float) Math.sqrt(3f) * st);
			// pick the larger
			if (Math.abs(l2) > Math.abs(l1)) l1 = l2;
			if (Math.abs(l3) > Math.abs(l1)) l1 = l3;
			// get the eigenvector
			return getMultiplicity1Evector(matrix, l1);
		} else {
			// two roots
			float rt;
			if (b < 0.0f) {
				rt = (float) -Math.pow(-0.5f * b, 1f / 3f);
			} else {
				rt = (float) Math.pow(0.5f * b, 1f / 3f);
			}
			float l1 = (1f / 3f) * c2 + rt;		// repeated
			float l2 = (1f / 3f) * c2 - 2.0f * rt;
			
			// get the eigenvector
			if (Math.abs(l1) > Math.abs(l2)) {
				return getMultiplicity2Evector(matrix, l1);
			} else {
				return getMultiplicity1Evector(matrix, l2);
			}
		}
	}
	
	private static Vec3 getMultiplicity1Evector(Sym3x3 matrix, float evalue) {
		// compute M
		Sym3x3 m = new Sym3x3();
		m.m[0] = matrix.m[0] - evalue;
		m.m[1] = matrix.m[1];
		m.m[2] = matrix.m[2];
		m.m[3] = matrix.m[3] - evalue;
		m.m[4] = matrix.m[4];
		m.m[5] = matrix.m[5] - evalue;
		// compute U
		Sym3x3 u = new Sym3x3();
		u.m[0] = m.m[3] * m.m[5] - m.m[4] * m.m[4];
		u.m[1] = m.m[2] * m.m[4] - m.m[1] * m.m[5];
		u.m[2] = m.m[1] * m.m[4] - m.m[2] * m.m[3];
		u.m[3] = m.m[0] * m.m[5] - m.m[2] * m.m[2];
		u.m[4] = m.m[1] * m.m[2] - m.m[4] * m.m[0];
		u.m[5] = m.m[0] * m.m[3] - m.m[1] * m.m[1];
		// find the largest component
		float mc = Math.abs(u.m[0]);
		int mi = 0;
		for (int i = 1; i < 6; i++) {
			float c = Math.abs(u.m[i]);
			if (c > mc) {
				mc = c;
				mi = i;
			}
		}
		// pick the column with this component
		switch (mi) {
			case 0:
				return new Vec3(u.m[0], u.m[1], u.m[2]);
			case 1:
			case 3:
				return new Vec3(u.m[1], u.m[3], u.m[4]);
			default:
				return new Vec3(u.m[2], u.m[4], u.m[5]);
		}
	}

	private static Vec3 getMultiplicity2Evector(Sym3x3 matrix, float evalue) {
		// compute M
		Sym3x3 m = new Sym3x3();
		m.m[0] = matrix.m[0] - evalue;
		m.m[1] = matrix.m[1];
		m.m[2] = matrix.m[2];
		m.m[3] = matrix.m[3] - evalue;
		m.m[4] = matrix.m[4];
		m.m[5] = matrix.m[5] - evalue;
		// find the largest component
		float mc = Math.abs(m.m[0]);
		int mi = 0;
		for (int i = 1; i < 6; i++) {
			float c = Math.abs(m.m[i]);
			if (c > mc) {
				mc = c;
				mi = i;
			}
		}
		// pick the first eigenvector based on this index
		switch (mi) {
			case 0:
			case 1:
				return new Vec3(-m.m[1], m.m[0], 0.0f);
			case 2:
				return new Vec3(m.m[2], 0.0f, -m.m[0]);
			case 3:
			case 4:
				return new Vec3(0.0f, -m.m[4], m.m[3]);
			default:
				return new Vec3(0.0f, -m.m[5], m.m[4]);
		}
	}
	
	public static int floatToInt(float a, int limit) {
		// use ANSI round-to-zero behaviour to get round-to-nearest
		int i = (int)(a + 0.5f);
		// clamp to the limit
		if (i < 0) {
			i = 0;
		} else if (i > limit) {
			i = limit; 
		}
		// done
		return i;
	}
}
