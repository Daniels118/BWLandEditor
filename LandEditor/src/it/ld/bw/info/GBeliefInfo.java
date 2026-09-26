/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GBeliefInfo extends Struct {
    public float townDesireBeliefThresholds;
    public float claimedTownBeliefMultiplier;
    public float lostATownBeliefInPlayerMultiplier;
    public float minimumThreshold;
    public float defaultBoredomOfMe;
    public float beliefLeftWhenHelpSpritesWarn;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        townDesireBeliefThresholds = str.readFloat();
        claimedTownBeliefMultiplier = str.readFloat();
        lostATownBeliefInPlayerMultiplier = str.readFloat();
        minimumThreshold = str.readFloat();
        defaultBoredomOfMe = str.readFloat();
        beliefLeftWhenHelpSpritesWarn = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        str.writeFloat(townDesireBeliefThresholds);
        str.writeFloat(claimedTownBeliefMultiplier);
        str.writeFloat(lostATownBeliefInPlayerMultiplier);
        str.writeFloat(minimumThreshold);
        str.writeFloat(defaultBoredomOfMe);
        str.writeFloat(beliefLeftWhenHelpSpritesWarn);
    }
}
