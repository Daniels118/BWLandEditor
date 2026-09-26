/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GSpeedThreshold extends Struct {
    public int speedMaxWalk;
    public int speedMaxRun;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        speedMaxWalk = str.readInt();
        speedMaxRun = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        str.writeInt(speedMaxWalk);
        str.writeInt(speedMaxRun);
    }
}
