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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.ld.bw.info.LHXAbodeInfo;
import it.ld.bw.info.LHXVillagerInfo;
import it.ld.bw.info.PlayerInfo;
import it.ld.bw.info.SpellSeedInfo;
import it.ld.bw.info.TreeInfo;
import it.ld.bw.info.TribeType;
import it.ld.bw.lhx.Command.ArgType;
import it.ld.bw.lhx.Command.Argument;
import it.ld.utils.Listeners;
import it.ld.utils.UChangeListener.EventType;

public class Statement {
	public enum Property {TEXT, COMMAND, ARGS, VALID}
	public final Listeners listeners = new Listeners(this);
	
	private String indent = "";
	private String text;
	private Command command;
	private List<Parameter> args;
	private boolean valid = true;
	
	private LHXFile lhx;
	private int hash = 0;
	
	public Statement(String text) {
		this.text = text;
	}
	
	public Statement(String indent, String verb, List<Token> args) {
		this(indent, verb, args, false);
	}
	
	public Statement(String indent, String verb, List<Token> args, boolean allowBadArgs) {
		this.indent = indent;
		this.command = Command.valueOf(verb.toUpperCase());
		this.args = new ArrayList<>(command.args.length);
		for (int i = 0; i < command.args.length; i++) {
			if (allowBadArgs) {
				Token arg = i < args.size() ? args.get(i) : null;
				this.args.add(new Parameter(command.args[i], arg, true));
			} else {
				this.args.add(new Parameter(command.args[i], args.get(i)));
			}
		}
	}
	
	public Statement(Command command) {
		this.command = command;
		this.args = new ArrayList<>(command.args.length);
		for (int i = 0; i < command.args.length; i++) {
			Argument arg = command.args[i];
			this.args.add(new Parameter(arg, arg.defaultValue));
		}
	}
	
	public Statement(Command command, List<Object> args) {
		this.command = command;
		this.args = new ArrayList<>(command.args.length);
		for (int i = 0; i < command.args.length; i++) {
			this.args.add(new Parameter(command.args[i], args.get(i)));
		}
	}
	
	public Statement(Command command, Object...args) {
		this.command = command;
		this.args = new ArrayList<>(command.args.length);
		for (int i = 0; i < command.args.length; i++) {
			this.args.add(new Parameter(command.args[i], args[i]));
		}
	}
	
	@Override
	public Statement clone() {
		Statement r = new Statement("");
		r.set(this);
		return r;
	}
	
	public void set(Statement ref) {
		this.indent = ref.indent;
		this.text = ref.text;
		if (ref.isCommand() && this.command == ref.command) {
			for (int i = 0; i < ref.command.args.length; i++) {
				this.setValue(i, ref.getArg(i).getValue());
			}
		} else {
			this.command = ref.command;
			if (ref.args != null) {
				this.args = new ArrayList<>(ref.args.size());
				for (Parameter refParam : ref.args) {
					this.args.add(refParam.clone());
				}
			} else {
				this.args = null;
			}
			this.valid = ref.valid;
			this.hash = 0;
			listeners.notify(EventType.CHANGE, Property.COMMAND);
		}
	}
	
	public void clear() {
		this.indent = "";
		this.text = "";
		this.command = null;
		this.args = null;
		this.valid = true;
		this.hash = 0;
		listeners.notify(EventType.CHANGE);
	}
	
	public LHXFile getLHX() {
		return lhx;
	}
	
	void setLHX(LHXFile lhx) {
		this.lhx = lhx;
	}
	
