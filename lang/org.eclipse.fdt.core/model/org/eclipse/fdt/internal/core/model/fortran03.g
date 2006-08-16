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
options {k=1;}
	:	(( prefix )? 'FUNCTION') => function_subprogram
	|	subroutine_subprogram
	;

// R204
// TODO putback
specification_part
options {k=1;}
	:	('USE') => ( use_stmt )*
		('IMPORT') => ( import_stmt )*
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
// TODO putback
internal_subprogram
	:	function_subprogram
//	|	subroutine_subprogram
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
//	|	associate_construct
//	|	case_construct
//	|	do_construct
//	|	forall_construct
//	|	if_construct
//	|	select_type_construct
//	|	where_construct
	;

// R214
// TODO putback
action_stmt
options {k=1;}
	:	('ALLOCATE') => allocate_stmt
//	|	assignment_stmt
//	|	('BACKSPACE') => backspace_stmt
//	|	('CALL') => call_stmt
//	|	('CLOSE') => close_stmt
//	|	('CONTINUE') => continue_stmt
//	|	('CYCLE') => cycle_stmt
//	|	('DEALLOCATE') => deallocate_stmt
//	|	('ENDFILE') => endfile_stmt
//	|	end_function_stmt
//	|	end_program_stmt
//	|	end_subroutine_stmt
//	|	('EXIT') => exit_stmt
//	|	('FLUSH') => flush_stmt
//	|	('FORALL') => forall_stmt
//	|	('GO' 'TO' Digit) => goto_stmt
//	|	('IF' '(' expr ')' action_stmt) => if_stmt
//	|	('INQUIRE') => inquire_stmt	|	('NULLIFY') => nullify_stmt
//	|	('OPEN') => open_stmt
//	|	pointer_assignment_stmt
//	|	('PRINT') => print_stmt
//	|	('READ') => read_stmt
//	|	('RETURN') => return_stmt
//	|	('REWIND') => rewind_stmt
//	|	('STOP') => stop_stmt
//	|	('WAIT') => wait_stmt
//	|	('WHERE') => where_stmt
//	|	('WRITE') => write_stmt
//	|	('IF' '(' expr LABEL) => arithmetic_if_stmt
//	|	('GO' 'TO' '(') => computed_goto_stmt
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
constant
	:	literal_constant
	|	named_constant
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
named_constant
	:	name
	;

// R308 int_constant converted to terminal

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
// removed defined_unary_op or defined_binary_op ambiguity with defined_unary_or_binary_op
defined_operator
	:	defined_unary_or_binary_op
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
// ERR_CHK 402 scalar_int_expr replaced by expr
type_param_value
	:	expr
	|	'*'
	|	':'
	;

// inlined scalar_int_expr C101 shall be a scalar

// inlined scalar_expr

// R403
intrinsic_type_spec
	:	'INTEGER' ( kind_selector )?
	|	'REAL' ( kind_selector )?
	|	'DOUBLE' 'PRECISION'
	|	'COMPLEX' ( kind_selector )?
	|	'CHARACTER' ( char_selector )?
	|	'LOGICAL' ( kind_selector )?
	;

// R404
// ERR_CHK 404 scalar_int_initialization_expr replaced by expr
kind_selector
    : '(' ('KIND' '=')? expr ')'
    ;

// TODO: turn into terminal (what about kind parameter)
// R405
signed_int_literal_constant
	:	('+'|'-')? int_literal_constant
	;

// R406
int_literal_constant
	:	INT_CONSTANT ('_' kind_param)?
	;

// R407
kind_param
	:	Digit_String
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
	:	('+'|'-')? real_literal_constant
	;

// R417 modified to use terminal
real_literal_constant
    :   REAL_CONSTANT ( '_' kind_param )?
    |   DOUBLE_CONSTANT ( '_' kind_param )?
    ;

// R418 significand converted to fragment

// R419 exponent_letter inlined in new Exponent

// R420 exponent inlined in new Exponent

// R421
complex_literal_constant
	:	'(' real_part ',' imag_part ')'
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
// ERR_CHK 424 scalar_int_initialization_expr replaced by expr
// TODO putback
char_selector
    :    length_selector
//    |    '(' 'LEN' '=' type_param_value ',' 'KIND' '=' expr ')'
//    |    '(' type_param_value ',' ('KIND' '=')? expr ')'
//    |    '(' 'KIND' '=' expr ( ',' 'LEN' '=' type_param_value )? ')'
    ;

// R425
length_selector
	:	'('
		('LEN' '=')?
		type_param_value
		')'
	|	'*'
		char_length
		(',')?
	;

// R426
char_length
	:	'('
		type_param_value
		')'
	|	scalar_int_literal_constant
	;

scalar_int_literal_constant
    :    int_literal_constant
    ;

// R427
char_literal_constant
    :    ( kind_param '_' ) '\'' ( Rep_Char )* '\''
    |    ( kind_param '_' ) '\"' ( Rep_Char )* '\"'
    ;

// R428
logical_literal_constant
    :    '.TRUE.' ( '_' kind_param )?
    |    '.FALSE.' ( '_' kind_param )?
    ;

// R429
// TODO putback
derived_type_def
	:	derived_type_stmt
//		( type_param_def_stmt )*
//		( private_or_sequence )*
//		( component_part )?
//		( type_bound_procedure_part )?
		end_type_stmt
	;

// R430
derived_type_stmt
    :    'TYPE' ( ( ',' type_attr_spec_list )? '::' )? type_name
         ( '(' type_param_name_list ')' )?
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
	|	'EXTENDS' '(' parent_type_name ')'
	|	'ABSTRACT'
	|	'BIND' '(' 'C' ')'
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
	:	'END' 'TYPE' ( type_name )?
	;

type_name
    :    name
    ;

// R434
sequence_stmt
	:	'SEQUENCE'
	;

