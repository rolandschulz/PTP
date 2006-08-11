
/**
 * NOTES
 *
 * R303 underscore - added _ to rule (what happened to it?)
 * R1209 import-stmt: MISSING a ]
 *
 */


/**
 * TOKENS - removed from grammar
 */

tokens {
 UNDERSCORE = '_';
 PLUS = '+';
 QUESTION = '?';
 DOT = '.';
 STAR = '*';
 STARSTAR = '**';
 COLON = ':';
 COLONCOLON = '::';
 KIND_EQ = ('KIND' '=');
 RPAREN = '(';
 LPAREN = ')';
 RBRACKET = '[';
 LBRACKET = ']';
 PERCENT = '%';
 SLASH = '/';
 SLASHSLASH = '//';
 COMMA = ',';
 EQUAL = '=';
 EQUAL_GREATER = '=>';
 DOT_TRUE = '.TRUE.'
 DOT_FALSE = '.FALSE.'
 DOT_EQ = '.EQ.';
 DOT_NE = '.NE.';
 DOT_LT = '.LT.';
 DOT_LE = '.LE.';
 DOT_GT = '.GT.';
 DOT_GE = '.GE.';
 EQUAL_EQ = '==';
 SLASH_EQ = '/=';
 LESSTHAN = '<';
 LESSTHAN_EQ = '<=';
 GREATERTHAN = '>';
 GREATERTHAN_EQ = '>=';
 DOT_NOT = '.NOT.';
 DOT_AND = '.AND.';
 DOT_OR = '.OR.';
 DOT_EQV = '.EQV.';
 DOT_NEQV = '.NEQV.';
 PROGRAM = 'PROGRAM';
 INTERFACE = 'INTERFACE';
 MODULE = 'MODULE';
 IN = 'IN';
 OUT = 'OUT';
 TYPE = 'TYPE';
 CLASS = 'CLASS';
 EXTENDS = 'EXTENDS';
 INTEGER = 'INTEGER';
 REAL = 'REAL';
 DOUBLE = 'DOUBLE';
 PRECISION = 'PRECISION';
 COMPLEX = 'COMPLEX';
 CHARACTER = 'CHARACTER';
 LOGICAL = 'LOGICAL';
 ASYNCHRONOUS = 'ASYNCHRONOUS';
 BIND = 'BIND';
 COMMON = 'COMMON';
 DATA = 'DATA';
 DIMENSION = 'DIMENSION';
 EQUIVALENCE = 'EQUIVALENCE';
 IMPLICIT = 'IMPLICIT';
 INTRINSIC = 'INTRINSIC';
 PARAMETER = 'PARAMETER';
 POINTER = 'SAVE'
 TARGET = 'TARGET';
 USE = 'USE';
 VOLATILE = 'VOLATILE';
 ALLOCATABLE = 'ALLOCATABLE';
 DEALLOCATE = 'DEALLOCATE';
 NAMELIST = 'NAMELIST';
 SAVE = 'SAVE';
 CASE = 'CASE';
 END = 'END';
 IMPORT = 'IMPORT';
 SUBROUTINE = 'SUBROUTINE';
 FUNCTION = 'FUNCTION';
 RESULT = 'RESULT';
 CALL = 'CALL';
 ENTRY = 'ENTRY';
 PASS = 'PASS';
 NOPASS = 'NOPASS';
 NON = 'NON';
 OVERRIDABLE = 'OVERRIDABLE';
 DEFERRED = 'DEFERRED';
 IF = 'IF';
 THEN = 'THEN';
 ELSE = 'ELSE';
 WHERE = 'WHERE';
 ELSEWHERE = 'ELSEWHERE';
 FORALL = 'FORALL';
 FORMAT = 'FORMAT';
 READ = 'READ';
 WRITE = 'WRITE';
 PRINT = 'PRINT';
 GENERIC = 'GENERIC';
}


/**
Section 1:



/*****
R101  xyz-list                     is xyz [ , xyz ] ...

R102  xyz-name                     is name

R103  scalar-xyz                   is xyz
******/

/**
C101  (R103) scalar-xyz shall be scalar.
 */

/**
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

/**
C201  (R208) An execution-part shall not contain an end-function-stmt, end-program-stmt, or end-
      subroutine-stmt.
 */

// R215
keyword
	:	name
	;

/**
Section 3:
 */

// R301
character
	:	alphanumeric_character
	|	special_character
	;

// R302
alphanumeric_character
	:	letter
	|	DIGIT
	|	UNDERSCORE
	;


/**
 * convert to terminal
R303  underscore                  : _
 */

// R304
name
	:	letter
		( alphanumeric_character )*
	;

/**
C301  (R304) The maximum length of a name is 63 characters.
 */

// R305
constant
	:	literal_constant
	|	named_constant
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

/**
C302  (R308)int-constant shall be of type integer.
 */

// R309
char_constant
	:	constant
	;

/**
C303  (R309) char-constant shall be of type character.
 */

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


/** TODO: Done - converted to terminal
R313  label                        is digit [ digit [ digit [ digit [ digit ] ] ] ]
 */

/**
C304  (R313) At least one digit in a label shall be nonzero.
 */

/**
Section 4:
 */

// R401
type_spec
	:	intrinsic_type_spec
	|	derived_type_spec
	;

/**
C401  (R401) The derived-type-spec shall not specify an abstract type (4.5.6).
 */

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

/**
C402  (R402) The type-param-value for a kind type parameter shall be an initialization expression.
 */

/**
C403  (R402) A colon may be used as a type-param-value only in the declaration of an entity or
      component that has the POINTER or ALLOCATABLE attribute.
 */

// R403
intrinsic_type_spec
	:	INTEGER
		( kind_selector )?
	|	REAL
		( kind_selector )?
	|	DOUBLE
		PRECISION
	|	COMPLEX
		( kind_selector )?
	|	CHARACTER
		( char_selector )?
	|	LOGICAL
		( kind_selector )?
	;

// R404
kind_selector
    : LPAREN ( KIND_EQ )? scalar_int_initialization_expr RPAREN
    ;

/**
C404  (R404) The value of scalar-int-initialization-expr shall be nonnegative and shall specify a rep-
      resentation method that exists on the processor.
 */

// R405
signed_int_literal_constant
	:	( sign )?
		int_literal_constant
	;

// R406
int_literal_constant
	:	digit_string
		( kind_param )?
	;

// R407
kind_param
	:	digit_string
	|	scalar_int_constant_name
	;

// R408
signed_digit_string
	:	( sign )?
		digit_string
	;

// R409
digit_string
	:	DIGIT
		( DIGIT )*
	;

// R410
sign
	:	PLUS
	|	QUESTION
	;

/**
C405  (R407) A scalar-int-constant-name shall be a named constant of type integer.
 */

/**
C406  (R407) The value of kind-param shall be nonnegative.
 */

/**
C407  (R406) The value of kind-param shall specify a representation method that exists on the pro-
      cessor.
 */

// R411
boz_literal_constant
	:	BINARY_CONSTANT
	|	OCTAL_CONSTANT
	|	HEX_CONSTANT
	;

/** TODO: Done - converted to terminal
R412 binary-constant              is B ' digit [ digit ] ... '
                                  or B " digit [ digit ] ... "
 */

/**
C408 (R412) digit shall have one of the values 0 or 1.
 */


/** TODO: Done - converted to terminal
R413 octal-constant               is O ' digit [ digit ] ... '
                                  or O " digit [ digit ] ... "
 */

/**
C409 (R413) digit shall have one of the values 0 through 7.
 */


/** TODO: Done - converted to terminal
R414 hex-constant                 is Z ' hex-digit [ hex-digit ] ... '
                                  or Z " hex-digit [ hex-digit ] ... "
R415 hex-digit                    is DIGIT

                                  or A

                                  or B

                                  or C

                                  or D

                                  or E

                                  or F
 */

/**
C410 (R411) A boz-literal-constant shall appear only as a data-stmt-constant in a DATA statement, as
     the actual argument associated with the dummy argument A of the numeric intrinsic functions
     DBLE, REAL or INT, or as the actual argument associated with the X or Y dummy argument
     of the intrinsic function CMPLX.
 */

// R416
signed_real_literal_constant
	:	( sign )? real_literal_constant
	;

// R417
real_literal_constant
    :   significand ( exponent_letter exponent )? ( kind_param )?
    |   digit_string exponent_letter exponent ( kind_param )?
    ;

// R418
significand
	:	digit_string
		DOT
		( digit_string )?
	|	DOT
		digit_string
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

/**
C411 (R417) If both kind-param and exponent-letter are present, exponent-letter shall be E.
 */

/**
C412 (R417) The value of kind-param shall specify an approximation method that exists on the
     processor.
 */

// R421
complex_literal_constant
	:	LPAREN
		real_part
		COMMA
		imag_part
		RPAREN
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

/**
C413 (R421) Each named constant in a complex literal constant shall be of type integer or real.
 */

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

/**
C414 (R424) The value of scalar-int-initialization-expr shall be nonnegative and shall specify a rep-
     resentation method that exists on the processor.
 */

/**
C415 (R426) The scalar-int-literal-constant shall not include a kind-param.
 */

/**
C416 (R424 R425 R426) A type-param-value of * may be used only in the following ways:
 */

/**
C417 A function name shall not be declared with an asterisk type-param-value unless it is of type CHAR-
     ACTER and is the name of the result of an external function or the name of a dummy function.
 */

/**
C418 A function name declared with an asterisk type-param-value shall not be an array, a pointer, recursive, or pure.
 */

/**
C419 (R425) The optional comma in a length-selector is permitted only in a declaration-type-spec in a type-declaration-stmt.
 */

/**
C420 (R425) The optional comma in a length-selector is permitted only if no double-colon separator appears in the type-declaration-stmt.
 */

/**
C421 (R424) The length specified for a character statement function or for a statement function dummy argument of type character shall be an initialization expression.
 */


// R427
char_literal_constant
    :    ( kind_param ) SINGLE_QUOTE ( rep_char )* SINGLE_QUOTE
    |    ( kind_param ) SINGLE_QUOTE ( rep_char )* SINGLE_QUOTE
    ;

/**
C422 (R427) The value of kind-param shall specify a representation method that exists on the pro-
     cessor.
 */


// R428
logical_literal_constant
    :    DOT_TRUE ( kind_param )?
    |    DOT_FALSE ( kind_param )?
    ;

/**
C423 (R428) The value of kind-param shall specify a representation method that exists on the pro-
     cessor.
 */

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

type_attr_name_list
    :    type_attr_name ( type_attr_name )*
    ;

// R431
type_attr_spec
	:	access_spec
	|	EXTENDS
		LPAREN
		parent_type_name
		RPAREN
	|	ABSTRACT
	|	BIND
		LPA
		r
		EN
		C
		LPAREN
	;

/**
C424 (R430) A derived type type-name shall not be DOUBLEPRECISION or the same as the name
     of any intrinsic type defined in this standard.
 */

/**
C425 (R430) The same type-attr-spec shall not appear more than once in a given derived-type-stmt.
 */

/**
C426 (R431) A parent-type-name shall be the name of an accessible extensible type (4.5.6).
 */

/**
C427 (R429) If the type definition contains or inherits (4.5.6.1) a deferred binding (4.5.4), ABSTRACT
     shall appear.
 */

/**
C428 (R429) If ABSTRACT appears, the type shall be extensible.
 */

/**
C429 (R429) If EXTENDS appears, SEQUENCE shall not appear.
 */

// R432
private_or_sequence
	:	private_components_stmt
	|	sequence_stmt
	;

/**
C430 (R429) The same private-or-sequence shall not appear more than once in a given derived-type-
     def .
 */

// R433
end_type_stmt
	:	END
		TYPE
		( type_name )?
	;

type_name
    :    name
    ;

/**
C431 (R433) If END TYPE is followed by a type-name, the type-name shall be the same as that in
     the corresponding derived-type-stmt.
 */

// R434
sequence_stmt
	:	SEQUENCE
	;

/**
C432 (R438) If SEQUENCE appears, all derived types specified in component definitions shall be
     sequence types.
 */

/**
C433 (R429) If SEQUENCE appears, a type-bound-procedure-part shall not appear.
 */

// R435
type_param_def_stmt
	:	INTEGER
		( kind_selector )?
		COMMA
		type_param_attr_spec
		COLONCOLON
		type_param_decl_list
	;

// R436
type_param_decl
    :    type_param_name ( EQUAL scalar_int_initialization_expr )?
    ;

/**
C434 (R435) A type-param-name in a type-param-def-stmt in a derived-type-def shall be one of the
     type-param-names in the derived-type-stmt of that derived-type-def .
 */

