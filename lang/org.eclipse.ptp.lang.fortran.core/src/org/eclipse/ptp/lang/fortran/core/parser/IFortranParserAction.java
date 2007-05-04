/*******************************************************************************
 * Copyright (c) 2005, 2006 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.ptp.lang.fortran.core.parser;

import org.antlr.runtime.Token;

/**
 * TODO - change/add from C to Fortran grammar in comments and actions
 * The goal is to do this slowly, one action at a time
 */
public abstract interface IFortranParserAction {

	public enum LiteralConstant {
		int_literal_constant,
		real_literal_constant,
		complex_literal_constant,
		logical_literal_constant,
		char_literal_constant,
		boz_literal_constant
	}
	
	public enum KindParam {
		none,
		literal,
		id
	}
	
	public enum KindSelector {
		none,
		expression
	}
	
	public enum KindLenParam {
		none,
		kind,
		len
	}
	
	public enum IntrinsicTypeSpec {
		INTEGER,
		REAL,
		DOUBLEPRECISION,
		DOUBLECOMPLEX,
		COMPLEX,
		CHARACTER,
		LOGICAL
	}
	
	public enum IntentSpec {
		IN,
		OUT,
		INOUT
	}
	
	public enum AttrSpec {
		none,
		PUBLIC,
		PRIVATE,
		ALLOCATABLE,
		ASYNCHRONOUS,
		DIMENSION,
		EXTERNAL,
		INTENT,
		INTRINSIC,
		BINDC,
		OPTIONAL,
		PARAMETER,
		POINTER,
		PROTECTED,
		SAVE,
		TARGET,
		VALUE,
		VOLATILE,
		// binding-attr
		PASS,
		NOPASS,
		NON_OVERRIDABLE,
		DEFERRED
	}

	/** R102 list
	 * generic_name (xyz-name)
	 * generic_name_list (xyz-list R101)
	 * 	:	T_IDENT ( T_COMMA T_IDENT )*
	 * 
	 * @param count The number of items in the list.
	 * @param ident The name of the item placed in the list.
	 */
	public abstract void generic_name_list__begin();
	public abstract void generic_name_list(int count);
	public void generic_name_list_part(Token ident);

	/** R210
	 * internal_subprogram_part
	 *	:	T_CONTAINS T_EOS internal_subprogram (internal_subprogram)*
	 *
	 * T_CONTAINS inlined for contains_stmt
	 *
	 * @param count The number of internal subprograms
	 */
	public abstract void internal_subprogram_part(int count);

	/** R305
	 * constant
	 *	:	literal_constant
	 *	|	T_IDENT
	 * 
	 * ERR_CHK 305 named_constant replaced by T_IDENT
	 * 
	 * @param id The identifier representing the named constant if present, otherwise is a literal-constant
	 */
	public abstract void constant(Token id);

	/** R308
	 * int_constant
	 *	:	int_literal_constant
	 *	|	T_IDENT
	 * 
	 * ERR_CHK 308 named_constant replaced by T_IDENT
	 * C302 R308 int_constant shall be of type integer
	 * inlined integer portion of constant
	 * 
	 * @param id The identifier representing the named constant if present, otherwise is a literal-constant
	 */
	public abstract void int_constant(Token id);

	/** R309
	 * 	char_constant
	 *	:	char_literal_constant
	 *	|	T_IDENT
	 * 
	 * ERR_CHK 309 named_constant replaced by T_IDENT
	 * C303 R309 char_constant shall be of type character
	 * inlined character portion of constant
	 * 
	 * @param id The identifier representing the named constant if present, otherwise is a literal-constant
	 */
	public abstract void char_constant(Token id);

	/** R311
	 * defined_operator
	 *	:	T_DEFINED_OP
	 *	|	extended_intrinsic_op
	 *
	 * removed defined_unary_op or defined_binary_op ambiguity with T_DEFINED_OP
	 * 
	 * @param definedOp The defined operator token if present, otherwise is an extended-intrinsic-op
	 */
	public abstract void defined_operator(Token definedOp);

	/** R313
	 * label	:	T_DIGIT_STRING
	 *
	 * @param lbl The token containing the label
     */
	public abstract void label(Token lbl);

	/** R313 list
	 * label	:	T_DIGIT_STRING
	 * label_list
	 *	:	label ( T_COMMA label )*
	 *
	 * // ERR_CHK 313 five characters or less
	 *
	 * @param count The number of items in the list.
     */
	public abstract void label_list__begin();
	public abstract void label_list(int count);

	/** R402
	 * type-param-value
	 *	: expr | T_ASTERISK | T_COLON
	 *
	 * @param hasExpr True if an expr is present
	 * @param hasAsterisk True if an '*' is present
	 * @param hasColon True if a ':' is present
	 */
	public abstract void type_param_value(boolean hasExpr, boolean hasAsterisk, boolean hasColon);

	/** R403
	 * intrinsic_type_spec
	 * :	T_INTEGER ( kind_selector )?
	 * |	T_REAL ( kind_selector )?
	 * |	T_DOUBLE T_PRECISION
	 * |	T_DOUBLEPRECISION
	 * |	T_COMPLEX ( kind_selector )?
	 * |	T_CHARACTER ( char_selector )?
	 * |	T_LOGICAL ( kind_selector )?
	 * 
	 * @param type The type specified (i.e., INTEGER)
	 * @param hasKindSelector True if a kind_selector (scalar_int_initialization_expr) is present
	 */
	public abstract void intrinsic_type_spec(IntrinsicTypeSpec type, boolean hasKindSelector);

