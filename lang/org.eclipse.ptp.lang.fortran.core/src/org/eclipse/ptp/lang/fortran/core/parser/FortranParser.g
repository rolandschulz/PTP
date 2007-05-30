/*
 * NOTES
 *
 * R303, R406, R417, R427, R428 underscore - added _ to rule (what happened to it?) * R410 sign - had '?' rather than '-'
 * R1209 import-stmt: MISSING a ]
 *
 * check comments regarding deleted for correctness
 *
 * TODO add (label)? to all statements...
 *    finished: continue-stmt, end-do-stmt
 *
 */


// added (label)? to any rule for a statement (*_stmt, for the most 
// part) because the draft says a label can exist with any statement.  
// questions are:
// - what about constructs such as if/else; where can labels all occur?
// - or the masked_elsewhere_stmt rule...


parser grammar FortranParser;

options {
    language=Java;
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
 * @author Craig E Rasmussen, Christopher D. Rickett, Bryan Rasmussen
 */
 
 package parser.java;
}

@members {
	public FortranParser(String[] args, TokenStream input, String kind, String filename) {
		super(input);
		ruleMemo = new HashMap[489+1];
		this.action = FortranParserActionFactory.newAction(args, this, kind, filename);
	}

	public boolean hasErrorOccurred = false;

    public void reportError(RecognitionException re) {
        super.reportError(re);
        hasErrorOccurred = true;
    }
    
    public IFortranParserAction getAction() {
        return action;
    }

    /** Provide an action object to implement the AST */
    private IFortranParserAction action = null;

	/* TODO - implement, needed by FortranParserAction */
	public Token getRightIToken() {
		return null;
	}

	/* TODO - implement, may be needed by FortranParserAction */
	public Token getRhsIToken(int i) {
		return null;
	}
}// end members


/*
 * Section 1:
 */

/*
 * Section 2:
 */


/** 
 * Got rid of the following rules: 
 * program
 * program_unit
 * external_subprogram
 *
 * this was done because Main() should now handle the top level rules
 * to try and reduce the amount of backtracking that must be done!
 * --Rickett, 12.07.06
 *
 * for some reason, leaving these three rules in, even though main() 
 * does NOT call them, prevents the parser from failing on the tests:
 * main_program.f03
 * note_6.24.f03
 * it appears to be something with the (program_unit)* part of the 
 * program rule.  --12.07.06
 *  --resolved: there's a difference in the code that is generated for 
 *              the end_of_stmt rule if these three rules are in there.
 *              to get around this, i modified the end_of_stmt rule.  
 *              see it for more details.  --12.11.06
 * 
 */

// // R201
// program
// 	:	program_unit
// 		( program_unit )*
// 	;

// // R202
// // backtracking needed to resolve prefix (e.g., REAL) ambiguity with main_program (REAL)
// program_unit
// options {backtrack=true; memoize=true; greedy=true;}
// 	:	main_program
// 	|	external_subprogram
// 	|	module
// 	|	block_data
// 	;

// // R203
// // modified to factor optional prefix
// external_subprogram
// 	:	(prefix)? function_subprogram
// 	|	subroutine_subprogram
// 	;

// R1101
// specification_part made non-optional to remove END ambiguity (as can be empty)
main_program
@init{boolean hasProgramStmt = false;
	  boolean hasExecutionPart = false;
	  boolean hasInternalSubprogramPart = false;
	 } // @init{INIT_BOOL_FALSE(hasProgramStmt); INIT_BOOL_FALSE(hasExecutionPart); INIT_BOOL_FALSE(hasInternalSubprogramPart);}
	:		{action.main_program__begin();}
		( program_stmt {hasProgramStmt = true;})?
		specification_part
		( execution_part {hasExecutionPart = true;})?
		( internal_subprogram_part {hasInternalSubprogramPart = true;})?
		end_program_stmt
			{ action.main_program(hasProgramStmt, hasExecutionPart, hasInternalSubprogramPart); }
	;

// added rule so could have one rule for main() to call for attempting
// to match a function subprogram.  the original rule, 
// external_subprogram, has (prefix)? for a function_subprogram.
ext_function_subprogram
    :   (prefix)? function_subprogram
    ;

// this rule used by main() to see if the first line we're about to 
// parse looks like a function subprogram or not.
ext_function_stmt_test
    :   (prefix)? function_stmt
    ;

// R204
// ERR_CHK 204 see ERR_CHK 207, implicit_part? removed (was after import_stmt*)
specification_part
	:	( use_stmt )*
		( import_stmt )*
		( declaration_construct )*
	;

// R205 implicit_part removed from grammar (see ERR_CHK 207)

// R206 implicit_part_stmt removed from grammar (see ERR_CHK 207)

// R207
// ERR_CHK 207 implicit_stmt must precede all occurances of rules following it in text below
// has been removed from grammar so must be done when reducing
declaration_construct
	:	entry_stmt
	|	parameter_stmt
	|	format_stmt
	|	implicit_stmt           // implicit_stmt must precede all occurances of the below
	|	derived_type_def
	|	enum_def
	|	interface_block
	|	procedure_declaration_stmt
	|	specification_stmt
	|	type_declaration_stmt
	|	stmt_function_stmt
	;

// R208
execution_part
	:	executable_construct
		( execution_part_construct )*
	;

// R209
execution_part_construct
	:	executable_construct  
	|	format_stmt
	|	entry_stmt
	|	data_stmt
	;

// R210
// T_CONTAINS inlined for contains_stmt
// TODO can contains_stmt have a label?
internal_subprogram_part
@init{int count = 1;}
	:	T_CONTAINS T_EOS
		internal_subprogram
		(internal_subprogram {count += 1;})*
			{ action.internal_subprogram_part(count); }
	;

// R211
// modified to factor optional prefix
internal_subprogram
	:	(prefix)? function_subprogram
	|	subroutine_subprogram
	;

// R212
specification_stmt
	:	access_stmt
	|	allocatable_stmt
	|	asynchronous_stmt
	|	bind_stmt
	|	common_stmt
	|	data_stmt
	|	dimension_stmt
	|	equivalence_stmt
	|	external_stmt
	|	intent_stmt
	|	intrinsic_stmt
	|	namelist_stmt
	|	optional_stmt
	|	pointer_stmt
	|	protected_stmt
	|	save_stmt
	|	target_stmt
	|	volatile_stmt
	|	value_stmt
	;

// R213
executable_construct
	:	action_stmt
	|	associate_construct
	|	case_construct
	|	do_construct
	|	forall_construct
	|	if_construct
	|	select_type_construct
	|	where_construct
	;


// R214
// C201 (R208) An execution-part shall not contain an end-function-stmt, end-program-stmt, or
//             end-subroutine-stmt.  (But they can be in a branch target statement, which
//             is not in the grammar, so the end-xxx-stmts deleted.)
// T_CONTINUE inlined for continue_stmt
// TODO continue-stmt is ambiguous with same in end-do, check for label and if
// label matches do-stmt label, then match end-do there
// the original generated rules do not allow the label, so add (label)?
action_stmt
@init {Token lbl = null;} //@init {INIT_TOKEN_NULL(lbl);}
// TODO:
// tried removing backtracking by inserting extra tokens in the stream by the 
// prepass that signals whether we have an assignment-stmt, a 
// pointer-assignment-stmt, or an arithmetic if.  this approach may work for
// other parts of backtracking also.  however, need to see if there is a way
// to define tokens w/o defining them in the lexer so that the lexer doesn't
// have to add them to it's parsing..  02.05.07
// options {backtrack=true;}
	:	allocate_stmt
	|	assignment_stmt
	|	backspace_stmt
	|	call_stmt
	|	close_stmt
	|	(label {lbl=$label.tk;})? T_CONTINUE T_EOS
			{ action.continue_stmt(lbl); } // R848 continue_stmt inlined
	|	cycle_stmt
	|	deallocate_stmt
	|	endfile_stmt
	|	exit_stmt
	|	flush_stmt
	|	forall_stmt
	|	goto_stmt
	|	if_stmt
    |   inquire_stmt  
	|	nullify_stmt
	|	open_stmt
	|	pointer_assignment_stmt
	|	print_stmt
	|	read_stmt
	|	return_stmt
	|	rewind_stmt
	|	stop_stmt
	|	wait_stmt
	|	where_stmt
	|	write_stmt
	|	arithmetic_if_stmt
	|	computed_goto_stmt
	;


// R215
keyword
	:	name
	;

/*
Section 3:
*/

// R301 character not used

// R302 alphanumeric_character converted to fragment

// R303 underscore inlined

// R304
name returns [Token tk]
	:	T_IDENT		{ tk = $T_IDENT; }
	;

// R305
// ERR_CHK 305 named_constant replaced by T_IDENT 
constant
	:	literal_constant	{ action.constant(null); }
	|	T_IDENT				{ action.constant($T_IDENT); }
	;

scalar_constant
    :    constant
    ;

// R306
literal_constant
	:	int_literal_constant
	|	real_literal_constant
	|	complex_literal_constant
	|	logical_literal_constant
	|	char_literal_constant
	|	boz_literal_constant
	;

// R307 named_constant was name inlined as T_IDENT

// R308
// C302 R308 int_constant shall be of type integer
// inlined integer portion of constant
int_constant
	:	int_literal_constant	{ action.int_constant(null); }
	|	T_IDENT					{ action.int_constant($T_IDENT); }
	;

// R309
// C303 R309 char_constant shall be of type character
// inlined character portion of constant
char_constant
	:	char_literal_constant	{ action.int_constant(null); }
	|	T_IDENT					{ action.int_constant($T_IDENT); }
	;

// R310
intrinsic_operator
	:	power_op
	|	mult_op
	|	add_op
	|	concat_op
	|	rel_op
	|	not_op
	|	and_op
	|	or_op
	|	equiv_op
	;

// R311
// removed defined_unary_op or defined_binary_op ambiguity with T_DEFINED_OP
defined_operator
	:	T_DEFINED_OP			{ action.defined_operator($T_DEFINED_OP); }
	|	extended_intrinsic_op	{ action.defined_operator(null); }
	;

// R312
extended_intrinsic_op
	:	intrinsic_operator
	;

// R313
// ERR_CHK 313 five characters or less
label returns [Token tk]
    : T_DIGIT_STRING {tk = $T_DIGIT_STRING;}
    ;

// action.label called here to store label in action class
label_list
@init{ int count=0;}
    :  		{action.label_list__begin();}
		lbl=label {count++; action.label($lbl.tk);} ( T_COMMA lbl=label {count++; action.label($lbl.tk);} )*
      		{action.label_list(count);}
    ;

/*
Section 4:
 */

// R401
type_spec
	:	intrinsic_type_spec
	|	derived_type_spec
	;

// R402
// ERR_CHK 402 scalar_int_expr replaced by expr
type_param_value
	:	expr		{ action.type_param_value(true, false, false); }
	|	T_ASTERISK	{ action.type_param_value(false, true, false); }
	|	T_COLON 	{ action.type_param_value(false, false, true); }
	;

// inlined scalar_int_expr C101 shall be a scalar

// inlined scalar_expr

// R403
// Nonstandard Extension: source BLAS
//	|	T_DOUBLE T_COMPLEX
//	|	T_DOUBLECOMPLEX
intrinsic_type_spec
@init{boolean hasKindSelector = false;} //@init{INIT_BOOL_FALSE(hasKindSelector);}
	:	T_INTEGER (kind_selector {hasKindSelector = true;})?
			{action.intrinsic_type_spec(IFortranParserAction.IntrinsicTypeSpec.INTEGER, hasKindSelector);}
	|	T_REAL (kind_selector {hasKindSelector = true;})?
			{action.intrinsic_type_spec(IFortranParserAction.IntrinsicTypeSpec.REAL, hasKindSelector);}
	|	T_DOUBLE T_PRECISION
			{action.intrinsic_type_spec(IFortranParserAction.IntrinsicTypeSpec.DOUBLEPRECISION, false);}
	|	T_DOUBLEPRECISION
			{action.intrinsic_type_spec(IFortranParserAction.IntrinsicTypeSpec.DOUBLEPRECISION, false);}
	|	T_COMPLEX (kind_selector {hasKindSelector = true;})?
			{action.intrinsic_type_spec(IFortranParserAction.IntrinsicTypeSpec.COMPLEX, hasKindSelector);}
	|	T_DOUBLE T_COMPLEX
			{action.intrinsic_type_spec(IFortranParserAction.IntrinsicTypeSpec.DOUBLECOMPLEX, false);}
	|	T_DOUBLECOMPLEX
			{action.intrinsic_type_spec(IFortranParserAction.IntrinsicTypeSpec.DOUBLECOMPLEX, false);}
	|	T_CHARACTER (char_selector {hasKindSelector = true;})?
			{action.intrinsic_type_spec(IFortranParserAction.IntrinsicTypeSpec.CHARACTER, hasKindSelector);}
	|	T_LOGICAL (kind_selector {hasKindSelector = true;})?
			{action.intrinsic_type_spec(IFortranParserAction.IntrinsicTypeSpec.LOGICAL, hasKindSelector);}
	;

// R404
// ERR_CHK 404 scalar_int_initialization_expr replaced by expr
// Nonstandard extension: source common practice
//	| T_ASTERISK T_DIGIT_STRING  // e.g., COMPLEX*16	
// TODO - check to see if second alternative is where it should go
kind_selector
    : T_LPAREN (T_KIND T_EQUALS)? expr T_RPAREN
    	{ action.kind_selector(true, null); }				// hasExpression = true
    | T_ASTERISK T_DIGIT_STRING
    	{ action.kind_selector(false, $T_DIGIT_STRING); }	// hasExpression = false
    ;

// R405
signed_int_literal_constant
@init{Token sign = null;} // @init{INIT_TOKEN_NULL(sign);}
	:	(T_PLUS {sign=$T_PLUS;} | T_MINUS {sign=$T_MINUS;})?
		int_literal_constant
			{ action.signed_int_literal_constant(sign); }
	;

// R406
int_literal_constant
@init{Token kind = null;} // @init{INIT_TOKEN_NULL(kind);}
	:	T_DIGIT_STRING (T_UNDERSCORE kind_param {kind = $kind_param.tk;})?
			{action.int_literal_constant($T_DIGIT_STRING, kind);}
	;

// R407
// T_IDENT inlined for scalar_int_constant_name
kind_param returns [Token tk]
	:	T_DIGIT_STRING {tk = $T_DIGIT_STRING;}
	|	T_IDENT        {tk = $T_IDENT;}
	;

// R408 signed_digit_string inlined

// R409 digit_string converted to fragment

// R410 sign inlined

// R411
boz_literal_constant
	:	BINARY_CONSTANT
	|	OCTAL_CONSTANT
	|	HEX_CONSTANT
	;

// R412 binary-constant converted to terminal

// R413 octal_constant converted to terminal

// R414 hex_constant converted to terminal

// R415 hex_digit inlined

// R416
signed_real_literal_constant
@init{Token sign = null;} // @init{INIT_TOKEN_NULL(sign);}
	:	(T_PLUS {sign=$T_PLUS;} | T_MINUS {sign=$T_PLUS;})?
		real_literal_constant
			{action.signed_real_literal_constant(sign);}
	;

// R417 modified to use terminal
// Grammar Modified slightly to prevent problems with input such as: if(1.and.1) then ... 
real_literal_constant
@init{Token kind = null;} // @init{INIT_TOKEN_NULL(kind);}
    :   T_DIGIT_STRING T_PERIOD_EXPONENT (T_UNDERSCORE kind_param {kind = $kind_param.tk;})?
    		{ action.real_literal_constant($T_DIGIT_STRING, $T_PERIOD_EXPONENT, kind); }
    |   T_DIGIT_STRING T_PERIOD (T_UNDERSCORE kind_param {kind = $kind_param.tk;})?
    		{ action.real_literal_constant($T_DIGIT_STRING, $T_PERIOD, kind); }
    |   T_PERIOD_EXPONENT (T_UNDERSCORE kind_param {kind = $kind_param.tk;})?
    		{ action.real_literal_constant(null, $T_PERIOD_EXPONENT, kind); }
//		WARNING must parse exponent E or D in action (look for D)
    ;

// R418 significand converted to fragment

// R419 exponent_letter inlined in new Exponent

// R420 exponent inlined in new Exponent

// R421
complex_literal_constant
	:	T_LPAREN real_part T_COMMA imag_part T_RPAREN
	;

// R422
// ERR_CHK 422 named_constant replaced by T_IDENT
real_part
	:	signed_int_literal_constant		{action.real_part(true,false,null);}
	|	signed_real_literal_constant	{action.real_part(false,true,null);}
	|	T_IDENT							{action.real_part(false,false,$T_IDENT);}
	;

// R423
// ERR_CHK 423 named_constant replaced by T_IDENT
imag_part
	:	signed_int_literal_constant		{action.imag_part(true,false,null);}
	|	signed_real_literal_constant	{action.imag_part(false,true,null);}
	|	T_IDENT							{action.imag_part(false,false,$T_IDENT);}
	;

