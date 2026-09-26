package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.utils.Disposable;

public class LandGrid extends Renderable implements Disposable {
	public LandGrid(float x0, float z0, int count, float step, Color color) {
    	this.material = new Material(
            ColorAttribute.createDiffuse(color),
            new DepthTestAttribute(GL20.GL_ALWAYS, false)
        );
    	
    	this.meshPart.primitiveType = GL20.GL_LINES;
		this.meshPart.size = (count + 1) * 4;
		this.meshPart.mesh = new Mesh(true, meshPart.size, 0, VertexAttribute.Position());
		float[] vertices = new float[meshPart.size * 3];
		final float size = count * step;
        final float y = 0f;
        for (int i = 0, dst = 0; i <= count; i++) {
        	float p = i * step;
            //
        	vertices[dst++] = x0;
    		vertices[dst++] = y;
    		vertices[dst++] = z0 + p;
    		vertices[dst++] = x0 + size;
    		vertices[dst++] = y;
    		vertices[dst++] = z0 + p;
    		//
    		vertices[dst++] = x0 + p;
    		vertices[dst++] = y;
    		vertices[dst++] = z0;
    		vertices[dst++] = x0 + p;
    		vertices[dst++] = y;
    		vertices[dst++] = z0 + size;
        }
        this.meshPart.mesh.setVertices(vertices);
    }
    
	@Override
	public void dispose() {
		this.meshPart.mesh.dispose();
	}
}