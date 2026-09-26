package it.ld.bw.spell;

public class RemoveRuleAfterCloseDown extends BaseObject {
	public String condition;
	public float delay;
	public int group;
	public boolean removeOnCloseDown;
	
	public RemoveRuleAfterCloseDown(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("Condition".equals(name)) {
			condition = parseString(value);
		} else if ("Delay".equals(name)) {
			delay = Float.parseFloat(value);
		} else if ("Group".equals(name)) {
			group = Integer.parseInt(value);
		} else if ("RemoveOnCloseDown".equals(name)) {
			removeOnCloseDown = "1".equals(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
}
