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

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.tools.CountryPreviewGenerator;
import it.ld.bw.lnd.tools.LandTool;
import it.ld.bw.lnd.tools.LandTool.CountryMatch;
import it.ld.bw.lndgui.CountryMapping;
import it.ld.bw.lndgui.DropEventType;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.libgdx.ui.components.ListTable;
import it.ld.libgdx.ui.components.MessageBox;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.GridView.State;
import it.ld.libgdx.ui.components.ImageButton;
import it.ld.libgdx.ui.components.ListTable.ItemClickEvent;
import it.ld.libgdx.ui.components.ListTable.ItemClickListener;
import it.ld.libgdx.utils.Utils;
import it.ld.utils.MathUtils;
import it.ld.utils.UChangeListener;

public class ImportLandWindow extends SmartWindow {
	private static final int THUMBW = 48;
	private static final int THUMBH = 64;
	
	private final MainApp app;
	
	private final Label filenameField;
	private final Button automatchButton;
	private final ListTable<CountryMapping> countryList;
	private final Label usedCountriesLabel;
	private final CheckBox rememberMappingsCheckbox;
	private final CheckBox alignToGridCheckbox;
	private final Button okButton;
	
	private LndFile srcLand = null;
	private final Array<CountryMapping> mappings = new Array<>(0);
	private int usedCountries = 0;
	
	private CountryPreviewGenerator srcCountryPreviewGenerator;
	private Array<Drawable> srcThumbs;
	private Texture srcAtlas;
	private Array<Drawable> dstThumbs;
	private Texture dstAtlas;
	
	public static ImportLandWindow showSingleInstance(MainApp app, Stage stage, Skin skin) {
		ImportLandWindow window = (ImportLandWindow) SmartWindow.getSingleInstance("importLand");
		if (window == null) {
			window = new ImportLandWindow(app, skin, true);
			window.setSingleInstance("importLand");
			window.show(stage, true);
			window.setSize(500, 520);
		}
		window.toFront();
		return window;
	}
	
