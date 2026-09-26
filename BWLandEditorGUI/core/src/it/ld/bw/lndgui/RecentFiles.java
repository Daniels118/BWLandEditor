package it.ld.bw.lndgui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import it.ld.utils.UChangeListener;

public final class RecentFiles {
	private static final String FILENAME = "BWLandEditorGUI_recent";
	
	private static LinkedList<File> files = null;
	
	private RecentFiles() {}
	
	public static void init() {
		if (files == null) {
			files = new LinkedList<>();
			Preferences prefs = Gdx.app.getPreferences(FILENAME);
			for (Object s : prefs.get().values()) {
				File file = new File((String)s);
				if (file.exists()) {
					files.add(file);
				}
			}
			Settings.listeners.add(settingsChangeListener);
		}
	}
	
	private static UChangeListener settingsChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == Settings.MAX_RECENT_FILES) {
				flush();
			}
		}
	};
	
	public static File[] get(String...extensions) {
		Set<String> extSet = new HashSet<>();
		for (String ext : extensions) {
			ext = ext.toLowerCase();
			if (!ext.startsWith(".")) {
				ext = "." + ext;
			}
			extSet.add(ext);
		}
		
		List<File> res = new ArrayList<>(files.size());
		for (File file : files) {
			String ext = file.getName().replaceFirst("^.*\\.", ".").toLowerCase();
			if (extSet.contains(ext)) {
				res.add(file);
			}
		}
		return res.toArray(new File[0]);
	}
	
	public static void add(File file) {
		final File absFile = file.getAbsoluteFile();
		//If the new file is already in the list at the beginning, do nothing
		if (!files.isEmpty() && files.getFirst().equals(absFile)) {
			return;
		}
		//If the file is already in the list, Remove it from the current position
		ListIterator<File> it = files.listIterator();
		while (it.hasNext()) {
			File tmp = it.next();
			if (tmp.equals(absFile)) {
				it.remove();
				break;
			}
		}
		//Insert the file at the beginning
		files.addFirst(absFile);
		flush();
	}
	
	private static void flush() {
		while (files.size() > Settings.MAX_RECENT_FILES.getInt()) {
			files.removeLast();
		}
		Preferences prefs = Gdx.app.getPreferences(FILENAME);
		prefs.clear();
		int i = 0;
		for (File tmp : files) {
			prefs.putString("F" + (i++), tmp.getAbsolutePath());
		}
		prefs.flush();
	}
}
