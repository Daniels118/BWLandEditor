package it.ld.bw.info;

import java.io.IOException;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GMagicFlockFlyingInfo extends GMagicInfo {
	public int numberToCreate;
	public float alignmentSwitch;
	public float distanceToTravel;
	
	@Override
	public void read(EndianDataInputStream str) throws IOException {
		little(str);
		super.read(str);
		numberToCreate = str.readInt();
		alignmentSwitch = str.readFloat();
		distanceToTravel = str.readFloat();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}
}
