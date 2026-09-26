package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GPlaytimeInfo extends Struct {
	public int associatedObject;
	public float priority;
	public int associatedStructure;
	public int associatedAbodeNumber;
	public int associatedDance;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		associatedObject = str.readInt();
		priority = str.readFloat();
		associatedStructure = str.readInt();
		associatedAbodeNumber = str.readInt();
		associatedDance = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