	/** R405
	 * signed_int_literal_constant
	 *  : 	(T_PLUS|T_MINUS)? int_literal_constant
	 *
	 * @param sign The sign: positive, negative, or null.
	 */
	 public abstract void signed_int_literal_constant(Token sign);

	/** R406
	 * int_literal_constant
	 *	:	T_DIGIT_STRING (T_UNDERSCORE kind_param)?
	 *
	 * @param digitString The digit string representing the constant
	 * @param kindParam The kind parameter
	 */
	 public abstract void int_literal_constant(Token digitString, Token kindParam);

	/** R416
	 * signed_real_literal_constant
	 *  : 	(T_PLUS|T_MINUS)? real_literal_constant
	 *
	 * @param sign The sign: positive, negative, or null.
	 */
	 public abstract void signed_real_literal_constant(Token sign);

	/** R417
	 * real_literal_constant
	 *	:   REAL_CONSTANT ( T_UNDERSCORE kind_param )?
	 * 	|   DOUBLE_CONSTANT ( T_UNDERSCORE kind_param )?
	 *
	 * Replaced by
	 *	:	T_DIGIT_STRING T_PERIOD_EXPONENT (T_UNDERSCORE kind_param)?
	 *	|	T_DIGIT_STRING T_PERIOD (T_UNDERSCORE kind_param)?
	 *	|	T_PERIOD_EXPONENT (T_UNDERSCORE kind_param)?
	 *
	 * @param digits The integral part
	 * @param fractionExp The fractional part and exponent
	 * @param kindParam The kind parameter
	 */
	 public abstract void real_literal_constant(Token digits, Token fractionExp, Token kindParam);

	/** R422
	 * real_part
	 * 
	 * ERR_CHK 422 named_constant replaced by T_IDENT
	 * 
	 * @param hasIntConstant True if signed-int-literal-constant is present
	 * @param hasRealConstant True if signed-real-literal-constant is present
	 * @param id The named-constant (optional)
	 */
	public abstract void real_part(boolean hasIntConstant, boolean hasRealConstant, Token id);

	/** R423
	 * imag_part
	 * 
	 * ERR_CHK 423 named_constant replaced by T_IDENT
	 * 
	 * @param hasIntConstant True if signed-int-literal-constant is present
	 * @param hasRealConstant True if signed-real-literal-constant is present
	 * @param id The named-constant (optional)
	 */
	public abstract void imag_part(boolean hasIntConstant, boolean hasRealConstant, Token id);

	/** R424
	 * char-selector
	 *	:	T_ASTERISK char_length (T_COMMA)?
	 *	|	T_LPAREN (T_KIND | T_LEN) T_EQUALS type_param_value
	 *             ( T_COMMA (T_KIND | T_LEN) T_EQUALS type_param_value )? T_RPAREN
	 *	|	T_LPAREN type_param_value ( T_COMMA (T_KIND T_EQUALS)? expr )? T_RPAREN
	 *
	 * @param kindOrLen1 Specifies whether the first kind or len type-param-value is present
	 * @param kindOrLen2 Specifies whether the second kind or len type-param-value is present
	 * @param hasAsterisk True if a '*' char-selector is specified
	 */
	public abstract void char_selector(KindLenParam kindOrLen1, KindLenParam kindOrLen2, boolean hasAsterisk);

	/** R425
	 * length-selector
	 *	:	T_LPAREN ( T_LEN T_EQUALS )? type_param_value T_RPAREN
	 *	|	T_ASTERISK char_length (T_COMMA)?
	 *
	 * @param kindOrLen Specifies whether a kind or len type-param-value is present
	 * @param hasAsterisk True if a '*' char-selector is specified
	 */
	public abstract void length_selector(KindLenParam kindOrLen, boolean hasAsterisk);

	/** R426
	 * char_length
	 *	:	T_LPAREN type_param_value T_RPAREN
	 *	|	scalar_int_literal_constant
	 *
	 * @param hasTypeParamValue True if a type-param-value is specified, otherwise is a scalar-int-literal-constant
	 */
	public abstract void char_length(boolean hasTypeParamValue);

	/** R427
	 * char_literal_constant
     * 	:	T_DIGIT_STRING T_UNDERSCORE T_CHAR_CONSTANT
	 *       // removed the T_UNDERSCORE because underscores are valid characters 
	 *       // for identifiers, which means the lexer would match the T_IDENT and 
	 *       // T_UNDERSCORE as one token (T_IDENT).
	 *	 	|	T_IDENT T_CHAR_CONSTANT
	 *	 	|	T_CHAR_CONSTANT
	 * 
	 * @param digitString Optional digit-string representing the kind parameter
	 * @param id Optional identifier representing the kind parameter variable AND the '_'
	 * @param str The token containing the literal character constant
	 */
	public abstract void char_literal_constant(Token digitString, Token id, Token str);

	/** R428
	 * logical_literal_constant
	 *	: T_TRUE | T_FALSE
	 *
	 * @param isTrue True if logical constant is true, false otherwise
	 * @param kindParam The kind parameter
	 */
	public abstract void logical_literal_constant(boolean isTrue, Token kindParam);
	
