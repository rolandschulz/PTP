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
 * @author Craig E Rasmussen, Christopher D. Rickett
 */
 
package parser.java;
}

@members {
    private Token prevToken;
    private boolean continueFlag;
    private boolean includeLine;

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


    // overrides nextToken in superclass
    public Token nextToken() {
        Token tmpToken;
        tmpToken = super.nextToken();

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
    }// end constructor()

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
      : ';' 
        { 
            // if the previous token was a T_EOS, then the one we're 
            // processing now is whitespace, so throw it away.
            // also, ignore semicolons that come before any real 
            // statements (is this correct??).  --Rickett, 11.28.06
            if(prevToken == null || 
                (prevToken != null && prevToken.getType() == T_EOS)) {
//                 _channel=99;
                $channel=HIDDEN;
//                 $channel=99;
//                 $channel=HIDDEN;
            }

        }
      |  ('\r')? ('\n')
        {
            // if the previous token was a T_EOS, then the one we're 
            // processing now is whitespace, so throw it away.
            // also, if the previous token is null it means we have a 
            // blank line or a semicolon at the start of the file and 
            // we need to ignore it.  
            if(prevToken == null || 
                (prevToken != null && prevToken.getType() == T_EOS)) {
//                _channel=99;
                $channel=HIDDEN;
//                 $channel=99;
            } 

            if(includeLine) {
//                 _channel=99; 
                $channel=HIDDEN;
//                 $channel=99;
                includeLine = false;
            }
        }
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
    : '.' ('0'..'9')* ('E' | 'e' | 'd' | 'D') ('+' | '-')? ('0'..'9')+  
    | '.' ('0'..'9')* ('+' | '-') ('0'..'9')+ 
    | '.' ('0'..'9')+
    | ('0'..'9')+ ('e' | 'E' | 'd' | 'D') ('+' | '-')? ('0'..'9')+ 
    ;

T_PERIOD        : '.' ;



// not sure what this is; is it just a place holder for something 
// else??  --Rickett, 10.27.06  
// it's a placeholder in the parser..
T_XYZ           : 'XYZ';

T_INTEGER       :       'INTEGER'       ;
T_REAL          :       'REAL'          ;
T_COMPLEX       :       'COMPLEX'       ;
T_CHARACTER     :       'CHARACTER'     ;
T_LOGICAL       :       'LOGICAL'       ;

T_ABSTRACT      :       'ABSTRACT'      ;
T_ALLOCATABLE   :       'ALLOCATABLE'   ;
T_ALLOCATE      :       'ALLOCATE'      ;
T_ASSIGNMENT    :       'ASSIGNMENT'    ;
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
T_FORMAT        :       'FORMAT'        ;
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



T_BIND_LPAREN_C
        : 'BIND' '(' 'C'
        ;


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

