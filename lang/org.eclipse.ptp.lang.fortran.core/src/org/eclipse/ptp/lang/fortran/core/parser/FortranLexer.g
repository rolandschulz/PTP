lexer grammar FortranLexer;

options {
    tokenVocab=FortranLexer;
}

@header {
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

/**
 *
 * @author Craig E Rasmussen, Christopher D. Rickett, Jeffrey Overbey
 */
 
package fortran.ofp.parser.java;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import fortran.ofp.parser.java.FortranToken;
}

@members {
    private Token prevToken;
    private boolean continueFlag;
    private boolean includeLine;
    private boolean inFormat;
    private ArrayList<String> includeDirs;
    private Stack<FortranStream> oldStreams;

    protected StringBuilder whiteText = new StringBuilder();

    public Token emit() {
        FortranToken t = new FortranToken(input, type, channel,
            tokenStartCharIndex, getCharIndex()-1);
        t.setLine(tokenStartLine);
        t.setText(text);
        t.setCharPositionInLine(tokenStartCharPositionInLine);

        if(channel == HIDDEN) {
            whiteText.append(getText());
        } else {
            t.setWhiteText(whiteText.toString());
            whiteText.delete(0, whiteText.length());
        }

        emit(t);
        return t;
    }

    public boolean isKeyword(Token tmpToken) {
        if(tmpToken.getType() >= T_INTEGER && 
            tmpToken.getType() <= T_LEN) {
            return true;
        } else {
            return false;
        }
    }// end isKeyword()

    public boolean isKeyword(int tokenType) {
        if(tokenType >= T_INTEGER && tokenType <= T_LEN) {
            return true;
        } else {
            return false;
        }
    }// end isKeyword()


    /**
     * This is necessary because the lexer class caches some values from 
     * the input stream.  Here we reset them to what the current input stream 
     * values are.  This is done when we switch streams for including files.
     */    
    private void resetLexerState() {
        tokenStartCharIndex = input.index();
        tokenStartCharPositionInLine = input.getCharPositionInLine();
        tokenStartLine = input.getLine();
        token = null;
        text = null;
    }// end resetLexerState()


    // overrides nextToken in superclass
    public Token nextToken() {
        Token tmpToken;
        tmpToken = super.nextToken();

        if(tmpToken.getType() == EOF) {
            tmpToken.setChannel(Token.DEFAULT_CHANNEL);
            if(this.oldStreams != null && this.oldStreams.empty() == false) {
                FortranToken eofToken = 
                    new FortranToken(this.input, T_EOF, Token.DEFAULT_CHANNEL,
                        this.input.index(), this.input.index()+1);
                eofToken.setText("EOF token text");
                tmpToken = eofToken;
                /* We have at least one previous input stream on the stack, 
                   meaning we should be at the end of an included file.  
                   Switch back to the previous stream and continue.  */
                this.input = this.oldStreams.pop();
                /* Is this ok to do??  */
                resetLexerState();
            }

            return tmpToken;
        }

        if(tmpToken.getType() != LINE_COMMENT && 
           tmpToken.getType() != WS &&
           tmpToken.getType() != PREPROCESS_LINE) {
            prevToken = tmpToken;
        } 

        if(tmpToken.getType() == T_EOS && continueFlag == true) {
            tmpToken.setChannel(99);
        } else if(continueFlag == true) {
            if(tmpToken.getType() != LINE_COMMENT &&
               tmpToken.getType() != WS &&
               tmpToken.getType() != PREPROCESS_LINE &&
               tmpToken.getType() != CONTINUE_CHAR) {
                // if the token we have is not T_EOS or any kind of WS or 
                // comment, and we have a continue, then this should be the
                // first token on the line folliwng the '&'.  this means that
                // we only have one '&' (no '&' on the second line) and we 
                // need to clear the flag so we know to process the T_EOS.
                continueFlag = false;
            }
        }

        return tmpToken;
    }// end nextToken()


    public int getIgnoreChannelNumber() {
        // return the channel number that antlr uses for ignoring a token
        return 99;
    }// end getIgnoreChannelNumber()

    
    public CharStream getInput() {
        return this.input;
    }


    /**
     * Do this here because not sure how to get antlr to generate the 
     * init code.  It doesn't seem to do anything with the @init block below.
     * This is called my FortranMain().
     */
    public FortranLexer(FortranStream input) {
        super(input);
        prevToken = null;
        continueFlag = false;
        includeLine = false;
        inFormat = false;
        oldStreams = new Stack<FortranStream>();
    }// end constructor()


    public void setIncludeDirs(ArrayList<String> includeDirs) {
        this.includeDirs = includeDirs;
    }// end setIncludeDirs()

    
    private File findFile(String fileName) {
        File tmpFile;
        String tmpPath;
        StringBuffer newFileName;
        
        tmpFile = new File(fileName);
        if(tmpFile.exists() == false) {
            /* the file doesn't exist by the given name from the include line, 
             * so we need to append it to each include dir and search.  */
            for(int i = 0; i < this.includeDirs.size(); i++) {
                tmpPath = this.includeDirs.get(i);

                newFileName = new StringBuffer();

                /* Build the new file name with the path.  Add separator to 
                 * end of path if necessary (unix specific).  */
                newFileName = newFileName.append(tmpPath);
                if(tmpPath.charAt(tmpPath.length()-1) != '/') {
                    newFileName = newFileName.append('/');
                }
                newFileName = newFileName.append(fileName);

                /* Try opening the new file.  */
                tmpFile = new File(newFileName.toString());
                if(tmpFile.exists() == true) {
                    return tmpFile;
                }
            }

            /* File did not exist.  */
            return null;
        } else {
            return null;
        }
    }// end findFile()


    private void includeFile() {
        /* For debugging, print out the list of include dirs.  */
        if(this.includeDirs == null || this.includeDirs.size() == 0) {
            System.err.println("no include dirs given!");
            return;
        }

        if(prevToken != null) {
            String fileName = null;
            String charConst = null;
            FortranStream includedStream = null;
            File includedFile;

            charConst = prevToken.getText();
            fileName = charConst.substring(1, charConst.length()-1);

            /* Find the file, including it's complete path.  */
            includedFile = findFile(fileName);
            if(includedFile == null) {
                System.err.println("Error: Could not open file '" + 
                    charConst.substring(1, charConst.length()-1) + "'");
                return;
            }

//             System.err.println("includedFile.getAbsolutePath() is: " + 
//                 includedFile.getAbsolutePath());

            /* Create a new stream for the included file.  */
            try {
                includedStream = 
                new FortranStream(includedFile.getAbsolutePath(), 
                    ((FortranStream)this.input).getSourceForm());
            } catch(IOException e) {
                e.printStackTrace();
                return;
            }

            /* Save current character stream.  */
            oldStreams.push((FortranStream)(this.input));
            this.input = includedStream;
            /* Is this ok to do??  */
            resetLexerState();
        } else {
            System.err.println("Error: Unable to determine file name from " + 
                               "include line");
        }

        return;
    }// end includeFile()

}

