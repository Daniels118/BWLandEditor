/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GPotInfo extends GMobileObjectInfo {
    public PotType potType;
    public ResourceType resourceType;
    public int maxAmountInPot;
    public int scaleEvery;
    public PotInfo nextPotForResource;
    public int associatedReaction;
    public int canBecomeAPhysicsObject;
    public int amountPickedUpInitially;
    public int amountPickedUpPerTurn;
    public int amountPickedUpPerTurnEnd;
    public int maxAmountCanBePickedUp;
    public float multiPickUpRampTime;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        potType = PotType.valueOf(str.readInt());
        resourceType = ResourceType.valueOf(str.readInt());
        maxAmountInPot = str.readInt();
        scaleEvery = str.readInt();
        nextPotForResource = PotInfo.valueOf(str.readInt());
        associatedReaction = str.readInt();
        canBecomeAPhysicsObject = str.readInt();
        amountPickedUpInitially = str.readInt();
        amountPickedUpPerTurn = str.readInt();
        amountPickedUpPerTurnEnd = str.readInt();
        maxAmountCanBePickedUp = str.readInt();
        multiPickUpRampTime = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(PotType.ordinal(potType));
        str.writeInt(resourceType.code);
        str.writeInt(maxAmountInPot);
        str.writeInt(scaleEvery);
        str.writeInt(PotInfo.ordinal(nextPotForResource));
        str.writeInt(associatedReaction);
        str.writeInt(canBecomeAPhysicsObject);
        str.writeInt(amountPickedUpInitially);
        str.writeInt(amountPickedUpPerTurn);
        str.writeInt(amountPickedUpPerTurnEnd);
        str.writeInt(maxAmountCanBePickedUp);
        str.writeFloat(multiPickUpRampTime);
    }
}