	/** R431 list
	 * type_attr_spec
	 * type_attr_spec_list
	 * 	:	type_attr_spec ( T_COMMA type_attr_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void type_attr_spec_list__begin();
	public abstract void type_attr_spec_list(int count);

	/** R436 list
	 * type_param_decl
	 * type_param_decl_list
	 *	:	type_param_decl ( T_COMMA type_param_decl )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void type_param_decl_list__begin();
	public abstract void type_param_decl_list(int count);

	/** R441 list
	 * component_attr_spec
	 * component_attr_spec_list
	 * 	:	component_attr_spec ( T_COMMA component_attr_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void component_attr_spec_list__begin();
	public abstract void component_attr_spec_list(int count);

	/** R442 list
	 * component_decl
	 * component_decl_list
	 *    :    component_decl ( T_COMMA component_decl )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void component_decl_list__begin();
	public abstract void component_decl_list(int count);

	/** R443 list
	 * deferred_shape_spec_list
	 * 		T_COLON {count++;} ( T_COMMA T_COLON {count++;} )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void deferred_shape_spec_list__begin();
	public abstract void deferred_shape_spec_list(int count);

	/** R446 list
	 * proc_component_attr_spec_list
	 * 		proc_component_attr_spec ( T_COMMA proc_component_attr_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void proc_component_attr_spec_list__begin();
	public abstract void proc_component_attr_spec_list(int count);

	/** R453
	 * binding_attr
	 *	: T_PASS ( T_LPAREN T_IDENT T_RPAREN )?
	 *	| T_NOPASS
	 *	| T_NON_OVERRIDABLE
	 *	| T_DEFERRED
	 *	| access_spec
	 *
	 * @param attr The binding attribute.
	 */
	public abstract void binding_attr(AttrSpec attr);

	/** R453 list
	 * binding_attr_list
	 * 		binding_attr ( T_COMMA binding_attr )*
     *
	 * @param count The number of items in the list.
	 */
	public abstract void binding_attr_list__begin();
	public abstract void binding_attr_list(int count);

	/** R456 list
	 * type_param_spec_list
	 * 		type_param_spec ( T_COMMA type_param_spec )*
     *
	 * @param count The number of items in the list.
	 */
	public abstract void type_param_spec_list__begin();
	public abstract void type_param_spec_list(int count);

	/** R458 list
	 * component_spec_list
	 * 		component_spec ( T_COMMA component_spec )*
     *
	 * @param count The number of items in the list.
	 */
	public abstract void component_spec_list__begin();
	public abstract void component_spec_list(int count);

	/** R463 list
	 * enumerator_list
	 * 		enumerator ( T_COMMA enumerator )*
     *
	 * @param count The number of items in the list.
	 */
	public abstract void enumerator_list__begin();
	public abstract void enumerator_list(int count);

	/** R465
	 * array_constructor
	 *	:	T_LPAREN T_SLASH ac_spec T_SLASH T_RPAREN
	 *	|	T_LBRACKET ac_spec T_RBRACKET
	 */
	public abstract void array_constructor();

	/** R469 list
	 * ac_value_list
	 * 		ac_value ( T_COMMA ac_value )*
     *
	 * @param count The number of items in the list.
	 */
	public abstract void ac_value_list__begin();
	public abstract void ac_value_list(int count);

	/** R501
	 * type_declaration_stmt
	 * :    (label)? declaration_type_spec ( (T_COMMA attr_spec)* T_COLON_COLON )?
	 *      entity_decl_list T_EOS
	 *
	 * @param label Optional statement label
	 * @param numAttributes The number of attributes present
	 */
	public abstract void type_declaration_stmt__begin();
	public abstract void type_declaration_stmt(Token label, int numAttributes);

	/** R503
	 * attr_spec
	 *	:	access_spec
	 *	|	T_ALLOCATABLE
	 *	|	T_ASYNCHRONOUS
	 *	|	T_DIMENSION T_LPAREN array_spec T_RPAREN
	 *	|	T_EXTERNAL
	 *	|	T_INTENT T_LPAREN intent_spec T_RPAREN
	 *	|	T_INTRINSIC
	 *	|	language_binding_spec		
	 *	|	T_OPTIONAL
	 *	|	T_PARAMETER
	 *	|	T_POINTER
	 *	|	T_PROTECTED
	 *	|	T_SAVE
	 *	|	T_TARGET
	 *	|	T_VALUE
	 *	|	T_VOLATILE
	 *
	 * @param attr The attribute specification
	 */
	public abstract void attr_spec(AttrSpec attr);

	/** R504, R503-F2008
	 * entity_decl
	 *  : T_IDENT ( T_LPAREN array_spec T_RPAREN )?
	 *            ( T_LBRACKET co_array_spec T_RBRACKET )?
	 *            ( T_ASTERISK char_length )? ( initialization )? 
	 */
	public abstract void entity_decl(Token id);

	/** R504 list
	 * entity_decl
	 * entity_decl_list
	 * 	:    entity_decl ( T_COMMA entity_decl )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void entity_decl_list__begin();
	public abstract void entity_decl_list(int count);
	
	/** R506
	 * initialization
	 * 	:	T_EQUALS expr
	 *	|	T_EQ_GT null_init
	 *
	 * ERR_CHK 506 initialization_expr replaced by expr
	 *
	 * @param hasExpr True if expr is present
	 * @param hasNullInit True if null-init is present
	 */
	public abstract void initialization(boolean hasExpr, boolean hasNullInit);

