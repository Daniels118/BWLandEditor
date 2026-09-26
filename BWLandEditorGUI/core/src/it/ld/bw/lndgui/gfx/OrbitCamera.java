/* Copyright (c) 2024-2026 Daniele Lombardi / Daniels118
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
package it.ld.bw.lndgui.gfx;

import static com.badlogic.gdx.math.MathUtils.HALF_PI;
import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.sin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import it.ld.utils.Listeners;
import it.ld.utils.UChangeListener.EventType;

public class OrbitCamera extends Camera {
	public enum Property {PIVOT, RADIUS, POSITION, MODE, FOV, ZOOM}
	public final Listeners listeners = new Listeners(this);
	
	public enum Mode {PERSPECTIVE, ORTHOGRAPHIC}
	
	private static final float ZDRATIO = 34f;
	
	private float yaw;
	private float pitch;
	
	private final Vector3 pivot = new Vector3();
	private float radius = 1;
	private float minRadius = 0.1f;
	private float maxRadius = 1000;
	private float maxPitch = 0;
	
	private boolean autoPlanesEnabled = true;
	private float nearRatio = 0.1f;
	private float farRatio = 100f;
	
	private Mode mode = Mode.PERSPECTIVE;
	private float fieldOfView = 67;
	private float zoom = 1f;
	
	public OrbitCamera() {
		super();
		update();
	}

	public OrbitCamera(float fieldOfViewY, float viewportWidth, float viewportHeight) {
		this.fieldOfView = fieldOfViewY;
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		update();
	}
	
	public void set(OrbitCamera camera) {
		viewportWidth = camera.viewportWidth;
		viewportHeight = camera.viewportHeight;
		near = camera.near;
		far = camera.far;
		mode = camera.getMode();
		fieldOfView = camera.getFieldOfView();
		minRadius = camera.getMinRadius();
		maxRadius = camera.getMaxRadius();
		radius = camera.getRadius();
		zoom = camera.getZoom();
		maxPitch = camera.getMaxPitch();
		pivot.set(camera.getPivot());
		yaw = camera.getYaw();
		pitch = camera.getPitch();
		update();
	}
	
	public Mode getMode() {
		return mode;
	}
	
	public void setMode(Mode mode) {
		if (mode != this.mode) {
			Object oldMode = this.mode;
			this.mode = mode;
			if (mode == Mode.PERSPECTIVE) {
				setRadius(zoom * (fieldOfView * ZDRATIO));
			} else if (mode == Mode.ORTHOGRAPHIC) {
				setZoom(radius / (fieldOfView * ZDRATIO));
			}
			listeners.notify(EventType.CHANGE, Property.MODE, oldMode, mode);
		}
	}
	
	public float getFieldOfView() {
		return this.fieldOfView;
	}
	
	public void setFieldOfView(float deg) {
		if (deg != this.fieldOfView) {
			Object oldValue = this.fieldOfView;
			this.fieldOfView = deg;
			listeners.notify(EventType.CHANGE, Property.FOV, oldValue, fieldOfView);
		}
	}
	
	public float getZoom() {
		return zoom;
	}
	
	public void setZoom(float zoom) {
		float minZoom = minRadius / (fieldOfView * ZDRATIO);
		float maxZoom = maxRadius / (fieldOfView * ZDRATIO);
		zoom = MathUtils.clamp(zoom, minZoom, maxZoom);
		if (zoom != this.zoom) {
			Object oldValue = this.zoom;
			this.zoom = zoom;
			this.update();
			listeners.notify(EventType.CHANGE, Property.ZOOM, oldValue, zoom);
		}
	}
	
	public void setZoomFromRadius() {
		setZoom(radius / (fieldOfView * ZDRATIO));
	}
	
	public Vector3 getPosition() {
		return position.cpy();
	}

	public void setPosition(Vector3 position) {
		this.position.set(position);
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
	
	public Vector3 getDirection() {
		float pitchCos = cos(pitch);
		float yaw2 = yaw - HALF_PI;
		return new Vector3(cos(yaw2) * pitchCos, sin(pitch), sin(yaw2) * pitchCos);
	}
	
	public Vector3 getUp() {
		float r = sin(-pitch);
		float yaw2 = yaw - HALF_PI;
		return new Vector3(cos(yaw2) * r, cos(-pitch), sin(yaw2) * r);
	}
	
	public Vector3 getPivot() {
		return pivot.cpy();
	}
	
	public Vector3 getPivot(Vector3 pos) {
		return pos.set(pivot);
	}
	
	public void setPivot(Vector3 pos) {
		if (!pos.equals(this.pivot)) {
			this.pivot.set(pos);
			listeners.notify(EventType.CHANGE, Property.PIVOT);
			this.update();
		}
	}
	
	public void setPivot(float x, float y, float z) {
		if (x != pivot.x || y != pivot.y || z != pivot.z) {
			this.pivot.set(x, y, z);
			listeners.notify(EventType.CHANGE, Property.PIVOT);
			this.update();
		}
	}
	
	public void setPivot(float x, float z) {
		if (x != this.pivot.x || z != this.pivot.z) {
			this.pivot.x = x;
			this.pivot.z = z;
			listeners.notify(EventType.CHANGE, Property.PIVOT);
			this.update();
		}
	}
	
	public void translatePivot(float dx, float dz) {
		if (dx != 0 || dz != 0) {
			this.pivot.x += dx;
			this.pivot.z += dz;
			listeners.notify(EventType.CHANGE, Property.PIVOT);
			this.update();
		}
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		radius = MathUtils.clamp(radius, minRadius, maxRadius);
		if (radius != this.radius) {
			this.radius = radius;
			listeners.notify(EventType.CHANGE, Property.RADIUS);
			this.update();
		}
	}
	
	public float getMinRadius() {
		return minRadius;
	}
	
	public void setMinRadius(float minRadius) {
		if (minRadius <= 0) throw new IllegalArgumentException("Radius cannot be negative");
		this.minRadius = minRadius;
	}
	
	public float getMaxRadius() {
		return maxRadius;
	}

	public void setMaxRadius(float maxRadius) {
		if (maxRadius <= 0) throw new IllegalArgumentException("Radius cannot be negative");
		this.maxRadius = maxRadius;
	}

	public float getMaxPitch() {
		return maxPitch;
	}

	public void setMaxPitch(float maxPitch) {
		this.maxPitch = maxPitch;
	}

	public boolean isAutoPlanesEnabled() {
		return autoPlanesEnabled;
	}

	public void setAutoPlanesEnabled(boolean autoPlanesEnabled) {
		this.autoPlanesEnabled = autoPlanesEnabled;
		if (autoPlanesEnabled) {
			this.update();
		}
	}

	public float getNearRatio() {
		return nearRatio;
	}

	public void setNearRatio(float nearRatio) {
		this.nearRatio = nearRatio;
		this.update();
	}

	public float getFarRatio() {
		return farRatio;
	}

	public void setFarRatio(float farRatio) {
		this.farRatio = farRatio;
		this.update();
	}

	public void rotate(float yaw, float pitch) {
		float newPitch = this.getPitch() + pitch;
		if (newPitch > maxPitch) {
			newPitch = maxPitch;
		} else if (newPitch < -HALF_PI) {
			newPitch = -HALF_PI;
		}
		this.setYaw(this.getYaw() + yaw);
		this.setPitch(newPitch);
		this.update();
	}
	
	public void zoom(float amount, float speed) {
		float curTime = (float)Math.log(radius / minRadius) / speed;
		float newTime = curTime + amount;
		setRadius(minRadius * (float)Math.exp(newTime * speed));
		//
		float minZoom = minRadius / (fieldOfView * ZDRATIO);
		curTime = (float)Math.log(zoom / minZoom) / speed;
		newTime = curTime + amount;
		setZoom(minZoom * (float)Math.exp(newTime * speed));
	}
	
	private final Vector3 flyStart = new Vector3();
	private final Vector3 flyEnd = new Vector3();
	private final Vector3 flyPivot = new Vector3();
	private boolean animate;
	private float initialRadius;
	private float finalRadius;
	private float flatDistance;
	private float flyDistance;
	private float flyDuration;
	private float flyElapsed;
	
	/**Animate the camera along a pseudo-circular trajectory from the current location to the given destination.
	 * @param destination
	 * @param finalRadius the new orbit radius at destination
	 */
	public void flyTo(Vector3 destination, float finalRadius) {
		getPivot(flyStart);
		flyEnd.set(destination);
		initialRadius = getRadius();
		this.finalRadius = finalRadius;
		flyDistance = flyStart.dst(flyEnd);
		float dx = flyEnd.x - flyStart.x;
		float dz = flyEnd.z - flyStart.z;
		flatDistance = (float)Math.sqrt(dx * dx + dz * dz);
		if (flatDistance > 0 || flyDistance != finalRadius) {
			flyPivot.set(flyStart).add(flyEnd).scl(0.5f);
			flyDuration = MathUtils.clamp(flyDistance * 0.006f, 0.75f, 2.25f);
			flyElapsed = 0f;
			animate = true;
		}
	}
	
	private final Vector3 tmpVec = new Vector3();
	
	public void act(float delta) {
		//Animate the camera along a pseudo-circular trajectory
		if (animate) {
        	flyElapsed += delta;
        	float timePercent = Math.min(flyElapsed / flyDuration, 1f);
        	float travelPercent = MathUtils.sin(timePercent * MathUtils.HALF_PI);
        	if (flatDistance > 0) {
        		float azimuth = travelPercent * MathUtils.PI;
            	float trajectoryRadius = flatDistance / 2;
        		float dx = flyEnd.x - flyStart.x;
            	float dz = flyEnd.z - flyStart.z;
        		float travelFlatPercent = -MathUtils.cos(azimuth);
	        	float cos = dx / flatDistance;
	        	float sin = dz / flatDistance;
	        	float x = flyPivot.x + cos * travelFlatPercent * trajectoryRadius;
	        	float z = flyPivot.z + sin * travelFlatPercent * trajectoryRadius;
	        	float y = MathUtils.lerp(flyStart.y, flyEnd.y, travelPercent);
	        	setPivot(x, y, z);
	        	setRadius(initialRadius * (1f - travelPercent) + trajectoryRadius * MathUtils.sin(azimuth) + finalRadius * travelPercent);
        	} else {
        		setRadius(initialRadius * (1f - travelPercent) + finalRadius * travelPercent);
        	}
    		if (travelPercent >= 1f) {
        		animate = false;
        	}
        }
	}
	
	@Override
	public void update() {
		this.update(true);
	}
	
	@Override
	public void update(boolean updateFrustum) {
		if (autoPlanesEnabled) {
			this.near = mode == Mode.PERSPECTIVE ? (radius * nearRatio) : 0f;
			this.far = radius * farRatio;
		}
		if (pivot != null) {
			Vector3 dir = this.getDirection();
			this.direction.set(dir);
			this.up.set(this.getUp());
			this.position.set(getPivot().sub(dir.scl(radius)));
			listeners.notify(EventType.CHANGE, Property.POSITION);
		}
		//
		if (mode == Mode.PERSPECTIVE) {
			updatePerspective(updateFrustum);
		} else {
			updateOrtho(updateFrustum);
		}
	}
	
	private void updatePerspective(boolean updateFrustum) {
		float aspect = viewportWidth / viewportHeight;
		projection.setToProjection(Math.abs(near), Math.abs(far), fieldOfView, aspect);
		view.setToLookAt(position, tmpVec.set(position).add(direction), up);
		combined.set(projection);
		Matrix4.mul(combined.val, view.val);

		if (updateFrustum) {
			invProjectionView.set(combined);
			Matrix4.inv(invProjectionView.val);
			frustum.update(invProjectionView);
		}
	}
	
	private void updateOrtho(boolean updateFrustum) {
		projection.setToOrtho(zoom * -viewportWidth / 2, zoom * (viewportWidth / 2), zoom * -(viewportHeight / 2),
				zoom * viewportHeight / 2, near, far);
		view.setToLookAt(direction, up);
		view.translate(-position.x, -position.y, -position.z);
		combined.set(projection);
		Matrix4.mul(combined.val, view.val);

		if (updateFrustum) {
			invProjectionView.set(combined);
			Matrix4.inv(invProjectionView.val);
			frustum.update(invProjectionView);
		}
	}
}
