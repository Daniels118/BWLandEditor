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
package it.ld.bw.lhx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.List;

import it.ld.utils.ControlledList;
import it.ld.utils.Listeners;
import it.ld.utils.UChangeListener;
import it.ld.utils.UChangeListener.EventType;

public class LHXFile {
	public enum Property {STATEMENTS, FILE}
	public final Listeners listeners = new Listeners(this);
	
	private boolean moving;
	
	private final List<Statement> statements = new ControlledList<Statement>(new ControlledList.ListControllerAdapter<Statement>() {
		public boolean beforeAdd(int index, Statement value) {
			if (moving) return true;
			if (value.getLHX() != null) throw new RuntimeException("Statement belongs to another file");
			return true;
		};
		
		public void afterAdd(int index, Statement value) {
			if (moving) return;
			value.setLHX(LHXFile.this);
			value.listeners.add(statementChangeListener);
			listeners.notify(EventType.ADD, Property.STATEMENTS, null, value, index);
		};
		
		public void afterAddAll(int index, Collection<? extends Statement> items) {
			if (moving) return;
			int last = index + items.size() - 1;
			int i = index;
			for (Statement item : items) {
				item.setLHX(LHXFile.this);
				item.listeners.add(statementChangeListener);
				listeners.notify(EventType.ADD, Property.STATEMENTS, null, item, i, i == last);
				i++;
			}
		};
		
		public void afterRemove(int index, Statement item) {
			if (moving) return;
			item.listeners.remove(statementChangeListener);
			item.setLHX(null);
			listeners.notify(EventType.REMOVE, Property.STATEMENTS, item, null, index);
		};
		
		public void afterClear(List<Statement> removedItems) {;
			int last = removedItems.size() - 1;
			int i = 0;
			for (Statement item : removedItems) {
				item.listeners.remove(statementChangeListener);
				item.setLHX(null);
				listeners.notify(EventType.REMOVE, Property.STATEMENTS, item, null, -1, i == last);
				i++;
			}
		};
	});
	
	private final UChangeListener statementChangeListener = new UChangeListener() {
		@Override
		public void onChange(UEvent event) {
			int index = statements.indexOf(event.getSource());
			listeners.notify(EventType.CHANGE, Property.STATEMENTS, null, event.getSource(), index, event, event.isLast());
		}
	};
	
	private File file;
	
	public void read(File file) throws Exception {
		LHXParser parser = new LHXParser();
		this.statements.clear();
		this.statements.addAll(parser.parse(file));
		this.file = file;
	}
	
	public void write(File file) throws Exception {
		try (FileWriter fw = new FileWriter(file);
				BufferedWriter bos = new BufferedWriter(fw)) {
			for (Statement statement : statements) {
				bos.write(statement.toString());
				bos.write("\r\n");
			}
		}
	}
	
	public List<Statement> getStatements() {
		return statements;
	}
	
	public boolean moveAfter(Statement stmt, Statement ref) {
		int index = statements.indexOf(ref);
		if (index < 0) throw new IllegalArgumentException("Statement does not belong to this script");
		return move(stmt, index + 1);
	}
	
	public boolean move(Statement stmt, final int dstIndex) {
		int srcIndex = statements.indexOf(stmt);
		if (srcIndex < 0) throw new IllegalArgumentException("Statement does not belong to this script");
		return move(srcIndex, dstIndex);
	}
	
	public boolean move(final int srcIndex, final int dstIndex) {
		if (srcIndex == dstIndex) return false;
		Statement stmt = statements.get(srcIndex);
		listeners.notifyBeforeMove(Property.STATEMENTS, stmt, srcIndex, dstIndex);
		moving = true;
		statements.remove(srcIndex);
		int insertPos = srcIndex < dstIndex ? dstIndex - 1 : dstIndex;
		statements.add(insertPos, stmt);
		moving = false;
		listeners.notifyMove(Property.STATEMENTS, stmt, srcIndex, dstIndex);
		return true;
	}
	
	public File getFile() {
		return this.file;
	}
	
	public void setFile(File file) {
		if (file != this.file) {
			Object oldValue = this.file;
			this.file = file;
			listeners.notify(EventType.CHANGE, Property.FILE, oldValue, this.file);
		}
	}
	
	public Statement findFirstStatement(Command command) {
		for (Statement stmt : statements) {
			if (stmt.isCommand() && stmt.getCommand() == command) {
				return stmt;
			}
		}
		return null;
	}
	
	public int findHighestId(Command command) {
		int id = -1;
		for (Statement stmt : statements) {
			if (stmt.isCommand() && stmt.getCommand() == command) {
				id = Math.max(id, stmt.getId());
			}
		}
		return id;
	}
	
	public Statement findClosest(Command command, float x, float z, float maxDistance) {
		Statement res = null;
		float minDist2 = maxDistance >= 0 ? (maxDistance * maxDistance + 0.000001f) : Float.MAX_VALUE;
		for (Statement stmt : statements) {
			if (stmt.isCommand() && stmt.getCommand() == command) {
				LHXCoord pos = stmt.getPosition();
				float d2 = pos.dst2(x, z);
				if (d2 < minDist2) {
					minDist2 = d2;
					res = stmt;
				}
			}
		}
		return res;
	}
	
	public String getLandscape() {
		Statement stmt = findFirstStatement(Command.LOAD_LANDSCAPE);
		if (stmt == null) return null;
		return stmt.getArg("path").getString();
	}
	
	@Override
	public String toString() {
		if (file != null) return file.toString();
		return super.toString();
	}
	
	public static LHXFile load(File file) throws Exception {
		LHXFile res = new LHXFile();
		res.read(file);
		return res;
	}
}