	/** R507
	 * null_init
	 * 	:	T_IDENT //'NULL'// T_LPAREN T_RPAREN
	 *
	 * C506 The function-reference shall be a reference to the NULL intrinsic function with no arguments.
	 *
	 * @param id The function-reference
	 */
	public abstract void null_init(Token id);

	/** R511
	 * explicit_shape_spec
 	 * expr ( T_COLON expr )?
	 * 
	 * @param hasUpperBound Whether the shape spec is of the form x:y.
	 */
	public abstract void explicit_shape_spec(boolean hasUpperBound);

	/** R511 list
	 * explicit_shape_spec_list
	 *	:	explicit_shape_spec ( T_COMMA explicit_shape_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void explicit_shape_spec_list__begin();
	public abstract void explicit_shape_spec_list(int count);

	/** R517
	 * intent_spec
	 *	:	T_IN | T_OUT | T_IN T_OUT | T_INOUT
	 *
	 * @param intent The type of intent-spec.
	 */
	public abstract void intent_spec(IntentSpec intent);

		/** R519-08 list
	 * deferred_co_shape_spec_list
	 *	:	T_COLON ( T_COMMA T_COLON )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void deferred_co_shape_spec_list__begin();
	public abstract void deferred_co_shape_spec_list(int count);

	/** R519 list
	 * access_id_list
	 * 	:    access_id ( T_COMMA access_id )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void access_id_list__begin();
	public abstract void access_id_list(int count);

	/** R523 list
	 * bind_entity_list
	 * 	:    bind_entity ( T_COMMA bind_entity )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void bind_entity_list__begin();
	public abstract void bind_entity_list(int count);

	/** R526 list
	 * data_stmt_object_list
	 * 	:    data_stmt_object ( T_COMMA data_stmt_object )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void data_stmt_object_list__begin();
	public abstract void data_stmt_object_list(int count);

	/** R528 list
	 * data_i_do_object_list
	 * 	:    data_i_do_object ( T_COMMA data_i_do_object )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void data_i_do_object_list__begin();
	public abstract void data_i_do_object_list(int count);

	/** R530 list
	 * data_stmt_value_list
	 * 	:    data_stmt_value ( T_COMMA data_stmt_value )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void data_stmt_value_list__begin();
	public abstract void data_stmt_value_list(int count);

	/** R539 list
	 * named_constant_def_list
	 * 	:    named_constant_def ( T_COMMA named_constant_def )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void named_constant_def_list__begin();
	public abstract void named_constant_def_list(int count);

	/** R541 list
	 * pointer_decl_list
	 * 	:    pointer_decl ( T_COMMA pointer_decl )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void pointer_decl_list__begin();
	public abstract void pointer_decl_list(int count);

	/** R544 list
	 * saved_entity_list
	 * 	:    saved_entity ( T_COMMA saved_entity )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void saved_entity_list__begin();
	public abstract void saved_entity_list(int count);

	/** R550 list
	 * implicit_spec_list
	 * 	:    implicit_spec ( T_COMMA implicit_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void implicit_spec_list__begin();
	public abstract void implicit_spec_list(int count);

	/** R551 list
	 * letter_spec_list
	 * 	:    letter_spec ( T_COMMA letter_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void letter_spec_list__begin();
	public abstract void letter_spec_list(int count);

	/** R553 list
	 * namelist_group_object_list
	 * 	:    T_IDENT ( T_COMMA T_IDENT )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void namelist_group_object_list__begin();
	public abstract void namelist_group_object_list(int count);

	/** R555 list
	 * equivalence_set_list
	 * 	:    equivalence_set ( T_COMMA equivalence_set )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void equivalence_set_list__begin();
	public abstract void equivalence_set_list(int count);

	/** R556 list
	 * equivalence_object_list
	 * 	:    equivalence_object ( T_COMMA equivalence_object )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void equivalence_object_list__begin();
	public abstract void equivalence_object_list(int count);

	/** R558 list
	 * common_block_object_list
	 * 	:    common_block_object ( T_COMMA common_block_object )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void common_block_object_list__begin();
	public abstract void common_block_object_list(int count);

	/** R601
	 * variable
	 * :	designator
	 */
	public abstract void variable();

	// R602 variable_name was name inlined as T_IDENT

	/** R603
	 * designator
	 *  :   object-name             // T_IDENT (data-ref isa T_IDENT)
	 *	|	array-element           // R616 is data-ref
	 *	|	array-section           // R617 is data-ref [ (substring-range) ] 
	 *	|	structure-component     // R614 is data-ref
	 *	|	substring
	 *
	 *@param hasSubstringRange True if substring-range is present.
	 */
	public abstract void designator(boolean hasSubstringRange);

	/** R609
	 * substring
	 *	:	data_ref (T_LPAREN substring_range T_RPAREN)?
	 *	|	char_literal_constant T_LPAREN substring_range T_RPAREN
	 *
	 * C608 (R610) parent_string shall be of type character
	 * fix for ambiguity in data_ref allows it to match T_LPAREN substring_range T_RPAREN,
	 * so required T_LPAREN substring_range T_RPAREN made optional
	 * ERR_CHK 609 ensure final () is (substring-range)
	 * 
	 * @param hasSubstringRange True if substring-range is present, otherwise it must be extracted from
	 * the data-ref.
	 */
	public abstract void substring(boolean hasSubstringRange);
	 