// R435
type_param_def_stmt
	:	'INTEGER' ( kind_selector )? ',' type_param_attr_spec '::' type_param_decl_list
	;

// R436
// ERR_CHK 436 scalar_int_initialization_expr replaced by expr
type_param_decl
    :    type_param_name ( '=' expr )?
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
	:	'KIND'
	|	'LEN'
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
    :    declaration_type_spec ( ( ',' component_attr_spec_list )? '::' )? component_decl_list
    ;

// R441
component_attr_spec
	:	'POINTER'
	|	'DIMENSION' '(' component_array_spec ')'
	|	'ALLOCATABLE'
	|	access_spec
	;

component_attr_spec_list
    :    component_attr_spec ( component_attr_spec )*
    ;

// R442
component_decl
    :    component_name ( '(' component_array_spec ')' )?
                        ( '*' char_length )? ( component_initialization )?
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
// ERR_CHK 444 initialization_expr replaced by expr
component_initialization
	:	'='
		expr
	|	'=>'
		null_init
	;

// R445
proc_component_def_stmt
	:	'PROCEDURE' '(' ( proc_interface )? ')' ','
		    proc_component_attr_spec_list '::' proc_decl_list
	;

// R446
proc_component_attr_spec
    :    'POINTER'
    |    'PASS' ( '(' arg_name ')' )?
    |    'NOPASS'
    |    access_spec
    ;

proc_component_attr_spec_list
    :    proc_component_attr_spec ( proc_component_attr_spec )*
    ;

// R447
private_components_stmt
	:	'PRIVATE'
	;

// R448
type_bound_procedure_part
	:	contains_stmt ( binding_private_stmt )? proc_binding_stmt ( proc_binding_stmt )*
	;

// R449
binding_private_stmt
	:	'PRIVATE'
	;

// R450
proc_binding_stmt
	:	specific_binding
	|	generic_binding
	|	final_binding
	;

// R451
specific_binding
    : 'PROCEDURE' ( '(' interface_name ')' )?
      ( ( ',' binding_attr_list )? '::' )?
      binding_name ( '=>' procedure_name )?
    ;

// R452
generic_binding
    :    'GENERIC' ( ',' access_spec )? '::' generic_spec '=>' binding_name_list
    ;

// R453
binding_attr
    : 'PASS' ( '(' arg_name ')' )?
    | 'NOPASS'
    | 'NON' 'OVERRIDABLE'
    | 'DEFERRED'
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
	:	'FINAL'
		( '::' )?
		final_subroutine_name_list
	;

final_subroutine_name_list
    :    name ( name )*
    ;

// R455
// TODO putback
derived_type_spec
    : type_name
//      ( '(' type_param_spec_list ')' )?
    ;

// R456
type_param_spec
    : ( keyword '=' )? type_param_value
    ;

type_param_spec_list
    :    type_param_spec ( type_param_spec )*
    ;

// R457
structure_constructor
	:	derived_type_spec
		'('
		( component_spec_list )?
		')'
	;

// R458
component_spec
    :    ( keyword '=' )? component_data_source
    ;

component_spec_list
    :    component_spec ( component_spec )*
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
	:	'ENUMCOMMA'
		'BIND'
		'('
		'C'
		')'
	;

// R462
enumerator_def_stmt
	:	'ENUMERATOR'
		( '::' )?
		enumerator_list
	;

// R463
// ERR_CHK 463 scalar_int_initialization_expr replaced by expr
enumerator
    :    named_constant ( '=' expr )?
    ;

enumerator_list
    :   enumerator ( enumerator )*
    ;

// R464
end_enum_stmt
	:	'END'
		'ENUM'
	;

// R465
array_constructor
	:	'(' '/' ac_spec '/' ')'
	|	left_square_bracket ac_spec right_square_bracket
	;


// R466
// TODO putback
ac_spec
    : type_spec '::'
//    | (type_spec '::')? ac_value_list
    ;

// R467
left_square_bracket
	:	'['
	;

// R468
right_square_bracket
	:	']'
	;

// R469
// TODO putback
ac_value
	:	expr
//	|	ac_implied_do
	;

ac_value_list
    :   ac_value ( ac_value )*
    ;

// R470
ac_implied_do
	:	'(' ac_value_list ',' ac_implied_do_control ')'
	;

// R471
// ERR_CHK 471 scalar_int_expr replaced by expr
ac_implied_do_control
    :    ac_do_variable '=' expr ',' expr ( ',' expr )?
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
    :    declaration_type_spec ( ( ',' attr_spec )* '::' )? entity_decl_list
    ;

// R502
declaration_type_spec
	:	intrinsic_type_spec
	|	'TYPE'
		'('
		derived_type_spec
		')'
	|	'CLASS'
		'('
		derived_type_spec
		')'
	|	'CLASS'
		'('
		'*'
		')'
	;

// R503
attr_spec
	:	access_spec
	|	'ALLOCATABLE'
	|	'ASYNCHRONOUS'
	|	'DIMENSION' '(' array_spec ')'
	|	'EXTERNAL'
	|	'INTENT' '(' intent_spec ')'
	|	'INTRINSIC'
	|	language_binding_spec
	|	'OPTIONAL'
	|	'PARAMETER'
	|	'POINTER'
	|	'PROTECTED'
	|	'SAVE'
	|	'TARGET'
	|	'VALUE'
	|	'VOLATILE'
	;


// R504
// TODO putback
entity_decl
    : object_name ( '(' array_spec ')' )? ( '*' char_length )? ( initialization )?
//    | function_name ( '*' char_length )?
    ;

// TODO putback
entity_decl_list
    :    entity_decl
//         ( entity_decl )*
    ;

// R505
object_name
	:	name
	;

object_name_list
    :    object_name ( object_name )*
    ;

// R506
// ERR_CHK 506 initialization_expr replaced by expr
initialization
	:	'='
		expr
	|	'=>'
		null_init
	;

