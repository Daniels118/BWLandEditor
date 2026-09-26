package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GTownDesireInfo extends Struct {
	public int associatedPrayerSite;
	public float showsAfterPercent;
	public float desireTriggersVillagerAction;
	public float desireTriggersVillagerEmergencyAction;
	public int[] associatedVillagerBirth = new int[3];
	public int worshipSiteMesh;
	public int worshipSiteSlot;
	public float worshipSiteScale;
	public float desireToBeliefScale;
	public float desireAffectsBeliefAfter;
	public float desireBuildWonderReducer;
	public float desireValueGreaterCausesDecayInBelief;
	public float desireToBeliefThresholdDecay;
	public float desireAffectsAlignmentAfter;
	public float howImportantDesireIsToAlignment;
	public int maxTimeForAlignmentChange;
	public float[] tribeMultiplier = new float[9];
	public int helpStartEnum;
	public int helpEndEnum;
	public int helpStatEnum;
	public int helpMessage;
	public int helpCondition;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		associatedPrayerSite = str.readInt();
		showsAfterPercent = str.readFloat();
		desireTriggersVillagerAction = str.readFloat();
		desireTriggersVillagerEmergencyAction = str.readFloat();
		associatedVillagerBirth = readIntArray(str, associatedVillagerBirth);
		worshipSiteMesh = str.readInt();
		worshipSiteSlot = str.readInt();
		worshipSiteScale = str.readFloat();
		desireToBeliefScale = str.readFloat();
		desireAffectsBeliefAfter = str.readFloat();
		desireBuildWonderReducer = str.readFloat();
		desireValueGreaterCausesDecayInBelief = str.readFloat();
		desireToBeliefThresholdDecay = str.readFloat();
		desireAffectsAlignmentAfter = str.readFloat();
		howImportantDesireIsToAlignment = str.readFloat();
		maxTimeForAlignmentChange = str.readInt();
		tribeMultiplier = readFloatArray(str, tribeMultiplier);
		helpStartEnum = str.readInt();
		helpEndEnum = str.readInt();
		helpStatEnum = str.readInt();
		helpMessage = str.readInt();
		helpCondition = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
