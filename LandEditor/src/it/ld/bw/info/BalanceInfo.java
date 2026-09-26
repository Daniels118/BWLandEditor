package it.ld.bw.info;

public enum BalanceInfo {
	TOWN_BOREDOM,
	IMPRESSIVE,
	IMPRESSIVE_SPELL,
	MISSIONARY,
	VILLAGER_SPEED,
	TREE_WOOD_VALUE,
	ARTIFACT_SPEED,
	VILLAGER_BELIEF_SPEED;
	
	private static BalanceInfo[] values = values();
	
	public static BalanceInfo valueOf(int ord) {
		return ord >= 0 && ord < values.length ? values[ord] : null;
	}
}