// R424
// ERR_CHK 424a scalar_int_initialization_expr replaced by expr
// ERR_CHK 424b T_KIND, if type_param_value, must be a scalar_int_initialization_expr
// ERR_CHK 424c T_KIND and T_LEN cannot both be specified
char_selector
@init{IFortranParserAction.KindLenParam kindOrLen1; kindOrLen1 = IFortranParserAction.KindLenParam.none;
      IFortranParserAction.KindLenParam kindOrLen2; kindOrLen2 = IFortranParserAction.KindLenParam.none;
      boolean hasAsterisk = false;
     }
	:	T_ASTERISK char_length (T_COMMA)?
			{ hasAsterisk=true; action.char_selector(kindOrLen1, kindOrLen2, hasAsterisk); }
	|	T_LPAREN (T_KIND {kindOrLen1=IFortranParserAction.KindLenParam.kind;} | T_LEN {kindOrLen1=IFortranParserAction.KindLenParam.len;})
		  T_EQUALS type_param_value {action.char_selector(kindOrLen1, kindOrLen2, hasAsterisk);}
		  ( T_COMMA (T_KIND {kindOrLen2=IFortranParserAction.KindLenParam.kind;} | T_LEN {kindOrLen2=IFortranParserAction.KindLenParam.len;})
          T_EQUALS type_param_value )?
		T_RPAREN
			{ action.char_selector(kindOrLen1, kindOrLen2, hasAsterisk); }
	|	T_LPAREN type_param_value (T_COMMA (T_KIND T_EQUALS)? expr
			{kindOrLen2=IFortranParserAction.KindLenParam.kind; action.type_param_value(true,false,false);} )?
        T_RPAREN
			{ action.char_selector(IFortranParserAction.KindLenParam.len, kindOrLen2, hasAsterisk); }
	;

// R425
length_selector
	:	T_LPAREN ( T_LEN T_EQUALS )? type_param_value T_RPAREN
			{ action.length_selector(IFortranParserAction.KindLenParam.len, false); }
	|	T_ASTERISK char_length (T_COMMA)?
			{ action.length_selector(IFortranParserAction.KindLenParam.none, true); }
    ; 

// R426
char_length
	:	T_LPAREN type_param_value T_RPAREN
			{ action.char_length(true); }
	|	scalar_int_literal_constant
			{ action.char_length(false); }
	;

scalar_int_literal_constant
	:	int_literal_constant
	;

// R427
// char_literal_constant
// // options {k=2;}
// 	:	T_DIGIT_STRING T_UNDERSCORE T_CHAR_CONSTANT
//         // removed the T_UNDERSCORE because underscores are valid characters 
//         // for identifiers, which means the lexer would match the T_IDENT and 
//         // T_UNDERSCORE as one token (T_IDENT).
// 	|	T_IDENT T_CHAR_CONSTANT
// 	|	T_CHAR_CONSTANT
//     ;
char_literal_constant
	:	T_DIGIT_STRING T_UNDERSCORE T_CHAR_CONSTANT
			{ action.char_literal_constant($T_DIGIT_STRING, null, $T_CHAR_CONSTANT); }
        // removed the T_UNDERSCORE because underscores are valid characters 
        // for identifiers, which means the lexer would match the T_IDENT and 
        // T_UNDERSCORE as one token (T_IDENT).
	|	T_IDENT T_CHAR_CONSTANT
			{ action.char_literal_constant(null, $T_IDENT, $T_CHAR_CONSTANT); }
	|	T_CHAR_CONSTANT
			{ action.char_literal_constant(null, null, $T_CHAR_CONSTANT); }
    ;

// R428
logical_literal_constant
@init{Token kind = null;} // @init{INIT_TOKEN_NULL(kind);}
	:	T_TRUE ( T_UNDERSCORE kind_param {kind = $kind_param.tk;})?
			{action.logical_literal_constant(true, kind);}
	|	T_FALSE ( T_UNDERSCORE kind_param {kind = $kind_param.tk;})?
			{action.logical_literal_constant(false, kind);}
	;

// R429
//	( component_part )? inlined as ( component_def_stmt )*
derived_type_def
	:	derived_type_stmt
		type_param_or_comp_def_stmt_list  // matches T_INTEGER possibilities in component_def_stmt
		( private_or_sequence )*
	  { /* ERR_CHK 429
	     * if private_or_sequence present, component_def_stmt in type_param_or_comp_def_stmt_list
	     * is an error
	     */
	  }
		( component_def_stmt )*
		( type_bound_procedure_part )?
		end_type_stmt
	;

// Includes:
//    ( type_param_def_stmt)*
//    ( component_def_stmt )* if starts with T_INTEGER (could be a parse error)
// REMOVED T_INTEGER junk (see statement above) with k=1
// TODO this must be tested can we get rid of this????
type_param_or_comp_def_stmt_list
//options {k=1;}
//	:	(T_INTEGER) => (kind_selector)? T_COMMA type_param_or_comp_def_stmt
//			type_param_or_comp_def_stmt_list
	:	(kind_selector)? T_COMMA type_param_or_comp_def_stmt
			type_param_or_comp_def_stmt_list
	|
		{ /* ERR_CHK R435
		   * type_param_def_stmt(s) must precede component_def_stmt(s)
		   */
		}
	;

type_param_or_comp_def_stmt
	:	type_param_attr_spec T_COLON_COLON type_param_decl_list T_EOS // see R435
	|	component_attr_spec_list T_COLON_COLON component_decl_list T_EOS // see R440
	;

// R430
// generic_name_list substituted for type_param_name_list
derived_type_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_TYPE ( ( T_COMMA type_attr_spec_list )? T_COLON_COLON )? T_IDENT
		( T_LPAREN generic_name_list T_RPAREN )? T_EOS
			{action.derived_type_stmt(lbl,$T_IDENT);}
	;

type_attr_spec_list
@init{int count = 0;}
	:		{action.type_attr_spec_list__begin();}
		type_attr_spec {count++;} ( T_COMMA type_attr_spec {count++;} )*
			{action.type_attr_spec_list(count);}
	;

generic_name_list
@init{int count = 0;}
	:		{action.generic_name_list__begin();}
		ident=T_IDENT
			{
				count++;
				action.generic_name_list_part(ident);
			} ( T_COMMA ident=T_IDENT 
			{
				count++;
				action.generic_name_list_part(ident);
			} )*
			{action.generic_name_list(count);}
	;

// R431
// T_IDENT inlined for parent_type_name
type_attr_spec
	:	access_spec
	|	T_EXTENDS T_LPAREN T_IDENT T_RPAREN
	|	T_ABSTRACT
	|	T_BIND T_LPAREN T_IDENT /* 'C' */ T_RPAREN
	;

// R432
private_or_sequence
    :   private_components_stmt
    |   sequence_stmt
    ;

// R433
end_type_stmt
@init{Token lbl = null;Token id=null;} 
	: (label {lbl=$label.tk;})? T_END T_TYPE ( T_IDENT {id=$T_IDENT;})? T_EOS
		{action.end_type_stmt(lbl,id);}
	| (label {lbl=$label.tk;})? T_ENDTYPE ( T_IDENT {id=$T_IDENT;})? T_EOS
		{action.end_type_stmt(lbl,id);}
	;

// R434
sequence_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_SEQUENCE T_EOS
			{action.sequence_stmt(lbl);}
	;

// R435 type_param_def_stmt inlined in type_param_or_comp_def_stmt_list

// R436
// ERR_CHK 436 scalar_int_initialization_expr replaced by expr
// T_IDENT inlined for type_param_name
type_param_decl
    :    T_IDENT ( T_EQUALS expr )?
    ;

type_param_decl_list
@init{int count=0;}
	:		{action.type_param_decl_list__begin();}
        type_param_decl {count++;} ( T_COMMA type_param_decl {count++;} )*
			{action.type_param_decl_list(count);}
    ;

// R437
type_param_attr_spec
	:	T_IDENT /* { KIND | LEN } */
	;

// R438 component_part inlined as ( component_def_stmt )* in R429

// R439
component_def_stmt
	:	data_component_def_stmt
	|	proc_component_def_stmt
	;


// R440
data_component_def_stmt
@init{Token lbl = null; boolean hasSpec=false; }
    :    (label {lbl=$label.tk;})? declaration_type_spec ( ( T_COMMA component_attr_spec_list {hasSpec=true;})? T_COLON_COLON )? component_decl_list T_EOS
			{action.data_component_def_stmt(lbl,hasSpec);}
    ;

// R441, R442-F2008
// TODO putback F2008
// TODO it appears there is a bug in the standard for a parameterized type,
//      it needs to accept KIND, LEN keywords, see NOTE 4.24 and 4.25
component_attr_spec
	:	T_POINTER
	|	T_DIMENSION T_LPAREN component_array_spec T_RPAREN
	|	T_DIMENSION /* (T_LPAREN component_array_spec T_RPAREN)? */ T_LBRACKET co_array_spec T_RBRACKET
	|	T_ALLOCATABLE
	|	access_spec
        // are T_KIND and T_LEN correct?
    |   T_KIND
    |   T_LEN
	;

component_attr_spec_list
@init{int count=0;}
    :		{action.component_attr_spec_list__begin();}
        component_attr_spec {count++;} ( T_COMMA component_attr_spec {count++;} )*
    		{action.component_attr_spec_list(count);}
    ;

// R442, R443-F2008
// T_IDENT inlined as component_name
component_decl
    :   T_IDENT ( T_LPAREN component_array_spec T_RPAREN )?
        ( T_LBRACKET co_array_spec T_RBRACKET )?
        ( T_ASTERISK char_length )? ( component_initialization )?
    ;

component_decl_list
@init{int count=0;}
	:		{action.component_decl_list__begin();}
       component_decl {count++;} ( T_COMMA component_decl {count++;} )*
			{action.component_decl_list(count);}
    ;

// R443
component_array_spec
	:	explicit_shape_spec_list
	|	deferred_shape_spec_list
	;

// deferred_shape_spec replaced by T_COLON
deferred_shape_spec_list
@init{int count=0;}
    :    	{action.deferred_shape_spec_list__begin();}
        T_COLON {count++;} ( T_COMMA T_COLON {count++;} )*
        	{action.deferred_shape_spec_list(count);}
    ;

// R444
// R447-F2008 can also be => initial_data_target, see NOTE 4.40 in J3/07-007
// ERR_CHK 444 initialization_expr replaced by expr
component_initialization
	:	T_EQUALS expr
	|	T_EQ_GT null_init
	;

// R445
proc_component_def_stmt
@init{Token lbl = null; boolean hasInterface=false;}
	:	(label {lbl=$label.tk;})? T_PROCEDURE T_LPAREN ( proc_interface {hasInterface=true;})? T_RPAREN T_COMMA
		    proc_component_attr_spec_list T_COLON_COLON proc_decl_list T_EOS
				{action.proc_component_def_stmt(lbl,hasInterface);}
	;

// R446
// T_IDENT inlined for arg_name
proc_component_attr_spec
    :    T_POINTER
    |    T_PASS ( T_LPAREN T_IDENT T_RPAREN )?
    |    T_NOPASS
    |    access_spec
    ;

proc_component_attr_spec_list
@init{int count=0;}
    :    	{action.proc_component_attr_spec_list__begin();}
        proc_component_attr_spec {count++;}( T_COMMA proc_component_attr_spec {count++;})*
        	{action.proc_component_attr_spec_list(count);}
    ;

// R447
private_components_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_PRIVATE T_EOS
			{action.private_components_stmt(lbl);}
	;

// R448
// T_CONTAINS inlined for contains_stmt
type_bound_procedure_part
	:	T_CONTAINS  T_EOS
        ( binding_private_stmt )? proc_binding_stmt ( proc_binding_stmt )*
	;

// R449
binding_private_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_PRIVATE T_EOS
			{action.binding_private_stmt(lbl);}
	;

// R450
proc_binding_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? specific_binding T_EOS
			{action.proc_binding_stmt(lbl);}
	|	(label {lbl=$label.tk;})? generic_binding T_EOS
			{action.proc_binding_stmt(lbl);}
	|	(label {lbl=$label.tk;})? final_binding T_EOS
			{action.proc_binding_stmt(lbl);}
	;

// R451
// T_IDENT inlined for interface_name, binding_name and procedure_name
specific_binding
    : T_PROCEDURE ( T_LPAREN T_IDENT T_RPAREN )?
      ( ( T_COMMA binding_attr_list )? T_COLON_COLON )?
      T_IDENT ( T_EQ_GT T_IDENT )?
    ;

// R452
// generic_name_list substituted for binding_name_list
generic_binding
    :    T_GENERIC ( T_COMMA access_spec )? T_COLON_COLON generic_spec T_EQ_GT generic_name_list
    ;

// R453
// T_IDENT inlined for arg_name
binding_attr
    : T_PASS ( T_LPAREN T_IDENT T_RPAREN )?
						{ action.binding_attr(IFortranParserAction.AttrSpec.PASS); }
    | T_NOPASS			{ action.binding_attr(IFortranParserAction.AttrSpec.NOPASS); }
    | T_NON_OVERRIDABLE	{ action.binding_attr(IFortranParserAction.AttrSpec.NON_OVERRIDABLE); }
    | T_DEFERRED		{ action.binding_attr(IFortranParserAction.AttrSpec.DEFERRED); }
    | access_spec		{ action.binding_attr(IFortranParserAction.AttrSpec.none); }
    ;

binding_attr_list
@init{int count=0;}
    :		{action.binding_attr_list__begin();}
        binding_attr {count++;} ( T_COMMA binding_attr {count++;} )*
    		{action.binding_attr_list(count);}
    ;

// R454
// generic_name_list substituted for final_subroutine_name_list
final_binding
	:	T_FINAL ( T_COLON_COLON )? generic_name_list
	;

// R455
derived_type_spec
@init{boolean hasList = false;}
    : T_IDENT ( T_LPAREN type_param_spec_list {hasList=true;} T_RPAREN )?
    	{ action.derived_type_spec($T_IDENT, hasList); }
    ;

// R456
type_param_spec
    : ( keyword T_EQUALS )? type_param_value
    ;

type_param_spec_list
@init{int count=0;}
    :    	{action.type_param_spec_list__begin();} 
        type_param_spec {count++;}( T_COMMA type_param_spec {count++;})*
        	{action.type_param_spec_list(count);} 
    ;

// R457
// inlined derived_type_spec (R662) to remove ambiguity using backtracking
// ERR_CHK R457 
// If any of the type-param-specs in the list are an '*' or ':', the 
// component-spec-list is required.
// the second alternative to the original rule for structure_constructor is 
// a subset of the first alternative because component_spec_list is a 
// subset of type_param_spec_list.  by combining these two alternatives we can
// remove the backtracking on this rule.
structure_constructor
// options {backtrack=true;}
//     : T_IDENT T_LPAREN type_param_spec_list T_RPAREN
// 		T_LPAREN
// 		( component_spec_list )?
// 		T_RPAREN
//     | T_IDENT
// 		T_LPAREN
// 		( component_spec_list )?
// 		T_RPAREN
    : T_IDENT T_LPAREN type_param_spec_list T_RPAREN
		(T_LPAREN
		( component_spec_list )?
		T_RPAREN)?
	;

// R458
component_spec
    :    ( keyword T_EQUALS )? component_data_source
    ;

component_spec_list
@init{int count=0;}
    :    	{action.component_spec_list__begin();} 
        component_spec {count++;}( T_COMMA component_spec {count++;})*
        	{action.component_spec_list(count);} 
    ;

// R459
// is (expr | data-target | proc-target)
// data_target isa expr so data_target deleted
// proc_target isa expr so proc_target deleted
component_data_source
	:	expr
	;

// R460
enum_def
	:	enum_def_stmt
		enumerator_def_stmt
		( enumerator_def_stmt )*
		end_enum_stmt
	;

// R461
enum_def_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_ENUM T_COMMA T_BIND T_LPAREN T_IDENT /* 'C' */ T_RPAREN T_EOS
			{action.enum_def_stmt(lbl);}
	;

// R462
enumerator_def_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_ENUMERATOR ( T_COLON_COLON )? enumerator_list T_EOS
			{action.enumerator_def_stmt(lbl);}
	;

// R463
// ERR_CHK 463 scalar_int_initialization_expr replaced by expr
// ERR_CHK 463 named_constant replaced by T_IDENT
enumerator
    :    T_IDENT ( T_EQUALS expr )?
    ;

enumerator_list
@init{int count=0;}
    :    	{action.enumerator_list__begin();} 
        enumerator {count++;}( T_COMMA enumerator {count++;})*
        	{action.enumerator_list(count);} 
    ;

// R464
end_enum_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_END T_ENUM T_EOS {action.end_enum_stmt(lbl);}
	|	(label {lbl=$label.tk;})? T_ENDENUM T_EOS {action.end_enum_stmt(lbl);}
	;

