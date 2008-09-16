/*******************************************************************************
 * Copyright (c) 2005, 2006 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.ptp.lang.fortran.core.parser;

import java.io.*;
import java.util.*;
import org.antlr.runtime.*;

/* The following needed for OFP packaging scheme */
//import fortran.ofp.parser.java.FortranToken;

public class FortranTokenStream extends CommonTokenStream {
   public FortranLexer lexer;
   public int needIdent;
   public int parserBacktracking;
   public boolean matchFailed;
   private List currLine;
   private int lineLength;
   private ArrayList<Token> packedList;
   private ArrayList<Token> newTokensList;

   public FortranTokenStream(FortranLexer lexer) {
      super(lexer);
      this.lexer = lexer;
      this.needIdent = 0;
      this.parserBacktracking = 0;
      this.matchFailed = false;
      this.currLine = null;
      this.lineLength = 0;
      this.packedList = null;
      this.newTokensList = new ArrayList<Token>();
   }// end constructor


   public void fixupFixedFormat() {
      ArrayList<Token> tmpArrayList = null;
      List tmpList = null;
      int i = 0;
      Token tmpToken;
      
      tmpList = super.getTokens();
      tmpArrayList = new ArrayList<Token>(tmpList.size());
      // TODO:
      // this won't be necessary once ANTLR updates their getTokens method
      // to return an ArrayList, that uses the syntax ArrayList<Token>.  
      // otherwise, the compiler gives a warning about unchecked or unsafe
      // operations.  this loop is overkill for simply avoiding the warning...
      // however, having an ArrayList that contains typed objects (Token) is 
      // useful below because we may have to rewrite the stream when handling
      // comments and continuations.  
      for(i = 0; i < tmpList.size(); i++) {
         try {
            tmpArrayList.add((Token)tmpList.get(i));
         } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
         }
      }

      // loop across the tokens and convert anything in the col 0 to a 
      // line comment, and anything in col 6 to continuation.  note: this may
      // require the splitting of tokens!
      for(i = 0; i < tmpArrayList.size(); i++) {
         tmpToken = tmpArrayList.get(i);
         if(tmpToken.getCharPositionInLine() == 5 &&
            tmpToken.getType() != FortranLexer.WS &&
            (tmpToken.getType() != FortranLexer.T_EOS ||
             (tmpToken.getType() == FortranLexer.T_EOS &&
              tmpToken.getText().charAt(0) == ';'))) {
            // any char, it appears, can be a continuation char if it's in 
            // the 6th column (col. 5 cause zero based), including '!' or ';'.
            // TODO:
            // if the length is greater than 1, then the user is most likely 
            // using a letter or number to signal the continuation.  in this 
            // case, we need to split off the character that's in column 6 and
            // make two tokens -- the continuation token and what's left.  we 
            // should maybe warn the user about this in case they accidentally
            // started in the wrong column?
            if(tmpToken.getText().length() > 1) {
               System.err.println("TODO: handle this continuation type!");
            } else {
               int j;
               int k;
               Token prevToken = null;

               tmpToken.setType(FortranLexer.CONTINUE_CHAR);
               // hide the continuation token
               tmpToken.setChannel(lexer.getIgnoreChannelNumber());
               tmpArrayList.set(i, tmpToken);

               j = i-1;
               do {
                  prevToken = tmpArrayList.get(j);
                  j--;
               } while(j >= 0 && (prevToken.getType() == FortranLexer.WS ||
                                  prevToken.getType() == 
                                  FortranLexer.LINE_COMMENT ||
                                  prevToken.getType() == FortranLexer.T_EOS));

               // channel 99 (hide) all tokens from after prevToken (j+1)+1 
               // through the continue token (i)
               for(k = j+2; k < i; k++) {
                  tmpToken = tmpArrayList.get(k);
                  // only hide the T_EOS tokens. all WS and LINE_COMMENT tokens
                  // should already be hidden.
                  if(tmpToken.getType() == FortranLexer.T_EOS &&
                     tmpToken.getText().charAt(0) != ';') {
                     tmpToken.setChannel(lexer.getIgnoreChannelNumber());
                     tmpArrayList.set(k, tmpToken);
                  }
               }
               
               // TODO:
               /* how can we handle fixed-format split tokens?  for example:
                  inte
                 $    ger j
                  this is the variable declaration 'integer j'.  how are we 
                  suppose to know this?  it compiles with gfortran.
               */
//                // need to find the next non-WS token
//                i++;
//                while(tmpArrayList.get(i).getType() == FortranLexer.WS ||
//                      tmpArrayList.get(i).getType() == 
//                      FortranLexer.LINE_COMMENT) {
//                   i++;
//                }

//                StringBuffer buffer = new StringBuffer();
//                Token token;
//                int tokenCount = 0;

//                buffer = buffer.append(prevToken.getText());
//                buffer = buffer.append(tmpArrayList.get(i).getText());
                  
//                ANTLRStringStream charStream = 
//                   new ANTLRStringStream(buffer.toString().toUpperCase());
//                FortranLexer myLexer = new FortranLexer(charStream);
//                System.out.println("trying to match the string: " + 
//                                   buffer.toString().toUpperCase() + 
//                                   " for fixed-format continuation");
            }
         }
      }// end for(each Token in the ArrayList) 

//       System.out.println("tmpArrayList as one big string: ");
//       StringBuffer buffer = new StringBuffer();
//       for(i = 0; i < tmpArrayList.size(); i++) {
//          tmpToken = tmpArrayList.get(i);
//          if(tmpToken.getType() == FortranLexer.WS ||
//             (tmpToken.getType() == FortranLexer.T_EOS &&
//              tmpToken.getText().charAt(0) != ';') ||
//             tmpToken.getChannel() != lexer.getIgnoreChannelNumber()) {
//             buffer = buffer.append(tmpToken.getText());
//          }
//       }
//       System.out.println(buffer.toString().toUpperCase());

//       {
//          System.out.println("parsing above buffer with FixedLexer");
//          ANTLRStringStream charStream = 
//             new ANTLRStringStream(buffer.toString().toUpperCase());
//          FixedLexer myFixed = new FixedLexer(charStream);
//          Token fixedToken;

//          do {
//             fixedToken = myFixed.nextToken();
//          } while(fixedToken.getType() >= 0);
//          System.out.println("done parsing above buffer with FixedLexer");
//          System.exit(1);
//       }

//       System.out.println("tmpArrayList.toString(): " + 
//                          tmpArrayList.toString());
//       System.out.println("tmpArrayList.size(): " + tmpArrayList.size());
//       System.out.println("super.tokens.size(): " + super.tokens.size());
//       System.out.println("super.p is: " + super.p);

