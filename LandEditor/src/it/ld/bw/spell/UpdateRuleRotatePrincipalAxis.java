package it.ld.bw.spell;

public class UpdateRuleRotatePrincipalAxis extends BaseObject {
	public float angularVel;
	public float axisChosen;
	public String condition; 
	public int group;
	public boolean removeOnCloseDown;
	
	public UpdateRuleRotatePrincipalAxis(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("AngularVel".equals(name)) {
			angularVel = Float.parseFloat(value);
		} else if ("AxisChosen".equals(name)) {
			axisChosen = Float.parseFloat(value);
		} else if ("Condition".equals(name)) {
			condition = parseString(value);
		} else if ("Group".equals(name)) {
			group = Integer.parseInt(value);
		} else if ("RemoveOnCloseDown".equals(name)) {
			removeOnCloseDown = "1".equals(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
}
