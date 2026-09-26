package it.ld.bw.spell;

public class ParticlePointCreator extends BaseObject {
	public int colorA;
	public int colorB;
	public int colorG;
	public int colorR;
	public float initialScale;
	public boolean loopAnim;
	
	public ParticlePointCreator(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("ColorA".equals(name)) {
			colorA = Integer.parseInt(value);
		} else if ("ColorB".equals(name)) {
			colorB = Integer.parseInt(value);
		} else if ("ColorG".equals(name)) {
			colorG = Integer.parseInt(value);
		} else if ("ColorR".equals(name)) {
			colorR = Integer.parseInt(value);
		} else if ("InitialScale".equals(name)) {
			initialScale = Float.parseFloat(value);
		} else if ("LoopAnim".equals(name)) {
			loopAnim = "1".equals(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
}
