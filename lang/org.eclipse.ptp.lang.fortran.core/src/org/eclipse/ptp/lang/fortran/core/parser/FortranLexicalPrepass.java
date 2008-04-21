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
import java.util.*;
import org.antlr.runtime.*;

public class FortranLexicalPrepass {
   private FortranLexer lexer;
   private FortranTokenStream tokens;
   private FortranParser parser;
   private Stack<Token> doLabels;
   private int sourceForm;

   public FortranLexicalPrepass(FortranLexer lexer, 
                                FortranTokenStream tokens, 
                                FortranParser parser) {
      this.lexer = lexer;
      this.tokens = tokens;
      this.parser = parser;
      this.doLabels = new Stack<Token>();
   }


   public void setSourceForm(int sourceForm) {
      this.sourceForm = sourceForm;
   }// end setSourceForm()


	private boolean isAssignment(int start, int end) {
		if(tokens.getToken(start).getType() == FortranLexer.T_ASSIGNMENT &&
			(start+3 < end) &&  
			tokens.getToken(start+1).getType() == FortranLexer.T_LPAREN &&
			tokens.getToken(start+2).getType() == FortranLexer.T_EQUALS)
			return true;
		else
			return false;
	}// end isAssignment()


	private boolean isOperator(int start, int end) {
		if(tokens.getToken(start).getType() == FortranLexer.T_OPERATOR &&
			(start+3 < end) && 
			tokens.getToken(start+1).getType() == FortranLexer.T_LPAREN &&
			tokens.getToken(start+2).getType() == FortranLexer.T_DEFINED_OP &&
			tokens.getToken(start+3).getType() == FortranLexer.T_RPAREN)
			return true;
		else 
			return false;
	}// end isOperator()


   private void convertToIdents(int start, int end) {
      int i;
      Token tmpToken;

      for(i = start; i < end; i++) {
         // get the token 
         tmpToken = tokens.getToken(i);

         // this should not happen, but just in case..
         if(tmpToken == null) {
            System.out.println("convertToIdents(): couldn't retrieve token");
            System.out.println("start: " + start + " end: " + end + 
                               " i: " + i);
            tokens.printCurrLine();
            System.exit(1);
         }
         if(lexer.isKeyword(tmpToken) == true) {
				// Do not convert the ASSIGNMENT(=) or OPERATOR(T_DEFINED_OP)
            if(isAssignment(i, end) == false &&
					isOperator(i, end) == false) {
               tmpToken.setType(FortranLexer.T_IDENT);
				}
         }
      }// end for(number of tokens in line)
      return;
   }// end convertToIdents()


   /**
    * TODO: Need to finish this to skip over anything in quotes and hollerith 
    * constants.  
    * Actually, the lexer already sucks up quotes (single and double) into the 
    * T_CHAR_CONSTANT tokens that it creates, so no need to consider here.
    */
   public int salesScanForToken(int start, int desiredToken) {
      int lookAhead = 0;
      int tmpToken;
      int parenOffset;
      int quoteOffset;

      // if this line is a comment, skip scanning it
      if(tokens.currLineLA(1) == FortranLexer.LINE_COMMENT)
         return -1;
      
      // start where the user says to
      lookAhead = start;
      do {
         // lookAhead was initialized to 0
         lookAhead++;

         // get the token and consume it (advances token index)
         tmpToken = tokens.currLineLA(lookAhead);

         // if have a left paren, find the matching right paren.  must 
         // add one to lookAhead for starting index because 
         // lookAhead is 0 based indexing and currLineLA() needs 1 based.
         if(tmpToken == FortranLexer.T_LPAREN ||
            tmpToken == FortranLexer.T_LBRACKET) {
            parenOffset = tokens.findToken(lookAhead-1, FortranLexer.T_LPAREN);
            parenOffset++;
            // lookAhead should be the exact lookAhead of where we found
            // the LPAREN or LBRACKET.  
            lookAhead = matchClosingParen(start, lookAhead);
            tmpToken = tokens.currLineLA(lookAhead);
//
// TODO - fix this for removal of token.
// This was removed because T_BIND_LPAREN_C token was removed and replaced by
// T_BIND T_LPAREN T_IDENT [='c'|'C']
//
//          } else if(tmpToken == FortranLexer.T_BIND_LPAREN_C) {
          } else if(tmpToken == FortranLexer.T_BIND) {

            parenOffset = tokens.findToken(lookAhead-1, 
//                  FortranLexer.T_BIND_LPAREN_C);
                  FortranLexer.T_BIND);
            // we need to advance by two.  parenOffset returned above is the 
            // raw location of the T_BIND.  we need to skip past it to be on
            // the LPAREN.  then, we add one more to convert the offset of the 
            // LPAREN into the lookAhead of the LPAREN, which is needed by 
            // the matchClosingParen routine.
            parenOffset+=2;
            lookAhead = matchClosingParen(lookAhead+1, parenOffset);
            tmpToken = tokens.currLineLA(lookAhead);
         }
      } while(tmpToken != FortranLexer.EOF && 
              tmpToken != FortranLexer.T_EOS && tmpToken != desiredToken);

      if(tmpToken == desiredToken)
         // we found a what we wanted to
         // have to subtract one because 0 based indexing 
         return lookAhead-1;
         
      return -1;
   }// end salesScanForToken()


   private boolean matchIfConstStmt(int lineStart, int lineEnd) {
      int tokenType;
      int rparenOffset = -1;
      int commaOffset = -1;

      // lineStart should be the physical index of the start (0, etc.)
      // currLinLA() is 1 based, so must add one to everything
      tokenType = tokens.currLineLA(lineStart+1);
      if(tokenType == FortranLexer.T_IF &&
         tokens.currLineLA(lineStart+2) == FortranLexer.T_LPAREN) {
         rparenOffset = matchClosingParen(lineStart+2, lineStart+2);
         commaOffset = salesScanForToken(rparenOffset+1, FortranLexer.T_COMMA);
         if(rparenOffset == -1) {
            System.err.println("Error in IF stmt at line: " + 
                               tokens.getToken(0).getLine());
            return false;
         }
            
         // if we have a T_THEN token, everything between if and then are ids
         // this is an if_construct in the grammar
         if(tokens.currLineLA(rparenOffset+1) == FortranLexer.T_THEN) {
            convertToIdents(lineStart+1, rparenOffset);

            // match an if_construct
            return true;
         } else if(commaOffset != -1 &&
                   tokens.currLineLA(rparenOffset+1) 
                   == FortranLexer.T_DIGIT_STRING) {
            // The arithmetic if requires a label T_COMMA label
            // T_COMMA label.  We can distinguish between
            // arithmetic_if_stmt and if_stmt by verifying that the
            // first thing after the T_RPAREN is a label, and it is
            // immediately followed by a T_COMMA
            
            // (label)? T_IF T_LPAREN expr T_RPAREN label T_COMMA label 
            // T_COMMA label T_EOS
            // convert everything after T_IF to ident if necessary
            convertToIdents(lineStart+1, rparenOffset);
            // insert a token into the start of the line to signal that this
            // is an arithmetic if and not an if_stmt so the parser doesn't
            // have to backtrack for action_stmt.  
            // 02.05.07
            tokens.addToken(lineStart, FortranLexer.T_ARITHMETIC_IF_STMT, 
                            "__T_ARITHMETIC_IF_STMT__");

            // matched an arithemetic if
            return true;
         } else {
            // TODO: must be an if_stmt, which is matched elsewhere (for now..)
            return false;
         }
      }

      return false;
   }// end matchIfConstStmt()

   
   private boolean matchElseStmt(int lineStart, int lineEnd) {
      int tokenType;
      boolean isElseIf = false;

      // lineStart should be physical index to start (0 based).  add 1 to 
      // make it one based.
      tokenType = tokens.currLineLA(lineStart+1);
      if(tokenType == FortranLexer.T_ELSE) {
         // see if there are any tokens following the else
         if(lineEnd >= 2) {
            if(tokens.currLineLA(lineStart+2) == FortranLexer.T_WHERE) {
               // ELSE WHERE stmt.  anything after these two are idents
               convertToIdents(lineStart+2, lineEnd);
            } else {
               // need to see if there is an if stmt to handle, starting 
               // at the  else location (lineStart+1)
               isElseIf = matchIfConstStmt(lineStart+1, lineEnd);
            }
         }

         return true;
      }
      return false;
   }// end matchElseStmt()

   
   private boolean matchDataDecl(int lineStart, int lineEnd) {
      int tokenType;

      tokenType = tokens.currLineLA(1);
      if(isIntrinsicType(tokenType) == true ||
			isPrefixToken(tokenType)  ||
         ((tokenType == FortranLexer.T_TYPE || 
           tokenType == FortranLexer.T_CLASS) &&
          tokens.currLineLA(lineStart+2) == FortranLexer.T_LPAREN)) {

		// If a subroutine, then this is handled elsewhere.
		if(isSubDecl(lineStart, lineEnd)) {
			return false;
		}

		// Test to see if it's a function decl.  If it is not, then
		// it has to be a data decl
		if(isFuncDecl(lineStart, lineEnd) == true) {
			fixupFuncDecl(lineStart, lineEnd);
		}

		else {
			// should have a variable declaration here
			fixupDataDecl(lineStart, lineEnd);
		}

         // We either matched a data decl or a function, but either way, 
         // the line has been matched.
         return true;

      } else if(tokenType == FortranLexer.T_FUNCTION) {
         // could be a function defn. that starts with the function keyword
         // instead of the type.  fix it up.
         fixupFuncDecl(lineStart, lineEnd);
         return true;
      }
      
      // didn't match the line.
      return false;
   }// end matchDataDecl()


