package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.antlr.runtime.*;
import org.eclipse.ptp.lang.fortran.core.parser.FortranLexer;
import org.eclipse.ptp.lang.fortran.core.parser.FortranParser;
import org.eclipse.ptp.lang.fortran.core.parser.FortranStream;
import org.eclipse.ptp.lang.fortran.core.parser.FortranTokenStream;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ProgramUnit {
	
   public static final String TEST_ROOT = "../org.eclipse.ptp.lang.fortran.core.tests/parser-test-files/";

   private void parse(String file) {
      boolean error = false;
      String path = TEST_ROOT + file;

      try {
         FortranLexer lexer = new FortranLexer(new FortranStream(path));
         FortranTokenStream tokens = new FortranTokenStream(lexer);
         FortranParser parser = new FortranParser(tokens);
         parser.program();
         error = parser.hasErrorOccurred;
      } catch(Exception e) {
 //        e.printStackTrace();
         error = true;
      } 
      
      assertEquals("Fortran Parser: unsuccessfully parsed " + file,
                   false,
                   error);
   }
   
   @Test public void program_0() {
	   parse("program-unit/program_0.f90");
   }

   @Test public void program_1() {
	   parse("program-unit/program_1.f90");
   }

   @Test public void program_2() {
	   parse("program-unit/program_2.f90");
   }

   @Test public void program_3() {
	   parse("program-unit/program_3.f90");
   }

   @Test public void program_4() {
	   parse("program-unit/program_4.f90");
   }

   @Test public void program_5() {
	   parse("program-unit/program_5.f90");
   }

   @Test public void module() {
	   parse("program-unit/module.f90");
   }

}
