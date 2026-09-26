package it.ld.bw.lndgui.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.tools.CountryPreviewGenerator;
import it.ld.bw.lndgui.I18n;
import it.ld.libgdx.ui.components.GridView;
import it.ld.libgdx.ui.components.GridView.GridModel;
import it.ld.libgdx.ui.components.GridView.ItemClickEvent;
import it.ld.libgdx.ui.components.GridView.ItemClickListener;
import it.ld.libgdx.ui.components.GridView.State;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.utils.Utils;
import it.ld.utils.MathUtils;
import it.ld.utils.UChangeListener;

public class CountryPicker extends SmartWindow {
	private LndFile land;
	
	private final GridView<LNDCountry> grid;
	private Drawable[] images;
	private Texture atlasTexture;
	
	private EventListener callback;
	
	private Styler styler;
	
	public CountryPicker(Skin skin, LndFile land, boolean autodispose) {
		this(skin, land, autodispose, false);
	}
	
	public CountryPicker(Skin skin, LndFile land, boolean autodispose, boolean multiselect) {
		super(I18n.tr("countryPicker.title"), skin);
		this.setAutodispose(autodispose);
		this.land = land;
		land.listeners.add(countriesChangeListener);
		
		grid = new GridView<LNDCountry>(skin)
		.setIconSize(128)
		.setMinCols(1)
		.setModel(new GridModel<LNDCountry>() {
			@Override
			public void render(int index, LNDCountry item, Container<Actor> container, Image image, Label label, State state) {
				image.setDrawable(images[index]);
				label.setText(item.toString());
				if (styler != null) {
					styler.process(index, item, state);
				}
			}
		});
		add(grid).grow().pad(10).row();
		
		if (multiselect) {
			TextButton selectButton = new TextButton(I18n.tr("dialog.select"), skin);
			selectButton.setDisabled(true);
			selectButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if (callback != null) {
						event.handle();
						callback.handle(new Event());
						remove();
					}
				}
			});
			add(selectButton).right().pad(0, 0, 10, 10);
			
			grid.getSelection().setMultiple(true);
			grid.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if (selectButton != null) {
						selectButton.setDisabled(grid.getSelection().isEmpty());
					}
				}
			});
		} else {
			grid.addListener(new ItemClickListener<LNDCountry>() {
				@Override
				public void clicked(ItemClickEvent<LNDCountry> event, int index, LNDCountry item) {
					if (callback != null) {
						event.handle();
						callback.handle(new Event());
						remove();
					}
				}
			});
		}
		
		updateCountries();
		pack();
	}
	
	public CountryPicker setStyler(Styler styler) {
		this.styler = styler;
		this.grid.refresh();
		return this;
	}
	
	public CountryPicker setCallback(EventListener callback) {
		this.callback = callback;
		return this;
	}
	
	public ArraySelection<LNDCountry> getSelection() {
		return grid.getSelection();
	}
	
	public int getSelectedIndex() {
		return this.grid.getSelectedIndex();
	}
	
	public LNDCountry getSelected() {
		return this.grid.getSelected();
	}
	
	private final UChangeListener countriesChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LndFile.Property.COUNTRIES) {
				updateCountries();
			} else if (event.getProperty() == LndFile.Property.NOISE_MAP) {
				updateCountries();
			}
		}
	};
	
	private void updateCountries() {
		//Cleanup
		if (atlasTexture != null) {
			atlasTexture.dispose();
		}
		//
		try (CountryPreviewGenerator generator = new CountryPreviewGenerator(land);) {
			final int tile = 128;
			Array<LNDCountry> countries = new Array<>(land.getCountries().toArray(new LNDCountry[0]));
			int count = countries.size;
			int radix = Math.max(1, (int)Math.ceil(Math.sqrt(count)));
			int nx = Math.max(1, count / radix);
			int ny = Math.max(1, MathUtils.ceilDiv(count, nx));
			Pixmap sheet = new Pixmap(nx * tile, ny * tile, Pixmap.Format.RGB888);
			for (int i = 0; i < count; i++) {
				Pixmap pixmap = Utils.toPixmap(generator.generatePreview(countries.get(i), 256, 256), 256, 256);
				int x = (i % nx) * tile;
		        int y = (i / nx) * tile;
		        sheet.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), x, y, tile, tile);
		        pixmap.dispose();
			}
			
			atlasTexture = new Texture(sheet);
		    sheet.dispose();
	
		    images = new Drawable[count];
		    for (int i = 0; i < count; i++) {
		        int x = (i % nx) * tile;
		        int y = (i / nx) * tile;
		        images[i] = new TextureRegionDrawable(new TextureRegion(atlasTexture, x, y, tile, tile));
		    }
		    
		    grid.setItems(countries);
		}
	    
		pack();
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		if (this.atlasTexture != null) {
			this.atlasTexture.dispose();
		}
		land.listeners.remove(countriesChangeListener);
		super.dispose();
	}
	
	
	public static class Styler {
		public void process(int index, LNDCountry item, State state) {}
	}
}