// R465
array_constructor
	:	T_LPAREN T_SLASH ac_spec T_SLASH T_RPAREN
			{ action.array_constructor(); }
	|	T_LBRACKET ac_spec T_RBRACKET
			{ action.array_constructor(); }
	;

// R466
// refactored to remove optional from lhs
ac_spec
options {backtrack=true;}
    : type_spec T_COLON_COLON (ac_value_list)?
    | ac_value_list
    ;

// R467 left_square_bracket inlined as T_LBRACKET

// R468 right_square_bracket inlined as T_RBRACKET

// R469
ac_value
options {backtrack=true;}
	:	expr
	|	ac_implied_do
	;

ac_value_list
@init{int count=0;}
    :    	{action.ac_value_list__begin();} 
        ac_value {count++;}( T_COMMA ac_value {count++;})*
        	{action.ac_value_list(count);} 
    ;

// R470
ac_implied_do
	:	T_LPAREN ac_value_list T_COMMA ac_implied_do_control T_RPAREN
	;

// R471
// ERR_CHK 471a scalar_int_expr replaced by expr
// ERR_CHK 471b ac_do_variable replaced by scalar_int_variable replaced by variable replaced by T_IDENT
ac_implied_do_control
    :    T_IDENT T_EQUALS expr T_COMMA expr ( T_COMMA expr )?
    ;

// R472 inlined ac_do_variable as scalar_int_variable (and finally T_IDENT) in R471
// C493 (R472) ac-do-variable shall be a named variable

scalar_int_variable
    :    variable
    ;


/*
Section 5:
 */

// R501
type_declaration_stmt
@init{Token lbl = null;
      int numAttrSpecs = 0;
     } // @init{INIT_TOKEN_NULL(lbl);}
    :		{ action.type_declaration_stmt__begin(); }
        (label {lbl=$label.tk;})? declaration_type_spec
    	 ( (T_COMMA attr_spec {numAttrSpecs += 1;})* T_COLON_COLON )?
    	 entity_decl_list T_EOS
    		{ action.type_declaration_stmt(lbl, numAttrSpecs); }
    ;

// R502
declaration_type_spec
	:	intrinsic_type_spec
	|	T_TYPE T_LPAREN	derived_type_spec T_RPAREN
			{ action.declaration_type_spec(IFortranParserAction.DeclarationTypeSpec.TYPE); }
	|	T_CLASS	T_LPAREN derived_type_spec T_RPAREN
			{ action.declaration_type_spec(IFortranParserAction.DeclarationTypeSpec.CLASS); }
	|	T_CLASS T_LPAREN T_ASTERISK T_RPAREN
			{ action.declaration_type_spec(IFortranParserAction.DeclarationTypeSpec.unlimited); }
	;

// R503
attr_spec
	:	access_spec		{ action.attr_spec(IFortranParserAction.AttrSpec.access); }
	|	T_ALLOCATABLE	{ action.attr_spec(IFortranParserAction.AttrSpec.ALLOCATABLE); }
	|	T_ASYNCHRONOUS	{ action.attr_spec(IFortranParserAction.AttrSpec.ASYNCHRONOUS); }
	|	T_DIMENSION T_LPAREN array_spec T_RPAREN
						{ action.attr_spec(IFortranParserAction.AttrSpec.DIMENSION ); }
	|	T_EXTERNAL		{ action.attr_spec(IFortranParserAction.AttrSpec.EXTERNAL); }
	|	T_INTENT T_LPAREN intent_spec T_RPAREN
						{ action.attr_spec(IFortranParserAction.AttrSpec.INTENT); }
	|	T_INTRINSIC		{ action.attr_spec(IFortranParserAction.AttrSpec.INTRINSIC); }
	|	language_binding_spec		
						{ action.attr_spec(IFortranParserAction.AttrSpec.language_binding); }
	|	T_OPTIONAL		{ action.attr_spec(IFortranParserAction.AttrSpec.OPTIONAL); }
	|	T_PARAMETER		{ action.attr_spec(IFortranParserAction.AttrSpec.PARAMETER); }
	|	T_POINTER		{ action.attr_spec(IFortranParserAction.AttrSpec.POINTER); }
	|	T_PROTECTED		{ action.attr_spec(IFortranParserAction.AttrSpec.PROTECTED); }
	|	T_SAVE			{ action.attr_spec(IFortranParserAction.AttrSpec.SAVE); }
	|	T_TARGET		{ action.attr_spec(IFortranParserAction.AttrSpec.TARGET); }
	|	T_VALUE			{ action.attr_spec(IFortranParserAction.AttrSpec.VALUE); }
	|	T_VOLATILE		{ action.attr_spec(IFortranParserAction.AttrSpec.VOLATILE); }
// TODO are T_KIND and T_LEN correct?
    |   T_KIND
    |   T_LEN
	;


// R504, R503-F2008
// T_IDENT inlined for object_name and function_name
// T_IDENT ( T_ASTERISK char_length )? (second alt) subsumed in first alt
// TODO Pass more info to action....
entity_decl
    : T_IDENT ( T_LPAREN array_spec T_RPAREN )?
              ( T_LBRACKET co_array_spec T_RBRACKET )?
              ( T_ASTERISK char_length )? ( initialization )?
		{action.entity_decl($T_IDENT);}
    ;

entity_decl_list
@init{int count = 0;}
    :		{action.entity_decl_list__begin();}
    	entity_decl {count += 1;} ( T_COMMA entity_decl {count += 1;} )*
    		{action.entity_decl_list(count);}
    ;

// R505 object_name inlined as T_IDENT

// R506
// ERR_CHK 506 initialization_expr replaced by expr
initialization
	:	T_EQUALS expr		{ action.initialization(true, false); }
	|	T_EQ_GT null_init	{ action.initialization(false, true); }
	;

// R507
// C506 The function-reference shall be a reference to the NULL intrinsic function with no arguments.
null_init
	:	T_IDENT /* 'NULL' */ T_LPAREN T_RPAREN
			{ action.null_init($T_IDENT); }
	;

// R508
access_spec
	:	T_PUBLIC
	|	T_PRIVATE
	;

// R509
// ERR_CHK 509 scalar_char_initialization_expr replaced by expr
language_binding_spec
@init{boolean hasName = false;}
    :	T_BIND T_LPAREN T_IDENT /* 'C' */ (T_COMMA name T_EQUALS expr {hasName=true;})? T_RPAREN
    		{ action.language_binding_spec($T_IDENT, hasName); }
    ;

// R510
array_spec
@init{int count=0;}
	:		{action.array_spec__begin();}
	 	array_spec_element {count++;}
		(T_COMMA array_spec_element {count++;})*
			{action.array_spec(count);}
	;

// Array specifications can consist of these beasts. Note that we can't 
// mix/match arbitrarily, so we have to check validity in actions.
// Types: 	0 expr (e.g. 3 or m+1)
// 			1 expr: (e.g. 3:)
// 			2 expr:expr (e.g. 3:5 or 7:(m+1))
// 			3 expr:* (e.g. 3:* end of assumed size)
// 			4 *  (end of assumed size)
// 			5 :	 (could be part of assumed or deferred shape)
array_spec_element
@init{IFortranParserAction.ArraySpecElement type = null;}
	:   expr {type=IFortranParserAction.ArraySpecElement.expr;}
          (T_COLON {type=IFortranParserAction.ArraySpecElement.expr_colon;}
        	(  expr {type=IFortranParserAction.ArraySpecElement.expr_colon_expr;}
        	 | T_ASTERISK {type=IFortranParserAction.ArraySpecElement.expr_colon_asterisk;}
        	)?
          )?
			{ action.array_spec_element(type); }
	|   T_ASTERISK
			{ action.array_spec_element(IFortranParserAction.ArraySpecElement.asterisk); }
	|	T_COLON
			{ action.array_spec_element(IFortranParserAction.ArraySpecElement.colon); }
	;

// R511
// refactored to remove conditional from lhs and inlined lower_bound and upper_bound
explicit_shape_spec
@init{boolean hasUpperBound=false;}
    : 	expr (T_COLON expr {hasUpperBound=true;})?
			{action.explicit_shape_spec(hasUpperBound);}
	;

explicit_shape_spec_list
@init{ int count=0;}
	:		{action.explicit_shape_spec_list__begin();}
     	explicit_shape_spec {count++;}( T_COMMA explicit_shape_spec {count++;})*
			{action.explicit_shape_spec_list(count);}
    ;

/*
 * F2008 co-array grammar addition
 */

// R511-F2008
co_array_spec
	:	deferred_co_shape_spec_list
	|	explicit_co_shape_spec
	;

// R519-F2008
deferred_co_shape_spec
	:	T_COLON
	;

deferred_co_shape_spec_list
@init{int count=0;}
	:		{action.deferred_co_shape_spec_list__begin();}
		T_COLON {count++;}( T_COMMA T_COLON {count++;})?
			{action.deferred_co_shape_spec_list(count);}
	;

// R520-F2008
// TODO putback F2008
explicit_co_shape_spec
	:	T_XYZ expr explicit_co_shape_spec_suffix
	|	T_ASTERISK
	;

explicit_co_shape_spec_suffix
	:	T_COLON T_ASTERISK
	|	T_COMMA explicit_co_shape_spec
	|	T_COLON expr explicit_co_shape_spec
	;

// R512 lower_bound was specification_expr inlined as expr

// R513 upper_bound was specification_expr inlined as expr

// R514 assumed_shape_spec was ( lower_bound )? T_COLON not used in R510 array_spec

// R515 deferred_shape_spec inlined as T_COLON in deferred_shape_spec_list

// R516 assumed_size_spec absorbed into array_spec.

// R517
intent_spec
	:	T_IN		{ action.intent_spec(IFortranParserAction.IntentSpec.IN); }
	|	T_OUT		{ action.intent_spec(IFortranParserAction.IntentSpec.OUT); }
	|	T_IN T_OUT	{ action.intent_spec(IFortranParserAction.IntentSpec.INOUT); }
	|	T_INOUT		{ action.intent_spec(IFortranParserAction.IntentSpec.INOUT); }
	;

// R518
access_stmt
@init{Token lbl = null;boolean hasList=false;} // @init{INIT_TOKEN_NULL(lbl);}
    :    (label {lbl=$label.tk;})? access_spec ( ( T_COLON_COLON )? access_id_list {hasList=true;})? T_EOS
			{action.access_stmt(lbl,hasList);}
    ;

// R519
// T_IDENT inlined for use_name
// generic_spec can be T_IDENT so T_IDENT deleted
// TODO - can this only be T_IDENTS?  generic_spec is more than that..
access_id
	:	generic_spec
	;

access_id_list
@init{ int count=0;}
    :  		{action.access_id_list__begin();}
		access_id {count++;} ( T_COMMA access_id {count++;} )*
      		{action.access_id_list(count);}
    ;

// R520, R526-F2008
// T_IDENT inlined for object_name
allocatable_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    : (label {lbl=$label.tk;})? T_ALLOCATABLE ( T_COLON_COLON )? allocatable_decl ( T_COMMA allocatable_decl )* T_EOS
    ;

// R527-F2008
// T_IDENT inlined for object_name
allocatable_decl
    : T_IDENT ( T_LPAREN array_spec T_RPAREN )?
              ( T_LBRACKET co_array_spec T_RBRACKET )?
    ;

// R521
// generic_name_list substituted for object_name_list
asynchronous_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_ASYNCHRONOUS ( T_COLON_COLON )?
		generic_name_list T_EOS
			{action.asynchronous_stmt(lbl);}
	;

// R522
bind_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? language_binding_spec
		( T_COLON_COLON )? bind_entity_list T_EOS
			{ action.bind_stmt(lbl); }
	;

// R523
// T_IDENT inlined for entity_name and common_block_name
bind_entity
	:	T_IDENT {action.bind_entity($T_IDENT, false);}	// isCommonBlockName=false
	|	T_SLASH T_IDENT T_SLASH {action.bind_entity($T_IDENT, true);}	// isCommonBlockname=true
	;

bind_entity_list
@init{ int count=0;}
    :  		{action.bind_entity_list__begin();}
		bind_entity {count++;} ( T_COMMA bind_entity {count++;} )*
      		{action.bind_entity_list(count);}
    ;

// R524
data_stmt
@init{Token lbl = null; int count=1;}
	:		{action.data_stmt__begin();}
        (label {lbl=$label.tk;})? T_DATA data_stmt_set ( ( T_COMMA )? data_stmt_set {count++;})* T_EOS
			{action.data_stmt(lbl,count);}
    ;

// R525
data_stmt_set
	:	data_stmt_object_list
		T_SLASH
		data_stmt_value_list
		T_SLASH
	;

// R526
data_stmt_object
	:	variable
	|	data_implied_do
	;

data_stmt_object_list
@init{ int count=0;}
    :  		{action.data_stmt_object_list__begin();}
		data_stmt_object {count++;} ( T_COMMA data_stmt_object {count++;} )*
      		{action.data_stmt_object_list(count);}
    ;


// R527
// ERR_CHK 527 scalar_int_expr replaced by expr
// data_i_do_variable replaced by T_IDENT
data_implied_do
    : T_LPAREN data_i_do_object_list T_COMMA T_IDENT T_EQUALS
      expr T_COMMA expr ( T_COMMA expr )? T_RPAREN
    ;

// R528
// data_ref inlined for scalar_structure_component and array_element
data_i_do_object
	:	data_ref
	|	data_implied_do
	;

data_i_do_object_list
@init{ int count=0;}
    :  		{action.data_i_do_object_list__begin();}
		data_i_do_object {count++;} ( T_COMMA data_i_do_object {count++;} )*
      		{action.data_i_do_object_list(count);}
    ;

// R529 data_i_do_variable was scalar_int_variable inlined as T_IDENT
// C556 (R529) The data-i-do-variable shall be a named variable.

// R530
// ERR_CHK R530 designator is scalar-constant or integer constant when followed by '*'
// data_stmt_repeat inlined from R531
// structure_constructure covers null_init if 'NULL()' so null_init deleted
// TODO - check for other cases of signed_real_literal_constant and real_literal_constant problems
data_stmt_value
options {backtrack=true; k=3;}
	:	designator (T_ASTERISK data_stmt_constant)?
	|	int_literal_constant (T_ASTERISK data_stmt_constant)?
		// added to fix bug #185631
	|	(T_DIGIT_STRING (T_PERIOD | T_PERIOD_EXPONENT)) => signed_real_literal_constant
	|	(T_PERIOD) => signed_real_literal_constant // see R532 comment for why k=3 lookahead
	|	signed_real_literal_constant
	|	( T_PLUS | T_MINUS ) int_literal_constant
	|	complex_literal_constant
	|	logical_literal_constant
	|	char_literal_constant
	|	boz_literal_constant
	|	structure_constructor // is null_init if 'NULL()'
    ;

data_stmt_value_list
@init{ int count=0;}
    :  		{action.data_stmt_value_list__begin();}
		data_stmt_value {count++;} ( T_COMMA data_stmt_value {count++;} )*
      		{action.data_stmt_value_list(count);}
    ;

// R531 data_stmt_repeat inlined as (int_literal_constant | designator) in R530
// ERRCHK 531 int_constant shall be a scalar_int_constant
// scalar_int_constant replaced by int_constant replaced by int_literal_constant as T_IDENT covered by designator
// scalar_int_constant_subobject replaced by designator

scalar_int_constant
    :    int_constant
    ;

// R532
// scalar_constant_subobject replaced by designator
// scalar_constant replaced by literal_constant as designator can be T_IDENT
// then literal_constant inlined (except for signed portion)
// structure_constructure covers null_init if 'NULL()' so null_init deleted
// The lookahead in the alternative for signed_real_literal_constant is 
// necessary because ANTLR won't look far enough ahead by itself and when it
// sees a T_DIGIT_STRING, it tries the signed_int_literal_constant.  this isn't
// correct since the new version of the real_literal_constants can start with
// a T_DIGIT_STRING.  
data_stmt_constant
options {backtrack=true; k=3;}
	:	designator
	|	signed_int_literal_constant
	|	( (T_PLUS | T_MINUS)? (T_DIGIT_STRING (T_PERIOD | T_PERIOD_EXPONENT)) 
    |	(T_PERIOD) ) => signed_real_literal_constant
	|	complex_literal_constant
	|	logical_literal_constant
	|	char_literal_constant
	|	boz_literal_constant
	|	structure_constructor // is null_init if 'NULL()'
	;

// R533 int_constant_subobject was constant_subobject inlined as designator in R531

// R534 constant_subobject inlined as designator in R533
// C566 (R534) constant-subobject shall be a subobject of a constant.

// R535, R543-F2008
// array_name replaced by T_IDENT
dimension_stmt
@init{Token lbl=null; int count=1;} // @init{INIT_TOKEN_NULL(lbl);}
	:		{action.dimension_stmt__begin();}
        (label {lbl=$label.tk;})? T_DIMENSION ( T_COLON_COLON )? dimension_decl ( T_COMMA dimension_decl {count++;})* T_EOS
			{action.dimension_stmt(lbl,count);}
    ;

