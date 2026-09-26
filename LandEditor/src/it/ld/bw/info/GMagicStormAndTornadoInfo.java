/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GMagicStormAndTornadoInfo extends GMagicRadiusSpellInfo {
    public float maxWindSpeed;
    public float rainAmount;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        maxWindSpeed = str.readFloat();
        rainAmount = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeFloat(maxWindSpeed);
        str.writeFloat(rainAmount);
    }
}
