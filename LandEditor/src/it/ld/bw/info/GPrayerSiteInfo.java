package it.ld.bw.info;

import java.io.IOException;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GPrayerSiteInfo extends GFeatureInfo {
	public float radius;
	
	@Override
	public void read(EndianDataInputStream str) throws IOException {
		little(str);
		super.read(str);
		radius = str.readFloat();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}
}
