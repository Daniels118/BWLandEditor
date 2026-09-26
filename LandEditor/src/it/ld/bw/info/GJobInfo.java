/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GJobInfo extends Struct {
    public int job;
    public int restAtHomeAfterWork;
    public int subVisitsAtWhileWork;
    public int timeAtEachSubVisit;
    public int maxGameTurnsToSpendAtJob;
    public float minResourceToGet;
    public int spring;
    public int summer;
    public int autumn;
    public int winter;
    public int bestResourceCollectedPerSubVisit;
    public int inJobSpiralArea;
    public int nutureValue;
    public float lookForJobLocationMaxDistance;
    public int maxNoLookForsPerTurn;
    public int nurtureEffect;
    public int harvestEffect;
    public int satisfyWhichTownDesire;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        job = str.readInt();
        restAtHomeAfterWork = str.readInt();
        subVisitsAtWhileWork = str.readInt();
        timeAtEachSubVisit = str.readInt();
        maxGameTurnsToSpendAtJob = str.readInt();
        minResourceToGet = str.readFloat();
        spring = str.readInt();
        summer = str.readInt();
        autumn = str.readInt();
        winter = str.readInt();
        bestResourceCollectedPerSubVisit = str.readInt();
        inJobSpiralArea = str.readInt();
        nutureValue = str.readInt();
        lookForJobLocationMaxDistance = str.readFloat();
        maxNoLookForsPerTurn = str.readInt();
        nurtureEffect = str.readInt();
        harvestEffect = str.readInt();
        satisfyWhichTownDesire = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        str.writeInt(job);
        str.writeInt(restAtHomeAfterWork);
        str.writeInt(subVisitsAtWhileWork);
        str.writeInt(timeAtEachSubVisit);
        str.writeInt(maxGameTurnsToSpendAtJob);
        str.writeFloat(minResourceToGet);
        str.writeInt(spring);
        str.writeInt(summer);
        str.writeInt(autumn);
        str.writeInt(winter);
        str.writeInt(bestResourceCollectedPerSubVisit);
        str.writeInt(inJobSpiralArea);
        str.writeInt(nutureValue);
        str.writeFloat(lookForJobLocationMaxDistance);
        str.writeInt(maxNoLookForsPerTurn);
        str.writeInt(nurtureEffect);
        str.writeInt(harvestEffect);
        str.writeInt(satisfyWhichTownDesire);
    }
}
