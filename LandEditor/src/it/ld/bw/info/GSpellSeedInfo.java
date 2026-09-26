package it.ld.bw.info;

import java.io.IOException;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GSpellSeedInfo extends GObjectInfo {
	private final boolean ci;
	
	public int targetType;	//i:1
	public GestureType quickGesture;	//i:2
	public int field0xf8;	//always 0
	public GestureType castGesture;
	public int field0x100;	//i:5
	public int field0x104;	//i:6
	public boolean castOnCreature;	//0 or 1
	public boolean field0x10c;	//0 for NATURE and TELEPORT, 1 for all others
	public boolean field0x110;	//0 or 1
	public MagicEffectInfo magicEffect;
	public int field0x118;	//i:11
	public int field0x11c;	//i:12
	public int field0x120;	//always 0
	public int powerUpCount;	//i:14
	public int field0x128;	//always 0
	public int field0x12c;	//i:16
	public int meshId;
	public float scaleMin;	//i:18
	public float scaleMax;	//i:19
	public float field0x13c;	//i:20
	public float field0x140;	//i:21
	public float yaw;
	public int field0x148;	//i:23
	public boolean field0x14c;	//0 for WATER, 1 for all others
	public float field0x150;	//i:25
	public float field0x154;	//i:26
	public boolean field0x158;	//0 or 1
	public float field0x15c;	//i:28	always 0.1
	public int field0x160;	//always 1
	public ParticleType particleType;	//i:30
	public boolean useMesh;	//Otherwise use particleType
	public boolean canBeCast;	//Some spells are available only from scripts
	public int field0x170;	//i:33
	public int helpText;	//i:34
	public int field0x178;	//i:35	always 0
	public boolean field0x17c;	//i:36	0 or 1
	
	/* These are the actual values of the above fields for each entry:
	STORM	 1 12 0 4 1 0 0 1 1 16 17 18 0 2 1 0 312 1.0 1.0 0.5 1.0 0.0 2 1 -1.5 0.4 0 0.1 1 58 0 1 2 3753 0 0
	NATURE	 1 16 0 0 2 0 0 0 1 13 0 0 0 0 0 0 309 1.0 1.0 -0.1 1.0 3.14 1 1 -1.5 0.0 0 0.1 1 0 1 1 4 3752 0 0
	FIRE	 1 7 0 0 1 0 0 1 0 1 2 3 0 2 1 0 305 1.0 1.0 0.5 1.0 0.0 2 1 -1.5 0.0 1 0.1 1 47 0 1 1 3757 0 1
	FOOD	 1 3 0 0 0 1 0 1 1 14 15 0 0 1 0 0 534 0.8 0.8 0.7 1.0 0.0 6 1 -1.5 0.0 0 0.1 1 0 1 1 0 3750 0 0
	SHIELD	 1 9 0 4 1 0 0 1 1 19 0 0 0 0 0 0 331 1.0 1.0 0.5 1.0 0.0 2 1 -1.5 0.0 0 0.1 1 66 1 1 3 3754 0 0
	PHYSICAL_SHIELD	 1 11 0 4 1 0 0 1 1 20 0 0 0 0 0 0 331 1.0 1.0 0.5 1.0 0.0 6 1 -1.5 0.0 0 0.1 1 0 1 1 5 3755 0 0
	LIGHTNING_BOLT	 1 10 0 0 0 1 0 1 1 4 5 6 0 2 1 0 311 1.0 1.0 0.5 1.0 0.0 2 1 -1.5 -1.5 0 0.1 1 60 0 1 7 3758 0 1
	HEAL	 1 13 0 0 2 0 0 1 0 10 11 0 0 1 0 0 310 0.3 1.0 0.5 1.0 0.0 2 1 -0.6 0.0 1 0.1 1 51 1 1 6 3756 0 1
	WOOD	 1 6 0 0 0 1 0 1 1 21 0 0 0 0 0 0 329 0.65 0.65 0.7 1.0 0.0 6 1 -1.5 0.0 0 0.1 1 0 1 1 8 3751 0 0
	WATER	 1 20 0 0 0 1 0 1 1 22 23 0 0 1 0 0 534 0.65 0.65 0.5 1.0 0.0 2 0 -1.5 0.0 0 0.1 1 18 0 1 8 3760 0 0
	FLYING_FLOCK	 1 15 0 0 1 0 0 1 0 24 0 0 0 0 0 0 11 2.6 2.8 0.0 0.4 0.0 1 1 -0.3 0.0 1 0.1 1 0 1 1 8 3761 0 0
	GROUND_FLOCK	 1 22 0 0 1 0 0 1 0 25 0 0 0 0 0 0 41 1.6 2.0 0.0 0.4 1.57 1 1 -1.0 0.0 1 0.1 1 0 1 1 8 3762 0 0
	CREATURE_SPELL_FREEZE	 2 8 0 0 2 1 1 1 1 26 0 0 0 0 0 0 539 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 92 1 1 9 3763 0 0
	CREATURE_SPELL_SMALL	 2 12 0 0 2 1 1 1 1 27 0 0 0 0 0 0 546 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 0 1 1 10 3765 0 0
	CREATURE_SPELL_BIG	 2 10 0 0 2 1 1 1 1 28 0 0 0 0 0 0 537 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 0 1 1 11 3766 0 0
	CREATURE_SPELL_WEAK	 2 11 0 0 2 1 1 1 1 29 0 0 0 0 0 0 551 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 0 1 1 12 3771 0 1
	CREATURE_SPELL_STRONG	 2 9 0 0 2 1 1 1 1 30 0 0 0 0 0 0 547 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 0 1 1 13 3770 0 1
	CREATURE_SPELL_FAT	 2 0 0 0 2 1 1 1 1 26 0 0 0 0 0 0 539 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 92 1 0 9 0 0 0
	CREATURE_SPELL_THIN	 2 0 0 0 2 1 1 1 1 26 0 0 0 0 0 0 539 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 92 1 0 9 0 0 0
	CREATURE_SPELL_INVISIBLE	 2 19 0 0 2 1 1 1 1 33 0 0 0 0 0 0 543 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 0 1 1 14 3764 0 0
	CREATURE_SPELL_COMPASSION	 2 13 0 0 2 1 1 1 1 34 0 0 0 0 0 0 545 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 95 1 1 15 3768 0 0
	CREATURE_SPELL_ANGRY	 2 7 0 0 2 1 1 1 1 35 0 0 0 0 0 0 536 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 0 1 1 16 3769 0 0
	CREATURE_SPELL_HUNGRY	 2 0 0 0 2 1 1 1 1 36 0 0 0 0 0 0 539 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 92 1 0 9 0 0 0
	CREATURE_SPELL_FRIGHTENED	 2 0 0 0 2 1 1 1 1 26 0 0 0 0 0 0 539 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 92 1 0 9 0 0 0
	CREATURE_SPELL_TIRED	 2 0 0 0 2 1 1 1 1 26 0 0 0 0 0 0 539 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 92 1 0 9 0 0 0
	CREATURE_SPELL_ILL	 2 0 0 0 2 1 1 1 1 26 0 0 0 0 0 0 539 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 92 1 0 9 0 0 0
	CREATURE_SPELL_THIRSTY	 2 0 0 0 2 1 1 1 1 26 0 0 0 0 0 0 539 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 92 1 0 9 0 0 0
	CREATURE_SPELL_ITCHY	 2 18 0 0 2 1 1 1 1 41 0 0 0 0 0 0 544 0.8 0.8 0.6 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 89 1 1 17 3767 0 0
	TELEPORT	 1 21 0 0 2 0 0 0 0 12 0 0 0 0 0 0 331 0.8 0.8 0.5 1.0 0.0 2 1 -1.5 0.0 1 0.1 1 74 0 1 18 3783 0 0
	BEAM_EXPLOSION	 1 8 0 0 2 0 0 1 0 7 8 9 0 2 1 0 305 0.8 0.8 0.5 1.0 0.0 6 1 -1.5 0.0 1 0.1 1 0 1 1 19 3759 0 0
	*/
	
	public int field0x180;	//CI only
	
	public GSpellSeedInfo(boolean ci) {
		this.ci = ci;
	}
	
	@Override
	public void read(EndianDataInputStream str) throws IOException {
		little(str);
		super.read(str);
		targetType = str.readInt();
		quickGesture = GestureType.values()[str.readInt()];
		field0xf8 = str.readInt();
		castGesture = GestureType.values()[str.readInt()];
		field0x100 = str.readInt();
		field0x104 = str.readInt();
		castOnCreature = readBool32(str);
		field0x10c = readBool32(str);
		field0x110 = readBool32(str);
		magicEffect = MagicEffectInfo.values()[str.readInt()];
		field0x118 = str.readInt();
		field0x11c = str.readInt();
		field0x120 = str.readInt();
		powerUpCount = str.readInt();
		field0x128 = str.readInt();
		field0x12c = str.readInt();
		meshId = str.readInt();
		scaleMin = str.readFloat();
		scaleMax = str.readFloat();
		field0x13c = str.readFloat();
		field0x140 = str.readFloat();
		yaw = str.readFloat();
		field0x148 = str.readInt();
		field0x14c = readBool32(str);
		field0x150 = str.readFloat();
		field0x154 = str.readFloat();
		field0x158 = readBool32(str);
		field0x15c = str.readFloat();
		field0x160 = str.readInt();
		particleType = ParticleType.values()[str.readInt()];
		useMesh = readBool32(str);
		canBeCast = readBool32(str);
		field0x170 = str.readInt();
		helpText = str.readInt();
		field0x178 = str.readInt();
		field0x17c = readBool32(str);
		if (ci) {
			field0x180 = str.readInt();
		}
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}
	
	public static GSpellSeedInfo newVanilla() {
		return new GSpellSeedInfo(false);
	}
	
	public static GSpellSeedInfo newCI() {
		return new GSpellSeedInfo(true);
	}
}
