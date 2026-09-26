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
package it.ld.bw.lnd.tools;

import java.util.ArrayList;
import java.util.List;

import it.ld.bw.lnd.model.BulkUpdate;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LNDMapMaterial;
import it.ld.utils.Listeners;
import it.ld.utils.UChangeListener;
import it.ld.utils.UChangeListener.EventType;

public class CountryTool {
	private final LNDCountry country;
	
	private CtrlPoint firstPoint;
	
	public CountryTool(LNDCountry country) {
		this.country = country;
		initControlPoints();
	}
	
	public List<CtrlPoint> getControlPoints() {
		List<CtrlPoint> r = new ArrayList<>(16);
		for (CtrlPoint point = firstPoint; point != null; point = point.next) {
			r.add(point);
		}
		return r;
	}
	
	public CtrlPoint getFirstPoint() {
		return this.firstPoint;
	}
	
	public int getPointsCount() {
		int r = 0;
		for (CtrlPoint point = firstPoint; point != null; point = point.next) {
			r++;
		}
		return r;
	}
	
	public CtrlPoint getPointBelow(int elevation) {
		CtrlPoint prevPoint = null;
		for (CtrlPoint point = firstPoint; point != null; point = point.next) {
			if (point.elevation < elevation) {
				prevPoint = point;
			} else {
				break;
			}
		}
		return prevPoint;
	}
	
	public CtrlPoint getPointNear(int elevation, int range) {
		int low = elevation - range;
		int high = elevation + range;
		for (CtrlPoint point = firstPoint; point != null; point = point.next) {
			if (low <= point.elevation && point.elevation <= high) {
				return point;
			}
		}
		return null;
	}
	
	public boolean splitUp(int newMaterial) {
		if (firstPoint.next.next != null) {
			return false;	//Split is allowed only when there is a single material
		}
		CtrlPoint prevPoint = firstPoint;
		CtrlPoint lastPoint = firstPoint.next;
		//
		int step = 255 / 3;
		int e = step;
		CtrlPoint point = new CtrlPoint(prevPoint, e, prevPoint.firstMaterial, newMaterial, 0f, ctrlPointListener);
		e += step;
		point = new CtrlPoint(point, e, newMaterial, newMaterial, 1f, ctrlPointListener);
		e += step;
		lastPoint.firstMaterial = newMaterial;
		lastPoint.secondMaterial = newMaterial;
		updateCountryFromControlPoints();
		return true;
	}
	
	public boolean splitDown(int newMaterial) {
		if (firstPoint.next.next != null) {
			return false;	//Split is allowed only when there is a single material
		}
		CtrlPoint prevPoint = firstPoint;
		//
		int step = 255 / 3;
		int e = step;
		CtrlPoint point = new CtrlPoint(prevPoint, e, newMaterial, prevPoint.secondMaterial, 0f, ctrlPointListener);
		e += step;
		point = new CtrlPoint(point, e, prevPoint.firstMaterial, prevPoint.secondMaterial, 1f, ctrlPointListener);
		e += step;
		prevPoint.firstMaterial = newMaterial;
		prevPoint.secondMaterial = newMaterial;
		updateCountryFromControlPoints();
		return true;
	}
	
	public boolean canSplit() {
		if (firstPoint.next.next != null) {
			return false;	//Split is allowed only when there is a single material
		}
		return true;
	}
	
	public boolean insertMaterial(int elevation, int newMaterial) {
		if (elevation <= 0 || elevation >= 255) {
			return false;	//First and last point can be only edited
		}
		CtrlPoint prevPoint = getPointBelow(elevation);
		CtrlPoint nextPoint = prevPoint.next;
		if (prevPoint.firstMaterial != prevPoint.secondMaterial) {
			return false;	//Materials cannot be inserted in the blending range
		}
		final int range = nextPoint.elevation - prevPoint.elevation;
		if (range < 25) {
			return false;	//The range is too narrow to insert a material
		}
		//
		int step = range / 5;
		int e = prevPoint.elevation + step;
		int outMaterial = prevPoint.firstMaterial;	//first and second are equal
		CtrlPoint point = new CtrlPoint(prevPoint, e, outMaterial, newMaterial, 0f, ctrlPointListener);
		e += step;
		point = new CtrlPoint(point, e, newMaterial, newMaterial, 1f, ctrlPointListener);
		e += step;
		point = new CtrlPoint(point, e, newMaterial, outMaterial, 0f, ctrlPointListener);
		e += step;
		point = new CtrlPoint(point, e, outMaterial, outMaterial, 1f, ctrlPointListener);
		updateCountryFromControlPoints();
		return true;
	}
	
