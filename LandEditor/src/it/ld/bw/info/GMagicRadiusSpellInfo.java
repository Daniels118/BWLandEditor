/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GMagicRadiusSpellInfo extends GMagicInfo {
    public float minRadius;
    public float maxRadius;
    public float radiusForNormalCost;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        minRadius = str.readFloat();
        maxRadius = str.readFloat();
        radiusForNormalCost = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeFloat(minRadius);
        str.writeFloat(maxRadius);
        str.writeFloat(radiusForNormalCost);
    }
}
