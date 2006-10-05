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


grammar Fortran03Parser;

options {
    language=Java;
}

/*
 * Section 1:
 */

/*
 * Section 2:
 */

// R201
program
	:	program_unit
		( program_unit )*
	;

// R202
// backtracking needed to resolve prefix (e.g., REAL) ambiguity with main_program (REAL)
program_unit
options {backtrack=true;}
	:	main_program
	|	external_subprogram
	|	module
	|	block_data
	;

// R203
// modified to factor optional prefix
external_subprogram
	:	(prefix)? function_subprogram
	|	subroutine_subprogram
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
internal_subprogram_part
	:	T_CONTAINS T_EOS
		internal_subprogram
		( internal_subprogram )*
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
options {backtrack=true;}
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
action_stmt
options {backtrack=true;}
	:	allocate_stmt
	|	assignment_stmt
	|	backspace_stmt
	|	call_stmt
	|	close_stmt
	|	(label)? T_CONTINUE T_EOS
	|	cycle_stmt
	|	deallocate_stmt
	|	endfile_stmt
	|	exit_stmt
	|	flush_stmt
	|	forall_stmt
	|	goto_stmt
	|	if_stmt
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
name
	:	T_IDENT
	;

// R305
// T_IDENT inlined as named_constant 
constant
	:	literal_constant
	|	T_IDENT
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
	:	T_IDENT
	|	int_literal_constant
	;

// R309
// C303 R309 char_constant shall be of type character
// inlined character portion of constant
char_constant
	:	T_IDENT
	|	char_literal_constant
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
	:	T_DEFINED_OP
	|	extended_intrinsic_op
	;

// R312
extended_intrinsic_op
	:	intrinsic_operator
	;

// R313
// ERR_CHK 313 five characters or less
label
    : T_DIGIT_STRING
    ;

label_list
    :    label ( T_COMMA label )*
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
	:	expr
	|	T_ASTERISK
	|	T_COLON
	;

// inlined scalar_int_expr C101 shall be a scalar

// inlined scalar_expr

// R403
intrinsic_type_spec
	:	T_INTEGER ( kind_selector )?
	|	T_REAL ( kind_selector )?
	|	T_DOUBLE T_PRECISION
	|	T_DOUBLEPRECISION
	|	T_COMPLEX ( kind_selector )?
	|	T_CHARACTER ( char_selector )?
	|	T_LOGICAL ( kind_selector )?
	;

// R404
// ERR_CHK 404 scalar_int_initialization_expr replaced by expr
kind_selector
    : T_LPAREN (T_IDENT /* 'KIND' */ T_EQUALS)? expr T_RPAREN
    ;

// TODO: turn into terminal (what about kind parameter)
// R405
signed_int_literal_constant
	:	(T_PLUS|T_MINUS)? int_literal_constant
	;

// R406
int_literal_constant
	:	T_DIGIT_STRING (T_UNDERSCORE kind_param)?
	;

// R407
// T_IDENT inlined for scalar_int_constant_name
kind_param
	:	T_DIGIT_STRING
	|	T_IDENT
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
	:	(T_PLUS|T_MINUS)? real_literal_constant
	;

// R417 modified to use terminal
real_literal_constant
    :   REAL_CONSTANT ( T_UNDERSCORE kind_param )?
    |   DOUBLE_CONSTANT ( T_UNDERSCORE kind_param )?
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
	:	signed_int_literal_constant
	|	signed_real_literal_constant
	|	T_IDENT
	;

// R423
// ERR_CHK 423 named_constant replaced by T_IDENT
imag_part
	:	signed_int_literal_constant
	|	signed_real_literal_constant
	|	T_IDENT
	;

// R424
// ERR_CHK 424a scalar_int_initialization_expr replaced by expr
// ERR_CHK 424b match identifiers with 'KIND =' expr / 'LEN = ' type_param_value,
//              note, expr replaced by (isa) type_param_value
char_selector
	:	T_ASTERISK char_length (T_COMMA)?
	|	T_LPAREN T_IDENT /* {'LEN','KIND'} */ T_EQUALS type_param_value
                ( T_COMMA T_IDENT /* {'LEN','KIND'} */ T_EQUALS type_param_value )? T_RPAREN
	|	T_LPAREN type_param_value ( T_COMMA (T_IDENT /* 'KIND' */ T_EQUALS)? expr )? T_RPAREN
	;

// R425 length_selector inlined in (and combined with) R424

// R426
char_length
	:	T_LPAREN
		type_param_value
		T_RPAREN
	|	scalar_int_literal_constant
	;

scalar_int_literal_constant
	:	int_literal_constant
	;

// R427
char_literal_constant
options {k=2;}
	:	(T_DIGIT_STRING T_UNDERSCORE) => T_DIGIT_STRING T_UNDERSCORE T_CHAR_CONSTANT
	|	(T_IDENT T_UNDERSCORE) => T_IDENT T_UNDERSCORE T_CHAR_CONSTANT
	|	T_CHAR_CONSTANT
    ;

// R428
logical_literal_constant
	:	T_TRUE ( T_UNDERSCORE kind_param )?
	|	T_FALSE ( T_UNDERSCORE kind_param )?
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
type_param_or_comp_def_stmt_list
options {k=1;}
	:	(T_INTEGER) => (kind_selector)? T_COMMA type_param_or_comp_def_stmt
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
	:	T_TYPE ( ( T_COMMA type_attr_spec_list )? T_COLON_COLON )? T_IDENT
		( T_LPAREN generic_name_list T_RPAREN )? T_EOS
	;

type_attr_spec_list
	:	type_attr_spec ( T_COMMA type_attr_spec )*
	;

generic_name_list
	:	T_IDENT ( T_COMMA T_IDENT )*
	;

// R431
// T_IDENT inlined for parent_type_name
type_attr_spec
	:	access_spec
	|	T_EXTENDS T_LPAREN T_IDENT T_RPAREN
	|	T_ABSTRACT
	|	T_BIND_LPAREN_C T_RPAREN
	;

// R432
private_or_sequence
    :   private_components_stmt
    |   sequence_stmt
    ;

// R433
end_type_stmt
options {k=2;}
	: (T_END T_TYPE) => T_END T_TYPE ( T_IDENT )? T_EOS
	| T_ENDTYPE ( T_IDENT )? T_EOS
	;

// R434
sequence_stmt
	:	T_SEQUENCE T_EOS
	;

// R435 type_param_def_stmt inlined in type_param_or_comp_def_stmt_list

// R436
// ERR_CHK 436 scalar_int_initialization_expr replaced by expr
// T_IDENT inlined for type_param_name
type_param_decl
    :    T_IDENT ( T_EQUALS expr )?
    ;

type_param_decl_list
    :    type_param_decl ( T_COMMA type_param_decl )*
    ;

// R437
type_param_attr_spec
	:	T_KIND
	|	T_LEN
	;

// R438 component_part inlined as ( component_def_stmt )* in R429

// R439
component_def_stmt
	:	data_component_def_stmt
	|	proc_component_def_stmt
	;