/**
C435 (R435) Each type-param-name in the derived-type-stmt in a derived-type-def shall appear as a
     type-param-name in a type-param-def-stmt in that derived-type-def .
 */

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
	|	DIMENSION
		LPAREN
		component_array_spec
		RPAREN
	|	ALLOCATABLE
	|	access_spec
	;

// R442
component_decl
    :    component_name ( LPAREN component_array_spec RPAREN )?
                        ( STAR char_length )? ( component_initialization )?
    ;

// R443
component_array_spec
	:	explicit_shape_spec_list
	|	deferred_shape_spec_list
	;

// R444
component_initialization
	:	EQUAL
		initialization_expr
	|	EQUAL_GREATER
		null_init
	;

/**
C436 (R440) No component-attr-spec shall appear more than once in a given component-def-stmt.
 */

/**
C437 (R440) A component declared with the CLASS keyword (5.1.1.2) shall have the ALLOCATABLE
     or POINTER attribute.
 */

/**
C438 (R440) If the POINTER attribute is not specified for a component, the declaration-type-spec in
     the component-def-stmt shall specify an intrinsic type or a previously defined derived type.
 */

/**
C439 (R440) If the POINTER attribute is specified for a component, the declaration-type-spec in the
     component-def-stmt shall specify an intrinsic type or any accessible derived type including the
     type being defined.
 */

/**
C440 (R440) If the POINTER or ALLOCATABLE attribute is specified, each component-array-spec
     shall be a deferred-shape-spec-list.
 */

/**
C441 (R440) If neither the POINTER attribute nor the ALLOCATABLE attribute is specified, each
     component-array-spec shall be an explicit-shape-spec-list.
 */

/**
C442 (R443) Each bound in the explicit-shape-spec shall either be an initialization expression or be a
     specification expression that does not contain references to specification functions or any object
     designators other than named constants or subobjects thereof.
 */

/**
C443 (R440) A component shall not have both the ALLOCATABLE and the POINTER attribute.
 */

/**
C444 (R442) The * char-length option is permitted only if the type specified is character.
 */

/**
C445 (R439) Each type-param-value within a component-def-stmt shall either be a colon, be an ini-
     tialization expression, or be a specification expression that contains neither references to speci-
     fication functions nor any object designators other than named constants or subobjects thereof.
 */

/**
C446 (R440) If component-initialization appears, a double-colon separator shall appear before the
     component-decl-list.
 */

/**
C447 (R440) If => appears in component-initialization, POINTER shall appear in the component-
     attr-spec-list. If = appears in component-initialization, POINTER or ALLOCATABLE shall
     not appear in the component-attr-spec-list.
 */

// R445
proc_component_def_stmt
	:	PROCEDURE
		LPAREN
		( proc_interface )?
		RPAREN
		COMMA
		proc_component_attr_spec_list
		COLONCOLON
		proc_decl_list
	;

// R446
proc_component_attr_spec
    :    POINTER
    |    PASS ( LPAREN arg-name RPAREN )?
    |    NOPASS
    |    access_spec
    ;

/**
C448 (R445) The same proc-component-attr-spec shall not appear more than once in a given proc-
     component-def-stmt.
 */

/**
C449 (R445) POINTER shall appear in each proc-component-attr-spec-list.
 */

/**
C450 (R445) If the procedure pointer component has an implicit interface or has no arguments,
     NOPASS shall be specified.
 */

/**
C451 (R445) If PASS (arg-name) appears, the interface shall have a dummy argument named arg-
     name.
 */

/**
C452 (R445) PASS and NOPASS shall not both appear in the same proc-component-attr-spec-list.
 */

/**
C453 The passed-object dummy argument shall be a scalar, nonpointer, nonallocatable dummy data
     object with the same declared type as the type being defined; all of its length type parameters
     shall be assumed; it shall be polymorphic (5.1.1.2) if and only if the type being defined is
     extensible (4.5.6).
 */

// R447
private_components_stmt
	:	PRIVATE
	;

/**
C454 (R447) A private-components-stmt is permitted only if the type definition is within the specifi-
     cation part of a module.
 */

// R448
type_bound_procedure_part
	:	contains_stmt
		( binding_private_stmt )?
		proc_binding_stmt
		( proc_binding_stmt )*
	;

// R449
binding_private_stmt
	:	PRIVATE
	;

/**
C455 (R448) A binding-private-stmt is permitted only if the type definition is within the specification
     part of a module.
 */

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

/**
C456 (R451) If => procedure-name appears, the double-colon separator shall appear.
 */

/**
C457 (R451) If => procedure-name appears, interface-name shall not appear.
 */

/**
C458 (R451) The procedure-name shall be the name of an accessible module procedure or an external
     procedure that has an explicit interface.
 */

// R452
generic_binding
    :    GENERIC ( COMMA access_spec )? COLONCOLON generic_spec EQUAL_GREATER binding_name_list
    ;

/**
C459 (R452) Within the specification-part of a module, each generic-binding shall specify, either
     implicitly or explicitly, the same accessibility as every other generic-binding with that generic-
     spec in the same derived type.
 */

/**
C460 (R452) Each binding-name in binding-name-list shall be the name of a specific binding of the
     type.
 */

/**
C461 (R452) If generic-spec is not generic-name, each of its specific bindings shall have a passed-object
     dummy argument (4.5.3.3).
 */

/**
C462 (R452) If generic-spec is OPERATOR ( defined-operator ), the interface of each binding shall
     be as specified in 12.3.2.1.1.
 */

/**
C463 (R452) If generic-spec is ASSIGNMENT ( = ), the interface of each binding shall be as specified
     in 12.3.2.1.2.
 */

/**
C464 (R452) If generic-spec is dtio-generic-spec, the interface of each binding shall be as specified in
     9.5.3.7. The type of the dtv argument shall be type-name.
 */

// R453
binding_attr
    : PASS ( LPAREN arg_name RPAREN )?
    | NOPASS
    | NON OVERRIDABLE
    | DEFERRED
    | access_spec
    ;

/**
C465 (R453) The same binding-attr shall not appear more than once in a given binding-attr-list.
 */

/**
C466 (R451) If the interface of the binding has no dummy argument of the type being defined,
     NOPASS shall appear.
 */

/**
C467 (R451) If PASS (arg-name) appears, the interface of the binding shall have a dummy argument
     named arg-name.
 */

/**
C468 (R453) PASS and NOPASS shall not both appear in the same binding-attr-list.
 */

/**
C469 (R453) NON OVERRIDABLE and DEFERRED shall not both appear in the same binding-
     attr-list.
 */

/**
C470 (R453) DEFERRED shall appear if and only if interface-name appears.
 */

/**
C471 (R451) An overriding binding (4.5.6.2) shall have the DEFERRED attribute only if the binding
     it overrides is deferred.
 */

/**
C472 (R451) A binding shall not override an inherited binding (4.5.6.1) that has the NON OVER-
     RIDABLE attribute.
 */

// R454
final_binding
	:	FINAL
		( COLONCOLON )?
		final_subroutine_name_list
	;

/**
C473 (R454) A final-subroutine-name shall be the name of a module procedure with exactly one
     dummy argument. That argument shall be nonoptional and shall be a nonpointer, nonallocat-
     able, nonpolymorphic variable of the derived type being defined. All length type parameters of
     the dummy argument shall be assumed. The dummy argument shall not be INTENT(OUT).
 */

/**
C474 (R454) A final-subroutine-name shall not be one previously specified as a final subroutine for
     that type.
 */

/**
C475 (R454) A final subroutine shall not have a dummy argument with the same kind type parameters
     and rank as the dummy argument of another final subroutine of that type.
 */

// R455
derived_type_spec
    : type_name ( LPAREN type_param_spec_list RPAREN )?
    ;

// R456
type_param_spec
    : ( keyword EQUAL )? type_param_value
    ;

/**
C476 (R455) type-name shall be the name of an accessible derived type.
 */

/**
C477 (R455) type-param-spec-list shall appear only if the type is parameterized.
 */

/**
C478 (R455) There shall be at most one type-param-spec corresponding to each parameter of the type.
     If a type parameter does not have a default value, there shall be a type-param-spec corresponding
     to that type parameter.
 */

/**
C479 (R456) The keyword= may be omitted from a type-param-spec only if the keyword= has been
     omitted from each preceding type-param-spec in the type-param-spec-list.
 */

/**
C480 (R456) Each keyword shall be the name of a parameter of the type.
 */

/**
C481 (R456) An asterisk may be used as a type-param-value in a type-param-spec only in the decla-
     ration of a dummy argument or associate name or in the allocation of a dummy argument.
 */

// R457
structure_constructor
	:	derived_type_spec
		LPAREN
		( component_spec_list )?
		RPAREN
	;

// R458
component_spec
: ( keyword EQUAL )? component_data_source
    ;

// R459
component_data_source
	:	expr
	|	data_target
	|	proc_target
	;

/**
C482 (R457) The derived-type-spec shall not specify an abstract type (4.5.6).
 */

/**
C483 (R457) At most one component-spec shall be provided for a component.
 */

/**
C484 (R457) If a component-spec is provided for a component, no component-spec shall be provided
     for any component with which it is inheritance associated.
 */

/**
C485 (R457) A component-spec shall be provided for a component unless it has default initialization
     or is inheritance associated with another component for which a component-spec is provided or
     that has default initialization.
 */

/**
C486 (R458) The keyword= may be omitted from a component-spec only if the keyword= has been
     omitted from each preceding component-spec in the constructor.
 */

/**
C487 (R458) Each keyword shall be the name of a component of the type.
 */

/**
C488 (R457) The type name and all components of the type for which a component-spec appears shall
     be accessible in the scoping unit containing the structure constructor.
 */

/**
C489 (R457) If derived-type-spec is a type name that is the same as a generic name, the component-
     spec-list shall not be a valid actual-arg-spec-list for a function reference that is resolvable as a
     generic reference (12.4.4.1).
 */

/**
C490 (R459) A data-target shall correspond to a nonprocedure pointer component; a proc-target shall
     correspond to a procedure pointer component.
 */

/**
C491 (R459) A data-target shall have the same rank as its corresponding component.
 */

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

// R464
end_enum_stmt
	:	END
		ENUM
	;

/**
C492 (R462) If = appears in an enumerator, a double-colon separator shall appear before the enu-
     merator-list.
 */

// R465
array_constructor
	:	LPAREN
		SLASH
		ac_spec
		SLASH
		RPAREN
	|	left_square_bracket
		ac_spec
		right_square_bracket
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

// R470
ac_implied_do
	:	LPAREN
		ac_value_list
		COMMA
		ac_implied_do_control
		RPAREN
	;

// R471
ac_implied_do_control
    :    ac_do_variable EQUAL scalar_int_expr COMMA scalar_int_expr ( COMMA scalar_int_expr )?
    ;

// R472
ac_do_variable
	:	scalar_int_variable
	;

/**
C493  (R472) ac-do-variable shall be a named variable.
 */

/**
C494  (R466) If type-spec is omitted, each ac-value expression in the array-constructor shall have the
      same type and kind type parameters.
 */

/**
C495  (R466) If type-spec specifies an intrinsic type, each ac-value expression in the array-constructor
      shall be of an intrinsic type that is in type conformance with a variable of type type-spec as
      specified in Table 7.8.
 */

/**
C496  (R466) If type-spec specifies a derived type, all ac-value expressions in the array-constructor
      shall be of that derived type and shall have the same kind type parameter values as specified by
      type-spec.
 */

/**
C497  (R470) The ac-do-variable of an ac-implied-do that is in another ac-implied-do shall not appear
      as the ac-do-variable of the containing ac-implied-do.
 */

/**
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

/**
C501  (R502) In a declaration-type-spec, every type-param-value that is not a colon or an asterisk shall
      be a specification-expr.
 */

/**
C502  (R502) In a declaration-type-spec that uses the CLASS keyword, derived-type-spec shall specify
      an extensible type.
 */

/**
C503  (R502) The TYPE(derived-type-spec) shall not specify an abstract type (4.5.6).
 */

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

/**
C504  (R504) If a type-param-value in a char-length in an entity-decl is not a colon or an asterisk, it
      shall be a specification-expr.
 */

// R505
object_name
	:	name
	;

/**
C505  (R505) The object-name shall be the name of a data object.
 */

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

/**
C506 (R507) The function-reference shall be a reference to the NULL intrinsic function with no
     arguments.
 */

/**
C507 (R501) The same attr-spec shall not appear more than once in a given type-declaration-stmt.
 */

