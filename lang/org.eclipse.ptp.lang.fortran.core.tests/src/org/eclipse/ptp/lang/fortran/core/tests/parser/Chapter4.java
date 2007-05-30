package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.eclipse.ptp.lang.fortran.core.parser.FortranMain;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Chapter4 {
	
   public static final String TEST_ROOT = "../org.eclipse.ptp.lang.fortran.core.tests/parser-test-files/";

   private void parse(String file) {
	      Boolean error = Boolean.FALSE;
	      String path = TEST_ROOT + file;
	      String action = "org.eclipse.ptp.lang.fortran.core.parser.FortranParserActionNull";

	      try {
	    	  FortranMain fortran = new FortranMain(new String[0], path, action);
	    	  error = fortran.call();
	      } catch(Exception e) {
	    	  //e.printStackTrace();
	    	  error = Boolean.TRUE;
	      } 
	      
	      assertEquals("Fortran Parser: unsuccessfully parsed " + file,
	                   Boolean.FALSE,
	                   error.booleanValue());
	   }
	   
   
	@Test public void note_4_6() {
		parse("chapter_4/note_4.6.f03");
	}

	@Test public void note_4_8() {
		parse("chapter_4/note_4.8.f03");
	}

	@Test public void note_4_9() {
		parse("chapter_4/note_4.9.f03");
	}

	@Test public void note_4_10() {
		parse("chapter_4/note_4.10.f03");
	}

	@Test public void note_4_12() {
		parse("chapter_4/note_4.12.f03");
	}

	@Test public void note_4_14() {
		parse("chapter_4/note_4.14.f03");
	}

	@Test public void note_4_16() {
		parse("chapter_4/note_4.16.f03");
	}

	@Test public void note_4_17() {
		parse("chapter_4/note_4.17.f03");
	}

	@Test public void note_4_18() {
		parse("chapter_4/note_4.18.f03");
	}

	@Test public void note_4_19() {
		parse("chapter_4/note_4.19.f03");
	}

	@Test public void note_4_21() {
		parse("chapter_4/note_4.21.f03");
	}

	@Test public void note_4_22() {
		parse("chapter_4/note_4.22.f03");
	}

	@Test public void note_4_24() {
		parse("chapter_4/note_4.24.f03");
	}

	@Test public void note_4_25() {
		parse("chapter_4/note_4.25.f03");
	}

	@Test public void note_4_28() {
		parse("chapter_4/note_4.28.f03");
	}

	@Test public void note_4_29() {
		parse("chapter_4/note_4.29.f03");
	}

	@Test public void note_4_31() {
		parse("chapter_4/note_4.31.f03");
	}

	@Test public void note_4_33() {
		parse("chapter_4/note_4.33.f03");
	}

	@Test public void note_4_34() {
		parse("chapter_4/note_4.34.f03");
	}

	@Test public void note_4_35() {
		parse("chapter_4/note_4.35.f03");
	}

	@Test public void note_4_36() {
		parse("chapter_4/note_4.36.f03");
	}

	@Test public void note_4_37() {
		parse("chapter_4/note_4.37.f03");
	}

	@Test public void note_4_40() {
		parse("chapter_4/note_4.40.f03");
	}

	@Test public void note_4_41() {
		parse("chapter_4/note_4.41.f03");
	}

	@Test public void note_4_42() {
		parse("chapter_4/note_4.42.f03");
	}

	@Test public void note_4_50() {
		parse("chapter_4/note_4.50.f03");
	}

	@Test public void note_4_54() {
		parse("chapter_4/note_4.54.f03");
	}

	@Test public void note_4_55() {
		parse("chapter_4/note_4.55.f03");
	}

	@Test public void note_4_57() {
		parse("chapter_4/note_4.57.f03");
	}

	@Test public void note_4_58() {
		parse("chapter_4/note_4.58.f03");
	}

	@Test public void note_4_59() {
		parse("chapter_4/note_4.59.f03");
	}

	@Test public void note_4_60() {
		parse("chapter_4/note_4.60.f03");
	}

	@Test public void note_4_65() {
		parse("chapter_4/note_4.65.f03");
	}

	@Test public void note_4_67() {
		parse("chapter_4/note_4.67.f03");
	}

	@Test public void note_4_68() {
		parse("chapter_4/note_4.68.f03");
	}

	@Test public void note_4_69() {
		parse("chapter_4/note_4.69.f03");
	}

	@Test public void note_4_70() {
		parse("chapter_4/note_4.70.f03");
	}

	@Test public void note_4_71() {
		parse("chapter_4/note_4.71.f03");
	}

	@Test public void note_4_72() {
		parse("chapter_4/note_4.72.f03");
	}

} // end class Chapter8
