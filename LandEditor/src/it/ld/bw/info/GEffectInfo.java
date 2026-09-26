package it.ld.bw.info;

import java.io.IOException;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GEffectInfo extends Struct {
	public float effectBurn;
	public float effectCrush;
	public float effectHit;
	public float effectHeal;
	public float effectFlyAway;
	public float effectAlignmentModification;
	public float effectBeliefModification;
	public float radius;
	public int testMesh;
	
	@Override
	public void read(EndianDataInputStream str) throws IOException {
		little(str);
		effectBurn = str.readFloat();
		effectCrush = str.readFloat();
		effectHit = str.readFloat();
		effectHeal = str.readFloat();
		effectFlyAway = str.readFloat();
		effectAlignmentModification = str.readFloat();
		effectBeliefModification = str.readFloat();
		radius = str.readFloat();
		testMesh = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}
}
