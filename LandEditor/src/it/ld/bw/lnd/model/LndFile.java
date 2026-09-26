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

import static it.ld.bw.lnd.model.LH3DLandBlock.SEG_PER_SIDE;
import static it.ld.bw.lnd.model.LH3DLandCell.CELL_SIZE;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import it.ld.bw.lnd.model.BulkUpdate.Change;
import it.ld.utils.ControlledList;
import it.ld.utils.ControlledList.ListControllerAdapter;
import it.ld.utils.UChangeListener.EventType;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Listeners;
import it.ld.utils.MathUtils;
import it.ld.utils.Struct;
import it.ld.utils.UChangeListener;

public class LndFile extends Struct {
	public static final int MAX_COUNTRIES = 16;
	public static final int NOISEMAP_DOWNSCALE = 4;
	
	public enum Property {COUNTRIES, MATERIALS, COASTLINE_BUMPMAP, NOISE_MAP, BLOCKS, LOWRES_TEXTURES, FILE, BLOCKS_PER_SIDE, MAX_BLOCKS, ALTITUDE_BITS}
	public final Listeners listeners = new Listeners(this);
	
	private File file;
	private boolean reindexingEnabled = true;
	private final boolean mergeCellsEnabled;
	private int blocksPerSide = 32;
	private int maxBlocks = 255;
	private LH3DLandBlock[][] blocksLookup;
	private Bounds3f bounds = null;
	
	private final LH3DLandHeader header = new LH3DLandHeader();
	
	private final List<LNDLowresTexture> lowresTextures = new ControlledList<>(new ListControllerAdapter<LNDLowresTexture>() {
		@Override
		public void afterAdd(int index, LNDLowresTexture item) {
			for (int i = lowresTextures.size() - 1; i >= index; i--) {
				LNDLowresTexture tmp = lowresTextures.get(i);
				tmp.setIndex(i);
			}
			listeners.notify(EventType.ADD, Property.LOWRES_TEXTURES, null, item, index);
		}
		
		@Override
		public void afterRemove(int index, LNDLowresTexture item) {
			item.setIndex(-1);
			for (int i = Math.max(0, index); i < lowresTextures.size(); i++) {
				LNDLowresTexture tmp = lowresTextures.get(i);
				tmp.setIndex(i);
			}
			listeners.notify(EventType.REMOVE, Property.LOWRES_TEXTURES, item, null, index);
		}
	});
	
	
	private boolean bulkUpdatingBlocks = false;
	private final List<LH3DLandBlock> landBlocks = new ControlledList<>(new ListControllerAdapter<LH3DLandBlock>() {
		@Override
		public boolean beforeAdd(int index, LH3DLandBlock item) {
			//if (landBlocks.size() >= getMaxBlocks()) throw new IllegalStateException("Land cannot have more than "+getMaxBlocks()+" blocks");
			if (item.getBlockX() < 0 || item.getBlockX() >= getBlocksPerSide() || item.getBlockZ() < 0 || item.getBlockZ() >= getBlocksPerSide()) {
				throw new IllegalArgumentException("Block position out of bounds [" + item.getBlockX() + ", " + item.getBlockZ() + "]");
			}
			if (!bulkUpdatingBlocks) {
				if (blocksLookup[item.getBlockX()][item.getBlockZ()] != null) throw new IllegalArgumentException("There is already a block at [" + item.getBlockX() + ", " + item.getBlockZ() + "]");
			}
			return true;
		}
		
		@Override
		public void afterAdd(int index, LH3DLandBlock block) {
			blocksLookup[block.getBlockX()][block.getBlockZ()] = block;
			for (int i = landBlocks.size() - 1; i >= index; i--) {
				LH3DLandBlock tmpBlock = landBlocks.get(i);
				tmpBlock.setIndex(i);
				header.setIndexBlock(tmpBlock);
			}
			bounds = null;
			block.listeners.add(blockChangeListener);
			if (bulkUpdatingBlocks) {
				if (blocksBulkUpdate != null) blocksBulkUpdate.setAdded(block, index);
			} else {
				mergeOverlappingCells(block);
				listeners.notify(EventType.ADD, Property.BLOCKS, null, block, index);
			}
		}
		
		public void afterAddAll(final int index, Collection<? extends LH3DLandBlock> items) {
			int i = index;
			for (LH3DLandBlock block : items) {
				blocksLookup[block.getBlockX()][block.getBlockZ()] = block;
				block.setIndex(i++);
				header.setIndexBlock(block);
				block.listeners.add(blockChangeListener);
			}
			bounds = null;
			if (bulkUpdatingBlocks) {
				if (blocksBulkUpdate != null) {
					i = index;
					for (LH3DLandBlock block : items) {
						blocksBulkUpdate.setAdded(block, i++);
					}
				}
			} else {
				for (LH3DLandBlock block : items) {
					mergeOverlappingCells(block);
				}
				int last = index + items.size() - 1;
				i = index;
				for (LH3DLandBlock block : items) {
					listeners.notify(EventType.ADD, Property.BLOCKS, null, block, i, i == last);
				}
			}
		};
		
		@Override
		public void afterRemove(int index, LH3DLandBlock block) {
			countryUsage = null;
			block.listeners.remove(blockChangeListener);
			header.clearIndexBlock(block.getBlockX(), block.getBlockZ());
			blocksLookup[block.getBlockX()][block.getBlockZ()] = null;
			block.setIndex(-1);
			for (int i = Math.max(0, index); i < landBlocks.size(); i++) {
				LH3DLandBlock tmpBlock = landBlocks.get(i);
				tmpBlock.setIndex(i);
				header.setIndexBlock(tmpBlock);
			}
			bounds = null;
			if (bulkUpdatingBlocks) {
				if (blocksBulkUpdate != null) blocksBulkUpdate.setRemoved(block, index);
			} else {
				listeners.notify(EventType.REMOVE, Property.BLOCKS, block, null, index);
			}
		}
	});
	
