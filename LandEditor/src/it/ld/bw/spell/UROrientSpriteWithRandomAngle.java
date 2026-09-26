package it.ld.bw.spell;

public class UROrientSpriteWithRandomAngle extends BaseObject {
	public String condition;
	public float defaultAngle;
	public int group;
	public float randomAngle;
	public boolean removeOnCloseDown;
	
	public UROrientSpriteWithRandomAngle(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("Condition".equals(name)) {
			condition = parseString(value);
		} else if ("DefaultAngle".equals(name)) {
			defaultAngle = Float.parseFloat(value);
		} else if ("Group".equals(name)) {
			group = Integer.parseInt(value);
		} else if ("RandomAngle".equals(name)) {
			randomAngle = Float.parseFloat(value);
		} else if ("RemoveOnCloseDown".equals(name)) {
			removeOnCloseDown = "1".equals(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
}
