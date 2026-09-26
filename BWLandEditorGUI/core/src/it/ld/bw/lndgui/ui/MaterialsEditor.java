package it.ld.bw.lndgui.ui;

import java.io.IOException;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import it.ld.bw.lnd.model.LNDMaterial;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.TerrainMaterialType;
import it.ld.bw.lnd.tools.LandTool;
import it.ld.bw.lnd.tools.MaterialTool;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.ui.TextureEditor.ApplyEvent;
import it.ld.bw.lndgui.ui.TextureEditor.PreviewChangedEvent;
import it.ld.libgdx.ui.components.GridView;
import it.ld.libgdx.ui.components.MessageBox;
import it.ld.libgdx.ui.components.PopupMenu;
import it.ld.libgdx.ui.components.GridView.GridModel;
import it.ld.libgdx.ui.components.GridView.ItemClickEvent;
import it.ld.libgdx.ui.components.GridView.ItemClickListener;
import it.ld.libgdx.ui.components.GridView.State;
import it.ld.libgdx.ui.components.MenuItemAction;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.events.RemoveEvent;

import it.ld.libgdx.utils.Utils;
import it.ld.utils.MathUtils;
import it.ld.utils.UChangeListener;

public class MaterialsEditor extends SmartWindow {
	private final MainApp app;
	private LndFile land;
	
	private final GridView<LNDMaterial> grid;
	private final SelectBox<TerrainMaterialType> typeField;
	private Table tools = new Table();
	private Table commands = new Table();
	private Container<TextButton> actionButtonContainer = new Container<>();
	private TextButton editButton;
	private TextButton selectButton;
	private Drawable[] images;
	private Texture atlasTexture;
	
	private EventListener callback;
	
