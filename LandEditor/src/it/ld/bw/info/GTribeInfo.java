/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GTribeInfo extends Struct {
    public int tribeType;
    public int worshipSiteInfo;
    public float percentFemaleInTown;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        tribeType = str.readInt();
        worshipSiteInfo = str.readInt();
        percentFemaleInTown = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        str.writeInt(tribeType);
        str.writeInt(worshipSiteInfo);
        str.writeFloat(percentFemaleInTown);
    }
}
