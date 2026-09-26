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
package it.ld.bw.lnd.tools;

import java.util.ArrayList;

import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LNDMapMaterial;
import it.ld.bw.lnd.model.LNDMaterial;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.SimpleMap;
import it.ld.utils.MathUtils;
import it.ld.utils.UChangeListener;

public class CountryPreviewGenerator implements AutoCloseable {
	private final LndFile land;
	private ArrayList<int[]> materials;
	
	public CountryPreviewGenerator(LndFile land) {
		this.land = land;
		this.materials = new ArrayList<>(land.getMaterials().size());
		for (LNDMaterial material : land.getMaterials()) {
			this.materials.add(material.getIntARGB());
		}
		land.listeners.add(landListener);
	}
	
	private final UChangeListener landListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LndFile.Property.MATERIALS) {
				if (event.getType() == EventType.ADD) {
					LNDMaterial material = (LNDMaterial)event.getNewValue();
					materials.add(material.getIntARGB());
				} else if (event.getType() == EventType.REMOVE) {
					if (event.getIndex() < 0) {
						materials.clear();
					} else {
						materials.remove(event.getIndex());
					}
				} else if (event.getType() == EventType.CHANGE) {
					LNDMaterial material = (LNDMaterial)event.getNewValue();
					materials.set(event.getIndex(), material.getIntARGB());
				}
			}
		}
	};
	
	public int[] getMaterial(int index) {
		return this.materials.get(index);
	}
	
	public int[] generatePreview(final LNDCountry country, int width, int height) {
		return generatePreview(country, width, height, true);
	}
	
	/**
	 * @param country
	 * @param width
	 * @param height
	 * @param showNoise
	 * @return a int-packed argb array
	 */
	public int[] generatePreview(final LNDCountry country, int width, int height, boolean showNoise) {
		int[] pixels = new int[width * height];
		final int h1 = height - 1;
		final SimpleMap noiseMap = land.getNoiseMap();
		LNDMapMaterial[] mapMaterials = country.getMapMaterialsForRead();
		Color col0 = new Color();
		Color col1 = new Color();
		Color mixed = new Color();
		//Draw preview with noise
		int dst = 0;
		for (int y = 0; y < height; y++) {
			int ty = y & 0xFF;
			final int altitude = (int)(((float)(h1 - y) / h1) * 255.99f);
			for (int x = 0; x < width; x++) {
				int noise = showNoise ? noiseMap.getUnsigned(x, y) / LndFile.NOISEMAP_DOWNSCALE : 0;
				int altitudeWithNoise = MathUtils.clamp(altitude + noise, 0, 255);
				final LNDMapMaterial mapMaterial = mapMaterials[altitudeWithNoise];
				float blend = mapMaterial.getBlend();
				int mat0Id = mapMaterial.getFirstMaterialIndex();
				int mat1Id = mapMaterial.getSecondMaterialIndex();
				int[] mat0 = getMaterial(mat0Id);
				int[] mat1 = getMaterial(mat1Id);
				//
				int tx = x & 0xFF;
				int rgb0 = mat0[ty * LNDMaterial.width + tx];
				int rgb1 = mat1[ty * LNDMaterial.width + tx];
				Color.argb8888ToColor(col0, rgb0);
				Color.argb8888ToColor(col1, rgb1);
				mixed.set(col0).lerp(col1, blend);
				mixed.a = 1f;
				pixels[dst++] = Color.argb8888(mixed);
			}
		}
		return pixels;
	}
	
	
	@Override
	public void close() {
		land.listeners.remove(landListener);
	}
}
