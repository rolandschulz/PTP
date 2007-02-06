package org.eclipse.ptp.lang.fortran.core.parser;

import java.io.*;
import java.util.*;
import org.antlr.runtime.*;

public class FortranTokenStream extends CommonTokenStream {
   public FortranLexer lexer;
   public int needIdent;
   public int parserBacktracking;
   public boolean matchFailed;
   private List currLine;
//    private int currLineSize;
   private int lineLength;
   private ArrayList<Token> packedList;
   private int packedListSize;
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
      this.packedListSize = 0;
      this.newTokensList = new ArrayList<Token>();
   }// end constructor


   private void createPackedList() {
      Token tmpToken = null;
      int i = 0;
      boolean success;

      this.packedList = new ArrayList<Token>(this.lineLength+1);
      this.packedListSize = 0;

      for(i = 0; i < currLine.size(); i++) {
         tmpToken = getTokenFromCurrLine(i);
//          if(tmpToken != null && tmpToken.getType() != lexer.WS) {
         if(tmpToken != null && tmpToken.getChannel() != 
            lexer.getIgnoreChannelNumber()) {
            try {
               packedList.add(this.packedListSize, tmpToken);
            } catch(Exception e) {
               e.printStackTrace();
               System.exit(1);
            }
            this.packedListSize++;
         }
      }// end for(each item in buffered line)

      // need to make sure the line was terminated with a T_EOS.  this may 
      // not happen if we're working on a file that ended w/o a newline
      if(packedList.get(packedListSize-1).getType() != lexer.T_EOS) {
         CommonToken eos = new CommonToken(lexer.T_EOS);
         packedList.add(this.packedListSize, eos);
         packedListSize++;
      }
      
// //       System.out.println("newTokensList.size(): " + newTokensList.size());
//       // add the packed list to the newTokensList
//       if(this.newTokensList.addAll(packedList) == false)
//          System.err.println("Couldn't add to newTokensList!");
// //       System.out.println("newTokensList.size() now is: " + 
// //                          newTokensList.size());
      return;
   }// end createPackedList()


   public String lineToString(int lineStart, int lineEnd) {
      int i = 0;
      StringBuffer lineText = new StringBuffer();

      for(i = lineStart; i < this.packedListSize-1; i++) {
         lineText.append(this.packedList.get(i).getText());
      }
      
//       System.out.println("lineText.toString(): " + lineText.toString());
      return lineText.toString();
   }// end lineToString()


   public List getTokens(int start, int stop) {
      return super.getTokens(start, stop);
   }// end getTokens()


   public int getCurrLineLength() {
      return this.packedListSize;
   }

   public int getRawLineLength() {
      return this.currLine.size();
   }

   public int getLineLength(int start) {
      int lineLength;
      Token token;

      lineLength = 0;
//       while((start+lineLength) < super.tokens.size() &&
//             super.get(start+lineLength).getType() != lexer.T_EOS &&
//             super.get(start+lineLength).getType() != lexer.EOF) {
//          lineLength++;
//       }

//       return lineLength++;

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

      if(start >= this.packedListSize)
         return -1;
      
      do {
         tmpToken = (Token)(packedList.get(start));
         start++;
      } while(start < this.packedListSize &&
              tmpToken.getType() != desiredToken);

      if(tmpToken.getType() == desiredToken)
         // start is one token past the one we want
         return start-1;

      return -1;
   }// end findTokenInPackedList()


   public Token getToken(int pos) {
      if(pos >= this.packedListSize || pos < 0) {
         System.out.println("pos is out of range!");
         System.out.println("pos: " + pos + 
                            " packedListSize: " + this.packedListSize);
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

      if(start >= this.packedListSize) {
         System.out.println("start is out of range!");
         System.out.println("start: " + start + 
                            " packedListSize: " + this.packedListSize);
         return -1;
      }
      
      do {
         tmpToken = (Token)(packedList.get(start));
         start++;
      } while(start < this.packedListSize &&
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
         System.out.println("currLine is null!!!!");
         System.exit(1);
      }

//       System.out.println("currLine.size() in setCurrLine is: " + 
//                          currLine.size());

//       // just for debugging
//       printCurrLine();

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
      System.out.println("packedListSize is: " + this.packedListSize);
      System.out.println(this.packedList.toString());
      System.out.println("*********************************");

      return;
   }// end printPackedList()


   public int currLineLA(int lookAhead) {
      Token tmpToken = null;
      int i;

      // make sure the requested lookAhead isn't out of range.  
      if((lookAhead-1) > this.packedListSize) 
         return -1;

      // then get the token from the packedList
      tmpToken = (Token)(packedList.get(lookAhead-1));
      return tmpToken.getType();
   }// end currLineLA()


   public boolean testForFunction() {
      int lookAhead = 1;
      int tmpToken;

      do {
         // get the next token
         tmpToken = this.LA(lookAhead);
         // update lookAhead in case we look again
         lookAhead++;
      } while(tmpToken != lexer.T_EOS && tmpToken != lexer.EOF && 
              tmpToken != lexer.T_FUNCTION);
      
      if(tmpToken == lexer.T_FUNCTION) {
         return true;
      } else {
         return false;
      }
   }// end testForFunction()

   
   public boolean appendToken(int tokenType, String tokenText) {
//       // append a token to the end of newTokenList
//       return this.newTokensList.add(new CommonToken(tokenType));   
      // append a token to the end of newTokenList
      return this.packedList.add(new CommonToken(tokenType));   
   }// end appendToken()


   public void addToken(Token token) {
      this.packedList.add(token);
   }


   public void addToken(int index, int tokenType, String tokenText) {
//       try {
//          // for example: 
//          // index = 1
//          // packedList == label T_CONTINUE T_EOS  (size is 3)
//          // newTokensList.size() == 22
//          // 22-3+1=20 
//          // so, inserted between the label and T_CONTINUE
// //          System.out.println("index to start is: " + index);
//          index = this.newTokensList.size() - this.packedListSize + index;
// //          System.out.println("index now is: " + index);
// //          System.out.println("newTokensList.size(): " + 
// //                             this.newTokensList.size());
// //          System.out.println("this.packedListSize is: " + this.packedListSize);
// //          System.out.println("newTokensList before inserting: " + 
// //                             this.newTokensList.toString());
//          this.newTokensList.add(index, new CommonToken(tokenType, tokenText));
//          // this is for debugging, to find an inserted Token easier
// //          this.newTokensList.add(index, new CommonToken(tokenType, 
// //                                                        "INSERTED THIS TOKEN"));
//       } catch(Exception e) {
//          e.printStackTrace();
//          System.exit(1);
//       }
      try {
         // for example: 
         // index = 1
         // packedList == label T_CONTINUE T_EOS  (size is 3)
         // newTokensList.size() == 22
         // 22-3+1=20 
         // so, inserted between the label and T_CONTINUE
//          System.out.println("index to start is: " + index);
//          System.out.println("index now is: " + index);
//          System.out.println("newTokensList.size(): " + 
//                             this.newTokensList.size());
//          System.out.println("this.packedListSize is: " + this.packedListSize);
//          System.out.println("newTokensList before inserting: " + 
//                             this.newTokensList.toString());
         this.packedList.add(index, new CommonToken(tokenType, tokenText));
         // this is for debugging, to find an inserted Token easier
//          this.newTokensList.add(index, new CommonToken(tokenType, 
//                                                        "INSERTED THIS TOKEN"));
      } catch(Exception e) {
         e.printStackTrace();
         System.exit(1);
      }
      
      return;
   }// end addToken()


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


   public CommonToken createToken(int type, String text, int line, int col) {
      CommonToken token = new CommonToken(type, text);
      token.setLine(line);
      token.setCharPositionInLine(col);
      return token;
   }// end createToken()


   public void finalizeLine() {
      if(this.newTokensList.addAll(packedList) == false)
         System.err.println("Couldn't add to newTokensList!");
//       // just for debugging
//       printPackedList();
   }// end finalizeLine()


   public void finalizeTokenStream() {
      System.out.println("super.tokens.size() in finalize.. is: " + 
                         super.tokens.size());
      System.out.println("this.newTokensList.size() in finalize.. is: " + 
                         this.newTokensList.size());
//       // this next line could print a lot of stuff...use on small tests only!
//       System.out.println("newTokensList.toString(): " + 
//                          this.newTokensList.toString());
//       System.out.println("================================================");
//       System.out.println("original tokens list: " + super.tokens.toString());
      super.tokens = this.newTokensList;
   }// end finalizeTokenStream()
}// end class FortranTokenStream
