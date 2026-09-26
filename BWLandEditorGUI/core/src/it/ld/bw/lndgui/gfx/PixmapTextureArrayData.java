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
package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureArrayData;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class PixmapTextureArrayData implements TextureArrayData {
	private Pixmap[] pixmaps;
	private boolean prepared;
	private Pixmap.Format format;
	private int depth;
	boolean useMipMaps;
	
	public PixmapTextureArrayData(Pixmap[] pixmaps) {
		this(Pixmap.Format.RGBA8888, false, pixmaps);
	}
	
	public PixmapTextureArrayData(Pixmap.Format format, boolean useMipMaps, Pixmap[] pixmaps) {
		this.pixmaps = pixmaps;
		this.format = format;
		this.useMipMaps = useMipMaps;
		this.depth = pixmaps.length;
	}
	
	@Override
	public boolean isPrepared() {
		return prepared;
	}
	
	@Override
	public void prepare() {
		int width = -1;
		int height = -1;
		for (Pixmap pixmap : pixmaps) {
			if (width == -1) {
				width = pixmap.getWidth();
				height = pixmap.getHeight();
				continue;
			}
			if (width != pixmap.getWidth() || height != pixmap.getHeight()) {
				throw new GdxRuntimeException(
					"Error whilst preparing TextureArray: TextureArray Textures must have equal dimensions.");
			}
		}
		prepared = true;
	}
	
	@Override
	public void consumeTextureArrayData() {
		for (int i = 0; i < pixmaps.length; i++) {
			Pixmap pixmap = pixmaps[i];
			Gdx.gl30.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, pixmap.getWidth(), pixmap.getHeight(), 1,
				pixmap.getGLInternalFormat(), pixmap.getGLType(), pixmap.getPixels());
		}
		if (useMipMaps) {
			Gdx.gl20.glGenerateMipmap(GL30.GL_TEXTURE_2D_ARRAY);
		}
	}
	
	@Override
	public int getWidth() {
		return pixmaps[0].getWidth();
	}
	
	@Override
	public int getHeight() {
		return pixmaps[0].getHeight();
	}
	
	@Override
	public int getDepth() {
		return depth;
	}
	
	@Override
	public boolean isManaged() {
		return true;
	}
	
	@Override
	public int getInternalFormat() {
		return Pixmap.Format.toGlFormat(format);
	}
	
	@Override
	public int getGLType() {
		return Pixmap.Format.toGlType(format);
	}
}