   /**
    * Note:
    * 'TYPE IS' part of a 'SELECT TYPE' statement is matched here because
    * there isn't a way to know which one it is.
    */ 
   private boolean matchDerivedTypeStmt(int lineStart, int lineEnd) {
      int colonOffset;
      Token identToken = null;
      int identOffset;

      // make sure it's a derived type defn, and not a declaration!
      if(tokens.currLineLA(lineStart+1) == FortranLexer.T_TYPE &&
         tokens.currLineLA(lineStart+2) != FortranLexer.T_LPAREN) {
         // we have a derived type defn.
         colonOffset = tokens.findToken(lineStart, FortranLexer.T_COLON_COLON);
         if(colonOffset != -1) {
            // there was a double colon; ident immediately follows it
            identOffset = colonOffset+1;
            // we know that it is not a 'TYPE IS' inside a 'SELECT TYPE'
            // convert everything after :: to idents
            convertToIdents(identOffset, lineEnd);
         } else {
            // offset lineStart+1 is the second token
            identToken = tokens.getToken(lineStart+1);
            identOffset = lineStart+1;
            // make sure the name is an identifier
            if(lexer.isKeyword(identToken) == true) {
               identToken.setType(FortranLexer.T_IDENT);
            }
            // see if there are parens after the type name.  if there
            // are, we're looking at a 'TYPE IS' and need to handle the
            // derived_type_spec or intrinsic_type_spec
            // note: we're guaranteed to have at least 3 tokens 
            if(tokens.currLineLA(lineStart+3) == FortranLexer.T_LPAREN) {
               int rparenOffset;
               // matchClosingParen returns the lookAhead (1 based); 
               // we want the offset (0 based), so subtract 1 from it.
               rparenOffset = 
                  matchClosingParen(lineStart+2, lineStart+3) - 1;
               // if the third token is a left paren, we have a 'type is'
               // and need to figure out what the type_spec is
               if(isIntrinsicType(tokens.currLineLA(lineStart+4)) 
                  == true) {
                  // we can't change the intrinsic type, but have to handle
                  // the optional kind selector, if given.
                  // fixup the intrinsic_type_spec, which is the 
                  // third token
                  fixupDeclTypeSpec(lineStart+3, lineEnd);
               } else {
                  // we have a 'type is' with a derived type name, so 
                  // convert everything on line to idents after '('
                  convertToIdents(lineStart+3, lineEnd);
               }// end else

               // have to see if a label is after the right paren and
               // convert it to an ident if necessary
               // lineEnd is 1 based; rparenOffset 0 based.  convert 
               // lineEnd to 0 based before testing
               if((lineEnd-1) > (rparenOffset+1)) {
                  // rparenOffset 0 based; convert to 1 based to get it's
                  // lookAhead value, then lookAhead 1 more to see what
                  // follows it (i.e., rparenOffset+2 is desired lookAhead)
                  if(lexer.isKeyword(tokens.currLineLA(rparenOffset+2))
                     == true) {
                     tokens.getToken(rparenOffset+1).
                        setType(FortranLexer.T_IDENT);
                  }
               }
            }// end if(is a 'type is')
         }// end else(no :: is derived-type-stmt)

         return true;
      }
            
      return false;
   }// end matchDerivedTypeStmt()

   
   private boolean matchSub(int lineStart, int lineEnd) {
      int tokenType;
      int bindOffset;

		// Move past the pure, elemental, and recursive keywords.
		while ( isPrefixToken(tokens.currLineLA(lineStart+1)) )
			lineStart++;

      tokenType = tokens.currLineLA(lineStart+1);
      // look for a bind statement
// TODO - fix for T_BIND token
//      bindOffset = tokens.findToken(lineStart, FortranLexer.T_BIND_LPAREN_C);
      bindOffset = tokens.findToken(lineStart, FortranLexer.T_BIND);
      if(bindOffset != -1) {
         // use the T_BIND_LPAREN_C token as a marker for the end 
         // of the subroutine name and any args.
         convertToIdents(lineStart+1, bindOffset+lineStart);
      } else {
         // convert any keyword in line after first token to ident
         convertToIdents(lineStart+1, lineEnd);
      }

      return true;
   }// end matchSub()

   
   /**
    * Match the various types of end statments.  For example: END, 
    * ENDSUBROUTINE, ENDDO, etc.
    */
   private boolean matchEnd(int lineStart, int lineEnd) {
      int tokenType;
      int identOffset;
      boolean matchedEnd = false;
      boolean isEndDo = false;

      // initialize to -1.  if we find a T_END, this will be set to 
      // the location of the identifier, if given.
      identOffset = -1;

      tokenType = tokens.currLineLA(lineStart+1);
      if(tokenType == FortranLexer.T_END) {
         if(lineEnd > 2) {
            if(tokens.currLineLA(lineStart+2) == FortranLexer.T_BLOCK)
               identOffset = lineStart+3;
            else if(tokens.currLineLA(lineStart+2) == 
                    FortranLexer.T_INTERFACE) {
               // have to accept a generic_spec
               identOffset = matchGenericSpec(lineStart+2, lineEnd);
            } else
               // identifier is after the T_END and T_<construct>
               identOffset = lineStart+2;
         } 

         // we have to fixup the END DO if it's labeled
         if(tokens.currLineLA(lineStart+2) == FortranLexer.T_DO)
            isEndDo = true;

         matchedEnd = true;
      } else if(tokenType == FortranLexer.T_ENDBLOCK) {
         // T_DATA must follow
         identOffset = lineStart+2;
         
         matchedEnd = true;
      } else if(tokenType == FortranLexer.T_ENDINTERFACE) {
         identOffset = matchGenericSpec(lineStart+1, lineEnd);
      } else {
         if(lineEnd > 1) 
            identOffset = lineStart+1;
         matchedEnd = true;
      }

      if(identOffset != -1) {
         // only converting one thing, so not necessary to use a method..
         convertToIdents(identOffset, lineEnd);
      } 

      // have to fixup a labeled END DO
      if(isEndDo == true || tokenType == FortranLexer.T_ENDDO) {
         fixupLabeledEndDo(lineStart, lineEnd);
      }

      return matchedEnd;
   }// end matchEnd()


   /**
    * Note: This must occur after checking for a procedure declaration!
    */
   private boolean matchModule(int lineStart, int lineEnd) {
      // convert everything after module to an identifier 
      convertToIdents(lineStart+1, lineEnd);
      return true;
   }// end matchModule()


   private boolean matchBlockData(int lineStart, int lineEnd) {
      // there should be a minimum of 2 tokens 
      // T_BLOCK T_DATA (T_IDENT)? T_EOS
      // T_BLOCKDATA (T_IDENT)? T_EOS
      // do a quick check
      if(lineEnd < (lineStart+2))
         return false;

      if(tokens.currLineLA(lineStart+1) == FortranLexer.T_BLOCK) {
         if(tokens.currLineLA(lineStart+2) == FortranLexer.T_DATA) {
            // T_BLOCK T_DATA (T_IDENT)? T_EOS
            if((lineEnd >= (lineStart+3)) &&
               lexer.isKeyword(tokens.currLineLA(lineStart+3)) == true) {
               // lookAhead 3 is index 2
               tokens.getToken(lineStart+2).setType(FortranLexer.T_IDENT);
            }
            // successfully matched a block data stmt
            return true;
         }

         // unsuccessfully matched a block data stmt
         return false;
      } else if(tokens.currLineLA(lineStart+1) == FortranLexer.T_BLOCKDATA) {
         if(lexer.isKeyword(tokens.currLineLA(lineStart+2)) == true) {
            // lookAhead 2 is index 1
            tokens.getToken(lineStart+1).setType(FortranLexer.T_IDENT);
         }
         // successfully matched a block data stmt
         return true;
      } else {
         // unsuccessfully matched a block data stmt
         return false;
      }
   }// end matchBlockData()


   private boolean matchUseStmt(int lineStart, int lineEnd) {
      int identPos;
      int colonOffset;
      Token tmpToken;
      Token onlyToken = null;

      // search for the only token, so we can reset it to a keyword
      // if it's there.
      colonOffset = tokens.findToken(lineStart, FortranLexer.T_COLON_COLON);
      if(colonOffset != -1) {
         // everything after the double colons must be treated as ids
         identPos = colonOffset+1;
      } else {
         // no double colon, so ident starts after the 'use' token
         identPos = lineStart+1;
      }

      // convert what we need to to idents
		if(lexer.isKeyword(tokens.currLineLA(identPos+1)))
			// the module name is a keyword so convert it.
			tokens.getToken(identPos).setType(FortranLexer.T_IDENT);

		// Skip past the module name.
		identPos++;

		// See if anything follows the module name
		if(identPos < lineEnd) {
			// see if we have an only clause
			if(tokens.currLineLA(identPos+1) == FortranLexer.T_COMMA && 
				tokens.currLineLA(identPos+2) == FortranLexer.T_ONLY)
				// Skip the T_COMMA, T_ONLY, and T_COLON.
				identPos+=3;
			
			// Convert everything following the module name and optional
			// T_ONLY (if given) to an identifier if necessary.
			convertToIdents(identPos, lineEnd);
		}

      // matched a use stmt
      return true;
   }// end matchUseStmt()


   /**
    * This depends on the handling of multi-line statements.  This 
    * function assumes that the T_EOS tokens in a multi-line statement
    * are removed for all lines except the last.  This allows this 
    * function to simply test if the first token on the line is
    * a digit string.
    */
   private boolean matchLabel(int lineStart, int lineEnd) {
      // assume that if the line starts with a digit string, it
      // must be a label.  this requires that the T_EOS is removed 
      // in all lines of a multi-line statement, except for the last!
      if(tokens.currLineLA(1) == FortranLexer.T_DIGIT_STRING) 
         return true;
      else
         return false;
   }// end matchLabel()


   private boolean matchIdentColon(int lineStart, int lineEnd) {
      int secondToken;

      secondToken = tokens.currLineLA(lineStart+2);
      if(secondToken == FortranLexer.T_COLON) {
         // line starts with the optional T_IDENT and T_COLON
         if(lexer.isKeyword(tokens.currLineLA(lineStart+1)) == true) {
            // convert keyword to T_IDENT
            tokens.getToken(lineStart).setType(FortranLexer.T_IDENT);
         }
         return true;
      }

      return false;
   }// end matchIdentColon()

   
   /**
    * Try matching a procedure statement.  
    * Note: This MUST be called BEFORE calling matchModule().
    * Also, procedure statements can only occur w/in an interface block.
    */
   private boolean matchProcStmt(int lineStart, int lineEnd) {
      int identOffset = -1;
      
      // make sure we have enough tokens
      if(lineEnd < (lineStart+2))
         return false;

      if(tokens.currLineLA(lineStart+1) == FortranLexer.T_PROCEDURE &&
         tokens.currLineLA(lineStart+2) != FortranLexer.T_LPAREN) {
         // T_PROCEDURE ...
         int colonOffset = -1;
         colonOffset = tokens.findToken(lineStart+1, 
                                        FortranLexer.T_COLON_COLON);
         if(colonOffset != -1) {
            identOffset = colonOffset+1;
         } else {
            identOffset = lineStart+1;
         }
      } else if(tokens.currLineLA(lineStart+1) == FortranLexer.T_MODULE &&
              tokens.currLineLA(lineStart+2) == FortranLexer.T_PROCEDURE) {
         // a module stmt has at most 3 tokens after the optional label:
         // T_MODULE (T_IDENT)? T_EOS
         // but a procedure stmt must have at least 4:
         // T_MODULE T_PROCEDURE generic_name_list T_EOS
         if(lineEnd < (lineStart+4))
            // it is a module stmt
            return false;
         identOffset = lineStart+2;
      }

      if(identOffset != -1) {
         convertToIdents(identOffset, lineEnd);
         return true;
      } else {
         return false;
      }
   }// end matchProcStmt()


   /**
    * Try matching a procedure declaration statement.  
    * Note: This is NOT for procedure statements, and MUST be called AFTER 
    * trying to match a procedure statement.
    */
   private boolean matchProcDeclStmt(int lineStart, int lineEnd) {
      int lParenOffset;
      int rParenOffset;
      int colonOffset;

      if(tokens.currLineLA(lineStart+1) == FortranLexer.T_PROCEDURE) {
         // found a procedure decl.  need to find the parens
			// The left paren should be the next token.
         lParenOffset = lineStart+1;
         rParenOffset = matchClosingParen(lineStart, lParenOffset+1);

			// Don't convert proc-interface since it can be a 
			// declaration-type-spec.

         // double colons, if there, must come after the T_RPAREN
         colonOffset = 
            tokens.findToken(rParenOffset+1, FortranLexer.T_COLON_COLON);
         
         if(colonOffset != -1) {
            // idents start after the double colons
            convertToIdents(colonOffset+1, lineEnd);
         } else {
            // idents start after the T_RPAREN
            convertToIdents(rParenOffset+1, lineEnd);
         }
         
         return true;
      }

      return false;
   }// end matchProcDeclStmt()


