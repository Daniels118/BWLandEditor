/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GCreaturePenInfo extends GCitadelPartInfo {
    public int info;
    public float startPlacementDistance;
    public float endPlacementDistance;
    public int placementAngle;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        info = str.readInt();
        startPlacementDistance = str.readFloat();
        endPlacementDistance = str.readFloat();
        placementAngle = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(info);
        str.writeFloat(startPlacementDistance);
        str.writeFloat(endPlacementDistance);
        str.writeInt(placementAngle);
    }
}
