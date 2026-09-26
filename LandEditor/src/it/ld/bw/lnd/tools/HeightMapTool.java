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
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import it.ld.bw.lnd.model.LH3DLandBlock;
import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LH3DLandCell.Sound;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LNDMaterial;
import it.ld.bw.lnd.model.LndFile;
import it.ld.utils.MathUtils;

public final class HeightMapTool {
	private final LndFile land;
	private LNDCountry country;
	private LNDCountry cliffCountry;
	private LNDCountry coastlineCountry;
	private float cliffSlope = 1f;
	private Sound defaultSound = Sound.BIRDS;
	
	public HeightMapTool(LndFile land) {
		this.land = land;
	}
	
	public LNDCountry getCountry() {
		return country;
	}
	
	public void setCountry(LNDCountry country) {
		this.country = country;
	}
	
	public LNDCountry getCliffCountry() {
		return cliffCountry;
	}
	
	public void setCliffCountry(LNDCountry cliffCountry) {
		this.cliffCountry = cliffCountry;
	}
	
	public LNDCountry getCoastlineCountry() {
		return coastlineCountry;
	}
	
	public void setCoastlineCountry(LNDCountry coastlineCountry) {
		this.coastlineCountry = coastlineCountry;
	}
	
	public float getCliffSlope() {
		return cliffSlope;
	}
	
	public void setCliffSlope(float cliffSlope) {
		this.cliffSlope = cliffSlope;
	}
	
	public Sound getDefaultSound() {
		return defaultSound;
	}
	
	public void setDefaultSound(Sound defaultSound) {
		this.defaultSound = defaultSound;
	}
	
	public LndFile createLandFromHeightMap(int[][] hmap, int altitudeBits) {
		List<LH3DLandBlock> blocks = createBlocksFromHeightMap(hmap, land.getAltitudeBits());
		LndFile newLand = new LndFile(true);
		newLand.setCoastlineBumpMap(land.getCoastlineBumpMap().clone());
		newLand.setNoiseMap(land.getNoiseMap().clone());
		for (LNDMaterial material : land.getMaterials()) {
			newLand.getMaterials().add(material.clone());
		}
		for (LNDCountry country : land.getCountries()) {
			newLand.getCountries().add(country.clone());
		}
		newLand.getLandBlocksForRead().addAll(blocks);
		return newLand;
	}
	
