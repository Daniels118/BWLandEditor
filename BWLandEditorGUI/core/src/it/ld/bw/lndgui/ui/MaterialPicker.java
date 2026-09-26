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

import it.ld.bw.lnd.model.LNDMaterial;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lndgui.I18n;
import it.ld.libgdx.ui.components.GridView;
import it.ld.libgdx.ui.components.GridView.ItemClickEvent;
import it.ld.libgdx.ui.components.GridView.ItemClickListener;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.components.GridView.GridModel;
import it.ld.libgdx.ui.components.GridView.State;
import it.ld.libgdx.ui.events.RemoveEvent;

import it.ld.libgdx.utils.Utils;
import it.ld.utils.MathUtils;
import it.ld.utils.UChangeListener;

public class MaterialPicker extends SmartWindow {
	private LndFile land;
	
	private final GridView<LNDMaterial> grid;
	private Drawable[] images;
	private Texture atlasTexture;
	
	private EventListener callback;
	
	public MaterialPicker(Skin skin, LndFile land, boolean autodispose) {
		this(skin, land, autodispose, false);
	}
	
	public MaterialPicker(Skin skin, LndFile land, boolean autodispose, boolean multiselect) {
		super(I18n.tr("materialPicker.title"), skin, Attribute.RESIZABLE);
		this.setAutodispose(autodispose);
		this.land = land;
		land.listeners.add(materialsChangeListener);
		
		grid = new GridView<LNDMaterial>(skin)
		.setIconSize(128)
		.setMinCols(1)
		.setModel(new GridModel<LNDMaterial>() {
			@Override
			public void render(int index, LNDMaterial item, Container<Actor> container, Image image, Label label, State state) {
				image.setDrawable(images[index]);
				label.setText(item.toString());
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
			grid.addListener(new ItemClickListener<LNDMaterial>() {
				@Override
				public void clicked(ItemClickEvent<LNDMaterial> event, int index, LNDMaterial item) {
					if (callback != null) {
						event.handle();
						callback.handle(new Event());
						remove();
					}
				}
			});
		}
		
		updateMaterials();
		pack();
	}
	
	public MaterialPicker setCallback(EventListener callback) {
		this.callback = callback;
		return this;
	}
	
	public ArraySelection<LNDMaterial> getSelection() {
		return this.grid.getSelection();
	}
	
	public int getSelectedIndex() {
		return this.grid.getSelectedIndex();
	}
	
	public LNDMaterial getSelected() {
		return this.grid.getSelected();
	}
	
	private final UChangeListener materialsChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LndFile.Property.MATERIALS) {
				updateMaterials();
			}
		}
	};
	
	private void updateMaterials() {
		//Cleanup
		if (atlasTexture != null) {
			atlasTexture.dispose();
		}
		//
		final int tile = 128;
		Array<LNDMaterial> materials = new Array<>(land.getMaterials().toArray(new LNDMaterial[0]));
		int count = materials.size;
		int radix = (int)Math.ceil(Math.sqrt(count));
		int nx = count / radix;
		int ny = MathUtils.ceilDiv(count, nx);
		Pixmap sheet = new Pixmap(nx * tile, ny * tile, Pixmap.Format.RGB888);
		int i = 0;
		for (LNDMaterial lndmat : materials) {
			Pixmap pixmap = Utils.toPixmap(lndmat.getIntARGB(), LNDMaterial.width, LNDMaterial.height);
			int x = (i % nx) * tile;
	        int y = (i / nx) * tile;
	        sheet.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), x, y, tile, tile);
	        pixmap.dispose();
	        i++;
		}
		
		atlasTexture = new Texture(sheet);
	    sheet.dispose();

	    images = new Drawable[count];
	    for (i = 0; i < count; i++) {
	        int x = (i % nx) * tile;
	        int y = (i / nx) * tile;
	        images[i] = new TextureRegionDrawable(new TextureRegion(atlasTexture, x, y, tile, tile));
	    }
	    
	    grid.setItems(materials);
	    
		pack();
	}
	
	@Override
	public boolean remove() {
		boolean removed = super.remove();
		if (removed) {
			this.notify(new RemoveEvent(this), false);
		}
		return removed;
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
		land.listeners.remove(materialsChangeListener);
		super.dispose();
	}
}
