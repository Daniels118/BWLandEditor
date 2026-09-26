package it.ld.bw.lnd.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.ld.bw.lnd.model.LH3DLandBlock;
import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.Vec3f;
import it.ld.utils.MathUtils;
import it.ld.utils.UChangeListener.EventType;

public final class ShadowTool {
	private static final Vec3f lightDirection = new Vec3f(1, -1, 1).nor();
	private static final Vec3f towardLight = lightDirection.cpy().scl(-1f);
	
	private ShadowTool() {}
	
	public static void updateShadows(LndFile land) {
		updateShadows(land, null);
	}
	
	public static void updateShadows(LndFile land, Collection<LH3DLandBlock> blocks) {
		final Vec3f pos = new Vec3f();
		final List<LH3DLandBlock> blocksToUpdate = blocks == null ? land.getLandBlocksForRead() : getBlocksToUpdate(land, blocks);
		final int size = land.getCellsPerSide() + 2;
		float[][] brightness = new float[size][size];
		//Compute shadows
		for (int ax = 0; ax < size; ax++) {
			for (int az = 0; az < size; az++) {
				brightness[ax][az] = LH3DLandCell.BRIGHT;
			}
		}
		Vec3f normal = new Vec3f();
		for (LH3DLandBlock tBlock : blocksToUpdate) {
			for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
				final int az = tBlock.getBlockZ() * LH3DLandBlock.SEG_PER_SIDE + cz;
				pos.z = tBlock.getMapZ() + cz * LH3DLandCell.CELL_SIZE;
				for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
					final int ax = tBlock.getBlockX() * LH3DLandBlock.SEG_PER_SIDE + cx;
					pos.x = tBlock.getMapX() + cx * LH3DLandCell.CELL_SIZE;
					LH3DLandCell cell = tBlock.getCell(cx, cz);
					pos.y = Math.max(0f, cell.getHeight());
					Vec3f hit = land.pickPos(pos, towardLight, false);
					if (hit != null) {
						brightness[ax + 1][az + 1] = LH3DLandCell.DARK;
					} else {
						land.getNormal(pos.x, pos.z, normal);
						float incidence = MathUtils.clamp(normal.dot(towardLight) + 0.0f, 0f, 1f);
						if (incidence < 0.1f) {
							brightness[ax + 1][az + 1] = MathUtils.lerp(LH3DLandCell.DARK, LH3DLandCell.BRIGHT, incidence * 5f);
						}
					}
				}
			}
		}
		//Smooth edges
		for (LH3DLandBlock tBlock : blocksToUpdate) {
			for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
				final int az = tBlock.getBlockZ() * LH3DLandBlock.SEG_PER_SIDE + cz;
				for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
					final int ax = tBlock.getBlockX() * LH3DLandBlock.SEG_PER_SIDE + cx;
					LH3DLandCell cell = tBlock.getCell(cx, cz);
					float centralBrightness = brightness[ax + 1][az + 1];
					/*if (centralBrightness > LH3DLandCell.DARK) {
						float sum = 2f * centralBrightness
								+ brightness[ax][az + 1]
								+ brightness[ax + 1][az];
						cell.setLightLevel(sum / 4f);
					} else {*/
						float sum = 2f * centralBrightness
								+ brightness[ax][az + 1]
								+ brightness[ax + 2][az + 1]
								+ brightness[ax + 1][az]
								+ brightness[ax + 1][az + 2];
						cell.setLightLevel(sum / 6f);
					//}
				}
			}
		}
		//Notify
		final int last = blocksToUpdate.size() - 1;
		for (int i = 0; i <= last; i++) {
			LH3DLandBlock tBlock = blocksToUpdate.get(i);
			tBlock.listeners.notify(EventType.CHANGE, LH3DLandBlock.Property.CELLS, null, tBlock, tBlock.getIndex(), null, i == last);
		}
	}
	
	private static List<LH3DLandBlock> getBlocksToUpdate(LndFile land, Collection<LH3DLandBlock> blocks) {
		LH3DLandBlock[] toUpdate = new LH3DLandBlock[land.getNumBlocks()];
		for (LH3DLandBlock block : blocks) {
			toUpdate[block.getIndex()] = block;
			setBlock(toUpdate, land, block, +1,  0);
			setBlock(toUpdate, land, block,  0, +1);
			setBlock(toUpdate, land, block, +1, +1);
		}
		List<LH3DLandBlock> res = new ArrayList<>(toUpdate.length);
		for (LH3DLandBlock block : toUpdate) {
			if (block != null) res.add(block);
		}
		return res;
	}
	
	private static void setBlock(LH3DLandBlock[] blocks, LndFile land, LH3DLandBlock ref, int offsetx, int offsetz) {
		LH3DLandBlock block = land.getBlock(ref.getBlockX() + offsetx, ref.getBlockZ() + offsetz);
		if (block != null) blocks[block.getIndex()] = block;
	}
}
