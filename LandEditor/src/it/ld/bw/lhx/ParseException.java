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

import java.io.File;

public class ParseException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private final File file;
	private final int lineno;
	private final int col;
	
	public ParseException(String msg, int lineno) {
		this(msg, null, lineno, 1);
	}
	
	public ParseException(String msg, int lineno, int col) {
		this(msg, null, lineno, col);
	}
	
	public ParseException(String msg, File file, int lineno) {
		this(msg, file, lineno, 1);
	}
	
	public ParseException(String msg, File file, int lineno, int col) {
		super(makeMsg(msg, file, lineno, col));
		this.file = file;
		this.lineno = lineno;
		this.col = col;
	}
	
	public ParseException(Exception parent, File file, int lineno) {
		this(parent, file, lineno, 1);
	}
	
	public ParseException(Exception parent, File file, int lineno, int col) {
		super(makeMsg(parent.getMessage(), file, lineno, col), parent);
		this.file = file;
		this.lineno = lineno;
		this.col = col;
	}
	
	public ParseException(String msg, Exception parent, File file, int lineno, int col) {
		super(makeMsg(msg, file, lineno, col), parent);
		this.file = file;
		this.lineno = lineno;
		this.col = col;
	}
	
	private static String makeMsg(String msg, File file, int lineno, int col) {
		if (file != null) {
			return msg + " at " + file.getName() + ":" + lineno + ":" + col;
		} else {
			return msg + " at line " + lineno + ", column " + col;
		}
	}
	
	public File getSourceFile() {
		return file;
	}
	
	public int getLineno() {
		return lineno;
	}
	
	public int getColumn() {
		return col;
	}
}