/**
C508 An entity shall not be explicitly given any attribute more than once in a scoping unit.
 */

/**
C509 (R501) An entity declared with the CLASS keyword shall be a dummy argument or have the
     ALLOCATABLE or POINTER attribute.
 */

/**
C510 (R501) An array that has the POINTER or ALLOCATABLE attribute shall be specified with
     an array-spec that is a deferred-shape-spec-list (5.1.2.5.3).
 */

/**
C511 (R501) An array-spec for an object-name that is a function result that does not have the AL-
     LOCATABLE or POINTER attribute shall be an explicit-shape-spec-list.
 */

/**
C512 (R501) If the POINTER attribute is specified, the ALLOCATABLE, TARGET, EXTERNAL,
     or INTRINSIC attribute shall not be specified.
 */

/**
C513 (R501) If the TARGET attribute is specified, the POINTER, EXTERNAL, INTRINSIC, or
     PARAMETER attribute shall not be specified.
 */

/**
C514 (R501) The PARAMETER attribute shall not be specified for a dummy argument, a pointer,
     an allocatable entity, a function, or an object in a common block.
 */

/**
C515 (R501) The INTENT, VALUE, and OPTIONAL attributes may be specified only for dummy
     arguments.
 */

/**
C516 (R501) The INTENT attribute shall not be specified for a dummy procedure without the
     POINTER attribute.
 */

/**
C517 (R501) The SAVE attribute shall not be specified for an object that is in a common block, a
     dummy argument, a procedure, a function result, an automatic data object, or an object with
     the PARAMETER attribute.
 */

/**
C518 An entity shall not have both the EXTERNAL attribute and the INTRINSIC attribute.
 */

/**
C519 (R501) An entity in an entity-decl-list shall not have the EXTERNAL or INTRINSIC attribute
     specified unless it is a function.
 */

/**
C520 (R504) The * char-length option is permitted only if the type specified is character.
 */

/**
C521 (R504) The function-name shall be the name of an external function, an intrinsic function, a
     function dummy procedure, or a statement function.
 */

/**
C522 (R501) The initialization shall appear if the statement contains a PARAMETER attribute
     (5.1.2.10).
 */

/**
C523 (R501) If initialization appears, a double-colon separator shall appear before the entity-decl-list.
 */

/**
C524 (R504)initialization shall not appear if object-name is a dummy argument, a function result, an
     object in a named common block unless the type declaration is in a block data program unit,
     an object in blank common, an allocatable variable, an external name, an intrinsic name, or an
     automatic object.
 */

/**
C525 (R504) If => appears in initialization, the object shall have the POINTER attribute. If =
     appears in initialization, the object shall not have the POINTER attribute.
 */

/**
C526 (R501) If the VOLATILE attribute is specified, the PARAMETER, INTRINSIC, EXTERNAL,
     or INTENT(IN) attribute shall not be specified.
 */

/**
C527 (R501) If the VALUE attribute is specified, the PARAMETER, EXTERNAL, POINTER,
     ALLOCATABLE, DIMENSION, VOLATILE, INTENT(INOUT), or INTENT(OUT) attribute
     shall not be specified.
 */

/**
C528 (R501) If the VALUE attribute is specified for a dummy argument of type character, the length
     parameter shall be omitted or shall be specified by an initialization expression with the value
      one.
 */

/**
C529  (R501) The VALUE attribute shall not be specified for a dummy procedure.
 */

/**
C530  (R501) The ALLOCATABLE, POINTER, or OPTIONAL attribute shall not be specified for a
      dummy argument of a procedure that has a proc-language-binding-spec.
 */

/**
C531  (R503) A language-binding-spec shall appear only in the specification part of a module.
 */

/**
C532  (R501) If a language-binding-spec is specified, the entity declared shall be an interoperable
      variable (15.2).
 */

/**
C533  (R501) If a language-binding-spec with a NAME= specifier appears, the entity-decl-list shall
      consist of a single entity-decl.
 */

/**
C534  (R503) The PROTECTED attribute is permitted only in the specification part of a module.
 */

/**
C535  (R501) The PROTECTED attribute is permitted only for a procedure pointer or named variable
      that is not in a common block.
 */

/**
C536  (R501) If the PROTECTED attribute is specified, the EXTERNAL, INTRINSIC, or PARAM-
      ETER attribute shall not be specified.
 */

/**
C537  A nonpointer object that has the PROTECTED attribute and is accessed by use association
      shall not appear in a variable definition context (16.5.7) or as the data-target or proc-target in
      a pointer-assignment-stmt.
 */

/**
C538  A pointer object that has the PROTECTED attribute and is accessed by use association shall
      not appear as
    (1)   A pointer-object in a pointer-assignment-stmt or nullify-stmt,
    (2)   An allocate-object in an allocate-stmt or deallocate-stmt, or
    (3)   An actual argument in a reference to a procedure if the associated dummy argument is a
          pointer with the INTENT(OUT) or INTENT(INOUT) attribute.
 */

// R508
access_spec
	:	PUBLIC
	|	PRIVATE
	;

/**
C539  (R508) An access-spec shall appear only in the specification-part of a module.
 */

// R509
language_binding_spec
    : BIND LPAREN C ( COMMA NAME EQUAL scalar_char_initialization_expr )? RPAREN

/**
C540  (R509) The scalar-char-initialization-expr shall be of default character kind.
 */

// R510
array_spec
	:	explicit_shape_spec_list
	|	assumed_shape_spec_list
	|	deferred_shape_spec_list
	|	assumed_size_spec
	;

/**
C541  (R510)The maximum rank is seven.
 */

// R511
explicit_shape_spec
    : ( lower_bound COLON )? upper_bound
    ;

// R512
lower_bound
	:	specification_expr
	;

// R513
upper_bound
	:	specification_expr
	;

/**
C542  (R511) An explicit-shape array whose bounds are not initialization expressions shall be a dummy
      argument, a function result, or an automatic array of a procedure.
 */

// R514
assumed_shape_spec
	:	( lower_bound )?
		COLON
	;

// R515
deferred_shape_spec
	:	COLON
	;

// R516
assumed_size_spec
    : ( explicit_shape_spec_list COMMA )? ( lower_bound COLON )? STAR
    ;

/**
C543  An assumed-size-spec shall not appear except as the declaration of the array bounds of a dummy
      data argument.
 */

/**
C544  An assumed-size array with INTENT (OUT) shall not be of a type for which default initialization
      is specified.
 */

// R517
intent_spec
	:	IN
	|	OUT
	|	INOUT
	;

/**
C545 (R517) A nonpointer object with the INTENT (IN) attribute shall not appear in a variable
     definition context (16.5.7).
 */

/**
C546 (R517) A pointer object with the INTENT (IN) attribute shall not appear as
 */

/**
C547 (R503) (R1216) If the name of a generic intrinsic procedure is explicitly declared to have the
     INTRINSIC attribute, and it is also the generic name in one or more generic interfaces (12.3.2.1)
     accessible in the same scoping unit, the procedures in the interfaces and the specific intrinsic
     procedures shall all be functions or all be subroutines, and the characteristics of the specific
     intrinsic procedures and the procedures in the interfaces shall differ as specified in 16.2.3.
 */

// R518
access_stmt
    :    access_spec ( ( COLONCOLON )? access_id_list )?
    ;

// R519
access_id
	:	use_name
	|	generic_spec
	;

/**
C548 (R518) An access-stmt shall appear only in the specification-part of a module. Only one ac-
     cessibility statement with an omitted access-id-list is permitted in the specification-part of a
     module.
 */

/**
C549 (R519) Each use-name shall be the name of a named variable, procedure, derived type, named
     constant, or namelist group.
 */

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

// R523
bind_entity
	:	entity_name
	|	SLASH
		common_block_name
		SLASH
	;

/**
C550 (R522) If any bind-entity in a bind-stmt is an entity-name, the bind-stmt shall appear in the
     specification part of a module and the entity shall be an interoperable variable (15.2.4, 15.2.5).
 */

/**
C551 (R522) If the language-binding-spec has a NAME= specifier, the bind-entity-list shall consist of
     a single bind-entity.
 */

/**
C552 (R522) If a bind-entity is a common block, each variable of the common block shall be interop-
     erable (15.2.4, 15.2.5).
 */


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

// R529
data_i_do_variable
	:	scalar_int_variable
	;

/**
C553 (R526) In a variable that is a data-stmt-object, any subscript, section subscript, substring start-
     ing point, and substring ending point shall be an initialization expression.
 */

/**
C554 (R526) A variable whose designator is included in a data-stmt-object-list or a data-i-do-object-
     list shall not be: a dummy argument, made accessible by use association or host association, in
     a named common block unless the DATA statement is in a block data program unit, in a blank
     common block, a function name, a function result name, an automatic object, or an allocatable
     variable.
 */

/**
C555 (R526) A data-i-do-object or a variable that appears as a data-stmt-object shall not be an object
     designator in which a pointer appears other than as the entire rightmost part-ref .
 */

/**
C556 (R529) The data-i-do-variable shall be a named variable.
 */

/**
C557 (R527) A scalar-int-expr of a data-implied-do shall involve as primaries only constants, subob-
     jects of constants, or DO variables of the containing data-implied-dos, and each operation shall
     be intrinsic.
 */

/**
C558 (R528) The array-element shall be a variable.
 */

/**
C559 (R528) The scalar-structure-component shall be a variable.
 */

/**
C560 (R528) The scalar-structure-component shall contain at least one part-ref that contains a sub-
     script-list.
 */

/**
C561 (R528) In an array-element or a scalar-structure-component that is a data-i-do-object, any sub-
     script shall be an expression whose primaries are either constants, subobjects of constants, or
     DO variables of this data-implied-do or the containing data-implied-dos, and each operation shall
     be intrinsic.
 */

// R530
data_stmt_value
    : ( data_stmt_repeat STAR )? data_stmt_constant
    ;

// R531
data_stmt_repeat
	:	scalar_int_constant
	|	scalar_int_constant_subobject
	;

/**
C562 (R531) The data-stmt-repeat shall be positive or zero. If the data-stmt-repeat is a named con-
     stant, it shall have been declared previously in the scoping unit or made accessible by use
     association or host association.
 */

// R532
data_stmt_constant
	:	scalar_constant
	|	scalar_constant_subobject
	|	signed_int_literal_constant
	|	signed_real_literal_constant
	|	null_init
	|	structure_constructor
	;

/**
C563 (R532) If a DATA statement constant value is a named constant or a structure constructor, the
     named constant or derived type shall have been declared previously in the scoping unit or made
     accessible by use or host association.
 */

/**
C564 (R532) If a data-stmt-constant is a structure-constructor, it shall be an initialization expression.
 */

// R533
int_constant_subobject
	:	constant_subobject
	;

/**
C565 (R533) int-constant-subobject shall be of type integer.
 */

// R534
constant_subobject
	:	designator
	;

/**
C566 (R534) constant-subobject shall be a subobject of a constant.
 */

/**
C567 (R534) Any subscript, substring starting point, or substring ending point shall be an initializa-
     tion expression.
 */

// R535
dimension_stmt
    :    DIMENSION ( COLONCOLON )? array_name LPAREN array_spec RPAREN
             ( COMMA array_name LPAREN array_spec RPAREN )*
    ;

// R536
intent_stmt
	:	INTENT
		LPAREN
		intent_spec
		RPAREN
		( COLONCOLON )?
		dummy_arg_name_list
	;

// R537
optional_stmt
	:	OPTIONAL
		( COLONCOLON )?
		dummy_arg_name_list
	;

// R538
parameter_stmt
	:	PARAMETER
		LPAREN
		named_constant_def_list
		RPAREN
	;

// R539
named_constant_def
	:	named_constant
		EQUAL
		initialization_expr
	;

// R540
pointer_stmt
	:	POINTER
		( COLONCOLON )?
		pointer_decl_list
	;

// R541
pointer_decl
    : object_name ( LPAREN deferred_shape_spec_list RPAREN )?
    | proc_entity_name
    ;

/**
C568 (R541) A proc-entity-name shall also be declared in a procedure-declaration-stmt.
 */

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
	|	SLASH
		common_block_name
		SLASH
	;

// R545
proc_pointer_name
	:	name
	;

/**
C569 (R545) A proc-pointer-name shall be the name of a procedure pointer.
 */

/**
C570 (R543) If a SAVE statement with an omitted saved entity list occurs in a scoping unit, no other
     explicit occurrence of the SAVE attribute or SAVE statement is permitted in the same scoping
     unit.
 */

