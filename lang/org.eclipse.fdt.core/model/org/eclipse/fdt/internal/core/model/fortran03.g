/*
 * NOTES
 *
 * R303 underscore - added _ to rule (what happened to it?)
 * R1209 import-stmt: MISSING a ]
 *
 */


grammar Fortran03Parser;

options {
    language=Java;
}

/*
Section 1:
 */

/*
Section 2:
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
external_subprogram
	:	function_subprogram
	|	subroutine_subprogram
	;

// R204
specification_part
	:	( use_stmt )*
		( import_stmt )*
		( implicit_part )?
		( declaration_construct )*
	;

// R205
implicit_part
	:	( implicit_part_stmt )*
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
	:	function_subprogram
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
action_stmt
	:	allocate_stmt
	|	assignment_stmt
	|	backspace_stmt
	|	call_stmt
	|	close_stmt
	|	continue_stmt
	|	cycle_stmt
	|	deallocate_stmt
	|	endfile_stmt
	|	end_function_stmt
	|	end_program_stmt
	|	end_subroutine_stmt
	|	exit_stmt
	|	flush_stmt
	|	forall_stmt
	|	goto_stmt
	|	if_stmt
	|	inquire_stmt
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

// R303 underscore converted to fragment

// R304
name
	:	T_IDENT
	;

// R305
constant
	:	literal_constant
	|	named_constant
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

// R307
named_constant
	:	name
	;

// R308
int_constant
	:	constant
	;

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
defined_operator
	:	defined_unary_op
	|	defined_binary_op
	|	extended_intrinsic_op
	;

// R312
extended_intrinsic_op
	:	intrinsic_operator
	;


/* TODO: Done - converted to terminal
R313  label                        is digit [ digit [ digit [ digit [ digit ] ] ] ]
 */

label_list
    :    LABEL ( LABEL )*
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
type_param_value
	:	scalar_int_expr
	|	STAR
	|	COLON
	;

// - C101 shall be a scalar
scalar_int_expr
	:	int_expr
	;

scalar_expr
    :    expr
    ;

// R403
intrinsic_type_spec
	:	INTEGER ( kind_selector )?
	|	REAL ( kind_selector )?
	|	DOUBLE PRECISION
	|	COMPLEX ( kind_selector )?
	|	CHARACTER ( char_selector )?
	|	LOGICAL ( kind_selector )?
	;

// R404
kind_selector
    : LPAREN ( KIND_EQ )? scalar_int_initialization_expr RPAREN
    ;

// TODO: turn into terminal
// R405
signed_int_literal_constant
	:	( sign )?
		int_literal_constant
	;

// TODO: turn into terminal
// R406
int_literal_constant
	:	DIGIT_STRING
		( kind_param )?
	;

// TODO: turn into terminal
// R407
kind_param
	:	DIGIT_STRING
	|	scalar_int_constant_name
	;

// TODO: turn into terminal
// R408
signed_digit_string
	:	( sign )?
		DIGIT_STRING
	;

// R409 digit_string turned into fragment

// R410
sign
	:	PLUS
	|	QUESTION
	;

// R411
boz_literal_constant
	:	BINARY_CONSTANT
	|	OCTAL_CONSTANT
	|	HEX_CONSTANT
	;

/* TODO: Done - converted to terminal
R412 binary-constant              is B ' digit [ digit ] ... '
                                  or B " digit [ digit ] ... "
 */

/* TODO: Done - converted to terminal
R413 octal-constant               is O ' digit [ digit ] ... '
                                  or O " digit [ digit ] ... "
 */

/* TODO: Done - converted to terminal
R414 hex_constant  : 'Z' SINGLE_QUOTE hex_digit ( hex_digit )* SINGLE_QUOTE
                   | 'Z' DOUBLE_QUOTE hex_digit ( hex_digit )* DOUBLE_QUOTE
                   ;

R415 hex_digit     :  DIGIT | 'A' | 'B' | 'C' | 'D' | 'E' | 'F' ;
*/

// R416
signed_real_literal_constant
	:	( sign )? real_literal_constant
	;

// R417
real_literal_constant
    :   significand ( exponent_letter exponent )? ( kind_param )?
    |   DIGIT_STRING exponent_letter exponent ( kind_param )?
    ;

// R418
significand
	:	DIGIT_STRING
		DOT
		( DIGIT_STRING )?
	|	DOT
		DIGIT_STRING
	;

// R419
exponent_letter
	:	'E'
	|	'D'
	;

// R420
exponent
	:	signed_digit_string
	;

// R421
complex_literal_constant
	:	LPAREN real_part COMMA imag_part RPAREN
	;

// R422
real_part
	:	signed_int_literal_constant
	|	signed_real_literal_constant
	|	named_constant
	;

// R423
imag_part
	:	signed_int_literal_constant
	|	signed_real_literal_constant
	|	named_constant
	;

// R424
char_selector
    :    length_selector
    |    LPAREN LEN_EQ type_param_value COMMA KIND_EQ scalar_int_initialization_expr RPAREN
    |    LPAREN type_param_value COMMA ( KIND_EQ )? scalar_int_initialization_expr RPAREN
    |    LPAREN KIND_EQ scalar_int_initialization_expr ( COMMA LEN_EQ type_param_value )? RPAREN
    ;

// R425
length_selector
	:	LPAREN
		( LEN_EQ )?
		type_param_value
		RPAREN
	|	STAR
		char_length
		( COMMA )?
	;

// R426
char_length
	:	LPAREN
		type_param_value
		RPAREN
	|	scalar_int_literal_constant
	;

scalar_int_literal_constant
    :    int_literal_constant
    ;

/* TODO: made a terminal
// R427
char_literal_constant
    :    ( kind_param ) SINGLE_QUOTE ( REP_CHAR )* SINGLE_QUOTE
    |    ( kind_param ) DOUBLE_QUOTE ( REP_CHAR )* DOUBLE_QUOTE
    ;
 */

// TODO: make a terminal
// R428
logical_literal_constant
    :    DOT_TRUE ( kind_param )?
    |    DOT_FALSE ( kind_param )?
    ;

// R429
derived_type_def
	:	derived_type_stmt
		( type_param_def_stmt )*
		( private_or_sequence )*
		( component_part )?
		( type_bound_procedure_part )?
		end_type_stmt
	;

// R430
derived_type_stmt
    :    TYPE ( ( COMMA type_attr_spec_list )? COLONCOLON )? type_name
         ( LPAREN type_param_name_list RPAREN )?
    ;

type_attr_spec_list
    :    type_attr_spec ( type_attr_spec )*
    ;

type_attr_name
    :    name
    ;

type_attr_name_list
    :    type_attr_name ( type_attr_name )*
    ;

// R431
type_attr_spec
	:	access_spec
	|	EXTENDS LPAREN parent_type_name RPAREN
	|	ABSTRACT
	|	BIND LPAREN C RPAREN
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
	:	END TYPE ( type_name )?
	;

type_name
    :    name
    ;

// R434
sequence_stmt
	:	SEQUENCE
	;

// R435
type_param_def_stmt
	:	INTEGER ( kind_selector )? COMMA type_param_attr_spec COLONCOLON type_param_decl_list
	;

// R436
type_param_decl
    :    type_param_name ( EQUAL scalar_int_initialization_expr )?
    ;

type_param_decl_list
    :    type_param_decl ( type_param_decl )*
    ;

type_param_name
    :    name
    ;

type_param_name_list
    : type_param_name ( type_param_name )?
    ;

// R437
type_param_attr_spec
	:	KIND
	|	LEN
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
    :    declaration_type_spec ( ( COMMA component_attr_spec_list )? COLONCOLON )? component_decl_list
    ;

// R441
component_attr_spec
	:	POINTER
	|	DIMENSION LPAREN component_array_spec RPAREN
	|	ALLOCATABLE
	|	access_spec
	;

component_attr_spec_list
    :    component_attr_spec ( component_attr_spec )*
    ;

