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

import it.ld.bw.lnd.model.BulkUpdate;
import it.ld.bw.lnd.model.BulkUpdate.BulkUpdateListener;
import it.ld.bw.lnd.model.BulkUpdate.Change;
import it.ld.bw.lnd.model.LH3DLandBlock;
import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LNDMapMaterial;
import it.ld.utils.Listeners;
import it.ld.utils.UChangeListener;
import it.ld.utils.UChangeListener.EventType;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

import static it.ld.bw.lnd.model.LH3DLandBlock.BLOCK_SIZE;
import static it.ld.bw.lnd.model.LH3DLandBlock.POINTS_PER_SIDE;
import static it.ld.bw.lnd.model.LH3DLandBlock.SEG_PER_SIDE;
import static it.ld.bw.lnd.model.LH3DLandCell.CELL_SIZE;

import java.util.List;

public class Block3D implements RenderableProvider, Disposable {
	private static final int TRI_CNT = SEG_PER_SIDE * SEG_PER_SIDE * 2;
	private static final int VERT_CNT = TRI_CNT * 3;
	private static final int VERT_ATTR_CNT = 11;
	
	private static final VertexAttribute VAPosition = new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position");
	
	private static final VertexAttribute VAAttr0 = new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_attr0");
	private static final VertexAttribute VAAttr1 = new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_attr1");
	private static final VertexAttribute VAAttr2 = new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_attr2");
	
	private static final VertexAttribute VAWeight = new VertexAttribute(VertexAttributes.Usage.Generic, 3, "a_weight");
	private static final VertexAttribute VALightLevel = new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_lightLevel");
	private static final VertexAttribute VAAltitude = new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_altitude");
	
	public enum Property {SELECTION}
	public final Listeners listeners = new Listeners(this);
	
	public final Land3D land;
	public final LH3DLandBlock block;
	
	private final byte[][] selectedCells = new byte[LH3DLandBlock.POINTS_PER_SIDE][LH3DLandBlock.POINTS_PER_SIDE];
	
	private final Renderable renderable = new Renderable();
	private boolean meshReady = false;
	
	public Block3D(Land3D land, LH3DLandBlock block) {
		this.land = land;
		this.block = block;
		//
		renderable.worldTransform.setToTranslation(block.getMapX(), 0f, -block.getMapZ());
		renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
		renderable.meshPart.size = VERT_CNT;
		renderable.meshPart.mesh = new Mesh(false, VERT_CNT, 0,
            VAPosition,
            VAAttr0, VAAttr1, VAAttr2,
            VAWeight, VALightLevel, VAAltitude
        );
	    //
		renderable.material = land.getLandMaterial();
        //
        block.listeners.add(blockListener);
	}
	
