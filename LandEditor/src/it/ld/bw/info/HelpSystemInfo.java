package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class HelpSystemInfo extends Struct {
	public float maxDistanceForFOVobject;
	public float wideScreenTime;
	public int readDefaultAdjustGTTime;
	public int readDefaultWordGTTime;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		maxDistanceForFOVobject = str.readFloat();
		wideScreenTime = str.readFloat();
		readDefaultAdjustGTTime = str.readInt();
		readDefaultWordGTTime = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
