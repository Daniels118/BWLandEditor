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
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import it.ld.bw.lnd.model.SimpleMap;

public class SimpleMapTool {
	public static void exportSimpleMap(SimpleMap map, File dst) throws IOException {
		String ext = dst.getName().substring(dst.getName().lastIndexOf('.') + 1).toLowerCase();
		if ("raw".equals(ext)) {
			try (FileOutputStream out = new FileOutputStream(dst)) {
				out.write(map.getPixels());
			}
		} else {
			BufferedImage image = new BufferedImage(SimpleMap.width, SimpleMap.height, BufferedImage.TYPE_BYTE_GRAY);
			WritableRaster raster = image.getRaster();
			DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
			System.arraycopy(map.getPixels(), 0, dataBuffer.getData(), 0, map.getPixels().length);
			ImageIO.write(image, ext, dst);
		}
	}
	
	public static SimpleMap importSimpleMap(File src) throws IOException {
		String ext = src.getName().substring(src.getName().lastIndexOf('.') + 1).toLowerCase();
		if ("raw".equals(ext)) {
			try (FileInputStream out = new FileInputStream(src)) {
				byte[] buffer = new byte[SimpleMap.width * SimpleMap.height];
				int n = out.read(buffer);
				if (n < buffer.length || out.available() > 0) throw new IOException("Invalid file size");
				return new SimpleMap(buffer);
			}
		} else {
			BufferedImage image = ImageIO.read(src);
			if (image.getWidth() != image.getHeight()) throw new IOException("The image must be squared");
			if (image.getWidth() != SimpleMap.width || image.getSampleModel().getNumBands() != 1) {
				image = scaleImage(image, SimpleMap.width, SimpleMap.height);
			}
			WritableRaster raster = image.getRaster();
			DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
			return new SimpleMap(dataBuffer.getData());
		}
	}
	
	private static BufferedImage scaleImage(BufferedImage original, int newWidth, int newHeight) {
	    BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
	    Graphics2D g2d = scaled.createGraphics();
	    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
	    g2d.dispose();
	    return scaled;
	}
}