// R544-F2008
// ERR_CHK 509-F2008 at least one of the array specs must exist
dimension_decl
    :    T_IDENT ( T_LPAREN array_spec T_RPAREN )? ( T_LBRACKET co_array_spec T_RBRACKET )?
    ;

// R509-F2008
// ERR_CHK 509-F2008 at least one of the array specs must exist
dimension_spec
    :    T_DIMENSION ( T_LPAREN array_spec T_RPAREN )? ( T_LBRACKET co_array_spec T_RBRACKET )?
    ;

// R536
// generic_name_list substituted for dummy_arg_name_list
intent_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_INTENT T_LPAREN intent_spec T_RPAREN ( T_COLON_COLON )? generic_name_list T_EOS
			{action.intent_stmt(lbl);}
	;

// R537
// generic_name_list substituted for dummy_arg_name_list
optional_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_OPTIONAL ( T_COLON_COLON )? generic_name_list T_EOS
			{action.optional_stmt(lbl);}
		
	;

// R538
parameter_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_PARAMETER T_LPAREN named_constant_def_list T_RPAREN T_EOS
			{action.parameter_stmt(lbl);}
	;

named_constant_def_list
@init{ int count=0;}
    :  		{action.named_constant_def_list__begin();}
		named_constant_def {count++;} ( T_COMMA named_constant_def {count++;} )*
      		{action.named_constant_def_list(count);}
    ;

// R539
// ERR_CHK 539 initialization_expr replaced by expr
// ERR_CHK 539 named_constant replaced by T_IDENT
named_constant_def
	:	T_IDENT T_EQUALS expr
	;

// R540
pointer_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_POINTER ( T_COLON_COLON )? pointer_decl_list T_EOS
			{action.pointer_stmt(lbl);}
	;

pointer_decl_list
@init{ int count=0;}
    :  		{action.pointer_decl_list__begin();}
		pointer_decl {count++;} ( T_COMMA pointer_decl {count++;} )*
      		{action.pointer_decl_list(count);}
    ;

// R541
// T_IDENT inlined as object_name and proc_entity_name (removing second alt)
pointer_decl
    :    T_IDENT ( T_LPAREN deferred_shape_spec_list T_RPAREN )?
    ;

// R542
// generic_name_list substituted for entity_name_list
protected_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_PROTECTED ( T_COLON_COLON )? generic_name_list T_EOS
			{action.protected_stmt(lbl);}
	;

// R543
save_stmt
@init{Token lbl = null;boolean hasSavedEntityList=false;}
    : (label {lbl=$label.tk;})? T_SAVE ( ( T_COLON_COLON )? saved_entity_list {hasSavedEntityList=true;})? T_EOS
		{action.save_stmt(lbl,hasSavedEntityList);}
    ;

// R544
// T_IDENT inlined for object_name, proc_pointer_name (removing second alt), and common_block_name
saved_entity
	:	T_IDENT
	|	T_SLASH T_IDENT T_SLASH
	;

saved_entity_list
@init{ int count=0;}
    :  		{action.saved_entity_list__begin();}
		saved_entity {count++;} ( T_COMMA saved_entity {count++;} )*
      		{action.saved_entity_list(count);}
    ;


// R545 proc_pointer_name was name inlined as T_IDENT

// R546, R555-F2008
// T_IDENT inlined for object_name
target_stmt
@init{Token lbl = null;int count=1;}
	:		{action.target_stmt__begin();}
     (label {lbl=$label.tk;})? T_TARGET ( T_COLON_COLON )? target_decl ( T_COMMA target_decl {count++;} )* T_EOS
		{action.target_stmt(lbl,count);}
    ;

// R556-F2008
target_decl
    : T_IDENT ( T_LPAREN array_spec T_RPAREN )?
              ( T_LBRACKET co_array_spec T_RBRACKET )?
    ;

// R547
// generic_name_list substituted for dummy_arg_name_list
value_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_VALUE ( T_COLON_COLON )? generic_name_list T_EOS
		{action.value_stmt(lbl);}
	;

// R548
// generic_name_list substituted for object_name_list
volatile_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_VOLATILE ( T_COLON_COLON )? generic_name_list T_EOS
		{action.volatile_stmt(lbl);}
	;

// R549
implicit_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_IMPLICIT implicit_spec_list T_EOS
			{action.implicit_stmt(lbl);}
	|	(label {lbl=$label.tk;})? T_IMPLICIT T_NONE T_EOS
			{action.implicit_stmt(lbl);}
	;

// R550
implicit_spec
	:	declaration_type_spec T_LPAREN letter_spec_list T_RPAREN
	;

implicit_spec_list
@init{ int count=0;}
    :  		{action.implicit_spec_list__begin();}
		implicit_spec {count++;} ( T_COMMA implicit_spec {count++;} )*
      		{action.implicit_spec_list(count);}
    ;


// R551
// TODO: here, we'll accept a T_IDENT, and then we'll have to do error 
// checking on it.  
letter_spec 
    : T_IDENT ( T_MINUS T_IDENT )?
    ;

letter_spec_list
@init{ int count=0;}
    :  		{action.letter_spec_list__begin();}
		letter_spec {count++;} ( T_COMMA letter_spec {count++;} )*
      		{action.letter_spec_list(count);}
    ;

// R552
// T_IDENT inlined for namelist_group_name
namelist_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    : (label {lbl=$label.tk;})? T_NAMELIST T_SLASH T_IDENT T_SLASH namelist_group_object_list
         ( ( T_COMMA )? T_SLASH T_IDENT T_SLASH namelist_group_object_list )* T_EOS
    ;

// R553 namelist_group_object was variable_name inlined as T_IDENT

// T_IDENT inlined for namelist_group_object
namelist_group_object_list
@init{ int count=0;}
    :  		{action.namelist_group_object_list__begin();}
		T_IDENT {count++;} ( T_COMMA T_IDENT {count++;} )*
      		{action.namelist_group_object_list(count);}
    ;


// R554
equivalence_stmt
@init{Token lbl = null;}
	:	(label {lbl=$label.tk;})? T_EQUIVALENCE equivalence_set_list T_EOS
			{action.equivalence_stmt(lbl);}
	;

// R555
equivalence_set
	:	T_LPAREN equivalence_object T_COMMA equivalence_object_list T_RPAREN
	;


equivalence_set_list
@init{ int count=0;}
    :  		{action.equivalence_set_list__begin();}
		equivalence_set {count++;} ( T_COMMA equivalence_set {count++;} )*
      		{action.equivalence_set_list(count);}
    ;

// R556
// T_IDENT inlined for variable_name
// data_ref inlined for array_element
// data_ref isa T_IDENT so T_IDENT deleted (removing first alt)
// substring isa data_ref so data_ref deleted (removing second alt)
equivalence_object
	:	substring
	;

equivalence_object_list
@init{ int count=0;}
    :  		{action.equivalence_object_list__begin();}
		equivalence_object {count++;} ( T_COMMA equivalence_object {count++;} )*
      		{action.equivalence_object_list(count);}
    ;

// R557
// T_IDENT inlined for common_block_name
common_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    : (label {lbl=$label.tk;})? T_COMMON ( T_SLASH ( T_IDENT )? T_SLASH )? common_block_object_list
         ( ( T_COMMA )? T_SLASH ( T_IDENT )? T_SLASH common_block_object_list )* T_EOS
    ;

// R558
// T_IDENT inlined for variable_name and proc_pointer_name
// T_IDENT covered by first alt so second deleted
common_block_object
    : T_IDENT ( T_LPAREN explicit_shape_spec_list T_RPAREN )?
    ;

common_block_object_list
@init{ int count=0;}
    :  		{action.common_block_object_list__begin();}
		common_block_object {count++;} ( T_COMMA common_block_object {count++;} )*
      		{action.common_block_object_list(count);}
    ;

/*
Section 6:
 */

// R601
variable
	:	designator {action.variable();}
	;

// R602 variable_name was name inlined as T_IDENT

// R603
//  :   object-name             // T_IDENT (data-ref isa T_IDENT)
//	|	array-element           // R616 is data-ref
//	|	array-section           // R617 is data-ref [ (substring-range) ] 
//	|	structure-component     // R614 is data-ref
//	|	substring
// (substring-range) may be matched in data-ref
// this rule is now identical to substring
designator
@init{boolean hasSubstringRange = false;}
	:	data_ref (T_LPAREN substring_range {hasSubstringRange=true;} T_RPAREN)?
			{ action.designator(hasSubstringRange); }
	|	char_literal_constant T_LPAREN substring_range T_RPAREN
			{ hasSubstringRange=true; action.substring(hasSubstringRange); }
	;

//
// a function_reference is ambiguous with designator, ie, foo(b) could be an array element
//
//	function_reference : procedure_designator T_LPAREN ( actual_arg_spec_list )? T_RPAREN
//                       procedure_designator isa data_ref
// C1220 (R1217) The procedure-designator shall designate a function.
// data_ref may (or not) match T_LPAREN ( actual_arg_spec_list )? T_RPAREN, so is optional
designator_or_func_ref
@init{boolean hasSubstringRangeOrArgList = false;
	  boolean hasSubstringRange = false;
	 }
	:	data_ref (T_LPAREN substring_range_or_arg_list
					{
						hasSubstringRangeOrArgList = true;
						hasSubstringRange=$substring_range_or_arg_list.isSubstringRange;
					}
				  T_RPAREN)?
			{
				if (hasSubstringRangeOrArgList) {
					if (hasSubstringRange) {
						action.designator(hasSubstringRange);
					} else {
						action.function_reference(true);	// hasActualArgSpecList=true
					}
				}
			}
	|	char_literal_constant T_LPAREN substring_range T_RPAREN
			{ hasSubstringRange=true; action.substring(hasSubstringRange); }
	;

substring_range_or_arg_list returns [boolean isSubstringRange]
@init{boolean hasUpperBound = false;
	  Token keyword = null;
	  int count = 0;
	 }
	:	T_COLON (expr {hasUpperBound = true;})?         // substring_range
			{
			  action.substring_range(false, hasUpperBound);	// hasLowerBound=false
			  isSubstringRange=true;
			}
	|		{ 
			  action.actual_arg_spec_list__begin();  /* mimic actual-arg-spec-list */
			}
		expr substr_range_or_arg_list_suffix
			{
			  isSubstringRange = $substr_range_or_arg_list_suffix.isSubstringRange;
			}
	|		{
			  action.actual_arg_spec_list__begin(); /* mimic actual-arg-spec-list */
			}
		T_IDENT T_EQUALS expr
			{
			  count++;
			  action.actual_arg(true, null);
			  action.actual_arg_spec($T_IDENT);
			}
		( T_COMMA actual_arg_spec {count++;} )*
			{
			  action.actual_arg_spec_list(count);
			  isSubstringRange = false;
			}
	|		{
			  action.actual_arg_spec_list__begin(); /* mimic actual-arg-spec-list */
			}
		( T_IDENT T_EQUALS {keyword=$T_IDENT;} )? T_ASTERISK label
			{
			  count++;
			  action.actual_arg(false, $label.tk);
			  action.actual_arg_spec(keyword);
			}
		( T_COMMA actual_arg_spec {count++;} )*
			{
			  action.actual_arg_spec_list(count);
			  isSubstringRange = false;
			}
	;

substr_range_or_arg_list_suffix returns [boolean isSubstringRange]
@init{boolean hasUpperBound = false; int count = 0;}
	:		{
			  action.actual_arg_spec_list(-1);  // guessed wrong on list creation, inform of error
			}
		T_COLON (expr {hasUpperBound=true;})?	// substring_range
			{
			  action.substring_range(true, hasUpperBound);	// hasLowerBound=true
			  isSubstringRange = true;
			}
	|
			{
			  count++;
			  action.actual_arg(true, null);	// hasExpr=true, label=null
			  action.actual_arg_spec(null);		// keywork=null
			}
		( T_COMMA actual_arg_spec {count++;} )*
			{
			  action.actual_arg_spec_list(count);
			  isSubstringRange=false;
			}	// actual_arg_spec_list
	;

// R604
logical_variable
	:	variable
	;

// R605
default_logical_variable
	:	variable
	;

scalar_default_logical_variable
	:	variable
	;

// R606
char_variable
	:	variable
	;

// R607
default_char_variable
	:	variable
	;

scalar_default_char_variable
	:	variable
	;

// R608
int_variable
	:	variable
	;

// R609
// C608 (R610) parent_string shall be of type character
// fix for ambiguity in data_ref allows it to match T_LPAREN substring_range T_RPAREN,
// so required T_LPAREN substring_range T_RPAREN made optional
// ERR_CHK 609 ensure final () is (substring-range)
substring
@init{boolean hasSubstringRange = false;}
	:	data_ref (T_LPAREN substring_range {hasSubstringRange=true;} T_RPAREN)?
			{ action.substring(hasSubstringRange); }
	|	char_literal_constant T_LPAREN substring_range T_RPAREN
			{ action.substring(true); }
	;

// R610 parent_string inlined in R609 as (data_ref | char_literal_constant)
// T_IDENT inlined for scalar_variable_name
// data_ref inlined for scalar_structure_component and array_element
// data_ref isa T_IDENT so T_IDENT deleted
// scalar_constant replaced by char_literal_constant as data_ref isa T_IDENT and must be character

// R611
// ERR_CHK 611 scalar_int_expr replaced by expr
substring_range
@init{boolean hasLowerBound = false;
	  boolean hasUpperBound = false;
	 }
	:	(expr {hasLowerBound = true;})? T_COLON	(expr {hasUpperBound = true;})?
			{ action.substring_range(hasLowerBound, hasUpperBound); }
	;

// R612
data_ref
@init{int numPartRefs = 0;}
	:	part_ref {numPartRefs += 1;} ( T_PERCENT part_ref {numPartRefs += 1;})*
			{action.data_ref(numPartRefs);}
	;

// R613, R613-F2008
// T_IDENT inlined for part_name
// with k=2, this path is chosen over T_LPAREN substring_range T_RPAREN
// TODO error: if a function call, should match id rather than (section_subscript_list)
// a = foo(b) is ambiguous YUK...
// TODO putback F2008
part_ref
options {k=2;}
@init{boolean hasSSL = false;
      boolean hasImageSelector = false;
     }
	:	( T_IDENT T_LPAREN) => T_IDENT T_LPAREN section_subscript_list T_RPAREN
		( image_selector {hasImageSelector=true;})?
			{hasSSL=true; action.part_ref($T_IDENT, hasSSL, hasImageSelector);}
//	|	 T_IDENT image_selector
//			{hasImageSelector=true; action.part_ref($T_IDENT, hasSSL, hasImageSelector);}
	|	T_IDENT
			{action.part_ref($T_IDENT, hasSSL, hasImageSelector);}
    ;

// R614 structure_component inlined as data_ref

// R615 type_param_inquiry inlined in R701 then deleted as can be designator
// T_IDENT inlined for type_param_name

// R616 array_element inlined as data_ref

// R617 array_section inlined in R603

// R618 subscript inlined as expr
// ERR_CHK 618 scalar_int_expr replaced by expr

// R619
// expr inlined for subscript, vector_subscript, and stride (thus deleted option 3)
// refactored first optional expr from subscript_triplet
// modified to also match actual_arg_spec_list to reduce ambiguities and need for backtracking
section_subscript returns [boolean isEmpty]
@init{boolean hasLowerBounds = false;
	  boolean hasUpperBounds = false;
	  boolean hasStride = false;
      boolean hasExpr = false;
     }
	:	expr section_subscript_ambiguous
	|	T_COLON (expr {hasUpperBounds=true;})? (T_COLON expr {hasStride=true;})?
			{ action.section_subscript(hasLowerBounds, hasUpperBounds, hasStride, false); }
    |   T_COLON_COLON expr
        	{hasStride=true; action.section_subscript(hasLowerBounds, hasUpperBounds, hasStride, false);}
	|	T_IDENT T_EQUALS expr				// could be an actual-arg, see R1220
			{ hasExpr=true; action.actual_arg(hasExpr, null); action.actual_arg_spec($T_IDENT); }
	|	T_IDENT T_EQUALS T_ASTERISK label	// could be an actual-arg, see R1220
			{ action.actual_arg(hasExpr, $label.tk); action.actual_arg_spec($T_IDENT); }
	|	T_ASTERISK label /* could be an actual-arg, see R1220 */
			{ action.actual_arg(hasExpr, $label.tk); action.actual_arg_spec(null); }
	|		{ isEmpty = true; /* empty could be an actual-arg, see R1220 */ }
	;

