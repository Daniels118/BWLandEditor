/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GVortexInfo extends Struct {
    public ParticleType particleTypePreLandscape;
    public ParticleType particleTypePostLandscape;
    public ParticleType particleTypeObjectMover;
    public ParticleType particleTypeLightMap;
    public int initialState;
    public int fadeWhenDeleted;
    public float baseScale;
    public int maxToCreateTotalObjects;
    public int maxToCreateResourceFood;
    public int maxToCreateResourceWood;
    public int maxToCreateVillager;
    public int maxToCreateOneShot;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        particleTypePreLandscape = ParticleType.values()[str.readInt()];
        particleTypePostLandscape = ParticleType.values()[str.readInt()];
        particleTypeObjectMover = ParticleType.values()[str.readInt()];
        particleTypeLightMap = ParticleType.values()[str.readInt()];
        initialState = str.readInt();
        fadeWhenDeleted = str.readInt();
        baseScale = str.readFloat();
        maxToCreateTotalObjects = str.readInt();
        maxToCreateResourceFood = str.readInt();
        maxToCreateResourceWood = str.readInt();
        maxToCreateVillager = str.readInt();
        maxToCreateOneShot = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        str.writeInt(particleTypePreLandscape.ordinal());
        str.writeInt(particleTypePostLandscape.ordinal());
        str.writeInt(particleTypeObjectMover.ordinal());
        str.writeInt(particleTypeLightMap.ordinal());
        str.writeInt(initialState);
        str.writeInt(fadeWhenDeleted);
        str.writeFloat(baseScale);
        str.writeInt(maxToCreateTotalObjects);
        str.writeInt(maxToCreateResourceFood);
        str.writeInt(maxToCreateResourceWood);
        str.writeInt(maxToCreateVillager);
        str.writeInt(maxToCreateOneShot);
    }
}