// R546
target_stmt
    : TARGET ( COLONCOLON )? object_name ( LPAREN array_spec RPAREN )?
             ( COMMA object_name ( LPAREN array_spec RPAREN )? )*
    ;

// R547
value_stmt
	:	VALUE
		( COLONCOLON )?
		dummy_arg_name_list
	;

// R548
volatile_stmt
	:	VOLATILE
		( COLONCOLON )?
		object_name_list
	;

// R549
implicit_stmt
	:	IMPLICIT
		implicit_spec_list
	|	IMPLICIT
		NONE
	;

// R550
implicit_spec
	:	declaration_type_spec
		LPAREN
		letter_spec_list
		RPAREN
	;

// R551
letter_spec
    : letter ( QUESTION letter )?
    ;

/**
C571 (R549) If IMPLICIT NONE is specified in a scoping unit, it shall precede any PARAMETER
     statements that appear in the scoping unit and there shall be no other IMPLICIT statements
     in the scoping unit.
 */

/**
C572 (R551) If the minus and second letter appear, the second letter shall follow the first letter
     alphabetically.
 */

// R552
namelist_stmt
    : NAMELIST SLASH namelist_group_name SLASH namelist_group_object_list
         ( ( COMMA )? SLASH namelist_group_name SLASH namelist_group_object_list )*
    ;

/**
C573 (R552) The namelist-group-name shall not be a name made accessible by use association.
 */

// R553
namelist_group_object
	:	variable_name
	;

/**
C574 (R553) A namelist-group-object shall not be an assumed-size array.
 */

/**
C575 (R552) A namelist-group-object shall not have the PRIVATE attribute if the namelist-group-
     name has the PUBLIC attribute.
 */

// R554
equivalence_stmt
	:	EQUIVALENCE
		equivalence_set_list
	;

// R555
equivalence_set
	:	LPAREN
		equivalence_object
		COMMA
		equivalence_object_list
		RPAREN
	;

// R556
equivalence_object
	:	variable_name
	|	array_element
	|	substring
	;

/**
C576 (R556) An equivalence-object shall not be a designator with a base object that is a dummy
     argument, a pointer, an allocatable variable, a derived-type object that has an allocatable ulti-
     mate component, an object of a nonsequence derived type, an object of a derived type that has
     a pointer at any level of component selection, an automatic object, a function name, an entry
     name, a result name, a variable with the BIND attribute, a variable in a common block that
     has the BIND attribute, or a named constant.
 */

/**
C577 (R556) An equivalence-object shall not be a designator that has more than one part-ref .
 */

/**
C578 (R556) An equivalence-object shall not have the TARGET attribute.
 */

/**
C579 (R556) Each subscript or substring range expression in an equivalence-object shall be an integer
     initialization expression (7.1.7).
 */

/**
C580 (R555) If an equivalence-object is of type default integer, default real, double precision real,
     default complex, default logical, or numeric sequence type, all of the objects in the equivalence
     set shall be of these types.
 */

/**
C581 (R555) If an equivalence-object is of type default character or character sequence type, all of the
      objects in the equivalence set shall be of these types.
 */

/**
C582  (R555) If an equivalence-object is of a sequence derived type that is not a numeric sequence or
      character sequence type, all of the objects in the equivalence set shall be of the same type with
      the same type parameter values.
 */

/**
C583  (R555) If an equivalence-object is of an intrinsic type other than default integer, default real,
      double precision real, default complex, default logical, or default character, all of the objects in
      the equivalence set shall be of the same type with the same kind type parameter value.
 */

/**
C584  (R556) If an equivalence-object has the PROTECTED attribute, all of the objects in the equiv-
      alence set shall have the PROTECTED attribute.
 */

/**
C585  (R556) The name of an equivalence-object shall not be a name made accessible by use association.
 */

/**
C586  (R556) A substring shall not have length zero.
 */

// R557
common_stmt
    : COMMON ( SLASH ( common_block_name )? SLASH )? common_block_object_list
         ( ( COMMA )? SLASH ( common_block_name )? SLASH common_block_object_list )*
    ;

/** TODO
R558  common_block_object     : variable_name ( LPAREN explicit_shape_spec_list RPAREN )?

                                | proc_pointer_name
 */

/**
C587  (R558) Only one appearance of a given variable-name or proc-pointer-name is permitted in all
      common-block-object-lists within a scoping unit.
 */

/**
C588  (R558) A common-block-object shall not be a dummy argument, an allocatable variable, a
      derived-type object with an ultimate component that is allocatable, an automatic object, a
      function name, an entry name, a variable with the BIND attribute, or a result name.
 */

/**
C589  (R558) If a common-block-object is of a derived type, it shall be a sequence type (4.5.1) or a
      type with the BIND attribute and it shall have no default initialization.
 */

/**
C590  (R558) A variable-name or proc-pointer-name shall not be a name made accessible by use
      association.
 */

/**
Section 6:
 */

// R601
variable
	:	designator
	;

/**
C601  (R601) designator shall not be a constant or a subobject of a constant.
 */

// R602
variable_name
	:	name
	;

/**
C602  (R602) A variable-name shall be the name of a variable.
 */

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

/**
C603  (R604) logical-variable shall be of type logical.
 */

// R605
default_logical_variable
	:	variable
	;

/**
C604  (R605) default-logical-variable shall be of type default logical.
 */

// R606
char_variable
	:	variable
	;

/**
C605  (R606) char-variable shall be of type character.
 */

// R607
default_char_variable
	:	variable
	;

/**
C606  (R607) default-char-variable shall be of type default character.
 */

// R608
int_variable
	:	variable
	;

/**
C607  (R608) int-variable shall be of type integer.
 */

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
	|	scalar_constant
	;

// R611
substring_range
	:	( scalar_int_expr )?
		COLON
		( scalar_int_expr )?
	;

/**
C608 (R610) parent-string shall be of type character.
 */


/** TODO
R612 data_ref               : part_ref ( PERCENT part_ref )*
 */


/** TODO
R613 part_ref               : part_name ( LPAREN section_subscript_list RPAREN )?
 */

/**
C609 (R612) Each part-name except the rightmost shall be of derived type.
 */

/**
C610 (R612) Each part-name except the leftmost shall be the name of a component of the declared
     type of the preceding part-name.
 */

/**
C611 (R612) If the rightmost part-name is of abstract type, data-ref shall be polymorphic.
 */

/**
C612 (R612) The leftmost part-name shall be the name of a data object.
 */

/**
C613 (R613) If a section-subscript-list appears, the number of section-subscripts shall equal the rank
     of part-name.
 */

/**
C614 (R612) There shall not be more than one part-ref with nonzero rank. A part-name to the right
     of a part-ref with nonzero rank shall not have the ALLOCATABLE or POINTER attribute.
 */

// R614
structure_component
	:	data_ref
	;

/**
C615 (R614) There shall be more than one part-ref and the rightmost part-ref shall be of the form
     part-name.
 */

// R615
type_param_inquiry
	:	designator
		PERCENT
		type_param_name
	;

/**
C616 (R615) The type-param-name shall be the name of a type parameter of the declared type of the
     object designated by the designator.
 */

// R616
array_element
	:	data_ref
	;

/**
C617 (R616) Every part-ref shall have rank zero and the last part-ref shall contain a subscript-list.
 */


/** TODO
R617 array_section is data_ref ( LPAREN substring_range RPAREN )?
 */

/**
C618 (R617) Exactly one part-ref shall have nonzero rank, and either the final part-ref shall have a
     section-subscript-list with nonzero rank or another part-ref shall have nonzero rank.
 */

/**
C619 (R617) If a substring-range appears, the rightmost part-name shall be of type character.
 */

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


/** TODO
R620 subscript_triplet      : ( subscript )? COLON ( subscript )? ( COLON stride )?
 */

// R621
stride
	:	scalar_int_expr
	;

// R622
vector_subscript
	:	int_expr
	;

/**
C620 (R622) A vector-subscript shall be an integer array expression of rank one.
 */

/**
C621 (R620) The second subscript shall not be omitted from a subscript-triplet in the last dimension
     of an assumed-size array.
 */

// R623
allocate_stmt
    :    ALLOCATE LPAREN ( type_spec COLONCOLON )? allocation_list ( COMMA alloc_opt_list )? RPAREN
    ;

// R624
alloc_opt
	:	STAT
		EQUAL
		stat_variable
	|	ERRMSG
		EQUAL
		errmsg_variable
	|	SOURCE
		EQUAL
		source_expr
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


/** TODO
R628  allocation              : allocate_object ( LPAREN allocate_shape_spec_list RPAREN )?
 */

// R629
allocate_object
	:	variable_name
	|	structure_component
	;


/** TODO
R630  allocate_shape_spec     : ( lower_bound_expr COLON )? upper_bound_expr
 */

// R631
lower_bound_expr
	:	scalar_int_expr
	;

// R632
upper_bound_expr
	:	scalar_int_expr
	;

/**
C622  (R629) Each allocate-object shall be a nonprocedure pointer or an allocatable variable.
 */

/**
C623  (R623) If any allocate-object in the statement has a deferred type parameter, either type-spec or
      SOURCE= shall appear.
 */

/**
C624  (R623) If a type-spec appears, it shall specify a type with which each allocate-object is type
      compatible.
 */

/**
C625  (R623) If any allocate-object is unlimited polymorphic, either type-spec or SOURCE= shall
      appear.
 */

/**
C626  (R623) A type-param-value in a type-spec shall be an asterisk if and only if each allocate-object
      is a dummy argument for which the corresponding type parameter is assumed.
 */

/**
C627  (R623) If a type-spec appears, the kind type parameter values of each allocate-object shall be
      the same as the corresponding type parameter values of the type-spec.
 */

/**
C628  (R628) An allocate-shape-spec-list shall appear if and only if the allocate-object is an array.
 */

/**
C629  (R628) The number of allocate-shape-specs in an allocate-shape-spec-list shall be the same as
      the rank of the allocate-object.
 */

/**
C630  (R624) No alloc-opt shall appear more than once in a given alloc-opt-list.
 */

/**
C631  (R623) If SOURCE= appears, type-spec shall not appear and allocation-list shall contain only
      one allocate-object, which shall be type compatible (5.1.1.2) with source-expr.
 */

/**
C632  (R623) The source-expr shall be a scalar or have the same rank as allocate-object.
 */

/**
C633  (R623) Corresponding kind type parameters of allocate-object and source-expr shall have the
      same values.
 */

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

/**
C634  (R634) Each pointer-object shall have the POINTER attribute.
 */

// R635
deallocate_stmt
    :    DEALLOCATE LPAREN allocate_object_list ( COMMA dealloc_opt_list )? RPAREN
    ;

/**
C635  (R635) Each allocate-object shall be a nonprocedure pointer or an allocatable variable.
 */

// R636
dealloc_opt
	:	STAT
		EQUAL
		stat_variable
	|	ERRMSG
		EQUAL
		errmsg_variable
	;

/**
C636  (R636) No dealloc-opt shall appear more than once in a given dealloc-opt-list.
 */

/**
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
	|	LPAREN
		expr
		RPAREN
	;

/**
C701  (R701) The type-param-name shall be the name of a type parameter.
 */

/**
C702  (R701) The designator shall not be a whole assumed-size array.
 */


/** TODO
R702 level_1_expr            : ( defined_unary_op )? primary
*/

// R703
defined_unary_op
	:	DOT
		letter
		( letter )*
		DOT
	;

/**
C703 (R703) A defined-unary-op shall not contain more than 63 letters and shall not be the same as
     any intrinsic-operator or logical-literal-constant.
 */


/** TODO
R704 mult_operand            : level_1_expr ( power_op mult_operand )?
 */


/** TODO
R705 add_operand             : ( add_operand mult_op )? mult_operand
 */


/** TODO
R706 level_2_expr            : ( ( level_2_expr )? add_op )? add_operand
 */

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


/** TODO
R710 level_3_expr            : ( level_3_expr concat_op )? level_2_expr
 */

// R711
concat_op
	:	SLASHSLASH
	;


/** TODO
R712 level_4_expr            : ( level_3_expr rel_op )? level_3_expr
 */

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


/** TODO
R714 and_operand             : ( not_op )? level_4_expr
 */


/** TODO
R715 or_operand              : ( or_operand and_op )? and_operand
 */


/** TODO
R716 equiv_operand           : ( equiv_operand or_op )? or_operand
 */


/** TODO
R717 level_5_expr            : ( level_5_expr equiv_op )? equiv_operand
 */

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


/** TODO
R722 expr                    : ( expr defined_binary_op )? level_5_expr
 */

