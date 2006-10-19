lexer grammar FortranLexer;
options {
  language=Java;

}

T194 : 'XYZ' ;

// $ANTLR src "fortran.g" 3142
/**
 * end of statement
 */
//eos
//	:	T_EOL
//	|	';'
//	;	


/*
 * Lexer rules
 */

T_EOS : ';' ;

// R427 from char-literal-constant
// $ANTLR src "fortran.g" 3158
T_CHAR_CONSTANT
        : '\'' ( Rep_Char )* '\''
        | '\"' ( Rep_Char )* '\"'
        ;

// $ANTLR src "fortran.g" 3163
T_DIGIT_STRING
	:	Digit_String
	;

// R412
// $ANTLR src "fortran.g" 3168
BINARY_CONSTANT
    : ('b'|'B') '\'' ('0'..'1')+ '\''
    | ('b'|'B') '\"' ('0'..'1')+ '\"'
    ;

// R413
// $ANTLR src "fortran.g" 3174
OCTAL_CONSTANT
    : ('o'|'O') '\'' ('0'..'7')+ '\''
    | ('o'|'O') '\"' ('0'..'7')+ '\"'
    ;

// R414
// $ANTLR src "fortran.g" 3180
HEX_CONSTANT
    : ('z'|'Z') '\'' (Digit|'a'..'f'|'A'..'F')+ '\''
    | ('z'|'Z') '\"' (Digit|'a'..'f'|'A'..'F')+ '\"'
    ;

// $ANTLR src "fortran.g" 3185
REAL_CONSTANT
	:	Significand E_Exponent?
	|	Digit_String  E_Exponent
	;	

// $ANTLR src "fortran.g" 3190
DOUBLE_CONSTANT
	:	Significand D_Exponent
	|	Digit_String  D_Exponent
	;	

// $ANTLR src "fortran.g" 3195
WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') {channel=99;}
    ;

/*
 * fragments
 */

// R409 digit_string
// $ANTLR src "fortran.g" 3203
fragment
Digit_String : Digit+ ;

// R418 significand
// $ANTLR src "fortran.g" 3207
fragment
Significand
    :   Digit_String '.' ( Digit_String )?
    |   '.' Digit_String
    ;	

// $ANTLR src "fortran.g" 3213
fragment
E_Exponent : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

// $ANTLR src "fortran.g" 3216
fragment
D_Exponent : ('d'|'D') ('+'|'-')? ('0'..'9')+ ;

// R302 alphanumeric_character
// $ANTLR src "fortran.g" 3220
fragment
Alphanumeric_Character : Letter | Digit | '_' ;

// $ANTLR src "fortran.g" 3223
fragment
Special_Character
    :    ' ' .. '/'
    |    ':' .. '@'
    |    '[' .. '^'
    |    '`'
    |    '{' .. '~'
    ;

// $ANTLR src "fortran.g" 3232
fragment
Rep_Char : ' '..'~' ;

// $ANTLR src "fortran.g" 3235
fragment
Letter : ('a'..'z' | 'A'..'Z') ;

// $ANTLR src "fortran.g" 3238
fragment
Digit : '0'..'9' ;

/*
 * from fortran03_lexer.g
 */

