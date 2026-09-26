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
package it.ld.bw.lndgui;

import java.util.ArrayList;
import java.util.List;

import it.ld.bw.lhx.Command;
import it.ld.bw.lhx.Command.Argument;
import it.ld.bw.lhx.LHXFile;
import it.ld.bw.lhx.Statement;
import it.ld.bw.lnd.model.BulkUpdate;
import it.ld.bw.lnd.model.LH3DLandBlock;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LNDMaterial;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.SimpleMap;
import it.ld.utils.Edit;
import it.ld.utils.EditManager;
import it.ld.utils.UChangeListener;
import it.ld.utils.UChangeListener.EventType;
import it.ld.utils.UndoableEdit;

public class LandEditManager extends EditManager implements AutoCloseable {
	private static final Argument GENERIC_ARG = new Argument(-1, null, null, "generic", null);
	
	private final MainApp app;
	private LndFile land;
	private LHXFile lhx;
	private boolean record = true;
	private boolean recordingDisabled = false;
	
	private boolean coastlineBumpmapChanged;
	private boolean noiseMapChanged;
	
	private final ArrayList<Boolean> editedMaterials = new ArrayList<>();
	private int editedMaterialsMin;
	private int editedMaterialsMax;
	
	private final ArrayList<Boolean> editedCountries = new ArrayList<>();
	private int editedCountriesMin;
	private int editedCountriesMax;
	
	private boolean[][] editedBlocks;
	private int editedBlocksMinX;
	private int editedBlocksMinZ;
	private int editedBlocksMaxX;
	private int editedBlocksMaxZ;
	
	private final SimpleMap oldBumpMap = new SimpleMap();
	private final SimpleMap oldNoiseMap = new SimpleMap();
	private final ArrayList<LNDMaterial> oldMaterials = new ArrayList<>();
	private final ArrayList<LNDCountry> oldCountries = new ArrayList<>();
	private LH3DLandBlock[][] oldBlocks;
	
	private final ArrayList<Statement> oldStatements = new ArrayList<>();
	private final ArrayList<Argument> editedStatements = new ArrayList<>();
	private int editedStatementsMin;
	private int editedStatementsMax;
	
	private boolean complexEditRunning;
	private ComplexEdit complexEdit;
	
	public LandEditManager(MainApp app) {
		this.app = app;
		Settings.listeners.add(settingsChangeListener);
		this.setMaxUndo(Settings.MAX_UNDO.getInt());
		app.listeners.add(appChangeListener);
	}
	
