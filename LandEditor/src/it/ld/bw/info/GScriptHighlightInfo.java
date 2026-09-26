/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GScriptHighlightInfo extends GSingleMapFixedInfo {
    public int active;
    public ParticleType particleTypeGlints;
    public ParticleType particleTypeActive;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        active = str.readInt();
        particleTypeGlints = ParticleType.values()[str.readInt()];
        particleTypeActive = ParticleType.values()[str.readInt()];
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(active);
        str.writeInt(particleTypeGlints.ordinal());
        str.writeInt(particleTypeActive.ordinal());
    }
}
