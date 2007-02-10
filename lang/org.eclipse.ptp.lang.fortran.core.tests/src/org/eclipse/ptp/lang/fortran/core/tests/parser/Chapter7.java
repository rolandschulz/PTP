package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.eclipse.ptp.lang.fortran.core.parser.FortranMain;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Chapter7 {
	
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
	                   error);
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

