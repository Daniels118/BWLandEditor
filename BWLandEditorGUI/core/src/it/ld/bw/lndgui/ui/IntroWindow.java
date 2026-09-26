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

import java.io.File;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import it.ld.bw.lhx.LHXFile;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.RecentFiles;
import it.ld.bw.lndgui.RecoveryManager;
import it.ld.bw.lndgui.RecoveryManager.RecoveredFile;
import it.ld.libgdx.ui.components.MenuItemAction;
import it.ld.libgdx.ui.components.PopupMenu;
import it.ld.libgdx.ui.components.SmartWindow;

public class IntroWindow extends SmartWindow {
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		IntroWindow window = (IntroWindow) SmartWindow.getSingleInstance("introWindow");
		if (window == null) {
			window = new IntroWindow(app, skin, true);
			window.setSingleInstance("introWindow");
		}
		window.show(stage, true);
		window.toFront();
	}
	
	public static void hideInstance() {
		IntroWindow window = (IntroWindow) SmartWindow.getSingleInstance("introWindow");
		if (window != null) {
			window.remove();
		}
	}
	
	
	public IntroWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("introWindow.title"), skin);
		this.setAutodispose(autodispose);
		
		add(createButton("file-new", "introWindow.new", () -> {
			hideInstance();
			app.newProject();
		})).fill().padRight(4);
		add(createButton("file-open", "introWindow.open", () -> {
			hideInstance();
			app.openLandDialog();
		})).fill().padRight(4);
		add(createButton("file-copy", "introWindow.copy", () -> {
			hideInstance();
			app.openLandDialog(() -> {
				app.getLand().setFile(null);
			});
		})).fill().padRight(4);
		add(createButton("tool-magic-hat", "introWindow.wizard", null)).fill();
		row();
		
		FocusListener focusListener = createListFocusListener(skin);
		
		add(new Label(I18n.tr("introWindow.recent"), skin)).colspan(4).left().row();
		List<File> recentFiles = new List<>(skin, "unfocused");
		recentFiles.setItems(RecentFiles.get(".lnd", ".txt"));
		recentFiles.addListener(focusListener);
		recentFiles.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (recentFiles.getSelected() != null && getTapCount() == 2) {
					app.openFile(recentFiles.getSelected(), true, () -> hideInstance());
				}
			}
		});
		add(recentFiles).colspan(4).minHeight(80).grow().row();
		
		RecoveredFile[] recoveredFiles = RecoveryManager.getRecoveredFiles();
		if (recoveredFiles.length > 0) {
			add(new Label(I18n.tr("introWindow.recovered"), skin)).colspan(4).left().row();
			List<RecoveredFile> recoveredList = new List<>(skin, "unfocused");
			recoveredList.setItems(recoveredFiles);
			recoveredList.addListener(focusListener);
			recoveredList.addListener(new ClickListener(Buttons.LEFT) {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if (recoveredList.getSelected() != null && getTapCount() == 2) {
						RecoveredFile recovered = recoveredList.getSelected();
						try {
							//We use "set" methods in place of "load" ones to have more control
							String ext = getExt(recovered.recovered);
							if ("txt".equals(ext)) {
								LHXFile lhx = LHXFile.load(recovered.recovered);
								lhx.setFile(recovered.original);
								app.setLHX(lhx, true, false, () -> {
									app.setUnsavedLHXChanges(true);
									hideInstance();
								});
							} else if ("lnd".equals(ext)) {
								LndFile land = LndFile.load(recovered.recovered, true);
								land.setFile(recovered.original);
								app.setLand(land, true, () -> {
									app.setUnsavedLandChanges(true);
									hideInstance();
								});
							}
						} catch (Exception e) {
							app.showError(I18n.tr("introWindow.recovery"), e);
						}
						RecoveryManager.removeRecoveredFile(recovered);
					}
				}
			});
			recoveredList.addListener(new ClickListener(Buttons.RIGHT) {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					event.getStage().setKeyboardFocus(recoveredList);
					RecoveredFile recovered = recoveredList.getSelected();
					if (recovered != null) {
						PopupMenu.show1(getStage(), event.getStageX(), event.getStageY(), new MenuItemAction[] {
							new MenuItemAction("remove", I18n.tr("introWindow.recovered.remove"), () -> {
								RecoveryManager.removeRecoveredFile(recovered);
								recoveredList.setItems(RecoveryManager.getRecoveredFiles());
							})
						});
					}
				}
			});
			add(recoveredList).colspan(4).grow().row();
		}
		
		pack();
	}
	
	private FocusListener createListFocusListener(Skin skin) {
		ListStyle focusedStyle = skin.get("default", ListStyle.class);
		ListStyle unfocusedStyle = skin.get("unfocused", ListStyle.class);
		
		return new FocusListener() {
		    @Override
		    public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
		        List<?> list = (List<?>)actor;
		    	if (focused) {
		            list.setStyle(focusedStyle);
		        } else {
		            list.setStyle(unfocusedStyle);
		        }
		    }
		};
	}
	
	private Button createButton(String icon, String txtId, Runnable action) {
		Table content = new Table();
		Image image = new Image(getSkin().getDrawable(icon), Scaling.fit, Align.top);
		content.add(image).height(48).fill().row();
		Label label = new Label(I18n.tr(txtId), getSkin());
		label.setAlignment(Align.center);
		label.setWrap(true);
		content.add(label).minWidth(100).expandY().center();
		Button button = new Button(content, getSkin(), "hover");
		button.padTop(10);
		button.getCell(content).grow();
		if (action != null) {
			button.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					action.run();
				}
			});
		} else {
			button.setDisabled(true);
			image.setColor(Color.GRAY);
			label.setColor(Color.GRAY);
		}
		return button;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	
	private static String getExt(File file) {
		String s = file.getName();
		int p = s.lastIndexOf('.');
		if (p < 0) return "";
		return s.substring(p + 1).toLowerCase();
	}
}
