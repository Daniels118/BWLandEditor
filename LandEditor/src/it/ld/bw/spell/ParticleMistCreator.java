package it.ld.bw.spell;

public class ParticleMistCreator extends BaseObject {
	public int colorA;
	public int colorB;
	public int colorG;
	public int colorR;
	public float initialScale;
	public float ratio;
	public float initialScaleMin;
	public boolean isShadowMap;
	public boolean loadLightMap;
	public boolean loopAnim;
	public int numFramesInFile;
	public int numFramesInUse;
	public int pitch;
	public boolean randomiseScale;
	public boolean takeRatioFromMatrix;
	public String textureFileName;
	
	public ParticleMistCreator(SpellFile spellFile) {
		super(spellFile);
	}
	
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
		} else if ("Ratio".equals(name)) {
			ratio = Float.parseFloat(value);
		} else if ("InitialScaleMin".equals(name)) {
			initialScaleMin = Float.parseFloat(value);
		} else if ("IsShadowMap".equals(name)) {
			isShadowMap = "1".equals(value);
		} else if ("LoadLightMap".equals(name)) {
			loadLightMap = "1".equals(value);
		} else if ("LoopAnim".equals(name)) {
			loopAnim = "1".equals(value);
		} else if ("NumFramesInFile".equals(name)) {
			numFramesInFile = Integer.parseInt(value);
		} else if ("NumFramesInUse".equals(name)) {
			numFramesInUse = Integer.parseInt(value);
		} else if ("Pitch".equals(name)) {
			pitch = Integer.parseInt(value);
		} else if ("RandomiseScale".equals(name)) {
			randomiseScale = "1".equals(value);
		} else if ("TakeRatioFromMatrix".equals(name)) {
			takeRatioFromMatrix = "1".equals(value);
		} else if ("TextureFileName".equals(name)) {
			textureFileName = parseString(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
}
