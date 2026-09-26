/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GDanceInfo extends Struct {
    public int duration;
    public int totalNumBeats;
    public String debugText = "";
    public String fileName = "";
    public int startsAutomatically;
    public float[] areaRequired = new float[2];
    public int minimumDancers;
    public int maximumDancers;
    public float impressiveness;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        duration = str.readInt();
        totalNumBeats = str.readInt();
        debugText = readFixedString(str, 64, true);
        fileName = readFixedString(str, 64, true);
        startsAutomatically = str.readInt();
        areaRequired = readFloatArray(str, areaRequired);
        minimumDancers = str.readInt();
        maximumDancers = str.readInt();
        impressiveness = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        str.writeInt(duration);
        str.writeInt(totalNumBeats);
        writeFixedString(str, debugText, 64);
        writeFixedString(str, fileName, 64);
        str.writeInt(startsAutomatically);
        writeFloatArray(str, areaRequired);
        str.writeInt(minimumDancers);
        str.writeInt(maximumDancers);
        str.writeFloat(impressiveness);
    }
}