   private boolean matchAttrStmt(int lineStart, int lineEnd) {
      int firstToken;
      int identOffset = -1;

      firstToken = tokens.currLineLA(lineStart+1);
      if(firstToken == FortranLexer.T_INTENT) {
         int lParenOffset;
         lParenOffset = tokens.findToken(lineStart+1, FortranLexer.T_LPAREN);
         identOffset = matchClosingParen(lineStart, lParenOffset+1);
//      } else if(firstToken == FortranLexer.T_BIND_LPAREN_C) {
      // TODO - fix for T_BIND token
      } else if(firstToken == FortranLexer.T_BIND) {
         int rParenOffset;
         
         // find the closing paren, starting at first location after the
         // left paren.  what follows it is optional :: and the ident(s).
         // the T_BIND and T_LPAREN are the first two tokens, so lineStart+2
         // puts you on the lookahead for LPAREN, which is the starting point
         // for the matching routine.
         rParenOffset = matchClosingParen(lineStart, lineStart+2);
         // rParenOffset will be at the location following the T_RPAREN
         identOffset = rParenOffset;
      } else if(firstToken == FortranLexer.T_PARAMETER) {
         int lParenOffset;
         // match a parameter stmt
         lParenOffset = tokens.findToken(lineStart+1, FortranLexer.T_LPAREN);
         if(lParenOffset == -1) {
            System.err.println("Syntax error in PARAMETER statement");
            System.exit(1);
         }
         // idents start after the T_LPAREN and stop at the T_RPAREN
         identOffset = lParenOffset;
         lineEnd = matchClosingParen(lineStart, lParenOffset+1);
      } else if(firstToken == FortranLexer.T_IMPLICIT) {
         int lparenOffset = -1;
         int rparenOffset = -1;

         // fixup an implicit statement.  search for the T_NONE.  
         // if given, nothing needs updated because it's an IMPLICIT NONE
         if(tokens.currLineLA(lineStart+2) != FortranLexer.T_NONE) {
            do {
               lparenOffset = 
                  tokens.findToken(lineStart, FortranLexer.T_LPAREN);
               if(lparenOffset != -1) {
                  rparenOffset = matchClosingParen(lineStart, lparenOffset+1);
                  // the first set of parens could be the optional kind 
                  // selector, or it is the letter designators for the 
                  // implicit stmt.  either way, we can convert anything 
                  // that's not T_KIND or T_LEN to an ident because T_KIND 
                  // and T_LEN can only appear in the kind selector.  then, 
                  // we don't need to look for an optional second paren set.
                  for(int i = lparenOffset; i < rparenOffset; i++) {
                     if(lexer.isKeyword(tokens.currLineLA(i+1)) &&
                        tokens.currLineLA(i+1) != FortranLexer.T_KIND &&
                        tokens.currLineLA(i+1) != FortranLexer.T_LEN) {
                        tokens.getToken(i).setType(FortranLexer.T_IDENT);
                     }
                  }

                  // there could be another set of parens, if the first set 
                  // (above) was for the kind selector, then the second set 
                  // would be for the letter designator(s) (required).
                  if(tokens.currLineLA(rparenOffset+1) == 
                     FortranLexer.T_LPAREN) {
                     rparenOffset = 
                        matchClosingParen(lineStart, rparenOffset+1);
                  }

                  // reset the lineStart so we can accept a an 
                  // implicit_spec_list
                  lineStart = rparenOffset;
               }
            } while(lineStart < lineEnd && 
                    tokens.currLineLA(lineStart+1) != FortranLexer.T_EOS);
         }
      } else {
         identOffset = lineStart+1;
      }

      if(identOffset != -1) {
         convertToIdents(identOffset, lineEnd);
         return true;
      } else {
         return false;
      }
   }// end matchAttrStmt()


   private int matchClosingParen(int lineStart, int offset) {
      int lookAhead = 0;
      int tmpTokenType;
      int nestingLevel = 0;

      // offset is the location of the LPAREN
      lookAhead = offset;
      // The parenLevel starts at one because we've matched the 
      // left paren before calling this method.
      nestingLevel = 1;  
      do {
         lookAhead++;
         tmpTokenType = tokens.currLineLA(lookAhead);
         if(tmpTokenType == FortranLexer.T_LPAREN ||
            tmpTokenType == FortranLexer.T_LBRACKET)
            nestingLevel++;
         else if(tmpTokenType == FortranLexer.T_RPAREN ||
                 tmpTokenType == FortranLexer.T_RBRACKET)
            nestingLevel--;

         // handle the error condition of the user not giving the 
         // closing paren(s)
         if((tmpTokenType == FortranLexer.T_EOS || 
             tmpTokenType == FortranLexer.EOF) &&
            nestingLevel != 0) {
            System.err.println("Error: matchClosingParen(): Missing " +
                               "closing paren on line " + 
                               tokens.getToken(lookAhead-1).getLine() + ":");
            System.err.println("nestingLevel: " + nestingLevel);
            System.err.println("lookAhead is: " + lookAhead);
            tokens.printPackedList();
            System.exit(1);
         }

         // have to continue until we're no longer in a nested
         // paren, and find the matching closing paren
      } while((nestingLevel != 0) || 
              (tmpTokenType != FortranLexer.T_RPAREN && 
               tmpTokenType != FortranLexer.T_RBRACKET &&
               tmpTokenType != FortranLexer.T_EOS && 
               tmpTokenType != FortranLexer.EOF));

      if(tmpTokenType == FortranLexer.T_RPAREN ||
         tmpTokenType == FortranLexer.T_RBRACKET)
         return lookAhead;

      return -1;
   }// end matchClosingParen()


   private int fixupDeclTypeSpec(int lineStart, int lineEnd) {
      int kindOffsetEnd = -1;

      // see if we have a derived type
      if(tokens.currLineLA(lineStart+1) == FortranLexer.T_TYPE ||
         tokens.currLineLA(lineStart+1) == FortranLexer.T_CLASS) {
         int rparenOffset = -1;
         // left-paren is next token (or we're in trouble)
         if(tokens.currLineLA(lineStart+2) != FortranLexer.T_LPAREN) {
            System.err.println("Derived type or Class declaration error!");
            System.exit(1);
         }
         rparenOffset = matchClosingParen(lineStart, lineStart+2);
         // convert anything between the (..) to idents
         convertToIdents(lineStart+1, rparenOffset);

         // change it to being 0 based indexing
         return rparenOffset-1;
      } else if(tokens.currLineLA(lineStart+2) == FortranLexer.T_LPAREN) {
         int kindTokenOffset = -1;
         int lenTokenOffset = -1;
         kindOffsetEnd = 
            matchClosingParen(lineStart, 
                              tokens.findToken(lineStart, 
                                               FortranLexer.T_LPAREN)+1);
         kindTokenOffset = tokens.findToken(lineStart+1, 
                                            FortranLexer.T_KIND);
         lenTokenOffset = tokens.findToken(lineStart+1, FortranLexer.T_LEN);

         convertToIdents(lineStart+1, kindOffsetEnd);

         if(kindTokenOffset != -1 && kindTokenOffset < kindOffsetEnd &&
            tokens.currLineLA(kindTokenOffset+2) == FortranLexer.T_EQUALS) {
            tokens.getToken(kindTokenOffset).setType(FortranLexer.T_KIND);
         }
         if(lenTokenOffset != -1 && lenTokenOffset < kindOffsetEnd &&
            tokens.currLineLA(lenTokenOffset+2) == FortranLexer.T_EQUALS) {
            tokens.getToken(lenTokenOffset).setType(FortranLexer.T_LEN);
         }

         // it is already 0 based??
         return kindOffsetEnd-1;
      } else if(tokens.currLineLA(lineStart+1) == FortranLexer.T_DOUBLE) {
         // return 0 based index of second token, which is lineStart+1
         lineStart = lineStart+1;
      }
      
      return lineStart;
   }// end fixupDeclTypeSpec()

   /**
    * TODO:: this could also be for a function, so need to handle 
    * that!!
    */
   private void fixupDataDecl(int lineStart, int lineEnd) {
      int tmpTokenType;
      int identOffset;
      Token tmpToken;

      // we know the line started with an intrinsic typespec, so 
      // now, we need to find the identifier(s) involved and convert 
      // any of them that are keyword to identifiers.

      // fixup the decl type spec part (which handles any kind selector)
      lineStart = fixupDeclTypeSpec(lineStart, lineEnd);
      identOffset = tokens.findToken(lineStart, FortranLexer.T_COLON_COLON);
      if(identOffset != -1) {
         // found the :: so the idents start at identOffset+1
         identOffset++;
      } else {
         // no kind selector and no attributes, so ident(s) should 
         // be the next token (0 based indexing)
         identOffset = lineStart+1;
      }

      // now we have the location of the ident(s).  simply loop 
      // across any tokens left in this line and convert keywords
      // to idents.
      convertToIdents(identOffset, lineEnd);
      
      return;
   }// end fixupDataDecl()


   /**
    * TODO:: make this handle the result clause and bind(c) attribute!
    */
   private void fixupFuncDecl(int lineStart, int lineEnd) {
      int identOffset;
      int identEndOffset;
      int resultOffset;
      int bindOffset;
      int newLineStart = 0;
      Token resultToken = null;
      Token bindToken = null;

      // fixup the kind selector, if given
      newLineStart = fixupDeclTypeSpec(lineStart, lineEnd);
      // bump lineStart to next token if it was modified above
      if(newLineStart != lineStart)
         lineStart = newLineStart+1;

      // find location of T_FUNCTION; identifiers start one past it
      identOffset = tokens.findToken(lineStart, FortranLexer.T_FUNCTION)+1;
      // find locations of result clause and bind(c), if exist
      // use the scan function so that it will skip any tokens inside 
      // of parens (which, in this case, would make them args)
      resultOffset = salesScanForToken(lineStart, FortranLexer.T_RESULT);
//      bindOffset = salesScanForToken(lineStart, FortranLexer.T_BIND_LPAREN_C);
// TODO - fix for T_BIND token
      bindOffset = salesScanForToken(lineStart, FortranLexer.T_BIND);
      
      // get the actual tokens for result and bind(c)
      if(resultOffset != -1) {
         resultToken = tokens.getToken(resultOffset);
      }
      if(bindOffset != -1) {
         bindToken = tokens.getToken(bindOffset);
      }
      
      // convert all keywords after the T_FUNCTION to identifers to 
      // make it easier, and to make sure we catch the result clause id
      // then, afterwards, reset the type of the result and bind tokens
      convertToIdents(identOffset, lineEnd);
      if(resultToken != null) {
         resultToken.setType(FortranLexer.T_RESULT);
      }
      if(bindToken != null) {
         // this one probably not necessary because i don't think it
         // is actually considered a keyword by lexer.isKeyword()
//          bindToken.setType(FortranLexer.T_BIND_LPAREN_C);
// TODO - fix for T_BIND token
          bindToken.setType(FortranLexer.T_BIND);
      }
 
      return;
   }// end fixupFuncDecl()


   private boolean isIntrinsicType(int type) {
      if(type == FortranLexer.T_INTEGER ||
         type == FortranLexer.T_REAL ||
         type == FortranLexer.T_DOUBLE ||
         type == FortranLexer.T_DOUBLEPRECISION ||
         type == FortranLexer.T_COMPLEX ||
         type == FortranLexer.T_CHARACTER ||
         type == FortranLexer.T_LOGICAL)
         return true;
      else
         return false;
   }// end isIntrinsicType()


