package it.ld.bw.info;

import java.io.IOException;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GMagicEffectInfo extends GEffectInfo {
	private final boolean ci;
	
	public String debugString;
	public int timerWhenOneShot;
	public int timerWhenPlayerCasting;
	public int timerWhenCreatureCasting;
	public int timerWhenComputerPlayerCasting;
	public float initialChants;
	public float costToCreate;
	public float costPerEvent;
	public float costPerGameTurn;
	public float costPerShieldCollide;
	public int divideCostsByTribalPower;
	public int createReactionOnCast;
	public int createReactionOnEvent;
	public int reactionType;
	public int[] perceivedPlayerDesire = new int[2];
	public int townDesireBeingHelped;
	public float agressiveRangeMin;
	public float agressiveRangeMax;
	public float calculatedCostForOneTurn;
	public float costForOneAppliedEffect;
	public float costInGameTurnsToCreateForOneVillager;
	public float tempNoVillagers;
	public float costForTempVillagers;
	public float costInRealTimeToCreate;
	public float impressiveValue;
	public float cpImpressiveBalance;
	public int[] useTribalPowerMultiplier = new int[9];
	public int isAggressiveSpellWhichIsUsedInCreatureFightArena;
	public int isDefensiveSpellWhichIsUsedInCreatureFightArena;
	public int helpStartEnum;
	public int helpEndEnum;
	public int creatureNearlyLearntEnum;
	public int creatureLearntEnum;
	public int helpMessage;
	public int helpCondition;
	public int toolTipsEnum;
	public float aggressiveAttackValue;
	public float computerCastDuration;
	
	public int unknown;	//CI only
	
	public GMagicEffectInfo(boolean ci) {
		this.ci = ci;
	}
	
	@Override
	public void read(EndianDataInputStream str) throws IOException {
		little(str);
		super.read(str);
		debugString = readFixedString(str, 0x30, true);
		timerWhenOneShot = str.readInt();
		timerWhenPlayerCasting = str.readInt();
		timerWhenCreatureCasting = str.readInt();
		timerWhenComputerPlayerCasting = str.readInt();
		initialChants = str.readFloat();
		costToCreate = str.readFloat();
		costPerEvent = str.readFloat();
		costPerGameTurn = str.readFloat();
		costPerShieldCollide = str.readFloat();
		divideCostsByTribalPower = str.readInt();
		createReactionOnCast = str.readInt();
		createReactionOnEvent = str.readInt();
		reactionType = str.readInt();
		perceivedPlayerDesire = readIntArray(str, perceivedPlayerDesire.length);
		townDesireBeingHelped = str.readInt();
		agressiveRangeMin = str.readFloat();
		agressiveRangeMax = str.readFloat();
		calculatedCostForOneTurn = str.readFloat();
		costForOneAppliedEffect = str.readFloat();
		costInGameTurnsToCreateForOneVillager = str.readFloat();
		tempNoVillagers = str.readFloat();
		costForTempVillagers = str.readFloat();
		costInRealTimeToCreate = str.readFloat();
		impressiveValue = str.readFloat();
		cpImpressiveBalance = str.readFloat();
		useTribalPowerMultiplier = readIntArray(str, useTribalPowerMultiplier.length);
		isAggressiveSpellWhichIsUsedInCreatureFightArena = str.readInt();
		isDefensiveSpellWhichIsUsedInCreatureFightArena = str.readInt();
		helpStartEnum = str.readInt();
		helpEndEnum = str.readInt();
		creatureNearlyLearntEnum = str.readInt();
		creatureLearntEnum = str.readInt();
		helpMessage = str.readInt();
		helpCondition = str.readInt();
		toolTipsEnum = str.readInt();
		aggressiveAttackValue = str.readFloat();
		computerCastDuration = str.readFloat();
		if (ci) {
			unknown = str.readInt();
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}
	
	@Override
    public String toString() {
    	return this.getClass().getSimpleName() + "(" + debugString + ")";
    }
	
	public static GMagicEffectInfo newVanilla() {
		return new GMagicEffectInfo(false);
	}
	
	public static GMagicEffectInfo newCI() {
		return new GMagicEffectInfo(true);
	}
}
