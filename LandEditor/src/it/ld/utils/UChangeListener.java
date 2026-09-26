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

import java.util.EventListener;

public interface UChangeListener extends EventListener {
	public void onChange(UEvent event);
	
	
	public enum EventType {
		CHANGE, ADD, REMOVE, MOVE, BEFORE_MOVE
	}
	
	public static class UEvent {
		Object source;
		EventType type;
		Object property;
		Object oldValue;
		Object newValue;
		int srcIndex;
		int index;
		UEvent cause;
		
		boolean consumed = false;
		boolean last = true;
		
		UEvent() {}
		
		/**Returns the object for which this event has been generated.
		 * @return
		 */
		public Object getSource() {
			return this.source;
		}
		
		public EventType getType() {
			return this.type;
		}
		
		/**Returns the property of the object which has changed, and thus generated this event.
		 * @return
		 */
		public Object getProperty() {
			return this.property;
		}
		
		public Object getOldValue() {
			return this.oldValue;
		}
		
		public Object getNewValue() {
			return this.newValue;
		}
		
		public int getSrcIndex() {
			return this.srcIndex;
		}
		
		public int getIndex() {
			return this.index;
		}
		
		/**Returns the event which caused this event, or null.
		 * @return
		 */
		public UEvent getCause() {
			return this.cause;
		}
		
		/**Marks this event as consumed.
		 */
		public void consume() {
			this.consumed = true;
		}
		
		/**Tells if this event has been consumed by a previous event handler. Consumed events should be ignored by event handlers.
		 * @return
		 */
		public boolean isConsumed() {
			return this.consumed;
		}
		
		public boolean isItemChange() {
			return type == EventType.CHANGE && cause != null;
		}
		
		public boolean isItemChange(Object property) {
			return type == EventType.CHANGE && cause != null && cause.getProperty() == property;
		}
		
		public boolean isLast() {
			return this.last;
		}
		
		@Override
		public String toString() {
			String r = this.getClass().getSimpleName() + "(" + type + " " + property;
			if (cause != null) r += ", caused by " + cause.getType() + " " + cause.getProperty();
			return r + ")";
		}
	};
}