// R507
null_init
	:	function_reference
	;

// R508
access_spec
	:	'PUBLIC'
	|	'PRIVATE'
	;

// R509
// ERR_CHK 509 scalar_char_initialization_expr replaced by expr
language_binding_spec
    : 'BIND' '(' 'C' ( ',' name '=' expr )? ')'
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
    : // ( lower_bound ':' )?
      upper_bound
    ;

explicit_shape_spec_list
    : explicit_shape_spec ( explicit_shape_spec )*
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
	:	( lower_bound )? ':'
	;

assumed_shape_spec_list
    :    assumed_shape_spec ( assumed_shape_spec )*
    ;

// R515
deferred_shape_spec
	:	':'
	;

// R516
assumed_size_spec
    : /*( explicit_shape_spec_list ',' )? ( lower_bound ':' )?*/
       '*'
    ;

// R517
intent_spec
	:	'IN'
	|	'OUT'
	|	'INOUT'
	;

// R518
access_stmt
    :    access_spec ( ( '::' )? access_id_list )?
    ;

// R519
access_id
	:	use_name
//	|	generic_spec
	;

access_id_list
    :    access_id ( access_id )*
    ;

// R520
allocatable_stmt
    : 'ALLOCATABLE' ( '::' )? object_name ( '(' deferred_shape_spec_list ')' )?
         ( ',' object_name ( '(' deferred_shape_spec_list ')' )? )*
    ;

// R521
asynchronous_stmt
	:	'ASYNCHRONOUS'
		( '::' )?
		object_name_list
	;

// R522
bind_stmt
	:	language_binding_spec
		( '::' )?
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
	|	'/' common_block_name '/'
	;

common_block_name
    :    name
    ;

bind_entity_list
    :    bind_entity ( bind_entity )*
    ;

// R524
data_stmt
    :    'DATA' data_stmt_set ( ( ',' )? data_stmt_set )*
    ;

// R525
data_stmt_set
	:	data_stmt_object_list
		'/'
		data_stmt_value_list
		'/'
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
// ERR_CHK 527 scalar_int_expr replaced by expr
data_implied_do
    : '(' data_i_do_object_list ',' data_i_do_variable '='
         expr ',' expr ( ',' expr )? ')'
    ;

// R528
// TODO putback
data_i_do_object
	:	array_element
//	|	scalar_structure_component
//	|	data_implied_do
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
    : ( data_stmt_repeat '*' )? data_stmt_constant
    ;

data_stmt_value_list
    :    data_stmt_value ( data_stmt_value )*
    ;

// R531
// TODO putback
data_stmt_repeat
	:	scalar_int_constant
//	|	scalar_int_constant_subobject
	;

scalar_int_constant
    :    INT_CONSTANT
    ;

scalar_int_constant_subobject
    :    INT_CONSTANT
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
    :    'DIMENSION' ( '::' )? array_name '(' array_spec ')'
             ( ',' array_name '(' array_spec ')' )*
    ;

array_name
    :    name
    ;

// R536
intent_stmt
	:	'INTENT' '(' intent_spec ')' ( '::' )? dummy_arg_name_list
	;

dummy_arg_name_list
    : dummy_arg_name ( dummy_arg_name )*
    ;

// R537
optional_stmt
	:	'OPTIONAL' ( '::' )? dummy_arg_name_list
	;

// R538
parameter_stmt
	:	'PARAMETER' '(' named_constant_def_list ')'
	;

named_constant_def_list
    :    named_constant_def ( named_constant_def )*
    ;

// R539
// ERR_CHK 539 initialization_expr replaced by expr
named_constant_def
	:	named_constant '=' expr
	;

// R540
pointer_stmt
	:	'POINTER' ( '::' )? pointer_decl_list
	;

pointer_decl_list
    :    pointer_decl ( pointer_decl )*
    ;

// R541
// TODO putback
pointer_decl
    : object_name ( '(' deferred_shape_spec_list ')' )?
//    | proc_entity_name
    ;

proc_entity_name
    :    name
    ;

// R542
protected_stmt
	:	'PROTECTED'
		( '::' )?
		entity_name_list
	;

// R543
save_stmt
    : 'SAVE' ( ( '::' )? saved_entity_list )?
    ;

// R544
saved_entity
	:	object_name
//	|	proc_pointer_name
//	|	'/' common_block_name '/'
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
    : 'TARGET' ( '::' )? object_name ( '(' array_spec ')' )?
             ( ',' object_name ( '(' array_spec ')' )? )*
    ;

// R547
value_stmt
	:	'VALUE' ( '::' )? dummy_arg_name_list
	;

// R548
volatile_stmt
	:	'VOLATILE' ( '::' )? object_name_list
	;

// R549
implicit_stmt
	:	'IMPLICIT' implicit_spec_list
	|	'IMPLICIT' 'NONE'
	;

// R550
implicit_spec
	:	declaration_type_spec '(' letter_spec_list ')'
	;

implicit_spec_list
    :    implicit_spec ( implicit_spec )*
    ;

// R551
letter_spec 
    : Letter ( '?' Letter )?
    ;

letter_spec_list
    :    letter_spec ( letter_spec )*
    ;

// R552
namelist_stmt
    : 'NAMELIST' '/' namelist_group_name '/' namelist_group_object_list
         ( ( ',' )? '/' namelist_group_name '/' namelist_group_object_list )*
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
	:	'EQUIVALENCE' equivalence_set_list
	;

// R555
equivalence_set
	:	'(' equivalence_object ',' equivalence_object_list ')'
	;

equivalence_set_list
    :    equivalence_set ( equivalence_set )*
    ;

// R556
// TODO putback
equivalence_object
	:	variable_name
//	|	array_element
//	|	substring
	;

equivalence_object_list
    :    equivalence_object ( equivalence_object )
    ;

