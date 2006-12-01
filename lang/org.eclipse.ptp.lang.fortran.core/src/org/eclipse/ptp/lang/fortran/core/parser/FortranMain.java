package org.eclipse.ptp.lang.fortran.core.parser;
import java.io.*;
import org.antlr.runtime.*;

// figured out what to write for a main() by looking at the generated 
// stuff for antlrworks.  
public class FortranMain {

   public static void main(String args[]) throws Exception {
//       FortranLexer lexer = 
//          new FortranLexer(new ANTLRFileStream(args[0]));
      FortranLexer lexer = 
         new FortranLexer(new FortranStream(args[0]));
//       CommonTokenStream tokens = new CommonTokenStream(lexer);
      FortranTokenStream tokens = new FortranTokenStream(lexer);
      FortranParser parser = new FortranParser(tokens);
      boolean error = false;

      try {
         parser.program();
         error = parser.hasErrorOccurred;
      } catch(RecognitionException e) {
         e.printStackTrace();
         System.exit(1);
         error = true;
      } 

      if(error != false) {
         System.out.println("Parser failed");
         System.exit(1);
      } else {
         System.out.println("Parser exiting normally");
      }

   }// end main()
}//end class FortranMain
