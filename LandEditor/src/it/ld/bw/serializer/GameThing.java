package it.ld.bw.serializer;

public abstract class GameThing {
	protected int unknown1 = 0;
	protected byte unknown2 = 0;
	
	public void deserialize(GameThingDeserializer ser) throws Exception {
		setUnknown1(ser.readInt());
		setUnknown2(ser.readByte());
	}
	
	public int getUnknown1() {
		return unknown1;
	}
	
	public void setUnknown1(int unknown1) {
		this.unknown1 = unknown1;
	}
	
	public byte getUnknown2() {
		return unknown2;
	}
	
	public void setUnknown2(byte unknown2) {
		this.unknown2 = unknown2;
	}
	
	public abstract String toJsonString(String indent);
}
