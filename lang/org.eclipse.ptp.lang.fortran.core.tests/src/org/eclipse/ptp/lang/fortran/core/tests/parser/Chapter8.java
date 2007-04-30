package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.eclipse.ptp.lang.fortran.core.parser.FortranMain;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Chapter8 {
	
   public static final String TEST_ROOT = "../org.eclipse.ptp.lang.fortran.core.tests/parser-test-files/";

   private void parse(String file) {
	      Boolean error = Boolean.FALSE;
	      String path = TEST_ROOT + file;
	      String action = "org.eclipse.ptp.lang.fortran.core.parser.FortranParserActionNull";

	      try {
	    	  FortranMain fortran = new FortranMain(path, action);
	    	  error = fortran.call();
	      } catch(Exception e) {
	 //        e.printStackTrace();
	         error = Boolean.TRUE;
	      } 
	      
	      assertEquals("Fortran Parser: unsuccessfully parsed " + file,
	                   Boolean.FALSE,
	                   error.booleanValue());
	   }
	   
   	@Test public void note_8_1() {
		parse("chapter_8/note_8.1.f03");
	}
   
   @Test public void note_8_2() {
      parse("chapter_8/note_8.2.f03");
   }

   @Test public void note_8_4() {
      parse("chapter_8/note_8.4.f03");
   }

   @Test public void note_8_5() {
      parse("chapter_8/note_8.5.f03");
   }

   @Test public void note_8_6() {
      parse("chapter_8/note_8.6.f03");
   }

   @Test public void note_8_7() {
      parse("chapter_8/note_8.7.f03");
   }

   @Test public void note_8_8() {
      parse("chapter_8/note_8.8.f03");
   }

   @Test public void note_8_9() {
      parse("chapter_8/note_8.9.f03");
   }

   @Test public void note_8_10() {
      parse("chapter_8/note_8.10.f03");
   }

   @Test public void note_8_13() {
      parse("chapter_8/note_8.13.f03");
   }

   @Test public void note_8_14() {
      parse("chapter_8/note_8.14.f03");
   }

	@Test public void note_8_16() {
		parse("chapter_8/note_8.16.f03");
	}

   @Test public void note_8_17() {
      parse("chapter_8/note_8.17.f03");
   }

   @Test public void note_8_18() {
      parse("chapter_8/note_8.18.f03");
   }

}// end class Chapter8

