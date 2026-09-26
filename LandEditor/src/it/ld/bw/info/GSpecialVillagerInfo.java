/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GSpecialVillagerInfo extends Struct {
    public String name = "";
    public int age;
    public int sex;
    public int job;
    public int married;
    public int tribe;
    public int pet;
    public int faceNumber;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        name = readFixedString(str, 48, true);
        age = str.readInt();
        sex = str.readInt();
        job = str.readInt();
        married = str.readInt();
        tribe = str.readInt();
        pet = str.readInt();
        faceNumber = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        writeFixedString(str, name, 48);
        str.writeInt(age);
        str.writeInt(sex);
        str.writeInt(job);
        str.writeInt(married);
        str.writeInt(tribe);
        str.writeInt(pet);
        str.writeInt(faceNumber);
    }
    
    @Override
    public String toString() {
    	return this.getClass().getSimpleName() + "(" + name + ")";
    }
}
