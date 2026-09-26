package it.ld.libgdx.utils;

import com.badlogic.gdx.graphics.Color;

public class HSVColor {
	private static final float EPS = 0.00001f;
	
	public float h;
	public float s;
	public float v;
	
	public void set(Color rgb) {
		float max = Math.max(Math.max(rgb.r, rgb.g), rgb.b);
		float min = Math.min(Math.min(rgb.r, rgb.g), rgb.b);
		float delta = max - min;

		if (delta == 0) {
			h = 0;
			s = 0;
			v = max;
			return;
		}

		if (max == rgb.r) {
			h = (rgb.g - rgb.b) / delta;
		    if (rgb.g < rgb.b) h += 6;
		} else if (max == rgb.g) {
			h = (rgb.b - rgb.r) / delta + 2;
		} else {
			h = (rgb.r - rgb.g) / delta + 4;
		}
		h *= 60;

		if (max == 0) {
			s = 0;
		} else {
			s = delta / max;
		}

		v = max;
	}
	
	public void get(Color rgb) {
		float hh = h / 60;
		int i = ((int) hh) % 6;

		float f = hh - i;
		float p = v * (1 - s);
		float q = v * (1 - f * s);
		float t = v * (1 - (1 - f) * s);

		switch (i) {
			case 0:
				rgb.r = v; rgb.g = t; rgb.b = q; break;
			case 1:
				rgb.r = q; rgb.g = v; rgb.b = p; break;
			case 2:
				rgb.r = p; rgb.g = v; rgb.b = t; break;
			case 3:
				rgb.r = p; rgb.g = q; rgb.b = v; break;
			case 4:
				rgb.r = t; rgb.g = p; rgb.b = v; break;
			case 5:
				rgb.r = v; rgb.g = p; rgb.b = q; break;
				default:
		}
	}
	
	@Override
	public int hashCode() {
		int result = Float.hashCode(h);
	    result = 31 * result + Float.hashCode(s);
	    result = 31 * result + Float.hashCode(v);
	    return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof HSVColor)) return false;
		HSVColor other = (HSVColor)obj;
		return Math.abs(h - other.h) < EPS
			    && Math.abs(s - other.s) < EPS
			    && Math.abs(v - other.v) < EPS;
	}
	
	@Override
	public String toString() {
		return "HSVColor("+h+", "+s+", "+v+")";
	}
}
