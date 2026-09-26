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

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LNDMapMaterial;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lndgui.Settings;
import it.ld.bw.lndgui.Settings.ShowCellAttrOption;
import it.ld.bw.lndgui.Settings.ShowGridOption;
import it.ld.utils.UChangeListener;

public class LandShader implements Shader {
	private final ShaderProgram program;
	private RenderContext context;
	
	private int u_proj;
	private int u_view;
	
	private int u_blockPos;
	private int u_countriesMaterials;
	private int u_materials;
	private int u_noise;
	private int u_bump;
	private int u_smallBump;
	//private int u_heightMap;
	
	private int u_blocksPerSide;
	private int u_countryToHighlight;
	private int u_shadeUnselected;
	private int u_outlineSelected;
	private int u_showAttr;
	private int u_showGrid;
	private int u_showCells;
	private int u_timeMod1;
	
	private float timeMod1;
	
	public LandShader(ShaderProgram program) {
		this.program = program;
	}
	
	@Override
	public boolean canRender(Renderable renderable) {
		if (renderable.material == null) return false;
    	return renderable.material.has(LandAttribute.LAND);
	}
	
	@Override
	public void init() {
		if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
		}
		System.err.println(program.getLog());
		u_proj		= program.getUniformLocation("u_proj");
        u_view		= program.getUniformLocation("u_view");
        //
        u_blockPos	= program.getUniformLocation("u_blockPos");
        u_countriesMaterials = program.getUniformLocation("u_countriesMaterials");
        u_materials	= program.getUniformLocation("u_materials");
        u_noise		= program.getUniformLocation("u_noise");
        u_bump		= program.getUniformLocation("u_bump");
        u_smallBump	= program.getUniformLocation("u_smallBump");
        //u_heightMap	= program.getUniformLocation("u_heightMap");
        //
        u_blocksPerSide	= program.getUniformLocation("u_blocksPerSide");
        u_countryToHighlight = program.getUniformLocation("u_countryToHighlight");
        u_shadeUnselected = program.getUniformLocation("u_shadeUnselected");
        u_outlineSelected = program.getUniformLocation("u_outlineSelected");
        u_showAttr	= program.getUniformLocation("u_showAttr");
        u_showGrid	= program.getUniformLocation("u_showGrid");
        u_showCells = program.getUniformLocation("u_showCells");
        u_timeMod1	= program.getUniformLocation("u_timeMod1");
	}
	
	public void act(float delta) {
		timeMod1 += delta;
		if (timeMod1 > 1f) {
			timeMod1 -= 1f;
		}
	}
	
	@Override
	public void begin(Camera camera, RenderContext context) {
		this.context = context;
		context.setDepthTest(GL20.GL_LEQUAL);
    	context.setCullFace(GL20.GL_BACK);
    	//context.setCullFace(0);
    	context.setBlending(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    	Gdx.gl.glColorMask(true, true, true, false);	//Don't update alpha channel
        //
    	program.bind();
    	program.setUniformMatrix(u_proj, camera.projection);
    	program.setUniformMatrix(u_view, camera.view);
    	program.setUniformf(u_timeMod1, timeMod1);
	}
	
	private final Vector3 tmpVec = new Vector3();
	
	@Override
	public void render(Renderable renderable) {
		renderable.worldTransform.getTranslation(tmpVec);
		LandAttribute landAttr = (LandAttribute)renderable.material.get(LandAttribute.LAND);
		program.setUniformf(u_blockPos, tmpVec.x, tmpVec.z);
		program.setUniformi(u_countriesMaterials, context.textureBinder.bind(landAttr.countriesMaterials));
		program.setUniformi(u_materials, context.textureBinder.bind(landAttr.getMaterials()));
		program.setUniformi(u_noise, context.textureBinder.bind(landAttr.noiseDescriptor));
		program.setUniformi(u_bump, context.textureBinder.bind(landAttr.coastlineBumpDescriptor));
		program.setUniformi(u_smallBump, context.textureBinder.bind(landAttr.smallBumpDescriptor));
		//program.setUniformi(u_heightMap, context.textureBinder.bind(landAttr.land.getHeightMap()));
		//
		program.setUniformf(u_blocksPerSide, landAttr.land.land.getBlocksPerSide());
		program.setUniformi(u_countryToHighlight, landAttr.countryToHighlight);
		program.setUniformi(u_shadeUnselected, landAttr.highlightSelectedCountry ? -1 : 0);
		program.setUniformi(u_outlineSelected, landAttr.outlineSelectedCountry ? -1 : 0);
		program.setUniformi(u_showAttr, landAttr.showCellsAttr.ordinal());
		program.setUniformi(u_showGrid, landAttr.showGrid == ShowGridOption.EVERYWHERE ? -1 : 0);
		program.setUniformi(u_showCells, landAttr.showCells ? -1 : 0);
		//
		renderable.meshPart.render(program);
	}
	
	@Override
	public void end() {
		Gdx.gl.glColorMask(true, true, true, true);		//Restore alpha channel write
	}
	
	@Override
	public void dispose() {
		program.dispose();
	}
	
	@Override
	public int compareTo(Shader other) {
		return 0;
	}
	
	
	public static class LandAttribute extends Attribute implements Disposable {
		public final static String LAND_ALIAS = "land";
    	public final static long LAND = register(LAND_ALIAS);
    	
    	private Land3D land;
    	private final TextureDescriptor<Texture> noiseDescriptor = new TextureDescriptor<>();
    	private final TextureDescriptor<Texture> coastlineBumpDescriptor = new TextureDescriptor<>();
    	private final TextureDescriptor<Texture> smallBumpDescriptor = new TextureDescriptor<>();
    	private Texture countriesMaterials;
    	
    	public boolean highlightSelectedCountry = false;
    	public boolean outlineSelectedCountry = false;
    	public int countryToHighlight = -1;
    	public Settings.ShowCellAttrOption showCellsAttr = ShowCellAttrOption.OFF;
    	public Settings.ShowGridOption showGrid = ShowGridOption.NONE;
    	public boolean showCells = false;
    	
    	public LandAttribute() {
    		super(LAND);
    		noiseDescriptor.minFilter = TextureFilter.Linear;
    		noiseDescriptor.magFilter = TextureFilter.Linear;
    		noiseDescriptor.uWrap = TextureWrap.Repeat;
    		noiseDescriptor.vWrap = TextureWrap.Repeat;
    		//
    		coastlineBumpDescriptor.minFilter = TextureFilter.Linear;
    		coastlineBumpDescriptor.magFilter = TextureFilter.Linear;
    		coastlineBumpDescriptor.uWrap = TextureWrap.Repeat;
    		coastlineBumpDescriptor.vWrap = TextureWrap.Repeat;
    		//
    		smallBumpDescriptor.minFilter = TextureFilter.Linear;
    		smallBumpDescriptor.magFilter = TextureFilter.Linear;
    		smallBumpDescriptor.uWrap = TextureWrap.Repeat;
    		smallBumpDescriptor.vWrap = TextureWrap.Repeat;
    	}
    	
    	public void setLand(Land3D land) {
    		//Detach listeners from previous land
    		if (this.land != null) {
    			this.land.land.listeners.remove(landChangeListener);
    		}
    		//Set fields
    		this.land = land;
    		noiseDescriptor.texture = land.getNoiseMap();
    		coastlineBumpDescriptor.texture = land.getCoastlineBumpMap();
    		smallBumpDescriptor.texture = land.getSmallBump();
    		updateCountriesMaterials();
    		//Attach listeners to new land
    		this.land.listeners.add(landChangeListener);
    	}
    	
    	private final UChangeListener landChangeListener = new UChangeListener() {
			@Override
			public void onChange(UEvent event) {
				if (event.getProperty() == Land3D.Property.COUNTRIES) {
					updateCountriesMaterials();
				} else if (event.getProperty() == Land3D.Property.NOISE_MAP) {
					noiseDescriptor.texture = land.getNoiseMap();
				} else if (event.getProperty() == Land3D.Property.BUMP_MAP) {
					coastlineBumpDescriptor.texture = land.getCoastlineBumpMap();
				} else if (event.getProperty() == Land3D.Property.SMALL_BUMP) {
					smallBumpDescriptor.texture = land.getSmallBump();
				}
			}
    	};
    	
    	public TextureArray getMaterials() {
    		return land.getMaterials();
    	}
    	
    	private void updateCountriesMaterials() {
    		//Store country data in a texture with x=countryId, y=elevation, color.rgb = (firstMaterial, secondMaterial, blendCoefficient)
    		if (countriesMaterials != null) {
    			countriesMaterials.dispose();
    		}
    		List<LNDCountry> countries = land.land.getCountries();
    		int numCountries = Math.min(countries.size(), LndFile.MAX_COUNTRIES);
    		Pixmap pixmap = new Pixmap(Math.max(1, numCountries), 256, Format.RGB888);
    		for (int countryId = 0; countryId < numCountries; countryId++) {
    			LNDCountry country = countries.get(countryId);
    			LNDMapMaterial[] mapMaterials = country.getMapMaterialsForRead();
    			for (int elevation = 0; elevation < 256; elevation++) {
    				LNDMapMaterial mapMaterial = mapMaterials[elevation];
    				int col = 0xFF |
    						mapMaterial.getFirstMaterialIndex() << 24 |
    						mapMaterial.getSecondMaterialIndex() << 16 |
    						mapMaterial.getCoefficient() << 8;
    				pixmap.drawPixel(countryId, elevation, col);
    			}
    		}
    		countriesMaterials = new Texture(pixmap);
    		countriesMaterials.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
    		countriesMaterials.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
    		pixmap.dispose();
    	}
    	
		@Override
		public Attribute copy() {
			return new LandAttribute();
		}

		@Override
		public int compareTo(Attribute o) {
			return (int)(LAND - o.type);
		}
		
		@Override
		public void dispose() {
			if (this.land != null) {
    			this.land.land.listeners.remove(landChangeListener);
    		}
			if (countriesMaterials != null) {
				countriesMaterials.dispose();
			}
		}
	}
}
