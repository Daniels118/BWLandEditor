package it.ld.bw.lndgui.tools;

import java.util.ArrayList;

import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.gfx.Coord;
import it.ld.utils.Listeners;
import it.ld.utils.UChangeListener.EventType;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public abstract class Brush implements Json.Serializable {
	public enum Property {NAME, SHAPE, SIZE, W, H, FLOW, SMOOTHNESS, ROTATE_WITH_CAMERA, ANGLE_OFFSET}
	public final Listeners listeners = new Listeners(this);
	
	public enum Shape {
		CIRCLE, TRIANGLE;
		
		private String text;
		
		@Override
		public String toString() {
			if (text == null) {
				text = I18n.tr("brush.shape."+this.name());
			}
			return text;
		}
	}
	
	private String name = "Default";
	private Shape shape = Shape.CIRCLE;
	private float size = 50f;
	private float w = 1f;
	private float h = 1f;
	private float flow = 100f;
	private float smoothness = 0.5f;
	private boolean rotateWithCamera = true;
	private float angleOffset = 0f;
	
	private boolean started;
	private Coord[] edgeCoords;
	
	public void set(Brush ref) {
		this.setName(ref.name);
		this.setShape(ref.shape);
		this.setSize(ref.size);
		this.setW(ref.w);
		this.setH(ref.h);
		this.setFlow(ref.flow);
		this.setSmoothness(ref.smoothness);
		this.setRotateWithCamera(ref.rotateWithCamera);
		this.setAngleOffset(ref.angleOffset);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		if (name == null) throw new IllegalArgumentException("Name cannot be null");
		if (!name.equals(this.name)) {
			Object oldValue = this.name;
			this.name = name;
			listeners.notify(EventType.CHANGE, Property.NAME, oldValue, this.name);
		}
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public void setShape(Shape shape) {
		if (shape != this.shape) {
			Object oldValue = this.shape;
			this.shape = shape;
			this.edgeCoords = null;
			listeners.notify(EventType.CHANGE, Property.SHAPE, oldValue, this.shape);
		}
	}
	
	public float getSize() {
		return size;
	}
	
	public void setSize(float size) {
		if (size <= 0) throw new IllegalArgumentException("Size must be grater than zero");
		if (size != this.size) {
			Object oldValue = this.size;
			this.size = size;
			this.edgeCoords = null;
			listeners.notify(EventType.CHANGE, Property.SIZE, oldValue, this.size);
		}
	}
	
	public float getW() {
		return w;
	}
	
	public void setW(float w) {
		if (w <= 0) throw new IllegalArgumentException("Width must be grater than zero");
		if (w != this.w) {
			Object oldValue = this.w;
			this.w = w;
			this.edgeCoords = null;
			listeners.notify(EventType.CHANGE, Property.W, oldValue, this.w);
		}
	}
	
	public float getH() {
		return h;
	}
	
	public void setH(float h) {
		if (h <= 0) throw new IllegalArgumentException("Height must be grater than zero");
		if (h != this.h) {
			Object oldValue = this.h;
			this.h = h;
			this.edgeCoords = null;
			listeners.notify(EventType.CHANGE, Property.H, oldValue, this.h);
		}
	}
	
	public float getProportions() {
		return w / h;
	}
	
	public float getWidth() {
		return w >= h ? size : (size * w / h);
	}
	
	public float getHeight() {
		return h >= w ? size : (size * h / w);
	}
	
	public float getFlow() {
		return flow;
	}
	
	public void setFlow(float flow) {
		if (flow != this.flow) {
			Object oldValue = this.flow;
			this.flow = flow;
			listeners.notify(EventType.CHANGE, Property.FLOW, oldValue, this.flow);
		}
	}

	public float getSmoothness() {
		return smoothness;
	}
	
	public void setSmoothness(float smoothness) {
		if (smoothness < 0) throw new IllegalArgumentException("Smoothness must be positive");
		if (smoothness != this.smoothness) {
			Object oldValue = this.smoothness;
			this.smoothness = smoothness;
			listeners.notify(EventType.CHANGE, Property.SMOOTHNESS, oldValue, this.smoothness);
		}
	}
	
	public boolean isRotateWithCameraEnabled() {
		return rotateWithCamera;
	}
	
	public void setRotateWithCamera(boolean value) {
		if (value != this.rotateWithCamera) {
			Object oldValue = this.rotateWithCamera;
			this.rotateWithCamera = value;
			listeners.notify(EventType.CHANGE, Property.ROTATE_WITH_CAMERA, oldValue, this.rotateWithCamera);
		}
	}
	
	public float getAngleOffset() {
		return angleOffset;
	}
	
	public void setAngleOffset(float angleOffset) {
		while (angleOffset < -MathUtils.PI) {
			angleOffset += MathUtils.PI2;
		}
		while (angleOffset > MathUtils.PI) {
			angleOffset -= MathUtils.PI2;
		}
		if (Math.abs(angleOffset - this.angleOffset) > MathUtils.degreesToRadians) {
			Object oldValue = this.angleOffset;
			this.angleOffset = angleOffset;
			listeners.notify(EventType.CHANGE, Property.ANGLE_OFFSET, oldValue, this.angleOffset);
		}
	}
	
	@Override
    public void write(Json json) {
		json.writeValue("name", this.name);
		json.writeValue("shape", this.shape.name());
		json.writeValue("size", this.size);
		json.writeValue("w", this.w);
		json.writeValue("h", this.h);
		json.writeValue("flow", this.flow);
		json.writeValue("smoothness", this.smoothness);
		json.writeValue("rotateWithCamera", this.rotateWithCamera);
	}
	
	@Override
    public void read(Json json, JsonValue jsonData) {
		if (jsonData.has("name")) setName(jsonData.getString("name"));
		if (jsonData.has("shape")) setShape(Shape.valueOf(jsonData.getString("shape")));
		if (jsonData.has("size")) setSize(jsonData.getFloat("size"));
		if (jsonData.has("w")) setW(jsonData.getFloat("w"));
		if (jsonData.has("h")) setH(jsonData.getFloat("h"));
		if (jsonData.has("flow")) setFlow(jsonData.getFloat("flow"));
		if (jsonData.has("smoothness")) setSmoothness(jsonData.getFloat("smoothness"));
		if (jsonData.has("rotateWithCamera")) setRotateWithCamera(jsonData.getBoolean("rotateWithCamera"));
	}
	
	private ArrayList<Coord> transformedEdgeCoords = new ArrayList<>();
	
	public ArrayList<Coord> getEdgeCoords(Coord coord, float cameraAngle) {
		final float angle = angleOffset - (rotateWithCamera ? cameraAngle : 0);
		Coord[] coords = getEdgeCoords();
		while (transformedEdgeCoords.size() < coords.length) {
			transformedEdgeCoords.add(new Coord());
		}
		while (transformedEdgeCoords.size() > coords.length) {
			transformedEdgeCoords.remove(transformedEdgeCoords.size() - 1);
		}
		float cos = (float)Math.cos(angle);
		float sin = (float)Math.sin(angle);
		for (int i = 0; i < coords.length; i++) {
			Coord c = coords[i];
			Coord o = transformedEdgeCoords.get(i);
			o.x = coord.x + c.x * cos - c.z * sin;
			o.z = coord.z + c.x * sin + c.z * cos;
		}
		return transformedEdgeCoords;
	}
	
	private void rotate(Coord[] coords, float angle) {
		float cos = (float)Math.cos(angle);
		float sin = (float)Math.sin(angle);
		for (int i = 0; i < coords.length; i++) {
			Coord c = coords[i];
			float x = c.x * cos - c.z * sin;
			float z = c.x * sin + c.z * cos;
			c.x = x;
			c.z = z;
		}
	}
	
	public Mask createMask(float cameraAngle) {
		final float size2 = size + 60;
		Mask mask = new Mask(size2);
		final float angle = angleOffset - (rotateWithCamera ? cameraAngle : 0f);
		final float smoothDist = Math.max(0f, Math.min(getWidth(), getHeight()) / 2 * smoothness);
		Coord[] polygon = createEdgeCoords(-1);
		rotate(polygon, angle);
		final float halfSize = size2 * 0.5f;
		for (float z = -halfSize; z <= halfSize; z += LH3DLandCell.CELL_SIZE) {
			for (float x = -halfSize; x <= halfSize; x += LH3DLandCell.CELL_SIZE) {
				float minDist = Float.POSITIVE_INFINITY;
				for (int i = 1; i < polygon.length; i++) {
					Coord a = polygon[i - 1];
					Coord b = polygon[i];
					float d = distancePointToSegment(x, z, a.x, a.z, b.x, b.z);
					if (d < minDist) {
						minDist = d;
					}
				}
				if (isPointInsidePolygon(x, z, polygon)) {
					float weight;
					if (smoothDist <= 0f) {
						weight = 1f;
					} else if (minDist >= smoothDist) {
						weight = 1f;
					} else {
						float t = minDist / smoothDist;
						weight = t;//t * t * (3f - 2f * t); // smoothstep
					}
					mask.set(x, z, weight);
				} else {
					mask.set(x, z, -minDist);
				}
			}
		}
		return mask;
	}
	
	public Coord[] getEdgeCoords() {
		if (edgeCoords == null) {
			edgeCoords = createEdgeCoords(LH3DLandCell.CELL_SIZE);
		}
		return edgeCoords;
	}
	
	public Coord[] createEdgeCoords(float step) {
		final float width = getWidth();
		final float height = getHeight();
		switch (shape) {
			case CIRCLE:
				if (step < 0) step = LH3DLandCell.CELL_SIZE;
				return createCircle(width, height, step);
			case TRIANGLE:
				if (step < 0) step = size;
				return createTriangle(width, height, step);
			default:
				throw new RuntimeException("Unimplemented shape: " + shape);
		}
	}
	
	private static Coord[] createCircle(float width, float height, float step) {
		final float halfw = width / 2;
		final float halfh = height / 2;
		final float perimeter = (float)Math.PI * Math.max(width, height);
        final int steps = Math.max(5, (int)Math.ceil(perimeter / step));
        
        Coord[] coords = new Coord[steps];
        for (int i = 0; i < steps; i++) {
            float angle = (float)Math.PI*2 * (float)i / (steps - 1);
            coords[i] = new Coord(halfw * (float)Math.cos(angle), 0, halfh * (float)Math.sin(angle));
        }
	    return coords;
	}
	
	private static Coord[] createTriangle(float width, float height, float step) {
		final float halfw = width / 2;
		final float halfh = height / 2;
		final float base = width;
		final float side = (float)Math.sqrt(halfw*halfw + height*height);
		final int baseSteps = Math.max(2, (int)Math.ceil(base / step));
		final int sideSteps = Math.max(2, (int)Math.ceil(side / step));
        final int steps = baseSteps + 2 * sideSteps - 2;
        
        Coord[] coords = new Coord[steps];
		int dst = 0;
        for (int i = 0; i < baseSteps; i++) {
        	float p = (float)i / (baseSteps - 1);
        	coords[dst++] = new Coord(p * base - halfw, 0, -halfh);
        }
        for (int i = 1; i < sideSteps; i++) {
            float p = (float)i / (sideSteps - 1);
        	coords[dst++] = new Coord(halfw - p * halfw, 0, p * height - halfh);
        }
        for (int i = 1; i < sideSteps; i++) {
            float p = (float)i / (sideSteps - 1);
        	coords[dst++] = new Coord(-p * halfw, 0, halfh - p * height);
        }
        return coords;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public final void begin(Coord coord) {
		this.startImpl(coord);
		this.started = true;
	}
	
	public final void apply(float dt, Coord coord, float cameraAngle, boolean shift) {
		if (started) {
			applyImpl(dt, coord, cameraAngle, shift);
		}
	}
	
	public final void end() {
		if (started) {
			this.endImpl();
			started = false;
		}
	}
	
	public boolean isStarted() {
		return this.started;
	}
	
	protected abstract void startImpl(Coord coord);
	protected abstract void applyImpl(float dt, Coord coord, float cameraAngle, boolean shift);
	protected abstract void endImpl();
	public abstract String getDescription();
	
	
	private static boolean isPointInsidePolygon(float px, float pz, Coord[] polygon) {
		boolean inside = false;
		for (int i = 1; i < polygon.length; i++) {
			Coord p0 = polygon[i - 1];
			Coord p1 = polygon[i];

			boolean intersect =
				((p0.z > pz) != (p1.z > pz)) &&
				(px < (p1.x - p0.x) * (pz - p0.z) / (p1.z - p0.z) + p0.x);

			if (intersect) {
				inside = !inside;
			}
		}
		return inside;
	}

	private static float distancePointToSegment(float px, float pz, float ax, float az, float bx, float bz) {
		float abx = bx - ax;
		float abz = bz - az;
		float apx = px - ax;
		float apz = pz - az;

		float abLen2 = abx * abx + abz * abz;
		if (abLen2 == 0f) {
			float dx = px - ax;
			float dz = pz - az;
			return (float)Math.sqrt(dx * dx + dz * dz);
		}

		float t = (apx * abx + apz * abz) / abLen2;
		t = Math.max(0f, Math.min(1f, t));

		float cx = ax + t * abx;
		float cz = az + t * abz;

		float dx = px - cx;
		float dz = pz - cz;
		return (float)Math.sqrt(dx * dx + dz * dz);
	}
	
	
	public static class Mask {
		private final int side;
		private final float offset;
		private final float[] weights;
		
		private float cx;
		private float cz;
		
		public Mask(float size) {
			this.side = (int)Math.ceil((size + LH3DLandCell.CELL_SIZE) / LH3DLandCell.CELL_SIZE);
			this.offset = size / 2 + 0.5f;
			this.weights = new float[side * side];
		}
		
		public void set(float x, float z, float value) {
			int x0 = (int)Math.round((offset + x) / LH3DLandCell.CELL_SIZE);
			int z0 = (int)Math.round((offset + z) / LH3DLandCell.CELL_SIZE);
			if (x0 >= 0 && x0 < side && z0 >= 0 && z0 < side) {
	            weights[x0 + z0 * side] = value;
	        }
		}
		
		public void setPosition(float x, float z) {
			this.cx = x;
			this.cz = z;
		}
		
		public float getLeft() {
			return cx - offset;
		}
		
		public float getBottom() {
			return cz - offset;
		}
		
		public float getRight() {
			return cx + offset;
		}
		
		public float getTop() {
			return cz + offset;
		}
		
		public float get(float x, float z) {
			x = (offset + (x - cx)) / LH3DLandCell.CELL_SIZE;
			z = (offset + (z - cz)) / LH3DLandCell.CELL_SIZE;
			int x0 = (int)Math.floor(x);
			int z0 = (int)Math.floor(z);
			int x1 = x0 + 1;
			int z1 = z0 + 1;
			float wx1 = x - x0;
			float wx0 = 1f - wx1;
			float wz1 = z - z0;
			float wz0 = 1f - wz1;
			float w00 = 0;
			float w01 = 0;
			float w10 = 0;
			float w11 = 0;
			if (x0 >= 0 && x0 < side) {
				if (z0 >= 0 && z0 < side) w00 = weights[x0 + z0 * side];
				if (z1 >= 0 && z1 < side) w01 = weights[x0 + z1 * side];
			}
			if (x1 >= 0 && x1 < side) {
				if (z0 >= 0 && z0 < side) w10 = weights[x1 + z0 * side];
				if (z1 >= 0 && z1 < side) w11 = weights[x1 + z1 * side];
			}
			return (w00 * wx0*wz0 + w01*wx0*wz1 + w10*wx1*wz0 + w11*wx1*wz1) * 0.25f;
		}
		
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder(side * side * 5);
			int i = 0;
			for (int z = 0; z < side; z++) {
				b.append(String.format("%+5.1f", weights[i++]));
				for (int x = 1; x < side; x++) {
					b.append(String.format(" %+5.1f", weights[i++]));
				}
				b.append("\n");
			}
			return b.toString();
		}
	}
}
