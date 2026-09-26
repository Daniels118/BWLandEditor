/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GLivingInfo extends GMobileWallHugInfo {
    public int creatureType;
    public int moveState;
    public float life;
    public float strength;
    public float defence;
    public int startAge;
    public int grownUpAge;
    public int oldAge;
    public int retirementAge;
    public IsReacting isReacting = new IsReacting();
    public int isShepherdable;
    public int minFlockingValue;
    public int maxFlockingValue;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        creatureType = str.readInt();
        moveState = str.readInt();
        life = str.readFloat();
        strength = str.readFloat();
        defence = str.readFloat();
        startAge = str.readInt();
        grownUpAge = str.readInt();
        oldAge = str.readInt();
        retirementAge = str.readInt();
        isReacting.read(str);
        isShepherdable = str.readInt();
        minFlockingValue = str.readInt();
        maxFlockingValue = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(creatureType);
        str.writeInt(moveState);
        str.writeFloat(life);
        str.writeFloat(strength);
        str.writeFloat(defence);
        str.writeInt(startAge);
        str.writeInt(grownUpAge);
        str.writeInt(oldAge);
        str.writeInt(retirementAge);
        isReacting.write(str);
        str.writeInt(isShepherdable);
        str.writeInt(minFlockingValue);
        str.writeInt(maxFlockingValue);
    }
}