// R442
component_decl
    :    component_name ( LPAREN component_array_spec RPAREN )?
                        ( STAR char_length )? ( component_initialization )?
    ;

component_decl_list
    :   component_decl ( component_decl )*
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
    :    deferred_shape_spec (deferred_shape_spec )*
    ;

// R444
component_initialization
	:	EQUAL
		initialization_expr
	|	EQUAL_GREATER
		null_init
	;

// R445
proc_component_def_stmt
	:	PROCEDURE LPAREN ( proc_interface )? RPAREN COMMA
		    proc_component_attr_spec_list COLONCOLON proc_decl_list
	;

// R446
proc_component_attr_spec
    :    POINTER
    |    PASS ( LPAREN arg_name RPAREN )?
    |    NOPASS
    |    access_spec
    ;

proc_component_attr_spec_list
    :    proc_component_attr_spec ( proc_component_attr_spec )*
    ;

// R447
private_components_stmt
	:	PRIVATE
	;

// R448
type_bound_procedure_part
	:	contains_stmt ( binding_private_stmt )? proc_binding_stmt ( proc_binding_stmt )*
	;

// R449
binding_private_stmt
	:	PRIVATE
	;

// R450
proc_binding_stmt
	:	specific_binding
	|	generic_binding
	|	final_binding
	;

// R451
specific_binding
    : PROCEDURE ( LPAREN interface_name RPAREN )?
      ( ( COMMA binding_attr_list )? COLONCOLON )?
      binding_name ( EQUAL_GREATER procedure_name )?
    ;

// R452
generic_binding
    :    GENERIC ( COMMA access_spec )? COLONCOLON generic_spec EQUAL_GREATER binding_name_list
    ;

// R453
binding_attr
    : PASS ( LPAREN arg_name RPAREN )?
    | NOPASS
    | NON OVERRIDABLE
    | DEFERRED
    | access_spec
    ;

binding_attr_list
    :    binding_attr ( binding_attr )*
    ;

arg_name
    : name
    ;

// R454
final_binding
	:	FINAL
		( COLONCOLON )?
		final_subroutine_name_list
	;

final_subroutine_name_list
    :    name ( name )*
    ;

// R455
derived_type_spec
    : type_name ( LPAREN type_param_spec_list RPAREN )?
    ;

// R456
type_param_spec
    : ( keyword EQUAL )? type_param_value
    ;

type_param_spec_list
    :    type_param_spec ( type_param_spec )*
    ;

// R457
structure_constructor
	:	derived_type_spec
		LPAREN
		( component_spec_list )?
		RPAREN
	;

// R458
component_spec
    :    ( keyword EQUAL )? component_data_source
    ;

component_spec_list
    :    component_spec ( component_spec )*
    ;

// R459
component_data_source
	:	expr
	|	data_target
	|	proc_target
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
	:	ENUMCOMMA
		BIND
		LPAREN
		C
		RPAREN
	;

// R462
enumerator_def_stmt
	:	ENUMERATOR
		( COLONCOLON )?
		enumerator_list
	;

// R463
enumerator
    :    named_constant ( EQUAL scalar_int_initialization_expr )?
    ;

enumerator_list
    :   enumerator ( enumerator )*
    ;

// R464
end_enum_stmt
	:	END
		ENUM
	;

// R465
array_constructor
	:	LPAREN SLASH ac_spec SLASH RPAREN
	|	left_square_bracket ac_spec right_square_bracket
	;


// R466
ac_spec
    : type_spec COLONCOLON
    | (type_spec COLONCOLON)? ac_value_list
    ;

// R467
left_square_bracket
	:	LBRACKET
	;

// R468
right_square_bracket
	:	RBRACKET
	;

// R469
ac_value
	:	expr
	|	ac_implied_do
	;

ac_value_list
    :   ac_value ( ac_value )*
    ;

// R470
ac_implied_do
	:	LPAREN ac_value_list COMMA ac_implied_do_control RPAREN
	;

// R471
ac_implied_do_control
    :    ac_do_variable EQUAL scalar_int_expr COMMA scalar_int_expr ( COMMA scalar_int_expr )?
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
    :    declaration_type_spec ( ( COMMA attr_spec )* COLONCOLON )? entity_decl_list
    ;

// R502
declaration_type_spec
	:	intrinsic_type_spec
	|	TYPE
		LPAREN
		derived_type_spec
		RPAREN
	|	CLASS
		LPAREN
		derived_type_spec
		RPAREN
	|	CLASS
		LPAREN
		STAR
		RPAREN
	;

// R503
attr_spec
	:	access_spec
	|	ALLOCATABLE
	|	ASYNCHRONOUS
	|	DIMENSION LPAREN array_spec RPAREN
	|	EXTERNAL
	|	INTENT LPAREN intent_spec RPAREN
	|	INTRINSIC
	|	language_binding_spec
	|	OPTIONAL
	|	PARAMETER
	|	POINTER
	|	PROTECTED
	|	SAVE
	|	TARGET
	|	VALUE
	|	VOLATILE
	;


// R504
entity_decl
    : object_name ( LPAREN array_spec RPAREN )? ( STAR char_length )? ( initialization )?
    | function_name ( STAR char_length )?
    ;

entity_decl_list
    :    entity_decl ( entity_decl )*
    ;

// R505
object_name
	:	name
	;

object_name_list
    :    object_name ( object_name )*
    ;

// R506
initialization
	:	EQUAL
		initialization_expr
	|	EQUAL_GREATER
		null_init
	;

// R507
null_init
	:	function_reference
	;

// R508
access_spec
	:	PUBLIC
	|	PRIVATE
	;

// R509
language_binding_spec
    : BIND LPAREN C ( COMMA name EQUAL scalar_char_initialization_expr )? RPAREN
    ;

// R510
array_spec
	:	explicit_shape_spec_list
	|	assumed_shape_spec_list
	|	deferred_shape_spec_list
	|	assumed_size_spec
	;

// R511
explicit_shape_spec
    : ( lower_bound COLON )? upper_bound
    ;

explicit_shape_spec_list
    : explicit_shape_spec ( explicit_shape_spec )*
    ;

// R512
lower_bound
	:	specification_expr
	;

// R513
upper_bound
	:	specification_expr
	;

// R514
assumed_shape_spec
	:	( lower_bound )? COLON
	;

assumed_shape_spec_list
    :    assumed_shape_spec ( assumed_shape_spec )*
    ;

// R515
deferred_shape_spec
	:	COLON
	;

// R516
assumed_size_spec
    : ( explicit_shape_spec_list COMMA )? ( lower_bound COLON )? STAR
    ;

// R517
intent_spec
	:	IN
	|	OUT
	|	INOUT
	;

// R518
access_stmt
    :    access_spec ( ( COLONCOLON )? access_id_list )?
    ;

// R519
access_id
	:	use_name
	|	generic_spec
	;

access_id_list
    :    access_id ( access_id )*
    ;

// R520
allocatable_stmt
    : ALLOCATABLE ( COLONCOLON )? object_name ( LPAREN deferred_shape_spec_list RPAREN )?
         ( COMMA object_name ( LPAREN deferred_shape_spec_list RPAREN )? )*
    ;

// R521
asynchronous_stmt
	:	ASYNCHRONOUS
		( COLONCOLON )?
		object_name_list
	;

// R522
bind_stmt
	:	language_binding_spec
		( COLONCOLON )?
		bind_entity_list
	;

entity_name
    :    name
    ;

entity_name_list
    :    entity_name ( entity_name )*
    ;

// R523
bind_entity
	:	entity_name
	|	SLASH common_block_name SLASH
	;

common_block_name
    :    name
    ;

bind_entity_list
    :    bind_entity ( bind_entity )*
    ;

// R524
data_stmt
    :    DATA data_stmt_set ( ( COMMA )? data_stmt_set )*
    ;

// R525
data_stmt_set
	:	data_stmt_object_list
		SLASH
		data_stmt_value_list
		SLASH
	;

// R526
data_stmt_object
	:	variable
	|	data_implied_do
	;

data_stmt_object_list
    :    data_stmt_object ( data_stmt_object )*
    ;

