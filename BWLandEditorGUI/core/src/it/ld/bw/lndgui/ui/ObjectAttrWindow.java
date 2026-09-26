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
package it.ld.bw.lndgui.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import it.ld.bw.info.DesireInfo;
import it.ld.bw.info.GAbodeInfo;
import it.ld.bw.info.PlayerInfo;
import it.ld.bw.lhx.Command;
import it.ld.bw.lhx.Command.ArgType;
import it.ld.bw.lhx.Command.Argument;
import it.ld.bw.lhx.LHXCoord;
import it.ld.bw.lhx.LHXFile;
import it.ld.bw.lhx.Statement;
import it.ld.bw.lhx.Statement.Parameter;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.gfx.Coord;
import it.ld.bw.lndgui.gfx.Object3D;
import it.ld.bw.lndgui.ui.View3D.PickObjectCallback;
import it.ld.libgdx.ui.components.ImageButton;
import it.ld.libgdx.ui.components.ListTable;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.Spinner;
import it.ld.libgdx.ui.components.TabbedPane;
import it.ld.libgdx.ui.components.TextSlider;
import it.ld.libgdx.utils.UndoHelper;
import it.ld.utils.UChangeListener;

public class ObjectAttrWindow extends SmartWindow {
	private final MainApp app;
	private final TabbedPane content;
	private Table attributesPanel = new Table();
	private final ListTable<Statement> beliefTable;
	private final ListTable<Statement> beliefCapTable;
	private final ListTable<Statement> desireBoostTable;
	
	private final ArrayList<Object3D> objects = new ArrayList<>();
	private LHXFile lhx;
	
	private Object3D town = null;
	private final Array<Statement> beliefStatements = new Array<>();
	private final Array<Statement> beliefCapStatements = new Array<>();
	private final Array<Statement> desireBoostStatements = new Array<>();
	
	private final Label villagersInAbodeLabel;
	private final Label childrenInAbodeLabel;
	
	private Actor[] attrToField = new Actor[0];
	
