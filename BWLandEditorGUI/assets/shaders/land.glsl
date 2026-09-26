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
[VS]
#version 130

uniform mat4 u_proj;
uniform mat4 u_view;
uniform vec2 u_blockPos;

in vec3 a_position;
in float a_country0;
in float a_country1;
in float a_country2;
in int a_attr0;
in int a_attr1;
in int a_attr2;
in vec3 a_weight;
in float a_lightLevel;
in float a_altitude;

out vec2 v_texcoord0;
out float v_elevation;
flat out int v_attr0;
flat out int v_attr1;
flat out int v_attr2;
out vec3 v_weight;
out float v_lightLevel;
out float v_altitude;
out float v_distToCamera;

void main() {
	vec3 pos = vec3(u_blockPos.x + a_position.x, a_position.y, u_blockPos.y + a_position.z);
	v_texcoord0 = vec2(-pos.z, pos.x) / 160.0;
	
	v_elevation = pos.y / 0.67;
	
	v_attr0 = a_attr0;
	v_attr1 = a_attr1;
	v_attr2 = a_attr2;
	v_altitude = a_altitude;
	
	v_weight = a_weight;
	
	v_lightLevel = a_lightLevel;
	
	vec4 cs_position = u_view * vec4(pos, 1.0);
	v_distToCamera = abs(cs_position.z);
	gl_Position = u_proj * cs_position;
}


[FS]
#version 130
#extension GL_EXT_texture_array : enable

uniform sampler2D u_countriesMaterials;
uniform sampler2DArray u_materials;
uniform sampler2D u_noise;
uniform sampler2D u_bump;
uniform sampler2D u_smallBump;
//uniform sampler2D u_heightMap;

uniform vec3 u_skyAndBump;
uniform float u_blocksPerSide;
uniform int u_countryToHighlight;
uniform bool u_shadeUnselected;
uniform bool u_outlineSelected;
uniform int u_showAttr;
uniform bool u_showGrid;
uniform bool u_showCells;
uniform float u_timeMod1;

#define OUTLINE_THICKNESS 0.75
#define GRID_THICKNESS 0.65
#define CELL_THICKNESS 0.008

#define SELECTION_THICKNESS 1.0
#define DASH_PERIOD 40.0
#define DASH_DUTY   0.5

#define CELLS_ATTR_PROPS 1
#define CELLS_ATTR_SOUNDS 2

in vec2 v_texcoord0;
in float v_elevation;
flat in int v_attr0;
flat in int v_attr1;
flat in int v_attr2;
in vec3 v_weight;
in float v_lightLevel;
in float v_altitude;
in float v_distToCamera;

out vec4 fragColor;

#define CELL0 r
#define CELL1 g
#define CELL2 b

#define firstMaterialIndex r
#define secondMaterialIndex g
#define blendFactor b

#define HAS_WATER 0x10
#define COASTLINE 0x20
#define FULLWATER 0x40
#define HIGHLIGHT_CELL 0x10000

vec3 attrColors[] = vec3[] (
	vec3(0, 0, 0),		//NONE
	vec3(0, 0, 0),
	vec3(0.8, 1, 1),	//SPLASH
	vec3(0, 0, 0.6),	//OCEAN
	vec3(0.1, 0.7, 1),	//SLOW_WAVES
	vec3(0, 0.8, 1),	//LAKE
	vec3(0.8, 0.8, 0),	//COAST
	vec3(0, 0, 1),		//FAST_WAVES
	vec3(0.5, 0.6, 0),	//JUNGLE
	vec3(0.5, 0.6, 0),
	vec3(0, 0.6, 0.6),	//WIND
	vec3(0, 0.6, 0.6),
	vec3(1, 1, 0),		//DESERT
	vec3(1, 1, 0),
	vec3(0, 0.9, 0),	//BIRDS
	vec3(0, 0.9, 0),
	vec3(0, 0.4, 0),	//FOREST
	vec3(0, 0.4, 0),
	vec3(0.2, 0.9, 1),	//RIVER
	vec3(0.2, 0.9, 0)
);

float gridMask(vec2 p, float stepSize, float lineWidth) {
	vec2 coord = p / stepSize;
    vec2 grid = abs(fract(coord - 0.5) - 0.5) / fwidth(coord);
    float line = min(grid.x, grid.y);
    return 1.0 - smoothstep(0.0, lineWidth, line);
}

float pointMask(vec2 p, float stepSize, float width) {
	vec2 coord = p / stepSize;
    vec2 grid = abs(fract(coord - 0.5) - 0.5);
    float point = grid.x * grid.x + grid.y * grid.y;
    return 1.0 - smoothstep(0.0, width, point);
}

