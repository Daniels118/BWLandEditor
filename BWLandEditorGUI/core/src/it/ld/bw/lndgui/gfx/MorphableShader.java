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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;

import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.utils.UChangeListener;

public class MorphableShader extends DefaultShader {
	private static String vscode;
	
	protected int u_heightMap;
	protected int u_mapSize;
	protected int u_maxHeight;
	
	private Texture nullTexture;
	
	public MorphableShader(Renderable renderable) {
		super(renderable, makeConfig(renderable));
	}
	
	private static Config makeConfig(Renderable renderable) {
		Config conf = new Config();
		conf.vertexShader = createPrefix(renderable, conf) + getVSCode();
		return conf;
	}
	
	private static String getVSCode() {
		if (vscode == null) {
			vscode = Gdx.files.internal("shaders/morphable.vs.glsl").readString();
		}
		return vscode;
	}
	
	@Override
	public boolean canRender(Renderable renderable) {
		if (renderable.material == null) return false;
    	return renderable.material.has(MorphableAttribute.MORPHABLE) &&
    			super.canRender(renderable);
	}
	
	@Override
	public void init() {
		super.init();
		u_heightMap = program.getUniformLocation("u_heightMap");
		u_mapSize = program.getUniformLocation("u_mapSize");
		u_maxHeight = program.getUniformLocation("u_maxHeight");
	}
	
	@Override
	protected void bindMaterial(Attributes attributes) {
		super.bindMaterial(attributes);
		MorphableAttribute attr = attributes.get(MorphableAttribute.class, MorphableAttribute.MORPHABLE);
		if (attr.land == null || attr.land.getHeightMap() == null) {
			if (nullTexture == null) {
				int c = LH3DLandCell.WET_ALTITUDE;
				int rgba = (c << 24) | 0x000000FF;
				Pixmap pixmap = new Pixmap(1, 1, Format.RGB888);
				pixmap.drawPixel(0, 0, rgba);
				nullTexture = new Texture(pixmap);
				pixmap.dispose();
			}
			program.setUniformi(u_heightMap, context.textureBinder.bind(nullTexture));
			program.setUniformf(u_mapSize, 1f);
			program.setUniformf(u_maxHeight, 0f);
		} else {
			program.setUniformi(u_heightMap, context.textureBinder.bind(attr.land.getHeightMap()));
			program.setUniformf(u_mapSize, attr.land.land.getSideLen());
			program.setUniformf(u_maxHeight, attr.land.land.getMaxHeight());
		}
	}
	
	@Override
	public void dispose() {
		if (nullTexture != null) nullTexture.dispose();
		super.dispose();
	}
	
	
	public static class MorphableAttribute extends Attribute {
		public final static String MORPHABLE_ALIAS = "morphable";
    	public final static long MORPHABLE = register(MORPHABLE_ALIAS);
    	
    	private Land3D land;
    	private final TextureDescriptor<Texture> heightMapDescriptor = new TextureDescriptor<>();
    	
    	public MorphableAttribute() {
    		super(MORPHABLE);
    		heightMapDescriptor.minFilter = TextureFilter.Linear;
    		heightMapDescriptor.magFilter = TextureFilter.Linear;
    		heightMapDescriptor.uWrap = TextureWrap.ClampToEdge;
    		heightMapDescriptor.vWrap = TextureWrap.ClampToEdge;
    	}
    	
    	public void setLand(Land3D land) {
    		if (this.land != null) {
    			this.land.listeners.remove(landChangeListener);
    			heightMapDescriptor.texture = null;
    		}
    		this.land = land;
    		if (land != null) {
    			heightMapDescriptor.texture = land.getHeightMap();
    			land.listeners.add(landChangeListener);
    		}
    	}
    	
    	private final UChangeListener landChangeListener = new UChangeListener() {
			@Override
			public void onChange(UEvent event) {
				if (event.getProperty() == Land3D.Property.BLOCKS && event.isLast()) {
					heightMapDescriptor.texture = land.getHeightMap();
				}
			}
    	};
    	
    	@Override
		public Attribute copy() {
			MorphableAttribute a = new MorphableAttribute();
			a.setLand(this.land);
			return a;
		}
    	
		@Override
		public int compareTo(Attribute o) {
			return (int)(MORPHABLE - o.type);
		}
	}
}
