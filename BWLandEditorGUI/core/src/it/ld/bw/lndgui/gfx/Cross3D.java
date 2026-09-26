package it.ld.bw.lndgui.gfx;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import it.ld.bw.lnd.model.LndFile;
import it.ld.utils.UChangeListener;

public class Cross3D extends Renderable implements Disposable {
	private final float SIZE = 1f;
	
	private static final int VERT_ATTR_CNT = 3;
	private static final int POINT_LINES = 2;
	private static final int POINT_VERTICES = POINT_LINES * 2;
	private static final int POINT_ATTR_CNT = POINT_VERTICES * VERT_ATTR_CNT;
	
	private static final VertexAttribute VAPosition = VertexAttribute.Position();
	
	private Land3D land3d;
	private List<Coord> points;
	private float[] vertices = new float[0];
	
	private Vector3 tmpVec = new Vector3();
	
	public Cross3D(Land3D land3d, List<Coord> points, Color color) {
		this.land3d = land3d;
		this.meshPart.primitiveType = GL20.GL_LINES;
		this.material = new Material(new ColorAttribute(ColorAttribute.Diffuse, color));
		setLand(land3d);
		setPoints(points);
		updateMesh();
    }
	
	public void setLand(Land3D land) {
		if (this.land3d != null) {
			this.land3d.land.listeners.remove(landChangeListener);
		}
		this.land3d = land;
		if (land3d != null) {
			land3d.land.listeners.add(landChangeListener);
			this.updateElevation();
		}
	}
	
	private final UChangeListener landChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LndFile.Property.BLOCKS) {
				updateElevation();
			}
		}
	};
	
	public void setPoints(List<Coord> points) {
		this.points = points;
		updateMesh();
	}
	
	public boolean isNull() {
		return vertices.length == 0;
	}
	
	private void updateMesh() {
		if (points != null && !points.isEmpty()) {
	        meshPart.size = points.size() * POINT_VERTICES;
	        if (meshPart.mesh == null || meshPart.mesh.getMaxVertices() < meshPart.size) {
	        	if (meshPart.mesh != null) {
	        		meshPart.mesh.dispose();
	        	}
	        	meshPart.mesh = new Mesh(false, meshPart.size * 2, 0, VAPosition);
	        }
			vertices = new float[points.size() * POINT_ATTR_CNT];
			for (int i = 0, dst = 0; i < points.size(); i++) {
				Coord coord = points.get(i);
	            coord.toVector3(tmpVec);
	        	
	    		vertices[dst++] = tmpVec.x - SIZE;
	    		vertices[dst++] = tmpVec.y;
	    		vertices[dst++] = tmpVec.z - SIZE;
	    		vertices[dst++] = tmpVec.x + SIZE;
	    		vertices[dst++] = tmpVec.y;
	    		vertices[dst++] = tmpVec.z + SIZE;
	    		
	    		vertices[dst++] = tmpVec.x - SIZE;
	    		vertices[dst++] = tmpVec.y;
	    		vertices[dst++] = tmpVec.z + SIZE;
	    		vertices[dst++] = tmpVec.x + SIZE;
	    		vertices[dst++] = tmpVec.y;
	    		vertices[dst++] = tmpVec.z - SIZE;
	        }
		} else {
			meshPart.size = 0;
			vertices = new float[0];
		}
		updateElevation();
	}
	
	public void updateElevation() {
		if (points != null) {
			if (this.land3d != null) {
				for (int src = 0, dst = 0; src < points.size(); src++, dst+=POINT_ATTR_CNT) {
					tmpVec.set(vertices[dst], 0f, vertices[dst + 2]);
					Coord point = points.get(src);
					float h = land3d.getHeight(tmpVec) + point.y + 0.5f;
					vertices[dst +  1] = h;
					vertices[dst +  4] = h;
					vertices[dst +  7] = h;
					vertices[dst + 10] = h;
				}
			} else {
				for (int src = 0, dst = 0; src < points.size(); src++, dst+=POINT_ATTR_CNT) {
					Coord point = points.get(src);
					float h = point.y + 0.5f;
					vertices[dst +  1] = h;
					vertices[dst +  4] = h;
					vertices[dst +  7] = h;
					vertices[dst + 10] = h;
				}
			}
			if (meshPart.mesh != null) {
				meshPart.mesh.setVertices(vertices);
			}
		}
	}
	
	@Override
	public void dispose() {
		setPoints(null);
		setLand(null);
		if (meshPart.mesh != null) {
			meshPart.mesh.dispose();
		}
	}
}