	public boolean isCommand() {
		return command != null;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public void setInvalid() {
		setInvalid(this.toString());
	}
	
	public void setInvalid(String text) {
		this.text = text;
		this.hash = 0;
		if (this.valid) {
			this.valid = false;
			listeners.notify(EventType.CHANGE, Property.VALID, !valid, valid);
		}
	}
	
	public void resetInvalid() {
		if (isCommand() && !valid) {
			text = null;
			valid = true;
			listeners.notify(EventType.CHANGE);
		}
	}
	
	public Command getCommand() {
		return command;
	}
	
	public void toggleAltCommand() {
		if (command.hasAltVersion()) {
			Command oldCommand = this.command;
			Command newCommand = this.command.getAltCommand();
			if (oldCommand == Command.CREATE_PLANNED_ABODE) {
				String type = this.getTypeString();
				if (type.endsWith("_TOWN_CENTRE")) {
					newCommand = Command.CREATE_TOWN_CENTRE;
				}
			}
			ArrayList<Parameter> newArgs = new ArrayList<>(newCommand.args.length);
			for (int i = 0; i < newCommand.args.length; i++) {
				Argument newArg = newCommand.args[i];
				if (newArg.name != null) {
					Parameter oldParam = this.getArg(newArg.name);
					if (oldParam != null) {
						newArgs.add(new Parameter(newCommand.args[i], oldParam.getValue()));
						continue;
					}
				}
				newArgs.add(new Parameter(newCommand.args[i], newArg.defaultValue));
			}
			this.command = newCommand;
			this.args = newArgs;
			listeners.notify(EventType.CHANGE, Property.COMMAND, oldCommand, newCommand);
		}
	}
	
	public boolean isTown() {
		return command == Command.CREATE_TOWN;
	}
	
	public boolean isFlock() {
		return command == Command.CREATE_FLOCK;
	}
	
	public boolean isForest() {
		return command == Command.CREATE_FOREST;
	}
	
	public boolean isStream() {
		return command == Command.CREATE_STREAM;
	}
	
	public boolean isTownCentre() {
		return command == Command.CREATE_TOWN_CENTRE ||
				(command == Command.CREATE_PLANNED_ABODE && getTypeString().endsWith("_TOWN_CENTRE"));
	}
	
	public boolean hasParent() {
		return command != null && command.parent >= 0;
	}
	
	public boolean isChildOfPlayer() {
		return command != null && command.parent >= 0 && command.parent == command.player;
	}
	
	public boolean isChildOfTown() {
		return command != null && command.parent >= 0 && command.parent == command.town;
	}
	
	public boolean isChildOfFlock() {
		return command != null && command.parent >= 0 && command.parent == command.flock;
	}
	
	public boolean isChildOfForest() {
		return command != null && command.parent >= 0 && command.parent == command.forest;
	}
	
	public boolean isChildOfStream() {
		return command != null && command.parent >= 0 && command.parent == command.stream;
	}
	
	public int getParentInt() {
		if (command.parent == command.town) {
			return getTown();
		} else if (command.parent == command.flock) {
			return getFlock();
		} else if (command.parent == command.forest) {
			return getForest();
		} else if (command.parent == command.stream) {
			return getStream();
		} else {
			throw new RuntimeException("Statement has no parent or parent isn't an int: " + this);
		}
	}
	
	public Parameter getArg(int index) {
		return args.get(index);
	}
	
	public Parameter getArg(String name) {
		for (Parameter arg : args) {
			if (name.equals(arg.getName())) {
				return arg;
			}
		}
		return null;
	}
	
	public Object getType() {
		if (command.type < 0) return null;
		Parameter arg = getArg(command.type);
		return arg.getValue();
	}
	
	public int getTypeInt() {
		if (command.type < 0) return -1;
		Parameter arg = getArg(command.type);
		return arg.getInt();
	}
	
	public String getTypeString() {
		if (command.type < 0) return null;
		Parameter arg = getArg(command.type);
		return arg.getString();
	}
	
	public void setType(Object val) {
		if (command.type < 0) return;
		Parameter arg = getArg(command.type);
		if (arg.getValue().getClass() != val.getClass()) {
			throw new IllegalArgumentException("Argument " + arg.def.index + " (" + arg.def.name + ") must be of type " + arg.getValue().getClass().getSimpleName());
		}
		setValue(arg, val);
	}
	
	public LHXCoord getPosition() {
		if (command.position < 0) return null;
		Parameter arg = getArg(command.position);
		return arg.getCoord();
	}
	
	public void setPosition(LHXCoord val) {
		if (command.position < 0) return;
		setCoord(command.position, val);
	}
	
	public LHXCoord getSecondaryCoords() {
		if (command.secondaryCoords < 0) return null;
		Parameter arg = getArg(command.secondaryCoords);
		return arg.getCoord();
	}
	
	public void setSecondaryCoords(float x, float z) {
		setSecondaryCoords(new LHXCoord(x, z));
	}
	
	public void setSecondaryCoords(LHXCoord val) {
		if (command.secondaryCoords < 0) return;
		setCoord(command.secondaryCoords, val);
	}
	
	public void setPosition(float x, float z) {
		if (command.position < 0) return;
		setCoord(command.position, x, z);
	}
	
	public float getElevation() {
		if (command.elevation < 0) return 0;
		Parameter arg = getArg(command.elevation);
		return arg.getFloat();
	}
	
	public void setElevation(float val) {
		if (command.elevation < 0) return;
		setFloat(command.elevation, val);
	}
	
	public float getRotation() {
		if (command.rotation < 0) return 0;
		Parameter arg = getArg(command.rotation);
		if (arg.getType() == ArgType.FLOAT) {
			return arg.getFloat();
		} else if (arg.getType() == ArgType.INT) {
			return arg.getInt() * 0.001f;
		} else {
			assert(false);
			return 0;
		}
	}
	
	public void setRotation(float val) {
		if (command.rotation < 0) return;
		Parameter arg = getArg(command.rotation);
		if (arg.getType() == ArgType.FLOAT) {
			setFloat(command.rotation, val);
		} else if (arg.getType() == ArgType.INT) {
			setInt(command.rotation, (int)(val * 1000));
		} else {
			assert(false);
		}
	}
	
	public float getPitch() {
		if (command.pitch < 0) return 0;
		Parameter arg = getArg(command.pitch);
		return arg.getFloat();
	}
	
	public void setPitch(float val) {
		if (command.pitch < 0) return;
		setFloat(command.pitch, val);
	}
	
	public float getRoll() {
		if (command.roll < 0) return 0;
		Parameter arg = getArg(command.roll);
		return arg.getFloat();
	}
	
	public void setRoll(float val) {
		if (command.roll < 0) return;
		setFloat(command.roll, val);
	}
	
	public float getScale() {
		if (command.scale < 0) return 1f;
		Parameter arg = getArg(command.scale);
		if (arg.getType() == ArgType.FLOAT) {
			return arg.getFloat();
		} else if (arg.getType() == ArgType.INT) {
			return arg.getInt() * 0.001f;
		} else {
			assert(false);
			return 0;
		}
	}
	
	public void setScale(float val) {
		if (command.scale < 0) return;
		Parameter arg = getArg(command.scale);
		if (arg.getType() == ArgType.FLOAT) {
			setFloat(command.scale, val);
		} else if (arg.getType() == ArgType.INT) {
			setInt(command.scale, (int)(val * 1000));
		} else {
			assert(false);
		}
	}
	
	public int getAge() {
		if (command.age < 0) return -1;
		Parameter arg = getArg(command.age);
		return arg.getInt();
	}
	
	public void setAge(int val) {
		if (command.age < 0) return;
		setInt(command.age, val);
	}
	
	public String getPlayer() {
		if (command.player < 0) return null;
		Parameter arg = getArg(command.player);
		return arg.getString();
	}
	
	public void setPlayer(String val) {
		if (command.player < 0) return;
		setString(command.player, val);
	}
	
	public void setPlayer(PlayerInfo val) {
		if (command.player < 0) return;
		setString(command.player, val.name());
	}
	
	public int getTown() {
		if (command.town < 0) return -1;
		Parameter arg = getArg(command.town);
		return arg.getInt();
	}
	
	public void setTown(int val) {
		if (command.town < 0) return;
		setInt(command.town, val);
	}
	
	public int getFlock() {
		if (command.flock < 0) return -1;
		Parameter arg = getArg(command.flock);
		return arg.getInt();
	}
	
	public void setFlock(int val) {
		if (command.flock < 0) return;
		setInt(command.flock, val);
	}
	
	public int getForest() {
		if (command.forest < 0) return -1;
		Parameter arg = getArg(command.forest);
		return arg.getInt();
	}
	
	public void setForest(int val) {
		if (command.forest < 0) return;
		setInt(command.forest, val);
	}
	
	public int getStream() {
		if (command.stream < 0) return -1;
		Parameter arg = getArg(command.stream);
		return arg.getInt();
	}
	
	public void setStream(int val) {
		if (command.stream < 0) return;
		setInt(command.stream, val);
	}
	
	public int getId() {
		if (command.id < 0) return -1;
		Parameter arg = getArg(command.id);
		return arg.getInt();
	}
	
	public void setId(int id) {
		if (command.id < 0) return;
		setInt(command.id, id);
	}
	
	public TribeType getEffectiveTribe() {
		if (command.type >= 0) {
			Parameter param = getArg(command.type);
			ArgType effectiveType = param.def.getEffectiveType();
			if (effectiveType == ArgType.ABODE) {
				LHXAbodeInfo abode = param.getAbode();
				return abode.tribe;
			} else if (effectiveType == ArgType.VILLAGER) {
				LHXVillagerInfo villager = param.getVillager();
				return villager.tribe;
			}
		}
		//
		{
			Parameter param = getArg("tribe");
			if (param != null) {
				return param.getTribe();
			}
		}
		return null;
	}
	
	public void setCoord(int index, LHXCoord val) {
		Parameter arg = args.get(index);
		if (arg.getType() != ArgType.COORD) {
			throw new IllegalArgumentException("Expected " + arg.getType());
		}
		setValue(arg, val);
	}
	
	public void setCoord(int index, float x, float z) {
		setCoord(index, new LHXCoord(x, z));
	}
	
	public void setFloat(int index, float val) {
		Parameter arg = args.get(index);
		if (arg.getType() != ArgType.FLOAT) throw new IllegalArgumentException("Expected " + arg.getType());
		setValue(arg, val);
	}
	
	public void setInt(int index, int val) {
		Parameter arg = args.get(index);
		if (arg.getType() != ArgType.INT) throw new IllegalArgumentException("Expected " + arg.getType());
		setValue(arg, val);
	}
	
	public void setBool(int index, boolean val) {
		Parameter arg = args.get(index);
		if (arg.getType() != ArgType.BOOL) throw new IllegalArgumentException("Expected " + arg.getType());
		setValue(arg, val ? 1 : 0);
	}
	
	public void setSpell(int index, SpellSeedInfo val) {
		Parameter arg = args.get(index);
		if (arg.getType() != ArgType.SPELL_SEED) throw new IllegalArgumentException("Expected " + arg.getType());
		setValue(arg, val);
	}
	
	public void setString(int index, String val) {
		Parameter arg = args.get(index);
		if (arg.getType() != ArgType.STRING) throw new IllegalArgumentException("Expected " + arg.getType());
		setValue(arg, val);
	}
	
	public void setVillager(int index, int val) {
		Parameter arg = args.get(index);
		if (arg.getType() != ArgType.VILLAGER) throw new IllegalArgumentException("Expected " + arg.getType());
		setValue(arg, val);
	}
	
	public void setCoord(String argName, float x, float z) {
		setCoord(argName, new LHXCoord(x, z));
	}
	
	public void setCoord(String argName, LHXCoord val) {
		Parameter arg = getArg(argName);
		setCoord(arg.def.index, val);
	}
	
	public void setFloat(String argName, float val) {
		Parameter arg = getArg(argName);
		setFloat(arg.def.index, val);
	}
	
	public void setInt(String argName, int val) {
		Parameter arg = getArg(argName);
		setInt(arg.def.index, val);
	}
	
	public void setBool(String argName, boolean val) {
		Parameter arg = getArg(argName);
		setBool(arg.def.index, val);
	}
	
	public void setSpell(String argName, SpellSeedInfo val) {
		Parameter arg = getArg(argName);
		setSpell(arg.def.index, val);
	}
	
	public void setString(String argName, String val) {
		Parameter arg = getArg(argName);
		setString(arg.def.index, val);
	}
	
	public void setVillager(String argName, int val) {
		Parameter arg = getArg(argName);
		setVillager(arg.def.index, val);
	}
	
	public void setValue(int index, Object value) {
		setValue(args.get(index), value);
	}
	
	public void setValue(Parameter param, Object value) {
		if (param.value != value && (param.value != null && !param.value.equals(value))) {
			Object oldValue = param.value;
			param.value = value;
			this.hash = 0;
			listeners.notify(EventType.CHANGE, Property.ARGS, oldValue, param.value, param.def.index);
		}
	}
	
	@Override
	public int hashCode() {
		if (hash == 0) {
			if (command != null) {
				hash = command.hashCode() ^ args.hashCode();
			} else if (text != null) {
				hash = text.hashCode();
			}
		}
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Statement)) return false;
		Statement other = (Statement)obj;
		if (this.hashCode() != other.hashCode()) return false;
		if ((this.text == null) != (other.text == null)) return false;
		if (this.text != null && !this.text.equals(other.text)) return false;
		if (this.command != other.command) return false;
		if (this.command != null && !this.args.equals(other.args)) return false;
		return true;
	}
	
	private String commandToString() {
		String[] sArgs = new String[args.size()];
		int i = 0;
		for (Parameter arg : args) {
			sArgs[i++] = arg.toString();
		}
		return indent + command.name() + "(" + String.join(", ", sArgs) + ")";
	}
	
	@Override
	public String toString() {
		if (isCommand() && valid) {
			return commandToString();
		} else {
			return text != null ? text : "";
		}
	}
	
	
	public class Parameter {
		public final Argument def;
		private Object value;
		
		public Parameter(Argument arg, Token token) {
			this(arg, token, false);
		}
		
		public Parameter(Argument arg, Token token, boolean allowBadValue) {
			this.def = arg;
			if (token == null && allowBadValue) return;
			try {
				switch (arg.type) {
					case COORD:
						value = new LHXCoord(token.stringVal());
						break;
					case FLOAT:
						value = token.floatVal();
						break;
					case INT:
						value = token.intVal();
						break;
					case BOOL:
						value = token.intVal();
						break;
					case SPELL_SEED:
						value = SpellSeedInfo.valueOf(token.value);
						break;
					case STRING:
						value = token.stringVal();
						break;
					case VILLAGER:
						value = LHXVillagerInfo.valueOf(token.value);
						break;
					case ABODE:
						value = LHXAbodeInfo.valueOf(token.value);
						break;
					case TREE:
						value = TreeInfo.valueOf(token.value);
						break;
					case TRIBE:
						value = TribeType.valueOf(token.value);
						break;
					default:
						throw new IllegalArgumentException("Type " + arg.type.keyword + " cannot be used directly");
				}
			} catch (Exception e) {
				if (!allowBadValue) throw e;
				value = token != null ? token.value : null;
			}
		}
		
		public Parameter(Argument arg, Object val) {
			this.def = arg;
			switch (arg.type) {
				case COORD:
					value = (LHXCoord)val;
					break;
				case FLOAT:
					value = (Float)val;
					break;
				case INT:
					value = (Integer)val;
					break;
				case BOOL:
					value = (Integer)val;
					break;
				case SPELL_SEED:
					value = (SpellSeedInfo)val;
					break;
				case STRING:
					value = (String)val;
					break;
				case VILLAGER:
					value = (LHXVillagerInfo)val;
					break;
				case ABODE:
					value = (LHXAbodeInfo)val;
					break;
				case TREE:
					value = (TreeInfo)val;
					break;
				case TRIBE:
					value = (TribeType)val;
					break;
				default:
					throw new IllegalArgumentException("Type " + arg.type.keyword + " cannot be used directly");
			}
		}
		
		@Override
		public Parameter clone() {
			return new Parameter(this.def, this.value);
		}
		
		public ArgType getType() {
			return def.type;
		}
		
		public String getName() {
			return def.name;
		}
		
		public Object getValue() {
			return value;
		}
		
		public LHXCoord getCoord() {
			return (LHXCoord)value;
		}
		
		public float getFloat() {
			return (Float)value;
		}
		
		public int getInt() {
			return (Integer)value;
		}
		
		public boolean getBool() {
			return (Integer)value != 0;
		}
		
		public SpellSeedInfo getSpell() {
			return (SpellSeedInfo)value;
		}
		
		public LHXVillagerInfo getVillager() {
			if (def.type == ArgType.VILLAGER) {
				return (LHXVillagerInfo)value;
			} else if (def.type == ArgType.STRING) {
				return LHXVillagerInfo.valueOf((String)value);
			} else {
				assert(false);
				return null;
			}
		}
		
		public LHXAbodeInfo getAbode() {
			if (def.type == ArgType.ABODE) {
				return (LHXAbodeInfo)value;
			} else if (def.type == ArgType.STRING) {
				return LHXAbodeInfo.valueOf((String)value);
			} else {
				assert(false);
				return null;
			}
		}
		
		public TreeInfo getTree() {
			return (TreeInfo)value;
		}
		
		public TribeType getTribe() {
			if (def.type == ArgType.TRIBE) {
				return (TribeType)value;
			} else if (def.type == ArgType.STRING) {
				return TribeType.valueOf((String)value);
			} else {
				assert(false);
				return null;
			}
		}
		
		public String getString() {
			return (String)value;
		}
		
		@Override
		public String toString() {
			if (def.type == ArgType.STRING) {
				return "\"" + value + "\"";
			} else if (def.type == ArgType.FLOAT) {
				return String.format(Locale.US, "%.6f", value);
			} else {
				return String.valueOf(value);
			}
		}
	}
}
