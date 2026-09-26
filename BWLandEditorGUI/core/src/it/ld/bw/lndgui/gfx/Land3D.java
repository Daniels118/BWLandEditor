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

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

import it.ld.bw.lnd.model.BulkUpdate;
import it.ld.bw.lnd.model.LH3DLandBlock;
import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LH3DLandCell.Sound;
import it.ld.bw.lnd.model.LNDMaterial;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.SimpleMap;
import it.ld.bw.lndgui.SelectionMode;
import it.ld.bw.lndgui.gfx.LandShader.LandAttribute;
import it.ld.bw.lndgui.tools.Selection;
import it.ld.libgdx.utils.Utils;
import it.ld.utils.Listeners;
import it.ld.utils.UChangeListener;
import it.ld.utils.UChangeListener.EventType;

import static it.ld.bw.lnd.model.LH3DLandBlock.BLOCK_SIZE;
import static it.ld.bw.lnd.model.LH3DLandBlock.POINTS_PER_SIDE;
import static it.ld.bw.lnd.model.LH3DLandBlock.SEG_PER_SIDE;
import static it.ld.bw.lnd.model.LH3DLandCell.CELL_SIZE;

public class Land3D implements RenderableProvider, Disposable {
	public enum Property {MATERIALS, COUNTRIES, NOISE_MAP, BUMP_MAP, SMALL_BUMP, BLOCKS, SELECTION}
	public final Listeners listeners = new Listeners(this);
	
	public final LndFile land;
	
	private final ArrayList<Material3D> materials = new ArrayList<>();
	
	private final LandAttribute landAttribute = new LandAttribute();
	private final Material landMaterial = new Material(landAttribute);
	
	private Texture noise;
	private Texture coastlineBump;
	private Texture smallBump;	//Shared, do not dispose!
	private TextureArray materialsArray;
	
	private Block3D[][] blocks;
	private byte[][] changedBlocksLUT;
	private int[] changedBlocks = new int[80];
	private int numChangedBlocks = 0;
	
	private Bounds3D bounds = null;
	
	private final int maxTextureSize;
	private Texture heightMapTexture;
	
