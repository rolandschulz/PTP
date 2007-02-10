package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.eclipse.ptp.lang.fortran.core.parser.FortranMain;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Misc {
	
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
	   
   
   @Test public void derived_type_def() {
      parse("misc/derived_type_def.f03");
   }

   @Test public void format() {
      parse("misc/format.f03");
   }

   @Test public void hello_world() {
      parse("misc/hello_world.f90");
   }

   @Test public void main_program() {
      parse("misc/main_program.f03");
   }

   @Test public void test_0() {
      parse("misc/test_0.f90");
   }

   @Test public void test_call_stmt() {
      parse("misc/test_call_stmt.f90");
   }

   @Test public void test_case() {
      parse("misc/test_case.f90");
   }

   @Test public void test_c_assoc() {
      parse("misc/test_c_assoc.f03");
   }

   @Test public void test_comments() {
      parse("misc/test_comments.f90");
   }

   @Test public void test_dos_new_line_eof_2() {
      parse("misc/test_dos_new_line_eof_2.f90");
   }

   @Test public void test_dos_new_line_eof() {
      parse("misc/test_dos_new_line_eof.f90");
   }

   @Test public void test_dos_new_line() {
      parse("misc/test_dos_new_line.f90");
   }

   @Test public void test_eos_2() {
      parse("misc/test_eos_2.f90");
   }

   @Test public void test_eos() {
      parse("misc/test_eos.f90");
   }

   @Test public void test_format() {
      parse("misc/test_format.f90");
   }

   @Test public void test_func_0() {
      parse("misc/test_func_0.f90");
   }

   @Test public void test_keyword_ids() {
      parse("misc/test_keyword_ids.f90");
   }

   @Test public void test_labels() {
      parse("misc/test_labels.f90");
   }

   @Test public void test_module() {
      parse("misc/test_module.f90");
   }

   @Test public void test_module_program() {
      parse("misc/test_module_program.f90");
   }

   @Test public void test_multi_line_1() {
      parse("misc/test_multi_line_1.f90");
   }

   @Test public void test_multi_line_2() {
      parse("misc/test_multi_line_2.f90");
   }

   @Test public void test_multi_line_3() {
      parse("misc/test_multi_line_3.f90");
   }

   @Test public void test_multi_line() {
      parse("misc/test_multi_line.f90");
   }

   @Test public void test_prepass() {
      parse("misc/test_prepass.f03");
   }

   @Test public void test_result() {
      parse("misc/test_result.f90");
   }

   @Test public void test_sales() {
      parse("misc/test_sales.f90");
   }

   @Test public void test_select_stmts() {
      parse("misc/test_select_stmts.f03");
   }

   @Test public void test_string_literals() {
      parse("misc/test_string_literals.f90");
   }

   @Test public void test_sub_0() {
      parse("misc/test_sub_0.f90");
   }

   @Test public void test_sub_1() {
      parse("misc/test_sub_1.f90");
   }
}// end class Misc

