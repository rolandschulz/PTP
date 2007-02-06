package org.eclipse.ptp.lang.fortran.core.parser;

import java.io.*;
import org.antlr.runtime.*;
import java.util.concurrent.Callable;

// figured out what to write for a main() by looking at the generated 
// stuff for antlrworks.  
public class FortranMain implements Callable<Boolean> {
	
	private FortranTokenStream		tokens;
	private FortranLexer			lexer;
	private FortranParser			parser;
	private FortranLexicalPrepass	prepass;
	
	public FortranMain(String filename) throws IOException {
		this.lexer = new FortranLexer(new FortranStream(filename));
		this.tokens = new FortranTokenStream(lexer);
		this.parser = new FortranParser(tokens);
		this.prepass = new FortranLexicalPrepass(lexer, tokens, parser);
	}
	
   private static boolean parseMainProgram(FortranTokenStream tokens, 
                                           FortranParser parser, 
                                           int start) throws Exception {
//       System.out.println("attempting to parse a main program");
      // try parsing the main program
      parser.main_program();

      return parser.hasErrorOccurred;
   }// end parseMainProgram()

   private static boolean parseModule(FortranTokenStream tokens, 
                                      FortranParser parser, int start) 
      throws Exception {
//       System.out.println("found MODULE in first line");
//       // try parsing the module
//       parser.module_stmt();
//       if(parser.hasErrorOccurred == false) {
//          // rewind tokens to include line we just looked at
//          tokens.rewind(start);
//          parser.module();
//       } else {
//          // it failed to look like block data, so try matching it as 
//          // a main program
//          tokens.rewind(start);
//          parseMainProgram(tokens, parser, start);
//       }// end if(module)/else(main program)         

      parser.module();
      return parser.hasErrorOccurred;
   }// end parseModule()

   private static boolean parseBlockData(FortranTokenStream tokens, 
                                         FortranParser parser, 
                                         int start) throws Exception {
//       System.out.println("found BLOCK in first line");
      // try parsing the block data
//       parser.block_data_stmt();
//       if(parser.hasErrorOccurred == false) {
//          // rewind tokens to include line we just looked at
//          tokens.rewind(start);
//          parser.block_data();
//       } else {
//          // it failed to look like block data, so try matching it as 
//          // a main program
//          tokens.rewind(start);
//          parseMainProgram(tokens, parser, start);
//       }// end if(block data)/else(main program)

      parser.block_data();
      
      return parser.hasErrorOccurred;
   }// end parseBlockData()      

   private static boolean parseSubroutine(FortranTokenStream tokens, 
                                          FortranParser parser, 
                                          int start) throws Exception {
//       System.out.println("found SUBROUTINE in first line");
//       // try parsing the subroutine
//       parser.subroutine_stmt();
//       if(parser.hasErrorOccurred == false) {
//          // rewind tokens to include line we just looked at
//          tokens.rewind(start);
//          parser.subroutine_subprogram();
//       } else {
//          // it failed to look like a subroutine, so try matching it as 
//          // a main program
//          tokens.rewind(start);
//          parseMainProgram(tokens, parser, start);
//       }// end if(subroutine)/else(main_program)         

      parser.subroutine_subprogram();

      return parser.hasErrorOccurred;
   }// end parserSubroutine()

   private static boolean parseFunction(FortranTokenStream tokens, 
                                        FortranParser parser, 
                                        int start) throws Exception {
//       System.out.println("found FUNCTION in first line");
//       // try parsing the function
//       parser.ext_function_stmt_test();
//       if(parser.hasErrorOccurred == false) {
//          // reset the token input since the test for 
//          // matching function_stmt moves token index ptr.
//          tokens.rewind(start);
//          parser.ext_function_subprogram();
//       } else {
//          // it failed to look like a function, so try matching it as 
//          // a main program
//          tokens.rewind(start);
//          parseMainProgram(tokens, parser, start);
//       }// end if(function)/else(main_program)

      parser.ext_function_subprogram();
      return parser.hasErrorOccurred;
   }// end parseFunction()

   private static boolean parseProgramUnit(FortranLexer lexer, 
                                           FortranTokenStream tokens, 
                                           FortranParser parser) 
      throws Exception {
      int firstToken;
      int lookAhead = 1;
      int start;
      boolean error = false;

      // first token on the *line*.  will check to see if it's
      // equal to module, block, etc. to determine what rule of 
      // the grammar to start with.
      try {
         lookAhead = 1;
         do {
            firstToken = tokens.LA(lookAhead);
//             System.out.println("firstToken is: " + firstToken);
            lookAhead++;
         } while(firstToken == FortranLexer.LINE_COMMENT || 
                 firstToken == FortranLexer.T_EOS);
         
         // mark the location of the first token we're looking at
         start = tokens.mark();
         
         // attempt to match the program unit
         // each of the parse routines called will first try and match
         // the unit they represent (function, block, etc.).  if that 
         // fails, they may or may not try and match it as a main
         // program; it depends on how it fails.
         //
         // due to Sale's algorithm, we know that if the token matches
         // then the parser should be able to successfully match.
         if(firstToken != FortranLexer.EOF) {
            if(firstToken == FortranLexer.T_MODULE &&
               tokens.LA(lookAhead) != FortranLexer.T_PROCEDURE) {
               // try matching a module
               error = parseModule(tokens, parser, start);
            } else if(firstToken == FortranLexer.T_BLOCK ||
                      firstToken == FortranLexer.T_BLOCKDATA) {
               // try matching block data
               error = parseBlockData(tokens, parser, start);
            } else if(firstToken == FortranLexer.T_SUBROUTINE) {
               // try matching a subroutine
               error = parseSubroutine(tokens, parser, start);
            } else if(tokens.testForFunction() == true) {
               // try matching a function
               error = parseFunction(tokens, parser, start);
            } else {
               // what's left should be a main program
               error = parseMainProgram(tokens, parser, start);
            }// end else(unhandled token)
         }// end if(file had nothing but comments empty)
      } catch(RecognitionException e) {
         e.printStackTrace();
         error = true;
      }// end try/catch(parsing program unit)
      
      return error;
   }// end parseProgramUnit()

   public static void main(String args[]) throws Exception {
      new FortranMain(args[0]).call();
   }// end main()
   
   public Boolean call() throws Exception {
	   boolean error = false;
	      
	   // apply Sale's algorithm to the tokens to allow keywords
	   // as identifiers.  also, fixup labeled do's, etc.
	      prepass.performPrepass();

	      // overwrite the old token stream with the (possibly) modified one
	      tokens.finalizeTokenStream();
	         
	      // parse each program unit in a given file
	      while(tokens.LA(1) != FortranLexer.EOF) {
	         // attempt to parse the current program unit
	         error = parseProgramUnit(lexer, tokens, parser);
	         
	         // see if we successfully parse the program unit or not
	         if(error != false) {
	            System.out.println("Parser failed");
	            System.exit(1);
	         } else {
	            System.out.println("Parser exiting normally");
	         }// end else(parser exited normally)
	      }// end while(not end of file) 

	      return new Boolean(error);
	   }// end main()

}//end class FortranMain