	public boolean canInsertMaterial(int elevation) {
		if (elevation <= 0 || elevation >= 255) {
			return false;	//First and last point can be only edited
		}
		CtrlPoint prevPoint = getPointBelow(elevation);
		CtrlPoint nextPoint = prevPoint.next;
		if (prevPoint.firstMaterial != prevPoint.secondMaterial) {
			return false;	//Materials cannot be inserted in the blending range
		}
		final int range = nextPoint.elevation - prevPoint.elevation;
		if (range < 25) {
			return false;	//The range is too narrow to insert a material
		}
		return true;
	}
	
	public boolean removeMaterial(int elevation) {
		if (elevation <= 0 || elevation >= 255) {
			return false;	//First and last point can be only edited
		}
		CtrlPoint prevPoint = getPointBelow(elevation);
		CtrlPoint nextPoint = prevPoint.next;
		if (prevPoint.firstMaterial != prevPoint.secondMaterial) {
			return false;	//Materials cannot be removed from the blending range
		}
		if (prevPoint.elevation == 0 && nextPoint.elevation == 255) {
			return false;	//At least one material is required
		}
		//
		if (nextPoint.next == null) {
			CtrlPoint newPrev =  prevPoint.prev.prev;
			newPrev.next = nextPoint;
			nextPoint.prev = newPrev;
			nextPoint.firstMaterial = newPrev.firstMaterial;
			nextPoint.secondMaterial = newPrev.secondMaterial;
		} else if (prevPoint == firstPoint) {
			CtrlPoint newNext = nextPoint.next.next;
			prevPoint.next = newNext;
			newNext.prev = prevPoint;
			prevPoint.firstMaterial = newNext.firstMaterial;
			prevPoint.secondMaterial = newNext.firstMaterial;
		} else {
			CtrlPoint newPrev =  prevPoint.prev;
			CtrlPoint newNext = nextPoint.next;
			newPrev.next = newNext;
			newNext.prev = newPrev;
			newPrev.secondMaterial = newNext.firstMaterial;
			if (newPrev.firstMaterial == newNext.firstMaterial && newPrev.secondMaterial == newNext.secondMaterial) {
				newPrev = newPrev.prev;
				newNext = newNext.next;
				newPrev.next = newNext;
				newNext.prev = newPrev;
				newPrev.secondMaterial = newNext.firstMaterial;
			}
		}
		updateCountryFromControlPoints();
		return true;
	}
	
	public boolean canRemoveMaterial(int elevation) {
		if (elevation <= 0 || elevation >= 255) {
			return false;	//First and last point can be only edited
		}
		CtrlPoint prevPoint = getPointBelow(elevation);
		CtrlPoint nextPoint = prevPoint.next;
		if (prevPoint.firstMaterial != prevPoint.secondMaterial) {
			return false;	//Materials cannot be removed from the blending range
		}
		if (prevPoint.elevation == 0 && nextPoint.elevation == 255) {
			return false;	//At least one material is required
		}
		return true;
	}
	
	public boolean changeMaterial(int elevation, int newMaterial) {
		CtrlPoint prevPoint = getPointBelow(elevation);
		CtrlPoint nextPoint = prevPoint.next;
		if (prevPoint.firstMaterial != prevPoint.secondMaterial) {
			return false;	//Materials cannot be changed in the blending range
		}
		//Update the blending materials
		if (prevPoint.prev != null) {
			prevPoint.prev.secondMaterial = newMaterial;
		}
		prevPoint.firstMaterial = newMaterial;
		prevPoint.secondMaterial = newMaterial;
		nextPoint.firstMaterial = newMaterial;
		updateCountryFromControlPoints();
		return true;
	}
	
	public boolean canChangeMaterial(int elevation) {
		CtrlPoint prevPoint = getPointBelow(elevation);
		if (prevPoint.firstMaterial != prevPoint.secondMaterial) {
			return false;	//Materials cannot be changed in the blending range
		}
		return true;
	}
	
	private void initControlPoints() {
		LNDMapMaterial[] mapMaterials = country.getMapMaterialsForRead();
		int prevFirstMaterial = -1;
		int prevSecondMaterial = -1;
		CtrlPoint ctrlPoint = null;
		firstPoint = null;
		for (int elevation = 0; elevation < 256; elevation++) {
			LNDMapMaterial mapMaterial = mapMaterials[elevation];
			if (mapMaterial.getFirstMaterialIndex() != prevFirstMaterial || mapMaterial.getSecondMaterialIndex() != prevSecondMaterial) {
				ctrlPoint = new CtrlPoint(ctrlPoint, elevation,
						mapMaterial.getFirstMaterialIndex(), mapMaterial.getSecondMaterialIndex(), mapMaterial.getBlend(), ctrlPointListener);
				if (firstPoint == null) {
					firstPoint = ctrlPoint;
					firstPoint.lock();
				}
				prevFirstMaterial = mapMaterial.getFirstMaterialIndex();
				prevSecondMaterial = mapMaterial.getSecondMaterialIndex();
			}
		}
		if (ctrlPoint.elevation < 255) {
			ctrlPoint = new CtrlPoint(ctrlPoint, 255, ctrlPoint.firstMaterial, ctrlPoint.secondMaterial, ctrlPoint.blend, ctrlPointListener);
			ctrlPoint.lock();
		}
	}
	
