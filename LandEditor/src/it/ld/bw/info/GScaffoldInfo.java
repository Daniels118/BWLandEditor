/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GScaffoldInfo extends GMobileObjectInfo {
    public float phantomBuildingRotationPerGameTurn;
    public int gameTurnsAfterPlacingCanStillPickUp;
    public float maxDistanceForImpressingTowns;
    public float proportionOfTownToImpress;
    public short maxNumberForCombining;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        phantomBuildingRotationPerGameTurn = str.readFloat();
        gameTurnsAfterPlacingCanStillPickUp = str.readInt();
        maxDistanceForImpressingTowns = str.readFloat();
        proportionOfTownToImpress = str.readFloat();
        maxNumberForCombining = str.readShort();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeFloat(phantomBuildingRotationPerGameTurn);
        str.writeInt(gameTurnsAfterPlacingCanStillPickUp);
        str.writeFloat(maxDistanceForImpressingTowns);
        str.writeFloat(proportionOfTownToImpress);
        str.writeShort(maxNumberForCombining);
    }
}
