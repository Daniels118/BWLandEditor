package it.ld.bw.serializer;

public class FootpathNode extends GameThing {
	private MapCoords coords;
	private byte unknown;
	
	public void deserialize(GameThingDeserializer str) throws Exception {
		super.deserialize(str);
		coords = str.readCoords();
		setUnknown(str.readByte());
	}
	
	public MapCoords getCoords() {
		return coords;
	}
	
	public byte getUnknown() {
		return unknown;
	}
	
	public void setUnknown(byte unknown) {
		this.unknown = unknown;
	}
	
	@Override
	public String toString() {
		if (coords == null) return super.toString();
		return coords.toString();
	}
	
	@Override
	public String toJsonString(String indent) {
		return indent+"{\n"+
				indent+"  \"unknown1\": "+unknown1+",\n"+
				indent+"  \"unknown2\": "+unknown2+",\n"+
				indent+"  \"coords\": "+coords.toJsonString("")+",\n"+
				indent+"  \"unknown\": "+unknown+"\n"+
				indent+"}";
	}
}
