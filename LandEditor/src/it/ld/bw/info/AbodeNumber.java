package it.ld.bw.info;

public enum AbodeNumber {
	A,
	B,
	C,
	D,
	E,
	F,
	TOTEM,
	STORAGE_PIT,
	CRECHE,
	WORKSHOP,
	WONDER,
	GRAVEYARD,
	TOWN_CENTRE,
	FOOTBALL_PITCH,
	SPELL_DISPENSER,
	FIELD;
	
	public static AbodeNumber valueOf(int ordinal) {
		if (ordinal == -1) return null;
		return values()[ordinal];
	}
	
	public static int ordinal(AbodeNumber value) {
		if (value == null) return -1;
		return value.ordinal();
	}
}
