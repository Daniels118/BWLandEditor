/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GCitadelInfo extends GContainerInfo {
    public int heartInfo;
    public int[] pensStart = new int[2];
    public int pensMaxSpiral;
    public int[] worshipSiteStart = new int[2];
    public int maxNo;
    public int maxDistanceOfTownFromCitadelOfPygmyTown;
    public int pygmyTownMaxSpiral;
    public int housesInPygmyTown;
    public int importanceOfBeingFarAwayFromOtherTowns;
    public int prayerSiteAngle;
    public float prayerSiteDistance;
    public float virtualInfluenceMaxDistance;
    public int virtualInfluenceMaxGameTicks;
    public float virtualInfluenceChantsToDouble;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        heartInfo = str.readInt();
        pensStart = readIntArray(str, 2);
        pensMaxSpiral = str.readInt();
        worshipSiteStart = readIntArray(str, 2);
        maxNo = str.readInt();
        maxDistanceOfTownFromCitadelOfPygmyTown = str.readInt();
        pygmyTownMaxSpiral = str.readInt();
        housesInPygmyTown = str.readInt();
        importanceOfBeingFarAwayFromOtherTowns = str.readInt();
        prayerSiteAngle = str.readInt();
        prayerSiteDistance = str.readFloat();
        virtualInfluenceMaxDistance = str.readFloat();
        virtualInfluenceMaxGameTicks = str.readInt();
        virtualInfluenceChantsToDouble = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(heartInfo);
        writeIntArray(str, pensStart, 2);
        str.writeInt(pensMaxSpiral);
        writeIntArray(str, worshipSiteStart, 2);
        str.writeInt(maxNo);
        str.writeInt(maxDistanceOfTownFromCitadelOfPygmyTown);
        str.writeInt(pygmyTownMaxSpiral);
        str.writeInt(housesInPygmyTown);
        str.writeInt(importanceOfBeingFarAwayFromOtherTowns);
        str.writeInt(prayerSiteAngle);
        str.writeFloat(prayerSiteDistance);
        str.writeFloat(virtualInfluenceMaxDistance);
        str.writeInt(virtualInfluenceMaxGameTicks);
        str.writeFloat(virtualInfluenceChantsToDouble);
    }
}