// R723
defined_binary_op
	:	DOT
		letter
		( letter )*
		DOT
	;

/**
C704 (R723) A defined-binary-op shall not contain more than 63 letters and shall not be the same as
     any intrinsic-operator or logical-literal-constant.
 */

// R724
logical_expr
	:	expr
	;

/**
C705 (R724) logical-expr shall be of type logical.
 */

// R725
char_expr
	:	expr
	;

/**
C706 (R725) char-expr shall be of type character.
 */

// R726
default_char_expr
	:	expr
	;

/**
C707 (R726) default-char-expr shall be of type default character.
 */

// R727
int_expr
	:	expr
	;

/**
C708 (R727) int-expr shall be of type integer.
 */

// R728
numeric_expr
	:	expr
	;

/**
C709 (R728) numeric-expr shall be of type integer, real, or complex.
 */

// R729
specification_expr
	:	scalar_int_expr
	;

/**
C710 (R729) The scalar-int-expr shall be a restricted expression.
 */

// R730
initialization_expr
	:	expr
	;

/**
C711 (R730) initialization-expr shall be an initialization expression.
 */

// R731
char_initialization_expr
	:	char_expr
	;

/**
C712 (R731) char-initialization-expr shall be an initialization expression.
 */

// R732
int_initialization_expr
	:	int_expr
	;

scalar_int_initialization_expr
	:	int_initialization_expr
	;

/**
C713 (R732) int-initialization-expr shall be an initialization expression.
 */

// R733
logical_initialization_expr
	:	logical_expr
	;

/**
C714 (R733) logical-initialization-expr shall be an initialization expression.
 */

// R734
assignment_stmt
	:	variable
		EQUAL
		expr
	;

/**
C715 (R734) The variable in an assignment-stmt shall not be a whole assumed-size array.
 */

// R735
pointer_assignment_stmt
    :    data_pointer_object ( LPAREN bounds_spec_list RPAREN )? EQUAL_GREATER data_target
    | data_pointer_object LPAREN bounds_remapping_list RPAREN EQUAL_GREATER data_target
    | proc_pointer_object EQUAL_GREATER proc_target
    ;

// R736
data_pointer_object
	:	variable_name
	|	variable
		PERCENT
		data_pointer_component_name
	;

/**
C716 (R735) If data-target is not unlimited polymorphic, data-pointer-object shall be type compatible
     (5.1.1.2) with it, and the corresponding kind type parameters shall be equal.
 */

/**
C717 (R735) If data-target is unlimited polymorphic, data-pointer-object shall be unlimited polymor-
     phic, of a sequence derived type, or of a type with the BIND attribute.
 */

/**
C718 (R735) If bounds-spec-list is specified, the number of bounds-specs shall equal the rank of data-
     pointer-object.
 */

/**
C719 (R735) If bounds-remapping-list is specified, the number of bounds-remappings shall equal the
     rank of data-pointer-object.
 */

/**
C720 (R735) If bounds-remapping-list is specified, data-target shall have rank one; otherwise, the
     ranks of data-pointer-object and data-target shall be the same.
 */

/**
C721 (R736) A variable-name shall have the POINTER attribute.
 */

/**
C722 (R736) A data-pointer-component-name shall be the name of a component of variable that is a
     data pointer.
 */

// R737
bounds_spec
	:	lower_bound_expr
		COLON
	;

// R738
bounds_remapping
	:	lower_bound_expr
		COLON
		upper_bound_expr
	;

// R739
data_target
	:	variable
	|	expr
	;

/**
C723 (R739) A variable shall have either the TARGET or POINTER attribute, and shall not be an
     array section with a vector subscript.
 */

/**
C724 (R739) An expr shall be a reference to a function whose result is a data pointer.
 */

// R740
proc_pointer_object
	:	proc_pointer_name
	|	proc_component_ref
	;

// R741
proc_component_ref
	:	variable
		PERCENT
		procedure_component_name
	;

/**
C725 (R741) the procedure-component-name shall be the name of a procedure pointer component of
     the declared type of variable.
 */

// R742
proc_target
	:	expr
	|	procedure_name
	|	proc_component_ref
	;

/**
C726 (R742) An expr shall be a reference to a function whose result is a procedure pointer.
 */

/**
C727 (R742) A procedure-name shall be the name of an external, module, or dummy procedure, a
     specific intrinsic function listed in 13.6 and not marked with a bullet (·), or a procedure pointer.
 */

/**
C728 (R742) The proc-target shall not be a nonintrinsic elemental procedure.
 */

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

/**
C729 (R747) A where-assignment-stmt that is a defined assignment shall be elemental.
 */

/**
C730 (R744) If the where-construct-stmt is identified by a where-construct-name, the corresponding
     end-where-stmt shall specify the same where-construct-name. If the where-construct-stmt is
     not identified by a where-construct-name, the corresponding end-where-stmt shall not specify
     a where-construct-name. If an elsewhere-stmt or a masked-elsewhere-stmt is identified by a
     where-construct-name, the corresponding where-construct-stmt shall specify the same where-
     construct-name.
 */

/**
C731 (R746) A statement that is part of a where-body-construct shall not be a branch target statement.
 */

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

/**
C732 (R758) If the forall-construct-stmt has a forall-construct-name, the end-forall-stmt shall have
     the same forall-construct-name. If the end-forall-stmt has a forall-construct-name, the forall-
      construct-stmt shall have the same forall-construct-name.
 */

/**
C733  (R754) The scalar-mask-expr shall be scalar and of type logical.
 */

/**
C734  (R754) Any procedure referenced in the scalar-mask-expr, including one referenced by a defined
      operation, shall be a pure procedure (12.6).
 */

/**
C735  (R755) The index-name shall be a named scalar variable of type integer.
 */

/**
C736  (R755) A subscript or stride in a forall-triplet-spec shall not contain a reference to any index-
      name in the forall-triplet-spec-list in which it appears.
 */

/**
C737  (R756) A statement in a forall-body-construct shall not define an index-name of the forall-
      construct.
 */

/**
C738  (R756) Any procedure referenced in a forall-body-construct, including one referenced by a defined
      operation, assignment, or finalization, shall be a pure procedure.
 */

/**
C739  (R756) A forall-body-construct shall not be a branch target.
 */

// R759
forall_stmt
	:	FORALL
		forall_header
		forall_assignment_stmt
	;

/**
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

/**
C801  (R802) If the if-then-stmt of an if-construct specifies an if-construct-name, the corresponding
      end-if-stmt shall specify the same if-construct-name. If the if-then-stmt of an if-construct does
      not specify an if-construct-name, the corresponding end-if-stmt shall not specify an if-construct-
      name. If an else-if-stmt or else-stmt specifies an if-construct-name, the corresponding if-then-
      stmt shall specify the same if-construct-name.
 */

// R807
if_stmt
	:	IF
		LPAREN
		scalar_logical_expr
		RPAREN
		action_stmt
	;

/**
C802  (R807) The action-stmt in the if-stmt shall not be an if-stmt, end-program-stmt, end-function-
      stmt, or end-subroutine-stmt.
 */

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

/**
C803  (R808) If the select-case-stmt of a case-construct specifies a case-construct-name, the corre-
      sponding end-select-stmt shall specify the same case-construct-name. If the select-case-stmt
      of a case-construct does not specify a case-construct-name, the corresponding end-select-stmt
      shall not specify a case-construct-name. If a case-stmt specifies a case-construct-name, the
      corresponding select-case-stmt shall specify the same case-construct-name.
 */

// R812
case_expr
	:	scalar_int_expr
	|	scalar_char_expr
	|	scalar_logical_expr
	;

// R813
case_selector
	:	LPAREN
		case_value_range_list
		RPAREN
	|	DEFAULT
	;

/**
C804 (R808) No more than one of the selectors of one of the CASE statements shall be DEFAULT.
 */

// R814
case_value_range
	:	case_value
	|	case_value
		COLON
	|	COLON
		case_value
	|	case_value
		COLON
		case_value
	;

// R815
case_value
	:	scalar_int_initialization_expr
	|	scalar_char_initialization_expr
	|	scalar_logical_initialization_expr
	;

/**
C805 (R808) For a given case-construct, each case-value shall be of the same type as case-expr. For
     character type, the kind type parameters shall be the same; character length differences are
     allowed.
 */

/**
C806 (R808) A case-value-range using a colon shall not be used if case-expr is of type logical.
 */

/**
C807 (R808) For a given case-construct, the case-value-ranges shall not overlap; that is, there shall
     be no possible value of the case-expr that matches more than one case-value-range.
 */

// R816
associate_construct
	:	associate_stmt
		block
		end_associate_stmt
	;


/** TODO
R817 associate-stmt         : ( associate-construct-name COLON )? ASSOCIATE

                                         LPAREN association-list RPAREN
*/

// R818
association
	:	associate_name
		EQUAL_GREATER
		selector
	;

// R819
selector
	:	expr
	|	variable
	;

/**
C808 (R818) If selector is not a variable or is a variable that has a vector subscript, associate-name
     shall not appear in a variable definition context (16.5.7).
 */

/**
C809 (R818) An associate-name shall not be the same as another associate-name in the same associate-
     stmt.
 */

// R820
end_associate_stmt
	:	END
		ASSOCIATE
		( associate_construct_name )?
	;

/**
C810 (R820) If the associate-stmt of an associate-construct specifies an associate-construct-name,
     the corresponding end-associate-stmt shall specify the same associate-construct-name. If the
     associate-stmt of an associate-construct does not specify an associate-construct-name, the cor-
     responding end-associate-stmt shall not specify an associate-construct-name.
 */

// R821
select_type_construct
    :    select_type_stmt ( type_guard_stmt block )* end_select_type_stmt
    ;

/** TODO
R822 select_type_stmt       : ( select_construct_name COLON )? SELECT TYPE

                                         LPAREN ( associate_name EQUAL_GREATER )? selector RPAREN
*/

/**
C811 (R822) If selector is not a named variable, associate-name => shall appear.
 */

/**
C812 (R822) If selector is not a variable or is a variable that has a vector subscript, associate-name
     shall not appear in a variable definition context (16.5.7).
 */

/**
C813 (R822) The selector in a select-type-stmt shall be polymorphic.
 */

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

/**
C814 (R823) The type-spec shall specify that each length type parameter is assumed.
 */

/**
C815 (R823) The type-spec shall not specify a sequence derived type or a type with the BIND attribute.
 */

/**
C816 (R823) If selector is not unlimited polymorphic, the type-spec shall specify an extension of the
     declared type of selector.
 */

/**
C817 (R823) For a given select-type-construct, the same type and kind type parameter values shall
     not be specified in more than one TYPE IS type-guard-stmt and shall not be specified in more
     than one CLASS IS type-guard-stmt.
 */

/**
C818 (R823) For a given select-type-construct, there shall be at most one CLASS DEFAULT type-
     guard-stmt.
 */

// R824
end_select_type_stmt
	:	END
		SELECT
		( select_construct_name )?
	;

/**
C819 (R821) If the select-type-stmt of a select-type-construct specifies a select-construct-name, the
     corresponding end-select-type-stmt shall specify the same select-construct-name. If the select-
     type-stmt of a select-type-construct does not specify a select-construct-name, the corresponding
     end-select-type-stmt shall not specify a select-construct-name. If a type-guard-stmt specifies a
     select-construct-name, the corresponding select-type-stmt shall specify the same select-construct-
     name.
 */

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


/** TODO
R828 label-do-stmt           : ( do-construct-name COLON )? DO LABEL ( loop-control )?
*/


/** TODO
R829 nonlabel-do-stmt        : ( do-construct-name COLON )? DO ( loop-control )?
*/


/** TODO
R830 loop-control            : ( COMMA )? do-variable EQUAL scalar-int-expr, scalar-int-expr

                                         ( COMMA scalar-int-expr )?

                               | ( COMMA )? WHILE LPAREN scalar-logical-expr RPAREN
 */

// R831
do_variable
	:	scalar_int_variable
	;

/**
C820 (R831) The do-variable shall be a named scalar variable of type integer.
 */

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
	:	END
		DO
		( do_construct_name )?
	;

/**
C821 (R826) If the do-stmt of a block-do-construct specifies a do-construct-name, the corresponding
     end-do shall be an end-do-stmt specifying the same do-construct-name. If the do-stmt of a
     block-do-construct does not specify a do-construct-name, the corresponding end-do shall not
     specify a do-construct-name.
 */

/**
C822 (R826) If the do-stmt is a nonlabel-do-stmt, the corresponding end-do shall be an end-do-stmt.
 */

