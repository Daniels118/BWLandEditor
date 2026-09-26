/* Copyright (c) 2026 Daniele Lombardi / Daniels118
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
package it.ld.bw.lnd.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import it.ld.bw.lnd.model.BulkUpdate;
import it.ld.bw.lnd.model.LH3DLandBlock;
import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LH3DLandCell.Sound;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LNDMapMaterial;
import it.ld.bw.lnd.model.LNDMaterial;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.Vec3f;
import it.ld.utils.Compat;
import it.ld.utils.MathUtils;

public class LandTool {
	private final LndFile land;
	
	public LandTool(LndFile land) {
		this.land = land;
	}
	
	public List<LH3DLandBlock> convertBlocks(LndFile srcLand, int[] countryMapping) {
		if (countryMapping != null && countryMapping.length != LndFile.MAX_COUNTRIES) {
			throw new IllegalArgumentException("Country mapping must have "+LndFile.MAX_COUNTRIES+" elements");
		}
		List<LH3DLandBlock> srcBlocks = srcLand.getLandBlocksForRead();
		int bxLow = Integer.MAX_VALUE;
		int bzLow = Integer.MAX_VALUE;
		for (LH3DLandBlock block : srcBlocks) {
			bxLow = Math.min(bxLow, block.getBlockX());
			bzLow = Math.min(bzLow, block.getBlockZ());
		}
		List<LH3DLandBlock> res = new ArrayList<>(land.getNumBlocks());
		for (LH3DLandBlock block : srcBlocks) {
			LH3DLandBlock newBlock = block.clone();
			int bx = block.getBlockX() - bxLow;
			int bz = block.getBlockZ() - bzLow;
			newBlock.setBlockX(bx);
			newBlock.setBlockZ(bz);
			newBlock.setMapX(LH3DLandBlock.BLOCK_SIZE * bx);
			newBlock.setMapZ(LH3DLandBlock.BLOCK_SIZE * bz);
			if (countryMapping != null) {
				for (LH3DLandCell cell : newBlock.getCellsForRead()) {
					cell.setCountry(countryMapping[cell.getCountry()]);
				}
			}
			res.add(newBlock);
		}
		return res;
	}
	
	//TODO merge new land with existing land
	public boolean importBlocks(List<LH3DLandBlock> blocks, final int offsetx, final int offsetz, int[] countryMapping) {
		if (blocks.isEmpty()) return false;
		if (offsetx < 0 || offsetz < 0) return false;
		if (countryMapping != null && countryMapping.length != LndFile.MAX_COUNTRIES) {
			throw new IllegalArgumentException("Country mapping must have "+LndFile.MAX_COUNTRIES+" elements");
		}
		//
		int bxLow = Integer.MAX_VALUE;
		int bzLow = Integer.MAX_VALUE;
		int bxHigh = Integer.MIN_VALUE;
		int bzHigh = Integer.MIN_VALUE;
		for (LH3DLandBlock block : blocks) {
			bxLow = Math.min(bxLow, block.getBlockX());
			bzLow = Math.min(bzLow, block.getBlockZ());
			bxHigh = Math.max(bxHigh, block.getBlockX());
			bzHigh = Math.max(bzHigh, block.getBlockZ());
		}
		int blockSpanX = bxHigh - bxLow + 1;
		int blockSpanZ = bzHigh - bzLow + 1;
		final int xCells = blockSpanX * LH3DLandBlock.SEG_PER_SIDE + 1;
		final int zCells = blockSpanZ * LH3DLandBlock.SEG_PER_SIDE + 1;
		final int baseBlockX = offsetx / LH3DLandBlock.SEG_PER_SIDE;
		final int baseBlockZ = offsetz / LH3DLandBlock.SEG_PER_SIDE;
		final int baseCellX = baseBlockX * LH3DLandBlock.SEG_PER_SIDE;
		final int baseCellZ = baseBlockZ * LH3DLandBlock.SEG_PER_SIDE;
		final int localOffsetX = offsetx - baseCellX;
		final int localOffsetZ = offsetz - baseCellZ;
		final int xBlocks = MathUtils.ceilDiv(localOffsetX + (xCells - 1), LH3DLandBlock.SEG_PER_SIDE);
		final int zBlocks = MathUtils.ceilDiv(localOffsetZ + (zCells - 1), LH3DLandBlock.SEG_PER_SIDE);
		if (baseBlockX + xBlocks - 1 >= land.getBlocksPerSide() || baseBlockZ + zBlocks - 1 >= land.getBlocksPerSide()) {
			return false;
		}
		//Ensure capacity
		final int oldLimit = land.getMaxBlocks();
		final int maxNewBlocks = blocks.size() + 2 * (blockSpanX + blockSpanZ) + 4;
		if (land.getNumBlocks() + maxNewBlocks > oldLimit) {
			land.setMaxBlocks(land.getNumBlocks() + maxNewBlocks);
		}
		//
		LH3DLandCell[][] srcCells = blocksToCells(blocks);
		final int srcw1 = srcCells.length - 1;
		final int srch1 = srcCells[0].length - 1;
		//Add non-empty blocks
		List<LH3DLandBlock> blocksToUpdate = new ArrayList<>(maxNewBlocks);
		for (int bx = baseBlockX; bx < baseBlockX + xBlocks; bx++) {
			final int basex = bx * LH3DLandBlock.SEG_PER_SIDE;
			for (int bz = baseBlockZ; bz < baseBlockZ + zBlocks; bz++) {
				final int basez = bz * LH3DLandBlock.SEG_PER_SIDE;
				boolean isEmpty = true;
				for (int cx = 1; cx < LH3DLandBlock.SEG_PER_SIDE && isEmpty; cx++) {
					final int ax = basex + cx;
					final int srcX = ax - offsetx;
					if (srcX < 0) continue;
					if (srcX > srcw1) break;
					for (int cz = 1; cz < LH3DLandBlock.SEG_PER_SIDE; cz++) {
						final int az = basez + cz;
						final int srcZ = az - offsetz;
						if (srcZ < 0) continue;
						if (srcZ > srch1) break;
						LH3DLandCell srcCell = srcCells[srcX][srcZ];
						if (srcCell != null && (srcCell.getAltitude() > 0 || srcCell.getSoundEnum() != Sound.OCEAN)) {
							isEmpty = false;
							break;
						}
					}
				}
				if (!isEmpty) {
					LH3DLandBlock block = land.getBlock(bx, bz);
					if (block == null) {
						block = land.addBlockAt(bx, bz);
					}
					blocksToUpdate.add(block);
				}
			}
		}
		//Merge
		for (int bx = baseBlockX; bx < baseBlockX + xBlocks; bx++) {
			final int basex = bx * LH3DLandBlock.SEG_PER_SIDE;
			for (int bz = baseBlockZ; bz < baseBlockZ + zBlocks; bz++) {
				final int basez = bz * LH3DLandBlock.SEG_PER_SIDE;
				LH3DLandBlock block = land.getBlock(bx, bz);
				if (block != null) {
					for (int cx = 0; cx < LH3DLandBlock.SEG_PER_SIDE; cx++) {
						final int ax = basex + cx;
						final int srcX = ax - offsetx;
						if (srcX < 0) continue;
						if (srcX > srcw1) break;
						for (int cz = 0; cz < LH3DLandBlock.SEG_PER_SIDE; cz++) {
							final int az = basez + cz;
							final int srcZ = az - offsetz;
							if (srcZ < 0) continue;
							if (srcZ > srch1) break;
							LH3DLandCell srcCell = srcCells[srcX][srcZ];
							if (srcCell != null) {
								int altitude = srcCell.getAltitude();
								LH3DLandCell dstCell = block.getCell(cx, cz);
								if (altitude > dstCell.getAltitude() ||
										(dstCell.getAltitude() <= 1 && (altitude > 0 || srcCell.getSoundEnum() != Sound.OCEAN))) {
									dstCell.set(srcCell);
									if (altitude < LH3DLandCell.COASTLINE_ALTITUDE) {
										dstCell.setWater(true);
									} else if (altitude == LH3DLandCell.COASTLINE_ALTITUDE) {
										dstCell.setWater(false);
										dstCell.setCoastLine(true);
									} else if (altitude < LH3DLandCell.DRY_ALTITUDE) {
										dstCell.setWater(false);
									} else {
										dstCell.setWater(false);
									}
									if (countryMapping != null) {
										dstCell.setCountry(countryMapping[srcCell.getCountry()]);
									}
								}
							}
						}
					}
				}
			}
		}
		//Restore old capacity
		land.setMaxBlocks(oldLimit);
		//Update shadows & notify
		ShadowTool.updateShadows(land, blocksToUpdate);
		/*for (int i = 0; i < blocksToUpdate.size(); i++) {
			LH3DLandBlock block = blocksToUpdate.get(i);
			block.listeners.notify(EventType.CHANGE, LH3DLandBlock.Property.CELLS, null, block.getCellsForRead(), i, i == blocksToUpdate.size() - 1);
		}*/
		return true;
	}
	
	private static LH3DLandCell[][] blocksToCells(List<LH3DLandBlock> blocks) {
		//calculate width and height
		int bxLow = Integer.MAX_VALUE;
		int bzLow = Integer.MAX_VALUE;
		int bxHigh = Integer.MIN_VALUE;
		int bzHigh = Integer.MIN_VALUE;
		for (LH3DLandBlock block : blocks) {
			bxLow = Math.min(bxLow, block.getBlockX());
			bzLow = Math.min(bzLow, block.getBlockZ());
			bxHigh = Math.max(bxHigh, block.getBlockX());
			bzHigh = Math.max(bzHigh, block.getBlockZ());
		}
		int blockSpanX = bxHigh - bxLow + 1;
		int blockSpanZ = bzHigh - bzLow + 1;
		final int xCells = blockSpanX * LH3DLandBlock.SEG_PER_SIDE + 1;
		final int zCells = blockSpanZ * LH3DLandBlock.SEG_PER_SIDE + 1;
		//Copy cells from blocks to matrix
		LH3DLandCell[][] cells = new LH3DLandCell[xCells][zCells];
		for (LH3DLandBlock block : blocks) {
			final int basex = (block.getBlockX() - bxLow) * LH3DLandBlock.SEG_PER_SIDE;
			final int basez = (block.getBlockZ() - bzLow) * LH3DLandBlock.SEG_PER_SIDE;
			for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
				final int ax = basex + cx;
				for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
					final int az = basez + cz;
					cells[ax][az] = block.getCell(cx, cz);
				}
			}
		}
		return cells;
	}
	
	public static LNDCountry createCountry(int material, String name) {
		LNDCountry country = new LNDCountry();
		try (BulkUpdate<LNDMapMaterial> mapMaterials = country.getMapMaterialsForUpdate()) {
			for (int i = 0; i < 256; i++) {
				LNDMapMaterial mapMaterial = mapMaterials.data[i];
				mapMaterial.setFirstMaterialIndex(material);
				mapMaterial.setSecondMaterialIndex(material);
			}
			country.setName(name);
		}
		return country;
	}
	
	public static void replaceCountry(LndFile land, int src, int dst) {
		if (src == dst) return;
		//IMPORTANT: since the blocks overlap, we must notify the listeners after all modifications have been applied,
		// otherwise modifications to the edges belonging to other blocks not yet modified will be hidden.
		try (BulkUpdate<LH3DLandBlock> blocks = land.getLandBlocksForUpdate()) {
			for (int i = 0; i < blocks.data.length; i++) {
				LH3DLandBlock block = blocks.data[i];
				try (BulkUpdate<LH3DLandCell> cells = block.getCellsForUpdate()) {
					boolean changed = false;
					for (LH3DLandCell cell : cells.data) {
						if (cell.getCountry() == src) {
							cell.setCountry(dst);
							changed = true;
						}
					}
					if (changed) blocks.setChanged(i, LH3DLandBlock.Property.CELLS);	//Mark for deferred notification
				}
			}
		}
	}
	
	public static LNDCountry importCountry(List<LNDMaterial> srcMaterials, LNDCountry srcCountry, LndFile dstLand) {
		return importCountry(srcMaterials, srcCountry, dstLand, -1);
	}
	
	public static LNDCountry importCountry(LndFile srcLand, int src, LndFile dstLand) {
		return importCountry(srcLand, src, dstLand, -1);
	}
	
	public static LNDCountry importCountry(LndFile srcLand, int src, LndFile dstLand, int dst) {
		return importCountry(srcLand.getMaterials(), srcLand.getCountries().get(src), dstLand, dst);
	}
	
	public static LNDCountry importCountry(File src, LndFile dstLand) throws IOException {
		return importCountry(src, dstLand, -1);
	}
	
	public static LNDCountry importCountry(List<LNDMaterial> srcMaterials, LNDCountry srcCountry, LndFile dstLand, int dst) {
		List<LNDMaterial> dstMaterials = dstLand.getMaterials();
		//Create a map for available materials for fast lookup based on the texels
		HashMap<LNDMaterial, LNDMaterial> availableMaterials = new HashMap<>();
		for (LNDMaterial material : dstMaterials) {
			availableMaterials.put(material, material);
		}
		//Collect materials used by source country
		HashSet<Integer> usedMaterialsIds = new HashSet<>();
		for (LNDMapMaterial mapMaterial : srcCountry.getMapMaterialsForRead()) {
			usedMaterialsIds.add(mapMaterial.getFirstMaterialIndex());
			usedMaterialsIds.add(mapMaterial.getSecondMaterialIndex());
		}
		//Map source materials to available materials, or add a copy if missing
		HashMap<Integer, Integer> materialsMap = new HashMap<>();
		for (Integer srcMatId : usedMaterialsIds) {
			LNDMaterial srcMat = srcMaterials.get(srcMatId);
			LNDMaterial dstMat = availableMaterials.get(srcMat);
			if (dstMat == null) {
        		dstMat = new LNDMaterial();
        		dstMat.setMaterialType(srcMat.getMaterialType());
        		dstMat.setTexels(srcMat.getTexels());
        		dstMaterials.add(dstMat);
        	}
        	materialsMap.put(srcMatId, dstMat.getIndex());
		}
		//Import
		LNDCountry dstCountry = dst == -1 ? new LNDCountry() : dstLand.getCountries().get(dst);
		dstCountry.setTerrainType(srcCountry.getTerrainType());
		try (BulkUpdate<LNDMapMaterial> mapMaterials = dstCountry.getMapMaterialsForUpdate()) {
			for (int i = 0; i < 256; i++) {
				LNDMapMaterial srcMaterial = srcCountry.getMapMaterialsForRead()[i];
				int srcMat0 = srcMaterial.getFirstMaterialIndex();
				int srcMat1 = srcMaterial.getSecondMaterialIndex();
				int dstMat0 = materialsMap.get(srcMat0);
				int dstMat1 = materialsMap.get(srcMat1);
				LNDMapMaterial dstMaterial = mapMaterials.data[i];
				dstMaterial.setFirstMaterialIndex(dstMat0);
				dstMaterial.setSecondMaterialIndex(dstMat1);
				dstMaterial.setCoefficient(srcMaterial.getCoefficient());
			}
		}
		dstCountry.setName(srcCountry.getName());
		if (dst == -1) {
			dstLand.getCountries().add(dstCountry);
		}
		return dstCountry;
	}
	
	public static LNDCountry importCountry(File src, LndFile dstLand, int dst) throws IOException {
		List<LNDMaterial> srcMaterials = new ArrayList<>();
		Properties attributes = new Properties();
		LNDMapMaterial[] mapMaterials = new LNDMapMaterial[256];
		try (FileInputStream fis = new FileInputStream(src);
			ZipInputStream zis = new ZipInputStream(fis)
		) {
		    ZipEntry entry;
		    while ((entry = zis.getNextEntry()) != null) {
		        if (!entry.isDirectory()) {
		        	if (entry.getName().startsWith("materials/")) {
		            	String name = entry.getName();
		            	String basename = name.substring(name.lastIndexOf('/') + 1);
		            	int srcMatId = Integer.parseInt(basename.substring(0, basename.lastIndexOf('.')));
		            	BufferedImage image = ImageIO.read(zis);
		            	if (image.getWidth() != LNDMaterial.width || image.getHeight() != LNDMaterial.height) {
		            		throw new IOException("Wrong texture size: " + name);
		            	}
		            	int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		            	LNDMaterial srcMaterial = new LNDMaterial();
		            	srcMaterial.setARGB(pixels);
		            	while (srcMatId >= srcMaterials.size()) {
		            		srcMaterials.add(null);
		            	}
		            	srcMaterials.set(srcMatId, srcMaterial);
		            } else if ("attributes.properties".equals(entry.getName())) {
		            	attributes.load(zis);
		            } else if ("materials.csv".equals(entry.getName())) {
		            	String[] sMaterials = new String(Compat.readAllBytes(zis), "ASCII").trim().split("\r\n");
		            	if (sMaterials.length != 256) {
		            		throw new IOException("Wrong materials length: " + sMaterials.length);
		            	}
		            	for (int i = 0; i < 256; i++) {
		            		String[] vals = sMaterials[i].split(",");
		            		if (vals.length != 3) {
		            			throw new IOException("Wrong materials row length: " + vals.length);
		            		}
		            		int mat0 = Integer.parseInt(vals[0]);
		            		int mat1 = Integer.parseInt(vals[1]);
		            		int factor = Integer.parseInt(vals[2]);
		            		mapMaterials[i] = new LNDMapMaterial(mat0, mat1, factor);
		            	}
			        }
		        }
		        zis.closeEntry();
		    }
		}
		//Import
		LNDCountry srcCountry = new LNDCountry();
		srcCountry.setTerrainType(Integer.parseInt(attributes.getProperty("terrainType")));
		srcCountry.setMapMaterials(mapMaterials);
		return importCountry(srcMaterials, srcCountry, dstLand, dst);
	}
	
	public static void exportCountry(LndFile land, int index, File dst) throws IOException {
		try (CountryPreviewGenerator generator = new CountryPreviewGenerator(land);) {
			final int previewWidth = 256;
			final int previewHeight = 512;
			LNDCountry country = land.getCountries().get(index);
			int[] previewData = generator.generatePreview(country, previewWidth, previewHeight);
			//Collect materials used by this country and remap their indices starting from 0
			HashMap<Integer, Integer> usedMaterials = new HashMap<>();
			for (LNDMapMaterial mapMaterial : country.getMapMaterialsForRead()) {
				if (!usedMaterials.containsKey(mapMaterial.getFirstMaterialIndex())) {
					usedMaterials.put(mapMaterial.getFirstMaterialIndex(), usedMaterials.size());
				}
				if (!usedMaterials.containsKey(mapMaterial.getSecondMaterialIndex())) {
					usedMaterials.put(mapMaterial.getSecondMaterialIndex(), usedMaterials.size());
				}
			}
			//Write
			try (
		        FileOutputStream fos = new FileOutputStream(dst);
		        ZipOutputStream zos = new ZipOutputStream(fos)
		    ) {
				//Materials
				for (Entry<Integer, Integer> matMap : usedMaterials.entrySet()) {
					int originalIndex = matMap.getKey();
					int remappedIndex = matMap.getValue();
					int[] materialData = generator.getMaterial(originalIndex);
					BufferedImage image = new BufferedImage(LNDMaterial.width, LNDMaterial.height, BufferedImage.TYPE_INT_ARGB);
					image.setRGB(0, 0, image.getWidth(), image.getHeight(), materialData, 0, image.getWidth());
					ZipEntry zipEntry = new ZipEntry("materials/" + remappedIndex + ".png");
			        zos.putNextEntry(zipEntry);
			        ImageIO.write(image, "png", zos);
			        zos.closeEntry();
				}
				//Attributes
				Properties attributes = new Properties();
				attributes.setProperty("name", country.getName());
				attributes.setProperty("terrainType", String.valueOf(country.getTerrainType()));
				ZipEntry zipEntry = new ZipEntry("attributes.properties");
		        zos.putNextEntry(zipEntry);
		        attributes.store(zos, "Attributes");
		        zos.closeEntry();
		        //mapMaterials
				StringBuffer sBuffer = new StringBuffer(13 * 256);
				for (LNDMapMaterial mapMaterial : country.getMapMaterialsForRead()) {
					int remappedMat0 = usedMaterials.get(mapMaterial.getFirstMaterialIndex());
					int remappedMat1 = usedMaterials.get(mapMaterial.getSecondMaterialIndex());
					sBuffer.append(remappedMat0 + "," + remappedMat1 + "," + mapMaterial.getCoefficient() + "\r\n");
				}
				zipEntry = new ZipEntry("materials.csv");
		        zos.putNextEntry(zipEntry);
		        zos.write(sBuffer.toString().getBytes());
		        zos.closeEntry();
				//Preview
		        BufferedImage image = new BufferedImage(previewWidth, previewHeight, BufferedImage.TYPE_INT_ARGB);
		        image.setRGB(0, 0, image.getWidth(), image.getHeight(), previewData, 0, image.getWidth());
		        zipEntry = new ZipEntry("preview.png");
		        zos.putNextEntry(zipEntry);
		        ImageIO.write(image, "png", zos);
		        zos.closeEntry();
		    }
		}
	}
	
	public static CountryMatch[] matchCountries(LndFile srcLand, LndFile dstLand) {
		final CountryMatch[] res = new CountryMatch[srcLand.getCountries().size()];
		if (srcLand == dstLand) {
			for (int i = 0; i < res.length; i++) {
				res[i] = new CountryMatch(i, i, 0f);
			}
		} else {
			final List<LNDCountry> srcCountries = srcLand.getCountries();
			final List<LNDCountry> dstCountries = dstLand.getCountries();
			//Prepare a map of destination countries for fast lookup
			Map<CountryKey, LNDCountry> dstKeys = new HashMap<>();
			for (LNDCountry dstCountry : dstCountries) {
				dstKeys.put(new CountryKey(dstCountry), dstCountry);
			}
			//Find identical countries
			boolean allMatched = true;
			for (int srcIndex = 0; srcIndex < srcCountries.size(); srcIndex++) {
				LNDCountry srcCountry = srcCountries.get(srcIndex);
				LNDCountry dstCountry = dstKeys.get(new CountryKey(srcCountry));
				if (dstCountry != null) {
					res[srcIndex] = new CountryMatch(srcIndex, dstCountry.getIndex(), 0f);
				} else {
					allMatched = false;
				}
			}
			//If some haven't an identical match, find best match based on similarity
			if (!allMatched) {
				final Color[] srcMaterialsColors = getDominantColors(srcLand.getMaterials());
				final CountryFingerprint[] dstFps = getCountryFingerprints(dstLand);
				for (int srcIndex = 0; srcIndex < srcCountries.size(); srcIndex++) {
					if (res[srcIndex] == null) {
						LNDCountry srcCountry = srcCountries.get(srcIndex);
						CountryFingerprint srcFp = getCountryFingerprint(srcMaterialsColors, srcCountry);
						int bestIndex = -1;
						float minDist = Float.POSITIVE_INFINITY;
						for (int dstIndex = 0; dstIndex < dstFps.length; dstIndex++) {
							CountryFingerprint dstFp = dstFps[dstIndex];
							float dist = srcFp.distance(dstFp);
							if (dist < minDist) {
								bestIndex = dstIndex;
								minDist = dist;
							}
						}
						res[srcIndex] = new CountryMatch(srcIndex, bestIndex, minDist + 0.001f);
					}
				}
			}
		}
		return res;
	}
	
	private static CountryFingerprint[] getCountryFingerprints(LndFile land) {
		List<LNDCountry> countries = land.getCountries();
		CountryFingerprint[] res = new CountryFingerprint[countries.size()];
		Color[] materialsColors = getDominantColors(land.getMaterials());
		for (int i = 0; i < countries.size(); i++) {
			LNDCountry country = countries.get(i);
			res[i] = getCountryFingerprint(materialsColors, country);
		}
		return res;
	}
	
	private static Color[] getDominantColors(List<LNDMaterial> materials) {
		Color[] res = new Color[materials.size()];
		for (int i = 0; i < materials.size(); i++) {
			res[i] = MaterialTool.getDominantColor(materials.get(i));
		}
		return res;
	}
	
	private static CountryFingerprint getCountryFingerprint(Color[] materialsColors, LNDCountry country) {
		final CountryFingerprint res = new CountryFingerprint();
		int pMat = -1;
		for (LNDMapMaterial mapMaterial : country.getMapMaterialsForRead()) {
			int mat = mapMaterial.getFirstMaterialIndex();
			if (mat != pMat) {
				res.add(materialsColors[mat]);
				pMat = mat;
			}
		}
		return res;
	}
	
	
	private static class CountryFingerprint {
		private final List<Vec3f> colors = new ArrayList<>();
		
		public void add(Color color) {
			colors.add(new Vec3f(color.r, color.g, color.b));
		}
		
		/**
		 * @param other
		 * @return the std deviation of the colors
		 */
		public float distance(CountryFingerprint other) {
			float sum = 0;
			List<Vec3f> colors0, colors1;
			if (this.colors.size() >= other.colors.size()) {
				colors0 = this.colors;
				colors1 = other.colors;
			} else {
				colors0 = other.colors;
				colors1 = this.colors;
			}
			int u0 = colors0.size() - 1;
			int u1 = colors1.size() - 1;
			for (int i0 = 0; i0 < colors0.size(); i0++) {
				float p = (float)i0 / u0;
				int i1 = Math.round(p * u1);
				Vec3f c0 = colors0.get(i0);
				Vec3f c1 = colors1.get(i1);
				sum += c0.dst2(c1);
			}
			return (float)Math.sqrt(sum / colors0.size());
		}
		
		@Override
		public String toString() {
			return colors.toString();
		}
	}
	
	
	public static class CountryMatch {
		public final int srcCountry;
		public final int dstCountry;
		public final float distance;
		
		public CountryMatch(int srcCountry, int dstCountry, float distance) {
			this.srcCountry = srcCountry;
			this.dstCountry = dstCountry;
			this.distance = distance;
		}
		
		@Override
		public String toString() {
			return srcCountry + " -> " + dstCountry + ": " + distance;
		}
	}
	
	
	public static class CountryKey {
		public final LNDCountry country;
		private final int hash;
		
		public CountryKey(LNDCountry country) {
			this.country = country;
			this.hash = getHash(country);
		}
		
		@Override
		public int hashCode() {
			return hash;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof CountryKey)) return false;
			return equals(this.country, ((CountryKey)obj).country);
		}
		
		
		public static int getHash(LNDCountry country) {
			if (country.getLand() == null) return 0;
			int hash = 0;
			List<LNDMaterial> materials = country.getLand().getMaterials();
			int pm0 = -1;
			int pm1 = -1;
			int n = 0;
			for (int i = 0; i < country.getMapMaterialsForRead().length; i++) {
				LNDMapMaterial mapMaterial = country.getMapMaterialsForRead()[i];
				int m0 = mapMaterial.getFirstMaterialIndex();
				int m1 = mapMaterial.getSecondMaterialIndex();
				if (m0 != pm0) {
					LNDMaterial material = materials.get(m0);
					hash ^= Integer.rotateLeft(material.hashCode(), n++);
					pm0 = m0;
				}
				if (m1 != pm1) {
					LNDMaterial material = materials.get(m1);
					hash ^= Integer.rotateLeft(material.hashCode(), n++);
					pm1 = m1;
				}
			}
			return hash;
		}
		
		public static boolean equals(LNDCountry a, LNDCountry b) {
			if (a.getTerrainType() != b.getTerrainType()) return false;
			if (a.getLand() == b.getLand()) {
				for (int i = 0; i < a.getMapMaterialsForRead().length; i++) {
					LNDMapMaterial aMat = a.getMapMaterialsForRead()[i];
					LNDMapMaterial bMat = b.getMapMaterialsForRead()[i];
					if (aMat.getFirstMaterialIndex() != aMat.getSecondMaterialIndex()) {
						if (aMat.getCoefficient() != bMat.getCoefficient()) return false;
					}
					if (aMat.getFirstMaterialIndex() != bMat.getFirstMaterialIndex()) return false;
					if (aMat.getSecondMaterialIndex() != bMat.getSecondMaterialIndex()) return false;
				}
				return true;
			}
			if (a.getLand() == null || b.getLand() == null) return false;
			List<LNDMaterial> aMaterials = a.getLand().getMaterials();
			List<LNDMaterial> bMaterials = b.getLand().getMaterials();
			int pam0 = -1;
			int pam1 = -1;
			int pbm0 = -1;
			int pbm1 = -1;
			for (int i = 0; i < a.getMapMaterialsForRead().length; i++) {
				LNDMapMaterial aMat = a.getMapMaterialsForRead()[i];
				LNDMapMaterial bMat = b.getMapMaterialsForRead()[i];
				int am0 = aMat.getFirstMaterialIndex();
				int am1 = aMat.getSecondMaterialIndex();
				int bm0 = bMat.getFirstMaterialIndex();
				int bm1 = bMat.getSecondMaterialIndex();
				if (am0 != am1) {
					if (aMat.getCoefficient() != bMat.getCoefficient()) return false;
				}
				if (am0 != pam0 || bm0 != pbm0) {
					if ((am0 != pam0) != (bm0 != pbm0)) return false;
					LNDMaterial aMaterial = aMaterials.get(am0);
					LNDMaterial bMaterial = bMaterials.get(bm0);
					if (!aMaterial.equals(bMaterial)) return false;
					pam0 = am0;
					pbm0 = bm0;
				}
				if (am1 != pam1 || bm1 != pbm1) {
					if ((am1 != pam1) != (bm1 != pbm1)) return false;
					LNDMaterial aMaterial = aMaterials.get(am1);
					LNDMaterial bMaterial = bMaterials.get(bm1);
					if (!aMaterial.equals(bMaterial)) return false;
					pam1 = am1;
					pbm1 = bm1;
				}
			}
			return true;
		}
	}
}