// R440
data_component_def_stmt
    :    declaration_type_spec ( ( T_COMMA component_attr_spec_list )? T_COLON_COLON )? component_decl_list T_EOS
    ;

// R441
component_attr_spec
	:	T_POINTER
	|	T_DIMENSION T_LPAREN component_array_spec T_RPAREN
	|	T_ALLOCATABLE
	|	access_spec
	;

component_attr_spec_list
    :    component_attr_spec ( T_COMMA component_attr_spec )*
    ;

// R442
// T_IDENT inlined as component_name
component_decl
    :    T_IDENT ( T_LPAREN component_array_spec T_RPAREN )?
                 ( T_ASTERISK char_length )? ( component_initialization )?
    ;

component_decl_list
    :   component_decl ( T_COMMA component_decl )*
    ;

// R443
component_array_spec
	:	explicit_shape_spec_list
	|	deferred_shape_spec_list
	;

// deferred_shape_spec replaced by T_COLON
deferred_shape_spec_list
    :    T_COLON ( T_COMMA T_COLON )*
    ;

// R444
// ERR_CHK 444 initialization_expr replaced by expr
component_initialization
	:	T_EQUALS
		expr
	|	T_EQ_GT
		null_init
	;

// R445
proc_component_def_stmt
	:	T_PROCEDURE T_LPAREN ( proc_interface )? T_RPAREN T_COMMA
		    proc_component_attr_spec_list T_COLON_COLON proc_decl_list T_EOS
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
    :    proc_component_attr_spec ( T_COMMA proc_component_attr_spec )*
    ;

// R447
private_components_stmt
	:	T_PRIVATE T_EOS
	;

// R448
// T_CONTAINS inlined for contains_stmt
type_bound_procedure_part
	:	T_CONTAINS  T_EOS
        ( binding_private_stmt )? proc_binding_stmt ( proc_binding_stmt )*
	;

// R449
binding_private_stmt
	:	T_PRIVATE T_EOS
	;

// R450
proc_binding_stmt
	:	specific_binding T_EOS
	|	generic_binding T_EOS
	|	final_binding T_EOS
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
    | T_NOPASS
    | T_NON_OVERRIDABLE
    | T_DEFERRED
    | access_spec
    ;

binding_attr_list
    :    binding_attr ( T_COMMA binding_attr )*
    ;

// R454
// generic_name_list substituted for final_subroutine_name_list
final_binding
	:	T_FINAL ( T_COLON_COLON )? generic_name_list
	;

// R455
derived_type_spec
    : T_IDENT ( T_LPAREN type_param_spec_list T_RPAREN )?
    ;

// R456
type_param_spec
    : ( keyword T_EQUALS )? type_param_value
    ;

type_param_spec_list
    :    type_param_spec ( T_COMMA type_param_spec )*
    ;

// R457
// inlined derived_type_spec (R662) to remove ambiguity using backtracking
structure_constructor
options {backtrack=true;}
    : T_IDENT T_LPAREN type_param_spec_list T_RPAREN
		T_LPAREN
		( component_spec_list )?
		T_RPAREN
    | T_IDENT
		T_LPAREN
		( component_spec_list )?
		T_RPAREN
	;

// R458
component_spec
    :    ( keyword T_EQUALS )? component_data_source
    ;

component_spec_list
    :    component_spec ( T_COMMA component_spec )*
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
	:	T_ENUM T_COMMA T_BIND_LPAREN_C T_RPAREN T_EOS
	;

// R462
enumerator_def_stmt
	:	T_ENUMERATOR ( T_COLON_COLON )? enumerator_list T_EOS
	;

// R463
// ERR_CHK 463 scalar_int_initialization_expr replaced by expr
// ERR_CHK 463 named_constant replaced by T_IDENT
enumerator
    :    T_IDENT ( T_EQUALS expr )?
    ;

enumerator_list
    :   enumerator ( T_COMMA enumerator )*
    ;

// R464
end_enum_stmt
options {k=2;}
	:	(T_END T_ENUM) => T_END T_ENUM T_EOS
	|	T_ENDENUM T_EOS
	;

// R465
array_constructor
	:	T_LPAREN T_SLASH ac_spec T_SLASH T_RPAREN
	|	T_LBRACKET ac_spec T_RBRACKET
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
    :   ac_value ( T_COMMA ac_value )*
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
    :    declaration_type_spec ( ( T_COMMA attr_spec )* T_COLON_COLON )? entity_decl_list T_EOS
    ;

// R502
declaration_type_spec
	:	intrinsic_type_spec
	|	T_TYPE
		T_LPAREN
		derived_type_spec
		T_RPAREN
	|	T_CLASS
		T_LPAREN
		derived_type_spec
		T_RPAREN
	|	T_CLASS
		T_LPAREN
		T_ASTERISK
		T_RPAREN
	;

// R503
attr_spec
	:	access_spec
	|	T_ALLOCATABLE
	|	T_ASYNCHRONOUS
	|	T_DIMENSION T_LPAREN array_spec T_RPAREN
	|	T_EXTERNAL
	|	T_INTENT T_LPAREN intent_spec T_RPAREN
	|	T_INTRINSIC
	|	language_binding_spec
	|	T_OPTIONAL
	|	T_PARAMETER
	|	T_POINTER
	|	T_PROTECTED
	|	T_SAVE
	|	T_TARGET
	|	T_VALUE
	|	T_VOLATILE
	;


// R504
// T_IDENT inlined for object_name and function_name
// T_IDENT ( T_ASTERISK char_length )? (second alt) subsumed in first alt
entity_decl
    : T_IDENT ( T_LPAREN array_spec T_RPAREN )? ( T_ASTERISK char_length )? ( initialization )?
    ;

entity_decl_list
    :    entity_decl ( T_COMMA entity_decl )*
    ;

// R505 object_name inlined as T_IDENT

// R506
// ERR_CHK 506 initialization_expr replaced by expr
initialization
	:	T_EQUALS
		expr
	|	T_EQ_GT
		null_init
	;

// R507
// C506 The function-reference shall be a reference to the NULL intrinsic function with no arguments.
null_init
	:	T_IDENT /* 'NULL' */ T_LPAREN T_RPAREN
	;

// R508
access_spec
	:	T_PUBLIC
	|	T_PRIVATE
	;

// R509
// ERR_CHK 509 scalar_char_initialization_expr replaced by expr
language_binding_spec
    : T_BIND_LPAREN_C ( T_COMMA name T_EQUALS expr )? T_RPAREN
    ;

// R510
// ERR_CHK 510 T_ASTERISK must not appear in any but the last dimension
// array-spec grammar was changed to make it easier to parse (less ambiguous)
array_spec
	:	T_COLON ( T_COMMA array_spec )?			// assumed shape
	|	T_ASTERISK ( T_COMMA array_spec )?		// assumed_size
	|	expr (upper_bound_spec)? ( T_COMMA array_spec )?// explicit shape (if no upper_spec)
	;

upper_bound_spec
	:	T_COLON			// assumed shape
	|	T_COLON T_ASTERISK	// assumed size
	|	T_COLON expr		// explicit shape
	;	

