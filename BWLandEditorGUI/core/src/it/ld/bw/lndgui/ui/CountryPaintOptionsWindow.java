package it.ld.bw.lndgui.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.tools.Brush;
import it.ld.bw.lndgui.tools.CountryBrush;
import it.ld.libgdx.ui.components.MultiSlider;
import it.ld.libgdx.ui.components.Prompt;
import it.ld.libgdx.ui.components.Prompt.PromptListener;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.Spinner;
import it.ld.libgdx.ui.components.TextSlider;
import it.ld.libgdx.ui.components.GridView.State;
import it.ld.libgdx.ui.components.ImageButton;
import it.ld.libgdx.utils.Utils;
import it.ld.utils.UChangeListener;

public class CountryPaintOptionsWindow extends SmartWindow {
	public static void showSingleInstance(MainApp app, Stage stage, Actor anchor, Skin skin) {
		CountryPaintOptionsWindow window = (CountryPaintOptionsWindow) SmartWindow.getSingleInstance("countryPaintOptionsWindow");
		if (window == null) {
			window = new CountryPaintOptionsWindow(app, skin, true);
			window.setSingleInstance("countryPaintOptionsWindow");
			Vector2 pos = new Vector2(anchor.getWidth(), anchor.getHeight());
			anchor.localToStageCoordinates(pos);
			window.show(stage, pos.x, pos.y - window.getHeight(), false);
		} else {
			window.toFront();
		}
	}
	
	public static void hideInstance() {
		CountryPaintOptionsWindow window = (CountryPaintOptionsWindow) SmartWindow.getSingleInstance("countryPaintOptionsWindow");
		if (window != null) {
			window.remove();
		}
	}
	
	private MainApp app;
	private CountryBrush brush;
	
	private SelectBox<CountryBrush> storedBrushes;
	private SelectBox<Brush.Shape> brushShape;
	private TextSlider brushAngle;
	private Button brushRotateWithCamera;
	private TextSlider brushSize;
	private Spinner brushW;
	private Spinner brushH;
	private final Image brushCountryIcon = new Image();
	private CheckBox brushOverCoastline;
	private CheckBox brushNearCoastline;
	private CheckBox brushOverLand;
	private MultiSlider brushSlopeRange;
	private MultiSlider brushHeightRange;
	private SelectBox<SoundOption> brushSound;
	
	private Texture countryTexture;
	
