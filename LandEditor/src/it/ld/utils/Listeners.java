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
package it.ld.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import it.ld.utils.UChangeListener.EventType;
import it.ld.utils.UChangeListener.UEvent;

public class Listeners {
	private final static int INITIAL_POOL_SIZE = 6;
	private final static ArrayList<UEvent> pool = new ArrayList<>(10);
	private static int maxEvents = INITIAL_POOL_SIZE;
	
	private final Object source;
	
	private final ArrayList<WeakReference<UChangeListener>> references = new ArrayList<>();
	
	private final ArrayList<WeakReference<UChangeListener>> toAdd = new ArrayList<>();
	private final ArrayList<WeakReference<UChangeListener>> toRemove = new ArrayList<>();
	private final ArrayList<WeakReference<UChangeListener>> garbaged = new ArrayList<>();
	
	private int firing = 0;
	
	static {
		//Pool warm up
		for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
			pool.add(new UEvent());
		}
	}
	
	public Listeners(Object source) {
		this.source = source;
	}
	
	private WeakReference<UChangeListener> getReference(UChangeListener listener) {
		for (WeakReference<UChangeListener> ref : references) {
			if (ref.get() == listener) return ref;
		}
		return null;
	}
	
	public boolean add(UChangeListener listener) {
		WeakReference<UChangeListener> ref = getReference(listener);
		if (ref != null) return false;
		ref = new WeakReference<>(listener);
		if (firing > 0) {
			toAdd.add(ref);
		} else {
			references.add(ref);
		}
		return true;
	}
	
	public boolean remove(UChangeListener listener) {
		WeakReference<UChangeListener> ref = getReference(listener);
		if (ref == null) return false;
		if (firing > 0) {
			toRemove.add(ref);
		} else {
			references.remove(ref);
		}
		return true;
	}
	
	public boolean contains(UChangeListener listener) {
		WeakReference<UChangeListener> ref = getReference(listener);
		return ref != null && !toRemove.contains(ref) || toAdd.contains(ref);
	}
	
	public void notify(EventType type) {
		notify(type, null, null, null, -1, -1, null, true);
	}
	
	public void notify(EventType type, Object property) {
		notify(type, property, null, null, -1, -1, null, true);
	}
	
	public void notifyMove(Object property, Object value, int srcIndex, int dstIndex) {
		notify(EventType.MOVE, property, value, value, srcIndex, dstIndex, null, true);
	}
	
	public void notifyBeforeMove(Object property, Object value, int srcIndex, int dstIndex) {
		notify(EventType.BEFORE_MOVE, property, value, value, srcIndex, dstIndex, null, true);
	}
	
	public void notify(EventType type, Object property, boolean last) {
		notify(type, property, null, null, -1, -1, null, last);
	}
	
	public void notify(EventType type, Object property, Object oldValue, Object newValue) {
		notify(type, property, oldValue, newValue, -1, -1, null, true);
	}
	
	public void notify(EventType type, Object property, Object oldValue, Object newValue, int index) {
		notify(type, property, oldValue, newValue, -1, index, null, true);
	}
	
	public void notify(EventType type, Object property, Object oldValue, Object newValue, int index, boolean last) {
		notify(type, property, oldValue, newValue, -1, index, null, last);
	}
	
	public void notify(EventType type, Object property, Object oldValue, Object newValue, int index, UEvent cause) {
		notify(type, property, oldValue, newValue, -1, index, cause, cause != null ? cause.isLast() : true);
	}
	
	public void notify(EventType type, Object property, Object oldValue, Object newValue, int index, UEvent cause, boolean last) {
		notify(type, property, oldValue, newValue, -1, index, cause, last);
	}
	
	public void notify(EventType type, Object property, Object oldValue, Object newValue, int srcIndex, int dstIndex, UEvent cause, boolean last) {
		firing++;
		final UEvent event = getEvent();
		event.source = source;
		event.type = type;
		event.property = property;
		event.oldValue = oldValue;
		event.newValue = newValue;
		event.srcIndex = srcIndex;
		event.index = dstIndex;
		event.cause = cause;
		event.consumed = false;
		event.last = last;
		for (WeakReference<UChangeListener> ref : references) {
			UChangeListener listener = ref.get();
			if (listener != null) {
				listener.onChange(event);
			} else {
				garbaged.add(ref);
			}
		}
		releaseEvent(event);
		firing--;
		if (firing == 0) {
			//Remove references to garbaged listeners
			for (WeakReference<UChangeListener> ref : garbaged) {
				references.remove(ref);
			}
			garbaged.clear();
			//Remove references to listeners removed while firing
			for (WeakReference<UChangeListener> ref : toRemove) {
				references.remove(ref);
			}
			toRemove.clear();
			//Add references to listeners added while firing
			for (WeakReference<UChangeListener> ref : toAdd) {
				references.add(ref);
			}
			toAdd.clear();
		}
	}
	
	@Override
	public String toString() {
		return "Listeners(" + source + ")";
	}
	
	
	private static UEvent getEvent() {
		if (pool.isEmpty()) return new UEvent();
		return pool.remove(pool.size() - 1);
	}
	
	private static void releaseEvent(UEvent event) {
		pool.add(event);
		if (pool.size() > maxEvents) {
			maxEvents = pool.size();
			System.err.println("Max events used: " + maxEvents);
		}
	}
}
