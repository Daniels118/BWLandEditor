package it.ld.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

public class ControlledList<E> implements List<E>, RandomAccess {
	private final ArrayList<E> items;
	private ListController<E> controller;
	
	public ControlledList(ListController<E> monitor) {
		this(monitor, 16);
	}
	
	public ControlledList(ListController<E> monitor, int initialCapacity) {
		this.controller = monitor;
		this.items = new ArrayList<>(initialCapacity);
	}
	
	public ControlledList(ListController<E> monitor, Collection<? extends E> items) {
		this.controller = monitor;
		this.items = new ArrayList<>(items);
	}
	
	protected void setController(ListController<E> controller) {
		this.controller = controller;
	}
	
	@Override
	public int size() {
		return items.size();
	}
	
	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return items.contains(o);
	}
	
	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}
	
	@Override
	public Object[] toArray() {
		return items.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		return items.toArray(a);
	}
	
	@Override
	public boolean add(E e) {
		final int index = items.size();
		if (controller.beforeAdd(index, e)) {
			items.add(e);
			controller.afterAdd(index, e);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		final int index = items.indexOf(o);
		if (index >= 0) {
			if (controller.beforeRemove(index, (E)o)) {
				items.remove(index);
				controller.afterRemove(index, (E)o);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return items.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		final int index = items.size();
		if (controller.beforeAddAll(index, c)) {
			boolean changed = items.addAll(index, c);
			controller.afterAddAll(index, c);
			return changed;
		}
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		if (controller.beforeAddAll(index, c)) {
			boolean changed = items.addAll(index, c);
			controller.afterAddAll(index, c);
			return changed;
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		HashSet<Object> removeSet = new HashSet<>();
		for (Object t : c) {
			removeSet.add(t);
		}
		
		HashMap<Integer, E> itemsToRemove = new HashMap<>();
		for (int i = 0; i < items.size(); i++) {
			E item = items.get(i);
			if (removeSet.contains(item)) {
				itemsToRemove.put(i, item);
			}
		}
		
		if (controller.beforeRemoveAll(this, itemsToRemove)) {
			boolean changed = items.removeAll(removeSet);
			controller.afterRemoveAll(this, itemsToRemove);
			return changed;
		}
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (controller.beforeRetainAll(this, c)) {
			HashSet<E> removedItems = new HashSet<>(items);
			removedItems.removeAll(c);
			boolean changed = items.retainAll(c);
			controller.afterRetainAll(this, removedItems);
			return changed;
		}
		return false;
	}

	@Override
	public void clear() {
		if (controller.beforeClear(this)) {
			List<E> removedItems = new ArrayList<>(items);
			items.clear();
			controller.afterClear(removedItems);
		}
	}

	@Override
	public E get(int index) {
		return items.get(index);
	}

	@Override
	public E set(int index, E element) {
		if (controller.beforeSet(index, element)) {
			E previous = items.set(index, element);
			controller.afterSet(index, element, previous);
			return previous;
		}
		return null;
	}

	@Override
	public void add(int index, E element) {
		if (controller.beforeAdd(index, element)) {
			items.add(index, element);
			controller.afterAdd(index, element);
		}
	}

	@Override
	public E remove(int index) {
		E item = items.get(index);
		if (controller.beforeRemove(index, item)) {
			items.remove(index);
			controller.afterRemove(index, item);
			return item;
		}
		return null;
	}

	@Override
	public int indexOf(Object o) {
		return items.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return items.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return new ListItr(0);
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new ListItr(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int hashCode() {
		return items.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return items.equals(obj);
	}
	
	@Override
	public String toString() {
		return items.toString();
	}
	
	
	private class ListItr implements ListIterator<E> {
		int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        
        public ListItr(int index) {
        	this.cursor = index;
        }
        
		@Override
		public boolean hasNext() {
			return cursor != items.size();
		}

		@Override
		public E next() {
			int i = cursor;
            if (i >= items.size()) throw new NoSuchElementException();
            cursor = i + 1;
            return items.get(lastRet = i);
		}

		@Override
		public boolean hasPrevious() {
			return cursor != 0;
		}

		@Override
		public E previous() {
			int i = cursor - 1;
            if (i < 0) throw new NoSuchElementException();
            cursor = i;
            return items.get(lastRet = i);
		}

		@Override
		public int nextIndex() {
			return cursor;
		}

		@Override
		public int previousIndex() {
			return cursor - 1;
		}

		@Override
		public void remove() {
			if (lastRet < 0) throw new IllegalStateException();
            E item = items.get(lastRet);
			if (!controller.beforeRemove(lastRet, item)) {
            	throw new IllegalStateException("The list monitor refused to remove the item");
            } else {
            	items.remove(lastRet);
            	controller.afterRemove(lastRet, item);
            }
			cursor = lastRet;
            lastRet = -1;
		}

		@Override
		public void set(E e) {
			if (lastRet < 0) throw new IllegalStateException();
			if (!controller.beforeSet(lastRet, e)) {
            	throw new IllegalStateException("The list monitor refused to set the item");
            } else {
            	E item = items.set(lastRet, e);
            	controller.afterSet(lastRet, e, item);
            }
		}

		@Override
		public void add(E e) {
			int i = cursor;
			if (!controller.beforeAdd(i, e)) {
            	throw new IllegalStateException("The list monitor refused to add the item");
            } else {
            	items.add(i, e);
            	controller.afterAdd(i, e);
            }
            cursor = i + 1;
            lastRet = -1;
		}
	}
	
	
	public interface ListController<E> {
		public boolean beforeAdd(int index, E value);
		public void afterAdd(int index, E value);
		
		public boolean beforeAddAll(int index, Collection<? extends E> items);
		public void afterAddAll(int index, Collection<? extends E> items);
		
		public boolean beforeRemoveAll(ControlledList<E> list, Map<Integer, ? extends E> items);
		public void afterRemoveAll(ControlledList<E> list, Map<Integer, ? extends E> removedItems);
		
		public boolean beforeRetainAll(ControlledList<E> list, Collection<?> items);
		public void afterRetainAll(ControlledList<E> list, Collection<E> removedItems);
		
		public boolean beforeRemove(int index, E item);
		public void afterRemove(int index, E item);
		
		public boolean beforeSet(int index, E value);
		public void afterSet(int index, E value, E previousValue);
		
		public boolean beforeClear(ControlledList<E> list);
		public void afterClear(List<E> removedItems);
	}
	
	
	public static class ListControllerAdapter<Q> implements ListController<Q> {
		@Override
		public boolean beforeAdd(int index, Q value) {
			return true;
		}

		@Override
		public void afterAdd(int index, Q value) {}

		@Override
		public boolean beforeAddAll(int index, Collection<? extends Q> items) {
			int i = index;
			for (Q item : items) {
				if (!beforeAdd(i++, item)) return false;
			}
			return true;
		}

		@Override
		public void afterAddAll(int index, Collection<? extends Q> items) {
			int i = index;
			for (Q item : items) {
				afterAdd(i++, item);
			}
		}

		@Override
		public boolean beforeRemoveAll(ControlledList<Q> list, Map<Integer, ? extends Q> items) {
			for (Entry<Integer, ? extends Q> e : items.entrySet()) {
				if (!beforeRemove(e.getKey(), e.getValue())) return false;
			}
			return true;
		}

		@Override
		public void afterRemoveAll(ControlledList<Q> list, Map<Integer, ? extends Q> removedItems) {
			for (Entry<Integer, ? extends Q> e : removedItems.entrySet()) {
				afterRemove(e.getKey(), e.getValue());
			}
		}
		
		@Override
		public boolean beforeRetainAll(ControlledList<Q> list, Collection<?> items) {
			HashSet<Q> itemsToRemove = new HashSet<>(list);
			itemsToRemove.removeAll(items);
			for (Q item : itemsToRemove) {
				if (!beforeRemove(-1, item)) return false;
			}
			return true;
		}

		@Override
		public void afterRetainAll(ControlledList<Q> list, Collection<Q> removedItems) {
			for (Q item : removedItems) {
				afterRemove(-1, item);
			}
		}

		@Override
		public boolean beforeRemove(int index, Q item) {
			return true;
		}

		@Override
		public void afterRemove(int index, Q item) {}

		@Override
		public boolean beforeSet(int index, Q value) {
			return true;
		}

		@Override
		public void afterSet(int index, Q value, Q previousValue) {}

		@Override
		public boolean beforeClear(ControlledList<Q> list) {
			ListIterator<Q> it = list.listIterator(list.size());
			while (it.hasPrevious()) {
				if (!beforeRemove(it.previousIndex(), it.previous())) return false;
			}
			return true;
		}

		@Override
		public void afterClear(List<Q> removedItems) {
			ListIterator<Q> it = removedItems.listIterator(removedItems.size());
			while (it.hasPrevious()) {
				afterRemove(it.previousIndex(), it.previous());
			}
		}
	}
}
