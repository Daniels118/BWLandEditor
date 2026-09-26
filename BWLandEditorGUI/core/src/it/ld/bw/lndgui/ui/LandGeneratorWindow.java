package it.ld.bw.lndgui.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.LH3DLandCell.Sound;
import it.ld.bw.lnd.tools.RandomLandscapeGenerator;
import it.ld.bw.lnd.tools.RandomLandscapeGenerator.BiomeRule;
import it.ld.bw.lnd.tools.RandomLandscapeGenerator.Params;
import it.ld.bw.lnd.tools.RandomLandscapeGenerator.Params.Property;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.Settings.ShowGridOption;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.Spinner;
import it.ld.libgdx.ui.components.TextSlider;
import it.ld.libgdx.ui.components.GridView.State;
import it.ld.libgdx.ui.components.ListTable.ItemClickEvent;
import it.ld.libgdx.ui.components.ListTable.ItemClickListener;
import it.ld.libgdx.ui.components.ImageButton;
import it.ld.libgdx.ui.components.ListTable;
import it.ld.libgdx.ui.components.MenuItemAction;
import it.ld.libgdx.ui.components.PopupMenu;
import it.ld.libgdx.ui.components.Prompt;
import it.ld.libgdx.ui.components.Prompt.PromptListener;
import it.ld.libgdx.utils.Utils;
import it.ld.utils.MathUtils;
import it.ld.utils.UChangeListener;

