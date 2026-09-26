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
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.tools.Brush;
import it.ld.bw.lndgui.tools.SculptBrush;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.Spinner;
import it.ld.libgdx.ui.components.TextSlider;
import it.ld.libgdx.ui.components.GridView.State;
import it.ld.libgdx.ui.components.ImageButton;
import it.ld.libgdx.utils.Utils;
import it.ld.utils.UChangeListener;

public class WaterOptionsWindow extends SmartWindow {
	public static void showSingleInstance(MainApp app, String title, SculptBrush brush, Stage stage, Actor anchor, Skin skin) {
		WaterOptionsWindow window = (WaterOptionsWindow) SmartWindow.getSingleInstance("waterOptionsWindow");
		if (window == null) {
			window = new WaterOptionsWindow(app, skin, true);
			window.setSingleInstance("waterOptionsWindow");
		}
		window.getTitleLabel().setText(title);
		window.setBrush(brush);
		Vector2 pos = new Vector2(anchor.getWidth(), anchor.getHeight());
		anchor.localToStageCoordinates(pos);
		window.show(stage, pos.x, pos.y - window.getHeight(), false);
		window.toFront();
	}
	
	public static void hideInstance() {
		WaterOptionsWindow window = (WaterOptionsWindow) SmartWindow.getSingleInstance("waterOptionsWindow");
		if (window != null) {
			window.remove();
		}
	}
	
	private MainApp app;
	private SculptBrush brush;
	
	private SelectBox<Brush.Shape> brushShape;
	private TextSlider brushAngle;
	private Button brushRotateWithCamera;
	private TextSlider brushSize;
	private Spinner brushW;
	private Spinner brushH;
	private Slider brushDepth;
	private TextSlider brushSmoothness;
	private final Image brushCoastlineCountryIcon = new Image();
	
	private Texture coastlineCountryTexture;
	
	public WaterOptionsWindow(MainApp app, Skin skin, boolean autodispose) {
		super("Water options", skin);
		this.setAutodispose(autodispose);
		this.defaults().left().pad(2f);
		
		this.app = app;
		
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
		
		add(new Label(I18n.tr("waterOptions.depth"), skin)).top();
		Table depthField = new Table();
		brushDepth = new Slider(LH3DLandCell.DEEP_WATER_ALTITUDE, LH3DLandCell.WET_ALTITUDE, 1, false, skin);
		brushDepth.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				if (brush == null) return;
				brush.setMaxDepth((int)brushDepth.getValue());
			};
		});
		depthField.add(brushDepth).colspan(3).fillX().row();
		depthField.add(new Label(I18n.tr("waterOptions.depth.deep"), skin));
		depthField.add().expandX();
		depthField.add(new Label(I18n.tr("waterOptions.depth.shallow"), skin));
		add(depthField).fillX().row();
		
		add(new Label(I18n.tr("brush.smoothness"), skin));
		brushSmoothness = new TextSlider(skin, 0, 0, 100, 1);
		brushSmoothness.setValueChangeListener((value) -> {
			if (brush == null) return;
			brush.setSmoothness(value / 100f);
		});
		add(brushSmoothness).right().row();
		
		add(new Label(I18n.tr("sculptOptions.coastlineCountry"), skin));
		Table coastlineCountryField = new Table();
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
		coastlineCountryField.add(brushCoastlineCountryButton).size(48, 48);
		Button pickCoastlineCountryButton = new ImageButton(skin, "tool-pickcolor", I18n.tr("sculptOptions.country.pick"), () -> {
			app.getView3D().pickCoord((coord, button) -> {
				LndFile land = app.getLand();
				LH3DLandCell cell = land.getCellAtCoord(coord.x, coord.z);
				if (cell != LH3DLandCell.EMPTY) {
					brush.setCoastlineCountry(land.getCountries().get(cell.getCountry()));
				}
			});
		});
		coastlineCountryField.add(pickCoastlineCountryButton).size(28, 28).padLeft(2).top();
		Button resetCoastlineCountryButton = new ImageButton(skin, "tool-delete", I18n.tr("sculptOptions.country.unset"), () -> brush.setCoastlineCountry(null));
		coastlineCountryField.add(resetCoastlineCountryButton).size(28, 28).padLeft(2).top();
		add(coastlineCountryField).row();
		
		pack();
	}
	
	private Texture createCountryTexture(LNDCountry country) {
		if (country == null) return null;
		return Utils.toTexture(app.getDefaultCountryPreviewGenerator().generatePreview(country, 48, 48), 48, 48, true);
	}
	
	private void setBrush(SculptBrush brush) {
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
			brushDepth.setValue(brush.getMaxDepth());
			brushSmoothness.setValue(brush.getSmoothness() * 100f);
			
			if (coastlineCountryTexture != null) coastlineCountryTexture.dispose();
			coastlineCountryTexture = createCountryTexture(brush.getCoastlineCountry());
			brushCoastlineCountryIcon.setDrawable(coastlineCountryTexture == null ? null : new TextureRegionDrawable(coastlineCountryTexture));
			
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
						case ROTATE_WITH_CAMERA:
							brushRotateWithCamera.setChecked(brush.isRotateWithCameraEnabled());
							break;
						default:
					}
				} else if (event.getProperty() instanceof SculptBrush.Property) {
					SculptBrush.Property property = (SculptBrush.Property)event.getProperty();
					switch (property) {
						case MAX_DEPTH:
							brushDepth.setValue(brush.getMaxDepth());
							break;
						case COASTLINE_COUNTRY:
							if (coastlineCountryTexture != null) coastlineCountryTexture.dispose();
							coastlineCountryTexture = createCountryTexture(brush.getCoastlineCountry());
							brushCoastlineCountryIcon.setDrawable(coastlineCountryTexture == null ? null : new TextureRegionDrawable(coastlineCountryTexture));
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
		if (coastlineCountryTexture != null) coastlineCountryTexture.dispose();
		if (brush != null) brush.listeners.remove(brushChangeListener);
		super.dispose();
	}
}
