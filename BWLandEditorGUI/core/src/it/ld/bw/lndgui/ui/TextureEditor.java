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

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Scaling;

import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import it.ld.bw.lndgui.I18n;
import it.ld.bw.lndgui.MainApp;
import it.ld.libgdx.ui.components.MessageBox;
import it.ld.libgdx.ui.components.PictureBox;
import it.ld.libgdx.ui.components.SmartWindow;
import it.ld.libgdx.ui.events.ResizeEvent;
import it.ld.libgdx.utils.SplineBuilder;
import it.ld.libgdx.utils.UndoHelper;
import it.ld.libgdx.utils.Utils;

public class TextureEditor extends SmartWindow {
	private enum Channel {Combined, RGB}
	
	private final MainApp app;
	
	private byte[] original;
	private byte[] reference;
	private byte[] modified;
	private int width;
	private int height;
	
	private boolean sizeChangeable = false;
	private boolean formatChangeable = false;
	
	private Texture referenceTexture;
	private Texture modifiedTexture;
	private Image referenceImage;
	private Image modifiedImage;
	
	private CheckBox rtPreview;
	
	private SelectBox<Channel> channelSelect;
	private Cell<?> controlPanel;
	private PictureBox cCurveControl;
	private Table rgbControls;
	private PictureBox rCurveControl;
	private PictureBox gCurveControl;
	private PictureBox bCurveControl;
	private Slider saturationControl;
	private CheckBox colorControl;
	private Slider tintControl;
	private Slider contrastControl;
	private Slider gammaControl;
	private Slider brightnessControl;
	
	private Channel channelMode = Channel.Combined;
	private ArrayList<Vector2> cPoints = new ArrayList<>();
	private ArrayList<Vector2> rPoints = new ArrayList<>();
	private ArrayList<Vector2> gPoints = new ArrayList<>();
	private ArrayList<Vector2> bPoints = new ArrayList<>();
	private int[] cMap = new int[256];
	private int[] rMap = new int[256];
	private int[] gMap = new int[256];
	private int[] bMap = new int[256];
	
	private boolean applied = false;
	private boolean canceled = false;
	
	public static void showSingleInstance(MainApp app, Stage stage, Skin skin) {
		TextureEditor editor = (TextureEditor) SmartWindow.getSingleInstance("textureEditor");
		if (editor == null) {
			editor = new TextureEditor(app, skin, true);
			editor.setSingleInstance("textureEditor");
		}
		editor.show(stage);
		editor.toFront();
	}
	
