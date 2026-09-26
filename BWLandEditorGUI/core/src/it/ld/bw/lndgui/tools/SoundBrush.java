package it.ld.bw.lndgui.tools;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import it.ld.bw.lnd.model.LH3DLandBlock;
import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.Vec3f;
import it.ld.bw.lnd.model.LH3DLandCell.Sound;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.gfx.Coord;
import it.ld.utils.MathUtils;
import it.ld.utils.UChangeListener.EventType;

import static it.ld.bw.lnd.model.LH3DLandCell.CELL_SIZE;

public class SoundBrush extends Brush implements AutoCloseable {
	public enum Property {SOUND, OVER_LAND, OVER_COASTLINE, NEAR_COASTLINE, OVER_WATER, MIN_SLOPE, MAX_SLOPE, MIN_HEIGHT, MAX_HEIGHT}
	
	private LndFile land;
	private Sound sound = Sound.COAST;
	private boolean overWater = false;
	private boolean overCoastline = false;
	private boolean nearCoastline = false;
	private boolean overLand = true;
	private float minSlope = 0f;
	private float maxSlope = 89;
	private float minHeight = LH3DLandCell.DEEP_WATER_HEIGHT;
	private float maxHeight = LH3DLandCell.getMaxHeight(8);
	
	public void set(SoundBrush ref) {
		super.set(ref);
		this.setSound(ref.sound);
		this.setOverWater(ref.overWater);
		this.setOverCoastline(ref.overCoastline);
		this.setNearCoastline(ref.nearCoastline);
		this.setOverLand(ref.overLand);
		this.setMinSlope(ref.minSlope);
		this.setMaxSlope(ref.maxSlope);
		this.setMinHeight(ref.minHeight);
		this.setMaxHeight(ref.maxHeight);
	}
	
	public void setLand(LndFile land) {
		if (land != this.land) {
			this.land = land;
		}
	}
	
	public boolean isOverCoastline() {
		return overCoastline;
	}
	
	public void setOverCoastline(boolean v) {
		if (v != this.overCoastline) {
			Object oldValue = this.overCoastline;
			this.overCoastline = v;
			listeners.notify(EventType.CHANGE, Property.OVER_COASTLINE, oldValue, this.overCoastline);
		}
	}
	
	public boolean isNearCoastline() {
		return nearCoastline;
	}
	
	public void setNearCoastline(boolean v) {
		if (v != this.nearCoastline) {
			Object oldValue = this.nearCoastline;
			this.nearCoastline = v;
			listeners.notify(EventType.CHANGE, Property.NEAR_COASTLINE, oldValue, this.nearCoastline);
		}
	}
	
	public boolean isOverLand() {
		return overLand;
	}
	
	public void setOverLand(boolean v) {
		if (v != this.overLand) {
			Object oldValue = this.overLand;
			this.overLand = v;
			listeners.notify(EventType.CHANGE, Property.OVER_LAND, oldValue, this.overLand);
		}
	}
	
	public boolean isOverWater() {
		return overWater;
	}
	
	public void setOverWater(boolean v) {
		if (v != this.overWater) {
			Object oldValue = this.overWater;
			this.overWater = v;
			listeners.notify(EventType.CHANGE, Property.OVER_WATER, oldValue, this.overWater);
		}
	}
	
	public float getMinSlope() {
		return minSlope;
	}

	public void setMinSlope(float minSlope) {
		if (minSlope < 0) throw new IllegalArgumentException("Slope must be positive");
		if (minSlope != this.minSlope) {
			Object oldValue = this.minSlope;
			this.minSlope = minSlope;
			if (minSlope > maxSlope) setMaxSlope(minSlope);
			listeners.notify(EventType.CHANGE, Property.MIN_SLOPE, oldValue, this.minSlope);
		}
	}
	
	public float getMaxSlope() {
		return maxSlope;
	}

	public void setMaxSlope(float maxSlope) {
		if (maxSlope < 0) throw new IllegalArgumentException("Slope must be positive");
		if (maxSlope != this.maxSlope) {
			Object oldValue = this.maxSlope;
			this.maxSlope = maxSlope;
			if (maxSlope < minSlope) setMinSlope(maxSlope);
			listeners.notify(EventType.CHANGE, Property.MAX_SLOPE, oldValue, this.maxSlope);
		}
	}
	
