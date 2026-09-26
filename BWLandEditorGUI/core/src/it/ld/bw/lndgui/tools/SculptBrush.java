package it.ld.bw.lndgui.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import it.ld.bw.lnd.model.LH3DLandBlock;
import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LH3DLandCell.Sound;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.tools.ShadowTool;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.gfx.Coord;
import it.ld.utils.MathUtils;
import it.ld.utils.UChangeListener;
import it.ld.utils.UChangeListener.EventType;

import static it.ld.bw.lnd.model.LH3DLandCell.CELL_SIZE;
import static it.ld.bw.lnd.model.LH3DLandCell.HEIGHT_UNIT;
import static it.ld.bw.lnd.model.LH3DLandCell.DEEP_WATER_ALTITUDE;
import static it.ld.bw.lnd.model.LH3DLandCell.SHALLOW_WATER_ALTITUDE;
import static it.ld.bw.lnd.model.LH3DLandCell.COASTLINE_ALTITUDE;
import static it.ld.bw.lnd.model.LH3DLandCell.WET_ALTITUDE;
import static it.ld.bw.lnd.model.LH3DLandCell.DRY_ALTITUDE;
import static it.ld.bw.lnd.model.LH3DLandCell.DEEP_WATER_HEIGHT;
import static it.ld.bw.lnd.model.LH3DLandCell.DRY_HEIGHT;

public class SculptBrush extends Brush implements AutoCloseable {
	public enum Property {MODE, ELEVATION, COUNTRY, CLIFF_COUNTRY, CLIFF_SLOPE, COASTLINE_COUNTRY, MAX_SLOPE, SOUND, LAKE, MAX_DEPTH}
	
	public enum Mode {
		ABSOLUTE, RELATIVE, FLATTEN, SMOOTH, TERRACE;

		private String text;
		
		@Override
		public String toString() {
			if (text == null) {
				text = I18n.tr("sculptOptions.mode."+this.name());
			}
			return text;
		}
	}
	
	private static final float DIAGONAL = LH3DLandCell.CELL_SIZE * 1.41f;
	
	private static final Vec2i[] offsets = new Vec2i[8];
	
	static {
		int i = 0;
		offsets[i++] = new Vec2i( 0, -1);
		offsets[i++] = new Vec2i(-1,  0);
		offsets[i++] = new Vec2i(+1,  0);
		offsets[i++] = new Vec2i( 0, +1);
		offsets[i++] = new Vec2i(-1, -1);
		offsets[i++] = new Vec2i(+1, -1);
		offsets[i++] = new Vec2i(-1, +1);
		offsets[i++] = new Vec2i(+1, +1);
	}
	
	
	private LndFile land;
	private Mode mode = Mode.RELATIVE;
	private float elevation = 30f;
	private LNDCountry country = null;
	private LNDCountry cliffCountry = null;
	private float cliffSlope = 1f;
	private LNDCountry coastlineCountry = null;
	private float maxSlope = 8f;
	private Sound sound = null;
	private boolean lake = false;
	private int maxDepth = DEEP_WATER_ALTITUDE;
	
	public void set(SculptBrush ref) {
		super.set(ref);
		this.setMode(ref.mode);
		this.setElevation(ref.elevation);
		this.setCountry(ref.country);
		this.setCliffCountry(ref.cliffCountry);
		this.setCliffSlope(ref.cliffSlope);
		this.setCoastlineCountry(ref.coastlineCountry);
		this.setMaxSlope(ref.maxSlope);
		this.setSound(ref.sound);
		this.setLake(ref.lake);
		this.setMaxDepth(ref.maxDepth);
	}
	
	public void setLand(LndFile land) {
		if (land != this.land) {
			if (this.land != null) {
				this.land.listeners.remove(landChangeListener);
				setCountry(null);
				setCliffCountry(null);
				setCoastlineCountry(null);
			}
			this.land = land;
			if (this.land != null) {
				if (!land.getCountries().isEmpty()) {
					setCountry(land.getCountries().get(0));
				}
				land.listeners.add(landChangeListener);
			}
		}
	}
	