	/** R611
	 * substring_range
	 *	:	(expr)? T_COLON	(expr)?
	 *
	 * ERR_CHK 611 scalar_int_expr replaced by expr
	 *
	 * @param hasLowerBound True if lower bound is present in a substring-range (lower_bound:upper_bound).
	 * @param hasUpperBound True if upper bound is present in a substring-range (lower_bound:upper_bound).
	 */
	public abstract void substring_range(boolean hasLowerBound, boolean hasUpperBound);

	/** R612
	 *	data_ref
	 *	:	part_ref (T_PERCENT part_ref)*
	 *
	 * @param numPartRef The number of optional part_refs
	 */
	public abstract void data_ref(int numPartRef);

	/** R613, R613-F2008
	 * part_ref
	 *  :	T_IDENT T_LPAREN section_subscript_list T_RPAREN (image_selector)? 
	 *	|	T_IDENT image_selector
	 *	|	T_IDENT
	 *
	 * @param id The identifier (variable name in most cases (all?))
	 * @param hasSelectionSubscriptList True if a selection-subscript-list is present
	 * @param hasImageSelector Ture if an image-selector is present
	 */
	public abstract void part_ref(Token id, boolean hasSelectionSubscriptList, boolean hasImageSelector);

	/** R619  (see R1220, actual_arg_spec)
	 * section_subscript/actual_arg_spec
	 *	:	expr section_subscript_suffix
	 *	|	T_COLON (expr)? (T_COLON expr)?
	 *	|	T_COLON_COLON expr
	 *	|	T_IDENT T_EQUALS (expr | T_ASTERISK label ) // could be an actual-arg-spec, see R1220
	 *	|	T_ASTERISK label // could be an actual-arg-spec, see R1220 
	 *	|	{ // empty could be an actual-arg, see R1220 // }
	 *
	 * R619, section_subscript has been combined with actual_arg_spec (R1220) 
	 * to reduce backtracking.  Only the first alternative is truly ambiguous.
	 * 
	 * @param hasLowerBound True if lower bound is present in a section-subscript (lower_bound:upper_bound:stride).
	 * @param hasUpperBound True if upper bound is present in a section-subscript (lower_bound:upper_bound:stride).
	 * @param hasStride True if stride is present in a section-subscript (lower_bound:upper_bound:stride).
	 * @param isAmbiguous True if the third alternative is taken
	 */
	public abstract void section_subscript(boolean hasLowerBound, boolean hasUpperBound, boolean hasStride, boolean isAmbiguous);

	/** R619 list
	 * section_subscript
	 * section_subscript_list
	 * 	:    section_subscript ( T_COMMA section_subscript )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void section_subscript_list__begin();
	public abstract void section_subscript_list(int count);

	/** R623
	 * allocate_stmt
	 *	:	(label)? T_ALLOCATE_STMT_1 T_ALLOCATE T_LPAREN type_spec T_COLON_COLON
	 *		allocation_list (T_COMMA alloc_opt_list)? T_RPAREN T_EOS
	 *	|	(label)? T_ALLOCATE T_LPAREN
	 *		allocation_list (T_COMMA alloc_opt_list)? T_RPAREN T_EOS
	 *
	 * @param label Optional statement label
	 * @param hasTypeSpec True if type-spec is present
	 * @param hasAllocOptList True if alloc-opt-list is present
	 */
	public abstract void allocate_stmt(Token label, boolean hasTypeSpec, boolean hasAllocOptList);

	/** R624
	 * alloc_opt
	 *	:	T_IDENT			// {'STAT','ERRMSG'} are variables {SOURCE'} is expr
	 *		T_EQUALS expr
	 *
	 * @param allocOpt Identifier representing {'STAT','ERRMSG','SOURCE'}
	 */
	public abstract void alloc_opt(Token allocOpt);
	
	/** R624 list
	 * alloc_opt_list
	 * 	:    alloc_opt ( T_COMMA alloc_opt )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void alloc_opt_list__begin();
	public abstract void alloc_opt_list(int count);

	/** R628, R631-F2008
	 * allocation
	 *    	( T_LPAREN allocate_shape_spec_list {hasAllocateShapeSpecList=true;} T_RPAREN )?
	 *		( T_LBRACKET allocate_co_array_spec {hasAllocateCoArraySpec=true;} T_RBRACKET )?
	 *
	 * NOTE: In current parser, hasAllocateShapeSpecList is always false, appears as
	 * R619 section-subscript-list.  In a section-subscript, the stride shall not be present
	 * and if hasUpperBound is false, hasLowerBound shall be present and must be interpreted
	 * as an upper-bound-expr.
	 * 
	 * @param hasAllocateShapeSpecList True if allocate-shape-spec-list is present.
	 * @param hasAllocateCoArraySpec True if allocate-co-array-spec is present.
	 */
	public abstract void allocation(boolean hasAllocateShapeSpecList, boolean hasAllocateCoArraySpec);

	/** R628 list
	 * allocation_list
	 * 	:    allocation ( T_COMMA allocation )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void allocation_list__begin();
	public abstract void allocation_list(int count);

	/** R629 list
	 * allocate_object_list
	 * 	:	allocate_object ( T_COMMA allocate_object )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void allocate_object_list__begin();
	public abstract void allocate_object_list(int count);

	/** R630
	 * allocate_shape_spec
	 *	:	expr (T_COLON expr)?
	 *
	 * NOTE: not called by current parser, appears as R619 section-subscript instead
	 *
	 * @param hasLowerBound True if optional lower-bound-expr is present.
	 * @param hasUpperBound True if upper-bound-expr is present (note always true).
	 */
	public abstract void allocate_shape_spec(boolean hasLowerBound, boolean hasUpperBound);

