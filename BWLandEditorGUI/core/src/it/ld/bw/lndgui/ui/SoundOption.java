package it.ld.bw.lndgui.ui;

import it.ld.bw.lnd.model.LH3DLandCell.Sound;
import it.ld.bw.lndgui.I18n;

public class SoundOption {
	private static SoundOption[] values;
	private static SoundOption[] valuesWithNull;
	
	public final Sound sound;
	private String text;
	
	public SoundOption(Sound sound) {
		this.sound = sound;
	}
	
	@Override
	public String toString() {
		if (text == null) {
			text = I18n.tr("cell.sounds."+(sound != null ? sound.name() : "keep"));
		}
		return text;
	}
	
	public static SoundOption[] values() {
		if (values == null) {
			SoundOption[] vals = valuesWithNull();
			values = new SoundOption[vals.length - 1];
			for (int i = 1; i < vals.length; i++) {
				values[i - 1] = vals[i];
			}
		}
		return values;
	}
	
	public static SoundOption[] valuesWithNull() {
		if (valuesWithNull == null) {
			Sound[] vals = Sound.values();
			valuesWithNull = new SoundOption[vals.length + 1];
			valuesWithNull[0] = new SoundOption(null);
			for (int i = 0; i < vals.length; i++) {
				valuesWithNull[i + 1] = new SoundOption(vals[i]);
			}
		}
		return valuesWithNull;
	}
	
	public static SoundOption valueOf(Sound sound) {
		if (sound == null) return valuesWithNull[0];
		for (SoundOption value : valuesWithNull) {
			if (value.sound == sound) return value;
		}
		return null;
	}
}