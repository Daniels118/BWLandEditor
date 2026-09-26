/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GCreatureInfo extends GLivingInfo {
    private boolean ci;
	
	public byte[] field0x1e4 = new byte[416];
	
	public int unknown;	//CI only
    
    public GCreatureInfo(boolean ci) {
		this.ci = ci;
	}

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        super.read(str);
        field0x1e4 = readByteArray(str, 416);
        if (ci) {
			unknown = str.readInt();
		}
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        super.write(str);
        writeByteArray(str, field0x1e4, 416);
        if (ci) {
			str.writeInt(unknown);
		}
    }
    
    public static GCreatureInfo newVanilla() {
		return new GCreatureInfo(false);
	}
	
	public static GCreatureInfo newCI() {
		return new GCreatureInfo(true);
	}
}
