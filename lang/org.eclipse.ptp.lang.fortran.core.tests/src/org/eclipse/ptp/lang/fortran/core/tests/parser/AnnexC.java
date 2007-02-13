package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.eclipse.ptp.lang.fortran.core.parser.FortranMain;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AnnexC {
	
   public static final String TEST_ROOT = "../org.eclipse.ptp.lang.fortran.core.tests/parser-test-files/";

   private void parse(String file) {
      Boolean error = null;
      String path = TEST_ROOT + file;

      try {
         FortranMain fortran = new FortranMain(path);
         error = fortran.call();
      } catch(Exception e) {
	 //        e.printStackTrace();
         error = new Boolean(true);
      } 
	      
      assertEquals("Fortran Parser: unsuccessfully parsed " + file,
                   false, error.booleanValue());
   }
	   
   
   @Test public void c_3_1_0() {
      parse("annex_c/c_3_1_0.f03");
   }

   @Test public void c_3_1_1() {
      parse("annex_c/c_3_1_1.f03");
   }

   @Test public void c_3_1_2() {
      parse("annex_c/c_3_1_2.f03");
   }

   @Test public void c_3_2_0() {
      parse("annex_c/c_3_2_0.f03");
   }

   @Test public void c_4_4() {
	      parse("annex_c/c_4_4.f03");
	   }

   @Test public void c_4_5() {
	      parse("annex_c/c_4_5.f03");
	   }

   @Test public void c_4_6() {
	      parse("annex_c/c_4_6.f03");
	   }

   @Test public void c_5_3_1() {
	      parse("annex_c/c_5_3_1.f03");
	   }

   @Test public void c_5_3_2() {
      parse("annex_c/c_5_3_2.f03");
   }

   @Test public void c_5_3_3() {
	      parse("annex_c/c_5_3_3.f03");
	   }

   @Test public void c_5_3_4() {
	      parse("annex_c/c_5_3_4.f03");
	   }

   @Test public void c_5_3_5() {
      parse("annex_c/c_5_3_5.f03");
   }

   @Test public void c_5_3_6() {
      parse("annex_c/c_5_3_6.f03");
   }

   @Test public void c_5_3_7() {
      parse("annex_c/c_5_3_7.f03");
   }
}// end class AnnexC