// R511
// refactored to remove conditional from lhs and inlined lower_bound and upper_bound
explicit_shape_spec
    : expr ( T_COLON expr )?
    ;

explicit_shape_spec_list
    : explicit_shape_spec ( T_COMMA explicit_shape_spec )*
    ;

// R512 lower_bound was specification_expr inlined as expr

// R513 upper_bound was specification_expr inlined as expr

// R514 assumed_shape_spec was ( lower_bound )? T_COLON not used in R510 array_spec

// R515 deferred_shape_spec inlined as T_COLON in deferred_shape_spec_list

// R516
// ERR_CHK R516 last dimension of array_spec must be ( T_ASTERISK | expr T_COLON T_ASTERISK )
assumed_size_spec
    : array_spec
    ;

// R517
intent_spec
options {k=2;}
	:	T_IN
	|	T_OUT
	|	(T_IN T_OUT) => T_IN T_OUT
	|	T_INOUT
	;

// R518
access_stmt
    :    access_spec ( ( T_COLON_COLON )? access_id_list )? T_EOS
    ;

// R519
// T_IDENT inlined for use_name
// generic_spec can be T_IDENT so T_IDENT deleted
access_id
	:	generic_spec
	;

access_id_list
    :    access_id ( T_COMMA access_id )*
    ;

// R520
// T_IDENT inlined for object_name
allocatable_stmt
    : T_ALLOCATABLE ( T_COLON_COLON )? T_IDENT ( T_LPAREN deferred_shape_spec_list T_RPAREN )?
         ( T_COMMA T_IDENT ( T_LPAREN deferred_shape_spec_list T_RPAREN )? )* T_EOS
    ;

// R521
// generic_name_list substituted for object_name_list
asynchronous_stmt
	:	T_ASYNCHRONOUS
		( T_COLON_COLON )?
		generic_name_list T_EOS
	;

// R522
bind_stmt
	:	language_binding_spec
		( T_COLON_COLON )?
		bind_entity_list T_EOS
	;

// R523
// T_IDENT inlined for entity_name and common_block_name
bind_entity
	:	T_IDENT
	|	T_SLASH T_IDENT T_SLASH
	;

bind_entity_list
    :    bind_entity ( T_COMMA bind_entity )*
    ;

// R524
data_stmt
    :    T_DATA data_stmt_set ( ( T_COMMA )? data_stmt_set )* T_EOS
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
    :    data_stmt_object ( T_COMMA data_stmt_object )*
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
    :   data_i_do_object ( T_COMMA data_i_do_object )*
    ;

// R529 data_i_do_variable was scalar_int_variable inlined as T_IDENT
// C556 (R529) The data-i-do-variable shall be a named variable.

// R530
// ERR_CHK R530 designator is scalar-constant or integer constant when followed by '*'
// data_stmt_repeat inlined from R531
// structure_constructure covers null_init if 'NULL()' so null_init deleted
data_stmt_value
options {backtrack=true;}
	:	designator (T_ASTERISK data_stmt_constant)?
	|	int_literal_constant (T_ASTERISK data_stmt_constant)?
	|	( T_PLUS | T_MINUS ) int_literal_constant
	|	signed_real_literal_constant
	|	complex_literal_constant
	|	logical_literal_constant
	|	char_literal_constant
	|	boz_literal_constant
	|	structure_constructor // is null_init if 'NULL()'
    ;

data_stmt_value_list
    :    data_stmt_value ( T_COMMA data_stmt_value )*
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
data_stmt_constant
options {backtrack=true;}
	:	designator
	|	signed_int_literal_constant
	|	signed_real_literal_constant
	|	complex_literal_constant
	|	logical_literal_constant
	|	char_literal_constant
	|	boz_literal_constant
	|	structure_constructor // is null_init if 'NULL()'
	;

// R533 int_constant_subobject was constant_subobject inlined as designator in R531

// R534 constant_subobject inlined as designator in R533
// C566 (R534) constant-subobject shall be a subobject of a constant.

// R535
// array_name replaced by T_IDENT
dimension_stmt
    :    T_DIMENSION ( T_COLON_COLON )? T_IDENT T_LPAREN array_spec T_RPAREN
             ( T_COMMA T_IDENT T_LPAREN array_spec T_RPAREN )* T_EOS
    ;

// R536
// generic_name_list substituted for dummy_arg_name_list
intent_stmt
	:	T_INTENT T_LPAREN intent_spec T_RPAREN ( T_COLON_COLON )? generic_name_list T_EOS
	;

// R537
// generic_name_list substituted for dummy_arg_name_list
optional_stmt
	:	T_OPTIONAL ( T_COLON_COLON )? generic_name_list T_EOS
	;

// R538
parameter_stmt
	:	T_PARAMETER T_LPAREN named_constant_def_list T_RPAREN T_EOS
	;

named_constant_def_list
    :    named_constant_def ( T_COMMA named_constant_def )*
    ;

// R539
// ERR_CHK 539 initialization_expr replaced by expr
// ERR_CHK 539 named_constant replaced by T_IDENT
named_constant_def
	:	T_IDENT T_EQUALS expr
	;

// R540
pointer_stmt
	:	T_POINTER ( T_COLON_COLON )? pointer_decl_list T_EOS
	;

pointer_decl_list
    :    pointer_decl ( T_COMMA pointer_decl )*
    ;

// R541
// T_IDENT inlined as object_name and proc_entity_name (removing second alt)
pointer_decl
    :    T_IDENT ( T_LPAREN deferred_shape_spec_list T_RPAREN )?
    ;

// R542
// generic_name_list substituted for entity_name_list
protected_stmt
	:	T_PROTECTED ( T_COLON_COLON )? generic_name_list T_EOS
	;

// R543
save_stmt
    : T_SAVE ( ( T_COLON_COLON )? saved_entity_list )? T_EOS
    ;

// R544
// T_IDENT inlined for object_name, proc_pointer_name (removing second alt), and common_block_name
saved_entity
	:	T_IDENT
	|	T_SLASH T_IDENT T_SLASH
	;

saved_entity_list
    :    saved_entity ( T_COMMA saved_entity )*
    ;

// R545 proc_pointer_name was name inlined as T_IDENT

// R546
// T_IDENT inlined for object_name
target_stmt
    : T_TARGET ( T_COLON_COLON )? T_IDENT ( T_LPAREN array_spec T_RPAREN )?
             ( T_COMMA T_IDENT ( T_LPAREN array_spec T_RPAREN )? )* T_EOS
    ;

// R547
// generic_name_list substituted for dummy_arg_name_list
value_stmt
	:	T_VALUE ( T_COLON_COLON )? generic_name_list T_EOS
	;

// R548
// generic_name_list substituted for object_name_list
volatile_stmt
	:	T_VOLATILE ( T_COLON_COLON )? generic_name_list T_EOS
	;

// R549
implicit_stmt
	:	T_IMPLICIT implicit_spec_list T_EOS
	|	T_IMPLICIT T_NONE T_EOS
	;

// R550
implicit_spec
	:	declaration_type_spec T_LPAREN letter_spec_list T_RPAREN
	;

implicit_spec_list
    :    implicit_spec ( T_COMMA implicit_spec )*
    ;