@init {
    prevToken = null;
}

/*
 * Lexer rules
 */

/* 
 * Note: antlr sometimes has LL(*) failures with the grammars, but not always.
 * it seems that it may be the timeout issue that was mentioned..
 */

T_EOS 
@after {
    // if the previous token was a T_EOS, then the one we're 
    // processing now is whitespace, so throw it away.
    // also, if the previous token is null it means we have a 
    // blank line or a semicolon at the start of the file and 
    // we need to ignore it.  
    if(prevToken == null || 
        (prevToken != null && prevToken.getType() == T_EOS)) {
        $channel=HIDDEN;
    } 

    if(includeLine) {
        $channel=HIDDEN;
        // Part of include file handling..
        includeFile();
        includeLine = false;
    }

    // Make sure we clear the flag saying we're in a format-stmt
    inFormat = false;
}
      : ';' 
      |  ('\r')? ('\n')
      ;
                

/* if this is a fragment, the generated code never seems to execute the
 * action.  the action needs to set the flag so T_EOS knows whether it should
 * be channel 99'ed or not (ignor T_EOS if continuation is true, which is the
 * case of the & at the end of a line).
 */
CONTINUE_CHAR : '&' { continueFlag = !continueFlag;
//                       _channel = 99;
                    $channel=HIDDEN;
//                     $channel=99;
        }
              ;