// $ANTLR src "fortran.g" 3245
T_ASTERISK      : '*'   ;
// $ANTLR src "fortran.g" 3246
T_COLON         : ':'   ;
// $ANTLR src "fortran.g" 3247
T_COLON_COLON   : '::'  ;
// $ANTLR src "fortran.g" 3248
T_COMMA         : ','   ;
// $ANTLR src "fortran.g" 3249
T_EQUALS        : '='   ;
// $ANTLR src "fortran.g" 3250
T_EQ_EQ         : '=='  ;
// $ANTLR src "fortran.g" 3251
T_EQ_GT         : '=>'  ;
// $ANTLR src "fortran.g" 3252
T_GREATERTHAN   : '>'   ;
// $ANTLR src "fortran.g" 3253
T_GREATERTHAN_EQ: '>='  ;
// $ANTLR src "fortran.g" 3254
T_LESSTHAN      : '<'   ;
// $ANTLR src "fortran.g" 3255
T_LESSTHAN_EQ   : '<='  ;
// $ANTLR src "fortran.g" 3256
T_LBRACKET      : '['   ;
// $ANTLR src "fortran.g" 3257
T_LPAREN        : '('   ;
// $ANTLR src "fortran.g" 3258
T_MINUS         : '-'   ;
// $ANTLR src "fortran.g" 3259
T_PERCENT       : '%'   ;
// $ANTLR src "fortran.g" 3260
T_PERIOD        : '.'   ;
// $ANTLR src "fortran.g" 3261
T_PLUS          : '+'   ;
// $ANTLR src "fortran.g" 3262
T_POWER         : '**'  ;
// $ANTLR src "fortran.g" 3263
T_SLASH         : '/'   ;
// $ANTLR src "fortran.g" 3264
T_SLASH_EQ      : '/='  ;
// $ANTLR src "fortran.g" 3265
T_SLASH_SLASH   : '//'   ;
// $ANTLR src "fortran.g" 3266
T_RBRACKET      : ']'   ;
// $ANTLR src "fortran.g" 3267
T_RPAREN        : ')'   ;
// $ANTLR src "fortran.g" 3268
T_UNDERSCORE    : '_'   ;

// $ANTLR src "fortran.g" 3270
T_EQ            : '.EQ.' ;
// $ANTLR src "fortran.g" 3271
T_NE            : '.NE.' ;
// $ANTLR src "fortran.g" 3272
T_LT            : '.LT.' ;
// $ANTLR src "fortran.g" 3273
T_LE            : '.LE.' ;
// $ANTLR src "fortran.g" 3274
T_GT            : '.GT.' ;
// $ANTLR src "fortran.g" 3275
T_GE            : '.GE.' ;

// $ANTLR src "fortran.g" 3277
T_TRUE          : '.TRUE.'  ;
// $ANTLR src "fortran.g" 3278
T_FALSE         : '.FALSE.' ;

// $ANTLR src "fortran.g" 3280
T_NOT           : '.NOT.' ;
// $ANTLR src "fortran.g" 3281
T_AND           : '.AND.' ;
// $ANTLR src "fortran.g" 3282
T_OR            : '.OR.'  ;
// $ANTLR src "fortran.g" 3283
T_EQV           : '.EQV.' ;
// $ANTLR src "fortran.g" 3284
T_NEQV          : '.NEQV.';

// $ANTLR src "fortran.g" 3286
T_INTEGER       :       'INTEGER'       ;
// $ANTLR src "fortran.g" 3287
T_REAL          :       'REAL'          ;
// $ANTLR src "fortran.g" 3288
T_COMPLEX       :       'COMPLEX'       ;
// $ANTLR src "fortran.g" 3289
T_CHARACTER     :       'CHARACTER'     ;
// $ANTLR src "fortran.g" 3290
T_LOGICAL       :       'LOGICAL'       ;

