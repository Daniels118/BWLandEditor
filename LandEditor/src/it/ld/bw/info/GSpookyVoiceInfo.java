package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GSpookyVoiceInfo extends Struct {
	public int soundNumber;
	public String name;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		soundNumber = str.readInt();
		name = readFixedString(str, 30, true);
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
	
	@Override
	public String toString() {
		return name != null ? name : super.toString();
	}
}
