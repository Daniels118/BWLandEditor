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
package it.ld.bw.lndgui.gfx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.utils.Null;

import it.ld.bw.info.GAbodeInfo;
import it.ld.bw.info.LHXVillagerInfo;
import it.ld.bw.info.PlayerInfo;
import it.ld.bw.lhx.Command;
import it.ld.bw.lhx.LHXCoord;
import it.ld.bw.lhx.LHXFile;
import it.ld.bw.lhx.Statement;
import it.ld.bw.lndgui.SelectionMode;
import it.ld.bw.lndgui.gfx.L3DModelManager.ModelInfo;
import it.ld.bw.lndgui.tools.Selection;
import it.ld.utils.Listeners;
import it.ld.utils.UChangeListener;
import it.ld.utils.UChangeListener.EventType;

public class LHX3D implements AutoCloseable {
	public enum Property {SELECTION, DUPLICATES}
	public final Listeners listeners = new Listeners(this);
	
	private final L3DModelManager modelManager;
	private final LHXFile lhx;
	public final Land3D land3d;
	
	private final Map<Integer, Object3D> towns = new HashMap<>();
	private final Map<Integer, Object3D> flocks = new HashMap<>();
	private final Map<Integer, Object3D> forests = new HashMap<>();
	private final Map<Integer, Object3D> streams = new HashMap<>();
	
	private final VObject3D environment = new VObject3D("Environment");
	private final Map<Command, VObject3D> environmentContainers = new HashMap<>();
	private final VObject3D orphans = new VObject3D("Orphans");
	private final Map<String, Object3D> players = new LinkedHashMap<>();
	private final Map<Statement, Object3D> objects = new IdentityHashMap<>();
	
	private boolean changingSelection = false;
	
	public LHX3D(L3DModelManager modelManager, LHXFile lhx, Land3D land3d) {
		this.modelManager = modelManager;
		this.lhx = lhx;
		this.land3d = land3d;
		for (PlayerInfo playerInfo : PlayerInfo.values()) {
			players.put(playerInfo.name(), new VObject3D(playerInfo.name()));
		}
		lhx.listeners.add(lhxChangeListener);
		processAll();
	}
	