// $ANTLR src "fortran.g" 3292
T_ABSTRACT      :       'ABSTRACT'      ;
// $ANTLR src "fortran.g" 3293
T_ALLOCATABLE   :       'ALLOCATABLE'   ;
// $ANTLR src "fortran.g" 3294
T_ALLOCATE      :       'ALLOCATE'      ;
// $ANTLR src "fortran.g" 3295
T_ASSIGNMENT    :       'ASSIGNMENT'    ;
// $ANTLR src "fortran.g" 3296
T_ASSOCIATE     :       'ASSOCIATE'     ;
// $ANTLR src "fortran.g" 3297
T_ASYNCHRONOUS  :       'ASYNCHRONOUS'  ;
// $ANTLR src "fortran.g" 3298
T_BACKSPACE     :       'BACKSPACE'     ;
// $ANTLR src "fortran.g" 3299
T_BLOCK         :       'BLOCK'         ;
// $ANTLR src "fortran.g" 3300
T_BLOCKDATA     :       'BLOCKDATA'     ;
// $ANTLR src "fortran.g" 3301
T_CALL          :       'CALL'          ;
// $ANTLR src "fortran.g" 3302
T_CASE          :       'CASE'          ;
// $ANTLR src "fortran.g" 3303
T_CLASS         :       'CLASS'         ;
// $ANTLR src "fortran.g" 3304
T_CLASS_IS      :       'CLASS' 'IS'    ;
// $ANTLR src "fortran.g" 3305
T_CLOSE         :       'CLOSE'         ;
// $ANTLR src "fortran.g" 3306
T_COMMON        :       'COMMON'        ;
// $ANTLR src "fortran.g" 3307
T_CONTAINS      :       'CONTAINS'      ;
// $ANTLR src "fortran.g" 3308
T_CONTINUE      :       'CONTINUE'      ;
// $ANTLR src "fortran.g" 3309
T_CYCLE         :       'CYCLE'         ;
// $ANTLR src "fortran.g" 3310
T_DATA          :       'DATA'          ;
// $ANTLR src "fortran.g" 3311
T_DEFAULT       :       'DEFAULT'       ;
// $ANTLR src "fortran.g" 3312
T_DEALLOCATE    :       'DEALLOCATE'    ;
// $ANTLR src "fortran.g" 3313
T_DEFERRED      :       'DEFERRED'      ;
// $ANTLR src "fortran.g" 3314
T_DIMENSION     :       'DIMENSION'     ;
// $ANTLR src "fortran.g" 3315
T_DO            :       'DO'            ;
// $ANTLR src "fortran.g" 3316
T_DOUBLE        :       'DOUBLE'        ;
// $ANTLR src "fortran.g" 3317
T_DOUBLEPRECISION:      'DOUBLEPRECISION' ;
// $ANTLR src "fortran.g" 3318
T_ELEMENTAL     :       'ELEMENTAL'     ;
// $ANTLR src "fortran.g" 3319
T_ELSE          :       'ELSE'          ;
// $ANTLR src "fortran.g" 3320
T_ELSEIF        :       'ELSEIF'        ;
// $ANTLR src "fortran.g" 3321
T_ELSEWHERE     :       'ELSEWHERE'     ;
// $ANTLR src "fortran.g" 3322
T_ENTRY         :       'ENTRY'         ;
// $ANTLR src "fortran.g" 3323
T_ENUM          :       'ENUM'          ;
// $ANTLR src "fortran.g" 3324
T_ENUMERATOR    :       'ENUMERATOR'    ;
// $ANTLR src "fortran.g" 3325
T_EQUIVALENCE   :       'EQUIVALENCE'   ;
// $ANTLR src "fortran.g" 3326
T_EXIT          :       'EXIT'          ;
// $ANTLR src "fortran.g" 3327
T_EXTENDS       :       'EXTENDS'       ;
// $ANTLR src "fortran.g" 3328
T_EXTERNAL      :       'EXTERNAL'      ;
// $ANTLR src "fortran.g" 3329
T_FILE          :       'FILE'          ;
// $ANTLR src "fortran.g" 3330
T_FINAL         :       'FINAL'         ;
// $ANTLR src "fortran.g" 3331
T_FLUSH         :       'FLUSH'         ;
// $ANTLR src "fortran.g" 3332
T_FORALL        :       'FORALL'        ;
// $ANTLR src "fortran.g" 3333
T_FORMAT        :       'FORMAT'        ;
// $ANTLR src "fortran.g" 3334
T_FORMATTED     :       'FORMATTED'     ;
// $ANTLR src "fortran.g" 3335
T_FUNCTION      :       'FUNCTION'      ;
// $ANTLR src "fortran.g" 3336
T_GENERIC       :       'GENERIC'       ;
// $ANTLR src "fortran.g" 3337
T_GO            :       'GO'            ;
// $ANTLR src "fortran.g" 3338
T_GOTO          :       'GOTO'          ;
// $ANTLR src "fortran.g" 3339
T_IF            :       'IF'            ;
// $ANTLR src "fortran.g" 3340
T_IMPLICIT      :       'IMPLICIT'      ;
// $ANTLR src "fortran.g" 3341
T_IMPORT        :       'IMPORT'        ;
// $ANTLR src "fortran.g" 3342
T_IN            :       'IN'            ;
// $ANTLR src "fortran.g" 3343
T_INOUT         :       'INOUT'         ;
// $ANTLR src "fortran.g" 3344
T_INTENT        :       'INTENT'        ;
// $ANTLR src "fortran.g" 3345
T_INTERFACE     :       'INTERFACE'     ;
// $ANTLR src "fortran.g" 3346
T_INTRINSIC     :       'INTRINSIC'     ;
// $ANTLR src "fortran.g" 3347
T_INQUIRE       :       'INQUIRE'       ;
// $ANTLR src "fortran.g" 3348
T_KIND          :       'KIND'          ;
// $ANTLR src "fortran.g" 3349
T_LEN           :       'LEN'           ;
// $ANTLR src "fortran.g" 3350
T_MODULE        :       'MODULE'        ;
// $ANTLR src "fortran.g" 3351
T_NAMELIST      :       'NAMELIST'      ;
// $ANTLR src "fortran.g" 3352
T_NONE          :       'NONE'          ;
// $ANTLR src "fortran.g" 3353
T_NON_INTRINSIC :       'NON_INTRINSIC' ;
// $ANTLR src "fortran.g" 3354
T_NON_OVERRIDABLE:      'NON_OVERRIDABLE';
// $ANTLR src "fortran.g" 3355
T_NOPASS        :       'NOPASS'        ;
// $ANTLR src "fortran.g" 3356
T_NULLIFY       :       'NULLIFY'       ;
// $ANTLR src "fortran.g" 3357
T_ONLY          :       'ONLY'          ;
// $ANTLR src "fortran.g" 3358
T_OPEN          :       'OPEN'          ;
// $ANTLR src "fortran.g" 3359
T_OPERATOR      :       'OPERATOR'      ;
// $ANTLR src "fortran.g" 3360
T_OPTIONAL      :       'OPTIONAL'      ;
// $ANTLR src "fortran.g" 3361
T_OUT           :       'OUT'           ;
// $ANTLR src "fortran.g" 3362
T_PARAMETER     :       'PARAMETER'     ;
// $ANTLR src "fortran.g" 3363
T_PASS          :       'PASS'          ;
// $ANTLR src "fortran.g" 3364
T_POINTER       :       'POINTER'       ;
// $ANTLR src "fortran.g" 3365
T_PRINT         :       'PRINT'         ;
// $ANTLR src "fortran.g" 3366
T_PRECISION     :       'PRECISION'     ;
// $ANTLR src "fortran.g" 3367
T_PRIVATE       :       'PRIVATE'       ;
// $ANTLR src "fortran.g" 3368
T_PROCEDURE     :       'PROCEDURE'     ;
// $ANTLR src "fortran.g" 3369
T_PROGRAM       :       'PROGRAM'       ;
// $ANTLR src "fortran.g" 3370
T_PROTECTED     :       'PROTECTED'     ;
// $ANTLR src "fortran.g" 3371
T_PUBLIC        :       'PUBLIC'        ;
// $ANTLR src "fortran.g" 3372
T_PURE          :       'PURE'          ;
// $ANTLR src "fortran.g" 3373
T_READ          :       'READ'          ;
// $ANTLR src "fortran.g" 3374
T_RECURSIVE     :       'RECURSIVE'     ;
// $ANTLR src "fortran.g" 3375
T_RESULT        :       'RESULT'        ;
// $ANTLR src "fortran.g" 3376
T_RETURN        :       'RETURN'        ;
// $ANTLR src "fortran.g" 3377
T_REWIND        :       'REWIND'        ;
// $ANTLR src "fortran.g" 3378
T_SAVE          :       'SAVE'          ;
// $ANTLR src "fortran.g" 3379
T_SELECT        :       'SELECT'        ;
// $ANTLR src "fortran.g" 3380
T_SELECTCASE    :       'SELECTCASE'    ;
// $ANTLR src "fortran.g" 3381
T_SELECTTYPE    :       'SELECTTYPE'    ;
// $ANTLR src "fortran.g" 3382
T_SEQUENCE      :       'SEQUENCE'      ;
// $ANTLR src "fortran.g" 3383
T_STOP          :       'STOP'          ;
// $ANTLR src "fortran.g" 3384
T_SUBROUTINE    :       'SUBROUTINE'    ;
// $ANTLR src "fortran.g" 3385
T_TARGET        :       'TARGET'        ;
// $ANTLR src "fortran.g" 3386
T_THEN          :       'THEN'          ;
// $ANTLR src "fortran.g" 3387
T_TO            :       'TO'            ;
// $ANTLR src "fortran.g" 3388
T_TYPE          :       'TYPE'          ;
// $ANTLR src "fortran.g" 3389
T_TYPE_IS       :       'TYPE' 'IS'     ;
// $ANTLR src "fortran.g" 3390
T_UNFORMATTED   :       'UNFORMATTED'   ;
// $ANTLR src "fortran.g" 3391
T_USE           :       'USE'           ;
// $ANTLR src "fortran.g" 3392
T_VALUE         :       'VALUE'         ;
// $ANTLR src "fortran.g" 3393
T_VOLATILE      :       'VOLATILE'      ;
// $ANTLR src "fortran.g" 3394
T_WAIT          :       'WAIT'          ;
// $ANTLR src "fortran.g" 3395
T_WHERE         :       'WHERE'         ;
// $ANTLR src "fortran.g" 3396
T_WHILE         :       'WHILE'         ;
// $ANTLR src "fortran.g" 3397
T_WRITE         :       'WRITE'         ;