	private final UndoHelper undoHelper;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		ObjectAttrWindow window = (ObjectAttrWindow) SmartWindow.getSingleInstance("objectAttr");
		if (window == null) {
			window = new ObjectAttrWindow(app, skin);
			window.setDockingManager(app.getDockingManager());
			window.setSingleInstance("objectAttr");
		}
		window.show(stage, stage.getWidth() - window.getWidth(), 28f, false);
		window.toFront();
	}
	
	public static void hideInstance() {
		ObjectAttrWindow window = (ObjectAttrWindow) SmartWindow.getSingleInstance("objectAttr");
		if (window != null) {
			window.remove();
		}
	}
	
	private ObjectAttrWindow(MainApp app, Skin skin) {
		super(I18n.tr("objectAttr.title"), skin, Attribute.DOCKABLE, Attribute.AUTODISPOSE);
		this.app = app;
		this.content = new TabbedPane(skin, Align.bottom);
		add(content).grow().top().row();
		attributesPanel.defaults().top().left().padBottom(2);
		beliefTable = createBeliefTable();
		beliefCapTable = createBeliefCapTable();
		desireBoostTable = createDesireBoostTable();
		villagersInAbodeLabel = new Label("", skin);
		childrenInAbodeLabel = new Label("", skin);
		undoHelper = new UndoHelper(app.getEditManager(), I18n.tr("action.editStatement"));
		app.getView3D().listeners.add(selectionChangeListener);
		List<Object3D> objects = app.getView3D().getSelectedObjects();
		setObjects(objects);
	}
	
	private final UChangeListener selectionChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.CHANGE && event.getProperty() == View3D.Property.SELECTION) {
				List<Object3D> objects = app.getView3D().getSelectedObjects();
				setObjects(objects);
			}
		}
	};
	
	private final UChangeListener statementChengeListener = new UChangeListener() {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.CHANGE) {
				if (event.getProperty() == null || event.getProperty() == Statement.Property.COMMAND) {
					createFields();
				} else if (event.getProperty() == Statement.Property.ARGS) {
					Object3D object = objects.get(0);
					Statement stmt = object.getStatement();
					int index = event.getIndex();
					Parameter param = stmt.getArg(index);
					Argument arg = param.def;
					ArgType effectiveType = arg.getEffectiveType();
					Actor field = attrToField[index];
					TextField textField;
					TextSlider slider;
					CheckBox checkBox;
					Spinner spinner;
					if (param.def.objectClass == null) {
						switch (param.def.type) {
							case COORD:
								textField = (TextField)((Table)field).getChild(0);
								textField.setText(coordToString(param.getCoord()));
								break;
							case FLOAT:
								if (field instanceof Spinner) {
									spinner = (Spinner)field;
									spinner.setValue(param.getFloat());
								} else if (field instanceof TextSlider) {
									slider = (TextSlider)field;
									slider.setValue(param.getFloat());
								} else {
									throw new RuntimeException("Unsupported field type");
								}
								break;
							case INT:
								if (field instanceof Spinner) {
									spinner = (Spinner)field;
									spinner.setValue(param.getInt());
								} else if (field instanceof TextSlider) {
									slider = (TextSlider)field;
									slider.setValue(param.getInt());
								} else {
									throw new RuntimeException("Unsupported field type");
								}
								break;
							case BOOL:
								checkBox = (CheckBox)field;
								checkBox.setChecked(param.getBool());
								break;
							case STRING:
								textField = (TextField)field;
								textField.setText(param.getString());
								break;
							default:
								assert(false);
						}
					} else {
						SelectBox selectbox = (SelectBox)field;
						Object[] values = effectiveType.enumClass.getEnumConstants();
						selectbox.setItems(values);
						if (arg.type == ArgType.STRING) {
							for (int j = 0; j < values.length; j++) {
								if (param.getString().equals(String.valueOf(values[j]))) {
									selectbox.setSelected(values[j]);
									break;
								}
							}
						} else if (arg.type == ArgType.INT) {
							selectbox.setSelectedIndex(param.getInt());
						} else {
							selectbox.setSelected(param.getValue());
						}
					}
				}
			}
		}
	};
	
	private boolean isSameCommand(List<Object3D> objects) {
		if (objects.isEmpty()) return false;
		Object3D obj0 = objects.get(0);
		Command cmd = obj0.getStatement().getCommand();
		if (cmd == null) return false;
		for (Object3D obj : objects) {
			if (obj.getStatement().getCommand() != cmd) {
				return false;
			}
		}
		return true;
	}
	
	private boolean[] getSharedAttr(List<Object3D> objects) {
		if (!isSameCommand(objects)) return null;
		Object3D obj0 = objects.get(0);
		Command cmd = obj0.getStatement().getCommand();
		boolean[] shared = new boolean[cmd.args.length];
		Object[] refVals = new Object[cmd.args.length];
		for (int i = 0; i < refVals.length; i++) {
			shared[i] = true;
			refVals[i] = obj0.getStatement().getArg(i).getValue();
		}
		for (Object3D obj : objects) {
			for (int i = 0; i < refVals.length; i++) {
				Object val = obj.getStatement().getArg(i).getValue();
				if (!Objects.equals(refVals[i], val)) {
					shared[i] = false;
				}
			}
		}
		return shared;
	}
	
	public void setObjects(List<Object3D> objects) {
		Object prevTag = content.getSelectedTag();
		if (lhx != null) {
			lhx.listeners.remove(lhxChangeListener);
			lhx = null;
		}
		if (this.objects.size() == 1) {
			Object3D object = this.objects.get(0);
			object.listeners.remove(objectChangeListener);
			object.getStatement().listeners.remove(statementChengeListener);
		}
		clearFields();
		//
		this.objects.clear();
		this.objects.addAll(objects);
		//
		if (!objects.isEmpty()) {
			Object3D object = objects.get(0);
			lhx = object.getStatement().getLHX();
			lhx.listeners.add(lhxChangeListener);
		}
		createFields();
		if (objects.size() == 1) {
			Object3D object = objects.get(0);
			object.listeners.add(objectChangeListener);
			object.getStatement().listeners.add(statementChengeListener);
		}
		content.showPanelByTag(prevTag);
	}
	
	private final UChangeListener objectChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == Object3D.Property.DUPLICATE_ID) {
				Object3D obj = (Object3D)event.getSource();
				int idIndex = obj.getStatement().getCommand().id;
				Spinner idField = (Spinner)attrToField[idIndex];
				idField.setTextFieldStyle(obj.isDupIdError() ? "error" : "default");
				if (obj.isDupIdError()) {
					app.showTempStatusMessage(I18n.tr("messages.duplicateId"));
				}
			} else if (event.getProperty() == Object3D.Property.CHILDREN) {
				Object3D obj = (Object3D)event.getSource();
				if (obj.getStatement().getCommand() == Command.CREATE_ABODE) {
					updateNumInhabitantsInAbode();
				}
			}
		}
	};
	
	private final UChangeListener lhxChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LHXFile.Property.STATEMENTS) {
				if (event.getType() == EventType.ADD) {
					Statement stmt = (Statement)event.getNewValue();
					Command cmd = stmt.getCommand();
					if (cmd == Command.SET_TOWN_BELIEF) {
						beliefStatements.add(stmt);
						beliefTable.setItems(beliefStatements);
					} else if (cmd == Command.SET_TOWN_BELIEF_CAP) {
						beliefCapStatements.add(stmt);
						beliefCapTable.setItems(beliefCapStatements);
					} else if (cmd == Command.TOWN_DESIRE_BOOST) {
						desireBoostStatements.add(stmt);
						desireBoostTable.setItems(desireBoostStatements);
					}
				} else if (event.getType() == EventType.CHANGE) {
					Statement stmt = (Statement)event.getNewValue();
					if (stmt != null) {
						Command cmd = stmt.getCommand();
						if (cmd == Command.SET_TOWN_BELIEF) {
							beliefTable.refresh(stmt);
						} else if (cmd == Command.SET_TOWN_BELIEF_CAP) {
							beliefCapTable.refresh(stmt);
						} else if (cmd == Command.TOWN_DESIRE_BOOST) {
							desireBoostTable.refresh(stmt);
						}
					}
				} else if (event.getType() == EventType.REMOVE) {
					Statement stmt = (Statement)event.getOldValue();
					Command cmd = stmt.getCommand();
					if (cmd == Command.SET_TOWN_BELIEF) {
						beliefStatements.removeValue(stmt, true);
						beliefTable.setItems(beliefStatements);
					} else if (cmd == Command.SET_TOWN_BELIEF_CAP) {
						beliefCapStatements.removeValue(stmt, true);
						beliefCapTable.setItems(beliefCapStatements);
					} else if (cmd == Command.TOWN_DESIRE_BOOST) {
						desireBoostStatements.removeValue(stmt, true);
						desireBoostTable.setItems(desireBoostStatements);
					}
				}
			}
		}
	};
	
	private void clearFields() {
		town = null;
		beliefStatements.clear();
		beliefCapStatements.clear();
		desireBoostStatements.clear();
		beliefTable.clearItems();
		beliefCapTable.clearItems();
		desireBoostTable.clearItems();
		attributesPanel.clearChildren();
		content.clearChildren();
	}
	
	private void createFields() {
		clearFields();
		content.add(I18n.tr("objectAttr.general"), attributesPanel);
		boolean[] visibleFields = getSharedAttr(objects);
		if (visibleFields != null) {
			final Skin skin = getSkin();
			Object3D rObject = objects.get(0);
			final Statement rStmt = rObject.getStatement();
			final Command rCommand = rStmt.getCommand();
			
			attributesPanel.add(new Label(I18n.tr("objectAttr.objectType"), skin)).left().padRight(10);
			attributesPanel.add(new Label(rCommand.objectDisplayName, skin)).left().row();
			
			if (rCommand.hasAltVersion() && objects.size() == 1) {
				Actor field = createAltCommandField();
				attributesPanel.add(field).left().row();
			}
			
			final Argument[] args = rStmt.getCommand().args;
			attrToField = new Actor[args.length];
			for (int i = 0; i < args.length; i++) {
				final int index = i;
				final Parameter rParam = rStmt.getArg(index);
				final Argument rArg = rParam.def;
				if (!visibleFields[i]) continue;
				if (rArg.name != null && rArg.name.startsWith("_")) continue;	//Arguments starting with underscore shouldn't be edited
				final ArgType effectiveType = rArg.getEffectiveType();
				final String argDisplayname = getArgName(rArg);
				final String editDescription = getEditDescription(rCommand.objectDisplayName, argDisplayname);
				attributesPanel.add(new Label(argDisplayname, skin)).left().padRight(10);
				Actor field = null;
				if (effectiveType.enumClass == null) {
					switch (effectiveType) {
						case COORD:
							field = createCoordField(index, editDescription);
							break;
						case FLOAT:
							field = createFloatField(index, editDescription);
							break;
						case INT:
							field = createIntField(index, editDescription);
							break;
						case BOOL:
							field = createBoolField(index, editDescription);
							break;
						case STRING:
							field = createStringField(index, editDescription);
							break;
						default:
							assert(false);
					}
				} else {
					field = createEnumField(index, editDescription);
				}
				Cell<Actor> cell = attributesPanel.add(field).expandX();
				if (field instanceof Spinner || field instanceof CheckBox || effectiveType == ArgType.COORD) {
					cell.left();
				} else {
					cell.fillX();
				}
				attributesPanel.row();
				attrToField[index] = field;
			}
			
			if (rCommand == Command.CREATE_TOWN && objects.size() == 1) {
				town = rObject;
				Actor field = createUninhabitableField();
				attributesPanel.add(field).left().row();
			}
			
			attributesPanel.add().grow().row();	//Filler
			
			if (rCommand == Command.CREATE_TOWN && objects.size() == 1) {
				createTownTabs();
			} else if (rCommand == Command.CREATE_ABODE && objects.size() == 1) {
				createAbodeTabs();
			}
		}
		pack();
		setMinSize(getPrefWidth(), getPrefHeight());
		invalidateHierarchy();
	}
	
	private void createAbodeTabs() {
		final Skin skin = getSkin();
		
		Table abodeInfoPanel = new Table();
		abodeInfoPanel.defaults().top().left().padBottom(5);
		abodeInfoPanel.columnDefaults(1).padLeft(10).growX();
		
		abodeInfoPanel.add(new Label(I18n.tr("objectAttr.abodeInfo.villagersInAbode"), skin));
		abodeInfoPanel.add(villagersInAbodeLabel).row();
		
		abodeInfoPanel.add(new Label(I18n.tr("objectAttr.abodeInfo.childrenInAbode"), skin));
		abodeInfoPanel.add(childrenInAbodeLabel).row();
		
		abodeInfoPanel.add().colspan(2).expandY();	//Filler
		
		updateNumInhabitantsInAbode();
		
		content.add(I18n.tr("objectAttr.abodeInfo"), abodeInfoPanel, "info");
	}
	
	private void updateNumInhabitantsInAbode() {
		Object3D rObject = objects.get(0);
		int adultsInAbode = 0;
		int childrenInAbode = 0;
		for (Object3D obj : rObject.getChildren()) {
			if (obj.isAdult()) {
				adultsInAbode++;
			} else {
				childrenInAbode++;
			}
		}
		GAbodeInfo info = (GAbodeInfo)rObject.getModelInfo().info;
		villagersInAbodeLabel.setText(adultsInAbode + "/" + info.maxVillagersInAbode);
		childrenInAbodeLabel.setText(childrenInAbode + "/" + info.maxChildrenInAbode);
	}
	
	private void createTownTabs() {
		Object3D rObject = objects.get(0);
		final int townId = rObject.getStatement().getTown();
		List<Statement> statements = lhx.getStatements();
		for (Statement stmt : statements) {
			if (stmt.getCommand() == Command.SET_TOWN_BELIEF) {
				if (stmt.getTown() == townId) {
					beliefStatements.add(stmt);
				}
			} else if (stmt.getCommand() == Command.SET_TOWN_BELIEF_CAP) {
				if (stmt.getTown() == townId) {
					beliefCapStatements.add(stmt);
				}
			} else if (stmt.getCommand() == Command.TOWN_DESIRE_BOOST) {
				if (stmt.getTown() == townId) {
					desireBoostStatements.add(stmt);
				}
			}
		}
		
		Table beliefPanel = new Table();
		beliefTable.setItems(beliefStatements);
		beliefPanel.add(beliefTable).grow();
		content.add(I18n.tr("objectAttr.belief"), beliefPanel, "belief");
		
		Table beliefCapPanel = new Table();
		beliefCapTable.setItems(beliefCapStatements);
		beliefCapPanel.add(beliefCapTable).grow();
		content.add(I18n.tr("objectAttr.beliefCap"), beliefCapPanel, "beliefCap");
		
		Table desireBoostPanel = new Table();
		desireBoostTable.setItems(desireBoostStatements);
		desireBoostPanel.add(desireBoostTable).grow();
		content.add(I18n.tr("objectAttr.desireBoost"), desireBoostPanel, "desireBoost");
	}
	
	private ListTable<Statement> createBeliefTable() {
		final Skin skin = getSkin();
		final Array<PlayerInfo> players = new Array<>(PlayerInfo.values());
		final ListTable<Statement> table = new ListTable<>(skin);
		table.setHeaderVisible(true);
		table.setCellPadding(2);
		table.setModel(new ListTable.DefaultModel<Statement>(skin) {
			@Override
			public String[] getHeaderNames() {
				return new String[] {
					"",
					I18n.tr("objectAttr.belief.player"),
					I18n.tr("objectAttr.belief.value")
				};
			}
			
			private void remove(int row) {
				Statement stmt = table.getItem(row);
				lhx.getStatements().remove(stmt);
				table.refresh();
			}
			
			private final ChangeListener playerChangeListener = new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					@SuppressWarnings("unchecked")
					SelectBox<PlayerInfo> field = (SelectBox<PlayerInfo>)actor;
					int row = table.getItemIndex(field);
					Statement stmt = table.getItem(row);
					stmt.setPlayer(field.getSelected());
				}
			};
			
			@Override
			public Actor createField(final int row, int col) {
				if (col == 0) {
					return new ImageButton(skin, "tool-delete", () -> remove(row));
				} else if (col == 1) {
					SelectBox<PlayerInfo> field = new SelectBox<>(skin);
					field.setItems(players);
					field.addListener(playerChangeListener);
					return field;
				} else if (col == 2) {
					Spinner field = new Spinner(skin, 0.5f, 0f, 100f, 0.05f, 6);
					field.setValueChangeListener((float newValue) -> {
						Statement stmt = table.getItem(row);
						stmt.setFloat("belief", newValue);
					});
					undoHelper.attachGroup(field);
					return field;
				}
				return null;
			}
			
			@Override
			public void render(int row, Statement item, Cell<Container<Actor>>[] fields) {
				fields[0].getActor().size(24);
				@SuppressWarnings("unchecked")
				SelectBox<PlayerInfo> fPlayer = (SelectBox<PlayerInfo>)(fields[1].getActor().getActor());
				Spinner fValue = (Spinner)(fields[2].getActor().getActor());
				//Content
				fPlayer.setSelected(PlayerInfo.valueOf(item.getPlayer()));
				fValue.setValue(item.getArg("belief").getFloat());
			}
		});
		
		Label addButton = new Label(I18n.tr("objectAttr.addRow"), skin);
		addButton.setAlignment(Align.center);
		addButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Statement ref = beliefStatements.isEmpty() ? town.getStatement() : beliefStatements.get(beliefStatements.size - 1);
				int index = lhx.getStatements().indexOf(ref) + 1;
				//
				Statement stmt = new Statement(Command.SET_TOWN_BELIEF);
				stmt.setTown(town.getStatement().getTown());
				lhx.getStatements().add(index, stmt);
			}
		});
		table.setExtraRow(addButton);
		
		return table;
	}
	
	private ListTable<Statement> createBeliefCapTable() {
		final Skin skin = getSkin();
		final Array<PlayerInfo> players = new Array<>(PlayerInfo.values());
		final ListTable<Statement> table = new ListTable<>(skin);
		table.setHeaderVisible(true);
		table.setCellPadding(2);
		table.setModel(new ListTable.DefaultModel<Statement>(skin) {
			@Override
			public String[] getHeaderNames() {
				return new String[] {
					"",
					I18n.tr("objectAttr.beliefCap.player"),
					I18n.tr("objectAttr.beliefCap.value")
				};
			}
			
			private void remove(int row) {
				Statement stmt = table.getItem(row);
				lhx.getStatements().remove(stmt);
				table.refresh();
			}
			
			private final ChangeListener playerChangeListener = new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					@SuppressWarnings("unchecked")
					SelectBox<PlayerInfo> field = (SelectBox<PlayerInfo>)actor;
					int row = table.getItemIndex(field);
					Statement stmt = table.getItem(row);
					stmt.setPlayer(field.getSelected());
				}
			};
			
			@Override
			public Actor createField(int row, int col) {
				if (col == 0) {
					return new ImageButton(skin, "tool-delete", () -> remove(row));
				} else if (col == 1) {
					SelectBox<PlayerInfo> field = new SelectBox<>(skin);
					field.setItems(players);
					field.addListener(playerChangeListener);
					return field;
				} else if (col == 2) {
					Spinner field = new Spinner(skin, 0.5f, 0f, 100f, 0.01f);
					field.setValueChangeListener((float newValue) -> {
						Statement stmt = table.getItem(row);
						stmt.setFloat("belief", newValue);
					});
					undoHelper.attachGroup(field);
					return field;
				}
				return null;
			}
			
			@Override
			public void render(int row, Statement item, Cell<Container<Actor>>[] fields) {
				fields[0].getActor().size(24);
				@SuppressWarnings("unchecked")
				SelectBox<PlayerInfo> fPlayer = (SelectBox<PlayerInfo>)(fields[1].getActor().getActor());
				Spinner fValue = (Spinner)(fields[2].getActor().getActor());
				//Content
				fPlayer.setSelected(PlayerInfo.valueOf(item.getPlayer()));
				fValue.setValue(item.getArg("belief").getFloat());
			}
		});
		
		Label addButton = new Label(I18n.tr("objectAttr.addRow"), skin);
		addButton.setAlignment(Align.center);
		addButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Statement ref = beliefCapStatements.isEmpty() ? town.getStatement() : beliefCapStatements.get(beliefCapStatements.size - 1);
				int index = lhx.getStatements().indexOf(ref) + 1;
				//
				Statement stmt = new Statement(Command.SET_TOWN_BELIEF_CAP);
				stmt.setTown(town.getStatement().getTown());
				lhx.getStatements().add(index, stmt);
			}
		});
		table.setExtraRow(addButton);
		
		return table;
	}
	
	private ListTable<Statement> createDesireBoostTable() {
		final Skin skin = getSkin();
		final Array<DesireInfo> desires = new Array<>(DesireInfo.values());
		final ListTable<Statement> table = new ListTable<>(skin);
		table.setHeaderVisible(true);
		table.setCellPadding(2);
		table.setModel(new ListTable.DefaultModel<Statement>(skin) {
			@Override
			public String[] getHeaderNames() {
				return new String[] {
					"",
					I18n.tr("objectAttr.desireBoost.desire"),
					I18n.tr("objectAttr.desireBoost.value")
				};
			}
			
			private void remove(int row) {
				Statement stmt = table.getItem(row);
				lhx.getStatements().remove(stmt);
				table.refresh();
			}
			
			private final ChangeListener desireChangeListener = new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					@SuppressWarnings("unchecked")
					SelectBox<DesireInfo> field = (SelectBox<DesireInfo>)actor;
					int row = table.getItemIndex(field);
					Statement stmt = table.getItem(row);
					stmt.setString("type", field.getSelected().name());
				}
			};
			
			@Override
			public Actor createField(int row, int col) {
				if (col == 0) {
					return new ImageButton(skin, "tool-delete", () -> remove(row));
				} else if (col == 1) {
					SelectBox<DesireInfo> field = new SelectBox<>(skin);
					field.setItems(desires);
					field.addListener(desireChangeListener);
					return field;
				} else if (col == 2) {
					TextSlider field = new TextSlider(skin, 0f, -1f, 1f, 0.01f);
					field.setValueChangeListener((float newValue) -> {
						Statement stmt = table.getItem(row);
						stmt.setFloat("balance", newValue);
					});
					undoHelper.attachGroup(field);
					return field;
				}
				return null;
			}
			
			@Override
			public void render(int row, Statement item, Cell<Container<Actor>>[] fields) {
				fields[0].getActor().size(24);
				@SuppressWarnings("unchecked")
				SelectBox<DesireInfo> fDesire = (SelectBox<DesireInfo>)(fields[1].getActor().getActor());
				TextSlider fValue = (TextSlider)(fields[2].getActor().getActor());
				//Content
				fDesire.setSelected(DesireInfo.valueOf(item.getTypeString()));
				fValue.setValue(item.getArg("balance").getFloat());
			}
		});
		
		Label addButton = new Label(I18n.tr("objectAttr.addRow"), skin);
		addButton.setAlignment(Align.center);
		addButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Statement ref = desireBoostStatements.isEmpty() ? town.getStatement() : desireBoostStatements.get(desireBoostStatements.size - 1);
				int index = lhx.getStatements().indexOf(ref) + 1;
				//
				Statement stmt = new Statement(Command.TOWN_DESIRE_BOOST);
				stmt.setTown(town.getStatement().getTown());
				lhx.getStatements().add(index, stmt);
			}
		});
		table.setExtraRow(addButton);
		
		return table;
	}
	
	private Actor createUninhabitableField() {
		final Skin skin = getSkin();
		Object3D rObject = objects.get(0);
		String altText = I18n.tr("objectAttr.uninhabitable");
		boolean checked = rObject.getLHX3D().isTownUninhabitable(rObject);
		attributesPanel.add(new Label(altText, skin)).left().padRight(10);
		CheckBox field = new CheckBox("", skin);
		field.setChecked(checked);
		field.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				for (Object3D object : objects) {
					object.getLHX3D().setTownUninhabitable(object, field.isChecked());
				}
			}
		});
		return field;
	}
	
	private Actor createAltCommandField() {
		final Skin skin = getSkin();
		String altText;
		boolean checked;
		Object3D rObject = objects.get(0);
		final Statement rStmt = rObject.getStatement();
		final Command rCommand = rStmt.getCommand();
		if (rCommand == Command.CREATE_NEW_TREE || rCommand == Command.CREATE_DEAD_TREE) {
			altText = I18n.tr("objectAttr.dead");
			checked = rCommand == Command.CREATE_DEAD_TREE;
		} else {
			altText = I18n.tr("objectAttr.planned");
			checked = rCommand.planned;
		}
		attributesPanel.add(new Label(altText, skin)).left().padRight(10);
		CheckBox field = new CheckBox("", skin);
		field.setChecked(checked);
		field.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				rStmt.toggleAltCommand();
			}
		});
		return field;
	}
	
	private Actor createCoordField(int index, String editDescription) {
		final Skin skin = getSkin();
		Object3D rObject = objects.get(0);
		final Statement rStmt = rObject.getStatement();
		final Parameter rParam = rStmt.getArg(index);
		final Argument rArg = rParam.def;
		final Table table = new Table();
		final TextField textfield = new TextField(coordToString(rParam.getCoord()), skin, "code-normal");
		textfield.addListener(new FocusListener() {
			@Override
			public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
				if (event.isFocused()) {
					LHXCoord coord = rParam.getCoord();
					app.getView3D().showTmpMarker(new Coord(coord.x, coord.z));
				} else {
					try {
						LHXCoord coord = new LHXCoord(textfield.getText());
						for (Object3D obj : objects) {
							obj.getStatement().setCoord(index, coord);
						}
						textfield.setStyle(skin.get("code-normal", TextFieldStyle.class));
					} catch (Exception e) {
						textfield.setStyle(skin.get("code-error", TextFieldStyle.class));
					}
					app.getView3D().hideTmpMarker();
				}
			}
		});
		textfield.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (this.getTapCount() == 2) {
					LHXCoord pos = rParam.getCoord();
					app.flyTo(pos.x, pos.z, 40f);
				}
			}
		});
		undoHelper.attachActors(editDescription, textfield);
		table.add(textfield);
		if ("homePosition".equals(rArg.name)) {
			Button pickButton = new ImageButton(skin, "tool-pickcolor", I18n.tr("objectAttr.home.pick"), () -> {
				app.getView3D().pickObject(new PickObjectCallback() {
					@Override
					public void onObject(Object3D home, int button) {
						Coord doorPos = home.getDoorPosition();
						if (doorPos != null) {
							app.getEditManager().begin(editDescription);
							LHXCoord homePos = new LHXCoord(doorPos.x, doorPos.z);
							for (Object3D obj : objects) {
								obj.getStatement().setCoord(rArg.index, homePos);
							}
							textfield.setText(coordToString(homePos));
							app.getEditManager().end();
						} else {
							app.getView3D().pickObject(this, true, null);
						}
					}
				}, true, null);
			});
			table.add(pickButton).size(20).padLeft(5);
		}
		return table;
	}
	
	private Actor createFloatField(int index, String editDescription) {
		final Skin skin = getSkin();
		Object3D rObject = objects.get(0);
		final Statement rStmt = rObject.getStatement();
		final Command rCommand = rStmt.getCommand();
		final Parameter rParam = rStmt.getArg(index);
		final Argument rArg = rParam.def;
		float min = Float.MIN_VALUE;
		float max = Float.MAX_VALUE;
		float step = 0.0001f;
		if (index == rCommand.scale) {
			min = 0;
			step = 0.1f;
		} else if (index == rCommand.elevation) {
			step = 0.1f;
		} else if (index == rCommand.rotation || index == rCommand.pitch || index == rCommand.roll) {
			min = -MathUtils.PI2;
			max = MathUtils.PI2;
			step = 0.01f;
		} else if ("spawnInterval".equals(rArg.name)) {
			step = 0.1f;
		}
		if (min != Float.MIN_VALUE && max != Float.MAX_VALUE) {
			final TextSlider slider = new TextSlider(skin, rParam.getFloat(), min, max, step);
			slider.setValueChangeListener((float v) -> {
				for (Object3D obj : objects) {
					obj.getStatement().setFloat(index, v);
				}
			});
			undoHelper.attachGroup(editDescription, slider);
			return slider;
		} else {
			final Spinner spinner = new Spinner(skin, rParam.getFloat(), min, max, step);
			spinner.setValueChangeListener((float v) -> {
				for (Object3D obj : objects) {
					obj.getStatement().setFloat(index, v);
				}
			});
			undoHelper.attachGroup(editDescription, spinner);
			return spinner;
		}
	}
	
	private Actor createIntField(int index, String editDescription) {
		final Skin skin = getSkin();
		Object3D rObject = objects.get(0);
		final Statement rStmt = rObject.getStatement();
		final Command rCommand = rStmt.getCommand();
		final Parameter rParam = rStmt.getArg(index);
		final Argument rArg = rParam.def;
		int min = Integer.MIN_VALUE;
		int max = Integer.MAX_VALUE;
		int step = 1;
		if (index == rCommand.scale) {
			min = 0;
			step = 50;
		} else if (index == rCommand.rotation || index == rCommand.pitch || index == rCommand.roll) {
			min = -(int)(MathUtils.PI2 * 1000);
			max = (int)(MathUtils.PI2 * 1000);
			step = 50;
		} else if (index == rCommand.age) {
			min = 0;
		} else if ("townId".equals(rArg.name)) {
			min = -1;
		}
		if (min != Integer.MIN_VALUE && max != Integer.MAX_VALUE) {
			final TextSlider slider = new TextSlider(skin, rParam.getInt(), min, max, step, true);
			slider.setValueChangeListener((float v) -> {
				for (Object3D obj : objects) {
					obj.getStatement().setInt(index, (int)v);
				}
			});
			undoHelper.attachGroup(editDescription, slider);
			return slider;
		} else {
			final Spinner spinner = new Spinner(skin, rParam.getInt(), min, max, step);
			spinner.setIntegerOnly(true);
			spinner.setValueChangeListener((float v) -> {
				for (Object3D obj : objects) {
					obj.getStatement().setInt(index, (int)v);
				}
			});
			if (rCommand.id >= 0 && rCommand.id == index) {
				if (rObject.isDupIdError()) {
					spinner.setTextFieldStyle("error");
				}
			}
			undoHelper.attachGroup(editDescription, spinner);
			return spinner;
		}
	}
	
	private Actor createBoolField(int index, String editDescription) {
		final Skin skin = getSkin();
		Object3D rObject = objects.get(0);
		final Statement rStmt = rObject.getStatement();
		final Parameter rParam = rStmt.getArg(index);
		final CheckBox checkBox = new CheckBox("", skin);
		checkBox.setChecked(rParam.getBool());
		checkBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				for (Object3D obj : objects) {
					obj.getStatement().setBool(index, checkBox.isChecked());
				}
			}
		});
		undoHelper.attachActors(editDescription, checkBox);
		return checkBox;
	}
	
	private Actor createStringField(int index, String editDescription) {
		final Skin skin = getSkin();
		Object3D rObject = objects.get(0);
		final Statement rStmt = rObject.getStatement();
		final Parameter rParam = rStmt.getArg(index);
		final TextField textfield = new TextField(rParam.getString(), skin);
		textfield.addListener(new FocusListener() {
			@Override
			public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
				if (!event.isFocused()) {
					app.getEditManager().begin(editDescription);
					for (Object3D obj : objects) {
						obj.getStatement().setString(index, textfield.getText());
					}
					app.getEditManager().end();
				}
			}
		});
		return textfield;
	}
	
	@SuppressWarnings("unchecked")
	private Actor createEnumField(int index, String editDescription) {
		final Skin skin = getSkin();
		Object3D rObject = objects.get(0);
		final Statement rStmt = rObject.getStatement();
		final Parameter rParam = rStmt.getArg(index);
		final Argument rArg = rParam.def;
		final ArgType effectiveType = rArg.getEffectiveType();
		@SuppressWarnings("rawtypes")
		SelectBox selectbox = new SelectBox(skin);
		Object[] values = effectiveType.enumClass.getEnumConstants();
		if ("LAST".equals(values[values.length - 1].toString())) {
			Object[] tmp = values;
			values = new Object[tmp.length - 1];
			System.arraycopy(tmp, 0, values, 0, values.length);
		}
		selectbox.setItems(values);
		if (rArg.type == ArgType.STRING) {
			for (int j = 0; j < values.length; j++) {
				if (rParam.getString().equals(String.valueOf(values[j]))) {
					selectbox.setSelected(values[j]);
					break;
				}
			}
		} else if (rArg.type == ArgType.INT) {
			selectbox.setSelectedIndex(rParam.getInt());
		} else {
			selectbox.setSelected(rParam.getValue());
		}
		selectbox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				app.getEditManager().begin(editDescription);
				Object value = selectbox.getSelected();
				if (rArg.type == ArgType.STRING) {
					value = value.toString();
				} else if (rArg.type == ArgType.INT) {
					value = ((Enum<?>)value).ordinal();
				}
				for (Object3D obj : objects) {
					obj.getStatement().setValue(index, value);
				}
				app.getEditManager().end();
			}
		});
		return selectbox;
	}
	
	private static String getArgName(Argument arg) {
		if (arg.name != null) {
			String key = "lhx.param." + arg.name;
			String val = I18n.tr(key);
			if (val.equals(key)) return arg.name;
			return val;
		} else {
			return "Parameter " + arg.index;
		}
	}
	
	private static String getEditDescription(String objectName, String argDisplayname) {
		return I18n.tr("action.editObjectAttr", objectName, argDisplayname.toLowerCase());
	}
	
	private static String coordToString(LHXCoord coord) {
		return String.format(Locale.US, "%.2f,%.2f", coord.x, coord.z);
	}
	
	@Override
	public void dispose() {
		app.getView3D().listeners.remove(selectionChangeListener);
		setObjects(Collections.emptyList());
		super.dispose();
	}
}
