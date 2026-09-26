/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GSoundInfo extends Struct {
    public float weatherPercentageForMaxFade;
    public float weatherPercentageForMaxWeatherVolume;
    public float radiusForMaxAtmosVolume;
    public float radiusForMinAtmosVolume;
    public float fadeOutStart;
    public float normalAtmosFadeStartHeight;
    public float normalAtmosFadeEndHeight;
    public float atmosphereHeight;
    public float atmosphereMaxVolHeight;
    public float spaceHeight;
    public float atmosVolumeStep;
    public float townTriggerDistance;
    public float townTriggerOffDistance;
    public float noBlocksForFullAtmos;
    public float distanceFromNearestAtmosForFadeStart;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        weatherPercentageForMaxFade = str.readFloat();
        weatherPercentageForMaxWeatherVolume = str.readFloat();
        radiusForMaxAtmosVolume = str.readFloat();
        radiusForMinAtmosVolume = str.readFloat();
        fadeOutStart = str.readFloat();
        normalAtmosFadeStartHeight = str.readFloat();
        normalAtmosFadeEndHeight = str.readFloat();
        atmosphereHeight = str.readFloat();
        atmosphereMaxVolHeight = str.readFloat();
        spaceHeight = str.readFloat();
        atmosVolumeStep = str.readFloat();
        townTriggerDistance = str.readFloat();
        townTriggerOffDistance = str.readFloat();
        noBlocksForFullAtmos = str.readFloat();
        distanceFromNearestAtmosForFadeStart = str.readFloat();
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
        little(str);
        str.writeFloat(weatherPercentageForMaxFade);
        str.writeFloat(weatherPercentageForMaxWeatherVolume);
        str.writeFloat(radiusForMaxAtmosVolume);
        str.writeFloat(radiusForMinAtmosVolume);
        str.writeFloat(fadeOutStart);
        str.writeFloat(normalAtmosFadeStartHeight);
        str.writeFloat(normalAtmosFadeEndHeight);
        str.writeFloat(atmosphereHeight);
        str.writeFloat(atmosphereMaxVolHeight);
        str.writeFloat(spaceHeight);
        str.writeFloat(atmosVolumeStep);
        str.writeFloat(townTriggerDistance);
        str.writeFloat(townTriggerOffDistance);
        str.writeFloat(noBlocksForFullAtmos);
        str.writeFloat(distanceFromNearestAtmosForFadeStart);
    }
}
