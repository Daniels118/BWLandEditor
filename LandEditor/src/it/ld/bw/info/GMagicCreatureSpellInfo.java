package it.ld.bw.info;

import java.io.IOException;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GMagicCreatureSpellInfo extends GMagicInfo {
	public int creatureReceiveSpellType;
	public String text;
	public float startTransitionDuration;
	public float finishTransitionDuration;
	public float totalDuration;
	public float maxDirnChangeWhenCtrCasting;
	
	@Override
	public void read(EndianDataInputStream str) throws IOException {
		little(str);
		super.read(str);
		creatureReceiveSpellType = str.readInt();
		text = readFixedString(str, 0x30, true);
		startTransitionDuration = str.readFloat();
		finishTransitionDuration = str.readFloat();
		totalDuration = str.readFloat();
		maxDirnChangeWhenCtrCasting = str.readFloat();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}
	
	@Override
	public String toString() {
		return text != null ? text : super.toString();
	}
}
