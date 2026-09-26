/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GBallInfo extends GMobileObjectInfo {
    public int defaultSpeed;
    public int collideType;
    public int strength;
    public float defence;
    public float kickXZVelocity;
    public float kickYVelocity;
    public float shootXZVelocity;
    public float shootYVelocity;
    public float dribbleXZVelocity;
    public float dribbleYVelocity;
    public float minDistanceToFindPlayer;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        defaultSpeed = str.readInt();
        collideType = str.readInt();
        strength = str.readInt();
        defence = str.readFloat();
        kickXZVelocity = str.readFloat();
        kickYVelocity = str.readFloat();
        shootXZVelocity = str.readFloat();
        shootYVelocity = str.readFloat();
        dribbleXZVelocity = str.readFloat();
        dribbleYVelocity = str.readFloat();
        minDistanceToFindPlayer = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(defaultSpeed);
        str.writeInt(collideType);
        str.writeInt(strength);
        str.writeFloat(defence);
        str.writeFloat(kickXZVelocity);
        str.writeFloat(kickYVelocity);
        str.writeFloat(shootXZVelocity);
        str.writeFloat(shootYVelocity);
        str.writeFloat(dribbleXZVelocity);
        str.writeFloat(dribbleYVelocity);
        str.writeFloat(minDistanceToFindPlayer);
    }
}
