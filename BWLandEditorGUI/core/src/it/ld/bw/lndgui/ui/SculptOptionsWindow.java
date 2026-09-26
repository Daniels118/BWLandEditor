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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.Vec3f;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.tools.Brush;
import it.ld.bw.lndgui.tools.SculptBrush;
import it.ld.bw.lndgui.tools.SculptBrush.Mode;
import it.ld.libgdx.ui.components.Prompt;
import it.ld.libgdx.ui.components.Prompt.PromptListener;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.Spinner;
import it.ld.libgdx.ui.components.TextSlider;
import it.ld.libgdx.ui.components.GridView.State;
import it.ld.libgdx.ui.components.ImageButton;
import it.ld.libgdx.utils.Utils;
import it.ld.utils.UChangeListener;

public class SculptOptionsWindow extends SmartWindow {
	public static void showSingleInstance(MainApp app, Stage stage, Actor anchor, Skin skin) {
		SculptOptionsWindow window = (SculptOptionsWindow) SmartWindow.getSingleInstance("sculptOptionsWindow");
		if (window == null) {
			window = new SculptOptionsWindow(app, skin, true);
			window.setSingleInstance("sculptOptionsWindow");
		}
		Vector2 pos = new Vector2(anchor.getWidth(), anchor.getHeight());
		anchor.localToStageCoordinates(pos);
		window.show(stage, pos.x, pos.y - window.getHeight(), false);
		window.toFront();
	}
	
	public static void hideInstance() {
		SculptOptionsWindow window = (SculptOptionsWindow) SmartWindow.getSingleInstance("sculptOptionsWindow");
		if (window != null) {
			window.remove();
		}
	}
	
	private MainApp app;
	private SculptBrush brush;
	
	private SelectBox<SculptBrush> storedBrushes;
	private SelectBox<Brush.Shape> brushShape;
	private TextSlider brushAngle;
	private Button brushRotateWithCamera;
	private TextSlider brushSize;
	private Spinner brushW;
	private Spinner brushH;
	private TextSlider brushElevation;
	private Slider brushMaxDepth;
	private TextSlider brushFlow;
	private TextSlider brushSmoothness;
	private final Image brushCountryIcon = new Image();
	private final Image brushCliffCountryIcon = new Image();
	private TextSlider brushCliffSlope;
	private TextSlider brushMaxSlope;
	private final Image brushCoastlineCountryIcon = new Image();
	private SelectBox<SoundOption> brushSound;
	
	private Texture countryTexture;
	private Texture cliffCountryTexture;
	private Texture coastlineCountryTexture;
	