	private final UChangeListener blockListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (!land.land.isBulkUpdatingBlocks()) {
				if (event.getType() == EventType.CHANGE) {
					if (event.getProperty() == LH3DLandBlock.Property.CELLS) {
						meshReady = false;
					} else if (event.getProperty() == LH3DLandBlock.Property.MAP_X || event.getProperty() == LH3DLandBlock.Property.MAP_Z) {
						renderable.worldTransform.setToTranslation(block.getMapX(), 0f, -block.getMapZ());
					}
				}
			}
		}
    };
    
    private void updateMesh() {
		float[] vertices = new float[VERT_CNT * VERT_ATTR_CNT];
		for (int ix = 0, dst = 0; ix < SEG_PER_SIDE; ix++) {
			for (int iz = 0; iz < SEG_PER_SIDE; iz++) {
	    		dst = makeQuad(vertices, dst, ix, iz);
	    	}
	    }
		renderable.meshPart.mesh.setVertices(vertices);
	}
	
	private int makeQuad(float[] vertices, int dst, int ix, int iz) {
		LH3DLandCell refCell = getCellAt(ix, iz);
		if (refCell.hasSplit()) {
			//diagonal is bottom-right -> top-left in cell coordinates (top=z+1)
			dst = makeTri(vertices, dst, ix, iz + 1, ix, iz, ix + 1, iz);
			dst = makeTri(vertices, dst, ix + 1, iz, ix + 1, iz + 1, ix, iz + 1);
		} else {
			//diagonal is top-right -> bottom-left in cell coordinates (top=z+1)
			dst = makeTri(vertices, dst, ix, iz, ix + 1, iz, ix + 1, iz + 1);
			dst = makeTri(vertices, dst, ix + 1, iz + 1, ix, iz + 1, ix, iz);
		}
		return dst;
	}
	
	private int makeTri(float[] vertices, int dst, int ix0, int iz0, int ix1, int iz1, int ix2, int iz2) {
		LH3DLandCell cell0 = getCellAt(ix0, iz0);
    	LH3DLandCell cell1 = getCellAt(ix1, iz1);
    	LH3DLandCell cell2 = getCellAt(ix2, iz2);
    	
    	boolean sel0 = selectedCells[ix0][iz0] != 0;
    	boolean sel1 = selectedCells[ix1][iz1] != 0;
    	boolean sel2 = selectedCells[ix2][iz2] != 0;
    	
		dst = makeVert(vertices, dst, ix0, iz0, cell0, cell0, cell1, cell2, 1f, 0f, 0f, sel0, sel1, sel2);
		dst = makeVert(vertices, dst, ix1, iz1, cell1, cell0, cell1, cell2, 0f, 1f, 0f, sel0, sel1, sel2);
		dst = makeVert(vertices, dst, ix2, iz2, cell2, cell0, cell1, cell2, 0f, 0f, 1f, sel0, sel1, sel2);
		return dst;
	}
	
	public LNDMapMaterial getMaterial(int x, int z, LH3DLandCell cell) {
		LNDCountry country = land.land.getCountries().get(cell.getCountry());
		int noise = this.land.land.getNoiseMap().getUnsigned(block.getBlockX() * 16 + x, block.getBlockZ() * 16 + z) / 4;
		int h = Math.min(cell.getAltitude() + noise, 255);
		return country.getMapMaterialsForRead()[h];
	}
	
	private int makeVert(float[] vertices, int dst, int cx, int cz, LH3DLandCell cell,
						 LH3DLandCell cell0, LH3DLandCell cell1, LH3DLandCell cell2,
						 float w0, float w1, float w2,
						 boolean sel0, boolean sel1, boolean sel2) {
		//Position
		vertices[dst++] = CELL_SIZE * cx;
		vertices[dst++] = cell.getHeight();
		vertices[dst++] = -(CELL_SIZE * cz);
		//Cell attributes
		vertices[dst++] = Float.intBitsToFloat(cell0.getProperties() | (sel0 ? 0x10000 : 0));
		vertices[dst++] = Float.intBitsToFloat(cell1.getProperties() | (sel1 ? 0x10000 : 0));
		vertices[dst++] = Float.intBitsToFloat(cell2.getProperties() | (sel2 ? 0x10000 : 0));
		//Weight
		vertices[dst++] = w0;
		vertices[dst++] = w1;
		vertices[dst++] = w2;
		//Light level
		vertices[dst++] = cell.getLightLevel();
		//Altitude
		vertices[dst++] = cell.getAltitude();
		return dst;
	}
	
	public float getMapX() {
		return block.getMapX();
	}
	
	public float getMapZ() {
		return -block.getMapZ();
	}
	
	public float getElevation(Vector3 pos) {
		return getCell(pos).getHeight();
	}
	
	public float getHighestAltitude() {
		return (float)block.getHighestElevation() * LH3DLandCell.HEIGHT_UNIT;
	}
	
	public LH3DLandCell getCell(Vector3 pos) {
		int ix = getCellX(pos.x);
		int iz = getCellZ(pos.z);
		return getCellAt(ix, iz);
	}
	
	public LH3DLandCell getCellAt(int ix, int iz) {
		return block.getCell(ix, iz);
		/*if (ix < SEG_PER_SIDE && iz < SEG_PER_SIDE) {
			return block.getCell(ix, iz);
		} else {
			final int absX = block.getBlockX() * SEG_PER_SIDE + ix;
			final int absZ = block.getBlockZ() * SEG_PER_SIDE + iz;
			return land.land.getCell(absX, absZ);
		}*/
	}
	
	private int getCellX(float x) {
		int i = (int)((x - getMapX()) / BLOCK_SIZE * POINTS_PER_SIDE);
		if (i < 0 || i >= POINTS_PER_SIDE) {
			throw new IllegalArgumentException("x "+x+" out of bounds for cell ("+getMapX()+", "+getMapZ()+")");
		}
		return i;
	}
	
	private int getCellZ(float z) {
		int i = (int)((getMapZ() - z) / BLOCK_SIZE * POINTS_PER_SIDE);
		if (i < 0 || i >= POINTS_PER_SIDE) {
			throw new IllegalArgumentException("z "+z+" out of bounds for cell ("+getMapX()+", "+getMapZ()+")");
		}
		return i;
	}
	
	public boolean hasSelectedCells() {
		for (int cx = 0; cx < POINTS_PER_SIDE; cx++) {
			for (int cz = 0; cz < POINTS_PER_SIDE; cz++) {
				if (selectedCells[cx][cz] != 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void clearSelection() {
		for (int cx = 0; cx < POINTS_PER_SIDE; cx++) {
			for (int cz = 0; cz < POINTS_PER_SIDE; cz++) {
				selectedCells[cx][cz] = 0;
			}
		}
		selectionChanged();
	}
	
	void processSelectionIntersection() {
		for (int cx = 0; cx < POINTS_PER_SIDE; cx++) {
			for (int cz = 0; cz < POINTS_PER_SIDE; cz++) {
				byte v = selectedCells[cx][cz];
				if (v == 1) {
					selectedCells[cx][cz] = 0;
				}
			}
		}
		for (int cx = 0; cx < POINTS_PER_SIDE; cx++) {
			for (int cz = 0; cz < POINTS_PER_SIDE; cz++) {
				byte v = selectedCells[cx][cz];
				if (v == 2) {
					selectedCells[cx][cz] = 1;
				}
			}
		}
		selectionChanged();
	}
	
	public BulkUpdate<byte[]> updateSelection() {
		return new BulkUpdate<byte[]>(selectedCells, new BulkUpdateListener<byte[]>() {
			@Override
			public void afterUpdate(List<Change<byte[]>> changes) {
				selectionChanged();
			}
		});
	}
	
	public boolean isSelected(int cx, int cz) {
		return selectedCells[cx][cz] != 0;
	}
	
	private void selectionChanged() {
		meshReady = false;
    	listeners.notify(EventType.CHANGE, Property.SELECTION);
	}
	
	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		if (!meshReady) {
			updateMesh();
			meshReady = true;
		}
		renderables.add(renderable);
	}
	
	@Override
	public String toString() {
		return "["+block.getBlockX()+", "+block.getBlockZ()+"]";
	}

	@Override
	public void dispose() {
		block.listeners.remove(blockListener);
		renderable.meshPart.mesh.dispose();
	}
}
