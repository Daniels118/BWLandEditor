package it.ld.bw.spell;

public class CreateRuleAnAtom extends BaseObject {
	public String condition;
	public int group;
	public int[] nextGroups; 
	public float offsetX;
	public float offsetY;
	public float offsetZ;
	private String pCreator;
	public boolean removeOnCloseDown;
	public SoundAction soundOfCreate;
	
	public CreateRuleAnAtom(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("Condition".equals(name)) {
			condition = parseString(value);
		} else if ("Group".equals(name)) {
			group = Integer.parseInt(value);
		} else if ("NextGroups".equals(name)) {
			nextGroups = parseArray(value);
		} else if ("OffsetX".equals(name)) {
			offsetX = Float.parseFloat(value);
		} else if ("OffsetY".equals(name)) {
			offsetY = Float.parseFloat(value);
		} else if ("OffsetZ".equals(name)) {
			offsetZ = Float.parseFloat(value);
		} else if ("PCreator".equals(name)) {
			pCreator = value;
		} else if ("RemoveOnCloseDown".equals(name)) {
			removeOnCloseDown = "1".equals(value);
		} else if ("SoundOfCreate".equals(name)) {
			soundOfCreate = new SoundAction(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
	
	public BaseObject getPCreator() {
		return spellFile.getObject(pCreator);
	}
}
