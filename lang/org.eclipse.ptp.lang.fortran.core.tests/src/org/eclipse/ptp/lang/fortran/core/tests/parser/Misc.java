package org.eclipse.ptp.lang.fortran.core.tests.parser;

import org.antlr.runtime.*;
import org.eclipse.ptp.lang.fortran.core.parser.FortranLexer;
import org.eclipse.ptp.lang.fortran.core.parser.FortranParser;
import org.eclipse.ptp.lang.fortran.core.parser.FortranStream;
import org.eclipse.ptp.lang.fortran.core.parser.FortranTokenStream;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Misc {
	
   public static final String TEST_ROOT = "../org.eclipse.ptp.lang.fortran.core.tests/parser-test-files/";

   private void parse(String file) {
      boolean error = false;
      String path = TEST_ROOT + file;

      try {
         FortranLexer lexer = new FortranLexer(new FortranStream(path));
         FortranTokenStream tokens = new FortranTokenStream(lexer);
         FortranParser parser = new FortranParser(tokens);
         parser.program();
         error = parser.hasErrorOccurred;
      } catch(Exception e) {
         error = true;
      } 
      
      assertEquals("Fortran Parser: unsuccessfully parsed " + file,
                   false, error);
   }
   
   @Test public void c_assoc() {
      parse("misc/c_assoc.f90");
   }

   @Test public void format() {
      parse("misc/format.f03");
   }

   @Test public void main_program() {
      parse("misc/main_program.f03");
   }

   @Test public void test_0() {
      parse("misc/test_0.f90");
   }

   @Test public void test_comments() {
      parse("misc/test_comments.f90");
   }

   @Test public void test_dos_new_line_eof() {
      parse("misc/test_dos_new_line_eof.f90");
   }

   @Test public void test_module() {
      parse("misc/test_module.f90");
   }

   @Test public void test_sub_0() {
      parse("misc/test_sub_0.f90");
   }

   @Test public void derived_type_def() {
      parse("misc/derived_type_def.f03");
   }

   @Test public void hello_world() {
      parse("misc/hello_world.f90");
   }

   @Test public void test_case() {
      parse("misc/test_case.f90");
   }

   @Test public void test_dos_new_line() {
      parse("misc/test_dos_new_line.f90");
   }

   @Test public void test_eos() {
      parse("misc/test_eos.f90");
   }

   @Test public void test_string_literals() {
      parse("misc/test_string_literals.f90");
   }

   @Test public void test_sub_1() {
      parse("misc/test_sub_1.f90");
   }

}// end class Misc

