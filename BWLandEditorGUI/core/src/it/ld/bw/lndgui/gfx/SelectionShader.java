/* Copyright (c) 2024-2026 Daniele Lombardi / Daniels118
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SelectionShader implements Shader {
	private final ShaderProgram program;
	
	private int u_projViewTrans;
	private int u_shift;
	
	private float shift;
	
	public SelectionShader(ShaderProgram program) {
		this.program = program;
	}
	
	@Override
	public boolean canRender(Renderable renderable) {
		if (renderable.material == null) return false;
    	return renderable.material.has(SelectionAttribute.SELECTION);
	}
	
	@Override
	public void init() {
		if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
		}
		u_projViewTrans	= program.getUniformLocation("u_projViewTrans");
        u_shift			= program.getUniformLocation("u_shift");
	}
	
	public void act(float delta) {
		shift += delta;
		if (shift > 1f) {
			shift -= 1f;
		}
	}
	
	@Override
	public void begin(Camera camera, RenderContext context) {
		context.setDepthTest(GL20.GL_LEQUAL);
    	context.setCullFace(0);
    	context.setBlending(false, 0, 0);
    	context.setDepthMask(false);
        //
    	program.bind();
    	program.setUniformMatrix(u_projViewTrans, camera.combined);
    	program.setUniformf(u_shift, shift);
	}
	
	@Override
	public void render(Renderable renderable) {
		renderable.meshPart.render(program);
	}
	
	@Override
	public void end() {
		
	}
	
	@Override
	public void dispose() {
		program.dispose();
	}
	
	@Override
	public int compareTo(Shader arg0) {
		return 1;	//Always last
	}
	
	
	public static class SelectionAttribute extends Attribute {
		public final static String SELECTION_ALIAS = "selection";
    	public final static long SELECTION = register(SELECTION_ALIAS);
    	
    	public SelectionAttribute() {
    		super(SELECTION);
    	}
    	
    	@Override
		public Attribute copy() {
			SelectionAttribute a = new SelectionAttribute();
			return a;
		}
    	
		@Override
		public int compareTo(Attribute o) {
			return (int)(SELECTION - o.type);
		}
	}
}
