package it.ld.bw.lndgui.tools;

import java.util.Collection;
import java.util.List;

import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lndgui.gfx.Bounds3D;
import it.ld.bw.lndgui.gfx.Coord;
import it.ld.utils.ControlledList;
import it.ld.utils.Listeners;
import it.ld.utils.UChangeListener.EventType;

public class Selection extends ControlledList<Coord> {
	private static final float MIN_DIST2 = (float)Math.pow(1f, 2);
	
	public enum Property {EDGE}
	public final Listeners listeners = new Listeners(this);
	
	public Selection() {
		super(null);
		setController(new ListControllerAdapter<Coord>() {
			private boolean internalEdit = false;
			
			@Override
			public boolean beforeAdd(int index, Coord coord) {
				if (internalEdit || isEmpty() || index < size()) return true;
				Coord last = get(size() - 1);
				float dx = coord.x - last.x;
				float dz = coord.z - last.z;
				float d2 = dx*dx + dz*dz;
				if (d2 <= MIN_DIST2) return false;
				float d = (float)Math.sqrt(d2);
				float sx = dx / d * LH3DLandCell.CELL_SIZE;
				float sz = dz / d * LH3DLandCell.CELL_SIZE;
				final int n = (int)Math.floor(d / LH3DLandCell.CELL_SIZE) - 1;
				internalEdit = true;
				for (int i = 1; i <= n; i++) {
					float x = last.x + sx * i;
					float z = last.z + sz * i;
					add(new Coord(x, 0f, z));
				}
				internalEdit = false;
				return true;
			}
			
			@Override
			public void afterAdd(int index, Coord value) {
				listeners.notify(EventType.CHANGE, Property.EDGE);
			}
			
			@Override
			public void afterAddAll(int index, Collection<? extends Coord> items) {
				listeners.notify(EventType.CHANGE, Property.EDGE);
			}
			
			@Override
			public void afterRemove(int index, Coord item) {
				listeners.notify(EventType.CHANGE, Property.EDGE);
			}
			
			@Override
			public void afterClear(List<Coord> removedItems) {
				listeners.notify(EventType.CHANGE, Property.EDGE);
			}
		});
	}
	
	public boolean isAcceptable(Coord coord) {
		if (isEmpty()) return true;
		Coord last = get(size() - 1);
		return coord.dst2(last) > MIN_DIST2;	//Avoid to add too close points
	}
	
	public boolean isClosed() {
		if (size() < 3) return false;
		Coord first = get(0);
		Coord last = get(size() - 1);
		return first.x == last.x && first.z == last.z;
	}
	
	public void closeLoop() {
		if (isClosed() || size() < 2) return;
		add(get(0));
	}
	
	public Bounds3D getBounds() {
		Bounds3D res = new Bounds3D();
		for (Coord coord : this) {
			res.update(coord.x, coord.y, coord.z);
		}
		return res;
	}
	
	public boolean isInside(Coord p) {
	    boolean inside = false;
	    final int n = size();
	    for (int i = 1; i < n; i++) {
	        Coord a = get(i - 1);
	        Coord b = get(i);
	        boolean intersects = ((a.z > p.z) != (b.z > p.z)) && (p.x < (b.x - a.x) * (p.z - a.z) / (b.z - a.z) + a.x);
	        if (intersects) {
	            inside = !inside;
	        }
	    }
	    return inside;
	}
}
