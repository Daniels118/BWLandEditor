package it.ld.bw.info;

public enum PotInfo {
	FOOD_POT,
	WOOD_POT,
	STORAGE_PIT_FOOD_PILE,
	WOOD_PILE_1,
	WOOD_PILE_2,
	WOOD_PILE_3,
	WOOD_PILE_4,
	WOOD_PILE_5,
	FOOD_PILE,
	MAGIC_WOOD,
	MAGIC_FOOD,
	HAND_WOOD,
	HAND_FOOD,
	TREE_BRANCH_EVERGREEN_IN_HAND,
	TREE_BRANCH_HARDWOOD_IN_HAND,
	TREE_FRUIT_IN_HAND,
	TREE_LOGS_IN_HAND,
	WHEAT_IN_HAND,
	FISH,
	LAST;
	
	public static PotInfo valueOf(int ordinal) {
		if (ordinal == -1) return null;
		return values()[ordinal];
	}
	
	public static int ordinal(PotInfo value) {
		if (value == null) return -1;
		return value.ordinal();
	}
}
