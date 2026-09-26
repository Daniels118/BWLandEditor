package it.ld.libgdx.ui.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

import it.ld.libgdx.ui.events.ResizeEvent;
import it.ld.libgdx.utils.Utils;

public class PictureBox extends Image implements Disposable {
	private Format colorFormat;
	
	private Pixmap pixmap;
	private PixmapTextureData textureData;
	private Texture texture;
	
	private Color backColor = Color.WHITE;
	private Color foreColor = Color.BLACK;
	private boolean keepContentOnResize = false;
	
	public PictureBox() {
		this(Format.RGB888);
	}
	
	public PictureBox(Format colorFormat) {
		this.colorFormat = colorFormat;
		this.pixmap = new Pixmap(1, 1, colorFormat);
		clear(null);
	}
	
	public Format getColorFormat() {
		return colorFormat;
	}
	
	public boolean isKeepContentOnResize() {
		return keepContentOnResize;
	}

	public PictureBox setKeepContentOnResize(boolean keepContentOnResize) {
		this.keepContentOnResize = keepContentOnResize;
		return this;
	}
	
	@Override
	protected void sizeChanged() {
		super.sizeChanged();
		final float width = Math.max(1, this.getWidth());
		final float height = Math.max(1, this.getHeight());
		
		Pixmap newPixmap = new Pixmap(Math.round(width), Math.round(height), colorFormat);
		if (this.texture != null) {
			if (keepContentOnResize) {
				newPixmap.drawPixmap(this.pixmap, 0, 0);
			}
			this.texture.dispose();
			this.pixmap.dispose();
		} else {
			clear(null);
		}
		this.pixmap = newPixmap;
		this.pixmap.setColor(foreColor);
		this.textureData = new PixmapTextureData(pixmap, null, false, false);
		this.texture = new Texture(textureData);
		this.texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		this.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
		super.setSize(width, height);
		
		fire(new ResizeEvent(this));
	}
	
	public Pixmap getPixmap() {
		return this.pixmap;
	}
	
	public int getPixmapWidth() {
		return pixmap.getWidth();
	}
	
	public int getPixmapHeight() {
		return pixmap.getHeight();
	}
	
	public Texture getTexture() {
		return this.texture;
	}
	
	public PixmapTextureData getTextureData() {
		return this.textureData;
	}
	
	public Color getBackColor() {
		return backColor;
	}

	public PictureBox setBackColor(Color backColor) {
		this.backColor = backColor;
		return this;
	}
	
	public Color getForeColor() {
		return foreColor;
	}

	public PictureBox setForeColor(Color foreColor) {
		this.foreColor = foreColor;
		return this;
	}
	
	/**
	 * @param color optional clear color, if null defaults to backcolor.
	 * @return
	 */
	public PictureBox clear(@Null Color color) {
		pixmap.setColor(color != null ? color : backColor);
		pixmap.fill();
		pixmap.setColor(foreColor);
		return this;
	}
	
	public PictureBox paint(int[] pixels) {
		if (pixels.length != pixmap.getWidth() * pixmap.getHeight()) throw new IllegalArgumentException("Wrong data size");
		int src = 0;
		for (int y = 0; y < pixmap.getHeight(); y++) {
			for (int x = 0; x < pixmap.getWidth(); x++) {
				pixmap.drawPixel(x, y, Utils.argbToRGBA(pixels[src++]));
			}
		}
		return this;
	}
	
	public PictureBox paint(byte[] pixels) {
		final int numPixels = pixmap.getWidth() * pixmap.getHeight();
		if (pixels.length == numPixels * 4) {
			int src = 0;
			for (int y = 0; y < pixmap.getHeight(); y++) {
				for (int x = 0; x < pixmap.getWidth(); x++) {
					pixmap.drawPixel(x, y, Utils.rgba(pixels[src++], pixels[src++], pixels[src++], pixels[src++]));
				}
			}
		} else if (pixels.length == numPixels * 3) {
			int src = 0;
			for (int y = 0; y < pixmap.getHeight(); y++) {
				for (int x = 0; x < pixmap.getWidth(); x++) {
					pixmap.drawPixel(x, y, Utils.rgba(pixels[src++], pixels[src++], pixels[src++], 0xFF));
				}
			}
		} else if (pixels.length == numPixels) {
			int src = 0;
			for (int y = 0; y < pixmap.getHeight(); y++) {
				for (int x = 0; x < pixmap.getWidth(); x++) {
					int v = pixels[src++] & 0xFF;
					pixmap.drawPixel(x, y, Utils.rgba(v, v, v, 0xFF));
				}
			}
		} else {
			throw new IllegalArgumentException("Wrong data size");
		}
		return this;
	}
	
	public PictureBox refresh() {
		if (texture != null) {
			texture.bind();
			Gdx.gl.glTexSubImage2D(
			    GL20.GL_TEXTURE_2D, 0,
			    0, 0, pixmap.getWidth(), pixmap.getHeight(),
			    GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE,
			    pixmap.getPixels()
			);
		}
		return this;
	}
	
	@Override
	public void dispose() {
		if (texture != null) {
			texture.dispose();
			pixmap.dispose();
		}
	}
}
