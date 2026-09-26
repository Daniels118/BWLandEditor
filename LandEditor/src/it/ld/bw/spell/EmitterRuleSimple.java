package it.ld.bw.spell;

public class EmitterRuleSimple extends BaseObject {
	public float emissionFreq;
	public int group;
	public boolean initiallyVisible;
	public int maxAtoms;
	public int[] nextGroups;
	public boolean orientWithParent;
	private String pCreator;
	public boolean randomise;
	public boolean removeOnCloseDown;
	public SoundAction soundEmission;
	public float speed;
	
	public EmitterRuleSimple(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("EmissionFreq".equals(name)) {
			emissionFreq = Float.parseFloat(value);
		} else if ("Group".equals(name)) {
			group = Integer.parseInt(value);
		} else if ("InitiallyVisible".equals(name)) {
			initiallyVisible = "1".equals(value);
		} else if ("MaxAtoms".equals(name)) {
			maxAtoms = Integer.parseInt(value);
		} else if ("NextGroups".equals(name)) {
			nextGroups = parseArray(value);
		} else if ("OrientWithParent".equals(name)) {
			orientWithParent = "1".equals(value);
		} else if ("PCreator".equals(name)) {
			pCreator = value;
		} else if ("Randomise".equals(name)) {
			randomise = "1".equals(value);
		} else if ("RemoveOnCloseDown".equals(name)) {
			removeOnCloseDown = "1".equals(value);
		} else if ("SoundEmission".equals(name)) {
			soundEmission = new SoundAction(value);
		} else if ("Speed".equals(name)) {
			speed = Float.parseFloat(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
	
	public BaseObject getPCreator() {
		return spellFile.getObject(pCreator);
	}
}