	private UChangeListener landChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.REMOVE && event.getProperty() == LndFile.Property.COUNTRIES) {
				if (event.getOldValue() == country) {
					setCountry(null);
				} else if (event.getOldValue() == cliffCountry) {
					setCliffCountry(null);
				} else if (event.getOldValue() == coastlineCountry) {
					setCoastlineCountry(null);
				}
			}
		}
	};
	
	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		if (mode != this.mode) {
			Object oldValue = this.mode;
			this.mode = mode;
			listeners.notify(EventType.CHANGE, Property.MODE, oldValue, this.mode);
		}
	}

	public float getElevation() {
		return elevation;
	}

	public void setElevation(float elevation) {
		if (elevation != this.elevation) {
			Object oldValue = this.elevation;
			this.elevation = elevation;
			listeners.notify(EventType.CHANGE, Property.ELEVATION, oldValue, this.elevation);
		}
	}
	
	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		if (maxDepth != this.maxDepth) {
			Object oldValue = this.maxDepth;
			this.maxDepth = maxDepth;
			listeners.notify(EventType.CHANGE, Property.MAX_DEPTH, oldValue, this.maxDepth);
		}
	}
	
	public LNDCountry getCountry() {
		return country;
	}

	public void setCountry(LNDCountry country) {
		if (country != this.country) {
			Object oldValue = this.country;
			this.country = country;
			listeners.notify(EventType.CHANGE, Property.COUNTRY, oldValue, this.country);
		}
	}

	public LNDCountry getCliffCountry() {
		return cliffCountry;
	}

	public void setCliffCountry(LNDCountry cliffCountry) {
		if (cliffCountry != this.cliffCountry) {
			Object oldValue = this.cliffCountry;
			this.cliffCountry = cliffCountry;
			listeners.notify(EventType.CHANGE, Property.CLIFF_COUNTRY, oldValue, this.cliffCountry);
		}
	}
	
	public float getCliffSlope() {
		return cliffSlope;
	}

	public void setCliffSlope(float cliffSlope) {
		if (cliffSlope < 0) throw new IllegalArgumentException("Slope must be positive");
		if (cliffSlope != this.cliffSlope) {
			Object oldValue = this.cliffSlope;
			this.cliffSlope = cliffSlope;
			listeners.notify(EventType.CHANGE, Property.CLIFF_SLOPE, oldValue, this.cliffSlope);
		}
	}
	
	public LNDCountry getCoastlineCountry() {
		return coastlineCountry;
	}

	public void setCoastlineCountry(LNDCountry coastlineCountry) {
		if (coastlineCountry != this.coastlineCountry) {
			Object oldValue = this.coastlineCountry;
			this.coastlineCountry = coastlineCountry;
			listeners.notify(EventType.CHANGE, Property.COASTLINE_COUNTRY, oldValue, this.coastlineCountry);
		}
	}

	public float getMaxSlope() {
		return maxSlope;
	}

	public void setMaxSlope(float maxSlope) {
		if (maxSlope != this.maxSlope) {
			Object oldValue = this.maxSlope;
			this.maxSlope = maxSlope;
			listeners.notify(EventType.CHANGE, Property.MAX_SLOPE, oldValue, this.maxSlope);
		}
	}
	
	public Sound getSound() {
		return sound;
	}

	public void setSound(Sound sound) {
		if (sound != this.sound) {
			Object oldValue = this.sound;
			this.sound = sound;
			listeners.notify(EventType.CHANGE, Property.SOUND, oldValue, this.sound);
		}
	}
	
	public boolean isLake() {
		return lake;
	}

	public void setLake(boolean lake) {
		if (lake != this.lake) {
			Object oldValue = this.lake;
			this.lake = lake;
			listeners.notify(EventType.CHANGE, Property.LAKE, oldValue, this.lake);
		}
	}
	
	private float[][] startHeights;
	private float startPointElevation;
	
	private void getHeightSnapshot() {
		startHeights = new float[land.getCellsPerSide()][land.getCellsPerSide()];
		for (int bx = 0; bx < land.getBlocksPerSide(); bx++) {
			final int cbx = bx * LH3DLandBlock.SEG_PER_SIDE;
			for (int bz = 0; bz < land.getBlocksPerSide(); bz++) {
				final int cbz = bz * LH3DLandBlock.SEG_PER_SIDE;
				LH3DLandBlock block = land.getBlock(bx, bz);
				if (block != null) {
					for (int ix = 0; ix < LH3DLandBlock.SEG_PER_SIDE; ix++) {
						for (int iz = 0; iz < LH3DLandBlock.SEG_PER_SIDE; iz++) {
							LH3DLandCell cell = block.getCell(ix, iz);
							startHeights[cbx + ix][cbz + iz] = cell.getHeight();
						}
					}
				} else {
					for (int ix = 0; ix < LH3DLandBlock.SEG_PER_SIDE; ix++) {
						for (int iz = 0; iz < LH3DLandBlock.SEG_PER_SIDE; iz++) {
							startHeights[cbx + ix][cbz + iz] = DEEP_WATER_HEIGHT;
						}
					}
				}
			}
		}
	}
	
	private float cameraAngle = 0f;
	private Mask mask = null;
	private final Set<LH3DLandBlock> allEditedBlocks = new HashSet<>();
	private List<LH3DLandBlock> blocksEditedAtCurrentApply = null;
	
	@Override
	protected void startImpl(Coord coord) {
		getHeightSnapshot();
		if (coord != null) {
			LH3DLandCell cell = land.getCellAtCoord(coord.x, coord.z);
			startPointElevation = cell.getHeight();
		} else {
			startPointElevation = Float.NaN;
		}
		mask = null;
		allEditedBlocks.clear();
	}
	
	@Override
	protected void applyImpl(float dt, Coord coord, float cameraAngle, boolean shift) {
		if (mask == null || cameraAngle != this.cameraAngle) {
			this.cameraAngle = cameraAngle;
			mask = createMask(cameraAngle);
		}
		mask.setPosition(coord.x, coord.z);
		if (mode == Mode.FLATTEN && Float.isNaN(startPointElevation)) return;
		if (mode == Mode.TERRACE && getElevation() < 2 * HEIGHT_UNIT) return;
		//Find blocks to update
		int cl = MathUtils.clamp((int)Math.round(mask.getLeft() / CELL_SIZE) - 0, 0, land.getCellsPerSide() - 1);
		int cr = MathUtils.clamp((int)Math.round(mask.getRight() / CELL_SIZE) + 0, 0, land.getCellsPerSide() - 1);
		int cb = MathUtils.clamp((int)Math.round(mask.getBottom() / CELL_SIZE) - 0, 0, land.getCellsPerSide() - 1);
		int ct = MathUtils.clamp((int)Math.round(mask.getTop() / CELL_SIZE) + 0, 0, land.getCellsPerSide() - 1);
		int bx0 = (int)Math.floor(mask.getLeft() / LH3DLandBlock.BLOCK_SIZE) - 1;
		int bz0 = (int)Math.floor(mask.getBottom() / LH3DLandBlock.BLOCK_SIZE) - 1;
		int bx1 = (int)Math.floor(mask.getRight() / LH3DLandBlock.BLOCK_SIZE) + 1;
		int bz1 = (int)Math.floor(mask.getTop() / LH3DLandBlock.BLOCK_SIZE) + 1;
		//if (mask.getLeft() <= bx0 * LH3DLandBlock.BLOCK_SIZE + CELL_SIZE) bx0--;
		// (mask.getBottom() <= bz0 * LH3DLandBlock.BLOCK_SIZE + CELL_SIZE) bz0--;
		//if (mask.getRight() >= (bx1 + 1) * LH3DLandBlock.BLOCK_SIZE - LH3DLandCell.CELL_SIZE) bx1++;
		//if (mask.getTop() >= (bz1 + 1) * LH3DLandBlock.BLOCK_SIZE - LH3DLandCell.CELL_SIZE) bz1++;
		bx0 = MathUtils.clamp(bx0, 0, land.getBlocksPerSide() - 1);
		bz0 = MathUtils.clamp(bz0, 0, land.getBlocksPerSide() - 1);
		bx1 = MathUtils.clamp(bx1, 0, land.getBlocksPerSide() - 1);
		bz1 = MathUtils.clamp(bz1, 0, land.getBlocksPerSide() - 1);
		blocksEditedAtCurrentApply = new ArrayList<>((bx1 - bx0 + 1) * (bz1 - bz0 + 1));
		for (int bx = bx0; bx <= bx1; bx++) {
			for (int bz = bz0; bz <= bz1; bz++) {
				LH3DLandBlock block = land.getBlock(bx, bz);
				if (block != null) {
					blocksEditedAtCurrentApply.add(block);
					allEditedBlocks.add(block);
				}
			}
		}
		//Sculpt
		final float refElevation = getElevation();
		final float tFlow = getFlow() * dt;
		final float maxDeltaHeight = (mode == Mode.SMOOTH && !shift) ? Float.MAX_VALUE : maxSlope * CELL_SIZE;
		final float minElevation = maxDepth == DEEP_WATER_ALTITUDE ? DEEP_WATER_HEIGHT : (maxDepth * HEIGHT_UNIT - 3f);
		for (int cx = cl; cx <= cr; cx++) {
			final float x = cx * CELL_SIZE;
			for (int cz = cb; cz <= ct; cz++) {
				final float z = cz * CELL_SIZE;
				final float weight = mask.get(x, z);
				if (weight > 0) {
					LH3DLandCell cell = land.getCell(cx, cz);
					float target = 0;
					switch (mode) {
						case ABSOLUTE:
							if (shift) {
								target = cell.getHeight() > refElevation ? land.getMaxHeight() : DEEP_WATER_HEIGHT;
							} else {
								target = refElevation;
							}
							break;
						case RELATIVE:
							float originalElevation = startHeights[cx][cz];
							target = originalElevation + (shift ? -refElevation : refElevation);
							break;
						case FLATTEN:
							target = shift ? startHeights[cx][cz] : startPointElevation;
							break;
						case SMOOTH:
							float avg = (cell.getHeight() * 4f
									+ land.getCell(cx - 1, cz).getHeight() * 2f
									+ land.getCell(cx + 1, cz).getHeight() * 2f
									+ land.getCell(cx, cz - 1).getHeight() * 2f
									+ land.getCell(cx, cz + 1).getHeight() * 2f
									+ land.getCell(cx - 1, cz - 1).getHeight()
									+ land.getCell(cx + 1, cz - 1).getHeight()
									+ land.getCell(cx - 1, cz + 1).getHeight()
									+ land.getCell(cx + 1, cz + 1).getHeight()
									) / 16f;
							if (shift) {
								target = cell.getHeight() * 2 - avg;
							} else {
								target = avg;
							}
							break;
						case TERRACE:
							target = Math.round((startHeights[cx][cz] - DRY_HEIGHT) / refElevation) * refElevation + DRY_HEIGHT;
							break;
					}
					if (target < minElevation) target = minElevation;
					target = Math.round(target / HEIGHT_UNIT) * HEIGHT_UNIT;
					float delta = target - cell.getHeight();
					float step = Math.signum(delta) * tFlow * weight;
					if (Math.abs(step) > Math.abs(delta)) step = delta;
					if (step != 0) {
						cell = getEditableCell(cell, cx, cz);
						if (cell != LH3DLandCell.EMPTY) {
							if (step < 0 && cell.getAltitude() <= SHALLOW_WATER_ALTITUDE) {
								cell.setAltitude(DEEP_WATER_ALTITUDE);
							} else {
								float newHeight = cell.getHeight() + step;
								float hMin = land.getMaxHeight();
								float hMax = DEEP_WATER_HEIGHT;
								for (Vec2i offset : offsets) {
									LH3DLandCell tmp = land.getCell(cx + offset.x, cz + offset.z);
									float h = tmp.getHeight();
									hMin = Math.min(hMin, h);
									hMax = Math.max(hMax, h);
								}
								if (Math.abs(newHeight - hMin) <= maxDeltaHeight && Math.abs(newHeight - hMax) <= maxDeltaHeight) {
									cell.setHeight(newHeight);
								}
							}
						}
					}
				}
			}
		}
		//Create coastline
		float brushYDir = Math.signum(shift ? -refElevation : refElevation);
		if (brushYDir > 0) {
			for (int altitude = WET_ALTITUDE; altitude > maxDepth; altitude--) {
				for (int cx = cl; cx <= cr; cx++) {
					final float x = cx * CELL_SIZE;
					for (int cz = cb; cz <= ct; cz++) {
						final float z = cz * CELL_SIZE;
						final float weight = mask.get(x, z);
						if (weight > -30) {
							LH3DLandCell refCell = land.getCell(cx, cz);
							if (refCell.getAltitude() > altitude) {
								final int numOffsets = altitude == COASTLINE_ALTITUDE ? 8 : 4;
								for (int i = 0; i < numOffsets; i++) {
									Vec2i offset = offsets[i];
									LH3DLandCell cell = land.getCell(cx + offset.x, cz + offset.z);
									if (cell.getAltitude() < altitude) {
										cell = getEditableCell(cell, cx + offset.x, cz + offset.z);
										if (cell != LH3DLandCell.EMPTY) {
											cell.setAltitude(altitude);
										}
									}
								}
							}
						}
					}
				}
			}
		} else if (brushYDir < 0) {
			for (int altitude = maxDepth + 1; altitude <= WET_ALTITUDE; altitude++) {
				for (int cx = cl; cx <= cr; cx++) {
					final float x = cx * CELL_SIZE;
					for (int cz = cb; cz <= ct; cz++) {
						final float z = cz * CELL_SIZE;
						final float weight = mask.get(x, z);
						if (weight > -30) {
							LH3DLandCell refCell = land.getCell(cx, cz);
							if (refCell.getAltitude() < altitude) {
								final int numOffsets = altitude == COASTLINE_ALTITUDE ? 8 : 4;
								for (int i = 0; i < numOffsets; i++) {
									Vec2i offset = offsets[i];
									LH3DLandCell cell = land.getCell(cx + offset.x, cz + offset.z);
									if (cell.getAltitude() > altitude) {
										cell = getEditableCell(cell, cx + offset.x, cz + offset.z);
										if (cell != LH3DLandCell.EMPTY) {
											cell.setAltitude(altitude);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		//Adjust split
		for (int cx = cl; cx <= cr; cx++) {
			final float x = cx * CELL_SIZE;
			for (int cz = cb; cz <= ct; cz++) {
				final float z = cz * CELL_SIZE;
				final float weight = mask.get(x, z);
				if (weight > -30) {
					LH3DLandCell c00 = land.getCell(cx, cz);
					if (c00 != LH3DLandCell.EMPTY && c00.getHeight() != startHeights[cx][cz]) {
						LH3DLandCell c10 = land.getCell(cx + 1, cz    );
						LH3DLandCell c01 = land.getCell(cx    , cz + 1);
						LH3DLandCell c11 = land.getCell(cx + 1, cz + 1);
						int h00 = c00.getAltitude();
						int h10 = c10.getAltitude();
						int h01 = c01.getAltitude();
						int h11 = c11.getAltitude();
						int dh0 = Math.abs(h11 - h00);
						int dh1 = Math.abs(h01 - h10);
						c00.setSplit(dh0 > dh1);
					}
				}
			}
		}
		//Paint & attributes
		for (int cx = cl; cx <= cr; cx++) {
			final float x = cx * CELL_SIZE;
			for (int cz = cb; cz <= ct; cz++) {
				final float z = cz * CELL_SIZE;
				final float weight = mask.get(x, z);
				if (weight > -30) {
					LH3DLandCell cell = land.getCell(cx, cz);
					if (cell != LH3DLandCell.EMPTY) {
						LH3DLandCell c0 = land.getCell(cx - 1, cz - 1);
						LH3DLandCell c1 = land.getCell(cx    , cz - 1);
						LH3DLandCell c2 = land.getCell(cx + 1, cz - 1);
						LH3DLandCell c3 = land.getCell(cx - 1, cz    );
						LH3DLandCell c4 = land.getCell(cx + 1, cz    );
						LH3DLandCell c5 = land.getCell(cx - 1, cz + 1);
						LH3DLandCell c6 = land.getCell(cx    , cz + 1);
						LH3DLandCell c7 = land.getCell(cx + 1, cz + 1);
						
						boolean isDeepWater = cell.getAltitude() == DEEP_WATER_ALTITUDE;
						boolean isWater = cell.getAltitude() < COASTLINE_ALTITUDE;
						boolean isCoastline = cell.getAltitude() == COASTLINE_ALTITUDE;
						
						boolean isNearShallowWater = c1.getAltitude() == SHALLOW_WATER_ALTITUDE
								 || c3.getAltitude() == SHALLOW_WATER_ALTITUDE
								 || c4.getAltitude() == SHALLOW_WATER_ALTITUDE
								 || c6.getAltitude() == SHALLOW_WATER_ALTITUDE;
						
						boolean isNearWetLand = c1.getAltitude() <= WET_ALTITUDE
								 || c3.getAltitude() <= WET_ALTITUDE
								 || c4.getAltitude() <= WET_ALTITUDE
								 || c6.getAltitude() <= WET_ALTITUDE;
						
						float s0 = Math.abs(c0.getHeight() - cell.getHeight()) / DIAGONAL;
						float s1 = Math.abs(c1.getHeight() - cell.getHeight()) / CELL_SIZE;
						float s2 = Math.abs(c2.getHeight() - cell.getHeight()) / DIAGONAL;
						float s3 = Math.abs(c3.getHeight() - cell.getHeight()) / CELL_SIZE;
						float s4 = Math.abs(c4.getHeight() - cell.getHeight()) / CELL_SIZE;
						float s5 = Math.abs(c5.getHeight() - cell.getHeight()) / DIAGONAL;
						float s6 = Math.abs(c6.getHeight() - cell.getHeight()) / CELL_SIZE;
						float s7 = Math.abs(c7.getHeight() - cell.getHeight()) / DIAGONAL;
						
						boolean isCliff = s0 >= cliffSlope || s1 >= cliffSlope || s2 >= cliffSlope
								 || s3 >= cliffSlope || s4 >= cliffSlope || s5 >= cliffSlope
								 || s6 >= cliffSlope || s7 >= cliffSlope;
						
						if (!isDeepWater) {
							cell.setFullWater(false);
						}
						cell.setWater(isWater);
						cell.setCoastLine(isCoastline);
						
						if (isCliff) {
							if (cliffCountry != null && weight > 0) {
								cell.setCountry(cliffCountry.getIndex());
							}
						} else if (cell.getHeight() < DRY_ALTITUDE) {
							if (coastlineCountry != null) {
								cell.setCountry(coastlineCountry.getIndex());
							}
						} else if (country != null && weight > 0) {
							cell.setCountry(country.getIndex());
						}
						
						if (lake) {
							if (isDeepWater) {
								cell.setSound(Sound.LAKE);
							} else if (isWater || isCoastline) {
								cell.setSound(Sound.SLOW_WAVES);
							}
						} else {
							if (isDeepWater) {
								if (isNearShallowWater) {
									cell.setSound(Sound.SPLASH);
								} else {
									cell.setSound(Sound.OCEAN);
								}
							} else if (isNearWetLand) {
								cell.setSound(Sound.COAST);
							} else {
								if (sound != null && weight > 0) {
									cell.setSound(sound);
								} else if (cell.getSoundEnum().water) {
									cell.setSound(Sound.BIRDS);
								}
							}
						}
					}
				}
			}
		}
		//Notify changes
		for (LH3DLandBlock block : blocksEditedAtCurrentApply) {
			if (block.getHighestElevation() <= LH3DLandCell.DEEP_WATER_HEIGHT) {
				land.removeBlock(block);
				allEditedBlocks.remove(block);
			} else {
				block.listeners.notify(EventType.CHANGE, LH3DLandBlock.Property.CELLS, null, block.getCellsForRead());
			}
		}
	}
	
	private LH3DLandCell getEditableCell(LH3DLandCell cell, int cx, int cz) {
		if (cell != LH3DLandCell.EMPTY) return cell;
		if (cx < 0 || cx >= land.getCellsPerSide() || cz < 0 || cz >= land.getCellsPerSide()) return cell;
		if (land.getNumBlocks() >= land.getMaxBlocks()) return cell;
		int bx = Math.min(cx / LH3DLandBlock.SEG_PER_SIDE, land.getBlocksPerSide() - 1);
		int bz = Math.min(cz / LH3DLandBlock.SEG_PER_SIDE, land.getBlocksPerSide() - 1);
		LH3DLandBlock block = land.getBlock(bx, bz);
		if (block == null) {
			block = land.addBlockAt(bx, bz);
		}
		blocksEditedAtCurrentApply.add(block);
		allEditedBlocks.add(block);
		return land.getCell(cx, cz);
	}
	
	@Override
	protected void endImpl() {
		//Fit the real elevation to the discrete value that can be stored.
		Iterator<LH3DLandBlock> it = allEditedBlocks.iterator();
		while (it.hasNext()) {
			LH3DLandBlock block = it.next();
			if (!block.fitCellsHeight()) {
				it.remove();			//Block doesn't need to fit
			}
		}
		//
		for (LH3DLandBlock block : allEditedBlocks) {
			block.recomputeHighestAltitude();
			block.listeners.notify(EventType.CHANGE, LH3DLandBlock.Property.CELLS, null, block.getCellsForRead());
		}
		ShadowTool.updateShadows(land, allEditedBlocks);
		allEditedBlocks.clear();
		blocksEditedAtCurrentApply = null;
		//
		startHeights = null;
	}

	@Override
	public void close() {
		if (land != null) {
			land.listeners.remove(landChangeListener);
			land = null;
		}
	}
	
	@Override
    public void write(Json json) {
		super.write(json);
		json.writeValue("mode", this.mode.name());
		json.writeValue("elevation", this.elevation);
		json.writeValue("country", country == null ? -1 : country.getIndex());
		json.writeValue("cliffCountry", cliffCountry == null ? -1 : cliffCountry.getIndex());
		json.writeValue("cliffSlope", this.cliffSlope);
		json.writeValue("coastlineCountry", coastlineCountry == null ? -1 : coastlineCountry.getIndex());
		json.writeValue("maxSlope", this.maxSlope);
		json.writeValue("sound", sound == null ? -1 : sound.code);
		json.writeValue("lake", lake);
	}
	
	@Override
	public void read(Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		if (jsonData.has("mode")) setMode(Mode.valueOf(jsonData.getString("mode")));
		if (jsonData.has("elevation")) setElevation(jsonData.getFloat("elevation"));
		if (jsonData.has("country")) {
			int v = jsonData.getInt("country");
			setCountry(v < 0 ? null : land.getCountries().get(v));
		}
		if (jsonData.has("cliffCountry")) {
			int v = jsonData.getInt("cliffCountry");
			setCliffCountry(v < 0 ? null : land.getCountries().get(v));
		}
		if (jsonData.has("cliffSlope")) setCliffSlope(jsonData.getFloat("cliffSlope"));
		if (jsonData.has("coastlineCountry")) {
			int v = jsonData.getInt("coastlineCountry");
			setCoastlineCountry(v < 0 ? null : land.getCountries().get(v));
		}
		if (jsonData.has("maxSlope")) setMaxSlope(jsonData.getFloat("maxSlope"));
		if (jsonData.has("sound")) {
			int v = jsonData.getInt("sound");
			setSound(v < 0 ? null : Sound.valueOf(v));
		}
		if (jsonData.has("lake")) setLake(jsonData.getBoolean("lake"));
	}
	
	@Override
	public String getDescription() {
		return I18n.tr("action.editLandscape." + mode.name());
	}
	
	
	private static class Vec2i {
		final int x;
		final int z;
		
		public Vec2i(int x, int z) {
			this.x = x;
			this.z = z;
		}
	}
}
