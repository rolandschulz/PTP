package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.eclipse.ptp.lang.fortran.core.parser.FortranMain;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Chapter7 {
	
   public static final String TEST_ROOT = "../org.eclipse.ptp.lang.fortran.core.tests/parser-test-files/";

   private void parse(String file) {
	      Boolean error = Boolean.FALSE;
	      String path = TEST_ROOT + file;
	      String action = "org.eclipse.ptp.lang.fortran.core.parser.FortranParserActionNull";

	      try {
	    	  FortranMain fortran = new FortranMain(new String[0], path, action);
	    	  error = fortran.call();
	      } catch(Exception e) {
	 //        e.printStackTrace();
	         error = Boolean.TRUE;
	      } 
	      
	      assertEquals("Fortran Parser: unsuccessfully parsed " + file,
	                   Boolean.FALSE,
	                   error.booleanValue());
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

   @Test public void note_7_10() {
      parse("chapter_7/note_7.10.f03");
   }

   @Test public void note_7_11() {
      parse("chapter_7/note_7.11.f03");
   }

   @Test public void note_7_43() {
      parse("chapter_7/note_7.43.f03");
   }

   @Test public void note_7_44() {
      parse("chapter_7/note_7.44.f03");
   }

   @Test public void note_7_45() {
      parse("chapter_7/note_7.45.f03");
   }

   @Test public void note_7_47() {
      parse("chapter_7/note_7.47.f03");
   }

   @Test public void note_7_48() {
      parse("chapter_7/note_7.48.f03");
   }

   @Test public void note_7_49() {
      parse("chapter_7/note_7.49.f03");
   }

   @Test public void note_7_50() {
      parse("chapter_7/note_7.50.f03");
   }

   @Test public void note_7_51() {
      parse("chapter_7/note_7.51.f03");
   }

   @Test public void note_7_55() {
      parse("chapter_7/note_7.55.f03");
   }

   @Test public void note_7_56() {
      parse("chapter_7/note_7.56.f03");
   }

   @Test public void note_7_57() {
      parse("chapter_7/note_7.57.f03");
   }
}// end class Chapter7