section_subscript_ambiguous
@init{boolean hasLowerBound = true;
      boolean hasUpperBound = false;
      boolean hasStride = false;
      boolean isAmbiguous = false;}
	:	T_COLON (expr {hasUpperBound=true;})? (T_COLON expr {hasStride=true;})?
	        {action.section_subscript(hasLowerBound, hasUpperBound, hasStride, isAmbiguous);}
        // this alternative is necessary because if alt1 above has no expr
        // following the first : and there is the optional second : with no 
        // WS between the two, the lexer will make a T_COLON_COLON token 
        // instead of two T_COLON tokens.  in this case, the second expr is
        // required.  for an example, see J3/04-007, Note 7.44.
    |   T_COLON_COLON expr
        	{hasStride=true; action.section_subscript(hasLowerBound, hasUpperBound, hasStride, isAmbiguous);}
	|		{/* empty, could be an actual-arg, see R1220 */
			 isAmbiguous=true; action.section_subscript(hasLowerBound, hasUpperBound, hasStride, isAmbiguous);
			}
	;

section_subscript_list
@init{int count = 0;}
    :		{ action.section_subscript_list__begin(); }
    	isEmpty=section_subscript
    		{
    			if (isEmpty == false) count += 1;
    		}
    	(T_COMMA section_subscript {count += 1;})*
    		{ action.section_subscript_list(count); }
    ;

// R620 subscript_triplet inlined in R619
// inlined expr as subscript and stride in subscript_triplet

// R621 stride inlined as expr
// ERR_CHK 621 scalar_int_expr replaced by expr

// R622
// ERR_CHK 622 int_expr replaced by expr
vector_subscript
	:	expr
	;

// R622 inlined vector_subscript as expr in R619
// ERR_CHK 622 int_expr replaced by expr

// R624-F2008
image_selector
	:	T_LBRACKET expr ( T_COMMA expr )* T_RBRACKET
	;

// R625-F2008 co_subscript was scalar_int_expr inlined as expr in R624-F2004

// R623
// modified to remove backtracking by looking for the token inserted during
// the lexical prepass if a :: was found (which required alt1 below).
allocate_stmt
@init{Token lbl = null;
	  boolean hasTypeSpec = false;
	  boolean hasAllocOptList = false;
	 }
    :	(label {lbl=$label.tk;})? T_ALLOCATE_STMT_1 T_ALLOCATE T_LPAREN
		type_spec T_COLON_COLON
		allocation_list 
		( T_COMMA alloc_opt_list {hasAllocOptList=true;} )? T_RPAREN T_EOS
    		{
    			hasTypeSpec = true;
    			action.allocate_stmt(lbl, hasTypeSpec, hasAllocOptList);
    		}
    |	(label {lbl=$label.tk;})? T_ALLOCATE T_LPAREN
    	allocation_list
    	( T_COMMA alloc_opt_list {hasAllocOptList=true;} )? T_RPAREN T_EOS
    		{
    			action.allocate_stmt(lbl, hasTypeSpec, hasAllocOptList);
    		}
    ;

// R624
// ERR_CHK 624 source_expr replaced by expr
// stat_variable and errmsg_variable replaced by designator
alloc_opt
	:	T_IDENT /* {'STAT','ERRMSG'} are variables {SOURCE'} is expr */ T_EQUALS expr
			{ action.alloc_opt($T_IDENT); }
	;

alloc_opt_list
@init{ int count=0;}
    :  		{action.alloc_opt_list__begin();}
		alloc_opt {count++;} ( T_COMMA alloc_opt {count++;} )*
      		{action.alloc_opt_list(count);}
    ;

// R625 stat_variable was scalar_int_variable inlined in R624 and R636

// R626 errmsg_variable was scalar_default_char_variable inlined in R624 and R636

// R627 inlined source_expr was expr

// R628, R631-F2008
allocation
@init{boolean hasAllocateShapeSpecList = false; boolean hasAllocateCoArraySpec = false;}
    : allocate_object
    	( T_LPAREN allocate_shape_spec_list {hasAllocateShapeSpecList=true;} T_RPAREN )?
		( T_LBRACKET allocate_co_array_spec {hasAllocateCoArraySpec=true;} T_RBRACKET )?
			{ action.allocation(hasAllocateShapeSpecList, hasAllocateCoArraySpec); }
    ;


allocation_list
@init{ int count=0;}
    :  		{action.allocation_list__begin();}
		allocation {count++;} ( T_COMMA allocation {count++;} )*
      		{action.allocation_list(count);}
    ;

// R629
// T_IDENT inlined for variable_name
// data_ref inlined for structure_component
// data_ref isa T_IDENT so T_IDENT deleted
allocate_object
	:	data_ref
	;

allocate_object_list
@init{ int count=0;}
    :  		{action.allocate_object_list__begin();}
		allocate_object {count++;} ( T_COMMA allocate_object {count++;} )*
      		{action.allocate_object_list(count);}
    ;

// R630
// ERR_CHK 630a lower_bound_expr replaced by expr
// ERR_CHK 630b upper_bound_expr replaced by expr
allocate_shape_spec
@init{boolean hasLowerBound = false; boolean hasUpperBound = true;}
	:	expr (T_COLON expr)?
    		{	// note, allocate-shape-spec always has upper bound
    			// grammar was refactored to remove left recursion, looks deceptive
    			action.allocate_shape_spec(hasLowerBound, hasUpperBound);
    		}
    ;

allocate_shape_spec_list
@init{ int count=0;}
    :  		{action.allocate_shape_spec_list__begin();}
		allocate_shape_spec {count++;} ( T_COMMA allocate_shape_spec {count++;} )*
      		{action.allocate_shape_spec_list(count);}
    ;

// R631 inlined lower_bound_expr was scalar_int_expr

// R632 inlined upper_bound_expr was scalar_int_expr

// R633
nullify_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})?
		T_NULLIFY T_LPAREN pointer_object_list T_RPAREN T_EOS
			{ action.nullify_stmt(lbl); }
	;

// R634
// T_IDENT inlined for variable_name and proc_pointer_name
// data_ref inlined for structure_component
// data_ref can be a T_IDENT so T_IDENT deleted
pointer_object
	:	data_ref
	;

pointer_object_list
@init{ int count=0;}
    :  		{action.pointer_object_list__begin();}
		pointer_object {count++;} ( T_COMMA pointer_object {count++;} )*
      		{action.pointer_object_list(count);}
    ;

// R635
deallocate_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    :    (label {lbl=$label.tk;})? T_DEALLOCATE T_LPAREN allocate_object_list ( T_COMMA dealloc_opt_list )? T_RPAREN T_EOS
    ;

// R636
// stat_variable and errmsg_variable replaced by designator
dealloc_opt
	:	T_IDENT /* {'STAT','ERRMSG'} */ T_EQUALS designator
	;

dealloc_opt_list
@init{ int count=0;}
    :  		{action.dealloc_opt_list__begin();}
		dealloc_opt {count++;} ( T_COMMA dealloc_opt {count++;} )*
      		{action.dealloc_opt_list(count);}
    ;

// R636-F2008
// TODO putback F2008
allocate_co_array_spec
    :   /* ( allocate_co_shape_spec_list T_COMMA )? ( expr T_COLON )? */ T_ASTERISK
    ;

// R637-F2008
allocate_co_shape_spec
    :    expr ( T_COLON expr )?
    ;

allocate_co_shape_spec_list
@init{ int count=0;}
    :  		{action.allocate_co_shape_spec_list__begin();}
		allocate_co_shape_spec {count++;} ( T_COMMA allocate_co_shape_spec {count++;} )*
      		{action.allocate_co_shape_spec_list(count);}
    ;

/*
 * Section 7:
 */

// R701
// constant replaced by literal_constant as T_IDENT can be designator
// T_IDENT inlined for type_param_name
// data_ref in designator can be a T_IDENT so T_IDENT deleted
// type_param_inquiry is designator T_PERCENT T_IDENT can be designator so deleted
// function_reference integrated with designator (was ambiguous) and deleted (to reduce backtracking)
primary
options {backtrack=true;}       // alt 1,4 ambiguous
	:	designator_or_func_ref
	|	literal_constant
	|	array_constructor
	|	structure_constructor
	|	T_LPAREN expr T_RPAREN
	;

// R702
level_1_expr
@init{Token tk = null;} //@init{INIT_TOKEN_NULL(tk);}
    : (defined_unary_op {tk = $defined_unary_op.tk;})? primary
    		{action.level_1_expr(tk);}
    ;

// R703
defined_unary_op returns [Token tk]
	:	T_DEFINED_OP {tk = $T_DEFINED_OP;}
	;

// inserted as R704 functionality
power_operand
@init{boolean hasPowerOperand = false;}
	: level_1_expr (power_op power_operand {hasPowerOperand = true;})?
			{action.power_operand(hasPowerOperand);}
	;	

// R704
// see power_operand
mult_operand
@init{int numMultOps = 0;}
//    : level_1_expr ( power_op mult_operand )?
//    : power_operand
    : power_operand (mult_op power_operand
    					{action.mult_operand__mult_op($mult_op.tk); numMultOps += 1;}
    				)*
    		{action.mult_operand(numMultOps);}
    ;

// R705
// moved leading optionals to mult_operand
add_operand
@init{int numAddOps = 0;}
//    : ( add_operand mult_op )? mult_operand
//    : ( mult_operand mult_op )* mult_operand
    : (tk=add_op)? mult_operand ( tk1=add_op mult_operand 
    								{action.add_operand__add_op(tk1); numAddOps += 1;}
    						 	)*
    		{action.add_operand(tk, numAddOps);}
    ;

// R706
// moved leading optionals to add_operand
level_2_expr
@init{int numConcatOps = 0;}
//    : ( ( level_2_expr )? add_op )? add_operand
// check notes on how to remove this left recursion  (WARNING something like the following)
//    : (add_op)? ( add_operand add_op )* add_operand
    : add_operand ( concat_op add_operand {numConcatOps += 1;})*
    		{action.level_2_expr(numConcatOps);}
    ;

// R707
power_op returns [Token tk]
	:	T_POWER	{tk = $T_POWER;}
	;

// R708
mult_op returns [Token tk]
	:	T_ASTERISK	{tk = $T_ASTERISK;}
	|	T_SLASH		{tk = $T_SLASH;}
	;

// R709
add_op returns [Token tk]
	:	T_PLUS  {tk = $T_PLUS;}
	|	T_MINUS {tk = $T_MINUS;}
	;

// R710
// moved leading optional to level_2_expr
level_3_expr
@init{Token relOp = null;} //@init{INIT_TOKEN_NULL(relOp);}
//    : ( level_3_expr concat_op )? level_2_expr
//    : ( level_2_expr concat_op )* level_2_expr
    : level_2_expr (rel_op level_2_expr {relOp = $rel_op.tk;})?
    		{action.level_3_expr(relOp);}
    ;

// R711
concat_op
	:	T_SLASH_SLASH
	;

// R712
// moved leading optional to level_3_expr
// inlined level_3_expr for level_4_expr in R714
//level_4_expr
//    : ( level_3_expr rel_op )? level_3_expr
//    : level_3_expr
//    ;

// R713
rel_op returns [Token tk]
	:	T_EQ				{tk=$T_EQ;}
	|	T_NE				{tk=$T_NE;}
	|	T_LT				{tk=$T_LT;}
	|	T_LE				{tk=$T_LE;}
	|	T_GT				{tk=$T_GT;}
	|	T_GE				{tk=$T_GE;}
	|	T_EQ_EQ				{tk=$T_EQ_EQ;}
	|	T_SLASH_EQ			{tk=$T_SLASH_EQ;}
	|	T_LESSTHAN			{tk=$T_LESSTHAN;}
	|	T_LESSTHAN_EQ		{tk=$T_LESSTHAN_EQ;}
	|	T_GREATERTHAN		{tk=$T_GREATERTHAN;}
	|	T_GREATERTHAN_EQ	{tk=$T_GREATERTHAN_EQ;}
	;

// R714
// level_4_expr inlined as level_3_expr
and_operand
@init{boolean hasNotOp0 = false; // @init{INIT_BOOL_FALSE(hasNotOp0);
      boolean hasNotOp1 = false; // @init{INIT_BOOL_FALSE(hasNotOp1);
      int numAndOps = 0;}
//    :    ( not_op )? level_3_expr
	:	(not_op {hasNotOp0=true;})?
    	level_3_expr
		(and_op {hasNotOp1=false;} (not_op {hasNotOp1=true;})? level_3_expr
				{action.and_operand__not_op(hasNotOp1); numAndOps += 1;}
		)*
				{action.and_operand(hasNotOp0, numAndOps);}
    ;

// R715
// moved leading optional to or_operand
or_operand
@init{int numOrOps = 0;}
//    : ( or_operand and_op )? and_operand
//    : ( and_operand and_op )* and_operand
    : and_operand (or_op and_operand {numOrOps += 1;})*
    		{ action.or_operand(numOrOps); }
    ;

// R716
// moved leading optional to or_operand
// TODO - action for equiv_op token
equiv_operand
@init{int numEquivOps = 0;}
//    : ( equiv_operand or_op )? or_operand
//    : ( or_operand or_op )* or_operand
    : or_operand (equiv_op or_operand
    				{action.equiv_operand__equiv_op($equiv_op.tk); numEquivOps += 1;}
    			 )*
			{action.equiv_operand(numEquivOps);}
    ;

// R717
// moved leading optional to equiv_operand
level_5_expr
@init{int numDefinedBinaryOps = 0;}
//    : ( level_5_expr equiv_op )? equiv_operand
//    : ( equiv_operand equiv_op )* equiv_operand
    : equiv_operand (defined_binary_op equiv_operand
    					{action.level_5_expr__defined_binary_op($defined_binary_op.tk); numDefinedBinaryOps += 1;}
    				)*
    		{action.level_5_expr(numDefinedBinaryOps);}
    ;

// R718
not_op returns [Token tk]
	:	T_NOT {tk = $T_NOT;}
	;

// R719
and_op returns [Token tk]
	:	T_AND {tk = $T_AND;}
	;

// R720
or_op returns [Token tk]
	:	T_OR {tk = $T_OR;}
	;

// R721
equiv_op returns [Token tk]
	:	T_EQV {tk = $T_EQV;}
	|	T_NEQV {tk = $T_NEQV;}
	;

// R722
// moved leading optional to level_5_expr
expr
//    : ( expr defined_binary_op )? level_5_expr
//    : ( level_5_expr defined_binary_op )* level_5_expr
    : level_5_expr
    	{action.expr();}
    ;

// R723
defined_binary_op returns [Token tk]
	:	T_DEFINED_OP {tk = $T_DEFINED_OP;}
	;

// R724 inlined logical_expr was expr

// R725 inlined char_expr was expr

// R726 inlined default_char_expr

// R727 inlined int_expr

// R728 inlined numeric_expr was expr

// inlined scalar_numeric_expr was expr

// R729 inlined specification_expr was scalar_int_expr

// R730 inlined initialization_expr

// R731 inlined char_initialization_expr was char_expr

// inlined scalar_char_initialization_expr was char_expr

// R732 inlined int_initialization_expr was int_expr

// inlined scalar_int_initialization_expr was int_initialization_expr

// R733 inlined logical_initialization_expr was logical_expr

// inlined scalar_logical_initialization_expr was logical_expr

// R734
assignment_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_ASSIGNMENT_STMT variable
		T_EQUALS expr T_EOS
			{action.assignment_stmt(lbl);}
	;

// R735
// ERR_TEST 735 ensure that part_ref in data_ref doesn't capture the T_LPAREN
// data_pointer_object and proc_pointer_object replaced by designator
// data_target and proc_target replaced by expr
// third alt covered by first alt so proc_pointer_object assignment deleted
// designator (R603), minus the substring part is data_ref, so designator replaced by data_ref,
// see NOTE 6.10 for why array-section does not have pointer attribute
// TODO: alt1 and alt3 require the backtracking.  if find a way to disambiguate
// them, should be able to remove backtracking.
pointer_assignment_stmt
options {backtrack=true;}
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    : (label {lbl=$label.tk;})? T_PTR_ASSIGNMENT_STMT data_ref T_EQ_GT expr T_EOS
    | (label {lbl=$label.tk;})? T_PTR_ASSIGNMENT_STMT data_ref T_LPAREN bounds_spec_list T_RPAREN T_EQ_GT expr T_EOS
    | (label {lbl=$label.tk;})? T_PTR_ASSIGNMENT_STMT data_ref T_LPAREN bounds_remapping_list T_RPAREN T_EQ_GT expr T_EOS
    ;

// R736
// ERR_CHK 736 ensure ( T_IDENT | designator ending in T_PERCENT T_IDENT)
// T_IDENT inlined for variable_name and data_pointer_component_name
// variable replaced by designator
data_pointer_object
	:	designator
	;

// R737
// ERR_CHK 737 lower_bound_expr replaced by expr
bounds_spec
	:	expr T_COLON
	;

bounds_spec_list
@init{ int count=0;}
    :  		{action.bounds_spec_list__begin();}
		bounds_spec {count++;} ( T_COMMA bounds_spec {count++;} )*
      		{action.bounds_spec_list(count);}
    ;

