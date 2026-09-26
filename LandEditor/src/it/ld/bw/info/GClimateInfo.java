/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GClimateInfo extends Struct {
    public String debugString = "";
    public float rainMinSpring;
    public float rainMinSummer;
    public float rainMinAutumn;
    public float rainMinWinter;
    public float rainMaxSpring;
    public float rainMaxSummer;
    public float rainMaxAutumn;
    public float rainMaxWinter;
    public float tempMinSpring;
    public float tempMinSummer;
    public float tempMinAutumn;
    public float tempMinWinter;
    public float tempMaxSpring;
    public float tempMaxSummer;
    public float tempMaxAutumn;
    public float tempMaxWinter;
    public float windMinSpring;
    public float windMinSummer;
    public float windMinAutumn;
    public float windMinWinter;
    public float windMaxSpring;
    public float windMaxSummer;
    public float windMaxAutumn;
    public float windMaxWinter;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        debugString = readFixedString(str, 48, true);
        rainMinSpring = str.readFloat();
        rainMinSummer = str.readFloat();
        rainMinAutumn = str.readFloat();
        rainMinWinter = str.readFloat();
        rainMaxSpring = str.readFloat();
        rainMaxSummer = str.readFloat();
        rainMaxAutumn = str.readFloat();
        rainMaxWinter = str.readFloat();
        tempMinSpring = str.readFloat();
        tempMinSummer = str.readFloat();
        tempMinAutumn = str.readFloat();
        tempMinWinter = str.readFloat();
        tempMaxSpring = str.readFloat();
        tempMaxSummer = str.readFloat();
        tempMaxAutumn = str.readFloat();
        tempMaxWinter = str.readFloat();
        windMinSpring = str.readFloat();
        windMinSummer = str.readFloat();
        windMinAutumn = str.readFloat();
        windMinWinter = str.readFloat();
        windMaxSpring = str.readFloat();
        windMaxSummer = str.readFloat();
        windMaxAutumn = str.readFloat();
        windMaxWinter = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        writeFixedString(str, debugString, 48);
        str.writeFloat(rainMinSpring);
        str.writeFloat(rainMinSummer);
        str.writeFloat(rainMinAutumn);
        str.writeFloat(rainMinWinter);
        str.writeFloat(rainMaxSpring);
        str.writeFloat(rainMaxSummer);
        str.writeFloat(rainMaxAutumn);
        str.writeFloat(rainMaxWinter);
        str.writeFloat(tempMinSpring);
        str.writeFloat(tempMinSummer);
        str.writeFloat(tempMinAutumn);
        str.writeFloat(tempMinWinter);
        str.writeFloat(tempMaxSpring);
        str.writeFloat(tempMaxSummer);
        str.writeFloat(tempMaxAutumn);
        str.writeFloat(tempMaxWinter);
        str.writeFloat(windMinSpring);
        str.writeFloat(windMinSummer);
        str.writeFloat(windMinAutumn);
        str.writeFloat(windMinWinter);
        str.writeFloat(windMaxSpring);
        str.writeFloat(windMaxSummer);
        str.writeFloat(windMaxAutumn);
        str.writeFloat(windMaxWinter);
    }
    
    @Override
    public String toString() {
    	return this.getClass().getSimpleName() + "(" + debugString + ")";
    }
}
