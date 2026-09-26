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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

/**This class can be used to split a map txt file into individual tokens.
 */
public class LHXLexer {
	private static final char EOF = 0xFFFF;
	
	private enum Status {
		DEFAULT, IDENTIFIER, NUMBER, STRING, COMMENT, BLANK
	}
	
	private File file;
	private int tabSize = 4;
	
	public LHXLexer() {}
	
	public int getTabSize() {
		return tabSize;
	}
	
	public void setTabSize(int tabSize) {
		this.tabSize = tabSize;
	}
	
	private void add(List<Token> tokens, Token token) {
		//if (token.type.important) {
			tokens.add(token);
		//}
	}
	
	public List<Token> tokenize(File file) throws FileNotFoundException, IOException, ParseException {
		this.file = file;
		try (FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);) {
			return tokenize(br);
		}
	}
	
	public List<Token> tokenize(String string) throws ParseException {
		try (StringReader reader = new StringReader(string);) {
			return tokenize(reader);
		}
	}
	
	public List<Token> tokenize(Reader reader) throws ParseException {
		List<Token> tokens = new LinkedList<>();
		Status status = Status.DEFAULT;
		StringBuffer buffer = new StringBuffer();
		boolean escape = false;
		int numDots = 0;
		int line = 1;
		int col = 0;
		Token token = null;
		try (PushbackReader str = new PushbackReader(reader);) {
			char c = (char) str.read();
			col++;
			while (true) {
				switch (status) {
					case DEFAULT:
						if (c == '\n') {
							add(tokens, new Token(line, col, TokenType.EOL, System.lineSeparator()));
							line++;
							col = 0;
						} else if (c == '"') {
							status = Status.STRING;
							token = new Token(line, col, TokenType.STRING);
							buffer.append(c);
						} else if (c == '*') {
							status = Status.COMMENT;
							token = new Token(line, col, TokenType.COMMENT);
							col++;
							buffer.append(c);
						} else if (c == ',') {
							add(tokens, new Token(line, col, TokenType.KEYWORD, c));
							buffer.setLength(0);
						} else if (c == '(') {
							add(tokens, new Token(line, col, TokenType.KEYWORD, c));
							buffer.setLength(0);
						} else if (c == ')') {
							add(tokens, new Token(line, col, TokenType.KEYWORD, c));
							buffer.setLength(0);
						} else if (Character.isJavaIdentifierStart(c)) {
							status = Status.IDENTIFIER;
							token = new Token(line, col, TokenType.IDENTIFIER);
							buffer.append(c);
						} else if (Character.isDigit(c) || c == '-') {
							status = Status.NUMBER;
							token = new Token(line, col, TokenType.NUMBER);
							numDots = 0;
							buffer.append(c);
						} else if (c == ' ') {
							status = Status.BLANK;
							token = new Token(line, col, TokenType.BLANK);
							buffer.append(c);
						} else if (c == '\t') {
							status = Status.BLANK;
							token = new Token(line, col, TokenType.BLANK);
							buffer.append(c);
							col += tabSize - (col - 1) % tabSize - 1;
						} else if (c == '\r') {
							//NOP
						} else if (c == EOF) {
							//NOP
						} else {
							throw new ParseException("Unexpected '"+String.valueOf(c)+"' character", file, line, col);
						}
						break;
					case COMMENT:
						if (c == '\n') {
							add(tokens, token.setValue(buffer.toString()));
							buffer.setLength(0);
							status = Status.DEFAULT;
							add(tokens, new Token(line, col, TokenType.EOL, System.lineSeparator()));
							line++;
							col = 0;
						} else if (c == '\r') {
							//NOP
						} else {
							buffer.append(c);
						}
						break;
					case IDENTIFIER:
						if (Character.isJavaIdentifierPart(c)) {
							buffer.append(c);
						} else {
							str.unread(c);
							col--;
							if ("REM".equalsIgnoreCase(buffer.toString())) {
								token.type = TokenType.COMMENT;
								status = Status.COMMENT;
							} else {
								add(tokens, token.setValue(buffer.toString()));
								buffer.setLength(0);
								status = Status.DEFAULT;
							}
						}
						break;
					case NUMBER:
						if (Character.isDigit(c) || c == '.') {
							if (c == '.') {
								numDots++;
								if (numDots > 1) throw new ParseException("Invalid number", file, line, col);
							}
							buffer.append(c);
						} else {
							str.unread(c);
							col--;
							add(tokens, token.setValue(buffer.toString()));
							buffer.setLength(0);
							status = Status.DEFAULT;
						}
						break;
					case STRING:
						if (escape) {
							buffer.append(c);
							escape = false;
						} else if (c == '\\') {
							escape = true;
							buffer.append(c);
						} else if (c == '"') {
							buffer.append(c);
							add(tokens, token.setValue(buffer.toString()));
							buffer.setLength(0);
							status = Status.DEFAULT;
						} else {
							buffer.append(c);
						}
						break;
					case BLANK:
						if (c == ' ' || c == '\t') {
							buffer.append(c);
							if (c == '\t') {
								col += tabSize - (col - 1) % tabSize - 1;
							}
						} else {
							str.unread(c);
							col--;
							add(tokens, token.setValue(buffer.toString()));
							buffer.setLength(0);
							status = Status.DEFAULT;
						}
						break;
				}
				if (c == EOF) {
					break;
				}
				c = (char) str.read();
				col++;
			}
		} catch (IOException e) {
			throw new ParseException(e, file, line, col);
		}
		if (status == Status.BLANK || status == Status.COMMENT) {
			add(tokens, token.setValue(buffer.toString()));
		} else if (status != Status.DEFAULT) {
			String msg = "Unexpected end of file while parsing "+status;
			if (token != null) {
				msg += " (started at "+token.line+":"+token.col+")";
			}
			throw new ParseException(msg, file, line, col);
		}
		return tokens;
	}
}
