/* Copyright (c) 2024-2026 Daniele Lombardi / Daniels118
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
package it.ld.bw.lnd;

import java.io.File;
import java.util.List;

import it.ld.bw.lhx.*;
import it.ld.utils.CmdLine;

public class Main2 {
	public static void main(String[] args) throws Exception {
		CmdLine cmd = new CmdLine(args);
		File inp = mandatory(cmd.getArgFile("-i"), "-i");
		
		/*LHXLexer lexer = new LHXLexer();
		List<Token> tokens = lexer.tokenize(inp);
		for (Token token : tokens) {
			System.out.println(token.type + "\t" + token.value);
		}*/
		
		LHXParser parser = new LHXParser();
		List<Statement> statements = parser.parse(inp);
		for (Statement statement : statements) {
			System.out.println(statement);
		}
	}
	
	private static <T> T mandatory(T value, String name) {
		if (value == null) throw new RuntimeException(name + " is mandatory");
		return value;
	}
}
