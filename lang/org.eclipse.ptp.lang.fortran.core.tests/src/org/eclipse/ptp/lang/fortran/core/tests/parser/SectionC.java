package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.eclipse.ptp.lang.fortran.core.parser.FortranMain;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class SectionC {
	
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

	@Test public void C_3_1() {
		parse("section_c/C.3.1.f03");
	}

	@Test public void C_3_2() {
		parse("section_c/C.3.2.f03");
	}

	@Test public void C_3_3() {
		parse("section_c/C.3.3.f03");
	}

	@Test public void C_4_4() {
		parse("section_c/C.4.4.f03");
	}

	@Test public void C_4_5() {
		parse("section_c/C.4.5.f03");
	}

	@Test public void C_4_6() {
		parse("section_c/C.4.6.f03");
	}

	@Test public void C_5_3() {
		parse("section_c/C.5.3.f03");
	}

}
