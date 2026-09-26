package it.ld.bw.spell;

public class SetScale extends BaseObject {
	public String condition;
	public int group;
	public boolean removeOnCloseDown;
	private String scale;
	
	public SetScale(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("Condition".equals(name)) {
			condition = parseString(value);
		} else if ("Group".equals(name)) {
			group = Integer.parseInt(value);
		} else if ("RemoveOnCloseDown".equals(name)) {
			removeOnCloseDown = "1".equals(value);
		} else if ("Scale".equals(name)) {
			scale = value;
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
	
	public BaseObject getScale() {
		return spellFile.getObject(scale);
	}
}
