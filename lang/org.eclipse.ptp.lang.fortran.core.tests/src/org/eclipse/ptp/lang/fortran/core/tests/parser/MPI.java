package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.antlr.runtime.*;
import org.eclipse.ptp.lang.fortran.core.parser.FortranLexer;
import org.eclipse.ptp.lang.fortran.core.parser.FortranParser;
import org.eclipse.ptp.lang.fortran.core.parser.FortranStream;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.eclipse.ptp.lang.fortran.core.parser.FortranMain;

public class MPI {
	
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
   
	@Test public void mpi_send() {
		parse("mpi/mpi_send.f90");
	}

}
