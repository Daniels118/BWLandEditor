package it.ld.bw.lndgui;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import it.ld.bw.lhx.LHXFile;
import it.ld.bw.lnd.model.LndFile;
import it.ld.libgdx.utils.Utils;
import it.ld.utils.UChangeListener;

public class RecoveryManager implements AutoCloseable {
	private static final String FILENAME = "BWLandEditorGUI_recovery";
	
	private final MainApp app;
	
	private LndFile land = null;
	private File recoveryLandFile = null;
	private float timeFromLastLandSave = 0;
	
	private LHXFile lhx = null;
	private File recoveryLHXFile = null;
	private float timeFromLastLHXSave = 0;
	
	public RecoveryManager(MainApp app) {
		this.app = app;
		app.listeners.add(appChangeListener);
	}
	
	private UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.LAND) {
				setLand(app.getLand());
			} else if (event.getProperty() == MainApp.Property.LAND_CHANGED) {
				if (!app.hasUnsavedLandChanges()) {
					deleteRecoveryLandFile();
					timeFromLastLandSave = 0;
				}
			} else if (event.getProperty() == MainApp.Property.LHX) {
				setLHX(app.getLHX());
			} else if (event.getProperty() == MainApp.Property.LHX_CHANGED) {
				if (!app.hasUnsavedLHXChanges()) {
					deleteRecoveryLHXFile();
					timeFromLastLHXSave = 0;
				}
			}
		}
	};
	
	private void setLand(LndFile land) {
		deleteRecoveryLandFile();
		this.land = land;
		timeFromLastLandSave = 0;
	}
	
	private void setLHX(LHXFile lhx) {
		deleteRecoveryLHXFile();
		this.lhx = lhx;
		timeFromLastLHXSave = 0;
	}
	
	public void act(float delta) {
		if (app.hasUnsavedLandChanges()) {
			timeFromLastLandSave += delta;
			if (Settings.AUTORECOVERY.getBool() && timeFromLastLandSave >= Settings.AUTORECOVERY_INT.getInt() * 60) {
				try {
					createRecoveryLandFile();
					land.write(recoveryLandFile);
					//System.out.println("Saved to " + recoveryLandFile.getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
				}
				timeFromLastLandSave = 0;
			}
		}
		//
		if (app.hasUnsavedLHXChanges()) {
			timeFromLastLHXSave += delta;
			if (Settings.AUTORECOVERY.getBool() && timeFromLastLHXSave >= Settings.AUTORECOVERY_INT.getInt() * 60) {
				try {
					createRecoveryLHXFile();
					lhx.write(recoveryLHXFile);
					//System.out.println("Saved to " + recoveryFile.getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
				}
				timeFromLastLHXSave = 0;
			}
		}
	}
	
	private void createRecoveryLandFile() {
		if (recoveryLandFile == null) {
			String prefix = "land-";
			if (land.getFile() != null) {
				prefix = land.getFile().getName();
				prefix = prefix.substring(0, prefix.lastIndexOf('.')) + "-";
			}
			try {
				recoveryLandFile = File.createTempFile(prefix, ".lnd");
				Preferences prefs = Gdx.app.getPreferences(FILENAME);
				prefs.putString(recoveryLandFile.getAbsolutePath(), land.getFile() != null ? land.getFile().getAbsolutePath() : "");
				prefs.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void createRecoveryLHXFile() {
		if (recoveryLHXFile == null) {
			String prefix = "land-";
			if (lhx.getFile() != null) {
				prefix = lhx.getFile().getName();
				prefix = prefix.substring(0, prefix.lastIndexOf('.')) + "-";
			}
			try {
				recoveryLHXFile = File.createTempFile(prefix, ".txt");
				Preferences prefs = Gdx.app.getPreferences(FILENAME);
				prefs.putString(recoveryLHXFile.getAbsolutePath(), lhx.getFile() != null ? lhx.getFile().getAbsolutePath() : "");
				prefs.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void deleteRecoveryLandFile() {
		if (recoveryLandFile != null) {
			removeRecoveredFile(recoveryLandFile);
			recoveryLandFile = null;
		}
	}
	
	private void deleteRecoveryLHXFile() {
		if (recoveryLHXFile != null) {
			removeRecoveredFile(recoveryLHXFile);
			recoveryLHXFile = null;
		}
	}
	
	public static RecoveredFile[] getRecoveredFiles() {
		List<RecoveredFile> res = new ArrayList<>();
		Preferences prefs = Gdx.app.getPreferences(FILENAME);
		for (Entry<String, ?> e : prefs.get().entrySet()) {
			File tmpFile = new File((String)e.getKey());
			if (tmpFile.exists()) {
				String dstFile = (String)e.getValue();
				res.add(new RecoveredFile(tmpFile, dstFile.isEmpty() ? null : new File(dstFile)));
			}
		}
		return res.toArray(new RecoveredFile[0]);
	}
	
	public static void clearRecoveredFiles() {
		Preferences prefs = Gdx.app.getPreferences(FILENAME);
		for (String tmpName : prefs.get().keySet()) {
			File tmpFile = new File((String)tmpName);
			tmpFile.delete();
		}
		prefs.clear();
		prefs.flush();
	}
	
	public static void removeRecoveredFile(RecoveredFile recovered) {
		removeRecoveredFile(recovered.recovered);
	}
	
	private static void removeRecoveredFile(File recovered) {
		Preferences prefs = Gdx.app.getPreferences(FILENAME);
		prefs.remove(recovered.getAbsolutePath());
		prefs.flush();
		recovered.delete();
		//System.out.println("Deleted: " + recovered.getAbsolutePath());
	}
	
	@Override
	public void close() throws Exception {
		app.listeners.remove(appChangeListener);
	}
	
	
	public static class RecoveredFile {
		public final File recovered;
		public final File original;
		
		private final String text;
		
		public RecoveredFile(File recovered, File original) {
			this.recovered = recovered;
			this.original = original;
			//
			DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
			ZonedDateTime dateTime = Instant.ofEpochMilli(recovered.lastModified()).atZone(ZoneId.systemDefault());
			String strLastMod = dateTime.format(formatter);
			this.text = (original != null ? Utils.ellipsis(original, 3) : "Unnamed") + " (" + strLastMod + ")";
		}
		
		@Override
		public String toString() {
			return text;
		}
	}
}