	private final UChangeListener lhxChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LHXFile.Property.STATEMENTS) {
				if (event.getType() == EventType.ADD) {
					if (isAutoSortEnabled() && !event.isLast()) {
						delayAutosort = true;
					}
					Statement stmt = (Statement)event.getNewValue();
					process(stmt);
					stmt.listeners.add(statementChangeListener);
					if (delayAutosort && event.isLast()) {
						delayAutosort = false;
						boolean moved = true;
						while (moved) {	//We need to sort items multiple times until all of them are in their final position
							moved = false;
							for (Object3D obj : objectsToSort) {
								moved |= sort(obj);
							}
						};
						objectsToSort.clear();
					}
				} else if (event.getType() == EventType.REMOVE) {
					Statement stmt = (Statement)event.getOldValue();
					stmt.listeners.remove(statementChangeListener);
					Object3D oldObj = objects.remove(event.getOldValue());
					if (oldObj != null) {
						if (oldObj.isTown()) {
							towns.remove(oldObj.getId());
						} else if (oldObj.isFlock()) {
							flocks.remove(oldObj.getId());
						} else if (oldObj.isForest()) {
							forests.remove(oldObj.getId());
						} else if (oldObj.isStream()) {
							streams.remove(oldObj.getId());
						}
						onObjectDeleted(oldObj);
						for (Object3D child : oldObj.getChildren()) {
							child.setParent(orphans);
						}
						oldObj.listeners.remove(objectChangeListener);
						oldObj.close();
					}
				}
			}
		}
	};
	
	private final UChangeListener statementChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			boolean rebuild = false;
			Statement stmt = (Statement) event.getSource();
			if (event.getProperty() == Statement.Property.COMMAND || event.getProperty() == null) {
				rebuild = true;
			} else if (event.getProperty() == Statement.Property.ARGS) {
				if (event.getIndex() == stmt.getCommand().type) {
					rebuild = true;
				}
			}
			if (rebuild) {
				process(stmt);
			}
			//
			if (event.getProperty() == Statement.Property.ARGS) {
				Object3D obj = objects.get(stmt);
				if (obj != null) {	//Some statements may not generate an object
					Command cmd = stmt.getCommand();
					int argIndex = event.getIndex();
					if (cmd.parent >= 0 && argIndex == cmd.parent) {
						updateParent(obj);
					} else if (cmd == Command.CREATE_ABODE) {
						Coord doorPos = obj.getDoorPosition();
						if (doorPos != null) {
							LHXCoord homePos = new LHXCoord(doorPos.x, doorPos.z);
							List<Object3D> inhabitants = obj.getChildren();
							for (Object3D inhabitant : inhabitants) {
								inhabitant.getStatement().setSecondaryCoords(homePos);
							}
							for (Object3D tObj : objects.values()) {
								Statement tStmt = tObj.getStatement();
								if (tStmt.getCommand() == Command.CREATE_VILLAGER_POS) {
									LHXCoord tPos = tStmt.getSecondaryCoords();
									if (tPos.dst2(homePos.x, homePos.z) <= 1f) {
										tObj.setParent(obj);
									}
								}
							}
						}
					} else if (cmd == Command.CREATE_VILLAGER_POS) {
						Object3D home = obj.getHome();
						obj.setParent(home != null ? home : orphans);
					}
					
					if (cmd.id >= 0 && argIndex == cmd.id) {
						updateLUTs(obj, (Integer)event.getOldValue());
						updateChildren(obj);
					}
				}
			}
		}
	};
	
	private void updateLUTs(Object3D obj, int prevId) {
		Statement stmt = obj.getStatement();
		if (stmt.isCommand()) {
			Command cmd = stmt.getCommand();
			if (cmd == Command.CREATE_TOWN) {
				updateLUT(obj, prevId, towns);
			} else if (cmd == Command.CREATE_FLOCK) {
				updateLUT(obj, prevId, flocks);
			} else if (cmd == Command.CREATE_FOREST) {
				updateLUT(obj, prevId, forests);
			} else if (cmd == Command.CREATE_STREAM) {
				updateLUT(obj, prevId, streams);
			}
		}
	}
	
	private void updateLUT(Object3D obj, int prevId, Map<Integer, Object3D> lut) {
		Statement stmt = obj.getStatement();
		int newId = stmt.getId();
		if (prevId >= 0) {
			Object3D prevObj = lut.get(prevId);
			if (prevObj == obj) {
				lut.remove(prevId);
			}
		}
		if (lut.containsKey(newId)) {
			if (!obj.isDupIdError()) {
				obj.setDupIdError(true);
				listeners.notify(EventType.ADD, Property.DUPLICATES, null, obj);
			}
			//System.err.println("Duplicate object: " + obj);
		} else {
			lut.put(newId, obj);
			if (obj.isDupIdError()) {
				obj.setDupIdError(false);
				listeners.notify(EventType.REMOVE, Property.DUPLICATES, obj, null);
			}
		}
		if (prevId >= 0 && newId != prevId) {
			onIdFreed(obj, prevId);
		}
	}
	
	private boolean processingAll;
	
	private void processAll() {
		processingAll = true;
		try {
			for (Object3D obj : objects.values()) {
				obj.listeners.remove(objectChangeListener);
				obj.close();
			}
			objects.clear();
			for (Statement stmt : lhx.getStatements()) {
				process(stmt);
				stmt.listeners.add(statementChangeListener);
			}
			rebuildHierarchy();
		} finally {
			processingAll = false;
		}
	}
	
	private void process(Statement stmt) {
		if (modelManager == null) return;
		try {
			boolean remove = true;
			if (stmt.isCommand()) {
				CommandImpl impl = commandsImpl[stmt.getCommand().ordinal()];
				if (impl != null) {
					remove = false;
					boolean deleted = false;
					Object3D oldObj = objects.get(stmt);
					if (oldObj != null) {
						Command oldCmd = oldObj.getStatement().getCommand();
						if (oldCmd != stmt.getCommand() || oldCmd == Command.CREATE_ABODE) {
							onObjectDeleted(oldObj);
							deleted = true;
						}
					}
					Object3D obj = impl.run(this, stmt, oldObj != null ? oldObj : new Object3D());
					if (obj != null && (oldObj == null || deleted)) {
						onObjectCreated(obj);
					}
					if (obj != null && oldObj == null) {
						objects.put(stmt, obj);
						obj.listeners.add(objectChangeListener);
					} else if (obj == null && oldObj != null) {
						oldObj.listeners.remove(objectChangeListener);
						oldObj.close();
					}
					if (obj != null) {
						sort(obj);
						obj.setSyntaxError(false);
					}
				} else {
					System.err.println("Command not implemented: " + stmt.getCommand());
				}
			}
			if (remove) {
				Object3D oldObj = objects.remove(stmt);
				if (oldObj != null) {
					onObjectDeleted(oldObj);
					oldObj.listeners.remove(objectChangeListener);
					oldObj.close();
				}
			}
		} catch (Exception e) {
			stmt.setInvalid(null);
			Object3D oldObj = objects.get(stmt);
			if (oldObj != null) {
				oldObj.setSyntaxError(true);
			}
		}
	}
	
	private Object3D getEnvContainer(Object3D obj) {
		Command cmd = obj.getStatement().getCommand();
		VObject3D envContainer = environmentContainers.get(cmd);
		if (envContainer == null) {
			String name = cmd.objectDisplayName;
			if (!name.endsWith("s")) name += "s";
			envContainer = new VObject3D(name, environment);
			environmentContainers.put(cmd, envContainer);
		}
		return envContainer;
	}
	
	private void updateChildren(Object3D obj) {
		Statement stmt = obj.getStatement();
		if (stmt.isCommand()) {
			Command cmd = stmt.getCommand();
			if (cmd == Command.CREATE_TOWN) {
				int townId = stmt.getTown();
				boolean isIndexed = obj == towns.get(townId);
				for (Object3D t : objects.values()) {
					Command tCmd = t.getStatement().getCommand();
					if (tCmd.parent >= 0 && tCmd.parent == tCmd.town) {
						if (isIndexed && t.getStatement().getTown() == townId) {
							t.setParent(obj);
						} else if (t.getParent() == obj) {
							updateParent(t);
						}
					}
				}
			} else if (cmd == Command.CREATE_FLOCK) {
				int flockId = stmt.getFlock();
				boolean isIndexed = obj == flocks.get(flockId);
				for (Object3D t : objects.values()) {
					Command tCmd = t.getStatement().getCommand();
					if (tCmd.parent >= 0 && tCmd.parent == tCmd.flock) {
						if (isIndexed && t.getStatement().getFlock() == flockId) {
							t.setParent(obj);
						} else if (t.getParent() == obj) {
							updateParent(t);
						}
					}
				}
			} else if (cmd == Command.CREATE_FOREST) {
				int forestId = stmt.getForest();
				boolean isIndexed = obj == forests.get(forestId);
				for (Object3D t : objects.values()) {
					Command tCmd = t.getStatement().getCommand();
					if (tCmd.parent >= 0 && tCmd.parent == tCmd.forest) {
						if (isIndexed && t.getStatement().getForest() == forestId) {
							t.setParent(obj);
						} else if (t.getParent() == obj) {
							updateParent(t);
						}
					}
				}
			} else if (cmd == Command.CREATE_STREAM) {
				int streamId = stmt.getStream();
				boolean isIndexed = obj == streams.get(streamId);
				for (Object3D t : objects.values()) {
					Command tCmd = t.getStatement().getCommand();
					if (tCmd.parent >= 0 && tCmd.parent == tCmd.stream) {
						if (isIndexed && t.getStatement().getStream() == streamId) {
							t.setParent(obj);
						} else if (t.getParent() == obj) {
							updateParent(t);
						}
					}
				}
			} else if (cmd == Command.CREATE_ABODE) {
				Coord door = obj.getDoorPosition();
				if (door != null) {
					Coord tDoor = new Coord();
					for (Object3D t : objects.values()) {
						Statement tStmt = t.getStatement();
						if (tStmt.getCommand() == Command.CREATE_VILLAGER_POS) {
							tDoor.set(tStmt.getSecondaryCoords());
							tDoor.y = door.y;
							if (door.dst2(tDoor) <= 1f) {
								t.setParent(obj);
							} else if (t.getParent() == obj) {
								updateParent(t);
							}
						}
					}
				}
			}
		}
	}
	
	private void updateParent(Object3D obj) {
		Statement stmt = obj.getStatement();
		if (stmt.isCommand()) {
			Command cmd = stmt.getCommand();
			if (cmd.parent >= 0) {
				Object3D parent = null;
				if (cmd.parent == cmd.player) {
					parent = players.get(stmt.getPlayer());
				} else if (cmd.parent == cmd.town) {
					int townId = stmt.getTown();
					parent = townId >= 0 ? towns.get(townId) : getEnvContainer(obj);
				} else if (cmd.parent == cmd.flock) {
					int flockId = stmt.getFlock();
					parent = flockId >= 0 ? flocks.get(flockId) : getEnvContainer(obj);
				} else if (cmd.parent == cmd.forest) {
					int forestId = stmt.getForest();
					parent = forestId >= 0 ? forests.get(forestId) : getEnvContainer(obj);
				} else if (cmd.parent == cmd.stream) {
					int streamId = stmt.getStream();
					parent = streamId >= 0 ? streams.get(streamId) : getEnvContainer(obj);
				}
				obj.setParent(parent != null ? parent : orphans);
			} else if (cmd == Command.CREATE_VILLAGER_POS) {
				Object3D parent = obj.getParent();
				if (parent != null && parent.hasDoor()) {
					Coord door = parent.getDoorPosition();
					obj.getStatement().setSecondaryCoords(door.x, door.z);
				} else {
					Object3D home = obj.getHome();
					obj.setParent(home == null ? orphans : home);
				}
			} else {
				obj.setParent(getEnvContainer(obj));
			}
		} else {
			obj.setParent(null);
		}
	}
	
	private void onObjectCreated(Object3D obj) {
		updateLUTs(obj, -1);
		updateChildren(obj);
		updateParent(obj);
	}
	
	private void onObjectDeleted(Object3D obj) {
		Statement stmt = obj.getStatement();
		if (stmt.isCommand()) {
			Command cmd = stmt.getCommand();
			if (cmd.id >= 0) {	//If this is a container
				onIdFreed(obj, obj.getId());
			}
			//
			Object3D parent = obj.getParent();
			obj.setParent(null);
			if (obj.isTownCentre() && parent != null) {
				Object3D.updateTownSpells(parent);
			}
		} else {
			//This may happen if the statement turns into a comment or blank
			obj.setParent(null);
		}
	}
	
	private void onIdFreed(Object3D object, int id) {
		Command cmd = object.getStatement().getCommand();
		//Look for a new container with the same ID (among duplicates)
		Object3D replacement = null;
		for (Object3D tObj : objects.values()) {
			Statement tStmt = tObj.getStatement();
			if (tStmt.getCommand() == cmd && tStmt.getId() == id) {
				replacement = tObj;
				break;
			}
		}
		//
		if (replacement != null) {
			//Replace the container in the proper set
			if (cmd == Command.CREATE_TOWN) {
				towns.put(id, replacement);
			} else if (cmd == Command.CREATE_FLOCK) {
				flocks.put(id, replacement);
			} else if (cmd == Command.CREATE_FOREST) {
				forests.put(id, replacement);
			} else if (cmd == Command.CREATE_STREAM) {
				streams.put(id, replacement);
			}
			//Link children
			for (Object3D tObj : object.getChildren()) {
				tObj.setParent(replacement);
			}
			//Clear error and notify
			replacement.setDupIdError(false);
			listeners.notify(EventType.REMOVE, Property.DUPLICATES, replacement, null);
		} else {
			//Make all children orphans
			for (Object3D tObj : object.getChildren()) {
				tObj.setParent(orphans);
			}
		}
	}
	
	private boolean delayAutosort = false;
	private final List<Object3D> objectsToSort = new ArrayList<>();
	
	private boolean sort(Object3D obj) {
		if (processingAll) return false;	//Avoid sorting objects when doing a full scan
		if (!autoSortEnabled) return false;
		if (delayAutosort) {
			objectsToSort.add(obj);
			return false;
		}
		boolean moved = false;
		if (obj != null && obj.getParent() != null) {
			Object3D parent = obj.getParent();
			if (parent != null && parent.getStatement() != null) {
				List<Statement> statements = lhx.getStatements();
				int parentIndex = statements.indexOf(parent.getStatement());
				for (int i = parentIndex + 1; i < statements.size(); i++) {
					Statement tStmt = statements.get(i);
					Object3D tObj = objects.get(tStmt);
					if (tObj != null) {
						if (!tObj.isDescendantOf(parent)) {
							return move(obj, i);
						} else if (tObj == obj) {
							return false;
						}
					}
				}
				moved |= move(obj, statements.size());
			}
		}
		return moved;
	}
	
	private final UChangeListener objectChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == Object3D.Property.SELECTED && !changingSelection) {
				listeners.notify(EventType.CHANGE, Property.SELECTION);
			} else if (event.getProperty() == Object3D.Property.ID) {
				//Move this statement before its new first child
				Object3D obj = (Object3D)event.getSource();
				int id = obj.getStatement().getId();
				Command parentCmd = obj.getStatement().getCommand();
				int before = 0;
				for (Statement stmt : lhx.getStatements()) {
					Command cmd = stmt.getCommand();
					if (cmd != null) {
						if (parentCmd == Command.CREATE_TOWN && cmd.parent == cmd.town && stmt.getTown() == id ||
								parentCmd == Command.CREATE_FLOCK && cmd.parent == cmd.flock && stmt.getFlock() == id ||
								parentCmd == Command.CREATE_FOREST && cmd.parent == cmd.forest && stmt.getForest() == id) {
							break;
						}
					}
					before++;
				}
				move(obj, before);
			} else if (event.getProperty() == Object3D.Property.PARENT) {
				Object3D obj = (Object3D) event.getSource();
				sort(obj);
			}
		}
	};
	
	private boolean autoSortEnabled = true;
	
	public void setAutoSortEnabled(boolean enabled) {
		this.autoSortEnabled = enabled;
	}
	
	public boolean isAutoSortEnabled() {
		return this.autoSortEnabled;
	}
	
	private boolean move(Object3D obj, int dstIndex) {
		if (autoSortEnabled) {
			return lhx.move(obj.getStatement(), dstIndex);
		}
		return false;
	}
	
	private void rebuildLUTs() {
		towns.clear();
		flocks.clear();
		forests.clear();
		streams.clear();
		for (Object3D obj : objects.values()) {
			Statement stmt = obj.getStatement();
			Command cmd = stmt.getCommand();
			if (cmd.id >= 0) {
				if (cmd.id == cmd.player) {
					//Nothing to do here
				} else if (cmd.id == cmd.town) {
					int townId = stmt.getTown();
					towns.put(townId, obj);
				} else if (cmd.id == cmd.flock) {
					int flockId = stmt.getFlock();
					flocks.put(flockId, obj);
				} else if (cmd.id == cmd.forest) {
					int forestId = stmt.getForest();
					forests.put(forestId, obj);
				} else if (cmd.id == cmd.stream) {
					int streamId = stmt.getStream();
					streams.put(streamId, obj);
				}
			}
		}
	}
	
	private void rebuildHierarchy() {
		rebuildLUTs();
		//
		for (Object3D obj : objects.values()) {
			Statement stmt = obj.getStatement();
			Command cmd = stmt.getCommand();
			if (cmd.parent >= 0) {
				Object3D parent = null;
				if (cmd.parent == cmd.player) {
					String playerId = stmt.getPlayer();
					parent = players.get(playerId);
				} else if (cmd.parent == cmd.town) {
					int townId = stmt.getTown();
					parent = townId < 0 ? getEnvContainer(obj) : towns.getOrDefault(townId, orphans);
				} else if (cmd.parent == cmd.flock) {
					int flockId = stmt.getFlock();
					parent = flockId < 0 ? getEnvContainer(obj) : flocks.getOrDefault(flockId, orphans);
				} else if (cmd.parent == cmd.forest) {
					int forestId = stmt.getForest();
					parent = forestId < 0 ? getEnvContainer(obj) : forests.getOrDefault(forestId, orphans);
				} else if (cmd.parent == cmd.stream) {
					int streamId = stmt.getStream();
					parent = streamId < 0 ? getEnvContainer(obj) : streams.getOrDefault(streamId, orphans);
				} else {
					assert(false);
				}
				obj.setParent(parent);
			} else if (cmd == Command.CREATE_VILLAGER_POS) {
				Object3D home = obj.getHome();
				obj.setParent(home == null ? orphans : home);
			} else {
				obj.setParent(getEnvContainer(obj));
			}
		}
	}
	
	public Object3D findClosestFreeHome(Coord position, float maxDistance, boolean adult) {
		float minDist2 = maxDistance >= 0 ? (maxDistance * maxDistance + 0.000001f) : Float.MAX_VALUE;
		Object3D res = null;
		for (Object3D obj : objects.values()) {
			if (obj.hasDoor() && obj.getStatement().getCommand() == Command.CREATE_ABODE) {
				float dist2 = position.dst2(obj.getPosition());
				if (dist2 <= minDist2) {
					int numAdults = 0;
					int numChildren = 0;
					for (Object3D inhabitant : obj.getChildren()) {
						if (inhabitant.isAdult()) {
							numAdults++;
						} else {
							numChildren++;
						}
					}
					GAbodeInfo info = (GAbodeInfo) obj.getModelInfo().info;
					if (adult && numAdults < info.maxVillagersInAbode ||
						!adult && numChildren < info.maxChildrenInAbode) {
						minDist2 = dist2;
						res = obj;
					}
				}
			}
		}
		return res;
	}
	
	private static final CommandImpl createTown = (lhx3d, stmt, object) -> {
		ModelInfo modelInfo = lhx3d.modelManager.getTown();
		return object.set(lhx3d, stmt, modelInfo, -1);
	};
	
	private static final CommandImpl setTownCongregationPos = (lhx3d, stmt, object) -> {
		ModelInfo modelInfo = lhx3d.modelManager.getTownCongregation();
		return object.set(lhx3d, stmt, modelInfo, -1);
	};
	
	private static final CommandImpl startCameraPos = (lhx3d, stmt, object) -> {
		ModelInfo modelInfo = lhx3d.modelManager.getCamera();
		return object.set(lhx3d, stmt, modelInfo, -1);
	};
	
	private static final CommandImpl createFlock = (lhx3d, stmt, object) -> {
		ModelInfo modelInfo = lhx3d.modelManager.getFlock();
		return object.set(lhx3d, stmt, modelInfo, -1);
	};
	
	private static final CommandImpl createForest = (lhx3d, stmt, object) -> {
		ModelInfo modelInfo = lhx3d.modelManager.getForest();
		return object.set(lhx3d, stmt, modelInfo, -1);
	};
	
	private static final CommandImpl createAbode = (lhx3d, stmt, object) -> {
		String type = stmt.getTypeString();
		ModelInfo modelInfo = lhx3d.modelManager.getAbode(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createPlannedAbode = (lhx3d, stmt, object) -> {
		String type = stmt.getTypeString();
		ModelInfo modelInfo = lhx3d.modelManager.getAbode(type);
		return object.set(lhx3d, stmt, modelInfo, 1);
	};
	
	private static final CommandImpl createCitadel = (lhx3d, stmt, object) -> {
		ModelInfo modelInfo = lhx3d.modelManager.getTemple();
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createPlannedCitadel = (lhx3d, stmt, object) -> {
		ModelInfo modelInfo = lhx3d.modelManager.getTemple();
		return object.set(lhx3d, stmt, modelInfo, 1);
	};
	
	private static final CommandImpl createMobileObject = (lhx3d, stmt, object) -> {
		int type = stmt.getTypeInt();
		ModelInfo modelInfo = lhx3d.modelManager.getMobileObject(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createMobileStatic = (lhx3d, stmt, object) -> {
		int type = stmt.getTypeInt();
		ModelInfo modelInfo = lhx3d.modelManager.getMobileStatic(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createAnimatedStatic = (lhx3d, stmt, object) -> {
		String type = stmt.getTypeString();
		ModelInfo modelInfo = lhx3d.modelManager.getAnimatedStatic(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createNewAnimal = (lhx3d, stmt, object) -> {
		int type = stmt.getTypeInt();
		ModelInfo modelInfo = lhx3d.modelManager.getAnimal(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createNewBigForest = (lhx3d, stmt, object) -> {
		int type = stmt.getTypeInt();
		ModelInfo modelInfo = lhx3d.modelManager.getBigForest(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createNewFeature = (lhx3d, stmt, object) -> {
		String type = stmt.getTypeString();
		ModelInfo modelInfo = lhx3d.modelManager.getFeature(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createNewTownField = (lhx3d, stmt, object) -> {
		int type = stmt.getTypeInt();
		ModelInfo modelInfo = lhx3d.modelManager.getField(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createNewTree = (lhx3d, stmt, object) -> {
		int type = stmt.getTypeInt();
		ModelInfo modelInfo = lhx3d.modelManager.getTree(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createDeadTree = (lhx3d, stmt, object) -> {
		int type = stmt.getTypeInt();
		ModelInfo modelInfo = lhx3d.modelManager.getDeadTree(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createDrinkWaypoint = (lhx3d, stmt, object) -> {
		ModelInfo modelInfo = lhx3d.modelManager.getDrinkWaypoint();
		return object.set(lhx3d, stmt, modelInfo, -1);
	};
	
	private static final CommandImpl createVillagerPos = (lhx3d, stmt, object) -> {
		String type = stmt.getTypeString();
		ModelInfo modelInfo = lhx3d.modelManager.getVillager(type, 20);
		ModelInfo youngModelInfo = lhx3d.modelManager.getVillager(type, 0);
		return object.set(lhx3d, lhx3d.land3d, stmt, modelInfo, youngModelInfo, 0);
	};
	
	private static final CommandImpl createTownVillager = (lhx3d, stmt, object) -> {
		LHXVillagerInfo type = (LHXVillagerInfo)stmt.getType();
		ModelInfo modelInfo = lhx3d.modelManager.getTownVillager(type, 20);
		ModelInfo youngModelInfo = lhx3d.modelManager.getTownVillager(type, 0);
		return object.set(lhx3d, lhx3d.land3d, stmt, modelInfo, youngModelInfo, 0);
	};
	
	private static final CommandImpl createSpellDispenser = (lhx3d, stmt, object) -> {
		String type = stmt.getTypeString();
		ModelInfo modelInfo = lhx3d.modelManager.getSpellDispenser(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createTownCentre = (lhx3d, stmt, object) -> {
		String type = stmt.getTypeString();
		ModelInfo modelInfo = lhx3d.modelManager.getTownCentre(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createStream = (lhx3d, stmt, object) -> {
		ModelInfo modelInfo = lhx3d.modelManager.getStream();
		return object.set(lhx3d, stmt, modelInfo, -1);
	};
	
	private static final CommandImpl createStreamPoint = (lhx3d, stmt, object) -> {
		ModelInfo modelInfo = lhx3d.modelManager.getStreamPoint();
		return object.set(lhx3d, stmt, modelInfo, -1);
	};
	
	private static final CommandImpl createStreetLantern = (lhx3d, stmt, object) -> {
		int type = stmt.getTypeInt();
		ModelInfo modelInfo = lhx3d.modelManager.getStreetLantern(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createBonfire = (lhx3d, stmt, object) -> {
		ModelInfo modelInfo = lhx3d.modelManager.getBonfire();
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createPot = (lhx3d, stmt, object) -> {
		int type = stmt.getTypeInt();
		ModelInfo modelInfo = lhx3d.modelManager.getPot(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createScaffold = (lhx3d, stmt, object) -> {
		ModelInfo modelInfo = lhx3d.modelManager.getScaffold();
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createSpecialTownVillager = (lhx3d, stmt, object) -> {
		int type = stmt.getTypeInt();
		ModelInfo modelInfo = lhx3d.modelManager.getSpecialVillager(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createTownFishFarm = (lhx3d, stmt, object) -> {
		ModelInfo modelInfo = lhx3d.modelManager.getFishFarm();
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl createWeatherClimate = (lhx3d, stmt, object) -> {
		System.err.println("Command not implemented: " + stmt.getCommand());
		return null;	//TODO
	};
	
	private static final CommandImpl createWeatherClimateRain = (lhx3d, stmt, object) -> {
		System.err.println("Command not implemented: " + stmt.getCommand());
		return null;	//TODO
	};
	
	private static final CommandImpl createWeatherClimateTemp = (lhx3d, stmt, object) -> {
		System.err.println("Command not implemented: " + stmt.getCommand());
		return null;	//TODO
	};
	
	private static final CommandImpl createWeatherClimateWind = (lhx3d, stmt, object) -> {
		System.err.println("Command not implemented: " + stmt.getCommand());
		return null;	//TODO
	};
	
	private static final CommandImpl createWorshipSite = (lhx3d, stmt, object) -> {
		System.err.println("Command not implemented: " + stmt.getCommand());
		return null;	//TODO
	};
	
	private static final CommandImpl createNewTownSpell = (lhx3d, stmt, object) -> {
		String type = stmt.getTypeString();
		ModelInfo modelInfo = lhx3d.modelManager.getTownSpell(type);
		return object.set(lhx3d, stmt, modelInfo, 0);
	};
	
	private static final CommandImpl voidCommand = (lhx3d, stmt, object) -> {
		return null;
	};
	
	private static final CommandImpl[] commandsImpl = new CommandImpl[Command.values().length];
	
	static {
		bind(Command.ADD_GAME_MESSAGE_LINE, voidCommand);
		bind(Command.BRUSH_SIZE, null);
		bind(Command.COUNTRY_CHANGE, null);
		bind(Command.CREATE_ABODE, createAbode);
		bind(Command.CREATE_ANIMAL, null);
		bind(Command.CREATE_ANIMATED_STATIC, createAnimatedStatic);
		bind(Command.CREATE_AREA, null);
		bind(Command.CREATE_ARENA, null);
		bind(Command.CREATE_BASE, null);
		bind(Command.CREATE_BIG_FOREST, null);
		bind(Command.CREATE_BONFIRE, createBonfire);
		bind(Command.CREATE_CITADEL, createCitadel);
		bind(Command.CREATE_CREATURE, null);
		bind(Command.CREATE_CREATURE_FROM_FILE, null);
		bind(Command.CREATE_CREATURE_PEN, null);
		bind(Command.CREATE_DEAD_TREE, createDeadTree);
		bind(Command.CREATE_DRINK_WAYPOINT, createDrinkWaypoint);
		bind(Command.CREATE_FEATURE, null);
		bind(Command.CREATE_FIELD, null);
		bind(Command.CREATE_FIRE_FLY, null);
		bind(Command.CREATE_FISH_FARM, null);
		bind(Command.CREATE_FLOCK, createFlock);
		bind(Command.CREATE_FLOWERS, null);
		bind(Command.CREATE_FOOTPATH, null);
		bind(Command.CREATE_FOOTPATH_NODE, null);
		bind(Command.CREATE_FOREST, createForest);
		bind(Command.CREATE_FURNITURE, null);
		bind(Command.CREATE_INFLUENCE_RING, null);
		bind(Command.CREATE_MIST, null);
		bind(Command.CREATE_MOBILEOBJECT, createMobileObject);
		bind(Command.CREATE_MOBILESTATIC, null);
		bind(Command.CREATE_MOBILE_STATIC, createMobileStatic);
		bind(Command.CREATE_NEW_ANIMAL, createNewAnimal);
		bind(Command.CREATE_NEW_BIG_FOREST, createNewBigForest);
		bind(Command.CREATE_NEW_FEATURE, createNewFeature);
		bind(Command.CREATE_NEW_TOWN_FIELD, createNewTownField);
		bind(Command.CREATE_NEW_TOWN_SPELL, createNewTownSpell);
		bind(Command.CREATE_NEW_TREE, createNewTree);
		bind(Command.CREATE_ONE_SHOT_SPELL, null);
		bind(Command.CREATE_ONE_SHOT_SPELL_PU, null);
		bind(Command.CREATE_PATH, null);
		bind(Command.CREATE_PITCH, null);
		bind(Command.CREATE_PLANNED_ABODE, createPlannedAbode);
		bind(Command.CREATE_PLANNED_CITADEL, createPlannedCitadel);
		bind(Command.CREATE_PLANNED_SPELL_ICON, null);
		bind(Command.CREATE_PLANNED_WALL_SECTION, null);
		bind(Command.CREATE_PLANNED_WORSHIP_SITE, null);
		bind(Command.CREATE_POT, createPot);
		bind(Command.CREATE_SCAFFOLD, createScaffold);
		bind(Command.CREATE_SPECIAL_TOWN_VILLAGER, createSpecialTownVillager);
		bind(Command.CREATE_SPELL_DISPENSER, createSpellDispenser);
		bind(Command.CREATE_SPELL_ICON, null);
		bind(Command.CREATE_STREAM, createStream);
		bind(Command.CREATE_STREAM_POINT, createStreamPoint);
		bind(Command.CREATE_STREET_LANTERN, createStreetLantern);
		bind(Command.CREATE_STREET_LIGHT, null);
		bind(Command.CREATE_TOWN, createTown);
		bind(Command.CREATE_TOWN_CENTRE, createTownCentre);
		bind(Command.CREATE_TOWN_CENTRE_SPELL_ICON, null);
		bind(Command.CREATE_TOWN_FIELD, null);
		bind(Command.CREATE_TOWN_FISH_FARM, createTownFishFarm);
		bind(Command.CREATE_TOWN_SPELL, null);
		bind(Command.CREATE_TOWN_TEMPORARY_POTS, null);
		bind(Command.CREATE_TOWN_VILLAGER, createTownVillager);
		bind(Command.CREATE_TREE, null);
		bind(Command.CREATE_VILLAGER, null);
		bind(Command.CREATE_VILLAGER_POS, createVillagerPos);
		bind(Command.CREATE_WALL_SECTION, null);
		bind(Command.CREATE_WATERFALL, null);
		bind(Command.CREATE_WEATHER_CLIMATE, createWeatherClimate);
		bind(Command.CREATE_WEATHER_CLIMATE_RAIN, createWeatherClimateRain);
		bind(Command.CREATE_WEATHER_CLIMATE_TEMP, createWeatherClimateTemp);
		bind(Command.CREATE_WEATHER_CLIMATE_WIND, createWeatherClimateWind);
		bind(Command.CREATE_WEATHER_STORM, null);
		bind(Command.CREATE_WORSHIP_SITE, createWorshipSite);
		bind(Command.EDIT_LEVEL, null);
		bind(Command.FIRE_FLY_SPELL_REWARD_PROB, voidCommand);
		bind(Command.FLY_BY_FILE, null);
		bind(Command.HEIGHT_CHANGE, null);
		bind(Command.LINK_FOOTPATH, null);
		bind(Command.LOAD_COMPUTER_PLAYER_PERSONALLTY, null);
		bind(Command.LOAD_LANDSCAPE, voidCommand);
		bind(Command.MAKE_LAST_OBJECT_ARTIFACT, null);
		bind(Command.MULTIPLAYER_DEBUG, null);
		bind(Command.SET_A_TOWNS_INFLUENCE_MULTIPLIER, null);
		bind(Command.SET_COMPUTER_PLAYER_CREATURE_LIKE, null);
		bind(Command.SET_COMPUTER_PLAYER_PERSONALLTY, null);
		bind(Command.SET_GLOBAL_LAND_BALANCE, voidCommand);
		bind(Command.SET_INTERACT_DESIRE, null);
		bind(Command.SET_LAND_BALANCE, null);
		bind(Command.SET_LAND_NUMBER, voidCommand);
		bind(Command.SET_LOST_TOWN_SCALE, null);
		bind(Command.SET_NIGHTTIME, voidCommand);
		bind(Command.SET_PLAYER_INFLUENCE_MULTIPLIER, voidCommand);
		bind(Command.SET_TOWN_BALANCE_BELIEF_SCALE, null);
		bind(Command.SET_TOWN_BELIEF, voidCommand);
		bind(Command.SET_TOWN_BELIEF_CAP, voidCommand);
		bind(Command.SET_TOWN_CONGREGATION_POS, setTownCongregationPos);
		bind(Command.SET_TOWN_INFLUENCE_MULTIPLIER, voidCommand);
		bind(Command.SET_TOWN_UNINHABITABLE, voidCommand);
		bind(Command.START_CAMERA_POS, startCameraPos);
		bind(Command.START_GAME_MESSAGE, voidCommand);
		bind(Command.TOGGLE_COMPUTER_PLAYER, voidCommand);
		bind(Command.TOWN_DESIRE_BOOST, voidCommand);
		bind(Command.TOWN_NEEDS_POS, null);
		bind(Command.VERSION, voidCommand);
	}
	
	private static void bind(Command command, CommandImpl impl) {
		commandsImpl[command.ordinal()] = impl;
	}
	
	public List<Object3D> getRootObjects() {
		ArrayList<Object3D> roots = new ArrayList<>();
		roots.add(environment);
		roots.addAll(players.values());
		roots.add(orphans);
		return roots;
	}
	
	public Object3D getEnvironment() {
		return environment;
	}
	
	public Object3D getOrphans() {
		return environment;
	}
	
	public Iterable<Object3D> getPlayers() {
		return players.values();
	}
	
	public Iterable<Object3D> getObjects() {
		return objects.values();
	}
	
	public Object3D getObject(Statement statement) {
		return objects.get(statement);
	}
	
	public Object3D getTownCentre(Object3D town) {
		for (Object3D child : town.getChildren()) {
			if (child.isTownCentre()) {
				return child;
			}
		}
		return null;
	}
	
	private Statement getTownUninhabitableStatement(Object3D town) {
		final int townId = town.getStatement().getId();
		for (Statement stmt : lhx.getStatements()) {
			if (stmt.getCommand() == Command.SET_TOWN_UNINHABITABLE) {
				if (stmt.getTown() == townId) {
					return stmt;
				}
			}
		}
		return null;
	}
	
	public boolean isTownUninhabitable(Object3D town) {
		return getTownUninhabitableStatement(town) != null;
	}
	
	public void setTownUninhabitable(Object3D town, boolean value) {
		Statement uninhabitableStmt = getTownUninhabitableStatement(town);
		boolean isUninhabitable = uninhabitableStmt != null;
		if (value != isUninhabitable) {
			if (value) {
				int index = lhx.getStatements().indexOf(town.getStatement());
				if (index >= 0) {
					uninhabitableStmt = new Statement(Command.SET_TOWN_UNINHABITABLE, town.getStatement().getId());
					lhx.getStatements().add(index + 1, uninhabitableStmt);
				}
			} else {
				lhx.getStatements().remove(uninhabitableStmt);
			}
		}
	}
	
	public boolean hasSelectedObjects() {
		for (Object3D obj : objects.values()) {
			if (obj.isSelected()) {
				return true;
			}
		}
		return false;
	}
	
	public List<Object3D> getSelected() {
		ArrayList<Object3D> res = new ArrayList<>(objects.size());
		for (Object3D obj : objects.values()) {
			if (obj.isSelected()) {
				res.add(obj);
			}
		}
		return res;
	}
	
	public void clearSelection() {
		if (changingSelection) return;
		changingSelection = true;
		for (Object3D obj : objects.values()) {
			obj.setSelected(false);
		}
		listeners.notify(EventType.CHANGE, Property.SELECTION);
		changingSelection = false;
	}
	
	public void setSelectedObjects(List<Object3D> objects) {
		if (changingSelection) return;
		changingSelection = true;
		IdentityHashMap<Object3D, Object> sel = new IdentityHashMap<>();
		for (Object3D obj : objects) {
			sel.put(obj, obj);
		}
		for (Object3D obj : this.objects.values()) {
			obj.setSelected(sel.containsKey(obj));
		}
		listeners.notify(EventType.CHANGE, Property.SELECTION);
		changingSelection = false;
	}
	
	public void setSelectedStatements(List<Statement> statements) {
		if (changingSelection) return;
		changingSelection = true;
		IdentityHashMap<Statement, Statement> sel = new IdentityHashMap<>();
		for (Statement stmt : statements) {
			sel.put(stmt, stmt);
		}
		for (Object3D obj : this.objects.values()) {
			obj.setSelected(sel.containsKey(obj.getStatement()));
		}
		listeners.notify(EventType.CHANGE, Property.SELECTION);
		changingSelection = false;
	}
	
	public void select(@Null Statement statement, SelectionMode mode) {
		Object3D obj = objects.get(statement);
		select(obj, mode);
	}
	
	public void select(@Null Object3D obj, SelectionMode mode) {
		if (changingSelection) return;
		changingSelection = true;
		switch (mode) {
			case ADD:
				if (obj != null) {
					obj.setSelected(true);
				}
				break;
			case INTERSECT:
			case NEW:
				for (Object3D tObj : objects.values()) {
					tObj.setSelected(tObj == obj);
				}
				break;
			case SUBTRACT:
				if (obj != null) {
					obj.setSelected(false);
				}
				break;
		}
		listeners.notify(EventType.CHANGE, Property.SELECTION);
		changingSelection = false;
	}
	
	public void select(Selection selection, SelectionMode mode) {
		if (changingSelection) return;
		changingSelection = true;
		Coord coord = new Coord();
		switch (mode) {
			case ADD:
				for (Object3D obj : objects.values()) {
					coord.set(obj.getPosition());
					if (selection.isInside(coord)) {
						obj.setSelected(true);
					}
				}
				break;
			case INTERSECT:
				for (Object3D obj : objects.values()) {
					coord.set(obj.getPosition());
					if (!selection.isInside(coord)) {
						obj.setSelected(false);
					}
				}
				break;
			case NEW:
				for (Object3D obj : objects.values()) {
					coord.set(obj.getPosition());
					obj.setSelected(selection.isInside(coord));
				}
				break;
			case SUBTRACT:
				for (Object3D obj : objects.values()) {
					coord.set(obj.getPosition());
					if (selection.isInside(coord)) {
						obj.setSelected(false);
					}
				}
				break;
		}
		listeners.notify(EventType.CHANGE, Property.SELECTION);
		changingSelection = false;
	}
	
	public Object3D getBuildingFromDoor(Coord doorPosition, float radius) {
		final float r2 = radius * radius;
		final Coord pos = new Coord();
		for (Object3D obj : objects.values()) {
			if (obj.hasDoor()) {
				obj.getDoorPosition(pos);
				pos.y = doorPosition.y;
				if (pos.dst2(doorPosition) <= r2) {
					return obj;
				}
			}
		}
		return null;
	}
	
	@Override
	public void close() {
		environment.close();
		for (Object3D player : players.values()) {
			player.close();
		}
		players.clear();
		for (Object3D obj : objects.values()) {
			obj.close();
		}
		objects.clear();
		lhx.listeners.remove(lhxChangeListener);
	}
	
	
	private static interface CommandImpl {
		public @Null Object3D run(LHX3D lhx3d, Statement stmt, Object3D object) throws Exception;
	}
	
	
	public static class VObject3D extends Object3D {
		private final String name;
		
		public VObject3D(String name) {
			this(name, null);
		}
		
		public VObject3D(String name, Object3D parent) {
			this.name = name;
			setParent(parent);
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
}
