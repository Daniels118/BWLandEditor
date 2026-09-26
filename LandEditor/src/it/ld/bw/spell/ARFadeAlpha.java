package it.ld.bw.spell;

public class ARFadeAlpha extends BaseObject {
	public String condition;
	public int group;
	public boolean removeOnCloseDown;
	public int startAlpha;
	public float startTime;
	public int stopAlpha;
	public float stopTime;
	
	public ARFadeAlpha(SpellFile spellFile) {
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
		} else if ("StartAlpha".equals(name)) {
			startAlpha = Integer.parseInt(value);
		} else if ("StartTime".equals(name)) {
			startTime = Float.parseFloat(value);
		} else if ("StopAlpha".equals(name)) {
			stopAlpha = Integer.parseInt(value);
		} else if ("StopTime".equals(name)) {
			stopTime = Float.parseFloat(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
}
