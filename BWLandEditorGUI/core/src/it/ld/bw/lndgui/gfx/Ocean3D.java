package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;


public class Ocean3D implements RenderableProvider, Disposable {
	private final FrustumPlaneIntersectionMesh oceanClip;
	private final Renderable renderable = new Renderable();
	
	private boolean isVisible = false;
	
	public Ocean3D(Texture texture, float alpha, float uvScale) {
		oceanClip = new FrustumPlaneIntersectionMesh(uvScale);
		//
		Material material = new Material(
            TextureAttribute.createDiffuse(texture),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, alpha),
            new DepthTestAttribute(GL20.GL_LEQUAL, false),
            IntAttribute.createCullFace(GL20.GL_NONE)
        );
		//
		renderable.material = material;
		renderable.meshPart.mesh = oceanClip.getMesh();
        renderable.meshPart.offset = 0;
        renderable.meshPart.size = 6;
        renderable.meshPart.primitiveType = com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
        renderable.worldTransform.idt();
        renderable.environment = null;
	}
	
	public void setTexture(Texture texture) {
		renderable.material.set(TextureAttribute.createDiffuse(texture));
	}
	
	public void update(Camera camera) {
		this.isVisible = oceanClip.update(camera);
	}
	
	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		if (isVisible) {
			renderables.add(renderable);
		}
	}
	
	@Override
	public void dispose() {
		oceanClip.dispose();
	}
}