	//TODO improve automatic sound
	public List<LH3DLandBlock> createBlocksFromHeightMap(int[][] hmap, int altitudeBits) {
		final int xCells = hmap.length;
		final int zCells = hmap[0].length;
		final int xBlocks = MathUtils.ceilDiv(xCells, LH3DLandBlock.SEG_PER_SIDE);
		final int zBlocks = MathUtils.ceilDiv(zCells, LH3DLandBlock.SEG_PER_SIDE);
		//Create a grid with required blocks
		LH3DLandBlock[][] blocks = new LH3DLandBlock[xBlocks][zBlocks];
		int numBlocks = 0;
		for (int bx = 0; bx < xBlocks; bx++) {
			final int basex = bx * LH3DLandBlock.SEG_PER_SIDE;
			final int endx = Math.min(basex + LH3DLandBlock.SEG_PER_SIDE, xCells);
			for (int bz = 0; bz < zBlocks; bz++) {
				final int basez = bz * LH3DLandBlock.SEG_PER_SIDE;
				final int endz = Math.min(basez + LH3DLandBlock.SEG_PER_SIDE, zCells);
				boolean isEmpty = true;
				for (int ax = basex; ax < endx && isEmpty; ax++) {
					for (int az = basez; az < endz; az++) {
						if (hmap[ax][az] > 0) {
							isEmpty = false;
							break;
						}
					}
				}
				if (!isEmpty) {
					LH3DLandBlock block = new LH3DLandBlock(bx, bz, altitudeBits);
					blocks[bx][bz] = block;
					numBlocks++;
				}
			}
		}
		//
		ArrayList<LH3DLandBlock> res = new ArrayList<LH3DLandBlock>(numBlocks);
		for (int bx = 0; bx < xBlocks; bx++) {
			final int basex = bx * LH3DLandBlock.SEG_PER_SIDE;
			final int endx = Math.min(basex + LH3DLandBlock.POINTS_PER_SIDE, xCells);
			for (int bz = 0; bz < zBlocks; bz++) {
				final int basez = bz * LH3DLandBlock.SEG_PER_SIDE;
				final int endz = Math.min(basez + LH3DLandBlock.POINTS_PER_SIDE, zCells);
				LH3DLandBlock block = blocks[bx][bz];
				if (block != null) {
					res.add(block);
					for (int ax = basex; ax < endx; ax++) {
						for (int az = basez; az < endz; az++) {
							int h00 = hmap[ax][az];
							int h10 = (ax + 1 < xCells) ? hmap[ax + 1][az] : 0;
							int h01 = (az + 1 < zCells) ? hmap[ax][az + 1] : 0;
							int h11 = (ax + 1 < xCells && az + 1 < zCells) ? hmap[ax + 1][az + 1] : 0;
							
							int dh0 = Math.abs(h11 - h00);
							int dh1 = Math.abs(h01 - h10);
							
							LH3DLandCell cell = block.getCell(ax - basex, az - basez);
							cell.setSplit(dh0 > dh1);
							cell.setAltitude(h00);
							if (h00 < LH3DLandCell.COASTLINE_ALTITUDE) {
								cell.setWater(true);
								cell.setSound(Sound.OCEAN);
							} else if (h00 == LH3DLandCell.COASTLINE_ALTITUDE) {
								cell.setWater(false);
								cell.setCoastLine(true);
								cell.setCountry(coastlineCountry.getIndex());
								cell.setSound(Sound.COAST);
							} else if (h00 < LH3DLandCell.DRY_ALTITUDE) {
								cell.setWater(false);
								cell.setCountry(coastlineCountry.getIndex());
								cell.setSound(Sound.COAST);
							} else {
								cell.setWater(false);
								float slope = Math.max(dh0, dh1) / LH3DLandCell.CELL_SIZE;
								cell.setCountry(slope > cliffSlope ? cliffCountry.getIndex() : country.getIndex());
								cell.setSound(Sound.BIRDS);
							}
						}
					}
				}
			}
		}
		return res;
	}
	
