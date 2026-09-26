/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GAnimalInfo extends GLivingInfo {
	private final boolean ci;
	
    public int animalInfo;
    public int sex;
    public int low;
    public int std;
    public int high;
    public int defaultAnim;
    public int hunger;
    public int thirst;
    public int sleep;
    public int needToBreed;
    public float flockDistance;
    public int turnAngle;
    public int viewAngle;
    public int playerCanPickUp;
    public int needsTown;
    public int needsFoodTypes;
    public int flocksCanMerge;
    public float stalkingDistance;
    public float attackDistance;
    public float huntingDistance;
    public int siSightDistance;
    public int farSightDistance;
    public int nearSightDistance;
    public int chaseTime;
    public int environment;
    public int domainInnerRadius;
    public int domainRadius;
    public int maxFlockSize;
    public int stayTime;
    public float altitudeMin;
    public float altitudeMax;
    public float altitudeVariance;
    public float altitudeMovementChange;
    public float altitudeNormal;
    public float[] ageToScale = new float[20];
    
    public int unknown;	//CI only
    
    public GAnimalInfo(boolean ci) {
		this.ci = ci;
	}

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        animalInfo = str.readInt();
        sex = str.readInt();
        low = str.readInt();
        std = str.readInt();
        high = str.readInt();
        defaultAnim = str.readInt();
        hunger = str.readInt();
        thirst = str.readInt();
        sleep = str.readInt();
        needToBreed = str.readInt();
        flockDistance = str.readFloat();
        turnAngle = str.readInt();
        viewAngle = str.readInt();
        playerCanPickUp = str.readInt();
        needsTown = str.readInt();
        needsFoodTypes = str.readInt();
        flocksCanMerge = str.readInt();
        stalkingDistance = str.readFloat();
        attackDistance = str.readFloat();
        huntingDistance = str.readFloat();
        siSightDistance = str.readInt();
        farSightDistance = str.readInt();
        nearSightDistance = str.readInt();
        chaseTime = str.readInt();
        environment = str.readInt();
        domainInnerRadius = str.readInt();
        domainRadius = str.readInt();
        maxFlockSize = str.readInt();
        stayTime = str.readInt();
        altitudeMin = str.readFloat();
        altitudeMax = str.readFloat();
        altitudeVariance = str.readFloat();
        altitudeMovementChange = str.readFloat();
        altitudeNormal = str.readFloat();
        readFloatArray(str, ageToScale);
        if (ci) {
			unknown = str.readInt();
		}
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(animalInfo);
        str.writeInt(sex);
        str.writeInt(high);
        str.writeInt(std);
        str.writeInt(low);
        str.writeInt(defaultAnim);
        str.writeInt(hunger);
        str.writeInt(thirst);
        str.writeInt(sleep);
        str.writeInt(needToBreed);
        str.writeFloat(flockDistance);
        str.writeInt(turnAngle);
        str.writeInt(viewAngle);
        str.writeInt(playerCanPickUp);
        str.writeInt(needsTown);
        str.writeInt(needsFoodTypes);
        str.writeInt(flocksCanMerge);
        str.writeFloat(stalkingDistance);
        str.writeFloat(attackDistance);
        str.writeFloat(huntingDistance);
        str.writeInt(siSightDistance);
        str.writeInt(farSightDistance);
        str.writeInt(nearSightDistance);
        str.writeInt(chaseTime);
        str.writeInt(environment);
        str.writeInt(domainInnerRadius);
        str.writeInt(domainRadius);
        str.writeInt(maxFlockSize);
        str.writeInt(stayTime);
        str.writeFloat(altitudeMin);
        str.writeFloat(altitudeMax);
        str.writeFloat(altitudeVariance);
        str.writeFloat(altitudeMovementChange);
        str.writeFloat(altitudeNormal);
        writeFloatArray(str, ageToScale);
        if (ci) {
			str.writeInt(unknown);
		}
    }
    
    public static GAnimalInfo newVanilla() {
		return new GAnimalInfo(false);
	}
	
	public static GAnimalInfo newCI() {
		return new GAnimalInfo(true);
	}
}