	private boolean isEditing = true;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		MaterialsEditor window = (MaterialsEditor) SmartWindow.getSingleInstance("materialsEditor");
		if (window == null) {
			window = new MaterialsEditor(app, skin, true);
			window.setDockingManager(app.getDockingManager());
			window.setSingleInstance("materialsEditor");
			window.show(stage, 54, 28, false);
		} else {
			window.toFront();
		}
	}
	
	public MaterialsEditor(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("materialsEditor.edit.title"), skin, Attribute.RESIZABLE, Attribute.DOCKABLE);
		this.setAutodispose(autodispose);
		this.app = app;
		
		grid = new GridView<LNDMaterial>(skin)
		.setIconSize(128)
		.setMinCols(1)
		.setModel(new GridModel<LNDMaterial>() {
			@Override
			public void render(int index, LNDMaterial item, Container<Actor> container, Image image, Label label, State state) {
				boolean used = land.isMaterialInUse(index);
				image.setDrawable(images[index]);
				label.setText(item.toString());
				label.setColor(used ? Color.WHITE : Color.GRAY);
			}
		});
		grid.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int index = grid.getSelectedIndex();
				selectButton.setDisabled(index < 0);
				if (index >= 0) {
					typeField.setSelected(land.getMaterials().get(index).getMaterialType());
				} else {
					typeField.setSelectedIndex(0);
				}
			}
		});
		grid.addListener(new ItemClickListener<LNDMaterial>() {
			@Override
			public void clicked(ItemClickEvent<LNDMaterial> event, int index, LNDMaterial material) {
				event.handle();
				InputEvent click = event.getSource();
				if (isEditing) {
					if (click.getButton() == Buttons.RIGHT) {
						LinkedList<MenuItemAction> actions = new LinkedList<>();
						actions.add(new MenuItemAction("editTexture", I18n.tr("materialsEditor.materials.editTexture"), () -> {
							TextureEditor editor = new TextureEditor(app, skin, true)
							.setTexture(material.getByteRGBA(), LNDMaterial.width, LNDMaterial.height);
							editor.addListener(new EventListener() {
	    						@Override
	    						public boolean handle(Event event2) {
	    							if (event2 instanceof ChangeEvent && !editor.isCanceled() && editor.isPreviewEnabled()) {
	    								material.setRGBA(editor.getModified());
	    							} else if (event2 instanceof ApplyEvent) {
	    								material.setRGBA(editor.getModified());
	    							} else if (event2 instanceof PreviewChangedEvent) {
	    								material.setRGBA(editor.isPreviewEnabled() ? editor.getModified() : editor.getOriginal());
	    							} else if (event2 instanceof RemoveEvent) {
	    								material.setRGBA(editor.getOriginal());
	    							}
	    							return true;
	    						}
	    					});
							editor.show(getStage());
	                    }));
						actions.add(new MenuItemAction("duplicate", I18n.tr("materialsEditor.materials.duplicate"), () -> {
							LNDMaterial newMaterial = material.clone();
							app.getLand().getMaterials().add(newMaterial);
	                    }));
						actions.add(new MenuItemAction("replaceOccurrences", I18n.tr("materialsEditor.materials.replaceOccurrences"), () -> {
							MaterialPicker tmpPicker = new MaterialPicker(skin, land, true);
							tmpPicker.setCallback(new EventListener() {
	    						@Override
	    						public boolean handle(Event event2) {
	    							Integer matId = tmpPicker.getSelectedIndex();
	    							if (matId != null) {
	    								app.getEditManager().begin(I18n.tr("action.replaceMaterial"));
	    								MaterialTool.replaceMaterial(land, material.getIndex(), matId);
	    								app.getEditManager().end();
	    							}
	    							return true;
	    						}
	    					})
							.show(getStage(), true);
	                    }));
						actions.add(new MenuItemAction("export", I18n.tr("materialsEditor.materials.export"), () -> {
							app.getOS().getFileChooser().chooseFile(
					            new NativeFileChooserConfiguration() {{
					                title = I18n.tr("dialog.exportMaterial.title");
					                directory = Gdx.files.absolute(System.getProperty("user.home"));
					                nameFilter = (dir, name) -> name.endsWith(".png");
					                mimeFilter = "PNG image/png";
					                intent = NativeFileChooserIntent.SAVE;
					            }},
					            new NativeFileChooserCallback() {
					                @Override
					                public void onFileChosen(FileHandle file) {
					                    try {
											MaterialTool.exportMaterial(land, material.getIndex(), file.file());
										} catch (IOException e) {
											e.printStackTrace();
											MessageBox.show(getStage(), I18n.tr("dialog.exportMaterial.title"), e);
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
	                    }));
						actions.add(PopupMenu.separator);
						if (MaterialTool.canRemove(material)) {
							actions.add(new MenuItemAction("remove", I18n.tr("materialsEditor.materials.remove"), () -> {
								land.getMaterials().remove(material.getIndex());
		                    }));
						} else {
							actions.add(new MenuItemAction("remove", I18n.tr("materialsEditor.materials.remove"), null));
						}
						PopupMenu.show1(getStage(), click.getStageX(), click.getStageY(), actions.toArray(new MenuItemAction[0]));
					}
				} else if (click.getButton() == Buttons.LEFT && callback != null) {
					callback.handle(new Event());
					remove();
				}
			}
		});
		
		add(grid).grow().pad(10);
		row();
		
		Label label = new Label(I18n.tr("materialsEditor.material.terrainType"), skin);
		commands.add(label).left().padRight(5);
		
		typeField = new SelectBox<>(skin);
		typeField.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (getSelectedIndex() >= 0) {
					getSelected().setMaterialType(typeField.getSelected());
				}
			}
		});
		typeField.setItems(TerrainMaterialType.values());
		commands.add(typeField).left().padRight(5);
		
		commands.add().expandX();
		
		TextButton importButton = new TextButton(I18n.tr("materialsEditor.importMaterial"), skin);
		importButton.addListener(new ClickListener() {
			@Override
			public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
				app.getOS().getFileChooser().chooseFile(
		            new NativeFileChooserConfiguration() {{
		                title = I18n.tr("dialog.importMaterial.title");
		                directory = Gdx.files.absolute(System.getProperty("user.home"));
		                nameFilter = (dir, name) -> {
		                	String ext = name.replaceFirst("^.*\\.", "").toLowerCase();
		                	return "lnd".equals(ext) ||
		                			"lndc".equals(ext) ||
		                			"png".equals(ext) ||
		                			"gif".equals(ext) ||
		                			"gpg".equals(ext) ||
		                			"gpeg".equals(ext) ||
		                			"bmp".equals(ext);
		                };
		                mimeFilter = "All supported formats/lnd,lndc,png,gif,jpg,jpeg,bmp";
		                intent = NativeFileChooserIntent.OPEN;
		            }},
		            new NativeFileChooserCallback() {
		                @Override
		                public void onFileChosen(FileHandle file) {
		                	try {
		                		final int prevCount = land.getMaterials().size();
		                		String ext = file.name().substring(file.name().lastIndexOf('.') + 1).toLowerCase();
			                	if ("lndc".equals(ext)) {
			                		LndFile tmpLand = new LndFile(false);
			                		LandTool.importCountry(file.file(), tmpLand);
			                		MaterialPicker tmpPicker = new MaterialPicker(skin, tmpLand, true, true);
			                		tmpPicker.setCallback(new EventListener() {
										@Override
										public boolean handle(Event event) {
											app.getEditManager().begin(I18n.tr("action.importMaterials"));
											ArraySelection<LNDMaterial> srcMaterials = tmpPicker.getSelection();
											for (LNDMaterial srcMaterial : srcMaterials) {
												LNDMaterial mat = MaterialTool.importMaterial(srcMaterial, land);
												grid.setSelectedIndex(mat.getIndex());
												if (land.getMaterials().size() == prevCount) {
													MessageBox.show(getStage(), I18n.tr("materialsEditor.importMaterial"), I18n.tr("materialsEditor.import.exists"));
												}
											}
											app.getEditManager().end();
											return true;
										}
									})
			                		.show(getStage(), true);
			                	} else if ("lnd".equals(ext)) {
			                		LndFile srcLand = LndFile.load(file.file(), false);
			                		MaterialPicker tmpPicker = new MaterialPicker(skin, srcLand, true, true);
			                		tmpPicker.setCallback(new EventListener() {
										@Override
										public boolean handle(Event event) {
											app.getEditManager().begin(I18n.tr("action.importMaterials"));
											ArraySelection<LNDMaterial> srcMaterials = tmpPicker.getSelection();
											for (LNDMaterial srcMaterial : srcMaterials) {
												LNDMaterial mat = MaterialTool.importMaterial(srcMaterial, land);
												grid.setSelectedIndex(mat.getIndex());
												if (land.getMaterials().size() == prevCount) {
													MessageBox.show(getStage(), I18n.tr("materialsEditor.importMaterial"), I18n.tr("materialsEditor.import.exists"));
												}
											}
											app.getEditManager().end();
											return true;
										}
									})
			                		.show(getStage(), true);
				                } else {
				                	LNDMaterial mat = MaterialTool.importMaterial(file.file(), land);
				                	grid.setSelectedIndex(mat.getIndex());
				                	if (land.getMaterials().size() == prevCount) {
										MessageBox.show(getStage(), I18n.tr("materialsEditor.importMaterial"), I18n.tr("materialsEditor.import.exists"));
									}
				                }
		                	} catch (Exception e) {
								e.printStackTrace();
								MessageBox.show(getStage(), I18n.tr("materialsEditor.importMaterial"), e);
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
		commands.add(importButton).padRight(5);
		
		tools.add(commands).growX();
		
		editButton = new TextButton(I18n.tr("materialsEditor.edit"), skin);
		editButton.addListener(new ClickListener() {
			@Override
			public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
				isEditing = true;
				selectButton.setDisabled(grid.getSelectedIndex() < 0);
				commands.setVisible(true);
				actionButtonContainer.setActor(selectButton);
			}
		});
		
		selectButton = new TextButton(I18n.tr("materialsEditor.select"), skin);
		selectButton.addListener(new ClickListener() {
			@Override
			public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
				if (grid.getSelectedIndex() >= 0) {
					callback.handle(new Event());
					remove();
				}
			}
		});
		
		tools.add(actionButtonContainer).right();
		
		add(tools).growX().pad(0, 10, 5, 10);
		
		this.setLand(app.getLand());
		app.listeners.add(appChangeListener);
		pack();
	}
	
	private final UChangeListener appChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == MainApp.Property.LAND) {
				setLand(app.getLand());
			}
		}
	};
	
	public MaterialsEditor setLand(LndFile land) {
		if (this.land != null) {
			this.land.listeners.remove(landChangeListener);
		}
		this.land = land;
		if (land != null) {
			land.listeners.add(landChangeListener);
		}
		updateMaterials();
		return this;
	}
	
	public void pick(Actor anchor, EventListener callback) {
		pick(anchor, 0, 0, callback);
	}
	
	public void pick(Actor anchor, float x, float y, EventListener callback) {
		Vector2 pos = new Vector2(x, y);
	    anchor.localToStageCoordinates(pos);
	    pick(anchor.getStage(), x, y - getHeight(), -1, callback);
	}
	
	public void pick(Stage stage, EventListener callback) {
		pick(stage, -1, callback);
	}
	
	public void pick(Stage stage, int selected, EventListener callback) {
		float x = (stage.getWidth() - getWidth()) / 2;
		float y = (stage.getHeight() - getHeight()) / 2;
		pick(stage, x, y, selected, callback);
	}
	
	public void pick(Stage stage, float x, float y, int selected, EventListener callback) {
		this.isEditing = false;
		this.getTitleLabel().setText(I18n.tr("materialsEditor.select.title"));
		this.commands.setVisible(false);
		this.actionButtonContainer.setActor(editButton);
		grid.setSelectedIndex(selected);
		this.callback = callback;
		this.setModal(true);
		super.show(stage, x, y, true);
	    invalidateHierarchy();
	}
	
	public void show(Stage stage, float x, float y) {
		this.isEditing = true;
		this.getTitleLabel().setText(I18n.tr("materialsEditor.edit.title"));
		this.commands.setVisible(true);
		this.actionButtonContainer.setActor(null);
		grid.setSelectedIndex(-1);
		this.callback = null;
		this.setModal(false);
		super.show(stage, x, y, false);
	    invalidateHierarchy();
	}
	
	public int getSelectedIndex() {
		return grid.getSelectedIndex();
	}
	
	public LNDMaterial getSelected() {
		return grid.getSelected();
	}
	
	private final UChangeListener landChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			if (event.getProperty() == LndFile.Property.MATERIALS) {
				updateMaterials();
			} else if (event.getProperty() == LndFile.Property.COUNTRIES) {
				grid.refresh();	//To update aspect based on usage stats
			}
		}
	};
	
	private void updateMaterials() {
		//Cleanup
		if (atlasTexture != null) {
			atlasTexture.dispose();
		}
		if (land == null) {
			images = new Drawable[0];
			grid.setItems(null);
		} else {
			final int tile = 128;
			Array<LNDMaterial> materials = new Array<>(land.getMaterials().toArray(new LNDMaterial[0]));
			int count = materials.size;
			int radix = Math.max(1, (int)Math.ceil(Math.sqrt(count)));
			int nx = Math.max(1, count / radix);
			int ny = Math.max(1, MathUtils.ceilDiv(count, nx));
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
		}
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
		app.listeners.remove(appChangeListener);
		if (this.atlasTexture != null) {
			this.atlasTexture.dispose();
		}
		if (land != null) {
			land.listeners.remove(landChangeListener);
		}
		super.dispose();
	}
}
