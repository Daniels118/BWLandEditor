package it.ld.bw.lnd.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Listeners;
import it.ld.utils.Struct;
import it.ld.utils.UChangeListener.EventType;

public class SimpleMap extends Struct {
	public enum Property {PIXELS}
	public final Listeners listeners = new Listeners(this);
	
	public static final int width = 256;
	public static final int height = 256;
	
	public final byte[] data = new byte[width * height];
	
	public SimpleMap() {}
	
	public SimpleMap(byte[] data) {
		if (data.length != this.data.length) throw new IllegalArgumentException("Data must be "+this.data.length+" bytes");
		System.arraycopy(data, 0, this.data, 0, data.length);
	}
	
	@Override
	public SimpleMap clone() {
		SimpleMap r = new SimpleMap();
		System.arraycopy(data, 0, r.data, 0, data.length);
		return r;
	}
	
	public void invert() {
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (255 - (data[i] & 0xFF));
		}
		listeners.notify(EventType.CHANGE, Property.PIXELS, null, data);
	}
	
	public void read(File file) throws Exception {
		try (EndianDataInputStream str = new EndianDataInputStream(new BufferedInputStream(new FileInputStream(file)));) {
			read(str);
			if (str.available() > 0) throw new Exception("Wrong size");
		} catch (Exception e) {
			throw new Exception(e.getMessage() + ", reading " + file.getName(), e);
		}
	}
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		int n = str.read(data);
		if (n != data.length) throw new Exception("Wrong size");
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		str.write(data);
	}
	
	public byte get(int x, int y) {
		x &= 0xFF;
		y &= 0xFF;
		return data[x * 256 + y];
	}
	
	public int getUnsigned(int x, int y) {
		return get(x, y) & 0xFF;
	}
	
	public byte[] getPixels() {
		return data;
	}
	
	public void setPixels(byte[] pixels) {
		if (pixels.length != data.length) throw new IllegalArgumentException("Wrong data size");
		System.arraycopy(pixels, 0, data, 0, data.length);
		listeners.notify(EventType.CHANGE, Property.PIXELS, null, data);
	}
	
	public static SimpleMap load(File file) throws Exception {
		SimpleMap r = new SimpleMap();
		r.read(file);
		return r;
	}
}
