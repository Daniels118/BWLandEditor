package it.ld.bw.info;

import java.io.IOException;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GMagicShieldInfo extends GMagicRadiusSpellInfo {
	public float chantCostPerImpactMomentum;
	public float shieldHeight;
	public float raiseWithScale;
	public float bobMagnitude;
	
	@Override
	public void read(EndianDataInputStream str) throws IOException {
		little(str);
		super.read(str);
		chantCostPerImpactMomentum = str.readFloat();
		shieldHeight = str.readFloat();
		raiseWithScale = str.readFloat();
		bobMagnitude = str.readFloat();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}
}