	/** R630 list
	 * allocate_shape_spec_list
	 * 	:    allocate_shape_spec ( T_COMMA allocate_shape_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void allocate_shape_spec_list__begin();
	public abstract void allocate_shape_spec_list(int count);

	/** R633
	 *	nullify_stmt
	 *	:	(label)? T_NULLIFY T_LPAREN pointer_object_list T_RPAREN T_EOS
	 *
	 * @param label Optional statement label
	 */
	public abstract void nullify_stmt(Token label);
	
	/** R634 list
	 * pointer_object_list
	 * 	:    pointer_object ( T_COMMA pointer_object )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void pointer_object_list__begin();
	public abstract void pointer_object_list(int count);

	/** R636 list
	 * dealloc_opt_list
	 * 	:    dealloc_opt ( T_COMMA dealloc_opt )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void dealloc_opt_list__begin();
	public abstract void dealloc_opt_list(int count);

	/** R637-F2008 list
	 * allocate_co_shape_spec_list
	 * 	:    allocate_co_shape_spec ( T_COMMA allocate_co_shape_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void allocate_co_shape_spec_list__begin();
	public abstract void allocate_co_shape_spec_list(int count);

	/** R701
	 * primary
	 *	:	designator_or_func_ref
	 *	|	literal_constant
	 *	|	array_constructor
	 *	|	structure_constructor
	 *	|	T_LPAREN expr T_RPAREN
	 *
	 */
	public abstract void primary();
	
	/** R702
	 * level_1_expr
	 *  : (defined_unary_op)? primary
	 */
	public abstract void level_1_expr(Token definedUnaryOp);
	
	/** R704: note, inserted as R704 functionality
	 * power_operand
	 *	: level_1_expr (power_op power_operand)?
	 */
	public abstract void power_operand(boolean hasPowerOperand);
	public abstract void power_operand__power_op(Token powerOp);

	/** R704: note, see power_operand
	 * mult_operand
	 *  : power_operand (mult_op power_operand)*
	 *  
	 *  @param numMults The number of optional mult_ops
	 */
	public abstract void mult_operand(int numMultOps);
	public abstract void mult_operand__mult_op(Token multOp);

	/** R705: note, moved leading optionals to mult_operand
	 * add_operand
	 *  : (add_op)? mult_operand (add_op mult_operand)*
	 *  
	 * @param addOp Optional add_op for this operand
	 * @param numAddOps The number of optional add_ops
	 */
	public abstract void add_operand(Token addOp, int numAddOps);
	public abstract void add_operand__add_op(Token addOp);

	/** R706: note, moved leading optionals to add_operand
	 * level_2_expr
	 *  : add_operand (concat_op add_operand)*
	 *  
	 *  @param numConcatOps The number of optional numConcatOps
	 */
	public abstract void level_2_expr(int numConcatOps);


	/** R710: note, moved leading optional to level_2_expr
	 * level_3_expr
	 *  : level_2_expr (rel_op level_2_expr)?
	 *  
	 *  @param relOp The rel-op, if present, null otherwise
	 */
	public abstract void level_3_expr(Token relOp);

	/** R714
	 * and_operand
	 *  :    (not_op)? level_3_expr (and_op level_3_expr)*
	 *
	 * @param hasNotOp True if optional not_op is present
	 * @param numAndOps The number of optional and_ops
	 */
	public abstract void and_operand(boolean hasNotOp, int numAndOps);
	public abstract void and_operand__not_op(boolean hasNotOp);

	/** R715: note, moved leading optional to or_operand
	 * or_operand
	 *    : and_operand (or_op and_operand)*
	 *
	 * @param numOrOps The number of optional or_ops
	 */
	public abstract void or_operand(int numOrOps);

	/** R716: note, moved leading optional to or_operand
	 * equiv_operand
	 *  : or_operand (equiv_op or_operand)*
	 *  
	 * @param numEquivOps The number of optional or_operands
	 * @param equivOp Optional equiv_op for this operand
	 */
	public abstract void equiv_operand(int numEquivOps);
	public abstract void equiv_operand__equiv_op(Token equivOp);

	/** R717: note, moved leading optional to equiv_operand
	 * level_5_expr
	 *  : equiv_operand (defined_binary_op equiv_operand)*
	 *  
	 * @param numDefinedBinaryOps The number of optional equiv_operands
	 * @param definedBinaryOp Optional defined_binary_op for this operand
	 */
	public abstract void level_5_expr(int numDefinedBinaryOps);
	public abstract void level_5_expr__defined_binary_op(Token definedBinaryOp);

	/** R722: note, moved leading optional to level_5_expr
	 * expr
	 *  : level_5_expr
	 */
	public abstract void expr();

	/** R734
	 *	assignment_stmt 
	 *	:	(label)? T_ASSIGNMENT_STMT variable	T_EQUALS expr T_EOS
	 *
	 * @param label Optional statement label
	 */
	public abstract void assignment_stmt(Token label);

