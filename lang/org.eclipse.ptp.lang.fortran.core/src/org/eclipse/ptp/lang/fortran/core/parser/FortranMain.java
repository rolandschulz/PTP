/**
 * Copyright (c) 2005, 2006 Los Alamos National Security, LLC.  This
 * material was produced under U.S. Government contract DE-
 * AC52-06NA25396 for Los Alamos National Laboratory (LANL), which is
 * operated by the Los Alamos National Security, LLC (LANS) for the
 * U.S. Department of Energy. The U.S. Government has rights to use,
 * reproduce, and distribute this software. NEITHER THE GOVERNMENT NOR
 * LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY
 * LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified to
 * produce derivative works, such modified software should be clearly
 * marked, so as not to confuse it with the version available from
 * LANL.
 *  
 * Additionally, this program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.ptp.lang.fortran.core.parser;

import java.io.*;
import java.lang.reflect.Constructor;

import org.antlr.runtime.*;
import java.util.concurrent.Callable;

// figured out what to write for a main() by looking at the generated 
// stuff for antlrworks.  
public class FortranMain implements Callable<Boolean> {
	
   private FortranTokenStream              tokens;
   private FortranLexer                    lexer;
   private FortranParser                   parser;
   private FortranLexicalPrepass           prepass;
   private String fileName;
   private int sourceForm;
   private boolean verbose = true;

   public static final int UNKNOWN_SOURCE_FORM = -1;
   public static final int FREE_FORM = 1;
   public static final int FIXED_FORM = 2;
        
   public FortranMain(String filename, String type) throws IOException {
      this.lexer = 
         new FortranLexer(new FortranStream(filename, this.determineSourceForm(filename))); 
      this.tokens = new FortranTokenStream(lexer);
      this.parser = new FortranParser(tokens, type, filename);
      this.prepass = new FortranLexicalPrepass(lexer, tokens, parser);
      this.fileName = filename;
      this.sourceForm = UNKNOWN_SOURCE_FORM;
   }

	
   private static boolean parseMainProgram(FortranTokenStream tokens, 
                                           FortranParser parser, 
                                           int start) throws Exception {
      // try parsing the main program
      parser.main_program();

      return parser.hasErrorOccurred;
   }// end parseMainProgram()

   private static boolean parseModule(FortranTokenStream tokens, 
                                      FortranParser parser, int start) 
      throws Exception {
      parser.module();
      return parser.hasErrorOccurred;
   }// end parseModule()

   private static boolean parseBlockData(FortranTokenStream tokens, 
                                         FortranParser parser, 
                                         int start) throws Exception {
      parser.block_data();
      
      return parser.hasErrorOccurred;
   }// end parseBlockData()      

   private static boolean parseSubroutine(FortranTokenStream tokens, 
                                          FortranParser parser, 
                                          int start) throws Exception {
      parser.subroutine_subprogram();

      return parser.hasErrorOccurred;
   }// end parserSubroutine()

   private static boolean parseFunction(FortranTokenStream tokens, 
                                        FortranParser parser, 
                                        int start) throws Exception {
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
      Boolean error = null;
      Boolean verbose = true;
      String type = "null";
      int nArgs = 0;

      for (int i = 0; i < args.length; i++) {
	      if (args[i].startsWith("--dump")) {
	          type = "dump";
	          nArgs += 1;
	      } else if (args[i].startsWith("--silent")) {
	    	  verbose = false;
	    	  nArgs += 1;
	      } else if (args[i].startsWith("--class")) {
	          i += 1;
	          type = args[i];
	          nArgs += 2;
	      }
      }
      
      if (args.length <= nArgs) {
    	  System.out.println("Usage: java FortranMain [--dump] [--class className] file1 [file2..fileN]");
      }

      for (int i = 0; i < args.length; i++) {
    	  if (args[i].startsWith("--class")) {
    		  i += 1;
    		  continue;
    	  } else if (args[i].startsWith("--")) {
    		  continue;
    	  }
    	  if (verbose) {
    		  System.out.println("********************************************");
    		  System.out.println("args[" + i + "]: " + args[i]);
    	  }
    	  
    	  FortranMain ofp = new FortranMain(args[i], type);
    	  ofp.setVerbose(verbose);
    	  if (ofp.getParser().getAction().getClass().getName() == "parser.java.FortranParserActionPrint") {
    		  FortranParserActionPrint action = (FortranParserActionPrint) ofp.getParser().getAction();
    		  action.setVerbose(verbose);
    	  }
    	  error = ofp.call();
    	  
    	  if (verbose) {
    		  System.out.println("********************************************");
    	  }
      }

   }// end main()
   
   public void setVerbose(boolean flag) {
	   this.verbose = flag;
   }
   
   public FortranParser getParser() {
	   return this.parser;
   }
   
   public Boolean call() throws Exception {
      boolean error = false;
              
      if(determineSourceForm(this.fileName) == FIXED_FORM)
         if (verbose) System.out.println(this.fileName + " is FIXED FORM");
      else
         if (verbose) System.out.println(this.fileName + " is FREE FORM");

      // determine whether the file is fixed or free form and 
      // set the source form in the prepass so it knows how to handle lines.
      prepass.setSourceForm(determineSourceForm(this.fileName));

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
         } else {
            if (verbose) System.out.println("Parser exiting normally");
         }// end else(parser exited normally)
      }// end while(not end of file) 

      return new Boolean(error);
   }// end call()

   private int determineSourceForm(String fileName) {
      if(fileName.endsWith(new String(".f")) == true ||
         fileName.endsWith(new String(".F")) == true) {
         this.sourceForm = FIXED_FORM;
         return FIXED_FORM;
      } else {
         this.sourceForm = FREE_FORM;
         return FREE_FORM;
      }
   }// end determineSourceForm()


   public int getSourceForm() {
      return this.sourceForm;
   }

}//end class FortranMain