/**
C823 (R826) If the do-stmt is a label-do-stmt, the corresponding end-do shall be identified with the
     same label.
 */

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

/**
C824  (R838) A do-term-action-stmt shall not be a continue-stmt, a goto-stmt, a return-stmt, a stop-
      stmt, an exit-stmt, a cycle-stmt, an end-function-stmt, an end-subroutine-stmt, an end-program-
      stmt, or an arithmetic-if-stmt.
 */

/**
C825  (R835) The do-term-action-stmt shall be identified with a label and the corresponding label-do-
      stmt shall refer to the same label.
 */

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
	:	label_do_stmt
		do_body
		do_term_shared_stmt
	;

// R842
do_term_shared_stmt
	:	action_stmt
	;

/**
C826  (R842) A do-term-shared-stmt shall not be a goto-stmt, a return-stmt, a stop-stmt, an exit-
      stmt, a cycle-stmt, an end-function-stmt, an end-subroutine-stmt, an end-program-stmt, or an
      arithmetic-if-stmt.
 */

/**
C827  (R840) The do-term-shared-stmt shall be identified with a label and all of the label-do-stmts of
      the inner-shared-do-construct and outer-shared-do-construct shall refer to the same label.
 */

// R843
cycle_stmt
	:	CYCLE
		( do_construct_name )?
	;

/**
C828  (R843) If a cycle-stmt refers to a do-construct-name, it shall be within the range of that do-
      construct; otherwise, it shall be within the range of at least one do-construct.
 */

// R844
exit_stmt
	:	EXIT
		( do_construct_name )?
	;

/**
C829  (R844) If an exit-stmt refers to a do-construct-name, it shall be within the range of that do-
      construct; otherwise, it shall be within the range of at least one do-construct.
 */

// R845
goto_stmt
	:	GO
		TO
		LABEL
	;

/**
C830  (R845) The label shall be the statement label of a branch target statement that appears in the
      same scoping unit as the goto-stmt.
 */

// R846
computed_goto_stmt
	:	GO
		TO
		LPAREN
		label_list
		RPAREN
		( COMMA )?
		scalar_int_expr
	;

/**
C831  (R846 Each label in label-list shall be the statement label of a branch target statement that
      appears in the same scoping unit as the computed-goto-stmt.
 */

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

/**
C832  (R847) Each label shall be the label of a branch target statement that appears in the same
      scoping unit as the arithmetic-if-stmt.
 */

/**
C833  (R847) The scalar-numeric-expr shall not be of type complex.
 */

// R848
continue_stmt
	:	CONTINUE
	;

// R849
stop_stmt
	:	STOP
		( stop_code )?
	;


/** TODO
R850  stop-code               : scalar-char-constant

                                | digit ( digit ( digit ( digit ( digit )? )? )? )?
*/

/**
C834  (R850) scalar-char-constant shall be of type default character.
 */

/**
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

/**
C901 (R903) The char-variable shall not be an array section with a vector subscript.
 */

/**
C902 (R903) The char-variable shall be of type default character, ASCII character, or ISO 10646
     character.
 */

// R904
open_stmt
	:	OPEN
		LPAREN
		connect_spec_list
		RPAREN
	;


/** TODO
R905 connect-spec           : ( UNIT EQUAL )? file-unit-number

                              | ACCESS EQUAL scalar-default-char-expr

                              | ACTION EQUAL scalar-default-char-expr

                              | ASYNCHRONOUS EQUAL scalar-default-char-expr

                              | BLANK EQUAL scalar-default-char-expr

                              | DECIMAL EQUAL scalar-default-char-expr

                              | DELIM EQUAL scalar-default-char-expr

                              | ENCODING EQUAL scalar-default-char-expr

                              | ERR EQUAL LABEL

                              | FILE EQUAL file-name-expr

                              | FORM EQUAL scalar-default-char-expr

                              | IOMSG EQUAL iomsg-variable

                              | IOSTAT EQUAL scalar-int-variable

                              | PAD EQUAL scalar-default-char-expr

                              | POSITION EQUAL scalar-default-char-expr

                              | RECL EQUAL scalar-int-expr

                              | ROUND EQUAL scalar-default-char-expr

                              | SIGN EQUAL scalar-default-char-expr

                              | STATUS EQUAL scalar-default-char-expr
*/

// R906
file_name_expr
	:	scalar_default_char_expr
	;

// R907
iomsg_variable
	:	scalar_default_char_variable
	;

/**
C903 (R905) No specifier shall appear more than once in a given connect-spec-list.
 */

/**
C904 (R905) A file-unit-number shall be specified; if the optional characters UNIT= are omitted, the
     file-unit-number shall be the first item in the connect-spec-list.
 */

/**
C905 (R905) The label used in the ERR= specifier shall be the statement label of a branch target
     statement that appears in the same scoping unit as the OPEN statement.
 */

// R908
close_stmt
	:	CLOSE
		LPAREN
		close_spec_list
		RPAREN
	;


/** TODO
R909 close_spec             : ( UNIT EQUAL )? file_unit_number

                              | IOSTAT EQUAL scalar_int_variable

                              | IOMSG EQUAL iomsg_variable

                              | ERR EQUAL LABEL

                              | STATUS EQUAL scalar_default_char_expr
*/

/**
C906 (R909) No specifier shall appear more than once in a given close_spec_list.
 */

/**
C907 (R909) A file-unit-number shall be specified; if the optional characters UNIT= are omitted, the
     file-unit-number shall be the first item in the close-spec-list.
 */

/**
C908 (R909) The label used in the ERR= specifier shall be the statement label of a branch target
     statement that appears in the same scoping unit as the CLOSE statement.
 */

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

/**
C909 (R913) No specifier shall appear more than once in a given io-control-spec-list.
 */

/**
C910 (R913) An io-unit shall be specified; if the optional characters UNIT= are omitted, the io-unit
     shall be the first item in the io-control-spec-list.
 */

/**
C911 (R913) A DELIM= or SIGN= specifier shall not appear in a read-stmt.
 */

/**
C912 (R913) A BLANK=, PAD=, END=, EOR=, or SIZE= specifier shall not appear in a write-stmt.
 */

/**
C913 (R913) The label in the ERR=, EOR=, or END= specifier shall be the statement label of a
     branch target statement that appears in the same scoping unit as the data transfer statement.
 */

/**
C914 (R913) A namelist-group-name shall be the name of a namelist group.
 */

/**
C915 (R913) A namelist-group-name shall not appear if an input-item-list or an output-item-list
     appears in the data transfer statement.
 */

/**
C916 (R913) An io-control-spec-list shall not contain both a format and a namelist-group-name.
 */

/**
C917 (R913) If format appears without a preceding FMT=, it shall be the second item in the io-
     control-spec-list and the first item shall be io-unit.
 */

/**
C918 (R913) If namelist-group-name appears without a preceding NML=, it shall be the second item
     in the io-control-spec-list and the first item shall be io-unit.
 */

/**
C919 (R913) If io-unit is not a file-unit-number, the io-control-spec-list shall not contain a REC=
     specifier or a POS= specifier.
 */

/**
C920 (R913) If the REC= specifier appears, an END= specifier shall not appear, a namelist-group-
     name shall not appear, and the format, if any, shall not be an asterisk.
 */

/**
C921 (R913) An ADVANCE= specifier may appear only in a formatted sequential or stream in-
     put/output statement with explicit format specification (10.1) whose control information list
     does not contain an internal-file-variable as the io-unit.
 */

/**
C922 (R913) If an EOR= specifier appears, an ADVANCE= specifier also shall appear.
 */

/**
C923 (R913) If a SIZE= specifier appears, an ADVANCE= specifier also shall appear.
 */

/**
C924 (R913) The scalar-char-initialization-expr in an ASYNCHRONOUS= specifier shall be of type
     default character and shall have the value YES or NO.
 */

/**
C925 (R913) An ASYNCHRONOUS= specifier with a value YES shall not appear unless io-unit is a
     file-unit-number.
 */

/**
C926 (R913) If an ID= specifier appears, an ASYNCHRONOUS= specifier with the value YES shall
     also appear.
 */

/**
C927 (R913) If a POS= specifier appears, the io-control-spec-list shall not contain a REC= specifier.
 */

/**
C928 (R913) If a DECIMAL=, BLANK=, PAD=, SIGN=, or ROUND= specifier appears, a format
     or namelist-group-name shall also appear.
 */

/**
C929 (R913) If a DELIM= specifier appears, either format shall be an asterisk or namelist-group-name
     shall appear.
 */

// R914
format
	:	default_char_expr
	|	LABEL
	|	STAR
	;

/**
C930 (R914) The label shall be the label of a FORMAT statement that appears in the same scoping
     unit as the statement containing the FMT= specifier.
 */

// R915
input_item
	:	variable
	|	io_implied_do
	;

// R916
output_item
	:	expr
	|	io_implied_do
	;

// R917
io_implied_do
	:	LPAREN
		io_implied_do_object_list
		COMMA
		io_implied_do_control
		RPAREN
	;

// R918
io_implied_do_object
	:	input_item
	|	output_item
	;


/** TODO
R919 io_implied_do_control    : do_variable EQUAL scalar_int_expr COMMA

                                          scalar_int_expr ( COMMA scalar_int_expr )?
*/

/**
C931 (R915) A variable that is an input-item shall not be a whole assumed-size array.
 */

/**
C932 (R915) A variable that is an input-item shall not be a procedure pointer.
 */

/**
C933 (R919) The do-variable shall be a named scalar variable of type integer.
 */

/**
C934 (R918) In an input-item-list, an io-implied-do-object shall be an input-item. In an output-item-
     list, an io-implied-do-object shall be an output-item.
 */

/**
C935 (R916) An expression that is an output-item shall not have a value that is a procedure pointer.
 */

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

/**
C936 (R920) If derived-type-spec specifies an extensible type, the CLASS keyword shall be used;
     otherwise, the TYPE keyword shall be used.
 */

/**
C937 (R920) All length type parameters of derived-type-spec shall be assumed.
 */

// R921
wait_stmt
	:	WAIT
		LPAREN
		wait_spec_list
		RPAREN
	;


/** TODO
R922 wait-spec                : ( UNIT EQUAL )? file-unit-number

                                | END EQUAL LABEL

                                | EOR EQUAL LABEL

                                | ERR EQUAL LABEL

                                | ID EQUAL scalar-int-variable

                                | IOMSG EQUAL iomsg-variable

                                | IOSTAT EQUAL scalar-int-variable
*/

/**
C938 (R922) No specifier shall appear more than once in a given wait-spec-list.
 */

/**
C939 (R922) A file-unit-number shall be specified; if the optional characters UNIT= are omitted, the
     file-unit-number shall be the first item in the wait-spec-list.
 */

/**
C940 (R922) The label in the ERR=, EOR=, or END= specifier shall be the statement label of a
     branch target statement that appears in the same scoping unit as the WAIT statement.
 */

// R923
backspace_stmt
	:	BACKSPACE
		file_unit_number
	|	BACKSPACE
		LPAREN
		position_spec_list
		RPAREN
	;

// R924
endfile_stmt
	:	ENDFILE
		file_unit_number
	|	ENDFILE
		LPAREN
		position_spec_list
		RPAREN
	;

// R925
rewind_stmt
	:	REWIND
		file_unit_number
	|	REWIND
		LPAREN
		position_spec_list
		RPAREN
	;


/** TODO
R926 position-spec            : ( UNIT EQUAL )? file-unit-number

                                | IOMSG EQUAL iomsg-variable

                                | IOSTAT EQUAL scalar-int-variable

                                | ERR EQUAL LABEL
*/

/**
C941 (R926) No specifier shall appear more than once in a given position-spec-list.
 */

/**
C942 (R926) A file-unit-number shall be specified; if the optional characters UNIT= are omitted, the
     file-unit-number shall be the first item in the position-spec-list.
 */

/**
C943 (R926) The label in the ERR= specifier shall be the statement label of a branch target statement
     that appears in the same scoping unit as the file positioning statement.
 */

// R927
flush_stmt
	:	FLUSH
		file_unit_number
	|	FLUSH
		LPAREN
		flush_spec_list
		RPAREN
	;


/** TODO
R928 flush-spec               : ( UNIT EQUAL )? file-unit-number

                                | IOSTAT EQUAL scalar-int-variable

                                | IOMSG EQUAL iomsg-variable

                                | ERR EQUAL LABEL
*/

/**
C944 (R928) No specifier shall appear more than once in a given flush-spec-list.
 */

