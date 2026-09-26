package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GPlayerInfo extends Struct {
	public float maxAlignmentChangePerGameTurn;
	public float maxScriptAlignmentChange;
	public float treePullPutAlignmentChange;
	public float applyEffectAlignmentChangeAddition;
	public float[] dealthReason = new float[10];
	public float computerPlayerBeliefChangeDecay;
	public float averageTownPopulationForCredits;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		maxAlignmentChangePerGameTurn = str.readFloat();
		maxScriptAlignmentChange = str.readFloat();
		treePullPutAlignmentChange = str.readFloat();
		applyEffectAlignmentChangeAddition = str.readFloat();
		dealthReason = readFloatArray(str, dealthReason);
		computerPlayerBeliefChangeDecay = str.readFloat();
		averageTownPopulationForCredits = str.readFloat();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
