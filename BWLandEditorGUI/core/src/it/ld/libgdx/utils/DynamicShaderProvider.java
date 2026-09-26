package it.ld.libgdx.utils;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.utils.Array;

public class DynamicShaderProvider extends DefaultShaderProvider {
	protected Array<Shader> managedShaders = new Array<>();
	protected Array<DynamicCreator> creators = new Array<>();
	
	public DynamicShaderProvider() {
		super();
	}
	
	public DynamicShaderProvider(Config config) {
		super(config);
	}
	
	@Override
	protected Shader createShader(Renderable renderable) {
		Shader shader = null;
		for (DynamicCreator creator : creators) {
			shader = creator.create(renderable);
			if (shader != null) break;
		}
		if (shader == null) {
			shader = super.createShader(renderable);
		}
		managedShaders.add(shader);
		return shader;
	}
	
	public DynamicShaderProvider add(Shader shader) {
		shaders.add(shader);
		return this;
	}
	
	public DynamicShaderProvider add(DynamicCreator creator) {
		creators.add(creator);
		return this;
	}
	
	@Override
	public void dispose() {
		for (Shader shader : managedShaders) {
			shader.dispose();
		}
		managedShaders.clear();
	}
	
	
	public static interface DynamicCreator {
		public Shader create(Renderable renderable);
	}
}