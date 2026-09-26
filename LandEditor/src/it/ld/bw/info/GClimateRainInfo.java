/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GClimateRainInfo extends Struct {
    public float january;
    public float february;
    public float march;
    public float april;
    public float may;
    public float june;
    public float july;
    public float august;
    public float september;
    public float october;
    public float november;
    public float december;
    public float midnight;
    public float oneOClock;
    public float twoOClock;
    public float threeOClock;
    public float fourOClock;
    public float fiveOClock;
    public float sixOClock;
    public float sevenOClock;
    public float heightOClock;
    public float nineOClock;
    public float tenOClock;
    public float elevenOClock;
    public float midday;
    public float thirdtenOClock;
    public float fourtenOClock;
    public float fivetenOClock;
    public float sixtenOClock;
    public float seventenOClock;
    public float heighttenOClock;
    public float ninetenOClock;
    public float twentyOClock;
    public float twentyoneOClock;
    public float twentytwoOClock;
    public float twentitreeOClock;
    public float natureRainDesire;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        january = str.readFloat();
        february = str.readFloat();
        march = str.readFloat();
        april = str.readFloat();
        may = str.readFloat();
        june = str.readFloat();
        july = str.readFloat();
        august = str.readFloat();
        september = str.readFloat();
        october = str.readFloat();
        november = str.readFloat();
        december = str.readFloat();
        midnight = str.readFloat();
        oneOClock = str.readFloat();
        twoOClock = str.readFloat();
        threeOClock = str.readFloat();
        fourOClock = str.readFloat();
        fiveOClock = str.readFloat();
        sixOClock = str.readFloat();
        sevenOClock = str.readFloat();
        heightOClock = str.readFloat();
        nineOClock = str.readFloat();
        tenOClock = str.readFloat();
        elevenOClock = str.readFloat();
        midday = str.readFloat();
        thirdtenOClock = str.readFloat();
        fourtenOClock = str.readFloat();
        fivetenOClock = str.readFloat();
        sixtenOClock = str.readFloat();
        seventenOClock = str.readFloat();
        heighttenOClock = str.readFloat();
        ninetenOClock = str.readFloat();
        twentyOClock = str.readFloat();
        twentyoneOClock = str.readFloat();
        twentytwoOClock = str.readFloat();
        twentitreeOClock = str.readFloat();
        natureRainDesire = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        str.writeFloat(january);
        str.writeFloat(february);
        str.writeFloat(march);
        str.writeFloat(april);
        str.writeFloat(may);
        str.writeFloat(june);
        str.writeFloat(july);
        str.writeFloat(august);
        str.writeFloat(september);
        str.writeFloat(october);
        str.writeFloat(november);
        str.writeFloat(december);
        str.writeFloat(midnight);
        str.writeFloat(oneOClock);
        str.writeFloat(twoOClock);
        str.writeFloat(threeOClock);
        str.writeFloat(fourOClock);
        str.writeFloat(fiveOClock);
        str.writeFloat(sixOClock);
        str.writeFloat(sevenOClock);
        str.writeFloat(heightOClock);
        str.writeFloat(nineOClock);
        str.writeFloat(tenOClock);
        str.writeFloat(elevenOClock);
        str.writeFloat(midday);
        str.writeFloat(thirdtenOClock);
        str.writeFloat(fourtenOClock);
        str.writeFloat(fivetenOClock);
        str.writeFloat(sixtenOClock);
        str.writeFloat(seventenOClock);
        str.writeFloat(heighttenOClock);
        str.writeFloat(ninetenOClock);
        str.writeFloat(twentyOClock);
        str.writeFloat(twentyoneOClock);
        str.writeFloat(twentytwoOClock);
        str.writeFloat(twentitreeOClock);
        str.writeFloat(natureRainDesire);
    }
}