// R527
data_implied_do
    : LPAREN data_i_do_object_list COMMA data_i_do_variable EQUAL
         scalar_int_expr COMMA scalar_int_expr ( COMMA scalar_int_expr )? RPAREN
    ;

// R528
data_i_do_object
	:	array_element
	|	scalar_structure_component
	|	data_implied_do
	;

data_i_do_object_list
    :   data_i_do_object ( data_i_do_object )*
    ;

// R529
data_i_do_variable
	:	scalar_int_variable
	;

// R530
data_stmt_value
    : ( data_stmt_repeat STAR )? data_stmt_constant
    ;

data_stmt_value_list
    :    data_stmt_value ( data_stmt_value )*
    ;

// R531
data_stmt_repeat
	:	scalar_int_constant
	|	scalar_int_constant_subobject
	;

scalar_int_constant
    :    int_constant
    ;

scalar_int_constant_subobject
    :    int_constant
    ;

scalar_int_constant_name
    :    name
    ;

// R532
data_stmt_constant
	:	scalar_constant
	|	scalar_constant_subobject
	|	signed_int_literal_constant
	|	signed_real_literal_constant
	|	null_init
	|	structure_constructor
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
    :    DIMENSION ( COLONCOLON )? array_name LPAREN array_spec RPAREN
             ( COMMA array_name LPAREN array_spec RPAREN )*
    ;

array_name
    :    name
    ;

// R536
intent_stmt
	:	INTENT LPAREN intent_spec RPAREN ( COLONCOLON )? dummy_arg_name_list
	;

dummy_arg_name_list
    : dummy_arg_name ( dummy_arg_name )*
    ;

// R537
optional_stmt
	:	OPTIONAL ( COLONCOLON )? dummy_arg_name_list
	;

// R538
parameter_stmt
	:	PARAMETER LPAREN named_constant_def_list RPAREN
	;

named_constant_def_list
    :    named_constant_def ( named_constant_def )*
    ;

// R539
named_constant_def
	:	named_constant EQUAL initialization_expr
	;

// R540
pointer_stmt
	:	POINTER ( COLONCOLON )? pointer_decl_list
	;

pointer_decl_list
    :    pointer_decl ( pointer_decl )*
    ;

// R541
pointer_decl
    : object_name ( LPAREN deferred_shape_spec_list RPAREN )?
    | proc_entity_name
    ;

proc_entity_name
    :    name
    ;

// R542
protected_stmt
	:	PROTECTED
		( COLONCOLON )?
		entity_name_list
	;

// R543
save_stmt
    : SAVE ( ( COLONCOLON )? saved_entity_list )?
    ;

// R544
saved_entity
	:	object_name
	|	proc_pointer_name
	|	SLASH common_block_name SLASH
	;

saved_entity_list
    :    saved_entity ( saved_entity )*
    ;

// R545
proc_pointer_name
	:	name
	;

// R546
target_stmt
    : TARGET ( COLONCOLON )? object_name ( LPAREN array_spec RPAREN )?
             ( COMMA object_name ( LPAREN array_spec RPAREN )? )*
    ;

// R547
value_stmt
	:	VALUE ( COLONCOLON )? dummy_arg_name_list
	;

// R548
volatile_stmt
	:	VOLATILE ( COLONCOLON )? object_name_list
	;

// R549
implicit_stmt
	:	IMPLICIT implicit_spec_list
	|	IMPLICIT NONE
	;

// R550
implicit_spec
	:	declaration_type_spec LPAREN letter_spec_list RPAREN
	;

implicit_spec_list
    :    implicit_spec ( implicit_spec )*
    ;

// R551
letter_spec 
    : LETTER ( QUESTION LETTER )?
    ;

letter_spec_list
    :    letter_spec ( letter_spec )*
    ;

// R552
namelist_stmt
    : NAMELIST SLASH namelist_group_name SLASH namelist_group_object_list
         ( ( COMMA )? SLASH namelist_group_name SLASH namelist_group_object_list )*
    ;

// R553
namelist_group_object
	:	variable_name
	;

namelist_group_object_list
    :    namelist_group_object ( namelist_group_object )*
    ;

namelist_group_name
    :    name
    ;

// R554
equivalence_stmt
	:	EQUIVALENCE equivalence_set_list
	;

// R555
equivalence_set
	:	LPAREN equivalence_object COMMA equivalence_object_list RPAREN
	;

equivalence_set_list
    :    equivalence_set ( equivalence_set )*
    ;

// R556
equivalence_object
	:	variable_name
	|	array_element
	|	substring
	;

equivalence_object_list
    :    equivalence_object ( equivalence_object )
    ;

// R557
common_stmt
    : COMMON ( SLASH ( common_block_name )? SLASH )? common_block_object_list
         ( ( COMMA )? SLASH ( common_block_name )? SLASH common_block_object_list )*
    ;

// R558
common_block_object
    : variable_name ( LPAREN explicit_shape_spec_list RPAREN )?
    | proc_pointer_name
    ;

common_block_object_list
    :    common_block_object ( common_block_object )*
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
designator
	:	object_name
	|	array_element
	|	array_section
	|	structure_component
	|	substring
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
		LPAREN
		substring_range
		RPAREN
	;

// R610
parent_string
	:	scalar_variable_name
	|	array_element
	|	scalar_structure_component
	|   scalar_constant
	;

scalar_variable_name
    :    name
    ;

// R611
substring_range
	:	( scalar_int_expr )?
		COLON
		( scalar_int_expr )?
	;

// R612
data_ref
    : part_ref ( PERCENT part_ref )*
    ;

// R613
part_ref
    : part_name ( LPAREN section_subscript_list RPAREN )?
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
		PERCENT
		type_param_name
	;

// R616
array_element
	:	data_ref
	;

// R617
array_section
    :    data_ref ( LPAREN substring_range RPAREN )?
    ;

// R618
subscript
	:	scalar_int_expr
	;

// R619
section_subscript
	:	subscript
	|	subscript_triplet
	|	vector_subscript
	;

section_subscript_list
    :    section_subscript ( section_subscript )*
    ;

// R620
subscript_triplet
    : ( subscript )? COLON ( subscript )? ( COLON stride )?
    ;

// R621
stride
	:	scalar_int_expr
	;

// R622
vector_subscript
	:	int_expr
	;

// R623
allocate_stmt
    :    ALLOCATE LPAREN ( type_spec COLONCOLON )? allocation_list ( COMMA alloc_opt_list )? RPAREN
    ;

// R624
alloc_opt
	:	STAT EQUAL stat_variable
	|	ERRMSG EQUAL errmsg_variable
	|	SOURCE EQUAL source_expr
	;

alloc_opt_list
    :    alloc_opt ( alloc_opt )*
    ;

// R625
stat_variable
	:	scalar_int_variable
	;

// R626
errmsg_variable
	:	scalar_default_char_variable
	;

// R627
source_expr
	:	expr
	;

// R628
allocation
    : allocate_object ( LPAREN allocate_shape_spec_list RPAREN )?
    ;

allocation_list
    :    allocation ( allocation )*
    ;

// R629
allocate_object
	:	variable_name
	|	structure_component
	;

allocate_object_list
    :    allocate_object ( allocate_object )*
    ;

// R630
allocate_shape_spec
    :    ( lower_bound_expr COLON )? upper_bound_expr
    ;

allocate_shape_spec_list
    :    allocate_shape_spec ( allocate_shape_spec )*
    ;

// R631
lower_bound_expr
	:	scalar_int_expr
	;

// R632
upper_bound_expr
	:	scalar_int_expr
	;

// R633
nullify_stmt
	:	NULLIFY
		LPAREN
		pointer_object_list
		RPAREN
	;

// R634
pointer_object
	:	variable_name
	|	structure_component
	|	proc_pointer_name
	;

pointer_object_list
    :    pointer_object ( pointer_object )*
    ;

// R635
deallocate_stmt
    :    DEALLOCATE LPAREN allocate_object_list ( COMMA dealloc_opt_list )? RPAREN
    ;

