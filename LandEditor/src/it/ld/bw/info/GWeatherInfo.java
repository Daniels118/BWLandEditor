package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GWeatherInfo extends Struct {
	public String debugString;
	public float fadeInOutTime;
	public float lastsFor;
	public float strength;
	public int temperature;
	public int wetness;
	public int snowFall;
	public int overCast;
	public int[] wind = new int[2];
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		debugString = readFixedString(str, 0x30, true);
		fadeInOutTime = str.readFloat();
		lastsFor = str.readFloat();
		strength = str.readFloat();
		temperature = str.readInt();
		wetness = str.readInt();
		snowFall = str.readInt();
		overCast = str.readInt();
		wind = readIntArray(str, wind);
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
	
	@Override
	public String toString() {
		return debugString != null ? debugString : super.toString();
	}
}
