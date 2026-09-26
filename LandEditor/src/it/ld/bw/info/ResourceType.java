package it.ld.bw.info;

public enum ResourceType {
	ANY(-2),
	NONE(-1),
	FOOD(0),
	WOOD(1);
	
	public final int code;
	
	private ResourceType(int code) {
		this.code = code;
	}
	
	public static ResourceType valueOf(int code) {
		return values()[code + 2];
	}
}
