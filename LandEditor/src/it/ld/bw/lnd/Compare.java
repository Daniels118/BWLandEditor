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
package it.ld.bw.lnd;

import java.io.File;
import java.util.List;

import it.ld.bw.lnd.model.LH3DLandBlock;
import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LH3DLandHeader;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LNDLowresTexture;
import it.ld.bw.lnd.model.LNDMapMaterial;
import it.ld.bw.lnd.model.LNDMaterial;
import it.ld.bw.lnd.model.LndFile;
import it.ld.utils.CmdLine;

public class Compare {
	private static final String FMT_STR  = "%1s %40s: %20s %20s\n";
	private static final String FMT_INT  = "%1s %40s: %20d %20d\n";
	private static final String FMT_FLT  = "%1s %40s: %20f %20f\n";
	private static final String FMT_HEX  = "%1s %40s: %20H %20H\n";
	
	public static void main(String[] args) throws Exception {
		boolean brk = true;
		
		CmdLine cmd = new CmdLine(args);
		File inp1 = mandatory(cmd.getArgFile("-i1"), "-i1");
		File inp2 = mandatory(cmd.getArgFile("-i2"), "-i2");
		LndFile lnd1 = LndFile.load(inp1, false);
		LndFile lnd2 = LndFile.load(inp2, false);
		
		LH3DLandHeader hdr1 = lnd1.getHeader();
		LH3DLandHeader hdr2 = lnd2.getHeader();
		
		out("File name"         , inp1.getName(), inp2.getName(), true);
		out("MaterialSize"      , hdr1.getMaterialSize(), hdr2.getMaterialSize(), true);
		out("BlockSize"         , hdr1.getBlockSize(), hdr2.getBlockSize(), true);
		out("CountrySize"       , hdr1.getCountrySize(), hdr2.getCountrySize(), true);
		out("LowresTextureCount", hdr1.getLowresTextureCount(), hdr2.getLowresTextureCount(), true);
		out("MaterialCount"     , hdr1.getMaterialCount(), hdr2.getMaterialCount(), true);
		out("CountryCount"      , hdr1.getCountryCount(), hdr2.getCountryCount(), true);
		out("BlockCount"        , hdr1.getBlockCount(), hdr2.getBlockCount(), true);
		System.out.println();
		
		System.out.println("IndexBlock:");
		byte[] indexBlock1 = hdr1.getIndexBlock();
		byte[] indexBlock2 = hdr2.getIndexBlock();
		for (int i = 0; i < 1024; i++) {
			if (indexBlock1[i] != indexBlock2[i]) {
				System.out.printf("    %4d -> %3d != %3d\n", i, indexBlock1[i] & 0xFF, indexBlock2[i] & 0xFF);
			}
		}
		System.out.println();
		
		List<LNDLowresTexture> textures1 = lnd1.getLowresTextures();
		List<LNDLowresTexture> textures2 = lnd2.getLowresTextures();
		out("LowresTextures", textures1.size(), textures2.size(), true);
		for (int i = 0; i < Math.min(textures1.size(), textures2.size()); i++) {
			LNDLowresTexture texture1 = textures1.get(i);
			LNDLowresTexture texture2 = textures2.get(i);
			out("LowresTextures["+i+"].Index"    , texture1.getIndex(), texture2.getIndex(), false);
			out("LowresTextures["+i+"].Material ", texture1.getMaterial(), texture2.getMaterial(), false);
			out("LowresTextures["+i+"].TextureId", texture1.getTextureId(), texture2.getTextureId(), false);
			out("LowresTextures["+i+"].Size     ", texture1.getSize(), texture2.getSize(), false);
			out("LowresTextures["+i+"].Hash     ", texture1.getDdsTexture().hashCode(), texture2.getDdsTexture().hashCode(), false);
		}
		System.out.println();
		
		List<LNDMaterial> materials1 = lnd1.getMaterials();
		List<LNDMaterial> materials2 = lnd2.getMaterials();
		out("Materials", materials1.size(), materials2.size(), true);
		for (int i = 0; i < Math.min(materials1.size(), materials1.size()); i++) {
			LNDMaterial material1 = materials1.get(i);
			LNDMaterial material2 = materials2.get(i);
			out("Materials["+i+"].MaterialType", material1.getMaterialType().name(), material2.getMaterialType().name(), false);
			out("Materials["+i+"].TexelsHash ", material1.getTexelsHash(), material2.getTexelsHash(), false);
		}
		System.out.println();
		
		List<LNDCountry> countries1 = lnd1.getCountries();
		List<LNDCountry> countries2 = lnd2.getCountries();
		out("Countries", countries1.size(), countries2.size(), true);
		for (int i = 0; i < Math.min(countries1.size(), countries1.size()); i++) {
			LNDCountry country1 = countries1.get(i);
			LNDCountry country2 = countries2.get(i);
			out("Countries["+i+"].TerrainType ", country1.getTerrainType(), country2.getTerrainType(), false);
			LNDMapMaterial[] mapMats1 = country1.getMapMaterialsForRead();
			LNDMapMaterial[] mapMats2 = country2.getMapMaterialsForRead();
			for (int j = 0; j < mapMats1.length; j++) {
				LNDMapMaterial mapMat1 = mapMats1[j];
				LNDMapMaterial mapMat2 = mapMats2[j];
				if (!out("Countries["+i+"].MapMaterials["+j+"].FirstMaterialIndex", mapMat1.getFirstMaterialIndex(), mapMat2.getFirstMaterialIndex(), false)) break;
				if (!out("Countries["+i+"].MapMaterials["+j+"].SecondMaterialIndex", mapMat1.getSecondMaterialIndex(), mapMat2.getSecondMaterialIndex(), false)) break;
				if (!out("Countries["+i+"].MapMaterials["+j+"].Coefficient", mapMat1.getCoefficient(), mapMat2.getCoefficient(), false)) break;
			}
		}
		System.out.println();
		
		brk = false;
		
		List<LH3DLandBlock> blocks1 = lnd1.getLandBlocksForRead();
		List<LH3DLandBlock> blocks2 = lnd2.getLandBlocksForRead();
		out("Blocks", blocks1.size(), blocks2.size(), true);
		for (int i = 0; i < Math.min(blocks1.size(), blocks2.size()); i++) {
			LH3DLandBlock block1 = blocks1.get(i);
			LH3DLandBlock block2 = blocks2.get(i);
			if (!out("Blocks["+i+"].Index", block1.getIndex(), block2.getIndex(), false) && brk) break;
			if (!out("Blocks["+i+"].BlockX", block1.getBlockX(), block2.getBlockX(), false) && brk) break;
			if (!out("Blocks["+i+"].BlockZ", block1.getBlockZ(), block2.getBlockZ(), false) && brk) break;
			if (!out("Blocks["+i+"].MapX", block1.getMapX(), block2.getMapX(), false) && brk) break;
			if (!out("Blocks["+i+"].MapX", block1.getMapZ(), block2.getMapZ(), false) && brk) break;
			if (!out("Blocks["+i+"].HighestAltitude", block1.getHighestAltitude(), block2.getHighestAltitude(), false) && brk) break;
			
			if (!out("Blocks["+i+"].Clipped", block1.getClipped(), block2.getClipped(), false) && brk) break;
			if (!out("Blocks["+i+"].DrawSomething", block1.getDrawSomething(), block2.getDrawSomething(), false) && brk) break;
			if (!out("Blocks["+i+"].Fog", block1.getFog(), block2.getFog(), false) && brk) break;
			if (!out("Blocks["+i+"].ForceLowResTex", block1.getForceLowResTex(), block2.getForceLowResTex(), false) && brk) break;
			if (!out("Blocks["+i+"].FrameVisibility", block1.getFrameVisibility(), block2.getFrameVisibility(), false) && brk) break;
			if (!out("Blocks["+i+"].Fu_lrs", block1.getFu_lrs(), block2.getFu_lrs(), false) && brk) break;
			if (!out("Blocks["+i+"].Fv_lrs", block1.getFv_lrs(), block2.getFv_lrs(), false) && brk) break;
			if (!out("Blocks["+i+"].LowResTexture", block1.getLowResTexture(), block2.getLowResTexture(), false) && brk) break;
			if (!out("Blocks["+i+"].MatPointer", block1.getMatPointer(), block2.getMatPointer(), false) && brk) break;
			if (!out("Blocks["+i+"].MeshBlending", block1.getMeshBlending(), block2.getMeshBlending(), false) && brk) break;
			if (!out("Blocks["+i+"].MeshLOD", block1.getMeshLOD(), block2.getMeshLOD(), false) && brk) break;
			if (!out("Blocks["+i+"].MeshLODType", block1.getMeshLODType(), block2.getMeshLODType(), false) && brk) break;
			if (!out("Blocks["+i+"].NextSortingPtr", block1.getNextSortingPtr(), block2.getNextSortingPtr(), false) && brk) break;
			if (!out("Blocks["+i+"].SmallTextUpdated", block1.getSmallTextUpdated(), block2.getSmallTextUpdated(), false) && brk) break;
			if (!out("Blocks["+i+"].SpecMatAfterPtr", block1.getSpecMatAfterPtr(), block2.getSpecMatAfterPtr(), false) && brk) break;
			if (!out("Blocks["+i+"].SpecMatBeforePtr", block1.getSpecMatBeforePtr(), block2.getSpecMatBeforePtr(), false) && brk) break;
			if (!out("Blocks["+i+"].TexPointer", block1.getTexPointer(), block2.getTexPointer(), false) && brk) break;
			if (!out("Blocks["+i+"].TextureBlend", block1.getTextureBlend(), block2.getTextureBlend(), false) && brk) break;
			if (!out("Blocks["+i+"].UseSmallBump", block1.getUseSmallBump(), block2.getUseSmallBump(), false) && brk) break;
			if (!out("Blocks["+i+"].ValueSorting", block1.getValueSorting(), block2.getValueSorting(), false) && brk) break;
			
			LH3DLandCell[] cells1 = block1.getCellsForRead();
			LH3DLandCell[] cells2 = block2.getCellsForRead();
			for (int j = 0; j < cells1.length; j++) {
				LH3DLandCell cell1 = cells1[j];
				LH3DLandCell cell2 = cells2[j];
				if (!out("Blocks["+i+"].cells["+j+"].Altitude", cell1.getAltitude(), cell2.getAltitude(), false) && brk) break;
				if (!out("Blocks["+i+"].cells["+j+"].Country", cell1.getCountry(), cell2.getCountry(), false) && brk) break;
				if (!out("Blocks["+i+"].cells["+j+"].LightLevel", cell1.getLightLevel(), cell2.getLightLevel(), false) && brk) break;
				if (!out("Blocks["+i+"].cells["+j+"].R", cell1.getR(), cell2.getR(), false) && brk) break;
				if (!out("Blocks["+i+"].cells["+j+"].G", cell1.getG(), cell2.getG(), false) && brk) break;
				if (!out("Blocks["+i+"].cells["+j+"].B", cell1.getB(), cell2.getB(), false) && brk) break;
				if (!out("Blocks["+i+"].cells["+j+"].Flags", cell1.getSound(), cell2.getSound(), false) && brk) break;
				if (!out("Blocks["+i+"].cells["+j+"].Luminosity", cell1.getLuminosity(), cell2.getLuminosity(), false) && brk) break;
				if (!out("Blocks["+i+"].cells["+j+"].Properties", cell1.getPropertiesLow(), cell2.getPropertiesLow(), false) && brk) break;
				if (!out("Blocks["+i+"].cells["+j+"].Savecolor", cell1.getSavecolor(), cell2.getSavecolor(), false) && brk) break;
			}
		}
	}
	
	private static boolean out(String name, String v1, String v2, boolean always) {
		boolean eq = v1 == null && v2 == null || v1 != null && v1.equals(v2);
		if (!eq || always) System.out.printf(FMT_STR, eq ? " " : "*", name, v1, v2);
		return eq;
	}
	
	private static boolean out(String name, int v1, int v2, boolean always) {
		boolean eq = v1 == v2;
		if (!eq || always) System.out.printf(FMT_INT, eq ? " " : "*", name, v1, v2);
		return eq;
	}
	
	private static boolean out(String name, float v1, float v2, boolean always) {
		boolean eq = v1 == v2;
		if (!eq || always) System.out.printf(FMT_FLT, eq ? " " : "*", name, v1, v2);
		return eq;
	}
	
	private static boolean out(String name, byte v1, byte v2, boolean always) {
		boolean eq = v1 == v2;
		if (!eq || always) {
			System.out.printf(FMT_HEX, eq ? " " : "*", name, v1, v2);
		}
		return eq;
	}
	
	private static <T> T mandatory(T value, String name) {
		if (value == null) throw new RuntimeException(name + " is mandatory");
		return value;
	}
}
