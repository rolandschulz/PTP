package org.eclipse.ptp.lang.fortran.core.tests.parser;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.eclipse.ptp.lang.fortran.core.parser.FrontEnd;

public class MPI {
	
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
					 Boolean.FALSE,
					 error.booleanValue());
	}
   
	@Test public void mpi_send() {
		parse("mpi/mpi_send.f90");
	}

}
