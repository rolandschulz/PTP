lexer grammar FortranLexer;

options {
    tokenVocab=FortranLexer;
}

@members {
    int prevIndex;
    int currIndex;
    int inLineComment;
//     TokenRewriteStream tokens;
//     List tokens;
    String lineString;
    String trimmedLine;
}

@init {
    prevIndex = 0;
    currIndex = 0;
    inLineComment = 0;
}

/*
 * Lexer rules
 */

// original rule
// T_EOS : ';';

T_EOS : ';' 
        { 
            // index of first char after the ';'
            // here, we must book keep the T_EOS like we do if it's 
            // a newline character so we can recognize if a newline 
            // comes after a ';' or if it's for a blank line, etc.
            currIndex = getCharIndex();
            prevIndex = currIndex;
        }
      |  ('\n')  
        {
            // index of the newline character
            if(inLineComment != 1) {
                currIndex = getCharIndex()-1;
            }
            
            lineString = input.substring(prevIndex, currIndex);
            // the trim() method removes all whitespace chars and 
            // returns a new String representing what's left.  if the 
            // resulting String has 0 length then the original string 
            // (or stmt in our case) has nothing but whitespace.
            trimmedLine = lineString.trim();
//             System.out.println("input.substring(prevIndex, currIndex): " + 
//                 input.substring(prevIndex, currIndex));
            
            
            if(trimmedLine.length() == 0) {
                channel=99; 
            }

            // now, if we may need to update the currIndex if we didn't 
            // before because we were at the end of a comment
            if(inLineComment == 1) {
                currIndex = getCharIndex()-1;
                inLineComment = 0;
            }
            prevIndex=currIndex;
            
        }
      ;
                



// R427 from char-literal-constant
T_CHAR_CONSTANT
        : '\'' ( Rep_Char )* '\''
        | '\"' ( Rep_Char )* '\"'
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

REAL_CONSTANT
	:	Significand E_Exponent?
	|	Digit_String  E_Exponent
	;	

DOUBLE_CONSTANT
	:	Significand D_Exponent
	|	Digit_String  D_Exponent
	;	

// original rule..
// WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') {channel=99;}
//     ;

WS  :  (' '|'\r'|'\t'|'\u000C') {channel=99;}
    ;

/*
 * fragments
 */

// R409 digit_string
fragment
Digit_String : Digit+ ;

// R418 significand
fragment
Significand
    :   Digit_String '.' ( Digit_String )?
    |   '.' Digit_String
    ;	

fragment
E_Exponent : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
D_Exponent : ('d'|'D') ('+'|'-')? ('0'..'9')+ ;

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
Rep_Char : ' '..'~' ;

fragment
Letter : ('a'..'z' | 'A'..'Z') ;

fragment
Digit : '0'..'9' ;

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
T_PERIOD        : '.'   ;
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

// not sure what this is; is it just a place holder for something 
// else??  --Rickett, 10.27.06
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
T_CLASS_IS      :       'CLASS' 'IS'    ;
T_CLOSE         :       'CLOSE'         ;
T_COMMON        :       'COMMON'        ;
T_CONTAINS      :       'CONTAINS'      ;
T_CONTINUE      :       'CONTINUE'      ;
T_CYCLE         :       'CYCLE'         ;
T_DATA          :       'DATA'          ;
T_DEFAULT       :       'DEFAULT'       ;
T_DEALLOCATE    :       'DEALLOCATE'    ;
T_DEFERRED      :       'DEFERRED'      ;
T_DIMENSION     :       'DIMENSION'     ;
T_DO            :       'DO'            ;
T_DOUBLE        :       'DOUBLE'        ;
T_DOUBLEPRECISION:      'DOUBLEPRECISION' ;
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
T_KIND          :       'KIND'          ;
T_LEN           :       'LEN'           ;
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
T_TYPE_IS       :       'TYPE' 'IS'     ;
T_UNFORMATTED   :       'UNFORMATTED'   ;
T_USE           :       'USE'           ;
T_VALUE         :       'VALUE'         ;
T_VOLATILE      :       'VOLATILE'      ;
T_WAIT          :       'WAIT'          ;
T_WHERE         :       'WHERE'         ;
T_WHILE         :       'WHILE'         ;
T_WRITE         :       'WRITE'         ;

T_BIND_LPAREN_C
        : 'BIND' '(' 'C'
        ;

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

// Must come after .EQ. (for example) or will get matched first
T_DEFINED_OP
    :    '.' Letter+ '.'
    ;

// used to catch edit descriptors and other situations
T_ID_OR_OTHER
	:	'ID_OR_OTHER'
	;

// extra, context-sensitive terminals that require communication between parser and scanner

T_LABEL_DO_TERMINAL
	:	'LABEL_DO_TERMINAL'
	;

T_STMT_FUNCTION 
	:	'STMT_FUNCTION'
	;

// R304
T_IDENT
options {k=1;}
	:	Letter ( Alphanumeric_Character )*
	;

// original rule.  need to remove the newline stuff since newlines 
// now are either whitespace or EOS.
// LINE_COMMENT
//     : '!' ~('\n'|'\r')* '\r'? '\n' 

//         {channel=99;}
//     ;

LINE_COMMENT
    : '!' {currIndex=getCharIndex()-2; inLineComment=1;} ~('\n'|'\r')*  

        {channel=99;}
    ;
