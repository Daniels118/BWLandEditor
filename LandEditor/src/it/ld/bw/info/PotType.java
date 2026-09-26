package it.ld.bw.info;

public enum PotType {
	POT,
	PILE_FOOD,
	PILE_WOOD;
	
	public static PotType valueOf(int ordinal) {
		if (ordinal == -1) return null;
		return values()[ordinal];
	}
	
	public static int ordinal(PotType value) {
		if (value == null) return -1;
		return value.ordinal();
	}
}