// R636
dealloc_opt
	:	STAT EQUAL stat_variable
	|	ERRMSG EQUAL errmsg_variable
	;

dealloc_opt_list
    :    dealloc_opt ( dealloc_opt )*
    ;

/*
Section 7:
 */

// R701
primary
	:	constant
	|	designator
	|	array_constructor
	|	structure_constructor
	|	function_reference
	|	type_param_inquiry
	|	type_param_name
	|	LPAREN expr RPAREN
	;

// R702
level_1_expr
    : ( defined_unary_op )? primary
    ;

// R703
defined_unary_op
	:	DOT LETTER ( LETTER )* DOT
	;

// R704
mult_operand
    : level_1_expr ( power_op mult_operand )?
    ;

// R705
add_operand
    : ( add_operand mult_op )? mult_operand
    ;

// R706
level_2_expr
    : ( ( level_2_expr )? add_op )? add_operand
    ;

// R707
power_op
	:	STARSTAR
	;

// R708
mult_op
	:	STAR
	|	SLASH
	;

// R709
add_op
	:	PLUS
	|	QUESTION
	;

// R710
level_3_expr
    : ( level_3_expr concat_op )? level_2_expr
    ;

// R711
concat_op
	:	SLASHSLASH
	;

// R712
level_4_expr
    : ( level_3_expr rel_op )? level_3_expr
    ;

// R713
rel_op
	:	DOT_EQ
	|	DOT_NE
	|	DOT_LT
	|	DOT_LE
	|	DOT_GT
	|	DOT_GE
	|	EQUAL_EQ
	|	SLASH_EQ
	|	LESSTHAN
	|	LESSTHAN_EQ
	|	GREATERTHAN
	|	GREATERTHAN_EQ
	;

// R714
and_operand
    :    ( not_op )? level_4_expr
    ;

// R715
or_operand
    : ( or_operand and_op )? and_operand
    ;

// R716
equiv_operand
    : ( equiv_operand or_op )? or_operand
    ;

// R717
level_5_expr
    : ( level_5_expr equiv_op )? equiv_operand
    ;

// R718
not_op
	:	DOT_NOT
	;

// R719
and_op
	:	DOT_AND
	;

// R720
or_op
	:	DOT_OR
	;

// R721
equiv_op
	:	DOT_EQV
	|	DOT_NEQV
	;

// R722
expr
    : ( expr defined_binary_op )? level_5_expr
    ;

// R723
defined_binary_op
	:	DOT LETTER ( LETTER )* DOT
	;

// R724
logical_expr
	:	expr
	;

// R725
char_expr
	:	expr
	;

// R726
default_char_expr
	:	expr
	;

// R727
int_expr
	:	expr
	;

// R728
numeric_expr
	:	expr
	;

scalar_numeric_expr
	:	expr
	;

// R729
specification_expr
	:	scalar_int_expr
	;

// R730
initialization_expr
	:	expr
	;

// R731
char_initialization_expr
	:	char_expr
	;

scalar_char_initialization_expr
    :   char_expr
    ;

// R732
int_initialization_expr
	:	int_expr
	;

scalar_int_initialization_expr
	:	int_initialization_expr
	;

// R733
logical_initialization_expr
	:	logical_expr
	;

scalar_logical_initialization_expr
	:	logical_expr
	;

// R734
assignment_stmt
	:	variable
		EQUAL
		expr
	;

// R735
pointer_assignment_stmt
    :    data_pointer_object ( LPAREN bounds_spec_list RPAREN )? EQUAL_GREATER data_target
    | data_pointer_object LPAREN bounds_remapping_list RPAREN EQUAL_GREATER data_target
    | proc_pointer_object EQUAL_GREATER proc_target
    ;

// R736
data_pointer_object
	:	variable_name
	|	variable PERCENT data_pointer_component_name
	;

data_pointer_component_name
    :    name
    ;

// R737
bounds_spec
	:	lower_bound_expr
		COLON
	;

bounds_spec_list
    :    bounds_spec ( bounds_spec )*
    ;

// R738
bounds_remapping
	:	lower_bound_expr
		COLON
		upper_bound_expr
	;

bounds_remapping_list
    :    bounds_remapping ( bounds_remapping )*
    ;

// R739
data_target
	:	variable
	|	expr
	;

// R740
proc_pointer_object
	:	proc_pointer_name
	|	proc_component_ref
	;

// R741
proc_component_ref
	:	variable PERCENT procedure_component_name
	;

procedure_component_name
    :    name
    ;

// R742
proc_target
	:	expr
	|	procedure_name
	|	proc_component_ref
	;

// R743
where_stmt
	:	WHERE
		LPAREN
		mask_expr
		RPAREN
		where_assignment_stmt
	;

// R744
where_construct
    :    where_construct_stmt ( where_body_construct )* ( masked_elsewhere_stmt
          ( where_body_construct )* )* ( elsewhere_stmt ( where_body_construct )* )? end_where_stmt
    ;

// R745
where_construct_stmt
    :    ( where_construct_name COLON )? WHERE LPAREN mask_expr RPAREN
    ;

// R746
where_body_construct
	:	where_assignment_stmt
	|	where_stmt
	|	where_construct
	;

// R747
where_assignment_stmt
	:	assignment_stmt
	;

// R748
mask_expr
	:	logical_expr
	;

scalar_mask_expr
    :    scalar_logical_expr
    ;

scalar_logical_expr
    :   logical_expr
    ;

// R749
masked_elsewhere_stmt
	:	ELSEWHERE
		LPAREN
		mask_expr
		RPAREN
		( where_construct_name )?
	;

// R750
elsewhere_stmt
	:	ELSEWHERE
		( where_construct_name )?
	;

// R751
end_where_stmt
	:	END WHERE ( where_construct_name )?
	;

where_construct_name
    :   name
    ;

// R752
forall_construct
	:	forall_construct_stmt
		( forall_body_construct )*
		end_forall_stmt
	;

// R753
forall_construct_stmt
    :    ( forall_construct_name COLON )? FORALL forall_header
    ;

// R754
forall_header
    : LPAREN forall_triplet_spec_list ( COMMA scalar_mask_expr )? RPAREN
    ;

// R755
forall_triplet_spec
    : index_name EQUAL subscript COLON subscript ( COLON stride )?
    ;

index_name
    :    name
    ;

forall_triplet_spec_list
    :    forall_triplet_spec ( forall_triplet_spec )*
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
	:	END
		FORALL
		( forall_construct_name )?
	;

forall_construct_name
    :   name
    ;

// R759
forall_stmt
	:	FORALL
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
if_then_stmt
    : ( if_construct_name COLON )? IF LPAREN scalar_logical_expr RPAREN THEN
    ;

// R804
else_if_stmt
	:	ELSE IF
		LPAREN scalar_logical_expr RPAREN
		THEN
		( if_construct_name )?
	;

// R805
else_stmt
	:	ELSE
		( if_construct_name )?
	;

// R806
end_if_stmt
	:	END
		IF
		( if_construct_name )?
	;

if_construct_name
    :    name
    ;

// R807
if_stmt
	:	IF
		LPAREN
		scalar_logical_expr
		RPAREN
		action_stmt
	;

// R808
case_construct
    :    select_case_stmt ( case_stmt block )* end_select_stmt
    ;

// R809
select_case_stmt
    :    ( case_construct_name COLON )? SELECT CASE LPAREN case_expr RPAREN
    ;

// R810
case_stmt
	:	CASE
		case_selector
		( case_construct_name )?
	;

// R811
end_select_stmt
	:	END
		SELECT
		( case_construct_name )?
	;

case_construct_name
    :   name
    ;

// R812
case_expr
	:	scalar_int_expr
	|	scalar_char_expr
	|	scalar_logical_expr
	;

scalar_char_expr
    :    char_expr
    ;

// R813
case_selector
	:	LPAREN
		case_value_range_list
		RPAREN
	|	DEFAULT
	;