/**
C945 (R928) A file-unit-number shall be specified; if the optional characters UNIT= are omitted from
     the unit specifier, the file-unit-number shall be the first item in the flush-spec-list.
 */

/**
C946 (R928) The label in the ERR= specifier shall be the statement label of a branch target statement
     that appears in the same scoping unit as the flush statement.
 */

// R929
inquire_stmt
	:	INQUIRE
		LPAREN
		inquire_spec_list
		RPAREN
	|	INQUIRE
		LPAREN
		IOLENGTH
		EQUAL
		scalar_int_variable
		RPAREN
		output_item_list
	;


/** TODO
R930 inquire_spec             : ( UNIT EQUAL )? file_unit_number

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
*/

/**
C947  (R930) No specifier shall appear more than once in a given inquire-spec-list.
 */

/**
C948  (R930) An inquire-spec-list shall contain one FILE= specifier or one UNIT= specifier, but not
      both.
 */

/**
C949  (R930) In the inquire by unit form of the INQUIRE statement, if the optional characters UNIT=
      are omitted, the file-unit-number shall be the first item in the inquire-spec-list.
 */

/**
C950  (R930) If an ID= specifier appears, a PENDING= specifier shall also appear.
 */

/**
Section 10:
 */

// R1001
format_stmt
	:	FORMAT
		format_specification
	;

// R1002
format_specification
	:	LPAREN
		( format_item_list )?
		RPAREN
	;

/**
C1001 (R1001) The format-stmt shall be labeled.
 */

/**
C1002 (R1002) The comma used to separate format-items in a format-item-list may be omitted
 */

// R1003
format_item
	:	( r )?
		data_edit_desc
	|	control_edit_desc
	|	char_string_edit_desc
	|	( r )?
		LPAREN
		format_item_list
		RPAREN
	;

// R1004
r
	:	int_literal_constant
	;

/**
C1003 (R1004) r shall be positive.
 */

/**
C1004 (R1004) r shall not have a kind parameter specified for it.
 */


/** TODO
R1005 data-edit-desc         : I w ( DOT m )?

                               | B w ( DOT m )?

                               | O w ( DOT m )?

                               | Z w ( DOT m )?

                               | F w DOT d

                               | E w DOT d ( E e )?

                               | EN w DOT d ( E e )?

                               | ES w DOT d ( E e )?

                               | G w DOT d ( E e )?

                               | L w

                               | A ( w )?

                               | D w DOT d

                               | DT ( char-literal-constant )? ( LPAREN v-list RPAREN )?
 */

// R1006
w
	:	int_literal_constant
	;

// R1007
m
	:	int_literal_constant
	;

// R1008
d
	:	int_literal_constant
	;

// R1009
e
	:	int_literal_constant
	;

// R1010
v
	:	signed_int_literal_constant
	;

/**
C1005 (R1009) e shall be positive.
 */

/**
C1006 (R1006) w shall be zero or positive for the I, B, O, Z, and F edit descriptors. w shall be positive
      for all other edit descriptors.
 */

/**
C1007 (R1005) w, m, d, e, and v shall not have kind parameters specified for them.
 */

/**
C1008 (R1005) The char-literal-constant in the DT edit descriptor shall not have a kind parameter
      specified for it.
 */

// R1011
control_edit_desc
	:	position_edit_desc
	|	( r )?
		SLASH
	|	COLON
	|	sign_edit_desc
	|	k
		P
	|	blank_interp_edit_desc
	|	round_edit_desc
	|	decimal_edit_desc
	;

// R1012
k
	:	signed_int_literal_constant
	;

/**
C1009 (R1012) k shall not have a kind parameter specified for it.
 */

// R1013
position_edit_desc
	:	T
		n
	|	TL
		n
	|	TR
		n
	|	n
		X
	;

// R1014
n
	:	int_literal_constant
	;

/**
C1010 (R1014) n shall be positive.
 */

/**
C1011 (R1014) n shall not have a kind parameter specified for it.
 */

// R1015
sign_edit_desc
	:	SS
	|	SP
	|	S
	;

// R1016
blank_interp_edit_desc
	:	BN
	|	BZ
	;

// R1017
round_edit_desc
	:	RU
	|	RD
	|	RZ
	|	RN
	|	RC
	|	RP
	;

// R1018
decimal_edit_desc
	:	DC
	|	DP
	;

// R1019
char_string_edit_desc
	:	char_literal_constant
	;

/**
C1012 (R1019) The char-literal-constant shall not have a kind parameter specified for it.
 */

/**
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

/**
C1101 (R1101) In a main-program, the execution-part shall not contain a RETURN statement or an
      ENTRY statement.
 */

/**
C1102 (R1101) The program-name may be included in the end-program-stmt only if the optional
      program-stmt is used and, if included, shall be identical to the program-name specified in the
      program-stmt.
 */

/**
C1103 (R1101) An automatic object shall not appear in the specification-part (R204) of a main program.
 */

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

end-module-stmt
    :   END ( MODULE ( program-name )? )?
    ;

/** TODO
R1106 end-module-stmt        : END ( MODULE ( module-name )? )?
*/

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

/**
C1104 (R1104) If the module-name is specified in the end-module-stmt, it shall be identical to the
      module-name specified in the module-stmt.
 */

/**
C1105 (R1104) A module specification-part shall not contain a stmt-function-stmt, an entry-stmt, or a
      format-stmt.
 */

/**
C1106 (R1104) An automatic object shall not appear in the specification-part of a module.
 */

/**
C1107 (R1104) If an object of a type for which component-initialization is specified (R444) appears
      in the specification-part of a module and does not have the ALLOCATABLE or POINTER
      attribute, the object shall have the SAVE attribute.
 */


// R1109
use_stmt
    :    USE ( ( COMMA module_nature )? COLONCOLON )? module_name ( COMMA rename_list )?
    |    USE ( ( COMMA module_nature )? COLONCOLON )? module_name COMMA ONLY COLON ( only_list )?

// R1110
module_nature
	:	INTRINSIC
	|	NON
		INTRINSIC
	;

// R1111
rename
	:	local_name
		EQUAL_GREATER
		use_name
	|	OPERATOR
		LPAREN
		local_defined_operator
		LPAREN
		EQUAL_GREATER
		OPERATOR
		LPAREN
		use_defined_operator
		LPAREN
	;

// R1112
only
	:	generic_spec
	|	only_use_name
	|	rename
	;

// R1113
only_use_name
	:	use_name
	;

/**
C1108 (R1109) If module-nature is INTRINSIC, module-name shall be the name of an intrinsic module.
 */

/**
C1109 (R1109) If module-nature is NON INTRINSIC, module-name shall be the name of a nonintrinsic
      module.
 */

/**
C1110 (R1109) A scoping unit shall not access an intrinsic module and a nonintrinsic module of the
      same name.
 */

/**
C1111 (R1111) OPERATOR(use-defined-operator) shall not identify a generic-binding.
 */

/**
C1112 (R1112) The generic-spec shall not identify a generic-binding.
 */

/**
C1113 (R1112) Each generic-spec shall be a public entity in the module.
 */

/**
C1114 (R1113) Each use-name shall be the name of a public entity in the module.
 */

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

/**
C1115 (R1115) Each use-defined-operator shall be a public entity in the module.
 */

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


/** TODO
R1118 end-block-data-stmt    : END ( BLOCK DATA ( block-data-name )? )?
*/

/**
C1116 (R1116) The block-data-name shall be included in the end-block-data-stmt only if it was provided
      in the block-data-stmt and, if included, shall be identical to the block-data-name in the block-
      data-stmt.
 */

/**
C1117 (R1116) A block-data specification-part shall contain only derived-type definitions and ASYN-
      CHRONOUS, BIND, COMMON, DATA, DIMENSION, EQUIVALENCE, IMPLICIT, INTRIN-
      SIC, PARAMETER, POINTER, SAVE, TARGET, USE, VOLATILE, and type declaration
      statements.
 */

/**
C1118 (R1116) A type declaration statement in a block-data specification-part shall not contain AL-
      LOCATABLE, EXTERNAL, or BIND attribute specifiers.
 */

/**
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
	:	INTERFACE
		( generic_spec )?
	|	ABSTRACT
		INTERFACE
	;

// R1204
end_interface_stmt
	:	END
		INTERFACE
		( generic_spec )?
	;

// R1205
interface_body
	:	function_stmt
		( specification_part )?
		end_function_stmt
	|	subroutine_stmt
		( specification_part )?
		end_subroutine_stmt
	;

// R1206
procedure_stmt
	:	( MODULE )?
		PROCEDURE
		procedure_name_list
	;

// R1207
generic_spec
	:	generic_name
	|	OPERATOR
		LPAREN
		defined_operator
		RPAREN
	|	ASSIGNMENT
		LPAREN
		EQUAL
		RPAREN
	|	dtio_generic_spec
	;

// R1208
dtio_generic_spec
	:	READ
		LPAREN
		FORMATTED
		RPAREN
	|	READ
		LPAREN
		UNFORMATTED
		RPAREN
	|	WRITE
		LPAREN
		FORMATTED
		RPAREN
	|	WRITE
		LPAREN
		UNFORMATTED
		RPAREN
	;

// R1209
import_stmt
    :    IMPORT ( ( COLONCOLON )? import_name_list )?
    ;

/**
C1201 (R1201) An interface-block in a subprogram shall not contain an interface-body for a procedure
      defined by that subprogram.
 */

/**
C1202 (R1201) The generic-spec shall be included in the end-interface-stmt only if it is provided in the
      interface-stmt. If the end-interface-stmt includes generic-name, the interface-stmt shall specify
      the same generic-name. If the end-interface-stmt includes ASSIGNMENT(=), the interface-
      stmt shall specify ASSIGNMENT(=). If the end-interface-stmt includes dtio-generic-spec,
      the interface-stmt shall specify the same dtio-generic-spec. If the end-interface-stmt includes
      OPERATOR(defined-operator), the interface-stmt shall specify the same defined-operator. If
      one defined-operator is .LT., .LE., .GT., .GE., .EQ., or .NE., the other is permitted to be the
      corresponding operator <, <=, >, >=, ==, or /=.
 */

/**
C1203 (R1203) If the interface-stmt is ABSTRACT INTERFACE, then the function-name in the
      function-stmt or the subroutine-name in the subroutine-stmt shall not be the same as a keyword
      that specifies an intrinsic type.
 */

/**
C1204 (R1202) A procedure-stmt is allowed only in an interface block that has a generic-spec.
 */

/**
C1205 (R1205) An interface-body of a pure procedure shall specify the intents of all dummy arguments
      except pointer, alternate return, and procedure arguments.
 */

/**
C1206 (R1205) An interface-body shall not contain an entry-stmt, data-stmt, format-stmt, or stmt-
      function-stmt.
 */

/**
C1207 (R1206) A procedure-name shall have an explicit interface and shall refer to an accessible pro-
      cedure pointer, external procedure, dummy procedure, or module procedure.
 */

/**
C1208 (R1206) If MODULE appears in a procedure-stmt, each procedure-name in that statement shall
      be accessible in the current scope as a module procedure.
 */

/**
C1209 (R1206) A procedure-name shall not specify a procedure that is specified previously in any
      procedure-stmt in any accessible interface with the same generic identifier.
 */

/**
C1210 (R1209) The IMPORT statement is allowed only in an interface-body.
 */

/**
C1211 (R1209) Each import-name shall be the name of an entity in the host scoping unit.
 */

// R1210
external_stmt
	:	EXTERNAL
		( COLONCOLON )?
		external_name_list
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
	|	INTENT
		LPAREN
		intent_spec
		RPAREN
	|	OPTIONAL
	|	POINTER
	|	SAVE
	;


/** TODO
R1214 proc_decl               : procedure_entity_name ( EQUAL_GREATER null_init )?
*/

// R1215
interface_name
	:	name
	;

/**
C1212 (R1215) The name shall be the name of an abstract interface or of a procedure that has an
      explicit interface. If name is declared by a procedure-declaration-stmt it shall be previously
      declared. If name denotes an intrinsic procedure it shall be one that is listed in 13.6 and not
      marked with a bullet (·).
 */

/**
C1213 (R1215) The name shall not be the same as a keyword that specifies an intrinsic type.
 */

/**
C1214 If a procedure entity has the INTENT attribute or SAVE attribute, it shall also have the
      POINTER attribute.
 */

/**
C1215 (R1211) If a proc-interface describes an elemental procedure, each procedure-entity-name shall
      specify an external procedure.
 */

/**
C1216 (R1214) If => appears in proc-decl, the procedure entity shall have the POINTER attribute.
 */

