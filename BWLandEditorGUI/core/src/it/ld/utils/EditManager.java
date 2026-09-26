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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import it.ld.utils.UChangeListener.EventType;

public class EditManager {
	public enum Property {BEFORE_UNDO, AFTER_UNDO, BEFORE_REDO, AFTER_REDO};
	public final Listeners listeners = new Listeners(this);
	
	private int maxUndo = 0;
	private LinkedList<Edit> undoHistory = new LinkedList<>();
	private int undoPosition = 0;
	private boolean applyingAction = false;
	
	private Stack<MultiEdit> transactionStack = new Stack<MultiEdit>();
	
	public List<Edit> getHistory() {
		return Collections.unmodifiableList(undoHistory);
	}
	
	public int getMaxUndo() {
		return maxUndo;
	}
	
	public void setMaxUndo(int count) {
		if (count < 0) throw new IllegalArgumentException("Max undo must be positive");
		this.maxUndo = count;
		while (undoHistory.size() > Math.max(maxUndo, undoPosition)) {
			undoHistory.removeLast();
		}
	}
	
	public void clear() {
		Edit last = undoHistory.isEmpty() ? null : undoHistory.getLast();
		undoHistory.clear();
		if (last != null) {
			undoHistory.addLast(new PlaceHolderEdit(last.toString()));
		}
		undoPosition = 0;
	}
	
	public boolean isApplyingAction() {
		return applyingAction;
	}
	
	public boolean isTransactionActive() {
		return !transactionStack.isEmpty();
	}
	
	public int getTransactionDepth() {
		return transactionStack.size();
	}
	
	private MultiEdit getCurrentTransaction() {
		return transactionStack.peek();
	}
	
	public UndoableEdit begin(String description) {
		MultiEdit transaction = new MultiEdit(description);
		if (!transactionStack.isEmpty()) {
			getCurrentTransaction().add(transaction);
		}
		transactionStack.add(transaction);
		return transaction;
	}
	
	public void add(Edit action, boolean execute) {
		if (isTransactionActive()) {
			if (action instanceof UndoableEdit) {
				if (execute) {
					action.execute();
				}
				getCurrentTransaction().add((UndoableEdit)action);
			} else {
				throw new IllegalStateException("Only undoable edits are allowed during transactions");
			}
		} else {
			if (execute) {
				action.execute();
			}
			add(action);
		}
	}
	
	public void resetTransaction() {
		if (!isTransactionActive()) throw new IllegalStateException("No active transaction");
		getCurrentTransaction().reset();
	}
	
	public void end() {
		if (!isTransactionActive()) throw new IllegalStateException("No active transaction");
		MultiEdit transaction = transactionStack.pop();
		if (transactionStack.isEmpty()) {
			if (!transaction.isEmpty()) {
				add(transaction);
			}
		}
	}
	
	public void cancel() {
		if (!isTransactionActive()) throw new IllegalStateException("No active transaction");
		MultiEdit transaction = transactionStack.pop();
		if (isTransactionActive()) {
			getCurrentTransaction().remove(transaction);
		}
	}
	
	public void clearRedos() {
		while (undoPosition > 0) {
			undoHistory.removeFirst();
			undoPosition--;
		}
	}
	
	private void add(Edit action) {
		clearRedos();
		if (action instanceof UndoableEdit) {
			undoHistory.addFirst(action);
			Edit last = null;
			while (undoHistory.size() > maxUndo) {
				last = undoHistory.removeLast();
			}
			if (last != null) {
				undoHistory.addLast(new PlaceHolderEdit(last.toString()));
			}
		} else {
			undoHistory.clear();
			undoHistory.addFirst(action);
		}
	}
	
	public void undo() {
		if (undoPosition < undoHistory.size() && !isTransactionActive() && undoHistory.get(undoPosition) instanceof UndoableEdit) {
			UndoableEdit action = (UndoableEdit)undoHistory.get(undoPosition);
			listeners.notify(EventType.CHANGE, Property.BEFORE_UNDO, action, action);
			applyingAction = true;
			try {
				action.undo();
				undoPosition++;
			} catch (Exception e) {
				clear();
				throw e;
			} finally {
				applyingAction = false;
				listeners.notify(EventType.CHANGE, Property.AFTER_UNDO, action, action);
			}
		}
	}
	
	public void redo() {
		if (undoPosition > 0 && !isTransactionActive()) {
			undoPosition--;
			UndoableEdit action = (UndoableEdit)undoHistory.get(undoPosition);
			listeners.notify(EventType.CHANGE, Property.BEFORE_REDO, action, action);
			applyingAction = true;
			try {
				action.execute();
			} catch (Exception e) {
				clear();
				throw e;
			} finally {
				applyingAction = false;
				listeners.notify(EventType.CHANGE, Property.AFTER_REDO, action, action);
			}
		}
	}
	
	public boolean canUndo() {
		return undoPosition < undoHistory.size() && undoHistory.get(undoPosition) instanceof UndoableEdit;
	}
	
	public boolean canRedo() {
		return undoPosition > 0;
	}
	
	public String getUndoDescription() {
		return canUndo() ? undoHistory.get(undoPosition).toString() : "";
	}
	
	public String getRedoDescription() {
		return canRedo() ? undoHistory.get(undoPosition - 1).toString() : "";
	}
	
	public int getPosition() {
		return this.undoPosition;
	}
	
	public void setPosition(final int position) {
		while (this.undoPosition < position && canUndo()) {
			undo();
		}
		while (this.undoPosition > position && canRedo()) {
			redo();
		}
	}
	
	
	private static class MultiEdit implements UndoableEdit {
		private String description;
		private List<UndoableEdit> edits = new LinkedList<>();
		
		public MultiEdit(String description) {
			this.description = description;
		}
		
		public void add(UndoableEdit edit) {
			edits.add(edit);
		}
		
		public boolean remove(UndoableEdit edit) {
			return edits.remove(edit);
		}
		
		public void reset() {
			edits.clear();
		}
		
		public boolean isEmpty() {
			for (Edit edit : edits) {
				if (edit instanceof MultiEdit) {
					if (!((MultiEdit)edit).isEmpty()) {
						return false;
					}
				} else {
					return false;
				}
			}
			return true;
		}
		
		@Override
		public void execute() {
			for (UndoableEdit edit : edits) {
				edit.execute();
			}
		}

		@Override
		public void undo() {
			ListIterator<UndoableEdit> it = edits.listIterator(edits.size());
			while (it.hasPrevious()) {
				UndoableEdit edit = it.previous();
				edit.undo();
			}
		}

		@Override
		public String toString() {
			return description;
		}
	}
}
