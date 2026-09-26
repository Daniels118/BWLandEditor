package it.ld.bw.serializer;

import java.util.ArrayList;
import java.util.List;

public class FootpathLink extends GameThing {
	private ArrayList<Footpath> footpaths;
	
	public void deserialize(GameThingDeserializer str) throws Exception {
		super.deserialize(str);
		footpaths = str.deserializeList(Footpath.class, GameThingType.Footpath);
	}
	
	public List<Footpath> getFootpaths() {
		return footpaths;
	}
	
	@Override
	public String toString() {
		return String.valueOf(footpaths);
	}
	
	@Override
	public String toJsonString(String indent) {
		String[] s1 = new String[footpaths.size()];
		for (int i = 0; i < s1.length; i++) {
			s1[i] = footpaths.get(i).toJsonString(indent + "    ");
		}
		return indent+"{\n"+
				indent+"  \"unknown1\": "+unknown1+",\n"+
				indent+"  \"unknown2\": "+unknown2+",\n"+
				indent+"  \"footpaths\": [\n"+String.join(",\n", s1)+"\n"+
				indent+"  ]\n"+
				indent+"}";
	}
}
