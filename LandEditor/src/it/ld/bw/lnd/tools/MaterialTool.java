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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import it.ld.bw.lnd.model.BulkUpdate;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LNDMapMaterial;
import it.ld.bw.lnd.model.LNDMaterial;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.TerrainMaterialType;

public class MaterialTool {
	public static boolean canRemove(LNDMaterial material) {
		return !material.getLand().isMaterialInUse(material.getIndex());
	}
	
	public static void replaceMaterial(LndFile land, int src, int dst) {
		if (src == dst) return;
		for (LNDCountry country : land.getCountries()) {
			try (BulkUpdate<LNDMapMaterial> mapMaterials = country.getMapMaterialsForUpdate()) {
				boolean changed = false;
				for (int i = 0; i < mapMaterials.data.length; i++) {
					LNDMapMaterial mat = mapMaterials.data[i];
					if (mat.getFirstMaterialIndex() == src) {
						mat.setFirstMaterialIndex(dst);
						changed = true;
					}
					if (mat.getSecondMaterialIndex() == src) {
						mat.setSecondMaterialIndex(dst);
						changed = true;
					}
				}
				if (!changed) mapMaterials.noChanges();
			}
		}
	}
	
	public static void exportMaterial(LndFile land, int index, File dst) throws IOException {
		String ext = dst.getName().substring(dst.getName().lastIndexOf('.') + 1).toLowerCase();
		LNDMaterial material = land.getMaterials().get(index);
		int[] materialData = material.getIntARGB();
		BufferedImage image = new BufferedImage(LNDMaterial.width, LNDMaterial.height, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, image.getWidth(), image.getHeight(), materialData, 0, image.getWidth());
		ImageIO.write(image, ext, dst);
	}
	
	public static LNDMaterial importMaterial(File file, LndFile dstLand) throws IOException {
		return importMaterial(file, null, dstLand, -1);
	}
	
	public static LNDMaterial importMaterial(File file, TerrainMaterialType materialType, LndFile dstLand) throws IOException {
		return importMaterial(file, materialType, dstLand, -1);
	}
	
	public static LNDMaterial importMaterial(LNDMaterial newMaterial, LndFile dstLand) {
		return importMaterial(newMaterial, dstLand, -1);
	}
	
	public static LNDMaterial importMaterial(File file, TerrainMaterialType materialType, LndFile dstLand, int dst) throws IOException {
		if (materialType == null) materialType = TerrainMaterialType.None;
		BufferedImage image = ImageIO.read(file);
		if (image.getWidth() != image.getHeight()) throw new IOException("The image must be a square");
		if (image.getWidth() != LNDMaterial.width) {
			image = scaleImage(image, LNDMaterial.width, LNDMaterial.height);
		}
		int[] data = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		//
		LNDMaterial newMaterial = new LNDMaterial();
		newMaterial.setMaterialType(materialType);
		newMaterial.setARGB(data);
		return importMaterial(newMaterial, dstLand, dst);
	}
	
	public static LNDMaterial importMaterial(LNDMaterial newMaterial, LndFile dstLand, int dst) {
		if (dst < 0) {
			//Check if an identical material exists to avoid duplicates
			LNDMaterial existsing = dstLand.findMaterialByTexture(newMaterial, true);
			if (existsing != null) {
				return existsing;
			} else {
				if (newMaterial.getLand() != null && newMaterial.getLand() != dstLand) {
					newMaterial = newMaterial.clone();
				}
				dstLand.getMaterials().add(newMaterial);
				return newMaterial;
			}
		} else {
			LNDMaterial oldMaterial = dstLand.getMaterials().get(dst);
			oldMaterial.setMaterialType(newMaterial.getMaterialType());
			oldMaterial.setTexels(newMaterial.getTexels());
			return oldMaterial;
		}
	}
	
	public static Color getDominantColor(LNDMaterial material) {
		byte[] rgba = material.getByteRGBA();
		Map<Integer, Integer> histogram = new HashMap<>();
        for (int i = 0; i < rgba.length; i += 4) {
            int r = rgba[i] & 0xFF;
            int g = rgba[i + 1] & 0xFF;
            int b = rgba[i + 2] & 0xFF;
            int a = rgba[i + 3] & 0xFF;
            if (a < 128) continue;
            // quantization
            r = (r >> 4) << 4;
            g = (g >> 4) << 4;
            b = (b >> 4) << 4;
            
            int rgb = (r << 16) | (g << 8) | b;
            histogram.put(rgb, histogram.getOrDefault(rgb, 0) + 1);
        }
        
        int dominantRgb = 0;
        int maxCount = 0;
        for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantRgb = entry.getKey();
            }
        }
        
        int r = (dominantRgb >> 16) & 0xFF;
        int g = (dominantRgb >>  8) & 0xFF;
        int b = (dominantRgb      ) & 0xFF;
		return new Color(r, g, b, 255);
	}
	
	private static BufferedImage scaleImage(BufferedImage original, int newWidth, int newHeight) {
	    BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2d = scaled.createGraphics();
	    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
	    g2d.dispose();
	    return scaled;
	}
}
