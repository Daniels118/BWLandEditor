package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GRewardProgress extends Struct {
	public int magicType;
	public int[] onLand = new int[6];
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		magicType = str.readInt();
		onLand = readIntArray(str, onLand);
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