   /**
    * Find the first index after the typespec (with or without the optional
    * kind selector).
    */
   private int skipTypeSpec(int lineStart) {
      int firstToken;
      int rparenOffset = -1;

      firstToken = tokens.currLineLA(lineStart+1);
      if(isIntrinsicType(firstToken) == true ||
         firstToken == FortranLexer.T_TYPE) {
         // if the first token is T_DOUBLE, we are expecting one more token
         // to finish the type, so bump the lineStart one more.
         if(firstToken == FortranLexer.T_DOUBLE) {
            lineStart++;
         }

         // see if the next token is a left paren -- means either a kind 
         // selector or a type declaration.
         if(tokens.currLineLA(lineStart+2) == FortranLexer.T_LPAREN) {
            // will return logical index of rparen.  this is not zero 
            // based!  it is based on look ahead, which starts at 1!
            // therefore, if it is 4, it's really at offset 3 in the 
            // packed list array, but is currLineLA(4)!
            rparenOffset = matchClosingParen(lineStart, lineStart+2);
         }
         
         if(rparenOffset != -1) 
            // rparenOffset will be the logical index of the right paren.
            // if it's token 4 in packedList, which is 0 based, it's actual
            // index is 3, but 4 is returned because we need 1 based for LA()
            lineStart = rparenOffset;
         else {
            lineStart = lineStart+1;
         }
         return lineStart;
      } else {
         // it wasn't a typespec, so return original start.  this should 
         // not happen because this method should only be called if we're
         // looking at a typespec!
         return lineStart;
      }
   }// end skipTypeSpec()


	// Skip whole prefix, not just the type declaration.
	private int skipPrefix(int lineStart) {

		// First, skip over the pure, elemental, recursive tokens.
		// Then skip type spec.
		// Then skip over the pure, elemental, recursive tokens again
		while ( isPrefixToken(tokens.currLineLA(lineStart+1)) )
			lineStart++;
		
		lineStart = skipTypeSpec(lineStart);

		while ( isPrefixToken(tokens.currLineLA(lineStart+1)) )
			lineStart++;

		return lineStart;

   }// end skipPrefix()

	// Test to see if a token is one of pure, elemental, or recursive.
	private boolean isPrefixToken(int token) {
      if(token == FortranLexer.T_PURE ||
         token == FortranLexer.T_ELEMENTAL ||
         token == FortranLexer.T_RECURSIVE)
         return true;
      else
         return false;
   }// end isIntrinsicType()

   private boolean isFuncDecl(int lineStart, int lineEnd) {

		// have to skip over any kind selector
		lineStart = skipPrefix(lineStart);

      // Here, we know the first token is one of the intrinsic types.
      // Now, look at the second token to see if it is T_FUNCTION.
      // If it is, AND a keyword/identifier immediately follows it, 
      // then this cannot be a data decl and must be a function decl.
      if(tokens.currLineLA(lineStart+1) == FortranLexer.T_FUNCTION) {
         if(tokens.currLineLA(lineStart+2) == FortranLexer.T_IDENT ||
            (lexer.isKeyword(tokens.currLineLA(3))))
            return true;
      } 

      return false;
   }// end isFuncDecl()

	// True if this is a subroutine declaration.
	private boolean isSubDecl(int lineStart, int lineEnd) {

		// Skip the prefix.
		lineStart = skipPrefix(lineStart);

		// Look at the first token to see if it is T_SUBROUTINE.
		// If it is, AND a keyword/identifier immediately follows it, 
		// then this is a subroutine declaration.
		if(tokens.currLineLA(lineStart+1) == FortranLexer.T_SUBROUTINE) {
			if(tokens.currLineLA(lineStart+2) == FortranLexer.T_IDENT ||
				lexer.isKeyword(tokens.currLineLA(lineStart+2)) ){
				return true;
			}
		}

		return false;

	}	// end isSubDecl()

   private boolean isValidDataEditDesc(String line, int lineIndex) {
      char firstChar;
      char secondChar = '\0';

      // need the first char in the string
      firstChar = Character.toLowerCase(line.charAt(lineIndex));
      if(lineIndex < line.length()-1)
         secondChar = Character.toLowerCase(line.charAt(lineIndex+1));

      // TODO: there should be a more efficient way to do this!!
      if(firstChar == 'i' || (firstChar == 'b' && secondChar != 'n' &&
                              secondChar != 'z') ||
         firstChar == 'o' || firstChar == 'z' || firstChar == 'f' || 
         firstChar == 'g' || firstChar == 'l' || firstChar == 'a' || 
         (firstChar == 'd' && ((secondChar == 't') ||
                               isDigit(secondChar))) ||
         (firstChar == 'e' && (secondChar == 'n' || secondChar == 's' ||
                               isDigit(secondChar)))) {
         // T_IDENT represents a valid data-edit-desc
         return true;
      }

      return false;
   }// end isValidDataEditDesc()


   private int findFormatItemEnd(String line, int lineIndex) {
      char currChar;
      int lineLength;
      
      lineLength = line.length();
      do {
         currChar = line.charAt(lineIndex);
         lineIndex++;
      } while(lineIndex < lineLength && currChar != ',' && 
              currChar != ')' && currChar != '/' && currChar != ':');

      // we went one past the line terminator, so move back to it's location
      return lineIndex - 1;
   }// end findFormatItemEnd()


	private int matchVList(String line, int lineIndex) {
		int tmpLineIndex;
		int lineLength;

		/* Skip the 'dt'.  */
		tmpLineIndex = lineIndex + 2;

		lineLength = line.length();
		
		/* We could have a char-literal-constant here to skip.  */
		if(line.charAt(tmpLineIndex) == '\'' ||
			line.charAt(tmpLineIndex) == '"') {
			tmpLineIndex++;
			while(line.charAt(tmpLineIndex) != '\'' &&
					line.charAt(tmpLineIndex) != '"' &&
					tmpLineIndex < lineLength)
				tmpLineIndex++;
		}
			
		/* If we hit the end, there's an error in the line so just return.  */
		if(tmpLineIndex == lineLength)
			return lineIndex;

		/* Move off the closing quotation.  */
		if(line.charAt(tmpLineIndex) == '\'' ||
			line.charAt(tmpLineIndex) == '"')
			tmpLineIndex++;

		/* Check for optional v-list and skip if present.  */
		if(line.charAt(tmpLineIndex) == '(') {
			tmpLineIndex++;

			while(tmpLineIndex < lineLength &&
					Character.isDigit(line.charAt(tmpLineIndex)))
				tmpLineIndex++;

			if(tmpLineIndex == lineLength)
				/* There is an error in the line!  */
				return lineIndex;

			if(line.charAt(tmpLineIndex) == ')') {
				tmpLineIndex++;
				/* We successfully matched the v-list.  Return new index.  */
				return tmpLineIndex;
			} else {
				System.err.println("Error: Unable to match v-list in " +
										 "data-edit-desc!");
				return lineIndex;
			}
		}

		/* No v-list.  Return where we started.  */
		return lineIndex;
	}// end matchVList()


   private int getDataEditDesc(String line, int lineIndex, int lineEnd) {
      // see if we have a repeat specification (T_DIGIT_STRING)
      while(lineIndex < lineEnd && isDigit(line.charAt(lineIndex))) 
         lineIndex++;

      // data-edit-desc starts with a T_IDENT token, representing one of: 
      // I, B, O, Z, F, E, EN, ES, G, L, A, D, or DT
      if(isValidDataEditDesc(line, lineIndex) == true) {
			/* For DT, there can be an optional v-list.  */
			if(Character.toLowerCase(line.charAt(lineIndex)) == 'd' 
				&& Character.toLowerCase(line.charAt(lineIndex+1)) == 't') {
				/* Advance past any optional v-list.  */
				lineIndex = matchVList(line, lineIndex);
			}
         return findFormatItemEnd(line, lineIndex);
      }

      return -1;
   }// end getDataEditDesc()


   private boolean isDigit(char tmpChar) {
      if(tmpChar >= '0' && tmpChar <= '9')
         return true;
      else
         return false;
   }// end isDigit()


   private boolean isLetter(char tmpChar) {
		tmpChar = Character.toLowerCase(tmpChar);
      if(tmpChar >= 'a' && tmpChar <= 'z')
         return true;
      else
         return false;
   }


   private boolean isValidControlEditDesc(String line, int lineIndex) {
      char firstChar;
      char secondChar = '\0';

      firstChar = Character.toLowerCase(line.charAt(lineIndex));
      if(lineIndex < line.length()-1)
         secondChar = Character.toLowerCase(line.charAt(lineIndex+1));

      if(firstChar == ':' || firstChar == '/' || firstChar == 'p' || 
         firstChar == 't' || firstChar == 's' || firstChar == 'b' ||
         firstChar == 'r' || firstChar == 'd' || firstChar == 'x') {
         // more checking to do on the t, s, b, r, and d cases
         if(firstChar == 's')
				/* TODO: verify the following is true for sign-edit-desc.  */
				/* If the first char is an 'S', then it can be followed by another 
					S, a P, or nothing (the single 'S' is a sign-edit-desc).  
					However, the single 'S' must not be immediately followed by 
					another letter or number, I think.  */
				if(secondChar != 's' && secondChar != 'p' && 
					Character.isLetterOrDigit(secondChar) == true)
					return false;
         else if(firstChar == 't' && (isDigit(secondChar) != true &&
                                      secondChar != 'l' && secondChar != 'r'))
            return false;
         else if(firstChar == 'b' && (secondChar != 'n' && secondChar != 'z'))
            return false;
         else if(firstChar == 'r' && (secondChar != 'u' && secondChar != 'd' &&
                                      secondChar != 'z' && secondChar != 'n' &&
                                      secondChar != 'c' && secondChar != 'p'))
            return false;
         else if(firstChar == 'd' && (secondChar != 'c' && secondChar != 'p'))
            return false;

         return true;
      }
      
      return false;
   }// end isValidControlEditDesc()


   private int getControlEditDesc(String line, int lineIndex, int lineLength) {
      // skip the possible number before X
      while(lineIndex < lineLength &&
            (line.charAt(lineIndex) >= '0' && line.charAt(lineIndex) <= '9'))
         lineIndex++;

      if(isValidControlEditDesc(line, lineIndex) == true) {
         // include the char we're on, in case it is a '/' or ':', which can
         // be the terminating char.
         return findFormatItemEnd(line, lineIndex);
      }

      return -1;
   }// end getControlEditDesc()


   private int getCharString(String line, int lineIndex, char quoteChar) {
      char nextChar;
      // we know the first character matches the quoteChar, so look at 
      // what the next char is
      lineIndex++;
      nextChar = line.charAt(lineIndex);
      if(nextChar == '\'' || nextChar == '"')
         return getCharString(line, lineIndex, nextChar);

      do {
         lineIndex++;
         nextChar = line.charAt(lineIndex);
      } while(nextChar != '\'' && nextChar != '"');

      return lineIndex;
   }// end getCharString()


