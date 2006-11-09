package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.antlr.runtime.*;
import org.eclipse.ptp.lang.fortran.core.parser.FortranLexer;
import org.eclipse.ptp.lang.fortran.core.parser.FortranParser;
import org.eclipse.ptp.lang.fortran.core.parser.FortranStream;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Chapter7 {
	
   public static final String TEST_ROOT = "../org.eclipse.ptp.lang.fortran.core.tests/parser-test-files/";

   private void parse(String file) {
      boolean error = false;
      String path = TEST_ROOT + file;

      try {
         FortranLexer lexer = new FortranLexer(new FortranStream(path));
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         FortranParser parser = new FortranParser(tokens);
         parser.program();
         error = parser.hasErrorOccurred;
      } catch(Exception e) {
         error = true;
      } 
      
      assertEquals("Fortran Parser: unsuccessfully parsed " + file,
                   false, error);
   }
   
   @Test public void note_7_1() {
      parse("chapter_7/note_7.1.f03");
   }

   @Test public void note_7_2() {
      parse("chapter_7/note_7.2.f03");
   }

   @Test public void note_7_3() {
      parse("chapter_7/note_7.3.f03");
   }
}// end class Chapter7

