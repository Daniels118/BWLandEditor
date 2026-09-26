/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GMagicResourceInfo extends GMagicInfo {
    public int resourceType;
    public int resourceAmountFirstEvent;
    public int resourceAmountPerEvent;
    public int costPerUnit;
    public int poisoned;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        resourceType = str.readInt();
        resourceAmountFirstEvent = str.readInt();
        resourceAmountPerEvent = str.readInt();
        costPerUnit = str.readInt();
        poisoned = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(resourceType);
        str.writeInt(resourceAmountFirstEvent);
        str.writeInt(resourceAmountPerEvent);
        str.writeInt(costPerUnit);
        str.writeInt(poisoned);
    }
}
