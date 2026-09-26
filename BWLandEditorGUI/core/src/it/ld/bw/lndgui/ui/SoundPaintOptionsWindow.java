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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LH3DLandCell.Sound;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.tools.Brush;
import it.ld.bw.lndgui.tools.SoundBrush;
import it.ld.libgdx.ui.components.GridView;
import it.ld.libgdx.ui.components.GridView.GridModel;
import it.ld.libgdx.ui.components.GridView.ItemClickEvent;
import it.ld.libgdx.ui.components.GridView.State;
import it.ld.libgdx.ui.components.MultiSlider;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.Spinner;
import it.ld.libgdx.ui.components.TextSlider;
import it.ld.utils.UChangeListener;

public class SoundPaintOptionsWindow extends SmartWindow {
	public static void showSingleInstance(MainApp app, Stage stage, Actor anchor, Skin skin) {
		SoundPaintOptionsWindow window = (SoundPaintOptionsWindow) SmartWindow.getSingleInstance("soundPaintOptionsWindow");
		if (window == null) {
			window = new SoundPaintOptionsWindow(app, skin, true);
			window.setSingleInstance("soundPaintOptionsWindow");
			Vector2 pos = new Vector2(anchor.getWidth(), anchor.getHeight());
			anchor.localToStageCoordinates(pos);
			window.show(stage, pos.x, pos.y - window.getHeight(), false);
		} else {
			window.toFront();
		}
	}
	
	public static void hideInstance() {
		SoundPaintOptionsWindow window = (SoundPaintOptionsWindow) SmartWindow.getSingleInstance("soundPaintOptionsWindow");
		if (window != null) {
			window.remove();
		}
	}
	
	private final MainApp app;
	private SoundBrush brush;
	
	private SelectBox<Brush.Shape> brushShape;
	private TextSlider brushAngle;
	private Button brushRotateWithCamera;
	private TextSlider brushSize;
	private Spinner brushW;
	private Spinner brushH;
	private GridView<Sound> brushSound;
	private CheckBox brushOverWater;
	private CheckBox brushOverCoastline;
	private CheckBox brushNearCoastline;
	private CheckBox brushOverLand;
	private MultiSlider brushSlopeRange;
	private MultiSlider brushHeightRange;
	
