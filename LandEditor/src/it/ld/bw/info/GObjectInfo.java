/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GObjectInfo extends Struct {
    public int type;
    public int alignmentType;
    public String debugString = "";
    public int collideSound;
    public int immersion;
    public int helpStartEnum;
    public int helpEndEnum;
    public int helpMessage;
    public int helpCondition;
    public int helpInHand;
    public int handCondition;
    public float foodValue;
    public int woodValue;
    public int foodType;
    public float defenceEffectBurn;
    public float defenceEffectCrush;
    public float defenceEffectHit;
    public float defenceEffectHeal;
    public float defenceEffectFlyAway;
    public float defenceEffectAlignmentModification;
    public float defenceEffectBeliefModification;
    public float defenceMultiplierBurn;
    public float defenceMultiplierCrush;
    public float defenceMultiplierHit;
    public float defenceMultiplierHeal;
    public float defenceMultiplierFlyAway;
    public float defenceMultiplierAlignmentModification;
    public float defenceMultiplierBeliefModification;
    public float weight;
    public float heatCapacity;
    public float combustionTemperature;
    public float burningPriority;
    public int canCreatureUseForBuilding;
    public int canCreatureInteractWithMe;
    public int canCreatureAttackMe;
    public int canCreaturePlayWithMe;
    public int canCreatureInspectMe;
    public int canCreatureGiveMeToLiving;
    public int canCreatureBringMeBackToTheCitadel;
    public int villagerInteractState;
    public int villagerInteractStateForBuilding;
    public float villagerInteractDesire;
    public float sacrificeValue;
    public float impressiveValue;
    public float aggressorValue;
    public float villagerImpressiveValue;
    public float artifactMultiplier;
    public float drawImportance;
    public float computerAttackDesire;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        type = str.readInt();
        alignmentType = str.readInt();
        debugString = readFixedString(str, 48, true);
        collideSound = str.readInt();
        immersion = str.readInt();
        helpStartEnum = str.readInt();
        helpEndEnum = str.readInt();
        helpMessage = str.readInt();
        helpCondition = str.readInt();
        helpInHand = str.readInt();
        handCondition = str.readInt();
        foodValue = str.readFloat();
        woodValue = str.readInt();
        foodType = str.readInt();
        defenceEffectBurn = str.readFloat();
        defenceEffectCrush = str.readFloat();
        defenceEffectHit = str.readFloat();
        defenceEffectHeal = str.readFloat();
        defenceEffectFlyAway = str.readFloat();
        defenceEffectAlignmentModification = str.readFloat();
        defenceEffectBeliefModification = str.readFloat();
        defenceMultiplierBurn = str.readFloat();
        defenceMultiplierCrush = str.readFloat();
        defenceMultiplierHit = str.readFloat();
        defenceMultiplierHeal = str.readFloat();
        defenceMultiplierFlyAway = str.readFloat();
        defenceMultiplierAlignmentModification = str.readFloat();
        defenceMultiplierBeliefModification = str.readFloat();
        weight = str.readFloat();
        heatCapacity = str.readFloat();
        combustionTemperature = str.readFloat();
        burningPriority = str.readFloat();
        canCreatureUseForBuilding = str.readInt();
        canCreatureInteractWithMe = str.readInt();
        canCreatureAttackMe = str.readInt();
        canCreaturePlayWithMe = str.readInt();
        canCreatureInspectMe = str.readInt();
        canCreatureGiveMeToLiving = str.readInt();
        canCreatureBringMeBackToTheCitadel = str.readInt();
        villagerInteractState = str.readInt();
        villagerInteractStateForBuilding = str.readInt();
        villagerInteractDesire = str.readFloat();
        sacrificeValue = str.readFloat();
        impressiveValue = str.readFloat();
        aggressorValue = str.readFloat();
        villagerImpressiveValue = str.readFloat();
        artifactMultiplier = str.readFloat();
        drawImportance = str.readFloat();
        computerAttackDesire = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        str.writeInt(type);
        str.writeInt(alignmentType);
        writeFixedString(str, debugString, 48);
        str.writeInt(collideSound);
        str.writeInt(immersion);
        str.writeInt(helpStartEnum);
        str.writeInt(helpEndEnum);
        str.writeInt(helpMessage);
        str.writeInt(helpCondition);
        str.writeInt(helpInHand);
        str.writeInt(handCondition);
        str.writeFloat(foodValue);
        str.writeInt(woodValue);
        str.writeInt(foodType);
        str.writeFloat(defenceEffectBurn);
        str.writeFloat(defenceEffectCrush);
        str.writeFloat(defenceEffectHit);
        str.writeFloat(defenceEffectHeal);
        str.writeFloat(defenceEffectFlyAway);
        str.writeFloat(defenceEffectAlignmentModification);
        str.writeFloat(defenceEffectBeliefModification);
        str.writeFloat(defenceMultiplierBurn);
        str.writeFloat(defenceMultiplierCrush);
        str.writeFloat(defenceMultiplierHit);
        str.writeFloat(defenceMultiplierHeal);
        str.writeFloat(defenceMultiplierFlyAway);
        str.writeFloat(defenceMultiplierAlignmentModification);
        str.writeFloat(defenceMultiplierBeliefModification);
        str.writeFloat(weight);
        str.writeFloat(heatCapacity);
        str.writeFloat(combustionTemperature);
        str.writeFloat(burningPriority);
        str.writeInt(canCreatureUseForBuilding);
        str.writeInt(canCreatureInteractWithMe);
        str.writeInt(canCreatureAttackMe);
        str.writeInt(canCreaturePlayWithMe);
        str.writeInt(canCreatureInspectMe);
        str.writeInt(canCreatureGiveMeToLiving);
        str.writeInt(canCreatureBringMeBackToTheCitadel);
        str.writeInt(villagerInteractState);
        str.writeInt(villagerInteractStateForBuilding);
        str.writeFloat(villagerInteractDesire);
        str.writeFloat(sacrificeValue);
        str.writeFloat(impressiveValue);
        str.writeFloat(aggressorValue);
        str.writeFloat(villagerImpressiveValue);
        str.writeFloat(artifactMultiplier);
        str.writeFloat(drawImportance);
        str.writeFloat(computerAttackDesire);
    }
    
    @Override
    public String toString() {
    	return this.getClass().getSimpleName() + "(" + debugString + ")";
    }
}
