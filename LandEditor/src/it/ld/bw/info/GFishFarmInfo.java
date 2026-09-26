package it.ld.bw.info;

import java.io.IOException;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GFishFarmInfo extends GMultiMapFixedInfo {
	public int maxNoFishermanPerFishFarm;
	public int numGameTurnsAfterWhichFoodIsIncreased;
	
	@Override
	public void read(EndianDataInputStream str) throws IOException {
		little(str);
		super.read(str);
		maxNoFishermanPerFishFarm = str.readInt();
		numGameTurnsAfterWhichFoodIsIncreased = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}
}
