/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class SpeedGroup extends Struct {
    public int speedDefault;
    public int speedFleeing;
    public int speed2;
    public int speed3;
    public int speed4;
    public int speed5;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        speedDefault = str.readInt();
        speedFleeing = str.readInt();
        speed2 = str.readInt();
        speed3 = str.readInt();
        speed4 = str.readInt();
        speed5 = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        str.writeInt(speedDefault);
        str.writeInt(speedFleeing);
        str.writeInt(speed2);
        str.writeInt(speed3);
        str.writeInt(speed4);
        str.writeInt(speed5);
    }
}
