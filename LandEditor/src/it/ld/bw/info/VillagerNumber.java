package it.ld.bw.info;

public enum VillagerNumber {
	HOUSEWIFE,
    FORESTER,
    FISHERMAN,
    FARMER,
    SHEPHERD,
    LEADER,
    TRADER;
	
	public static VillagerNumber valueOf(int ordinal) {
		if (ordinal == -1) return null;
		return values()[ordinal];
	}
	
	public static int ordinal(VillagerNumber value) {
		if (value == null) return -1;
		return value.ordinal();
	}
}
