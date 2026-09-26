package it.ld.bw.lndgui.gfx;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

import it.ld.bw.lndgui.MainApp;
import it.ld.bw.lndgui.gfx.L3DModelManager.ModelInfo;
import it.ld.bw.lndgui.ui.View3D;

public class L3DPreviewManager implements Disposable {
	private final int previewWidth = 128;
	private final int previewHeight = 128;
	private final int textureSize;
	private final int xRegions;
	private final int yRegions;
	private final int maxRegions;
	
	private final List<Texture> spritesheets = new ArrayList<>();
	private final Map<ModelInfo, TextureRegionDrawable> previews = new IdentityHashMap<>();
	private final Map<ModelInfo, List<PreviewCallback>> requests = new LinkedHashMap<>();
	
	private Texture spritesheet;
	private int numRegions;
	private View3D view3d;
	private Object3D obj3d;
	
	public L3DPreviewManager(MainApp app) {
		IntBuffer buf = BufferUtils.newIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buf);
		int maxTextureSize = buf.get(0);
		
		textureSize = Math.min(2048, maxTextureSize);
		xRegions = textureSize / previewWidth;
		yRegions = textureSize / previewHeight;
		maxRegions = xRegions * yRegions;
		
		view3d = new View3D(app);
		view3d.setSize(previewWidth, previewHeight);
		view3d.setBackgroundColor(new Color(0, 0, 0, 0));
		view3d.setAlphaEnabled(true);
	}
	
	/**Returns a TextureRegionDrawable, possibly with a null region. If the region is null, then it is loaded
	 * asynchronously and then the callback is invoked. The callback is not invoked is the region is already available.
	 * @param model
	 * @param callback
	 * @return
	 */
	public TextureRegionDrawable getPreview(ModelInfo modelInfo, @Null PreviewCallback callback) {
		TextureRegionDrawable preview = previews.get(modelInfo);
		if (preview == null) {
			preview = new TextureRegionDrawable();
			previews.put(modelInfo, preview);
			
			List<PreviewCallback> callbacks = requests.get(modelInfo);
			if (callbacks == null) {
				callbacks = new ArrayList<>();
				requests.put(modelInfo, callbacks);
			}
			if (callback != null) {
				callbacks.add(callback);
			}
		}
		return preview;
	}
	
	private void generatePreview() {
		if (requests.isEmpty()) return;
		ModelInfo modelInfo = requests.keySet().iterator().next();
		//Load the model into 3D view
		if (obj3d != null) {
			obj3d.close();
			obj3d = null;
		}
		obj3d = new Object3D(modelInfo, 0);
		if (!obj3d.getState(0)) {
			obj3d.enableStates(-1, -1);
		}
		ModelInstance inst = obj3d.getModelInstance();
		view3d.getExtraObjects().clear();
		view3d.getExtraObjects().add(inst);
		//Setup the camera
		BoundingBox bb = inst.calculateBoundingBox(new BoundingBox());
		OrbitCamera camera = view3d.getCamera();
		camera.setPivot(bb.getCenter(new Vector3()));
		float fovTan = MathUtils.tanDeg(camera.getFieldOfView() / 2f);
		float halfw = bb.getWidth() * 0.5f;
		float halfh = bb.getHeight() * 0.5f;
		float halfd = bb.getDepth() * 0.5f;
		float wDist = halfw / fovTan;
		float hDist = halfh / fovTan;
		float dDist = halfd / fovTan;
		float maxDist = Math.max(Math.max(wDist, hDist), dDist);
		if (halfh < 0.001f) {
			camera.setYaw(0f);
			camera.setPitch(-MathUtils.HALF_PI);
		} else if (halfd < 0.001f) {
			camera.setYaw(0f);
			camera.setPitch(0f);
		} else {
			camera.setYaw(0.5f);
			camera.setPitch(-0.5f);
		}
		camera.setRadius(maxDist + Math.max(halfw, halfd));
		//Take a shot
		view3d.getRenderedImage(pixmap -> {
			if (spritesheet == null || numRegions >= maxRegions) {
				spritesheet = new Texture(textureSize, textureSize, Format.RGBA8888);
				numRegions = 0;
				spritesheets.add(spritesheet);
			}
			//Draw the preview in the sprite sheet. We use negative height to flip the image vertically
			int x = (numRegions % xRegions) * previewWidth;
			int y = (numRegions / xRegions) * previewHeight;
			spritesheet.bind();
			Gdx.gl.glTexSubImage2D(
			    GL20.GL_TEXTURE_2D, 0,
			    x, y,
			    previewWidth, previewHeight,
			    GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE,
			    pixmap.getPixels()
			);
			int err = Gdx.gl.glGetError();
			if (err != GL20.GL_NO_ERROR) {
			    System.err.println("glTexSubImage2D error: " + err);
			}
			TextureRegionDrawable preview = previews.get(modelInfo);
			preview.setRegion(new TextureRegion(spritesheet, x, y + previewHeight, previewWidth, -previewHeight));
			numRegions++;
			//Notify callers
			List<PreviewCallback> callbacks = requests.remove(modelInfo);
			for (PreviewCallback callback : callbacks) {
				try {
					callback.onPreviewReady(modelInfo, preview);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void act(float delta) {
		for (int i = 0; i < 4 && !requests.isEmpty(); i++) {
			if (view3d.isCapturingFrame()) {
				view3d.act(delta);
			} else {
				generatePreview();
			}
		}
	}
	
	@Override
	public void dispose() {
		view3d.dispose();
		if (obj3d != null) obj3d.close();
		
		for (Texture texture : spritesheets) {
			texture.dispose();
		}
		spritesheets.clear();
	}
	
	
	public static interface PreviewCallback {
		public void onPreviewReady(ModelInfo modelInfo, TextureRegionDrawable preview);
	}
}