	public Land3D(LndFile land) {
		this.land = land;
		//
		IntBuffer buf = BufferUtils.newIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buf);
		maxTextureSize = buf.get(0);
		//
		updateMaterials();
		//
		initBlocks();
		//
		noise = Utils.toTexture(land.getNoiseMap().getPixels(), SimpleMap.width, SimpleMap.height, true);
		coastlineBump = Utils.toTexture(land.getCoastlineBumpMap().getPixels(), SimpleMap.width, SimpleMap.height, true);
		landAttribute.setLand(this);
		//
		land.listeners.add(landChangeListener);
	}
	
	private final UChangeListener landChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LndFile.Property.MATERIALS) {
				updateMaterials();
				listeners.notify(EventType.CHANGE, Property.MATERIALS);
			} else if (event.getProperty() == LndFile.Property.COUNTRIES) {
				listeners.notify(EventType.CHANGE, Property.COUNTRIES);
			} else if (event.getProperty() == LndFile.Property.NOISE_MAP) {
				noise.dispose();
				noise = Utils.toTexture(land.getNoiseMap().getPixels(), SimpleMap.width, SimpleMap.height, true);
				listeners.notify(EventType.CHANGE, Property.NOISE_MAP);
			} else if (event.getProperty() == LndFile.Property.COASTLINE_BUMPMAP) {
				coastlineBump.dispose();
				coastlineBump = Utils.toTexture(land.getCoastlineBumpMap().getPixels(), SimpleMap.width, SimpleMap.height, true);
				listeners.notify(EventType.CHANGE, Property.BUMP_MAP);
			} else if (event.getProperty() == LndFile.Property.BLOCKS) {
				LH3DLandBlock block = null;
				if (event.getType() == EventType.ADD) {
					block = (LH3DLandBlock) event.getNewValue();
					Block3D block3D = new Block3D(Land3D.this, block);
					setBlockChanged(block.getBlockX(), block.getBlockZ());
					block3D.listeners.add(block3DChangeListener);
					blocks[block.getBlockX()][block.getBlockZ()] = block3D;
				} else if (event.getType() == EventType.CHANGE) {
					block = (LH3DLandBlock) event.getNewValue();
					setBlockChanged(block.getBlockX(), block.getBlockZ());
				} else if (event.getType() == EventType.REMOVE) {
					block = (LH3DLandBlock) event.getOldValue();
					setBlockChanged(block.getBlockX(), block.getBlockZ());
					Block3D block3D = blocks[block.getBlockX()][block.getBlockZ()];
					if (block3D != null) {
						block3D.listeners.remove(block3DChangeListener);
						block3D.dispose();
						blocks[block.getBlockX()][block.getBlockZ()] = null;
					}
				}
				if (event.isLast()) {
					bounds = null;
					listeners.notify(EventType.CHANGE, Property.BLOCKS);
				}
			} else if (event.getProperty() == LndFile.Property.BLOCKS_PER_SIDE) {
				initBlocks();
			}
		}
	};
	
	private void initBlocks() {
		if (blocks != null) {
			clearBlocks();
		}
		int heightMapSize = (int)Math.ceil(Math.min(land.getCellsPerSide(), maxTextureSize) / 4f) * 4;
		heightMapTexture = new Texture(heightMapSize, heightMapSize, Format.RGB888);
		heightMapTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		heightMapTexture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		//
		blocks = new Block3D[land.getBlocksPerSide()][land.getBlocksPerSide()];
		for (LH3DLandBlock block : land.getLandBlocksForRead()) {
			Block3D block3D = new Block3D(this, block);
			updateHeightMap(block.getBlockX(), block.getBlockZ());
			block3D.listeners.add(block3DChangeListener);
			blocks[block.getBlockX()][block.getBlockZ()] = block3D;
		}
		changedBlocksLUT = new byte[land.getBlocksPerSide()][land.getBlocksPerSide()];
		bounds = null;
	}
	
	private void clearBlocks() {
		heightMapTexture.dispose();
		heightMapTexture = null;
		//
		for (Block3D[] col : blocks) {
			for (Block3D block : col) {
				if (block != null) {
					block.listeners.remove(block3DChangeListener);
					block.dispose();
				}
			}
		}
		blocks = null;
	}
	
	private final UChangeListener block3DChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.CHANGE && event.getProperty() == Block3D.Property.SELECTION && event.isLast()) {
				listeners.notify(EventType.CHANGE, Property.SELECTION);
			}
		}
	};
	
	private void setBlockChanged(int bx, int bz) {
		if (changedBlocksLUT[bx][bz] == 0) {
			int packedPos = bx | (bz << 16);
			if (numChangedBlocks >= changedBlocks.length) {
				int newSize = Math.min(changedBlocks.length * 2, land.getBlocksPerSide() * land.getBlocksPerSide());
				int[] tmp = new int[newSize];
				System.arraycopy(changedBlocks, 0, tmp, 0, changedBlocks.length);
				changedBlocks = tmp;
			}
			changedBlocks[numChangedBlocks] = packedPos;
			numChangedBlocks++;
			changedBlocksLUT[bx][bz] = 1;
		}
	}
	
	private void clearChangedBlocks() {
		for (int i = 0; i < numChangedBlocks; i++) {
			int packedPos = changedBlocks[i];
			int bx = packedPos & 0xFFFF;
			int bz = (packedPos >>> 16) & 0xFFFF;
			changedBlocksLUT[bx][bz] = 0;
		}
		numChangedBlocks = 0;
	}
	
	private void updateMaterials() {
		//Cleanup
		if (materialsArray != null) {
			materialsArray.dispose();
			materialsArray = null;
		}
		this.materials.clear();
		//
		if (land.getMaterials().isEmpty()) {
			Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
			pixmap.drawPixel(0, 0, Color.rgba8888(Color.BROWN));
			Pixmap[] pixmaps = new Pixmap[] {pixmap};
			PixmapTextureArrayData data = new PixmapTextureArrayData(pixmaps);
			materialsArray = new TextureArray(data);
			materialsArray.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			pixmap.dispose();
		} else {
			Pixmap[] pixmaps = new Pixmap[land.getMaterials().size()];
			int i = 0;
			for (LNDMaterial lndmat : land.getMaterials()) {
				Material3D mat = new Material3D(lndmat);
				this.materials.add(mat);
				pixmaps[i++] = Utils.toPixmap(mat.getLandMaterial().getIntARGB(), LNDMaterial.width, LNDMaterial.height);
			}
			PixmapTextureArrayData data = new PixmapTextureArrayData(pixmaps);
			materialsArray = new TextureArray(data);
			materialsArray.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			materialsArray.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			for (i = 0; i < pixmaps.length; i++) {
				pixmaps[i].dispose();
			}
		}
	}
	
	private void updateHeightMap(int bx, int bz) {
		Pixmap pixmap = new Pixmap(LH3DLandBlock.POINTS_PER_SIDE, LH3DLandBlock.POINTS_PER_SIDE, Format.RGB888);
		final int altitudeShift = land.getAltitudeBits() - 8;
		final int basex = bx * LH3DLandBlock.SEG_PER_SIDE;
		final int basez = bz * LH3DLandBlock.SEG_PER_SIDE;
		LH3DLandBlock block = land.getBlock(bx, bz);
		if (block == null) {
			int rgba = 0x000000FF;
			for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
				for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
					pixmap.drawPixel(cx, cz, rgba);
				}
			}
		} else {
			for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
				for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
					int c = block.getCell(cx, cz).getAltitude() >> altitudeShift;
					int rgba = (c << 24) | 0x000000FF;
					pixmap.drawPixel(cx, cz, rgba);
				}
			}
		}
		heightMapTexture.bind();
		Gdx.gl.glTexSubImage2D(
		    GL20.GL_TEXTURE_2D, 0,
		    basex,
		    basez,
		    pixmap.getWidth(), pixmap.getHeight(),
		    GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE,
		    pixmap.getPixels()
		);
		int err = Gdx.gl.glGetError();
		if (err != GL20.GL_NO_ERROR) {
		    System.err.println("glTexSubImage2D error: " + err);
		}
		pixmap.dispose();
	}
	
	public LandAttribute getMaterialAttribute() {
		return this.landAttribute;
	}
	
	public Material getLandMaterial() {
		return this.landMaterial;
	}
	
	public Texture getNoiseMap() {
		return this.noise;
	}
	
	public Texture getCoastlineBumpMap() {
		return this.coastlineBump;
	}
	
	public Texture getSmallBump() {
		return this.smallBump;
	}
	
	public void setSmallBump(Texture smallBump) {
		if (smallBump != this.smallBump) {
			this.smallBump = smallBump;
			listeners.notify(EventType.CHANGE, Property.SMALL_BUMP);
		}
	}
	
	public TextureArray getMaterials() {
		return this.materialsArray;
	}
	
	public Texture getHeightMap() {
		return heightMapTexture;
	}
	
	public Vector3 pickPos(Ray ray) {
		Vector3 pos = new Vector3();
		Vector3 end = new Vector3();
		Bounds3D bounds = this.getBounds();
		if (bounds.intersect(ray, pos, end)) {
			Vector3 step = new Vector3(ray.direction).scl(CELL_SIZE);
			boolean refining = false;
			while (bounds.containsXZ(pos)) {
				if (pos.y < 0) {
					return pickBase(ray);
				} else if (pos.y <= this.getHeight(pos)) {
					if (refining) {
						return pos;
					} else {
						pos.sub(step);
						step.scl(0.2f);
						refining = true;
					}
				}
				pos.add(step);
			}
		}
		return pickBase(ray);
	}
	
	public Coord pickCoord(Ray ray) {
		Vector3 pos = pickPos(ray);
		if (pos == null) return null;
		return new Coord(pos);
	}
	
	private Vector3 pickBase(Ray ray) {
		Vector3 direction = new Vector3(ray.direction);
		float distance = ray.origin.y / -direction.y;
		if (distance <= 0) return null;
		return direction.scl(distance).add(ray.origin);
	}
	
	private final Coord tmpCoord = new Coord();
	
	public float getHeight(Vector3 pos) {
		tmpCoord.set(pos);
		return land.getHeight(tmpCoord.x, tmpCoord.z);
	}
	
	public Block3D getBlock(Vector3 pos) {
		int bx = (int)(pos.x / BLOCK_SIZE);
		int bz = (int)(-pos.z / BLOCK_SIZE);
		return getBlock(bx, bz);
	}
	
	public Block3D getBlock(int bx, int bz) {
		if (bx < 0 || bz < 0 || bx >= land.getBlocksPerSide() || bz >= land.getBlocksPerSide()) return null;
		return blocks[bx][bz];
	}
	
	public Bounds3D getBounds() {
		if (bounds == null) {
			bounds = new Bounds3D();
			for (Block3D[] col : blocks) {
				for (Block3D block : col) {
					if (block != null) {
						bounds.update(block.getMapX(), 0f, block.getMapZ());
						bounds.update(block.getMapX() + LH3DLandBlock.BLOCK_SIZE, block.getHighestAltitude(), block.getMapZ() - LH3DLandBlock.BLOCK_SIZE);
					}
				}
			}
		}
		return bounds;
	}
	
	public void clearSelection() {
		for (int bx = 0; bx < land.getBlocksPerSide(); bx++) {
			for (int bz = 0; bz < land.getBlocksPerSide(); bz++) {
				Block3D block = blocks[bx][bz];
				if (block != null) {
					block.clearSelection();
				}
			}
		}
	}
	
	public void select(Selection selection, SelectionMode mode) {
		if (mode == SelectionMode.NEW) {
			clearSelection();
		}
		if (!selection.isClosed()) return;
		final int w1 = land.getCellsPerSide() - 1;
		final int n = selection.size();
		Bounds3D selBounds = selection.getBounds();
		float minZ = selBounds.getLow().z;
		int minCZ = (int)Math.floor(minZ / CELL_SIZE);
		int minBZ = MathUtils.clamp(minCZ / SEG_PER_SIDE, 0, land.getBlocksPerSide() - 1);
		float maxZ = selBounds.getHigh().z;
		int maxCZ = (int)Math.ceil(maxZ / CELL_SIZE);
		int maxBZ = MathUtils.clamp(maxCZ / SEG_PER_SIDE, 0, land.getBlocksPerSide() - 1);
		for (int bz = minBZ; bz <= maxBZ; bz++) {
			final int basez = bz * SEG_PER_SIDE;
			for (int cz = 0; cz < POINTS_PER_SIDE; cz++) {
				final int az = basez + cz;
				if (az < minCZ || az > maxCZ) continue;
				final float z = CELL_SIZE * az;
				List<Float> intersections = new ArrayList<>();
				for (int i = 1; i < n; i++) {
					Coord p1 = selection.get(i - 1);
					Coord p2 = selection.get(i);
					if ((p1.z <= z && p2.z > z) || (p2.z <= z && p1.z > z)) {
		                float x = p1.x + (z - p1.z) * (p2.x - p1.x) / (p2.z - p1.z);
		                intersections.add(x);
		            }
				}
				Collections.sort(intersections);
				for (int i = 1; i < intersections.size(); i += 2) {
		            float x0 = intersections.get(i - 1);
		            float x1 = intersections.get(i);
		            int ax0 = Math.max(0, (int)Math.ceil(x0 / CELL_SIZE));
		            int ax1 = Math.min((int)Math.floor(x1 / CELL_SIZE), w1);
		            int bx0 = ax0 / SEG_PER_SIDE;
		            int bx1 = ax1 / SEG_PER_SIDE;
		            for (int bx = bx0; bx <= bx1; bx++) {
		            	Block3D block = blocks[bx][bz];
		            	if (block != null) {
		            		try (BulkUpdate<byte[]> update = block.updateSelection()) {
					            final int bax0 = bx * SEG_PER_SIDE;
					            final int bax1 = bax0 + SEG_PER_SIDE;
					            final int startX = Math.max(bax0, ax0);
					            final int endX = Math.min(ax1, bax1);
			            		for (int ax = startX; ax <= endX; ax++) {
					                int cx = ax - bax0;
					                LH3DLandCell cell = block.getCellAt(cx, cz);
					                if (cell.getAltitude() > 0 || (cell.getSoundEnum() != Sound.OCEAN && cell.getSoundEnum() != Sound.NONE)) {
						                switch (mode) {
						                	case NEW:	
						                	case ADD:
						                		update.data[cx][cz] = 1;
												break;
											case INTERSECT:
												if (update.data[cx][cz] == 1) {
													update.data[cx][cz] = 2;
						                		}
												break;
											case SUBTRACT:
												update.data[cx][cz] = 0;
												break;
						                }
					                }
					            }
		            		}
		            	}
		            }
		        }
			}
		}
		if (mode == SelectionMode.INTERSECT) {
			for (int bx = 0; bx < land.getBlocksPerSide(); bx++) {
				for (int bz = 0; bz < land.getBlocksPerSide(); bz++) {
					Block3D block = blocks[bx][bz];
					if (block != null) {
						block.processSelectionIntersection();
					}
				}
			}
		}
	}
	
	public boolean hasSelectedCells() {
		for (int bx = 0; bx < land.getBlocksPerSide(); bx++) {
			for (int bz = 0; bz < land.getBlocksPerSide(); bz++) {
				Block3D block = blocks[bx][bz];
				if (block != null && block.hasSelectedCells()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public List<Block3D> getSelectedBlocks() {
		List<Block3D> res = new ArrayList<>();
		for (int bx = 0; bx < land.getBlocksPerSide(); bx++) {
			for (int bz = 0; bz < land.getBlocksPerSide(); bz++) {
				Block3D block = blocks[bx][bz];
				if (block != null && block.hasSelectedCells()) {
					res.add(block);
				}
			}
		}
		return res;
	}
	
	public boolean isWithinSelection(Coord coord) {
		int ax = Math.round(coord.x / CELL_SIZE);
		int az = Math.round(coord.z / CELL_SIZE);
		int bx = ax / SEG_PER_SIDE;
		int bz = az / SEG_PER_SIDE;
		Block3D block = blocks[bx][bz];
		if (block != null) {
			int cx = ax - bx * SEG_PER_SIDE;
			int cz = az - bz * SEG_PER_SIDE;
			return block.isSelected(cx, cz);
		}
		return false;
	}
	
	public void act(float delta) {
		for (int i = 0; i < numChangedBlocks; i++) {
			int packedPos = changedBlocks[i];
			int bx = packedPos & 0xFFFF;
			int bz = (packedPos >>> 16) & 0xFFFF;
			updateHeightMap(bx, bz);
		}
		clearChangedBlocks();
	}

	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		for (Block3D[] col : blocks) {
			for (Block3D block : col) {
				if (block != null) {
					block.getRenderables(renderables, pool);
				}
			}
		}
	}

	@Override
	public void dispose() {
		land.listeners.remove(landChangeListener);
		clearBlocks();
		for (Material3D material : materials) {
			material.dispose();
		}
		materials.clear();
		if (landAttribute != null) landAttribute.dispose();
		if (materialsArray != null) materialsArray.dispose();
		if (noise != null) noise.dispose();
		if (coastlineBump != null) coastlineBump.dispose();
	}
}
