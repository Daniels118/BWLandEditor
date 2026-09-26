/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GShotInfo extends GMobileObjectInfo {
    public int hitEffect;
    public int strength;
    public int acceleration;
    public int heightAcceleration;
    public int velocity;
    public int finalHeightVelocity;
    public int speed;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        hitEffect = str.readInt();
        strength = str.readInt();
        acceleration = str.readInt();
        heightAcceleration = str.readInt();
        velocity = str.readInt();
        finalHeightVelocity = str.readInt();
        speed = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(hitEffect);
        str.writeInt(strength);
        str.writeInt(acceleration);
        str.writeInt(heightAcceleration);
        str.writeInt(velocity);
        str.writeInt(finalHeightVelocity);
        str.writeInt(speed);
    }
}