// R551
letter_spec 
    : Letter ( T_MINUS Letter )?
    ;

letter_spec_list
    :    letter_spec ( T_COMMA letter_spec )*
    ;

// R552
// T_IDENT inlined for namelist_group_name
namelist_stmt
    : T_NAMELIST T_SLASH T_IDENT T_SLASH namelist_group_object_list
         ( ( T_COMMA )? T_SLASH T_IDENT T_SLASH namelist_group_object_list )* T_EOS
    ;

// R553 namelist_group_object was variable_name inlined as T_IDENT

// T_IDENT inlined for namelist_group_object
namelist_group_object_list
    :    T_IDENT ( T_COMMA T_IDENT )*
    ;

// R554
equivalence_stmt
	:	T_EQUIVALENCE equivalence_set_list T_EOS
	;

// R555
equivalence_set
	:	T_LPAREN equivalence_object T_COMMA equivalence_object_list T_RPAREN
	;

equivalence_set_list
    :    equivalence_set ( T_COMMA equivalence_set )*
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
    :    equivalence_object ( T_COMMA equivalence_object )
    ;

// R557
// T_IDENT inlined for common_block_name
common_stmt
    : T_COMMON ( T_SLASH ( T_IDENT )? T_SLASH )? common_block_object_list
         ( ( T_COMMA )? T_SLASH ( T_IDENT )? T_SLASH common_block_object_list )* T_EOS
    ;

// R558
// T_IDENT inlined for variable_name and proc_pointer_name
// T_IDENT covered by first alt so second deleted
common_block_object
    : T_IDENT ( T_LPAREN explicit_shape_spec_list T_RPAREN )?
    ;

common_block_object_list
    :    common_block_object ( T_COMMA common_block_object )*
    ;

/*
Section 6:
 */

// R601
variable
	:	designator
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
options {backtrack=true;}
	:	data_ref (T_LPAREN substring_range T_RPAREN)?
	|	char_literal_constant T_LPAREN substring_range T_RPAREN
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
	:	data_ref (T_LPAREN substring_range T_RPAREN)?
	|	char_literal_constant T_LPAREN substring_range T_RPAREN
	;

// R610 parent_string inlined in R609 as (data_ref | char_literal_constant)
// T_IDENT inlined for scalar_variable_name
// data_ref inlined for scalar_structure_component and array_element
// data_ref isa T_IDENT so T_IDENT deleted
// scalar_constant replaced by char_literal_constant as data_ref isa T_IDENT and must be character

// R611
// ERR_CHK 611 scalar_int_expr replaced by expr
substring_range
	:	( expr )?
		T_COLON
		( expr )?
	;

// R612
data_ref
options {backtrack=true;}
	:	part_ref ( T_PERCENT part_ref )*
	;

// R613
// T_IDENT inlined for part_name
// with k=2, this path is chosen over T_LPAREN substring_range T_RPAREN
part_ref
options {k=2;}
	:	 (T_IDENT T_LPAREN) => T_IDENT T_LPAREN section_subscript_list T_RPAREN
	|	 T_IDENT
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
section_subscript
	:	expr ( T_COLON ( expr )? ( T_COLON expr )? )?
	|	T_COLON ( expr )? ( T_COLON expr )?
	;

section_subscript_list
    :    section_subscript ( T_COMMA section_subscript )*
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

// R623
allocate_stmt
options {backtrack=true;}
    :    T_ALLOCATE T_LPAREN type_spec T_COLON_COLON allocation_list ( T_COMMA alloc_opt_list )? T_RPAREN T_EOS
    |    T_ALLOCATE T_LPAREN allocation_list ( T_COMMA alloc_opt_list )? T_RPAREN T_EOS
    ;

// R624
// ERR_CHK 624 source_expr replaced by expr
// stat_variable and errmsg_variable replaced by designator
alloc_opt
	:	T_IDENT /* {'STAT','ERRMSG'} are variables {SOURCE'} is expr */ T_EQUALS expr
	;

alloc_opt_list
    :    alloc_opt ( T_COMMA alloc_opt )*
    ;

// R625 stat_variable was scalar_int_variable inlined in R624 and R636

// R626 errmsg_variable was scalar_default_char_variable inlined in R624 and R636

// R627 inlined source_expr was expr

// R628
allocation
    : allocate_object ( T_LPAREN allocate_shape_spec_list T_RPAREN )?
    ;

allocation_list
    :    allocation ( T_COMMA allocation )*
    ;

// R629
// T_IDENT inlined for variable_name
// data_ref inlined for structure_component
// data_ref isa T_IDENT so T_IDENT deleted
allocate_object
	:	data_ref
	;

allocate_object_list
    :    allocate_object ( T_COMMA allocate_object )*
    ;

// R630
// ERR_CHK 630a lower_bound_expr replaced by expr
// ERR_CHK 630b upper_bound_expr replaced by expr
allocate_shape_spec
    :    expr ( T_COLON expr )?
    ;

allocate_shape_spec_list
    :    allocate_shape_spec ( T_COMMA allocate_shape_spec )*
    ;

// R631 inlined lower_bound_expr was scalar_int_expr

// R632 inlined upper_bound_expr was scalar_int_expr

// R633
nullify_stmt
	:	T_NULLIFY
		T_LPAREN
		pointer_object_list
		T_RPAREN T_EOS
	;

// R634
// T_IDENT inlined for variable_name and proc_pointer_name
// data_ref inlined for structure_component
// data_ref can be a T_IDENT so T_IDENT deleted
pointer_object
	:	data_ref
	;

pointer_object_list
    :    pointer_object ( T_COMMA pointer_object )*
    ;

// R635
deallocate_stmt
    :    T_DEALLOCATE T_LPAREN allocate_object_list ( T_COMMA dealloc_opt_list )? T_RPAREN T_EOS
    ;

// R636
// stat_variable and errmsg_variable replaced by designator
dealloc_opt
	:	T_IDENT /* {'STAT','ERRMSG'} */ T_EQUALS designator
	;

dealloc_opt_list
    :    dealloc_opt ( T_COMMA dealloc_opt )*
    ;


/*
 * Section 7:
 */

// R701
// constant replaced by literal_constant as T_IDENT can be designator
// T_IDENT inlined for type_param_name
// data_ref in designator can be a T_IDENT so T_IDENT deleted
// type_param_inquiry is designator T_PERCENT T_IDENT can be designator so deleted
primary
options {backtrack=true;}
	:	literal_constant
	|	designator
	|	array_constructor
	|	structure_constructor
	|	function_reference
	|	T_LPAREN expr T_RPAREN
	;

// R702
level_1_expr
    : ( defined_unary_op )? primary
    ;

// R703
defined_unary_op
	:	T_DEFINED_OP
	;

// inserted as R704 functionality
power_operand
	: level_1_expr ( power_op power_operand )?
	;	

// R704
// see power_operand
mult_operand
//    : level_1_expr ( power_op mult_operand )?
//    : power_operand
    : power_operand ( mult_op power_operand )*
    ;

