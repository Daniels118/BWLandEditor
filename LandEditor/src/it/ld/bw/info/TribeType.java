package it.ld.bw.info;

public enum TribeType {
	CELTIC,
	AFRICAN,
	AZTEC,
	JAPANESE,
	INDIAN,
	EGYPTIAN,
	GREEK,
	NORSE,
	TIBETAN;
	
	public static TribeType valueOf(int ordinal) {
		if (ordinal == -1) return null;
		return values()[ordinal];
	}
	
	public static int ordinal(TribeType value) {
		if (value == null) return -1;
		return value.ordinal();
	}
}
