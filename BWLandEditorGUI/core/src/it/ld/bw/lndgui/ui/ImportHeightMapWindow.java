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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.MathUtils;
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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LH3DLandCell.Sound;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.Vec3f;
import it.ld.bw.lnd.tools.HeightMapTool;
import it.ld.bw.lndgui.DropEventType;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.tools.SculptBrush;
import it.ld.libgdx.ui.components.ImageButton;
import it.ld.libgdx.ui.components.MessageBox;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.TextSlider;
import it.ld.libgdx.utils.Utils;
import it.ld.utils.UChangeListener;

public class ImportHeightMapWindow extends SmartWindow {
	private final MainApp app;
	
	private int[][] heightmap;
	private LNDCountry country;
	private LNDCountry cliffCountry;
	private LNDCountry coastlineCountry;
	private float cliffSlope = 1f;
	private Sound sound = Sound.BIRDS;
	
	private final Image heightmapIcon = new Image();
	private final Image countryIcon = new Image();
	private final Image cliffCountryIcon = new Image();
	private TextSlider cliffSlopeField;
	private final Image coastlineCountryIcon = new Image();
	private SelectBox<SoundOption> soundField;
	private final CheckBox alignToGridCheckbox;
	private Button okButton;
	
	private Texture heightmapTexture;
	private Texture countryTexture;
	private Texture cliffCountryTexture;
	private Texture coastlineCountryTexture;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		ImportHeightMapWindow window = (ImportHeightMapWindow) SmartWindow.getSingleInstance("importHeightMap");
		if (window == null) {
			window = new ImportHeightMapWindow(app, skin, true);
			window.setSingleInstance("importHeightMap");
			window.show(stage, true);
		}
		window.toFront();
	}
	
	public ImportHeightMapWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("importHeightMap.title"), skin, Attribute.RESIZABLE);
		this.setAutodispose(autodispose);
		this.setMinSize(368, 290);
		this.app = app;
		this.defaults().pad(2).left();
		
		Table leftCol = new Table();
		leftCol.defaults().padBottom(2);
		leftCol.add(new Label(I18n.tr("importHeightMap.heightmap"), skin)).colspan(3).left().row();
		
		leftCol.add().expandX();
		Button rotateCWButton = new ImageButton(skin, "rotate-right", I18n.tr("importHeightMap.heightmap.rotateCW"), () -> rotateCW());
		leftCol.add(rotateCWButton).size(24);
		Button rotateCCWButton = new ImageButton(skin, "rotate-left", I18n.tr("importHeightMap.heightmap.rotateCCW"), () -> rotateCCW());
		leftCol.add(rotateCCWButton).size(24).row();
		
		leftCol.add().expandX();
		Button reflectHButton = new ImageButton(skin, "reflect-h", I18n.tr("importHeightMap.heightmap.reflectH"), () -> reflectHorizontal());
		leftCol.add(reflectHButton).size(24);
		Button reflectVButton = new ImageButton(skin, "reflect-v", I18n.tr("importHeightMap.heightmap.reflectV"), () -> reflectVertical());
		leftCol.add(reflectVButton).size(24).row();
		
		add(leftCol).fillX().top();
		
		heightmapIcon.setScaling(Scaling.fit);
		Button heightmapButton = new Button(heightmapIcon, skin, "hover");
		heightmapButton.getCell(heightmapIcon).grow();
		heightmapButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				app.getOS().getFileChooser().chooseFile(
		            new NativeFileChooserConfiguration() {{
		                title = I18n.tr("importHeightMap.title");
		                directory = Gdx.files.absolute(System.getProperty("user.home"));
		                nameFilter = (dir, name) -> name.endsWith(".png");
		                mimeFilter = "PNG images/png";
		                intent = NativeFileChooserIntent.OPEN;
		            }},
		            new NativeFileChooserCallback() {
		                @Override
		                public void onFileChosen(FileHandle file) {
		                	try {
		                		setHeightmap(file);
							} catch (Exception e) {
								e.printStackTrace();
								MessageBox.show(getStage(), I18n.tr("importHeightMap.title"), e);
							}
		                }
	
		                @Override
		                public void onCancellation() {}
	
		                @Override
		                public void onError(Exception exception) {
		                    exception.printStackTrace();
		                }
		            }
		        );
			}
		});
		add(heightmapButton).minSize(100).prefSize(270).grow().fill().row();
		
		add(new Label(I18n.tr("importHeightMap.country"), skin));
		Table countriesField = new Table();
		Button countryButton = new Button(countryIcon, skin, "hover");
		countryButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				CountryPicker tmpPicker = new CountryPicker(skin, app.getLand(), true, false);
        		tmpPicker.setCallback(new EventListener() {
					@Override
					public boolean handle(Event event) {
						LNDCountry country = tmpPicker.getSelected();
						if (country != null) {
							setCountry(country);
						}
						return true;
					}
				})
        		.show(getStage(), true);
			}
		});
		countryButton.addListener(new TextTooltip(I18n.tr("importHeightMap.country.normal"), skin));
		countriesField.add(countryButton).size(49, 49).padRight(1);
		Button pickCountryButton = new ImageButton(skin, "tool-pickcolor", I18n.tr("importHeightMap.country.pick"), () -> {
			app.getView3D().pickCoord((coord, button) -> {
				LndFile land = app.getLand();
				LH3DLandCell cell = land.getCellAtCoord(coord.x, coord.z);
				if (cell != LH3DLandCell.EMPTY) {
					setCountry(land.getCountries().get(cell.getCountry()));
				}
			});
		});
		Table buttons = new Table();
		buttons.add(pickCountryButton).size(24, 24).padBottom(1).row();
		Button resetCountryButton = new ImageButton(skin, "tool-delete", I18n.tr("importHeightMap.country.unset"), () -> setCountry(null));
		buttons.add(resetCountryButton).size(24, 24);
		countriesField.add(buttons).padRight(4);
		
		Button cliffCountryButton = new Button(cliffCountryIcon, skin, "hover");
		cliffCountryButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				CountryPicker tmpPicker = new CountryPicker(skin, app.getLand(), true, false);
        		tmpPicker.setCallback(new EventListener() {
					@Override
					public boolean handle(Event event) {
						LNDCountry country = tmpPicker.getSelected();
						if (country != null) {
							setCliffCountry(country);
						}
						return true;
					}
				})
        		.show(getStage(), true);
			}
		});
		cliffCountryButton.addListener(new TextTooltip(I18n.tr("importHeightMap.country.cliff"), skin));
		countriesField.add(cliffCountryButton).size(49, 49).padRight(1);
		Button pickCliffCountryButton = new ImageButton(skin, "tool-pickcolor", I18n.tr("importHeightMap.country.pick"), () -> {
			app.getView3D().pickCoord((coord, button) -> {
				LndFile land = app.getLand();
				LH3DLandCell cell = land.getCellAtCoord(coord.x, coord.z);
				if (cell != LH3DLandCell.EMPTY) {
					setCliffCountry(land.getCountries().get(cell.getCountry()));
				}
			});
		});
		buttons = new Table();
		buttons.add(pickCliffCountryButton).size(24, 24).padBottom(1).row();
		Button resetCliffCountryButton = new ImageButton(skin, "tool-delete", I18n.tr("importHeightMap.country.unset"), () -> setCliffCountry(null));
		buttons.add(resetCliffCountryButton).size(24, 24);
		countriesField.add(buttons).padRight(4);
		
		Button coastlineCountryButton = new Button(coastlineCountryIcon, skin, "hover");
		coastlineCountryButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				CountryPicker tmpPicker = new CountryPicker(skin, app.getLand(), true, false);
        		tmpPicker.setCallback(new EventListener() {
					@Override
					public boolean handle(Event event) {
						LNDCountry country = tmpPicker.getSelected();
						if (country != null) {
							setCoastlineCountry(country);
						}
						return true;
					}
				})
        		.show(getStage(), true);
			}
		});
		coastlineCountryButton.addListener(new TextTooltip(I18n.tr("importHeightMap.country.coastline"), skin));
		countriesField.add(coastlineCountryButton).size(49, 49).padRight(1);
		Button pickCoastlineCountryButton = new ImageButton(skin, "tool-pickcolor", I18n.tr("importHeightMap.country.pick"), () -> {
			app.getView3D().pickCoord((coord, button) -> {
				LndFile land = app.getLand();
				LH3DLandCell cell = land.getCellAtCoord(coord.x, coord.z);
				if (cell != LH3DLandCell.EMPTY) {
					setCoastlineCountry(land.getCountries().get(cell.getCountry()));
				}
			});
		});
		buttons = new Table();
		buttons.add(pickCoastlineCountryButton).size(24, 24).padBottom(1).row();
		Button resetCoastlineCountryButton = new ImageButton(skin, "tool-delete", I18n.tr("importHeightMap.country.unset"), () -> setCoastlineCountry(null));
		buttons.add(resetCoastlineCountryButton).size(24, 24);
		countriesField.add(buttons).padRight(4);
		
		Button countryWizardButton = new ImageButton(skin, "tool-wizard", I18n.tr("importHeightMap.oneClickConfig"), () -> startOneClickConfig());
		countriesField.add(countryWizardButton).size(32, 32).top().row();
		countriesField.add(new Label(I18n.tr("importHeightMap.country.normal"), skin)).colspan(2).center();
		countriesField.add(new Label(I18n.tr("importHeightMap.country.cliff"), skin)).colspan(2).center();
		countriesField.add(new Label(I18n.tr("importHeightMap.country.coastline"), skin)).colspan(2).center();
		countriesField.add();
		add(countriesField).colspan(2).row();
		
		add(new Label(I18n.tr("importHeightMap.cliffSlope"), skin));
		cliffSlopeField = new TextSlider(skin, 45, 0, 89, 1);
		cliffSlopeField.setValueChangeListener((value) -> {
			cliffSlope = MathUtils.tanDeg(value);
		});
		add(cliffSlopeField).right().row();
		
		add(new Label(I18n.tr("importHeightMap.sound"), skin));
		soundField = new SelectBox<>(skin);
		soundField.setItems(SoundOption.values());
		soundField.setSelected(SoundOption.valueOf(sound));
		soundField.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setSound(soundField.getSelected().sound);
			}
		});
		add(soundField).fillX().row();
		
		Table tmpTable = new Table();
		tmpTable.add(new Label(I18n.tr("importHeightMap.alignToGrid"), skin)).padRight(6);
		alignToGridCheckbox = new CheckBox("", skin);
		tmpTable.add(alignToGridCheckbox);
		add(tmpTable).colspan(2).row();
		
		okButton = new TextButton(I18n.tr("dialog.ok"), skin);
		okButton.setDisabled(true);
		okButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (okButton.isDisabled()) return;
				startPositioning();
			}
		});
		add(okButton).colspan(2).minWidth(60).right().padBottom(4);
		
		for (SculptBrush brush : app.getSculptBrushes()) {
			if (country == null && brush.getCountry() != null) {
				setCountry(brush.getCountry());
			}
			if (cliffCountry == null && brush.getCliffCountry() != null) {
				setCliffCountry(brush.getCliffCountry());
			}
			if (coastlineCountry == null && brush.getCoastlineCountry() != null) {
				setCoastlineCountry(brush.getCoastlineCountry());
			}
		}
		
		pack();
		app.listeners.add(appChangeListener);
	}
	
	private final UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.LAND) {
				//cancelPositioning();
				remove();
			}
		}
	};
	
	private void rotateCW() {
		if (heightmap != null) {
			final int srcw = heightmap.length;
			final int srch = heightmap[0].length;
			final int dstw = srch;
			final int dsth = srcw;
			int[][] dst = new int[dstw][dsth];
			for (int x = 0; x < srcw; x++) {
				for (int y = 0; y < srch; y++) {
					dst[y][dsth - 1 - x] = heightmap[x][y];
				}
			}
			setHeightmap(dst);
		}
	}
	
	private void rotateCCW() {
		if (heightmap != null) {
			final int srcw = heightmap.length;
			final int srch = heightmap[0].length;
			final int dstw = srch;
			final int dsth = srcw;
			int[][] dst = new int[dstw][dsth];
			for (int x = 0; x < srcw; x++) {
				for (int y = 0; y < srch; y++) {
					dst[dstw - 1 - y][x] = heightmap[x][y];
				}
			}
			setHeightmap(dst);
		}
	}
	
	private void reflectHorizontal() {
		if (heightmap != null) {
			final int w = heightmap.length;
			final int h = heightmap[0].length;
			int[][] dst = new int[w][h];
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					dst[w - 1 - x][y] = heightmap[x][y];
				}
			}
			setHeightmap(dst);
		}
	}
	
	private void reflectVertical() {
		if (heightmap != null) {
			final int w = heightmap.length;
			final int h = heightmap[0].length;
			int[][] dst = new int[w][h];
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					dst[x][h - 1 - y] = heightmap[x][y];
				}
			}
			setHeightmap(dst);
		}
	}
	
	private boolean checkInput() {
		boolean valid = heightmap != null && country != null && cliffCountry != null && coastlineCountry != null;
		okButton.setDisabled(!valid);
		return valid;
	}
	
	private void startPositioning() {
		if (!checkInput()) return;
		LndFile land = app.getLand();
		HeightMapTool hmapTool = new HeightMapTool(land);
		hmapTool.setCountry(country);
		hmapTool.setCliffCountry(cliffCountry);
		hmapTool.setCliffSlope(cliffSlope);
		hmapTool.setCoastlineCountry(coastlineCountry);
		hmapTool.setDefaultSound(sound);
		LndFile srcLand = hmapTool.createLandFromHeightMap(heightmap, land.getAltitudeBits());
		app.beginDropLand(srcLand, null, alignToGridCheckbox.isChecked(), I18n.tr("action.importHeightmap"), DropEventType.ENTER, null);
		remove();
	}
	
	public void startOneClickConfig() {
		app.showStatusMessage(I18n.tr("importHeightMap.oneClickConfig.hint"));
		app.getView3D().pickCoord((coord, button) -> {
			app.showStatusMessage("");
			LndFile land = app.getLand();
			LH3DLandCell cell = land.getCellAtCoord(coord.x, coord.z);
			if (cell != LH3DLandCell.EMPTY && !cell.isCoastLine()) {
				Vec3f normal = land.getNormal(coord.x, coord.z);
				if (normal.x != 0 || normal.z != 0) {
					final int slopeCountry = cell.getCountry();
					setCliffCountry(land.getCountries().get(slopeCountry));
					Vec3f pos = new Vec3f(coord.x, 0, coord.z);
					Vec3f step = new Vec3f(normal.x, 0, normal.z).nor().scl(LH3DLandCell.CELL_SIZE);
					while (true) {
						pos.sub(step);
						cell = land.getCellAtCoord(pos.x, pos.z);
						if (cell == LH3DLandCell.EMPTY || cell.isCoastLine()) {
							break;
						} else if (cell.getCountry() != slopeCountry) {
							setCountry(land.getCountries().get(cell.getCountry()));
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
							setCoastlineCountry(land.getCountries().get(cell.getCountry()));
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
	
	public void setHeightmap(FileHandle file) {
		if (heightmapTexture != null) {
			heightmapTexture.dispose();
			heightmapTexture = null;
			heightmapIcon.setDrawable(null);
		}
		heightmap = null;
    	Pixmap pixmap = new Pixmap(file);
    	Format format = pixmap.getFormat();
    	//Find bounds
		int xMin = Integer.MAX_VALUE;
		int yMin = Integer.MAX_VALUE;
		int xMax = 0;
		int yMax = 0;
		for (int x = 0; x < pixmap.getWidth(); x++) {
			for (int y = 0; y < pixmap.getHeight(); y++) {
				int p = pixmap.getPixel(x, y);
				int r = (p >> 24) & 0xFF;
				int g = (p >> 16) & 0xFF;
				int b = (p >>  8) & 0xFF;
				int a =  p        & 0xFF;
				int avg = format == Format.Alpha ? a : (r + g + b) / 3;
				if (avg > 0) {
					xMin = Math.min(xMin, x);
					xMax = Math.max(xMax, x);
					yMin = Math.min(yMin, y);
					yMax = Math.max(yMax, y);
				}
			}
		}
		final int w = xMax - xMin + 1;
		final int h = yMax - yMin + 1;
		//Create height map
		if (w > 0 && h > 0) {
			int[][] hmap = new int[w][h];
			for (int x = xMin; x <= xMax; x++) {
				for (int y = yMin; y <= yMax; y++) {
					int p = pixmap.getPixel(x, y);
					int r = (p >> 24) & 0xFF;
					int g = (p >> 16) & 0xFF;
					int b = (p >>  8) & 0xFF;
					int a =  p        & 0xFF;
					int avg = format == Format.Alpha ? a : (r + g + b) / 3;
					hmap[x - xMin][y - yMin] = avg;
				}
			}
			setHeightmap(hmap);
		}
		pixmap.dispose();
		checkInput();
	}
	
	private void setHeightmap(int[][] hmap) {
		this.heightmap = hmap;
		final int w = hmap.length;
		final int h = hmap[0].length;
		int[][] pixels = new int[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				pixels[x][(h - 1) - y] = hmap[x][y];
			}
		}
		if (heightmapTexture != null) heightmapTexture.dispose();
		heightmapTexture = Utils.toTexture(pixels, 1, true);
		heightmapTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		heightmapIcon.setDrawable(new TextureRegionDrawable(heightmapTexture));
		checkInput();
	}
	
	private void setCountry(LNDCountry country) {
		this.country = country;
		if (countryTexture != null) countryTexture.dispose();;
		countryTexture = createCountryTexture(country);
		setTexture(countryIcon, countryTexture);
		checkInput();
	}
	
	private void setCliffCountry(LNDCountry cliffCountry) {
		this.cliffCountry = cliffCountry;
		if (cliffCountryTexture != null) cliffCountryTexture.dispose();;
		cliffCountryTexture = createCountryTexture(cliffCountry);
		setTexture(cliffCountryIcon, cliffCountryTexture);
		checkInput();
	}
	
	private void setCoastlineCountry(LNDCountry coastlineCountry) {
		this.coastlineCountry = coastlineCountry;
		if (coastlineCountryTexture != null) coastlineCountryTexture.dispose();;
		coastlineCountryTexture = createCountryTexture(coastlineCountry);
		setTexture(coastlineCountryIcon, coastlineCountryTexture);
		checkInput();
	}
	
	private void setSound(Sound sound) {
		this.sound = sound;
	}
	
	private void setTexture(Image image, Texture texture) {
		image.setDrawable(texture == null ? null : new TextureRegionDrawable(texture));
	}
	
	private Texture createCountryTexture(LNDCountry country) {
		if (country == null) return null;
		return Utils.toTexture(app.getDefaultCountryPreviewGenerator().generatePreview(country, 49, 49), 49, 49, true);
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		if (countryTexture != null) countryTexture.dispose();;
		if (cliffCountryTexture != null) cliffCountryTexture.dispose();
		if (coastlineCountryTexture != null) coastlineCountryTexture.dispose();
		app.listeners.remove(appChangeListener);
		super.dispose();
	}
}
