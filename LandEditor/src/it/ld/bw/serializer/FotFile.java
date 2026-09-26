package it.ld.bw.serializer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class FotFile {
	private ArrayList<FootpathLinkSave> footpathLinkSaves;
	private ArrayList<Footpath> footpaths;
	
	private File file;
	
	public void read(GameThingDeserializer str) throws Exception {
		footpathLinkSaves = str.deserializeList(FootpathLinkSave.class, GameThingType.FootpathLinkSave);
		footpaths = str.deserializeList(Footpath.class, GameThingType.Footpath);
	}

	public void read(File file) throws Exception {
		this.file = file;
		try (FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);
				GameThingDeserializer gtd = new GameThingDeserializer(bis)) {
			read(gtd);
		}
	}
	
	public ArrayList<FootpathLinkSave> getFootpathLinkSaves() {
		return footpathLinkSaves;
	}
	
	public ArrayList<Footpath> getFootpaths() {
		return footpaths;
	}
	
	public File getFile() {
		return this.file;
	}
	
	public static FotFile load(File file) throws Exception {
		FotFile fot = new FotFile();
		fot.read(file);
		return fot;
	}
	
	public String toJsonString() {
		String[] s1 = new String[footpathLinkSaves.size()];
		for (int i = 0; i < s1.length; i++) {
			s1[i] = footpathLinkSaves.get(i).toJsonString("    ");
		}
		String[] s2 = new String[footpaths.size()];
		for (int i = 0; i < s2.length; i++) {
			s2[i] = footpaths.get(i).toJsonString("    ");
		}
		return "{\n"+
			"  \"footpathLinkSaves\": [\n"+String.join(",\n", s1)+"\n  ],\n"+
			"  \"footpaths\": [\n"+String.join(",\n", s2)+"\n  ]"+
			"\n}";
	}
	
	@Override
	public String toString() {
		if (file != null) return file.toString();
		return super.toString();
	}
}
