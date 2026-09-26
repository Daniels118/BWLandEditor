package it.ld.bw.spell;

public class EventConditionTrueOnCloseDown extends BaseObject {
	public boolean invertResponse;
	
	public EventConditionTrueOnCloseDown(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("InvertResponse".equals(name)) {
			invertResponse = "1".equals(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
}