// R705
// moved leading optionals to mult_operand
add_operand
//    : ( add_operand mult_op )? mult_operand
//    : ( mult_operand mult_op )* mult_operand
    : (add_op)? mult_operand ( add_op mult_operand )*
    ;

// R706
// moved leading optionals to add_operand
level_2_expr
//    : ( ( level_2_expr )? add_op )? add_operand
// check notes on how to remove this left recursion  (WARNING something like the following)
//    : (add_op)? ( add_operand add_op )* add_operand
    : add_operand ( concat_op add_operand )*
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
// moved leading optional to level_2_expr
level_3_expr
//    : ( level_3_expr concat_op )? level_2_expr
//    : ( level_2_expr concat_op )* level_2_expr
    : level_2_expr ( rel_op level_2_expr )?
    ;

// R711
concat_op
	:	T_SLASH_SLASH
	;

// R712
// moved leading optional to level_3_expr
level_4_expr
//    : ( level_3_expr rel_op )? level_3_expr
    : level_3_expr
    ;

// R713
rel_op
	:	T_EQ
	|	T_NE
	|	T_LT
	|	T_LE
	|	T_GT
	|	T_GE
	|	T_EQ_EQ
	|	T_SLASH_EQ
	|	T_LESSTHAN
	|	T_LESSTHAN_EQ
	|	T_GREATERTHAN
	|	T_GREATERTHAN_EQ
	;

// R714
and_operand
//    :    ( not_op )? level_4_expr
    :    ( not_op )? level_4_expr ( and_op level_4_expr )*
    ;

// R715
// moved leading optional to or_operand
or_operand
//    : ( or_operand and_op )? and_operand
//    : ( and_operand and_op )* and_operand
    : and_operand ( or_op and_operand )*
    ;

// R716
// moved leading optional to or_operand
equiv_operand
//    : ( equiv_operand or_op )? or_operand
//    : ( or_operand or_op )* or_operand
    : or_operand ( equiv_op or_operand )*
    ;

// R717
// moved leading optional to equiv_operand
level_5_expr
//    : ( level_5_expr equiv_op )? equiv_operand
//    : ( equiv_operand equiv_op )* equiv_operand
    : equiv_operand ( defined_binary_op equiv_operand )*
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
	|	T_NEQV
	;

// R722
// moved leading optional to level_5_expr
expr
//    : ( expr defined_binary_op )? level_5_expr
//    : ( level_5_expr defined_binary_op )* level_5_expr
    : level_5_expr
    ;

// R723
defined_binary_op
	:	T_DEFINED_OP
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
	:	variable
		T_EQUALS
		expr T_EOS
	;

// R735
// ERR_TEST 735 ensure that part_ref in data_ref doesn't capture the T_LPAREN
// data_pointer_object and proc_pointer_object replaced by designator
// data_target and proc_target replaced by expr
// third alt covered by first alt so proc_pointer_object assignment deleted
// designator (R603), minus the substring part is data_ref, so designator replaced by data_ref,
// see NOTE 6.10 for why array-section does not have pointer attribute
pointer_assignment_stmt
options {backtrack=true;}
    : data_ref T_EQ_GT expr T_EOS
    | data_ref T_LPAREN bounds_spec_list T_RPAREN T_EQ_GT expr T_EOS
    | data_ref T_LPAREN bounds_remapping_list T_RPAREN T_EQ_GT expr T_EOS
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
	:	expr
		T_COLON
	;

bounds_spec_list
    :    bounds_spec ( T_COMMA bounds_spec )*
    ;

// R738
// ERR_CHK 738a lower_bound_expr replaced by expr
// ERR_CHK 738b upper_bound_expr replaced by expr
bounds_remapping
	:	expr
		T_COLON
		expr
	;

bounds_remapping_list
    :    bounds_remapping ( T_COMMA bounds_remapping )*
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
	:	T_WHERE
		T_LPAREN
		expr
		T_RPAREN
		assignment_stmt
	;

// R744
where_construct
    :    where_construct_stmt ( where_body_construct )* ( masked_elsewhere_stmt
          ( where_body_construct )* )* ( elsewhere_stmt ( where_body_construct )* )? end_where_stmt
    ;

// R745
// ERR_CHK 745 mask_expr replaced by expr
where_construct_stmt
    :    ( T_IDENT T_COLON )? T_WHERE T_LPAREN expr T_RPAREN T_EOS
    ;

// R746
// assignment_stmt inlined for where_assignment_stmt
where_body_construct
options {backtrack=true;}
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
options {k=2;}
	:	(T_ELSE T_WHERE) => T_ELSE T_WHERE
		T_LPAREN	expr T_RPAREN ( T_IDENT )? T_EOS
	|	(T_ELSEWHERE) => T_ELSEWHERE
		T_LPAREN	expr T_RPAREN ( T_IDENT )? T_EOS
	;

// R750
elsewhere_stmt
options {k=2;}
	:	(T_ELSE T_WHERE) => T_ELSE T_WHERE
		( T_IDENT )? T_EOS
	|	(T_ELSEWHERE) => T_ELSEWHERE
		( T_IDENT )? T_EOS
	;

// R751
end_where_stmt
options {k=2;}
	: (T_END T_WHERE) => T_END T_WHERE ( T_IDENT )? T_EOS
	| T_ENDWHERE ( T_IDENT )? T_EOS
	;

// R752
forall_construct
	:	forall_construct_stmt
		( forall_body_construct )*
		end_forall_stmt
	;

// R753
forall_construct_stmt
    :    ( T_IDENT T_COLON )? T_FORALL forall_header T_EOS
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
    :    forall_triplet_spec ( T_COMMA forall_triplet_spec )*
    ;

// R756
forall_body_construct
options {backtrack=true;}
	:	forall_assignment_stmt
	|	where_stmt
	|	where_construct
	|	forall_construct
	|	forall_stmt
	;

// R757
forall_assignment_stmt
options {backtrack=true;}
	:	assignment_stmt
	|	pointer_assignment_stmt
	;

// R758
end_forall_stmt
options {k=2;}
	: (T_END T_FORALL) => T_END T_FORALL ( T_IDENT )? T_EOS
	| T_ENDFORALL ( T_IDENT )? T_EOS
	;

// R759
forall_stmt
	:	T_FORALL
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
    : ( T_IDENT T_COLON )? T_IF T_LPAREN expr T_RPAREN T_THEN T_EOS
    ;

// R804
// ERR_CHK 804 scalar_logical_expr replaced by expr
else_if_stmt
options {k=2;}
	: (T_ELSE T_IF) => T_ELSE T_IF
        T_LPAREN expr T_RPAREN T_THEN ( T_IDENT )? T_EOS
	| T_ELSEIF
        T_LPAREN expr T_RPAREN T_THEN ( T_IDENT )? T_EOS
	;

// R805
else_stmt
	:	T_ELSE
		( T_IDENT )? T_EOS
	;

// R806
end_if_stmt
options {k=2;}
	: (T_END T_IF) => T_END T_IF ( T_IDENT )? T_EOS
	| T_ENDIF ( T_IDENT )? T_EOS
	;

