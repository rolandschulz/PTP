package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.antlr.runtime.*;
import org.eclipse.ptp.lang.fortran.core.parser.FortranLexer;
import org.eclipse.ptp.lang.fortran.core.parser.FortranParser;
import org.eclipse.ptp.lang.fortran.core.parser.FortranStream;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Chapter5 {
	
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
   
   @Test public void note_5_17() {
      parse("chapter_5/note_5.17.f03");
   }

   @Test public void note_5_19() {
      parse("chapter_5/note_5.19.f03");
   }

   @Test public void note_5_25() {
      parse("chapter_5/note_5.25.f03");
   }

   @Test public void note_5_26() {
      parse("chapter_5/note_5.26.f03");
   }

   @Test public void note_5_27() {
      parse("chapter_5/note_5.27.f03");
   }

   @Test public void note_5_28() {
      parse("chapter_5/note_5.28.f03");
   }

   @Test public void note_5_29() {
      parse("chapter_5/note_5.29.f03");
   }
}// end class Chapter5