/**
C1217 (R1211) If proc-language-binding-spec with a NAME= is specified, then proc-decl-list shall con-
      tain exactly one proc-decl, which shall neither have the POINTER attribute nor be a dummy
      procedure.
 */

/**
C1218 (R1211) If proc-language-binding-spec is specified, the proc-interface shall appear, it shall be an
      interface-name, and interface-name shall be declared with a proc-language-binding-spec.
 */

// R1216
intrinsic_stmt
	:	INTRINSIC
		( COLONCOLON )?
		intrinsic_procedure_name_list
	;

/**
C1219 (R1216) Each intrinsic-procedure-name shall be the name of an intrinsic procedure.
 */

// R1217
function_reference
	:	procedure_designator
		LPAREN
		( actual_arg_spec_list )?
		RPAREN
	;

/**
C1220 (R1217) The procedure-designator shall designate a function.
 */

/**
C1221 (R1217) The actual-arg-spec-list shall not contain an alt-return-spec.
 */

// R1218
call_stmt
    :    CALL procedure_designator ( LPAREN ( actual_arg_spec_list )? RPAREN )?
    ;

/**
C1222 (R1218) The procedure-designator shall designate a subroutine.
 */

// R1219
procedure_designator
	:	procedure_name
	|	proc_component_ref
	|	data_ref
		PERCENT
		binding_name
	;

/**
C1223 (R1219) A procedure-name shall be the name of a procedure or procedure pointer.
 */

/**
C1224 (R1219) A binding-name shall be a binding name (4.5.4) of the declared type of data-ref .
 */


/** TODO
R1220 actual_arg_spec            : ( keyword EQUAL )? actual_arg
*/

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
	:	STAR
		LABEL
	;

/**
C1225 (R1220) The keyword = shall not appear if the interface of the procedure is implicit in the
      scoping unit.
 */

/**
C1226 (R1220) The keyword = shall not be omitted from an actual-arg-spec unless it has been omitted
      from each preceding actual-arg-spec in the argument list.
 */

/**
C1227 (R1220) Each keyword shall be the name of a dummy argument in the explicit interface of the
      procedure.
 */

/**
C1228 (R1221) A nonintrinsic elemental procedure shall not be used as an actual argument.
 */

/**
C1229 (R1221) A procedure-name shall be the name of an external procedure, a dummy procedure, a
      module procedure, a procedure pointer, or a specific intrinsic function that is listed in 13.6 and
      not marked with a bullet(·).
 */

/**
C1230 (R1221) In a reference to a pure procedure, a procedure-name actual-arg shall be the name of a
      pure procedure (12.6).
 */

/**
C1231 (R1222) The label used in the alt-return-spec shall be the statement label of a branch target statement that
      appears in the same scoping unit as the call-stmt.
 */

/**
C1232 (R1221) If an actual argument is an array section or an assumed-shape array, and the corre-
      sponding dummy argument has either the VOLATILE or ASYNCHRONOUS attribute, that
      dummy argument shall be an assumed-shape array.
 */

/**
C1233 (R1221) If an actual argument is a pointer array, and the corresponding dummy argument
      has either the VOLATILE or ASYNCHRONOUS attribute, that dummy argument shall be an
      assumed-shape array or a pointer array.
 */

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
	:	( prefix )?
		FUNCTION
		function_name
		LPAREN
		( dummy_arg_name_list )?
		RPAREN
		( suffix )?
	;

/**
C1234 (R1224) If RESULT is specified, result-name shall not be the same as function-name and shall
      not be the same as the entry-name in any ENTRY statement in the subprogram.
 */

/**
C1235 (R1224) If RESULT is specified, the function-name shall not appear in any specification state-
      ment in the scoping unit of the function subprogram.
 */

// R1225
proc_language_binding_spec
	:	language_binding_spec
	;

/**
C1236 (R1225) A proc-language-binding-spec with a NAME= specifier shall not be specified in the
      function-stmt or subroutine-stmt of an interface body for an abstract interface or a dummy
      procedure.
 */

/**
C1237 (R1225) A proc-language-binding-spec shall not be specified for an internal procedure.
 */

/**
C1238 (R1225) If proc-language-binding-spec is specified for a procedure, each of the procedure's dummy
      arguments shall be a nonoptional interoperable variable (15.2.4, 15.2.5) or an interoperable
      procedure (15.2.6). If proc-language-binding-spec is specified for a function, the function result
      shall be an interoperable variable.
 */

// R1226
dummy_arg_name
	:	name
	;

/**
C1239 (R1226) A dummy-arg-name shall be the name of a dummy argument.
 */

// R1227
prefix
	:	prefix_spec
		( prefix_spec )*
	;

// R1228
prefix_spec
	:	declaration_type_spec
	|	RECURSIVE
	|	PURE
	|	ELEMENTAL
	;

/**
C1240 (R1227) A prefix shall contain at most one of each prefix-spec.
 */

/**
C1241 (R1227) A prefix shall not specify both ELEMENTAL and RECURSIVE.
 */

/**
C1242 (R1227) A prefix shall not specify ELEMENTAL if proc-language-binding-spec appears in the
      function-stmt or subroutine-stmt.
 */

// R1229
suffix
    :    proc-language-binding-spec ( RESULT LPAREN result-name RPAREN )?
    | RESULT LPAREN result-name RPAREN ( proc-language-binding-spec )?
    ;

// R1230
end_function_stmt
    : END ( FUNCTION ( function-name )? )?
    ;

/**
C1243 (R1230) FUNCTION shall appear in the end-function-stmt of an internal or module function.
 */

/**
C1244 (R1223) An internal function subprogram shall not contain an ENTRY statement.
 */

/**
C1245 (R1223) An internal function subprogram shall not contain an internal-subprogram-part.
 */

/**
C1246 (R1230) If a function-name appears in the end-function-stmt, it shall be identical to the function-
      name specified in the function-stmt.
 */

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

/**
C1247 (R1232) The prefix of a subroutine-stmt shall not contain a declaration-type-spec.
 */

// R1233
dummy_arg
	:	dummy_arg_name
	|	STAR
	;

// R1234
end_subroutine_stmt
    :    END ( SUBROUTINE ( subroutine_name )? )?
    ;

/**
C1248 (R1234) SUBROUTINE shall appear in the end-subroutine-stmt of an internal or module sub-
      routine.
 */

/**
C1249 (R1231) An internal subroutine subprogram shall not contain an ENTRY statement.
 */

/**
C1250 (R1231) An internal subroutine subprogram shall not contain an internal-subprogram-part.
 */

/**
C1251 (R1234) If a subroutine-name appears in the end-subroutine-stmt, it shall be identical to the
      subroutine-name specified in the subroutine-stmt.
 */

// R1235
entry_stmt
    :    ENTRY entry_name ( LPAREN ( dummy_arg_list )? RPAREN ( suffix )? )?
    ;

/**
C1252 (R1235) If RESULT is specified, the entry-name shall not appear in any specification or type-
      declaration statement in the scoping unit of the function program.
 */

/**
C1253 (R1235) An entry-stmt shall appear only in an external-subprogram or module-subprogram. An
      entry-stmt shall not appear within an executable-construct.
 */

/**
C1254 (R1235) RESULT shall appear only if the entry-stmt is in a function subprogram.
 */

/**
C1255 (R1235) Within the subprogram containing the entry-stmt, the entry-name shall not appear
      as a dummy argument in the FUNCTION or SUBROUTINE statement or in another ENTRY
      statement nor shall it appear in an EXTERNAL, INTRINSIC, or PROCEDURE statement.
 */

/**
C1256 (R1235) A dummy-arg shall not be an alternate return indicator if the ENTRY statement is in a function
      subprogram.
 */

/**
C1257 (R1235) If RESULT is specified, result-name shall not be the same as the function-name in the
      FUNCTION statement and shall not be the same as the entry-name in any ENTRY statement
      in the subprogram.
 */

// R1236
return_stmt
	:	RETURN
		( scalar_int_expr )?
	;

/**
C1258 (R1236) The return-stmt shall be in the scoping unit of a function or subroutine subprogram.
 */

/**
C1259 (R1236) The scalar-int-expr is allowed only in the scoping unit of a subroutine subprogram.
 */

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

/**
C1260 (R1238) The primaries of the scalar-expr shall be constants (literal and named), references to variables, references
      to functions and function dummy procedures, and intrinsic operations. If scalar-expr contains a reference to a
      function or a function dummy procedure, the reference shall not require an explicit interface, the function shall
      not require an explicit interface unless it is an intrinsic, the function shall not be a transformational intrinsic,
      and the result shall be scalar. If an argument to a function or a function dummy procedure is an array, it shall
      be an array name. If a reference to a statement function appears in scalar-expr, its definition shall have been
      provided earlier in the scoping unit and shall not be the name of the statement function being defined.
 */

/**
C1261 (R1238) Named constants in scalar-expr shall have been declared earlier in the scoping unit or made accessible
      by use or host association. If array elements appear in scalar-expr, the array shall have been declared as an array
      earlier in the scoping unit or made accessible by use or host association.
 */

/**
C1262 (R1238) If a dummy-arg-name, variable, function reference, or dummy function reference is typed by the implicit
      typing rules, its appearance in any subsequent type declaration statement shall confirm this implied type and
      the values of any implied type parameters.
 */

/**
C1263 (R1238) The function-name and each dummy-arg-name shall be specified, explicitly or implicitly, to be scalar.
 */

/**
C1264 (R1238) A given dummy-arg-name shall not appear more than once in any dummy-arg-name-list.
 */

/**
C1265 (R1238) Each variable reference in scalar-expr may be either a reference to a dummy argument of the statement
      function or a reference to a variable accessible in the same scoping unit as the statement function statement.
 */

/**
C1266 The specification-part of a pure function subprogram shall specify that all its nonpointer dummy
      data objects have INTENT(IN).
 */

/**
C1267 The specification-part of a pure subroutine subprogram shall specify the intents of all its non-
      pointer dummy data objects.
 */

/**
C1268 A local variable declared in the specification-part or internal-subprogram-part of a pure subpro-
      gram shall not have the SAVE attribute.
 */

/**
C1269 The specification-part of a pure subprogram shall specify that all its dummy procedures are
      pure.
 */

/**
C1270 If a procedure that is neither an intrinsic procedure nor a statement function is used in a context
      that requires it to be pure, then its interface shall be explicit in the scope of that use. The
      interface shall specify that the procedure is pure.
 */

/**
C1271 All internal subprograms in a pure subprogram shall be pure.
 */

/**
C1272 In a pure subprogram any designator with a base object that is in common or accessed by
      host or use association, is a dummy argument of a pure function, is a dummy argument with
      INTENT (IN) of a pure subroutine, or an object that is storage associated with any such variable,
      shall not be used in the following contexts:
 */

/**
C1273 Any procedure referenced in a pure subprogram, including one referenced via a defined operation,
      assignment, or finalization, shall be pure.
 */

/**
C1274 A pure subprogram shall not contain a print-stmt, open-stmt, close-stmt, backspace-stmt, endfile-
      stmt, rewind-stmt, flush-stmt, wait-stmt, or inquire-stmt.
 */

/**
C1275 A pure subprogram shall not contain a read-stmt or write-stmt whose io-unit is a file-unit-number
      or *.
 */

/**
C1276 A pure subprogram shall not contain a stop-stmt.
 */

/**
C1277 All dummy arguments of an elemental procedure shall be scalar dummy data objects and shall
      not have the POINTER or ALLOCATABLE attribute.
 */

/**
C1278 The result variable of an elemental function shall be scalar and shall not have the POINTER or
      ALLOCATABLE attribute.
 */

/**
C1279 In the scoping unit of an elemental subprogram, an object designator with a dummy argument
      as the base object shall not appear in a specification-expr except as the argument to one of the
      intrinsic functions BIT SIZE, KIND, LEN, or the numeric inquiry functions (13.5.6).
 */

/**
Section 13:
 */

/**
Section 14:
 */

/**
Section 15:
 */

/**
C1501 (R429) A derived type with the BIND attribute shall not be a SEQUENCE type.
 */

/**
C1502 (R429) A derived type with the BIND attribute shall not have type parameters.
 */

/**
C1503 (R429) A derived type with the BIND attribute shall not have the EXTENDS attribute.
 */

/**
C1504 (R429) A derived type with the BIND attribute shall not have a type-bound-procedure-part.
 */

/**
C1505 (R429) Each component of a derived type with the BIND attribute shall be a nonpointer,
      nonallocatable data component with interoperable type and type parameters.
 */

/**
Section 16:
 */


/**
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
