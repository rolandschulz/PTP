package org.eclipse.ptp.lang.fortran.core.parser;
import java.io.*;
import org.antlr.runtime.*;

public class FortranTokenStream extends CommonTokenStream {
   public FortranLexer myLexer;
   public int needIdent;
   public FortranTokenStream(FortranLexer lexer) {
      super(lexer);
      this.myLexer = lexer;
      this.needIdent = 0;
   }// end constructor

   public Token LT(int k) {
      Token tmpToken;
//       System.out.println("inside LT() in FortranTokenStream");
      tmpToken = super.LT(k);
//       System.out.println("tmpToken.getType(): " + tmpToken.getType());
//       System.out.println("FortranTokenStream.needIdent is: " + 
//                          this.needIdent);
//       System.out.println("tmpToken.getText(): " + tmpToken.getText());
      myLexer.convertToIdent(tmpToken, this.needIdent);
      // clear the flag after possibly overriding this token
      this.needIdent = 0;
      return super.LT(k);
   }
}// end class FortranTokenStream
