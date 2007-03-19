package org.eclipse.ptp.lang.fortran.core.parser;

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
import java.io.*;
import java.util.ArrayList;
import org.antlr.runtime.*;

public class FortranStream extends ANTLRFileStream {
   public FortranStream(String fileName) throws IOException {
      super(fileName);

      int i;
      int currIndex = 0;
      for(i = 0; i < super.n; i++) {
         // skip chars in comments
         if(super.data[i] == '!') {
            do {
               super.data[currIndex] = super.data[i];
               currIndex++;
               i++;
            } while(i < super.n && super.data[i] != '\n') ;
         } else {
            if(super.data[i] == '&') {
               int contEnd = i+1;
               char prevChar;
               char nextChar;

               // i is location of '&', i-1 is character just before it
               prevChar = super.data[i-1];

               while(contEnd < super.n && 
                     (Character.isWhitespace(super.data[contEnd]))) 
                  contEnd++;
               // should have stopped on the first non-WS character.  if it is
               // an '&', we need to look at what follows it.
               System.out.println("hello");
               if(super.data[contEnd] == '&') {
                  boolean splitToken = true;
                  contEnd++;

                  // store the character immediately following the '&'
                  nextChar = super.data[contEnd];

                  // could have split a token across a line
                  while(contEnd < super.n && 
                        (Character.isLetterOrDigit(super.data[contEnd]) == 
                         false)) {
                     contEnd++;
                  }
               
                  // if prevChar and nextChar are both letter/digit, we are 
                  // splitting a token across the line.  if not, we need to
                  // insert a '&' here signal a new-line
                  if((Character.isLetterOrDigit(prevChar) == true &&
                      Character.isLetterOrDigit(nextChar) == true) == false) {
                     super.data[currIndex] = '&';
                     currIndex++;
                     splitToken = false;
                  }

                  while(contEnd < super.n && 
                        (Character.isLetterOrDigit(super.data[contEnd]) 
                         == true)) {
                     // have to add character to the dataList
                     super.data[currIndex] = super.data[contEnd];
                     currIndex++;
                     contEnd++;
                  }

                  if(splitToken == true) {
                     super.data[currIndex] = '&';
                     currIndex++;
                  }
               } else {
                  // we can NOT be splitting a token across a line, so need
                  // to insert a '&' to signify a new-line to the lexer
                  // we had a '&' on the first line, but not on the 
                  // following one
                  super.data[currIndex] = '&';
                  currIndex++;
               }

               // stopped on the next character that is not part of the 
               // continuation string.  advance 'i' to it.
               i = contEnd;
            }// end if('&' was found at end of a line)
         }// end else(not a comment)

         if(super.data[i] == '&') {
            // we can't add & characters w/o processing them.  if that is 
            // the character we're currently on, we need to make sure that
            // we process it in the loop.
            i--;
         } else {
            super.data[currIndex] = super.data[i];
            currIndex++;
         }
      }// end for()

      // make sure we get the terminating character (EOF).
      super.data[currIndex-1] = super.data[super.n-1];

//       for(i = 0; i < currIndex; i++)
//          System.out.println("data[" + i + "]: " + super.data[i]);
      System.out.println("super.n originally was: " + super.n);
      super.n = currIndex;
      System.out.println("super.n now is: " + super.n);
   }// end constructor


   public int LA(int i) {
      int letter_value;

      letter_value = super.LA(i);

      // the letter is lower-case
      if(Character.isLowerCase((char)letter_value)) {
         // convert to upper-case
         letter_value = (int)(Character.toUpperCase((char)(letter_value)));
      } 
         
      return letter_value;
   }// end LA()
}// end class FortranStream