// R557
common_stmt
    : 'COMMON' ( '/' ( common_block_name )? '/' )? common_block_object_list
         ( ( ',' )? '/' ( common_block_name )? '/' common_block_object_list )*
    ;

// R558
common_block_object
    : variable_name ( '(' explicit_shape_spec_list ')' )?
//    | proc_pointer_name
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
		'('
		substring_range
		')'
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
		':'
		( expr )?
	;

// R612
// TODO putback
data_ref
    : part_ref
//      ( '%' part_ref )*
    ;

// R613
// TODO putback
part_ref
    : part_name
//      ( '(' section_subscript_list ')' )?
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
		'%'
		type_param_name
	;

// R616
array_element
	:	data_ref
	;

// R617
array_section
    :    data_ref ( '(' substring_range ')' )?
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
    :    section_subscript ( section_subscript )*
    ;

// R620
subscript_triplet
    : ( subscript )? ':' ( subscript )? ( ':' stride )?
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
    :    'ALLOCATE' '(' ( type_spec '::' )? allocation_list ( ',' alloc_opt_list )? ')'
    ;

// R624
// ERR_CHK 624 source_expr replaced by expr
alloc_opt
	:	'STAT' '=' stat_variable
	|	'ERRMSG' '=' errmsg_variable
	|	'SOURCE' '=' expr
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

// R627 inlined source_expr was expr

// R628
allocation
    : allocate_object ( '(' allocate_shape_spec_list ')' )?
    ;

allocation_list
    :    allocation ( allocation )*
    ;

// R629
// TODO putback
allocate_object
	:	variable_name
//	|	structure_component
	;

allocate_object_list
    :    allocate_object ( allocate_object )*
    ;

// R630
// ERR_CHK 630a lower_bound_expr replaced by expr
// ERR_CHK 630b upper_bound_expr replaced by expr
// TODO putback
allocate_shape_spec
    :    ( expr ':' )?
//          expr
    ;

// TODO putback
allocate_shape_spec_list
    :    allocate_shape_spec
//         ( allocate_shape_spec )*
    ;

// R631 inlined lower_bound_expr was scalar_int_expr

// R632 inlined upper_bound_expr was scalar_int_expr

// R633
nullify_stmt
	:	'NULLIFY'
		'('
		pointer_object_list
		')'
	;

// R634
// TODO putback
pointer_object
	:	variable_name
//	|	structure_component
//	|	proc_pointer_name
	;

pointer_object_list
    :    pointer_object ( pointer_object )*
    ;

// R635
deallocate_stmt
    :    'DEALLOCATE' '(' allocate_object_list ( ',' dealloc_opt_list )? ')'
    ;

// R636
dealloc_opt
	:	'STAT' '=' stat_variable
	|	'ERRMSG' '=' errmsg_variable
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
//	|	designator
//	|	array_constructor
//	|	structure_constructor
//	|	function_reference
//	|	type_param_inquiry
//	|	type_param_name
//	|	'(' expr ')'
	;

// R702
level_1_expr
    : ( defined_unary_op )? primary
    ;

// R703
defined_unary_op
	:	'.' Letter ( Letter )* '.'
	;

// R704
mult_operand
    : level_1_expr ( power_op mult_operand )?
    ;

// R705
add_operand
    : /* ( add_operand mult_op )? */ mult_operand
    ;

// R706
level_2_expr
    : ( /* ( level_2_expr )? */ add_op )? add_operand
    ;

// R707
power_op
	:	'**'
	;

// R708
mult_op
	:	'*'
	|	'/'
	;

// R709
add_op
	:	'+'
	|	'?'
	;

// R710
level_3_expr
    : /* ( level_3_expr concat_op )? */ level_2_expr
    ;

// R711
concat_op
	:	'//'
	;

// R712
level_4_expr
    : /* ( level_3_expr rel_op )? */ level_3_expr
    ;

// R713
rel_op
	:	'.EQ.'
	|	'.NE.'
	|	'.LT.'
	|	'.LE.'
	|	'.GT.'
	|	'.GE.'
	|	'=='
	|	'/='
	|	'<'
	|	'<='
	|	'>'
	|	'>='
	;

// R714
and_operand
    :    ( not_op )? level_4_expr
    ;

// R715
or_operand
    : /* ( or_operand and_op )? */ and_operand
    ;

// R716
equiv_operand
    : /* ( equiv_operand or_op )? */ or_operand
    ;

// R717
level_5_expr
    : /* ( level_5_expr equiv_op )? */ equiv_operand
    ;

// R718
not_op
	:	'.NOT.'
	;

// R719
and_op
	:	'.AND.'
	;

// R720
or_op
	:	'.OR.'
	;

// R721
equiv_op
	:	'.EQV.'
	|	'.NEQV.'
	;

// R722
expr
    : /* ( expr defined_binary_op )? */ level_5_expr
    ;

// R723
defined_binary_op
	:	'.' Letter ( Letter )* '.'
	;

// created new rule to remove defined_unary_op or defined_binary_op ambiguity
defined_unary_or_binary_op
	:	'.' Letter ( Letter )* '.'
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
		'='
		expr
	;

// R735
// TODO putback
pointer_assignment_stmt
    :    data_pointer_object ( '(' bounds_spec_list ')' )? '=>' data_target
//    | data_pointer_object '(' bounds_remapping_list ')' '=>' data_target
//    | proc_pointer_object '=>' proc_target
    ;

// R736
data_pointer_object
	:	variable_name
	|	variable '%' data_pointer_component_name
	;

data_pointer_component_name
    :    name
    ;

// R737
// ERR_CHK 737 lower_bound_expr replaced by expr
bounds_spec
	:	expr
		':'
	;

bounds_spec_list
    :    bounds_spec ( bounds_spec )*
    ;

// R738
// ERR_CHK 738a lower_bound_expr replaced by expr
// ERR_CHK 738b upper_bound_expr replaced by expr
bounds_remapping
	:	expr
		':'
		expr
	;

