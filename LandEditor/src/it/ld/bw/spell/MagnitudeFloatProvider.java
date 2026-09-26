package it.ld.bw.spell;

public class MagnitudeFloatProvider extends BaseObject {
	public float scaleBy;
	
	public MagnitudeFloatProvider(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("ScaleBy".equals(name)) {
			scaleBy = Float.parseFloat(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
}
