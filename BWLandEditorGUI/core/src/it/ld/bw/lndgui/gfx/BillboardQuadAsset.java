package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Disposable;

public class BillboardQuadAsset implements Disposable {
    public Model model;
    
    public BillboardQuadAsset(TextureRegion textureRegion, float width, float height, boolean vertical, boolean depthTest) {
    	this(TextureAttribute.createDiffuse(textureRegion), width, height, vertical, 0f, depthTest);
    }
    
    public BillboardQuadAsset(Texture texture, float width, float height, boolean vertical, boolean depthTest) {
    	this(TextureAttribute.createDiffuse(texture), width, height, vertical, 0f, depthTest);
    }
    
    public BillboardQuadAsset(TextureRegion textureRegion, float width, float height, boolean vertical, float baseY, boolean depthTest) {
    	this(TextureAttribute.createDiffuse(textureRegion), width, height, vertical, baseY, depthTest);
    }
    
    public BillboardQuadAsset(Texture texture, float width, float height, boolean vertical, float baseY, boolean depthTest) {
    	this(TextureAttribute.createDiffuse(texture), width, height, vertical, baseY, depthTest);
    }
    
    private BillboardQuadAsset(TextureAttribute diffuseTexture, float width, float height, boolean vertical, float baseY, boolean depthTest) {
        ModelBuilder builder = new ModelBuilder();

        Material mat = new Material();
        mat.set(diffuseTexture);
        mat.set(IntAttribute.createCullFace(0));
        mat.set(new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1f));
        
        DepthTestAttribute depth = new DepthTestAttribute(depthTest ? GL20.GL_LEQUAL : GL20.GL_NONE);
        depth.depthMask = false;
        mat.set(depth);
        
        if (vertical) {
        	model = builder.createRect(
                -width / 2f, baseY, 0f,
                 width / 2f, baseY, 0f,
                 width / 2f, baseY + height, 0f,
                -width / 2f, baseY + height, 0f,
                0f, 0f, 1f,
                mat,
                VertexAttributes.Usage.Position |
                VertexAttributes.Usage.Normal |
                VertexAttributes.Usage.TextureCoordinates
        	);
        } else {
        	model = builder.createRect(
                -width / 2f, baseY,  height / 2f,
                 width / 2f, baseY,  height / 2f,
                 width / 2f, baseY, -height / 2f,
                -width / 2f, baseY, -height / 2f,
                0f, 1f, 0f,
                mat,
                VertexAttributes.Usage.Position |
                VertexAttributes.Usage.Normal |
                VertexAttributes.Usage.TextureCoordinates
        	);
        }
    }

    @Override
    public void dispose() {
    	if (model != null) {
    		model.dispose();
    		model = null;
    	}
    }
}
