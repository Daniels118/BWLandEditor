/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GMultiMapFixedInfo extends GObjectInfo {
    public int groundInfo;
    public int woodRequiredPerBuild;
    public int timeToBuild;
    public int scaffoldsRequired;
    public int maxVillagerNeededToBuild;
    public float desireToBeBuilt;
    public float desireToBeRepaired;
    public float influence;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        groundInfo = str.readInt();
        woodRequiredPerBuild = str.readInt();
        timeToBuild = str.readInt();
        scaffoldsRequired = str.readInt();
        maxVillagerNeededToBuild = str.readInt();
        desireToBeBuilt = str.readFloat();
        desireToBeRepaired = str.readFloat();
        influence = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(groundInfo);
        str.writeInt(woodRequiredPerBuild);
        str.writeInt(timeToBuild);
        str.writeInt(scaffoldsRequired);
        str.writeInt(maxVillagerNeededToBuild);
        str.writeFloat(desireToBeBuilt);
        str.writeFloat(desireToBeRepaired);
        str.writeFloat(influence);
    }
}
