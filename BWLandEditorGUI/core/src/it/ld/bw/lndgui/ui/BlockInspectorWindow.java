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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

import it.ld.bw.lnd.model.LH3DLandBlock;
import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.gfx.Coord;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.utils.UChangeListener;

public class BlockInspectorWindow extends SmartWindow {
	private final MainApp app;
	
	private final Label blockLabel;
	private final CheckBox altitudeCB;
	private final CheckBox countryCB;
	private final CheckBox flagsCB;
	private final CheckBox soundCB;
	
	private final Table[][] cellsTables = new Table[LH3DLandBlock.POINTS_PER_SIDE][LH3DLandBlock.POINTS_PER_SIDE];
	private final Label[][][] cellLabels = new Label[LH3DLandBlock.POINTS_PER_SIDE][LH3DLandBlock.POINTS_PER_SIDE][4];
	
	private LndFile land = null;
	private LH3DLandBlock block = null;
	
	private final Drawable emptyBackground;
	private final Drawable waterBackground;
	private final Drawable coastBackground;
	private final Drawable dryBackground;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		BlockInspectorWindow window = (BlockInspectorWindow) SmartWindow.getSingleInstance("blockInspector");
		if (window == null) {
			window = new BlockInspectorWindow(app, skin, true);
			window.setSingleInstance("blockInspector");
			window.show(stage, stage.getWidth() - window.getWidth(), 28, false);
		}
		window.toFront();
	}
	
	public BlockInspectorWindow(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("blockInspector.title"), skin);
		this.setAutodispose(autodispose);
		this.app = app;
		
		setMinSize(165, 180);
		setSize(180, 250);
		
		emptyBackground = skin.getDrawable("button");
		waterBackground = skin.newDrawable("white", new Color(0.0f, 0.3f, 0.4f, 1f));
		coastBackground = skin.newDrawable("white", new Color(0.6f, 0.5f, 0.0f, 1f));
		dryBackground = skin.newDrawable("white", new Color(0.3f, 0.2f, 0.0f, 1f));
		
		Table firstRow = new Table();
		firstRow.defaults().padRight(6);
		
		firstRow.add(new Label(I18n.tr("blockInspector.block"), skin));
		blockLabel = new Label("", skin);
		firstRow.add(blockLabel);
		firstRow.add().expandX();
		firstRow.add(new Label(I18n.tr("blockInspector.attributes"), skin));
		
		altitudeCB = new CheckBox(I18n.tr("blockInspector.altitude"), skin);
		altitudeCB.setChecked(true);
		altitudeCB.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				update();
			}
		});
		firstRow.add(altitudeCB);
		
		flagsCB = new CheckBox(I18n.tr("blockInspector.flags"), skin);
		flagsCB.setChecked(true);
		flagsCB.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				update();
			}
		});
		firstRow.add(flagsCB);
		
		countryCB = new CheckBox(I18n.tr("blockInspector.country"), skin);
		countryCB.setChecked(true);
		countryCB.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				update();
			}
		});
		firstRow.add(countryCB);
		
		soundCB = new CheckBox(I18n.tr("blockInspector.sound"), skin);
		soundCB.setChecked(true);
		soundCB.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				update();
			}
		});
		firstRow.add(soundCB).left();
		
		add(firstRow).fillX().row();
		
		Table cellsTable = new Table();
		cellsTable.background(skin.newDrawable("white", Color.LIGHT_GRAY));
		cellsTable.defaults().size(46).pad(0.5f);
		for (int row = 0; row < LH3DLandBlock.POINTS_PER_SIDE; row++) {
			for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
				Table cellTable = new Table();
				cellTable.setBackground(emptyBackground);
				
				Label label = new Label("", skin);
				label.setAlignment(Align.center);
				cellLabels[cx][LH3DLandBlock.SEG_PER_SIDE - row][0] = label;
				cellTable.add(label).padRight(2);
				
				label = new Label("", skin);
				label.setAlignment(Align.center);
				cellLabels[cx][LH3DLandBlock.SEG_PER_SIDE - row][1] = label;
				cellTable.add(label);
				
				cellTable.row();
				
				label = new Label("", skin);
				label.setAlignment(Align.center);
				cellLabels[cx][LH3DLandBlock.SEG_PER_SIDE - row][2] = label;
				cellTable.add(label).padRight(2);
				
				label = new Label("", skin);
				label.setAlignment(Align.center);
				cellLabels[cx][LH3DLandBlock.SEG_PER_SIDE - row][3] = label;
				cellTable.add(label);
				
				cellsTable.add(cellTable);
				
				cellsTables[cx][LH3DLandBlock.SEG_PER_SIDE - row] = cellTable;
			}
			cellsTable.row();
		}
		add(cellsTable).grow();
		
		app.getView3D().addListener(viewListener);
		app.listeners.add(appChangeListener);
		setLand(app.getLand());
		pack();
	}
	
	private final InputListener viewListener = new InputListener() {
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			Coord coord = app.getView3D().getCursorCoord();
			if (land != null && coord != null) {
				setBlock(land.getBlockAtCoord(coord.x, coord.z, false));
			}
			return false;
		};
	};
	
	private final UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.LAND) {
				setLand(app.getLand());
			}
		}
	};
	
	private void setLand(LndFile land) {
		setBlock(null);
		if (this.land != null) {
			this.land.listeners.remove(landChangeListener);
		}
		this.land = land;
		if (land != null) {
			land.listeners.add(landChangeListener);
		}
	}
	
	private final UChangeListener landChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LndFile.Property.BLOCKS) {
				if (event.getType() == EventType.CHANGE) {
					if (block == event.getNewValue()) {
						update();
					}
				} else if (event.getType() == EventType.REMOVE) {
					if (block == event.getOldValue()) {
						setBlock(null);
					}
				}
			}
		}
	};
	
	private void setBlock(LH3DLandBlock block) {
		if (block != this.block) { 
			if (this.block != null) {
				
			}
			this.block = block;
			if (block != null) {
				
			}
			update();
		}
	}
	
	private void update() {
		if (block != null) {
			blockLabel.setText(block.getBlockX() + ", " + block.getBlockZ());
			boolean showAltitude = altitudeCB.isChecked();
			boolean showCountry = countryCB.isChecked();
			boolean showFlags = flagsCB.isChecked();
			boolean showSound = soundCB.isChecked();
			for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
				for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
					LH3DLandCell cell = block.getCell(cx, cz);
					if (showAltitude) {
						cellLabels[cx][cz][0].setText(cell.getAltitude());
					} else {
						cellLabels[cx][cz][0].setText("");
					}
					if (showFlags) {
						String flags = "";
						if (cell.isFullWater()) flags += "F";
						if (cell.hasWater()) flags += "W";
						if (cell.isCoastLine()) flags += "C";
						if (cell.isTransparent()) flags += "T";
						cellLabels[cx][cz][1].setText(flags);
					} else {
						cellLabels[cx][cz][1].setText("");
					}
					if (showCountry) {
						cellLabels[cx][cz][2].setText(cell.getCountry());
					} else {
						cellLabels[cx][cz][2].setText("");
					}
					if (showSound) {
						cellLabels[cx][cz][3].setText(cell.getSound());
					} else {
						cellLabels[cx][cz][3].setText("");
					}
					
					if (cell.hasWater()) {
						cellsTables[cx][cz].setBackground(waterBackground);
					} else if (cell.isCoastLine()) {
						cellsTables[cx][cz].setBackground(coastBackground);
					} else {
						cellsTables[cx][cz].setBackground(dryBackground);
					}
				}
			}
		} else {
			blockLabel.setText("");
			for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
				for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
					cellLabels[cx][cz][0].setText("");
					cellLabels[cx][cz][1].setText("");
					cellLabels[cx][cz][2].setText("");
					cellLabels[cx][cz][3].setText("");
					cellsTables[cx][cz].setBackground(emptyBackground);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		app.listeners.remove(appChangeListener);
		app.getView3D().removeListener(viewListener);
		setLand(null);
		super.dispose();
	}
}