	public SculptOptionsWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("sculptOptions.title"), skin);
		this.setAutodispose(autodispose);
		this.defaults().left().pad(2f);
		
		this.app = app;
		
		add(new Label(I18n.tr("brushes"), skin));
		Table brushesField = new Table();
		storedBrushes = new SelectBox<>(skin);
		storedBrushes.setItems(app.getSculptBrushes().toArray(new SculptBrush[0]));
		storedBrushes.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				app.setSculptBrush(storedBrushes.getSelected());
			}
		});
		brushesField.add(storedBrushes).minWidth(100).expandX().fillX();
		Button removeBrushButton = new ImageButton(skin, "tool-delete", I18n.tr("brushes.remove"), () -> {
			if (app.getSculptBrushes().size() > 1) {
				app.getSculptBrushes().remove(brush);
			}
		});
		brushesField.add(removeBrushButton).size(24, 24).padLeft(2);
		Button addBrushButton = new ImageButton(skin, "tool-add", I18n.tr("brushes.add"), () -> {
			addBrush("Brush " + (app.getSculptBrushes().size() + 1));
		});
		brushesField.add(addBrushButton).size(24, 24).padLeft(2);
		add(brushesField).expandX().fillX().row();
		
		add(new Label(I18n.tr("brush.shape"), skin));
		brushShape = new SelectBox<>(skin);
		brushShape.setItems(SculptBrush.Shape.values());
		brushShape.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setShape(brushShape.getSelected());
			}
		});
		add(brushShape).fillX().row();
		
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
		add(angleField).right().row();
		
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
		
		Table modesField = new Table();
		modesField.add(new Label(I18n.tr("sculptOptions.mode"), skin));
		modesField.add().growX();
		for (Mode mode : Mode.values()) {
			Button button = new Button(new Image(skin, "sculpt-"+mode.name().toLowerCase()), skin, "toggle") {
				public boolean isChecked() {
					return brush != null && brush.getMode() == mode;
				};
			};
			button.addListener(new ClickListener() {
				public void clicked(InputEvent event, float x, float y) {
					if (brush == null) return;
					brush.setMode(mode);
				}
			});
			button.addListener(new TextTooltip(I18n.tr("sculptOptions.mode."+mode.name()), skin));
			modesField.add(button);
		}
		add(modesField).colspan(2).fillX().row();
		
		add(new Label(I18n.tr("sculptOptions.elevation"), skin));
		Table elevationField = new Table();
		Button pickElevation = new ImageButton(skin, "tool-pickcolor", I18n.tr("sculptOptions.elevation.pick"), () -> {
			app.getView3D().pickCoord((coord, button) -> {
				LndFile land = app.getLand();
				LH3DLandCell cell = land.getCellAtCoord(coord.x, coord.z);
				if (cell != LH3DLandCell.EMPTY) {
					brush.setElevation(cell.getHeight());
				}
			});
		});
		elevationField.add(pickElevation).size(24).padRight(2);
		brushElevation = new TextSlider(skin, 0, LH3DLandCell.DEEP_WATER_HEIGHT, LH3DLandCell.getMaxHeight(8), LH3DLandCell.HEIGHT_UNIT);
		brushElevation.setValueChangeListener((value) -> {
			if (brush == null) return;
			brush.setElevation(value);
		});
		elevationField.add(brushElevation);
		add(elevationField).right().row();
		
		add(new Label(I18n.tr("sculptOptions.maxDepth"), skin)).top();
		Table depthField = new Table();
		brushMaxDepth = new Slider(LH3DLandCell.DEEP_WATER_ALTITUDE, LH3DLandCell.DRY_ALTITUDE, 1, false, skin);
		brushMaxDepth.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setMaxDepth((int)brushMaxDepth.getValue());
			};
		});
		depthField.add(brushMaxDepth).colspan(3).fillX().row();
		depthField.add(new Label(I18n.tr("sculptOptions.maxDepth.deep"), skin));
		depthField.add(new Label(I18n.tr("sculptOptions.maxDepth.shallow"), skin)).expandX().center();
		depthField.add(new Label(I18n.tr("sculptOptions.maxDepth.dry"), skin));
		add(depthField).fillX().row();
		
		add(new Label(I18n.tr("brush.flow"), skin));
		brushFlow = new TextSlider(skin, 0, 0, 1000, 1);
		brushFlow.setValueChangeListener((value) -> {
			if (brush == null) return;
			brush.setFlow(value);}
		);
		add(brushFlow).right().row();
		
		add(new Label(I18n.tr("brush.smoothness"), skin));
		brushSmoothness = new TextSlider(skin, 0, 0, 100, 1);
		brushSmoothness.setValueChangeListener((value) -> {
			if (brush == null) return;;
			brush.setSmoothness(value / 100f);
		});
		add(brushSmoothness).right().row();
		
		Table countriesField = new Table();
		countriesField.add(new Label(I18n.tr("sculptOptions.country"), skin)).padRight(6);
		countriesField.add().expandX();
		
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
		brushCountryButton.addListener(new TextTooltip(I18n.tr("sculptOptions.country.normal"), skin));
		countriesField.add(brushCountryButton).size(49, 49).padRight(1);
		Button pickCountryButton = new ImageButton(skin, "tool-pickcolor", I18n.tr("sculptOptions.country.pick"), () -> {
			app.getView3D().pickCoord((coord, button) -> {
				LndFile land = app.getLand();
				LH3DLandCell cell = land.getCellAtCoord(coord.x, coord.z);
				if (cell != LH3DLandCell.EMPTY) {
					brush.setCountry(land.getCountries().get(cell.getCountry()));
				}
			});
		});
		Table buttons = new Table();
		buttons.add(pickCountryButton).size(24, 24).padBottom(1).row();
		Button resetCountryButton = new ImageButton(skin, "tool-delete", I18n.tr("sculptOptions.country.unset"), () -> brush.setCountry(null));
		buttons.add(resetCountryButton).size(24, 24);
		countriesField.add(buttons).padRight(4);
		
		Button brushCliffCountryButton = new Button(brushCliffCountryIcon, skin, "hover");
		brushCliffCountryButton.addListener(new ClickListener() {
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
							brush.setCliffCountry(country);
						}
						return true;
					}
				})
        		.show(getStage(), true);
			}
		});
		brushCliffCountryButton.addListener(new TextTooltip(I18n.tr("sculptOptions.country.cliff"), skin));
		countriesField.add(brushCliffCountryButton).size(49, 49).padRight(1);
		Button pickCliffCountryButton = new ImageButton(skin, "tool-pickcolor", I18n.tr("sculptOptions.country.pick"), () -> {
			app.getView3D().pickCoord((coord, button) -> {
				LndFile land = app.getLand();
				LH3DLandCell cell = land.getCellAtCoord(coord.x, coord.z);
				if (cell != LH3DLandCell.EMPTY) {
					brush.setCliffCountry(land.getCountries().get(cell.getCountry()));
				}
			});
		});
		buttons = new Table();
		buttons.add(pickCliffCountryButton).size(24, 24).padBottom(1).row();
		Button resetCliffCountryButton = new ImageButton(skin, "tool-delete", I18n.tr("sculptOptions.country.unset"), () -> brush.setCliffCountry(null));
		buttons.add(resetCliffCountryButton).size(24, 24);
		countriesField.add(buttons).padRight(4);
		
		Button brushCoastlineCountryButton = new Button(brushCoastlineCountryIcon, skin, "hover");
		brushCoastlineCountryButton.addListener(new ClickListener() {
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
							brush.setCoastlineCountry(country);
						}
						return true;
					}
				})
        		.show(getStage(), true);
			}
		});
		brushCoastlineCountryButton.addListener(new TextTooltip(I18n.tr("sculptOptions.country.coastline"), skin));
		countriesField.add(brushCoastlineCountryButton).size(49, 49).padRight(1);
		Button pickCoastlineCountryButton = new ImageButton(skin, "tool-pickcolor", I18n.tr("sculptOptions.country.pick"), () -> {
			app.getView3D().pickCoord((coord, button) -> {
				LndFile land = app.getLand();
				LH3DLandCell cell = land.getCellAtCoord(coord.x, coord.z);
				if (cell != LH3DLandCell.EMPTY) {
					brush.setCoastlineCountry(land.getCountries().get(cell.getCountry()));
				}
			});
		});
		buttons = new Table();
		buttons.add(pickCoastlineCountryButton).size(24, 24).padBottom(1).row();
		Button resetCoastlineCountryButton = new ImageButton(skin, "tool-delete", I18n.tr("sculptOptions.country.unset"), () -> brush.setCoastlineCountry(null));
		buttons.add(resetCoastlineCountryButton).size(24, 24);
		countriesField.add(buttons).padRight(4);
		
		Button countryWizardButton = new ImageButton(skin, "tool-wizard", I18n.tr("sculptOptions.oneClickConfig"), () -> {
			startOneClickConfig();
		});
		countriesField.add(countryWizardButton).size(32, 32).top().row();
		/*countriesField.add().colspan(2);
		countriesField.add(new Label(I18n.tr("sculptOptions.country.normal"), skin)).colspan(2).center();
		countriesField.add(new Label(I18n.tr("sculptOptions.country.cliff"), skin)).colspan(2).center();
		countriesField.add(new Label(I18n.tr("sculptOptions.country.coastline"), skin)).colspan(2).center();
		countriesField.add();*/
		add(countriesField).colspan(2).fillX().row();
		
		add(new Label(I18n.tr("sculptOptions.cliffSlope"), skin));
		brushCliffSlope = new TextSlider(skin, 0, 0, 89, 1);
		brushCliffSlope.setValueChangeListener((value) -> {
			if (brush == null) return;
			brush.setCliffSlope(MathUtils.tanDeg(value));
		});
		add(brushCliffSlope).right().row();
		
		add(new Label(I18n.tr("sculptOptions.maxSlope"), skin));
		brushMaxSlope = new TextSlider(skin, 0, 0, 89, 1);
		brushMaxSlope.setValueChangeListener((value) -> {
			if (brush == null) return;
			brush.setMaxSlope(MathUtils.tanDeg(value));
		});
		add(brushMaxSlope).right().row();
		
		add(new Label(I18n.tr("sculptOptions.sound"), skin));
		brushSound = new SelectBox<>(skin);
		brushSound.setItems(SoundOption.valuesWithNull());
		brushSound.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setSound(brushSound.getSelected().sound);
			}
		});
		add(brushSound).fillX().row();
		
		app.listeners.add(appChangeListener);
		setBrush(app.getSculptBrush());
		pack();
	}
	
	private UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.SCULPT_BRUSHES && (event.getType() == EventType.ADD || event.getType() == EventType.REMOVE)) {
				storedBrushes.setItems(app.getSculptBrushes().toArray(new SculptBrush[0]));
				storedBrushes.setSelected(app.getSculptBrush());
			} else if (event.getProperty() == MainApp.Property.SCULPT_BRUSH) {
				setBrush((SculptBrush)event.getNewValue());
			}
		}
	};
	
	private void addBrush(String defaultName) {
		Prompt.show(getStage(), getSkin(), I18n.getInstance(), I18n.tr("brushes.add.title"), I18n.tr("brushes.add.message"), defaultName, new PromptListener() {
			public void confirm(String value) {
				SculptBrush newBrush = new SculptBrush();
				newBrush.setName(value);
				try {
					app.getSculptBrushes().add(newBrush);
					app.setSculptBrush(newBrush);
				} catch (IllegalArgumentException e) {
					app.showError(I18n.tr("brushes.add.title"), e, () -> addBrush(value));
				}
			}
		});
	}
	
	public void startOneClickConfig() {
		app.showStatusMessage(I18n.tr("sculptOptions.oneClickConfig.hint"));
		app.getView3D().pickCoord((coord, button) -> {
			app.showStatusMessage("");
			LndFile land = app.getLand();
			LH3DLandCell cell = land.getCellAtCoord(coord.x, coord.z);
			if (cell != LH3DLandCell.EMPTY && !cell.isCoastLine()) {
				Vec3f normal = land.getNormal(coord.x, coord.z);
				if (normal.x != 0 || normal.z != 0) {
					final int slopeCountry = cell.getCountry();
					brush.setCliffCountry(land.getCountries().get(slopeCountry));
					Vec3f pos = new Vec3f(coord.x, 0, coord.z);
					Vec3f step = new Vec3f(normal.x, 0, normal.z).nor().scl(LH3DLandCell.CELL_SIZE);
					while (true) {
						pos.sub(step);
						cell = land.getCellAtCoord(pos.x, pos.z);
						if (cell == LH3DLandCell.EMPTY || cell.isCoastLine()) {
							break;
						} else if (cell.getCountry() != slopeCountry) {
							brush.setCountry(land.getCountries().get(cell.getCountry()));
							break;
						}
					}
					pos = new Vec3f(coord.x, 0, coord.z);
					while (true) {
						pos.add(step);
						cell = land.getCellAtCoord(pos.x, pos.z);
						if (cell == LH3DLandCell.EMPTY) {
							break;
						} else if (cell.isCoastLine()) {
							brush.setCoastlineCountry(land.getCountries().get(cell.getCountry()));
							break;
						}
					}
					return;
				}
			}
			startOneClickConfig();
		}, () -> {
			app.showStatusMessage("");
		});
	}
	
	private Texture createCountryTexture(LNDCountry country) {
		if (country == null) return null;
		return Utils.toTexture(app.getDefaultCountryPreviewGenerator().generatePreview(country, 49, 49), 49, 49, true);
	}
	
	public void setBrush(SculptBrush brush) {
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
			brushElevation.setValue(brush.getElevation());
			brushMaxDepth.setValue(brush.getMaxDepth());
			brushFlow.setValue(brush.getFlow());
			brushSmoothness.setValue(brush.getSmoothness() * 100f);
			
			if (countryTexture != null) countryTexture.dispose();
			countryTexture = createCountryTexture(brush.getCountry());
			brushCountryIcon.setDrawable(countryTexture == null ? null : new TextureRegionDrawable(countryTexture));
			
			if (cliffCountryTexture != null) cliffCountryTexture.dispose();
			cliffCountryTexture = createCountryTexture(brush.getCliffCountry());
			brushCliffCountryIcon.setDrawable(cliffCountryTexture == null ? null : new TextureRegionDrawable(cliffCountryTexture));
			
			brushCliffSlope.setValue(MathUtils.atanDeg(brush.getCliffSlope()));
			brushMaxSlope.setValue(MathUtils.atanDeg(brush.getMaxSlope()));
			
			if (coastlineCountryTexture != null) coastlineCountryTexture.dispose();
			coastlineCountryTexture = createCountryTexture(brush.getCoastlineCountry());
			brushCoastlineCountryIcon.setDrawable(coastlineCountryTexture == null ? null : new TextureRegionDrawable(coastlineCountryTexture));
			
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
						case SMOOTHNESS:
							brushSmoothness.setValue(brush.getSmoothness() * 100f);
							break;
						case FLOW:
							brushFlow.setValue(brush.getFlow());
							break;
						case ROTATE_WITH_CAMERA:
							brushRotateWithCamera.setChecked(brush.isRotateWithCameraEnabled());
							break;
						default:
					}
				} else if (event.getProperty() instanceof SculptBrush.Property) {
					SculptBrush.Property property = (SculptBrush.Property)event.getProperty();
					switch (property) {
						case ELEVATION:
							brushElevation.setValue(brush.getElevation());
							break;
						case MAX_DEPTH:
							brushMaxDepth.setValue(brush.getMaxDepth());
							break;
						case COUNTRY:
							if (countryTexture != null) countryTexture.dispose();
							countryTexture = createCountryTexture(brush.getCountry());
							brushCountryIcon.setDrawable(countryTexture == null ? null : new TextureRegionDrawable(countryTexture));
							break;
						case CLIFF_COUNTRY:
							if (cliffCountryTexture != null) cliffCountryTexture.dispose();
							cliffCountryTexture = createCountryTexture(brush.getCliffCountry());
							brushCliffCountryIcon.setDrawable(cliffCountryTexture == null ? null : new TextureRegionDrawable(cliffCountryTexture));
							break;
						case CLIFF_SLOPE:
							brushCliffSlope.setValue(MathUtils.atanDeg(brush.getCliffSlope()));
							break;
						case MAX_SLOPE:
							brushMaxSlope.setValue(MathUtils.atanDeg(brush.getMaxSlope()));
							break;
						case COASTLINE_COUNTRY:
							if (coastlineCountryTexture != null) coastlineCountryTexture.dispose();
							coastlineCountryTexture = createCountryTexture(brush.getCoastlineCountry());
							brushCoastlineCountryIcon.setDrawable(coastlineCountryTexture == null ? null : new TextureRegionDrawable(coastlineCountryTexture));
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
		if (countryTexture != null) countryTexture.dispose();;
		if (cliffCountryTexture != null) cliffCountryTexture.dispose();
		if (coastlineCountryTexture != null) coastlineCountryTexture.dispose();
		if (brush != null) brush.listeners.remove(brushChangeListener);
		super.dispose();
	}
}
