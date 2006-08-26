/*
 * NOTES
 *
 * R303, R406, R417, R427, R428 underscore - added _ to rule (what happened to it?) * R410 sign - had '?' rather than '-'
 * R1209 import-stmt: MISSING a ]
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
program_unit
	:	main_program
	|	external_subprogram
	|	module
	|	block_data
	;

// R203
// modified to factor out common optional prefix
external_subprogram
        :       function_or_subroutine_subprogram
        ;

function_or_subroutine_subprogram
        :       (prefix)? (function_subprogram_body | subroutine_subprogram_body)
        ;

// R204
// TODO putback
specification_part
	:	( use_stmt )*
		( import_stmt )*
//		( implicit_part )?
		( declaration_construct )*
	;

// R205
// TODO putback
implicit_part
	:	// ( implicit_part_stmt )*
		implicit_stmt
	;

// R206
implicit_part_stmt
	:	implicit_stmt
	|	parameter_stmt
	|	format_stmt
	|	entry_stmt
	;

// R207
declaration_construct
	:	derived_type_def
	|	entry_stmt
	|	enum_def
	|	format_stmt
	|	interface_block
	|	parameter_stmt
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
internal_subprogram_part
	:	contains_stmt
		internal_subprogram
		( internal_subprogram )*
	;

// R211
internal_subprogram
        :       function_or_subroutine_subprogram
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
// TODO putback
executable_construct
	:	action_stmt
	|	associate_construct
	|	case_construct
	|	do_construct
	|	forall_construct
//	|	if_construct
	|	select_type_construct
	|	where_construct
	;

// R214
// TODO putback
action_stmt
	:	allocate_stmt
	|	assignment_stmt
	|	backspace_stmt
	|	call_stmt
	|	close_stmt
//	|	continue_stmt
	|	cycle_stmt
	|	deallocate_stmt
	|	endfile_stmt
//	|   end_func_prog_or_sub_stmt // normal end_function_stmt... removed
//	|	end_function_stmt
//	|	end_program_stmt
//	|	end_subroutine_stmt
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
//	|	where_stmt
	|	write_stmt
	|	arithmetic_if_stmt
	|	computed_goto_stmt
	;

//end_func_prog_or_sub_stmt
//options {k=2;}
//	: T_END
//	| (T_END T_FUNCTION) => T_END T_FUNCTION T_IDENT?
//	| T_ENDFUNCTION T_IDENT?
//	| (T_END T_PROGRAM) => T_END T_PROGRAM T_IDENT?
//	| T_ENDPROGRAM T_IDENT?
//	| (T_END T_SUBROUTINE) => T_END T_SUBROUTINE T_IDENT?
//	| T_ENDSUBROUTINE T_IDENT?
//	;

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
// named_constant causes an ambiguity with variable is designator so removed from grammar
constant
	:	literal_constant
//	|	named_constant
	;

scalar_constant
    :    constant
    ;

// R306
// TODO putback
literal_constant
	:	int_literal_constant
//	|	real_literal_constant
//	|	complex_literal_constant
//	|	logical_literal_constant
//	|	char_literal_constant
//	|	boz_literal_constant
	;

// R307
// named_constant causes an ambiguity with variable is designator so removed from grammar
// named_constant
//	:	name
//	;

// R308 int_constant inlined as T_DIGIT_STRING

// R309
char_constant
	:	constant
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
    : T_LPAREN (T_KIND_EQUALS)? expr T_RPAREN
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
kind_param
	:	T_DIGIT_STRING
	|	scalar_int_constant_name
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
// ERR_CHK 422 named_constant replaced by T_IDENT (
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
// ERR_CHK 424 scalar_int_initialization_expr replaced by expr
// TODO putback
char_selector
    :    length_selector
//    |    T_LPAREN T_LEN_EQUALS type_param_value T_COMMA T_KIND_EQUALS expr T_RPAREN
//    |    T_LPAREN type_param_value T_COMMA (T_KIND_EQUALS)? expr T_RPAREN
//    |    T_LPAREN T_KIND_EQUALS expr ( T_COMMA T_LEN_EQUALS type_param_value )? T_RPAREN
    ;

// R425
length_selector
	:	T_LPAREN
		(T_LEN_EQUALS)?
		type_param_value
		T_RPAREN
	|	T_ASTERISK
		char_length
		(T_COMMA)?
	;

// R426
char_length
	:	T_LPAREN
		type_param_value
		T_RPAREN
	|	scalar_int_literal_constant
	;

scalar_int_literal_constant
    :    int_literal_constant
    ;

// R427
char_literal_constant
    :    ( kind_param T_UNDERSCORE )? T_CHAR_CONSTANT
    ;

// R428
logical_literal_constant
    :    T_TRUE ( T_UNDERSCORE kind_param )?
    |    T_FALSE ( T_UNDERSCORE kind_param )?
    ;

// R429
// TODO putback
derived_type_def
	:	derived_type_stmt
		( type_param_def_stmt )*
		( private_or_sequence )*
//		( component_part )?
		( type_bound_procedure_part )?
		end_type_stmt
	;

// R430
derived_type_stmt
    :    T_TYPE ( ( T_COMMA type_attr_spec_list )? T_COLON_COLON )? T_IDENT
         ( T_LPAREN type_param_name_list T_RPAREN )?
    ;

type_attr_spec_list
    :    type_attr_spec ( T_COMMA type_attr_spec )*
    ;

type_attr_name
    :    T_IDENT
    ;

type_attr_name_list
    :    type_attr_name ( T_COMMA type_attr_name )*
    ;

// R431
type_attr_spec
	:	access_spec
	|	T_EXTENDS T_LPAREN parent_type_name T_RPAREN
	|	T_ABSTRACT
	|	T_BIND_LPAREN_C T_RPAREN
	;

parent_type_name
    : name
    ;

// R432
private_or_sequence
    :   private_components_stmt
    |   sequence_stmt
    ;

// R433
end_type_stmt
options {k=2;}
	: (T_END T_TYPE) => T_END T_TYPE ( T_IDENT )?
	| T_ENDTYPE ( T_IDENT )?
	;

// R434
sequence_stmt
	:	T_SEQUENCE
	;

// R435
type_param_def_stmt
	:	T_INTEGER ( kind_selector )? T_COMMA type_param_attr_spec T_COLON_COLON type_param_decl_list
	;

// R436
// ERR_CHK 436 scalar_int_initialization_expr replaced by expr
type_param_decl
    :    type_param_name ( T_EQUALS expr )?
    ;

type_param_decl_list
    :    type_param_decl ( T_COMMA type_param_decl )*
    ;

type_param_name
    :    T_IDENT
    ;

type_param_name_list
    : type_param_name ( T_COMMA type_param_name )?
    ;

// R437
type_param_attr_spec
	:	T_KIND
	|	T_LEN
	;

// R438
component_part
	:	( component_def_stmt )*
	;

// R439
component_def_stmt
	:	data_component_def_stmt
	|	proc_component_def_stmt
	;


// R440
data_component_def_stmt
    :    declaration_type_spec ( ( T_COMMA component_attr_spec_list )? T_COLON_COLON )? component_decl_list
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
component_decl
    :    component_name ( T_LPAREN component_array_spec T_RPAREN )?
                        ( T_ASTERISK char_length )? ( component_initialization )?
    ;

component_decl_list
    :   component_decl ( T_COMMA component_decl )*
    ;

component_name
    :    name
    ;

// R443
component_array_spec
	:	explicit_shape_spec_list
	|	deferred_shape_spec_list
	;

deferred_shape_spec_list
    :    deferred_shape_spec ( T_COMMA deferred_shape_spec )*
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
		    proc_component_attr_spec_list T_COLON_COLON proc_decl_list
	;

// R446
proc_component_attr_spec
    :    T_POINTER
    |    T_PASS ( T_LPAREN arg_name T_RPAREN )?
    |    T_NOPASS
    |    access_spec
    ;

proc_component_attr_spec_list
    :    proc_component_attr_spec ( T_COMMA proc_component_attr_spec )*
    ;

// R447
private_components_stmt
	:	T_PRIVATE
	;

// R448
type_bound_procedure_part
	:	contains_stmt ( binding_private_stmt )? proc_binding_stmt ( proc_binding_stmt )*
	;

// R449
binding_private_stmt
	:	T_PRIVATE
	;

// R450
proc_binding_stmt
	:	specific_binding
	|	generic_binding
	|	final_binding
	;

// R451
specific_binding
    : T_PROCEDURE ( T_LPAREN interface_name T_RPAREN )?
      ( ( T_COMMA binding_attr_list )? T_COLON_COLON )?
      binding_name ( T_EQ_GT procedure_name )?
    ;

// R452
generic_binding
    :    T_GENERIC ( T_COMMA access_spec )? T_COLON_COLON generic_spec T_EQ_GT binding_name_list
    ;

// R453
binding_attr
    : T_PASS ( T_LPAREN arg_name T_RPAREN )?
    | T_NOPASS
    | T_NON_OVERRIDABLE
    | T_DEFERRED
    | access_spec
    ;

binding_attr_list
    :    binding_attr ( T_COMMA binding_attr )*
    ;

arg_name
    : name
    ;

// R454
final_binding
	:	T_FINAL
		( T_COLON_COLON )?
		final_subroutine_name_list
	;

final_subroutine_name_list
    :    name ( T_COMMA name )*
    ;

// R455
derived_type_spec
    : T_IDENT
      ( T_LPAREN type_param_spec_list T_RPAREN )?
    ;

// R456
type_param_spec
    : ( keyword T_EQUALS )? type_param_value
    ;

type_param_spec_list
    :    type_param_spec ( T_COMMA type_param_spec )*
    ;

// R457
structure_constructor
	:	derived_type_spec
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
// TODO putback
component_data_source
	:	expr
//	|	data_target
//	|	proc_target
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
	:	T_ENUM T_COMMA T_BIND_LPAREN_C T_RPAREN
	;

// R462
enumerator_def_stmt
	:	T_ENUMERATOR ( T_COLON_COLON )? enumerator_list
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
	:	(T_END T_ENUM) => T_END T_ENUM
	|	T_ENDENUM
	;

// R465
array_constructor
	:	T_LPAREN T_SLASH ac_spec T_SLASH T_RPAREN
	|	left_square_bracket ac_spec right_square_bracket
	;


// R466
// TODO putback
ac_spec
    : type_spec T_COLON_COLON
//    | (type_spec T_COLON_COLON)? ac_value_list
    ;

// R467
left_square_bracket
	:	T_LBRACKET
	;

// R468
right_square_bracket
	:	T_RBRACKET
	;

// R469
// TODO putback
ac_value
	:	expr
//	|	ac_implied_do
	;

ac_value_list
    :   ac_value ( T_COMMA ac_value )*
    ;

// R470
ac_implied_do
	:	T_LPAREN ac_value_list T_COMMA ac_implied_do_control T_RPAREN
	;

// R471
// ERR_CHK 471 scalar_int_expr replaced by expr
ac_implied_do_control
    :    ac_do_variable T_EQUALS expr T_COMMA expr ( T_COMMA expr )?
    ;

// R472
ac_do_variable
	:	scalar_int_variable
	;

scalar_int_variable
    :    variable
    ;


/*
Section 5:
 */

