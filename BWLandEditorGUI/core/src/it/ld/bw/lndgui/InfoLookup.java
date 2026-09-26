package it.ld.bw.lndgui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.ld.bw.info.AbodeNumber;
import it.ld.bw.info.GAbodeInfo;
import it.ld.bw.info.GAnimatedStaticInfo;
import it.ld.bw.info.GFeatureInfo;
import it.ld.bw.info.GMobileObjectInfo;
import it.ld.bw.info.GMobileStaticInfo;
import it.ld.bw.info.GSpellSeedInfo;
import it.ld.bw.info.GVillagerInfo;
import it.ld.bw.info.GWorshipSiteInfo;
import it.ld.bw.info.InfoConstants;

public class InfoLookup {
	private final InfoConstants info;
	
	private final Map<String, GAbodeInfo> abodes = new HashMap<>();
	private final Map<String, GAbodeInfo> spellDispensers = new HashMap<>();
	private final Map<String, GAbodeInfo> townCentres = new HashMap<>();
	private final Map<String, GFeatureInfo> features = new HashMap<>();
	private final Map<String, GWorshipSiteInfo> worshipSites = new HashMap<>();
	private final Map<String, GAnimatedStaticInfo> animatedStatics = new HashMap<>();
	private final Map<String, GSpellSeedInfo> spellSeeds = new HashMap<>();
	private final Map<String, GVillagerInfo> villagers = new HashMap<>();
	
	public InfoLookup(InfoConstants info) {
		this.info = info;
		//
		for (GAbodeInfo abode : info.abode) {
			if (abode.abodeNumber == AbodeNumber.FIELD) continue;
			String key;
			if (abode.tribeType != null) {
				key = abode.tribeType.name() + "_ABODE_" + abode.abodeNumber.name();
			} else {
				key = "ABODE_" + abode.abodeNumber.name();
			}
			if (abode.abodeNumber == AbodeNumber.SPELL_DISPENSER) {
				spellDispensers.put(key, abode);
			} else if (abode.abodeNumber == AbodeNumber.TOWN_CENTRE) {
				townCentres.put(key, abode);
			} else {
				abodes.put(key, abode);
			}
		}
		//
		for (GFeatureInfo feature : info.feature) {
			features.put(feature.debugString, feature);
		}
		//
		for (GWorshipSiteInfo worshipSite : info.worshipSite) {
			worshipSites.put(worshipSite.debugString, worshipSite);
		}
		//
		for (GAnimatedStaticInfo animatedStatic : info.animatedStatic) {
			animatedStatics.put(animatedStatic.debugString, animatedStatic);
		}
		//
		for (GSpellSeedInfo spellSeed : info.spellSeed) {
			spellSeeds.put(spellSeed.debugString, spellSeed);
			for (int pu = 1; pu <= spellSeed.powerUpCount; pu++) {
				spellSeeds.put(spellSeed.debugString + "_PU" + pu, spellSeed);
			}
		}
		//
		for (GVillagerInfo villager : info.villager) {
			if (villager.tribeType != null) {
				String key = villager.tribeType.name() + "_" + villager.villagerNumber.name();
				villagers.put(key, villager);
			}
		}
	}
	
	public List<String> getAbodeTypes() {
		return new ArrayList<>(abodes.keySet());
	}
	
	public GAbodeInfo getAbode(String name) {
		return abodes.get(name);
	}
	
	public List<String> getSpellDispenserTypes() {
		return new ArrayList<>(spellDispensers.keySet());
	}
	
	public GAbodeInfo getSpellDispenser(String name) {
		return spellDispensers.get(name);
	}
	
	public List<String> getTownCentreTypes() {
		return new ArrayList<>(townCentres.keySet());
	}
	
	public GAbodeInfo getTownCentre(String name) {
		return townCentres.get(name);
	}
	
	public List<String> getFeatureTypes() {
		return new ArrayList<>(features.keySet());
	}
	
	public GFeatureInfo getFeature(String name) {
		return features.get(name);
	}
	
	public List<String> getWorshipSiteTypes() {
		return new ArrayList<>(worshipSites.keySet());
	}
	
	public GWorshipSiteInfo getWorshipSite(String name) {
		return worshipSites.get(name);
	}
	
	public int getMobileObjectsCount() {
		return info.mobileObject.length;
	}
	
	public GMobileObjectInfo getMobileObject(int type) {
		return info.mobileObject[type];
	}
	
	public int getMobileStaticsCount() {
		return info.mobileStatic.length;
	}
	
	public GMobileStaticInfo getMobileStatic(int type) {
		return info.mobileStatic[type];
	}
	
	public List<String> getAnimatedStaticTypes() {
		return new ArrayList<>(animatedStatics.keySet());
	}
	
	public GAnimatedStaticInfo getAnimatedStatic(String type) {
		return animatedStatics.get(type);
	}
	
	public List<String> getSpellSeedTypes() {
		return new ArrayList<>(spellSeeds.keySet());
	}
	
	public GSpellSeedInfo getSpellSeed(String type) {
		return spellSeeds.get(type);
	}
	
	public List<String> getVillagerTypes() {
		return new ArrayList<>(villagers.keySet());
	}
	
	public GVillagerInfo getVillager(String type) {
		return villagers.get(type);
	}
}
