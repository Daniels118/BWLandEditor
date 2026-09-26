package it.ld.bw.lndgui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;

import it.ld.bw.lndgui.interfaces.ImageWriter;

public class DesktopImageWriter implements ImageWriter {
	@Override
	public void write(File file, int width, int height, int[] pixels) throws IOException {
		String name = file.getName();
		String fmt = name.substring(name.lastIndexOf('.') + 1).toUpperCase();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		write(file, fmt, image);
	}

	@Override
	public void write(File file, Pixmap pixmap) throws IOException {
		if (pixmap.getFormat() != Format.RGBA8888) throw new IllegalArgumentException("Only pixmaps in RGBA8888 format are supported");
		String name = file.getName();
		String fmt = name.substring(name.lastIndexOf('.') + 1).toUpperCase();
		final int width = pixmap.getWidth();
		final int height = pixmap.getHeight();
        
		final boolean alpha = "PNG".equals(fmt);
        
        BufferedImage image = new BufferedImage(width, height, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        ByteBuffer pixels = pixmap.getPixels();
        pixels.rewind();
        int[] rgbArray = new int[width * height];
        for (int y = height - 1; y >= 0; y--) {
        	int i = y * width;
        	for (int x = 0; x < width; x++) {
	            int r = pixels.get() & 0xFF;
	            int g = pixels.get() & 0xFF;
	            int b = pixels.get() & 0xFF;
	            int a = pixels.get() & 0xFF;
	            rgbArray[i++] = (a << 24) | (r << 16) | (g << 8) | b;
        	}
        }
        image.setRGB(0, 0, width, height, rgbArray, 0, width);
        //
        write(file, fmt, image);
	}
	
	private void write(File file, String fmt, BufferedImage image) throws IOException {
		ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(image);
		Iterator<javax.imageio.ImageWriter> writers = ImageIO.getImageWriters(type, fmt);
        if (!writers.hasNext()) throw new IOException("Unsupported format: " + fmt);
        javax.imageio.ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(Settings.IMG_SAVE_QUALITY.getFloat());
        }
        file.delete();
        try (FileImageOutputStream output = new FileImageOutputStream(file);) {
	        writer.setOutput(output);
	        writer.write(null, new IIOImage(image, null, null), param);
	        writer.dispose();
        }
	}
}