   private int getCharStringEditDesc(String line, int lineIndex, 
                                     int lineLength) {
      char quoteChar;
      int startIndex = lineIndex;

      // see if we have a repeat specification (T_DIGIT_STRING)
      while(lineIndex < lineLength && isDigit(line.charAt(lineIndex))) 
         lineIndex++;

      quoteChar = Character.toLowerCase(line.charAt(lineIndex));

      if(quoteChar == 'h') {
         // We have an H char-string-edit-desc, which is valid F90 but 
         // deleted from F03.
         if(startIndex != lineIndex) {
            // there has to be a number before the H so we know how many
            // chars to read (skip).
            return lineIndex+
               (Integer.parseInt(line.substring(startIndex, lineIndex)));
         }            
      }
      
      if(quoteChar != '\'' && quoteChar != '"')
         return -1;

      // find the end of the char string.  the lexer already verified that
      // the string was valid (it should have, at least..), but we need the
      // end so we don't consider anything w/in the string as a terminator
      lineIndex = getCharString(line, lineIndex, quoteChar);

      return findFormatItemEnd(line, lineIndex+1);
   }// end getCharStringEditDesc()


   private int parseFormatString(String line, int lineIndex, int lineNum, 
                                 int charPos) {
      int lineLength;
      int descIndex = 0;
      boolean foundClosingParen = false;

      lineLength = line.length();

      // stop before processing the closing RPAREN
      while(lineIndex < (lineLength-1) && foundClosingParen == false) {
         descIndex = getCharStringEditDesc(line, lineIndex, lineLength);
         if(descIndex == -1) {
            descIndex = getDataEditDesc(line, lineIndex, lineLength);
            if(descIndex == -1) {
               descIndex = getControlEditDesc(line, lineIndex, lineLength);
               if(descIndex != -1) {
                  // found a control-edit-desc
                  // Don't create a token if we just have a / because it 
                  // is handled below since it is also a terminating char.
                  if((descIndex - lineIndex) > 0 
                     || line.charAt(descIndex) != '/') {
                     tokens.addToken(
                        tokens.createToken(FortranLexer.T_CONTROL_EDIT_DESC, 
                                           line.substring(lineIndex, 
                                                          descIndex),
                                           lineNum, charPos));
                     charPos += line.substring(lineIndex, descIndex).length();
                  }
               }
            } else {
               // found a data-edit-desc
               tokens.addToken(
                  tokens.createToken(FortranLexer.T_DATA_EDIT_DESC, 
                                     line.substring(lineIndex, descIndex),
                                     lineNum, charPos));
               charPos += line.substring(lineIndex, descIndex).length();
            }
         } else {
            // found a char-string-edit-desc
            tokens.addToken(
               tokens.createToken(FortranLexer.T_CHAR_STRING_EDIT_DESC, 
                                  line.substring(lineIndex, descIndex),
                                  lineNum, charPos));
            charPos += line.substring(lineIndex, descIndex).length();
         }

         // need to see if we found a descriptor, or if we didn't, if we are
         // not on a LPAREN then we should be looking at a ',' or some other
         // terminating character.  this can happen in a case where the format
         // string has something of the form: (i12, /, 'hello')
         // because the '/' is a control edit desc that terminates itself, 
         // so we'd next look at the ','.
         if(descIndex != -1 || 
            (descIndex == -1 && isDigit(line.charAt(lineIndex)) == false &&
             line.charAt(lineIndex) != '(')) {
            String termString = null;

            // if we started our search on a terminating character the 
            // descIndex won't have been set, so set it here.
            if(descIndex == -1) {
               descIndex = lineIndex;
            }

            // add a token for out terminating character
            if(line.charAt(descIndex) == ',') {
               termString = new String(",");
               tokens.addToken(
                  tokens.createToken(FortranLexer.T_COMMA, ",", lineNum,
                                     charPos));
            } else if(line.charAt(descIndex) == ')') {
               tokens.addToken(
                  tokens.createToken(FortranLexer.T_RPAREN, ")", lineNum,
                                     charPos));
            } else {
               if(line.charAt(descIndex) == ':') {
                  termString = new String(":");
               } else if(line.charAt(descIndex) == '/') {
                  termString = new String("/");
               } else {
                  // we have no terminator (this is allowed, apparently).
                  termString = null;
               }

               if(termString != null) {
                  // we could be using a / or : as a terminator, and they are 
                  // valid control-edit-descriptors themselves.  
                  tokens.addToken(
                     tokens.createToken(FortranLexer.T_CONTROL_EDIT_DESC, 
                                        termString, lineNum, charPos));
               }
            }

            // we're on the terminating char so bump past it
            lineIndex = descIndex+1;
         } else {
            int startIndex = lineIndex;
            // we may have a nested format stmt
            // skip over the optional T_DIGIT_STRING, but if there, add a 
            // token for it.
            while(lineIndex < lineLength && isDigit(line.charAt(lineIndex))) {
               lineIndex++;
               charPos++;
            }

            if(startIndex != lineIndex) {
               // We have a T_DIGIT_STRING in front of the nested format stmt.
               // Put a token for it in the stream.
               tokens.addToken(
                  tokens.createToken(FortranLexer.T_DIGIT_STRING, 
                                     line.substring(startIndex, lineIndex),
                                     lineNum, charPos-(lineIndex-startIndex)));
            }

            // make sure we're on a left paren
            if(line.charAt(lineIndex) == '(') {
               tokens.addToken(
                  tokens.createToken(FortranLexer.T_LPAREN, "(", lineNum, 
                                     charPos));
               charPos++;
               // move past the left paren
               lineIndex++; 
               descIndex = parseFormatString(line, lineIndex, lineNum, 
                                             charPos);
               if(descIndex == -1) {
                  System.err.println("Could not parse the format string: " + 
                                     line);
                  return -1;
               } else {
                  lineIndex = descIndex+1;
               }// end else()
            } else {
               // couldn't match anything!
               return -1;
            }
         }

         charPos++;
      }

      /* this can happen in cases where a format item is terminated with 
       * a / or :, because these are also valid control-edit-descriptors.
       * for example:
       * 004 format(//)
       * would create a T_CONTROL_EDIT_DESCRIPTOR for the last /, and then
       * advance the index to the ')'.  however, the rparen is not a format
       * item, and so is not considered in the above while loop.  that is 
       * why a T_RPAREN is added here if necessary.
       */ 
      if(lineIndex < lineLength && line.charAt(lineIndex) == ')') {
         tokens.addToken(
            tokens.createToken(FortranLexer.T_RPAREN, ")", lineNum, charPos));
         lineIndex++;
      }

      // return either the index of where we stopped parsing the format
      // sting, or a -1 if nothing was matched.  the -1 case shouldn't reach
      // here because it should get handled above when looking for a nested
      // format stmt.
      return lineIndex;
   }// end parseFormatString()


   private int fixupFormatStmt(int lineStart, int lineEnd) {
      int descIndex;
      String line;
      int lineIndex = 0;
      int i = 0;
      int lineLength = 0;
      int lineNum = 0;
      int charPos = 0;
      ArrayList<Token> origLine = new ArrayList<Token>();

      /* NOTE: the T_COMMA to separate items in a format_item_list is not 
       * always required!  See J3/04-007, pg. 221, lines 17-22
       */
      // get the lineNum that the format stmt occurs on
      lineNum = tokens.getToken(lineStart).getLine();
      lineStart++; // move past the T_FORMAT
      charPos = tokens.getToken(lineStart).getCharPositionInLine();

      if(tokens.currLineLA(lineStart+1) != lexer. T_LPAREN)
         // error in the format stmt; missing paren
         return -1;

      // get the all text left in the line as one String
      line = tokens.lineToString(lineStart, lineEnd);
//       line = line.toLowerCase();

      // make a copy of the original packed line
      origLine.addAll(tokens.getTokensList());

      // now, delete the tokens in the curr line so we can rewrite them
      tokens.clearTokensList();
      // first, copy the starting tokens to the new line (label T_FORMAT, etc.)
      for(i = 0; i < lineStart; i++)
         // adds to the end
         tokens.addToken(origLine.get(i));

      lineIndex = 0;
      lineLength = line.length();

      lineIndex = parseFormatString(line, lineIndex, lineNum, charPos);

      // terminate the newLine with a T_EOS
      tokens.addToken(
         tokens.createToken(FortranLexer.T_EOS, "\n", lineNum, 
                            charPos+lineIndex));

      // if there was an error, put the original line back
      if(lineIndex == -1) {
         System.err.println("Error in format statement " + line + 
                            " at line " + lineNum);
         tokens.clearTokensList();
         for(i = 0; i < lineEnd; i++)
            tokens.addToken(origLine.get(i));
      }
      
      return lineIndex;
   }// end fixupFormatStmt()


   private boolean matchIOStmt(int lineStart, int lineEnd) {
      int tokenType;
      int identOffset = -1;

      tokenType = tokens.currLineLA(lineStart+1);
      
      if(tokenType == FortranLexer.T_PRINT)
         if(tokens.currLineLA(lineStart+2) == FortranLexer.T_EQUALS)
            return false;
         else
            identOffset = lineStart+1;
      else {
         if(tokens.currLineLA(lineStart+2) == FortranLexer.T_LPAREN) {
            identOffset = lineStart+2;

            // fixup the inquire statement to try and help the parser not
            // have to backtrack.  for an inquire_stmt, if something other
            // than T_EOS follows the closing RPAREN, it must try and match
            // alt2.  
            if(tokenType == FortranLexer.T_INQUIRE) {
               int rparenOffset = -1;
               rparenOffset = matchClosingParen(lineStart+2, lineStart+2);
               // should not be possible for it to be -1..
               if(rparenOffset != -1 && 
                  (rparenOffset < (lineEnd-1))) {
                  if(tokens.currLineLA(rparenOffset+1) != 
                     FortranLexer.T_EOS) {
                     // add a token saying it must be alt2
                     tokens.addToken(lineStart, FortranLexer.T_INQUIRE_STMT_2,
                                     "__T_INQUIRE_STMT_2__");
                     // increment the identOffset because added token before it
                     identOffset++;
                  }
               }
            }// end if(was T_INQUIRE)
         } else if((tokenType == FortranLexer.T_FLUSH ||
                    tokenType == FortranLexer.T_REWIND) &&
                   tokens.currLineLA(lineStart+2) != FortranLexer.T_EQUALS) {
            // this is the case if you have a FLUSH/REWIND stmt w/ no parens 
            identOffset = lineStart+1;
         }
      }

      if(identOffset != -1) {
         convertToIdents(identOffset, lineEnd);

         // do the fixup after we've converted to identifiers because the
         // identOffset and lineEnd are based on the original line!
         if(tokenType == FortranLexer.T_FORMAT) 
            fixupFormatStmt(lineStart, lineEnd);

         // need to see if this has a label, and if so, see if it's needed
         // to terminate a do loop.
         if(lineStart > 0 && 
            tokens.currLineLA(lineStart) == FortranLexer.T_DIGIT_STRING)
            fixupLabeledEndDo(lineStart, lineEnd);

         return true;
      }
      else {
         return false;
      }
   }// end matchIOStmt()


   private boolean matchProgramStmt(int lineStart, int lineEnd) {
      // try to match T_PROGRAM T_IDENT T_EOS
      if(lexer.isKeyword(tokens.currLineLA(lineStart+2))) {
         // getToken is 0 based indexing; currLineLA is 1 based
         tokens.getToken(lineStart+1).setType(FortranLexer.T_IDENT);
      }
      return true;
   }// end matchProgramStmt()


	private boolean labelsMatch(String label1, String label2) {
		if(Integer.parseInt(label1) == Integer.parseInt(label2)) {
			return true;
		}
		return false;
	}// end labelsMatch()


