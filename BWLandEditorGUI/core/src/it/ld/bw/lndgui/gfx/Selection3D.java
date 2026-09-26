package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lndgui.tools.Selection;
import it.ld.utils.UChangeListener;

public class Selection3D extends Renderable implements Disposable {
	private static final int VERT_ATTR_CNT = 4;
	
	private static final VertexAttribute VAPosition = new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position");
	private static final VertexAttribute VALen = new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_len");
	
	private Land3D land3d;
	private Selection selection;
	private float[] vertices;
	
	private Vector3 tmpVec = new Vector3();
	
	public Selection3D(Land3D land3d, Selection selection) {
		this.land3d = land3d;
		this.meshPart.primitiveType = GL20.GL_LINE_LOOP;
		this.material = new Material(new SelectionShader.SelectionAttribute());
		setLand(land3d);
		setSelection(selection);
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
	
	public void setSelection(Selection selection) {
		if (this.selection != null) {
			this.selection.listeners.remove(selectionChangeListener);
		}
		this.selection = selection;
		updateMesh();
		if (selection != null) {
			selection.listeners.add(selectionChangeListener);
		}
	}
	
	private final UChangeListener selectionChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			updateMesh();
		}
	};
	
	public boolean isNull() {
		return vertices.length == 0;
	}
	
	private void updateMesh() {
		if (selection != null && selection.size() >= 2) {
	        meshPart.size = selection.size();
	        if (meshPart.mesh == null || meshPart.mesh.getMaxVertices() < meshPart.size) {
	        	if (meshPart.mesh != null) {
	        		meshPart.mesh.dispose();
	        	}
	        	meshPart.mesh = new Mesh(false, meshPart.size * 2, 0, VAPosition, VALen);
	        }
			vertices = new float[selection.size() * VERT_ATTR_CNT];
			float len = 0;
			Coord prev = selection.get(0);
	        for (int i = 0, dst = 0; i < selection.size(); i++) {
	            Coord coord = selection.get(i);
	            len += coord.dst(prev);
	        	coord.toVector3(tmpVec);
	        	vertices[dst++] = tmpVec.x;
	    		vertices[dst++] = tmpVec.y;
	    		vertices[dst++] = tmpVec.z;
	    		vertices[dst++] = len;
	    		prev = coord;
	        }
		} else {
			meshPart.size = 0;
			vertices = new float[0];
		}
		updateElevation();
	}
	
	public void updateElevation() {
		if (this.land3d != null) {
			for (int i = 0; i < vertices.length; i+=VERT_ATTR_CNT) {
				tmpVec.set(vertices[i], 0f, vertices[i + 2]);
				vertices[i + 1] = land3d.getHeight(tmpVec);
			}
		} else {
			for (int i = 0; i < vertices.length; i+=VERT_ATTR_CNT) {
				vertices[i + 1] = 0f;
			}
		}
		if (meshPart.mesh != null) {
			meshPart.mesh.setVertices(vertices);
		}
	}
	
	@Override
	public void dispose() {
		setSelection(null);
		setLand(null);
		if (meshPart.mesh != null) {
			meshPart.mesh.dispose();
		}
	}
}
