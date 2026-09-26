/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GAlignmentInfo extends Struct {
    public float animalNice;
    public float animalNasty;
    public float creature;
    public float priest;
    public float skeleton;
    public float villager;
    public float building;
    public float plant;
    public float field;
    public float feature;
    public float mobileObject;
    public float land;
    public float script;
    public float unimportant;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        animalNice = str.readFloat();
        animalNasty = str.readFloat();
        creature = str.readFloat();
        priest = str.readFloat();
        skeleton = str.readFloat();
        villager = str.readFloat();
        building = str.readFloat();
        plant = str.readFloat();
        field = str.readFloat();
        feature = str.readFloat();
        mobileObject = str.readFloat();
        land = str.readFloat();
        script = str.readFloat();
        unimportant = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        str.writeFloat(animalNice);
        str.writeFloat(animalNasty);
        str.writeFloat(creature);
        str.writeFloat(priest);
        str.writeFloat(skeleton);
        str.writeFloat(villager);
        str.writeFloat(building);
        str.writeFloat(plant);
        str.writeFloat(field);
        str.writeFloat(feature);
        str.writeFloat(mobileObject);
        str.writeFloat(land);
        str.writeFloat(script);
        str.writeFloat(unimportant);
    }
}
