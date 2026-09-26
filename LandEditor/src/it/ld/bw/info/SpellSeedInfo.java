package it.ld.bw.info;

public enum SpellSeedInfo {
	NONE(-1),
	STORM(0),
	STORM_PU1(0, 1),
	STORM_PU2(0, 2),
	NATURE(1),
	FIRE(2),
	FIRE_PU1(2, 1),
	FIRE_PU2(2, 2),
	FOOD(3),
	FOOD_PU1(3, 1),
	SHIELD(4),
	PHYSICAL_SHIELD(5),
	LIGHTNING_BOLT(6),
	LIGHTNING_BOLT_PU1(6, 1),
	LIGHTNING_BOLT_PU2(6, 2),
	HEAL(7),
	HEAL_PU1(7, 1),
	WOOD(8),
	WATER(9),
	WATER_PU1(9, 1),
	FLYING_FLOCK(10),
	GROUND_FLOCK(11),
	CREATURE_SPELL_FREEZE(12),
	CREATURE_SPELL_SMALL(13),
	CREATURE_SPELL_BIG(14),
	CREATURE_SPELL_WEAK(15),
	CREATURE_SPELL_STRONG(16),
	CREATURE_SPELL_FAT(17),
	CREATURE_SPELL_THIN(18),
	CREATURE_SPELL_INVISIBLE(19),
	CREATURE_SPELL_COMPASSION(20),
	CREATURE_SPELL_ANGRY(21),
	CREATURE_SPELL_HUNGRY(22),
	CREATURE_SPELL_FRIGHTENED(23),
	CREATURE_SPELL_TIRED(24),
	CREATURE_SPELL_ILL(25),
	CREATURE_SPELL_THIRSTY(26),
	CREATURE_SPELL_ITCHY(27),
	TELEPORT(28),
	BEAM_EXPLOSION(29),
	BEAM_EXPLOSION_PU1(29, 1),
	BEAM_EXPLOSION_PU2(29, 2),
	CREATURE_SPELL_ANTI_SPELL(30),	//CI only
	CREATURE_SPELL_FAST(31);	//CI only
	
	private static final int BASE_COUNT = 32;
	private static SpellSeedInfo[][] lut;
	
	public final int id;
	public final int pu;
	
	private SpellSeedInfo(int id) {
		this(id, 0);
	}
	
	private SpellSeedInfo(int id, int pu) {
		this.id = id;
		this.pu = pu;
	}
	
	private static void initLUT() {
		if (lut == null) {
			lut = new SpellSeedInfo[BASE_COUNT][];
			for (SpellSeedInfo info : values()) {
				SpellSeedInfo[] entry = lut[info.id];
				if (entry == null) {
					entry = new SpellSeedInfo[1];
					lut[info.id] = entry;
				} else if (info.pu >= entry.length) {
					SpellSeedInfo[] t = new SpellSeedInfo[info.pu + 1];
					System.arraycopy(entry, 0, t, 0, entry.length);
					entry = t;
					lut[info.id] = entry;
				}
				entry[info.pu] = info;
			}
		}
	}
	
	public static SpellSeedInfo find(int id, int pu) {
		initLUT();
		return lut[id][pu];
	}
}
