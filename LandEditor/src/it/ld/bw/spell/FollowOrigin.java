package it.ld.bw.spell;

public class FollowOrigin extends BaseObject {
	public int group;
	
	public FollowOrigin(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("Group".equals(name)) {
			group = Integer.parseInt(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
}
