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
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Array;

import it.ld.bw.lhx.Command;
import it.ld.bw.lhx.Command.ArgType;
import it.ld.bw.lhx.Command.Argument;
import it.ld.bw.lhx.LHXCoord;
import it.ld.bw.lhx.LHXFile;
import it.ld.bw.lhx.LHXParser;
import it.ld.bw.lhx.Statement;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.gfx.Coord;
import it.ld.bw.lndgui.gfx.Object3D;
import it.ld.libgdx.ui.components.ImageButton;
import it.ld.libgdx.ui.components.ListTable;
import it.ld.libgdx.ui.components.MenuItemAction;
import it.ld.libgdx.ui.components.PopupMenu;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.ListTable.ItemClickEvent;
import it.ld.utils.UChangeListener;

public class LHXScriptWindow extends SmartWindow {
	private final MainApp app;
	
	private LHXFile lhx;
	
	private TextField searchField;
	private ListTable<Statement> list;
	
	private LHXParser parser = new LHXParser();
	
	private boolean focusing = false;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		LHXScriptWindow window = (LHXScriptWindow) SmartWindow.getSingleInstance("lhxScriptWindow");
		if (window == null) {
			window = new LHXScriptWindow(app, skin, true);
			window.setDockingManager(app.getDockingManager());
			window.setSingleInstance("lhxScriptWindow");
			window.show(stage, stage.getWidth() - window.getWidth(), stage.getHeight() - window.getHeight() - 62, false);
		}
		window.toFront();
	}
	
	public static void hideInstance() {
		LHXScriptWindow window = (LHXScriptWindow) SmartWindow.getSingleInstance("lhxScriptWindow");
		if (window != null) {
			window.remove();
		}
	}
	
	public LHXScriptWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("lhxScript.title"), skin, Attribute.RESIZABLE, Attribute.DOCKABLE);
		this.setAutodispose(autodispose);
		this.app = app;
		
		setMinSize(200, 200);
		
		Table searchPanel = new Table();
		searchField = new TextField("", skin);
		searchField.addListener(new InputListener() {
			@Override
			public boolean keyTyped(InputEvent event, char character) {
				if (lhx != null && event.getKeyCode() != Keys.ENTER) {
					search(searchField.getText());
				}
				return false;
			}
			
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Keys.ENTER) {
					searchNext(searchField.getText());
				}
				return false;
			}
		});
		searchPanel.add(searchField).growX();
		ImageButton searchPrevButton = new ImageButton(skin, "icon-arrow-left", I18n.tr("dialog.findPrev"), () -> searchPrev(searchField.getText()));
		searchPanel.add(searchPrevButton);
		ImageButton searchNextButton = new ImageButton(skin, "icon-arrow-right", I18n.tr("dialog.findNext"), () -> searchNext(searchField.getText()));
		searchPanel.add(searchNextButton);
		add(searchPanel).growX().pad(5, 5, 0, 5);
		row();
		
		list = new ListTable<Statement>(skin)
		.setCellPadding(0);
		list.setModel(new ListTable.DefaultModel<Statement>(skin) {
			private final LabelStyle linenoStyle = skin.get("mono", LabelStyle.class);
			private final TextFieldStyle normalStyle = skin.get("code-normal", TextFieldStyle.class);
			private final TextFieldStyle errorStyle = skin.get("code-error", TextFieldStyle.class);
			private final TextFieldStyle commentStyle = skin.get("code-comment", TextFieldStyle.class);
			
			private int cursorPosBefore;
			
			@Override
			public String[] getHeaderNames() {
				return new String[] {"", ""};
			}
			
			private final Vector2 tmpPos = new Vector2();
			
			private final InputListener captureListener = new InputListener() {
				@Override
				public boolean keyDown(InputEvent event, int keycode) {
					CodeField field = (CodeField)event.getTarget();
					cursorPosBefore = field.getCursorPosition();
					return false;
				}
			};
			
			private final InputListener inputListener = new InputListener() {
				private boolean wasEmpty;
				
				public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
					tmpPos.set(x, y);
					event.getTarget().localToAscendantCoordinates(list, tmpPos);
					int row = list.getItemIndexAt(tmpPos.y);
					if (row < 0) return;
					Statement stmt = lhx.getStatements().get(row);
					if (stmt.isCommand() && stmt.getCommand().hasPosition()) {
						LHXCoord pos = stmt.getPosition();
						app.getView3D().showTmpMarker(new Coord(pos.x, pos.z));
					}
				};
				
				@Override
				public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
					app.getView3D().hideTmpMarker();
				}
				
				@Override
				public boolean keyDown(InputEvent event, int keycode) {
					if (keycode == Keys.HOME || keycode == Keys.END) {
						event.stop();	//Prevent events to be propagated to the scroll pane.
						return false;
					}
					if (keycode == Keys.UP || keycode == Keys.DOWN || keycode == Keys.LEFT || keycode == Keys.RIGHT ||
							keycode == Keys.PAGE_UP || keycode == Keys.PAGE_DOWN || 
							keycode == Keys.ENTER || keycode == Keys.BACKSPACE || keycode == Keys.FORWARD_DEL ||
							(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && keycode == Keys.SPACE)) {
						CodeField field = (CodeField)event.getTarget();
						wasEmpty = field.getText().isEmpty();
						return true;
					}
					return false;
				}
				
				@Override
				public boolean keyUp(InputEvent event, int keycode) {
					CodeField field = (CodeField)event.getTarget();
					int row = list.getItemIndex(field);
					if (row < 0) return false;
					switch (keycode) {
						case Keys.UP:
							if (row <= 0) return false;
							field = (CodeField) list.getField(row - 1, 1).getActor();
							list.getStage().setKeyboardFocus(field);
							field.setCursorPosition(cursorPosBefore);
							return true;
						case Keys.DOWN:
							if (row >= lhx.getStatements().size() - 1) return false;
							field = (CodeField) list.getField(row + 1, 1).getActor();
							list.getStage().setKeyboardFocus(field);
							field.setCursorPosition(cursorPosBefore);
							return true;
						case Keys.LEFT:
							if (row <= 0) return false;
							if (cursorPosBefore > 0) return false;
							field = (CodeField) list.getField(row - 1, 1).getActor();
							list.getStage().setKeyboardFocus(field);
							field.setCursorPosition(field.getText().length());
							return true;
						case Keys.RIGHT:
							if (row >= lhx.getStatements().size() - 1) return false;
							if (cursorPosBefore < field.getText().length() - 1) return false;
							field = (CodeField) list.getField(row + 1, 1).getActor();
							list.getStage().setKeyboardFocus(field);
							field.setCursorPosition(0);
							return true;
						case Keys.ENTER:
							int pos;
							if (cursorPosBefore == 0) {
								pos = row;
							} else if (cursorPosBefore == field.getText().length()) {
								pos = row + 1;
							} else {
								return false;
							}
							Statement stmt = new Statement("");
							lhx.getStatements().add(pos, stmt);
							updateAll(true);
							field = (CodeField) list.getField(row + 1, 1).getActor();
							field.setCursorPosition(0);
							list.getStage().setKeyboardFocus(field);
							return true;
						case Keys.BACKSPACE:
							if (row <= 0) return false;
							if (!wasEmpty) return false;
							lhx.getStatements().remove(row);
							updateAll(true);
							field = (CodeField) list.getField(row - 1, 1).getActor();
							list.getStage().setKeyboardFocus(field);
							field.setCursorPosition(field.getText().length());
							return true;
						case Keys.FORWARD_DEL:
							if (row >= lhx.getStatements().size() - 1) return false;
							if (!wasEmpty) return false;
							lhx.getStatements().remove(row);
							updateAll(true);
							field = (CodeField) list.getField(row, 1).getActor();
							list.getStage().setKeyboardFocus(field);
							field.setCursorPosition(0);
							return true;
						case Keys.PAGE_UP:
							int visibleRows = list.getVisibleRows();
							row = Math.max(0, row - visibleRows);
							field = (CodeField) list.getField(row, 1).getActor();
							list.getStage().setKeyboardFocus(field);
							field.setCursorPosition(cursorPosBefore);
							return true;
						case Keys.PAGE_DOWN:
							visibleRows = list.getVisibleRows();
							row = Math.min(row + visibleRows, lhx.getStatements().size() - 1);
							field = (CodeField) list.getField(row, 1).getActor();
							list.getStage().setKeyboardFocus(field);
							field.setCursorPosition(cursorPosBefore);
							return true;
						case Keys.HOME:
							if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
								row = 0;
								field = (CodeField) list.getField(row, 1).getActor();
								list.getStage().setKeyboardFocus(field);
								field.setCursorPosition(0);
							}
							break;
						case Keys.END:
							if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
								row = lhx.getStatements().size() - 1;
								field = (CodeField) list.getField(row, 1).getActor();
								list.getStage().setKeyboardFocus(field);
								field.setCursorPosition(field.getText().length());
							}
							break;
					}
					if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && keycode == Keys.SPACE) {
						showHint(row);
						return true;
					}
					return false;
				}
			};
			
			private final FocusListener focusListener = new FocusListener() {
				private String prevText = "";
				
				@Override
				public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
					CodeField.forceDrawSelectionOn = null;
					CodeField field = (CodeField)actor;
					if (focused) {
						focusing = true;
						prevText = field.getText();
						int row = list.getItemIndex(actor);
						list.setSelectedIndex(row);
						Statement stmt = lhx.getStatements().get(row);
						app.getView3D().setSelectedObject(stmt);
						focusing = false;
					} else {
						String newText = field.getText();
						if (!prevText.equals(newText)) {
							int row = list.getItemIndex(actor);
							if (row >= 0) {
								processRow(row);
							}
						}
					}
				}
			};
			
			@Override
			public Actor createField(int row, int col) {
				if (col == 0) {
					Label field = new Label(String.valueOf(row + 1), linenoStyle);
					field.setColor(Color.GRAY);
					return field;
				} else if (col == 1) {
					CodeField field = new CodeField("", skin);
					field.addCaptureListener(captureListener);
					field.addListener(inputListener);
					field.addListener(focusListener);
					return field;
				}
				return super.createField(row, col);
			}
			
			@Override
			public void render(int row, Statement stmt, Cell<Container<Actor>>[] fields) {
				fields[0].getActor().fill(false, true).right();
				CodeField fStmt = ((CodeField)fields[1].getActor().getActor());
				fields[1].expandX().pad(0, 10, 0, 3);
				//Content
				fStmt.setText(stmt.toString());
				if (stmt.isValid()) {
					fStmt.setStyle(stmt.isCommand() ? normalStyle : commentStyle);
				} else {
					fStmt.setStyle(errorStyle);
				}
			}
		})
		.setHeaderVisible(false)
		.setRowsBackground(null, null);
		list.addListener(new ListTable.ItemClickListener<Statement>() {
			@Override
			public void clicked(ItemClickEvent<Statement> event, int row, int col, Statement item) {
				if (col == 0) {
					CodeField field = (CodeField) list.getField(row, 1).getActor();
					field.setCursorPosition(0);
					getStage().setKeyboardFocus(field);
				}
			}
		});
		
		add(list).minSize(160, 160).prefSize(400, 500).grow().pad(5);
		
		setLHX(app.getLHX());
		
		app.listeners.add(appChangeListener);
		app.getView3D().listeners.add(view3dChangeListener);
		setSize(800, 700);
	}
	
	private final UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.LHX) {
				setLHX(app.getLHX());
			}
		}
	};
	
	private final UChangeListener view3dChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == View3D.Property.SELECTION && !focusing) {
				List<Object3D> objects = app.getView3D().getSelectedObjects();
				if (objects.size() == 1) {
					Object3D obj = objects.get(0);
					int index = lhx.getStatements().indexOf(obj.getStatement());
					list.setSelectedIndex(index);
					CodeField field = (CodeField) list.getField(index, 1).getActor();
					field.setSelection(0, 999);
					CodeField.forceDrawSelectionOn = field;
				} else {
					CodeField.forceDrawSelectionOn = null;
				}
			}
		}
	};
	
	public boolean restartSearchFromTop;
	
	public void search(String text) {
		final int rows = list.getItems().size;
		if (rows == 0) return;
		text = text.toLowerCase();
		int row = Math.max(0, list.getSelectedIndex());
		CodeField field = (CodeField) list.getField(row, 1).getActor();
		int start = field.getSelection().isEmpty() ? field.getCursorPosition() : field.getSelectionStart();
		if (text.isEmpty()) {
			field.setCursorPosition(start);
			CodeField.forceDrawSelectionOn = null;
			return;
		}
		if (restartSearchFromTop) {
			row = 0;
			start = 0;
			field = (CodeField) list.getField(row, 1).getActor();
			restartSearchFromTop = false;
		}
		int p = field.getText().toLowerCase().indexOf(text, start);
		while (p < 0 && row < rows - 1) {
			row++;
			field = (CodeField) list.getField(row, 1).getActor();
			p = field.getText().toLowerCase().indexOf(text);
		}
		if (p >= 0) {
			list.setSelectedIndex(row);
			field.setSelection(p, p + text.length());
			if (getStage().getKeyboardFocus() != null && getStage().getKeyboardFocus().isDescendantOf(list)) {
				getStage().setKeyboardFocus(null);
			}
			CodeField.forceDrawSelectionOn = field;
		} else {
			CodeField.forceDrawSelectionOn = null;
			restartSearchFromTop = true;
		}
	}
	
	public void searchPrev(String text) {
		final int rows = list.getItems().size;
		if (rows == 0) return;
		if (text.isEmpty()) return;
		text = text.toLowerCase();
		int row = Math.max(0, list.getSelectedIndex());
		CodeField field = (CodeField) list.getField(row, 1).getActor();
		int end = field.getSelection().isEmpty() ? field.getCursorPosition() : Math.min(field.getSelectionStart(), field.getCursorPosition());
		if (restartSearchFromTop) {
			row = rows - 1;
			field = (CodeField) list.getField(row, 1).getActor();
			end = field.getText().length();
			restartSearchFromTop = false;
		}
		int p = field.getText().toLowerCase().lastIndexOf(text, end - 1);
		while (p < 0 && row > 0) {
			row--;
			field = (CodeField) list.getField(row, 1).getActor();
			p = field.getText().toLowerCase().lastIndexOf(text);
		}
		if (p >= 0) {
			list.setSelectedIndex(row);
			field.setSelection(p, p + text.length());
			if (getStage().getKeyboardFocus() != null && getStage().getKeyboardFocus().isDescendantOf(list)) {
				getStage().setKeyboardFocus(null);
			}
			CodeField.forceDrawSelectionOn = field;
		} else {
			restartSearchFromTop = true;
			app.showTempStatusMessage(I18n.tr("messages.foundFirstResume"));
		}
	}
	
	public void searchNext(String text) {
		final int rows = list.getItems().size;
		if (rows == 0) return;
		if (text.isEmpty()) return;
		text = text.toLowerCase();
		int row = Math.max(0, list.getSelectedIndex());
		CodeField field = (CodeField) list.getField(row, 1).getActor();
		int start = field.getSelection().isEmpty() ? field.getCursorPosition() : (field.getSelectionStart() + field.getSelection().length());
		if (restartSearchFromTop) {
			row = 0;
			start = 0;
			field = (CodeField) list.getField(row, 1).getActor();
			restartSearchFromTop = false;
		}
		int p = field.getText().toLowerCase().indexOf(text, start);
		while (p < 0 && row < rows - 1) {
			row++;
			field = (CodeField) list.getField(row, 1).getActor();
			p = field.getText().toLowerCase().indexOf(text);
		}
		if (p >= 0) {
			list.setSelectedIndex(row);
			field.setSelection(p, p + text.length());
			if (getStage().getKeyboardFocus() != null && getStage().getKeyboardFocus().isDescendantOf(list)) {
				getStage().setKeyboardFocus(null);
			}
			CodeField.forceDrawSelectionOn = field;
		} else {
			restartSearchFromTop = true;
			app.showTempStatusMessage(I18n.tr("messages.foundLastResume"));
		}
	}
	
	private void showHint(final int row) {
		final CodeField field = (CodeField) list.getField(row, 1).getActor();
		String text = field.getText().substring(0, field.getCursorPosition());
		String trimmed = text.trim();
		if (trimmed.startsWith("*") || trimmed.toUpperCase().startsWith("REM ")) return;
		List<Object> hints = new ArrayList<>();
		Argument arg = null;
		String prefix;
		int p = trimmed.indexOf('(');
		if (p < 0) {
			prefix = trimmed.toUpperCase();
			for (Command cmd : Command.values()) {
				if (cmd.name().startsWith(prefix)) {
					hints.add(cmd.name());
				}
			}
		} else {
			String sCmd = trimmed.substring(0, p).trim().toUpperCase();
			Command cmd;
			try {
				cmd = Command.valueOf(sCmd);
			} catch (Exception e) {
				return;
			}
			String sArgs = trimmed.substring(p + 1).trim();
			if (sArgs.endsWith(")")) {
				sArgs = sArgs.substring(0, sArgs.length() - 1).trim();
			}
			String[] args = splitArgs(sArgs);
			int index = args.length - 1;
			prefix = args[index];
			String sArg = prefix.toUpperCase();
			if (sArg.startsWith("\"")) {
				sArg = sArg.substring(1);
				if (sArg.endsWith("\"")) {
					sArg = sArg.substring(0, sArg.length() - 1);
				}
			}
			arg = cmd.args[index];
			Class<?> enumClass = arg.getEffectiveType().enumClass;
			if (enumClass != null) {
				List<Object> additionalHints = new ArrayList<>();
				for (Object c : enumClass.getEnumConstants()) {
					Enum<?> e = (Enum<?>)c;
					if (e.name().startsWith(sArg)) {
						hints.add(e);
					} else if (e.name().contains(sArg)) {
						additionalHints.add(e);
					}
				}
				hints.addAll(additionalHints);
			}
		}
		if (!hints.isEmpty()) {
			final ArgType type = arg != null ? arg.type : null;
			final String pfx = prefix;
			MenuItemAction[] items = new MenuItemAction[hints.size()];
			for (int i = 0; i < items.length; i++) {
				final Object hint = hints.get(i);
				items[i] = new MenuItemAction("", hint.toString(), () -> {
					String oldText = field.getText();
					String begin = oldText.substring(0, field.getCursorPosition() - pfx.length());
					String end = oldText.substring(field.getCursorPosition());
					int p1 = end.indexOf('(');
					if (p1 < 0) p1 = end.indexOf(',');
					if (p1 < 0) p1 = end.indexOf(')');
					if (p1 < 0) p1 = end.length();
					end = end.substring(p1);
					String sHint = "";
					if (type == ArgType.STRING) {
						sHint = "\"" +  hint.toString() + "\"";
					} else if (type == ArgType.INT) {
						sHint = String.valueOf(((Enum<?>)hint).ordinal());
					} else {
						sHint = hint.toString();
					}
					String newText = begin + sHint + end;
					field.setText(newText);
					field.getStage().setKeyboardFocus(field);
					field.setCursorPosition(begin.length() + sHint.length());
					processRow(row);
				});
			}
			float x = field.getCursorX();
			PopupMenu.show1(field, x, 0, items);
		}
	}
	
	private String[] splitArgs(String text) {
		String arg = "";
		boolean inStr = false;
		char[] chars = text.trim().toCharArray();
		List<String> args = new ArrayList<>();
		for (char c : chars) {
			if (inStr) {
				arg += c;
				if (c == '"') {
					inStr = false;
				}
			} else if (c == ',') {
				args.add(arg.trim());
				arg = "";
			} else if (c == '"') {
				arg += "\"";
				inStr = true;
			} else {
				arg += c;
			}
		}
		args.add(arg.trim());
		return args.toArray(new String[0]);
	}
	
	private void processRow(int row) {
		CodeField field = (CodeField)list.getField(row, 1).getActor();
		String text = field.getText();
		Statement stmt = lhx.getStatements().get(row);
		try {
			List<Statement> stmts = parser.parse(text, false);
			if (stmts.isEmpty()) {
				stmt.clear();
				list.refresh(row);
			} else if (stmts.size() == 1) {
				stmt.set(stmts.get(0));
				list.refresh(row);
			} else {
				throw new Exception("Invalid number of statements");
			}
		} catch (Exception e) {
			stmt.setInvalid(text);
			list.refresh(row);
		}
	}
	
	private void setLHX(LHXFile lhx) {
		if (lhx != this.lhx) {
			if (this.lhx != null) {
				this.lhx.listeners.remove(lhxChangeListener);
			}
			this.lhx = lhx;
			updateAll(true);
			if (lhx != null) {
				lhx.listeners.add(lhxChangeListener);
			}
		}
	}
	
	private boolean mustUpdateAll;
	
	private void updateAll(boolean immed) {
		if (immed) {
			mustUpdateAll = false;
			if (lhx != null) {
				list.setItems(new Array<>(lhx.getStatements().toArray(new Statement[0])));
			} else {
				list.setItems(new Array<>());
			}
		} else {
			mustUpdateAll = true;
		}
	}
	
	private final UChangeListener lhxChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LHXFile.Property.STATEMENTS) {
				if (event.getType() == EventType.CHANGE && event.getIndex() >= 0) {
					list.refresh(event.getIndex());
				} else if (event.isLast()) {
					updateAll(false);
				}
			}
		}
	};
	
	@Override
	public void act(float delta) {
		if (mustUpdateAll) {
			updateAll(true);
		}
		super.act(delta);
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		app.listeners.remove(appChangeListener);
		app.getView3D().listeners.remove(view3dChangeListener);
		setLHX(null);
		super.dispose();
	}
	
	
	private static class CodeField extends TextField {
		private static CodeField forceDrawSelectionOn;
		
		public CodeField(String text, Skin skin) {
			super(text, skin);
		}
		
		/*public float getTextWidth() {
			return glyphPositions.get(glyphPositions.size - 1);
		}*/
		
		@Override
		public boolean hasKeyboardFocus() {
			return super.hasKeyboardFocus() || this == forceDrawSelectionOn;
		}
		
		public float getCursorX() {
			BitmapFont font = getStyle().font;
			return textOffset + glyphPositions.get(cursor) - glyphPositions.get(visibleTextStart) + fontOffset + font.getData().cursorX;
		}
		
		@Override
		protected InputListener createInputListener() {
			return new TextFieldClickListener() {
				@Override
				public boolean keyTyped(InputEvent event, char character) {
					if (character == ' ' && Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
						return true;
					}
					return super.keyTyped(event, character);
				}
			};
		}
	}
}
