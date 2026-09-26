package it.ld.bw.spell;

public class ParticleSpriteCreator extends BaseObject {
	public boolean centreAtBase;
	public int colorA;
	public int colorB;
	public int colorG;
	public int colorR;
	public float frameRate;
	public int initFrame;
	public float initialScale;
	public boolean loopAnim;
	public boolean materialSetDoubleSided;
	public boolean materialUpdateZBuffer;
	public int numFrames;
	public boolean playAnim;
	public boolean randomiseFrameDirection;
	public boolean randomiseInitFrame;
	public boolean randomiseScale;
	public int scaleAlpha;
	public boolean setHorozontal;
	public boolean setVertical;
	public float spriteOriginX;
	public float spriteOriginY;
	public float stretchVertically;
	public int fileOffset;
	public String textureFileName;
	public boolean useAdditiveAlpha;
	public boolean usePlayerColor;
	
	public ParticleSpriteCreator(SpellFile spellFile) {
		super(spellFile);
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("CentreAtBase".equals(name)) {
			centreAtBase = "1".equals(value);
		} else if ("ColorA".equals(name)) {
			colorA = Integer.parseInt(value);
		} else if ("ColorB".equals(name)) {
			colorB = Integer.parseInt(value);
		} else if ("ColorG".equals(name)) {
			colorG = Integer.parseInt(value);
		} else if ("ColorR".equals(name)) {
			colorR = Integer.parseInt(value);
		} else if ("FrameRate".equals(name)) {
			frameRate = Float.parseFloat(value);
		} else if ("InitFrame".equals(name)) {
			initFrame = Integer.parseInt(value);
		} else if ("InitialScale".equals(name)) {
			initialScale = Float.parseFloat(value);
		} else if ("LoopAnim".equals(name)) {
			loopAnim = "1".equals(value);
		} else if ("MaterialSetDoubleSided".equals(name)) {
			materialSetDoubleSided = "1".equals(value);
		} else if ("MaterialUpdateZBuffer".equals(name)) {
			materialUpdateZBuffer = "1".equals(value);
		} else if ("NumFrames".equals(name)) {
			numFrames = Integer.parseInt(value);
		} else if ("PlayAnim".equals(name)) {
			playAnim = "1".equals(value);
		} else if ("RandomiseFrameDirection".equals(name)) {
			randomiseFrameDirection = "1".equals(value);
		} else if ("RandomiseInitFrame".equals(name)) {
			randomiseInitFrame = "1".equals(value);
		} else if ("RandomiseScale".equals(name)) {
			randomiseScale = "1".equals(value);
		} else if ("ScaleAlpha".equals(name)) {
			scaleAlpha = Integer.parseInt(value);
		} else if ("SetHorozontal".equals(name)) {
			setHorozontal = "1".equals(value);
		} else if ("SetVertical".equals(name)) {
			setVertical = "1".equals(value);
		} else if ("SpriteOriginX".equals(name)) {
			spriteOriginX = Float.parseFloat(value);
		} else if ("SpriteOriginY".equals(name)) {
			spriteOriginY = Float.parseFloat(value);
		} else if ("StretchVertically".equals(name)) {
			stretchVertically = Float.parseFloat(value);
		} else if ("FileOffset".equals(name)) {
			fileOffset = Integer.parseInt(value);
		} else if ("TextureFileName".equals(name)) {
			textureFileName = parseString(value);
		} else if ("UseAdditiveAlpha".equals(name)) {
			useAdditiveAlpha = "1".equals(value);
		} else if ("UsePlayerColor".equals(name)) {
			usePlayerColor = "1".equals(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
}
