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
package it.ld.bw.lnd.model;

import java.nio.ByteOrder;
import java.util.Iterator;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Listeners;
import it.ld.utils.Struct;
import it.ld.utils.UChangeListener.EventType;

public class LH3DLandBlock extends Struct {
	public enum Property {CELLS, BLOCK_X, BLOCK_Z, MAP_X, MAP_Z}
	public final Listeners listeners = new Listeners(this);
	
	public static final int STRUCT_SIZE = 2520;
	
	public static final int POINTS_PER_SIDE = 17;
	public static final int SEG_PER_SIDE = POINTS_PER_SIDE - 1;
	public static final float BLOCK_SIZE = LH3DLandCell.CELL_SIZE * SEG_PER_SIDE;
	public static final int CELL_COUNT = POINTS_PER_SIDE * POINTS_PER_SIDE;
	
	private final LH3DLandCell[] cells = new LH3DLandCell[CELL_COUNT]; // 17*17
	private int index;
	private float mapX;
	private float mapZ;
	private int blockX;
	private int blockZ;
	private int clipped; // Runtime (0/1)
	private int frameVisibility = 2; // Runtime
	private int highestAltitude; // Runtime
	private int useSmallBump = 1; // Runtime
	private int forceLowResTex; // Runtime
	private int meshLOD;// Runtime
	private int meshBlending = 4; // Runtime
	private int textureBlend; // Runtime
	private int meshLODType; // Runtime
	private int fog; // Runtime
	private int texPointer; // Runtime
	private int matPointer; // Runtime
	private int drawSomething; // Runtime
	private int specMatBeforePtr; // Runtime
	private int specMatAfterPtr; // Runtime
	private float[] transformUVBefore = new float[3 * 4];	//[3][4]
	private float[] transformUVAfter = new float[3 * 4];		//[3][4]
	private int nextSortingPtr;
	private float valueSorting;
	private int lowResTexture;
	private float fu_lrs; // (iu_lrs / 256)
	private float fv_lrs; // (iv_lrs / 256)
	private int iu_lrs; // lowrestex x (0, 64, 128, 192)
	private int iv_lrs; // lowrestex y (0, 64, 128, 192)
	private int smallTextUpdated;	// (0, 1)
	
	public LH3DLandBlock(int altitudeBits) {
		this(0, 0, altitudeBits);
	}
	
	public LH3DLandBlock(int bx, int bz, int altitudeBits) {
		setBlockX(bx);
		setBlockZ(bz);
		setMapX(bx * LH3DLandBlock.BLOCK_SIZE);
		setMapZ(bz * LH3DLandBlock.BLOCK_SIZE);
		for (int i = 0; i < cells.length; i++) {
			cells[i] = new LH3DLandCell(altitudeBits);
		}
	}
	
	@Override
	public LH3DLandBlock clone() {
		LH3DLandBlock res = new LH3DLandBlock(this.getAltitudeBits());
		res.set(this);
		return res;
	}
	
	public void set(LH3DLandBlock ref) {
		this.mapX = ref.mapX;
		this.mapZ = ref.mapZ;
		this.blockX = ref.blockX;
		this.blockZ = ref.blockZ;
		this.highestAltitude = ref.highestAltitude;
		this.fu_lrs = ref.fu_lrs;
		this.fv_lrs = ref.fv_lrs;
		this.iu_lrs = ref.iu_lrs;
		this.iv_lrs = ref.iv_lrs;
		this.smallTextUpdated = ref.smallTextUpdated;
		for (int i = 0; i < cells.length; i++) {
			this.cells[i].set(ref.cells[i]);
		}
		this.listeners.notify(EventType.CHANGE, Property.CELLS, null, this.cells);
	}
	
	public int getAltitudeBits() {
		return cells[0].getAltitudeBits();
	}
	
