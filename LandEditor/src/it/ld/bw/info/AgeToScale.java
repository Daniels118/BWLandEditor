/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class AgeToScale extends Struct {
    public float[] values = new float[20];

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        values = readFloatArray(str, values);
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        writeFloatArray(str, values);
    }
}
