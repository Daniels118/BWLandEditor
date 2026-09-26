package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GSpellSystemInfo extends Struct {
	public int selectionSystemGestureR;
	public int selectionSystemGestureNonCreature;
	public int selectionSystemGestureCreature;
	public float selectionSystemTimeOut;
	public float selectionSystemRepeatTimeOut;
	public float delayBeforeSeedActive;
	public int creatureSpecialMoveGesture;
	public float leashSelectionSystemTimeOut;
	public int leashSelectionStart;
	public int[] leashSelectionGestures = new int[4];
	public int creatureZoomTo;
	public int creatureEndGive;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		selectionSystemGestureR = str.readInt();
		selectionSystemGestureNonCreature = str.readInt();
		selectionSystemGestureCreature = str.readInt();
		selectionSystemTimeOut = str.readFloat();
		selectionSystemRepeatTimeOut = str.readFloat();
		delayBeforeSeedActive = str.readFloat();
		creatureSpecialMoveGesture = str.readInt();
		leashSelectionSystemTimeOut = str.readFloat();
		leashSelectionStart = str.readInt();
		leashSelectionGestures = readIntArray(str, leashSelectionGestures);
		creatureZoomTo = str.readInt();
		creatureEndGive = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
