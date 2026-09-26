package it.ld.bw.info;

public enum TreeInfo {
	BEECH,
	BIRCH,
	CEDAR,
	CONIFER,
	CONIFER_A,
	OAK,
	OAK_A,
	OLIVE,
	PALM,
	PALM_A,
	PALM_B,
	PALM_C,
	PINE,
	BUSH,
	BUSH_A,
	BUSH_B,
	CYPRESS,
	CYPRESS_A,
	COPSE,
	COPSE_A,
	HEDGE,
	HEDGE_A,
	BURNT;
	
	public static TreeInfo valueOf(int ordinal) {
		if (ordinal == -1) return null;
		return values()[ordinal];
	}
	
	public static int ordinal(TreeInfo value) {
		if (value == null) return -1;
		return value.ordinal();
	}
}