// R807
// ERR_CHK 807 scalar_logical_expr replaced by expr
if_stmt
	:	T_IF
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
    :    ( T_IDENT T_COLON )?
        t_select_case
        T_LPAREN expr T_RPAREN T_EOS
    ;

t_select_case
options {k=2;}
    : (T_SELECT T_CASE) => T_SELECT T_CASE
    | T_SELECTCASE
    ;

// R810
case_stmt
	:	T_CASE
		case_selector
		( T_IDENT )? T_EOS
	;

// R811
end_select_stmt
options {k=2;}
	: (T_END T_SELECT) => T_END T_SELECT T_IDENT T_EOS
	| T_ENDSELECT T_IDENT T_EOS
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
    :    case_value_range ( T_COMMA case_value_range )*
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
    : ( T_IDENT T_COLON )? T_ASSOCIATE T_LPAREN association_list T_RPAREN T_EOS
    ;

association_list
    :    association ( T_COMMA association )*
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
options {k=2;}
	: (T_END T_ASSOCIATE) => T_END T_ASSOCIATE ( T_IDENT )? T_EOS
	| T_ENDASSOCIATE ( T_IDENT )? T_EOS
	;

// R821
select_type_construct
    :    select_type_stmt ( type_guard_stmt block )* end_select_type_stmt
    ;

// R822
// T_IDENT inlined for select_construct_name and associate_name
select_type_stmt
    : ( T_IDENT T_COLON )? select_type
         T_LPAREN ( T_IDENT T_EQ_GT )? selector T_RPAREN T_EOS
    ;

select_type
options {k=2;}
    : (T_SELECT T_TYPE) => T_SELECT T_TYPE
    | T_SELECTTYPE
    ;

// R823
// T_IDENT inlined for select_construct_name
type_guard_stmt
	:	T_TYPE_IS T_LPAREN
		type_spec
		T_RPAREN
		( T_IDENT )? T_EOS
	|	T_CLASS_IS T_LPAREN
		type_spec
		T_RPAREN
		( T_IDENT )? T_EOS
	|	T_CLASS	T_DEFAULT
		( T_IDENT )? T_EOS
	;

// R824
// T_IDENT inlined for select_construct_name
end_select_type_stmt
options {k=2;}
	:	(T_END T_SELECT) => T_END T_SELECT ( T_IDENT )? T_EOS
	|	T_ENDSELECT ( T_IDENT )? T_EOS
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
	:	( T_IDENT T_COLON )? T_DO ( T_DIGIT_STRING )? ( loop_control )? T_EOS
	;

// R828
// T_IDENT inlined for do_construct_name
// T_DIGIT_STRING inlined for label
label_do_stmt
	:	( T_IDENT T_COLON )? T_DO T_DIGIT_STRING ( loop_control )? T_EOS
	;

// R829 inlined in R827
// T_IDENT inlined for do_construct_name

// R830
// ERR_CHK 830a scalar_int_expr replaced by expr
// ERR_CHK 830b scalar_logical_expr replaced by expr
loop_control
    : ( T_COMMA )? do_variable T_EQUALS expr T_COMMA expr ( T_COMMA expr )?
    | ( T_COMMA )? T_WHILE T_LPAREN expr T_RPAREN
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
	:	(label)? end_do_stmt
//	|	(label)? T_CONTINUE T_EOS
	|	do_term_action_stmt
	;

// R834
// T_IDENT inlined for do_construct_name
end_do_stmt
options {k=2;}
	: (T_END T_DO) => T_END T_DO ( T_IDENT )? T_EOS
	| T_ENDDO ( T_IDENT )? T_EOS
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
	:	T_LABEL_DO_TERMINAL action_stmt
	;

// R839 outer_shared_do_construct removed because it caused ambiguity in R835 (see comment in R835)

// R840 shared_term_do_construct deleted (see comments for R839 and R835)

// R841 inner_shared_do_construct deleted (see comments for R839 and R835)

// R842 do_term_shared_stmt deleted (see comments for R839 and R835)

// R843
// T_IDENT inlined for do_construct_name
cycle_stmt
	:	T_CYCLE ( T_IDENT )? T_EOS
	;

// R844
// T_IDENT inlined for do_construct_name
exit_stmt
	:	T_EXIT ( T_IDENT )? T_EOS
	;

// R845
goto_stmt
	:	t_go_to label T_EOS
	;

// R846
// ERR_CHK 846 scalar_int_expr replaced by expr
computed_goto_stmt
	:	t_go_to T_LPAREN label_list T_RPAREN ( T_COMMA )? expr T_EOS
	;

t_go_to
options {k=2;}
	: (T_GO T_TO) => T_GO T_TO
	| T_GOTO
	;



// R847
// ERR_CHK 847 scalar_numeric_expr replaced by expr
arithmetic_if_stmt
	:	T_IF
		T_LPAREN
		expr
		T_RPAREN
		label
		T_COMMA
		label
		T_COMMA
		label T_EOS
	;

// R848 continue_stmt inlined as T_CONTINUE

// R849
stop_stmt
	:	T_STOP ( stop_code )? T_EOS
	;

// R850
stop_code
    : scalar_char_constant
    | Digit ( Digit ( Digit ( Digit ( Digit )? )? )? )?
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
	:	T_OPEN T_LPAREN connect_spec_list T_RPAREN T_EOS
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
    :    connect_spec ( T_COMMA connect_spec )*
    ;

// inlined scalar_default_char_expr

// R906 inlined file_name_expr with expr was scalar_default_char_expr

// R907 iomsg_variable inlined as scalar_default_char_variable in R905,R909,R913,R922,R926,R928

// R908
close_stmt
	:	T_CLOSE T_LPAREN close_spec_list T_RPAREN T_EOS
	;

// R909
// file_unit_number, scalar_int_variable, iomsg_variable, label replaced by expr
close_spec
	:	expr
	|	T_IDENT /* {'UNIT','IOSTAT','IOMSG','ERR','STATUS'} */ T_EQUALS expr
	;

close_spec_list
	:	close_spec ( T_COMMA close_spec )*
	;

// R910
read_stmt
options {k=2;}
    :    (T_READ T_LPAREN) => T_READ T_LPAREN io_control_spec_list T_RPAREN ( input_item_list )? T_EOS
    |    (T_READ) => T_READ format ( T_COMMA input_item_list )? T_EOS
    ;

// R911
write_stmt
	:	T_WRITE T_LPAREN io_control_spec_list T_RPAREN ( output_item_list )? T_EOS
	;

// R912
print_stmt
    :    T_PRINT format ( T_COMMA output_item_list )? T_EOS
    ;

// R913
// ERR_CHK 913 check expr type with identifier
// io_unit and format are both (expr|'*') so combined
io_control_spec
        :	expr
        |	T_ASTERISK
        |	T_IDENT /* {'UNIT','FMT'} */ T_EQUALS T_ASTERISK
        |	T_IDENT
		    /* {'UNIT','FMT'} are expr 'NML' is T_IDENT} */
		    /* {'ADVANCE','ASYNCHRONOUS','BLANK','DECIMAL','DELIM'} are expr */
		    /* {'END','EOR','ERR'} are labels */
		    /* {'ID','IOMSG',IOSTAT','SIZE'} are variables */
		    /* {'PAD','POS','REC','ROUND','SIGN'} are expr */
		T_EQUALS expr
    ;