   /**
    * Fix up a DO loop that is terminated by an action statement.  
    * TODO:: There are a number of contraints on what action statements can 
    * be used to do this, but the parser will have to check them. 
    */
   private void fixupLabeledEndDo(int lineStart, int lineEnd) {
      // if we don't have a label, return
      if(tokens.currLineLA(1) != FortranLexer.T_DIGIT_STRING)
         return;

      if(doLabels.empty() == false) {
         String doLabelString = doLabels.peek().getText();
         Token firstToken = tokens.getToken(0);
         // the lineStart was advanced past the label, so the T_CONTINUE or
         // T_END is the first token in look ahead (lineStart+1)
         int endType = tokens.currLineLA(lineStart+1);
         String labeledDoText = new String("LABELED_DO_TERM");

         if(labelsMatch(doLabelString, firstToken.getText()) == true) {
            // labels match up
            // try inserting a new token after the label. this will help 
            // the parser recognize a do loop being terminated
            tokens.addToken(1, FortranLexer.T_LABEL_DO_TERMINAL, 
                            labeledDoText);

            // need to pop off all occurrences of this label that
            // were pushed.  this can happen if one labeled action stmt
            // terminates nested do stmts.  start by popping the first one, 
            // then checking if there are any more.
            doLabels.pop();
            while(doLabels.empty() == false &&
                  (labelsMatch(doLabels.peek().getText(), 
										 firstToken.getText()) == true)) {
               // for each extra matching labeled do with this labeled end do, 
               // we need to add a T_LABEL_DO_TERMINAL to the token stream.
               // also, append a new statement for each do loop we need to 
               // terminate.  the added stmt is: 
               // label T_LABEL_DO_TERMINAL T_CONTINUE T_EOS
               if(tokens.appendToken(FortranLexer.T_DIGIT_STRING, 
                                     new String(firstToken.getText())) 
                  == false ||
                  tokens.appendToken(FortranLexer.T_LABEL_DO_TERMINAL, 
                                     labeledDoText) == false ||
                  tokens.appendToken(FortranLexer.T_CONTINUE, 
                                     new String("CONTINUE")) == false ||
                  tokens.appendToken(FortranLexer.T_EOS, null) == false) {
                  // should we exit here??
                  System.err.println("Couldn't add tokens!");
                  System.exit(1);
               }
               doLabels.pop();
            }
         }
      }
      return;
   }// end fixupLabeledEndDo()


   private boolean matchActionStmt(int lineStart, int lineEnd) {
      int tokenType;
      int identOffset = -1;

      tokenType = tokens.currLineLA(lineStart+1);
      // these all start with a keyword, but after that, rest must 
      // be idents, if applicable.  this does not care about parens, if
      // the rule calls for them.  they will be skipped, so can start 
      // conversion on their location.  this simplifies the logic.
      if(tokenType == FortranLexer.T_GO) {
         if(tokens.currLineLA(lineStart+2) != FortranLexer.T_TO)
            return false;

         // there is a space between GO and TO.  skip over the T_TO.
         identOffset = lineStart+2;
      } else if(tokenType == FortranLexer.T_ALLOCATE) {
         int colonOffset = -1;
         // allocate_stmt can have a type_spec if there is a double colon
         // search for the double colon, and if given, idents follow it.
         colonOffset = tokens.findToken(lineStart+1, 
                                        FortranLexer.T_COLON_COLON);
         if(colonOffset != -1) {
            // insert a token for the parser to know whether this is alt 1
            // or alt 2 in allocate_stmt (depends on the ::)
            tokens.addToken(lineStart, FortranLexer.T_ALLOCATE_STMT_1, 
                            "__T_ALLOCATE_STMT_1__");
            lineStart++;
            // identifiers follow the ::
            // it's +2 instead of +1 because we just inserted a new token
            identOffset = colonOffset+2;
         } else {
            identOffset = lineStart+1;
         }
      } else {
         identOffset = lineStart+1;
      }

      if(identOffset != -1) {
         convertToIdents(identOffset, lineEnd);

         // a labeled action stmt can terminate a do loop.  see if we 
         // have to fix it up (possibly insert extra tokens).
         // a number of things can't terminate a non-block DO, including
         // a goto.  
         if((lineStart > 0 &&
             tokens.currLineLA(lineStart) == FortranLexer.T_DIGIT_STRING) &&
            tokenType != FortranLexer.T_GOTO)
            fixupLabeledEndDo(lineStart, lineEnd);

         return true;
      } else {
         return false;
      }
   }// end matchActionStmt()


   private boolean matchSingleTokenStmt(int lineStart, int lineEnd) {
      int firstToken;

      firstToken = tokens.currLineLA(lineStart+1);

      // if any of these tokens starts a line, any keywords that follow 
      // must be idents.
      // ones i'm unsure about:
      // T_WHERE (assuming where_stmt is handled before this is called)
      if(firstToken == FortranLexer.T_COMMON ||
         firstToken == FortranLexer.T_EQUIVALENCE ||
         firstToken == FortranLexer.T_NAMELIST ||
         firstToken == FortranLexer.T_WHERE ||
         firstToken == FortranLexer.T_ELSEWHERE ||
         firstToken == FortranLexer.T_FORALL ||
         firstToken == FortranLexer.T_SELECT ||
         firstToken == FortranLexer.T_SELECTCASE ||
         firstToken == FortranLexer.T_SELECTTYPE ||
         firstToken == FortranLexer.T_CASE ||
         (firstToken == FortranLexer.T_CLASS &&
			 tokens.currLineLA(lineStart+2) != FortranLexer.T_DEFAULT) || 
         firstToken == FortranLexer.T_INTERFACE ||
         firstToken == FortranLexer.T_ENTRY ||
         firstToken == FortranLexer.T_IMPORT ||
         firstToken == FortranLexer.T_DATA) {
         // if we have a T_CLASS, it must be used in a select-type because
         // we should have already tried to match the T_CLASS used in a 
         // data declaration.  there appears to be no overlap between a 
         // data decl with T_CLASS and it's use here, unlike derived types..

         // if we have a T_SELECT, a T_CASE or T_TYPE must follow, 
         // then ident(s).  also, if have T_CASE T_DEFAULT, idents follow it
         if(firstToken == FortranLexer.T_SELECT ||
            (firstToken == FortranLexer.T_CASE && 
             tokens.currLineLA(lineStart+2) == FortranLexer.T_DEFAULT)) {
            convertToIdents(lineStart+2, lineEnd);
         } else if(firstToken == FortranLexer.T_INTERFACE) {
            int identOffset;
            // need to match the generic spec and then convert to idents.
            identOffset = matchGenericSpec(lineStart+1, lineEnd);

            // if matchGenericSpec fails, we won't convert anything because 
            // there is an error on the line and we'll let the parser deal 
            // with it.
            if(identOffset != -1)
               convertToIdents(identOffset, lineEnd);
         } else if(firstToken == FortranLexer.T_ENTRY) {
				// an ENTRY stmt can have a result clause, so we need to 
				// look for one.  if it does, it must have the parens after the
				// entry-name, so look for them.
				// lineStart+3 is the first token after the required entry-name.
				if(lineStart+3 < lineEnd) {
					if(tokens.currLineLA(lineStart+3) == FortranLexer.T_LPAREN) {
						int resultLA;
						resultLA = matchClosingParen(lineStart+3, lineStart+3);

						convertToIdents(lineStart+1, resultLA-1);

						// The resultLA is either the LA for T_RESULT or T_EOS.  If 
						// it is a T_RESULT, we need to convert what follows it.  
						if(tokens.currLineLA(resultLA) == FortranLexer.T_RESULT) 
							convertToIdents(resultLA, lineEnd);
					}
				} else {
					// No dummy-arg-list given.
					convertToIdents(lineStart+1, lineEnd);
				}
			} else {
            // all other cases
            convertToIdents(lineStart+1, lineEnd);
         }

         // insert token(s) to help disambiguate the grammar for the parser
         if(firstToken == FortranLexer.T_WHERE)
            tokens.addToken(lineStart, FortranLexer.T_WHERE_CONSTRUCT_STMT, 
                            "__T_WHERE_CONSTRUCT_STMT__");
         else if(firstToken == FortranLexer.T_FORALL)
            tokens.addToken(lineStart, FortranLexer.T_FORALL_CONSTRUCT_STMT,
                            "__T_FORALL_CONSTRUCT_STMT__");

         // we matched the stmt successfully
         return true;
      } 

      return false;
   }// end matchSingleTokenStmt()


   private boolean matchDoStmt(int lineStart, int lineEnd) {
      int whileOffset = -1;
      int commaOffset;
      int equalsOffset;
      int identOffset;

      // see if we can return quickly -- no expression, etc., just the
      // T_EOS next.
      if(tokens.currLineLA(lineStart+2) == FortranLexer.T_EOS)
         return true;

      // see if the next token is a label.  if so, save it so we 
      // can change the token type for the labeled continue
      if(tokens.currLineLA(lineStart+2) == FortranLexer.T_DIGIT_STRING)
         doLabels.push(new FortranToken(tokens.getToken(lineStart+1)));

      // there can be a label after the do and no loop expression.  
      if(tokens.currLineLA(lineStart+3) == FortranLexer.T_EOS)
         return true;
         
      // see if we have a T_WHILE in the loop control
      whileOffset = tokens.findToken(lineStart+1, FortranLexer.T_WHILE);
      // see if we have a while token, and see if it's part of the 
      // loop control.  if there is a T_EQUALS and T_COMMA, the T_WHILE 
      // is a T_IDENT and can not a loop in the loop control.  
      // otherwise, it must be a while loop.
      equalsOffset = salesScanForToken(lineStart+1, FortranLexer.T_EQUALS);
      if(equalsOffset != -1) {
         // we have an equals and a comma, so if there is a while, it
         // shouldn't be an identifier..
         identOffset = lineStart+1;
      } else {
         // the first T_WHILE (assuming there could be more than one) must
         // be part of the loop control and is a keyword.  so, start 
         // converting after it
         identOffset = whileOffset+1;
      }

      // convert keywords on the line to idents, starting at the identOffset
      convertToIdents(identOffset, lineEnd);

      return true;
   }// end matchDoStmt()

   
   private boolean matchOneLineStmt(int lineStart, int lineEnd) {
      int tokenType;
      int identOffset = -1;
      int rparenOffset = -1;

      // a few stmts can be one liners, such as a where-stmt.  these will
      // fail Sale's because they will have an equal sign and no comma.
      // Sale's says that it must not start w/ a keyword then, but these
      // are exceptions.  they could also have no equal and no comma, such
      // as: if(result < 0.) cycle
      
      // get the token type and determine if we have an applicable stmt
      tokenType = tokens.currLineLA(lineStart+1);
      if(tokenType == FortranLexer.T_WHERE ||
         tokenType == FortranLexer.T_IF ||
         tokenType == FortranLexer.T_FORALL) {
         // next token must be the required left paren!
         if(tokens.currLineLA(lineStart+2) == FortranLexer.T_LPAREN) {
            identOffset = lineStart+2;
            // find the right paren (end of the expression)
            rparenOffset = matchClosingParen(lineStart, lineStart+2);
            // convert anything between the parens to idents
            convertToIdents(identOffset, rparenOffset);

            // match the rest of the line (action statements)
            // the matchLine() allows for more than we should, but the
            // parser should catch those errors.  
            if(matchLine(rparenOffset, lineEnd) == false) {
               matchAssignStmt(rparenOffset, lineEnd);
            } 

            // insert a token to signal that this a one-liner statement, 
            // either a where_stmt, if_stmt, or forall_stmt.  hopefully this 
            // will allow the parser to do less backtracking.
            if(tokenType == FortranLexer.T_WHERE) {
               tokens.addToken(lineStart, FortranLexer.T_WHERE_STMT, 
                               "__T_WHERE_STMT__");
            } else if(tokenType == FortranLexer.T_IF) {
               tokens.addToken(lineStart, FortranLexer.T_IF_STMT,
                               "__T_IF_STMT__");
            } else {
               tokens.addToken(lineStart, FortranLexer.T_FORALL_STMT, 
                               "__T_FORALL_STMT__");
            }

            // a labeled action stmt can terminate a do loop.  see if we 
            // have to fix it up (possibly insert extra tokens).
            if(lineStart > 0 && 
               tokens.currLineLA(lineStart) == FortranLexer.T_DIGIT_STRING)
               fixupLabeledEndDo(lineStart, lineEnd);

            return true;
         } else {
            // didn't match the required left paren after the token
            return false;
         }
      } 

      return false;
   }// end matchOneLineStmt()