// R501
type_declaration_stmt
    :    declaration_type_spec ( ( T_COMMA attr_spec )* T_COLON_COLON )? entity_decl_list
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
// TODO putback
entity_decl
    : object_name ( T_LPAREN array_spec T_RPAREN )? ( T_ASTERISK char_length )? ( initialization )?
//    | T_IDENT ( T_ASTERISK char_length )?
    ;

// TODO putback
entity_decl_list
    :    entity_decl
         ( T_COMMA entity_decl )*
    ;

// R505
object_name
	:	T_IDENT
	;

object_name_list
    :    object_name ( T_COMMA object_name )*
    ;

// R506
// ERR_CHK 506 initialization_expr replaced by expr
initialization
	:	T_EQUALS
		expr
	|	T_EQ_GT
		null_init
	;

// R507
null_init
	:	function_reference
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
// TODO putback
array_spec
	:	explicit_shape_spec_list
//	|	assumed_shape_spec_list
//	|	deferred_shape_spec_list
//	|	assumed_size_spec
	;

// R511
// TODO putback
explicit_shape_spec
    : // ( lower_bound T_COLON )?
      upper_bound
    ;

explicit_shape_spec_list
    : explicit_shape_spec ( T_COMMA explicit_shape_spec )*
    ;

// R512
// ERR_CHK 512 specification_expr replaced by expr
lower_bound
	:	expr
	;

// R513
// ERR_CHK 513 specification_expr replaced by expr
upper_bound
	:	expr
	;

// R514
assumed_shape_spec
	:	( lower_bound )? T_COLON
	;

assumed_shape_spec_list
    :    assumed_shape_spec ( T_COMMA assumed_shape_spec )*
    ;

// R515
deferred_shape_spec
	:	T_COLON
	;

// R516
assumed_size_spec
    : /*( explicit_shape_spec_list T_COMMA )? ( lower_bound T_COLON )?*/
       T_ASTERISK
    ;

// R517
// TODO putback WARNING this may be tricky
intent_spec
options {k=2;}
	:	T_IN
	|	T_OUT
    |   (T_IN T_OUT) => T_IN T_OUT
	|	T_INOUT
	;

// R518
access_stmt
    :    access_spec ( ( T_COLON_COLON )? access_id_list )?
    ;

// R519
access_id
	:	use_name
//	|	generic_spec
	;

access_id_list
    :    access_id ( T_COMMA access_id )*
    ;

// R520
allocatable_stmt
    : T_ALLOCATABLE ( T_COLON_COLON )? object_name ( T_LPAREN deferred_shape_spec_list T_RPAREN )?
         ( T_COMMA object_name ( T_LPAREN deferred_shape_spec_list T_RPAREN )? )*
    ;

// R521
asynchronous_stmt
	:	T_ASYNCHRONOUS
		( T_COLON_COLON )?
		object_name_list
	;

// R522
bind_stmt
	:	language_binding_spec
		( T_COLON_COLON )?
		bind_entity_list
	;

entity_name
    :    name
    ;

entity_name_list
    :    entity_name ( T_COMMA entity_name )*
    ;

// R523
bind_entity
	:	entity_name
	|	T_SLASH common_block_name T_SLASH
	;

common_block_name
    :    name
    ;

bind_entity_list
    :    bind_entity ( T_COMMA bind_entity )*
    ;

// R524
data_stmt
    :    T_DATA data_stmt_set ( ( T_COMMA )? data_stmt_set )*
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
data_implied_do
    : T_LPAREN data_i_do_object_list T_COMMA data_i_do_variable T_EQUALS
         expr T_COMMA expr ( T_COMMA expr )? T_RPAREN
    ;

// R528
// TODO putback
data_i_do_object
	:	array_element
//	|	scalar_structure_component
//	|	data_implied_do
	;

data_i_do_object_list
    :   data_i_do_object ( T_COMMA data_i_do_object )*
    ;

// R529
data_i_do_variable
	:	scalar_int_variable
	;

// R530
data_stmt_value
    : ( data_stmt_repeat T_ASTERISK )? data_stmt_constant
    ;

data_stmt_value_list
    :    data_stmt_value ( T_COMMA data_stmt_value )*
    ;

// R531
// TODO putback
data_stmt_repeat
	:	scalar_int_constant
//	|	scalar_int_constant_subobject
	;

scalar_int_constant
    :    T_DIGIT_STRING
    ;

