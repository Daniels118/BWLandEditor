package it.ld.bw.lndgui.interfaces;

import java.io.File;
import java.io.IOException;

import com.badlogic.gdx.graphics.Pixmap;

public interface ImageWriter {
	public void write(File file, int width, int height, int[] pixels) throws IOException;
	public void write(File file, Pixmap pixmap) throws IOException;
}