// R814
case_value_range
	:	case_value
	|	case_value COLON
	|	COLON case_value
	|	case_value COLON case_value
	;

case_value_range_list
    :    case_value_range ( case_value_range )*
    ;

// R815
case_value
	:	scalar_int_initialization_expr
	|	scalar_char_initialization_expr
	|	scalar_logical_initialization_expr
	;

// R816
associate_construct
	:	associate_stmt
		block
		end_associate_stmt
	;

// R817
associate_stmt
    : ( associate_construct_name COLON )? ASSOCIATE LPAREN association_list RPAREN
    ;

associate_construct_name
    :    name
    ;

association_list
    :    association ( association )*
    ;

// R818
association
	:	associate_name EQUAL_GREATER selector
	;

associate_name
    :    name
    ;

// R819
selector
	:	expr
	|	variable
	;

// R820
end_associate_stmt
	:	END
		ASSOCIATE
		( associate_construct_name )?
	;

// R821
select_type_construct
    :    select_type_stmt ( type_guard_stmt block )* end_select_type_stmt
    ;

// R822
select_type_stmt
    : ( select_construct_name COLON )? SELECT TYPE
         LPAREN ( associate_name EQUAL_GREATER )? selector RPAREN
    ;

select_construct_name
    :    name
    ;

// R823
type_guard_stmt
	:	TYPE
		IS
		LPAREN
		type_spec
		RPAREN
		( select_construct_name )?
	|	CLASS
		IS
		LPAREN
		type_spec
		RPAREN
		( select_construct_name )?
	|	CLASS
		DEFAULT
		( select_construct_name )?
	;

// R824
end_select_type_stmt
	:	END
		SELECT
		( select_construct_name )?
	;

// R825
do_construct
	:	block_do_construct
	|	nonblock_do_construct
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
    :    ( do_construct_name COLON )? DO LABEL ( loop_control )?
    ;

do_construct_name
    :    name
    ;

// R829
nonlabel_do_stmt
    :    ( do_construct_name COLON )? DO ( loop_control )?
    ;

// R830
loop_control
    : ( COMMA )? do_variable EQUAL scalar_int_expr COMMA scalar_int_expr ( COMMA scalar_int_expr )?
    | ( COMMA )? WHILE LPAREN scalar_logical_expr RPAREN
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
	:	END DO ( do_construct_name )?
	;

// R835
nonblock_do_construct
	:	action_term_do_construct
	|	outer_shared_do_construct
	;

// R836
action_term_do_construct
	:	label_do_stmt
		do_body
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
outer_shared_do_construct
	:	label_do_stmt
		do_body
		shared_term_do_construct
	;

// R840
shared_term_do_construct
	:	outer_shared_do_construct
	|	inner_shared_do_construct
	;

// R841
inner_shared_do_construct
	:	label_do_stmt do_body do_term_shared_stmt
	;

// R842
do_term_shared_stmt
	:	action_stmt
	;

// R843
cycle_stmt
	:	CYCLE ( do_construct_name )?
	;

// R844
exit_stmt
	:	EXIT ( do_construct_name )?
	;

// R845
goto_stmt
	:	GO TO LABEL
	;

// R846
computed_goto_stmt
	:	GO TO LPAREN label_list RPAREN ( COMMA )? scalar_int_expr
	;

// R847
arithmetic_if_stmt
	:	IF
		LPAREN
		scalar_numeric_expr
		RPAREN
		LABEL
		COMMA
		LABEL
		COMMA
		LABEL
	;

// R848
continue_stmt
	:	CONTINUE
	;

// R849
stop_stmt
	:	STOP ( stop_code )?
	;

// R850
stop_code
    : scalar_char_constant
    | DIGIT ( DIGIT ( DIGIT ( DIGIT ( DIGIT )? )? )? )?
    ;

scalar_char_constant
    :    char_constant
    ;

/*
Section 9:
 */

// R901
io_unit
	:	file_unit_number
	|	STAR
	|	internal_file_variable
	;

// R902
file_unit_number
	:	scalar_int_expr
	;

// R903
internal_file_variable
	:	char_variable
	;

// R904
open_stmt
	:	OPEN LPAREN connect_spec_list RPAREN
	;

// R905
connect_spec
    : ( UNIT EQUAL )? file_unit_number
    | ACCESS EQUAL scalar_default_char_expr
    | ACTION EQUAL scalar_default_char_expr
    | ASYNCHRONOUS EQUAL scalar_default_char_expr
    | BLANK EQUAL scalar_default_char_expr
    | DECIMAL EQUAL scalar_default_char_expr
    | DELIM EQUAL scalar_default_char_expr
    | ENCODING EQUAL scalar_default_char_expr
    | ERR EQUAL LABEL
    | FILE EQUAL file_name_expr
    | FORM EQUAL scalar_default_char_expr
    | IOMSG EQUAL iomsg_variable
    | IOSTAT EQUAL scalar_int_variable
    | PAD EQUAL scalar_default_char_expr
    | POSITION EQUAL scalar_default_char_expr
    | RECL EQUAL scalar_int_expr
    | ROUND EQUAL scalar_default_char_expr
    | SIGN EQUAL scalar_default_char_expr
    | STATUS EQUAL scalar_default_char_expr
    ;

connect_spec_list
    :    connect_spec ( connect_spec )*
    ;

scalar_default_char_expr
    :    scalar_char_expr
    ;

// R906
file_name_expr
	:	scalar_default_char_expr
	;

// R907
iomsg_variable
	:	scalar_default_char_variable
	;

// R908
close_stmt
	:	CLOSE LPAREN close_spec_list RPAREN
	;

// R909
close_spec
    : ( UNIT EQUAL )? file_unit_number
    | IOSTAT EQUAL scalar_int_variable
    | IOMSG EQUAL iomsg_variable
    | ERR EQUAL LABEL
    | STATUS EQUAL scalar_default_char_expr
    ;

close_spec_list
    :    close_spec ( close_spec )*
    ;

// R910
read_stmt
    :    READ LPAREN io_control_spec_list RPAREN ( input_item_list )?
    |    READ format ( COMMA input_item_list )?
    ;

// R911
write_stmt
	:	WRITE LPAREN io_control_spec_list RPAREN ( output_item_list )?
	;

// R912
print_stmt
    :    PRINT format ( COMMA output_item_list )?
    ;

// R913
io_control_spec
    :    ( UNIT EQUAL )? io_unit
    | ( FMT EQUAL )? format
    | ( NML EQUAL )? namelist_group_name
    | ADVANCE EQUAL scalar_default_char_expr
    | ASYNCHRONOUS EQUAL scalar_char_initialization_expr
    | BLANK EQUAL scalar_default_char_expr
    | DECIMAL EQUAL scalar_default_char_expr
    | DELIM EQUAL scalar_default_char_expr
    | END EQUAL LABEL
    | EOR EQUAL LABEL
    | ERR EQUAL LABEL
    | ID EQUAL scalar_int_variable
    | IOMSG EQUAL iomsg_variable
    | IOSTAT EQUAL scalar_int_variable
    | PAD EQUAL scalar_default_char_expr
    | POS EQUAL scalar_int_expr
    | REC EQUAL scalar_int_expr
    | ROUND EQUAL scalar_default_char_expr
    | SIGN EQUAL scalar_default_char_expr
    | SIZE EQUAL scalar_int_variable
    ;

io_control_spec_list
    :    io_control_spec ( io_control_spec )*
    ;

// R914
format
	:	default_char_expr
	|	LABEL
	|	STAR
	;

// R915
input_item
	:	variable
	|	io_implied_do
	;

input_item_list
    :    input_item ( input_item )*
    ;

// R916
output_item
	:	expr
	|	io_implied_do
	;

output_item_list
    :    output_item ( output_item )*
    ;

// R917
io_implied_do
	:	LPAREN io_implied_do_object_list COMMA io_implied_do_control RPAREN
	;

// R918
io_implied_do_object
	:	input_item
	|	output_item
	;

io_implied_do_object_list
    :    io_implied_do_object ( io_implied_do_object )*
    ;