	public CountryPaintOptionsWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("countryPaintOptions.title"), skin);
		this.setAutodispose(autodispose);
		this.defaults().left().pad(2f);
		
		this.app = app;
		
		add(new Label(I18n.tr("brushes"), skin));
		Table brushesField = new Table();
		storedBrushes = new SelectBox<>(skin);
		storedBrushes.setItems(app.getCountryBrushes().toArray(new CountryBrush[0]));
		storedBrushes.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				app.setCountryBrush(storedBrushes.getSelected());
			}
		});
		brushesField.add(storedBrushes).minWidth(100).expandX().fillX();
		Button removeBrushButton = new ImageButton(skin, "tool-delete", I18n.tr("brushes.remove"), () -> {
			if (app.getCountryBrushes().size() > 1) {
				app.getCountryBrushes().remove(brush);
			}
		});
		brushesField.add(removeBrushButton).size(24, 24).padLeft(2);
		Button addBrushButton = new ImageButton(skin, "tool-add", I18n.tr("brushes.add"), () -> {
			addBrush("Brush " + (app.getCountryBrushes().size() + 1));
		});
		brushesField.add(addBrushButton).size(24, 24).padLeft(2);
		add(brushesField).expandX().fillX().row();
		
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
		
		add(new Label(I18n.tr("sculptOptions.country"), skin));
		Table countryField = new Table();
		Button brushCountryButton = new Button(brushCountryIcon, skin, "hover");
		brushCountryButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				CountryPicker tmpPicker = new CountryPicker(skin, app.getLand(), true, false)
				.setStyler(new CountryPicker.Styler() {
					@Override
					public void process(int i, LNDCountry item, State state) {
						state.setDisabled(i >= 16);
					}
				});
        		tmpPicker.setCallback(new EventListener() {
					@Override
					public boolean handle(Event event) {
						LNDCountry country = tmpPicker.getSelected();
						if (country != null) {
							brush.setCountry(country);
						}
						return true;
					}
				})
        		.show(getStage(), true);
			}
		});
		countryField.add(brushCountryButton).size(48, 48);
		Button pickCountryButton = new ImageButton(skin, "tool-pickcolor", I18n.tr("countryPaintOptions.country.pick"), () -> {
			app.getView3D().pickCoord((coord, button) -> {
				LndFile land = app.getLand();
				LH3DLandCell cell = land.getCellAtCoord(coord.x, coord.z);
				if (cell != LH3DLandCell.EMPTY) {
					brush.setCountry(land.getCountries().get(cell.getCountry()));
				}
			});
		});
		countryField.add(pickCountryButton).size(28, 28).padLeft(2).top();
		add(countryField).row();
		
		add(new Label(I18n.tr("countryPaintOptions.sound"), skin));
		brushSound = new SelectBox<>(skin);
		brushSound.setItems(SoundOption.valuesWithNull());
		brushSound.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setSound(brushSound.getSelected().sound);
			}
		});
		add(brushSound).row();
		
		add(new Label(I18n.tr("countryPaintOptions.overCoastline"), skin));
		brushOverCoastline = new CheckBox("", skin);
		brushOverCoastline.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setOverCoastline(brushOverCoastline.isChecked());
			}
		});
		add(brushOverCoastline).row();
		
		add(new Label(I18n.tr("countryPaintOptions.nearCoastline"), skin));
		brushNearCoastline = new CheckBox("", skin);
		brushNearCoastline.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setNearCoastline(brushNearCoastline.isChecked());
			}
		});
		add(brushNearCoastline).row();
		
		add(new Label(I18n.tr("countryPaintOptions.overLand"), skin));
		brushOverLand = new CheckBox("", skin);
		brushOverLand.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setOverLand(brushOverLand.isChecked());
			}
		});
		add(brushOverLand).row();
		
		add(new Label(I18n.tr("countryPaintOptions.slopeRange"), skin));
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
		
		add(new Label(I18n.tr("countryPaintOptions.heightRange"), skin));
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
		setBrush(app.getCountryBrush());
		pack();
	}
	
	private UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.COUNTRY_BRUSHES && (event.getType() == EventType.ADD || event.getType() == EventType.REMOVE)) {
				storedBrushes.setItems(app.getCountryBrushes().toArray(new CountryBrush[0]));
				storedBrushes.setSelected(app.getCountryBrush());
			} else if (event.getProperty() == MainApp.Property.COUNTRY_BRUSH) {
				setBrush((CountryBrush)event.getNewValue());
			}
		}
	};
	
	private void addBrush(String defaultName) {
		Prompt.show(getStage(), getSkin(), I18n.getInstance(), I18n.tr("brushes.add.title"), I18n.tr("brushes.add.message"), defaultName, new PromptListener() {
			public void confirm(String value) {
				CountryBrush newBrush = new CountryBrush();
				newBrush.setName(value);
				try {
					app.getCountryBrushes().add(newBrush);
					app.setCountryBrush(newBrush);
				} catch (IllegalArgumentException e) {
					app.showError(I18n.tr("brushes.add.title"), e, () -> addBrush(value));
				}
			}
		});
	}
	
	private Texture createCountryTexture(LNDCountry country) {
		if (country == null) return null;
		return Utils.toTexture(app.getDefaultCountryPreviewGenerator().generatePreview(country, 48, 48), 48, 48, true);
	}
	
	public void setBrush(CountryBrush brush) {
		if (this.brush != null) {
			this.brush.listeners.remove(brushChangeListener);
			this.brush = null;
		}
		if (brush != null) {
			storedBrushes.setSelected(brush);
			brushShape.setSelected(brush.getShape());
			brushAngle.setValue(brush.getAngleOffset() * MathUtils.radiansToDegrees);
			brushRotateWithCamera.setChecked(brush.isRotateWithCameraEnabled());
			brushSize.setValue(brush.getSize());
			brushW.setValue(brush.getW());
			brushH.setValue(brush.getH());
			
			if (countryTexture != null) countryTexture.dispose();
			countryTexture = createCountryTexture(brush.getCountry());
			brushCountryIcon.setDrawable(countryTexture == null ? null : new TextureRegionDrawable(countryTexture));
			
			brushOverCoastline.setChecked(brush.isOverCoastline());
			brushNearCoastline.setChecked(brush.isNearCoastline());
			brushOverLand.setChecked(brush.isOverLand());
			brushSlopeRange.setValue(0, MathUtils.atanDeg(brush.getMinSlope()));
			brushSlopeRange.setValue(1, MathUtils.atanDeg(brush.getMaxSlope()));
			brushHeightRange.setValue(0, brush.getMinHeight());
			brushHeightRange.setValue(1, brush.getMaxHeight());
			brushSound.setSelected(SoundOption.valueOf(brush.getSound()));
			
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
				} else if (event.getProperty() instanceof CountryBrush.Property) {
					CountryBrush.Property property = (CountryBrush.Property)event.getProperty();
					switch (property) {
						case COUNTRY:
							if (countryTexture != null) countryTexture.dispose();
							countryTexture = createCountryTexture(brush.getCountry());
							brushCountryIcon.setDrawable(countryTexture == null ? null : new TextureRegionDrawable(countryTexture));
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
							brushSound.setSelected(SoundOption.valueOf(brush.getSound()));
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
		if (countryTexture != null) countryTexture.dispose();
		if (brush != null) brush.listeners.remove(brushChangeListener);
		super.dispose();
	}
}