// R427 from char-literal-constant
T_CHAR_CONSTANT
        : ('\'' ( SQ_Rep_Char )* '\'')+ { 
            if(includeLine) 
//                 _channel=99;
                $channel=HIDDEN;
//             $channel=99;
        }
        | ('\"' ( DQ_Rep_Char )* '\"')+ { 
            if(includeLine) 
//                _channel=99;
                $channel=HIDDEN;
//             $channel=99;
        }
        ;

T_DIGIT_STRING
	:	Digit_String 
	;

// R412
BINARY_CONSTANT
    : ('b'|'B') '\'' ('0'..'1')+ '\''
    | ('b'|'B') '\"' ('0'..'1')+ '\"'
    ;

// R413
OCTAL_CONSTANT
    : ('o'|'O') '\'' ('0'..'7')+ '\''
    | ('o'|'O') '\"' ('0'..'7')+ '\"'
    ;

// R414
HEX_CONSTANT
    : ('z'|'Z') '\'' (Digit|'a'..'f'|'A'..'F')+ '\''
    | ('z'|'Z') '\"' (Digit|'a'..'f'|'A'..'F')+ '\"'
    ;


WS  :  (' '|'\r'|'\t'|'\u000C') {// _channel=99;
            $channel=HIDDEN;
//             $channel=99;
//             _channel=99;
        }
    ;

/*
 * fragments
 */

// R409 digit_string
fragment
Digit_String : Digit+  ;


// R302 alphanumeric_character
fragment
Alphanumeric_Character : Letter | Digit | '_' ;

fragment
Special_Character
    :    ' ' .. '/' 
    |    ':' .. '@' 
    |    '[' .. '^' 
    |    '`' 
    |    '{' .. '~' 
    ;

fragment
Rep_Char : ~('\'' | '\"') ;

fragment
SQ_Rep_Char : ~('\'') ;
fragment
DQ_Rep_Char : ~('\"') ;

fragment
Letter : ('a'..'z' | 'A'..'Z') ;

fragment
Digit : '0'..'9'  ;

PREPROCESS_LINE : '#' ~('\n'|'\r')*  { // _channel=99;
            $channel=HIDDEN;
//             $channel=99;
        } ;

T_INCLUDE 
options {k=1;} : 'INCLUDE' { includeLine = true; // _channel=99;
            $channel=HIDDEN;
//             $channel=99;
        };


/*
 * from fortran03_lexer.g
 */

T_ASTERISK      : '*'   ;
T_COLON         : ':'   ;
T_COLON_COLON   : '::'  ;
T_COMMA         : ','   ;
T_EQUALS        : '='   ;
T_EQ_EQ         : '=='  ;
T_EQ_GT         : '=>'  ;
T_GREATERTHAN   : '>'   ;
T_GREATERTHAN_EQ: '>='  ;
T_LESSTHAN      : '<'   ;
T_LESSTHAN_EQ   : '<='  ;
T_LBRACKET      : '['   ;
T_LPAREN        : '('   ;
T_MINUS         : '-'   ;
T_PERCENT       : '%'   ;
T_PLUS          : '+'   ;
T_POWER         : '**'  ;
T_SLASH         : '/'   ;
T_SLASH_EQ      : '/='  ;
T_SLASH_SLASH   : '//'   ;
T_RBRACKET      : ']'   ;
T_RPAREN        : ')'   ;
T_UNDERSCORE    : '_'   ;