bounds_remapping_list
    :    bounds_remapping ( bounds_remapping )*
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
	:	variable '%' procedure_component_name
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
	:	'WHERE'
		'('
		expr
		')'
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
    :    ( where_construct_name ':' )? 'WHERE' '(' expr ')'
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
	:	'ELSEWHERE'
		'('
		expr
		')'
		( where_construct_name )?
	;

// R750
elsewhere_stmt
	:	'ELSEWHERE'
		( where_construct_name )?
	;

// R751
end_where_stmt
	:	'END' 'WHERE' ( where_construct_name )?
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
    :    ( forall_construct_name ':' )? 'FORALL' forall_header
    ;

// R754
// ERR_CHK 754 scalar_mask_expr replaced by expr
forall_header
    : '(' forall_triplet_spec_list ( ',' expr )? ')'
    ;

// R755
forall_triplet_spec
    : index_name '=' subscript ':' subscript ( ':' stride )?
    ;

index_name
    :    name
    ;

forall_triplet_spec_list
    :    forall_triplet_spec ( forall_triplet_spec )*
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
	:	'END'
		'FORALL'
		( forall_construct_name )?
	;

forall_construct_name
    :   name
    ;

// R759
forall_stmt
	:	'FORALL'
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
    : ( if_construct_name ':' )? 'IF' '(' expr ')' 'THEN'
    ;

// R804
// ERR_CHK 804 scalar_logical_expr replaced by expr
else_if_stmt
	:	'ELSE' 'IF'
		'(' expr ')'
		'THEN'
		( if_construct_name )?
	;

// R805
else_stmt
	:	'ELSE'
		( if_construct_name )?
	;

// R806
end_if_stmt
	:	'END'
		'IF'
		( if_construct_name )?
	;

if_construct_name
    :    name
    ;

// R807
// ERR_CHK 807 scalar_logical_expr replaced by expr
if_stmt
	:	'IF'
		'('
		expr
		')'
		action_stmt
	;

// R808
case_construct
    :    select_case_stmt ( case_stmt block )* end_select_stmt
    ;

// R809
// ERR_CHK 809 case_expr replaced by expr
select_case_stmt
    :    ( case_construct_name ':' )? 'SELECT' 'CASE' '(' expr ')'
    ;

// R810
case_stmt
	:	'CASE'
		case_selector
		( case_construct_name )?
	;

// R811
end_select_stmt
	:	'END'
		'SELECT'
		( case_construct_name )?
	;

case_construct_name
    :   name
    ;

// R812 inlined case_expr with expr was either scalar_int_expr scalar_char_expr scalar_logical_expr

// inlined scalar_char_expr with expr was char_expr

// R813
case_selector
	:	'('
		case_value_range_list
		')'
	|	'DEFAULT'
	;

// R814
// TODO putback
case_value_range
	:	case_value
//	|	case_value ':'
//	|	':' case_value
//	|	case_value ':' case_value
	;

case_value_range_list
    :    case_value_range ( case_value_range )*
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
    : ( associate_construct_name ':' )? 'ASSOCIATE' '(' association_list ')'
    ;

associate_construct_name
    :    name
    ;

association_list
    :    association ( association )*
    ;

// R818
association
	:	associate_name '=>' selector
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
	:	'END'
		'ASSOCIATE'
		( associate_construct_name )?
	;

// R821
select_type_construct
    :    select_type_stmt ( type_guard_stmt block )* end_select_type_stmt
    ;

// R822
select_type_stmt
    : ( select_construct_name ':' )? 'SELECT' 'TYPE'
         '(' ( associate_name '=>' )? selector ')'
    ;

select_construct_name
    :    name
    ;

// R823
type_guard_stmt
	:	'TYPE'
		'IS'
		'('
		type_spec
		')'
		( select_construct_name )?
	|	'CLASS'
		'IS'
		'('
		type_spec
		')'
		( select_construct_name )?
	|	'CLASS'
		'DEFAULT'
		( select_construct_name )?
	;

// R824
// TODO putback
end_select_type_stmt
	:	'END'
		'SELECT'
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
    :    ( do_construct_name ':' )? 'DO' LABEL ( loop_control )?
    ;

do_construct_name
    :    name
    ;

// R829
nonlabel_do_stmt
    :    ( do_construct_name ':' )? 'DO' ( loop_control )?
    ;

// R830
// ERR_CHK 830a scalar_int_expr replaced by expr
// ERR_CHK 830b scalar_logical_expr replaced by expr
loop_control
    : ( ',' )? do_variable '=' expr ',' expr ( ',' expr )?
    | ( ',' )? 'WHILE' '(' expr ')'
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
	:	'END' 'DO' ( do_construct_name )?
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
	:	'CYCLE' ( do_construct_name )?
	;

// R844
exit_stmt
	:	'EXIT' ( do_construct_name )?
	;

// R845
goto_stmt
	:	'GO' 'TO' LABEL
	;

// R846
// ERR_CHK 846 scalar_int_expr replaced by expr
computed_goto_stmt
	:	'GO' 'TO' '(' label_list ')' ( ',' )? expr
	;

// R847
// ERR_CHK 847 scalar_numeric_expr replaced by expr
arithmetic_if_stmt
	:	'IF'
		'('
		expr
		')'
		LABEL
		','
		LABEL
		','
		LABEL
	;

// R848
continue_stmt
	:	'CONTINUE'
	;

// R849
stop_stmt
	:	'STOP' ( stop_code )?
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
//	|	'*'
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
	:	'OPEN' '(' connect_spec_list ')'
	;