scalar_int_constant_subobject
    :    T_DIGIT_STRING
    ;

scalar_int_constant_name
    :    name
    ;

// R532
// TODO putback
data_stmt_constant
	:	scalar_constant
//	|	scalar_constant_subobject
//	|	signed_int_literal_constant
//	|	signed_real_literal_constant
//	|	null_init
//	|	structure_constructor
	;

scalar_constant_subobject
    :    scalar_constant
    ;

// R533
int_constant_subobject
	:	constant_subobject
	;

// R534
constant_subobject
	:	designator
	;

// R535
dimension_stmt
    :    T_DIMENSION ( T_COLON_COLON )? array_name T_LPAREN array_spec T_RPAREN
             ( T_COMMA array_name T_LPAREN array_spec T_RPAREN )*
    ;

array_name
    :    name
    ;

// R536
intent_stmt
	:	T_INTENT T_LPAREN intent_spec T_RPAREN ( T_COLON_COLON )? dummy_arg_name_list
	;

dummy_arg_name_list
    : dummy_arg_name ( T_COMMA dummy_arg_name )*
    ;

// R537
optional_stmt
	:	T_OPTIONAL ( T_COLON_COLON )? dummy_arg_name_list
	;

// R538
parameter_stmt
	:	T_PARAMETER T_LPAREN named_constant_def_list T_RPAREN
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
	:	T_POINTER ( T_COLON_COLON )? pointer_decl_list
	;

pointer_decl_list
    :    pointer_decl ( T_COMMA pointer_decl )*
    ;

// R541
// TODO putback
pointer_decl
    : object_name ( T_LPAREN deferred_shape_spec_list T_RPAREN )?
//    | proc_entity_name
    ;

proc_entity_name
    :    name
    ;

// R542
protected_stmt
	:	T_PROTECTED
		( T_COLON_COLON )?
		entity_name_list
	;

// R543
save_stmt
    : T_SAVE ( ( T_COLON_COLON )? saved_entity_list )?
    ;

// R544
saved_entity
	:	object_name
//	|	proc_pointer_name
//	|	T_SLASH common_block_name T_SLASH
	;

saved_entity_list
    :    saved_entity ( T_COMMA saved_entity )*
    ;

// R545
proc_pointer_name
	:	name
	;

// R546
target_stmt
    : T_TARGET ( T_COLON_COLON )? object_name ( T_LPAREN array_spec T_RPAREN )?
             ( T_COMMA object_name ( T_LPAREN array_spec T_RPAREN )? )*
    ;

// R547
value_stmt
	:	T_VALUE ( T_COLON_COLON )? dummy_arg_name_list
	;

// R548
volatile_stmt
	:	T_VOLATILE ( T_COLON_COLON )? object_name_list
	;

// R549
implicit_stmt
	:	T_IMPLICIT implicit_spec_list
	|	T_IMPLICIT T_NONE
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
namelist_stmt
    : T_NAMELIST T_SLASH namelist_group_name T_SLASH namelist_group_object_list
         ( ( T_COMMA )? T_SLASH namelist_group_name T_SLASH namelist_group_object_list )*
    ;

// R553
namelist_group_object
	:	variable_name
	;

namelist_group_object_list
    :    namelist_group_object ( T_COMMA namelist_group_object )*
    ;

namelist_group_name
    :    name
    ;

// R554
equivalence_stmt
	:	T_EQUIVALENCE equivalence_set_list
	;

// R555
equivalence_set
	:	T_LPAREN equivalence_object T_COMMA equivalence_object_list T_RPAREN
	;

equivalence_set_list
    :    equivalence_set ( T_COMMA equivalence_set )*
    ;

// R556
// TODO putback
equivalence_object
	:	variable_name
//	|	array_element
//	|	substring
	;

equivalence_object_list
    :    equivalence_object ( T_COMMA equivalence_object )
    ;

// R557
common_stmt
    : T_COMMON ( T_SLASH ( common_block_name )? T_SLASH )? common_block_object_list
         ( ( T_COMMA )? T_SLASH ( common_block_name )? T_SLASH common_block_object_list )*
    ;

// R558
common_block_object
    : variable_name ( T_LPAREN explicit_shape_spec_list T_RPAREN )?
//    | proc_pointer_name
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

// R602
variable_name
	:	name
	;

// R603
// TODO putback
designator
	:	object_name
//	|	array_element
//	|	array_section
//	|	structure_component
//	|	substring
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
substring
	:	parent_string
		T_LPAREN
		substring_range
		T_RPAREN
	;

// R610
// TODO putback
parent_string
	:	scalar_variable_name
//	|	array_element
//	|	scalar_structure_component
//	|   scalar_constant
	;

scalar_variable_name
    :    name
    ;

// R611
// ERR_CHK 611 scalar_int_expr replaced by expr
substring_range
	:	( expr )?
		T_COLON
		( expr )?
	;

// R612
// TODO putback
data_ref
    : part_ref
//      ( T_PERCENT part_ref )*
    ;

// R613
// TODO putback
part_ref
    : part_name
//      ( T_LPAREN section_subscript_list T_RPAREN )?
    ;

part_name
    :    name
    ;

// R614
structure_component
	:	data_ref
	;

scalar_structure_component
    :   data_ref
    ;

// R615
type_param_inquiry
	:	designator
		T_PERCENT
		type_param_name
	;

// R616
array_element
	:	data_ref
	;

// R617
array_section
    :    data_ref ( T_LPAREN substring_range T_RPAREN )?
    ;

// R618
// ERR_CHK 618 scalar_int_expr replaced by expr
subscript
	:	expr
	;

// R619
// TODO putback
section_subscript
	:	subscript
//	|	subscript_triplet
//	|	vector_subscript
	;

section_subscript_list
    :    section_subscript ( T_COMMA section_subscript )*
    ;

// R620
subscript_triplet
    : ( subscript )? T_COLON ( subscript )? ( T_COLON stride )?
    ;

// R621
// ERR_CHK 621 scalar_int_expr replaced by expr
stride
	:	expr
	;

// R622
// ERR_CHK 622 int_expr replaced by expr
vector_subscript
	:	expr
	;

// R623
allocate_stmt
    :    T_ALLOCATE T_LPAREN ( type_spec T_COLON_COLON )? allocation_list ( T_COMMA alloc_opt_list )? T_RPAREN
    ;

// R624
// ERR_CHK 624 source_expr replaced by expr
alloc_opt
	:	T_STAT_EQUALS stat_variable
	|	T_ERRMSG_EQUALS errmsg_variable
	|	T_SOURCE_EQUALS expr
	;

alloc_opt_list
    :    alloc_opt ( T_COMMA alloc_opt )*
    ;

// R625
stat_variable
	:	scalar_int_variable
	;

// R626
errmsg_variable
	:	scalar_default_char_variable
	;

// R627 inlined source_expr was expr

// R628
allocation
    : allocate_object ( T_LPAREN allocate_shape_spec_list T_RPAREN )?
    ;

allocation_list
    :    allocation ( T_COMMA allocation )*
    ;

// R629
// TODO putback
allocate_object
	:	variable_name
//	|	structure_component
	;

allocate_object_list
    :    allocate_object ( T_COMMA allocate_object )*
    ;

// R630
// ERR_CHK 630a lower_bound_expr replaced by expr
// ERR_CHK 630b upper_bound_expr replaced by expr
// TODO putback
allocate_shape_spec
    :    ( expr T_COLON )?
//          expr
    ;

// TODO putback
allocate_shape_spec_list
    :    allocate_shape_spec
//         ( T_COMMA allocate_shape_spec )*
    ;

// R631 inlined lower_bound_expr was scalar_int_expr

// R632 inlined upper_bound_expr was scalar_int_expr

// R633
nullify_stmt
	:	T_NULLIFY
		T_LPAREN
		pointer_object_list
		T_RPAREN
	;