// R919
io_implied_do_control
    : do_variable EQUAL scalar_int_expr COMMA scalar_int_expr ( COMMA scalar_int_expr )?
    ;

// R920
dtv_type_spec
	:	TYPE
		LPAREN
		derived_type_spec
		RPAREN
	|	CLASS
		LPAREN
		derived_type_spec
		RPAREN
	;

// R921
wait_stmt
	:	WAIT LPAREN wait_spec_list RPAREN
	;

// R922
wait_spec
    : ( UNIT EQUAL )? file_unit_number
    | END EQUAL LABEL
    | EOR EQUAL LABEL
    | ERR EQUAL LABEL
    | ID EQUAL scalar_int_variable
    | IOMSG EQUAL iomsg_variable
    | IOSTAT EQUAL scalar_int_variable
    ;

wait_spec_list
    :    wait_spec ( wait_spec )*
    ;

// R923
backspace_stmt
	:	BACKSPACE file_unit_number
	|	BACKSPACE LPAREN position_spec_list RPAREN
	;

// R924
endfile_stmt
	:	ENDFILE file_unit_number
	|	ENDFILE LPAREN position_spec_list RPAREN
	;

// R925
rewind_stmt
	:	REWIND file_unit_number
	|	REWIND LPAREN position_spec_list RPAREN
	;

// R926
position_spec
    : ( UNIT EQUAL )? file_unit_number
    | IOMSG EQUAL iomsg_variable
    | IOSTAT EQUAL scalar_int_variable
    | ERR EQUAL LABEL
    ;

position_spec_list
    :    position_spec ( position_spec )*
    ;

// R927
flush_stmt
	:	FLUSH file_unit_number
	|	FLUSH LPAREN flush_spec_list RPAREN
	;

// R928
flush_spec
    : ( UNIT EQUAL )? file_unit_number
    | IOSTAT EQUAL scalar_int_variable
    | IOMSG EQUAL iomsg_variable
    | ERR EQUAL LABEL
    ;

flush_spec_list
    :    flush_spec ( flush_spec )*
    ;

// R929
inquire_stmt
	:	INQUIRE LPAREN inquire_spec_list RPAREN
	|	INQUIRE LPAREN IOLENGTH EQUAL scalar_int_variable RPAREN output_item_list
	;

// R930
inquire_spec
    : ( UNIT EQUAL )? file_unit_number
    | FILE EQUAL file_name_expr
    | ACCESS EQUAL scalar_default_char_variable
    | ACTION EQUAL scalar_default_char_variable
    | ASYNCHRONOUS EQUAL scalar_default_char_variable
    | BLANK EQUAL scalar_default_char_variable
    | DECIMAL EQUAL scalar_default_char_variable
    | DELIM EQUAL scalar_default_char_variable
    | DIRECT EQUAL scalar_default_char_variable
    | ENCODING EQUAL scalar_default_char_variable
    | ERR EQUAL LABEL
    | EXIST EQUAL scalar_default_logical_variable
    | FORM EQUAL scalar_default_char_variable
    | FORMATTED EQUAL scalar_default_char_variable
    | ID EQUAL scalar_int_variable
    | IOMSG EQUAL iomsg_variable
    | IOSTAT EQUAL scalar_int_variable
    | NAME EQUAL scalar_default_char_variable
    | NAMED EQUAL scalar_default_logical_variable
    | NEXTREC EQUAL scalar_int_variable
    | NUMBER EQUAL scalar_int_variable
    | OPENED EQUAL scalar_default_logical_variable
    | PAD EQUAL scalar_default_char_variable
    | PENDING EQUAL scalar_default_logical_variable
    | POS EQUAL scalar_int_variable
    | POSITION EQUAL scalar_default_char_variable
    | READ EQUAL scalar_default_char_variable
    | READWRITE EQUAL scalar_default_char_variable
    | RECL EQUAL scalar_int_variable
    | ROUND EQUAL scalar_default_char_variable
    | SEQUENTIAL EQUAL scalar_default_char_variable
    | SIGN EQUAL scalar_default_char_variable
    | SIZE EQUAL scalar_int_variable
    | STREAM EQUAL scalar_default_char_variable
    | UNFORMATTED EQUAL scalar_default_char_variable
    | WRITE EQUAL scalar_default_char_variable
    ;

inquire_spec_list
    :    inquire_spec ( inquire_spec )*
    ;

/*
Section 10:
 */

// R1001
format_stmt
	:	FORMAT format_specification
	;

// R1002
format_specification
	:	LPAREN ( format_item_list )? RPAREN
	;

// R1003
format_item
	:	( int_literal_constant )? data_edit_desc
	|	control_edit_desc
	|	char_string_edit_desc
	|	( int_literal_constant )? LPAREN format_item_list RPAREN
	;

format_item_list
    :    format_item ( format_item )*
    ;

/* TODO - inline it (in R1003)
// R1004
r
	:	int_literal_constant
	;
 */

// R1005
data_edit_desc
    : 'I' int_literal_constant ( DOT int_literal_constant )?
    | 'B' int_literal_constant ( DOT int_literal_constant )?
    | 'O' int_literal_constant ( DOT int_literal_constant )?
    | 'Z' int_literal_constant ( DOT int_literal_constant )?
    | 'F' int_literal_constant DOT int_literal_constant
    | 'E' int_literal_constant DOT int_literal_constant ( 'E' int_literal_constant )?
    | 'EN' int_literal_constant DOT int_literal_constant ( 'E' int_literal_constant )?
    | 'ES' int_literal_constant DOT int_literal_constant ( 'E' int_literal_constant )?
    | 'G' int_literal_constant DOT int_literal_constant ( 'E' int_literal_constant )?
    | 'L' int_literal_constant
    | 'A' ( int_literal_constant )?
    | 'D' int_literal_constant DOT int_literal_constant
    | 'DT' ( char_literal_constant )? ( LPAREN v_list RPAREN )?
    ;

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
    :   signed_int_literal_constant ( signed_int_literal_constant )*
    ;

// R1011
control_edit_desc
	:	position_edit_desc
	|	( int_literal_constant )? SLASH
	|	COLON
	|	sign_edit_desc
	|	signed_int_literal_constant 'P'
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
position_edit_desc
	:	'T' int_literal_constant
	|	'TL' int_literal_constant
	|	'TR' int_literal_constant
	|	int_literal_constant 'X'
	;

/* TODO: inlined in R1013
// R1014
n
	:	int_literal_constant
	;
*/

// R1015
sign_edit_desc
	:	'SS'
	|	'SP'
	|	'S'
	;

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
main_program
	:	( program_stmt )?
		( specification_part )?
		( execution_part )?
		( internal_subprogram_part )?
		end_program_stmt
	;

// R1102
program_stmt
	:	PROGRAM
		program_name
	;

// R1103
end_program_stmt
    :    END ( PROGRAM ( program_name )? )?
    ;

program_name
    :    name
    ;

// R1104
module
	:	module_stmt
		( specification_part )?
		( module_subprogram_part )?
		end_module_stmt
	;

// R1105
module_stmt
	:	MODULE
		module_name
	;

// R1106
end_module_stmt
    :   END ( MODULE ( module_name )? )?
    ;

module_name
    :    name
    ;

// R1107
module_subprogram_part
	:	contains_stmt
		module_subprogram
		( module_subprogram )*
	;

// R1108
module_subprogram
	:	function_subprogram
	|	subroutine_subprogram
	;

// R1109
use_stmt
    :    USE ( ( COMMA module_nature )? COLONCOLON )? module_name ( COMMA rename_list )?
    |    USE ( ( COMMA module_nature )? COLONCOLON )? module_name COMMA ONLY COLON ( only_list )?
    ;

// R1110
module_nature
	:	INTRINSIC
	|	NON	INTRINSIC
	;

// R1111
rename
	:	local_name EQUAL_GREATER use_name
	|	OPERATOR LPAREN local_defined_operator LPAREN EQUAL_GREATER
		OPERATOR LPAREN use_defined_operator LPAREN
	;