	private UChangeListener settingsChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == Settings.MAX_UNDO) {
				setMaxUndo(Settings.MAX_UNDO.getInt());
			}
		}
	};
	
	public boolean isRecordingDisabled() {
		return recordingDisabled;
	}

	public void setRecordingDisabled(boolean recordingDisabled) {
		this.recordingDisabled = recordingDisabled;
	}
	
	public void beginComplexEdit(String description, Parts parts, boolean bumpmap, boolean noisemap) {
		record = false;		//Do not record individual updates
		complexEdit = new ComplexEdit(description, parts, bumpmap, noisemap);
		complexEditRunning = true;
		super.begin("");	//This is required to activate a transaction
	}
	
	@Override
	public void add(Edit action, boolean execute) {
		if (complexEditRunning) return;
		super.add(action, execute);
		if (!isTransactionActive()) {
			app.listeners.notify(EventType.CHANGE, MainApp.Property.UNDO_HISTORY);
		}
	}
	
	@Override
	public void end() {
		if (complexEditRunning) {
			complexEdit.end();
			complexEditRunning = false;
			add(complexEdit, false);
			complexEdit = null;
			getLandSnapshot();	//We need have lost individual updates, so a full refresh is required
			record = true;		//Restore normal recording
		} else {
			//if (getTransactionDepth() == 1) {
				flushChanges();
			//}
			super.end();
		}
		app.listeners.notify(EventType.CHANGE, MainApp.Property.UNDO_HISTORY);
	}
	
	@Override
	public void undo() {
		try {
			super.undo();
		} catch (Exception e) {
			e.printStackTrace();
			clear();
			app.disableAutosave();
			app.showError("Undo", e);
		}
		app.listeners.notify(EventType.CHANGE, MainApp.Property.UNDO_HISTORY);
	}
	
	@Override
	public void redo() {
		try {
			super.redo();
		} catch (Exception e) {
			e.printStackTrace();
			clear();
			app.disableAutosave();
			app.showError("Redo", e);
		}
		app.listeners.notify(EventType.CHANGE, MainApp.Property.UNDO_HISTORY);
	}
	
	@Override
	public void clear() {
		if (complexEditRunning) return;
		super.clear();
		app.listeners.notify(EventType.CHANGE, MainApp.Property.UNDO_HISTORY);
	}
	
	@Override
	public void clearRedos() {
		super.clearRedos();
		app.listeners.notify(EventType.CHANGE, MainApp.Property.UNDO_HISTORY);
	}
	
	private LH3DLandBlock clone(LH3DLandBlock block) {
		return block == null ? null : block.clone();
	}
	
	private void flushChanges() {
		flushLandChanges();
		flushLHXChanges();
	}
	
	private void flushLandChanges() {
		flushBumpMapChanges();
		flushNoiseMapChanges();
		flushMaterialsChanges();
		flushCountriesChanges();
		flushBlocksChanges();
	}
	
	private void flushBumpMapChanges() {
		if (coastlineBumpmapChanged) {
			final SimpleMap prevCoastlineBumpMap = oldBumpMap.clone();
			final SimpleMap newCoastlineBumpMap = land.getCoastlineBumpMap().clone();
			add(new UndoableEdit() {
				private String description = I18n.tr("action.editBumpMap");
				
				@Override
				public void execute() {
					record = false;
					land.setCoastlineBumpMap(newCoastlineBumpMap.clone());
					oldBumpMap.setPixels(newCoastlineBumpMap.getPixels());
					record = true;
				}
				
				@Override
				public void undo() {
					record = false;
					land.setCoastlineBumpMap(prevCoastlineBumpMap.clone());
					oldBumpMap.setPixels(prevCoastlineBumpMap.getPixels());
					record = true;
				}
				
				@Override
				public String toString() {
					return description;
				}
			}, false);
			oldBumpMap.setPixels(newCoastlineBumpMap.getPixels());
			coastlineBumpmapChanged = false;
		}
	}
	
	private void flushNoiseMapChanges() {
		if (noiseMapChanged) {
			final SimpleMap prevNoiseMap = oldNoiseMap.clone();
			final SimpleMap newNoiseMap = land.getNoiseMap().clone();
			add(new UndoableEdit() {
				private String description = I18n.tr("action.editNoiseMap");
				
				@Override
				public void execute() {
					record = false;
					land.setNoiseMap(newNoiseMap.clone());
					oldNoiseMap.setPixels(newNoiseMap.getPixels());
					record = true;
				}
				
				@Override
				public void undo() {
					record = false;
					land.setNoiseMap(prevNoiseMap.clone());
					oldNoiseMap.setPixels(prevNoiseMap.getPixels());
					record = true;
				}
				
				@Override
				public String toString() {
					return description;
				}
			}, false);
			oldNoiseMap.setPixels(newNoiseMap.getPixels());
			noiseMapChanged = false;
		}
	}
	
	private void flushMaterialsChanges() {
		for (int i = editedMaterialsMin; i <= editedMaterialsMax; i++) {
			final int index = i;
			if (editedMaterials.get(index)) {
				if (!oldMaterials.get(index).equals(land.getMaterials().get(index))) {
					final LNDMaterial newMaterial = land.getMaterials().get(index).clone();
					final LNDMaterial oldMaterial = oldMaterials.get(index).clone();
					add(new UndoableEdit() {
						private String description = I18n.tr("action.editMaterial");
						
						@Override
						public void execute() {
							record = false;
							land.getMaterials().get(index).set(newMaterial);
							oldMaterials.get(index).set(newMaterial);
							record = true;
						}
						
						@Override
						public void undo() {
							record = false;
							land.getMaterials().get(index).set(oldMaterial);
							oldMaterials.get(index).set(oldMaterial);
							record = true;
						}
						
						@Override
						public String toString() {
							return description;
						}
					}, false);
					oldMaterials.get(index).set(newMaterial);
				}
				editedMaterials.set(index, Boolean.FALSE);
			}
		}
		editedMaterialsMin = Integer.MAX_VALUE;
		editedMaterialsMax = Integer.MIN_VALUE;
	}
	
	private void flushCountriesChanges() {
		for (int i = editedCountriesMin; i <= editedCountriesMax; i++) {
			final int index = i;
			if (editedCountries.get(index)) {
				final LNDCountry newCountry = land.getCountries().get(index).clone();
				final LNDCountry oldCountry = oldCountries.get(index).clone();
				add(new UndoableEdit() {
					private String description = I18n.tr("action.editCountry");
					
					@Override
					public void execute() {
						record = false;
						land.getCountries().get(index).set(newCountry);
						oldCountries.get(index).set(newCountry);
						record = true;
					}
					
					@Override
					public void undo() {
						record = false;
						land.getCountries().get(index).set(oldCountry);
						oldCountries.get(index).set(oldCountry);
						record = true;
					}
					
					@Override
					public String toString() {
						return description;
					}
				}, false);
				oldCountries.get(index).set(newCountry);
				editedCountries.set(index, Boolean.FALSE);
			}
		}
		editedCountriesMin = Integer.MAX_VALUE;
		editedCountriesMax = Integer.MIN_VALUE;
	}
	
	private void flushBlocksChanges() {
		for (int tbx = editedBlocksMinX; tbx <= editedBlocksMaxX; tbx++) {
			for (int tbz = editedBlocksMinZ; tbz <= editedBlocksMaxZ; tbz++) {
				final int bx = tbx;
				final int bz = tbz;
				if (editedBlocks[bx][bz]) {
					final LH3DLandBlock oldBlock = clone(oldBlocks[bx][bz]);
					final LH3DLandBlock newBlock = clone(land.getBlock(bx, bz));
					if (oldBlock != null && newBlock != null) {
						//Change
						add(new UndoableEdit() {
							private String description = I18n.tr("action.editLandscape");
							
							@Override
							public void execute() {
								record = false;
								land.getBlock(bx, bz).set(newBlock);
								oldBlocks[bx][bz].set(newBlock);
								record = true;
							}
							
							@Override
							public void undo() {
								record = false;
								land.getBlock(bx, bz).set(oldBlock);
								oldBlocks[bx][bz].set(oldBlock);
								record = true;
							}

							@Override
							public String toString() {
								return description;
							}
						}, false);
						oldBlocks[bx][bz].set(newBlock);
					} else if (newBlock != null) {
						//Add
						add(new UndoableEdit() {
							private String description = I18n.tr("action.editLandscape");
							
							@Override
							public void execute() {
								record = false;
								land.addBlock(newBlock.clone());
								oldBlocks[bx][bz] = newBlock.clone();
								record = true;
							}
							
							@Override
							public void undo() {
								record = false;
								land.removeBlock(bx, bz);
								oldBlocks[bx][bz] = null;
								record = true;
							}
							
							@Override
							public String toString() {
								return description;
							}
						}, false);
						oldBlocks[bx][bz] = newBlock.clone();
					} else if (oldBlock != null) {
						//Remove
						add(new UndoableEdit() {
							private String description = I18n.tr("action.editLandscape");
							
							@Override
							public void execute() {
								record = false;
								land.removeBlock(bx, bz);
								oldBlocks[bx][bz] = null;
								record = true;
							}
							
							@Override
							public void undo() {
								record = false;
								land.addBlock(oldBlock.clone());
								oldBlocks[bx][bz] = oldBlock.clone();
								record = true;
							}
							
							@Override
							public String toString() {
								return description;
							}
						}, false);
						oldBlocks[bx][bz] = null;
					}
					editedBlocks[bx][bz] = false;
				}
			}
		}
		editedBlocksMinX = Integer.MAX_VALUE;
		editedBlocksMinZ = Integer.MAX_VALUE;
		editedBlocksMaxX = Integer.MIN_VALUE;
		editedBlocksMaxZ = Integer.MIN_VALUE;
	}
	
	private final UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.LAND) {
				setLand(app.getLand());
			} else if (event.getProperty() == MainApp.Property.LHX) {
				setLHX(app.getLHX());
			}
		}
	};
	
	private void setLand(LndFile land) {
		if (this.land != null) {
			this.land.listeners.remove(landChangeListener);
			clearLandSnapshot();
		}
		this.land = land;
		if (land != null) {
			getLandSnapshot();
			land.listeners.add(landChangeListener);
		}
	}
	
	private void setLHX(LHXFile lhx) {
		if (this.lhx != null) {
			this.lhx.listeners.remove(lhxChangeListener);
			clearLHXSnapshot();
		}
		this.lhx = lhx;
		if (lhx != null) {
			getLHXSnapshot();
			lhx.listeners.add(lhxChangeListener);
		}
	}
	
	private void getLHXSnapshot() {
		List<Statement> statements = lhx.getStatements();
		oldStatements.clear();
		editedStatements.clear();
		oldStatements.ensureCapacity(statements.size());
		editedStatements.ensureCapacity(statements.size());
		for (Statement stmt : statements) {
			oldStatements.add(stmt.clone());
			editedStatements.add(null);
		}
		editedStatementsMin = Integer.MAX_VALUE;
		editedStatementsMax = Integer.MIN_VALUE;
	}
	
	private void clearLHXSnapshot() {
		oldStatements.clear();
		editedStatements.clear();
	}
	
	private void getLandSnapshot() {
		getMaterialsSnapshot();
		getCountriesSnapshot();
		getBlocksSnapshot();
		oldBumpMap.setPixels(land.getCoastlineBumpMap().getPixels());
		oldNoiseMap.setPixels(land.getNoiseMap().getPixels());
	}
	
	private void clearLandSnapshot() {
		//Materials
		oldMaterials.clear();
		editedMaterials.clear();
		//Countries
		oldCountries.clear();
		editedCountries.clear();
		//Blocks
		editedBlocks = null;
		oldBlocks = null;
	}
	
	private void getMaterialsSnapshot() {
		List<LNDMaterial> materials = land.getMaterials();
		oldMaterials.clear();
		editedMaterials.clear();
		oldMaterials.ensureCapacity(materials.size());
		editedMaterials.ensureCapacity(materials.size());
		for (LNDMaterial material : materials) {
			oldMaterials.add(material.clone());
			editedMaterials.add(Boolean.FALSE);
		}
		editedMaterialsMin = Integer.MAX_VALUE;
		editedMaterialsMax = Integer.MIN_VALUE;
	}
	
	private void getCountriesSnapshot() {
		List<LNDCountry> countries = land.getCountries();
		oldCountries.clear();
		editedCountries.clear();
		oldCountries.ensureCapacity(countries.size());
		editedCountries.ensureCapacity(countries.size());
		for (LNDCountry country : countries) {
			oldCountries.add(country.clone());
			editedCountries.add(Boolean.FALSE);
		}
		editedCountriesMin = Integer.MAX_VALUE;
		editedCountriesMax = Integer.MIN_VALUE;
	}
	
	private void getBlocksSnapshot() {
		if (oldBlocks == null || oldBlocks.length != land.getBlocksPerSide()) {
			oldBlocks = new LH3DLandBlock[land.getBlocksPerSide()][land.getBlocksPerSide()];
			editedBlocks = new boolean[land.getBlocksPerSide()][land.getBlocksPerSide()];
		}
		for (int bx = 0; bx < land.getBlocksPerSide(); bx++) {
			for (int bz = 0; bz < land.getBlocksPerSide(); bz++) {
				oldBlocks[bx][bz] = clone(land.getBlock(bx, bz));
				editedBlocks[bx][bz] = false;
			}
		}
		editedBlocksMinX = Integer.MAX_VALUE;
		editedBlocksMinZ = Integer.MAX_VALUE;
		editedBlocksMaxX = Integer.MIN_VALUE;
		editedBlocksMaxZ = Integer.MIN_VALUE;
	}
	
	private void blockChanged(int bx, int bz) {
		editedBlocks[bx][bz] = true;
		if (bx < editedBlocksMinX) editedBlocksMinX = bx;
		if (bz < editedBlocksMinZ) editedBlocksMinZ = bz;
		if (bx > editedBlocksMaxX) editedBlocksMaxX = bx;
		if (bz > editedBlocksMaxZ) editedBlocksMaxZ = bz;
	}
	
	private void materialAdded(final int index) {
		if (editedMaterialsMin <= editedMaterialsMax) throw new IllegalStateException("Cannot add or remove item while editing");
		final LNDMaterial newMaterial = land.getMaterials().get(index).clone();
		add(new UndoableEdit() {
			private String description = I18n.tr("action.addMaterial");
			
			@Override
			public void execute() {
				record = false;
				land.getMaterials().add(index, newMaterial.clone());
				oldMaterials.add(index, newMaterial.clone());
				editedMaterials.add(index, Boolean.FALSE);
				record = true;
			}
			
			@Override
			public void undo() {
				record = false;
				land.getMaterials().remove(index);
				oldMaterials.remove(index);
				editedMaterials.remove(index);
				record = true;
			}
			
			@Override
			public String toString() {
				return description;
			}
		}, false);
		oldMaterials.add(index, newMaterial.clone());
		editedMaterials.add(index, Boolean.FALSE);
		getCountriesSnapshot();
	}
	
	private void materialChanged(final int index) {
		editedMaterials.set(index, Boolean.TRUE);
		if (index < editedMaterialsMin) editedMaterialsMin = index;
		if (index > editedMaterialsMax) editedMaterialsMax = index;
	}
	
	private void materialRemoved(final int index) {
		if (editedMaterialsMin <= editedMaterialsMax) throw new IllegalStateException("Cannot add or remove item while editing");
		final LNDMaterial oldMaterial = oldMaterials.get(index).clone();
		add(new UndoableEdit() {
			private String description = I18n.tr("action.removeMaterial");
			
			@Override
			public void execute() {
				record = false;
				land.getMaterials().remove(index);
				oldMaterials.remove(index);
				editedMaterials.remove(index);
				record = true;
			}
			
			@Override
			public void undo() {
				record = false;
				land.getMaterials().add(index, oldMaterial.clone());
				oldMaterials.add(index, oldMaterial.clone());
				editedMaterials.add(index, Boolean.FALSE);
				record = true;
			}
			
			@Override
			public String toString() {
				return description;
			}
		}, false);
		oldMaterials.remove(index);
		editedMaterials.remove(index);
		getCountriesSnapshot();
	}
	
	private void countryAdded(final int index) {
		if (editedCountriesMin <= editedCountriesMax) throw new IllegalStateException("Cannot add or remove item while editing");
		final LNDCountry newCountry = land.getCountries().get(index).clone();
		add(new UndoableEdit() {
			private String description = I18n.tr("action.addCountry");
			
			@Override
			public void execute() {
				record = false;
				land.getCountries().add(index, newCountry.clone());
				oldCountries.add(index, newCountry.clone());
				editedCountries.add(index, Boolean.FALSE);
				record = true;
			}
			
			@Override
			public void undo() {
				record = false;
				land.getCountries().remove(index);
				oldCountries.remove(index);
				editedCountries.remove(index);
				record = true;
			}
			
			@Override
			public String toString() {
				return description;
			}
		}, false);
		oldCountries.add(index, newCountry.clone());
		editedCountries.add(index, Boolean.FALSE);
		getBlocksSnapshot();
	}
	
	private void countryChanged(int index) {
		editedCountries.set(index, Boolean.TRUE);
		if (index < editedCountriesMin) editedCountriesMin = index;
		if (index > editedCountriesMax) editedCountriesMax = index;
	}
	
	private void countryRemoved(final int index) {
		if (editedCountriesMin <= editedCountriesMax) throw new IllegalStateException("Cannot add or remove item while editing");
		final LNDCountry oldCountry = oldCountries.get(index).clone();
		add(new UndoableEdit() {
			private String description = I18n.tr("action.removeCountry");
			
			@Override
			public void execute() {
				record = false;
				land.getCountries().remove(index);
				oldCountries.remove(index);
				editedCountries.remove(index);
				record = true;
			}
			
			@Override
			public void undo() {
				record = false;
				land.getCountries().add(index, oldCountry.clone());
				oldCountries.add(index, oldCountry.clone());
				editedCountries.add(index, Boolean.FALSE);
				record = true;
			}
			
			@Override
			public String toString() {
				return description;
			}
		}, false);
		oldCountries.remove(index);
		editedCountries.remove(index);
		getBlocksSnapshot();
	}
	
	private void blocksPerSideChanged(final int oldCount, final int newCount) {
		if (!isTransactionActive()) throw new IllegalStateException("Resizing must be done in a transaction");
		this.resetTransaction();
		if (newCount >= oldCount) {
			add(new UndoableEdit() {
				private String description = I18n.tr("action.resizeBlocksGrid");
				
				@Override
				public void execute() {
					record = false;
					land.setBlocksPerSide(newCount);
					getBlocksSnapshot();
					record = true;
				}
				
				@Override
				public void undo() {
					record = false;
					land.setBlocksPerSide(oldCount);
					getBlocksSnapshot();
					record = true;
				}
				
				@Override
				public String toString() {
					return description;
				}
			}, false);
		} else {
			int numRemoved = oldCount * oldCount - newCount * newCount;
			final List<LH3DLandBlock> removedBlocks = new ArrayList<>(numRemoved);
			for (int bx = 0; bx < oldCount; bx++) {
				for (int bz = newCount; bz < oldCount; bz++) {
					if (oldBlocks[bx][bz] != null) {
						removedBlocks.add(oldBlocks[bx][bz].clone());
					}
				}
			}
			for (int bx = newCount; bx < oldCount; bx++) {
				for (int bz = 0; bz < newCount; bz++) {
					if (oldBlocks[bx][bz] != null) {
						removedBlocks.add(oldBlocks[bx][bz].clone());
					}
				}
			}
			add(new UndoableEdit() {
				private String description = I18n.tr("action.resizeBlocksGrid");
				
				@Override
				public void execute() {
					record = false;
					land.setBlocksPerSide(newCount);
					getBlocksSnapshot();
					record = true;
				}
				
				@Override
				public void undo() {
					record = false;
					land.setBlocksPerSide(oldCount);
					for (LH3DLandBlock block : removedBlocks) {
						land.addBlock(block.clone());
					}
					getBlocksSnapshot();
					record = true;
				}
				
				@Override
				public String toString() {
					return description;
				}
			}, false);
		}
		getBlocksSnapshot();
	}
	
	private void altitudeBitsChanged(final int oldBits, final int newBits) {
		add(new UndoableEdit() {
			private String description = I18n.tr("action.editMaxAltitude");
			
			@Override
			public void execute() {
				record = false;
				land.setAltitudeBits(newBits);
				record = true;
			}
			
			@Override
			public void undo() {
				record = false;
				land.setAltitudeBits(oldBits);
				record = true;
			}
			
			@Override
			public String toString() {
				return description;
			}
		}, false);
	}
	
	private void flushLHXChanges() {
		for (int i = editedStatementsMin; i <= editedStatementsMax; i++) {
			final int index = i;
			final Argument editedArg = editedStatements.get(index);
			if (editedArg != null) {
				final Statement newStatement = lhx.getStatements().get(index).clone();
				final Statement oldStatement = oldStatements.get(index).clone();
				add(new UndoableEdit() {
					private String description = "";
					
					{
						if (oldStatement.isCommand() && oldStatement.getCommand().name().startsWith("CREATE_")) {
							description = editedArg == GENERIC_ARG ?
								I18n.tr("action.editObject", oldStatement.getCommand().objectDisplayName) :
								I18n.tr("action.editObjectAttr", getArgName(editedArg).toLowerCase());
						} else {
							description = I18n.tr("action.editStatement");
						}
					}
					
					@Override
					public void execute() {
						record = false;
						lhx.getStatements().get(index).set(newStatement);
						oldStatements.get(index).set(newStatement);
						record = true;
					}
					
					@Override
					public void undo() {
						record = false;
						lhx.getStatements().get(index).set(oldStatement);
						oldStatements.get(index).set(oldStatement);
						record = true;
					}
					
					@Override
					public String toString() {
						return description;
					}
					
					private String getArgName(Argument arg) {
						if (arg.name != null) {
							String key = "lhx.param." + arg.name;
							String val = I18n.tr(key);
							if (val.equals(key)) return arg.name;
							return val;
						} else {
							return "Parameter " + arg.index;
						}
					}
				}, false);
				oldStatements.get(index).set(newStatement);
				editedStatements.set(index, null);
			}
		}
		editedStatementsMin = Integer.MAX_VALUE;
		editedStatementsMax = Integer.MIN_VALUE;
	}
	
	private void objectAdded(int index) {
		if (editedStatementsMin <= editedStatementsMax) throw new IllegalStateException("Cannot add or remove item while editing");
		final Statement newStatement = lhx.getStatements().get(index).clone();
		add(new UndoableEdit() {
			private String description = newStatement.isCommand() ? (
						newStatement.getCommand().name().startsWith("CREATE_") ?
						I18n.tr("action.addObject", newStatement.getCommand().objectDisplayName) : I18n.tr("action.addStatement")
					) : I18n.tr("action.addStatement");
			
			@Override
			public void execute() {
				record = false;
				lhx.getStatements().add(index, newStatement.clone());
				oldStatements.add(index, newStatement.clone());
				editedStatements.add(index, null);
				record = true;
			}
			
			@Override
			public void undo() {
				record = false;
				lhx.getStatements().remove(index);
				oldStatements.remove(index);
				editedStatements.remove(index);
				record = true;
			}
			
			@Override
			public String toString() {
				return description;
			}
		}, false);
		oldStatements.add(index, newStatement.clone());
		editedStatements.add(index, null);
	}
	
	private void objectChanged(int index, Argument editedArg) {
		Argument prevEdited = editedStatements.get(index);
		if (prevEdited == null) {
			editedStatements.set(index, editedArg);
		} else if (editedArg != prevEdited) {
			editedStatements.set(index, GENERIC_ARG);
		}
		if (index < editedStatementsMin) editedStatementsMin = index;
		if (index > editedStatementsMax) editedStatementsMax = index;
	}
	
	private void statementMoved(int srcIndex, int dstIndex) {
		if (editedStatementsMin <= editedStatementsMax) throw new IllegalStateException("Cannot add or remove item while editing");
		final Statement oldStatement = oldStatements.get(srcIndex);
		add(new UndoableEdit() {
			private String description = I18n.tr("action.moveStatement");
			
			@Override
			public void execute() {
				record = false;
				lhx.move(srcIndex, dstIndex);
				oldStatements.remove(srcIndex);
				int insertPos = srcIndex < dstIndex ? dstIndex - 1 : dstIndex;
				oldStatements.add(insertPos, oldStatement);
				//There's no need to move items in editedStatements since they are all null
				record = true;
			}
			
			@Override
			public void undo() {
				record = false;
				int fromIndex = srcIndex < dstIndex ? dstIndex - 1 : dstIndex;
			    int toIndex = srcIndex < dstIndex ? srcIndex : srcIndex + 1;
			    lhx.move(fromIndex, toIndex);
			    oldStatements.remove(fromIndex);
			    int insertPos = fromIndex < toIndex ? toIndex - 1 : toIndex;
			    oldStatements.add(insertPos, oldStatement);
			    //There's no need to move items in editedStatements since they are all null
				record = true;
			}
			
			@Override
			public String toString() {
				return description;
			}
		}, false);
		oldStatements.remove(srcIndex);
		int insertPos = srcIndex < dstIndex ? dstIndex - 1 : dstIndex;
		oldStatements.add(insertPos, oldStatement);
		//There's no need to move items in editedStatements since they are all null
	}
	
	private void objectRemoved(int index) {
		if (editedStatementsMin <= editedStatementsMax) throw new IllegalStateException("Cannot add or remove item while editing");
		final Statement oldStatement = oldStatements.get(index).clone();
		add(new UndoableEdit() {
			private String description = oldStatement.isCommand() ? (
						oldStatement.getCommand().name().startsWith("CREATE_") ?
								I18n.tr("action.removeObject", oldStatement.getCommand().objectDisplayName) : I18n.tr("action.removeStatement")
					) : I18n.tr("action.removeStatement");
			
			@Override
			public void execute() {
				record = false;
				lhx.getStatements().remove(index);
				oldStatements.remove(index);
				editedStatements.remove(index);
				record = true;
			}
			
			@Override
			public void undo() {
				record = false;
				lhx.getStatements().add(index, oldStatement.clone());
				oldStatements.add(index, oldStatement.clone());
				editedStatements.add(index, null);
				record = true;
			}
			
			@Override
			public String toString() {
				return description;
			}
		}, false);
		oldStatements.remove(index);
		editedStatements.remove(index);
	}
	
	private final UChangeListener lhxChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (!record || recordingDisabled || complexEditRunning) return;
			if (event.getProperty() == LHXFile.Property.STATEMENTS) {
				if (event.getIndex() >= 0) {
					switch (event.getType()) {
						case ADD:
							objectAdded(event.getIndex());
							break;
						case CHANGE:
							Argument editedArg = GENERIC_ARG;
							if (event.getCause() != null) {
								UEvent cause = event.getCause();
								if (cause.getProperty() == Statement.Property.ARGS && cause.getIndex() >= 0) {
									Statement stmt = (Statement)cause.getSource();
									Command cmd = stmt.getCommand();
									editedArg = cmd.args[cause.getIndex()];
								}
							}
							objectChanged(event.getIndex(), editedArg);
							break;
						case BEFORE_MOVE:
							flushLHXChanges();
							break;
						case MOVE:
							statementMoved(event.getSrcIndex(), event.getIndex());
							break;
						case REMOVE:
							objectRemoved(event.getIndex());
							break;
					}
				} else {
					System.err.println("not implemented yet");
				}
				if (!isTransactionActive()) {
					flushLHXChanges();
					app.listeners.notify(EventType.CHANGE, MainApp.Property.UNDO_HISTORY);
				}
			}
		}
	};
	
	private final UChangeListener landChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (!record || recordingDisabled || complexEditRunning) return;
			LndFile.Property prop = (LndFile.Property)event.getProperty();
			switch (prop) {
				case BLOCKS:
					if (!event.isItemChange(LNDCountry.Property.INDEX)) {
						LH3DLandBlock block = (LH3DLandBlock)(event.getType() == EventType.REMOVE ? event.getOldValue() : event.getNewValue());
						blockChanged(block.getBlockX(), block.getBlockZ());
					}
					break;
				case COASTLINE_BUMPMAP:
					coastlineBumpmapChanged = true;
					break;
				case COUNTRIES:
					switch (event.getType()) {
						case ADD:
							countryAdded(event.getIndex());
							break;
						case CHANGE:
							if (!event.isItemChange(LNDCountry.Property.INDEX)) {
								countryChanged(event.getIndex());
							}
							break;
						case REMOVE:
							countryRemoved(event.getIndex());
							break;
						case MOVE:
							break;
						case BEFORE_MOVE:
							break;
					}
					break;
				case LOWRES_TEXTURES:
					//Nothing to do here
					break;
				case MATERIALS:
					switch (event.getType()) {
						case ADD:
							materialAdded(event.getIndex());
							break;
						case CHANGE:
							if (!event.isItemChange(LNDMaterial.Property.INDEX)) {
								materialChanged(event.getIndex());
							}
							break;
						case REMOVE:
							materialRemoved(event.getIndex());
							break;
						case MOVE:
							break;
						case BEFORE_MOVE:
							break;
					}
					break;
				case NOISE_MAP:
					noiseMapChanged = true;
					break;
				case BLOCKS_PER_SIDE:
					blocksPerSideChanged((int)event.getOldValue(), (int)event.getNewValue());
					break;
				case MAX_BLOCKS:
					//Nothing to do here
					break;
				case ALTITUDE_BITS:
					altitudeBitsChanged((int)event.getOldValue(), (int)event.getNewValue());
					break;
				default:
					clear();
					break;
			}
			if (!isTransactionActive()) {
				flushLandChanges();
				app.listeners.notify(EventType.CHANGE, MainApp.Property.UNDO_HISTORY);
			}
		}
	};

	@Override
	public void close() {
		app.listeners.remove(appChangeListener);
		Settings.listeners.remove(settingsChangeListener);
	}
	

	public enum Parts {
		NONE, BLOCKS, BLOCKS_COUNTRIES, BLOCKS_COUNTRIES_MATERIALS
	}
	
	
	private class ComplexEdit implements UndoableEdit {
		private String description;
		
		private List<LH3DLandBlock> oldBlocks;
		private List<LNDCountry> oldCountries;
		private List<LNDMaterial> oldMaterials;
		private SimpleMap oldCoastlineBumpMap;
		private SimpleMap oldNoiseMap;
		
		private List<LH3DLandBlock> newBlocks;
		private List<LNDCountry> newCountries;
		private List<LNDMaterial> newMaterials;
		private SimpleMap newCoastlineBumpMap;
		private SimpleMap newNoiseMap;
		
		public ComplexEdit(String description, Parts parts, boolean saveCoastlineBumpmap, boolean saveNoisemap) {
			this.description = description;
			if (parts.ordinal() >= Parts.BLOCKS.ordinal()) {
				oldBlocks = new ArrayList<>(land.getNumBlocks());
				for (LH3DLandBlock block : land.getLandBlocksForRead()) {
					oldBlocks.add(block.clone());
				}
			}
			if (parts.ordinal() >= Parts.BLOCKS_COUNTRIES.ordinal()) {
				oldCountries = new ArrayList<>(land.getCountries().size());
				for (LNDCountry country : land.getCountries()) {
					oldCountries.add(country.clone());
				}
			}
			if (parts.ordinal() >= Parts.BLOCKS_COUNTRIES_MATERIALS.ordinal()) {
				oldMaterials = new ArrayList<>(land.getMaterials().size());
				for (LNDMaterial material : land.getMaterials()) {
					oldMaterials.add(material.clone());
				}
			}
			if (saveCoastlineBumpmap) {
				oldCoastlineBumpMap = land.getCoastlineBumpMap().clone();
			}
			if (saveNoisemap) {
				oldNoiseMap = land.getNoiseMap().clone();
			}
		}
		
		public void end() {
			if (oldBlocks != null) {
				newBlocks = new ArrayList<>(land.getNumBlocks());
				for (LH3DLandBlock block : land.getLandBlocksForRead()) {
					newBlocks.add(block.clone());
				}
			}
			if (oldCountries != null) {
				newCountries = new ArrayList<>(land.getCountries().size());
				for (LNDCountry country : land.getCountries()) {
					newCountries.add(country.clone());
				}
			}
			if (oldMaterials != null) {
				newMaterials = new ArrayList<>(land.getMaterials().size());
				for (LNDMaterial material : land.getMaterials()) {
					newMaterials.add(material.clone());
				}
			}
			if (oldCoastlineBumpMap != null) {
				newCoastlineBumpMap = land.getCoastlineBumpMap().clone();
			}
			if (oldNoiseMap != null) {
				newNoiseMap = land.getNoiseMap().clone();
			}
		}
		
		@Override
		public void execute() {
			record = false;
			if (newCoastlineBumpMap != null) {
				land.setCoastlineBumpMap(newCoastlineBumpMap.clone());
			}
			if (newNoiseMap != null) {
				land.setNoiseMap(newNoiseMap.clone());
			}
			
			if (newBlocks != null) {
				try (BulkUpdate<LH3DLandBlock> update = land.getLandBlocksForUpdate();) {
					land.getLandBlocksForRead().clear();
				}
			}
			if (newCountries != null) {
				try (BulkUpdate<LNDCountry> update = land.getCountriesForUpdate();) {
					land.getCountries().clear();
				}
			}
			if (newMaterials != null) {
				land.getMaterials().clear();
			}
			
			if (newMaterials != null) {
				for (LNDMaterial material : newMaterials) {
					land.getMaterials().add(material.clone());
				}
			}
			if (newCountries != null) {
				try (BulkUpdate<LNDCountry> update = land.getCountriesForUpdate();) {
					for (LNDCountry country : newCountries) {
						land.getCountries().add(country.clone());
					}
				}
			}
			if (newBlocks != null) {
				try (BulkUpdate<LH3DLandBlock> update = land.getLandBlocksForUpdate();) {
					for (LH3DLandBlock block : newBlocks) {
						land.addBlock(block.clone());
					}
				}
			}
			record = true;
		}

		@Override
		public void undo() {
			record = false;
			if (oldCoastlineBumpMap != null) {
				land.setCoastlineBumpMap(oldCoastlineBumpMap.clone());
			}
			if (oldNoiseMap != null) {
				land.setNoiseMap(oldNoiseMap.clone());
			}
			
			if (oldBlocks != null) {
				try (BulkUpdate<LH3DLandBlock> update = land.getLandBlocksForUpdate();) {
					land.getLandBlocksForRead().clear();
				}
			}
			if (oldCountries != null) {
				try (BulkUpdate<LNDCountry> update = land.getCountriesForUpdate();) {
					land.getCountries().clear();
				}
			}
			if (oldMaterials != null) {
				land.getMaterials().clear();
			}
			
			if (oldMaterials != null) {
				for (LNDMaterial material : oldMaterials) {
					land.getMaterials().add(material.clone());
				}
			}
			if (oldCountries != null) {
				try (BulkUpdate<LNDCountry> update = land.getCountriesForUpdate();) {
					for (LNDCountry country : oldCountries) {
						land.getCountries().add(country.clone());
					}
				}
			}
			if (oldBlocks != null) {
				try (BulkUpdate<LH3DLandBlock> update = land.getLandBlocksForUpdate();) {
					for (LH3DLandBlock block : oldBlocks) {
						land.addBlock(block.clone());
					}
				}
			}
			record = true;
		}
		
		@Override
		public String toString() {
			return description;
		}
	}
}
