/* Copyright (c) 2026 Daniele Lombardi / Daniels118 */
package it.ld.bw.info;

import java.io.IOException;
import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;
import it.ld.utils.Struct;

public class GMagicInfo extends Struct {
    public int magicType;
    public int immersion;
    public int stopImmersion;
    public float perceivedPower;
    public ParticleType particleType;
    public int impressiveType;
    public int spellSeedType;
    public int gestureType;
    public int powerupType;
    public int castRuleType;
    public int isSpellSeedDrawnInHand;
    public int isSpellRecharged;
    public int isCreatureCastFromAbove;
    public int oneOffSpellIsPlayful;
    public int oneOffSpellIsAggressive;
    public int oneOffSpellIsCompassionate;
    public int oneOffSpellIsToRestoreHealth;
    public ParticleType particleTypeInHand;

    @Override
    public void read(EndianDataInputStream str) throws IOException {
        little(str);
        magicType = str.readInt();
        immersion = str.readInt();
        stopImmersion = str.readInt();
        perceivedPower = str.readFloat();
        particleType = ParticleType.values()[str.readInt()];
        impressiveType = str.readInt();
        spellSeedType = str.readInt();
        gestureType = str.readInt();
        powerupType = str.readInt();
        castRuleType = str.readInt();
        isSpellSeedDrawnInHand = str.readInt();
        isSpellRecharged = str.readInt();
        isCreatureCastFromAbove = str.readInt();
        oneOffSpellIsPlayful = str.readInt();
        oneOffSpellIsAggressive = str.readInt();
        oneOffSpellIsCompassionate = str.readInt();
        oneOffSpellIsToRestoreHealth = str.readInt();
        particleTypeInHand = ParticleType.values()[str.readInt()];
    }

    @Override
    public void write(EndianDataOutputStream str) throws IOException {
    	throw new RuntimeException("Method not implemented");
    }
}
