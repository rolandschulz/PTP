/**
 * 
 */
package org.eclipse.fdt.fortran.core.tests.parser;

import junit.framework.TestCase;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.eclipse.fdt.fortran.core.parser.Fortran;
import org.eclipse.fdt.fortran.core.parser.FortranLexer;

/**
 * @author CraigERasmussen
 *
 */
public class BaseParserTest extends TestCase {

	protected void parseString(String code) throws Exception {
		CharStream input = new ANTLRStringStream(code);
		FortranLexer lexer = new FortranLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		Fortran parser = new Fortran(tokens);
//		parser.compilationUnit();
	}
	
	public void test1() throws Exception {
		String code =
			"PROGRAM main\n" +
			"END PROGRAM";
		parseString(code);
	}
	
	public void test2() throws Exception {
		String code =
		"MODLE mod" +
		"END MODULE mod";
		parseString(code);
	}
}
