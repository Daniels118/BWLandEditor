package it.ld.bw.lndgui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lndgui.Settings.MetaStore;
import it.ld.bw.lndgui.tools.Brush;
import it.ld.bw.lndgui.tools.CountryBrush;
import it.ld.bw.lndgui.tools.SculptBrush;
import it.ld.bw.lndgui.tools.SoundBrush;
import it.ld.utils.UChangeListener;

public class MetaManager implements AutoCloseable {
	private final MainApp app;
	private LndFile land;
	
	private boolean changed = false;
	
	public MetaManager(MainApp app) {
		this.app = app;
		app.listeners.add(appChangeListener);
		Settings.listeners.add(settingsChangeListener);
	}
	
	private UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			MainApp.Property property = (MainApp.Property)event.getProperty();
			switch (property) {
				case SCULPT_BRUSHES:
				case COUNTRY_BRUSHES:
				case OCEAN_BRUSH:
				case LAKE_BRUSH:
				case SOUND_BRUSH:
					if (!app.isChangingLand() && land != null) {
						if (event.getType() == EventType.ADD) {
							changed = true;
						} else if (event.getType() == EventType.CHANGE && event.getCause() != null) {
							Object brushProperty = event.getCause().getProperty();
							if (brushProperty!= Brush.Property.SIZE
									&& brushProperty != Brush.Property.ANGLE_OFFSET
									&& brushProperty != Brush.Property.ROTATE_WITH_CAMERA) {
								changed = true;
							}
						} else if (event.getType() == EventType.REMOVE) {
							changed = true;
						}
						if (changed && Settings.META_STORE.getValue() == MetaStore.EMBEDDED) {
							app.setUnsavedLandChanges(true);
						}
					}
					break;
				case LAND:
					if (changed && Settings.META_STORE.getValue() == MetaStore.EXTERNAL) {
						store();
					}
					land = app.getLand();
					if (land != null) {
						load();
					}
					changed = false;
					break;
				default:
			}
		}
	};
	
	private UChangeListener settingsChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == Settings.META_STORE) {
				if (Settings.META_STORE.getValue() == MetaStore.EMBEDDED) {
					File metaFile = getMetaFile();
					if (metaFile != null) {
						metaFile.delete();
					}
				}
				changed = true;
				app.setUnsavedLandChanges(true);
			}
		}
	};
	
	private void load() {
		if (land != null) {
			File metaFile = getMetaFile();
			Json json = new Json();
			JsonValue root = null;
			if (metaFile != null && metaFile.exists()) {
				try (InputStream reader = new FileInputStream(metaFile);) {
					root = json.fromJson(null, reader);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				byte[] gzipped = land.getMetadata().getData();
				if (gzipped.length > 0) {
					try (ByteArrayInputStream compressed = new ByteArrayInputStream(gzipped);
							GZIPInputStream gunzip = new GZIPInputStream(compressed);) {
						root = json.fromJson(null, gunzip);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if (root != null) {
				JsonValue sculptBrushes = root.get("sculptBrushes");
				if (sculptBrushes != null && sculptBrushes.size > 0) {
					app.getSculptBrushes().clear();
					for (int i = 0; i < sculptBrushes.size; i++) {
						JsonValue item = sculptBrushes.get(i);
						SculptBrush brush = app.createSculptBrush();
						brush.read(json, item);
					}
				}
				JsonValue countryBrushes = root.get("countryBrushes");
				if (countryBrushes != null && countryBrushes.size > 0) {
					app.getCountryBrushes().clear();
					for (int i = 0; i < countryBrushes.size; i++) {
						JsonValue item = countryBrushes.get(i);
						CountryBrush brush = app.createCountryBrush();
						brush.read(json, item);
					}
				}
				JsonValue oceanBrush = root.get("oceanBrush");
				if (oceanBrush != null) {
					app.getOceanBrush().read(json, oceanBrush);
				}
				JsonValue lakeBrush = root.get("lakeBrush");
				if (lakeBrush != null) {
					app.getLakeBrush().read(json, lakeBrush);
				}
				JsonValue soundBrush = root.get("soundBrush");
				if (soundBrush != null) {
					app.getSoundBrush().read(json, soundBrush);
				}
			}
		}
	}
	
	public void store() {
		if (changed && land != null) {
			MetaStore metaStore = (MetaStore)Settings.META_STORE.getValue();
			File metaFile = getMetaFile();
			if (metaFile != null && metaFile.exists()) {
				metaStore = MetaStore.EXTERNAL;
			}
			
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
					Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);) {
				Json json = new Json(OutputType.json);
				json.setWriter(writer);
				json.writeObjectStart();
				json.writeValue("sculptBrushes", app.getSculptBrushes().toArray(), null, SculptBrush.class);
				json.writeValue("countryBrushes", app.getCountryBrushes().toArray(), null, CountryBrush.class);
				json.writeValue("oceanBrush", app.getOceanBrush(), SculptBrush.class);
				json.writeValue("lakeBrush", app.getLakeBrush(), SculptBrush.class);
				json.writeValue("soundBrush", app.getSoundBrush(), SoundBrush.class);
				json.writeObjectEnd();
				writer.flush();
				
				switch (metaStore) {
					case EMBEDDED:
						byte[] gzipped;
						try (ByteArrayOutputStream compressed = new ByteArrayOutputStream();
								GZIPOutputStream gzip = new GZIPOutputStream(compressed);) {
							gzip.write(baos.toByteArray());
						    gzip.finish();
						    gzipped = compressed.toByteArray();
						}
						app.getLand().getMetadata().setData(gzipped);
						if (metaFile != null) metaFile.delete();
						break;
					case EXTERNAL:
						app.getLand().getMetadata().setData(new byte[0]);
						if (metaFile != null) {
							try {
								Files.write(metaFile.toPath(), baos.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							System.err.println("Cannot save metadata: LndFile.file is null");
						}
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		changed = false;
	}
	
	private File getMetaFile() {
		File landFile = land.getFile();
		if (landFile != null) {
			return new File(landFile.getParent(), basename(landFile) + ".json");
		}
		return null;
	}
	
	@Override
	public void close() throws Exception {
		app.listeners.remove(appChangeListener);
		Settings.listeners.remove(settingsChangeListener);
	}
	
	private static String basename(File file) {
		String name = file.getName();
		int p = name.lastIndexOf('.');
		if (p < 0) return name;
		return name.substring(0, p);
	}
}