T_EQ            : '.EQ.' ;
T_NE            : '.NE.' ;
T_LT            : '.LT.' ;
T_LE            : '.LE.' ;
T_GT            : '.GT.' ;
T_GE            : '.GE.' ;

T_TRUE          : '.TRUE.'  ;
T_FALSE         : '.FALSE.' ;

T_NOT           : '.NOT.' ;
T_AND           : '.AND.' ;
T_OR            : '.OR.'  ;
T_EQV           : '.EQV.' ;
T_NEQV          : '.NEQV.';

T_PERIOD_EXPONENT 
    : '.' ('0'..'9')+ ('E' | 'e' | 'd' | 'D') ('+' | '-')? ('0'..'9')+  
    | '.' ('E' | 'e' | 'd' | 'D') ('+' | '-')? ('0'..'9')+  
    | '.' ('0'..'9')+
    | ('0'..'9')+ ('e' | 'E' | 'd' | 'D') ('+' | '-')? ('0'..'9')+ 
    ;

T_PERIOD        : '.' ;



// not sure what this is; is it just a place holder for something 
// else??  --Rickett, 10.27.06  
// it's a placeholder in the parser..
// make the text unreachable from valid Fortran.
T_XYZ           : '__XYZ__';

T_INTEGER       :       'INTEGER'       ;
T_REAL          :       'REAL'          ;
T_COMPLEX       :       'COMPLEX'       ;
T_CHARACTER     :       'CHARACTER'     ;
T_LOGICAL       :       'LOGICAL'       ;