// R905
// ERR_CHK 905a scalar_default_char_expr replaced by expr
// ERR_CHK 905b scalar_int_expr replaced by expr
// ERR_CHK 905c file_name_expr replaced by expr
connect_spec
    : ( 'UNIT' '=' )? file_unit_number
    | 'ACCESS' '=' expr // scalar_default_char_expr
    | 'ACTION' '=' expr // scalar_default_char_expr
    | 'ASYNCHRONOUS' '=' expr // scalar_default_char_expr
    | 'BLANK' '=' expr // scalar_default_char_expr
    | 'DECIMAL' '=' expr // scalar_default_char_expr
    | 'DELIM' '=' expr // scalar_default_char_expr
    | 'ENCODING' '=' expr // scalar_default_char_expr
    | 'ERR' '=' LABEL
    | 'FILE' '=' expr // file_name_expr
    | 'FORM' '=' expr // scalar_default_char_expr
    | 'IOMSG' '=' iomsg_variable
    | 'IOSTAT' '=' scalar_int_variable
    | 'PAD' '=' expr // scalar_default_char_expr
    | 'POSITION' '=' expr // scalar_default_char_expr
    | 'RECL' '=' expr // scalar_int_expr
    | 'ROUND' '=' expr // scalar_default_char_expr
    | 'SIGN' '=' expr // scalar_default_char_expr
    | 'STATUS' '=' expr // scalar_default_char_expr
    ;

connect_spec_list
    :    connect_spec ( connect_spec )*
    ;

// inlined scalar_default_char_expr

// R906 inlined file_name_expr with expr was scalar_default_char_expr

// R907
iomsg_variable
	:	scalar_default_char_variable
	;

// R908
close_stmt
	:	'CLOSE' '(' close_spec_list ')'
	;

// R909
// ERR_CHK 909 scalar_default_char_expr replaced by expr
close_spec
    : ( 'UNIT' '=' )? file_unit_number
    | 'IOSTAT' '=' scalar_int_variable
    | 'IOMSG' '=' iomsg_variable
    | 'ERR' '=' LABEL
    | 'STATUS' '=' expr
    ;

close_spec_list
    :    close_spec ( close_spec )*
    ;

// R910
read_stmt
    :    'READ' '(' io_control_spec_list ')' ( input_item_list )?
    |    'READ' format ( ',' input_item_list )?
    ;

// R911
write_stmt
	:	'WRITE' '(' io_control_spec_list ')' ( output_item_list )?
	;

// R912
print_stmt
    :    'PRINT' format ( ',' output_item_list )?
    ;

// R913
// ERR_CHK 913a scalar_default_char_expr replaced by expr
// ERR_CHK 913b scalar_int_expr replaced by expr
// ERR_CHK 913c scalar_char_initialization_expr replaced by expr
io_control_spec
    :    ( 'UNIT' '=' )? io_unit
//    | ( 'FMT' '=' )? format
//    | ( 'NML' '=' )? namelist_group_name
//    | 'ADVANCE' '=' expr // scalar_default_char_expr
//    | 'ASYNCHRONOUS' '=' expr // scalar_char_initialization_expr
//    | 'BLANK' '=' expr // scalar_default_char_expr
//    | 'DECIMAL' '=' expr // scalar_default_char_expr
//    | 'DELIM' '=' expr // scalar_default_char_expr
//    | 'END' '=' LABEL
//    | 'EOR' '=' LABEL
//    | 'ERR' '=' LABEL
//    | 'ID' '=' scalar_int_variable
//    | 'IOMSG' '=' iomsg_variable
//    | 'IOSTAT' '=' scalar_int_variable
//    | 'PAD' '=' expr // scalar_default_char_expr
//    | 'POS' '=' expr // scalar_int_expr
//    | 'REC' '=' expr // scalar_int_expr
//    | 'ROUND' '=' expr // scalar_default_char_expr
//    | 'SIGN' '=' expr // scalar_default_char_expr
//    | 'SIZE' '=' scalar_int_variable
    ;

io_control_spec_list
    :    io_control_spec ( io_control_spec )*
    ;

// R914
// ERR_CHK 914 default_char_expr replaced by expr
format
	:	expr
	|	LABEL
	|	'*'
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
	:	'(' io_implied_do_object_list ',' io_implied_do_control ')'
	;

// R918
// TODO putback
io_implied_do_object
	:	input_item
//	|	output_item
	;

io_implied_do_object_list
    :    io_implied_do_object ( io_implied_do_object )*
    ;

// R919
// ERR_CHK 919 scalar_int_expr replaced by expr
io_implied_do_control
    : do_variable '=' expr ',' expr ( ',' expr )?
    ;

// R920
dtv_type_spec
	:	'TYPE'
		'('
		derived_type_spec
		')'
	|	'CLASS'
		'('
		derived_type_spec
		')'
	;

// R921
wait_stmt
	:	'WAIT' '(' wait_spec_list ')'
	;

// R922
wait_spec
    : ( 'UNIT' '=' )? file_unit_number
    | 'END' '=' LABEL
    | 'EOR' '=' LABEL
    | 'ERR' '=' LABEL
    | 'ID' '=' scalar_int_variable
    | 'IOMSG' '=' iomsg_variable
    | 'IOSTAT' '=' scalar_int_variable
    ;

wait_spec_list
    :    wait_spec ( wait_spec )*
    ;

// R923
backspace_stmt
	:	'BACKSPACE' file_unit_number
	|	'BACKSPACE' '(' position_spec_list ')'
	;

// R924
endfile_stmt
	:	'ENDFILE' file_unit_number
	|	'ENDFILE' '(' position_spec_list ')'
	;

// R925
rewind_stmt
	:	'REWIND' file_unit_number
	|	'REWIND' '(' position_spec_list ')'
	;

// R926
position_spec
    : ( 'UNIT' '=' )? file_unit_number
    | 'IOMSG' '=' iomsg_variable
    | 'IOSTAT' '=' scalar_int_variable
    | 'ERR' '=' LABEL
    ;

position_spec_list
    :    position_spec ( position_spec )*
    ;

// R927
flush_stmt
	:	'FLUSH' file_unit_number
	|	'FLUSH' '(' flush_spec_list ')'
	;

