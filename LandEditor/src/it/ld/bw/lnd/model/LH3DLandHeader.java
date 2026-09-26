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

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class LH3DLandHeader extends Struct {
	private int blockCount;			//The number of Block structs.
	private final byte[] indexBlock = new byte[1024];
	private int	materialCount;		//The number of Material structs.
	private int	countryCount;		//The number of Country structs.
	private int	blockSize = LH3DLandBlock.STRUCT_SIZE;	//The size of the Block struct.
	private int	materialSize = LNDMaterial.STRUCT_SIZE;	//The size of the Material struct.
	private int	countrySize = LNDCountry.STRUCT_SIZE;	//The size of the Country struct.
	private int	lowresTextureCount;	//The number of low resolution textures.
	
	public void set(LH3DLandHeader ref) {
		this.blockCount = ref.blockCount;
		System.arraycopy(ref.indexBlock, 0, this.indexBlock, 0, indexBlock.length);
		this.materialCount = ref.materialCount;
		this.countryCount = ref.countryCount;
		this.lowresTextureCount = ref.lowresTextureCount;
	}
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			blockCount = str.readInt();
			int n = str.read(indexBlock);
			if (n != indexBlock.length) throw new Exception("Not enough index blocks");
			materialCount = str.readInt();
			countryCount = str.readInt();
			blockSize = str.readInt();
			materialSize = str.readInt();
			countrySize = str.readInt();
			lowresTextureCount = str.readInt();
		} finally {
			
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			str.writeInt(blockCount);
			str.write(indexBlock);
			str.writeInt(materialCount);
			str.writeInt(countryCount);
			str.writeInt(blockSize);
			str.writeInt(materialSize);
			str.writeInt(countrySize);
			str.writeInt(lowresTextureCount);
		} finally {
			
		}
	}
	
	public int getBlockCount() {
		return blockCount;
	}

	public void setBlockCount(int blockCount) {
		this.blockCount = blockCount;
	}

	public byte[] getIndexBlock() {
		return indexBlock;
	}
	
	public void setIndexBlock(LH3DLandBlock block) {
		final int bx = block.getBlockX();
		final int bz = block.getBlockZ();
		final int index = block.getIndex();
		if (bx >= 0 && bx < 32 && bz >= 0 && bz < 32 && index < 255) {
			int p = bx * 32 + bz;
			indexBlock[p] = (byte)(index + 1);
		}
	}
	
	public void clearIndexBlock(int bx, int bz) {
		if (bx >= 0 && bx < 32 && bz >= 0 && bz < 32) {
			int p = bx * 32 + bz;
			indexBlock[p] = 0;
		}
	}
	
	public void clearIndexBlock() {
		for (int i = 0; i < indexBlock.length; i++) {
			indexBlock[i] = 0;
		}
	}
	
	public int getMaterialCount() {
		return materialCount;
	}

	public void setMaterialCount(int materialCount) {
		this.materialCount = materialCount;
	}

	public int getCountryCount() {
		return countryCount;
	}

	public void setCountryCount(int countryCount) {
		this.countryCount = countryCount;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public int getMaterialSize() {
		return materialSize;
	}

	public void setMaterialSize(int materialSize) {
		this.materialSize = materialSize;
	}

	public int getCountrySize() {
		return countrySize;
	}

	public void setCountrySize(int countrySize) {
		this.countrySize = countrySize;
	}

	public int getLowresTextureCount() {
		return lowresTextureCount;
	}

	public void setLowresTextureCount(int lowresTextureCount) {
		this.lowresTextureCount = lowresTextureCount;
	}

}