	public TextureEditor(MainApp app, Skin skin, boolean autodispose) {
		super(I18n.tr("textureEditor.title"), skin, SmartWindow.Attribute.RESIZABLE);
		this.app = app;
		this.setAutodispose(autodispose);
		
		//Images pane (first line, left side)
		Table imagesPane = new Table();
		imagesPane.add(new Label(I18n.tr("textureEditor.original"), skin)).align(Align.topLeft).padRight(5);
		imagesPane.add(new Label(I18n.tr("textureEditor.result"), skin)).align(Align.topLeft);
		imagesPane.row();
		this.referenceImage = new Image();
		referenceImage.setScaling(Scaling.fit);
		referenceImage.setAlign(Align.topLeft);
		imagesPane.add(referenceImage).minSize(128, 128).expand().align(Align.topLeft).padRight(5);
		this.modifiedImage = new Image();
		modifiedImage.setScaling(Scaling.fit);
		modifiedImage.setAlign(Align.topLeft);
		imagesPane.add(modifiedImage).minSize(128, 128).expand().align(Align.topLeft);
		add(imagesPane).expand().fill().align(Align.topLeft).pad(10, 10, 10, 10);
		
		//Buttons panel (first line, right side)
		Table buttons = new Table();
		buttons.add(new Label("", skin)).top().row();	//Filler

		TextButton importButton = new TextButton(I18n.tr("textureEditor.import"), skin);
		importButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				app.getOS().getFileChooser().chooseFile(
		            new NativeFileChooserConfiguration() {{
		                title = I18n.tr("dialog.exportMaterial.title");
		                directory = Gdx.files.absolute(System.getProperty("user.home"));
		                nameFilter = (dir, name) ->
	                	name.endsWith(".png" ) ||
	                	name.endsWith(".gif" ) ||
	                	name.endsWith(".gpg" ) ||
	                	name.endsWith(".gpeg") ||
	                	name.endsWith(".bmp" );
	                mimeFilter = "All supported formats/png,gif,jpg,jpeg,bmp";
		                intent = NativeFileChooserIntent.OPEN;
		            }},
		            new NativeFileChooserCallback() {
		                @Override
		                public void onFileChosen(FileHandle file) {
		                    try {
		                    	Pixmap srcPixmap = new Pixmap(file);
		                    	int srcWidth = srcPixmap.getWidth();
		                    	int srcHeight = srcPixmap.getHeight();
		                    	byte[] out;
		                    	int originalChannels = original.length / (width * height);
		                        if (original != null && (
		                        		(!sizeChangeable && (srcWidth != width || srcHeight != height)) || 
		                        		(!formatChangeable && (srcPixmap.getPixels().limit() / (srcPixmap.getWidth() * srcPixmap.getHeight())) != originalChannels)
		                        	)) {
		                        	srcWidth = width;
		                        	srcHeight = height;
		                        	
		                        	Pixmap scaled = Utils.resize(srcPixmap, srcWidth, srcHeight, getFormat(originalChannels));
		                        	srcPixmap.dispose();
		                        	ByteBuffer pixels = scaled.getPixels();
			                        final int size = pixels.limit();
			                        out = new byte[size];
			                        pixels.get(out);
			                        scaled.dispose();
		                        } else {
		                        	ByteBuffer pixels = srcPixmap.getPixels();
			                        final int size = pixels.position();
			                        pixels.rewind();
			                        out = new byte[size];
			                        pixels.get(out);
			                        srcPixmap.dispose();
		                        }
		                    	setTexture(out, srcWidth, srcHeight, false);
							} catch (Exception e) {
								e.printStackTrace();
								MessageBox.show(getStage(), I18n.tr("dialog.importImage.title"), e);
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
		buttons.add(importButton).fillX().top().padBottom(5f).row();
		
		TextButton exportButton = new TextButton(I18n.tr("textureEditor.export"), skin);
		exportButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
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
		                    	TextureData textureData = modifiedTexture.getTextureData();
		                    	if (!textureData.isPrepared()) textureData.prepare();
		                    	Pixmap pixmap = textureData.consumePixmap();
		                    	PixmapIO.writePNG(file, pixmap);
		                    	if (textureData.disposePixmap()) pixmap.dispose();
							} catch (Exception e) {
								e.printStackTrace();
								MessageBox.show(getStage(), I18n.tr("dialog.exportImage.title"), e);
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
		buttons.add(exportButton).fillX().top().padBottom(5f).row();
		
		TextButton resetButton = new TextButton(I18n.tr("textureEditor.reset"), skin);
		resetButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				resetCurves();
			}
		});
		buttons.add(resetButton).fillX().top().padBottom(5f).row();
		
		rtPreview = new CheckBox(I18n.tr("textureEditor.preview"), skin);
		rtPreview.setChecked(true);
		rtPreview.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				fire(new PreviewChangedEvent());
			}
		});
		buttons.add(rtPreview).top();
		
		buttons.add().expandY().row();	//Filler
		
		TextButton cancelButton = new TextButton(I18n.tr("textureEditor.cancel"), skin);
		cancelButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				remove();
			}
		});
		buttons.add(cancelButton).fillX().top().padBottom(5f).row();
		
		TextButton applyButton = new TextButton(I18n.tr("textureEditor.apply"), skin);
		applyButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				System.arraycopy(modified, 0, original, 0, modified.length);
				System.arraycopy(modified, 0, reference, 0, modified.length);
				applied = true;
				updateReference();
				resetCurves();
				app.getEditManager().end();
				fire(new ApplyEvent());
				app.getEditManager().begin(I18n.tr("action.editTexture"));
			}
		});
		buttons.add(applyButton).fillX().top().padBottom(5f).row();
		this.add(buttons).fillY().align(Align.topRight).pad(10, 0, 10, 10).row();
		
		//Curves panel (second line)
		Table curvesPanel = new Table();
		Table topPanel = new Table();
		topPanel.add(new Label(I18n.tr("textureEditor.curves"), skin)).left();
		topPanel.add().expandX().padRight(10);	//Filler
		topPanel.add(new Label(I18n.tr("textureEditor.channels"), skin)).right().padRight(5);
		channelSelect = new SelectBox<>(skin);
		channelSelect.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setChannelMode(channelSelect.getSelected());
			}
		});
		topPanel.add(channelSelect).right();
		curvesPanel.add(topPanel).align(Align.topLeft).fillX().row();
		cCurveControl = new PictureBox().setBackColor(Color.DARK_GRAY).setForeColor(Color.WHITE).setKeepContentOnResize(true);
		cCurveControl.addListener(curveListener);
		rgbControls = new Table();
		rCurveControl = new PictureBox().setBackColor(Color.DARK_GRAY).setForeColor(Color.WHITE).setKeepContentOnResize(true);
		rCurveControl.addListener(curveListener);
		rgbControls.add(rCurveControl).minSize(128, 128).expand().fill().padRight(5);
		gCurveControl = new PictureBox().setBackColor(Color.DARK_GRAY).setForeColor(Color.WHITE).setKeepContentOnResize(true);
		gCurveControl.addListener(curveListener);
		rgbControls.add(gCurveControl).minSize(128, 128).expand().fill().padRight(5);
		bCurveControl = new PictureBox().setBackColor(Color.DARK_GRAY).setForeColor(Color.WHITE).setKeepContentOnResize(true);
		bCurveControl.addListener(curveListener);
		rgbControls.add(bCurveControl).minSize(128, 128).expand().fill();
		controlPanel = curvesPanel.add().minSize(128, 128).expand().fill();
		add(curvesPanel).colspan(2).expand().fill().align(Align.topLeft).pad(0, 10, 10, 10).row();
		
		//Sliders panel (third line)
		Table slidersPanel = new Table();
		slidersPanel.defaults().padBottom(3);
		slidersPanel.add(new Label(I18n.tr("textureEditor.saturation"), skin)).left().padRight(10);
		saturationControl = new Slider(0, 2, 1f/512f, false, skin);
		saturationControl.setValue(1);
		saturationControl.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				applyTransforms();
			}
		});
		slidersPanel.add(saturationControl).expandX().fillX().right().row();
		Table tmp = new Table();
		tmp.add(new Label(I18n.tr("textureEditor.tint"), skin)).growX().left().padRight(10);
		colorControl = new CheckBox(null, skin);
		colorControl.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				applyTransforms();
			}
		});
		tmp.add(colorControl).right().padRight(4);
		slidersPanel.add(tmp).fillX();
		tintControl = new Slider(-180, 180, 1, false, skin);
		tintControl.setValue(0);
		tintControl.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				applyTransforms();
			}
		});
		slidersPanel.add(tintControl).expandX().fillX().right().row();
		slidersPanel.add(new Label(I18n.tr("textureEditor.contrast"), skin)).left().padRight(10);
		contrastControl = new Slider(-5, 5, 0.1f, false, skin);
		contrastControl.setValue(0);
		contrastControl.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				applyTransforms();
			}
		});
		slidersPanel.add(contrastControl).expandX().fillX().right().row();
		slidersPanel.add(new Label(I18n.tr("textureEditor.gamma"), skin)).left().padRight(10);
		gammaControl = new Slider(-255, 255, 1, false, skin);
		gammaControl.setValue(0);
		gammaControl.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				applyTransforms();
			}
		});
		slidersPanel.add(gammaControl).expandX().fillX().right().row();
		slidersPanel.add(new Label(I18n.tr("textureEditor.brightness"), skin)).left().padRight(10);
		brightnessControl = new Slider(-10, 10, 0.1f, false, skin);
		brightnessControl.setValue(0);
		brightnessControl.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				applyTransforms();
			}
		});
		slidersPanel.add(brightnessControl).expandX().fillX().right().row();
		add(slidersPanel).colspan(2).expandX().fillX().align(Align.topLeft).pad(0, 10, 7, 10).row();
		
		resetCurves();
		pack();
		
		UndoHelper.attachGroup(this, app.getEditManager(), I18n.tr("action.editTexture"))
			.attachActors(cCurveControl, rCurveControl, gCurveControl, bCurveControl);
	}
	
	public boolean isApplied() {
		return this.applied;
	}
	
	public boolean isCanceled() {
		return this.canceled;
	}
	
	public boolean isPreviewEnabled() {
		return rtPreview.isChecked();
	}
	
	public void setPreviewEnabled(boolean v) {
		rtPreview.setChecked(v);
	}
	
	private void setChannelMode(Channel channel) {
		this.channelMode = channel;
		controlPanel.setActor(channel == Channel.Combined ? cCurveControl : rgbControls);
		applyTransforms();
	}
	
	private int findPoint(ArrayList<Vector2> points, int x, int y, float radius) {
		if (points == null) return -1;
		float radius2 = radius * radius;
		int closest = -1;
		float minDist = Float.POSITIVE_INFINITY;
		for (int i = 0; i < points.size(); i++) {
			Vector2 point = points.get(i);
			float d = point.dst2(x, y);
			if (d < radius2 && d < minDist) {
				closest = i;
				minDist = d;
			}
		}
		return closest;
	}
	
	private int insertPoint(ArrayList<Vector2> points, int x, int y, int minDist) {
		if (points == null || (x < 0 || x >= 255)) return -1;
		Vector2 prev = points.get(0);
		for (int i = 1; i < points.size(); i++) {
			Vector2 point = points.get(i);
			if (prev.x + minDist < x && x < point.x - minDist) {
				points.add(i, new Vector2(x, y));
				return i;
			}
			prev = point;
		}
		return -1;
	}
	
	private int getPoint(ArrayList<Vector2> points, int x, int y, float radius, int minDist) {
		int index = findPoint(points, x, y, radius);
		if (index < 0) index = insertPoint(points, x, y, minDist);
		return index;
	}
	
	private final InputListener curveListener = new InputListener() {
		private static final float TOLERANCE = 8f;
		private static final int MIN_DISTANCE = 15;
		
		private boolean pressed = false;
		private PictureBox control;
		private ArrayList<Vector2> points;
		private int pointIndex;
		private int[] map;
		
		private int getIn(PictureBox control, float x) {
			return MathUtils.clamp(Math.round(x / control.getWidth() * 255), 0, 255);
		}
		
		private int getOut(PictureBox control, float y) {
			return MathUtils.clamp(Math.round(y / control.getHeight() * 255), 0, 255);
		}
		
		@Override
		public boolean handle(Event event) {
			if (event instanceof ResizeEvent) {
				PictureBox control = (PictureBox)event.getTarget();
				ArrayList<Vector2> points = null;
				int[] map = null;
				if (control == cCurveControl) {
					points = cPoints;
					map = cMap;
				} else if (control == rCurveControl) {
					points = rPoints;
					map = rMap;
				} else if (control == gCurveControl) {
					points = gPoints;
					map = gMap;
				} else if (control == bCurveControl) {
					points = bPoints;
					map = bMap;
				}
				updateCurve(control, points, map, false);
				return true;
			}
			return super.handle(event);
		}
		
		private void updateFields(InputEvent event) {
			this.control = (PictureBox)event.getTarget();
			if (control == cCurveControl) {
				this.points = cPoints;
				this.map = cMap;
			} else if (control == rCurveControl) {
				this.points = rPoints;
				this.map = rMap;
			} else if (control == gCurveControl) {
				this.points = gPoints;
				this.map = gMap;
			} else if (control == bCurveControl) {
				this.points = bPoints;
				this.map = bMap;
			}
		}
		
		@Override
		public boolean mouseMoved(InputEvent event, float x, float y) {
			if (!pressed) {
				updateFields(event);
				int in = getIn(control, x);
				int out = getOut(control, y);
				int index = findPoint(points, in, out, TOLERANCE);
				if (index >= 0) {
					Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
				} else {
					Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Crosshair);
				}
				return true;
			}
			return false;
		}
		
		public void enter(InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
			Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Crosshair);
		}
		
		public void exit(InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
			Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
		}
		
		@Override
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			pressed = true;
			updateFields(event);
			int in = getIn(control, x);
			int out = getOut(control, y);
			pointIndex = -1;
			if (button == Buttons.LEFT) {
				pointIndex = getPoint(points, in, out, TOLERANCE, MIN_DISTANCE);
				if (pointIndex >= 0) {
					if (pointIndex == 0 || pointIndex == points.size() - 1) {
						Gdx.graphics.setSystemCursor(Cursor.SystemCursor.VerticalResize);
					} else {
						Gdx.graphics.setSystemCursor(Cursor.SystemCursor.AllResize);
					}
					updateCurve(control, points, map, true);
					return true;
				}
			} else if (button == Buttons.RIGHT) {
				int index = findPoint(points, in, out, TOLERANCE);
				if (index > 0 && index < points.size() - 1) {
					points.remove(index);
					updateCurve(control, points, map, true);
				}
				pointIndex = -1;
			}
			return false;
		};
		
		@Override
		public void touchDragged(InputEvent event, float x, float y, int pointer) {
			if (pointIndex >= 0) {
				event.handle();
				int in = getIn(control, x);
				int out = getOut(control, y);
				Vector2 point = points.get(pointIndex);
				int xMin = (pointIndex > 0 ? (int)points.get(pointIndex - 1).x : 0) + 3;
				int xMax = (pointIndex + 1 < points.size() ? (int)points.get(pointIndex + 1).x : 255) - 3;
				if (point.x > 0 && point.x < 255 && xMin < xMax) {
					point.x = MathUtils.clamp(in, xMin, xMax);
				}
				point.y = out;
				updateCurve(control, points, map, true);
			}
		};
		
		@Override
		public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
			pressed = false;
			Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Crosshair);
		}
	};
	
	public boolean isSizeChangeable() {
		return sizeChangeable;
	}

	public void setSizeChangeable(boolean sizeChangeable) {
		this.sizeChangeable = sizeChangeable;
	}
	
	public boolean isFormatChangeable() {
		return formatChangeable;
	}

	public void setFormatChangeable(boolean formatChangeable) {
		this.formatChangeable = formatChangeable;
	}
	
	public TextureEditor setTexture(byte[] pixels, int width, int height) {
		return setTexture(pixels, width, height, true);
	}
	
	public TextureEditor setTexture(byte[] pixels, int width, int height, boolean updateOriginal) {
		if (this.original != null) {
			if (!this.sizeChangeable) {
				if (width != this.width || height != this.height) throw new IllegalArgumentException("The texture size cannot be changed");
			}
			if (!this.formatChangeable) {
				int oldChannels = original.length / (this.width * this.height);
				int newChannels = pixels.length / (width * height);
				if (oldChannels != newChannels) throw new IllegalArgumentException("The texture format cannot be changed");
			}
		}
		
		this.width = width;
		this.height = height;
		
		if (updateOriginal) {
			this.original = new byte[pixels.length];
			System.arraycopy(pixels, 0, original, 0, pixels.length);
		}
		
		this.reference = new byte[pixels.length];
		System.arraycopy(pixels, 0, reference, 0, pixels.length);
		
		this.modified = new byte[pixels.length];
		System.arraycopy(pixels, 0, modified, 0, pixels.length);
		
		int channels = pixels.length / (width * height);
		if (channels >= 3) {
			channelSelect.setItems(Channel.values());
		} else {
			channelSelect.setItems(new Channel[] {Channel.Combined});
		}
		updateReference();
		resetCurves();
		pack();
		return this;
	}
	
	public byte[] getOriginal() {
		return this.original;
	}
	
	public byte[] getReference() {
		return this.reference;
	}
	
	public byte[] getModified() {
		return this.modified;
	}
	
	private void updateReference() {
		if (this.referenceTexture != null) {
			referenceTexture.dispose();
		}
		this.referenceTexture = Utils.toTexture(reference, width, height, true);
		referenceImage.setDrawable(new TextureRegionDrawable(referenceTexture));
	}
	
	private void updateModified() {
		if (this.modifiedTexture != null) {
			modifiedTexture.dispose();
		}
		this.modifiedTexture = Utils.toTexture(modified, width, height, true);
		modifiedImage.setDrawable(new TextureRegionDrawable(modifiedTexture));
		if (isPreviewEnabled()) fire(new ChangeEvent());
	}
	
	private void resetCurves() {
		initMap(cPoints, cMap);
		initMap(rPoints, rMap);
		initMap(gPoints, gMap);
		initMap(bPoints, bMap);
		tintControl.setValue(0);
		contrastControl.setValue(0);
		gammaControl.setValue(0);
		brightnessControl.setValue(0);
		updateCurves(true);
	}
	
	private void updateCurves(boolean apply) {
		updateCurve(cCurveControl, cPoints, cMap, false);
		updateCurve(rCurveControl, rPoints, rMap, false);
		updateCurve(gCurveControl, gPoints, gMap, false);
		updateCurve(bCurveControl, bPoints, bMap, false);
		if (apply) applyTransforms();
	}
	
	private void updateCurve(PictureBox control, ArrayList<Vector2> points, int[] map, boolean apply) {
		if (control == null) return;
		control.clear(null);
		applied = false;
		SplineBuilder.buildSplineMap(points, map);
		Pixmap pixmap = control.getPixmap();
		final int width = pixmap.getWidth();
		final int height = pixmap.getHeight();
		int prevx = 0;
		int prevy = Math.round((float)map[0] / 255f * height);
		for (int in = 1; in < 256; in++) {
			int x = Math.round((float)in / 255f * width);
			int y = Math.round((float)map[in] / 255f * height);
			pixmap.drawLine(prevx, height - prevy, x, height - y);
			prevx = x;
			prevy = y;
		}
		for (Vector2 point : points) {
			int x = Math.round(point.x / 255f * width);
			int y = Math.round(point.y / 255f * height);
			pixmap.drawRectangle(x - 2, height - y - 2, 5, 5);
		}
		control.refresh();
		if (apply) applyTransforms();
	}
	
	private void applyTransforms() {
		if (reference == null) return;
		System.arraycopy(reference, 0, modified, 0, reference.length);
		applyCurves();
		applySaturation();
		applyTint();
		applyContrast();
		applyGamma();
		applyBrightness();
		updateModified();
	}
	
	private void applyCurves() {
		final int channels = reference.length / (width * height);
		final int skipAlpha = channels - 2;
		if (channelMode == Channel.Combined) {
			for (int i = 0; i < modified.length; i++) {
				modified[i] = (byte)cMap[modified[i] & 0xFF];
			}
		} else {
			for (int i = 0; i < modified.length;) {
				modified[i] = (byte)rMap[modified[i] & 0xFF];
				i++;
				modified[i] = (byte)gMap[modified[i] & 0xFF];
				i++;
				modified[i] = (byte)bMap[modified[i] & 0xFF];
				i += skipAlpha;
			}
		}
	}
	
	private void applySaturation() {
		final float sat = saturationControl.getValue();
		final int channels = reference.length / (width * height);
		if (channels >= 3) {
			for (int i = 0; i < modified.length; i += channels) {
				float r = (float)(modified[i + 0] & 0xFF);
				float g = (float)(modified[i + 1] & 0xFF);
				float b = (float)(modified[i + 2] & 0xFF);
				float y = 0.299f * r + 0.587f * g + 0.114f * b;
			    r = MathUtils.clamp(y + (r - y) * sat, 0f, 255f);
			    g = MathUtils.clamp(y + (g - y) * sat, 0f, 255f);
			    b = MathUtils.clamp(y + (b - y) * sat, 0f, 255f);
				modified[i + 0] = (byte)r;
				modified[i + 1] = (byte)g;
				modified[i + 2] = (byte)b;
		    }
		}
	}
	
	private void applyTint() {
		boolean applyColor = colorControl.isChecked();
		float tint = tintControl.getValue();
		if (tint < 0) tint += 360;
		if (tint == 0 && !applyColor) return;
		final float angle = (float)Math.toRadians(tint);
		final float cos = (float)Math.cos(angle);
		final float sin = (float)Math.sin(angle);
		final int channels = reference.length / (width * height);
		if (channels >= 3) {
			for (int i = 0; i < modified.length; i += channels) {
				float r = (float)(modified[i + 0] & 0xFF) / 255f;
				float g = (float)(modified[i + 1] & 0xFF) / 255f;
				float b = (float)(modified[i + 2] & 0xFF) / 255f;
				if (applyColor) {
					r = 0.299f * r + 0.587f * g + 0.114f * b;
					g = 0;
					b = 0;
				}
				float r2 = (0.299f + 0.701f*cos + 0.168f*sin) * r
			             + (0.587f - 0.587f*cos + 0.330f*sin) * g
			             + (0.114f - 0.114f*cos - 0.497f*sin) * b;
			    float g2 = (0.299f - 0.299f*cos - 0.328f*sin) * r
			             + (0.587f + 0.413f*cos + 0.035f*sin) * g
			             + (0.114f - 0.114f*cos + 0.292f*sin) * b;
			    float b2 = (0.299f - 0.300f*cos + 1.250f*sin) * r
			             + (0.587f - 0.588f*cos - 1.050f*sin) * g
			             + (0.114f + 0.886f*cos - 0.203f*sin) * b;
				modified[i + 0] = (byte) MathUtils.clamp(r2 * 255f, 0f, 255f);
				modified[i + 1] = (byte) MathUtils.clamp(g2 * 255f, 0f, 255f);
				modified[i + 2] = (byte) MathUtils.clamp(b2 * 255f, 0f, 255f);
		    }
		}
	}
	
	private void applyContrast() {
		if (contrastControl.getValue() == 0) return;
		final float contrast = getExpVal(contrastControl);
		final int channels = reference.length / (width * height);
		if (channels >= 3) {
			final int skipAlpha = channels - 3;
			long sum = 0;
			for (int i = 0; i < modified.length; i += skipAlpha) {
		        sum += modified[i++] & 0xFF;
		        sum += modified[i++] & 0xFF;
		        sum += modified[i++] & 0xFF;
		    }
			final float avg = sum / (width * height * 3);
			for (int i = 0; i < modified.length; i += skipAlpha) {
				modified[i] = (byte)MathUtils.clamp(Math.round(avg + ((float)(modified[i] & 0xFF) - avg) * contrast), 0, 255);
				i++;
				modified[i] = (byte)MathUtils.clamp(Math.round(avg + ((float)(modified[i] & 0xFF) - avg) * contrast), 0, 255);
		        i++;
		        modified[i] = (byte)MathUtils.clamp(Math.round(avg + ((float)(modified[i] & 0xFF) - avg) * contrast), 0, 255);
		        i++;
		    }
		} else {
			long sum = 0;
			for (int i = 0; i < modified.length; i++) {
		        sum += modified[i++] & 0xFF;
		    }
			final float avg = sum / (width * height);
			for (int i = 0; i < modified.length; i++) {
				modified[i] = (byte)MathUtils.clamp(Math.round(avg + ((float)(modified[i] & 0xFF) - avg) * contrast), 0, 255);
		    }
		}
	}
	
	private void applyGamma() {
		final int gamma = (int) gammaControl.getValue();
		if (gamma == 0) return;
		final int channels = reference.length / (width * height);
		if (channels >= 3) {
			final int skipAlpha = channels - 3;
			for (int i = 0; i < modified.length; i += skipAlpha) {
				modified[i] = (byte)MathUtils.clamp((modified[i] & 0xFF) + gamma, 0, 255);
				i++;
				modified[i] = (byte)MathUtils.clamp((modified[i] & 0xFF) + gamma, 0, 255);
		        i++;
		        modified[i] = (byte)MathUtils.clamp((modified[i] & 0xFF) + gamma, 0, 255);
		        i++;
		    }
		} else {
			for (int i = 0; i < modified.length; i++) {
				modified[i] = (byte)MathUtils.clamp((modified[i] & 0xFF) + gamma, 0, 255);
		    }
		}
	}
	
	private void applyBrightness() {
		if (brightnessControl.getValue() == 0) return;
		final float brightness = getExpVal(brightnessControl);
		final int channels = reference.length / (width * height);
		if (channels >= 3) {
			final int skipAlpha = channels - 3;
			for (int i = 0; i < modified.length; i += skipAlpha) {
				modified[i] = (byte)MathUtils.clamp(Math.round(((float)(modified[i] & 0xFF)) * brightness), 0, 255);
				i++;
				modified[i] = (byte)MathUtils.clamp(Math.round(((float)(modified[i] & 0xFF)) * brightness), 0, 255);
		        i++;
		        modified[i] = (byte)MathUtils.clamp(Math.round(((float)(modified[i] & 0xFF)) * brightness), 0, 255);
		        i++;
		    }
		} else {
			for (int i = 0; i < modified.length; i++) {
				modified[i] = (byte)MathUtils.clamp(Math.round(((float)(modified[i] & 0xFF)) * brightness), 0, 255);
		    }
		}
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
	}
	
	@Override
	public void dispose() {
		cCurveControl.dispose();
		rCurveControl.dispose();
		gCurveControl.dispose();
		bCurveControl.dispose();
		if (modifiedTexture != null) {
			modifiedTexture.dispose();
			modifiedTexture = null;
		}
		if (referenceTexture != null) {
			referenceTexture.dispose();
			referenceTexture = null;
		}
		super.dispose();
	}
	
	@Override
	public SmartWindow show(Stage stage, float x, float y, boolean modal) {
		app.getEditManager().begin(I18n.tr("action.editTexture"));
		this.canceled = false;
		app.disableAllExcept3DViewAnd(this);
		return super.show(stage, x, y, modal);
	}
	
	@Override
	public boolean remove() {
		this.canceled = !this.applied;
		boolean r = super.remove();
		app.getEditManager().end();
		app.enableAll();
		return r;
	}
	
	
	private static void initMap(ArrayList<Vector2> points, int[] map) {
		points.clear();
		points.add(new Vector2(0, 0));
		points.add(new Vector2(255, 255));
		for (int i = 0; i < 256; i++) {
			map[i] = i;
		}
	}
	
	private static Format getFormat(int channels) {
		if (channels == 1) return Format.Intensity;
		if (channels == 3) return Format.RGB888;
		if (channels == 4) return Format.RGBA8888;
		return null;
	}
	
	private static float getExpVal(Slider slider) {
		return (float) Math.pow(slider.getMaxValue(), slider.getValue() / slider.getMaxValue());
	}
	
	
	public class ApplyEvent extends Event {}
	public class PreviewChangedEvent extends Event {}
}