	public float getMinHeight() {
		return minHeight;
	}

	public void setMinHeight(float minHeight) {
		if (minHeight < LH3DLandCell.DEEP_WATER_HEIGHT) throw new IllegalArgumentException("Height must be positive");
		if (minHeight != this.minHeight) {
			Object oldValue = this.minHeight;
			this.minHeight = minHeight;
			if (minHeight > maxHeight) setMaxHeight(minHeight);
			listeners.notify(EventType.CHANGE, Property.MIN_HEIGHT, oldValue, this.minSlope);
		}
	}
	
	public float getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(float maxHeight) {
		if (maxHeight < LH3DLandCell.DEEP_WATER_HEIGHT) throw new IllegalArgumentException("Height must be positive");
		if (maxHeight != this.maxHeight) {
			Object oldValue = this.maxHeight;
			this.maxHeight = maxHeight;
			if (maxHeight < minHeight) setMinHeight(maxHeight);
			listeners.notify(EventType.CHANGE, Property.MAX_HEIGHT, oldValue, this.maxHeight);
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

	private float cameraAngle = 0f;
	private Mask mask = null;
	
	@Override
	protected void startImpl(Coord coord) {
		mask = null;
	}
	
	@Override
	protected void applyImpl(float dt, Coord coord, float cameraAngle, boolean shift) {
		if (mask == null || cameraAngle != this.cameraAngle) {
			this.cameraAngle = cameraAngle;
			mask = createMask(cameraAngle);
		}
		mask.setPosition(coord.x, coord.z);
		if (sound == null) return;
		//Find blocks to update
		int cl = (int)Math.round(mask.getLeft() / CELL_SIZE);
		int cr = (int)Math.round(mask.getRight() / CELL_SIZE);
		int cb = (int)Math.round(mask.getBottom() / CELL_SIZE);
		int ct = (int)Math.round(mask.getTop() / CELL_SIZE);
		int bx0 = (int)Math.floor(mask.getLeft() / LH3DLandBlock.BLOCK_SIZE);
		int bz0 = (int)Math.floor(mask.getBottom() / LH3DLandBlock.BLOCK_SIZE);
		int bx1 = (int)Math.floor(mask.getRight() / LH3DLandBlock.BLOCK_SIZE);
		int bz1 = (int)Math.floor(mask.getTop() / LH3DLandBlock.BLOCK_SIZE);
		if (mask.getLeft() <= bx0 * LH3DLandBlock.BLOCK_SIZE + CELL_SIZE) bx0--;
		if (mask.getBottom() <= bz0 * LH3DLandBlock.BLOCK_SIZE + CELL_SIZE) bz0--;
		//if (mask.getRight() >= (bx1 + 1) * LH3DLandBlock.BLOCK_SIZE - LH3DLandCell.CELL_SIZE) bx1++;
		//if (mask.getTop() >= (bz1 + 1) * LH3DLandBlock.BLOCK_SIZE - LH3DLandCell.CELL_SIZE) bz1++;
		bx0 = MathUtils.clamp(bx0, 0, land.getBlocksPerSide() - 1);
		bz0 = MathUtils.clamp(bz0, 0, land.getBlocksPerSide() - 1);
		bx1 = MathUtils.clamp(bx1, 0, land.getBlocksPerSide() - 1);
		bz1 = MathUtils.clamp(bz1, 0, land.getBlocksPerSide() - 1);
		List<LH3DLandBlock> blocksToEdit = new ArrayList<>((bx1 - bx0 + 1) * (bz1 - bz0 + 1));
		for (int bx = bx0; bx <= bx1; bx++) {
			for (int bz = bz0; bz <= bz1; bz++) {
				LH3DLandBlock block = land.getBlock(bx, bz);
				if (block != null) {
					blocksToEdit.add(block);
				}
			}
		}
		//Paint
		final float normalMinY = (float)Math.cos(Math.atan(maxSlope));
		final float normalMaxY = (float)Math.cos(Math.atan(minSlope));
		for (int cx = cl; cx <= cr; cx++) {
			final float x = cx * CELL_SIZE;
			for (int cz = cb; cz <= ct; cz++) {
				final float z = cz * CELL_SIZE;
				
				final float weight = mask.get(x, z);
				if (weight < 0.1f) continue;
				
				LH3DLandCell cell = land.getCell(cx, cz);
				if (cell == LH3DLandCell.EMPTY) continue;
				
				float h = cell.getHeight();
				if (h < minHeight || h > maxHeight) continue;
				
				boolean isWater = cell.getAltitude() < LH3DLandCell.COASTLINE_ALTITUDE;
				
				LH3DLandCell c0 = land.getCell(cx - 1, cz - 1);
				LH3DLandCell c1 = land.getCell(cx    , cz - 1);
				LH3DLandCell c2 = land.getCell(cx + 1, cz - 1);
				LH3DLandCell c3 = land.getCell(cx - 1, cz    );
				LH3DLandCell c4 = land.getCell(cx + 1, cz    );
				LH3DLandCell c5 = land.getCell(cx - 1, cz + 1);
				LH3DLandCell c6 = land.getCell(cx    , cz + 1);
				LH3DLandCell c7 = land.getCell(cx + 1, cz + 1);
				
				boolean isNearCoastline = !cell.isCoastLine() && (
					    c0.isCoastLine() || c1.isCoastLine() || c2.isCoastLine()
					 || c3.isCoastLine() || c4.isCoastLine() || c5.isCoastLine()
					 || c6.isCoastLine() || c7.isCoastLine());
				
				boolean isNormalLand = !cell.isCoastLine() && !isNearCoastline;
				
				if ((isWater && overWater)
						|| (cell.isCoastLine() && overCoastline)
						|| (isNearCoastline && nearCoastline)
						|| (isNormalLand && overLand)) {
					Vec3f normal = land.getNormal(x, z);
					if (normalMinY <= normal.y && normal.y <= normalMaxY) {
						cell.setSound(sound);
					}
				}
			}
		}
		//Notify changes
		for (LH3DLandBlock block : blocksToEdit) {
			block.listeners.notify(EventType.CHANGE, LH3DLandBlock.Property.CELLS, null, block.getCellsForRead());
		}
	}
	
	@Override
	protected void endImpl() {
		
	}

	@Override
	public void close() {
		
	}
	
	@Override
    public void write(Json json) {
		super.write(json);
		json.writeValue("sound", sound == null ? -1 : sound.code);
		json.writeValue("overCoastline", overCoastline);
		json.writeValue("nearCoastline", nearCoastline);
		json.writeValue("overLand", overLand);
		json.writeValue("minSlope", minSlope);
		json.writeValue("maxSlope", maxSlope);
		json.writeValue("minHeight", minHeight);
		json.writeValue("maxHeight", maxHeight);
	}
	
	@Override
	public void read(Json json, JsonValue jsonData) {
		super.read(json, jsonData);
		if (jsonData.has("sound")) {
			int v = jsonData.getInt("sound");
			setSound(v < 0 ? null : Sound.valueOf(v));
		}
		if (jsonData.has("overCoastline")) setOverCoastline(jsonData.getBoolean("overCoastline"));
		if (jsonData.has("nearCoastline")) setNearCoastline(jsonData.getBoolean("nearCoastline"));
		if (jsonData.has("overLand")) setOverLand(jsonData.getBoolean("overLand"));
		if (jsonData.has("minSlope")) setMinSlope(jsonData.getFloat("minSlope"));
		if (jsonData.has("maxSlope")) setMaxSlope(jsonData.getFloat("maxSlope"));
		if (jsonData.has("minHeight")) setMinHeight(jsonData.getFloat("minHeight"));
		if (jsonData.has("maxHeight")) setMaxHeight(jsonData.getFloat("maxHeight"));
	}
	
	@Override
	public String getDescription() {
		return I18n.tr("action.paintSounds");
	}
}