	/** R737 list
	 * bounds_spec_list
	 * 	:    bounds_spec ( T_COMMA bounds_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void bounds_spec_list__begin();
	public abstract void bounds_spec_list(int count);

	/** R738 list
	 * bounds_remapping_list
	 * 	:    bounds_remapping ( T_COMMA bounds_remapping )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void bounds_remapping_list__begin();
	public abstract void bounds_remapping_list(int count);

	/** R755 list
	 * forall_triplet_spec_list
	 * 	:    forall_triplet_spec ( T_COMMA forall_triplet_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void forall_triplet_spec_list__begin();
	public abstract void forall_triplet_spec_list(int count);

	/** R814 list
	 * case_value_range_list
	 * 	:    case_value_range ( T_COMMA case_value_range )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void case_value_range_list__begin();
	public abstract void case_value_range_list(int count);

	/** R817 list
	 * association_list
	 * 	:    association ( T_COMMA association )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void association_list__begin();
	public abstract void association_list(int count);

	/** R843
	 * cycle_stmt
	 *	:	(label)? T_CYCLE (T_IDENT)? T_EOS
	 * 
	 * T_IDENT inlined for do_construct_name
	 * 
	 * @param label Optional statement label
	 * @param id Optional do-construct-name
	 */
	public abstract void cycle_stmt(Token label, Token id);

	/** R844
	 * exit_stmt
	 *	:	(label)? T_EXIT (T_IDENT)? T_EOS
	 *
	 * T_IDENT inlined for do_construct_name
	 * 
	 * @param label Optional statement label
	 * @param id Optional do-construct-name
	 */
	public abstract void exit_stmt(Token label, Token id);

	/** R845
	 * goto_stmt
	 *	:	t_go_to label T_EOS
	 *
	 * @param label The branch target statement label
	 */
	public abstract void goto_stmt(Token label);

	/** R846
	 * computed_goto_stmt
	 *	:	(label)? t_go_to T_LPAREN label_list T_RPAREN (T_COMMA)? expr T_EOS
	 *
	 * ERR_CHK 846 scalar_int_expr replaced by expr
	 * 
	 * @param label Optional statement label
	 */
	public abstract void computed_goto_stmt(Token label);

	/** R847
	 * arithmetic_if_stmt
	 *	:	(label)? T_ARITHMETIC_IF_STMT T_IF
	 *		T_LPAREN expr T_RPAREN label T_COMMA label T_COMMA label T_EOS
	 *
	 * ERR_CHK 847 scalar_numeric_expr replaced by expr
	 * 
	 * @param label  Optional statement label
	 * @param label1 The first branch target statement label
	 * @param label2 The second branch target statement label
	 * @param label3 The third branch target statement label
	 */
	public abstract void arithmetic_if_stmt(Token label, Token label1, Token label2, Token label3);

	/** R848
	 * continue_stmt
	 *	:	(label)? T_CONTINUE
	 * 
	 * @param label  Optional statement label
	 */
	public abstract void continue_stmt(Token label);

	/** R849
	 * stop_stmt
	 *	:	(label)? T_STOP (stop_code)? T_EOS
	 *
	 *@param label Optional statement label
	 *@param hasStopCode True if the stop-code is present, false otherwise
	 */
	public abstract void stop_stmt(Token label, boolean hasStopCode);

	/** R850
	 * stop_code
	 *	: scalar_char_constant
	 *	| T_DIGIT_STRING
	 * 
	 * ERR_CHK 850 T_DIGIT_STRING must be 5 digits or less
	 * 
	 * @param digitString The stop-code token, otherwise is a scalar-char-constant
	 */
	public abstract void stop_code(Token digitString);

	/** R905 list
	 * connect_spec_list
	 * 	:    connect_spec ( T_COMMA connect_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void connect_spec_list__begin();
	public abstract void connect_spec_list(int count);

	/** R909 list
	 * close_spec_list
	 * 	:    close_spec ( T_COMMA close_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void close_spec_list__begin();
	public abstract void close_spec_list(int count);

	/** R913 list
	 * io_control_spec_list
	 * 	:    io_control_spec ( T_COMMA io_control_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void io_control_spec_list__begin();
	public abstract void io_control_spec_list(int count);

	/** R915 list
	 * input_item_list
	 * 	:    input_item ( T_COMMA input_item )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void input_item_list__begin();
	public abstract void input_item_list(int count);

	/** R916 list
	 * output_item_list
	 * 	:    output_item ( T_COMMA output_item )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void output_item_list__begin();
	public abstract void output_item_list(int count);

	/** R922 list
	 * wait_spec_list
	 * 	:    wait_spec ( T_COMMA wait_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void wait_spec_list__begin();
	public abstract void wait_spec_list(int count);

	/** R926 list
	 * position_spec_list
	 * 	:    position_spec ( T_COMMA position_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void position_spec_list__begin();
	public abstract void position_spec_list(int count);

	/** R928 list
	 * flush_spec_list
	 * 	:    flush_spec ( T_COMMA flush_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void flush_spec_list__begin();
	public abstract void flush_spec_list(int count);

	/** R930 list
	 * inquire_spec_list
	 * 	:    inquire_spec ( T_COMMA inquire_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void inquire_spec_list__begin();
	public abstract void inquire_spec_list(int count);

	/** R1003 list
	 * format_item_list
	 * 	:    format_item ( T_COMMA format_item )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void format_item_list__begin();
	public abstract void format_item_list(int count);

	/** R1010 list
	 * v_list_part
	 * v_list
	 * 	:    (T_PLUS|T_MINUS)? T_DIGIT_STRING ( T_COMMA (T_PLUS|T_MINUS)? T_DIGIT_STRING )*
	 * 
	 * @param plus_minus Optional T_PLUSIT_MINUS token.
	 * @param digitString The digit string token.
	 * @param count The number of items in the list.
	 */
	public abstract void v_list_part(Token plus_minus, Token digitString);
	public abstract void v_list__begin();
	public abstract void v_list(int count);

