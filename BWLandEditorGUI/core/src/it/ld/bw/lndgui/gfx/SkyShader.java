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
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SkyShader implements Shader {
	private final ShaderProgram program;
	
	private int u_invProjView;
	private int u_camPos;
	private int u_zenithColor;
	private int u_horizonColor;
	private int u_belowColor;
	private int u_gradientPower;
	private int u_belowPower;
	
	public SkyShader(ShaderProgram program) {
		this.program = program;
	}
	
	@Override
	public boolean canRender(Renderable renderable) {
		if (renderable.material == null) return false;
    	return renderable.material.has(SkyAttribute.SKY);
	}
	
	@Override
	public void init() {
		if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
		}
		u_invProjView	= program.getUniformLocation("u_invProjView");
        u_camPos		= program.getUniformLocation("u_camPos");
        u_zenithColor	= program.getUniformLocation("u_zenithColor");
        u_horizonColor	= program.getUniformLocation("u_horizonColor");
        u_belowColor	= program.getUniformLocation("u_belowColor");
        u_gradientPower	= program.getUniformLocation("u_gradientPower");
        u_belowPower	= program.getUniformLocation("u_belowPower");
	}
	
	@Override
	public void begin(Camera camera, RenderContext context) {
		context.setDepthTest(0);
    	context.setCullFace(0);
    	context.setBlending(false, 0, 0);
    	context.setDepthMask(false);
        //
    	program.bind();
    	program.setUniformMatrix(u_invProjView, camera.invProjectionView);
    	program.setUniformf(u_camPos, camera.position);
	}
	
	@Override
	public void render(Renderable renderable) {
		program.setUniformf(u_zenithColor, 0.3f, 0.7f, 0.9f);
		program.setUniformf(u_horizonColor, 1.0f, 1.0f, 1.0f);
		program.setUniformf(u_belowColor, 0.3f, 0.8f, 0.95f);
		program.setUniformf(u_gradientPower, 0.3f);
		program.setUniformf(u_belowPower, 0.2f);
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
		return -1;	//Sky always first
	}
	
	
	public static class SkyAttribute extends Attribute {
		public final static String SKY_ALIAS = "sky";
    	public final static long SKY = register(SKY_ALIAS);
    	
    	public SkyAttribute() {
    		super(SKY);
    	}
    	
    	@Override
		public Attribute copy() {
			SkyAttribute a = new SkyAttribute();
			return a;
		}
    	
    	@Override
    	protected boolean equals(Attribute other) {
            return true;
        }
    	
		@Override
		public int compareTo(Attribute o) {
			return (int)(SKY - o.type);
		}
	}
}
