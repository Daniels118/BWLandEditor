package it.ld.bw.info;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class ReactionInfo extends Struct {
	public int priority;
	public int numGameTurnsForNormalThingsToReact;
	public int numGameTurnsForNormalThingsBeforeReactingAgain;
	public int numGameTurnsForCreatureToReact;
	public int numGameTurnsForCreatureBeforeReactingAgain;
	public int whetherItImpresses;
	public int whetherReactionFinishesIfInitiatorInHand;
	public float maxReactionDistance;
	public float howImportantIsDistance;
	public int whetherReactionGrows;
	public float reactionGrowthPerGameTurn;
	public int stealthRandomChance;
	public float minDistanceToRunAwayFromObject;
	public float maxDistanceToRunAwayFromObject;
	public float defaultReactionImpressiveMultiplier;
	public float additionToTownBoredomMultipliers;
	public int correspondingTownDesire;
	public int correspondingTownDesireForAlignment;
	public float alignmentModifier;
	public int alignmentForSFX;
	
	@Override
	public void read(EndianDataInputStream str) throws Exception {
		little(str);
		priority = str.readInt();
		numGameTurnsForNormalThingsToReact = str.readInt();
		numGameTurnsForNormalThingsBeforeReactingAgain = str.readInt();
		numGameTurnsForCreatureToReact = str.readInt();
		numGameTurnsForCreatureBeforeReactingAgain = str.readInt();
		whetherItImpresses = str.readInt();
		whetherReactionFinishesIfInitiatorInHand = str.readInt();
		maxReactionDistance = str.readFloat();
		howImportantIsDistance = str.readFloat();
		whetherReactionGrows = str.readInt();
		reactionGrowthPerGameTurn = str.readFloat();
		stealthRandomChance = str.readInt();
		minDistanceToRunAwayFromObject = str.readFloat();
		maxDistanceToRunAwayFromObject = str.readFloat();
		defaultReactionImpressiveMultiplier = str.readFloat();
		additionToTownBoredomMultipliers = str.readFloat();
		correspondingTownDesire = str.readInt();
		correspondingTownDesireForAlignment = str.readInt();
		alignmentModifier = str.readFloat();
		alignmentForSFX = str.readInt();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws Exception {
		throw new RuntimeException("Method not implemented");
	}
}