	/** R1101
	 * main_program
	 *	(program_stmt)?	specification_part (execution_part)? (internal_subprogram_part)?
	 *	end_program_stmt
	 * 
	 * @param hasProgramStmt Optional program-stmt
	 * @param hasExecutionPart Optional execution-part
	 * @param hasInternalSubprogramPart Optional internal-subprogram-part
	 */
	public abstract void main_program__begin();
	public abstract void main_program(boolean hasProgramStmt, boolean hasExecutionPart, boolean hasInternalSubprogramPart);

	/** R1102
	 * program_stmt
	 * :	(label)? ...
	 * 
	 * @param label Optional statement label
	 * @param id Optional program name
	 */
	public abstract void program_stmt(Token label, Token id);

	/** R1103
	 * end_program_stmt
	 * :	(label)? ...
	 * 
	 * @param label Optional statement label
	 * @param id Optional program name
	 */
	public abstract void end_program_stmt(Token label, Token id);

	/** R1111 list
	 * rename_list
	 * 	:    rename ( T_COMMA rename )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void rename_list__begin();
	public abstract void rename_list(int count);

	/** R1112 list
	 * only_list
	 * 	:    only ( T_COMMA only )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void only_list__begin();
	public abstract void only_list(int count);

	/** R1212
	 * proc_interface
	 *	:	T_IDENT
	 *	|	declaration_type_spec
	 *
	 * @param id The interface name.
	 */
	public abstract void proc_interface(Token id);

	/** R1213
	 * proc_attr_spec
	 *	:	access_spec
	 *	|	proc_language_binding_spec
	 *	|	T_INTENT T_LPAREN intent_spec T_RPAREN
	 *	|	T_OPTIONAL
	 *	|	T_POINTER
	 *	|	T_SAVE
	 *
	 * @param spec The procedure attribute specification.
	 */
	public abstract void proc_attr_spec(AttrSpec spec);

	/** R1214
	 * proc_decl
	 *    :	T_IDENT ( T_EQ_GT null_init {hasNullInit=true;} )?
	 *    
	 * @param id The name of the procedure.
	 * @param hasNullInit True if null-init is present.
	 */
	public abstract void proc_decl(Token id, boolean hasNullInit);
	   
	/** R1214 list
	 * proc_decl_list
	 * 	:    proc_decl ( T_COMMA proc_decl )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void proc_decl_list__begin();
	public abstract void proc_decl_list(int count);

	/** R1217
	 * function_reference
	 * 	:	procedure-designator LPAREN (actual_arg_spec_list)* RPAREN
	 * 
	 * Called from designator_or_proc_ref to reduce ambiguities.
	 * procedure-designator is replaced by data-ref thus function-reference may also
	 * be matched in data-ref as an array-ref, i.e., foo(1) looks like an array
	 * 
	 * @param hasActualArgSpecList True if an actual-arg-spec-list is present
	 */
	public abstract void function_reference(boolean hasActualArgSpecList);

	/** R1218
	 * call_stmt
	 *	:	(label)? T_CALL procedure_designator
			( T_LPAREN (actual_arg_spec_list)? T_RPAREN )? T_EOS
	 * 
	 * @param label Optional statement label
	 * @param hasActionArgSpecList True if an actual-arg-spec-list is present
	 */
	public abstract void call_stmt(Token label, boolean hasActualArgSpecList);

	/** R1220
	 * actual_arg_spec
	 *	:	(T_IDENT T_EQUALS)? actual_arg
	 *
	 * R619, section_subscript has been combined with actual_arg_spec (R1220) 
	 * to reduce backtracking thus R619 is called from R1220.
	 * 
	 * @param keyword The keyword is the name of the dummy argument in the explicit
	 * interface of the procedure.
	 */
	public abstract void actual_arg_spec(Token keyword);
	
	/** R1220 list
	 * actual_arg_spec_list
	 * 	:    actual_arg_spec ( T_COMMA actual_arg_spec )*
	 *
	 * List begin may be called incorrectly from substring_range_or_arg_list.  This
	 * will be noted by a count of less than zero.
	 *
	 * @param count The number of items in the list.  If count is less than zero, clean
	 * up the effects of list begin (as if it had not been called).
	 */
	public abstract void actual_arg_spec_list__begin();
	public abstract void actual_arg_spec_list(int count);
	
	/** R1221
	 * actual_arg
	 *	:	expr
	 *	|	T_ASTERISK label
	 *
	 * ERR_CHK 1221 ensure ( expr | designator ending in T_PERCENT T_IDENT)
	 * T_IDENT inlined for procedure_name
	 * 
	 * @param hasExpr True if actual-arg is an expression.
	 * @param label The label of the alt-return-spec (if not null).
	 */
	public abstract void actual_arg(boolean hasExpr, Token label);
	
	/** R1233
	 * dummy_arg
	 *	:	T_IDENT | T_ASTERISK
	 *
	 * @param dummy The dummy argument token.
	 */
	public abstract void dummy_arg(Token dummy);
	
	/** R1233 list
	 * dummy_arg_list
	 * 	:    dummy_arg ( T_COMMA dummy_arg )*
	 * 
	 * @param count The number of items in the list.
	 */
	public abstract void dummy_arg_list__begin();
	public abstract void dummy_arg_list(int count);

}