float mix2(float d0, float d1, float s0, float s1, float s) {
	float a = clamp((s - s0) / (s1 - s0), 0.0, 1.0);
	return mix(d0, d1, a);
}

vec3 mix3(vec3 d0, vec3 d1, float s0, float s1, float s) {
	float a = clamp((s - s0) / (s1 - s0), 0.0, 1.0);
	return mix(d0, d1, a);
}

void main(void) {
	// unpack uniforms
	float skyType = 2.0; //u_skyAndBump.x;
	float bumpMapStrength = 0.25; //u_skyAndBump.y;
	float smallBumpMapStrength = 0.8; //u_skyAndBump.z;
	
	float noise = texture2D(u_noise, v_texcoord0).r;
	int elevation = int(clamp(v_elevation + noise * 64.0, 0, 255));
	
	int v_country0 = v_attr0 & 0xF;
	int v_country1 = v_attr1 & 0xF;
	int v_country2 = v_attr2 & 0xF;
	
	vec3 mapMaterial0 = texelFetch(u_countriesMaterials, ivec2(v_country0, elevation), 0).rgb;
	vec3 mapMaterial1 = texelFetch(u_countriesMaterials, ivec2(v_country1, elevation), 0).rgb;
	vec3 mapMaterial2 = texelFetch(u_countriesMaterials, ivec2(v_country2, elevation), 0).rgb;
	
	//rg here are first and second material index; they must be denormalized
	mapMaterial0.rg *= 255.0;
	mapMaterial1.rg *= 255.0;
	mapMaterial2.rg *= 255.0;
	
	// do each vert with both materials
	vec4 col0 = mix(
		texture2DArray(u_materials, vec3(v_texcoord0, mapMaterial0.firstMaterialIndex)),
		texture2DArray(u_materials, vec3(v_texcoord0, mapMaterial0.secondMaterialIndex)),
		mapMaterial0.blendFactor
	);
	
	vec4 col1 = mix(
		texture2DArray(u_materials, vec3(v_texcoord0, mapMaterial1.firstMaterialIndex)),
		texture2DArray(u_materials, vec3(v_texcoord0, mapMaterial1.secondMaterialIndex)),
		mapMaterial1.blendFactor
	);
	
	vec4 col2 = mix(
		texture2DArray(u_materials, vec3(v_texcoord0, mapMaterial2.firstMaterialIndex)),
		texture2DArray(u_materials, vec3(v_texcoord0, mapMaterial2.secondMaterialIndex)),
		mapMaterial2.blendFactor
	);
	
	// add the 3 blended textures together
	vec4 col = col0 * v_weight.CELL0 + col1 * v_weight.CELL1 + col2 * v_weight.CELL2;
	
	//Apply noise map
	col = col * (0.5 + noise);
	
	// apply light map
	float skyBightness = skyType / 2.0;
	col = col * mix(0.25, clamp(v_lightLevel * 2.0, 0.5, 1.0), skyBightness);
	
	float bump = texture2D(u_bump, vec2(v_texcoord0.x, v_texcoord0.y)).r;
	float dry = clamp((v_altitude - 2.0) * 0.8 + bump * 3.0 - 0.2, 0.0, 1.0);
	
	// don't apply smallbump unless we're close
	float smallStrength = clamp((200.0 - v_distToCamera) / 40.0, 0.0, 1.0) * smallBumpMapStrength;
	float smallbump = texture2D(u_smallBump, v_texcoord0.xy * 12.0).r;
	
	col = col * (1.0 - smallbump * smallStrength);
	col = col * clamp(1.0 + (dry - 0.25) * 3.0, 0.0, 1.0);
	col.a = clamp(mix(smallbump, 1.0, dry), 0.0, 1.0);
	
	//Depth alpha
	col.a = clamp(col.a * (v_altitude - 0.5) * 0.5, 0.0, 1.0);
	
	//Highlight selected country
	if (u_countryToHighlight >= 0) {
		float highlightVertex0 = v_country0 == u_countryToHighlight ? 1.0 : 0.0;
		float highlightVertex1 = v_country1 == u_countryToHighlight ? 1.0 : 0.0;
		float highlightVertex2 = v_country2 == u_countryToHighlight ? 1.0 : 0.0;
		float highlight0 = highlightVertex0 * v_weight.CELL0;
		float highlight1 = highlightVertex1 * v_weight.CELL1;
		float highlight2 = highlightVertex2 * v_weight.CELL2;
		
		//col.rgb = mix(col.rgb, vec3(0.0, 0.8, 1.0), round(highlight0 + highlight1 + highlight2) * 0.5);
		
		//Shade unselected countries
		if (u_shadeUnselected) {
			float highlight = round(highlight0 + highlight1 + highlight2);
			col.rgb = mix(col.rgb, vec3(0.0, 0.0, 0.0), (1.0 - highlight) * 0.5);
		}
		//Outline selected country
		if (u_outlineSelected) {
			float s = highlight0 + highlight1 + highlight2;
			float d = abs(s - 0.5);
			float w = fwidth(s);
			float line = 1.0 - smoothstep(0.0, OUTLINE_THICKNESS * w, d);
			col = mix(col, vec4(0.0, 1.0, 1.0, 1.0), line);
		}
	}
	
	int attr = 0;
	if (v_weight.CELL0 >= v_weight.CELL1 && v_weight.CELL0 >= v_weight.CELL2) {
		attr = v_attr0;
	} else if (v_weight.CELL1 >= v_weight.CELL0 && v_weight.CELL1 >= v_weight.CELL2) {
		attr = v_attr1;
	} else {
		attr = v_attr2;
	}
	
	if (u_showAttr != 0) {
		if (u_showAttr == CELLS_ATTR_PROPS) {
			if ((attr & COASTLINE) != 0) {
				col = mix(col, vec4(1.0, 1.0, 0.0, 1.0), 0.7);
			}
			if ((attr & HAS_WATER) != 0) {
				col = mix(col, vec4(0.0, 1.0, 1.0, 1.0), 0.7);
			}
			if ((attr & FULLWATER) != 0) {
				col = mix(col, vec4(0.0, 0.0, 1.0, 1.0), 0.7);
			}
		} else if (u_showAttr == CELLS_ATTR_SOUNDS) {
			attr = (attr & 0xFF00) >> 9;
			vec3 aCol = attrColors[attr];
			col = mix(col, vec4(aCol, 1.0), 0.7);
			//if (v_elevation <= 2) col.a = 1.0;
		}
	}
	
	//Grid
	if (u_showGrid) {
		float mask = gridMask(v_texcoord0, 1.0, GRID_THICKNESS);
		col = mix(col, vec4(0.8, 0.8, 0.8, 1.0), mask * 0.8);
	}
	
	//Cells
	if (u_showCells && v_distToCamera < 1500.0) {
		float mask = pointMask(v_texcoord0, 1.0 / 16.0, CELL_THICKNESS);
		col = mix(col, vec4(0.8, 0.8, 0.8, 1.0), mask * 0.8);
	}
	
	//Make points outside bounds reddish
	if (v_texcoord0.x < 0.0 || v_texcoord0.x > u_blocksPerSide || v_texcoord0.y < 0.0 || v_texcoord0.y > u_blocksPerSide) {
		col = mix(col, vec4(1.0, 0.0, 0.0, 1.0), 0.5);
	}
	
	
	//Highlight selected cells
	{
		float highlightVertex0 = (v_attr0 & HIGHLIGHT_CELL) != 0 ? 1.0 : 0.0;
		float highlightVertex1 = (v_attr1 & HIGHLIGHT_CELL) != 0 ? 1.0 : 0.0;
		float highlightVertex2 = (v_attr2 & HIGHLIGHT_CELL) != 0 ? 1.0 : 0.0;
		float highlight0 = highlightVertex0 * v_weight.CELL0;
		float highlight1 = highlightVertex1 * v_weight.CELL1;
		float highlight2 = highlightVertex2 * v_weight.CELL2;
		
		//Highlight
		float highlight = round(highlight0 + highlight1 + highlight2);
		col.rgb = mix(col.rgb, vec3(0.0, 1.0, 1.0), highlight * 0.5);
		
		//Outline
		float s = highlight0 + highlight1 + highlight2;
		float d = abs(s - 0.5);
		float w = fwidth(s);
		
		float line = 1.0 - smoothstep(0.0, SELECTION_THICKNESS * w, d);
		
		vec2 grad = vec2(dFdx(s), dFdy(s));
		vec2 tangent = normalize(vec2(-grad.y, grad.x));	//Local line direction in screen-space
		
		float phase = dot(gl_FragCoord.xy, tangent);
		float f = fract(phase / DASH_PERIOD + u_timeMod1);
		float dash = step(f, DASH_DUTY);
		
		col = mix(col, vec4(1.0, 1.0, 1.0, 1.0), line * dash);
	}
	
	//float a = texture2D(u_heightMap, vec2(v_texcoord0.y, v_texcoord0.x) / 32.0).r;
	//col.rgb = vec3(a, a, a);
	
	fragColor = col;
	if (col.a == 0.0) {
		discard;
	}
}