// R634
// TODO putback
pointer_object
	:	variable_name
//	|	structure_component
//	|	proc_pointer_name
	;

pointer_object_list
    :    pointer_object ( T_COMMA pointer_object )*
    ;

// R635
deallocate_stmt
    :    T_DEALLOCATE T_LPAREN allocate_object_list ( T_COMMA dealloc_opt_list )? T_RPAREN
    ;

// R636
dealloc_opt
	:	T_STAT_EQUALS stat_variable
	|	T_ERRMSG_EQUALS errmsg_variable
	;

dealloc_opt_list
    :    dealloc_opt ( T_COMMA dealloc_opt )*
    ;

/*
Section 7:
 */

// R701
primary
	:	constant
//	|	designator
//	|	array_constructor
//	|	structure_constructor
//	|	function_reference
//	|	type_param_inquiry
//	|	type_param_name
//	|	T_LPAREN expr T_RPAREN
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
    : equiv_operand // ( defined_binary_op equiv_operand )*
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
		expr
	;

// R735
// TODO putback
pointer_assignment_stmt
    :    data_pointer_object ( T_LPAREN bounds_spec_list T_RPAREN )? T_EQ_GT data_target
//    | data_pointer_object T_LPAREN bounds_remapping_list T_RPAREN T_EQ_GT data_target
//    | proc_pointer_object T_EQ_GT proc_target
    ;

// R736
data_pointer_object
	:	variable_name
	|	variable T_PERCENT data_pointer_component_name
	;

data_pointer_component_name
    :    name
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

// R739
// TODO putback
data_target
	:	variable
//	|	expr
	;

// R740
proc_pointer_object
	:	proc_pointer_name
	|	proc_component_ref
	;

// R741
proc_component_ref
	:	variable T_PERCENT procedure_component_name
	;

procedure_component_name
    :    name
    ;

// R742
// TODO putback
proc_target
	:	expr
//	|	procedure_name
//	|	proc_component_ref
	;

// R743
// ERR_CHK 743 mask_expr replaced by expr
where_stmt
	:	T_WHERE
		T_LPAREN
		expr
		T_RPAREN
		where_assignment_stmt
	;

// R744
where_construct
    :    where_construct_stmt ( where_body_construct )* ( masked_elsewhere_stmt
          ( where_body_construct )* )* ( elsewhere_stmt ( where_body_construct )* )? end_where_stmt
    ;

// R745
// ERR_CHK 745 mask_expr replaced by expr
where_construct_stmt
    :    ( T_IDENT T_COLON )? T_WHERE T_LPAREN expr T_RPAREN
    ;

// R746
// TODO putback
where_body_construct
	:	where_assignment_stmt
//	|	where_stmt
//	|	where_construct
	;

// R747
where_assignment_stmt
	:	assignment_stmt
	;

// R748 inlined mask_expr was logical_expr

// inlined scalar_mask_expr was scalar_logical_expr

// inlined scalar_logical_expr was logical_expr

// R749
// ERR_CHK 749 mask_expr replaced by expr
masked_elsewhere_stmt
options {k=2;}
	:	(T_ELSE T_WHERE) => T_ELSE T_WHERE
		T_LPAREN	expr T_RPAREN ( T_IDENT )?
	|	(T_ELSEWHERE) => T_ELSEWHERE
		T_LPAREN	expr T_RPAREN ( T_IDENT )?
	;

// R750
elsewhere_stmt
options {k=2;}
	:	(T_ELSE T_WHERE) => T_ELSE T_WHERE
		( T_IDENT )?
	|	(T_ELSEWHERE) => T_ELSEWHERE
		( T_IDENT )?
	;

// R751
end_where_stmt
options {k=2;}
	: (T_END T_WHERE) => T_END T_WHERE ( T_IDENT )?
	| T_ENDWHERE ( T_IDENT )?
	;

// R752
forall_construct
	:	forall_construct_stmt
		( forall_body_construct )*
		end_forall_stmt
	;

// R753
forall_construct_stmt
    :    ( T_IDENT T_COLON )? T_FORALL forall_header
    ;

// R754
// ERR_CHK 754 scalar_mask_expr replaced by expr
forall_header
    : T_LPAREN forall_triplet_spec_list ( T_COMMA expr )? T_RPAREN
    ;

// R755
forall_triplet_spec
    : index_name T_EQUALS subscript T_COLON subscript ( T_COLON stride )?
    ;

index_name
    :    name
    ;

forall_triplet_spec_list
    :    forall_triplet_spec ( T_COMMA forall_triplet_spec )*
    ;

// R756
// TODO putback
forall_body_construct
	:	forall_assignment_stmt
//	|	where_stmt
//	|	where_construct
//	|	forall_construct
//	|	forall_stmt
	;

// R757
forall_assignment_stmt
	:	assignment_stmt
	|	pointer_assignment_stmt
	;

// R758
end_forall_stmt
options {k=2;}
	: (T_END T_FORALL) => T_END T_FORALL ( T_IDENT )?
	| T_ENDFORALL ( T_IDENT )?
	;

// R759
forall_stmt
	:	T_FORALL
		forall_header
		forall_assignment_stmt
	;