io_control_spec_list
    :    io_control_spec ( T_COMMA io_control_spec )*
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
	:	input_item ( T_COMMA input_item )*
	;

// R916
output_item
options {backtrack=true;}
	:	expr
	|	io_implied_do
	;

output_item_list
    :    output_item ( T_COMMA output_item )*
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
	:	T_WAIT T_LPAREN wait_spec_list T_RPAREN T_EOS
	;

// R922
// file_unit_number, scalar_int_variable, iomsg_variable, label replaced by expr
wait_spec
	:	expr
	|	T_IDENT /* {'UNIT','END','EOR','ERR','ID','IOMSG','IOSTAT'} */ T_EQUALS expr
	;

wait_spec_list
	:	wait_spec ( T_COMMA wait_spec )*
	;

// R923
backspace_stmt
options {k=2;}
	:	(T_BACKSPACE T_LPAREN) => T_BACKSPACE T_LPAREN position_spec_list T_RPAREN T_EOS
	|	(T_BACKSPACE) => T_BACKSPACE file_unit_number T_EOS
	;

// R924
endfile_stmt
options {k=3;}
	:	(T_END T_FILE T_LPAREN) => T_END T_FILE T_LPAREN position_spec_list T_RPAREN T_EOS
	|	(T_ENDFILE T_LPAREN) => T_ENDFILE T_LPAREN position_spec_list T_RPAREN T_EOS
	|	(T_END T_FILE) => T_END T_FILE file_unit_number T_EOS
	|	(T_ENDFILE) => T_ENDFILE file_unit_number T_EOS
	;

// R925
rewind_stmt
options {k=2;}
	:	(T_REWIND T_LPAREN) => T_REWIND T_LPAREN position_spec_list T_RPAREN T_EOS
	|	(T_REWIND) => T_REWIND file_unit_number T_EOS
	;

// R926
// file_unit_number, scalar_int_variable, iomsg_variable, label replaced by expr
position_spec
	:	expr
	|	T_IDENT /* {'UNIT','IOSTAT','IOMSG','ERR'} */ T_EQUALS expr
    ;

position_spec_list
	:	position_spec ( T_COMMA position_spec )*
	;

// R927
flush_stmt
options {k=2;}
	:	(T_FLUSH T_LPAREN) => T_FLUSH T_LPAREN flush_spec_list T_RPAREN T_EOS
	|	(T_FLUSH) => T_FLUSH file_unit_number T_EOS
	;

// R928
// file_unit_number, scalar_int_variable, iomsg_variable, label replaced by expr
flush_spec
	:	expr
	|	T_IDENT /* {'UNIT','IOSTAT','IOMSG','ERR'} */ T_EQUALS expr
    ;

flush_spec_list
    :    flush_spec ( T_COMMA flush_spec )*
    ;

// R929
inquire_stmt
options {backtrack=true;}
	:	T_INQUIRE T_LPAREN inquire_spec_list T_RPAREN T_EOS
	|	T_INQUIRE T_LPAREN T_IDENT /* 'IOLENGTH' */ T_EQUALS scalar_int_variable T_RPAREN output_item_list T_EOS
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
    :    inquire_spec ( T_COMMA inquire_spec )*
    ;

/*
Section 10:
 */

// R1001
format_stmt
	:	T_FORMAT format_specification T_EOS
	;

// R1002
format_specification
	:	T_LPAREN ( format_item_list )? T_RPAREN
	;

// R1003
// r replaced by int_literal_constant replaced by char_literal_constant replaced by T_CHAR_CONSTANT
// char_string_edit_desc replaced by T_CHAR_CONSTANT
format_item
	:	T_DIGIT_STRING data_edit_desc
	|	data_plus_control_edit_desc
	|	T_CHAR_CONSTANT
	|	(T_DIGIT_STRING)? T_LPAREN format_item_list T_RPAREN
	;

format_item_list
    :    format_item ( T_COMMA format_item )*
    ;

// R1004 r inlined in R1003 and R1011 as int_literal_constant (then as DIGIT_STRING)
// C1004 (R1004) r shall not have a kind parameter associated with it

// R1005
// w,m,d,e replaced by int_literal_constant replaced by T_DIGIT_STRING
// char_literal_constant replaced by T_CHAR_CONSTANT
// ERR_CHK 1005 matching T_ID_OR_OTHER with alternatives will have to be done here
data_edit_desc
    : T_ID_OR_OTHER /* {'I','B','O','Z','F','E','EN','ES','G','L','A','D'} */ 
      T_DIGIT_STRING ( T_PERIOD T_DIGIT_STRING )?
      ( T_ID_OR_OTHER /* is 'E' */ T_DIGIT_STRING )?
    | T_ID_OR_OTHER /* is 'DT' */ T_CHAR_CONSTANT ( T_LPAREN v_list T_RPAREN )?
    | T_ID_OR_OTHER /* {'A','DT'},{'X','P' from control_edit_desc} */
    ;

data_plus_control_edit_desc
	:	T_ID_OR_OTHER /* {'I','B','O','Z','F','E','EN','ES','G','L','A','D'},{T','TL','TR'} */ 
		    T_DIGIT_STRING ( T_PERIOD T_DIGIT_STRING )?
		    ( T_ID_OR_OTHER /* is 'E' */ T_DIGIT_STRING )?
	|	T_ID_OR_OTHER /* is 'DT' */ T_CHAR_CONSTANT ( T_LPAREN v_list T_RPAREN )?
	|	T_ID_OR_OTHER /* {'A','DT'},{'BN','BZ','RU','RD','RZ','RN','RC','RP','DC','DP'} */
// following only from control_edit_desc
	|	( T_DIGIT_STRING )? T_SLASH
	|	T_COLON
	|	(T_PLUS|T_MINUS) T_DIGIT_STRING T_ID_OR_OTHER /* is 'P' */
	;

// R1006 w inlined in R1005 as int_literal_constant replaced by T_DIGIT_STRING

// R1007 m inlined in R1005 as int_literal_constant replaced by T_DIGIT_STRING

// R1008 d inlined in R1005 as int_literal_constant replaced by T_DIGIT_STRING

// R1009 e inlined in R1005 as int_literal_constant replaced by T_DIGIT_STRING

// R1010 v inlined as signed_int_literal_constant in v_list replaced by (T_PLUS or T_MINUS) T_DIGIT_STRING

v_list
    :   (T_PLUS|T_MINUS)? T_DIGIT_STRING ( T_COMMA (T_PLUS|T_MINUS)? T_DIGIT_STRING )*
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

// R1101
// specification_part made non-optional to remove END ambiguity (as can be empty)
main_program
	:	( program_stmt )?
		specification_part
		( execution_part )?
		( internal_subprogram_part )?
		end_program_stmt
	;

// R1102
// T_IDENT inlined for program_name
program_stmt
	:	T_PROGRAM
		T_IDENT T_EOS
	;

