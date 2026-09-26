package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import it.ld.bw.lndgui.gfx.OrbitCamera.Mode;

public class CompassBillboard {
    private final ModelInstance instance;

    private final Vector3 tmpUp = new Vector3();
    private final Vector3 tmpRight = new Vector3();
    private final Vector3 tmpPosition = new Vector3();
    
    public CompassBillboard(BillboardQuadAsset asset) {
        this.instance = new ModelInstance(asset.model);
    }
    
    public void update(OrbitCamera camera) {
    	float distance = (camera.near + camera.far) / 2;
    	float scale = distance * 0.17f;
    	
    	float aspect = camera.viewportWidth / camera.viewportHeight;
    	
    	tmpRight.set(camera.direction).crs(camera.up);
    	tmpUp.set(camera.up);
    	
    	if (camera.getMode() == Mode.PERSPECTIVE) {
    		float offset = scale * 0.6f;
    		float fovY2 = MathUtils.degreesToRadians * (camera.getFieldOfView() / 2);
        	float fovX2 = MathUtils.atan(MathUtils.tan(fovY2) * aspect);
    		tmpRight.scl(offset - MathUtils.tan(fovX2) * distance);
    		tmpUp.scl(offset - MathUtils.tan(fovY2) * distance);
    	} else {
    		scale = 260f * camera.getZoom();
    		tmpRight.scl(scale * 0.6f - camera.viewportWidth * 0.5f * camera.getZoom());
    		tmpUp.scl(scale * 0.6f - camera.viewportHeight * 0.5f * camera.getZoom());
    	}
        
        tmpPosition.set(camera.direction).scl(distance)
        .add(tmpRight)
        .add(tmpUp)
        .add(camera.position);
        
        instance.transform.setToTranslation(tmpPosition);
        instance.transform.scale(scale, scale, scale);
    }

    public ModelInstance getInstance() {
        return instance;
    }
}
