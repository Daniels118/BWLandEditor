package it.ld.bw.spell;

public class RemoveRuleOldAgeOnly extends BaseObject {
	public String condition;
	public float dieAge;
	public int group;
	public int minAtoms;
	public boolean removeOnCloseDown;
	
	public RemoveRuleOldAgeOnly(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("Condition".equals(name)) {
			condition = parseString(value);
		} else if ("DieAge".equals(name)) {
			dieAge = Float.parseFloat(value);
		} else if ("Group".equals(name)) {
			group = Integer.parseInt(value);
		} else if ("MinAtoms".equals(name)) {
			minAtoms = Integer.parseInt(value);
		} else if ("RemoveOnCloseDown".equals(name)) {
			removeOnCloseDown = "1".equals(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
}