public class LandGeneratorWindow extends SmartWindow {
	private static final int THUMBW = 48;
	private static final int THUMBH = 64;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		LandGeneratorWindow window = (LandGeneratorWindow) SmartWindow.getSingleInstance("landGeneratorWindow");
		if (window == null) {
			window = new LandGeneratorWindow(app, skin, true);
			window.setSingleInstance("landGeneratorWindow");
		}
		window.show(stage, true);
		window.toFront();
	}
	
	public static void hideInstance() {
		LandGeneratorWindow window = (LandGeneratorWindow) SmartWindow.getSingleInstance("landGeneratorWindow");
		if (window != null) {
			window.remove();
		}
	}
	
	private final MainApp app;
	private final LndFile land;
	
	private TextSlider widthField;
	private TextSlider heightField;
	private TextField seedField;
	private TextSlider mountainAmountField;
	private TextSlider ruggednessField;
	private TextSlider maxAltitudeField;
	
	private TextSlider coastNoiseFrequencyField;
	private Spinner coastNoiseOctavesField;
	
	private TextSlider hillFrequencyField;
	private Spinner hillOctavesField;
	
	private TextSlider mountainFrequencyField;
	private Spinner mountainOctavesField;
	
	private TextSlider mountainDetailFrequencyField;
	private Spinner mountainDetailOctavesField;
	
	private TextSlider biomeFrequencyField;
	private Spinner biomeOctavesField;
	
	private TextSlider coastalPlainWidthField;
	private TextSlider coastalPlainMaxAltitudeField;
	
	private TextSlider inlandRiseField;
	private TextSlider mountainThresholdField;
	private TextSlider mountainSharpnessField;
	
	private Spinner smallIslandCountField;
	private Spinner lakeAttemptsField;
	private TextSlider maxLakeRadiusField;
	
	private final Image coastlineCountryIcon = new Image();
	
	private final ListTable<BiomeRule> biomeList;
	
	private View3D view3D;
	
	private Params params;
	private LndFile newLand = null;
	private boolean resetView = true;
	
	private Texture coastlineCountryTexture;
	private Array<Drawable> countryThumbs;
	private Texture countryAtlas;
	
	public LandGeneratorWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("landGenerator.title"), skin, SmartWindow.Attribute.RESIZABLE);
		this.setAutodispose(autodispose);
		this.defaults().align(Align.topLeft).pad(2f);
		this.columnDefaults(1).expandX();
		
		this.app = app;
		this.land = app.getLand();
		updateCountryThumbs();
		
		Table leftPanel = new Table();
		leftPanel.defaults().padBottom(2);
		leftPanel.columnDefaults(0).left().padRight(2);
		leftPanel.columnDefaults(1).expandX().right();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.seed"), skin));
		seedField = new TextField("", skin);
		seedField.setAlignment(Align.right);
		seedField.addListener(new FocusListener() {
			@Override
			public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
				if (!event.isFocused()) {
					try {
						if (!seedField.getText().isEmpty()) {
							params.setSeed(Long.parseLong(seedField.getText()));
						}
					} catch (Exception e) {}
				}
			}
		});
		leftPanel.add(seedField).row();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.w"), skin));
		widthField = new TextSlider(skin, 0, 16, 500, 1, true);
		widthField.setValueChangeListener((value) -> {
			params.setWidth((int)value);
		});
		leftPanel.add(widthField).row();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.h"), skin));
		heightField = new TextSlider(skin, 0, 16, 500, 1, true);
		heightField.setValueChangeListener((value) -> {
			params.setHeight((int)value);
		});
		leftPanel.add(heightField).row();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.mountainAmount"), skin));
		mountainAmountField = new TextSlider(skin, 0, 0, 1, 0.05f);
		mountainAmountField.setValueChangeListener((value) -> {
			params.setMountainAmount(value);
		});
		leftPanel.add(mountainAmountField).row();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.ruggedness"), skin));
		ruggednessField = new TextSlider(skin, 0, 0, 1, 0.05f);
		ruggednessField.setValueChangeListener((value) -> {
			params.setRuggedness(value);
		});
		leftPanel.add(ruggednessField).row();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.maxAltitude"), skin));
		maxAltitudeField = new TextSlider(skin, 0, 3, 255, 1, true);
		maxAltitudeField.setValueChangeListener((value) -> {
			params.setMaxAltitude((int)value);
		});
		leftPanel.add(maxAltitudeField).row();
		
		Table tmpTable = new Table();
		tmpTable.columnDefaults(0).left().padRight(12);
		tmpTable.columnDefaults(1).padRight(12);
		
		tmpTable.add();
		tmpTable.add(new Label(I18n.tr("landGenerator.frequency"), skin));
		tmpTable.add(new Label(I18n.tr("landGenerator.octaves"), skin)).row();
		
		tmpTable.add(new Label(I18n.tr("landGenerator.coastNoise"), skin));
		coastNoiseFrequencyField = new TextSlider(skin, 0, 0, 0.1f, 0.001f);
		coastNoiseFrequencyField.setValueChangeListener((value) -> {
			params.setCoastNoiseFrequency(value);
		});
		coastNoiseFrequencyField.addListener(new TextTooltip(I18n.tr("landGenerator.coastNoiseFrequencyHint"), skin));
		tmpTable.add(coastNoiseFrequencyField);
		coastNoiseOctavesField = new Spinner(skin, 0, 1, 10, 1, true);
		coastNoiseOctavesField.setValueChangeListener((value) -> {
			params.setCoastNoiseOctaves((int)value);
		});
		tmpTable.add(coastNoiseOctavesField).row();
		
		tmpTable.add(new Label(I18n.tr("landGenerator.hill"), skin));
		hillFrequencyField = new TextSlider(skin, 0, 0, 0.1f, 0.001f);
		hillFrequencyField.setValueChangeListener((value) -> {
			params.setHillFrequency(value);
		});
		tmpTable.add(hillFrequencyField);
		hillOctavesField = new Spinner(skin, 0, 1, 10, 1, true);
		hillOctavesField.setValueChangeListener((value) -> {
			params.setHillOctaves((int)value);
		});
		tmpTable.add(hillOctavesField).row();
		
		tmpTable.add(new Label(I18n.tr("landGenerator.mountain"), skin));
		mountainFrequencyField = new TextSlider(skin, 0, 0.002f, 0.1f, 0.001f);
		mountainFrequencyField.setValueChangeListener((value) -> {
			params.setMountainFrequency(value);
		});
		tmpTable.add(mountainFrequencyField);
		mountainOctavesField = new Spinner(skin, 0, 1, 10, 1, true);
		mountainOctavesField.setValueChangeListener((value) -> {
			params.setMountainOctaves((int)value);
		});
		tmpTable.add(mountainOctavesField).row();
		
		tmpTable.add(new Label(I18n.tr("landGenerator.mountainDetail"), skin));
		mountainDetailFrequencyField = new TextSlider(skin, 0, 0, 0.1f, 0.001f);
		mountainDetailFrequencyField.setValueChangeListener((value) -> {
			params.setMountainDetailFrequency(value);
		});
		mountainDetailFrequencyField.addListener(new TextTooltip(I18n.tr("landGenerator.mountainDetailFrequencyHint"), skin));
		tmpTable.add(mountainDetailFrequencyField);
		mountainDetailOctavesField = new Spinner(skin, 0, 1, 10, 1, true);
		mountainDetailOctavesField.setValueChangeListener((value) -> {
			params.setMountainDetailOctaves((int)value);
		});
		tmpTable.add(mountainDetailOctavesField).row();
		
		tmpTable.add(new Label(I18n.tr("landGenerator.biome"), skin));
		biomeFrequencyField = new TextSlider(skin, 0, 0, 0.1f, 0.001f);
		biomeFrequencyField.setValueChangeListener((value) -> {
			params.setBiomeFrequency(value);
		});
		tmpTable.add(biomeFrequencyField);
		biomeOctavesField = new Spinner(skin, 0, 1, 10, 1, true);
		biomeOctavesField.setValueChangeListener((value) -> {
			params.setBiomeOctaves((int)value);
		});
		tmpTable.add(biomeOctavesField).row();
		
		leftPanel.add(tmpTable).colspan(2).padBottom(8).row();
		
		
		leftPanel.add(new Label(I18n.tr("landGenerator.coastalPlainWidth"), skin));
		coastalPlainWidthField = new TextSlider(skin, 0, 0, 20, 1, true);
		coastalPlainWidthField.setValueChangeListener((value) -> {
			params.setCoastalPlainWidth((int)value);
		});
		coastalPlainWidthField.addListener(new TextTooltip(I18n.tr("landGenerator.coastalPlainWidthHint"), skin));
		leftPanel.add(coastalPlainWidthField).row();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.coastalPlainMaxAltitude"), skin));
		coastalPlainMaxAltitudeField = new TextSlider(skin, 0, 3, 50, 1, true);
		coastalPlainMaxAltitudeField.setValueChangeListener((value) -> {
			params.setCoastalPlainMaxAltitude((int)value);
		});
		coastalPlainMaxAltitudeField.addListener(new TextTooltip(I18n.tr("landGenerator.coastalPlainMaxAltitudeHint"), skin));
		leftPanel.add(coastalPlainMaxAltitudeField).row();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.inlandRise"), skin));
		inlandRiseField = new TextSlider(skin, 0, 0, 5, 0.1f);
		inlandRiseField.setValueChangeListener((value) -> {
			params.setInlandRise(value);
		});
		inlandRiseField.addListener(new TextTooltip(I18n.tr("landGenerator.inlandRiseHint"), skin));
		leftPanel.add(inlandRiseField).row();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.mountainThreshold"), skin));
		mountainThresholdField = new TextSlider(skin, 0, 0, 1, 0.05f);
		mountainThresholdField.setValueChangeListener((value) -> {
			params.setMountainThreshold(value);
		});
		mountainThresholdField.addListener(new TextTooltip(I18n.tr("landGenerator.mountainThresholdHint"), skin));
		leftPanel.add(mountainThresholdField).row();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.mountainSharpness"), skin));
		mountainSharpnessField = new TextSlider(skin, 0, 0.5f, 3, 0.05f);
		mountainSharpnessField.setValueChangeListener((value) -> {
			params.setMountainSharpness(value);
		});
		mountainSharpnessField.addListener(new TextTooltip(I18n.tr("landGenerator.mountainSharpnessHint"), skin));
		leftPanel.add(mountainSharpnessField).row();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.smallIslandCount"), skin));
		smallIslandCountField = new Spinner(skin, 0, 0, 10, 1, true);
		smallIslandCountField.setValueChangeListener((value) -> {
			params.setSmallIslandCount((int)value);
		});
		leftPanel.add(smallIslandCountField).row();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.lakeAttempts"), skin));
		lakeAttemptsField = new Spinner(skin, 0, 0, 20, 1, true);
		lakeAttemptsField.setValueChangeListener((value) -> {
			params.setLakeAttempts((int)value);
		});
		leftPanel.add(lakeAttemptsField).row();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.maxLakeRadius"), skin));
		maxLakeRadiusField = new TextSlider(skin, 0, 3, 40, 1, true);
		maxLakeRadiusField.setValueChangeListener((value) -> {
			params.setMaxLakeRadius((int)value);
		});
		leftPanel.add(maxLakeRadiusField).row();
		
		leftPanel.add(new Label(I18n.tr("landGenerator.coastlineCountry"), skin));
		Button brushCoastlineCountryButton = new Button(coastlineCountryIcon, skin, "hover");
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
						params.setCoastlineCountry(country);
						return true;
					}
				})
        		.show(getStage(), true);
			}
		});
		leftPanel.add(brushCoastlineCountryButton).size(48).left().row();
		
		leftPanel.add().colspan(2).expandY().row();	//Filler
		
		tmpTable = new Table();
		tmpTable.add(new Label(I18n.tr("landGenerator.biomes"), skin));
		Button addBiomeButton = new ImageButton(skin, "tool-add", I18n.tr("landGenerator.biomes.add"), () -> addBiome());
		tmpTable.add(addBiomeButton).size(24).padLeft(6);
		
		leftPanel.add(tmpTable).colspan(2).padTop(8).padBottom(0).row();
		
		add(leftPanel).fillY().padRight(4);
		
		view3D = new View3D(app);
		view3D.setShowGrid(ShowGridOption.EVERYWHERE);
		add(view3D).minSize(400).grow().row();
		
		biomeList = new ListTable<BiomeRule>(skin)
		.setCellPadding(2);
		biomeList.setModel(new ListTable.DefaultModel<BiomeRule>(skin) {
			private final Drawable[] soundImages;
			private final String[] soundLabels;
			
			{
				final Array<Sound> sounds = new Array<Sound>(Sound.values());
				soundImages = new Drawable[sounds.size];
				soundLabels = new String[sounds.size];
				for (int i = 0; i < sounds.size; i++) {
					Sound sound = sounds.get(i);
					soundImages[i] = skin.getDrawable("sound-" + sound.name().toLowerCase());
					soundLabels[i] = I18n.tr("cell.sounds." + sound.name());
				}
			}
			
			@Override
			public String[] getHeaderNames() {
				return new String[] {
					I18n.tr("landGenerator.biomes.name"),
					I18n.tr("landGenerator.biomes.country"),
					I18n.tr("landGenerator.biomes.sound"),
					I18n.tr("landGenerator.biomes.altitude"),
					I18n.tr("landGenerator.biomes.slope"),
					I18n.tr("landGenerator.biomes.humidity"),
					I18n.tr("landGenerator.biomes.temperature"),
					I18n.tr("landGenerator.biomes.distanceFromWater"),
					I18n.tr("landGenerator.biomes.distanceFromLake"),
					""	//Filler
				};
			}
			
			private final TextSlider.ValueChangeListener minAltitudeChangeListener = (value) -> {
				int row = biomeList.getTouchRow();
				if (row < 0) return;
				BiomeRule rule = params.getBiomes().get(row);
				rule.setMinAltitude((int) value);
			};
			
			private final TextSlider.ValueChangeListener maxAltitudeChangeListener = (value) -> {
				int row = biomeList.getTouchRow();
				if (row < 0) return;
				BiomeRule rule = params.getBiomes().get(row);
				rule.setMaxAltitude((int) value);
			};
			
			private final TextSlider.ValueChangeListener minSlopeChangeListener = (value) -> {
				int row = biomeList.getTouchRow();
				if (row < 0) return;
				BiomeRule rule = params.getBiomes().get(row);
				rule.setMinSlope((int) value);
			};
			
			private final TextSlider.ValueChangeListener maxSlopeChangeListener = (value) -> {
				int row = biomeList.getTouchRow();
				if (row < 0) return;
				BiomeRule rule = params.getBiomes().get(row);
				rule.setMaxSlope((int) value);
			};
			
			private final TextSlider.ValueChangeListener minHumidityChangeListener = (value) -> {
				int row = biomeList.getTouchRow();
				if (row < 0) return;
				BiomeRule rule = params.getBiomes().get(row);
				rule.setMinHumidity(value);
			};
			
			private final TextSlider.ValueChangeListener maxHumidityChangeListener = (value) -> {
				int row = biomeList.getTouchRow();
				if (row < 0) return;
				BiomeRule rule = params.getBiomes().get(row);
				rule.setMaxHumidity(value);
			};
			
			private final TextSlider.ValueChangeListener minTemperatureChangeListener = (value) -> {
				int row = biomeList.getTouchRow();
				if (row < 0) return;
				BiomeRule rule = params.getBiomes().get(row);
				rule.setMinTemperature(value);
			};
			
			private final TextSlider.ValueChangeListener maxTemperatureChangeListener = (value) -> {
				int row = biomeList.getTouchRow();
				if (row < 0) return;
				BiomeRule rule = params.getBiomes().get(row);
				rule.setMaxTemperature(value);
			};
			
			private final Spinner.ValueChangeListener minDistanceFromWaterChangeListener = (value) -> {
				int row = biomeList.getTouchRow();
				if (row < 0) return;
				BiomeRule rule = params.getBiomes().get(row);
				rule.setMinDistanceFromWater((int) value);
			};
			
			private final Spinner.ValueChangeListener maxDistanceFromWaterChangeListener = (value) -> {
				int row = biomeList.getTouchRow();
				if (row < 0) return;
				BiomeRule rule = params.getBiomes().get(row);
				rule.setMaxDistanceFromWater((int) value);
			};
			
			private final Spinner.ValueChangeListener minDistanceFromLakeChangeListener = (value) -> {
				int row = biomeList.getTouchRow();
				if (row < 0) return;
				BiomeRule rule = params.getBiomes().get(row);
				rule.setMinDistanceFromLake((int) value);
			};
			
			private final Spinner.ValueChangeListener maxDistanceFromLakeChangeListener = (value) -> {
				int row = biomeList.getTouchRow();
				if (row < 0) return;
				BiomeRule rule = params.getBiomes().get(row);
				rule.setMaxDistanceFromLake((int) value);
			};
			
			@Override
			public Actor createField(int row, int col) {
				if (col == 0) {
					TextField field = new TextField("", skin);
					return field;
				} else if (col == 1 || col == 2) {
					Image image = new Image();
					image.setScaling(Scaling.fit);
					image.setSize(THUMBW, THUMBH);
					Button button = new Button(image, skin, "hover");
					button.setSize(THUMBW, THUMBH);
					return button;
				} else if (col == 3) {	//Altitude
					Table fields = new Table();
					TextSlider fieldLow = new TextSlider(skin, 0, 0, 255, 1, true);
					fieldLow.setValueChangeListener(minAltitudeChangeListener);
					fields.add(fieldLow).minWidth(60).row();
					TextSlider fieldHigh = new TextSlider(skin, 0, 0, 255, 1, true);
					fieldHigh.setValueChangeListener(maxAltitudeChangeListener);
					fields.add(fieldHigh).minWidth(60);
					return fields;
				} else if (col == 4) {	//Slope
					Table fields = new Table();
					TextSlider fieldLow = new TextSlider(skin, 0, 0, 255, 1, true);
					fieldLow.setValueChangeListener(minSlopeChangeListener);
					fields.add(fieldLow).minWidth(60).row();
					TextSlider fieldHigh = new TextSlider(skin, 0, 0, 255, 1, true);
					fieldHigh.setValueChangeListener(maxSlopeChangeListener);
					fields.add(fieldHigh).minWidth(60);
					return fields;
				} else if (col == 5) {	//Humidity
					Table fields = new Table();
					TextSlider fieldLow = new TextSlider(skin, 0, 0, 1f, 0.01f);
					fieldLow.setValueChangeListener(minHumidityChangeListener);
					fields.add(fieldLow).minWidth(60).row();
					TextSlider fieldHigh = new TextSlider(skin, 0, 0, 1f, 0.01f);
					fieldHigh.setValueChangeListener(maxHumidityChangeListener);
					fields.add(fieldHigh).minWidth(60);
					return fields;
				} else if (col == 6) {	//Temperature
					Table fields = new Table();
					TextSlider fieldLow = new TextSlider(skin, 0, 0, 1f, 0.01f);
					fieldLow.setValueChangeListener(minTemperatureChangeListener);
					fields.add(fieldLow).minWidth(60).row();
					TextSlider fieldHigh = new TextSlider(skin, 0, 0, 1f, 0.01f);
					fieldHigh.setValueChangeListener(maxTemperatureChangeListener);
					fields.add(fieldHigh).minWidth(60);
					return fields;
				} else if (col == 7) {	//DistanceFromWater
					Table fields = new Table();
					Spinner fieldLow = new Spinner(skin, 0, 0, Integer.MAX_VALUE, 1, true);
					fieldLow.setValueChangeListener(minDistanceFromWaterChangeListener);
					fields.add(fieldLow).minWidth(60).row();
					Spinner fieldHigh = new Spinner(skin, 0, 0, Integer.MAX_VALUE, 1, true);
					fieldHigh.setValueChangeListener(maxDistanceFromWaterChangeListener);
					fields.add(fieldHigh).minWidth(60);
					return fields;
				} else if (col == 8) {	//DistanceFromLake
					Table fields = new Table();
					Spinner fieldLow = new Spinner(skin, 0, 0, Integer.MAX_VALUE, 1, true);
					fieldLow.setValueChangeListener(minDistanceFromLakeChangeListener);
					fields.add(fieldLow).minWidth(60).row();
					Spinner fieldHigh = new Spinner(skin, 0, 0, Integer.MAX_VALUE, 1, true);
					fieldHigh.setValueChangeListener(maxDistanceFromLakeChangeListener);
					fields.add(fieldHigh).minWidth(60);
					return fields;
				}
				return super.createField(row, col);
			}
			
			@Override
			public void render(int row, BiomeRule rule, Cell<Container<Actor>>[] fields) {
				fields[0].getActor().minWidth(60);
				fields[1].size(60, 70);
				fields[2].width(60);
				fields[9].expandX();	//Filler
				TextField fName = ((TextField)fields[0].getActor().getActor());
				Image fCountry = (Image)((Button)fields[1].getActor().getActor()).getChild(0);
				Image fSound = (Image)((Button)fields[2].getActor().getActor()).getChild(0);
				TextSlider fMinAltitude = (TextSlider)((Table)fields[3].getActor().getActor()).getChild(0);
				TextSlider fMaxAltitude = (TextSlider)((Table)fields[3].getActor().getActor()).getChild(1);
				TextSlider fMinSlope = (TextSlider)((Table)fields[4].getActor().getActor()).getChild(0);
				TextSlider fMaxSlope = (TextSlider)((Table)fields[4].getActor().getActor()).getChild(1);
				TextSlider fMinHumidity = (TextSlider)((Table)fields[5].getActor().getActor()).getChild(0);
				TextSlider fMaxHumidity = (TextSlider)((Table)fields[5].getActor().getActor()).getChild(1);
				TextSlider fMinTemperature = (TextSlider)((Table)fields[6].getActor().getActor()).getChild(0);
				TextSlider fMaxTemperature = (TextSlider)((Table)fields[6].getActor().getActor()).getChild(1);
				Spinner fMinDistanceFromWater = (Spinner)((Table)fields[7].getActor().getActor()).getChild(0);
				Spinner fMaxDistanceFromWater = (Spinner)((Table)fields[7].getActor().getActor()).getChild(1);
				Spinner fMinDistanceFromLake = (Spinner)((Table)fields[8].getActor().getActor()).getChild(0);
				Spinner fMaxDistanceFromLake = (Spinner)((Table)fields[8].getActor().getActor()).getChild(1);
				//Content
				fName.setText(rule.getName());
				if (rule.getCountry() != null) {
					fCountry.setDrawable(countryThumbs.get(rule.getCountry().getIndex()));
				} else {
					fCountry.setDrawable(null);
				}
				if (rule.getSound() != null) {
					fSound.setDrawable(soundImages[rule.getSound().ordinal()]);
					fSound.addListener(new TextTooltip(soundLabels[rule.getSound().ordinal()], skin));
				} else {
					fSound.setDrawable(null);
				}
				fMinAltitude.setValue(rule.getMinAltitude());
				fMaxAltitude.setValue(rule.getMaxAltitude());
				
				fMinSlope.setValue(rule.getMinSlope());
				fMaxSlope.setValue(rule.getMaxSlope());
				
				fMinHumidity.setValue(rule.getMinHumidity());
				fMaxHumidity.setValue(rule.getMaxHumidity());
				
				fMinTemperature.setValue(rule.getMinTemperature());
				fMaxTemperature.setValue(rule.getMaxTemperature());
				
				fMinDistanceFromWater.setValue(rule.getMinDistanceFromWater());
				fMaxDistanceFromWater.setValue(rule.getMaxDistanceFromWater());
				
				fMinDistanceFromLake.setValue(rule.getMinDistanceFromLake());
				fMaxDistanceFromLake.setValue(rule.getMaxDistanceFromLake());
			}
		})
		.setHeaderVisible(true)
		.setRowsBackground(null, null);
		biomeList.addListener(new ItemClickListener<BiomeRule>() {
			@Override
			public void clicked(ItemClickEvent<BiomeRule> event, int row, int col, BiomeRule rule) {
				if (event.getSource().getButton() == Buttons.LEFT) {
					if (col == 1) {
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
									rule.setCountry(country);
									biomeList.refresh(row);
								}
								return true;
							}
						})
		        		.show(getStage(), true);
					} else if (col == 2) {
						SoundPicker tmpPicker = new SoundPicker(skin, true);
						tmpPicker.setCallback(new EventListener() {
							@Override
							public boolean handle(Event event) {
								Sound sound = tmpPicker.getSelected();
								rule.setSound(sound);
								biomeList.refresh(row);
								return true;
							}
						})
						.show(getStage(), true);
					}
				}
			}
		});
		biomeList.addListener(new ClickListener(Buttons.RIGHT) {
			public void clicked(InputEvent event, float x, float y) {
				BiomeRule rule = biomeList.getSelected();
				if (rule != null) {
					List<MenuItemAction> actions = new LinkedList<>();
					int index = params.getBiomes().indexOf(rule);
					if (index > 0) {
						actions.add(new MenuItemAction("moveUp", I18n.tr("landGenerator.biomes.moveUp"), () -> {
							params.getBiomes().remove(rule);
							params.getBiomes().add(index - 1, rule);
							updateBiomes();
						}));
					}
					if (index < params.getBiomes().size() - 1) {
						actions.add(new MenuItemAction("moveDown", I18n.tr("landGenerator.biomes.moveDown"), () -> {
							params.getBiomes().remove(rule);
							params.getBiomes().add(index + 1, rule);
							updateBiomes();
						}));
					}
					if (!actions.isEmpty()) {
						actions.add(PopupMenu.separator);
					}
					actions.add(new MenuItemAction("remove", I18n.tr("landGenerator.biomes.remove"), () -> {
						params.getBiomes().remove(rule);
						updateBiomes();
					}));
					PopupMenu.show1(getStage(), event.getStageX(), event.getStageY(), actions.toArray(new MenuItemAction[0]));
				}				
			};
		});
		add(biomeList).colspan(2).minSize(400, 200).grow().row();
		
		Table buttons = new Table();
		buttons.defaults().width(100);
		Button generateButton = new TextButton(I18n.tr("landGenerator.generate"), skin);
		generateButton.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				if (generateButton.isDisabled()) return;
				generateAsync();
			};
		});
		buttons.add(generateButton).padRight(4);
		
		Button randomizeButton = new TextButton(I18n.tr("landGenerator.randomize"), skin);
		randomizeButton.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				if (randomizeButton.isDisabled()) return;
				resetView = true;
				params.setSeed(System.currentTimeMillis());
			};
		});
		buttons.add(randomizeButton).padRight(4);
		
		Button settleButton = new TextButton(I18n.tr("landGenerator.settle"), skin);
		//settleButton.setDisabled(true);
		settleButton.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				if (settleButton.isDisabled()) return;
				settle();
			};
		});
		buttons.add(settleButton);
		add(buttons).colspan(2).right().row();
		
		app.listeners.add(appChangeListener);
		setParams(createDefaultParams());
		pack();
		this.setMinSize(1000, this.getHeight());
		Timer.schedule(generatorTask, 0.1f, 0.1f);
	}
	
	private UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getType() == EventType.CHANGE && event.getProperty() == MainApp.Property.LAND) {
				remove();
			}
		}
	};
	
	private void addBiome() {
		final Stage stage = getStage();
		final Skin skin = getSkin();
		final LndFile land = app.getLand();
		Prompt.show(stage, skin, I18n.getInstance(), I18n.tr("landGenerator.biomes.add"), I18n.tr("landGenerator.biomes.add.promptForName"), "", new PromptListener() {
			@Override
			public void confirm(String name) {
				CountryPicker tmpPicker = new CountryPicker(skin, land, true, false)
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
							BiomeRule rule = new BiomeRule(name, country, null);
							params.getBiomes().add(rule);
							updateBiomes();
						}
						return true;
					}
				})
        		.show(stage, true);
			}
		});
	}
	
	private static Params createDefaultParams() {
		Params params = new Params();
		BiomeRule fertile = new BiomeRule("Fertile", null, Sound.BIRDS);
		fertile.setMinHumidity(0.4f);
		fertile.setMaxHumidity(1f);
		fertile.setMinTemperature(0.3f);
		fertile.setMaxTemperature(0.4f);
		BiomeRule rock = new BiomeRule("Cliff", null, Sound.WIND);
		rock.setMinSlope(15);
		BiomeRule snow = new BiomeRule("Snow", null, Sound.WIND);
		snow.setMinAltitude(100);
		snow.setMaxTemperature(0.4f);
		BiomeRule arid = new BiomeRule("Arid", null, Sound.DESERT);
		arid.setMaxHumidity(0.5f);
		arid.setMinTemperature(0.3f);
		params.getBiomes().add(snow);
		params.getBiomes().add(rock);
		params.getBiomes().add(arid);
		params.getBiomes().add(fertile);
		return params;
	}
	
	private boolean regenerate = false;
	
	private final Task generatorTask = new Task() {
		@Override
		public void run() {
			if (regenerate) {
				generate();
				regenerate = false;
			}
		}
	};
	
	private void generateAsync() {
		regenerate = true;
	}
	
	private void generate() {
		RandomLandscapeGenerator generator = new RandomLandscapeGenerator();
		newLand = generator.generateLand(this.land, params);
		view3D.setLand(newLand);
		if (resetView) {
			view3D.viewFromTop();
			resetView = false;
		}
	}
	
	private void settle() {
		remove();
		app.pasteLand(newLand, "generate land", "<land generator>");
	}
	
	private Texture createCountryTexture(LNDCountry country) {
		if (country == null) return null;
		return Utils.toTexture(app.getDefaultCountryPreviewGenerator().generatePreview(country, 49, 49), 49, 49, true);
	}
	
	public void setParams(Params params) {
		if (this.params != null) {
			this.params.listeners.remove(paramsChangeListener);
		}
		this.params = params;
		if (params != null) {
			resetView = true;
			widthField.setValue(params.getWidth());
			heightField.setValue(params.getHeight());
			seedField.setText(String.valueOf(params.getSeed()));
			mountainAmountField.setValue(params.getMountainAmount());
			ruggednessField.setValue(params.getRuggedness());
			maxAltitudeField.setValue(params.getMaxAltitude());
			
			coastNoiseFrequencyField.setValue(params.getCoastNoiseFrequency());
			coastNoiseOctavesField.setValue(params.getCoastNoiseOctaves());
			
			hillFrequencyField.setValue(params.getHillFrequency());
			hillOctavesField.setValue(params.getHillOctaves());
			
			mountainFrequencyField.setValue(params.getMountainFrequency());
			mountainOctavesField.setValue(params.getMountainOctaves());
			
			mountainDetailFrequencyField.setValue(params.getMountainDetailFrequency());
			mountainDetailOctavesField.setValue(params.getMountainDetailOctaves());
			
			biomeFrequencyField.setValue(params.getBiomeFrequency());
			biomeOctavesField.setValue(params.getBiomeOctaves());
			
			coastalPlainWidthField.setValue(params.getCoastalPlainWidth());
			coastalPlainMaxAltitudeField.setValue(params.getCoastalPlainMaxAltitude());
			
			inlandRiseField.setValue(params.getInlandRise());
			mountainThresholdField.setValue(params.getMountainThreshold());
			mountainSharpnessField.setValue(params.getMountainSharpness());
			
			smallIslandCountField.setValue(params.getSmallIslandCount());
			lakeAttemptsField.setValue(params.getLakeAttempts());
			maxLakeRadiusField.setValue(params.getMaxLakeRadius());
			
			setCoastlineCountry(params.getCoastlineCountry());
			
			updateBiomes();
			params.listeners.add(paramsChangeListener);
		}
	}
	
	private void updateBiomes() {
		biomeList.setItems(new Array<>(params.getBiomes().toArray(new BiomeRule[0])));
	}
	
	private final UChangeListener paramsChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			Params.Property property = (Property) event.getProperty();
			switch (property) {
				case BIOMES:
					if (event.getType() == EventType.ADD) {
						BiomeRule rule = (BiomeRule) event.getNewValue();
						rule.listeners.add(biomeChangeListener);
					} else if (event.getType() == EventType.REMOVE) {
						BiomeRule rule = (BiomeRule) event.getOldValue();
						rule.listeners.remove(biomeChangeListener);
					}
					break;
				case BIOME_FREQUENCY:
					biomeFrequencyField.setValue(params.getBiomeFrequency());
					break;
				case BIOME_OCTAVES:
					biomeOctavesField.setValue(params.getBiomeOctaves());
					break;
				case COASTAL_PLAIN_MAX_ALTITUDE:
					coastalPlainMaxAltitudeField.setValue(params.getCoastalPlainMaxAltitude());
					break;
				case COASTAL_PLAIN_WIDTH:
					coastalPlainWidthField.setValue(params.getCoastalPlainWidth());
					break;
				case COASTLINE_COUNTRY:
					setCoastlineCountry(params.getCoastlineCountry());
					break;
				case COAST_NOISE_FREQUENCY:
					coastNoiseFrequencyField.setValue(params.getCoastNoiseFrequency());
					break;
				case COAST_NOISE_OCTAVES:
					coastNoiseOctavesField.setValue(params.getCoastNoiseOctaves());
					break;
				case HEIGHT:
					heightField.setValue(params.getHeight());
					resetView = true;
					break;
				case HILL_FREQUENCY:
					hillFrequencyField.setValue(params.getHillFrequency());
					break;
				case HILL_OCTAVES:
					hillOctavesField.setValue(params.getHillOctaves());
					break;
				case INLAND_RISE:
					inlandRiseField.setValue(params.getInlandRise());
					break;
				case ISLAND_RADIUS:
					//islandRadiusField.setValue(params.getIslandRadius());
					break;
				case LAKE_ATTEMPTS:
					lakeAttemptsField.setValue(params.getLakeAttempts());
					break;
				case MAX_ALTITUDE:
					maxAltitudeField.setValue(params.getMaxAltitude());
					break;
				case MAX_LAKE_RADIUS:
					maxLakeRadiusField.setValue(params.getMaxLakeRadius());
					break;
				case MOUNTAIN_AMOUNT:
					mountainAmountField.setValue(params.getMountainAmount());
					break;
				case MOUNTAIN_DETAIL_FREQUENCY:
					mountainDetailFrequencyField.setValue(params.getMountainDetailFrequency());
					break;
				case MOUNTAIN_DETAIL_OCTAVES:
					mountainDetailOctavesField.setValue(params.getMountainDetailOctaves());
					break;
				case MOUNTAIN_FREQUENCY:
					mountainFrequencyField.setValue(params.getMountainFrequency());
					break;
				case MOUNTAIN_OCTAVES:
					mountainOctavesField.setValue(params.getMountainOctaves());
					break;
				case MOUNTAIN_SHARPNESS:
					mountainSharpnessField.setValue(params.getMountainSharpness());
					break;
				case MOUNTAIN_THRESHOLD:
					mountainThresholdField.setValue(params.getMountainThreshold());
					break;
				case RUGGEDNESS:
					ruggednessField.setValue(params.getRuggedness());
					break;
				case SEED:
					seedField.setText(String.valueOf(params.getSeed()));
					break;
				case SMALL_ISLAND_COUNT:
					smallIslandCountField.setValue(params.getSmallIslandCount());
					break;
				case WIDTH:
					widthField.setValue(params.getWidth());
					resetView = true;
					break;
			}
			generateAsync();
		}
	};
	
	private final UChangeListener biomeChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			biomeList.refresh();
		}
	};
	
	private void setCoastlineCountry(LNDCountry country) {
		if (coastlineCountryTexture != null) coastlineCountryTexture.dispose();
		coastlineCountryTexture = createCountryTexture(country);
		coastlineCountryIcon.setDrawable(coastlineCountryTexture == null ? null : new TextureRegionDrawable(coastlineCountryTexture));
	}
	
	private void updateCountryThumbs() {
		//Cleanup
		if (countryAtlas != null) {
			countryAtlas.dispose();
		}
		//
		List<LNDCountry> countries = land != null ? land.getCountries() : new ArrayList<>(0);
		int count = countries.size();
		int radix = Math.max(1, (int)Math.ceil(Math.sqrt(count)));
		int nx = Math.max(1, count / radix);
		int ny = Math.max(1, MathUtils.ceilDiv(count, nx));
		Pixmap sheet = new Pixmap(nx * THUMBW, ny * THUMBH, Pixmap.Format.RGB888);
		for (int i = 0; i < count; i++) {
			Pixmap pixmap = Utils.toPixmap(app.getDefaultCountryPreviewGenerator().generatePreview(countries.get(i), 256, 512), 256, 512);
			int x = (i % nx) * THUMBW;
	        int y = (i / nx) * THUMBH;
	        sheet.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), x, y, THUMBW, THUMBH);
	        pixmap.dispose();
		}
		
		countryAtlas = new Texture(sheet);
	    sheet.dispose();

	    countryThumbs = new Array<>(count);
	    for (int i = 0; i < count; i++) {
	        int x = (i % nx) * THUMBW;
	        int y = (i / nx) * THUMBH;
	        countryThumbs.add(new TextureRegionDrawable(new TextureRegion(countryAtlas, x, y, THUMBW, THUMBH)));
	    }
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		generatorTask.cancel();
		app.listeners.remove(appChangeListener);
		if (countryAtlas != null) countryAtlas.dispose();
		if (coastlineCountryTexture != null) coastlineCountryTexture.dispose();
		view3D.dispose();
		super.dispose();
	}
}
