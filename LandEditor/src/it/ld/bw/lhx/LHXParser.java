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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class LHXParser {
	private File file;
	private ListIterator<Token> it;
	private int line;
	private int col;
	
	public List<Statement> parse(String text, boolean allowErrors) throws IOException, ParseException {
		LHXLexer lexer = new LHXLexer();
		List<Token> tokens = lexer.tokenize(text);
		return parse(tokens, allowErrors);
	}
	
	public List<Statement> parse(File file) throws FileNotFoundException, IOException, ParseException {
		this.file = file;
		LHXLexer lexer = new LHXLexer();
		List<Token> tokens = lexer.tokenize(file);
		return parse(tokens, false);
	}
	
	public List<Statement> parse(List<Token> tokens, boolean allowErrors) throws ParseException {
		this.it = tokens.listIterator();
		
		List<Statement> statements = new LinkedList<>();
		Statement statement = null;
		String indent = "";
		while (!eof()) {
			Token token = next(false);
			if (token.type == TokenType.IDENTIFIER) {
				boolean bad = false;
				List<Token> args = new ArrayList<>();
				try {
					parseArgs(args);
				} catch (Exception e) {
					if (!allowErrors) throw e;
					bad = true;
				}
				if (!eof()) accept(TokenType.EOL, null);
				try {
					statement = new Statement(indent, token.value, args, allowErrors);
				} catch (IllegalArgumentException e) {
					throw new ParseException(e, file, line, col);
				}
				if (bad) statement.setInvalid();
				statements.add(statement);
				indent = "";
			} else if (token.type == TokenType.BLANK) {
				indent = token.value;
			} else if (token.type == TokenType.EOL) {
				statement = new Statement(indent);
				statements.add(statement);
				indent = "";
			} else {
				statement = new Statement(indent + token.value);
				if (!eof()) accept(TokenType.EOL, null);
				statements.add(statement);
				indent = "";
			}
		}
		
		return statements;
	}
	
	private void parseArgs(List<Token> args) throws ParseException {
		accept("(");
		Token token = next(true);
		if (!is(token, ")")) {
			while (true) {
				if (token.type != TokenType.NUMBER && token.type != TokenType.STRING && token.type != TokenType.IDENTIFIER) {
					throw new ParseException("Unexpected token: " + token.value, file, line, col);
				}
				args.add(token);
				token = next(true);
				if (is(token, ")")) break;
				if (!is(token, ",")) {
					throw new ParseException("Unexpected token: " + token.value, file, line, col);
				}
				token = next(true);
			}
		}
	}
	
	private boolean eof() {
		return !it.hasNext();
	}
	
	private Token next(boolean important) throws ParseException {
		if (!it.hasNext()) throw new ParseException("Unexpected end of file", file, line, col);
		Token token = it.next();
		if (important) {
			while (!token.type.important) {
				if (!it.hasNext()) throw new ParseException("Unexpected end of file", file, line, col);
				token = it.next();
			}
		}
		line = token.line;
		col = token.col;
		return token;
	}
	
	private boolean is(Token token, String value) {
		return token.type == TokenType.KEYWORD && value.equals(token.value);
	}
	
	private Token accept(String value) throws ParseException {
		return accept(TokenType.KEYWORD, value);
	}
	
	private Token accept(TokenType type, String value) throws ParseException {
		Token token = next(true);
		if (token.type != type) {
			throw new ParseException("Expected " + type + " but " + token.type + " found", file, line, col);
		}
		if (value != null && !value.equals(token.value)) {
			throw new ParseException("Expected \"" + value + "\" but \"" + token.value + "\" found", file, line, col);
		}
		return token;
	}
}
