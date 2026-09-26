package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

public class LandMeasure extends Renderable implements Disposable {
	private final float CROSS_SIZE = 5f;
	private final float[] vertices = new float[4 * 2 * 4];
	
	public LandMeasure() {
    	this.material = new Material(
            new DepthTestAttribute(GL20.GL_ALWAYS, false)
        );
    	
    	vertices[3] = Color.WHITE_FLOAT_BITS;
    	vertices[7] = Color.WHITE_FLOAT_BITS;
    	vertices[11] = Color.WHITE_FLOAT_BITS;
    	vertices[15] = Color.WHITE_FLOAT_BITS;
    	vertices[19] = Color.WHITE_FLOAT_BITS;
    	vertices[23] = Color.WHITE_FLOAT_BITS;
    	vertices[27] = Color.WHITE_FLOAT_BITS;
    	vertices[27] = Color.YELLOW.toFloatBits();
    	vertices[31] = Color.YELLOW.toFloatBits();
    	
    	this.meshPart.primitiveType = GL20.GL_LINES;
		this.meshPart.size = 4 * 2;
		this.meshPart.mesh = new Mesh(false, meshPart.size, 0, VertexAttribute.Position(), VertexAttribute.ColorPacked());
		this.meshPart.mesh.setVertices(vertices);
    }
	
	public void setStart(Vector3 p0) {
		vertices[0] = p0.x - CROSS_SIZE;
		vertices[1] = p0.y + 0.5f;
		vertices[2] = p0.z;
		vertices[4] = p0.x + CROSS_SIZE;
		vertices[5] = p0.y + 0.5f;
		vertices[6] = p0.z;
		
		vertices[8] = p0.x;
		vertices[9] = p0.y + 0.5f;
		vertices[10] = p0.z - CROSS_SIZE;
		vertices[12] = p0.x;
		vertices[13] = p0.y + 0.5f;
		vertices[14] = p0.z + CROSS_SIZE;
		
		vertices[16] = p0.x;
		vertices[17] = p0.y - CROSS_SIZE;
		vertices[18] = p0.z;
		vertices[20] = p0.x;
		vertices[21] = p0.y + CROSS_SIZE;
		vertices[22] = p0.z;
		
		vertices[24] = p0.x;
		vertices[25] = p0.y + 0.5f;
		vertices[26] = p0.z;
		this.meshPart.mesh.setVertices(vertices);
	}
	
	public void setEnd(Vector3 p1) {
		vertices[28] = p1.x;
		vertices[29] = p1.y + 0.5f;
		vertices[30] = p1.z;
		this.meshPart.mesh.setVertices(vertices);
	}
    
	@Override
	public void dispose() {
		this.meshPart.mesh.dispose();
	}
}