/*
Section 8:
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
    : ( T_IDENT T_COLON )? T_IF T_LPAREN expr T_RPAREN T_THEN
    ;

// R804
// ERR_CHK 804 scalar_logical_expr replaced by expr
else_if_stmt
options {k=2;}
	: (T_ELSE T_IF) => T_ELSE T_IF
        T_LPAREN expr T_RPAREN T_THEN ( T_IDENT )?
	| T_ELSEIF
        T_LPAREN expr T_RPAREN T_THEN ( T_IDENT )?
	;

// R805
else_stmt
	:	T_ELSE
		( T_IDENT )?
	;

// R806
end_if_stmt
options {k=2;}
	: (T_END T_IF) => T_END T_IF ( T_IDENT )?
	| T_ENDIF ( T_IDENT )?
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
        T_LPAREN expr T_RPAREN
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
		( T_IDENT )?
	;

// R811
end_select_stmt
options {k=2;}
	: (T_END T_SELECT) => T_END T_SELECT T_IDENT
	| T_ENDSELECT T_IDENT
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
// TODO putback
case_value_range
	:	case_value
//	|	case_value T_COLON
//	|	T_COLON case_value
//	|	case_value T_COLON case_value
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
    : ( T_IDENT T_COLON )? T_ASSOCIATE T_LPAREN association_list T_RPAREN
    ;

association_list
    :    association ( T_COMMA association )*
    ;

// R818
association
	:	associate_name T_EQ_GT selector
	;

associate_name
    :    name
    ;

// R819
// TODO putback
selector
	:	expr
//	|	variable
	;

// R820
end_associate_stmt
options {k=2;}
	: (T_END T_ASSOCIATE) => T_END T_ASSOCIATE ( T_IDENT )?
	| T_ENDASSOCIATE ( T_IDENT )?
	;

// R821
select_type_construct
    :    select_type_stmt ( type_guard_stmt block )* end_select_type_stmt
    ;

// R822
select_type_stmt
    : ( select_construct_name T_COLON )? select_type
         T_LPAREN ( associate_name T_EQ_GT )? selector T_RPAREN
    ;

select_type
options {k=2;}
    : (T_SELECT T_TYPE) => T_SELECT T_TYPE
    | T_SELECTTYPE
    ;

select_construct_name
    :    name
    ;

// R823
type_guard_stmt
	:	T_TYPE_IS T_LPAREN
		type_spec
		T_RPAREN
		( select_construct_name )?
	|	T_CLASS_IS T_LPAREN
		type_spec
		T_RPAREN
		( select_construct_name )?
	|	T_CLASS	T_DEFAULT
		( select_construct_name )?
	;

// R824
// TODO putback see R811 for potential ambiguity
end_select_type_stmt
options {k=2;}
	:	(T_END T_SELECT) => T_END T_SELECT
//		( select_construct_name )?
	| T_ENDSELECT
//		( select_construct_name )?
	;

// R825
// TODO putback
do_construct
	:	block_do_construct
//	|	nonblock_do_construct
	;

// R826
block_do_construct
	:	do_stmt
		do_block
		end_do
	;

// R827
do_stmt
	:	label_do_stmt
	|	nonlabel_do_stmt
	;

// R828
label_do_stmt
    :    ( do_construct_name T_COLON )? T_DO label ( loop_control )?
    ;

do_construct_name
    :    name
    ;

// R829
nonlabel_do_stmt
    :    ( do_construct_name T_COLON )? T_DO ( loop_control )?
    ;

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

// R832
do_block
	:	block
	;

// R833
end_do
	:	end_do_stmt
	|	continue_stmt
	;

// R834
end_do_stmt
options {k=2;}
	: (T_END T_DO) => T_END T_DO ( do_construct_name )?
	| T_ENDDO ( do_construct_name )?
	;

// R835
// TODO putback
nonblock_do_construct
	:	action_term_do_construct
//	|	outer_shared_do_construct
	;

// R836
// TODO putback
action_term_do_construct
	:	label_do_stmt
//		do_body
		do_term_action_stmt
	;

// R837
do_body
	:	( execution_part_construct )*
	;

// R838
do_term_action_stmt
	:	action_stmt
	;

// R839
// TODO putback
outer_shared_do_construct
	:	label_do_stmt
//		do_body
		shared_term_do_construct
	;

// R840
// TODO putback
shared_term_do_construct
	:	outer_shared_do_construct
//	|	inner_shared_do_construct
	;

// R841
// TODO putback
inner_shared_do_construct
	:	label_do_stmt
//        do_body
        do_term_shared_stmt
	;

// R842
do_term_shared_stmt
	:	action_stmt
	;

// R843
cycle_stmt
	:	T_CYCLE ( do_construct_name )?
	;

// R844
exit_stmt
	:	T_EXIT ( do_construct_name )?
	;

// R845
goto_stmt
	:	t_go_to label
	;

// R846
// ERR_CHK 846 scalar_int_expr replaced by expr
computed_goto_stmt
	:	t_go_to T_LPAREN label_list T_RPAREN ( T_COMMA )? expr
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
		label
	;

// R848
continue_stmt
	:	T_CONTINUE
	;

// R849
stop_stmt
	:	T_STOP ( stop_code )?
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
// TODO putback
io_unit
	:	file_unit_number
//	|	T_ASTERISK
//	|	internal_file_variable
	;

// R902
// ERR_CHK 902 scalar_int_expr replaced by expr
file_unit_number
	:	expr
	;

// R903
internal_file_variable
	:	char_variable
	;

// R904
open_stmt
	:	T_OPEN T_LPAREN connect_spec_list T_RPAREN
	;

// R905
// ERR_CHK 905a scalar_default_char_expr replaced by expr
// ERR_CHK 905b scalar_int_expr replaced by expr
// ERR_CHK 905c file_name_expr replaced by expr
connect_spec
    : ( T_UNIT_EQUALS )? file_unit_number
    | T_ACCESS_EQUALS expr // scalar_default_char_expr
    | T_ACTION_EQUALS expr // scalar_default_char_expr
    | T_ASYNCHRONOUS_EQUALS expr // scalar_default_char_expr
    | T_BLANK_EQUALS expr // scalar_default_char_expr
    | T_DECIMAL_EQUALS expr // scalar_default_char_expr
    | T_DELIM_EQUALS expr // scalar_default_char_expr
    | T_ENCODING_EQUALS expr // scalar_default_char_expr
    | T_ERR_EQUALS label
    | T_FILE_EQUALS expr // file_name_expr
    | T_FORM_EQUALS expr // scalar_default_char_expr
    | T_IOMSG_EQUALS iomsg_variable
    | T_IOSTAT_EQUALS scalar_int_variable
    | T_PAD_EQUALS expr // scalar_default_char_expr
    | T_POSITION_EQUALS expr // scalar_default_char_expr
    | T_RECL_EQUALS expr // scalar_int_expr
    | T_ROUND_EQUALS expr // scalar_default_char_expr
    | T_SIGN_EQUALS expr // scalar_default_char_expr
    | T_STATUS_EQUALS expr // scalar_default_char_expr
    ;

connect_spec_list
    :    connect_spec ( T_COMMA connect_spec )*
    ;

// inlined scalar_default_char_expr

// R906 inlined file_name_expr with expr was scalar_default_char_expr

// R907
iomsg_variable
	:	scalar_default_char_variable
	;

// R908
close_stmt
	:	T_CLOSE T_LPAREN close_spec_list T_RPAREN
	;

// R909
// ERR_CHK 909 scalar_default_char_expr replaced by expr
close_spec
    : ( T_UNIT_EQUALS )? file_unit_number
    | T_IOSTAT_EQUALS scalar_int_variable
    | T_IOMSG_EQUALS iomsg_variable
    | T_ERR_EQUALS label
    | T_STATUS_EQUALS expr
    ;

close_spec_list
    :    close_spec ( T_COMMA close_spec )*
    ;

// R910
read_stmt
    :    T_READ T_LPAREN io_control_spec_list T_RPAREN ( input_item_list )?
    |    T_READ format ( T_COMMA input_item_list )?
    ;

// R911
write_stmt
	:	T_WRITE T_LPAREN io_control_spec_list T_RPAREN ( output_item_list )?
	;

// R912
print_stmt
    :    T_PRINT format ( T_COMMA output_item_list )?
    ;

// R913
// ERR_CHK 913a scalar_default_char_expr replaced by expr
// ERR_CHK 913b scalar_int_expr replaced by expr
// ERR_CHK 913c scalar_char_initialization_expr replaced by expr
io_control_spec
    :    ( T_UNIT_EQUALS )? io_unit
//    | ( T_FMT_EQUALS )? format
//    | ( T_NML_EQUALS )? namelist_group_name
    | T_ADVANCE_EQUALS expr // scalar_default_char_expr
    | T_ASYNCHRONOUS_EQUALS expr // scalar_char_initialization_expr
    | T_BLANK_EQUALS expr // scalar_default_char_expr
    | T_DECIMAL_EQUALS expr // scalar_default_char_expr
    | T_DELIM_EQUALS expr // scalar_default_char_expr
//    | T_END_EQUALS label
    | T_EOR_EQUALS label
    | T_ERR_EQUALS label
    | T_ID_EQUALS scalar_int_variable
    | T_IOMSG_EQUALS iomsg_variable
    | T_IOSTAT_EQUALS scalar_int_variable
    | T_PAD_EQUALS expr // scalar_default_char_expr
    | T_POS_EQUALS expr // scalar_int_expr
    | T_REC_EQUALS expr // scalar_int_expr
    | T_ROUND_EQUALS expr // scalar_default_char_expr
    | T_SIGN_EQUALS expr // scalar_default_char_expr
    | T_SIZE_EQUALS scalar_int_variable
    ;

io_control_spec_list
    :    io_control_spec ( T_COMMA io_control_spec )*
    ;

// R914
// ERR_CHK 914 default_char_expr replaced by expr
// TODO putback
format
	:	expr
//	|	label
	|	T_ASTERISK
	;

// R915
input_item
	:	variable
	|	io_implied_do
	;

input_item_list
    :    input_item ( T_COMMA input_item )*
    ;

// R916
output_item
	:	expr
	|	io_implied_do
	;

output_item_list
    :    output_item ( T_COMMA output_item )*
    ;

// R917
io_implied_do
	:	T_LPAREN io_implied_do_object_list T_COMMA io_implied_do_control T_RPAREN
	;

// R918
// TODO putback
io_implied_do_object
	:	input_item
//	|	output_item
	;

io_implied_do_object_list
    :    io_implied_do_object ( T_COMMA io_implied_do_object )*
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
	:	T_WAIT T_LPAREN wait_spec_list T_RPAREN
	;

// R922
// TODO putback
wait_spec
    : ( T_UNIT_EQUALS )? file_unit_number
//    | T_END_EQUALS label
    | T_EOR_EQUALS label
    | T_ERR_EQUALS label
    | T_ID_EQUALS scalar_int_variable
    | T_IOMSG_EQUALS iomsg_variable
    | T_IOSTAT_EQUALS scalar_int_variable
    ;

wait_spec_list
    :    wait_spec ( T_COMMA wait_spec )*
    ;

// R923
backspace_stmt
	:	T_BACKSPACE file_unit_number
	|	T_BACKSPACE T_LPAREN position_spec_list T_RPAREN
	;

// R924
endfile_stmt
	:	t_end_file file_unit_number
	|	t_end_file T_LPAREN position_spec_list T_RPAREN
	;

t_end_file
options {k=2;}
	: (T_END T_FILE) => T_END T_FILE
	| T_ENDFILE
	;

// R925
rewind_stmt
	:	T_REWIND file_unit_number
	|	T_REWIND T_LPAREN position_spec_list T_RPAREN
	;

// R926
position_spec
    : ( T_UNIT_EQUALS )? file_unit_number
    | T_IOMSG_EQUALS iomsg_variable
    | T_IOSTAT_EQUALS scalar_int_variable
    | T_ERR_EQUALS label
    ;

position_spec_list
    :    position_spec ( T_COMMA position_spec )*
    ;

// R927
flush_stmt
	:	T_FLUSH file_unit_number
	|	T_FLUSH T_LPAREN flush_spec_list T_RPAREN
	;

// R928
flush_spec
    : ( T_UNIT_EQUALS )? file_unit_number
    | T_IOSTAT_EQUALS scalar_int_variable
    | T_IOMSG_EQUALS iomsg_variable
    | T_ERR_EQUALS label
    ;

flush_spec_list
    :    flush_spec ( T_COMMA flush_spec )*
    ;

// R929
inquire_stmt
	:	T_INQUIRE T_LPAREN inquire_spec_list T_RPAREN
	|	T_INQUIRE T_LPAREN T_IOLENGTH_EQUALS scalar_int_variable T_RPAREN output_item_list
	;

// R930
// ERR_CHK 930 file_name_expr replaced by expr
inquire_spec
    : ( T_UNIT_EQUALS )? file_unit_number
    | T_FILE_EQUALS expr
    | T_ACCESS_EQUALS scalar_default_char_variable
    | T_ACTION_EQUALS scalar_default_char_variable
    | T_ASYNCHRONOUS_EQUALS scalar_default_char_variable
    | T_BLANK_EQUALS scalar_default_char_variable
    | T_DECIMAL_EQUALS scalar_default_char_variable
    | T_DELIM_EQUALS scalar_default_char_variable
    | T_DIRECT_EQUALS scalar_default_char_variable
    | T_ENCODING_EQUALS scalar_default_char_variable
    | T_ERR_EQUALS label
    | T_EXIST_EQUALS scalar_default_logical_variable
    | T_FORM_EQUALS scalar_default_char_variable
    | T_FORMATTED_EQUALS scalar_default_char_variable
    | T_ID_EQUALS scalar_int_variable
    | T_IOMSG_EQUALS iomsg_variable
    | T_IOSTAT_EQUALS scalar_int_variable
    | T_NAME_EQUALS scalar_default_char_variable
    | T_NAMED_EQUALS scalar_default_logical_variable
    | T_NEXTREC_EQUALS scalar_int_variable
    | T_NUMBER_EQUALS scalar_int_variable
    | T_OPENED_EQUALS scalar_default_logical_variable
    | T_PAD_EQUALS scalar_default_char_variable
    | T_PENDING_EQUALS scalar_default_logical_variable
    | T_POS_EQUALS scalar_int_variable
    | T_POSITION_EQUALS scalar_default_char_variable
    | T_READ_EQUALS scalar_default_char_variable
    | T_READWRITE_EQUALS scalar_default_char_variable
    | T_RECL_EQUALS scalar_int_variable
    | T_ROUND_EQUALS scalar_default_char_variable
    | T_SEQUENTIAL_EQUALS scalar_default_char_variable
    | T_SIGN_EQUALS scalar_default_char_variable
    | T_SIZE_EQUALS scalar_int_variable
    | T_STREAM_EQUALS scalar_default_char_variable
    | T_UNFORMATTED_EQUALS scalar_default_char_variable
    | T_WRITE_EQUALS scalar_default_char_variable
    ;

inquire_spec_list
    :    inquire_spec ( T_COMMA inquire_spec )*
    ;

/*
Section 10:
 */

