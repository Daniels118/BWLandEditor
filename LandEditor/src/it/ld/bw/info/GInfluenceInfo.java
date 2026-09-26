package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GInfluenceInfo extends Struct {
	public float percentageFullInfluence;
	public float percentageDistanceForDecreasingGradient;
	public float valueOfSmall;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		percentageFullInfluence = str.readFloat();
		percentageDistanceForDecreasingGradient = str.readFloat();
		valueOfSmall = str.readFloat();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