rename_list
    :    rename ( rename )*
    ;

local_name
    :    name
    ;

use_name
    :    name
    ;

// R1112
only
	:	generic_spec
	|	only_use_name
	|	rename
	;

only_list
    :    only ( only )*
    ;

// R1113
only_use_name
	:	use_name
	;

// R1114
local_defined_operator
	:	defined_unary_op
	|	defined_binary_op
	;

// R1115
use_defined_operator
	:	defined_unary_op
	|	defined_binary_op
	;

// R1116
block_data
	:	block_data_stmt
		( specification_part )?
		end_block_data_stmt
	;

// R1117
block_data_stmt
	:	BLOCK
		DATA
		( block_data_name )?
	;

// R1118
end_block_data_stmt
    : END ( BLOCK DATA ( block_data_name )? )?
    ;

block_data_name
    :    name
    ;

/*
Section 12:
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
	:	INTERFACE ( generic_spec )?
	|	ABSTRACT INTERFACE
	;

// R1204
end_interface_stmt
	:	END INTERFACE ( generic_spec )?
	;

// R1205
interface_body
	:	function_stmt ( specification_part )? end_function_stmt
	|	subroutine_stmt ( specification_part )? end_subroutine_stmt
	;

// R1206
procedure_stmt
	:	( MODULE )? PROCEDURE procedure_name_list
	;

procedure_name
    :    name
    ;

procedure_name_list
    :    procedure_name ( procedure_name )*
    ;

// R1207
generic_spec
	:	generic_name
	|	OPERATOR LPAREN defined_operator RPAREN
	|	ASSIGNMENT LPAREN EQUAL RPAREN
	|	dtio_generic_spec
	;

generic_name
    :   name
    ;

// R1208
dtio_generic_spec
	:	READ LPAREN FORMATTED RPAREN
	|	READ LPAREN UNFORMATTED RPAREN
	|	WRITE LPAREN FORMATTED RPAREN
	|	WRITE LPAREN UNFORMATTED RPAREN
	;

// R1209
import_stmt
    :    IMPORT ( ( COLONCOLON )? import_name_list )?
    ;

import_name
    :    name
    ;

import_name_list
    :    import_name ( import_name )*
    ;

// R1210
external_stmt
	:	EXTERNAL ( COLONCOLON )? external_name_list
	;

external_name
    :    name
    ;

external_name_list
    :    external_name ( external_name )*
    ;

// R1211
procedure_declaration_stmt
    :    PROCEDURE LPAREN ( proc_interface )? RPAREN
            ( ( COMMA proc_attr_spec )* COLONCOLON )? proc_decl_list
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
	|	INTENT LPAREN intent_spec RPAREN
	|	OPTIONAL
	|	POINTER
	|	SAVE
	;

// R1214
proc_decl
    :    procedure_entity_name ( EQUAL_GREATER null_init )?
    ;

procedure_entity_name
    :    name
    ;

proc_decl_list
    :    proc_decl ( proc_decl )*
    ;

// R1215
interface_name
	:	name
	;

// R1216
intrinsic_stmt
	:	INTRINSIC
		( COLONCOLON )?
		intrinsic_procedure_name_list
	;

intrinsic_procedure_name
    : name
    ;

intrinsic_procedure_name_list
    :    intrinsic_procedure_name ( intrinsic_procedure_name )*
    ;

// R1217
function_reference
	:	procedure_designator
		LPAREN
		( actual_arg_spec_list )?
		RPAREN
	;

// R1218
call_stmt
    :    CALL procedure_designator ( LPAREN ( actual_arg_spec_list )? RPAREN )?
    ;

// R1219
procedure_designator
	:	procedure_name
	|	proc_component_ref
	|	data_ref PERCENT binding_name
	;

binding_name
    :    name
    ;

binding_name_list
    :    binding_name ( binding_name )*
    ;

// R1220
actual_arg_spec
    : ( keyword EQUAL )? actual_arg
    ;

actual_arg_spec_list
    :    actual_arg_spec ( actual_arg_spec )*
    ;

// R1221
actual_arg
	:	expr
	|	variable
	|	procedure_name
	|	proc_component_ref
	|	alt_return_spec
	;

// R1222
alt_return_spec
	:	STAR LABEL
	;

// R1223
function_subprogram
	:	function_stmt
		( specification_part )?
		( execution_part )?
		( internal_subprogram_part )?
		end_function_stmt
	;

// R1224
function_stmt
	:	( prefix )? FUNCTION function_name
		LPAREN ( dummy_arg_name_list )? RPAREN ( suffix )?
	;

function_name
    :    name
    ;

// R1225
proc_language_binding_spec
	:	language_binding_spec
	;

// R1226
dummy_arg_name
	:	name
	;

// R1227
prefix
	:	prefix_spec ( prefix_spec )*
	;

// R1228
prefix_spec
	:	declaration_type_spec
	|	RECURSIVE
	|	PURE
	|	ELEMENTAL
	;

// R1229
suffix
    :    proc_language_binding_spec ( RESULT LPAREN result_name RPAREN )?
    | RESULT LPAREN result_name RPAREN ( proc_language_binding_spec )?
    ;

result_name
    :    name
    ;

// R1230
end_function_stmt
    : END ( FUNCTION ( function_name )? )?
    ;

// R1231
subroutine_subprogram
	:	subroutine_stmt
		( specification_part )?
		( execution_part )?
		( internal_subprogram_part )?
		end_subroutine_stmt
	;

// R1232
subroutine_stmt
    :     ( prefix )? SUBROUTINE subroutine_name
          ( LPAREN ( dummy_arg_list )? RPAREN ( proc_language_binding_spec )? )?
    ;

// R1233
dummy_arg
	:	dummy_arg_name
	|	STAR
	;

dummy_arg_list
    :    dummy_arg ( dummy_arg )*
    ;

// R1234
end_subroutine_stmt
    :    END ( SUBROUTINE ( subroutine_name )? )?
    ;

subroutine_name
    :    name
    ;

// R1235
entry_stmt
    :    ENTRY entry_name ( LPAREN ( dummy_arg_list )? RPAREN ( suffix )? )?
    ;

entry_name
    :    name
    ;

// R1236
return_stmt
	:	RETURN ( scalar_int_expr )?
	;

// R1237
contains_stmt
	:	CONTAINS
	;

// R1238
stmt_function_stmt
	:	function_name
		LPAREN
		( dummy_arg_name_list )?
		RPAREN
		EQUAL
		scalar_expr
	;

/*
Lexer rules
 */

// R313
LABEL
    :   DIGIT ( DIGIT ( DIGIT ( DIGIT ( DIGIT )? )? )? )?
    ;

// R412
BINARY_CONSTANT
    : 'B' SINGLE_QUOTE DIGIT_2 ( DIGIT_2 )* SINGLE_QUOTE
    | 'B' DOUBLE_QUOTE DIGIT_2 ( DIGIT_2 )* DOUBLE_QUOTE
    ;

// R413
OCTAL_CONSTANT
    : 'O' SINGLE_QUOTE DIGIT_8 ( DIGIT_8 )* SINGLE_QUOTE
    | 'O' DOUBLE_QUOTE DIGIT_8 ( DIGIT_8 )* DOUBLE_QUOTE
    ;

// R414
HEX_CONSTANT
    : 'Z' SINGLE_QUOTE DIGIT_16 ( DIGIT_16 )* SINGLE_QUOTE
    | 'Z' DOUBLE_QUOTE DIGIT_16 ( DIGIT_16 )* DOUBLE_QUOTE
    ;

// R427
char_literal_constant
    :    ( kind_param ) SINGLE_QUOTE ( REP_CHAR )* SINGLE_QUOTE
    |    ( kind_param ) DOUBLE_QUOTE ( REP_CHAR )* DOUBLE_QUOTE
    ;

