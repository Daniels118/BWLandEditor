package it.ld.bw.info;

import java.io.IOException;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GSpellIconInfo extends GMultiMapFixedInfo {
	int meshId;
	float radiusFromCitadel;
	float gatheringChantAddPerGameTurn;
	
	@Override
	public void read(EndianDataInputStream str) throws IOException {
		little(str);
		super.read(str);
		meshId = str.readInt();
		radiusFromCitadel = str.readFloat();
		gatheringChantAddPerGameTurn = str.readFloat();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}
}