   private int matchDataRef(int lineStart, int lineEnd) {
      if(tokens.currLineLA(lineStart+1) == FortranLexer.T_IDENT ||
         lexer.isKeyword(tokens.currLineLA(lineStart+1)) == true) {
         // look to see if the next token is a paren so can skip it
         if(tokens.currLineLA(lineStart+2) == FortranLexer.T_LPAREN) {
            int tmpLineStart;

            // matchClosingParen() will give us the lookAhead required to 
            // find the RPAREN, which will be the actual offset (0 based) of 
            // the first token after the RPAREN
            tmpLineStart = matchClosingParen(lineStart, lineStart+2);
            
            // the data_ref was a function call, so reset the line start
            // to account for it and then test for the '%'
            lineStart = tmpLineStart-1;
         } 

         if(tokens.currLineLA(lineStart+2) == FortranLexer.T_PERCENT) {
            // see if the next token is a %
            return matchDataRef(lineStart+2, lineEnd);
         } else {
            // return lineStart, which is the raw index of the *last* 
            // identifier in a chain of id%id%id
            return lineStart;
         }
      } 

      return lineStart;
   }// end matchDataRef()


   private boolean matchAssignStmt(int lineStart, int lineEnd) {
      int identOffset = -1;
      int newLineStart;
      int assignType = 0;

      if(lineEnd < (lineStart+3)) {
         return false;
      }

      // advance past any '%' references in the data_ref
      newLineStart = matchDataRef(lineStart, lineEnd);

      // need to see if we have an assignment token, either as the second 
      // token, or the first token after a given ()
      if(tokens.currLineLA(newLineStart+2) == FortranLexer.T_EQUALS ||
         tokens.currLineLA(newLineStart+2) == FortranLexer.T_EQ_GT) {
         // it must be an assignment stmt.  convert the line to idents
         identOffset = lineStart;
         assignType = tokens.currLineLA(newLineStart+2);
      } else if(tokens.currLineLA(newLineStart+2) == FortranLexer.T_LPAREN) {
         int rparenOffset = -1;

         rparenOffset = matchClosingParen(newLineStart, newLineStart+2);
         if(tokens.currLineLA(rparenOffset+1) == FortranLexer.T_EQUALS ||
            tokens.currLineLA(rparenOffset+1) == FortranLexer.T_EQ_GT) {
            // matched an assignment statement (including ptr assignment)
            // convert everything on line to identifier
            identOffset = lineStart;
            assignType = tokens.currLineLA(rparenOffset+1);
         } 
      }

      // fixup the line if we found a valid ptr assignment and return true;
      // otherwise, change nothing and return false
      if(identOffset != -1) {
         // found no '%', but did find the assignment token
         convertToIdents(identOffset, lineEnd);

         // try inserting a token, before the assignment stmt, to 
         // signify what type of assignment it is.  hopefully this will allow
         // the parser to not backtrack for action_stmt.
         // 02.05.07
         if(assignType == FortranLexer.T_EQUALS) {
            tokens.addToken(lineStart, FortranLexer.T_ASSIGNMENT_STMT, 
                            "__T_ASSIGNMENT_STMT__");
         }
         else if(assignType == FortranLexer.T_EQ_GT) {
            tokens.addToken(lineStart, FortranLexer.T_PTR_ASSIGNMENT_STMT, 
                            "__T_PTR_ASSIGNMENT_STMT__");
         }

         // a labeled action stmt can terminate a do loop.  see if we 
         // have to fix it up (possibly insert extra tokens).
         if(lineStart > 0 && 
            tokens.currLineLA(lineStart) == FortranLexer.T_DIGIT_STRING)
            fixupLabeledEndDo(lineStart, lineEnd);
         

         return true;
      } else {
//          System.out.println("couldn't match assignment statement with first "
//                             + "token on line being: " + 
//                             tokens.currLineLA(lineStart+1));
         return false;
      }
   }// end matchAssignStmt()


   private int matchGenericSpec(int lineStart, int lineEnd) {
      int firstToken;
      
      firstToken = tokens.currLineLA(lineStart+1);
      if(firstToken == FortranLexer.T_OPERATOR ||
         firstToken == FortranLexer.T_ASSIGNMENT) {
         // nothing to do except skip over OPERATOR or ASSIGNMENT
         return lineStart+1;
      } else if(firstToken == FortranLexer.T_READ ||
                firstToken == FortranLexer.T_WRITE) {
         // find end of parentheses
         int rparenOffset;
         if(tokens.currLineLA(lineStart+2) != FortranLexer.T_LPAREN)
            // syntax error in the spec.  parser will report
            return -1;

         // find the rparen
         rparenOffset = matchClosingParen(lineStart, lineStart+2);
         return rparenOffset+1;
      } else {
         // generic spec is simply an identifier
         return lineStart;
      }
   }


   private boolean matchGenericBinding(int lineStart, int lineEnd) {
      if(tokens.currLineLA(lineStart+1) == FortranLexer.T_GENERIC) {
         int colonOffset;
         int nextToken;
         // search for the required ::
         colonOffset = salesScanForToken(lineStart+1, 
                                         FortranLexer.T_COLON_COLON);
         if(colonOffset == -1)
            return false;
         
         // see what we may need to convert
         // if the next token is a T_OPERATOR, T_ASSIGNMENT, T_READ, or T_WRITE
         // if so, then we have a dtio_generic_spec.
         // colonOffset is physical offset (0 based) of ::.  add one to get 
         // physical offset of next token, and 1 more for LA (1 based).
         nextToken = tokens.currLineLA(colonOffset+2);
         if(nextToken == FortranLexer.T_OPERATOR ||
            nextToken == FortranLexer.T_ASSIGNMENT) {
            convertToIdents(colonOffset+2, lineEnd);
         } else if(nextToken == FortranLexer.T_READ || 
                   nextToken == FortranLexer.T_WRITE) {
            // find end of parentheses
            int nextTokenLA = colonOffset+2;
            int rparenOffset;
            if(tokens.currLineLA(nextTokenLA+1) != FortranLexer.T_LPAREN)
               // syntax error in the spec.  parser will report
               return false;

            // find the rparen
            rparenOffset = matchClosingParen(lineStart, nextTokenLA+1);
            convertToIdents(rparenOffset+1, lineEnd);
         }
         return true;
      } else {
         return false;
      }
   }// end matchGenericBinding()


   private boolean matchLine(int lineStart, int lineEnd) {

      // determine what this line should be, knowing that it MUST
      // start with a keyword!
      if(matchDataDecl(lineStart, lineEnd) == true){
         return true;
		}
      else if(matchDerivedTypeStmt(lineStart, lineEnd) == true)
         return true;

      switch(tokens.currLineLA(lineStart+1)) {

		// If there is a function, not a subroutine, then it should have been 
		// caught by matchDataDecl.
		case FortranLexer.T_PURE:
		case FortranLexer.T_RECURSIVE:
		case FortranLexer.T_ELEMENTAL:
		case FortranLexer.T_SUBROUTINE:
			return matchSub(lineStart, lineEnd);

		// End stuff.
      case FortranLexer.T_END:
      case FortranLexer.T_ENDASSOCIATE:
      case FortranLexer.T_ENDBLOCKDATA:
      case FortranLexer.T_ENDDO:
      case FortranLexer.T_ENDENUM:
      case FortranLexer.T_ENDFORALL:
      case FortranLexer.T_ENDFILE:
      case FortranLexer.T_ENDFUNCTION:
      case FortranLexer.T_ENDIF:
      case FortranLexer.T_ENDINTERFACE:
      case FortranLexer.T_ENDMODULE:
      case FortranLexer.T_ENDPROGRAM:
      case FortranLexer.T_ENDSELECT:
      case FortranLexer.T_ENDSUBROUTINE:
      case FortranLexer.T_ENDTYPE:
      case FortranLexer.T_ENDWHERE:
      case FortranLexer.T_ENDBLOCK:
         return matchEnd(lineStart, lineEnd);
      case FortranLexer.T_PROCEDURE:
         if(matchProcStmt(lineStart, lineEnd) == true)
            return true;
         else
            return matchProcDeclStmt(lineStart, lineEnd);
      case FortranLexer.T_MODULE:
         // module procedure stmt.
         if(matchProcStmt(lineStart, lineEnd) == true)
            return true;
         else
            return matchModule(lineStart, lineEnd);
      case FortranLexer.T_BLOCK:
      case FortranLexer.T_BLOCKDATA:
         return matchBlockData(lineStart, lineEnd);
      case FortranLexer.T_USE:
         return matchUseStmt(lineStart, lineEnd);
      case FortranLexer.T_PROGRAM:
         return matchProgramStmt(lineStart, lineEnd);
      case FortranLexer.T_STOP:
      case FortranLexer.T_NULLIFY:
      case FortranLexer.T_RETURN:
      case FortranLexer.T_EXIT:
      case FortranLexer.T_WAIT:
      case FortranLexer.T_ALLOCATE:
      case FortranLexer.T_DEALLOCATE:
      case FortranLexer.T_CALL:
      case FortranLexer.T_ASSOCIATE:
      case FortranLexer.T_CYCLE:
      case FortranLexer.T_CONTINUE:
      case FortranLexer.T_GOTO:
      case FortranLexer.T_GO:  // Is this correct?  second token must be T_TO
         /* If this fails because we had a T_GO the was NOT followed by a 
            T_TO, then there isn't anything else in this method that we could
            match, so we simply need to return the failure.  The caller must 
            handle this.  */
         return matchActionStmt(lineStart, lineEnd);
      case FortranLexer.T_IF:
         if(matchIfConstStmt(lineStart, lineEnd) == true)
            return true;
         else
            return matchOneLineStmt(lineStart, lineEnd);
      case FortranLexer.T_ELSE:
         if(matchElseStmt(lineStart, lineEnd) == true)
            return true;
         else
            return matchSingleTokenStmt(lineStart, lineEnd);
      case FortranLexer.T_DO:
         return matchDoStmt(lineStart, lineEnd);
      case FortranLexer.T_CLOSE:
      case FortranLexer.T_OPEN:
      case FortranLexer.T_READ:
      case FortranLexer.T_FLUSH:
      case FortranLexer.T_REWIND:
      case FortranLexer.T_WRITE:
      case FortranLexer.T_INQUIRE:
      case FortranLexer.T_FORMAT:
      case FortranLexer.T_PRINT:
         return matchIOStmt(lineStart, lineEnd);
      case FortranLexer.T_INTENT:
      case FortranLexer.T_DIMENSION:
      case FortranLexer.T_ASYNCHRONOUS:
      case FortranLexer.T_ALLOCATABLE:
      case FortranLexer.T_PUBLIC:
      case FortranLexer.T_PRIVATE:
      case FortranLexer.T_ENUMERATOR:
      case FortranLexer.T_OPTIONAL:
      case FortranLexer.T_POINTER:
      case FortranLexer.T_PROTECTED:
      case FortranLexer.T_SAVE:
      case FortranLexer.T_TARGET:
      case FortranLexer.T_VALUE:
      case FortranLexer.T_VOLATILE:
      case FortranLexer.T_EXTERNAL:
      case FortranLexer.T_INTRINSIC:
//      case FortranLexer.T_BIND_LPAREN_C:
// TODO - fix to T_BIND token
      case FortranLexer.T_BIND:
      case FortranLexer.T_PARAMETER:
      case FortranLexer.T_IMPLICIT:
         return matchAttrStmt(lineStart, lineEnd);
      default:
         /* What's left should either be a single token stmt or failure.  */
         return matchSingleTokenStmt(lineStart, lineEnd);
      }
   }// end matchLine()


