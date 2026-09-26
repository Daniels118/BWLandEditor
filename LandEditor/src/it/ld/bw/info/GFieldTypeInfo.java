package it.ld.bw.info;

import java.io.IOException;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GFieldTypeInfo extends GMultiMapFixedInfo {
	public float ageGrowth;
	public float ageRecolt;
	public float timesToSow;
	public float foodValueTakenWithHand;
	public float totalFoodInField;
	public int maxFarmerInFarm;
	public float effectSunWhenGrowing;
	public float effectSunWhenRipening;
	public float effectRainWhenGrowing;
	public float effectRainWhenRipening;
	public int fence;
	public float ratioBeforeRipe;
	public float effectOfWaterSpell;
	
	@Override
	public void read(EndianDataInputStream str) throws IOException {
		little(str);
		super.read(str);
		ageGrowth = str.readFloat();
		ageRecolt = str.readFloat();
		timesToSow = str.readFloat();
		foodValueTakenWithHand = str.readFloat();
		totalFoodInField = str.readFloat();
		maxFarmerInFarm = str.readInt();
		effectSunWhenGrowing = str.readFloat();
		effectSunWhenRipening = str.readFloat();
		effectRainWhenGrowing = str.readFloat();
		effectRainWhenRipening = str.readFloat();
		fence = str.readInt();
		ratioBeforeRipe = str.readFloat();
		effectOfWaterSpell = str.readFloat();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}
}
