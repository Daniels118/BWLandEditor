package it.ld.bw.spell;

public class EmitterRuleLightningSprite extends BaseObject {
	public String condition;
	public float emissionFreq;
	public int group;
	public int maxAtoms;
	public int[] nextGroups;
	private String pCreator;
	public boolean randomise;
	public boolean removeOnCloseDown;
	
	public EmitterRuleLightningSprite(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("Condition".equals(name)) {
			condition = parseString(value);
		} else if ("EmissionFreq".equals(name)) {
			emissionFreq = Float.parseFloat(value);
		} else if ("Group".equals(name)) {
			group = Integer.parseInt(value);
		} else if ("MaxAtoms".equals(name)) {
			maxAtoms = Integer.parseInt(value);
		} else if ("NextGroups".equals(name)) {
			nextGroups = parseArray(value);
		} else if ("PCreator".equals(name)) {
			pCreator = value;
		} else if ("Randomise".equals(name)) {
			randomise = "1".equals(value);
		} else if ("RemoveOnCloseDown".equals(name)) {
			removeOnCloseDown = "1".equals(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
	
	public BaseObject getPCreator() {
		return spellFile.getObject(pCreator);
	}
}
