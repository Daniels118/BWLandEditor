package it.ld.bw.lnd.model;

import java.util.ArrayList;
import java.util.List;

import it.ld.utils.UChangeListener.EventType;

/**Classes which exposes array getters may return an instance of this class which wraps the array
 * and exposes convenient methods for tracking which items have been updated by the invoking method.
 * The owner of the array will be notified after the bulk update ends, and will receive the list of the items
 * marked as changed by the caller.
 * @param <E>
 */
public class BulkUpdate<E> implements AutoCloseable {
	public final E[] data;
	private BulkUpdateListener<E> afterUpdate;
	
	private ArrayList<Change<E>> changes;
	
	public BulkUpdate(E[] data, BulkUpdateListener<E> afterUpdate) {
		this.data = data;
		this.afterUpdate = afterUpdate;
		changes = new ArrayList<>(data.length);
	}
	
	public void setAdded(E item, int index) {
		changes.add(new Change<E>(EventType.ADD, item, index, null));
	}
	
	public void setRemoved(E item, int index) {
		changes.add(new Change<E>(EventType.REMOVE, item, index, null));
	}
	
	/**Mark the item at the given index as changed.
	 * @param index
	 */
	public void setChanged(int index) {
		changes.add(new Change<E>(EventType.CHANGE, data[index], index, null));
	}
	
	/**Mark the item at the given index as changed, and associate the property which was changed.
	 * @param index
	 * @param property
	 */
	public void setChanged(int index, Object property) {
		changes.add(new Change<E>(EventType.CHANGE, data[index], index, property));
	}
	
	/**Add all indices to the list of changes.
	 */
	public void setAllChanged() {
		setAllChanged(null);
	}
	
	/**Add all indices to the list of changes for a specific property.
	 * @param property
	 */
	public void setAllChanged(Object property) {
		for (int i = 0; i < data.length; i++) {
			changes.add(new Change<E>(EventType.CHANGE, data[i], i, property));
		}
	}
	
	/**Calling this method to tell that no item has been changed.
	 */
	public void noChanges() {
		changes = null;
	}
	
	@Override
	public void close() {
		if (afterUpdate != null) {
			afterUpdate.afterUpdate(changes);
			afterUpdate = null;
		}
		changes = null;
	}
	
	
	public static class Change<E> {
		public final EventType type;
		public final E item;
		public final int index;
		public final Object property;
		
		public Change(EventType type, E item, int index, Object property) {
			this.type = type;
			this.item = item;
			this.index = index;
			this.property = property;
		}
		
		@Override
		public String toString() {
			if (property == null) return type + "(" + index + ")";
			return type + "(" + index + ", " + property + ")";
		}
	}
	
	
	public interface BulkUpdateListener<E> {
		/**Called after the bulk update ends.
		 * @param changes the list of changes, or null if no item has been changed.
		 */
		public void afterUpdate(List<Change<E>> changes);
	}
}
