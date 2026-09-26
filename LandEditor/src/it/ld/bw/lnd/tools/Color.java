package it.ld.bw.lnd.tools;

public class Color {
	public float r, g, b, a;
	
	public Color() {}
	
	public Color(int r, int g, int b, int a) {
		this.r = (float)r / 255f;
		this.g = (float)g / 255f;
		this.b = (float)b / 255f;
		this.a = (float)a / 255f;
	}
	
	public Color set(Color color) {
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
		this.a = color.a;
		return this;
	}
	
	public static void argb8888ToColor(Color color, int value) {
		color.a = ((value & 0xff000000) >>> 24) / 255f;
		color.r = ((value & 0x00ff0000) >>> 16) / 255f;
		color.g = ((value & 0x0000ff00) >>> 8) / 255f;
		color.b = ((value & 0x000000ff)) / 255f;
	}
	
	public void lerp(final Color target, final float t) {
		this.r += t * (target.r - this.r);
		this.g += t * (target.g - this.g);
		this.b += t * (target.b - this.b);
		this.a += t * (target.a - this.a);
	}
	
	public static int argb8888(Color color) {
		return ((int)(color.a * 255) << 24) | ((int)(color.r * 255) << 16) | ((int)(color.g * 255) << 8) | (int)(color.b * 255);
	}
}