// R928
flush_spec
    : ( 'UNIT' '=' )? file_unit_number
    | 'IOSTAT' '=' scalar_int_variable
    | 'IOMSG' '=' iomsg_variable
    | 'ERR' '=' LABEL
    ;

flush_spec_list
    :    flush_spec ( flush_spec )*
    ;

// R929
inquire_stmt
	:	'INQUIRE' '(' inquire_spec_list ')'
	|	'INQUIRE' '(' 'IOLENGTH' '=' scalar_int_variable ')' output_item_list
	;

// R930
// ERR_CHK 930 file_name_expr replaced by expr
inquire_spec
    : ( 'UNIT' '=' )? file_unit_number
    | 'FILE' '=' expr
    | 'ACCESS' '=' scalar_default_char_variable
    | 'ACTION' '=' scalar_default_char_variable
    | 'ASYNCHRONOUS' '=' scalar_default_char_variable
    | 'BLANK' '=' scalar_default_char_variable
    | 'DECIMAL' '=' scalar_default_char_variable
    | 'DELIM' '=' scalar_default_char_variable
    | 'DIRECT' '=' scalar_default_char_variable
    | 'ENCODING' '=' scalar_default_char_variable
    | 'ERR' '=' LABEL
    | 'EXIST' '=' scalar_default_logical_variable
    | 'FORM' '=' scalar_default_char_variable
    | 'FORMATTED' '=' scalar_default_char_variable
    | 'ID' '=' scalar_int_variable
    | 'IOMSG' '=' iomsg_variable
    | 'IOSTAT' '=' scalar_int_variable
    | 'NAME' '=' scalar_default_char_variable
    | 'NAMED' '=' scalar_default_logical_variable
    | 'NEXTREC' '=' scalar_int_variable
    | 'NUMBER' '=' scalar_int_variable
    | 'OPENED' '=' scalar_default_logical_variable
    | 'PAD' '=' scalar_default_char_variable
    | 'PENDING' '=' scalar_default_logical_variable
    | 'POS' '=' scalar_int_variable
    | 'POSITION' '=' scalar_default_char_variable
    | 'READ' '=' scalar_default_char_variable
    | 'READWRITE' '=' scalar_default_char_variable
    | 'RECL' '=' scalar_int_variable
    | 'ROUND' '=' scalar_default_char_variable
    | 'SEQUENTIAL' '=' scalar_default_char_variable
    | 'SIGN' '=' scalar_default_char_variable
    | 'SIZE' '=' scalar_int_variable
    | 'STREAM' '=' scalar_default_char_variable
    | 'UNFORMATTED' '=' scalar_default_char_variable
    | 'WRITE' '=' scalar_default_char_variable
    ;

inquire_spec_list
    :    inquire_spec ( inquire_spec )*
    ;

/*
Section 10:
 */

// R1001
format_stmt
	:	'FORMAT' format_specification
	;

// R1002
format_specification
	:	'(' ( format_item_list )? ')'
	;

// R1003
format_item
	:	( int_literal_constant )? data_edit_desc
	|	control_edit_desc
	|	char_string_edit_desc
	|	( int_literal_constant )? '(' format_item_list ')'
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
// TODO putback
data_edit_desc
    : 'I' int_literal_constant ( '.' int_literal_constant )?
//    | 'B' int_literal_constant ( '.' int_literal_constant )?
//    | 'O' int_literal_constant ( '.' int_literal_constant )?
//    | 'Z' int_literal_constant ( '.' int_literal_constant )?
//    | 'F' int_literal_constant '.' int_literal_constant
//    | 'E' int_literal_constant '.' int_literal_constant ( 'E' int_literal_constant )?
//    | 'EN' int_literal_constant '.' int_literal_constant ( 'E' int_literal_constant )?
//    | 'ES' int_literal_constant '.' int_literal_constant ( 'E' int_literal_constant )?
//    | 'G' int_literal_constant '.' int_literal_constant ( 'E' int_literal_constant )?
//    | 'L' int_literal_constant
//    | 'A' ( int_literal_constant )?
//    | 'D' int_literal_constant '.' int_literal_constant
//    | 'DT' ( char_literal_constant )? ( '(' v_list ')' )?
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
	|	( int_literal_constant )? '/'
	|	':'
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

// TODO convert to fragment
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
//		( specification_part )?
//		( execution_part )?
//		( internal_subprogram_part )?
		end_program_stmt
	;

// R1102
program_stmt
	:	'PROGRAM'
		program_name
	;

// R1103
end_program_stmt
options {k=2;}
	:	('END' 'PROGRAM') => 'END' 'PROGRAM' ( program_name )?
	|	'END'
	;
	
program_name
    :    name
    ;

// R1104
// TODO putback
module
	:	module_stmt
//		( specification_part )?
//		( module_subprogram_part )?
		end_module_stmt
	;

// R1105
module_stmt
	:	'MODULE'
		module_name
	;

// R1106
end_module_stmt
options {k=2;}
	:	('END' 'MODULE') => 'END' 'MODULE' ( module_name )?
	|	'END'
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
// TODO putback
module_subprogram
	:	function_subprogram
//	|	subroutine_subprogram
	;

// R1109
use_stmt
    :    'USE' ( ( ',' module_nature )? '::' )? module_name ( ',' rename_list )?
    |    'USE' ( ( ',' module_nature )? '::' )? module_name ',' 'ONLY' ':' ( only_list )?
    ;

// R1110
module_nature
	:	'INTRINSIC'
	|	'NON'	'INTRINSIC'
	;

// R1111
// inlined local_defined_operator with defined_unary_or_binary_op
// inlined use_defined_operator with defined_unary_or_binary_op
rename
	:	local_name '=>' use_name
	|	'OPERATOR' '(' defined_unary_or_binary_op '(' '=>'
		'OPERATOR' '(' defined_unary_or_binary_op '('
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
// TODO putback
only
	:	generic_spec