	public ImportLandWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("importLand.title"), skin, Attribute.RESIZABLE);
		this.setAutodispose(autodispose);
		this.setMinSize(300, 350);
		this.app = app;
		this.defaults().pad(2).left();
		
		ClickListener filenameListener = new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				app.getOS().getFileChooser().chooseFile(
		            new NativeFileChooserConfiguration() {{
		                title = I18n.tr("importLand.title");
		                directory = Gdx.files.absolute(System.getProperty("user.home"));
		                nameFilter = (dir, name) -> name.endsWith(".lnd");
		                mimeFilter = "Land files/lnd";
		                intent = NativeFileChooserIntent.OPEN;
		            }},
		            new NativeFileChooserCallback() {
		                @Override
		                public void onFileChosen(FileHandle file) {
		                	try {
		                		LndFile srcLand = LndFile.load(file.file(), true);
		                		setSrcLand(srcLand, null);
							} catch (Exception e) {
								e.printStackTrace();
								MessageBox.show(getStage(), I18n.tr("importLand.title"), e);
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
		};
		
		add(new Label(I18n.tr("importLand.file"), skin));
		Table fileField = new Table();
		filenameField = new Label("", skin, "textfield");
		filenameField.addListener(filenameListener);
		fileField.add(filenameField).growX().padRight(1);
		Button chooseFileButton = new TextButton("...", skin);
		chooseFileButton.addListener(filenameListener);
		fileField.add(chooseFileButton);
		add(fileField).growX().row();
		
		Table tmpTable = new Table();
		tmpTable.add(new Label(I18n.tr("importLand.countryMapping"), skin)).growX();
		automatchButton = new ImageButton(skin, "tool-magic-hat", I18n.tr("importLand.automatch"), () -> automatch());
		automatchButton.setDisabled(true);
		tmpTable.add(automatchButton).size(24).right();
		add(tmpTable).colspan(2).fillX().row();
		
		countryList = new ListTable<CountryMapping>(skin)
		.setCellPadding(2)
		.setModel(new ListTable.DefaultModel<CountryMapping>(skin) {
			@Override
			public String[] getHeaderNames() {
				return new String[] {
					I18n.tr("importLand.countryMapping.src"),
					I18n.tr("importLand.countryMapping.used"),
					I18n.tr("importLand.countryMapping.import"),
					I18n.tr("importLand.countryMapping.mapTo"),
					""	//Filler
				};
			}
			
			@Override
			public Actor createField(int row, int col) {
				if (col == 0) {
					Image image = new Image();
					image.setScaling(Scaling.fit);
					image.setSize(THUMBW, THUMBH);
					return image;
				} else if (col == 2) {
					return new CheckBox("", skin);
				} else if (col == 3) {
					Image image = new Image();
					image.setScaling(Scaling.fit);
					image.setSize(THUMBW, THUMBH);
					return new Button(image, skin, "hover");
				}
				return super.createField(row, col);
			}
			
			@Override
			public int[] getAlignments(int[] alignments) {
				alignments[1] = Align.right;	//Usage field
				return alignments;
			}
			
			@Override
			public void render(int row, CountryMapping mapping, Cell<Container<Actor>>[] fields) {
				fields[0].width(60);
				fields[1].minWidth(60);
				fields[2].width(60);
				fields[3].width(60);
				fields[4].expandX();	//Filler
				Image fSrc = ((Image)fields[0].getActor().getActor());
				Label fUsed = ((Label)fields[1].getActor().getActor());
				CheckBox fImport = ((CheckBox)fields[2].getActor().getActor());
				Image fMapTo = (Image)((Button)fields[3].getActor().getActor()).getChild(0);
				//
				int total = srcLand.getTotalCells();
				int used = mapping.srcCountry.getIndex() >= 0 ? srcLand.getCellsPerCountry(mapping.srcCountry.getIndex()) : -1;
				int percent = Math.round((float)used / total * 100f);
				String pUsed = used == -1 ? "?" : ((used > 0 && percent == 0) ? "<1%" : percent + "%");
				//Content
				fSrc.setDrawable(srcThumbs.get(mapping.srcCountry.getIndex()));
				fUsed.setText(pUsed);
				fImport.setChecked(mapping.dstCountry == null);
				fMapTo.setDrawable(mapping.dstCountry == null ? null : dstThumbs.get(mapping.dstCountry.getIndex()));
			}
		})
		.setHeaderVisible(true)
		.setRowsBackground(null, null);
		countryList.addListener(new ItemClickListener<CountryMapping>() {
			@Override
			public void clicked(ItemClickEvent<CountryMapping> event, int row, int col, CountryMapping item) {
				if (col == 2) {
					item.dstCountry = null;
					countryList.refresh(row);
					updateUsedCountries();
				} else if (col == 3) {
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
								item.dstCountry = country;
								countryList.refresh(row);
								updateUsedCountries();
							}
							return true;
						}
					})
	        		.show(getStage(), true);
				}
			}
		});
		add(countryList).colspan(2).minSize(200, 150).grow().row();
		
		usedCountriesLabel = new Label("", skin);
		add(usedCountriesLabel).colspan(2).row();
		
		tmpTable = new Table();
		rememberMappingsCheckbox = new CheckBox(I18n.tr("importLand.rememberMappings"), skin);
		rememberMappingsCheckbox.setChecked(true);
		tmpTable.add(rememberMappingsCheckbox).padRight(6);
		Button clearButton = new TextButton(I18n.tr("importLand.clearDecisions"), skin);
		clearButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				app.forgetCountryMappings();
			}
		});
		tmpTable.add(clearButton);
		add(tmpTable).colspan(2).row();
		
		alignToGridCheckbox = new CheckBox(I18n.tr("importLand.alignToGrid"), skin);
		add(alignToGridCheckbox).colspan(2).row();
		
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
		
		updateDstThumbs();
		updateUsedCountries();
		pack();
		app.listeners.add(appChangeListener);
	}
	
	private final UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.LAND) {
				remove();
			}
		}
	};
	
	public ImportLandWindow setSrcLand(LndFile srcLand, String srcName) {
		if (srcCountryPreviewGenerator != null) {
			srcCountryPreviewGenerator.close();
			srcCountryPreviewGenerator = null;
		}
		this.srcLand = srcLand;
		if (srcLand != null) {
			if (srcName == null) {
				srcName = srcLand.getFile() == null ? "<unnamed>" : Utils.ellipsis(srcLand.getFile(), 2);
			}
			filenameField.setText(srcName);
			srcCountryPreviewGenerator = new CountryPreviewGenerator(srcLand);
			updateSrcThumbs();
			mappings.clear();
			mappings.ensureCapacity(srcLand.getCountries().size());
			for (LNDCountry srcCountry : srcLand.getCountries()) {
				CountryMapping mapping = new CountryMapping(srcCountry);
				mapping.dstCountry = app.getPreferredCountryMapping(srcCountry);
				mappings.add(mapping);
			}
			countryList.setItems(mappings);
		} else {
			mappings.clear();
			countryList.clear();
		}
		automatchButton.setDisabled(srcLand == null || app.getLand().getCountries().isEmpty());
		updateUsedCountries();
		return this;
	}
	
	public void setMappings(Iterable<CountryMapping> mappings) {
		for (CountryMapping ref : mappings) {
			if (ref.srcCountry.getLand() != this.srcLand) {
				throw new IllegalArgumentException("The input mapping doesn't reference the source land");
			}
			CountryMapping mapping = this.mappings.get(ref.srcCountry.getIndex());
			if (mapping.srcCountry != ref.srcCountry) {
				throw new IllegalArgumentException("The input mapping doesn't reference the source country");
			}
			mapping.dstCountry = ref.dstCountry;
		}
		countryList.refresh();
		updateUsedCountries();
	}
	
	public void automatch() {
		LndFile dstLand = app.getLand();
		List<LNDCountry> dstCountries = dstLand.getCountries();
		CountryMatch[] matches = LandTool.matchCountries(srcLand, dstLand);
		for (int i = 0; i < mappings.size; i++) {
			CountryMapping mapping = mappings.get(i);
			CountryMatch match = matches[i];
			assert(mapping.srcCountry.getIndex() == match.srcCountry);
			LNDCountry dstCountry = app.getPreferredCountryMapping(mapping.srcCountry);
			if (dstCountry != null) {
				mapping.dstCountry = dstCountry;
			} else {
				mapping.dstCountry = match.distance < 0.1f ? dstCountries.get(match.dstCountry) : null;
			}
		}
		countryList.refresh();
		updateUsedCountries();
	}
	
	private void updateUsedCountries() {
		usedCountries = app.getLand().getCountries().size();
		for (CountryMapping mapping : mappings) {
			if (mapping.dstCountry == null) usedCountries++;
		}
		usedCountriesLabel.setText(I18n.tr("importLand.usedCountries", usedCountries, LndFile.MAX_COUNTRIES));
		usedCountriesLabel.setColor(usedCountries <= LndFile.MAX_COUNTRIES ? Color.WHITE : Color.RED);
		checkInput();
	}
	
	private void updateSrcThumbs() {
		//Cleanup
		if (srcAtlas != null) {
			srcAtlas.dispose();
		}
		//
		List<LNDCountry> countries = srcLand != null ? srcLand.getCountries() : new ArrayList<>(0);
		int count = countries.size();
		int radix = Math.max(1, (int)Math.ceil(Math.sqrt(count)));
		int nx = Math.max(1, count / radix);
		int ny = Math.max(1, MathUtils.ceilDiv(count, nx));
		Pixmap sheet = new Pixmap(nx * THUMBW, ny * THUMBH, Pixmap.Format.RGB888);
		for (int i = 0; i < count; i++) {
			Pixmap pixmap = Utils.toPixmap(srcCountryPreviewGenerator.generatePreview(countries.get(i), 256, 512), 256, 512);
			int x = (i % nx) * THUMBW;
	        int y = (i / nx) * THUMBH;
	        sheet.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), x, y, THUMBW, THUMBH);
	        pixmap.dispose();
		}
		
		srcAtlas = new Texture(sheet);
	    sheet.dispose();

	    srcThumbs = new Array<>(count);
	    for (int i = 0; i < count; i++) {
	        int x = (i % nx) * THUMBW;
	        int y = (i / nx) * THUMBH;
	        srcThumbs.add(new TextureRegionDrawable(new TextureRegion(srcAtlas, x, y, THUMBW, THUMBH)));
	    }
	}
	
	private void updateDstThumbs() {
		//Cleanup
		if (dstAtlas != null) {
			dstAtlas.dispose();
		}
		//
		LndFile dstLand = app.getLand();
		List<LNDCountry> countries = dstLand != null ? dstLand.getCountries() : new ArrayList<>(0);
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
		
		dstAtlas = new Texture(sheet);
	    sheet.dispose();

	    dstThumbs = new Array<>(count);
	    for (int i = 0; i < count; i++) {
	        int x = (i % nx) * THUMBW;
	        int y = (i / nx) * THUMBH;
	        dstThumbs.add(new TextureRegionDrawable(new TextureRegion(dstAtlas, x, y, THUMBW, THUMBH)));
	    }
	}
	
	private boolean checkInput() {
		boolean valid = srcLand != null && usedCountries <= LndFile.MAX_COUNTRIES;
		okButton.setDisabled(!valid);
		return valid;
	}
	
	private void startPositioning() {
		if (rememberMappingsCheckbox.isChecked()) {
			for (CountryMapping mapping : mappings) {
				if (mapping.dstCountry != null) {
					app.rememberCountryMapping(mapping.srcCountry, mapping.dstCountry);
				}
			}
		}
		app.beginDropLand(srcLand, mappings, alignToGridCheckbox.isChecked(), I18n.tr("action.importLand"), DropEventType.ENTER, null);
		remove();
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		app.listeners.remove(appChangeListener);
		if (srcCountryPreviewGenerator != null) {
			srcCountryPreviewGenerator.close();
		}
		if (srcAtlas != null) srcAtlas.dispose();
		if (dstAtlas != null) dstAtlas.dispose();
		super.dispose();
	}
}