// R304
T_IDENT
	:	LETTER ( ALPHANUMERIC_CHARACTER )*
	;




 PLUS : '+';
 QUESTION : '?';
 DOT : '.';
 STAR : '*';
 STARSTAR : '**';
 COLON : ':';
 COLONCOLON : '::';
 RPAREN : ')';
 LPAREN : '(';
 RBRACKET : ']';
 LBRACKET : '[';
 PERCENT : '%';
 SLASH : '/';
 SLASHSLASH : '//';
 COMMA : ',';
 EQUAL : ':';
 EQUAL_GREATER : ':>';
 DOT_TRUE : '.TRUE.';
 DOT_FALSE : '.FALSE.';
 DOT_EQ : '.EQ.';
 DOT_NE : '.NE.';
 DOT_LT : '.LT.';
 DOT_LE : '.LE.';
 DOT_GT : '.GT.';
 DOT_GE : '.GE.';
 EQUAL_EQ : '==';
 SLASH_EQ : '/=';
 LESSTHAN : '<';
 LESSTHAN_EQ : '<=';
 GREATERTHAN : '>';
 GREATERTHAN_EQ : '>=';
 DOT_NOT : '.NOT.';
 DOT_AND : '.AND.';
 DOT_OR : '.OR.';
 DOT_EQV : '.EQV.';
 DOT_NEQV : '.NEQV.';
 PROGRAM : 'PROGRAM';
 INTERFACE : 'INTERFACE';
 MODULE : 'MODULE';
 CONTAINS : 'CONTAINS';
 INTENT : 'INTENT';
 IN : 'IN';
 OUT : 'OUT';
 INOUT : 'INOUT';
 VALUE : 'VALUE';
 TYPE : 'TYPE';
 CLASS : 'CLASS';
 EXTENDS : 'EXTENDS';
 ABSTRACT : 'ABSTRACT';
 PUBLIC : 'PUBLIC';
 PRIVATE : 'PRIVATE';
 PROTECTED : 'PROTECTED';
 SEQUENCE : 'SEQUENCE';
 ENUM : 'ENUM';
 ENUMCOMMA : 'ENUMCOMMA';
 ENUMERATOR : 'ENUMERATOR';
 FINAL : 'FINAL';
 INTEGER : 'INTEGER';
 REAL : 'REAL';
 DOUBLE : 'DOUBLE';
 PRECISION : 'PRECISION';
 COMPLEX : 'COMPLEX';
 CHARACTER : 'CHARACTER';
 LOGICAL : 'LOGICAL';
 KIND : 'KIND';
 LEN : 'LEN';
 ASYNCHRONOUS : 'ASYNCHRONOUS';
 BIND : 'BIND';
 C : 'C';
 COMMON : 'COMMON';
 DATA : 'DATA';
 DIMENSION : 'DIMENSION';
 EQUIVALENCE : 'EQUIVALENCE';
 IMPLICIT : 'IMPLICIT';
 NONE : 'NONE';
 INTRINSIC : 'INTRINSIC';
 PARAMETER : 'PARAMETER';
 POINTER : 'POINTER';
 TARGET : 'TARGET';
 NULLIFY : 'NULLIFY';
 USE : 'USE';
 ONLY : 'ONLY';
 VOLATILE : 'VOLATILE';
 ASSOCIATE : 'ASSOCIATE';
 ALLOCATE : 'ALLOCATE';
 ALLOCATABLE : 'ALLOCATABLE';
 DEALLOCATE : 'DEALLOCATE';
 NAMELIST : 'NAMELIST';
 SAVE : 'SAVE';
 SOURCE : 'SOURCE';
 CASE : 'CASE';
 END : 'END';
 IMPORT : 'IMPORT';
 FUNCTION : 'FUNCTION';
 PROCEDURE : 'PROCEDURE';
 SUBROUTINE : 'SUBROUTINE';
 EXTERNAL : 'EXTERNAL';
 OPTIONAL : 'OPTIONAL';
 ELEMENTAL : 'ELEMENTAL';
 PURE : 'PURE';
 RECURSIVE : 'RECURSIVE';
 RESULT : 'RESULT';
 CALL : 'CALL';
 ENTRY : 'ENTRY';
 PASS : 'PASS';
 NOPASS : 'NOPASS';
 NON : 'NON';
 OPERATOR : 'OPERATOR';
 OVERRIDABLE : 'OVERRIDABLE';
 DEFERRED : 'DEFERRED';
 CYCLE : 'CYCLE';
 CONTINUE : 'CONTINUE';
 WAIT : 'WAIT';
 STOP : 'STOP';
 EXIT : 'EXIT';
 RETURN : 'RETURN';
 ASSIGNMENT : 'ASSIGNMENT';
 BLOCK : 'BLOCK';
 IS : 'IS';
 IF : 'IF';
 THEN : 'THEN';
 ELSE : 'ELSE';
 DO : 'DO';
 GO : 'GO';
 TO : 'TO';
 WHERE : 'WHERE';
 ELSEWHERE : 'ELSEWHERE';
 WHILE : 'WHILE';
 FORALL : 'FORALL';
 FORMAT : 'FORMAT';
 FMT : 'FMT';
 NML : 'NML';
 READ : 'READ';
 READWRITE : 'READWRITE';
 WRITE : 'WRITE';
 PRINT : 'PRINT';
 UNIT : 'UNIT';
 OPEN : 'OPEN';
 OPENED : 'OPENED';
 CLOSE : 'CLOSE';
 ADVANCE : 'ADVANCE';
 ACCESS : 'ACCESS';
 ACTION : 'ACTION';
 BACKSPACE : 'BACKSPACE';
 BLANK : 'BLANK';
 DECIMAL : 'DECIMAL';
 DEFAULT : 'DEFAULT';
 DELIM : 'DELIM';
 DIRECT : 'DIRECT';
 ENCODING : 'ENCODING';
 EOR : 'EOR';
 ERR : 'ERR';
 ERRMSG : 'ERRMSG';
 EXIST : 'EXIST';
 ENDFILE : 'ENDFILE';
 FILE : 'FILE';
 FLUSH : 'FLUSH';
 FORM : 'FORM';
 FORMATTED : 'FORMATTED';
 UNFORMATTED : 'UNFORMATTED';
 ID : 'ID';
 INQUIRE : 'INQUIRE';
 IOLENGTH : 'IOLENGTH';
 IOMSG : 'IOMSG';
 IOSTAT : 'IOSTAT';
 NAME : 'NAME';
 NAMED : 'NAMED';
 NEXTREC : 'NEXTREC';
 NUMBER : 'NUMBER';
 PAD : 'PAD';
 PENDING : 'PENDING'; 
 POS : 'POS';
 POSITION : 'POSITION';
 REC : 'REC';
 RECL : 'RECL';
 REWIND : 'REWIND';
 ROUND : 'ROUND';
 SELECT : 'SELECT';
 SEQUENTIAL : 'SEQUENTIAL';
 SIGN : 'SIGN';
 SIZE : 'SIZE';
 STAT : 'STAT';
 STATUS : 'STATUS';
 STREAM : 'STREAM';
 GENERIC : 'GENERIC';


// R303  underscore
fragment
UNDERSCORE : '_' ;

// R409 digit_string
fragment
DIGIT_STRING : DIGIT+ ;

// R302 alphanumeric_character
fragment
ALPHANUMERIC_CHARACTER : LETTER | DIGIT | UNDERSCORE ;

fragment
SPECIAL_CHARACTER
    :    ' ' .. '/'
    |    ':' .. '@'
    |    '[' .. '^'
    |    '`'
    |    '{' .. '~'
    ;

fragment
REP_CHAR : ' '..'~' ;

fragment
LETTER : ('a'..'z' | 'A'..'Z') ;

fragment
DIGIT : '0'..'9' ;

fragment
DIGIT_2 : '0'..'1' ;

fragment
DIGIT_8 : '0'..'7' ;

fragment
DIGIT_16 : (DIGIT | 'A'..'F') ;

fragment
SINGLE_QUOTE : '\'' ;

fragment
DOUBLE_QUOTE : '\"' ;

fragment
LEN_EQ : 'LEN' EQUAL ;

fragment
KIND_EQ : ('KIND' '=') ;
