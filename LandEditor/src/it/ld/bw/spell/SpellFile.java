package it.ld.bw.spell;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.InflaterInputStream;

import it.ld.utils.EndianDataInputStream;

public class SpellFile extends BaseObject {
	private File file;
	
	public boolean deleteOnCloseDown;
	public int[] hierarchies;
	public int[] initiallyCreated;
	public float maxSpellAge;
	
	private final Map<String, BaseObject> objects = new HashMap<>();
	
	public SpellFile() {
		super(null);
	}
	
	public void read(File file) throws FileNotFoundException, IOException {
		this.file = file;
		byte[] buf;
		try (FileInputStream fis = new FileInputStream(file)) {
			int size;
			try (EndianDataInputStream dis = new EndianDataInputStream(fis)) {
				dis.order(ByteOrder.LITTLE_ENDIAN);
				size = dis.readInt();
			}
			buf = new byte[size];
			try (InflaterInputStream inflater = new InflaterInputStream(fis)) {
				int total = 0;
				while (total < size) {
					int n = inflater.read(buf, total, size - total);
					if (n < 0) break;
					total += n;
				}
				if (total != size) {
					throw new IOException("Wrong size: expected "+size+", found "+total);
				}
			}
		}
		BaseObject currentObject = this;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(buf);
				InputStreamReader isr = new InputStreamReader(bais);
				BufferedReader br = new BufferedReader(isr);) {
			String line = br.readLine();
			while (line != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					String[] words = line.split(" ", 4);
					String verb = words[0];
					if ("BEGINCLASS".equals(verb)) {
						String className = words[1];
						String key = words[2];
						currentObject = createObject(className);
						objects.put(key, currentObject);
					} else if ("ENDCLASS".equals(verb)) {
						currentObject = null;
					} else if ("BEGINPROPERTIES".equals(verb)) {
						//
					} else if ("ENDPROPERTIES".equals(verb)) {
						//
					} else if ("PROPERTY".equals(verb)) {
						String name = words[1];
						String type = words[2];
						String value = words[3];
						currentObject.setProperty(name, type, value);
					}
				}
				line = br.readLine();
			}
		}
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public Map<String, BaseObject> getObjects() {
		return objects;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends BaseObject> T getObject(Class<T> clazz) {
		for (BaseObject obj : objects.values()) {
			if (clazz.isAssignableFrom(obj.getClass())) {
				return (T)obj;
			}
		}
		return null;
	}
	
	public BaseObject getObject(String key) {
		BaseObject res = objects.get(key);
		if (res == null) throw new RuntimeException("Key not found: " + key);
		return res;
	}
	
	@Override
	public void setProperty(String name, String type, String value) {
		if ("DeleteOnCloseDown".equals(name)) {
			deleteOnCloseDown = "1".equals(value);
		} else if ("Hierarchies".equals(name)) {
			hierarchies = parseArray(value);
		} else if ("InitiallyCreated".equals(name)) {
			initiallyCreated = parseArray(value);
		} else if ("MaxSpellAge".equals(name)) {
			maxSpellAge = Float.parseFloat(value);
		} else {
			throw new IllegalArgumentException("Unknown property: " + name);
		}
	}
	
	@Override
	public String toString() {
		return file != null ? file.toString() : super.toString();
	}
	
	private BaseObject createObject(String className) {
		if ("AR_FadeAlpha".equals(className)) {
			return new ARFadeAlpha(this);
		} else if ("AR_FadeOutOnceConditionTrue".equals(className)) {
			return new ARFadeOutOnceConditionTrue(this);
		} else if ("CreateRuleAnAtom".equals(className)) {
			return new CreateRuleAnAtom(this);
		} else if ("EmitterRuleLightningSprite".equals(className)) {
			return new EmitterRuleLightningSprite(this);
		} else if ("EmitterRuleSimple".equals(className)) {
			return new EmitterRuleSimple(this);
		} else if ("EventConditionTrueOnCloseDown".equals(className)) {
			return new EventConditionTrueOnCloseDown(this);
		} else if ("FollowOrigin".equals(className)) {
			return new FollowOrigin(this);
		} else if ("MagnitudeFloatProvider".equals(className)) {
			return new MagnitudeFloatProvider(this);
		} else if ("ParticleMistCreator".equals(className)) {
			return new ParticleMistCreator(this);
		} else if ("ParticlePointCreator".equals(className)) {
			return new ParticlePointCreator(this);
		} else if ("ParticleSpriteCreator".equals(className)) {
			return new ParticleSpriteCreator(this);
		} else if ("RemoveRuleAfterCloseDown".equals(className)) {
			return new RemoveRuleAfterCloseDown(this);
		} else if ("RemoveRuleOldAgeOnly".equals(className)) {
			return new RemoveRuleOldAgeOnly(this);
		} else if ("SetScale".equals(className)) {
			return new SetScale(this);
		} else if ("UpdateRuleRotatePrincipalAxis".equals(className)) {
			return new UpdateRuleRotatePrincipalAxis(this);
		} else if ("UR_OrientSpriteWithRandomAngle".equals(className)) {
			return new UROrientSpriteWithRandomAngle(this);
		} else {
			throw new IllegalArgumentException("Unknown class: " + className);
		}
	}
	
	public static SpellFile load(File file) throws FileNotFoundException, IOException {
		SpellFile spellFile = new SpellFile();
		spellFile.read(file);
		return spellFile;
	}
}