T_ABSTRACT      :       'ABSTRACT'      ;
T_ALLOCATABLE   :       'ALLOCATABLE'   ;
T_ALLOCATE      :       'ALLOCATE'      ;
T_ASSIGNMENT    :       'ASSIGNMENT'    ;
// ASSIGN statements are a deleted feature.
T_ASSIGN        :       'ASSIGN'        ;
T_ASSOCIATE     :       'ASSOCIATE'     ;
T_ASYNCHRONOUS  :       'ASYNCHRONOUS'  ;
T_BACKSPACE     :       'BACKSPACE'     ;
T_BLOCK         :       'BLOCK'         ;
T_BLOCKDATA     :       'BLOCKDATA'     ;
T_CALL          :       'CALL'          ;
T_CASE          :       'CASE'          ;
T_CLASS         :       'CLASS'         ;
T_CLOSE         :       'CLOSE'         ;
T_COMMON        :       'COMMON'        ;
T_CONTAINS      :       'CONTAINS'      ;
T_CONTINUE      :       'CONTINUE'      ;
T_CYCLE         :       'CYCLE'         ;
T_DATA          :       'DATA'          ;
T_DEFAULT       :       'DEFAULT'       ;
T_DEALLOCATE    :       'DEALLOCATE'    ;
T_DEFERRED      :       'DEFERRED'      ;
T_DO            :       'DO'            ;
T_DOUBLE        :       'DOUBLE'        ;
T_DOUBLEPRECISION:      'DOUBLEPRECISION' ;
T_DOUBLECOMPLEX:        'DOUBLECOMPLEX' ;
T_ELEMENTAL     :       'ELEMENTAL'     ;
T_ELSE          :       'ELSE'          ;
T_ELSEIF        :       'ELSEIF'        ;
T_ELSEWHERE     :       'ELSEWHERE'     ;
T_ENTRY         :       'ENTRY'         ;
T_ENUM          :       'ENUM'          ;
T_ENUMERATOR    :       'ENUMERATOR'    ;
T_EQUIVALENCE   :       'EQUIVALENCE'   ;
T_EXIT          :       'EXIT'          ;
T_EXTENDS       :       'EXTENDS'       ;
T_EXTERNAL      :       'EXTERNAL'      ;
T_FILE          :       'FILE'          ;
T_FINAL         :       'FINAL'         ;
T_FLUSH         :       'FLUSH'         ;
T_FORALL        :       'FORALL'        ;
T_FORMAT        :       'FORMAT'        { inFormat = true; };
T_FORMATTED     :       'FORMATTED'     ;
T_FUNCTION      :       'FUNCTION'      ;
T_GENERIC       :       'GENERIC'       ;
T_GO            :       'GO'            ;
T_GOTO          :       'GOTO'          ;
T_IF            :       'IF'            ;
T_IMPLICIT      :       'IMPLICIT'      ;
T_IMPORT        :       'IMPORT'        ;
T_IN            :       'IN'            ;
T_INOUT         :       'INOUT'         ;
T_INTENT        :       'INTENT'        ;
T_INTERFACE     :       'INTERFACE'     ;
T_INTRINSIC     :       'INTRINSIC'     ;
T_INQUIRE       :       'INQUIRE'       ;
T_MODULE        :       'MODULE'        ;
T_NAMELIST      :       'NAMELIST'      ;
T_NONE          :       'NONE'          ;
T_NON_INTRINSIC :       'NON_INTRINSIC' ;
T_NON_OVERRIDABLE:      'NON_OVERRIDABLE';
T_NOPASS        :       'NOPASS'        ;
T_NULLIFY       :       'NULLIFY'       ;
T_ONLY          :       'ONLY'          ;
T_OPEN          :       'OPEN'          ;
T_OPERATOR      :       'OPERATOR'      ;
T_OPTIONAL      :       'OPTIONAL'      ;
T_OUT           :       'OUT'           ;
T_PARAMETER     :       'PARAMETER'     ;
T_PASS          :       'PASS'          ;
T_PAUSE         :       'PAUSE'         ;
T_POINTER       :       'POINTER'       ;
T_PRINT         :       'PRINT'         ;
T_PRECISION     :       'PRECISION'     ;
T_PRIVATE       :       'PRIVATE'       ;
T_PROCEDURE     :       'PROCEDURE'     ;
T_PROGRAM       :       'PROGRAM'       ;
T_PROTECTED     :       'PROTECTED'     ;
T_PUBLIC        :       'PUBLIC'        ;
T_PURE          :       'PURE'          ;
T_READ          :       'READ'          ;
T_RECURSIVE     :       'RECURSIVE'     ;
T_RESULT        :       'RESULT'        ;
T_RETURN        :       'RETURN'        ;
T_REWIND        :       'REWIND'        ;
T_SAVE          :       'SAVE'          ;
T_SELECT        :       'SELECT'        ;
T_SELECTCASE    :       'SELECTCASE'    ;
T_SELECTTYPE    :       'SELECTTYPE'    ;
T_SEQUENCE      :       'SEQUENCE'      ;
T_STOP          :       'STOP'          ;
T_SUBROUTINE    :       'SUBROUTINE'    ;
T_TARGET        :       'TARGET'        ;
T_THEN          :       'THEN'          ;
T_TO            :       'TO'            ;
T_TYPE          :       'TYPE'          ;
T_UNFORMATTED   :       'UNFORMATTED'   ;
T_USE           :       'USE'           ;
T_VALUE         :       'VALUE'         ;
T_VOLATILE      :       'VOLATILE'      ;
T_WAIT          :       'WAIT'          ;
T_WHERE         :       'WHERE'         ;
T_WHILE         :       'WHILE'         ;
T_WRITE         :       'WRITE'         ;

T_ENDASSOCIATE  :       'ENDASSOCIATE'  ;
T_ENDBLOCK      :       'ENDBLOCK'      ;
T_ENDBLOCKDATA  :       'ENDBLOCKDATA'  ;
T_ENDDO         :       'ENDDO'         ;
T_ENDENUM       :       'ENDENUM'       ;
T_ENDFORALL     :       'ENDFORALL'     ;
T_ENDFILE       :       'ENDFILE'       ;
T_ENDFUNCTION   :       'ENDFUNCTION'   ;
T_ENDIF         :       'ENDIF'         ;
T_ENDINTERFACE  :       'ENDINTERFACE'  ;
T_ENDMODULE     :       'ENDMODULE'     ;
T_ENDPROGRAM    :       'ENDPROGRAM'    ;
T_ENDSELECT     :       'ENDSELECT'     ;
T_ENDSUBROUTINE :       'ENDSUBROUTINE' ;
T_ENDTYPE       :       'ENDTYPE'       ;
T_ENDWHERE      :       'ENDWHERE'      ;