   private void fixupFixedFormatLine(int lineStart, int lineEnd, 
                                     boolean startsWithKeyword) {
      StringBuffer buffer = new StringBuffer();
      Token token;
      int i = 0;

      if(startsWithKeyword == true) {
         do {
            System.out.println("fixed-format line must start with keyword");
            tokens.printPackedList();
            buffer = buffer.append(tokens.getToken(lineStart+i).getText());
                  
            ANTLRStringStream charStream = 
               new ANTLRStringStream(buffer.toString().toUpperCase());
            FortranLexer myLexer = new FortranLexer(charStream);

//             System.out.println("trying to match the string: " + 
//                                buffer.toString().toUpperCase() + 
//                                " as keyword for fixed-format continuation");
            token = myLexer.nextToken();
//             System.out.println("lexer said next token.getText() is: " + 
//                                token.getText());
//             System.out.println("lexer said next token.getType() is: " + 
//                                token.getType());
            i++;
         } while((lineStart + i) < lineEnd &&
                 lexer.isKeyword(token.getType()) == false);

         // make sure we found something that is a keyword
         if((lineStart + i) == lineEnd) {
            System.err.println("Error: Expected keyword on line: " + 
                               token.getLine());
         } else {
            // hide all tokens that we combined to make a keyword
            int j = 0;
            Token tmpToken;

            for(j = lineStart; j < lineStart+i; j++) {
               tmpToken = tokens.getToken(j);
               tmpToken.setChannel(lexer.getIgnoreChannelNumber());
//                System.out.println("hiding token: " + tmpToken.getText() + 
//                                   " on line: " + tmpToken.getLine());
               tokens.set(j, tmpToken);
            }
            
            // add the newly created token
            tokens.add(j, token);
         } 
      } else {
         System.out.println("fixed-format line must NOT start with keyword");
      }

      return;
   }// end fixupFixedFormatLine()


   public int scanForRealConsts(int lineStart, int lineEnd) {
      int i;

      for(i = lineStart; i < lineEnd; i++) {
         if(tokens.currLineLA(i+1) == FortranLexer.T_PERIOD_EXPONENT) {
            // this case happens if the real number starts with a period, so
            // there is one token.  set the type of the T_PERIOD_EXPONENT 
            // to T_REAL_CONSTANT for the parser.
            tokens.getToken(i).setType(FortranLexer.T_REAL_CONSTANT);
         } else if(tokens.currLineLA(i+1) == FortranLexer.T_DIGIT_STRING &&
                   (i+2) < lineEnd && 
                   (tokens.currLineLA(i+2) == FortranLexer.T_PERIOD ||
                    tokens.currLineLA(i+2) == 
                    FortranLexer.T_PERIOD_EXPONENT)) {
            StringBuffer newTokenText = new StringBuffer();
            int line = tokens.getToken(i).getLine();
            int col = tokens.getToken(i).getCharPositionInLine();
            newTokenText.append(tokens.getToken(i).getText());
            newTokenText.append(tokens.getToken(i+1).getText());

            if(this.sourceForm != FrontEnd.FIXED_FORM &&
               (col + tokens.getToken(i).getText().length())
					!= (tokens.getToken(i+1).getCharPositionInLine())) {
               System.err.println("Error: Whitespace within real constant at " 
                                  + "{line:col}: " + line + ":" + (col+1));
            }
            // remove the two tokens for T_DIGIT_STRING and the T_PERIOD or 
            // T_PERIOD_EXPONENT so we can replace them with our own token
            // for a real constant.
            tokens.removeToken(i);
            
            // since we just removed one token, location i now has the 
            // T_PERIOD or T_PERIOD_EXPONENT.
            tokens.removeToken(i);
            
            // insert the new token for a real constant
            tokens.add(i, tokens.createToken(FortranLexer.T_REAL_CONSTANT, 
                                             newTokenText.toString(), 
                                             line, col));

            // we just removed two tokens and replaced it with one, so the line
            // is now one token shorter.
            lineEnd -= 1;
         }
      }

      return lineEnd;
   }


   public void performPrepass() {
      int commaIndex = -1;
      int equalsIndex = -1;
      int i;  
      int lineLength = 0;
      int lineStart;
      int rawLineStart;
      int rawLineEnd;
      int tokensStart;
      int newLineLength = 0;
		Token eof = null;

      if(this.sourceForm == FrontEnd.FIXED_FORM) {
         tokensStart = tokens.mark();
         tokens.fixupFixedFormat();
         tokens.rewind(tokensStart);
      }

      tokensStart = tokens.mark();
      // the mark is the curr index into the tokens array and needs to start 
      // at -1 (before the list).  this use to be what it always was when 
      // entering this method in antlr-3.0b*, but in antlr-3.0, the number 
      // was no longer guaranteed to be -1.  so, seek the index ptr to -1.
      if(tokensStart != -1) {
         tokens.seek(-1);
         tokensStart = -1;
      }

      while(tokens.LA(1) != FortranLexer.EOF) {
         // initialize necessary variables
         commaIndex = -1;
         equalsIndex = -1;
         lineStart = 0;

         // mark the start of the line
         rawLineStart = tokens.mark();

         // call the routine that buffers the whole line, including WS
         tokens.setCurrLine(rawLineStart);
         // get the line length (number of non-WS tokens)
         lineLength = tokens.getCurrLineLength();

         // get the end of the line
         rawLineEnd = tokens.findTokenInSuper(rawLineStart, 
                                              FortranLexer.T_EOS);
         if(rawLineEnd == -1) {
            // EOF was reached so use EOF as T_EOS to break loop
            rawLineEnd = tokens.getRawLineLength();
         }
         // add offset of T_EOS from the start to lineStart to get end
         rawLineEnd += rawLineStart;

			// Check for a generated T_EOF and skip it if it exists.
			if(tokens.currLineLA(1) == FortranLexer.T_EOF) {
// 				System.err.println("SKIPPING T_EOF in prepass!!!!!!!!!!!!!!!");
				lineStart++;
			}

         // check for a label and consume it if exists
         if(matchLabel(lineStart, lineLength) == true) {
            // consume label by advancing lineStart to next nonWS char.
            lineStart++;
         }

         // check for the optional (T_IDENT T_COLON) that some 
         // constructs can have and skip if it's there.
         if(matchIdentColon(lineStart, lineLength) == true) {
            lineStart+=2;
         }

         // scan for the real literal constant tokens created by the lexer.  
         // this method could shorten the line if it combines tokens into 
         // T_REAL_CONSTANT tokens so we need to update our lineLength and 
         // the stored size of tokens.packedListSize (probably safer to not 
         // have the variable and simply use packedList.size() calls).
         newLineLength = scanForRealConsts(lineStart, lineLength);
         if(newLineLength != lineLength) {
            tokens.updatePackedListSize();
            lineLength = newLineLength;
         }

         // see if there is a comma in the stmt
         commaIndex = salesScanForToken(lineStart, FortranLexer.T_COMMA);
         if(commaIndex != -1) {
            // if there is a comma, the stmt must start with a keyword
            matchLine(lineStart, lineLength);
         } else {
            // see if there is an equal sign in the stmt
            equalsIndex = salesScanForToken(lineStart, FortranLexer.T_EQUALS);
            if(equalsIndex == -1)
               // see if it's a pointer assignment stmt
               equalsIndex = salesScanForToken(lineStart, 
                                               FortranLexer.T_EQ_GT);
            if(equalsIndex != -1) {
               // TODO: 
               // have to figure out how to rearrange the case where we 
               // can't start with a keyword (given the tests below fail) for 
               // fixed format where we may have to combine tokens to get the
               // statement to be accepted.  
//                if(this.sourceForm == FrontEnd.FIXED_FORM) {
//                   fixupFixedFormatLine(lineStart, lineLength, false);
//                }

               // we have an equal but no comma, so stmt can not 
               // start with a keyword.
               // try converting any keyword node found in this line 
               // to an identifier
               // this is NOT true for data declarations that have an 
               // initialization expression (e.g., integer :: i = 1 inside
               // a derived type).  also, this does not work for one-liner
               // statements, such as a where_stmt or procedure stmts.
               // first, see if it's a one-liner
               if(matchOneLineStmt(lineStart, lineLength) == false) {
                  if(matchProcStmt(lineStart, lineLength) == false) {
                     // if not, see if it's an assignment stmt
                     if(matchAssignStmt(lineStart, lineLength) == false) {
                        // else, match it as a data declaration
                        if(matchDataDecl(lineStart, lineLength) == false) {
                           if(matchGenericBinding(lineStart, lineLength) 
                              == false) {
                              System.err.println("Couldn't match line!");
                              tokens.printPackedList();
                           }
                        }
                     }
                  }
               } 
            } else {
               // TODO:
               // need to make sure that this can be here because it may 
               // prevent something from matching below...
//                if(this.sourceForm == FrontEnd.FIXED_FORM) {
//                   fixupFixedFormatLine(lineStart, lineLength, true);
//                }


               // no comma and no equal sign; must start with a keyword
               // can have a one-liner stmt w/ neither
//                if(matchOneLineStmt(lineStart, lineLength) == false) {
//                   matchLine(lineStart, lineLength);
//                }
               // call matchLine() first because it will try and match an
               // if_construct, etc.  if that fails, we may still have a 
               // one-liner statement, such as if_stmt, where_stmt, etc.
               if(matchLine(lineStart, lineLength) == false) {
                  matchOneLineStmt(lineStart, lineLength);
               }
            }
         }// end if(found comma)/else(found equals or neither)

         // consume the tokens we just processed
         for(i = rawLineStart; i < rawLineEnd; i++) {
            tokens.consume();
         }

         // need to finalize the line with the FortranTokenStream in case
         // we had to change any tokens in the line
         tokens.finalizeLine();
      }//end while(not EOF)
		
		// We need to include the EOF in the token stream so the parser can 
		// signal when to pop the include stream stack.
		eof = tokens.LT(1);
		eof.setText("EOF");
		tokens.addTokenToNewList(eof);

      // reset to the beginning of the tokens for the parser
      tokens.rewind(tokensStart);

      return;
   }// end performPrepass()
   
}// end class FortranLexicalPrepass
