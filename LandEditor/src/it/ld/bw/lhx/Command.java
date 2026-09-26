/* Copyright (c) 2026 Daniele Lombardi / Daniels118
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.ld.bw.lhx;

import java.util.HashMap;
import java.util.Map;

import it.ld.bw.info.AnimalInfo;
import it.ld.bw.info.BalanceInfo;
import it.ld.bw.info.BigForestInfo;
import it.ld.bw.info.CreatureType;
import it.ld.bw.info.DesireInfo;
import it.ld.bw.info.FeatureInfo;
import it.ld.bw.info.FieldTypeInfo;
import it.ld.bw.info.LHXAbodeInfo;
import it.ld.bw.info.MobileObjectInfo;
import it.ld.bw.info.MobileStaticInfo;
import it.ld.bw.info.PlayerInfo;
import it.ld.bw.info.PotInfo;
import it.ld.bw.info.SpellSeedInfo;
import it.ld.bw.info.TreeInfo;
import it.ld.bw.info.TribeType;
import it.ld.bw.info.LHXVillagerInfo;

public enum Command {
	ADD_GAME_MESSAGE_LINE("String message, int numPlayers"),
	BRUSH_SIZE("float, float"),
	COUNTRY_CHANGE("Coord position, int"),
	CREATE_ABODE("int townId, Coord position, String<LHXAbodeInfo> type, int rotation, int scale = 1000, int foodAmount, int woodAmount"),
	CREATE_ANIMAL("Coord position, int, int, int"),
	CREATE_ANIMATED_STATIC("Coord position, String type, int rotation, int scale = 1000"),
	CREATE_AREA("Coord position, float"),
	CREATE_ARENA("Coord position, float"),
	CREATE_BASE("Coord position, int"),
	CREATE_BIG_FOREST("Coord position, BigForestInfo type, float rotation, float scale = 1.0"),
	CREATE_BONFIRE("Coord position, float rotation, float param3, float scale = 1.0"),
	CREATE_CITADEL("Coord position, int _zero, String<PlayerInfo> player, int rotation, int scale = 1000"),
	CREATE_CREATURE("Coord position, int, int"),
	CREATE_CREATURE_FROM_FILE("String<PlayerInfo> player, int<CreatureType> type, String creatureMindPath, Coord position"),
	CREATE_CREATURE_PEN("Coord position, int, int, int, int, int"),
	CREATE_DEAD_TREE("Coord position, String<PlayerInfo> player, int<TreeInfo> type, float scale = 1.0, float roll, float rotation, float pitch"),
	CREATE_DRINK_WAYPOINT("Coord position"),
	CREATE_FEATURE("Coord position, FeatureInfo type, int rotation, int scale = 1000, int"),
	CREATE_FIELD("Coord position, FieldTypeInfo type"),
	CREATE_FIRE_FLY("Coord position"),
	CREATE_FISH_FARM("Coord position, int"),
	CREATE_FLOCK("int flockId, Coord position, Coord destination, int scattering = 50, int radius = 20, int townId = -1"),
	CREATE_FLOWERS("Coord position, int, float, float"),
	CREATE_FOOTPATH("int footpathId"),
	CREATE_FOOTPATH_NODE("int footpathId, Coord position"),
	CREATE_FOREST("int forestId, Coord position"),
	CREATE_FURNITURE("Coord position, int, float"),
	CREATE_INFLUENCE_RING("Coord position, int, float, int"),
	CREATE_MIST("Coord position, float elevation, int color, float scale, float stretch"),
	CREATE_MOBILEOBJECT("Coord position, int<MobileObjectInfo> type, int rotation, int"),
	CREATE_MOBILESTATIC("Coord position, int<MobileStaticInfo> type, float, float"),
	CREATE_MOBILE_STATIC("Coord position, int<MobileStaticInfo> type, float elevation, float pitch, float rotation, float roll, float scale = 1.0"),
	CREATE_NEW_ANIMAL("Coord position, int<AnimalInfo> type, int flockId = -1, int townId, int age = 5", "flockId"),
	CREATE_NEW_BIG_FOREST("Coord position, int<BigForestInfo> type, int unknown, float rotation, float scale = 1.0"),
	CREATE_NEW_FEATURE("Coord position, String type, int rotation, int scale = 1000, int param5"),
	CREATE_NEW_TOWN_FIELD("int townId, Coord position, int<FieldTypeInfo> type, float rotation"),
	CREATE_NEW_TOWN_SPELL("int townId, String<SpellSeedInfo> type"),
	CREATE_NEW_TREE("int forestId = -1, Coord position, int<TreeInfo> type, bool isNonScenic = 1, float rotation, float scale = 1.0, float maxScale = 1.0"),
	CREATE_ONE_SHOT_SPELL("Coord position, String<SpellSeedInfo> type"),
	CREATE_ONE_SHOT_SPELL_PU("Coord position, String<SpellSeedInfo> type"),
	CREATE_PATH("int param1, int param2, int param3, int param4"),
	CREATE_PITCH("Coord position, int, int, int, int, int"),
	CREATE_PLANNED_ABODE("int townId, Coord position, String<LHXAbodeInfo> type, int rotation, int scale = 1000, int foodAmount, int woodAmount"),
	CREATE_PLANNED_CITADEL("int townId, Coord position, int, String<PlayerInfo> player, int rotation, int scale = 1000", "player"),
	CREATE_PLANNED_SPELL_ICON("int, Coord position, String, int, int, int"),
	CREATE_PLANNED_WALL_SECTION("Coord position, int, int, int, int"),
	CREATE_PLANNED_WORSHIP_SITE("Coord position, int, String<PlayerInfo> player, String<TribeType> tribe, int, int"),
	CREATE_POT("Coord position, int<PotInfo> type, int, int amount"),
	CREATE_SCAFFOLD("int townId, Coord position, int _zero, int rotation, int scale = 1000"),
	CREATE_SPECIAL_TOWN_VILLAGER("int townId, Coord position, int type, int age = 20"),
	CREATE_SPELL_DISPENSER("int townId, Coord position, String<LHXAbodeInfo> type, String<SpellSeedInfo> spellType, float rotation, float scale, float spawnInterval"),
	CREATE_SPELL_ICON("Coord position, String, int, int, int"),
	CREATE_STREAM("int streamId"),
	CREATE_STREAM_POINT("int streamId, Coord position"),
	CREATE_STREET_LANTERN("Coord position, int type = 7"),
	CREATE_STREET_LIGHT("Coord position"),
	CREATE_TOWN("int townId, Coord position, String<PlayerInfo> player, int<TribeType> civilization, String<TribeType> tribe"),
	CREATE_TOWN_CENTRE("int townId, Coord position, String<LHXAbodeInfo> type, int rotation, int scale = 1000, int"),
	CREATE_TOWN_CENTRE_SPELL_ICON("int, String"),
	CREATE_TOWN_FIELD("int townId, Coord position, FieldTypeInfo type"),
	CREATE_TOWN_FISH_FARM("int townId, Coord position, int"),
	CREATE_TOWN_SPELL("int townId, String type"),
	CREATE_TOWN_TEMPORARY_POTS("int townId, int, int"),	//Never found
	CREATE_TOWN_VILLAGER("int townId, Coord position, LHXVillagerInfo type, int age = 20"),
	CREATE_TREE("int forestId, Coord position, TreeInfo type, int rotation, int scale = 1000"),
	CREATE_VILLAGER("Coord, Coord, String"),	//Never found
	CREATE_VILLAGER_POS("Coord homePosition, Coord position, String<LHXVillagerInfo> type, int age = 20"),
	CREATE_WALL_SECTION("Coord position, int, int, int, int"),
	CREATE_WATERFALL("Coord position"),
	CREATE_WEATHER_CLIMATE("int, int, Coord, float, float"),
	CREATE_WEATHER_CLIMATE_RAIN("int, float, int, int, int"),
	CREATE_WEATHER_CLIMATE_TEMP("int, float, float"),
	CREATE_WEATHER_CLIMATE_WIND("int, float, float, float"),
	CREATE_WEATHER_STORM("int, Coord, float, int, String, String, String, float, Coord"),
	CREATE_WORSHIP_SITE("Coord position, int, String<PlayerInfo> player, String<TribeType> tribe, int, int"),
	EDIT_LEVEL(""),
	FIRE_FLY_SPELL_REWARD_PROB("SpellSeedInfo type, float probability"),
	FLY_BY_FILE("String path"),
	HEIGHT_CHANGE("Coord position, int"),
	LINK_FOOTPATH("int footpathId"),
	LOAD_COMPUTER_PLAYER_PERSONALLTY("int, Coord"),
	LOAD_LANDSCAPE("String path"),
	MAKE_LAST_OBJECT_ARTIFACT("int, String, float"),
	MULTIPLAYER_DEBUG("int, int"),
	SET_A_TOWNS_INFLUENCE_MULTIPLIER("int townId, float multiplier"),	//Never found
	SET_COMPUTER_PLAYER_CREATURE_LIKE("String, String"),
	SET_COMPUTER_PLAYER_PERSONALLTY("String, Coord, float"),
	SET_GLOBAL_LAND_BALANCE("int<BalanceInfo> property, float multiplier"),
	SET_INTERACT_DESIRE("float"),
	SET_LAND_BALANCE("String, int, float"),
	SET_LAND_NUMBER("int number"),
	SET_LOST_TOWN_SCALE("float scale = 1.0"),
	SET_NIGHTTIME("float dayDuration = 1700.0, float nightToDayRatio = 0.083, float dawnDuskRatio = 0.04"),
	SET_PLAYER_INFLUENCE_MULTIPLIER("float multiplier"),
	SET_TOWN_BALANCE_BELIEF_SCALE("int townId, float scale = 1.0"),	//Never found
	SET_TOWN_BELIEF("int townId, String<PlayerInfo> player, float belief", "townId"),
	SET_TOWN_BELIEF_CAP("int townId, String<PlayerInfo> player, float belief", "townId"),
	SET_TOWN_CONGREGATION_POS("int townId, Coord position"),
	SET_TOWN_INFLUENCE_MULTIPLIER("float multiplier"),
	SET_TOWN_UNINHABITABLE("int townId"),
	START_CAMERA_POS("Coord position"),
	START_GAME_MESSAGE("String message, int numPlayers"),
	TOGGLE_COMPUTER_PLAYER("String<PlayerInfo> player, bool toggle = 1"),
	TOWN_DESIRE_BOOST("int townId, String<DesireInfo> type, float balance"),
	TOWN_NEEDS_POS("int townId, Coord position"),	//Never found
	VERSION("float version");
	
	static {
		CREATE_PLANNED_ABODE.altCommand = CREATE_ABODE;
		CREATE_PLANNED_CITADEL.altCommand = CREATE_CITADEL;
		CREATE_PLANNED_SPELL_ICON.altCommand = CREATE_SPELL_ICON;
		CREATE_PLANNED_WALL_SECTION.altCommand = CREATE_WALL_SECTION;
		CREATE_PLANNED_WORSHIP_SITE.altCommand = CREATE_WORSHIP_SITE;
		
		CREATE_ABODE.altCommand = CREATE_PLANNED_ABODE;
		CREATE_CITADEL.altCommand = CREATE_PLANNED_CITADEL;
		CREATE_SPELL_ICON.altCommand = CREATE_PLANNED_SPELL_ICON;
		CREATE_WALL_SECTION.altCommand = CREATE_PLANNED_WALL_SECTION;
		CREATE_WORSHIP_SITE.altCommand = CREATE_PLANNED_WORSHIP_SITE;
		
		CREATE_TOWN_CENTRE.altCommand = CREATE_PLANNED_ABODE;
		
		CREATE_NEW_TREE.altCommand = CREATE_DEAD_TREE;
		CREATE_DEAD_TREE.altCommand = CREATE_NEW_TREE;
	}
	
	public final Argument[] args;
	public final byte type;
	public final byte position;
	public final byte secondaryCoords;
	public final byte elevation;
	public final byte rotation;
	public final byte pitch;
	public final byte roll;
	public final byte scale;
	public final byte age;
	public final byte player;
	public final byte town;
	public final byte flock;
	public final byte forest;
	public final byte stream;
	
	public final byte id;
	public final byte parent;
	
	public final String objectDisplayName;
	public final boolean planned;
	
	private Command altCommand;
	
	Command(String sArgs) {
		this(sArgs, null);
	}
	
	Command(String sArgs, String parentName) {
		if (sArgs == null || sArgs.isEmpty()) {
			this.args = new Argument[0];
			this.type = -1;
			this.position = -1;
			this.secondaryCoords = -1;
			this.elevation = -1;
			this.rotation = -1;
			this.pitch = -1;
			this.roll = -1;
			this.scale = -1;
			this.age = -1;
			this.player = -1;
			this.town = -1;
			this.flock = -1;
			this.forest = -1;
			this.id = -1;
			this.parent = -1;
			this.stream = -1;
		} else {
			String[] sArgArray = sArgs.split("\\s*,\\s*", -1);
			this.args = new Argument[sArgArray.length];
			byte typeIndex = -1;
			byte posIndex = -1;
			byte secCoordIndex = -1;
			byte elevIndex = -1;
			byte rotIndex = -1;
			byte pitchIndex = -1;
			byte rollIndex = -1;
			byte sclIndex = -1;
			byte ageIndex = -1;
			byte playerIndex = -1;
			byte townIndex = -1;
			byte flockIndex = -1;
			byte forestIndex = -1;
			byte streamIndex = -1;
			byte idIndex = -1;
			byte parentIndex = -1;
			for (int i = 0; i < args.length; i++) {
				Argument arg = Argument.parse(i, sArgArray[i]);
				args[i] = arg;
				if ("type".equals(arg.name)) {
					typeIndex = (byte)i;
				} else if ("position".equals(arg.name)) {
					posIndex = (byte)i;
				} else if ("elevation".equals(arg.name)) {
					elevIndex = (byte)i;
				} else if ("rotation".equals(arg.name)) {
					rotIndex = (byte)i;
				} else if ("pitch".equals(arg.name)) {
					pitchIndex = (byte)i;
				} else if ("roll".equals(arg.name)) {
					rollIndex = (byte)i;
				} else if ("scale".equals(arg.name)) {
					sclIndex = (byte)i;
				} else if ("age".equals(arg.name)) {
					ageIndex = (byte)i;
				} else if ("player".equals(arg.name)) {
					playerIndex = (byte)i;
					if (parentName == null || parentName.equals(arg.name)) {
						parentIndex = (byte)i;
					}
				} else if ("townId".equals(arg.name)) {
					townIndex = (byte)i;
					if ("CREATE_TOWN".equals(name())) {
						idIndex = (byte)i;
					} else if (parentName == null || parentName.equals(arg.name)) {
						parentIndex = (byte)i;
					}
				} else if ("flockId".equals(arg.name)) {
					flockIndex = (byte)i;
					if ("CREATE_FLOCK".equals(name())) {
						idIndex = (byte)i;
					} else if (parentName == null || parentName.equals(arg.name)) {
						parentIndex = (byte)i;
					}
				} else if ("forestId".equals(arg.name)) {
					forestIndex = (byte)i;
					if ("CREATE_FOREST".equals(name())) {
						idIndex = (byte)i;
					} else if (parentName == null || parentName.equals(arg.name)) {
						parentIndex = (byte)i;
					}
				} else if ("streamId".equals(arg.name)) {
					streamIndex = (byte)i;
					if ("CREATE_STREAM".equals(name())) {
						idIndex = (byte)i;
					} else if (parentName == null || parentName.equals(arg.name)) {
						parentIndex = (byte)i;
					}
				} else if (arg.type == ArgType.COORD && arg.name != null) {
					secCoordIndex = (byte)i;
				}
			}
			this.type = typeIndex;
			this.position = posIndex;
			this.secondaryCoords = secCoordIndex;
			this.elevation = elevIndex;
			this.rotation = rotIndex;
			this.pitch = pitchIndex;
			this.roll = rollIndex;
			this.scale = sclIndex;
			this.age = ageIndex;
			this.player = playerIndex;
			this.town = townIndex;
			this.flock = flockIndex;
			this.forest = forestIndex;
			this.stream = streamIndex;
			this.id = idIndex;
			this.parent = parentIndex;
		}
		this.planned = this.name().contains("PLANNED");
		String s = this.name()
				.replace("CREATE_", "")
				.replace("SET_", "")
				.replace("NEW_", "")
				.replaceAll("_", " ").toLowerCase();
		this.objectDisplayName = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}
	
	public boolean hasAltVersion() {
		return altCommand != null;
	}
	
	public Command getAltCommand() {
		return altCommand;
	}
	
	public boolean hasPosition() {
		return position >= 0;
	}
	
	public String getArgsString() {
		StringBuilder b = new StringBuilder();
		if (args.length > 0) {
			b.append(args[0].toString());
			for (int i = 1; i < args.length; i++) {
				b.append(", ");
				b.append(args[i].toString());
			}
		}
		return b.toString();
	}
	
	public String getCStyleSignature() {
		return "void " + this + "(" + getArgsString() + ")";
	}
	
	
	public static Command fromCode(int code) throws IllegalArgumentException {
		Command[] commands = values();
		if (code < 0 || code >= commands.length) {
			throw new IllegalArgumentException(""+code);
		}
		return commands[code];
	}
	
	
	public static class Argument {
		public final int index;
		public final ArgType type;
		public final ArgType objectClass;
		/**The guessed name of the argument. May be null if we don't know what this argument means.
		 */
		public final String name;
		/**Tells if this argument may occurr a variable number of times
		 */
		public final Object defaultValue;
		
		public Argument(int position, ArgType type, ArgType objectClass, String name, Object defaultValue) {
			this.index = position;
			this.type = type;
			this.objectClass = objectClass;
			this.name = name;
			this.defaultValue = defaultValue;
		}
		
		private static Argument parse(int position, String expr) {
			String[] tks = expr.split("\\s*=\\s*", 2);
			String sDefaultValue = tks.length == 2 ? tks[1] : null;
			tks = tks[0].split("\\s+");
			String strType = tks[0].trim();
			String name = tks.length >= 2 ? tks[1].trim() : null;
			tks = strType.split("[<>]");
			ArgType type = ArgType.fromKeyword(tks[0]);
			ArgType objectClass = tks.length == 1 ? null : ArgType.fromKeyword(tks[1]);
			Object defaultValue = objectClass != null ? objectClass.defaultValue : type.defaultValue;
			if (sDefaultValue != null) {
				if (sDefaultValue.startsWith("'")) {
					sDefaultValue = sDefaultValue.substring(1, sDefaultValue.length() - 2);
				}
				ArgType effectiveType = objectClass != null ? objectClass : type;
				if (effectiveType.enumClass != null) {
					for (Object c : effectiveType.enumClass.getEnumConstants()) {
						Enum<?> e = (Enum<?>)c;
						if (e.name().equals(sDefaultValue)) {
							defaultValue = e;
							break;
						}
					}
				} else {
					switch (effectiveType) {
						case COORD:
							defaultValue = new LHXCoord(sDefaultValue);
							break;
						case FLOAT:
							defaultValue = Float.valueOf(sDefaultValue);
							break;
						case INT:
							defaultValue = Integer.valueOf(sDefaultValue);
							break;
						case STRING:
							defaultValue = sDefaultValue;
							break;
						default:
					}
				}
			}
			if (type == ArgType.STRING) {
				defaultValue = String.valueOf(defaultValue);
			} else if (type == ArgType.INT && defaultValue.getClass().isEnum()) {
				defaultValue = ((Enum<?>)defaultValue).ordinal();
			}
			return new Argument(position, type, objectClass, name, defaultValue);
		}
		
		public ArgType getEffectiveType() {
			return objectClass != null ? objectClass : type;
		}
		
		@Override
		public String toString() {
			String s = type.toString();
			if (name != null) {
				s += " " + name;
			}
			return s;
		}
		
		@Override
		public boolean equals(Object obj) {
			Argument other = (Argument)obj;
			if (this.type != other.type) return false;
			if (this.objectClass != null && !this.objectClass.equals(other.objectClass)) return false;
			return true;
		}
		
		@Override
		public int hashCode() {
			int r = type.ordinal();
			if (objectClass != null) r += objectClass.hashCode();
			return r;
		}
	}
	
	
	public enum ArgType {
		INT("int", 0),
		BOOL("bool", 0),
		FLOAT("float", 0f),
		COORD("Coord", new LHXCoord("0,0")),
		STRING("String", ""),
		ABODE(LHXAbodeInfo.class, LHXAbodeInfo.NORSE_ABODE_A),
		ANIMAL(AnimalInfo.class, AnimalInfo.COW),
		BALANCE(BalanceInfo.class, BalanceInfo.ARTIFACT_SPEED),
		BIG_FOREST(BigForestInfo.class, BigForestInfo.BIG_FOREST_1),
		CREATURE(CreatureType.class, CreatureType.APE),
		DESIRE(DesireInfo.class, DesireInfo.Abodes),
		FEATURE(FeatureInfo.class, FeatureInfo.FEATURE_INFO_ARK),
		FIELD(FieldTypeInfo.class, FieldTypeInfo.WHEAT),
		MOBILE_OBJECT(MobileObjectInfo.class, MobileObjectInfo.ARK),
		MOBILE_STATIC(MobileStaticInfo.class, MobileStaticInfo.BONFIRE),
		PLAYER(PlayerInfo.class, PlayerInfo.PLAYER_ONE),
		POT(PotInfo.class, PotInfo.FOOD_POT),
		SPELL_SEED(SpellSeedInfo.class, SpellSeedInfo.FIRE),
		TREE(TreeInfo.class, TreeInfo.OAK),
		TRIBE(TribeType.class, TribeType.NORSE),
		VILLAGER(LHXVillagerInfo.class, LHXVillagerInfo.NORSE_FARMER);
		
		private static final Map<String, ArgType> map = new HashMap<>();
		
		static {
			for (ArgType t : values()) {
				map.put(t.keyword, t);
			}
		}
		
		public final String keyword;
		public final Class<?> enumClass;
		public final Object defaultValue;
		
		private ArgType(String keyword, Object defaultValue) {
			this(keyword, null, defaultValue);
		}
		
		private ArgType(Class<?> enumClass, Object defaultValue) {
			this(enumClass.getSimpleName(), enumClass, defaultValue);
		}
		
		private ArgType(String keyword, Class<?> enumClass, Object defaultValue) {
			this.keyword = keyword;
			this.enumClass = enumClass;
			this.defaultValue = defaultValue;
		}
		
		@Override
		public String toString() {
			return keyword;
		}
		
		public static ArgType fromKeyword(String keyword) {
			ArgType t = map.get(keyword);
			if (t == null) throw new IllegalArgumentException("Invalid ArgType: "+keyword);
			return t;
		}
	}
}
