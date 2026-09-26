/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GAbodeInfo extends GMultiMapFixedInfo {
    public int abodeType;
    public AbodeNumber abodeNumber;
    public String debugString = "";
    public TribeType tribeType;
    public int meshId;
    public int canBePhysicallyDamaged;
    public float startLife;
    public int startStrength;
    public float startDefence;
    public int startInfluence;
    public int maxVillagersInAbode;
    public int maxChildrenInAbode;
    public int startVillagersInAbode;
    public int startChildrenInAbode;
    public int startFood;
    public int startFoodRAnd;
    public int startWood;
    public int startWoodRAnd;
    public int howLongRuinLastsFor;
    public int potForResourceFood;
    public int potForResourceWood;
    public float percentTooCrowded;
    public int producesMobileObject;
    public float maxNumMobileObjectsToProduce;
    public float timeEachMobileObjectTakesToProduce;
    public float emptyAbodeLifeReducer;
    public int populationWhenNeeded;
    public float thresholdForStopBeingFunctional;
    public int toolTipsForBuild;
    public int didYouKnow;
    public int dykCategory;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        abodeType = str.readInt();
        abodeNumber = AbodeNumber.valueOf(str.readInt());
        debugString = readFixedString(str, 48, true);
        tribeType = TribeType.valueOf(str.readInt());
        meshId = str.readInt();
        canBePhysicallyDamaged = str.readInt();
        startLife = str.readFloat();
        startStrength = str.readInt();
        startDefence = str.readFloat();
        startInfluence = str.readInt();
        maxVillagersInAbode = str.readInt();
        maxChildrenInAbode = str.readInt();
        startVillagersInAbode = str.readInt();
        startChildrenInAbode = str.readInt();
        startFood = str.readInt();
        startFoodRAnd = str.readInt();
        startWood = str.readInt();
        startWoodRAnd = str.readInt();
        howLongRuinLastsFor = str.readInt();
        potForResourceFood = str.readInt();
        potForResourceWood = str.readInt();
        percentTooCrowded = str.readFloat();
        producesMobileObject = str.readInt();
        maxNumMobileObjectsToProduce = str.readFloat();
        timeEachMobileObjectTakesToProduce = str.readFloat();
        emptyAbodeLifeReducer = str.readFloat();
        populationWhenNeeded = str.readInt();
        thresholdForStopBeingFunctional = str.readFloat();
        toolTipsForBuild = str.readInt();
        didYouKnow = str.readInt();
        dykCategory = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(abodeType);
        str.writeInt(AbodeNumber.ordinal(abodeNumber));
        writeFixedString(str, debugString, 48);
        str.writeInt(TribeType.ordinal(tribeType));
        str.writeInt(meshId);
        str.writeInt(canBePhysicallyDamaged);
        str.writeFloat(startLife);
        str.writeInt(startStrength);
        str.writeFloat(startDefence);
        str.writeInt(startInfluence);
        str.writeInt(maxVillagersInAbode);
        str.writeInt(maxChildrenInAbode);
        str.writeInt(startVillagersInAbode);
        str.writeInt(startChildrenInAbode);
        str.writeInt(startFood);
        str.writeInt(startFoodRAnd);
        str.writeInt(startWood);
        str.writeInt(startWoodRAnd);
        str.writeInt(howLongRuinLastsFor);
        str.writeInt(potForResourceFood);
        str.writeInt(potForResourceWood);
        str.writeFloat(percentTooCrowded);
        str.writeInt(producesMobileObject);
        str.writeFloat(maxNumMobileObjectsToProduce);
        str.writeFloat(timeEachMobileObjectTakesToProduce);
        str.writeFloat(emptyAbodeLifeReducer);
        str.writeInt(populationWhenNeeded);
        str.writeFloat(thresholdForStopBeingFunctional);
        str.writeInt(toolTipsForBuild);
        str.writeInt(didYouKnow);
        str.writeInt(dykCategory);
    }
    
    @Override
    public String toString() {
    	return this.getClass().getSimpleName() + "(" + debugString + "/" + super.debugString + ")";
    }
}