// R1001
format_stmt
	:	T_FORMAT format_specification
	;

// R1002
// TODO putback
format_specification
	:	T_LPAREN //( format_item_list )? T_RPAREN
	;

// R1003
// TODO putback
format_item
	:	( int_literal_constant )? // data_edit_desc
//	|	control_edit_desc
//	|	char_string_edit_desc
//	|	( int_literal_constant )? T_LPAREN format_item_list T_RPAREN
	;

format_item_list
    :    format_item ( T_COMMA format_item )*
    ;

// R1004 r inlined in R1003 as int_literal_constant

// R1005
// TODO putback
//data_edit_desc
//    : 'I' int_literal_constant ( T_PERIOD int_literal_constant )?
//    | 'B' int_literal_constant ( T_PERIOD int_literal_constant )?
//    | 'O' int_literal_constant ( T_PERIOD int_literal_constant )?
//    | 'Z' int_literal_constant ( T_PERIOD int_literal_constant )?
//    | 'F' int_literal_constant T_PERIOD int_literal_constant
//    | 'E' int_literal_constant T_PERIOD int_literal_constant ( 'E' int_literal_constant )?
//    | 'EN' int_literal_constant T_PERIOD int_literal_constant ( 'E' int_literal_constant )?
//    | 'ES' int_literal_constant T_PERIOD int_literal_constant ( 'E' int_literal_constant )?
//    | 'G' int_literal_constant T_PERIOD int_literal_constant ( 'E' int_literal_constant )?
//    | 'L' int_literal_constant
//    | 'A' ( int_literal_constant )?
//    | 'D' int_literal_constant T_PERIOD int_literal_constant
//    | 'DT' ( char_literal_constant )? ( T_LPAREN v_list T_RPAREN )?
//    ;

/* TODO: inlined w in R1005
// R1006
w
	:	int_literal_constant
	;
*/

/* TODO: inlined m in R1005
// R1007
m
	:	int_literal_constant
	;
*/

/* TODO: inlined d in R1005
// R1008
d
	:	int_literal_constant
	;
*/

/* TODO: inlined e in R1005
// R1009
e
	:	int_literal_constant
	;
*/

/* TODO: only need v_list
// R1010
v
	:	signed_int_literal_constant
	;
*/

v_list
    :   signed_int_literal_constant ( T_COMMA signed_int_literal_constant )*
    ;

// R1011
// TODO putback
control_edit_desc
	:	//position_edit_desc
	|	( int_literal_constant )? T_SLASH
	|	T_COLON
//	|	sign_edit_desc
//	|	signed_int_literal_constant 'P'
	|	blank_interp_edit_desc
	|	round_edit_desc
	|	decimal_edit_desc
	;

/* TODO: inlined in R1011
// R1012
k
	:	signed_int_literal_constant
	;
*/

// R1013
//position_edit_desc
//	:	'T' int_literal_constant
//	|	'TL' int_literal_constant
//	|	'TR' int_literal_constant
//	|	int_literal_constant 'X'
//	;

/* TODO: inlined in R1013
// R1014
n
	:	int_literal_constant
	;
*/