	public int getMaxAltitude() {
		int maxAltitude = 0;
		for (LH3DLandCell cell : cells) {
			maxAltitude = Math.max(maxAltitude, cell.getAltitude());
		}
		return maxAltitude;
	}
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 0; i < cells.length; i++) {
				cells[i].read(str);
			}
			index = str.readInt() - 1;
			mapX = str.readFloat();
			mapZ = str.readFloat();
			blockX = str.readInt();
			blockZ = str.readInt();
			clipped = str.readInt();
			frameVisibility = str.readInt();
			highestAltitude = str.readInt();
			useSmallBump = str.readInt();
			forceLowResTex = str.readInt();
			meshLOD = str.readInt();
			meshBlending = str.readInt();
			textureBlend = str.readInt();
			meshLODType = str.readInt();
			fog = str.readInt();
			texPointer = str.readInt();
			matPointer = str.readInt();
			drawSomething = str.readInt();
			specMatBeforePtr = str.readInt();
			specMatAfterPtr = str.readInt();
			for (int i = 0; i < transformUVBefore.length; i++) {
				transformUVBefore[i] = str.readFloat();
			}
			for (int i = 0; i < transformUVAfter.length; i++) {
				transformUVAfter[i] = str.readFloat();
			}
			nextSortingPtr = str.readInt();
			valueSorting = str.readFloat();
			lowResTexture = str.readInt();
			fu_lrs = str.readFloat();
			fv_lrs = str.readFloat();
			iu_lrs = str.readInt();
			iv_lrs = str.readInt();
			smallTextUpdated = str.readInt();
			int cBlockX = (int) (mapX / BLOCK_SIZE);
			int cBlockZ = (int) (mapZ / BLOCK_SIZE);
			if (cBlockX != blockX || cBlockZ != blockZ) {
				float cMapX = BLOCK_SIZE * blockX;
				float cMapZ = BLOCK_SIZE * blockZ;
				System.err.println("Coordinate mismatch for block "+index+": ("+blockX+", "+blockZ+") should fall in ("+cMapX+", "+cMapZ+"), but ("+mapX+", "+mapZ+") was found instead");
				mapX = cMapX;
				mapZ = cMapZ;
			}
		} finally {
			
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			for (LH3DLandCell cell : cells) {
				cell.write(str);
			}
			str.writeInt(index + 1);
			str.writeFloat(mapX);
			str.writeFloat(mapZ);
			str.writeInt(blockX);
			str.writeInt(blockZ);
			str.writeInt(clipped);
			str.writeInt(frameVisibility);
			str.writeInt(highestAltitude);
			str.writeInt(useSmallBump);
			str.writeInt(forceLowResTex);
			str.writeInt(meshLOD);
			str.writeInt(meshBlending);
			str.writeInt(textureBlend);
			str.writeInt(meshLODType);
			str.writeInt(fog);
			str.writeInt(texPointer);
			str.writeInt(matPointer);
			str.writeInt(drawSomething);
			str.writeInt(specMatBeforePtr);
			str.writeInt(specMatAfterPtr);
			for (int i = 0; i < transformUVBefore.length; i++) {
				str.writeFloat(transformUVBefore[i]);
			}
			for (int i = 0; i < transformUVAfter.length; i++) {
				str.writeFloat(transformUVAfter[i]);
			}
			str.writeInt(nextSortingPtr);
			str.writeFloat(valueSorting);
			str.writeInt(lowResTexture);
			str.writeFloat(fu_lrs);
			str.writeFloat(fv_lrs);
			str.writeInt(iu_lrs);
			str.writeInt(iv_lrs);
			str.writeInt(smallTextUpdated);
		} finally {
			
		}
	}
	
	public LH3DLandCell getCell(int cx, int cz) {
		return cells[cx * POINTS_PER_SIDE + cz];
	}
	
	void setCell(LH3DLandCell cell, int cx, int cz) {
		cells[cx * POINTS_PER_SIDE + cz] = cell;
	}
	
	public LH3DLandCell[] getCellsForRead() {
		return this.cells;
	}
	
	public BulkUpdate<LH3DLandCell> getCellsForUpdate() {
		return new BulkUpdate<>(cells, (changes) -> {
			if (changes != null) {
				//For cells we generate a single event, since having one event per cell would generate too much overhead
				listeners.notify(EventType.CHANGE, Property.CELLS, null, this.cells);
			}
		});
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public float getMapX() {
		return mapX;
	}
	
	public void setMapX(float mapX) {
		if (mapX != this.mapX) {
			Object oldValue = this.mapX;
			this.mapX = mapX;
			listeners.notify(EventType.CHANGE, Property.MAP_X, oldValue, this.mapX);
		}
	}
	
	public float getMapZ() {
		return mapZ;
	}

	public void setMapZ(float mapZ) {
		if (mapZ != this.mapZ) {
			Object oldValue = this.mapZ;
			this.mapZ = mapZ;
			listeners.notify(EventType.CHANGE, Property.MAP_Z, oldValue, this.mapZ);
		}
	}
	
	public int getBlockX() {
		return blockX;
	}
	
	public void setBlockX(int blockX) {
		if (blockX != this.blockX) {
			Object oldValue = this.blockX;
			this.blockX = blockX;
			listeners.notify(EventType.CHANGE, Property.BLOCK_X, oldValue, this.blockX);
		}
	}
	
	public int getBlockZ() {
		return blockZ;
	}
	
	public void setBlockZ(int blockZ) {
		if (blockZ != this.blockZ) {
			Object oldValue = this.blockZ;
			this.blockZ = blockZ;
			listeners.notify(EventType.CHANGE, Property.BLOCK_Z, oldValue, this.blockZ);
		}
	}
	
	public int getClipped() {
		return clipped;
	}

	public void setClipped(int clipped) {
		this.clipped = clipped;
	}

	public int getFrameVisibility() {
		return frameVisibility;
	}

	public void setFrameVisibility(int frameVisibility) {
		this.frameVisibility = frameVisibility;
	}

	public int getHighestAltitude() {
		return highestAltitude;
	}

	public void setHighestAltitude(int highestAltitude) {
		this.highestAltitude = highestAltitude;
	}

	public int getUseSmallBump() {
		return useSmallBump;
	}

	public void setUseSmallBump(int useSmallBump) {
		this.useSmallBump = useSmallBump;
	}

	public int getForceLowResTex() {
		return forceLowResTex;
	}

	public void setForceLowResTex(int forceLowResTex) {
		this.forceLowResTex = forceLowResTex;
	}

	public int getMeshLOD() {
		return meshLOD;
	}

	public void setMeshLOD(int meshLOD) {
		this.meshLOD = meshLOD;
	}

	public int getMeshBlending() {
		return meshBlending;
	}

	public void setMeshBlending(int meshBlending) {
		this.meshBlending = meshBlending;
	}

	public int getTextureBlend() {
		return textureBlend;
	}

	public void setTextureBlend(int textureBlend) {
		this.textureBlend = textureBlend;
	}

	public int getMeshLODType() {
		return meshLODType;
	}

	public void setMeshLODType(int meshLODType) {
		this.meshLODType = meshLODType;
	}

	public int getFog() {
		return fog;
	}

	public void setFog(int fog) {
		this.fog = fog;
	}

	public int getTexPointer() {
		return texPointer;
	}

	public void setTexPointer(int texPointer) {
		this.texPointer = texPointer;
	}

	public int getMatPointer() {
		return matPointer;
	}

	public void setMatPointer(int matPointer) {
		this.matPointer = matPointer;
	}

	public int getDrawSomething() {
		return drawSomething;
	}

	public void setDrawSomething(int drawSomething) {
		this.drawSomething = drawSomething;
	}

	public int getSpecMatBeforePtr() {
		return specMatBeforePtr;
	}

	public void setSpecMatBeforePtr(int specMatBeforePtr) {
		this.specMatBeforePtr = specMatBeforePtr;
	}

	public int getSpecMatAfterPtr() {
		return specMatAfterPtr;
	}

	public void setSpecMatAfterPtr(int specMatAfterPtr) {
		this.specMatAfterPtr = specMatAfterPtr;
	}

	public float[] getTransformUVBefore() {
		return transformUVBefore;
	}

	public void setTransformUVBefore(float[] transformUVBefore) {
		this.transformUVBefore = transformUVBefore;
	}

	public float[] getTransformUVAfter() {
		return transformUVAfter;
	}

	public void setTransformUVAfter(float[] transformUVAfter) {
		this.transformUVAfter = transformUVAfter;
	}

	public int getNextSortingPtr() {
		return nextSortingPtr;
	}

	public void setNextSortingPtr(int nextSortingPtr) {
		this.nextSortingPtr = nextSortingPtr;
	}

	public float getValueSorting() {
		return valueSorting;
	}

	public void setValueSorting(float valueSorting) {
		this.valueSorting = valueSorting;
	}

	public int getLowResTexture() {
		return lowResTexture;
	}

	public void setLowResTexture(int lowResTexture) {
		this.lowResTexture = lowResTexture;
	}

	public float getFu_lrs() {
		return fu_lrs;
	}

	public void setFu_lrs(float fu_lrs) {
		this.fu_lrs = fu_lrs;
	}

	public float getFv_lrs() {
		return fv_lrs;
	}

	public void setFv_lrs(float fv_lrs) {
		this.fv_lrs = fv_lrs;
	}

	public int getIu_lrs() {
		return iu_lrs;
	}

	public void setIu_lrs(int iu_lrs) {
		this.iu_lrs = iu_lrs;
	}

	public int getIv_lrs() {
		return iv_lrs;
	}

	public void setIv_lrs(int iv_lrs) {
		this.iv_lrs = iv_lrs;
	}

	public int getSmallTextUpdated() {
		return smallTextUpdated;
	}

	public void setSmallTextUpdated(int smallTextUpdated) {
		this.smallTextUpdated = smallTextUpdated;
	}
	
	/**Fit the real elevation to the discrete value that can be stored.
	 * @return
	 */
	public boolean fitCellsHeight() {
		boolean r = false;
		for (LH3DLandCell cell : cells) {
			r |= cell.fitHeight();
		}
		return r;
	}
	
	public void recomputeHighestAltitude() {
		int max = 0;
		for (LH3DLandCell cell : getEditableCells()) {
			if (cell.getAltitude() > max) {
				max = cell.getAltitude();
			}
		}
		this.highestAltitude = max;
	}
	
	public float getHighestElevation() {
		float max = Float.NEGATIVE_INFINITY;
		for (LH3DLandCell cell : getEditableCells()) {
			if (cell.getHeight() > max) {
				max = cell.getHeight();
			}
		}
		return max;
	}
	
	@Override
	public String toString() {
		return "["+getBlockX()+", "+getBlockZ()+"]";
	}
	
	/**Returns an Iterable light view of the effective cells (that is, excluding last column and last row).
	 * @return
	 */
	public Iterable<LH3DLandCell> getEditableCells() {
		return new EditableCells(cells);
	}
	
	
	private static class EditableCells implements Iterable<LH3DLandCell> {
		private final LH3DLandCell[] cells;
		
		public EditableCells(LH3DLandCell[] cells) {
			this.cells = cells;
		}
		
		@Override
		public Iterator<LH3DLandCell> iterator() {
			return new EditableCellsIterator(cells);
		}
	}
	
	
	/**An iterator which scans just effective cells (that is, excluding last column and last row).
	 * Also provides convenient methods to retrieve the cell index and position.
	 */
	public static class EditableCellsIterator implements Iterator<LH3DLandCell> {
		private final LH3DLandCell[] cells;
		
		private int index = -1;
		private int nextIndex = 0;
		
		public EditableCellsIterator(LH3DLandCell[] cells) {
			this.cells = cells;
		}
		
		@Override
		public boolean hasNext() {
			return nextIndex < CELL_COUNT - POINTS_PER_SIDE - 1;	//Skip last row and last column
		}
		
		@Override
		public LH3DLandCell next() {
			index = nextIndex;
			LH3DLandCell cell = cells[index];
			nextIndex++;
			if (nextIndex % POINTS_PER_SIDE == SEG_PER_SIDE) nextIndex++;	//Skip last column
			return cell;
		}
		
		public int getIndex() {
			return index;
		}
		
		public int getX() {
			return index % POINTS_PER_SIDE;
		}
		
		public int getZ() {
			return index / POINTS_PER_SIDE;
		}
	}
}