	private final UChangeListener blockChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			countryUsage = null;
			bounds = null;
			if (!bulkUpdatingBlocks) {
				LH3DLandBlock block = (LH3DLandBlock) event.getSource();
				listeners.notify(EventType.CHANGE, Property.BLOCKS, null, block, block.getIndex(), event);
			}
		}
	};
	
	
	private boolean bulkUpdatingCountries = false;
	private final List<LNDCountry> countries = new ControlledList<>(new ListControllerAdapter<LNDCountry>() {
		@Override
		public boolean beforeAdd(int index, LNDCountry country) {
			if (country.getLand() != LndFile.this && country.getLand() != null) {
				throw new IllegalArgumentException("This country belongs to another land");
			}
			return true;
		}
		
		@Override
		public void afterAdd(int index, LNDCountry country) {
			if (!bulkUpdatingCountries) countryUsage = null;
			country.setLand(LndFile.this);
			for (int i = countries.size() - 1; i >= index; i--) {
				countries.get(i).setIndex(i);
			}
			if (reindexingEnabled) {
				//IMPORTANT: since the blocks overlap, we must notify the listeners after all modifications have been applied,
				// otherwise modifications to the edges belonging to other blocks not yet modified will be hidden.
				boolean[][] visitedCells = new boolean[getCellsPerSide()][getCellsPerSide()];
				try (BulkUpdate<LH3DLandBlock> blocks = getLandBlocksForUpdate()) {
					for (int i = 0; i < blocks.data.length; i++) {
						LH3DLandBlock block = blocks.data[i];
						final int basex = block.getBlockX() * LH3DLandBlock.SEG_PER_SIDE;
						final int basez = block.getBlockZ() * LH3DLandBlock.SEG_PER_SIDE;
						boolean changed = false;
						for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
							final int ax = basex + cx;
							for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
								final int az = basez + cz;
								if (!visitedCells[ax][az]) {	//Overlapping cells are visited multiple times
									LH3DLandCell cell = block.getCell(cx, cz);
									if (cell.getCountry() >= index) {
										cell.setCountry(cell.getCountry() + 1);
										changed = true;
									}
									visitedCells[ax][az] = true;
								}
							}
						}
						if (changed) blocks.setChanged(i, LH3DLandBlock.Property.CELLS);
					}
				}
			}
			country.listeners.add(countryChangeListener);
			if (bulkUpdatingCountries) {
				if (countriesBulkUpdate != null) countriesBulkUpdate.setAdded(country, index);
			} else {
				listeners.notify(EventType.ADD, Property.COUNTRIES, null, country, index);
			}
		}
		
		@Override
		public boolean beforeRemove(int index, LNDCountry country) {
			if (index >= 0 && isCountryInUse(index)) {
				throw new IllegalStateException("Country " + index + " cannot be removed because it is used by some cells");
			}
			return true;
		}
		
		@Override
		public void afterRemove(int index, LNDCountry country) {
			materialsInUse = null;
			if (!bulkUpdatingCountries) countryUsage = null;
			country.listeners.remove(countryChangeListener);
			country.setIndex(-1);
			for (int i = Math.max(0, index); i < countries.size(); i++) {
				countries.get(i).setIndex(i);
			}
			if (reindexingEnabled && index >= 0 && !countries.isEmpty()) {
				//IMPORTANT: since the blocks overlap, we must notify the listeners after all modifications have been applied,
				// otherwise modifications to the edges belonging to other blocks not yet modified will be hidden.
				boolean[][] visitedCells = new boolean[getCellsPerSide()][getCellsPerSide()];
				try (BulkUpdate<LH3DLandBlock> blocks = getLandBlocksForUpdate()) {
					for (int i = 0; i < blocks.data.length; i++) {
						LH3DLandBlock block = blocks.data[i];
						final int basex = block.getBlockX() * LH3DLandBlock.SEG_PER_SIDE;
						final int basez = block.getBlockZ() * LH3DLandBlock.SEG_PER_SIDE;
						boolean changed = false;
						for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
							final int ax = basex + cx;
							for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
								final int az = basez + cz;
								if (!visitedCells[ax][az]) {	//Overlapping cells are visited multiple times
									LH3DLandCell cell = block.getCell(cx, cz);
									if (cell.getCountry() > index) {
										cell.setCountry(cell.getCountry() - 1);
										changed = true;
									}
									visitedCells[ax][az] = true;
								}
							}
						}
						if (changed) blocks.setChanged(i, LH3DLandBlock.Property.CELLS);	//Mark for deferred notification
					}
				}
			}
			if (bulkUpdatingCountries) {
				if (countriesBulkUpdate != null) countriesBulkUpdate.setRemoved(country, index);
			} else {
				listeners.notify(EventType.REMOVE, Property.COUNTRIES, country, null, index);
			}
		}
	});
	
	private final UChangeListener countryChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LNDCountry.Property.MAP_MATERIALS) {
				materialsInUse = null;
			}
			if (!bulkUpdatingCountries) {
				LNDCountry country = (LNDCountry)event.getSource();
				listeners.notify(EventType.CHANGE, Property.COUNTRIES, null, country, country.getIndex(), event);
			}
		}
	};
	
	
	private final List<LNDMaterial> materials = new ControlledList<>(new ListControllerAdapter<LNDMaterial>() {
		@Override
		public boolean beforeAdd(int index, LNDMaterial material) {
			if (material.getLand() != LndFile.this && material.getLand() != null) {
				throw new IllegalArgumentException("This material belongs to another land");
			}
			return true;
		}
		
		@Override
		public void afterAdd(int index, LNDMaterial material) {
			materialsInUse = null;
			material.setLand(LndFile.this);
			for (int i = materials.size() - 1; i >= index; i--) {
				materials.get(i).setIndex(i);
			}
			if (reindexingEnabled) {
				try (BulkUpdate<LNDCountry> countries = getCountriesForUpdate()) {
					for (int countryIndex = 0; countryIndex < countries.data.length; countryIndex++) {
						LNDCountry country = countries.data[countryIndex];
						boolean changed = false;
						try (BulkUpdate<LNDMapMaterial> mapMaterials = country.getMapMaterialsForUpdate()) {
							for (int i = 0; i < mapMaterials.data.length; i++) {
								LNDMapMaterial mapMaterial = mapMaterials.data[i];
								if (mapMaterial.getFirstMaterialIndex() >= index) {
									mapMaterial.setFirstMaterialIndex(mapMaterial.getFirstMaterialIndex() + 1);
									changed = true;
								}
								if (mapMaterial.getSecondMaterialIndex() >= index) {
									mapMaterial.setSecondMaterialIndex(mapMaterial.getSecondMaterialIndex() + 1);
									changed = true;
								}
							}
							if (!changed) mapMaterials.noChanges();
						}
						if (changed) countries.setChanged(countryIndex, LNDCountry.Property.MAP_MATERIALS);
					}
				}
			}
			material.listeners.add(materialChangeListener);
			listeners.notify(EventType.ADD, Property.MATERIALS, null, material, index);
		}
		
		@Override
		public boolean beforeRemove(int index, LNDMaterial item) {
			if (index >= 0 && isMaterialInUse(index)) {
				throw new IllegalStateException("Material " + index + " cannot be removed because it is used by some countries");
			}
			return true;
		}
		
		@Override
		public void afterRemove(int index, LNDMaterial material) {
			materialsInUse = null;
			material.listeners.remove(materialChangeListener);
			material.setIndex(-1);
			for (int i = Math.max(0, index); i < materials.size(); i++) {
				materials.get(i).setIndex(i);
			}
			if (reindexingEnabled && index >= 0 && !materials.isEmpty()) {
				try (BulkUpdate<LNDCountry> countries = getCountriesForUpdate()) {
					for (int countryIndex = 0; countryIndex < countries.data.length; countryIndex++) {
						LNDCountry country = countries.data[countryIndex];
						boolean changed = false;
						try (BulkUpdate<LNDMapMaterial> mapMaterials = country.getMapMaterialsForUpdate()) {
							for (int i = 0; i < mapMaterials.data.length; i++) {
								LNDMapMaterial mapMaterial = mapMaterials.data[i];
								if (mapMaterial.getFirstMaterialIndex() > index) {
									mapMaterial.setFirstMaterialIndex(mapMaterial.getFirstMaterialIndex() - 1);
									changed = true;
								}
								if (mapMaterial.getSecondMaterialIndex() > index) {
									mapMaterial.setSecondMaterialIndex(mapMaterial.getSecondMaterialIndex() - 1);
									changed = true;
								}
							}
							if (!changed) mapMaterials.noChanges();
						}
						if (changed) countries.setChanged(countryIndex, LNDCountry.Property.MAP_MATERIALS);
					}
				}
			}
			boolean last = !materials.isEmpty() || (materials.isEmpty() && index == 0);
			listeners.notify(EventType.REMOVE, Property.MATERIALS, material, null, index, last);
		}
	});
	
	private final UChangeListener materialChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			LNDMaterial material = (LNDMaterial)event.getSource();
			listeners.notify(EventType.CHANGE, Property.MATERIALS, null, material, material.getIndex(), event);
		}
	};
	
	private SimpleMap coastlineBumpMap = new SimpleMap();
	private SimpleMap noiseMap = new SimpleMap();
	private LH3DLandExt extendedData = new LH3DLandExt();
	private LH3DMeta metadata = new LH3DMeta();
	
	public LndFile(boolean mergeCells) {
		this.mergeCellsEnabled = mergeCells;
		this.blocksLookup = new LH3DLandBlock[getBlocksPerSide()][getBlocksPerSide()];
	}
	
	@Override
	public LndFile clone() {
		LndFile res = new LndFile(true);
		res.set(this);
		return res;
	}
	
	public void set(LndFile ref) {
		this.reindexingEnabled = false;
		this.setBlocksPerSide(ref.blocksPerSide);
		this.header.set(ref.header);
		this.coastlineBumpMap.setPixels(ref.coastlineBumpMap.getPixels());
		this.noiseMap.setPixels(ref.noiseMap.getPixels());
		try (BulkUpdate<LH3DLandBlock> update = this.getLandBlocksForUpdate();) {
			this.landBlocks.clear();
		}
		try (BulkUpdate<LNDCountry> update = this.getCountriesForUpdate();) {
			this.countries.clear();
		}
		this.materials.clear();
		for (LNDMaterial material : ref.materials) {
			this.materials.add(material.clone());
		}
		try (BulkUpdate<LNDCountry> update = this.getCountriesForUpdate();) {
			for (LNDCountry country : ref.countries) {
				LNDCountry newCountry = country.clone();
				this.countries.add(newCountry);
			}
		}
		try (BulkUpdate<LH3DLandBlock> update = this.getLandBlocksForUpdate();) {
			for (LH3DLandBlock block : ref.landBlocks) {
				LH3DLandBlock newBlock = block.clone();
				this.landBlocks.add(newBlock);
			}
		}
		this.lowresTextures.clear();
		for (LNDLowresTexture texture : ref.lowresTextures) {
			this.lowresTextures.add(texture.clone());
		}
		this.extendedData.set(ref.extendedData);
		this.metadata.set(ref.metadata);
		mergeOverlappingCells();
		this.reindexingEnabled = true;
		this.countryUsage = null;
		this.materialsInUse = null;
	}
	
	public void read(File file) throws Exception {
		this.file = file;
		try (EndianDataInputStream str = new EndianDataInputStream(new BufferedInputStream(new FileInputStream(file)));) {
			read(str);
		} catch (Exception e) {
			throw new Exception(e.getMessage() + ", reading " + file.getName(), e);
		}
	}
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		try {
			reindexingEnabled = false;
			bulkUpdatingBlocks = true;
			str.order(ByteOrder.LITTLE_ENDIAN);
			//Header
			header.read(str);
			//Low res textures
			for (int i = 0; i < header.getLowresTextureCount(); i++) {
				LNDLowresTexture texture = new LNDLowresTexture();
				texture.read(str);
				lowresTextures.add(texture);
			}
			//Load blocks data into a buffer (must be processed after custom data has been read)
			final int blocksDataSize = (header.getBlockCount() - 1) * LH3DLandBlock.STRUCT_SIZE;
			final byte[] blocksData = new byte[blocksDataSize];
			int n = str.read(blocksData);
			if (n < blocksData.length) {
				throw new IOException("Not enough data");
			}
			//Countries
			for (int i = 0; i < header.getCountryCount(); i++) {
				LNDCountry country = new LNDCountry();
				country.read(str);
				countries.add(country);
			}
			//Materials
			for (int i = 0; i < header.getMaterialCount(); i++) {
				LNDMaterial material = new LNDMaterial();
				material.read(str);
				materials.add(material);
			}
			//Bump & noise map
			coastlineBumpMap.read(str);
			noiseMap.read(str);
			//Custom data structures
			String magic = readFixedString(str, 4, false);
			while (magic.length() == 4) {
				if (LH3DLandExt.checkMagic(magic)) {
					extendedData.read(str);
				} else if (LH3DMeta.checkMagic(magic)) {
					metadata.read(str);
				} else {
					break;
				}
				magic = readFixedString(str, 4, false);
			}
			//Process blocks data
			try (ByteArrayInputStream baos = new ByteArrayInputStream(blocksData);
					EndianDataInputStream blocksStream = new EndianDataInputStream(baos);) {
				int span = 31;
				List<LH3DLandBlock> blocks = new ArrayList<>(header.getBlockCount() - 1);
				for (int i = 1; i < header.getBlockCount(); i++) {	//i=1 is not an error, since the count is always +1
					LH3DLandBlock block = new LH3DLandBlock(extendedData.getAltitudeBits());
					block.read(blocksStream);
					blocks.add(block);
					span = Math.max(span, Math.max(block.getBlockX(), block.getBlockZ()));
				}
				setBlocksPerSide(span + 1);
				this.landBlocks.addAll(blocks);
			}
			//
			mergeOverlappingCells();
			bulkUpdatingBlocks = false;
			reindexingEnabled = true;
		} finally {
			
		}
	}
	
	public void write(File file) throws Exception {
		//fixOverlappingCells();
		try (EndianDataOutputStream str = new EndianDataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));) {
			write(str);
		} catch (Exception e) {
			throw new Exception(e.getMessage() + ", writing " + file.getName(), e);
		}
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		try {
			str.order(ByteOrder.LITTLE_ENDIAN);
			header.setMaterialCount(materials.size());
			header.setCountryCount(countries.size());
			header.setBlockCount(landBlocks.size() + 1);
			header.setLowresTextureCount(lowresTextures.size());
			header.write(str);
			for (LNDLowresTexture texture : lowresTextures) {
				texture.write(str);
			}
			for (LH3DLandBlock block : landBlocks) {
				block.write(str);
			}
			for (LNDCountry country : countries) {
				country.write(str);
			}
			for (LNDMaterial material : materials) {
				material.write(str);
			}
			coastlineBumpMap.write(str);
			noiseMap.write(str);
			extendedData.write(str);
			metadata.write(str);
		} finally {
			
		}
	}

	public LH3DLandHeader getHeader() {
		return header;
	}
	
	public List<LNDLowresTexture> getLowresTextures() {
		return lowresTextures;
	}
	
	public LH3DLandBlock addBlockAt(int bx, int bz) {
		LH3DLandBlock block = new LH3DLandBlock(bx, bz, this.getAltitudeBits());
		this.landBlocks.add(block);
		return block;
	}
	
	public LH3DLandBlock addBlockAtCoord(float x, float z) {
		int bx = (int)Math.floor(x / LH3DLandBlock.BLOCK_SIZE);
		int bz = (int)Math.floor(z / LH3DLandBlock.BLOCK_SIZE);
		return addBlockAt(bx, bz);
	}
	
	public void addBlock(LH3DLandBlock block) {
		if (block.getAltitudeBits() != this.getAltitudeBits()) {
			throw new IllegalArgumentException("Altitude bits doesn't match");
		}
		this.landBlocks.add(block);
	}
	
	public void removeBlock(LH3DLandBlock block) {
		if (block != null && block.getIndex() >= 0) {
			this.landBlocks.remove(block.getIndex());
		}
	}
	
	public void removeBlock(int bx, int bz) {
		LH3DLandBlock block = getBlock(bx, bz);
		removeBlock(block);
	}
	
	public List<LH3DLandBlock> getLandBlocksForRead() {
		return landBlocks;
	}
	
	public int getNumBlocks() {
		return landBlocks.size();
	}
	
	private BulkUpdate<LH3DLandBlock> blocksBulkUpdate;
	
	public BulkUpdate<LH3DLandBlock> getLandBlocksForUpdate() {
		if (bulkUpdatingBlocks) throw new IllegalStateException("Concurrent update");
		bulkUpdatingBlocks = true;
		blocksBulkUpdate = new BulkUpdate<LH3DLandBlock>(landBlocks.toArray(new LH3DLandBlock[0]), (changes) -> {
			bulkUpdatingBlocks = false;
			countryUsage = null;
			mergeOverlappingCells();
			if (changes != null) {
				final int lastIndex = changes.size() - 1;
				for (int i = 0; i <= lastIndex; i++) {
					Change<LH3DLandBlock> change = changes.get(i);
					if (change.type == EventType.ADD) {
						listeners.notify(EventType.ADD, Property.BLOCKS, null, change.item, change.index, i == lastIndex);
					} else if (change.type == EventType.CHANGE) {
						landBlocks.get(change.index).listeners.notify(change.type, change.property, null, change.item, change.index, i == lastIndex);
					} else if (change.type == EventType.REMOVE) {
						listeners.notify(EventType.REMOVE, Property.BLOCKS, change.item, null, change.index, i == lastIndex);
					}
				}
			}
			blocksBulkUpdate = null;
		});
		return blocksBulkUpdate;
	}
	
	public boolean isBulkUpdatingBlocks() {
		return this.bulkUpdatingBlocks;
	}
	
	public List<LNDCountry> getCountries() {
		return this.countries;
	}
	
	private BulkUpdate<LNDCountry> countriesBulkUpdate;
	
	public BulkUpdate<LNDCountry> getCountriesForUpdate() {
		if (bulkUpdatingCountries) throw new IllegalStateException("Concurrent update");
		bulkUpdatingCountries = true;
		countriesBulkUpdate = new BulkUpdate<LNDCountry>(this.countries.toArray(new LNDCountry[0]), (changes) -> {
			bulkUpdatingCountries = false;
			materialsInUse = null;
			if (changes != null) {
				final int lastIndex = changes.size() - 1;
				for (int i = 0; i <= lastIndex; i++) {
					Change<LNDCountry> change = changes.get(i);
					if (change.type == EventType.ADD) {
						listeners.notify(EventType.ADD, Property.COUNTRIES, null, change.item, change.index, i == lastIndex);
					} else if (change.type == EventType.CHANGE) {
						countries.get(change.index).listeners.notify(change.type, change.property, i == lastIndex);
					} else if (change.type == EventType.REMOVE) {
						listeners.notify(EventType.REMOVE, Property.COUNTRIES, change.item, null, change.index, i == lastIndex);
					}
				}
			}
			countriesBulkUpdate = null;
		});
		return countriesBulkUpdate;
	}
	
	public int getTotalCells() {
		return landBlocks.size() * LH3DLandBlock.CELL_COUNT;
	}
	
	private int[] countryUsage;
	
	public int getCellsPerCountry(int country) {
		if (countryUsage == null || country >= countryUsage.length) {
			computeCellsPerCountry();
		}
		return countryUsage[country];
	}
	
	private void computeCellsPerCountry() {
		List<Integer> usage = new ArrayList<Integer>(countries.size());
		for (int i = 0; i < countries.size(); i++) {
			usage.add(0);
		}
		for (LH3DLandBlock block : landBlocks) {
			for (LH3DLandCell cell : block.getCellsForRead()) {
				final int country = cell.getCountry();
				while (usage.size() <= country) {
					usage.add(0);
				}
				usage.set(country, usage.get(country) + 1);
			}
		}
		countryUsage = new int[usage.size()];
		for (int i = 0; i < countryUsage.length; i++) {
			countryUsage[i] = usage.get(i);
		}
	}
	
	public boolean isCountryInUse(int country) {
		for (LH3DLandBlock block : landBlocks) {
			for (LH3DLandCell cell : block.getCellsForRead()) {
				if (cell.getCountry() == country) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean[] materialsInUse;
	
	public boolean isMaterialInUse(int material) {
		if (materialsInUse == null || material >= materialsInUse.length) {
			computeMaterialsInUse();
		}
		return materialsInUse[material];
	}
	
	private void computeMaterialsInUse() {
		List<Boolean> usage = new ArrayList<Boolean>(materials.size());
		for (int i = 0; i < materials.size(); i++) {
			usage.add(false);
		}
		for (LNDCountry country : countries) {
			for (LNDMapMaterial mapMaterial : country.getMapMaterialsForRead()) {
				int i1 = mapMaterial.getFirstMaterialIndex();
				int i2 = mapMaterial.getSecondMaterialIndex();
				final int iMax = Math.max(i1, i2);
				while (usage.size() <= iMax) {
					usage.add(false);
				}
				usage.set(i1, true);
				usage.set(i2, true);
			}
		}
		materialsInUse = new boolean[usage.size()];
		for (int i = 0; i < materialsInUse.length; i++) {
			materialsInUse[i] = usage.get(i);
		}
	}
	
	public List<LNDMaterial> getMaterials() {
		return materials;
	}
	
	public LNDMaterial findMaterialByTexture(LNDMaterial ref, boolean preferUsed) {
		LNDMaterial found = null;
		for (LNDMaterial mat : materials) {
			if (mat.getTexelsHash() == ref.getTexelsHash() && mat.texelsEquals(ref)) {
				if (preferUsed) {
					if (isMaterialInUse(mat.getIndex())) return mat;
					found = mat;
				} else {
					return mat;
				}
			}
		}
		return found;
	}
	
	public SimpleMap getCoastlineBumpMap() {
		return coastlineBumpMap;
	}
	
	public void setCoastlineBumpMap(SimpleMap bumpMap) {
		if (this.coastlineBumpMap != bumpMap) {
			Object oldValue = this.coastlineBumpMap;
			this.coastlineBumpMap = bumpMap;
			listeners.notify(EventType.CHANGE, Property.COASTLINE_BUMPMAP, oldValue, this.coastlineBumpMap);
		}
	}
	
	public SimpleMap getNoiseMap() {
		return this.noiseMap;
	}
	
	public void setNoiseMap(SimpleMap noiseMap) {
		if (this.noiseMap != noiseMap) {
			Object oldValue = this.noiseMap;
			this.noiseMap = noiseMap;
			listeners.notify(EventType.CHANGE, Property.NOISE_MAP, oldValue, this.noiseMap);
		}
	}
	
	public LH3DLandExt getExtendedData() {
		return extendedData;
	}
	
	public LH3DMeta getMetadata() {
		return metadata;
	}
	
	private void rebuildBlocksLookup() {
		blocksLookup = new LH3DLandBlock[blocksPerSide][blocksPerSide];
		for (LH3DLandBlock block : landBlocks) {
			blocksLookup[block.getBlockX()][block.getBlockZ()] = block;
		}
	}
	
	public int getBlocksSpan() {
		int max = 31;
		for (LH3DLandBlock block : landBlocks) {
			max = Math.max(max, block.getBlockX());
			max = Math.max(max, block.getBlockZ());
		}
		return max + 1;
	}
	
	private void removeBlocksPast(int limit) {
		List<LH3DLandBlock> blocksToRemove = new LinkedList<>();
		for (LH3DLandBlock block : landBlocks) {
			if (block.getBlockX() >= limit || block.getBlockZ() >= limit) {
				blocksToRemove.add(block);
			}
		}
		if (!blocksToRemove.isEmpty()) {
			try (BulkUpdate<LH3DLandBlock> update = this.getLandBlocksForUpdate()) {
				for (LH3DLandBlock block : blocksToRemove) {
					this.removeBlock(block);
				}
			}
		}
	}
	
	public int getBlocksPerSide() {
		return blocksPerSide;
	}
	
	public void setBlocksPerSide(int blocksPerSide) {
		if (blocksPerSide < 32) throw new IllegalArgumentException("Blocks per side must be >= 32");
		if (blocksPerSide != this.blocksPerSide) {
			removeBlocksPast(blocksPerSide);
			Object oldValue = this.blocksPerSide;
			this.blocksPerSide = blocksPerSide;
			rebuildBlocksLookup();
			listeners.notify(EventType.CHANGE, Property.BLOCKS_PER_SIDE, oldValue, this.blocksPerSide);
		}
	}
	
	public float getSideLen() {
		return getBlocksPerSide() * LH3DLandBlock.BLOCK_SIZE;
	}
	
	public int getCellsPerSide() {
		return getBlocksPerSide() * LH3DLandBlock.SEG_PER_SIDE + 1;
	}
	
	public int getMaxBlocks() {
		return maxBlocks;
	}
	
	public void setMaxBlocks(int maxBlocks) {
		if (maxBlocks != this.maxBlocks) {
			Object oldValue = this.maxBlocks;
			this.maxBlocks = maxBlocks;
			listeners.notify(EventType.CHANGE, Property.MAX_BLOCKS, oldValue, this.maxBlocks);
		}
	}
	
	public void rebuildBlockIndices() {
		header.clearIndexBlock();
		for (LH3DLandBlock block : landBlocks) {
			header.setIndexBlock(block);
		}
	}
	
	private void mergeOverlappingCells() {
		if (mergeCellsEnabled) {
			for (LH3DLandBlock block : landBlocks) {
				mergeOverlappingCells(block);
			}
		}
	}
	
	private void mergeOverlappingCells(LH3DLandBlock block) {
		if (mergeCellsEnabled) {
			final int LAST = LH3DLandBlock.SEG_PER_SIDE;
			LH3DLandBlock refBlock = getBlock(block.getBlockX() + 1, block.getBlockZ());
			if (refBlock != null) {
				for (int i = 0; i < LH3DLandBlock.POINTS_PER_SIDE; i++) {
					LH3DLandCell ref = refBlock.getCell(0, i);
					block.setCell(ref, LAST, i);
				}
			}
			refBlock = getBlock(block.getBlockX(), block.getBlockZ() + 1);
			if (refBlock != null) {
				for (int i = 0; i < LH3DLandBlock.POINTS_PER_SIDE; i++) {
					LH3DLandCell ref = refBlock.getCell(i, 0);
					block.setCell(ref, i, LAST);
				}
			}
			refBlock = getBlock(block.getBlockX() + 1, block.getBlockZ() + 1);
			if (refBlock != null) {
				LH3DLandCell ref = refBlock.getCell(0, 0);
				block.setCell(ref, LAST, LAST);
			}
			//
			refBlock = getBlock(block.getBlockX() - 1, block.getBlockZ());
			if (refBlock != null) {
				for (int i = 0; i < LH3DLandBlock.POINTS_PER_SIDE; i++) {
					LH3DLandCell ref = block.getCell(0, i);
					refBlock.setCell(ref, LAST, i);
				}
			}
			refBlock = getBlock(block.getBlockX(), block.getBlockZ() - 1);
			if (refBlock != null) {
				for (int i = 0; i < LH3DLandBlock.POINTS_PER_SIDE; i++) {
					LH3DLandCell ref = block.getCell(i, 0);
					refBlock.setCell(ref, i, LAST);
				}
			}
			refBlock = getBlock(block.getBlockX() - 1, block.getBlockZ() - 1);
			if (refBlock != null) {
				LH3DLandCell ref = block.getCell(0, 0);
				refBlock.setCell(ref, LAST, LAST);
			}
		}
	}
	
	public LH3DLandBlock getBlockAtCoord(float x, float z, boolean create) {
		int bx = (int)Math.floor(x / LH3DLandBlock.BLOCK_SIZE);
		int bz = (int)Math.floor(z / LH3DLandBlock.BLOCK_SIZE);
		LH3DLandBlock block = getBlock(bx, bz);
		if (block == null && create) {
			block = addBlockAtCoord(x, z);
		}
		return block;
	}
	
	public LH3DLandBlock getBlock(int bx, int bz) {
		if (bx < 0 || bx >= getBlocksPerSide() || bz < 0 || bz >= getBlocksPerSide()) return null;
		return blocksLookup[bx][bz];
	}
	
	public LH3DLandCell getCellAtCoord(float x, float z) {
		int ix = (int)Math.round(x / LH3DLandCell.CELL_SIZE);
		int iz = (int)Math.round(z / LH3DLandCell.CELL_SIZE);
		return getCell(ix, iz);
	}
	
	public LH3DLandCell getCell(int ix, int iz) {
		if (ix < 0 || iz < 0) return LH3DLandCell.EMPTY;
		int bx = ix / SEG_PER_SIDE;
		int bz = iz / SEG_PER_SIDE;
		LH3DLandBlock block = getBlock(bx, bz);
		if (block == null) return LH3DLandCell.EMPTY;
		int cx = ix % SEG_PER_SIDE;
		int cz = iz % SEG_PER_SIDE;
		return block.getCell(cx, cz);
	}
	
	public int getAltitudeBits() {
		return this.extendedData.getAltitudeBits();
	}
	
	public void setAltitudeBits(int altitudeBits) {
		int oldValue = this.extendedData.getAltitudeBits();
		if (altitudeBits != oldValue) {
			this.extendedData.setAltitudeBits(altitudeBits);
			listeners.notify(EventType.CHANGE, Property.ALTITUDE_BITS, oldValue, altitudeBits);
			final int newMaxAltitude = LH3DLandCell.getMaxAltitude(altitudeBits);
			try (BulkUpdate<LH3DLandBlock> blocksUpdate = getLandBlocksForUpdate();) {
				for (LH3DLandBlock block : blocksUpdate.data) {
					int blockAltitude = block.getMaxAltitude();
					for (LH3DLandCell cell : block.getCellsForRead()) {
						cell.setAltitudeBits(altitudeBits);
					}
					if (blockAltitude > newMaxAltitude) {
						blocksUpdate.setChanged(block.getIndex(), LH3DLandBlock.Property.CELLS);
						this.bounds = null;
					}
				}
			}
		}
	}
	
	public int getMaxAltitude() {
		int maxAltitude = 0;
		for (LH3DLandBlock block : landBlocks) {
			maxAltitude = Math.max(maxAltitude, block.getMaxAltitude());
		}
		return maxAltitude;
	}
	
	public float getMaxHeight() {
		return LH3DLandCell.getMaxHeight(getAltitudeBits());
	}
	
	public float getHeight(final float x, final float z) {
		int x0 = (int)Math.floor(x / CELL_SIZE);
		int z0 = (int)Math.floor(z / CELL_SIZE);

		float wx = x / CELL_SIZE - x0;   // in [0,1)
		float wz = z / CELL_SIZE - z0;   // in [0,1)

		LH3DLandCell cbl = getCell(x0    , z0    );
		LH3DLandCell cbr = getCell(x0 + 1, z0    );
		LH3DLandCell ctl = getCell(x0    , z0 + 1);
		LH3DLandCell ctr = getCell(x0 + 1, z0 + 1);

		float hbl = cbl != null ? cbl.getHeight() : 0f;
		float hbr = cbr != null ? cbr.getHeight() : 0f;
		float htl = ctl != null ? ctl.getHeight() : 0f;
		float htr = ctr != null ? ctr.getHeight() : 0f;

		boolean split = (cbl != null && cbl.hasSplit());

		if (split) {
		    // diagonal is bottom-right -> top-left : (1,0) -> (0,1)  => wx + wz = 1
		    if (wx + wz < 1f) {
		        // triangle: BL(0,0), BR(1,0), TL(0,1)
		        return (1f - wx - wz) * hbl + wx * hbr + wz * htl;
		    } else {
		        // triangle: TR(1,1), BR(1,0), TL(0,1)
		        return (wx + wz - 1f) * htr + (1f - wz) * hbr + (1f - wx) * htl;
		    }
		} else {
		    // diagonal is top-right -> bottom-left : (1,1) -> (0,0)  => wz = wx
		    if (wz > wx) {
		        // triangle: BL(0,0), TL(0,1), TR(1,1)
		        // weights: BL = 1 - wz, TL = wz - wx, TR = wx
		        return (1f - wz) * hbl + (wz - wx) * htl + wx * htr;
		    } else {
		        // triangle: BL(0,0), BR(1,0), TR(1,1)
		        // weights: BL = 1 - wx, BR = wx - wz, TR = wz
		        return (1f - wx) * hbl + (wx - wz) * hbr + wz * htr;
		    }
		}
	}
	
	public Vec3f getNormal(float x, float z) {
		return getNormal(x, z, new Vec3f());
	}
	
	private final Vec3f tmpVec = new Vec3f();
	
	public Vec3f getNormal(float x, float z, Vec3f out) {
		final float eps = 1f;
		float el = getHeight(x - eps, z);
		float er = getHeight(x + eps, z);
		float eb = getHeight(x, z - eps);
		float et = getHeight(x, z + eps);
		tmpVec.set(2 * eps, er - el, 0);
		out.set(0, et - eb, 2 * eps);
		return out.crs(tmpVec).nor();
	}
	
	public Vec3f pickPos(Vec3f origin, Vec3f direction, boolean getOutGround) {
		Vec3f pos = new Vec3f();
		Vec3f end = new Vec3f();
		Bounds3f bounds = this.getBounds();
		if (bounds.intersect(origin, direction, pos, end)) {
			Vec3f step = new Vec3f(direction).scl(CELL_SIZE);
			if (getOutGround) {
				while (bounds.containsXZ(pos) && pos.y < this.getHeight(pos.x, pos.z)) {
					pos.add(step);
				}
			}
			//Find the intersection
			boolean refining = false;
			while (bounds.containsXZ(pos)) {
				if (pos.y < 0) {
					return pickBase(origin, direction);
				} else if (pos.y < this.getHeight(pos.x, pos.z)) {
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
		return pickBase(origin, direction);
	}
	
	private Vec3f pickBase(Vec3f origin, Vec3f direction) {
		direction = new Vec3f(direction);
		float distance = origin.y / -direction.y;
		if (distance <= 0) return null;
		return direction.scl(distance).add(origin);
	}
	
	public Bounds3f getBounds() {
		if (bounds == null) {
			bounds = new Bounds3f();
			for (LH3DLandBlock block : landBlocks) {
				bounds.update(block.getMapX(), 0f, block.getMapZ());
				float maxHeight = (float)block.getHighestAltitude() * LH3DLandCell.HEIGHT_UNIT;
				bounds.update(block.getMapX() + LH3DLandBlock.BLOCK_SIZE, maxHeight, block.getMapZ() - LH3DLandBlock.BLOCK_SIZE);
			}
		}
		return bounds;
	}
	
	public static LndFile load(File file, boolean mergeCells) throws Exception {
		LndFile lnd = new LndFile(mergeCells);
		lnd.read(file);
		return lnd;
	}
	
	public File getFile() {
		return this.file;
	}
	
	public void setFile(File file) {
		if (file != this.file) {
			Object oldValue = this.file;
			this.file = file;
			listeners.notify(EventType.CHANGE, Property.FILE, oldValue, this.file);
		}
	}
	
	public void updateLowResTextures() {
		int[][] materialsPixels = new int[materials.size()][];
		for (int i = 0; i < materials.size(); i++) {
			LNDMaterial material = materials.get(i);
			materialsPixels[i] = material.getIntARGB();
		}
		//
		int[] pixels = new int[LNDLowresTexture.WIDTH * LNDLowresTexture.HEIGHT];
		lowresTextures.clear();
		int subCount = 0;
		for (LH3DLandBlock block : landBlocks) {
			final int dstX0 = (subCount % LNDLowresTexture.HTEXTURES) * LNDLowresTexture.SUBW;
			final int dstY0 = (subCount / LNDLowresTexture.HTEXTURES) * LNDLowresTexture.SUBH;
			block.setLowResTexture(lowresTextures.size());
			block.setSmallTextUpdated(1);
			block.setIu_lrs(dstX0);
			block.setIv_lrs(dstY0);
			block.setFu_lrs((float)dstX0 / LNDLowresTexture.WIDTH);
			block.setFv_lrs((float)dstY0 / LNDLowresTexture.HEIGHT);
			
			for (int cx = 0; cx < LH3DLandBlock.SEG_PER_SIDE; cx++) {
				int bx = block.getBlockX() * LH3DLandBlock.SEG_PER_SIDE;
				for (int cz = 0; cz < LH3DLandBlock.SEG_PER_SIDE; cz++) {
					int bz = block.getBlockZ() * LH3DLandBlock.SEG_PER_SIDE;
					LH3DLandCell cell00 = getCell(bx + cx    , bz + cz    );
					LH3DLandCell cell10 = getCell(bx + cx + 1, bz + cz    );
					LH3DLandCell cell01 = getCell(bx + cx    , bz + cz + 1);
					LH3DLandCell cell11 = getCell(bx + cx + 1, bz + cz + 1);
					
					LNDCountry country00 = countries.get(cell00.getCountry());
					LNDCountry country10 = countries.get(cell10.getCountry());
					LNDCountry country01 = countries.get(cell01.getCountry());
					LNDCountry country11 = countries.get(cell11.getCountry());
					
					LNDMapMaterial mapMaterial00 = country00.getMapMaterialsForRead()[Math.min(cell00.getAltitude(), 255)];
					LNDMapMaterial mapMaterial10 = country10.getMapMaterialsForRead()[Math.min(cell10.getAltitude(), 255)];
					LNDMapMaterial mapMaterial01 = country01.getMapMaterialsForRead()[Math.min(cell01.getAltitude(), 255)];
					LNDMapMaterial mapMaterial11 = country11.getMapMaterialsForRead()[Math.min(cell11.getAltitude(), 255)];
					
					int[] material001 = materialsPixels[mapMaterial00.getFirstMaterialIndex()];
					int[] material002 = materialsPixels[mapMaterial00.getSecondMaterialIndex()];
					int[] material101 = materialsPixels[mapMaterial10.getFirstMaterialIndex()];
					int[] material102 = materialsPixels[mapMaterial10.getSecondMaterialIndex()];
					int[] material011 = materialsPixels[mapMaterial01.getFirstMaterialIndex()];
					int[] material012 = materialsPixels[mapMaterial01.getSecondMaterialIndex()];
					int[] material111 = materialsPixels[mapMaterial11.getFirstMaterialIndex()];
					int[] material112 = materialsPixels[mapMaterial11.getSecondMaterialIndex()];
					
					float bw002 = mapMaterial00.getBlend();
					float bw001 = 1f - bw002;
					float bw102 = mapMaterial10.getBlend();
					float bw101 = 1f - bw102;
					float bw012 = mapMaterial01.getBlend();
					float bw011 = 1f - bw012;
					float bw112 = mapMaterial11.getBlend();
					float bw111 = 1f - bw112;
					
					for (int lx = 0; lx < 4; lx++) {
						float wr = (float)lx / 4f;
						float wl = 1f - wr;
						int dstX = cx * 4 + lx;
						float dstU = (float)dstX / (LNDLowresTexture.SUBW - 1);
						float srcU = (float)(((double)block.getMapX() / LH3DLandBlock.BLOCK_SIZE + dstU) % 1.0);
						int srcX = (int)(srcU * (LNDMaterial.width - 1));
						for (int ly = 0; ly < 4; ly++) {
							float wt = (float)ly / 4f;
							float wb = 1f - wt;
							
							float cw00 = wl * wb;
							float cw10 = wr * wb;
							float cw01 = wl * wt;
							float cw11 = wr * wt;
							
							int dstY = cz * 4 + ly;
							float dstV = (float)dstY / (LNDLowresTexture.SUBH - 1);
							float srcV = (float)(((double)block.getMapZ() / LH3DLandBlock.BLOCK_SIZE + dstV) % 1.0);
							int srcY = (int)(srcV * (LNDMaterial.height - 1));
							int src = srcX + srcY * LNDMaterial.width;
							
							int col001 = material001[src];
							int col002 = material002[src];
							int col101 = material101[src];
							int col102 = material102[src];
							int col011 = material011[src];
							int col012 = material012[src];
							int col111 = material111[src];
							int col112 = material112[src];
							
							float r001 =  col001 & 0xFF;
							float g001 = (col001 >> 8) & 0xFF;
							float b001 = (col001 >> 16) & 0xFF;
							float r002 =  col002 & 0xFF;
							float g002 = (col002 >> 8) & 0xFF;
							float b002 = (col002 >> 16) & 0xFF;
							
							float r101 =  col101 & 0xFF;
							float g101 = (col101 >> 8) & 0xFF;
							float b101 = (col101 >> 16) & 0xFF;
							float r102 =  col102 & 0xFF;
							float g102 = (col102 >> 8) & 0xFF;
							float b102 = (col102 >> 16) & 0xFF;
							
							float r011 =  col011 & 0xFF;
							float g011 = (col011 >> 8) & 0xFF;
							float b011 = (col011 >> 16) & 0xFF;
							float r012 =  col012 & 0xFF;
							float g012 = (col012 >> 8) & 0xFF;
							float b012 = (col012 >> 16) & 0xFF;
							
							float r111 =  col111 & 0xFF;
							float g111 = (col111 >> 8) & 0xFF;
							float b111 = (col111 >> 16) & 0xFF;
							float r112 =  col112 & 0xFF;
							float g112 = (col112 >> 8) & 0xFF;
							float b112 = (col112 >> 16) & 0xFF;
							
							float r00 = r001 * bw001 + r002 * bw002;
							float g00 = g001 * bw001 + g002 * bw002;
							float b00 = b001 * bw001 + b002 * bw002;
							float a00 = cell00.getAlpha() * 255;
							
							float r10 = r101 * bw101 + r102 * bw102;
							float g10 = g101 * bw101 + g102 * bw102;
							float b10 = b101 * bw101 + b102 * bw102;
							float a10 = cell10.getAlpha() * 255;
							
							float r01 = r011 * bw011 + r012 * bw012;
							float g01 = g011 * bw011 + g012 * bw012;
							float b01 = b011 * bw011 + b012 * bw012;
							float a01 = cell01.getAlpha() * 255;
							
							float r11 = r111 * bw111 + r112 * bw112;
							float g11 = g111 * bw111 + g112 * bw112;
							float b11 = b111 * bw111 + b112 * bw112;
							float a11 = cell11.getAlpha() * 255;
							
							int r = MathUtils.clamp((int)(r00 * cw00 + r10 * cw10 + r01 * cw01 + r11 * cw11), 0, 255);
							int g = MathUtils.clamp((int)(g00 * cw00 + g10 * cw10 + g01 * cw01 + g11 * cw11), 0, 255);
							int b = MathUtils.clamp((int)(b00 * cw00 + b10 * cw10 + b01 * cw01 + b11 * cw11), 0, 255);
							int a = MathUtils.clamp((int)(a00 * cw00 + a10 * cw10 + a01 * cw01 + a11 * cw11), 0, 255);
							
							int dst = (dstX0 + dstY) + (dstY0 + dstX) * LNDLowresTexture.WIDTH;	//IMPORTANT: subtexture X and Y are flipped!
							pixels[dst] = a << 24 | b << 16 | g << 8 | r;
						}
					}
				}
			}
			
			subCount++;
			if (subCount == LNDLowresTexture.COUNT) {
				LNDLowresTexture texture = new LNDLowresTexture();
				texture.getDdsTexture().setPixels(LNDLowresTexture.WIDTH, LNDLowresTexture.HEIGHT, pixels);
				texture.setCount(subCount);
				lowresTextures.add(texture);
				subCount = 0;
				pixels = new int[LNDLowresTexture.WIDTH * LNDLowresTexture.HEIGHT];
			}
		}
		if (subCount > 0) {
			LNDLowresTexture texture = new LNDLowresTexture();
			texture.getDdsTexture().setPixels(LNDLowresTexture.WIDTH, LNDLowresTexture.HEIGHT, pixels);
			texture.setCount(subCount);
			lowresTextures.add(texture);
		}
	}
	
	@Override
	public String toString() {
		if (file != null) return file.toString();
		return super.toString();
	}
}