// R738
// ERR_CHK 738a lower_bound_expr replaced by expr
// ERR_CHK 738b upper_bound_expr replaced by expr
bounds_remapping
	:	expr T_COLON expr
	;

bounds_remapping_list
@init{ int count=0;}
    :  		{action.bounds_remapping_list__begin();}
		bounds_remapping {count++;} ( T_COMMA bounds_remapping {count++;} )*
      		{action.bounds_remapping_list(count);}
    ;

// R739 data_target inlined as expr in R459 and R735
// expr can be designator (via primary) so variable deleted

// R740
// ERR_CHK 740 ensure ( T_IDENT | ends in T_PERCENT T_IDENT )
// T_IDENT inlined for proc_pointer_name
// proc_component_ref replaced by designator T_PERCENT T_IDENT replaced by designator
proc_pointer_object
	:	designator
	;

// R741 proc_component_ref inlined as designator T_PERCENT T_IDENT in R740, R742, R1219, and R1221
// T_IDENT inlined for procedure_component_name
// designator inlined for variable

// R742 proc_target inlined as expr in R459 and R735
// ERR_CHK 736 ensure ( expr | designator ending in T_PERCENT T_IDENT)
// T_IDENT inlined for procedure_name
// T_IDENT isa expr so T_IDENT deleted
// proc_component_ref is variable T_PERCENT T_IDENT can be designator so deleted

// R743
// ERR_CHK 743 mask_expr replaced by expr
// assignment_stmt inlined for where_assignment_stmt
where_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_WHERE_STMT T_WHERE
		T_LPAREN expr T_RPAREN assignment_stmt
			{action.where_stmt(lbl);}
	;

// R744
where_construct
    :    where_construct_stmt ( where_body_construct )* ( masked_elsewhere_stmt
          ( where_body_construct )* )* ( elsewhere_stmt ( where_body_construct )* )? end_where_stmt
    ;

// R745
// ERR_CHK 745 mask_expr replaced by expr
where_construct_stmt
@init{Token id=null;}
	:	( T_IDENT T_COLON {id=$T_IDENT;})? T_WHERE_CONSTRUCT_STMT T_WHERE 
            T_LPAREN expr T_RPAREN T_EOS
				{action.where_construct_stmt(id);}
    ;

// R746
// assignment_stmt inlined for where_assignment_stmt
where_body_construct
	:	assignment_stmt
	|	where_stmt
	|	where_construct
	;

// R747 where_assignment_stmt inlined as assignment_stmt in R743 and R746

// R748 inlined mask_expr was logical_expr

// inlined scalar_mask_expr was scalar_logical_expr

// inlined scalar_logical_expr was logical_expr

// R749
// ERR_CHK 749 mask_expr replaced by expr
masked_elsewhere_stmt
@init{Token lbl = null;Token id=null;}
	:	(label {lbl=$label.tk;})? T_ELSE T_WHERE T_LPAREN expr T_RPAREN ( T_IDENT {id=$T_IDENT;})? T_EOS 
			{action.masked_elsewhere_stmt(lbl,id);}
	|	(label {lbl=$label.tk;})? T_ELSEWHERE T_LPAREN expr T_RPAREN ( T_IDENT {id=$T_IDENT;})? T_EOS 
			{action.masked_elsewhere_stmt(lbl,id);}
	;

// R750
elsewhere_stmt
@init{ Token lbl = null; Token id=null;} 
	:	(label {lbl=$label.tk;})? T_ELSE T_WHERE (T_IDENT {id=$T_IDENT;})? T_EOS
			{action.elsewhere_stmt(lbl,id);}
	|	(label {lbl=$label.tk;})? T_ELSEWHERE (T_IDENT {id=$T_IDENT;})? T_EOS 
			{action.elsewhere_stmt(lbl,id);}
	;

// R751
end_where_stmt
@init{Token lbl = null; Token id=null;} // @init{INIT_TOKEN_NULL(lbl);}
	: (label {lbl=$label.tk;})? T_END T_WHERE ( T_IDENT {id=$T_IDENT;} )? T_EOS
		{action.end_where_stmt(lbl,id);}
	| (label {lbl=$label.tk;})? T_ENDWHERE ( T_IDENT {id=$T_IDENT;} )? T_EOS
		{action.end_where_stmt(lbl,id);}
	;

// R752
forall_construct
	:	forall_construct_stmt
		( forall_body_construct )*
		end_forall_stmt
	;

// R753
forall_construct_stmt
@init{Token lbl = null; Token id = null;} 
    :    (label {lbl=$label.tk;})? ( T_IDENT T_COLON {id=$T_IDENT;})? T_FORALL_CONSTRUCT_STMT T_FORALL 
            forall_header T_EOS
				{action.forall_construct_stmt(lbl,id);}
    ;

// R754
// ERR_CHK 754 scalar_mask_expr replaced by expr
forall_header
    : T_LPAREN forall_triplet_spec_list ( T_COMMA expr )? T_RPAREN
    ;

// R755
// T_IDENT inlined for index_name
// expr inlined for subscript and stride
forall_triplet_spec
    : T_IDENT T_EQUALS expr T_COLON expr ( T_COLON expr )?
    ;


forall_triplet_spec_list
@init{ int count=0;}
    :  		{action.forall_triplet_spec_list__begin();}
		forall_triplet_spec {count++;} ( T_COMMA forall_triplet_spec {count++;} )*
      		{action.forall_triplet_spec_list(count);}
    ;

// R756
forall_body_construct
	:	forall_assignment_stmt
	|	where_stmt
	|	where_construct
	|	forall_construct
	|	forall_stmt
	;

// R757
forall_assignment_stmt
	:	assignment_stmt
	|	pointer_assignment_stmt
	;

// R758
end_forall_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	: (label {lbl=$label.tk;})? T_END T_FORALL ( T_IDENT )? T_EOS
	| (label {lbl=$label.tk;})? T_ENDFORALL ( T_IDENT )? T_EOS
	;

// R759
// T_FORALL_STMT token is inserted by scanner to remove need for backtracking
forall_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_FORALL_STMT T_FORALL
		forall_header
		forall_assignment_stmt
	;

/*
 * Section 8:
 */

// R801
block
	:	( execution_part_construct )*
	;

// R802
if_construct
    :    if_then_stmt block ( else_if_stmt block )* ( else_stmt block )? end_if_stmt
    ;

// R803
// ERR_CHK 803 scalar_logical_expr replaced by expr
if_then_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    : (label {lbl=$label.tk;})? ( T_IDENT T_COLON )? T_IF T_LPAREN expr T_RPAREN T_THEN T_EOS
    ;

// R804
// ERR_CHK 804 scalar_logical_expr replaced by expr
else_if_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	: (label {lbl=$label.tk;})? T_ELSE T_IF
        T_LPAREN expr T_RPAREN T_THEN ( T_IDENT )? T_EOS
	| (label {lbl=$label.tk;})? T_ELSEIF
        T_LPAREN expr T_RPAREN T_THEN ( T_IDENT )? T_EOS
	;

// R805
else_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_ELSE
		( T_IDENT )? T_EOS
	;

// R806
end_if_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	: (label {lbl=$label.tk;})? T_END T_IF ( T_IDENT )? T_EOS
	| (label {lbl=$label.tk;})? T_ENDIF    ( T_IDENT )? T_EOS
	;

// R807
// ERR_CHK 807 scalar_logical_expr replaced by expr
// T_IF_STMT inserted by scanner to remove need for backtracking
if_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_IF_STMT T_IF
		T_LPAREN
		expr
		T_RPAREN
		action_stmt
	;

// R808
case_construct
    :    select_case_stmt ( case_stmt block )* end_select_stmt
    ;

// R809
// ERR_CHK 809 case_expr replaced by expr
select_case_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    :    (label {lbl=$label.tk;})? ( T_IDENT T_COLON )?
        (T_SELECT T_CASE | T_SELECTCASE)
        T_LPAREN expr T_RPAREN T_EOS
    ;

// R810
case_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_CASE
		case_selector
		( T_IDENT )? T_EOS
	;

// R811
end_select_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	: (label {lbl=$label.tk;})? T_END T_SELECT (T_IDENT)? T_EOS
	| (label {lbl=$label.tk;})? T_ENDSELECT    (T_IDENT)? T_EOS
	;

// R812 inlined case_expr with expr was either scalar_int_expr scalar_char_expr scalar_logical_expr

// inlined scalar_char_expr with expr was char_expr

// R813
case_selector
	:	T_LPAREN
		case_value_range_list
		T_RPAREN
	|	T_DEFAULT
	;

// R814
case_value_range
	:	T_COLON case_value
	|	case_value case_value_range_suffix
	;

case_value_range_suffix
	:	T_COLON ( case_value )?
	|	{ /* empty */ }
	;

case_value_range_list
@init{ int count=0;}
    :  		{action.case_value_range_list__begin();}
		case_value_range {count++;} ( T_COMMA case_value_range {count++;} )*
      		{action.case_value_range_list(count);}
    ;

// R815
// ERR_CHK 815 expr either scalar_int_initialization_expr scalar_char_initialization_expr scalar_logical_initialization_expr
case_value
	:	expr
	;

// R816
associate_construct
	:	associate_stmt
		block
		end_associate_stmt
	;

// R817
associate_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    : (label {lbl=$label.tk;})? ( T_IDENT T_COLON )? T_ASSOCIATE T_LPAREN association_list T_RPAREN T_EOS
    ;

association_list
@init{ int count=0;}
    :  		{action.association_list__begin();}
		association {count++;} ( T_COMMA association {count++;} )*
      		{action.association_list(count);}
    ;

// R818
// T_IDENT inlined for associate_name
association
	:	T_IDENT T_EQ_GT selector
	;

// R819
// expr can be designator (via primary) so variable deleted
selector
	:	expr
	;

// R820
end_associate_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	: (label {lbl=$label.tk;})? T_END T_ASSOCIATE ( T_IDENT )? T_EOS
	| (label {lbl=$label.tk;})? T_ENDASSOCIATE    ( T_IDENT )? T_EOS
	;

// R821
select_type_construct
    :    select_type_stmt ( type_guard_stmt block )* end_select_type_stmt
    ;

// R822
// T_IDENT inlined for select_construct_name and associate_name
select_type_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    : (label {lbl=$label.tk;})? ( T_IDENT T_COLON )? select_type
         T_LPAREN ( T_IDENT T_EQ_GT )? selector T_RPAREN T_EOS
    ;

select_type
    : T_SELECT T_TYPE
    | T_SELECTTYPE
    ;

// R823
// T_IDENT inlined for select_construct_name
// TODO - FIXME - have to remove T_TYPE_IS and T_CLASS_IS because the 
// lexer never matches the sequences.  lexer now matches a T_IDENT for 
// the 'IS'.  this rule should be fixed (see test_select_stmts.f03)
type_guard_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_TYPE T_IDENT T_LPAREN
		type_spec
		T_RPAREN
		( T_IDENT )? T_EOS
	|	(label {lbl=$label.tk;})? T_CLASS T_IDENT T_LPAREN
		type_spec
		T_RPAREN
		( T_IDENT )? T_EOS
	|	(label {lbl=$label.tk;})? T_CLASS	T_DEFAULT
		( T_IDENT )? T_EOS
	;

// R824
// T_IDENT inlined for select_construct_name
end_select_type_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_END T_SELECT ( T_IDENT )? T_EOS
	|	(label {lbl=$label.tk;})? T_ENDSELECT    ( T_IDENT )? T_EOS
	;

// R825
// deleted second alternative, nonblock_do_construct, to reduce backtracking, see comments for R835 on how
// termination of nested loops must be handled.
do_construct
	:	block_do_construct
	;

// R826
// do_block replaced by block
block_do_construct
	:	do_stmt
		block
		end_do
	;

// R827
// label_do_stmt and nonlabel_do_stmt inlined
do_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? ( T_IDENT T_COLON )? T_DO ( T_DIGIT_STRING )? ( loop_control )? T_EOS
	;

// R828
// T_IDENT inlined for do_construct_name
// T_DIGIT_STRING inlined for label
label_do_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? ( T_IDENT T_COLON )? T_DO T_DIGIT_STRING ( loop_control )? T_EOS
	;

// R829 inlined in R827
// T_IDENT inlined for do_construct_name

// R830
// ERR_CHK 830a scalar_int_expr replaced by expr
// ERR_CHK 830b scalar_logical_expr replaced by expr
loop_control
    : ( T_COMMA )? T_WHILE T_LPAREN expr T_RPAREN
    | ( T_COMMA )? do_variable T_EQUALS expr T_COMMA expr ( T_COMMA expr )?
    ;

// R831
do_variable
	:	scalar_int_variable
	;

// R832 do_block was block inlined in R826

// R833
// T_CONTINUE inlined for continue_stmt
// TODO continue-stmt is ambiguous with same in action statement, check there for label and if
// label matches do-stmt label, then match end-do
// do_term_action_stmt added to allow block_do_construct to cover nonblock_do_construct as well
// TODO putback
end_do
	:	end_do_stmt
	|	do_term_action_stmt
	;

// R834
// T_IDENT inlined for do_construct_name
end_do_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	: (label {lbl=$label.tk;})? T_END T_DO ( T_IDENT )? T_EOS
	| (label {lbl=$label.tk;})? T_ENDDO    ( T_IDENT )? T_EOS
	;

// R835 nonblock_do_construct deleted as it was combined with block_do_construct to reduce backtracking
// Second alternative, outer_shared_do_construct (nested loops sharing a termination label) is ambiguous
// with do_construct in do_body, so deleted.  Loop termination will have to be coordinated with
// the scanner to unwind nested loops sharing a common termination statement (see do_term_action_stmt).

// R836 action_term_do_construct deleted because nonblock_do_construct combined with block_do_construct
// to reduce backtracking

// R837 do_body deleted because nonblock_do_construct combined with block_do_construct
// to reduce backtracking

// R838
// C826 (R842) A do-term-shared-stmt shall not be a goto-stmt, a return-stmt, a stop-stmt, an exit-stmt, a cyle-stmt, an end-function-stmt, an end-subroutine-stmt, an end-program-stmt, or an arithmetic-if-stmt.
// TODO need interaction with scanner to have this extra terminal emitted when do label matched
// TODO need interaction with scanner to terminate shared terminal action statements (see R835).
do_term_action_stmt
    // try requiring an action_stmt and then we can simply insert the new
    // T_LABEL_DO_TERMINAL during the Sale's prepass.  T_EOS is in action_stmt.
    // added the T_END T_DO and T_ENDDO options to this rule because of the
    // token T_LABEL_DO_TERMINAL that is inserted if they end a labeled DO.
    :   label T_LABEL_DO_TERMINAL 
        (action_stmt | ( (T_END T_DO (T_IDENT)?) | (T_ENDDO) (T_IDENT)?) T_EOS)
// 	:	T_LABEL_DO_TERMINAL action_stmt  
// 	:	T_LABEL_DO_TERMINAL action_or_cont_stmt  
	;

// R839 outer_shared_do_construct removed because it caused ambiguity in R835 (see comment in R835)

// R840 shared_term_do_construct deleted (see comments for R839 and R835)

// R841 inner_shared_do_construct deleted (see comments for R839 and R835)

// R842 do_term_shared_stmt deleted (see comments for R839 and R835)

// R843
// T_IDENT inlined for do_construct_name
cycle_stmt
@init{Token lbl = null; Token id = null;} // @init{INIT_TOKEN_NULL(lbl); INIT_TOKEN_NULL(id);}
	:	(label {lbl=$label.tk;})? T_CYCLE (T_IDENT {id=$T_IDENT;})? T_EOS
			{ action.cycle_stmt(lbl, id); }
	;

// R844
// T_IDENT inlined for do_construct_name
exit_stmt
@init{Token lbl = null; Token id = null;} // @init{INIT_TOKEN_NULL(lbl); INIT_TOKEN_NULL(id);}
	:	(label {lbl=$label.tk;})? T_EXIT (T_IDENT {id=$T_IDENT;})? T_EOS
			{ action.exit_stmt(lbl, id); }
	;

// R845
goto_stmt
	:	(T_GO T_TO | T_GOTO) label T_EOS
			{ action.goto_stmt($label.tk); }
	;

// R846
// ERR_CHK 846 scalar_int_expr replaced by expr
computed_goto_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})?
		(T_GO T_TO | T_GOTO) T_LPAREN label_list T_RPAREN ( T_COMMA )? expr T_EOS
			{ action.computed_goto_stmt(lbl); }
	;

// R847
// ERR_CHK 847 scalar_numeric_expr replaced by expr
arithmetic_if_stmt
	:	(lbl=label)? T_ARITHMETIC_IF_STMT T_IF
		T_LPAREN expr T_RPAREN label1=label
		T_COMMA label2=label
		T_COMMA label3=label T_EOS
			{ action.arithmetic_if_stmt(lbl, label1, label2, label3); }
	;