//	|	only_use_name
//	|	rename
	;

only_list
    :    only ( only )*
    ;

// R1113
only_use_name
	:	use_name
	;

// R1114 inlined local_defined_operator in R1111 as defined_unary_or_binary_op

// R1115 inlined use_defined_operator in R1111 as defined_unary_or_binary_op

// R1116
block_data
	:	block_data_stmt
//		( specification_part )?
		end_block_data_stmt
	;

// R1117
// TODO putback
block_data_stmt
	:	'BLOCK'
		'DATA'
//		( block_data_name )?
	;

// R1118
// TODO putback
end_block_data_stmt
    : 'END'
//       ( 'BLOCK' 'DATA' ( block_data_name )? )?
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
	:	'INTERFACE' ( generic_spec )?
	|	'ABSTRACT' 'INTERFACE'
	;

// R1204
end_interface_stmt
	:	'END' 'INTERFACE' ( generic_spec )?
	;

// R1205
// TODO putback
interface_body
	:	function_stmt // ( specification_part )? end_function_stmt
//	|	subroutine_stmt ( specification_part )? end_subroutine_stmt
	;

// R1206
procedure_stmt
	:	( 'MODULE' )? 'PROCEDURE' procedure_name_list
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
	|	'OPERATOR' '(' defined_operator ')'
	|	'ASSIGNMENT' '(' '=' ')'
	|	dtio_generic_spec
	;

generic_name
    :   name
    ;

// R1208
dtio_generic_spec
	:	'READ' '(' 'FORMATTED' ')'
	|	'READ' '(' 'UNFORMATTED' ')'
	|	'WRITE' '(' 'FORMATTED' ')'
	|	'WRITE' '(' 'UNFORMATTED' ')'
	;

// R1209
import_stmt
    :    'IMPORT' ( ( '::' )? import_name_list )?
    ;

import_name
    :    name
    ;

import_name_list
    :    import_name ( import_name )*
    ;

// R1210
external_stmt
	:	'EXTERNAL' ( '::' )? external_name_list
	;

external_name
    :    name
    ;

external_name_list
    :    external_name ( external_name )*
    ;

// R1211
procedure_declaration_stmt
    :    'PROCEDURE' '(' ( proc_interface )? ')'
            ( ( ',' proc_attr_spec )* '::' )? proc_decl_list
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
	|	'INTENT' '(' intent_spec ')'
	|	'OPTIONAL'
	|	'POINTER'
	|	'SAVE'
	;

// R1214
proc_decl
    :    procedure_entity_name ( '=>' null_init )?
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
	:	'INTRINSIC'
		( '::' )?
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
		'('
		( actual_arg_spec_list )?
		')'
	;

// R1218
call_stmt
    :    'CALL' procedure_designator ( '(' ( actual_arg_spec_list )? ')' )?
    ;

// R1219
// TODO putback
procedure_designator
	:	procedure_name
//	|	proc_component_ref
//	|	data_ref '%' binding_name
	;

binding_name
    :    name
    ;

binding_name_list
    :    binding_name ( binding_name )*
    ;

// R1220
actual_arg_spec
    : ( keyword '=' )? actual_arg
    ;

actual_arg_spec_list
    :    actual_arg_spec ( actual_arg_spec )*
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
	:	'*' LABEL
	;

// R1223
// TODO putback
function_subprogram
	:	function_stmt
//		( specification_part )?
//		( execution_part )?
//		( internal_subprogram_part )?
		end_function_stmt
	;

// R1224
function_stmt
	:	( prefix )? 'FUNCTION' // function_name
//		'(' ( dummy_arg_name_list )? ')' ( suffix )?
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
	|	'RECURSIVE'
	|	'PURE'
	|	'ELEMENTAL'
	;

// R1229
// TODO putback
suffix
    :    proc_language_binding_spec ( 'RESULT' '(' result_name ')' )?
//    | 'RESULT' '(' result_name ')' ( proc_language_binding_spec )?
    ;

result_name
    :    name
    ;

// R1230
// TODO putback
end_function_stmt
    : 'END'
//       ( 'FUNCTION' ( function_name )? )?
    ;

// R1231
// TODO putback
subroutine_subprogram
	:	subroutine_stmt
//		( specification_part )?
//		( execution_part )?
//		( internal_subprogram_part )?
		end_subroutine_stmt
	;

// R1232
subroutine_stmt
    :     ( prefix )? 'SUBROUTINE' subroutine_name
          ( '(' ( dummy_arg_list )? ')' ( proc_language_binding_spec )? )?
    ;

// R1233
dummy_arg
	:	dummy_arg_name
	|	'*'
	;

dummy_arg_list
    :    dummy_arg ( dummy_arg )*
    ;

// R1234
// TODO putback
end_subroutine_stmt
    :    'END'
//         ( 'SUBROUTINE' ( subroutine_name )? )?
    ;

subroutine_name
    :    name
    ;

// R1235
// TODO putback
entry_stmt
    :    'ENTRY' entry_name
//          ( '(' ( dummy_arg_list )? ')' ( suffix )? )?
    ;

entry_name
    :    name
    ;

// R1236
// ERR_CHK 1236 scalar_int_expr replaced by expr
return_stmt
	:	'RETURN' ( expr )?
	;

// R1237
contains_stmt
	:	'CONTAINS'
	;

// R1238
// ERR_CHK 1239 scalar_expr replaced by expr
stmt_function_stmt
	:	function_name
		'('
		( dummy_arg_name_list )?
		')'
		'='
		expr
	;

/*
Lexer rules
 */

// R304
T_IDENT
	:	Letter ( Alphanumeric_Character )*
	;

// R308
INT_CONSTANT
	:	Digit_String
	;	

// R313
// TODO putback
LABEL
    :   Digit
//        ( Digit ( Digit ( Digit ( Digit )? )? )? )?
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
