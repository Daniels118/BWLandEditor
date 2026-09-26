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
 */
package it.ld.libgdx.utils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Null;

public class Utils {
	public static String ellipsis(File file, int levels) {
		if (!file.isAbsolute()) return file.toString();
		Path path = file.toPath();
		int count = path.getNameCount();
		if (levels < 1) levels = 1;
		int start = Math.max(0, count - levels);
		if (start <= 1) start = 0;
		String res = path.subpath(start, count).toString();
		if (start > 0) {
			res = "..." + File.separator + res;
		}
		return path.getRoot().toString() + res;
	}
	
	public static Matrix4 setFaceTo(Matrix4 transform, Vector3 position, Vector3 direction) {
		return setFaceTo(transform, position, direction, 0);
	}
	
	public static Matrix4 setFaceTo(Matrix4 transform, Vector3 position, Vector3 direction, float rotateZ) {
		float x = direction.x;
		float y = direction.y;
		float z = direction.z;
		float axz = (float) (Math.atan2(x, z) * MathUtils.radiansToDegrees);
		float r = (float) Math.sqrt(x * x + z * z);
		float ay = (float) (Math.atan2(y, r) * MathUtils.radiansToDegrees);
		transform
		.setToRotation(Vector3.Y, axz)
		.rotate(Vector3.X, -ay)
		.rotate(Vector3.Z, rotateZ)
		.setTranslation(position);
		return transform;
	}
	
	public static Pixmap toPixmap(int[][] pixels, int channels) {
		int width = pixels.length;
		int height = pixels[0].length;
		Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
		ByteBuffer buffer = pixmap.getPixels();
		int argb = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				argb = pixels[x][y];
				if (channels == 4) {
					buffer.put((byte) ((argb >> 16) & 0xFF));	//R
					buffer.put((byte) ((argb >>  8) & 0xFF));	//G
					buffer.put((byte) ( argb        & 0xFF));	//B
					buffer.put((byte) ((argb >> 24) & 0xFF));	//A
				} else if (channels == 3) {
					buffer.put((byte) ((argb >> 16) & 0xFF));	//R
					buffer.put((byte) ((argb >>  8) & 0xFF));	//G
					buffer.put((byte) ( argb        & 0xFF));	//B
					buffer.put((byte) 0xFF);					//A
				} else if (channels == 1) {
					buffer.put((byte) argb);	//R
					buffer.put((byte) argb);	//G
					buffer.put((byte) argb);	//B
					buffer.put((byte) 0xFF);	//A
				}
			}
		}
		buffer.position(0);
		return pixmap;
	}
	
	/**
	 * @param pixels pixels in packed ARGB8888 format
	 * @param width
	 * @param height
	 * @return
	 */
	public static Pixmap toPixmap(int[] pixels, int width, int height) {
		if (pixels.length != width * height) throw new IllegalArgumentException("Wrong data size");
		Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
		ByteBuffer buffer = pixmap.getPixels();
		for (int src = 0; src < pixels.length; src++) {
			int argb = pixels[src];
			buffer.put((byte) ((argb >> 16) & 0xFF));	//R
			buffer.put((byte) ((argb >>  8) & 0xFF));	//G
			buffer.put((byte) ( argb        & 0xFF));	//B
			buffer.put((byte) ((argb >> 24) & 0xFF));	//A
		}
		buffer.position(0);
		return pixmap;
	}
	
	public static Pixmap toPixmap(byte[] pixels, int width, int height) {
		if (pixels.length == width * height * 4) {			//RGBA
			Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
			ByteBuffer buffer = pixmap.getPixels();
			buffer.put(pixels);
			buffer.position(0);
			return pixmap;
		} else if (pixels.length == width * height * 3) {	//RGB
			Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
			ByteBuffer buffer = pixmap.getPixels();
			for (int src = 0; src < pixels.length;) {
				buffer.put(pixels[src++]);
				buffer.put(pixels[src++]);
				buffer.put(pixels[src++]);
				buffer.put((byte)255);
			}
			buffer.position(0);
			return pixmap;
		} else if (pixels.length == width * height) {		//Grey scale
			Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
			ByteBuffer buffer = pixmap.getPixels();
			for (int src = 0; src < pixels.length; src++) {
				buffer.put(pixels[src]);
				buffer.put(pixels[src]);
				buffer.put(pixels[src]);
				buffer.put((byte)255);
			}
			buffer.position(0);
			return pixmap;
		} else {
			throw new IllegalArgumentException("Wrong data size");
		}
	}
	
	public static Texture toTexture(int[][] pixels, int channels, boolean disposePixmap) {
		Pixmap pixmap = toPixmap(pixels, channels);
		Texture texture = new Texture(pixmap);
		if (disposePixmap) pixmap.dispose();
		return texture;
	}
	
	/**
	 * @param pixels pixels in packed ARGB8888 format
	 * @param width
	 * @param height
	 * @param disposePixmap
	 * @return
	 */
	public static Texture toTexture(int[] pixels, int width, int height, boolean disposePixmap) {
		Pixmap pixmap = toPixmap(pixels, width, height);
		Texture texture = new Texture(pixmap);
		if (disposePixmap) pixmap.dispose();
		return texture;
	}
	
	public static Texture toTexture(byte[] pixels, int width, int height, boolean disposePixmap) {
		Pixmap pixmap = toPixmap(pixels, width, height);
		Texture texture = new Texture(pixmap);
		if (disposePixmap) pixmap.dispose();
		return texture;
	}
	
	public static Pixmap resize(Pixmap src, int width, int height, @Null Format newFormat) {
	    Filter oldFilter = src.getFilter();
		src.setFilter(Pixmap.Filter.BiLinear);
	    Pixmap dst = new Pixmap(width, height, newFormat != null ? newFormat : src.getFormat());
	    dst.drawPixmap(src,
	        0, 0, src.getWidth(), src.getHeight(),
	        0, 0, width, height
	    );
	    src.setFilter(oldFilter);
	    return dst;
	}
	
	public static int[] argbToRGBA(int[] pixels) {
		for (int i = 0; i < pixels.length; i++) {
			int col = pixels[i];
			int a = (col >> 24) & 0xFF;
			int r = (col >> 16) & 0xFF;
			int g = (col >>  8) & 0xFF;
			int b =  col        & 0xFF;
			pixels[i] = (r << 24) | (g << 16) | (b << 8) | a;
		}
		return pixels;
	}
	
	public static int[] abgrToARGB(int[] pixels) {
		for (int i = 0; i < pixels.length; i++) {
			int col = pixels[i];
			int a = (col >> 24) & 0xFF;
			int b = (col >> 16) & 0xFF;
			int g = (col >>  8) & 0xFF;
			int r =  col        & 0xFF;
			pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
		}
		return pixels;
	}
	
	public static int argbToRGBA(int col) {
		int a = (col >> 24) & 0xFF;
		int r = (col >> 16) & 0xFF;
		int g = (col >>  8) & 0xFF;
		int b =  col        & 0xFF;
		return (r << 24) | (g << 16) | (b << 8) | a;
	}
	
	public static int rgba(byte r, byte g, byte b, byte a) {
		return (r & 0xFF) << 24 | (g & 0xFF) << 16 | (b & 0xFF) << 8 | (a & 0xFF);
	}
	
	public static int rgba(int r, int g, int b, int a) {
		return r << 24 | g << 16 | b << 8 | a;
	}
}