// R848 continue_stmt inlined as T_CONTINUE

// R849
stop_stmt
@init{Token lbl = null; boolean hasStopCode = false;}
	:	(label {lbl=$label.tk;})? T_STOP (stop_code {hasStopCode=true;})? T_EOS
			{ action.stop_stmt(lbl, hasStopCode); }
	;

// R850
// ERR_CHK 850 T_DIGIT_STRING must be 5 digits or less
stop_code
    : scalar_char_constant
//     | Digit ( Digit ( Digit ( Digit ( Digit )? )? )? )?
    | T_DIGIT_STRING
    	{ action.stop_code($T_DIGIT_STRING); } 
    ;

scalar_char_constant
    :    char_constant
    ;

/*
Section 9:
 */

// R901
// file_unit_number replaced by expr
// internal_file_variable isa expr so internal_file_variable deleted
io_unit
	:	expr
	|	T_ASTERISK
	;

// R902
// ERR_CHK 902 scalar_int_expr replaced by expr
file_unit_number
	:	expr
	;

// R903 internal_file_variable was char_variable inlined (and then deleted) in R901

// R904
open_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_OPEN T_LPAREN connect_spec_list T_RPAREN T_EOS
	;

// R905
// ERR_CHK 905 check expr type with identifier
connect_spec
    : expr
    | T_IDENT
        /* {'UNIT','ACCESS','ACTION','ASYNCHRONOUS','BLANK','DECIMAL','DELIM','ENCODING'} are expr */
        /* {'ERR'} is T_DIGIT_STRING */
        /* {'FILE','FORM'} are expr */
        /* {'IOMSG','IOSTAT'} are variables */
        /* {'PAD','POSITION','RECL','ROUND','SIGN','STATUS'} are expr */
      T_EQUALS expr
    ;

connect_spec_list
@init{ int count=0;}
    :  		{action.connect_spec_list__begin();}
		connect_spec {count++;} ( T_COMMA connect_spec {count++;} )*
      		{action.connect_spec_list(count);}
    ;

// inlined scalar_default_char_expr

// R906 inlined file_name_expr with expr was scalar_default_char_expr

// R907 iomsg_variable inlined as scalar_default_char_variable in R905,R909,R913,R922,R926,R928

// R908
close_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_CLOSE T_LPAREN close_spec_list T_RPAREN T_EOS
	;

// R909
// file_unit_number, scalar_int_variable, iomsg_variable, label replaced by expr
close_spec
	:	expr
	|	T_IDENT /* {'UNIT','IOSTAT','IOMSG','ERR','STATUS'} */ T_EQUALS expr
	;

close_spec_list
@init{ int count=0;}
    :  		{action.close_spec_list__begin();}
		close_spec {count++;} ( T_COMMA close_spec {count++;} )*
      		{action.close_spec_list(count);}
    ;

// R910
read_stmt
options {k=3;}
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    :    ((label)? T_READ T_LPAREN) => (label {lbl=$label.tk;})? T_READ T_LPAREN io_control_spec_list T_RPAREN ( input_item_list )? T_EOS
    |    ((label)? T_READ) => (label {lbl=$label.tk;})? T_READ format ( T_COMMA input_item_list )? T_EOS
    ;

// R911
write_stmt
@init{Token lbl = null; boolean hasOutputList=false;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_WRITE T_LPAREN io_control_spec_list T_RPAREN ( output_item_list {hasOutputList=true;})? T_EOS
			{ action.write_stmt(lbl, hasOutputList); }
	;

// R912
print_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    :    (label {lbl=$label.tk;})? T_PRINT format ( T_COMMA output_item_list )? T_EOS
    ;

// R913
// ERR_CHK 913 check expr type with identifier
// io_unit and format are both (expr|'*') so combined
io_control_spec
        :	expr
        		{ action.io_control_spec(true, null, false); }	// hasExpression=true
        |	T_ASTERISK
        		{ action.io_control_spec(false, null, true); }	// hasAsterisk=true
        |	T_IDENT /* {'UNIT','FMT'} */ T_EQUALS T_ASTERISK
        		{ action.io_control_spec(false, $T_IDENT, true); }	// hasAsterisk=true
        |	T_IDENT
		    /* {'UNIT','FMT'} are expr 'NML' is T_IDENT} */
		    /* {'ADVANCE','ASYNCHRONOUS','BLANK','DECIMAL','DELIM'} are expr */
		    /* {'END','EOR','ERR'} are labels */
		    /* {'ID','IOMSG',IOSTAT','SIZE'} are variables */
		    /* {'PAD','POS','REC','ROUND','SIGN'} are expr */
		T_EQUALS expr
        		{ action.io_control_spec(true, $T_IDENT, false); }	// hasExpression=true
    ;


io_control_spec_list
@init{ int count=0;}
    :  		{action.io_control_spec_list__begin();}
		io_control_spec {count++;} ( T_COMMA io_control_spec {count++;} )*
      		{action.io_control_spec_list(count);}
    ;

// R914
// ERR_CHK 914 default_char_expr replaced by expr
// label replaced by T_DIGIT_STRING is expr so deleted
format
	:	expr
	|	T_ASTERISK
	;

// R915
input_item
	:	variable
	|	io_implied_do
	;

input_item_list
@init{ int count=0;}
    :  		{action.input_item_list__begin();}
		input_item {count++;} ( T_COMMA input_item {count++;} )*
      		{action.input_item_list(count);}
    ;

// R916
output_item
options {backtrack=true;}
	:	expr
	|	io_implied_do
	;


output_item_list
@init{ int count=0;}
    :  		{action.output_item_list__begin();}
		output_item {count++;} ( T_COMMA output_item {count++;} )*
      		{action.output_item_list(count);}
    ;

// R917
io_implied_do
	:	T_LPAREN io_implied_do_object io_implied_do_suffix T_RPAREN
	;

// R918
// expr in output_item can be variable in input_item so input_item deleted
io_implied_do_object
	:	output_item
	;

io_implied_do_suffix
options {backtrack=true;}
	:	T_COMMA io_implied_do_object io_implied_do_suffix
	|	T_COMMA io_implied_do_control
	;

// R919
// ERR_CHK 919 scalar_int_expr replaced by expr
io_implied_do_control
    : do_variable T_EQUALS expr T_COMMA expr ( T_COMMA expr )?
    ;

// R920
dtv_type_spec
	:	T_TYPE
		T_LPAREN
		derived_type_spec
		T_RPAREN
	|	T_CLASS
		T_LPAREN
		derived_type_spec
		T_RPAREN
	;

// R921
wait_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_WAIT T_LPAREN wait_spec_list T_RPAREN T_EOS
	;

// R922
// file_unit_number, scalar_int_variable, iomsg_variable, label replaced by expr
wait_spec
	:	expr
	|	T_IDENT /* {'UNIT','END','EOR','ERR','ID','IOMSG','IOSTAT'} */ T_EQUALS expr
	;


wait_spec_list
@init{ int count=0;}
    :  		{action.wait_spec_list__begin();}
		wait_spec {count++;} ( T_COMMA wait_spec {count++;} )*
      		{action.wait_spec_list(count);}
    ;

// R923
backspace_stmt
options {k=3;}
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	((label)? T_BACKSPACE T_LPAREN) => (label {lbl=$label.tk;})? T_BACKSPACE T_LPAREN position_spec_list T_RPAREN T_EOS
	|	((label)? T_BACKSPACE) => (label {lbl=$label.tk;})? T_BACKSPACE file_unit_number T_EOS
	;

// R924
endfile_stmt
options {k=3;}
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	((label)? T_END T_FILE T_LPAREN) => (label {lbl=$label.tk;})? T_END T_FILE T_LPAREN position_spec_list T_RPAREN T_EOS
	|	((label)? T_ENDFILE T_LPAREN) => (label {lbl=$label.tk;})? T_ENDFILE T_LPAREN position_spec_list T_RPAREN T_EOS
	|	((label)? T_END T_FILE) => (label {lbl=$label.tk;})? T_END T_FILE file_unit_number T_EOS
	|	((label)? T_ENDFILE) => (label {lbl=$label.tk;})? T_ENDFILE file_unit_number T_EOS
	;

// R925
rewind_stmt
options {k=3;}
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	((label)? T_REWIND T_LPAREN) => (label {lbl=$label.tk;})? T_REWIND T_LPAREN position_spec_list T_RPAREN T_EOS
	|	((label)? T_REWIND) => (label {lbl=$label.tk;})? T_REWIND file_unit_number T_EOS
	;

// R926
// file_unit_number, scalar_int_variable, iomsg_variable, label replaced by expr
position_spec
	:	expr
	|	T_IDENT /* {'UNIT','IOSTAT','IOMSG','ERR'} */ T_EQUALS expr
    ;

position_spec_list
@init{ int count=0;}
    :  		{action.position_spec_list__begin();}
		position_spec {count++;} ( T_COMMA position_spec {count++;} )*
      		{action.position_spec_list(count);}
    ;

// R927
flush_stmt
options {k=3;}
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	((label)? T_FLUSH T_LPAREN) => (label {lbl=$label.tk;})? T_FLUSH T_LPAREN flush_spec_list T_RPAREN T_EOS
	|	((label)? T_FLUSH) => (label {lbl=$label.tk;})? T_FLUSH file_unit_number T_EOS
	;

// R928
// file_unit_number, scalar_int_variable, iomsg_variable, label replaced by expr
flush_spec
	:	expr
	|	T_IDENT /* {'UNIT','IOSTAT','IOMSG','ERR'} */ T_EQUALS expr
    ;

flush_spec_list
@init{ int count=0;}
    :  		{action.flush_spec_list__begin();}
		flush_spec {count++;} ( T_COMMA flush_spec {count++;} )*
      		{action.flush_spec_list(count);}
    ;

// R929
inquire_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label)? T_INQUIRE T_LPAREN inquire_spec_list T_RPAREN T_EOS
// 	|	(label)? T_INQUIRE T_LPAREN T_IDENT /* 'IOLENGTH' */ T_EQUALS scalar_int_variable T_RPAREN output_item_list T_EOS
	|	(label {lbl=$label.tk;})? T_INQUIRE_STMT_2 
            T_INQUIRE T_LPAREN T_IDENT /* 'IOLENGTH' */ T_EQUALS 
            scalar_int_variable T_RPAREN output_item_list T_EOS
	;


// R930
// ERR_CHK 930 file_name_expr replaced by expr
// file_unit_number replaced by expr
// scalar_default_char_variable replaced by designator
inquire_spec
	:	expr
	|	T_IDENT /* {'UNIT','FILE'} '=' expr portion, '=' designator portion below */
			/* {'ACCESS','ACTION','ASYNCHRONOUS','BLANK','DECIMAL',DELIM','DIRECT'} */
			/* {'ENCODING','ERR','EXIST','FORM','FORMATTED','ID','IOMSG','IOSTAT'} */
			/* {'NAME','NAMED','NEXTREC','NUMBER',OPENED','PAD','PENDING','POS'} */
			/* {'POSITION','READ','READWRITE','RECL','ROUND','SEQUENTIAL','SIGN'} */
			/* {'SIZE','STREAM','UNFORMATTED','WRITE'}*/
		T_EQUALS expr
	;

inquire_spec_list
@init{ int count=0;}
    :  		{action.inquire_spec_list__begin();}
		inquire_spec {count++;} ( T_COMMA inquire_spec {count++;} )*
      		{action.inquire_spec_list(count);}
    ;

/*
Section 10:
 */

// R1001
// TODO: error checking: label is required.  accept as optional so we can
// report the error to the user.
format_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_FORMAT format_specification T_EOS
	;

// R1002
format_specification
	:	T_LPAREN ( format_item_list )? T_RPAREN
	;

// R1003
// r replaced by int_literal_constant replaced by char_literal_constant replaced by T_CHAR_CONSTANT
// char_string_edit_desc replaced by T_CHAR_CONSTANT
format_item
    :   T_DATA_EDIT_DESC 
    |   T_CONTROL_EDIT_DESC
    |   T_CHAR_STRING_EDIT_DESC
    |   (T_DIGIT_STRING)? T_LPAREN format_item_list T_RPAREN
    ;

// the comma is not always required.  see J3/04-007, pg. 221, lines
// 17-22
// ERR_CHK
format_item_list
@init{ int count=0;}
    :  		{action.format_item_list__begin();}
		format_item {count++;} ( (T_COMMA)? format_item {count++;} )*
      		{action.format_item_list(count);}
    ;


// the following rules, from here to the v_list, are the originals.  modifying 
// to try and simplify and make match up with the standard.
// original rules. 02.01.07
// // R1003
// // r replaced by int_literal_constant replaced by char_literal_constant replaced by T_CHAR_CONSTANT
// // char_string_edit_desc replaced by T_CHAR_CONSTANT
// format_item
// 	:	T_DIGIT_STRING data_edit_desc
// 	|	data_plus_control_edit_desc
// 	|	T_CHAR_CONSTANT
// 	|	(T_DIGIT_STRING)? T_LPAREN format_item_list T_RPAREN
// 	;

// format_item_list
//     :    format_item ( T_COMMA format_item )*
//     ;

// // R1004 r inlined in R1003 and R1011 as int_literal_constant (then as DIGIT_STRING)
// // C1004 (R1004) r shall not have a kind parameter associated with it

// // R1005
// // w,m,d,e replaced by int_literal_constant replaced by T_DIGIT_STRING
// // char_literal_constant replaced by T_CHAR_CONSTANT
// // ERR_CHK 1005 matching T_ID_OR_OTHER with alternatives will have to be done here
// data_edit_desc
//     : T_ID_OR_OTHER /* {'I','B','O','Z','F','E','EN','ES','G','L','A','D'} */ 
//       T_DIGIT_STRING ( T_PERIOD T_DIGIT_STRING )?
//       ( T_ID_OR_OTHER /* is 'E' */ T_DIGIT_STRING )?
//     | T_ID_OR_OTHER /* is 'DT' */ T_CHAR_CONSTANT ( T_LPAREN v_list T_RPAREN )?
//     | T_ID_OR_OTHER /* {'A','DT'},{'X','P' from control_edit_desc} */
//     ;

// data_plus_control_edit_desc
// 	:	T_ID_OR_OTHER /* {'I','B','O','Z','F','E','EN','ES','G','L','A','D'},{T','TL','TR'} */ 
// 		    T_DIGIT_STRING ( T_PERIOD T_DIGIT_STRING )?
// 		    ( T_ID_OR_OTHER /* is 'E' */ T_DIGIT_STRING )?
// 	|	T_ID_OR_OTHER /* is 'DT' */ T_CHAR_CONSTANT ( T_LPAREN v_list T_RPAREN )?
// 	|	T_ID_OR_OTHER /* {'A','DT'},{'BN','BZ','RU','RD','RZ','RN','RC','RP','DC','DP'} */
// // following only from control_edit_desc
// 	|	( T_DIGIT_STRING )? T_SLASH
// 	|	T_COLON
// 	|	(T_PLUS|T_MINUS) T_DIGIT_STRING T_ID_OR_OTHER /* is 'P' */
// 	;

// R1006 w inlined in R1005 as int_literal_constant replaced by T_DIGIT_STRING

// R1007 m inlined in R1005 as int_literal_constant replaced by T_DIGIT_STRING

// R1008 d inlined in R1005 as int_literal_constant replaced by T_DIGIT_STRING

// R1009 e inlined in R1005 as int_literal_constant replaced by T_DIGIT_STRING

// R1010 v inlined as signed_int_literal_constant in v_list replaced by (T_PLUS or T_MINUS) T_DIGIT_STRING

v_list
@init{int count=0;}
    :  		{action.v_list__begin();}
		(pm=T_PLUS|T_MINUS)? ds=T_DIGIT_STRING
			{
				count++;
				action.v_list_part(pm, ds);
			}
		( T_COMMA (pm=T_PLUS|T_MINUS)? ds=T_DIGIT_STRING
			{
				count++;
				action.v_list_part(pm, ds);
			}
		)*
      		{action.v_list(count);}
    ;

// R1011 control_edit_desc inlined/combined in R1005 and data_plus_control_edit_desc
// r replaced by int_literal_constant replaced by T_DIGIT_STRING
// k replaced by signed_int_literal_constant replaced by (T_PLUS|T_MINUS)? T_DIGIT_STRING
// position_edit_desc inlined
// sign_edit_desc replaced by T_ID_OR_OTHER was {'SS','SP','S'}
// blank_interp_edit_desc replaced by T_ID_OR_OTHER was {'BN','BZ'}
// round_edit_desc replaced by T_ID_OR_OTHER was {'RU','RD','RZ','RN','RC','RP'}
// decimal_edit_desc replaced by T_ID_OR_OTHER was {'DC','DP'}
// leading T_ID_OR_OTHER alternates combined with data_edit_desc in data_plus_control_edit_desc