// $ANTLR src "fortran.g" 3399
T_BIND_LPAREN_C
        : 'BIND' '(' 'C'
        ;

// $ANTLR src "fortran.g" 3403
T_ENDASSOCIATE  :       'ENDASSOCIATE'  ;
// $ANTLR src "fortran.g" 3404
T_ENDBLOCK      :       'ENDBLOCK'      ;
// $ANTLR src "fortran.g" 3405
T_ENDBLOCKDATA  :       'ENDBLOCKDATA'  ;
// $ANTLR src "fortran.g" 3406
T_ENDDO         :       'ENDDO'         ;
// $ANTLR src "fortran.g" 3407
T_ENDENUM       :       'ENDENUM'       ;
// $ANTLR src "fortran.g" 3408
T_ENDFORALL     :       'ENDFORALL'     ;
// $ANTLR src "fortran.g" 3409
T_ENDFILE       :       'ENDFILE'       ;
// $ANTLR src "fortran.g" 3410
T_ENDFUNCTION   :       'ENDFUNCTION'   ;
// $ANTLR src "fortran.g" 3411
T_ENDIF         :       'ENDIF'         ;
// $ANTLR src "fortran.g" 3412
T_ENDINTERFACE  :       'ENDINTERFACE'  ;
// $ANTLR src "fortran.g" 3413
T_ENDMODULE     :       'ENDMODULE'     ;
// $ANTLR src "fortran.g" 3414
T_ENDPROGRAM    :       'ENDPROGRAM'    ;
// $ANTLR src "fortran.g" 3415
T_ENDSELECT     :       'ENDSELECT'     ;
// $ANTLR src "fortran.g" 3416
T_ENDSUBROUTINE :       'ENDSUBROUTINE' ;
// $ANTLR src "fortran.g" 3417
T_ENDTYPE       :       'ENDTYPE'       ;
// $ANTLR src "fortran.g" 3418
T_ENDWHERE      :       'ENDWHERE'      ;

// $ANTLR src "fortran.g" 3420
T_END   : 'END'
        ;

// Must come after .EQ. (for example) or will get matched first
// $ANTLR src "fortran.g" 3424
T_DEFINED_OP
    :    '.' Letter+ '.'
    ;

// used to catch edit descriptors and other situations
// $ANTLR src "fortran.g" 3429
T_ID_OR_OTHER
	:	'ID_OR_OTHER'
	;

// extra, context-sensitive terminals that require communication between parser and scanner

// $ANTLR src "fortran.g" 3435
T_LABEL_DO_TERMINAL
	:	'LABEL_DO_TERMINAL'
	;

// $ANTLR src "fortran.g" 3439
T_STMT_FUNCTION 
	:	'STMT_FUNCTION'
	;

// R304
// $ANTLR src "fortran.g" 3444
T_IDENT
options {k=1;}
	:	Letter ( Alphanumeric_Character )*
	;

// $ANTLR src "fortran.g" 3449
LINE_COMMENT
    : '!' ~('\n'|'\r')* '\r'? '\n' {channel=99;}
    ;