// TODO convert to fragment
// R1015
//sign_edit_desc
//	:	'SS'
//	|	'SP'
//	|	'S'
//	;

// R1016
blank_interp_edit_desc
	:	'BN'
	|	'BZ'
	;

// R1017
round_edit_desc
	:	'RU'
	|	'RD'
	|	'RZ'
	|	'RN'
	|	'RC'
	|	'RP'
	;

// R1018
decimal_edit_desc
	:	'DC'
	|	'DP'
	;

// R1019
char_string_edit_desc
	:	char_literal_constant
	;

/*
Section 11:
 */

// R1101
// specification_part made non-optional to remove END ambiguity (as can be empty)
main_program
	:	( program_stmt )?
		specification_part
//		( execution_part )?
//		( internal_subprogram_part )?
		end_program_stmt
	;

// R1102
program_stmt
	:	T_PROGRAM
		program_name
	;

// R1103
end_program_stmt
options {k=2;}
	:	(T_END T_PROGRAM) => T_END T_PROGRAM ( program_name )?
    |	T_ENDPROGRAM ( program_name )?
    |	T_END
	;
	
program_name
    :    T_IDENT
    ;

// R1104
// TODO putback
// specification_part made non-optional to remove END ambiguity (as can be empty)
module
	:	module_stmt
		specification_part
//		( module_subprogram_part )?
		end_module_stmt
	;

// R1105
module_stmt
	:	T_MODULE ( T_IDENT )?
	;

// R1106
end_module_stmt
options {k=2;}
        :       (T_END T_MODULE) => T_END T_MODULE ( T_IDENT )?
        |       T_ENDMODULE ( T_IDENT )?
        |       T_END
        ;

// R1107
module_subprogram_part
	:	contains_stmt
		module_subprogram
		( module_subprogram )*
	;

// R1108
module_subprogram
        :       function_or_subroutine_subprogram
        ;

// R1109
use_stmt
    :    T_USE ( ( T_COMMA module_nature )? T_COLON_COLON )? T_IDENT ( T_COMMA rename_list )?
    |    T_USE ( ( T_COMMA module_nature )? T_COLON_COLON )? T_IDENT T_COMMA T_ONLY T_COLON ( only_list )?
    ;

// R1110
module_nature
	:	T_INTRINSIC
	|	T_NON_INTRINSIC
	;

// R1111
// inlined local_defined_operator with  T_DEFINED_OP 
// inlined use_defined_operator with  T_DEFINED_OP 
rename
	:	local_name T_EQ_GT use_name
	|	T_OPERATOR T_LPAREN T_DEFINED_OP T_RPAREN T_EQ_GT
		T_OPERATOR T_LPAREN T_DEFINED_OP T_RPAREN
	;

rename_list
    :    rename ( T_COMMA rename )*
    ;

local_name
    :    name
    ;

use_name
    :    name
    ;

// R1112
// TODO putback
only
	:	generic_spec
//	|	only_use_name
//	|	rename
	;

only_list
    :    only ( T_COMMA only )*
    ;

// R1113
only_use_name
	:	use_name
	;

// R1114 inlined local_defined_operator in R1111 as T_DEFINED_OP

// R1115 inlined use_defined_operator in R1111 as T_DEFINED_OP

// R1116
block_data
	:	block_data_stmt
//		( specification_part )?
		end_block_data_stmt
	;

// R1117
block_data_stmt
options {k=2;}
	:	(T_BLOCK T_DATA) => T_BLOCK T_DATA ( T_IDENT )?
	|   T_BLOCKDATA ( T_IDENT )?
	;

// R1118
end_block_data_stmt
options {k=2;}
	:   (T_END T_BLOCK) => T_END T_BLOCK T_DATA ( T_IDENT )?
	|   (T_ENDBLOCK T_DATA) => T_ENDBLOCK T_DATA ( T_IDENT )?
	|   (T_END T_BLOCKDATA) => T_END T_BLOCKDATA ( T_IDENT )?
	|   T_ENDBLOCKDATA ( T_IDENT )?
	|	T_END
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
	:   T_INTERFACE ( generic_spec )?
	|	T_ABSTRACT T_INTERFACE
	;

// R1204
end_interface_stmt
options {k=2;}
	: (T_END T_INTERFACE) => T_END T_INTERFACE ( generic_spec )?
	| T_ENDINTERFACE ( generic_spec )?
	;

// R1205
// TODO putback
interface_body
	:	(prefix)? function_stmt_body /* ( specification_part )? */ end_function_stmt
//	|	subroutine_stmt ( specification_part )? end_subroutine_stmt
	;

// R1206
procedure_stmt
	:	( T_MODULE )? T_PROCEDURE procedure_name_list
	;

procedure_name
    :    name
    ;

procedure_name_list
    :    procedure_name ( T_COMMA procedure_name )*
    ;

// R1207
generic_spec
	:	generic_name
	|	T_OPERATOR T_LPAREN defined_operator T_RPAREN
	|	T_ASSIGNMENT T_LPAREN T_EQUALS T_RPAREN
	|	dtio_generic_spec
	;

generic_name
    :   name
    ;

// R1208
dtio_generic_spec
	:	T_READ T_LPAREN T_FORMATTED T_RPAREN
	|	T_READ T_LPAREN T_UNFORMATTED T_RPAREN
	|	T_WRITE T_LPAREN T_FORMATTED T_RPAREN
	|	T_WRITE T_LPAREN T_UNFORMATTED T_RPAREN
	;

// R1209
import_stmt
    :    T_IMPORT ( ( T_COLON_COLON )? import_name_list )?
    ;

import_name
    :    name
    ;

import_name_list
    :    import_name ( T_COMMA import_name )*
    ;

// R1210
external_stmt
	:	T_EXTERNAL ( T_COLON_COLON )? external_name_list
	;

external_name
    :    name
    ;

external_name_list
    :    external_name ( T_COMMA external_name )*
    ;

// R1211
procedure_declaration_stmt
    :    T_PROCEDURE T_LPAREN ( proc_interface )? T_RPAREN
            ( ( T_COMMA proc_attr_spec )* T_COLON_COLON )? proc_decl_list
    ;

// R1212
proc_interface
	:	interface_name
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
proc_decl
    :    procedure_entity_name ( T_EQ_GT null_init )?
    ;

procedure_entity_name
    :    name
    ;

proc_decl_list
    :    proc_decl ( T_COMMA proc_decl )*
    ;

// R1215
interface_name
	:	name
	;

// R1216
intrinsic_stmt
	:	T_INTRINSIC
		( T_COLON_COLON )?
		intrinsic_procedure_name_list
	;

intrinsic_procedure_name
    : name
    ;

intrinsic_procedure_name_list
    :    intrinsic_procedure_name ( T_COMMA intrinsic_procedure_name )*
    ;

// R1217
function_reference
	:	procedure_designator
		T_LPAREN
		( actual_arg_spec_list )?
		T_RPAREN
	;

// R1218
call_stmt
    :    T_CALL procedure_designator ( T_LPAREN ( actual_arg_spec_list )? T_RPAREN )?
    ;

// R1219
// TODO putback
procedure_designator
	:	procedure_name
//	|	proc_component_ref
//	|	data_ref T_PERCENT binding_name
	;

binding_name
    :    name
    ;

binding_name_list
    :    binding_name ( T_COMMA binding_name )*
    ;

// R1220
actual_arg_spec
    : ( keyword T_EQUALS )? actual_arg
    ;

actual_arg_spec_list
    :    actual_arg_spec ( T_COMMA actual_arg_spec )*
    ;

// R1221
// TODO putback
actual_arg
	:	expr
//	|	variable
//	|	procedure_name
//	|	proc_component_ref
//	|	alt_return_spec
	;

// R1222
alt_return_spec
	:	T_ASTERISK label
	;

