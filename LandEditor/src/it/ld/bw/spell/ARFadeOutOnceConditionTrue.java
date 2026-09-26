package it.ld.bw.spell;

public class ARFadeOutOnceConditionTrue extends BaseObject {
	public String condition;
	private String conditionStartFadeOut;
	public boolean fadeAlpha;
	public int group;
	public boolean removeOnCloseDown;
	public boolean shrinkScale;
	public float timeToFadeOut;
	
	public ARFadeOutOnceConditionTrue(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("Condition".equals(name)) {
			condition = parseString(value);
		} else if ("ConditionStartFadeOut".equals(name)) {
			conditionStartFadeOut = value;
		} else if ("FadeAlpha".equals(name)) {
			fadeAlpha = "1".equals(value);
		} else if ("Group".equals(name)) {
			group = Integer.parseInt(value);
		} else if ("RemoveOnCloseDown".equals(name)) {
			removeOnCloseDown = "1".equals(value);
		} else if ("ShrinkScale".equals(name)) {
			shrinkScale = "1".equals(value);
		} else if ("TimeToFadeOut".equals(name)) {
			timeToFadeOut = Float.parseFloat(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
	
	public BaseObject getConditionStartFadeOut() {
		return spellFile.getObject(conditionStartFadeOut);
	}
}
