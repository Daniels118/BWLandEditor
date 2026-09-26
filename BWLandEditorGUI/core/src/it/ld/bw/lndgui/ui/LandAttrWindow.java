package it.ld.bw.lndgui.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent.Type;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import it.ld.bw.info.BalanceInfo;
import it.ld.bw.info.PlayerInfo;
import it.ld.bw.info.SpellSeedInfo;
import it.ld.bw.lhx.Command;
import it.ld.bw.lhx.LHXFile;
import it.ld.bw.lhx.Statement;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.Settings;
import it.ld.libgdx.ui.components.ImageButton;
import it.ld.libgdx.ui.components.ListTable;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.Spinner;
import it.ld.libgdx.ui.components.TabbedPane;
import it.ld.libgdx.ui.components.TextSlider;
import it.ld.libgdx.utils.UndoHelper;
import it.ld.utils.UChangeListener;

public class LandAttrWindow extends SmartWindow {
	private final String[] NUMBER_NAMES = new String[] {
			"Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight"
	};
	
	private final MainApp app;
	
	private final TabbedPane tabs;
	
	private final Table generalPanel = new Table();
	private Spinner versionField;
	private Spinner landNumberField;
	private TextField landscapeField;
	private Spinner townInfluenceMultiplierField;
	private Spinner playerInfluenceMultiplierField;
	private Spinner dayDurationField;
	private Spinner nightToDayRatioField;
	private Spinner dawnDuskRatioField;
	
	private final Table playersPanel = new Table();
	private ListTable<Statement> computerPlayersList;
	
	private final Table balancePanel = new Table();
	private ListTable<Statement> balanceList;
	
	private final Table fireflySpellsPanel = new Table();
	private ListTable<Statement> fireflySpellsList;
	
	private final Table skirmishPanel = new Table();
	private Spinner numPlayersField;
	private TextField startGameMessageField;
	private TextField gameMessageLineField;
	
	private LHXFile lhx;
	
	private Statement versionStmt;
	private Statement setLandNumberStmt;
	private Statement loadLandscapeStmt;
	private Statement setTownInfluenceMultiplierStmt;
	private Statement setPlayerInfluenceMultiplierStmt;
	private Statement setNightTimeStmt;
	private Statement startGameMessageStmt;
	private Statement addGameMessageLineStmt;
	private final Array<Statement> balanceStatements = new Array<>();
	private final Array<Statement> fireflySpellsStatements = new Array<>();
	private final Array<Statement> toggleComputerPlayerStatements = new Array<>();
	
	private final UndoHelper undoHelper;
	
