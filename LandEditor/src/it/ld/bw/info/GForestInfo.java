/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GForestInfo extends GContainerInfo {
    public int defaultNoTrees;
    public int helpStartEnum;
    public int collideSound;
    public int immersion;
    public int helpEndEnum;
    public int helpMessage;
    public int helpCondition;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        defaultNoTrees = str.readInt();
        helpStartEnum = str.readInt();
        collideSound = str.readInt();
        immersion = str.readInt();
        helpEndEnum = str.readInt();
        helpMessage = str.readInt();
        helpCondition = str.readInt();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        str.writeInt(defaultNoTrees);
        str.writeInt(helpStartEnum);
        str.writeInt(collideSound);
        str.writeInt(immersion);
        str.writeInt(helpEndEnum);
        str.writeInt(helpMessage);
        str.writeInt(helpCondition);
    }
}
