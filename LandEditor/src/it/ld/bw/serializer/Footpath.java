package it.ld.bw.serializer;

import java.util.ArrayList;
import java.util.List;

public class Footpath extends GameThing {
	private ArrayList<FootpathNode> nodes;
	private int unknown = 1;
	
	@Override
	public void deserialize(GameThingDeserializer str) throws Exception {
		super.deserialize(str);
		nodes = str.deserializeList(FootpathNode.class, GameThingType.FootpathNode);
		setUnknown(str.readInt());
	}
	
	public List<FootpathNode> getNodes() {
		return nodes;
	}
	
	public int getUnknown() {
		return unknown;
	}
	
	public void setUnknown(int unknown) {
		this.unknown = unknown;
	}
	
	@Override
	public String toString() {
		if (nodes.isEmpty()) return super.toString();
		return "(" + nodes.get(0).toString() + " -> " + nodes.get(nodes.size() - 1).toString() + ")";
	}
	
	@Override
	public String toJsonString(String indent) {
		String[] s1 = new String[nodes.size()];
		for (int i = 0; i < s1.length; i++) {
			s1[i] = nodes.get(i).toJsonString(indent + "    ");
		}
		return indent+"{\n"+
				indent+"  \"unknown1\": "+unknown1+",\n"+
				indent+"  \"unknown2\": "+unknown2+",\n"+
				indent+"  \"unknown\": "+unknown+",\n"+
				indent+"  \"nodes\": [\n"+String.join(",\n", s1)+"\n"+
				indent+"  ]\n"+
				indent+"}";
	}
}
