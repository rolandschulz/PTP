package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.eclipse.ptp.lang.fortran.core.parser.FortranMain;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Chapter6 {
	
   public static final String TEST_ROOT = "../org.eclipse.ptp.lang.fortran.core.tests/parser-test-files/";

   private void parse(String file) {
      Boolean error = Boolean.FALSE;
      String path = TEST_ROOT + file;

      try {
         FortranMain fortran = new FortranMain(path);
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
      parse("chapter_6/note_6.5.f03");
   }

   @Test public void note_6_9() {
      parse("chapter_6/note_6.9.f03");
   }

   @Test public void note_6_11() {
      parse("chapter_6/note_6.11.f03");
   }

   @Test public void note_6_17() {
      parse("chapter_6/note_6.17.f03");
   }

   @Test public void note_6_19() {
      parse("chapter_6/note_6.19.f03");
   }

   @Test public void note_6_22() {
      parse("chapter_6/note_6.22.f03");
   }

   @Test public void note_6_24() {
      parse("chapter_6/note_6.24.f03");
   }

   @Test public void note_6_25() {
      parse("chapter_6/note_6.25.f03");
   }
}// end class Chapter6