	//TODO improve automatic sound
	public void importHeightMap(final int offsetx, final int offsetz, int[][] hmap) {
		if (offsetx < 0 || offsetz < 0) throw new IllegalArgumentException("Offset can't be negative");
		final int xCells = hmap.length;
		final int zCells = hmap[0].length;
		final int baseBlockX = offsetx / LH3DLandBlock.SEG_PER_SIDE;
		final int baseBlockZ = offsetz / LH3DLandBlock.SEG_PER_SIDE;
		final int baseCellX = baseBlockX * LH3DLandBlock.SEG_PER_SIDE;
		final int baseCellZ = baseBlockZ * LH3DLandBlock.SEG_PER_SIDE;
		final int localOffsetX = offsetx - baseCellX;
		final int localOffsetZ = offsetz - baseCellZ;
		final int xBlocks = MathUtils.ceilDiv(localOffsetX + xCells, LH3DLandBlock.SEG_PER_SIDE);
		final int zBlocks = MathUtils.ceilDiv(localOffsetZ + zCells, LH3DLandBlock.SEG_PER_SIDE);
		if (baseBlockX + xBlocks - 1 >= land.getBlocksPerSide() || baseBlockZ + zBlocks - 1 >= land.getBlocksPerSide()) {
			throw new IllegalArgumentException("Destination is out of bounds");
		}
		//Ensure capacity
		final int maxNewBlocks = xBlocks * zBlocks;
		final int oldLimit = land.getMaxBlocks();
		if (land.getNumBlocks() + maxNewBlocks > oldLimit) {
			land.setMaxBlocks(land.getNumBlocks() + maxNewBlocks);
		}
		//Add non-empty blocks
		for (int bx = baseBlockX; bx < baseBlockX + xBlocks; bx++) {
			final int basex = bx * LH3DLandBlock.SEG_PER_SIDE;
			for (int bz = baseBlockZ; bz < baseBlockZ + zBlocks; bz++) {
				final int basez = bz * LH3DLandBlock.SEG_PER_SIDE;
				LH3DLandBlock block = land.getBlock(bx, bz);
				if (block == null) {
					boolean isEmpty = true;
					for (int cx = 0; cx < LH3DLandBlock.SEG_PER_SIDE && isEmpty; cx++) {
						final int ax = basex + cx;
						final int srcX = ax - offsetx;
						if (srcX < 0) continue;
						if (srcX >= xCells) break;
						for (int cz = 0; cz < LH3DLandBlock.SEG_PER_SIDE; cz++) {
							final int az = basez + cz;
							final int srcZ = az - offsetz;
							if (srcZ < 0) continue;
							if (srcZ >= zCells) break;
							if (hmap[srcX][srcZ] > 0) {
								isEmpty = false;
								break;
							}
						}
					}
					if (!isEmpty) {
						block = land.addBlockAt(bx, bz);
					}
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
					for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
						final int ax = basex + cx;
						final int srcX = ax - offsetx;
						if (srcX < 0) continue;
						if (srcX >= xCells) break;
						for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
							final int az = basez + cz;
							final int srcZ = az - offsetz;
							if (srcZ < 0) continue;
							if (srcZ >= zCells) break;
							int h00 = hmap[srcX][srcZ];
							if (h00 > 0) {
								int h10 = (srcX + 1 < xCells) ? hmap[srcX + 1][srcZ] : 0;
								int h01 = (srcZ + 1 < zCells) ? hmap[srcX][srcZ + 1] : 0;
								int h11 = (srcX + 1 < xCells && srcZ + 1 < zCells) ? hmap[srcX + 1][srcZ + 1] : 0;
								int dh0 = Math.abs(h11 - h00);
								int dh1 = Math.abs(h01 - h10);
								LH3DLandCell cell = block.getCell(cx, cz);
								cell.setAltitude(h00);
								cell.setSplit(dh0 > dh1);
								cell.setAltitude(h00);
								if (h00 < LH3DLandCell.COASTLINE_ALTITUDE) {
									cell.setWater(true);
									cell.setSound(Sound.OCEAN);
								} else if (h00 == LH3DLandCell.COASTLINE_ALTITUDE) {
									cell.setWater(false);
									cell.setCoastLine(true);
									cell.setCountry(coastlineCountry.getIndex());
									cell.setSound(Sound.COAST);
								} else if (h00 < LH3DLandCell.DRY_ALTITUDE) {
									cell.setWater(false);
									cell.setCountry(coastlineCountry.getIndex());
									cell.setSound(Sound.COAST);
								} else {
									cell.setWater(false);
									float slope = Math.max(dh0, dh1) / LH3DLandCell.CELL_SIZE;
									cell.setCountry(slope > cliffSlope ? cliffCountry.getIndex() : country.getIndex());
									cell.setSound(Sound.BIRDS);
								}
							}
						}
					}
				}
			}
		}
		//Restore old capacity
		land.setMaxBlocks(oldLimit);
	}
	
	/**Returns the height map as int[width][height], with [0][0] at the bottom left corner. Values are in range [0, 255].
	 * @param land
	 * @return
	 */
	public int[][] getHeightMap() {
		final int size = land.getCellsPerSide();
		final int upper = size - 1;
		int[][] res = new int[size][size];
		for (int bx = 0; bx < land.getBlocksPerSide(); bx++) {
			final int basex = bx * LH3DLandBlock.SEG_PER_SIDE;
			for (int bz = 0; bz < land.getBlocksPerSide(); bz++) {
				final int basez = bz * LH3DLandBlock.SEG_PER_SIDE;
				LH3DLandBlock block = land.getBlock(bx, bz);
				if (block != null) {
					for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
						final int ax = basex + cx;
						for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
							final int az = basez + cz;
							LH3DLandCell cell = block.getCell(cx, cz);
							res[ax][upper - az] = cell.getAltitude();
						}
					}
				}
			}
		}
		return res;
	}
	
	public void exportHeightMap(File dst) throws IOException {
		final int[][] data = getHeightMap();
		final int w = data.length;
		final int h = data[0].length;
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = img.getRaster();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) { 
                raster.setSample(x, y, 0, data[x][y]);
            }
        }
        if (!ImageIO.write(img, "png", dst)) {
        	throw new IOException("No writer found");
        }
	}
}
