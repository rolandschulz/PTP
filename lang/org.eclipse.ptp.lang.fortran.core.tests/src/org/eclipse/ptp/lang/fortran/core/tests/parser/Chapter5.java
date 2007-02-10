package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.eclipse.ptp.lang.fortran.core.parser.FortranMain;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Chapter5 {
	
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
	                   Boolean.FALSE,
	                   error.booleanValue());
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

