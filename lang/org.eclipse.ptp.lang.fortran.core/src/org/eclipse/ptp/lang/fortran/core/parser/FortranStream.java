import java.io.*;
import org.antlr.runtime.*;

public class FortranStream extends ANTLRFileStream {
   public FortranStream(String fileName) throws IOException {
      super(fileName);
   }// end constructor

   public int LA(int i) {
      int char_offset;
      int letter_value;

      letter_value = super.LA(i);

      // the letter is lower-case
      if(letter_value >= 'a' && letter_value <= 'z') {
         // figure out what letter it is by it's offset from 'a'
         char_offset = letter_value - 'a';
         // add offset to 'A' to get upper-case version
         letter_value = 'A' + char_offset;
      }
         
      return letter_value;
   }// end LA()
}// end class FortranStream
