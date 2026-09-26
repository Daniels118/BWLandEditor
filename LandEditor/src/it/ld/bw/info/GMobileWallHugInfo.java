/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GMobileWallHugInfo extends GMobileInfo {
    public SpeedGroup speedGroup = new SpeedGroup();
    public int collideType;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        speedGroup.read(str);
        collideType = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        speedGroup.write(str);
        str.writeInt(collideType);
    }
}
