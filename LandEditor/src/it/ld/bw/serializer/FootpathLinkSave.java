package it.ld.bw.serializer;

public class FootpathLinkSave extends GameThing {
	private MapCoords coords;
	/**If link is null, then coords is the position of an obstacle (i.e. fences, rocks, etc.),
	 * otherwise coords is the position of the center of a building, and link contains references to all the footpaths
	 * which lead to the entrance door of that building.
	 * For crop fields, both the footpath endpoint and the entrance position match with the center of the field.
	 * For fishing farms, both the footpath endpoint and the entrance position are placed on the shoreline.
	 */
	private FootpathLink link;
	
	public void deserialize(GameThingDeserializer str) throws Exception {
		super.deserialize(str);
		coords = str.readCoords();
		link = str.deserializeOne(FootpathLink.class, GameThingType.FootpathLink);
	}
	
	public MapCoords getCoords() {
		return coords;
	}
	
	public FootpathLink getLink() {
		return link;
	}
	
	@Override
	public String toString() {
		if (coords == null) return super.toString();
		return coords.toString() + "->" + String.valueOf(link);
	}
	
	@Override
	public String toJsonString(String indent) {
		String s1 = link == null ? "null" : link.toJsonString(indent);
		return indent+"{\n"+
				indent+"  \"unknown1\": "+unknown1+",\n"+
				indent+"  \"unknown2\": "+unknown2+",\n"+
				indent+"  \"coords\": "+coords.toJsonString("")+",\n"+
				indent+"  \"link\": "+s1+"\n"+
				indent+"}";
	}
}
