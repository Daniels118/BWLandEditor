package it.ld.libgdx.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;

public class ImageButton extends Button {
	private static final Color DISABLED_COLOR = new Color(1f, 1f, 1f, 0.3f);
	
	private Image img;
	
	public ImageButton(Skin skin, String icon, Runnable action) {
		this(skin, icon, null, action);
	}
	
	public ImageButton(Skin skin, String icon, String tooltip, Runnable action) {
		super(skin.get("hover", Button.ButtonStyle.class));
		img = new Image(skin.getDrawable(icon));
		img.setScaling(Scaling.fit);
		add(img);
		addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (isDisabled() || action == null) return;
				try {
					action.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		if (tooltip != null && !tooltip.isEmpty()) {
			addListener(new TextTooltip(tooltip, skin));
		}
	}
	
	@Override
	public void setDisabled(boolean isDisabled) {
		super.setDisabled(isDisabled);
		img.setColor(isDisabled() ? DISABLED_COLOR : Color.WHITE);
	}
}
