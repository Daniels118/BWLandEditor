/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GWorshipSiteInfo extends GCitadelPartInfo {
    public float radiusFromCitadel;
    public int baseMesh;
    public int potForResourceFood;
    public int potForResourceWood;
    public float chantsPerVillager;
    public float prayerSiteDistance;
    public int maxDancersVisible;
    public float chantsToFillBattery;
    public float eachVillagerAddToFillBattery;
    public int chantsToReserveForMaintaining;
    public float artifactPowerupMultiplier;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        radiusFromCitadel = str.readFloat();
        baseMesh = str.readInt();
        potForResourceFood = str.readInt();
        potForResourceWood = str.readInt();
        chantsPerVillager = str.readFloat();
        prayerSiteDistance = str.readFloat();
        maxDancersVisible = str.readInt();
        chantsToFillBattery = str.readFloat();
        eachVillagerAddToFillBattery = str.readFloat();
        chantsToReserveForMaintaining = str.readInt();
        artifactPowerupMultiplier = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeFloat(radiusFromCitadel);
        str.writeInt(baseMesh);
        str.writeInt(potForResourceFood);
        str.writeInt(potForResourceWood);
        str.writeFloat(chantsPerVillager);
        str.writeFloat(prayerSiteDistance);
        str.writeInt(maxDancersVisible);
        str.writeFloat(chantsToFillBattery);
        str.writeFloat(eachVillagerAddToFillBattery);
        str.writeInt(chantsToReserveForMaintaining);
        str.writeFloat(artifactPowerupMultiplier);
    }
}
