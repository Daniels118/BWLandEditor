package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class BillboardQuad {
    private final ModelInstance instance;

    private final Vector3 basePosition = new Vector3();
    private final Vector3 currentPosition = new Vector3();

    private float time = 0f;

    //Bobbing
    private float bobAmplitude = 10f;
    private float bobSpeed = 4.0f;
    private float bobPhase = 0f;

    //Scaling for distance
    private float baseScale = 1f;
    private float referenceDistance = 200f; // distance where scale = baseScale
    private float minScale = 0.1f;
    private float maxScale = 100.0f;

    public BillboardQuad(BillboardQuadAsset asset) {
        this.instance = new ModelInstance(asset.model);
    }

    public void setPosition(Vector3 pos) {
        basePosition.set(pos);
        currentPosition.set(basePosition);
        instance.transform.setTranslation(currentPosition);
    }

    public void setBobbing(float amplitude, float speed, float phaseRadians) {
        this.bobAmplitude = Math.max(0f, amplitude);
        this.bobSpeed = Math.max(0f, speed);
        this.bobPhase = phaseRadians;
    }

    /**
     * @param baseScale         scale at referenceDistance
     * @param referenceDistance distance in world units where scale = baseScale
     * @param minScale          clamp min
     * @param maxScale          clamp max
     */
    public void setDistanceScaling(float baseScale, float referenceDistance, float minScale, float maxScale) {
        this.baseScale = Math.max(0.0001f, baseScale);
        this.referenceDistance = Math.max(0.0001f, referenceDistance);
        this.minScale = Math.max(0.0001f, minScale);
        this.maxScale = Math.max(this.minScale, maxScale);
    }
    
    
    private final Vector3 tmpDir = new Vector3();
    private final Vector3 tmpScale = new Vector3(1f, 1f, 1f);
    private final Quaternion tmpRot = new Quaternion();
    
    public void update(Camera camera) {
        float dt = Gdx.graphics.getDeltaTime();
        time += dt;

        //Increase scaling with distance
        float dist = camera.position.dst(basePosition);
        float scale = baseScale * (dist / Math.max(referenceDistance, 0.0001f));
        scale = MathUtils.clamp(scale, minScale, maxScale);

        //Bobbing only above base Y
        float effectiveAmp = bobAmplitude * scale;

        //sin remapped in [0, 1]
        float bob01 = (MathUtils.sin(time * bobSpeed + bobPhase) + 1f) * 0.5f;
        float offsetY = bob01 * effectiveAmp;

        currentPosition.set(
                basePosition.x,
                basePosition.y + offsetY,
                basePosition.z
        );

        //Direction toward camera on XZ plane
        tmpDir.set(camera.position).sub(currentPosition);
        tmpDir.y = 0f;

        if (tmpDir.len2() < 0.000001f) {
            tmpDir.set(0f, 0f, 1f);
        } else {
            tmpDir.nor();
        }

        //yaw: +Z toward camera
        float yawDeg = MathUtils.atan2(tmpDir.x, tmpDir.z) * MathUtils.radiansToDegrees;

        //Final trasform
        tmpScale.set(scale, scale, scale);
        tmpRot.setFromAxis(Vector3.Y, yawDeg);
        instance.transform.set(currentPosition, tmpRot, tmpScale);
    }

    public ModelInstance getInstance() {
        return instance;
    }
}
