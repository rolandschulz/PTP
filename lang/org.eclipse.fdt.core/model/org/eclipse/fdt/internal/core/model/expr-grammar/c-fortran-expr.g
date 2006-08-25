grammar exprParser;

start 	:	expr ;

/*
 * Section 7:
 */

// R701
primary
	:	Digit+
	|	('a'|'b'|'c'| 'd' )
//	|	array_constructor
//	|	structure_constructor
//	|	type_param_inquiry
//	|	type_param_name
	|	'(' expr ')'
	;

// R702
level_1_expr
//    : ( defined_unary_op )? primary
    : ( defined_unary_op )? primary
    ;

// R703
defined_unary_op
//	:	T_PERIOD Letter ( Letter )* T_PERIOD
	:	'.DU.'
	;

// R704
mult_operand
//    : level_1_expr ( power_op mult_operand )?
// no left recursion
    : level_1_expr /* ( power_op mult_operand )? */ (mult_op level_1_expr)* 
    ;

// R705
add_operand
//    : ( add_operand mult_op )? mult_operand
//    : ( mult_operand mult_op )? mult_operand
        // not sure of order of (add_op)? (could trail everything?
    : mult_operand /*(add_op)?*/ (add_op mult_operand)* 
    ;

// R706
// C doesn't have concat operator, shift_expression call additive_expression
level_2_expr
//    : ( ( level_2_expr )? add_op )? add_operand
// check notes on how to remove this left recursion  (WARNING something like the following)
// (add_op)? (add_operand add_op)* add_operand
    : (add_op)? add_operand (concat_op add_operand)*
// not sure what to do about trailing level_2_expr
// I think substituting add_operand is ok as it effectively inlines the level_2_expr calling and_operand
    ;
    
// R707
power_op
	:	T_POWER
	;

// R708
mult_op
	:	T_ASTERISK
	|	T_SLASH
	;

// R709
add_op
	:	T_PLUS
	|	T_MINUS
	;

// R710
// relational_expression
//   : shift_expression (('<'|'>'|'<='|'>=') shift_expression)* ;
// equality_expression etc chained in separately
level_3_expr
//    : ( level_3_expr concat_op )? level_2_expr
//    : ( level_2_expr concat_op )* level_2_expr
    : level_2_expr (rel_op level_2_expr)* 
    ;

// R711
concat_op
	:	T_SLASH_SLASH
	;

// R712
level_4_expr
//    : ( level_3_expr rel_op )? level_3_expr
    : level_3_expr  // is  not_op )? appended here?
    ;

// R713
rel_op
	:	T_EQ
//	|	T_NE
//	|	T_LT
//	|	T_LE
//	|	T_GT
//	|	T_GE
//	|	T_EQ_EQ
//	|	T_SLASH_EQ
//	|	T_LESSTHAN
//	|	T_LESSTHAN_EQ
//	|	T_GREATERTHAN
//	|	T_GREATERTHAN_EQ
	;

// R714
// logical_and_expression
//   : inclusive_or_expression ('&&' inclusive_or_expression)* ;

and_operand
//    :    ( not_op )? level_4_expr
    :    ( not_op )? level_4_expr (and_op level_4_expr)* // test recursive level_4_expr call, could be and_operand?
    ;

// R715
// logical_or_expression
//   : logical_and_expression ('||' logical_and_expression)* ;
or_operand
//    : ( or_operand and_op )? and_operand
//    : ( and_operand and_op )* and_operand 
    : and_operand ( or_op and_operand )*
// leading optional moved to and_operand
// trailing conditional op from equiv_operand and operand from and_operand
    ;

// R716
// conditional_expression
//   : logical_or_expression ('?' expression ':' conditional_expression)? ;
// actual C equality_expression chained after logical_and_expression
equiv_operand
//    : ( equiv_operand or_op )? or_operand
//    : ( or_operand or_op )* or_operand 
    : or_operand ( equiv_op or_operand )*
// leading optional moved to or_operand
// trailing conditional op from level_5_expr and operand from or_operand
    ;


// R717
level_5_expr
//    : ( level_5_expr equiv_op )? equiv_operand
//    : ( equiv_operand equiv_op )* equiv_operand
    : equiv_operand ( defined_binary_op equiv_operand )*
// leading optional moved to equiv_operand
// trailing conditional op from expr and operand from equiv_operand
    ;

// R718
not_op
	:	T_NOT
	;

// R719
and_op
	:	T_AND
	;

// R720
or_op
	:	T_OR
	;

// R721
equiv_op
	:	T_EQV
//	|	T_NEQV
	;

// R722
// assignment_expression is conditional_expression begins c-expr grammar
expr
//    : ( expr defined_binary_op )? level_5_expr
//    : ( level_5_expr defined_binary_op )* level_5_expr
    : level_5_expr
    ;

// R723
defined_binary_op
	:	T_PERIOD Letter ( Letter )* T_PERIOD
	;

// created new rule to remove defined_unary_op or defined_binary_op ambiguity
defined_unary_or_binary_op
	:	T_PERIOD Letter ( Letter )* T_PERIOD
	;


Digit	:	'0'..'9' ;
T_PERIOD:	'.' ;
Letter	:	'a'..'z' ;
T_POWER	:	'**' ;
T_ASTERISK :	'*' ;
T_SLASH	:	'/' ;
T_PLUS	:	'+' ;
T_MINUS	:	'-' ;
T_SLASH_SLASH :	'//' ;

T_EQ	:	'.EQ.' ;
T_NOT	:	'.NOT.' ;
T_AND 	:	'.AND.' ;
T_OR	:	'.OR.' ;
T_EQV	:	'.EQV.' ;