	private final UChangeListener ctrlPointListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			updateCountryFromControlPoints();
		}
	};
	
	private void updateCountryFromControlPoints() {
		try (BulkUpdate<LNDMapMaterial> mapMaterials = country.getMapMaterialsForUpdate()) {
			for (CtrlPoint ctrlPoint = firstPoint.next; ctrlPoint != null; ctrlPoint = ctrlPoint.next) {
				CtrlPoint prevCtrlPoint = ctrlPoint.prev;
				float blend0 = prevCtrlPoint.blend;
				float blend1 = ctrlPoint.blend;
				float deltaElevation = ctrlPoint.elevation - prevCtrlPoint.elevation;
				for (int elevation = prevCtrlPoint.elevation; elevation <= ctrlPoint.elevation; elevation++) {
					LNDMapMaterial mapMaterial = mapMaterials.data[elevation];
					float weight1 = (float)(elevation - prevCtrlPoint.elevation) / deltaElevation;
					float weight0 = 1f - weight1;
					float blend = blend0 * weight0 + blend1 * weight1;
					int coefficient = Math.min(Math.max(0, Math.round(blend * 255)), 255);
					mapMaterial.setFirstMaterialIndex(prevCtrlPoint.getFirstMaterial());
					mapMaterial.setSecondMaterialIndex(prevCtrlPoint.getSecondMaterial());
					mapMaterial.setCoefficient(coefficient);
				}
			}
		}
	}
	
	
	public static class CtrlPoint {
		public enum Property {PREV, NEXT, ELEVATION, FIRST_MATERIAL, SECOND_MATERIAL, BLEND}
		public final Listeners listeners = new Listeners(this);
		
		private CtrlPoint prev;
		private CtrlPoint next;
		private int elevation;
		private int firstMaterial;
		private int secondMaterial;
		private float blend;
		private boolean locked;
		
		public CtrlPoint(CtrlPoint prev, int elevation, int firstMaterial, int secondMaterial, float blend, UChangeListener listener) {
			this.prev = prev;
			this.elevation = elevation;
			this.firstMaterial = firstMaterial;
			this.secondMaterial = secondMaterial;
			this.blend = blend;
			if (prev != null) {
				this.next = prev.next;
				if (prev.next != null) {
					prev.next.prev = this;
				}
				prev.next = this;
			}
			if (listener != null) {
				this.listeners.add(listener);
			}
		}
		
		public CtrlPoint getPrev() {
			return this.prev;
		}
		
		public CtrlPoint getNext() {
			return this.next;
		}
		
		public void lock() {
			this.locked = true;
		}
		
		public boolean isLocked() {
			return this.locked;
		}

		public int getElevation() {
			return elevation;
		}

		public boolean setElevation(int newElevation) {
			if (newElevation == this.elevation || this.locked || newElevation <= 0 || newElevation >= 255) return false;
			if (prev != null && newElevation <= prev.elevation + 3) return false;
			if (next != null && newElevation >= next.elevation - 3) return false;
			Object oldValue = this.elevation;
			this.elevation = newElevation;
			this.listeners.notify(EventType.CHANGE, Property.ELEVATION, oldValue, newElevation);
			return true;
		}

		public int getFirstMaterial() {
			return firstMaterial;
		}

		public void setFirstMaterial(int firstMaterial) {
			if (firstMaterial != this.firstMaterial) {
				if (firstMaterial < 0) return;
				Object oldValue = this.firstMaterial;
				this.firstMaterial = firstMaterial;
				this.listeners.notify(EventType.CHANGE, Property.FIRST_MATERIAL, oldValue, this.firstMaterial);
			}
		}

		public int getSecondMaterial() {
			return secondMaterial;
		}

		public void setSecondMaterial(int secondMaterial) {
			if (secondMaterial != this.secondMaterial) {
				if (secondMaterial < 0) return;
				Object oldValue = this.secondMaterial;
				this.secondMaterial = secondMaterial;
				this.listeners.notify(EventType.CHANGE, Property.SECOND_MATERIAL, oldValue, this.secondMaterial);
			}
		}

		public float getBlend() {
			return blend;
		}
		
		public void setBlend(float blend) {
			blend = Math.max(0f, Math.min(blend, 1f));
			if (blend != this.blend) {
				Object oldValue = this.blend;
				this.blend = blend;
				this.listeners.notify(EventType.CHANGE, Property.BLEND, oldValue, this.blend);
			}
		}
		
		@Override
		public String toString() {
			return "CtrlPoint(" + elevation + ", " + blend + ")";
		}
	}
}