// R1103
// T_IDENT inlined for program_name
end_program_stmt
options {k=2;}
	:	(T_END T_PROGRAM) => T_END T_PROGRAM ( T_IDENT )? T_EOS
	|	T_ENDPROGRAM ( T_IDENT )? T_EOS
	|	T_END T_EOS
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
	:	T_MODULE ( T_IDENT )? T_EOS
	;

// R1106
end_module_stmt
options {k=2;}
        :       (T_END T_MODULE) => T_END T_MODULE ( T_IDENT )? T_EOS
        |       T_ENDMODULE ( T_IDENT )? T_EOS
        |       T_END T_EOS
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
    :    T_USE ( ( T_COMMA module_nature )? T_COLON_COLON )? T_IDENT ( T_COMMA rename_list )? T_EOS
    |    T_USE ( ( T_COMMA module_nature )? T_COLON_COLON )? T_IDENT T_COMMA T_ONLY T_COLON ( only_list )? T_EOS
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
    :    rename ( T_COMMA rename )*
    ;

// R1112
// T_IDENT inlined for only_use_name
// generic_spec can be T_IDENT so T_IDENT deleted
only
	:	generic_spec
	|	rename
	;

only_list
    :    only ( T_COMMA only )*
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
options {k=2;}
	:	(T_BLOCK T_DATA) => T_BLOCK T_DATA ( T_IDENT )? T_EOS
	|   T_BLOCKDATA ( T_IDENT )? T_EOS
	;

// R1118
end_block_data_stmt
options {k=2;}
	:   (T_END T_BLOCK) => T_END T_BLOCK T_DATA ( T_IDENT )? T_EOS
	|   (T_ENDBLOCK T_DATA) => T_ENDBLOCK T_DATA ( T_IDENT )? T_EOS
	|   (T_END T_BLOCKDATA) => T_END T_BLOCKDATA ( T_IDENT )? T_EOS
	|   T_ENDBLOCKDATA ( T_IDENT )? T_EOS
	|	T_END T_EOS
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
	:   T_INTERFACE ( generic_spec )? T_EOS
	|	T_ABSTRACT T_INTERFACE T_EOS
	;

// R1204
end_interface_stmt
options {k=2;}
	: (T_END T_INTERFACE) => T_END T_INTERFACE ( generic_spec )? T_EOS
	| T_ENDINTERFACE ( generic_spec )? T_EOS
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
	:	( T_MODULE )? T_PROCEDURE generic_name_list T_EOS
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
    :    T_IMPORT ( ( T_COLON_COLON )? generic_name_list )? T_EOS
    ;

// R1210
// generic_name_list substituted for external_name_list
external_stmt
	:	T_EXTERNAL ( T_COLON_COLON )? generic_name_list T_EOS
	;

// R1211
procedure_declaration_stmt
    :    T_PROCEDURE T_LPAREN ( proc_interface )? T_RPAREN
            ( ( T_COMMA proc_attr_spec )* T_COLON_COLON )? proc_decl_list T_EOS
    ;

// R1212
// T_IDENT inlined for interface_name
proc_interface
	:	T_IDENT
	|	declaration_type_spec
	;

// R1213
proc_attr_spec
	:	access_spec
	|	proc_language_binding_spec
	|	T_INTENT T_LPAREN intent_spec T_RPAREN
	|	T_OPTIONAL
	|	T_POINTER
	|	T_SAVE
	;

// R1214
// T_IDENT inlined for procedure_entity_name
proc_decl
    :    T_IDENT ( T_EQ_GT null_init )?
    ;

proc_decl_list
    :    proc_decl ( T_COMMA proc_decl )*
    ;

// R1215 interface_name was name inlined as T_IDENT

// R1216
// generic_name_list substituted for intrinsic_procedure_name_list
intrinsic_stmt
	:	T_INTRINSIC
		( T_COLON_COLON )?
		generic_name_list T_EOS
	;

// R1217
// C1220 (R1217) The procedure-designator shall designate a function.
function_reference
	:	procedure_designator
		T_LPAREN
		( actual_arg_spec_list )?
		T_RPAREN
	;

// R1218
// C1222 (R1218) The procedure-designator shall designate a subroutine.
call_stmt
    :    T_CALL procedure_designator
         ( T_LPAREN ( actual_arg_spec_list )? T_RPAREN )? T_EOS
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
    : ( keyword T_EQUALS )? actual_arg
    ;

actual_arg_spec_list
    :    actual_arg_spec ( T_COMMA actual_arg_spec )*
    ;

// R1221
// ERR_CHK 1221 ensure ( expr | designator ending in T_PERCENT T_IDENT)
// T_IDENT inlined for procedure_name
// expr isa designator (via primary) so variable deleted
// designator isa T_IDENT so T_IDENT deleted
// proc_component_ref is variable T_PERCENT T_IDENT can be designator so deleted
actual_arg
	:	expr
	|	T_ASTERISK label
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
	:	T_FUNCTION T_IDENT
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

t_prefix_spec
	:	T_RECURSIVE
	|	T_PURE
	|	T_ELEMENTAL
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
options {k=2;}
	: (T_END T_FUNCTION) => T_END T_FUNCTION ( T_IDENT )? T_EOS
	| T_ENDFUNCTION ( T_IDENT )? T_EOS
	| T_END T_EOS
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
    :     (t_prefix)? T_SUBROUTINE T_IDENT
          ( T_LPAREN ( dummy_arg_list )? T_RPAREN ( proc_language_binding_spec )? )? T_EOS
    ;

// R1233
// T_IDENT inlined for dummy_arg_name
dummy_arg
	:	T_IDENT
	|	T_ASTERISK
	;

dummy_arg_list
    :    dummy_arg ( T_COMMA dummy_arg )*
    ;

// R1234
end_subroutine_stmt
options {k=2;}
    : (T_END T_SUBROUTINE) => T_END T_SUBROUTINE ( T_IDENT )? T_EOS
    | T_ENDSUBROUTINE ( T_IDENT )? T_EOS
    | T_END T_EOS
    ;

// R1235
// T_INDENT inlined for entry_name
entry_stmt
    :    T_ENTRY T_IDENT
          ( T_LPAREN ( dummy_arg_list )? T_RPAREN ( suffix )? )? T_EOS
    ;

// R1236
// ERR_CHK 1236 scalar_int_expr replaced by expr
return_stmt
	:	T_RETURN ( expr )? T_EOS
	;

// R1237 contains_stmt inlined as T_CONTAINS

// R1238
// ERR_CHK 1239 scalar_expr replaced by expr
// generic_name_list substituted for dummy_arg_name_list
// TODO Hopefully scanner and parser can help work together here to work around ambiguity.
//      Need scanner to send special token if it sees what?
stmt_function_stmt
	:	T_STMT_FUNCTION T_IDENT T_LPAREN ( generic_name_list )? T_RPAREN T_EQUALS expr T_EOS
	;


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

WS  :  (' '|'\r'|'\t'|'\u000C'|'\n') {channel=99;}
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
T_DEFERRED      :       'DEFFERRED'     ;
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

LINE_COMMENT
    : '!' ~('\n'|'\r')* '\r'? '\n' {channel=99;}
    ;
