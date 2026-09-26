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
package it.ld.libgdx.ui.components;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Scaling;

import it.ld.libgdx.utils.I18nSupport;

public class MessageBox extends Dialog {
	public enum Choice {
		OK, CANCEL, YES, NO
	}
	
	public enum Options {
		OK, OK_CANCEL, YES_NO, YES_NO_CANCEL
	}
	
	public enum MessageType {
		INFO("icon-info"),
		WARN("icon-warning"),
		ERROR("icon-error"),
		ASK("icon-help");
		
		public String atlasName;
		
		private MessageType(String atlasName) {
			this.atlasName = atlasName;
		}
	}
	
	public static Skin skin;
	public static I18nSupport i18n = new I18nSupport() {
		@Override
		public String get(String key, Object... args) {
			return key;
		}
		
		@Override
		public String get(String key) {
			return key;
		}
	};
	
	private Image modalShadow;
	
	private final Image iconImage;
	private final Label label;
	private Label detailsLabel;
	private final Callback callback;
	
	public static void init(I18nSupport i18n, Skin skin) {
		MessageBox.i18n = i18n;
		MessageBox.skin = skin;
	}
	
	public MessageBox(String title, String message, String details, Drawable icon, Options buttons, Callback callback) {
		super(title, skin);
		this.callback = callback;
		
		this.modalShadow = new Image(getSkin().newDrawable("white", new Color(0f, 0f, 0f, 0.5f)));
		this.modalShadow.setScaling(Scaling.fill);
		
		this.iconImage = new Image(icon);
		this.getContentTable().add(iconImage).pad(1, 10, 1, 5);
		
		label = new Label(message, skin);
		label.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (button == Buttons.RIGHT) {
					PopupMenu.show1(event.getTarget(), x, y, new MenuItemAction[] {
						new MenuItemAction("copy", i18n.get("dialog.copy"), () -> {
							Gdx.app.getClipboard().setContents(message);
						})
					});
					return true;
				}
				return false;
			}
		});
		this.getContentTable().add(label).growX().pad(1, 0, 1, 10).row();
		if (details != null) {
			this.getContentTable().add().pad(1, 10, 1, 5);	//Filler
			
			TextButton showDetails = new TextButton(i18n.get("dialog.showDetails"), skin);
			showDetails.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					showDetails.remove();
					getContentTable().add().pad(1, 10, 1, 5);	//Filler
					getContentTable().add(detailsLabel).growX().pad(1, 0, 1, 10);
					detailsLabel.setVisible(true);
					arrange();
				}
			});
			this.getContentTable().add(showDetails).left().pad(1, 0, 1, 10).row();
			
			detailsLabel = new Label(details, skin);
			detailsLabel.setVisible(false);
			detailsLabel.addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					if (button == Buttons.RIGHT) {
						PopupMenu.show1(event.getTarget(), x, y, new MenuItemAction[] {
							new MenuItemAction("copy", i18n.get("dialog.copy"), () -> {
								Gdx.app.getClipboard().setContents(details);
							})
						});
						return true;
					}
					return false;
				}
			});
		}
		this.getButtonTable().center().pad(3, 10, 8, 10).defaults().minWidth(60).fill(false).pad(1);
		if (buttons == null) buttons = Options.OK;
		switch (buttons) {
			case OK_CANCEL:
				this.button(i18n.get("dialog.ok"), Choice.OK);
				this.button(i18n.get("dialog.cancel"), Choice.CANCEL);
				this.key(Keys.ENTER, Choice.OK);
				this.key(Keys.NUMPAD_ENTER, Choice.OK);
				this.key(Keys.ESCAPE, Choice.CANCEL);
				break;
			case YES_NO:
				this.button(i18n.get("dialog.yes"), Choice.YES);
				this.button(i18n.get("dialog.no"), Choice.NO);
				this.key(Keys.ENTER, Choice.YES);
				this.key(Keys.NUMPAD_ENTER, Choice.YES);
				this.key(Keys.ESCAPE, Choice.NO);
				break;
			case YES_NO_CANCEL:
				this.button(i18n.get("dialog.yes"), Choice.YES);
				this.button(i18n.get("dialog.no"), Choice.NO);
				this.button(i18n.get("dialog.cancel"), Choice.CANCEL);
				this.key(Keys.ENTER, Choice.YES);
				this.key(Keys.NUMPAD_ENTER, Choice.YES);
				this.key(Keys.ESCAPE, Choice.CANCEL);
				break;
			default:
				this.button(i18n.get("dialog.ok"), Choice.OK);
				this.getButtonTable().right();
				this.key(Keys.ENTER, Choice.OK);
				this.key(Keys.NUMPAD_ENTER, Choice.OK);
				this.key(Keys.ESCAPE, Choice.OK);
		}
		this.key(Keys.ENTER, null);
		this.key(Keys.NUMPAD_ENTER, null);
		this.key(Keys.ESCAPE, null);
	}
	
	private static Drawable getIconSafe(String name) {
		try {
			return skin.getDrawable(name);
		} catch (GdxRuntimeException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void show(Stage stage, String title, Throwable exception) {
		show(stage, title, exception, null);
	}
	
	public static void show(Stage stage, String title, Throwable exception, Callback callback) {
		String msg = exception instanceof NumberFormatException ? i18n.get("error.invalidValue") : exception.getLocalizedMessage();
		if (msg == null || msg.isEmpty()) msg = exception.getClass().getSimpleName();
		StringWriter buffer = new StringWriter();
		exception.printStackTrace(new PrintWriter(buffer));
		String details = buffer.toString().replaceAll("\t", "    ");
		MessageBox prompt = new MessageBox(title, msg, details, getIconSafe(MessageType.ERROR.atlasName), null, callback);
		prompt.show(stage);
	}
	
	public static void show(Stage stage, String title, String message) {
		MessageBox prompt = new MessageBox(title, message, null, getIconSafe(MessageType.INFO.atlasName), null, null);
		prompt.show(stage);
	}
	
	public static void show(Stage stage, String title, String message, MessageType type) {
		MessageBox prompt = new MessageBox(title, message, null, getIconSafe(type.atlasName), null, null);
		prompt.show(stage);
	}
	
	public static void show(Stage stage, String title, String message, String icon) {
		MessageBox prompt = new MessageBox(title, message, null, getIconSafe(icon), null, null);
		prompt.show(stage);
	}
	
	public static void show(Stage stage, String title, String message, MessageType type, Options options, Callback callback) {
		MessageBox prompt = new MessageBox(title, message, null, getIconSafe(type.atlasName), options, callback);
		prompt.show(stage);
	}
	
	public static void show(Stage stage, String title, String message, String icon, Options options, Callback callback) {
		MessageBox prompt = new MessageBox(title, message, null, getIconSafe(icon), options, callback);
		prompt.show(stage);
	}
	
	public static void show(Stage stage, String title, String message, Drawable icon, Options options, Callback callback) {
		MessageBox prompt = new MessageBox(title, message, null, icon, options, callback);
		prompt.show(stage);
	}
	
	@Override
	protected void result(Object object) {
		if (callback != null) {
			callback.result((Choice)object);
		}
	}
	
	@Override
	public Dialog show(Stage stage) {
		modalShadow.setBounds(0, 0, stage.getWidth(), stage.getHeight());
		stage.addActor(modalShadow);
		show(stage, null);
		arrange();
		return this;
	}
	
	@Override
	public float getPrefWidth() {
		Stage stage = getStage();
		float maxWidth = stage != null ? stage.getWidth() * 0.8f : Float.POSITIVE_INFINITY;
		float textWidth;
		label.setWrap(false);
		if (detailsLabel != null && detailsLabel.isVisible()) {
			detailsLabel.setWrap(false);
			textWidth = Math.max(label.getPrefWidth(), detailsLabel.getPrefWidth());
			detailsLabel.setWrap(true);
		} else {
			textWidth = label.getPrefWidth();
		}
		label.setWrap(true);
		return Math.min(iconImage.getWidth() + textWidth + 45f, maxWidth);
	}
	
	private void arrange() {
		Stage stage = getStage();
		layout();
		pack();
		setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
	}
	
	@Override
	public void hide() {
		hide(null);
	}
	
	@Override
	public boolean remove() {
		modalShadow.remove();
		return super.remove();
	}
	
	
	public interface Callback {
		public void result(Choice choice);
	}
}