// R1012 k inlined in R1011 as signed_int_literal_constant
// C1009 (R1012) k shall not have a kind parameter specified for it

// R1013 position_edit_desc inlined in R1011
// n in R1013 was replaced by int_literal_constant replaced by T_DIGIT_STRING

// R1014 n inlined in R1013 as int_literal_constant (is T_DIGIT_STRING, see C1010)
// C1010 (R1014) n shall not have a kind parameter specified for it

// R1015 sign_edit_desc inlined in R1011 as T_ID_OR_OTHER was {'SS','SP','S'}

// R1016 blank_interp_edit_desc inlined in R1011 as T_ID_OR_OTHER was {'BN','BZ'}

// R1017 round_edit_desc inlined in R1011 as T_ID_OR_OTHER was {'RU','RD','RZ','RN','RC','RP'}

// R1018 decimal_edit_desc inlined in R1011 as T_ID_OR_OTHER was {'DC','DP'}

// R1019 char_string_edit_desc was char_literal_constant inlined in R1003 as T_CHAR_CONSTANT

/*
 * Section 11:
 */


// R1102
// T_IDENT inlined for program_name
program_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_PROGRAM T_IDENT T_EOS
		{ action.program_stmt(lbl, $T_IDENT); }
	;

// R1103
// T_IDENT inlined for program_name
end_program_stmt
@init{Token lbl = null; Token id = null;}
	:	(label {lbl=$label.tk;})? T_END T_PROGRAM (T_IDENT {id=$T_IDENT;})? end_of_stmt
			{ action.end_program_stmt(lbl, id); }
	|	(label {lbl=$label.tk;})? T_ENDPROGRAM (T_IDENT {id=$T_IDENT;})? end_of_stmt
			{ action.end_program_stmt(lbl, id); }
	|	(label {lbl=$label.tk;})? T_END end_of_stmt
			{ action.end_program_stmt(lbl, null); }
	;

	
// R1104
// C1104 (R1104) A module specification-part shall not contain a stmt-function-stmt, an entry-stmt or a format-stmt
// specification_part made non-optional to remove END ambiguity (as can be empty)
module
	:	module_stmt
		specification_part
		( module_subprogram_part )?
		end_module_stmt
	;

// R1105
module_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_MODULE ( T_IDENT )? T_EOS
	;


// R1106
end_module_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
        :       (label {lbl=$label.tk;})? T_END T_MODULE ( T_IDENT )? end_of_stmt
        |       (label {lbl=$label.tk;})? T_ENDMODULE    ( T_IDENT )? end_of_stmt
        |       (label {lbl=$label.tk;})? T_END end_of_stmt
        ;

// R1107
// T_CONTAINS inlined for contains_stmt
module_subprogram_part
	:	T_CONTAINS T_EOS
		module_subprogram
		( module_subprogram )*
	;

// R1108
// modified to factor optional prefix
module_subprogram
	:	(prefix)? function_subprogram
	|	subroutine_subprogram
	;

// R1109
use_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    :    (label {lbl=$label.tk;})? T_USE ( ( T_COMMA module_nature )? T_COLON_COLON )? T_IDENT ( T_COMMA rename_list )? T_EOS
    |    (label {lbl=$label.tk;})? T_USE ( ( T_COMMA module_nature )? T_COLON_COLON )? T_IDENT T_COMMA T_ONLY T_COLON ( only_list )? T_EOS
    ;

// R1110
module_nature
	:	T_INTRINSIC
	|	T_NON_INTRINSIC
	;

// R1111
// T_DEFINED_OP inlined for local_defined_operator and use_defined_operator
// T_IDENT inlined for local_name and use_name
rename
	:	T_IDENT T_EQ_GT T_IDENT
	|	T_OPERATOR T_LPAREN T_DEFINED_OP T_RPAREN T_EQ_GT
		T_OPERATOR T_LPAREN T_DEFINED_OP T_RPAREN
	;

rename_list
@init{ int count=0;}
    :  		{action.rename_list__begin();}
		rename {count++;} ( T_COMMA rename {count++;} )*
      		{action.rename_list(count);}
    ;

// R1112
// T_IDENT inlined for only_use_name
// generic_spec can be T_IDENT so T_IDENT deleted
only
	:	generic_spec
	|	rename
	;

only_list
@init{ int count=0;}
    :  		{action.only_list__begin();}
		only {count++;} ( T_COMMA only {count++;} )*
      		{action.only_list(count);}
    ;

// R1113 only_use_name was use_name inlined as T_IDENT

// R1114 inlined local_defined_operator in R1111 as T_DEFINED_OP

// R1115 inlined use_defined_operator in R1111 as T_DEFINED_OP

// R1116
// specification_part made non-optional to remove END ambiguity (as can be empty)
block_data
	:	block_data_stmt
		specification_part
		end_block_data_stmt
	;

// R1117
block_data_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_BLOCK T_DATA ( T_IDENT )? T_EOS
	|   (label {lbl=$label.tk;})? T_BLOCKDATA ( T_IDENT )? T_EOS
	;

// R1118
end_block_data_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:   (label {lbl=$label.tk;})? T_END T_BLOCK T_DATA ( T_IDENT )? end_of_stmt
	|   (label {lbl=$label.tk;})? T_ENDBLOCK T_DATA    ( T_IDENT )? end_of_stmt
	|   (label {lbl=$label.tk;})? T_END T_BLOCKDATA    ( T_IDENT )? end_of_stmt
	|   (label {lbl=$label.tk;})? T_ENDBLOCKDATA       ( T_IDENT )? end_of_stmt
	|	(label)? T_END end_of_stmt
	;

/*
 * Section 12:
 */

// R1201
interface_block
	:	interface_stmt
		( interface_specification )*
		end_interface_stmt
	;

// R1202
interface_specification
	:	interface_body
	|	procedure_stmt
	;

// R1203
interface_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:   (label {lbl=$label.tk;})? T_INTERFACE ( generic_spec )? T_EOS
	|	(label)? T_ABSTRACT T_INTERFACE T_EOS
	;

// R1204
end_interface_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	: (label {lbl=$label.tk;})? T_END T_INTERFACE ( generic_spec )? T_EOS
	| (label {lbl=$label.tk;})? T_ENDINTERFACE    ( generic_spec )? T_EOS
	;

// R1205
// specification_part made non-optional to remove END ambiguity (as can be empty)
interface_body
	:	(prefix)? function_stmt specification_part end_function_stmt
	|	subroutine_stmt specification_part end_subroutine_stmt
	;

// R1206
// generic_name_list substituted for procedure_name_list
procedure_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? ( T_MODULE )? T_PROCEDURE generic_name_list T_EOS
	;

// R1207
// T_IDENT inlined for generic_name
generic_spec
	:	T_IDENT
	|	T_OPERATOR T_LPAREN defined_operator T_RPAREN
	|	T_ASSIGNMENT T_LPAREN T_EQUALS T_RPAREN
	|	dtio_generic_spec
	;

// R1208
dtio_generic_spec
	:	T_READ T_LPAREN T_FORMATTED T_RPAREN
	|	T_READ T_LPAREN T_UNFORMATTED T_RPAREN
	|	T_WRITE T_LPAREN T_FORMATTED T_RPAREN
	|	T_WRITE T_LPAREN T_UNFORMATTED T_RPAREN
	;

// R1209
// generic_name_list substituted for import_name_list
import_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    :    (label {lbl=$label.tk;})? T_IMPORT ( ( T_COLON_COLON )? generic_name_list )? T_EOS
    ;

// R1210
// generic_name_list substituted for external_name_list
external_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_EXTERNAL ( T_COLON_COLON )? generic_name_list T_EOS
	;

// R1211
procedure_declaration_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    :    (label {lbl=$label.tk;})? T_PROCEDURE T_LPAREN ( proc_interface )? T_RPAREN
            ( ( T_COMMA proc_attr_spec )* T_COLON_COLON )? proc_decl_list T_EOS
    ;

// R1212
// T_IDENT inlined for interface_name
proc_interface
	:	T_IDENT					{ action.proc_interface($T_IDENT); }
	|	declaration_type_spec	{ action.proc_interface(null); }
	;

// R1213
proc_attr_spec
	:	access_spec
					{ action.proc_attr_spec(IFortranParserAction.AttrSpec.none); }
	|	proc_language_binding_spec
					{ action.proc_attr_spec(IFortranParserAction.AttrSpec.none); }
	|	T_INTENT T_LPAREN intent_spec T_RPAREN
					{ action.proc_attr_spec(IFortranParserAction.AttrSpec.INTENT); }
	|	T_OPTIONAL	{ action.proc_attr_spec(IFortranParserAction.AttrSpec.OPTIONAL); }
	|	T_POINTER	{ action.proc_attr_spec(IFortranParserAction.AttrSpec.POINTER); }
	|	T_SAVE		{ action.proc_attr_spec(IFortranParserAction.AttrSpec.SAVE); }
// TODO: are T_PASS, T_NOPASS, and T_DEFERRED correct?
// From R453 binding-attr
	|   T_PASS ( T_LPAREN T_IDENT T_RPAREN)?
    |   T_NOPASS
    |   T_DEFERRED
	;

// R1214
// T_IDENT inlined for procedure_entity_name
proc_decl
@init{boolean hasNullInit = false;}
    :	T_IDENT ( T_EQ_GT null_init {hasNullInit=true;} )?
    		{ action.proc_decl($T_IDENT, hasNullInit); }
    ;

proc_decl_list
@init{ int count=0;}
    :  		{action.proc_decl_list__begin();}
		proc_decl {count++;} ( T_COMMA proc_decl {count++;} )*
      		{action.proc_decl_list(count);}
    ;

// R1215 interface_name was name inlined as T_IDENT

// R1216
// generic_name_list substituted for intrinsic_procedure_name_list
intrinsic_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_INTRINSIC
		( T_COLON_COLON )?
		generic_name_list T_EOS
	;

// R1217 function_reference replaced by designator_or_func_ref to reduce backtracking

// R1218
// C1222 (R1218) The procedure-designator shall designate a subroutine.
call_stmt
@init{Token lbl = null; boolean hasActualArgSpecList = false;} // @init{INIT_TOKEN_NULL(lbl);}
    :    (label {lbl=$label.tk;})? T_CALL procedure_designator
         ( T_LPAREN (actual_arg_spec_list {hasActualArgSpecList=true;})? T_RPAREN )? T_EOS
         	{ action.call_stmt(lbl, hasActualArgSpecList); }
    ;

// R1219
// ERR_CHK 1219 must be (T_IDENT | designator T_PERCENT T_IDENT)
// T_IDENT inlined for procedure_name and binding_name
// proc_component_ref is variable T_PERCENT T_IDENT (variable is designator)
// data_ref subset of designator so data_ref T_PERCENT T_IDENT deleted
// designator (R603), minus the substring part is data_ref, so designator replaced by data_ref
//R1219 procedure-designator            is procedure-name
//                                      or proc-component-ref
//                                      or data-ref % binding-name
procedure_designator
	:	data_ref
	;

// R1220
actual_arg_spec
@init{Token keyword = null;}
    :	(T_IDENT T_EQUALS {keyword=$T_IDENT;})? actual_arg
    		{ action.actual_arg_spec(keyword); }
    ;

// TODO - delete greedy?
actual_arg_spec_list
options{greedy=false;}
@init{int count = 0;}
    :		{ action.actual_arg_spec_list__begin(); }
    	actual_arg_spec {count++;} ( T_COMMA actual_arg_spec {count++;} )*
    		{ action.actual_arg_spec_list(count); }
    ;

// R1221
// ERR_CHK 1221 ensure ( expr | designator ending in T_PERCENT T_IDENT)
// T_IDENT inlined for procedure_name
// expr isa designator (via primary) so variable deleted
// designator isa T_IDENT so T_IDENT deleted
// proc_component_ref is variable T_PERCENT T_IDENT can be designator so deleted
actual_arg
@init{boolean hasExpr = false;}
	:	expr				{ hasExpr=true; action.actual_arg(hasExpr, null); }
	|	T_ASTERISK label	{               action.actual_arg(hasExpr, $label.tk); }
	;

// R1222 alt_return_spec inlined as T_ASTERISK label in R1221

// R1223
// 1. left factored optional prefix in function_stmt from function_subprogram
// 2. specification_part made non-optional to remove END ambiguity (as can be empty)
function_subprogram
	:	function_stmt
		specification_part
		( execution_part )?
		( internal_subprogram_part )?
		end_function_stmt
	;

// R1224
// left factored optional prefix from function_stmt
// generic_name_list substituted for dummy_arg_name_list
function_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_FUNCTION T_IDENT
		T_LPAREN ( generic_name_list )? T_RPAREN ( suffix )? T_EOS
	;

// R1225
proc_language_binding_spec
	:	language_binding_spec
	;

// R1226 dummy_arg_name was name inlined as T_IDENT

// R1227
// C1240 (R1227) A prefix shall contain at most one of each prefix-spec
// C1241 (R1227) A prefix shall not specify both ELEMENTAL AND RECURSIVE
prefix
	:	prefix_spec ( prefix_spec (prefix_spec)? )?
	;

t_prefix
	:	t_prefix_spec ( t_prefix_spec (t_prefix_spec)? )?
	;

// R1228
prefix_spec
	:	declaration_type_spec
	|	t_prefix_spec
	;

t_prefix_spec returns [Token tk]
	:	T_RECURSIVE		{tk = $T_RECURSIVE;}
	|	T_PURE			{tk = $T_PURE;}
	|	T_ELEMENTAL		{tk = $T_ELEMENTAL;}
	;

// R1229
suffix
	:	proc_language_binding_spec ( T_RESULT T_LPAREN result_name T_RPAREN )?
	|	T_RESULT T_LPAREN result_name T_RPAREN ( proc_language_binding_spec )?
    ;

result_name
    :    name
    ;

// R1230
end_function_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	: (label {lbl=$label.tk;})? T_END T_FUNCTION ( T_IDENT )? end_of_stmt
	| (label {lbl=$label.tk;})? T_ENDFUNCTION    ( T_IDENT )? end_of_stmt
	| (label {lbl=$label.tk;})? T_END end_of_stmt
	;

// R1231
// specification_part made non-optional to remove END ambiguity (as can be empty)
subroutine_subprogram
	:	subroutine_stmt
		specification_part
		( execution_part )?
		( internal_subprogram_part )?
		end_subroutine_stmt
	;

// R1232
subroutine_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    :     (label {lbl=$label.tk;})? (t_prefix)? T_SUBROUTINE T_IDENT
          ( T_LPAREN ( dummy_arg_list )? T_RPAREN ( proc_language_binding_spec )? )? T_EOS
    ;

// R1233
// T_IDENT inlined for dummy_arg_name
dummy_arg
options{greedy=false; memoize=false;}
	:	T_IDENT		{ action.dummy_arg($T_IDENT); }
	|	T_ASTERISK	{ action.dummy_arg($T_ASTERISK); }
	;

dummy_arg_list
@init{ int count=0;}
    :  		{action.dummy_arg_list__begin();}
		dummy_arg {count++;} ( T_COMMA dummy_arg {count++;} )*
      		{action.dummy_arg_list(count);}
    ;

// R1234
end_subroutine_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    : (label {lbl=$label.tk;})? T_END T_SUBROUTINE ( T_IDENT )? end_of_stmt
    | (label {lbl=$label.tk;})? T_ENDSUBROUTINE    ( T_IDENT )? end_of_stmt
    | (label {lbl=$label.tk;})? T_END end_of_stmt
    ;

// R1235
// T_INDENT inlined for entry_name
entry_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
    :    (label {lbl=$label.tk;})? T_ENTRY T_IDENT
          ( T_LPAREN ( dummy_arg_list )? T_RPAREN ( suffix )? )? T_EOS
    ;

// R1236
// ERR_CHK 1236 scalar_int_expr replaced by expr
return_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_RETURN ( expr )? T_EOS
	;

// R1237 contains_stmt inlined as T_CONTAINS

// R1238
// ERR_CHK 1239 scalar_expr replaced by expr
// generic_name_list substituted for dummy_arg_name_list
// TODO Hopefully scanner and parser can help work together here to work around ambiguity.
// why can't this be accepted as an assignment statement and then the parser
// look up the symbol for the T_IDENT to see if it is a function??
//      Need scanner to send special token if it sees what?
// TODO - won't do a(b==3,c) = 2
stmt_function_stmt
@init{Token lbl = null;} // @init{INIT_TOKEN_NULL(lbl);}
	:	(label {lbl=$label.tk;})? T_STMT_FUNCTION T_IDENT T_LPAREN ( generic_name_list )? T_RPAREN T_EQUALS expr T_EOS
	;

// added this to have a way to match the T_EOS and EOF combinations
end_of_stmt
    : T_EOS
        // the (EOF) => EOF is done with lookahead because if it's not there, 
        // then antlr will crash with an internal error while trying to 
        // generate the java code.  (as of 12.11.06)
    | (EOF) => EOF
    ;
