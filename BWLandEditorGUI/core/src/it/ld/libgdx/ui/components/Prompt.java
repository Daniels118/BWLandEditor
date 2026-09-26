package it.ld.libgdx.ui.components;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Scaling;

import it.ld.libgdx.utils.I18nSupport;

public class Prompt extends Dialog {
	private final PromptListener callback;
	
	private Image modalShadow;
	
	private TextField field;
	
	public Prompt(Skin skin, I18nSupport i18n, String title, String message, String defval, PromptListener callback) {
		super(title, skin);
		this.modalShadow = new Image(getSkin().newDrawable("white", new Color(0f, 0f, 0f, 0.5f)));
		this.modalShadow.setScaling(Scaling.fill);
		this.callback = callback;
		this.getContentTable().add(new Label(message, skin)).left().pad(1, 10, 1, 10);
		this.getContentTable().row();
		this.field = new TextField(defval, skin);
		this.getContentTable().add(field).growX().pad(1, 10, 1, 10);
		this.getButtonTable().right().pad(3, 10, 8, 10).defaults().minWidth(60).fill(false).right();
		this.button(i18n.get("dialog.cancel"), false);
		this.button(i18n.get("dialog.ok"), true);
		this.key(Keys.ENTER, true);
		this.key(Keys.NUMPAD_ENTER, true);
		this.key(Keys.ESCAPE, false);
	}
	
	public static void show(Stage stage, Skin skin, I18nSupport i18n, String title, String message, String defval, PromptListener callback) {
		Prompt prompt = new Prompt(skin, i18n, title, message, defval, callback);
		prompt.show(stage);
		prompt.field.selectAll();
		stage.setKeyboardFocus(prompt.field);
	}
	
	@Override
	protected void result(Object object) {
		if ((boolean)object) {
			callback.confirm(field.getText());
		} else {
			callback.cancel();
		}
	}
	
	@Override
	public float getPrefWidth() {
		return Math.max(super.getPrefWidth(), getStage().getWidth() / 3);
	}
	
	@Override
	public Dialog show(Stage stage) {
		modalShadow.setBounds(0, 0, stage.getWidth(), stage.getHeight());
		stage.addActor(modalShadow);
		show(stage, null);
		setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
		return this;
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
	
	public static abstract class PromptListener extends Event {
		public void confirm(String value) {}
		public void cancel() {}
	}
}