	private LinkedHashMap<Command, Statement> order = new LinkedHashMap<>(); {
		order.put(Command.VERSION, null);
		order.put(Command.SET_LAND_NUMBER, null);
		order.put(Command.LOAD_LANDSCAPE, null);
		order.put(Command.SET_GLOBAL_LAND_BALANCE, null);
		order.put(Command.SET_TOWN_INFLUENCE_MULTIPLIER, null);
		order.put(Command.SET_PLAYER_INFLUENCE_MULTIPLIER, null);
		order.put(Command.SET_NIGHTTIME, null);
		order.put(Command.START_GAME_MESSAGE, null);
		order.put(Command.ADD_GAME_MESSAGE_LINE, null);
		order.put(Command.FIRE_FLY_SPELL_REWARD_PROB, null);
	}
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		LandAttrWindow window = (LandAttrWindow) SmartWindow.getSingleInstance("landAttr");
		if (window == null) {
			window = new LandAttrWindow(app, skin);
			window.setDockingManager(app.getDockingManager());
			window.setSingleInstance("landAttr");
		}
		window.show(stage);
		window.toFront();
	}
	
	public static void hideInstance() {
		LandAttrWindow window = (LandAttrWindow) SmartWindow.getSingleInstance("landAttr");
		if (window != null) {
			window.remove();
		}
	}
	
	private LandAttrWindow(MainApp app, Skin skin) {
		super(I18n.tr("landAttr.title"), skin, Attribute.DOCKABLE, Attribute.AUTODISPOSE);
		this.app = app;
		this.undoHelper = new UndoHelper(app.getEditManager(), I18n.tr("action.editStatement"));
		
		this.tabs = new TabbedPane(skin, Align.top);
		initGeneralPanel(skin);
		initPlayersPanel(skin);
		initBalancePanel(skin);
		initFireflySpellsPanel(skin);
		initSkirmishPanel(skin);
		
		add(tabs).grow().row();
		app.listeners.add(appChangeListener);
		setLHX(app.getLHX());
		
		pack();
		setMinSize(getWidth(), getHeight());
	}
	
	private void initGeneralPanel(Skin skin) {
		generalPanel.defaults().top().left().padBottom(2);
		generalPanel.columnDefaults(1).expandX().left().padLeft(5);
		
		generalPanel.add(new Label(I18n.tr("landAttr.version"), skin));
		versionField = new Spinner(skin, 2.3f, 0, Float.MAX_VALUE, 0.1f);
		versionField.setValueChangeListener((float v) -> {
			if (versionStmt == null) {
				versionStmt = new Statement(Command.VERSION, v);
				insert(versionStmt);
			} else {
				versionStmt.setFloat(0, v);
			}
		});
		generalPanel.add(versionField).row();
		
		generalPanel.add(new Label(I18n.tr("landAttr.landNumber"), skin));
		landNumberField = new Spinner(skin, 0, 0, Integer.MAX_VALUE, 1, true);
		landNumberField.setValueChangeListener((float v) -> {
			if (setLandNumberStmt == null) {
				setLandNumberStmt = new Statement(Command.SET_LAND_NUMBER, (int)v);
				insert(setLandNumberStmt);
			} else {
				setLandNumberStmt.setInt(0, (int)v);
			}
		});
		generalPanel.add(landNumberField).row();
		
		generalPanel.add(new Label(I18n.tr("landAttr.landscape"), skin));
		Table t = new Table();
		landscapeField = new TextField("", skin);
		landscapeField.addListener(new FocusListener() {
			@Override
			public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
				if (!event.isFocused()) {
					if (loadLandscapeStmt == null) {
						loadLandscapeStmt = new Statement(Command.LOAD_LANDSCAPE, landscapeField.getText());
						insert(loadLandscapeStmt);
					} else {
						loadLandscapeStmt.setString(0, landscapeField.getText());
					}
				}
			}
		});
		t.add(landscapeField).growX();
		final TextButton landscapeButton = new TextButton("...", skin);
		landscapeButton.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				File gameDir = Settings.GAME_DIR.getFile();
				app.getOS().getFileChooser().chooseFile(
		            new NativeFileChooserConfiguration() {{
		                title = I18n.tr("dialog.open");
		                directory = Gdx.files.absolute(gameDir != null ? new File(gameDir, "Data/Landscape").toString() : System.getProperty("user.home"));
		                nameFilter = (dir, name) -> name.endsWith(".lnd");
		                mimeFilter = "Landscape files/lnd";
		                intent = NativeFileChooserIntent.OPEN;
		            }},
		            new NativeFileChooserCallback() {
		                @Override
		                public void onFileChosen(FileHandle file) {
		                	Path filePath = file.file().toPath().toAbsolutePath().normalize();
		                	if (gameDir != null) {
		                		Path gamePath = gameDir.toPath().toAbsolutePath().normalize();
		                		if (filePath.startsWith(gamePath)) {
		                			filePath = gamePath.relativize(filePath);
		                		}
		                	}
		                	landscapeField.setText(filePath.toString());
		                	FocusEvent lostFocus = new FocusEvent();
		                	lostFocus.setType(Type.keyboard);
		                	landscapeField.fire(lostFocus);
		                }

		                @Override
		                public void onCancellation() {}

		                @Override
		                public void onError(Exception exception) {
		                    exception.printStackTrace();
		                }
		            }
		        );
			}
		});
		t.add(landscapeButton);
		generalPanel.add(t).growX().prefWidth(370).row();
		
		generalPanel.add(new Label(I18n.tr("landAttr.townInfluenceMultiplier"), skin));
		townInfluenceMultiplierField = new Spinner(skin, 1.0f, 0, Float.MAX_VALUE, 0.01f);
		townInfluenceMultiplierField.setValueChangeListener((float v) -> {
			if (setTownInfluenceMultiplierStmt == null) {
				setTownInfluenceMultiplierStmt = new Statement(Command.SET_TOWN_INFLUENCE_MULTIPLIER, v);
				insert(setTownInfluenceMultiplierStmt);
			} else {
				setTownInfluenceMultiplierStmt.setFloat(0, v);
			}
		});
		generalPanel.add(townInfluenceMultiplierField).row();
		
		generalPanel.add(new Label(I18n.tr("landAttr.playerInfluenceMultiplier"), skin));
		playerInfluenceMultiplierField = new Spinner(skin, 1.0f, 0, Float.MAX_VALUE, 0.01f);
		playerInfluenceMultiplierField.setValueChangeListener((float v) -> {
			if (setPlayerInfluenceMultiplierStmt == null) {
				setPlayerInfluenceMultiplierStmt = new Statement(Command.SET_PLAYER_INFLUENCE_MULTIPLIER, v);
				insert(setPlayerInfluenceMultiplierStmt);
			} else {
				setPlayerInfluenceMultiplierStmt.setFloat(0, v);
			}
		});
		generalPanel.add(playerInfluenceMultiplierField).row();
		
		generalPanel.add(new Label(I18n.tr("landAttr.dayDuration"), skin));
		dayDurationField = new Spinner(skin, 1700.0f, 0, Float.MAX_VALUE, 1f);
		dayDurationField.setValueChangeListener((float v) -> {
			if (setNightTimeStmt == null) {
				setNightTimeStmt = new Statement(Command.SET_NIGHTTIME, v, 0.083f, 0.04f);
				insert(setNightTimeStmt);
			} else {
				setNightTimeStmt.setFloat(0, v);
			}
		});
		generalPanel.add(dayDurationField).row();
		
		generalPanel.add(new Label(I18n.tr("landAttr.nightToDayRatio"), skin));
		nightToDayRatioField = new Spinner(skin, 0.083f, 0, Float.MAX_VALUE, 0.001f);
		nightToDayRatioField.setValueChangeListener((float v) -> {
			if (setNightTimeStmt == null) {
				setNightTimeStmt = new Statement(Command.SET_NIGHTTIME, 1700f, v, 0.04f);
				insert(setNightTimeStmt);
			} else {
				setNightTimeStmt.setFloat(1, v);
			}
		});
		generalPanel.add(nightToDayRatioField).row();
		
		generalPanel.add(new Label(I18n.tr("landAttr.dawnDuskRatio"), skin));
		dawnDuskRatioField = new Spinner(skin, 0.04f, 0, Float.MAX_VALUE, 0.001f);
		dawnDuskRatioField.setValueChangeListener((float v) -> {
			if (setNightTimeStmt == null) {
				setNightTimeStmt = new Statement(Command.SET_NIGHTTIME, 1700f, 0.083f, v);
				insert(setNightTimeStmt);
			} else {
				setNightTimeStmt.setFloat(2, v);
			}
		});
		generalPanel.add(dawnDuskRatioField).row();
		
		generalPanel.add().expandY();	//Filler
		tabs.add(I18n.tr("landAttr.general"), generalPanel);
	}
	
	private void initPlayersPanel(Skin skin) {
		final Array<PlayerInfo> players = new Array<>(PlayerInfo.values());
		ListTable<Statement> table = new ListTable<>(skin);
		table.setHeaderVisible(true);
		table.setCellPadding(2);
		table.setModel(new ListTable.DefaultModel<Statement>(skin) {
			@Override
			public String[] getHeaderNames() {
				return new String[] {
					"",
					I18n.tr("landAttr.players.name"),
					I18n.tr("landAttr.players.computer")
				};
			}
			
			private void remove(int row) {
				Statement stmt = table.getItem(row);
				lhx.getStatements().remove(stmt);
				table.refresh();
			}
			
			private final ChangeListener propChangeListener = new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					@SuppressWarnings("unchecked")
					SelectBox<PlayerInfo> field = (SelectBox<PlayerInfo>)actor;
					int row = table.getItemIndex(field);
					Statement stmt = table.getItem(row);
					stmt.setString(0, field.getSelected().name());
				}
			};
			
			@Override
			public Actor createField(final int row, int col) {
				if (col == 0) {
					return new ImageButton(skin, "tool-delete", () -> remove(row));
				} else if (col == 1) {
					SelectBox<PlayerInfo> field = new SelectBox<>(skin);
					field.setItems(players);
					field.addListener(propChangeListener);
					return field;
				} else if (col == 2) {
					CheckBox field = new CheckBox("", skin);
					field.addListener(new ChangeListener() {
						@Override
						public void changed(ChangeEvent event, Actor actor) {
							Statement stmt = table.getItem(row);
							stmt.setBool(1, field.isChecked());
						}
					});
					undoHelper.attachActors(field);
					return field;
				}
				return null;
			}
			
			@Override
			public void render(int row, Statement item, Cell<Container<Actor>>[] fields) {
				fields[0].getActor().size(24);
				@SuppressWarnings("unchecked")
				SelectBox<PlayerInfo> fProp = (SelectBox<PlayerInfo>)(fields[1].getActor().getActor());
				CheckBox fValue = (CheckBox)(fields[2].getActor().getActor());
				//Content
				PlayerInfo key = PlayerInfo.valueOf(item.getArg(0).getString());
				fProp.setSelected(key);
				fValue.setChecked(item.getArg(1).getBool());
			}
		});
		
		Label addButton = new Label(I18n.tr("landAttr.players.addRow"), skin);
		addButton.setAlignment(Align.center);
		addButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Set<String> currentPlayers = new HashSet<>();
				for (Statement stmt : toggleComputerPlayerStatements) {
					currentPlayers.add(stmt.getArg(0).getString());
				}
				String player = "PLAYER_TWO";
				for (int i = 1; i < players.size; i++) {
					PlayerInfo tPlayer = players.get(i);
					if (!currentPlayers.contains(tPlayer.name())) {
						player = tPlayer.name();
						break;
					}
				}
				Statement stmt = new Statement(Command.TOGGLE_COMPUTER_PLAYER, player, 1);
				insert(stmt);
			}
		});
		table.setExtraRow(addButton);
		table.setPrefHeight(400);
		
		computerPlayersList = table;
		playersPanel.add(computerPlayersList).grow();
		tabs.add(I18n.tr("landAttr.players"), playersPanel);
	}
	
	private void initBalancePanel(Skin skin) {
		ListTable<Statement> table = new ListTable<>(skin);
		table.setHeaderVisible(true);
		table.setCellPadding(2);
		table.setModel(new ListTable.DefaultModel<Statement>(skin) {
			private final SelectBoxStyle propStyleNormal = skin.get("default", SelectBoxStyle.class);
			private final SelectBoxStyle propStyleError = skin.get("error", SelectBoxStyle.class);
			private final Array<BalanceInfo> properties = new Array<>(BalanceInfo.values());
			
			@Override
			public String[] getHeaderNames() {
				return new String[] {
					"",
					I18n.tr("landAttr.balance.property"),
					I18n.tr("landAttr.balance.value")
				};
			}
			
			private void remove(int row) {
				Statement stmt = table.getItem(row);
				lhx.getStatements().remove(stmt);
				table.refresh();
			}
			
			private final ChangeListener propChangeListener = new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					@SuppressWarnings("unchecked")
					SelectBox<BalanceInfo> field = (SelectBox<BalanceInfo>)actor;
					int row = table.getItemIndex(field);
					Statement stmt = table.getItem(row);
					stmt.setInt(0, field.getSelected().ordinal());
				}
			};
			
			@Override
			public Actor createField(final int row, int col) {
				if (col == 0) {
					return new ImageButton(skin, "tool-delete", () -> remove(row));
				} else if (col == 1) {
					SelectBox<BalanceInfo> field = new SelectBox<>(skin);
					field.setItems(properties);
					field.addListener(propChangeListener);
					return field;
				} else if (col == 2) {
					TextSlider field = new TextSlider(skin, 1.0f, 0f, 10f, 0.01f, 4);
					field.setValueChangeListener((float newValue) -> {
						Statement stmt = table.getItem(row);
						stmt.setFloat(1, newValue);
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
				SelectBox<BalanceInfo> fProp = (SelectBox<BalanceInfo>)(fields[1].getActor().getActor());
				TextSlider fValue = (TextSlider)(fields[2].getActor().getActor());
				//Content
				BalanceInfo key = BalanceInfo.valueOf(item.getArg(0).getInt());
				if (key != null) {
					fProp.setSelected(key);
					fProp.setStyle(propStyleNormal);
				} else {
					fProp.setStyle(propStyleError);
				}
				fValue.setValue(item.getArg(1).getFloat());
			}
		});
		
		Label addButton = new Label(I18n.tr("landAttr.balance.addRow"), skin);
		addButton.setAlignment(Align.center);
		addButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Statement stmt = new Statement(Command.SET_GLOBAL_LAND_BALANCE, 0, 1.0f);
				insert(stmt);
			}
		});
		table.setExtraRow(addButton);
		table.setPrefHeight(400);
		
		balanceList = table;
		balancePanel.add(balanceList).grow();
		tabs.add(I18n.tr("landAttr.balance"), balancePanel);
	}
	
	private void initFireflySpellsPanel(Skin skin) {
		ListTable<Statement> table = new ListTable<>(skin);
		table.setHeaderVisible(true);
		table.setCellPadding(2);
		table.setModel(new ListTable.DefaultModel<Statement>(skin) {
			private final Array<SpellSeedInfo> properties = new Array<>(SpellSeedInfo.values());
			
			@Override
			public String[] getHeaderNames() {
				return new String[] {
					"",
					I18n.tr("landAttr.fireflySpellsProb.spell"),
					I18n.tr("landAttr.fireflySpellsProb.probability")
				};
			}
			
			private void remove(int row) {
				Statement stmt = table.getItem(row);
				lhx.getStatements().remove(stmt);
				table.refresh();
			}
			
			private final ChangeListener propChangeListener = new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					@SuppressWarnings("unchecked")
					SelectBox<SpellSeedInfo> field = (SelectBox<SpellSeedInfo>)actor;
					int row = table.getItemIndex(field);
					Statement stmt = table.getItem(row);
					stmt.setValue(0, field.getSelected());
				}
			};
			
			@Override
			public Actor createField(final int row, int col) {
				if (col == 0) {
					return new ImageButton(skin, "tool-delete", () -> remove(row));
				} else if (col == 1) {
					SelectBox<SpellSeedInfo> field = new SelectBox<>(skin);
					field.setItems(properties);
					field.addListener(propChangeListener);
					return field;
				} else if (col == 2) {
					TextSlider field = new TextSlider(skin, 0.0f, 0f, 1f, 0.01f, 3);
					field.setValueChangeListener((float newValue) -> {
						Statement stmt = table.getItem(row);
						stmt.setFloat(1, newValue);
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
				SelectBox<SpellSeedInfo> fProp = (SelectBox<SpellSeedInfo>)(fields[1].getActor().getActor());
				TextSlider fValue = (TextSlider)(fields[2].getActor().getActor());
				//Content
				fProp.setSelected((SpellSeedInfo)item.getArg(0).getValue());
				fValue.setValue(item.getArg(1).getFloat());
			}
		});
		
		Label addButton = new Label(I18n.tr("landAttr.fireflySpellsProb.addRow"), skin);
		addButton.setAlignment(Align.center);
		addButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Statement stmt = new Statement(Command.FIRE_FLY_SPELL_REWARD_PROB, SpellSeedInfo.NONE, 0.0f);
				insert(stmt);
			}
		});
		table.setExtraRow(addButton);
		table.setPrefHeight(400);
		
		fireflySpellsList = table;
		fireflySpellsPanel.add(fireflySpellsList).grow();
		tabs.add(I18n.tr("landAttr.fireflySpellsProb"), fireflySpellsPanel);
	}
	
	private void initSkirmishPanel(Skin skin) {
		skirmishPanel.defaults().top().left().padBottom(2);
		skirmishPanel.columnDefaults(1).expandX().left().padLeft(5);
		
		skirmishPanel.add(new Label(I18n.tr("landAttr.numPlayers"), skin));
		numPlayersField = new Spinner(skin, 2, 1, 8, 1, true);
		skirmishPanel.add(numPlayersField).row();
		
		skirmishPanel.add(new Label(I18n.tr("landAttr.startGameMessage"), skin));
		startGameMessageField = new TextField("", skin);
		startGameMessageField.addListener(new FocusListener() {
			@Override
			public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
				if (!event.isFocused()) {
					if (startGameMessageStmt == null) {
						startGameMessageStmt = new Statement(Command.START_GAME_MESSAGE, startGameMessageField.getText(), numPlayersField.getIntValue());
						insert(startGameMessageStmt);
					} else {
						startGameMessageStmt.setString(0, startGameMessageField.getText());
					}
				}
			}
		});
		skirmishPanel.add(startGameMessageField).growX().prefWidth(370).row();
		
		skirmishPanel.add(new Label(I18n.tr("landAttr.gameMessageLine"), skin));
		gameMessageLineField = new TextField("", skin);
		gameMessageLineField.addListener(new FocusListener() {
			@Override
			public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
				if (!event.isFocused()) {
					if (addGameMessageLineStmt == null) {
						addGameMessageLineStmt = new Statement(Command.ADD_GAME_MESSAGE_LINE, gameMessageLineField.getText(), numPlayersField.getIntValue());
						insert(addGameMessageLineStmt);
					} else {
						addGameMessageLineStmt.setString(0, gameMessageLineField.getText());
					}
				}
			}
		});
		skirmishPanel.add(gameMessageLineField).growX().prefWidth(370).row();
		
		numPlayersField.setValueChangeListener((float v) -> {
			if (startGameMessageStmt == null) {
				startGameMessageField.setText(((v >= 0 && v <= 8) ? NUMBER_NAMES[(int)v] : (int)v) + " Gods");
				FocusEvent focusLost = new FocusEvent();
				focusLost.setType(Type.keyboard);
				startGameMessageField.fire(focusLost);
			} else {
				startGameMessageStmt.setInt(1, (int)v);
			}
			if (addGameMessageLineStmt == null) {
				gameMessageLineField.setText("Battle " + ((int)v) + " gods for this realm");
				FocusEvent focusLost = new FocusEvent();
				focusLost.setType(Type.keyboard);
				gameMessageLineField.fire(focusLost);
			} else {
				addGameMessageLineStmt.setInt(1, (int)v);
			}
		});
		
		skirmishPanel.add().expandY();	//Filler
		tabs.add(I18n.tr("landAttr.skirmish"), skirmishPanel);
	}
	
	private final UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.CHANGE && event.getProperty() == MainApp.Property.LHX) {
				setLHX(app.getLHX());
			}
		}
	};
	
	public void setLHX(LHXFile lhx) {
		if (this.lhx != null) {
			this.lhx.listeners.remove(lhxChangeListener);
			for (Entry<Command, Statement> e : order.entrySet()) {
				e.setValue(null);
			}
			balanceStatements.clear();
			fireflySpellsStatements.clear();
			toggleComputerPlayerStatements.clear();
		}
		this.lhx = lhx;
		if (lhx != null) {
			versionStmt = lhx.findFirstStatement(Command.VERSION);
			if (versionStmt != null) {
				versionField.setValue(versionStmt.getArg(0).getFloat());
				order.put(Command.VERSION, versionStmt);
			}
			
			setLandNumberStmt = lhx.findFirstStatement(Command.SET_LAND_NUMBER);
			if (setLandNumberStmt != null) {
				landNumberField.setValue(setLandNumberStmt.getArg(0).getInt());
				order.put(Command.SET_LAND_NUMBER, setLandNumberStmt);
			}
			
			loadLandscapeStmt = lhx.findFirstStatement(Command.LOAD_LANDSCAPE);
			if (loadLandscapeStmt != null) {
				landscapeField.setText(loadLandscapeStmt.getArg(0).getString());
				order.put(Command.LOAD_LANDSCAPE, loadLandscapeStmt);
			}
			
			setTownInfluenceMultiplierStmt = lhx.findFirstStatement(Command.SET_TOWN_INFLUENCE_MULTIPLIER);
			if (setTownInfluenceMultiplierStmt != null) {
				townInfluenceMultiplierField.setValue(setTownInfluenceMultiplierStmt.getArg(0).getFloat());
				order.put(Command.SET_TOWN_INFLUENCE_MULTIPLIER, setTownInfluenceMultiplierStmt);
			}
			
			setPlayerInfluenceMultiplierStmt = lhx.findFirstStatement(Command.SET_PLAYER_INFLUENCE_MULTIPLIER);
			if (setPlayerInfluenceMultiplierStmt != null) {
				playerInfluenceMultiplierField.setValue(setPlayerInfluenceMultiplierStmt.getArg(0).getFloat());
				order.put(Command.SET_PLAYER_INFLUENCE_MULTIPLIER, setPlayerInfluenceMultiplierStmt);
			}
			
			setNightTimeStmt = lhx.findFirstStatement(Command.SET_NIGHTTIME);
			if (setNightTimeStmt != null) {
				dayDurationField.setValue(setNightTimeStmt.getArg(0).getFloat());
				nightToDayRatioField.setValue(setNightTimeStmt.getArg(1).getFloat());
				dawnDuskRatioField.setValue(setNightTimeStmt.getArg(2).getFloat());
				order.put(Command.SET_NIGHTTIME, setNightTimeStmt);
			}
			
			startGameMessageStmt = lhx.findFirstStatement(Command.START_GAME_MESSAGE);
			if (startGameMessageStmt != null) {
				startGameMessageField.setText(startGameMessageStmt.getArg(0).getString());
				numPlayersField.setValue(startGameMessageStmt.getArg(1).getInt());
				order.put(Command.START_GAME_MESSAGE, startGameMessageStmt);
			}
			
			addGameMessageLineStmt = lhx.findFirstStatement(Command.ADD_GAME_MESSAGE_LINE);
			if (addGameMessageLineStmt != null) {
				gameMessageLineField.setText(addGameMessageLineStmt.getArg(0).getString());
				numPlayersField.setValue(addGameMessageLineStmt.getArg(1).getInt());
				order.put(Command.ADD_GAME_MESSAGE_LINE, addGameMessageLineStmt);
			}
			
			for (Statement stmt : lhx.getStatements()) {
				if (stmt.isCommand()) {
					Command cmd = stmt.getCommand();
					if (cmd == Command.SET_GLOBAL_LAND_BALANCE) {
						balanceStatements.add(stmt);
					} else if (cmd == Command.FIRE_FLY_SPELL_REWARD_PROB) {
						fireflySpellsStatements.add(stmt);
					} else if (cmd == Command.TOGGLE_COMPUTER_PLAYER) {
						toggleComputerPlayerStatements.add(stmt);
					}
				}
			}
			balanceList.setItems(balanceStatements);
			fireflySpellsList.setItems(fireflySpellsStatements);
			computerPlayersList.setItems(toggleComputerPlayerStatements);
			
			lhx.listeners.add(lhxChangeListener);
		}
	}
	
	private final UChangeListener lhxChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LHXFile.Property.STATEMENTS) {
				if (event.getType() == EventType.ADD) {
					Statement stmt = (Statement)event.getNewValue();
					if (stmt.isCommand()) {
						boolean managed = true;
						Command cmd = stmt.getCommand();
						switch (cmd) {
							case VERSION:
								versionStmt = stmt;
								versionField.setValue(stmt.getArg(0).getFloat());
								break;
							case SET_LAND_NUMBER:
								setLandNumberStmt = stmt;
								landNumberField.setValue(stmt.getArg(0).getInt());
								break;
							case LOAD_LANDSCAPE:
								loadLandscapeStmt = stmt;
								landscapeField.setText(stmt.getArg(0).getString());
								break;
							case SET_TOWN_INFLUENCE_MULTIPLIER:
								setTownInfluenceMultiplierStmt = stmt;
								townInfluenceMultiplierField.setValue(stmt.getArg(0).getFloat());
								break;
							case SET_PLAYER_INFLUENCE_MULTIPLIER:
								setPlayerInfluenceMultiplierStmt = stmt;
								playerInfluenceMultiplierField.setValue(stmt.getArg(0).getFloat());
								break;
							case SET_NIGHTTIME:
								setNightTimeStmt = stmt;
								dayDurationField.setValue(stmt.getArg(0).getFloat());
								nightToDayRatioField.setValue(stmt.getArg(1).getFloat());
								dawnDuskRatioField.setValue(stmt.getArg(2).getFloat());
								break;
							case START_GAME_MESSAGE:
								startGameMessageStmt = stmt;
								startGameMessageField.setText(stmt.getArg(0).getString());
								numPlayersField.setValue(startGameMessageStmt.getArg(1).getInt());
								break;
							case ADD_GAME_MESSAGE_LINE:
								addGameMessageLineStmt = stmt;
								gameMessageLineField.setText(stmt.getArg(0).getString());
								numPlayersField.setValue(addGameMessageLineStmt.getArg(1).getInt());
								break;
							case SET_GLOBAL_LAND_BALANCE:
								balanceStatements.add(stmt);
								balanceList.setItems(balanceStatements);
								break;
							case FIRE_FLY_SPELL_REWARD_PROB:
								fireflySpellsStatements.add(stmt);
								fireflySpellsList.setItems(fireflySpellsStatements);
								break;
							case TOGGLE_COMPUTER_PLAYER:
								toggleComputerPlayerStatements.add(stmt);
								computerPlayersList.setItems(toggleComputerPlayerStatements);
								break;
							
							default:
								managed = false;
						}
						if (managed) {
							order.put(cmd, stmt);
						}
					}
				} else if (event.getType() == EventType.CHANGE) {
					Statement stmt = (Statement)event.getNewValue();
					if (stmt.isCommand()) {
						Command cmd = stmt.getCommand();
						switch (cmd) {
							case VERSION:
								versionField.setValue(stmt.getArg(0).getFloat());
								break;
							case SET_LAND_NUMBER:
								landNumberField.setValue(stmt.getArg(0).getInt());
								break;
							case LOAD_LANDSCAPE:
								landscapeField.setText(stmt.getArg(0).getString());
								break;
							case SET_TOWN_INFLUENCE_MULTIPLIER:
								townInfluenceMultiplierField.setValue(stmt.getArg(0).getFloat());
								break;
							case SET_PLAYER_INFLUENCE_MULTIPLIER:
								playerInfluenceMultiplierField.setValue(stmt.getArg(0).getFloat());
								break;
							case SET_NIGHTTIME:
								dayDurationField.setValue(stmt.getArg(0).getFloat());
								nightToDayRatioField.setValue(stmt.getArg(1).getFloat());
								dawnDuskRatioField.setValue(stmt.getArg(2).getFloat());
								break;
							case START_GAME_MESSAGE:
								startGameMessageField.setText(stmt.getArg(0).getString());
								numPlayersField.setValue(startGameMessageStmt.getArg(1).getInt());
								break;
							case ADD_GAME_MESSAGE_LINE:
								gameMessageLineField.setText(stmt.getArg(0).getString());
								numPlayersField.setValue(addGameMessageLineStmt.getArg(1).getInt());
								break;
							case SET_GLOBAL_LAND_BALANCE:
								int row = balanceStatements.indexOf(stmt, true);
								if (row >= 0) {
									balanceList.refresh(row);
								} else {
									balanceStatements.add(stmt);
									balanceList.setItems(balanceStatements);
								}
								break;
							case FIRE_FLY_SPELL_REWARD_PROB:
								row = fireflySpellsStatements.indexOf(stmt, true);
								if (row >= 0) {
									fireflySpellsList.refresh(row);
								} else {
									fireflySpellsStatements.add(stmt);
									fireflySpellsList.setItems(fireflySpellsStatements);
								}
								break;
							case TOGGLE_COMPUTER_PLAYER:
								row = toggleComputerPlayerStatements.indexOf(stmt, true);
								if (row >= 0) {
									computerPlayersList.refresh(row);
								} else {
									toggleComputerPlayerStatements.add(stmt);
									computerPlayersList.setItems(toggleComputerPlayerStatements);
								}
								break;
							
							default:
						}
					}
				} else if (event.getType() == EventType.REMOVE) {
					Statement stmt = (Statement)event.getOldValue();
					if (stmt.isCommand()) {
						Command cmd = stmt.getCommand();
						if (cmd == Command.SET_GLOBAL_LAND_BALANCE) {
							balanceStatements.removeValue(stmt, true);
							if (balanceStatements.isEmpty()) {
								order.put(cmd, null);
							} else {
								order.put(cmd, balanceStatements.get(balanceStatements.size - 1));
							}
							balanceList.setItems(fireflySpellsStatements);
						} else if (cmd == Command.FIRE_FLY_SPELL_REWARD_PROB) {
							fireflySpellsStatements.removeValue(stmt, true);
							if (fireflySpellsStatements.isEmpty()) {
								order.put(cmd, null);
							} else {
								order.put(cmd, fireflySpellsStatements.get(fireflySpellsStatements.size - 1));
							}
							fireflySpellsList.setItems(fireflySpellsStatements);
						} else if (cmd == Command.TOGGLE_COMPUTER_PLAYER) {
							toggleComputerPlayerStatements.removeValue(stmt, true);
							computerPlayersList.setItems(toggleComputerPlayerStatements);
						} else if (order.containsKey(cmd)) {
							order.put(cmd, null);
						}
					}
				}
			}
		}
	};
	
	private static int getPlayerOrdinal(String player) {
		try {
			return PlayerInfo.valueOf(player).ordinal();
		} catch (Exception e) {
			return Integer.MAX_VALUE;
		}
	}
	
	private void insert(Statement stmt) {
		Command cmd = stmt.getCommand();
		app.getEditManager().begin(I18n.tr("action.editObject", cmd.objectDisplayName));
		List<Statement> statements = lhx.getStatements();
		int index = 0;
		if (cmd == Command.TOGGLE_COMPUTER_PLAYER) {
			//Skip all statements until one for a player with the same or a greater index has been found
			final int player = getPlayerOrdinal(stmt.getPlayer());
			for (Statement tStmt : statements) {
				if (tStmt.isCommand()) {
					if (tStmt.isChildOfPlayer()) {
						int tPlayer = getPlayerOrdinal(tStmt.getPlayer());
						if (tPlayer >= player) {
							break;
						}
					}
				}
				index++;
			}
		} else {
			Statement after = null;
			for (Entry<Command, Statement> e : order.entrySet()) {
				if (e.getValue() != null) {
					after = e.getValue();
				}
				if (e.getKey() == cmd) {
					break;
				}
			}
			if (after != null) {
				index = statements.indexOf(after) + 1;
			} else {
				//Skip initial comments
				for (Statement tStmt : statements) {
					if (tStmt.isCommand()) {
						break;
					}
					index++;
				}
			}
		}
		statements.add(index, stmt);
		order.put(cmd, stmt);
		app.getEditManager().end();
	}
	
	@Override
	public void dispose() {
		app.listeners.remove(appChangeListener);
		setLHX(null);
		super.dispose();
	}
}
