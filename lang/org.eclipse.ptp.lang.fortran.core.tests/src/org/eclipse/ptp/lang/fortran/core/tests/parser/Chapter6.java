package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.eclipse.ptp.lang.fortran.core.parser.FrontEnd;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Chapter6 {
	
   public static final String TEST_ROOT = "../org.eclipse.ptp.lang.fortran.core.tests/parser-test-files/";

   private void parse(String file) {
      Boolean error = Boolean.FALSE;
      String path = TEST_ROOT + file;
      String action = "org.eclipse.ptp.lang.fortran.core.parser.FortranParserActionNull";

      try {
         FrontEnd fortran = new FrontEnd(new String[0], path, action);
         error = fortran.call();
      } catch(Exception e) {
	 //        e.printStackTrace();
         error = Boolean.TRUE;
      } 
	      
      assertEquals("Fortran Parser: unsuccessfully parsed " + file,
                   Boolean.FALSE, error.booleanValue());
   }
	   
   
   @Test public void note_6_1() {
      parse("chapter_6/note_6.1.f03");
   }

   @Test public void note_6_5() {
      parse("chapter_6/note_6_5.f03");
   }

   @Test public void note_6_9() {
      parse("chapter_6/note_6_9.f03");
   }

   @Test public void note_6_11() {
      parse("chapter_6/note_6_11.f03");
   }

   @Test public void note_6_17() {
      parse("chapter_6/note_6_17.f03");
   }

   @Test public void note_6_19() {
      parse("chapter_6/note_6_19.f03");
   }

   @Test public void note_6_22() {
      parse("chapter_6/note_6_22.f03");
   }

   @Test public void note_6_24() {
      parse("chapter_6/note_6_24.f03");
   }

   @Test public void note_6_25() {
      parse("chapter_6/note_6_25.f03");
   }
}// end class Chapter6