T_END   : 'END'
        ;

T_DIMENSION     :       'DIMENSION'     ;

T_KIND : 'KIND' ;
T_LEN : 'LEN' ;

T_BIND : 'BIND' ;

T_HOLLERITH : Digit_String 'H' 
    { 
        // If we're inside a format stmt we don't want to process it as 
        // a Hollerith constant because it's most likely an H-edit descriptor. 
        // However, the H-edit descriptor needs processed the same way both 
        // here and in the prepass.
        StringBuffer hollConst = new StringBuffer();
        int count = Integer.parseInt($Digit_String.text);

        for(int i = 0; i < count; i++) 
           hollConst = hollConst.append((char)input.LA(i+1));
        for(int i = 0; i < count; i++)
           // consume the character so the lexer doesn't try matching it.
           input.consume();
    };

// Must come after .EQ. (for example) or will get matched first
// TODO:: this may have to be done in the parser w/ a rule such as:
// T_PERIOD T_IDENT T_PERIOD
T_DEFINED_OP
    :    '.' Letter+ '.'
    ;

// // used to catch edit descriptors and other situations
// T_ID_OR_OTHER
// 	:	'ID_OR_OTHER'
// 	;

// extra, context-sensitive terminals that require communication between parser and scanner
// added the underscores so there is no way this could overlap w/ any valid
// idents in Fortran.  we just need this token to be defined so we can 
// create one of them while we're fixing up labeled do stmts.
T_LABEL_DO_TERMINAL
	:	'__LABEL_DO_TERMINAL__'
	;

T_DATA_EDIT_DESC : '__T_DATA_EDIT_DESC__' ;
T_CONTROL_EDIT_DESC : '__T_CONTROL_EDIT_DESC__' ;
T_CHAR_STRING_EDIT_DESC : '__T_CHAR_STRING_EDIT_DESC__' ;

T_STMT_FUNCTION 
	:	'STMT_FUNCTION'
	;

T_ASSIGNMENT_STMT : '__T_ASSIGNMENT_STMT__' ;
T_PTR_ASSIGNMENT_STMT : '__T_PTR_ASSIGNMENT_STMT__' ;
T_ARITHMETIC_IF_STMT : '__T_ARITHMETIC_IF_STMT__' ;
T_ALLOCATE_STMT_1 : '__T_ALLOCATE_STMT_1__' ;
T_WHERE_STMT : '__T_WHERE_STMT__' ;
T_IF_STMT : '__T_IF_STMT__' ;
T_FORALL_STMT : '__T_FORALL_STMT__' ;
T_WHERE_CONSTRUCT_STMT : '__T_WHERE_CONSTRUCT_STMT__' ;
T_FORALL_CONSTRUCT_STMT : '__T_FORALL_CONSTRUCT_STMT__' ;
T_INQUIRE_STMT_2 : '__T_INQUIRE_STMT_2__' ;
// text for the real constant will be set when a token of this type is 
// created by the prepass.
T_REAL_CONSTANT : '__T_REAL_CONSTANT__' ; 

T_EOF: '__T_EOF__' ;

// R304
T_IDENT
options {k=1;}
	:	Letter ( Alphanumeric_Character )*
	;

LINE_COMMENT
    : '!'  ~('\n'|'\r')*  

        {// _channel=99;
            $channel=HIDDEN;
//             $channel=99;
        }
    ;

/* Need a catch-all rule because of fixed-form being allowed to use any 
   character in column 6 to designate a continuation.  */
MISC_CHAR : ~('\n' | '\r') ;

