/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GPBallInfo extends GMobileObjectInfo {
    public float kickXZVelocity;
    public float kickYVelocity;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        kickXZVelocity = str.readFloat();
        kickYVelocity = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeFloat(kickXZVelocity);
        str.writeFloat(kickYVelocity);
    }
}
