package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import it.ld.bw.lndgui.tools.Brush;
import it.ld.utils.UChangeListener;

public class Brush3D extends Renderable implements Disposable {
	private static final Color color = Color.WHITE;
	
	private Land3D land3d;
	private final Brush brush;
	private final OrbitCamera camera;
	private float[] vertices = new float[6];
	
	private Vector3 tmpVec = new Vector3();
	
	public Brush3D(Land3D land3d, Brush brush, OrbitCamera camera) {
		this.land3d = land3d;
		this.brush = brush;
		this.camera = camera;
		this.meshPart.primitiveType = GL20.GL_LINE_STRIP;
		this.material = new Material(
            ColorAttribute.createDiffuse(color),
            new DepthTestAttribute(GL20.GL_LEQUAL, false)
        );
		updateMesh();
		brush.listeners.add(brushChangeListener);
    }
	
	private UChangeListener brushChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.CHANGE && event.getProperty() instanceof Brush.Property) {
				Brush.Property property = (Brush.Property)event.getProperty();
				switch (property) {
					case SHAPE:
					case SIZE:
					case W:
					case H:
						updateMesh();
						break;
					case ANGLE_OFFSET:
					case ROTATE_WITH_CAMERA:
						worldTransform.getTranslation(tmpVec);
						setPosition(tmpVec.x, tmpVec.z);
						break;
					default:
				}
			}
		}
	};
	
	public void setLand(Land3D land) {
		this.land3d = land;
		this.updateElevation();
	}
	
	private void updateMesh() {
		if (this.meshPart.mesh != null) this.meshPart.mesh.dispose();
		Coord[] coords = brush.getEdgeCoords();
        this.meshPart.size = coords.length;
		this.meshPart.mesh = new Mesh(false, coords.length, 0, VertexAttribute.Position());
		vertices = new float[coords.length * 3];
        for (int i = 0, dst = 0; i < coords.length; i++) {
            coords[i].toVector3(tmpVec);
        	vertices[dst++] = tmpVec.x;
    		vertices[dst++] = tmpVec.y;
    		vertices[dst++] = tmpVec.z;
        }
	    this.updateElevation();
	}
	
	public void setPosition(float x, float z) {
		float angle = brush.getAngleOffset();
		if (brush.isRotateWithCameraEnabled()) {
			/*if (Math.abs(camera.direction.y) < 0.5f) {
				tmpVec.set(camera.direction.x, 0, camera.direction.z);
			} else {
				tmpVec.set(camera.up.x, 0, camera.up.z);
			}
			tmpVec.nor();
			float yaw = MathUtils.atan2(tmpVec.z, tmpVec.x) + MathUtils.HALF_PI;
			angle -= yaw;*/
			angle -= camera.getYaw();
		}
		this.worldTransform.setToTranslation(x, 0.3f, z);
		this.worldTransform.rotate(Vector3.Y, angle * MathUtils.radDeg);
		this.updateElevation();
	}
	
	public void updateElevation() {
		if (this.land3d != null) {
			for (int i = 0; i < vertices.length; i+=3) {
				tmpVec.set(vertices[i], 0f, vertices[i + 2]);
				tmpVec.mul(this.worldTransform);
				vertices[i + 1] = land3d.getHeight(tmpVec);
			}
		} else {
			for (int i = 0; i < vertices.length; i+=3) {
				vertices[i + 1] = 0f;
			}
		}
		this.meshPart.mesh.setVertices(vertices);
	}
	
	@Override
	public void dispose() {
		brush.listeners.remove(brushChangeListener);
		this.meshPart.mesh.dispose();
	}
}