// R1223
// TODO putback
// 1. factored out optional prefix in function_stmt from function_subprogram
// 2. specification_part made non-optional to remove END ambiguity (as can be empty)
function_subprogram_body
	:	function_stmt_body
		specification_part
//		( execution_part )?
//		( internal_subprogram_part )?
		end_function_stmt
	;

// R1224
// factored out optional prefix from function_stmt
function_stmt_body
	:	T_FUNCTION T_IDENT
		T_LPAREN ( dummy_arg_name_list )? T_RPAREN ( suffix )?
	;

// R1225
proc_language_binding_spec
	:	language_binding_spec
	;

// R1226
dummy_arg_name
	:	T_IDENT
	;

// R1227
prefix
	:	prefix_spec ( prefix_spec )*
	;

// R1228
prefix_spec
	:	declaration_type_spec
	|	T_RECURSIVE
	|	T_PURE
	|	T_ELEMENTAL
	;

// R1229
// TODO putback
suffix
    :    proc_language_binding_spec ( T_RESULT T_LPAREN result_name T_RPAREN )?
//    | T_RESULT T_LPAREN result_name T_RPAREN ( proc_language_binding_spec )?
    ;

result_name
    :    name
    ;

// R1230
end_function_stmt
options {k=2;}
	: (T_END T_FUNCTION) => T_END T_FUNCTION ( T_IDENT )?
	| T_ENDFUNCTION ( T_IDENT )?
	| T_END
	;

// R1231
// TODO putback
// 1. factored out optional prefix in subroutine_stmt from subroutine_subprogram
// 2. specification_part made non-optional to remove END ambiguity (as can be empty)
subroutine_subprogram_body
	:	subroutine_stmt_body
		specification_part
//		( execution_part )?
//		( internal_subprogram_part )?
		end_subroutine_stmt
	;

// R1232
// factored out optional prefix from subroutine_stmt
subroutine_stmt_body
    :     T_SUBROUTINE T_IDENT
          ( T_LPAREN ( dummy_arg_list )? T_RPAREN ( proc_language_binding_spec )? )?
    ;

// R1233
dummy_arg
	:	dummy_arg_name
	|	T_ASTERISK
	;

dummy_arg_list
    :    dummy_arg ( T_COMMA dummy_arg )*
    ;

// R1234
end_subroutine_stmt
options {k=2;}
    : (T_END T_SUBROUTINE) => T_END T_SUBROUTINE ( T_IDENT )?
    | T_ENDSUBROUTINE ( T_IDENT )?
    | T_END
    ;

// R1235
// TODO putback
entry_stmt
    :    T_ENTRY entry_name
          ( T_LPAREN ( dummy_arg_list )? T_RPAREN /*( suffix )? */ )?
    ;

entry_name
    :    T_IDENT
    ;

// R1236
// ERR_CHK 1236 scalar_int_expr replaced by expr
return_stmt
	:	T_RETURN ( expr )?
	;

// R1237
contains_stmt
	:	T_CONTAINS
	;

// R1238
// ERR_CHK 1239 scalar_expr replaced by expr
stmt_function_stmt
	:	T_IDENT
		T_LPAREN
		( dummy_arg_name_list )?
		T_RPAREN
		T_EQUALS
		expr
	;

/*
 * Lexer rules
 */

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

// R427 from char-literal-constant
T_CHAR_CONSTANT
        : '\'' ( Rep_Char )* '\''
        | '\"' ( Rep_Char )* '\"'
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


//
// line breaks are OK before equals
//
T_FILE_EQUALS   : 'FILE' '='    ;
T_ACCESS_EQUALS : 'ACCESS' '='  ;
T_ACTION_EQUALS : 'ACTION' '='  ;
T_ADVANCE_EQUALS: 'ADVANCE' '=' ;
T_ASYNCHRONOUS_EQUALS: 'ASYNCHRONOUS' '=' ;
T_BLANK_EQUALS  : 'BLANK' '='   ;
T_DECIMAL_EQUALS: 'DECIMAL' '=' ;
T_DELIM_EQUALS  : 'DELIM' '='   ;
T_DIRECT_EQUALS : 'DIRECT' '='  ;
T_ENCODING_EQUALS: 'ENCODING' '=' ;
//T_END_EQUALS    : 'END' '='     ;
T_EOR_EQUALS    : 'EOR' '='     ;
T_ERR_EQUALS    : 'ERR' '='     ;
T_ERRMSG_EQUALS : 'ERRMSG' '='  ;
T_EXIST_EQUALS  : 'EXIST' '='   ;
T_FORM_EQUALS   : 'FORM' '='    ;
T_FORMATTED_EQUALS: 'FORMATTED' '=' ;
T_ID_EQUALS     : 'ID' '='      ;
T_IOLENGTH_EQUALS: 'IOLENGTH' '=' ;
T_IOMSG_EQUALS  : 'IOMSG' '='   ;
T_IOSTAT_EQUALS : 'IOSTAT' '='  ;
T_KIND_EQUALS   : 'KIND' '='    ;
T_LEN_EQUALS    : 'LEN' '='     ;
T_NAME_EQUALS   : 'NAME' '='    ;
T_NAMED_EQUALS  : 'NAMED' '='   ;
T_NEXTREC_EQUALS: 'NEXTREC' '=' ;
T_NML_EQUALS    : 'NML' '='     ;
T_NUMBER_EQUALS : 'NUMBER' '='  ;
T_OPENED_EQUALS : 'OPENED' '='  ;
T_PAD_EQUALS    : 'PAD' '='     ;
T_PENDING_EQUALS: 'PENDING' '=' ;
T_POS_EQUALS    : 'POS' '='     ;
T_POSITION_EQUALS: 'POSITION' '=' ;
T_READ_EQUALS   : 'READ' '='    ;
T_READWRITE_EQUALS: 'READWRITE' '=' ;
T_REC_EQUALS    : 'REC' '='     ;
T_RECL_EQUALS   : 'RECL' '='    ;
T_ROUND_EQUALS  : 'ROUND' '='   ;
T_SEQUENTIAL_EQUALS: 'SEQUENTIAL' '=' ;
T_SIGN_EQUALS   : 'SIGN' '='    ;
T_SIZE_EQUALS   : 'SIZE' '='    ;
T_SOURCE_EQUALS : 'SOURCE' '='  ;
T_STAT_EQUALS   : 'STAT' '='    ;
T_STATUS_EQUALS : 'STATUS' '='  ;
T_STREAM_EQUALS : 'STREAM' '='  ;
T_WRITE_EQUALS  : 'WRITE' '='   ;
T_UNFORMATTED_EQUALS: 'UNFORMATTED' '=' ;
T_UNIT_EQUALS   : 'UNIT' '='    ;

//T_EDIT_BN	: 'BN'  ;
//T_EDIT_BZ	: 'BZ'  ;
//T_EDIT_DC	: 'DC'  ;
//T_EDIT_DP	: 'DP'  ;
//T_EDIT_DT	: 'DT'  ;
//T_EDIT_RU	: 'RU'  ;
//T_EDIT_RD	: 'RD'  ;
//T_EDIT_RZ	: 'RZ'  ;
//T_EDIT_RN	: 'RN'  ;
//T_EDIT_RC	: 'RC'  ;
//T_EDIT_RP	: 'RP'  ;
//T_EDIT_S	: 'S'   ;
//T_EDIT_SP	: 'SP'  ;
//T_EDIT_SS	: 'SS'  ;
//T_EDIT_T	: 'T'   ;
//T_EDIT_TL	: 'TL'  ;
//T_EDIT_TR	: 'TR'  ;
//T_EDIT_X	: 'X'   ;


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

// R304
T_IDENT
options {k=1;}
	:	Letter ( Alphanumeric_Character )*
	;

LINE_COMMENT
    : '!' ~('\n'|'\r')* '\r'? '\n' {channel=99;}
    ;
