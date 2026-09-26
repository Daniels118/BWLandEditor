/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GMagicFireBallInfo extends GObjectInfo {
    public float initialTemperature;
    public float deletionTemperature;
    public float catchIncreaseFactor;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        initialTemperature = str.readFloat();
        deletionTemperature = str.readFloat();
        catchIncreaseFactor = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeFloat(initialTemperature);
        str.writeFloat(deletionTemperature);
        str.writeFloat(catchIncreaseFactor);
    }
}
