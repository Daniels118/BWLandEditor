/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GWallSectionInfo extends GFeatureInfo {
    public float nearEnoughToImprove;
    public int gameTurnsBeforeImprove;
    public float heightIncreasePerImprove;
    public float distanceToConsiderSeperateWall;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        nearEnoughToImprove = str.readFloat();
        gameTurnsBeforeImprove = str.readInt();
        heightIncreasePerImprove = str.readFloat();
        distanceToConsiderSeperateWall = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeFloat(nearEnoughToImprove);
        str.writeInt(gameTurnsBeforeImprove);
        str.writeFloat(heightIncreasePerImprove);
        str.writeFloat(distanceToConsiderSeperateWall);
    }
}
