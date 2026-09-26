package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GHelpSystemTooltipsInfo extends Struct {
	public float priority;
	public float displayTime;
	public float displayTimeAfterFocus;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		priority = str.readFloat();
		displayTime = str.readFloat();
		displayTimeAfterFocus = str.readFloat();
	}

	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