      // save the new ArrayList (possibly modified) to the super classes 
      // token list.
      super.tokens = tmpArrayList;

      return;
   }// end fixupFixedFormat()


   private void createPackedList() {
      Token tmpToken = null;
      int i = 0;
      boolean success;

      this.packedList = new ArrayList<Token>(this.lineLength+1);

      for(i = 0; i < currLine.size(); i++) {
         tmpToken = getTokenFromCurrLine(i);
         // get all tokens, including channel 99'ed ones, so we can fixup
         // continued lines.  we'll drop ignored tokens after that.
         try {
            packedList.add(tmpToken);
         } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
         }
      }// end for(each item in buffered line)

      // need to make sure the line was terminated with a T_EOS.  this may 
      // not happen if we're working on a file that ended w/o a newline
      if (packedList.get(packedList.size()-1).getType() != lexer.T_EOS) {
         FortranToken eos = new FortranToken(lexer.getInput(), lexer.T_EOS, 
                                             Token.DEFAULT_CHANNEL, 
                                             lexer.getInput().index(), 
                                             lexer.getInput().index()+1);
         eos.setText("\n");
         packedList.add(eos);
      }

      fixupContinuedLine(packedList);

      dropIgnoredTokens(packedList);
      
      return;
   }// end createPackedList()


   private boolean possiblySplitToken(ArrayList<Token> packedList, 
                                      int firstContCharOffset, 
                                      int currOffset) {
      int i = 0;

      for(i = firstContCharOffset+1; i < currOffset; i++) {
         if(packedList.get(i).getType() != FortranLexer.WS &&
            packedList.get(i).getType() != FortranLexer.T_EOS) {
            return false;
         }
      }
      return true;
   }// end possiblySplitToken()


   private void fixupContinuedLine(ArrayList<Token> packedList) {
      int firstContCharOffset = -1;
      int secondContCharOffset = -1;
      int i;
      int j;

      // search for a continue char ('&' in free form)
      for(i = 0; i < packedList.size(); i++) {
         if(packedList.get(i).getType() == FortranLexer.CONTINUE_CHAR) {
            if(firstContCharOffset == -1)
               firstContCharOffset = i;
            else {
               // if all tokens between the first '&' and this one are WS, 
               // we have to consider the '&' chars together.  otherwise, 
               // we don't.
               if(possiblySplitToken(packedList, firstContCharOffset, i) 
                  == true) {
                  // we have to consider the token preceding the first '&' and
                  // the one following the second '&' together.
                  // two continue chars.  need to re-tokenize what's 
                  // immediately before the first continue and immediately 
                  // after the second.
                  StringBuffer buffer = new StringBuffer();
                  Token token;
                  int tokenCount = 0;

                  // channel 99 all of the tokens from the from the 
                  // token preceding the first '&' and the token following 
                  // the second '&', inclusive
                  for(j = firstContCharOffset-1; j <= i; j++) {
                     packedList.get(j).setChannel(
                        lexer.getIgnoreChannelNumber());
                  }
            
                  buffer = 
                     buffer.append(
                        packedList.get(firstContCharOffset-1).getText());
                  buffer = 
                     buffer.append(
                        packedList.get(i+1).getText());
                  
                  ANTLRStringStream charStream = 
                     new ANTLRStringStream(buffer.toString().toUpperCase());
                  FortranLexer myLexer = new FortranLexer(charStream);

                  // drop the token following the second '&'.  the token 
                  // the first '&' has already been dropped by the 'else' 
                  // clause below.
                  packedList.get(i+1).setChannel(
                     lexer.getIgnoreChannelNumber());

                  do {
                     tokenCount++;
                     token = myLexer.nextToken();
                     if(tokenCount == 1) {
                        // this is the first of two possible tokens that 
                        // we're adding to the packed list, so look up the 
                        // line/col position from
                        // the original token (at firstContCharOffset-1).
                        token.setLine(
                           packedList.get(firstContCharOffset-1).getLine());
                        token.setCharPositionInLine(
                           packedList.get(firstContCharOffset-1).
                           getCharPositionInLine());
                     } else {
                        // the second of two tokens we're adding
                        token.setLine(
                           packedList.get(i+1).getLine());
                        token.setCharPositionInLine(
                           packedList.get(i+1).
                           getCharPositionInLine());
                     }
                     if(token.getType() >= 0) {
                        token.setText(token.getText().toLowerCase());
                        // insert the token
                        try {
                           packedList.add(i, token);
                        } catch(Exception e) {
                           e.printStackTrace();
                           System.exit(1);
                        }
                        // increment the loop variable to advance past the 
                        // token we just inserted.
                        i++;
                     }
                  } while(token.getType() >= 0);

                  firstContCharOffset = -1;
               } else {
                  // separate tokens, so drop the '&' and update to the current
                  // '&' as being the first cont char.
                  packedList.get(firstContCharOffset).setChannel(
                     lexer.getIgnoreChannelNumber());
                  firstContCharOffset = i;
               }
            }
         }// end if(FortranLexer.T_CONTINUE_CHAR)
      }// end for()

      return;
   }// end fixupContinuedLine() 


   private void dropIgnoredTokens(ArrayList<Token> packedList) {
      ArrayList<Token> tmpList = packedList;
      Token tmpToken = null;
      int i;

      this.packedList = new ArrayList<Token>(this.packedList.size());

      for (i = 0; i < tmpList.size(); i++) {
         tmpToken = tmpList.get(i);
         if (tmpToken != null && tmpToken.getChannel() != 
            lexer.getIgnoreChannelNumber()) {
            try {
               this.packedList.add(tmpToken);
            } catch(Exception e) {
               e.printStackTrace();
               System.exit(1);
            }
         }
      }

      return;
   }// end dropIgnoredTokens()


   public String lineToString(int lineStart, int lineEnd) {
      int i = 0;
      StringBuffer lineText = new StringBuffer();

      for(i = lineStart; i < packedList.size()-1; i++) {
         lineText.append(packedList.get(i).getText());
      }
      
      return lineText.toString();
   }// end lineToString()


   public List getTokens(int start, int stop) {
      return super.getTokens(start, stop);
   }// end getTokens()


   public int getCurrLineLength() {
      return this.packedList.size();
   }

   public int getRawLineLength() {
      return this.currLine.size();
   }

   public int getLineLength(int start) {
      int lineLength;
      Token token;

      lineLength = 0;

      // this will not give you a lexer.EOF, so may need to 
      // add a T_EOS token when creating the packed list if the file
      // ended w/o a T_EOS (now new line at end of the file).
      do {
         token = super.get(start+lineLength);
         lineLength++;
      } while((start+lineLength) < super.tokens.size() &&
              (token.getChannel() == lexer.getIgnoreChannelNumber() || 
               token.getType() != lexer.T_EOS && 
               token.getType() != lexer.EOF));

      return lineLength;
   }// end getLineLength()


   public int findTokenInPackedList(int start, int desiredToken) {
      Token tmpToken;

      if(start >= this.packedList.size()) {
         return -1;
      }
      
      do {
         tmpToken = (Token)(packedList.get(start));
         start++;
      } while(start < this.packedList.size() &&
              tmpToken.getType() != desiredToken);

      if(tmpToken.getType() == desiredToken)
         // start is one token past the one we want
         return start-1;

      return -1;
   }// end findTokenInPackedList()


   public Token getToken(int pos) {
      if (pos >= this.packedList.size() || pos < 0) {
         System.out.println("pos is out of range!");
         System.out.println("pos: " + pos + 
                            " packedListSize: " + this.packedList.size());
         return null;
      }
      else
         return (Token)(packedList.get(pos));
   }// end getToken()


   public Token getToken(int start, int desiredToken) {
      int index;
      
      index = findToken(start, desiredToken);
      if(index != -1)
         return (Token)(packedList.get(index));
      else 
         return null;
   }//end getToken()


   public int findToken(int start, int desiredToken) {
      Token tmpToken;

      if(start >= this.packedList.size()) {
         System.out.println("start is out of range!");
         System.out.println("start: " + start + 
                            " packedListSize: " + this.packedList.size());
         return -1;
      }
      
      do {
         tmpToken = (Token)(packedList.get(start));
         start++;
      } while (start < this.packedList.size() &&
              tmpToken.getType() != desiredToken);

      if(tmpToken.getType() == desiredToken)
         // start is one token past the one we want
         return start-1;

      return -1;
   }// end findToken()


   /**
    * Search the currLine list for the desired token.
    */
   public int findTokenInCurrLine(int start, int desiredToken) {
      int tmpTokenType;
      int size;
      Token tmpToken;

      size = currLine.size();
      if(start >= size) 
         return -1;

      do {
         // get the i'th object out of the list
         tmpToken = (Token)(currLine.get(start));
         start++;
      } while(start < size && 
              tmpToken.getType() != desiredToken);
         
      
      if(tmpToken.getType() == desiredToken)
         return start;

      return -1;
   }// end findTokenInCurrLine()

   
   /**
    * @param pos Current location in the currLine list; the search 
    * will begin by looking at the next token (pos+1).
    */
   public Token getNextNonWSToken(int pos) {
      Token tmpToken;
      
      tmpToken = (Token)(packedList.get(pos+1));

      return tmpToken;
   }// end getNextNonWSToken()


   /**
    * @param pos Current location in the currLine list; the search 
    * will begin by looking at the next token (pos+1).
    */
   public int getNextNonWSTokenPos(int pos) {
      Token tmpToken;
      
      // find the next non WS token
      tmpToken = getNextNonWSToken(pos);
      // find it's position now
      pos = findTokenInCurrLine(pos, tmpToken.getType());

      return pos;
   }// end getNextNonWSTokenPos()


   public Token getTokenFromCurrLine(int pos) {
      if(pos >= currLine.size() || pos < 0) 
         return null;
      else
         return ((Token)(currLine.get(pos)));
   }// end getTokenFromCurrLine()


   public void setCurrLine(int lineStart) {
      this.lineLength = this.getLineLength(lineStart);
      
      // this will get the tokens [lineStart->((lineStart+lineLength)-1)]
      currLine = this.getTokens(lineStart, (lineStart + this.lineLength) - 1);
      if(currLine == null) {
         System.err.println("currLine is null!!!!");
         System.exit(1);
      }

      // pack all non-ws tokens
      createPackedList();

//       // just for debugging
//       printPackedList();
      
      return;
   }// end setCurrLine()       


   /**
    * This will use the super classes methods to keep track of the 
    * start and end of the original line, not the line buffered by
    * this class.
    */
   public int findTokenInSuper(int lineStart, int desiredToken) {
      int lookAhead = 0;
      int tmpToken;

      // if this line is a comment, skip scanning it
      if(super.LA(1) == lexer.LINE_COMMENT)
         return -1;

      do {
         // lookAhead was initialized to 0
         lookAhead++;

         // get the token 
         tmpToken = super.LA(lookAhead);

         // continue until find what looking for or reach end
      } while(tmpToken != lexer.EOF && tmpToken != lexer.T_EOS && 
              tmpToken != desiredToken);

      if(tmpToken == desiredToken)
         // we found a what we wanted to
         return lookAhead;
         
      return -1;
   }// end findTokenInSuper()


   public void printCurrLine() {
      int i;
      Token tmpToken;

      System.out.println("=================================");
      System.out.println("currLine.size() is: " + currLine.size());
      System.out.println(currLine.toString());
      System.out.println("=================================");

      return;
   }// end printCurrLine()


   public void printPackedList() {

      System.out.println("*********************************");
      System.out.println("packedListSize is: " + this.packedList.size());
      System.out.println(this.packedList.toString());
      System.out.println("*********************************");

      return;
   }// end printPackedList()


   public int currLineLA(int lookAhead) {
      Token tmpToken = null;
      int i;

      // get the token from the packedList
      try {
         tmpToken = (Token)(packedList.get(lookAhead-1));
      } catch(Exception e) {
         e.printStackTrace();
         System.exit(1);
      }
      return tmpToken.getType();
   }// end currLineLA()


   public boolean lookForToken(int desiredToken) {
      int lookAhead = 1;
      int tmpToken;

      do {
         // get the next token
         tmpToken = this.LA(lookAhead);
         // update lookAhead in case we look again
         lookAhead++;
      } while(tmpToken != lexer.T_EOS && tmpToken != lexer.EOF && 
              tmpToken != desiredToken);
      
      if(tmpToken == desiredToken) {
         return true;
      } else {
         return false;
      }
   }// end testForFunction()

   
   public boolean appendToken(int tokenType, String tokenText) {
		FortranToken newToken = new FortranToken(tokenType);
		newToken.setText(tokenText);
      // append a token to the end of newTokenList
      return this.packedList.add(newToken);   
   }// end appendToken()


   public void addToken(Token token) {
      this.packedList.add(token);
   }


   public void addToken(int index, int tokenType, String tokenText) {
      try {
         // for example: 
         // index = 1
         // packedList == label T_CONTINUE T_EOS  (size is 3)
         // newTokensList.size() == 22
         // 22-3+1=20 
         // so, inserted between the label and T_CONTINUE
         this.packedList.add(index, new FortranToken(tokenType, tokenText));
      } catch(Exception e) {
         e.printStackTrace();
         System.exit(1);
      }
      
      return;
   }// end addToken()


   public void set(int index, Token token) {
      packedList.set(index, token);
   }// end set()


   public void add(int index, Token token) {
      packedList.add(index, token);
   }


   public void removeToken(int index) {
      packedList.remove(index);
      return;
   }// end removeToken()


   public void clearTokensList() {
      this.packedList.clear();
      return;
   }// end clearTokensList()


   public ArrayList<Token> getTokensList() {
      return this.packedList;
   }// end getTokensList()

   
   public void setTokensList(ArrayList<Token> newList) {
      this.packedList = newList;
      return;
   }// end setTokensList()


   public int getTokensListSize() {
      return this.packedList.size();
   }// end getTokensListSize()


   public FortranToken createToken(int type, String text, int line, int col) {
      FortranToken token = new FortranToken(type, text);
      token.setLine(line);
      token.setCharPositionInLine(col);
      return token;
   }// end createToken()


	public void addTokenToNewList(Token token) {
		if(this.newTokensList.add(token) == false) 
			System.err.println("Couldn't add to newTokensList!");
		return;
	}


   public void finalizeLine() {
      if(this.newTokensList.addAll(packedList) == false)
         System.err.println("Couldn't add to newTokensList!");
//       // just for debugging
//       printPackedList();
   }// end finalizeLine()


   public void finalizeTokenStream() {
//       System.out.println("super.tokens.size() in finalize.. is: " + 
//                          super.tokens.size());
//       System.out.println("this.newTokensList.size() in finalize.. is: " + 
//                          this.newTokensList.size());
//       // this next line could print a lot of stuff...use on small tests only!
//       System.out.println("newTokensList.toString(): " + 
//                          this.newTokensList.toString());
//       System.out.println("================================================");
//       System.out.println("original tokens list: " + super.tokens.toString());
      super.tokens = this.newTokensList;
//       System.out.println("================================================");
//       System.out.println("super.tokens new list: " + super.tokens.toString());
   }// end finalizeTokenStream()
}// end class FortranTokenStream