	public SoundPaintOptionsWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("soundPaintOptions.title"), skin);
		this.setAutodispose(autodispose);
		this.defaults().left().pad(2f);
		
		this.app = app;
		
		add(new Label(I18n.tr("brush.shape"), skin));
		brushShape = new SelectBox<>(skin);
		brushShape.setItems(Brush.Shape.values());
		brushShape.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setShape(brushShape.getSelected());
			}
		});
		add(brushShape).row();
		
		add(new Label(I18n.tr("brush.angle"), skin));
		Table angleField = new Table();
		brushRotateWithCamera = new Button(new Image(skin.getDrawable("cinema"), Scaling.fit), skin, "toggle");
		brushRotateWithCamera.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setRotateWithCamera(brushRotateWithCamera.isChecked());
			}
		});
		brushRotateWithCamera.addListener(new TextTooltip(I18n.tr("brush.rotateWithCamera"), skin));
		angleField.add(brushRotateWithCamera).size(24).padRight(2);
		brushAngle = new TextSlider(skin, 0, -180, 180, 1);
		brushAngle.setValueChangeListener((value) -> {
			if (brush == null) return;
			brush.setAngleOffset(value * MathUtils.degreesToRadians);
		});
		angleField.add(brushAngle);
		add(angleField).fillX().row();
		
		add(new Label(I18n.tr("brush.size"), skin));
		brushSize = new TextSlider(skin, 0, 10, 500, 10);
		brushSize.setValueChangeListener((value) -> {
			if (brush == null) return;
			brush.setSize(value);
		});
		add(brushSize).right().row();
		
		Table propField = new Table();
		
		propField.add(new Label(I18n.tr("brush.w"), skin)).padRight(2);
		brushW = new Spinner(skin, 1, 0.01f, 2000, 0.1f);
		brushW.setValueChangeListener((value) -> {
			if (brush == null) return;
			brush.setW(value);
		});
		propField.add(brushW).padRight(8);
		
		propField.add(new Label(I18n.tr("brush.h"), skin)).padRight(2);
		brushH = new Spinner(skin, 1, 0.01f, 2000, 0.1f);
		brushH.setValueChangeListener((value) -> {
			if (brush == null) return;
			brush.setH(value);
		});
		propField.add(brushH);
		
		add(propField).colspan(2).right().row();
		
		Table soundField = new Table();
		soundField.add(new Label(I18n.tr("soundPaintOptions.sound"), skin)).padRight(6);
		final Array<Sound> sounds = new Array<Sound>(Sound.values());
		sounds.removeValue(Sound.NONE, true);
		brushSound = new GridView<>(skin);
		brushSound.setMinCols(6);
		brushSound.setForceScroll(false);
		brushSound.setUnwrapSelectedEnabled(false);
		brushSound.setPadding(2);
		brushSound.setIconSize(48);
		brushSound.setItems(sounds);
		brushSound.setModel(new GridModel<LH3DLandCell.Sound>() {
			private final Drawable[] images;
			private final String[] labels;
			
			{
				images = new Drawable[sounds.size];
				labels = new String[sounds.size];
				for (int i = 0; i < sounds.size; i++) {
					Sound sound = sounds.get(i);
					images[i] = skin.getDrawable("sound-" + sound.name().toLowerCase());
					labels[i] = I18n.tr("cell.sounds." + sound.name());
				}
			}
			
			@Override
			public void render(int index, Sound item, Container<Actor> container, Image image, Label label, State state) {
				image.setDrawable(images[index]);
				label.setText(labels[index]);
				container.addListener(new TextTooltip(labels[index], skin));
			}
		});
		brushSound.addListener(new GridView.ItemClickListener<Sound>() {
			@Override
			public void clicked(ItemClickEvent<Sound> event, int index, Sound item) {
				brush.setSound(item);
			}
		});
		soundField.add(brushSound).expandX().fillX().height(160);
		add(soundField).colspan(2).fillX().row();
		
		add(new Label(I18n.tr("soundPaintOptions.overWater"), skin));
		brushOverWater = new CheckBox("", skin);
		brushOverWater.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setOverWater(brushOverWater.isChecked());
			}
		});
		add(brushOverWater).row();
		
		add(new Label(I18n.tr("soundPaintOptions.overCoastline"), skin));
		brushOverCoastline = new CheckBox("", skin);
		brushOverCoastline.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setOverCoastline(brushOverCoastline.isChecked());
			}
		});
		add(brushOverCoastline).row();
		
		add(new Label(I18n.tr("soundPaintOptions.nearCoastline"), skin));
		brushNearCoastline = new CheckBox("", skin);
		brushNearCoastline.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setNearCoastline(brushNearCoastline.isChecked());
			}
		});
		add(brushNearCoastline).row();
		
		add(new Label(I18n.tr("soundPaintOptions.overLand"), skin));
		brushOverLand = new CheckBox("", skin);
		brushOverLand.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setOverLand(brushOverLand.isChecked());
			}
		});
		add(brushOverLand).row();
		
		add(new Label(I18n.tr("soundPaintOptions.slopeRange"), skin));
		brushSlopeRange = new MultiSlider(0f, 89f, 1f, 1f, false, 2, skin);
		brushSlopeRange.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				float[] values = brushSlopeRange.getValues();
				brush.setMinSlope(MathUtils.tanDeg(values[0]));
				brush.setMaxSlope(MathUtils.tanDeg(values[1]));
			}
		});
		add(brushSlopeRange).fillX().row();
		
		add(new Label(I18n.tr("soundPaintOptions.heightRange"), skin));
		brushHeightRange = new MultiSlider(LH3DLandCell.DEEP_WATER_HEIGHT, LH3DLandCell.getMaxHeight(8), LH3DLandCell.HEIGHT_UNIT, LH3DLandCell.HEIGHT_UNIT, false, 2, skin);
		brushHeightRange.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				float[] values = brushHeightRange.getValues();
				brush.setMinHeight(values[0]);
				brush.setMaxHeight(values[1]);
			}
		});
		add(brushHeightRange).fillX().row();
		
		app.listeners.add(appChangeListener);
		setBrush(app.getSoundBrush());
		pack();
	}
	
	private UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.SOUND_BRUSH) {
				setBrush(app.getSoundBrush());
			}
		}
	};
	
	private void setBrush(SoundBrush brush) {
		if (this.brush != null) {
			this.brush.listeners.remove(brushChangeListener);
			this.brush = null;
		}
		if (brush != null) {
			brushShape.setSelected(brush.getShape());
			brushAngle.setValue(brush.getAngleOffset() * MathUtils.radiansToDegrees);
			brushRotateWithCamera.setChecked(brush.isRotateWithCameraEnabled());
			brushSize.setValue(brush.getSize());
			brushW.setValue(brush.getW());
			brushH.setValue(brush.getH());
			
			brushOverWater.setChecked(brush.isOverWater());
			brushOverCoastline.setChecked(brush.isOverCoastline());
			brushNearCoastline.setChecked(brush.isNearCoastline());
			brushOverLand.setChecked(brush.isOverLand());
			brushSlopeRange.setValue(0, MathUtils.atanDeg(brush.getMinSlope()));
			brushSlopeRange.setValue(1, MathUtils.atanDeg(brush.getMaxSlope()));
			brushHeightRange.setValue(0, brush.getMinHeight());
			brushHeightRange.setValue(1, brush.getMaxHeight());
			brushSound.setSelected(brush.getSound());
			
			brush.listeners.add(brushChangeListener);
		}
		this.brush = brush;
	}
	
	private UChangeListener brushChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.CHANGE) {
				if (event.getProperty() instanceof Brush.Property) {
					Brush.Property property = (Brush.Property)event.getProperty();
					switch (property) {
						case SHAPE:
							brushShape.setSelected(brush.getShape());
							break;
						case ANGLE_OFFSET:
							brushAngle.setValue(brush.getAngleOffset() * MathUtils.radiansToDegrees);
							break;
						case SIZE:
							brushSize.setValue(brush.getSize());
							break;
						case W:
							brushW.setValue(brush.getW());
							break;
						case H:
							brushH.setValue(brush.getH());
							break;
						case ROTATE_WITH_CAMERA:
							brushRotateWithCamera.setChecked(brush.isRotateWithCameraEnabled());
							break;
						default:
					}
				} else if (event.getProperty() instanceof SoundBrush.Property) {
					SoundBrush.Property property = (SoundBrush.Property)event.getProperty();
					switch (property) {
						case OVER_WATER:
							brushOverWater.setChecked(brush.isOverWater());
							break;
						case OVER_COASTLINE:
							brushOverCoastline.setChecked(brush.isOverCoastline());
							break;
						case NEAR_COASTLINE:
							brushNearCoastline.setChecked(brush.isNearCoastline());
							break;
						case OVER_LAND:
							brushOverLand.setChecked(brush.isOverLand());
							break;
						case MIN_SLOPE:
							brushSlopeRange.setValue(0, MathUtils.atanDeg(brush.getMinSlope()));
							break;
						case MAX_SLOPE:
							brushSlopeRange.setValue(1, MathUtils.atanDeg(brush.getMaxSlope()));
							break;
						case MIN_HEIGHT:
							brushHeightRange.setValue(0, brush.getMinHeight());
							break;
						case MAX_HEIGHT:
							brushHeightRange.setValue(1, brush.getMaxHeight());
							break;
						case SOUND:
							brushSound.setSelected(brush.getSound());
							break;
						default:
					}
				}
			}
		}
	};
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		app.listeners.remove(appChangeListener);
		if (brush != null) brush.listeners.remove(brushChangeListener);
		super.dispose();
	}
}
