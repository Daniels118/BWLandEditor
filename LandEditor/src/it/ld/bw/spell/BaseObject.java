package it.ld.bw.spell;

public abstract class BaseObject {
	protected final SpellFile spellFile;
	
	public BaseObject(SpellFile spellFile) {
		this.spellFile = spellFile;
	}
	
	public abstract void setProperty(String name, String type, String value);
	
	protected int[] parseArray(String value) {
		String[] words = value.trim().split(" +");
		if (!"SIZE".equals(words[0])) throw new IllegalArgumentException("Expected SIZE");
		final int size = Integer.parseInt(words[1]);
		int[] res = new int[size];
		for (int i = 0; i < size; i++) {
			res[i] = Integer.parseInt(words[2 + i]);
		}
		return res;
	}
	
	protected static String parseString(String value) {
		if ("NULL_STRING".equals(value)) return null;
		return value;
	}
}
