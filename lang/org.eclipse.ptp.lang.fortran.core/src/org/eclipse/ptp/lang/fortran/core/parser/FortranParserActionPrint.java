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

public class FortranParserActionPrint implements IFortranParserAction {
	
	private boolean verbose = true;
	
	FortranParserActionPrint(String[] args, FortranParser parser, String filename) {
		super();
	}
	
	private void printRuleHeader(int rule, String name) {
		printRuleHeader(rule, name, "");
	}
	
	private void printRuleHeader(int rule, String name, String addendum) {
		if (verbose) {
			System.out.print("R");
			if (rule < 1000) System.out.print(" ");
		}
		System.out.print(rule);
		if (verbose) {
			System.out.print(":" + name + ":");
		} else {
			if (addendum.length() > 0) System.out.print(":" + addendum);
		}
	}
	
	private void printRuleTrailer() {
		System.out.println();
	}
	
	private void printParameter(Object param, String name) {
		System.out.print(" ");
		if (verbose) System.out.print(name + "=");
		System.out.print(param);				
	}
	
	private void printParameter(Token param, String name) {
		System.out.print(" ");
		if (verbose) {
			System.out.print(name + "=");
			System.out.print(param);
		} else {
			if (param != null) System.out.print(param.getText());
			else System.out.print("null");
		}
	}
	
	public void setVerbose(boolean flag) {
		verbose = flag;
	}
	
	public static String toString(KindParam kp) {
	    switch (kp) {
		case none:		return "none";
		case literal:	return "literal";
		case id:		return "id";
		}
	    return "";
	}


	/** R102 list
	 * generic_name (xyz-name)
	 * generic_name_list (xyz-list R101)
	 * generic_name_list_part
	 */
	public void generic_name_list_part(Token ident) {
		printRuleHeader(102, "generic-name-list-part", "part");
		printParameter(ident, "ident");
		printRuleTrailer();
	}
	public void generic_name_list__begin() {
		printRuleHeader(102, "generic-name-list__begin", "list-begin");
		printRuleTrailer();
	}
	public void generic_name_list(int count) {
		printRuleHeader(102, "generic-name-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R210
	 * internal_subprogram_part
	 */
	public void internal_subprogram_part(int count) {
		printRuleHeader(210, "internal-subprogram-part");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R305
	 * constant
	 */
	public void constant(Token id) {
		printRuleHeader(305, "constant");
		printParameter(id, "id");
		printRuleTrailer();
	}

	/** R308
	 * int_constant
	 */
	public void int_constant(Token id) {
		printRuleHeader(308, "int-constant");
		printParameter(id, "id");
		printRuleTrailer();
	}

	/** R309
	 * 	char_constant
	 */
	public void char_constant(Token id) {
		printRuleHeader(309, "char-constant");
		printParameter(id, "id");
		printRuleTrailer();
	}

	/** R311
	 * defined_operator
	 */
	public void defined_operator(Token definedOp) {
		printRuleHeader(311, "defined-operator");
		printParameter(definedOp, "definedOp");
		printRuleTrailer();
	}

	/** R313
	 * label
     */
	public void label(Token lbl) {
		printRuleHeader(313, "label");
		printParameter(lbl, "lbl");
		printRuleTrailer();
	}

	/** R313 list
	 * label
	 * label_list
     */
	public void label_list__begin() {
		printRuleHeader(313, "label-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void label_list(int count) {
		printRuleHeader(313, "label-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R402
	 * type-param-value
	 */
	public void type_param_value(boolean hasExpr, boolean hasAsterisk, boolean hasColon) {
		printRuleHeader(402, "type-param-value");
		printParameter(hasExpr, "hasExpr");
		printParameter(hasAsterisk, "hasAsterisk");
		printParameter(hasColon, "hasColon");
		printRuleTrailer();
	}


	/** R403
	 * intrinsic_type_spec
	 */
	public void intrinsic_type_spec(IntrinsicTypeSpec type, boolean hasKindSelector) {
		printRuleHeader(403, "intrinsic-type-spec");
		printParameter(type, "type");
		printParameter(hasKindSelector, "hasKindSelector");
		printRuleTrailer();
	}

	/** R404
	 * kind_selector
	 */
	public void kind_selector(boolean hasExpression, Token typeSize) {
		printRuleHeader(404, "kind-selector");
		printParameter(hasExpression, "hasExpression");
		printParameter(typeSize, "typeSize");
		printRuleTrailer();
	}

	/** R405
	 * signed_int_literal_constant
	 */
	 public void signed_int_literal_constant(Token sign) {
		 printRuleHeader(405, "signed-int-literal-constant");
		 printParameter(sign, "sign");
		 printRuleTrailer(); 
	 }

	/** R406
	 * int_literal_constant
	 */
	 public void int_literal_constant(Token digitString, Token kindParam) {
		 printRuleHeader(406, "int-literal-constant");
		 printParameter(digitString, "digitString");
		 printParameter(kindParam, "kindParam");
		 printRuleTrailer(); 
	 }

	/** R416
	 * signed_real_literal_constant
	 */
	 public void signed_real_literal_constant(Token sign) {
		 printRuleHeader(416, "signed-real-literal-constant");
		 printParameter(sign, "sign");
		 printRuleTrailer(); 
	 }

	/** R417
	 * real_literal_constant
	 */
	 public void real_literal_constant(Token digits, Token fractionExp, Token kindParam) {
		 printRuleHeader(417, "real-literal-constant");
		 printParameter(digits, "digits");
		 printParameter(fractionExp, "fractionExp");
		 printParameter(kindParam, "kindParam");
		 printRuleTrailer(); 
	 }
	 
	/** R422
	 * real_part
	 */
	public void real_part(boolean hasIntConstant, boolean hasRealConstant, Token id) {
		printRuleHeader(422, "real-part");
		printParameter(hasIntConstant, "hasIntConstant");
		printParameter(hasRealConstant, "hasRealConstant");
		printParameter(id, "id");
		printRuleTrailer();
	}

	/** R423
	 * imag_part
	 */
	public void imag_part(boolean hasIntConstant, boolean hasRealConstant, Token id) {
		printRuleHeader(423, "imag-part");
		printParameter(hasIntConstant, "hasIntConstant");
		printParameter(hasRealConstant, "hasRealConstant");
		printParameter(id, "id");
		printRuleTrailer();
	}

	/** R424
	 * char-selector
	 */
	public void char_selector(KindLenParam kindOrLen1, KindLenParam kindOrLen2, boolean hasAsterisk) {
		printRuleHeader(424, "char-selector");
		printParameter(kindOrLen1, "kindOrLen1");
		printParameter(kindOrLen2, "kindOrLen2");
		printParameter(hasAsterisk, "hasAsterisk");
		printRuleTrailer();
	}

	/** R425
	 * length-selector
	 */
	public void length_selector(KindLenParam kindOrLen, boolean hasAsterisk) {
		printRuleHeader(425, "length-selector");
		printParameter(kindOrLen, "kindOrLen");
		printParameter(hasAsterisk, "hasAsterisk");
		printRuleTrailer();
	}

	/** R426
	 * char_length
	 */
	public void char_length(boolean hasTypeParamValue) {
		printRuleHeader(426, "char-length");
		printParameter(hasTypeParamValue, "hasTypeParamValue");
		printRuleTrailer();
	}

	/** R427
	 * char_literal_constant
	 */
	public void char_literal_constant(Token digitString, Token id, Token str) {
		printRuleHeader(428, "char-literal-constant");
		printParameter(digitString, "digitString-kind-param");
		printParameter(id, "identifier-kind-param");
		printParameter(str, "str");
		printRuleTrailer(); 	
	}

	/** R428
	 * logical_literal_constant
	 */
	public void logical_literal_constant(boolean isTrue, Token kindParam) {
		printRuleHeader(428, "logical-literal-constant");
		printParameter(isTrue, "isTrue");
		printParameter(kindParam, "kindParam");
		printRuleTrailer(); 
	}

	/** R430 
	 * derived_type_stmt
	 *
	 */
	public void derived_type_stmt(Token label, Token id) {
		printRuleHeader(430, "derived-type-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(id, "id");
		printRuleTrailer();
	}

	/** R431 list
	 * type_attr_spec
	 * type_attr_spec_list
	 */
	public void type_attr_spec_list__begin() {
		printRuleHeader(431, "type-attr-spec-list__begin", "list-begin");
		printRuleTrailer();
	}
	public void type_attr_spec_list(int count) {
		printRuleHeader(431, "type-attr-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();	
	}

	/** R433 
	 * end_type_stmt
	 *
	 */
	public void end_type_stmt(Token label, Token id) {
		printRuleHeader(430, "end-type-stmt");
		if (label != null) printParameter(label, "label");
		if (id != null) printParameter(id, "id");
		printRuleTrailer();
	}

	/** R434 
	 * sequence_stmt
	 *
	 */
	public void sequence_stmt(Token label) {
		printRuleHeader(430, "sequence-stmt");
		if (label != null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R436 list
	 * type_param_decl
	 * type_param_decl_list
	 */
	public void type_param_decl_list__begin() {
		printRuleHeader(436, "type-param-decl-list__begin", "list-begin");
		printRuleTrailer();
	}
	public void type_param_decl_list(int count) {
		printRuleHeader(436, "type-param-decl-list", "list");
		printParameter(count, "count");
		printRuleTrailer();	
	}

	/** R440
	 *	data_component_def_stmt
	 */
	public void data_component_def_stmt(Token label, boolean hasSpec) {
		printRuleHeader(440, "data-component-def-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(hasSpec, "hasSpec");
		printRuleTrailer();
	}

	/** R441 list
	 * component_attr_spec
	 * component_attr_spec_list
	 */
	public void component_attr_spec_list__begin() {
		printRuleHeader(441, "component-attr-spec-list__begin", "list-begin");
		printRuleTrailer();

	}
	public void component_attr_spec_list(int count) {
		printRuleHeader(441, "component-attr-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();	
	
	}

	/** R442 list
	 * component_decl
	 * component_decl_list
	 */
	public void component_decl_list__begin() {
		printRuleHeader(442, "component-decl-list__begin", "list-begin");
		printRuleTrailer();
	
	}
	public void component_decl_list(int count) {
		printRuleHeader(442, "component-decl-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R443 list
	 * deferred_shape_spec_list
	 */
	public void deferred_shape_spec_list__begin() {
		printRuleHeader(443, "deferred-shape-spec-list__begin", "list-begin");
		printRuleTrailer();
	}
	public void deferred_shape_spec_list(int count) {
		printRuleHeader(443, "deferred-shape-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R445
	 *	proc_component_def_stmt
	 */
	public void proc_component_def_stmt(Token label, boolean hasInterface) {
		printRuleHeader(445, "proc-component-def-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(hasInterface, "hasInterface");
		printRuleTrailer();
	}

	/** R446 list
	 * proc_component_attr_spec_list
	 */
	public void proc_component_attr_spec_list__begin() {
		printRuleHeader(446, "proc-component-attr-spec-list__begin", 
			"list-begin");
		printRuleTrailer();
	}
	public void proc_component_attr_spec_list(int count) {
		printRuleHeader(446, "proc-component-attr-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R447
	 * private_components_stmt
	 */
	public void private_components_stmt(Token label) {
		printRuleHeader(447, "private-components-stmt");
		if (label != null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R449
	 * binding_private_stmt
	 */
	public void binding_private_stmt(Token label) {
		printRuleHeader(447, "binding-private-stmt");
		if (label != null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R450
	 * proc_binding_stmt
	 */
	public void proc_binding_stmt(Token label) {
		printRuleHeader(450, "proc-binding-stmt");
		if (label != null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R453
	 * binding_attr
	 */
	public void binding_attr(AttrSpec attr) {
		printRuleHeader(453, "binding-attr");
		printParameter(attr, "attr");
		printRuleTrailer();	
	}

	/** R453 list
	 * binding_attr_list
	 */
	public void binding_attr_list__begin() {
		printRuleHeader(453, "binding-attr-list__begin", 
			"list-begin");
		printRuleTrailer();
	}
	public void binding_attr_list(int count) {
		printRuleHeader(453, "binding-attr-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R455
	 * derived_type_spec
	 */
	public void derived_type_spec(Token typeName, boolean hasTypeParamSpecList) {
		printRuleHeader(455, "derived-type-spec");
		printParameter(typeName, "typeName");
		printParameter(hasTypeParamSpecList, "hasTypeParamSpecList");
		printRuleTrailer();			
	}

	/** R456 list
	 * type_param_spec_list
	 */
	public void type_param_spec_list__begin() {
		printRuleHeader(456, "type-param-spec-list__begin", "list-begin");
		printRuleTrailer();
	}
	public void type_param_spec_list(int count) {
		printRuleHeader(456, "type-param-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R458 list
	 * component_spec_list
	 */
	public void component_spec_list__begin() {
		printRuleHeader(458, "component-spec-list__begin", "list-begin");
		printRuleTrailer();
	}
	public void component_spec_list(int count) {
		printRuleHeader(458, "component-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R461
	 * enum_def_stmt
	 */
	public void enum_def_stmt(Token label) {
		printRuleHeader(461, "enum-def-stmt");
		if (label != null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R462
	 * enumerator_def_stmt
	 */
	public void enumerator_def_stmt(Token label) {
		printRuleHeader(462, "enumerator-def-stmt");
		if (label != null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R463 list
	 * enumerator_list
	 */
	public void enumerator_list__begin() {
		printRuleHeader(463, "enumerator-list__begin", "list-begin");
		printRuleTrailer();
	}
	public void enumerator_list(int count) {
		printRuleHeader(463, "enumerator-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R464
	 * end_enum_stmt
	 */
	public void end_enum_stmt(Token label) {
		printRuleHeader(464, "end-enum-stmt");
		if (label != null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R465
	 * array_constructor
	 */
	public void array_constructor() {
		printRuleHeader(465, "array-constructor");
		printRuleTrailer();
	}

	/** R469 list
	 * ac_value_list
	 */
	public void ac_value_list__begin() {
		printRuleHeader(469, "ac-value-list__begin", "list-begin");
		printRuleTrailer();
	}
	public void ac_value_list(int count) {
		printRuleHeader(469, "ac-value-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R501
	 * type_declaration_stmt
	 */
	public void type_declaration_stmt__begin() {
		printRuleHeader(501, "type-declaration-stmt__begin", "begin");
		printRuleTrailer();
	}
	public void type_declaration_stmt(Token label, int numAttributes) {
		printRuleHeader(501, "type-declaration-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(numAttributes, "numAttributes");
		printRuleTrailer();
	}

	/** R502
	 * declaration_type_spec
	 *	:	intrinsic_type_spec
	 *	|	T_TYPE T_LPAREN	derived_type_spec T_RPAREN
	 *	|	T_CLASS	T_LPAREN derived_type_spec T_RPAREN
	 *	|	T_CLASS T_LPAREN T_ASTERISK T_RPAREN
	 *
	 * @param type The type of declaration-type-spec {INTRINSIC,TYPE,CLASS,POLYMORPHIC}.
	 */
	public void declaration_type_spec(DeclarationTypeSpec type) {
		printRuleHeader(502, "declaration-type-spec");
		printParameter(type, "type");
		printRuleTrailer();
	}

	/** R503
	 * attr_spec
	 */
	public void attr_spec(AttrSpec attr) {
		printRuleHeader(503, "attr-spec");
		printParameter(attr, "attr");
		printRuleTrailer();
	}

	/** R504, R503-F2008
	 * entity_decl
	 *  : T_IDENT ( T_LPAREN array_spec T_RPAREN )?
	 *            ( T_LBRACKET co_array_spec T_RBRACKET )?
	 *            ( T_ASTERISK char_length )? ( initialization )? 
	 */
	public void entity_decl(Token id) {
		printRuleHeader(504, "entity-decl");
		printParameter(id, "id");
		printRuleTrailer();
	}

	/** R504 list
	 * entity_decl_list
	 * 	:    entity_decl ( T_COMMA entity_decl )*
	 */
	public void entity_decl_list__begin() {
		printRuleHeader(504, "entity-decl-list__begin", "list-begin");
		printRuleTrailer();
	}
	public void entity_decl_list(int count) {
		printRuleHeader(504, "entity-decl-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R506
	 * initialization
	 */
	public void initialization(boolean hasExpr, boolean hasNullInit) {
		printRuleHeader(506, "initialization");
		printParameter(hasExpr, "hasExpr");
		printParameter(hasNullInit, "hasNullInit");
		printRuleTrailer();
	}

	/** R507
	 * null_init
	 */
	public void null_init(Token id) {
		printRuleHeader(507, "null-init");
		printParameter(id, "function-reference");
		printRuleTrailer();	
	}

	/** R509
	 * language_binding_spec 
	 */
	public void language_binding_spec(Token id, boolean hasName) {
		printRuleHeader(509, "language-binding-spec");
		printParameter(id, "language");
		printParameter(hasName, "hasName");
		printRuleTrailer();	
	}

	/** R510
	 * array_spec
	 */
	public void array_spec__begin() {
		printRuleHeader(510, "array-spec__begin", "begin");
		printRuleTrailer();
	}
	public void array_spec(int count) {
		printRuleHeader(510, "array-spec");
		printParameter(count, "count");
		printRuleTrailer();
	}
	public void array_spec_element(ArraySpecElement type) {
		printRuleHeader(510, "array-spec-element");
		printParameter(type, "type");
		printRuleTrailer();
	}

	/** R511 list
	 * explicit_shape_spec
	 * explicit_shape_spec_list
	 */
	public void explicit_shape_spec(boolean hasUpperBound) {
		printRuleHeader(511, "explicit-shape-spec");
		printParameter(hasUpperBound, "hasUpperBound");
		printRuleTrailer();
	}
	public void explicit_shape_spec_list__begin() {
		printRuleHeader(511, "explicit-shape-spec-list__begin", "list-begin");
		printRuleTrailer();
	}
	public void explicit_shape_spec_list(int count) {
		printRuleHeader(511, "explicit-shape-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R517
	 * intent_spec
	 */
	public void intent_spec(IntentSpec intent) {
		printRuleHeader(517, "intent-spec");
		printParameter(intent, "intent");
		printRuleTrailer();
	}

	/** R519-F2008 list
	 * deferred_co_space_spec_list
	 */
	public void deferred_co_shape_spec_list__begin() {
		printRuleHeader(519, "deferred-co-shape-spec-list__begin", 
			"list-begin-F2008");
		printRuleTrailer();
	}
	public void deferred_co_shape_spec_list(int count) {
		printRuleHeader(519, "deferred-co-shape-spec-list", "list-F2008");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R518
	 * access_stmt
	 */
	public void access_stmt(Token label, boolean hasList) {
		printRuleHeader(447, "access-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(hasList, "has-access-id-list");
		printRuleTrailer();
	}

	/** R519 list
	 * access_id_list
     */
	public void access_id_list__begin() {
		printRuleHeader(519, "access-id-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void access_id_list(int count) {
		printRuleHeader(519, "access-id-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R521
	 * asynchronous_stmt
	 */
	public void asynchronous_stmt(Token label) {
		printRuleHeader(447, "asynchronous-stmt");
		if (label != null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R522
	 * bind_stmt
     */
	public void bind_stmt(Token label) {
		printRuleHeader(522, "bind-stmt");
		printParameter(label, "label");
		printRuleTrailer();
	}

	/** R523 list
	 * bind_entity
	 * bind_entity_list
     */
	public void bind_entity(Token entity, boolean isCommonBlockName) {
		printRuleHeader(523, "bind-entity");
		printParameter(entity, "entity");
		printParameter(isCommonBlockName, "isCommonBlockName");
		printRuleTrailer();
	}
	public void bind_entity_list__begin() {
		printRuleHeader(523, "bind-entity-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void bind_entity_list(int count) {
		printRuleHeader(523, "bind-entity-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R524
	 * data_stmt (Kinda, sorta list like.)
     */
	public void data_stmt__begin() {
		printRuleHeader(524, "data-stmt__begin", "list-begin");
		printRuleTrailer();	
	}
	public void data_stmt(Token label, int count) {
		printRuleHeader(524, "data-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R526 list
	 * data_stmt_object_list
     */
	public void data_stmt_object_list__begin() {
		printRuleHeader(526, "data-stmt-object-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void data_stmt_object_list(int count) {
		printRuleHeader(526, "data-stmt-object-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R528 list
	 * data_i_do_object_list
     */
	public void data_i_do_object_list__begin() {
		printRuleHeader(528, "data-i-do-object-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void data_i_do_object_list(int count) {
		printRuleHeader(528, "data-i-do-object-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R530 list
	 * data_stmt_value_list
     */
	public void data_stmt_value_list__begin() {
		printRuleHeader(530, "data-stmt-value-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void data_stmt_value_list(int count) {
		printRuleHeader(530, "data-stmt-value-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R535
	 * dimension_stmt (Kinda, sorta list like.)
     */
	public void dimension_stmt__begin() {
		printRuleHeader(535, "dimension-stmt__begin", "list-begin");
		printRuleTrailer();	
	}
	public void dimension_stmt(Token label, int count) {
		printRuleHeader(535, "dimension-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R536
	 * intent_stmt
     */
	public void intent_stmt(Token label) {
		printRuleHeader(536, "intent-stmt");
		if (label!=null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R537
	 * optional_stmt
     */
	public void optional_stmt(Token label) {
		printRuleHeader(536, "optional-stmt");
		if (label!=null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R538
	 * parameter_stmt
     */
	public void parameter_stmt(Token label) {
		printRuleHeader(538, "parameter-stmt");
		if (label!=null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R539 list
	 * named_constant_def_list
     */
	public void named_constant_def_list__begin() {
		printRuleHeader(539, "named-constant-def-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void named_constant_def_list(int count) {
		printRuleHeader(539, "named-constant-def-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R540
	 * pointer_stmt
     */
	public void pointer_stmt(Token label) {
		printRuleHeader(540, "pointer-stmt");
		if (label!=null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R541 list
	 * pointer_decl_list
     */
	public void pointer_decl_list__begin() {
		printRuleHeader(541, "pointer-decl-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void pointer_decl_list(int count) {
		printRuleHeader(541, "pointer-decl-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R542
	 * protected_stmt
     */
	public void protected_stmt(Token label) {
		printRuleHeader(542, "protected-stmt");
		if (label!=null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R543
	 * save_stmt
     */
	public void save_stmt(Token label, boolean hasSavedEntityList) {
		printRuleHeader(543, "save-stmt");
		if (label!=null) printParameter(label, "label");
		printParameter(hasSavedEntityList, "hasSavedEntityList");
		printRuleTrailer();
	}

	/** R544 list
	 * saved_entity_list
     */
	public void saved_entity_list__begin() {
		printRuleHeader(544, "saved-entity-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void saved_entity_list(int count) {
		printRuleHeader(544, "saved-entity-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R546
	 * target_stmt
     */
	public void target_stmt__begin() {
		printRuleHeader(546, "target-stmt__begin");
		printRuleTrailer();	
	}
	public void target_stmt(Token label, int count) {
		printRuleHeader(546, "target-stmt");
		if (label!=null) printParameter(label, "label");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R547
	 * value_stmt
     */
	public void value_stmt(Token label) {
		printRuleHeader(547, "value-stmt");
		if (label!=null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R548
	 * volatile_stmt
     */
	public void volatile_stmt(Token label) {
		printRuleHeader(548, "volatile-stmt");
		if (label!=null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R549
	 * implicit_stmt
     */
	public void implicit_stmt(Token label) {
		printRuleHeader(549, "implicit-stmt");
		if (label!=null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R550 list
	 * implicit_spec_list
     */
	public void implicit_spec_list__begin() {
		printRuleHeader(550, "implicit-spec-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void implicit_spec_list(int count) {
		printRuleHeader(550, "implicit-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R551 list
	 * letter_spec_list
     */
	public void letter_spec_list__begin() {
		printRuleHeader(551, "letter-spec-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void letter_spec_list(int count) {
		printRuleHeader(551, "letter-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R553 list
	 * namelist_group_object_list
     */
	public void namelist_group_object_list__begin() {
		printRuleHeader(553, "namelist-group-object-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void namelist_group_object_list(int count) {
		printRuleHeader(553, "namelist-group-object-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R554
	 * equivalence_stmt
     */
	public void equivalence_stmt(Token label) {
		printRuleHeader(554, "equivalence-stmt");
		if (label!=null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R555 list
	 * equivalence_set_list
     */
	public void equivalence_set_list__begin() {
		printRuleHeader(555, "equivalence-set-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void equivalence_set_list(int count) {
		printRuleHeader(555, "equivalence-set-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R556 list
	 * equivalence_object_list
     */
	public void equivalence_object_list__begin() {
		printRuleHeader(556, "equivalence-object-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void equivalence_object_list(int count) {
		printRuleHeader(556, "equivalence-object-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R558 list
	 * common_block_object_list
     */
	public void common_block_object_list__begin() {
		printRuleHeader(558, "common-block-object-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void common_block_object_list(int count) {
		printRuleHeader(558, "common-block-object-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R601
	 * variable
	 * :	designator
	 */
	public void variable() {
		printRuleHeader(601, "variable");
		printRuleTrailer();
	}

	// R602 variable_name was name inlined as T_IDENT

	/** R603
	 * designator
	 */
	public void designator(boolean hasSubstringRange) {
		printRuleHeader(603, "designator");
		printParameter(hasSubstringRange, "hasSubstringRange");
		printRuleTrailer();
	}

	/** R609
	 * substring
	 */
	public void substring(boolean hasSubstringRange) {
		printRuleHeader(609, "substring");
		printParameter(hasSubstringRange, "hasSubstringRange");
		printRuleTrailer();		
	}
	 

	/** R611
	 * substring_range
	 *	:	(expr)? T_COLON	(expr)?
	 *
	 * ERR_CHK 611 scalar_int_expr replaced by expr
	 *
	 * @param hasLowerBound True if lower bound is present in a substring-range (lower_bound:upper_bound).
	 * @param hasUpperBound True if upper bound is present in a substring-range (lower_bound:upper_bound).
	 */
	public void substring_range(boolean hasLowerBound, boolean hasUpperBound) {
		printRuleHeader(611, "substring-range");
		printParameter(hasLowerBound, "hasLowerBound");
		printParameter(hasUpperBound, "hasUpperBound");
		printRuleTrailer();
	}
	
	/** R612
	 *	data_ref
	 */
	public void data_ref(int numPartRef) {
		printRuleHeader(612, "data-ref");
		printParameter(numPartRef, "numPartRef");
		printRuleTrailer();
	}

	/** R613, R613-F2008
	 * part_ref
	 */
	public void part_ref(Token id, boolean hasSelectionSubscriptList, boolean hasImageSelector) {
		printRuleHeader(613, "part-ref");
		printParameter(id, "id");
		printParameter(hasSelectionSubscriptList, "hasSelectionSubscriptList");
		printParameter(hasImageSelector, "hasImageSelector");
		printRuleTrailer();
	}

	/** R619  (see R1220, actual_arg_spec)
	 * section_subscript/actual_arg_spec
	 */
	public void section_subscript(boolean hasLowerBound, boolean hasUpperBound, boolean hasStride, boolean isAmbiguous) {
		printRuleHeader(619, "section-subscript");
		printParameter(hasLowerBound, "hasLowerBound");
		printParameter(hasUpperBound, "hasUpperBound");
		printParameter(hasStride, "hasStride");
		printParameter(isAmbiguous, "isAmbiguous");
		printRuleTrailer();
	}

	/** R619 list
	 * section_subscript
	 * section_subscript_list
	 */
	public void section_subscript_list__begin() {
		printRuleHeader(619, "section-subscript-list__begin", "list-begin");
		printRuleTrailer();
	}
	public void section_subscript_list(int count) {
		printRuleHeader(619, "section-subscript-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R623
	 * allocate_stmt
	 */
	public void allocate_stmt(Token label, boolean hasTypeSpec, boolean hasAllocOptList) {
		printRuleHeader(623, "allocate-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(hasTypeSpec, "hasTypeSpec");
		printParameter(hasAllocOptList, "hasAllocOptList");
		printRuleTrailer();
	}

	/** R624
	 * alloc_opt
	 */
	public void alloc_opt(Token allocOpt) {
		printRuleHeader(624, "alloc-opt");
		printParameter(allocOpt, "allocOpt");
		printRuleTrailer();
	}
	
	/** R624 list
	 * alloc_opt_list
     */
	public void alloc_opt_list__begin() {
		printRuleHeader(624, "alloc-opt-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void alloc_opt_list(int count) {
		printRuleHeader(624, "alloc-opt-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R628, R631-F2008
	 * allocation
	 */
	public void allocation(boolean hasAllocateShapeSpecList, boolean hasAllocateCoArraySpec) {
		printRuleHeader(628, "allocation");
		printParameter(hasAllocateShapeSpecList, "hasAllocateShapeSpecList");
		printParameter(hasAllocateCoArraySpec, "hasAllocateCoArraySpec");
		printRuleTrailer();	
	}

	/** R628 list
	 * allocation_list
     */
	public void allocation_list__begin() {
		printRuleHeader(628, "allocation-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void allocation_list(int count) {
		printRuleHeader(628, "allocation-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R629 list
	 * allocate_object_list
     */
	public void allocate_object_list__begin() {
		printRuleHeader(629, "allocate-object-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void allocate_object_list(int count) {
		printRuleHeader(629, "allocate-object-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R630
	 * allocate_shape_spec
	 */
	public void allocate_shape_spec(boolean hasLowerBound, boolean hasUpperBound) {
		printRuleHeader(630, "allocate-shape-spec");
		printParameter(hasLowerBound, "hasLowerBound");
		printParameter(hasUpperBound, "hasUpperBound");
		printRuleTrailer();	
	}

	/** R630 list
	 * allocate_shape_spec_list
     */
	public void allocate_shape_spec_list__begin() {
		printRuleHeader(630, "allocate-shape-spec-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void allocate_shape_spec_list(int count) {
		printRuleHeader(630, "allocate-shape-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R633
	 *	nullify_stmt
	 */
	public void nullify_stmt(Token label) {
		printRuleHeader(633, "nullify-stmt");
		if (label != null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R634 list
	 * pointer_object_list
     */
	public void pointer_object_list__begin() {
		printRuleHeader(634, "pointer-object-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void pointer_object_list(int count) {
		printRuleHeader(634, "pointer-object-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R636 list
	 * dealloc_opt_list
     */
	public void dealloc_opt_list__begin() {
		printRuleHeader(636, "dealloc-opt-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void dealloc_opt_list(int count) {
		printRuleHeader(636, "dealloc-opt-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R637-F2008 list
	 * allocate_co_shape_spec_list
     */
	public void allocate_co_shape_spec_list__begin() {
		printRuleHeader(637, "allocate-co-shape-spec-list__begin", "list-begin-F2008");
		printRuleTrailer();	
	}
	public void allocate_co_shape_spec_list(int count) {
		printRuleHeader(637, "allocate-co-shape-spec-list", "list-F2008");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R701
	 * primary
	 */
	public void primary() {
		printRuleHeader(701, "primary");
		printRuleTrailer();
	}
	
	/** R702
	 * level_1_expr
	 */
	public void level_1_expr(Token definedUnaryOp) {
		if (definedUnaryOp != null) {
			printRuleHeader(702, "level-1-expr");
			printParameter(definedUnaryOp, "definedUnaryOp");
			printRuleTrailer();
		}
	}
	
	/** R704: note, inserted as R704 functionality
	 * power_operand
	 */
	public void power_operand(boolean hasPowerOperand) {
		if (hasPowerOperand) {
			printRuleHeader(704, "power-operand", "power-operand");
			printParameter(hasPowerOperand, "hasPowerOperand");
			printRuleTrailer();
		}
	}

	public void power_operand__power_op(Token powerOp) {
		printRuleHeader(704, "power-operand__power-op", "power-op");
		printParameter(powerOp, "powerOp");
		printRuleTrailer();
	}

	/** R704: note, see power_operand
	 * mult_operand
	 */
	public void mult_operand(int numMultOps) {
		if (numMultOps > 0) {
			printRuleHeader(704, "mult-operand");
			printParameter(numMultOps, "numMultOps");
			printRuleTrailer();
		}
	}

	public void mult_operand__mult_op(Token multOp) {
		printRuleHeader(704, "mult-operand__mult-op", "mult-op");
		printParameter(multOp, "multOp");
		printRuleTrailer();
	}

	/** R705: note, moved leading optionals to mult_operand
	 * add_operand
	 */
	public void add_operand(Token addOp, int numAddOps) {
		if (addOp != null | numAddOps > 0) {
			printRuleHeader(705, "add-operand");
			printParameter(addOp, "addOp");
			printParameter(numAddOps, "numAddOps");
			printRuleTrailer();
		}
	}

	public void add_operand__add_op(Token addOp) {
		printRuleHeader(705, "add-operand__add-op", "add-op");
		printParameter(addOp, "addOp");
		printRuleTrailer();
	}

	/** R706: note, moved leading optionals to add_operand
	 * level_2_expr
	 */
	public void level_2_expr(int numConcatOps) {
		if (numConcatOps > 0) {
			printRuleHeader(706, "level-2-expr");
			printParameter(numConcatOps, "numConcatOps");
			printRuleTrailer();
		}
	}

	/** R710: note, moved leading optional to level_2_expr
	 * level_3_expr
	 */
	public void level_3_expr(Token relOp) {
		if (relOp != null) {
			printRuleHeader(710, "level-3-expr");
			printParameter(relOp, "relOp");
			printRuleTrailer();
		}
	}

	/** R714
	 * and_operand
	 */
	public void and_operand(boolean hasNotOp, int numAndOps) {
		if (hasNotOp | numAndOps > 0) {
			printRuleHeader(714, "and-operand");
			printParameter(hasNotOp, "hasNotOp");
			printParameter(numAndOps, "numAndOps");
			printRuleTrailer();
		}
	}
	public void and_operand__not_op(boolean hasNotOp) {
		printRuleHeader(714, "and-operand__not-op", "not-op");
		printParameter(hasNotOp, "hasNotOp");
		printRuleTrailer();
	}

	/** R715: note, moved leading optional to or_operand
	 * or_operand
	 */
	public void or_operand(int numOrOps) {
		if (numOrOps > 0) {
			printRuleHeader(715, "or-operand");
			printParameter(numOrOps, "numOrOps");
			printRuleTrailer();
		}
	}

	/** R716: note, moved leading optional to or_operand
	 * equiv_operand
	 */
	public void equiv_operand(int numEquivOps) {
		if (numEquivOps > 0) {
			printRuleHeader(716, "equiv-operand");
			printParameter(numEquivOps, "numEquivOps");
			printRuleTrailer();
		}
	}
	public void equiv_operand__equiv_op(Token equivOp) {
		printRuleHeader(716, "equiv-operand__equiv-op", "equiv-op");
		printParameter(equivOp, "equivOp");
		printRuleTrailer();
	}

	/** R717: note, moved leading optional to equiv_operand
	 * level_5_expr
	 */
	public void level_5_expr(int numDefinedBinaryOps) {
		if (numDefinedBinaryOps > 0) {
			printRuleHeader(717, "level-5-expr");
			printParameter(numDefinedBinaryOps, "numDefinedBinaryOps");
			printRuleTrailer();
		}
	}
	public void level_5_expr__defined_binary_op(Token definedBinaryOp) {
		printRuleHeader(717, "level-5-expr__defined-binary-op", "defined-binary-op");
		printParameter(definedBinaryOp, "definedBinaryOp");
		printRuleTrailer();
	}

	/** R722: note, moved leading optional to level_5_expr
	 */
	public void expr() {
		// TODO (make optional) printRuleHeader(722, "expr");
		// printRuleTrailer();
	}

	/** R734
	 *	assignment_stmt 
	 */
	public void assignment_stmt(Token label) {
		printRuleHeader(734, "assignment-stmt");
		if (label != null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R737 list
	 * bounds_spec_list
     */
	public void bounds_spec_list__begin() {
		printRuleHeader(737, "bounds-spec-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void bounds_spec_list(int count) {
		printRuleHeader(737, "bounds-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R738 list
	 * bounds_remapping_list
     */
	public void bounds_remapping_list__begin() {
		printRuleHeader(738, "bounds-remapping-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void bounds_remapping_list(int count) {
		printRuleHeader(738, "bounds-remapping-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R743
	 * where_stmt
     */
	public void where_stmt(Token label) {
		printRuleHeader(743, "where-stmt");
		if (label != null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R744
	 * where_construct_stmt
     */
	public void where_construct_stmt(Token id) {
		printRuleHeader(744, "where-construct-stmt");
		if (id != null) printParameter(id, "id");
		printRuleTrailer();
	}

	/** R749
	 * masked_elsewhere_stmt
     */
	public void masked_elsewhere_stmt(Token label, Token id) {
		printRuleHeader(749, "masked-elsewhere-stmt");
		if (label != null) printParameter(label, "label");
		if (id != null) printParameter(id, "id");
		printRuleTrailer();
	}

	/** R750
	 * elsewhere_stmt
     */
	public void elsewhere_stmt(Token label, Token id) {
		printRuleHeader(750, "elsewhere-stmt");
		if (label != null) printParameter(label, "label");
		if (id != null) printParameter(id, "id");
		printRuleTrailer();
	}

	/** R751
	 * end_where_stmt
     */
	public void end_where_stmt(Token label, Token id) {
		printRuleHeader(751, "end-where-stmt");
		if (label != null) printParameter(label, "label");
		if (id != null) printParameter(id, "id");
		printRuleTrailer();
	}

	/** R753
	 * forall_construct_stmt
     */
	public void forall_construct_stmt(Token label, Token id) {
		printRuleHeader(753, "forall-construct-stmt");
		if (label != null) printParameter(label, "label");
		if (id != null) printParameter(id, "id");
		printRuleTrailer();
	}

	/** R755 list
	 * forall_triplet_spec_list
     */
	public void forall_triplet_spec_list__begin() {
		printRuleHeader(755, "forall-triplet-spec-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void forall_triplet_spec_list(int count) {
		printRuleHeader(755, "forall-triplet-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R814 list
	 * case_value_range_list
     */
	public void case_value_range_list__begin() {
		printRuleHeader(814, "case-value-range-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void case_value_range_list(int count) {
		printRuleHeader(814, "case-value-range-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R817 list
	 * association_list
     */
	public void association_list__begin() {
		printRuleHeader(817, "association-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void association_list(int count) {
		printRuleHeader(817, "association-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R843
	 * cycle_stmt
	 */
	public void cycle_stmt(Token label, Token id) {
		printRuleHeader(843, "cycle-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(id, "do-construct-name");
		printRuleTrailer();
	}

	/** R844
	 * exit_stmt
	 */
	public void exit_stmt(Token label, Token id) {
		printRuleHeader(844, "exit-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(id, "do-construct-name");
		printRuleTrailer();
	}

	/** R845
	 * goto_stmt
	 *	:	t_go_to label T_EOS
	 *
	 * @param label The branch target statement label
	 */
	public void goto_stmt(Token label) {
		printRuleHeader(845, "goto-stmt");
		printParameter(label, "label");
		printRuleTrailer();
	}

	/** R846
	 * computed_goto_stmt
	 */
	public void computed_goto_stmt(Token label) {
		printRuleHeader(846, "computed-goto-stmt");
		if (label != null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R847
	 * arithmetic_if_stmt
	 */
	public void arithmetic_if_stmt(Token label, Token label1, Token label2, Token label3) {
		printRuleHeader(847, "arithmetic-if-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(label1, "label1");
		printParameter(label2, "label2");
		printParameter(label3, "label3");
		printRuleTrailer();
	}

	/** R848
	 * continue_stmt
	 *	:	(label)? T_CONTINUE
	 * 
	 * @param label  Optional statement label
	 */
	public void continue_stmt(Token label) {
		printRuleHeader(848, "continue-stmt");
		if (label != null) printParameter(label, "label");
		printRuleTrailer();
	}

	/** R849
	 * stop_stmt
	 */
	public void stop_stmt(Token label, boolean hasStopCode) {
		printRuleHeader(849, "stop-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(hasStopCode, "hasStopCode");
		printRuleTrailer();
	}

	/** R850
	 * stop_code
	 */
	public void stop_code(Token digitString) {
		printRuleHeader(850, "stop-code");
		printParameter(digitString, "digitString");
		printRuleTrailer();
	}

	/** R905 list
	 * connect_spec_list
     */
	public void connect_spec_list__begin() {
		printRuleHeader(905, "connect-spec-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void connect_spec_list(int count) {
		printRuleHeader(905, "connect-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R909 list
	 * close_spec_list
     */
	public void close_spec_list__begin() {
		printRuleHeader(909, "close-spec-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void close_spec_list(int count) {
		printRuleHeader(909, "close-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R911
	 * write_stmt
	 */
	public void write_stmt(Token label, boolean hasOutputList) {
		printRuleHeader(911, "write-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(hasOutputList, "hasOutputList");
		printRuleTrailer();
	}

	/** R913
	 * io_control_spec
	 */
	 public void io_control_spec(boolean hasExpression, Token keyword, boolean hasAsterisk) {
			printRuleHeader(913, "io-control-spec-list");
			printParameter(hasExpression, "hasExpression");
			printParameter(keyword, "keyword");
			printParameter(hasAsterisk, "hasAsterisk");
			printRuleTrailer();		 
	 }
	/** R913 list
	 * io_control_spec_list
     */
	public void io_control_spec_list__begin() {
		printRuleHeader(913, "io-control-spec-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void io_control_spec_list(int count) {
		printRuleHeader(913, "io-control-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R915 list
	 * input_item_list
     */
	public void input_item_list__begin() {
		printRuleHeader(915, "input-item-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void input_item_list(int count) {
		printRuleHeader(915, "input-item-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R916 list
	 * output_item_list
     */
	public void output_item_list__begin() {
		printRuleHeader(916, "output-item-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void output_item_list(int count) {
		printRuleHeader(916, "output-item-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R922 list
	 * wait_spec_list
     */
	public void wait_spec_list__begin() {
		printRuleHeader(922, "wait-spec-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void wait_spec_list(int count) {
		printRuleHeader(922, "wait-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R926 list
	 * position_spec_list
     */
	public void position_spec_list__begin() {
		printRuleHeader(926, "position-spec-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void position_spec_list(int count) {
		printRuleHeader(926, "position-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R928 list
	 * flush_spec_list
     */
	public void flush_spec_list__begin() {
		printRuleHeader(928, "flush-spec-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void flush_spec_list(int count) {
		printRuleHeader(928, "flush-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R930 list
	 * inquire_spec_list
     */
	public void inquire_spec_list__begin() {
		printRuleHeader(930, "inquire-spec-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void inquire_spec_list(int count) {
		printRuleHeader(930, "inquire-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R1003 list
	 * format_item_list
     */
	public void format_item_list__begin() {
		printRuleHeader(1003, "format-item-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void format_item_list(int count) {
		printRuleHeader(1003, "format-item-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R1010 list
	 * v_list_part
	 * v_list
     */
	public void v_list_part(Token plus_minus, Token digitString) {
		printRuleHeader(1010, "v-list-part");
		printParameter(plus_minus, "plus_minus");
		printParameter(digitString, "digitString");
		printRuleTrailer();
	}
	public void v_list__begin() {
		printRuleHeader(1010, "v-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void v_list(int count) {
		printRuleHeader(1010, "v-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R1101
	 * main-program
	 * :	(label)? ...
	 * 
	 * @param label Optional statement label
	 * @param id Optional program name
	 */
	public void main_program__begin() {
		printRuleHeader(1101, "main-program__begin", "begin");
		printRuleTrailer();
	}
	
	public void main_program(boolean hasProgramStmt, boolean hasExecutionPart, boolean hasInternalSubprogramPart) {
		printRuleHeader(1101, "main-program");
		printParameter(hasProgramStmt, "hasProgramStmt");
		printParameter(hasExecutionPart, "hasExecutionPart");
		printParameter(hasInternalSubprogramPart, "hasInternalSubprogramPart");
		printRuleTrailer();
	}
	
	/** R1102
	 * program_stmt
	 * :	(label)? ...
	 * 
	 * @param label Optional statement label
	 * @param id Optional program name
	 */
	public void program_stmt(Token label, Token id) {
		printRuleHeader(1102, "program-stmt");
		if (label != null) printParameter(label, "label");
		printParameter(id, "id");
		printRuleTrailer();
	}

	/** R1103
	 * end-program-stmt
	 * :	(label)? ...
	 * 
	 * @param label Optional statement label
	 * @param id Optional program name
	 */
	public void end_program_stmt(Token label, Token id) {
		printRuleHeader(1103, "end-program-stmt");
		if (label != null) printParameter(label, "label");
		if (id    != null) printParameter(id, "id");
		printRuleTrailer();
	}

	/** R1111 list
	 * rename_list
     */
	public void rename_list__begin() {
		printRuleHeader(1111, "rename-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void rename_list(int count) {
		printRuleHeader(1111, "rename-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R1112 list
	 * only_list
     */
	public void only_list__begin() {
		printRuleHeader(1112, "only-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void only_list(int count) {
		printRuleHeader(1112, "only-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}
	
	/** R1212
	 * proc_interface
	 */
	public void proc_interface(Token id) {
		printRuleHeader(1212, "proc-interface");
		printParameter(id, "id");
		printRuleTrailer();		
	}

	/** R1213
	 * proc_attr_spec
	 */
	public void proc_attr_spec(AttrSpec spec) {
		printRuleHeader(1213, "proc-attr-spec");
		printParameter(spec, "spec");
		printRuleTrailer();		
	}

	/** R1214
	 * proc_decl
	 */
	public void proc_decl(Token id, boolean hasNullInit) {
		printRuleHeader(1214, "proc-decl");
		printParameter(id, "id");
		printParameter(hasNullInit, "hasNullInit");
		printRuleTrailer();		
	}

	/** R1214 list
	 * proc_decl_list
     */
	public void proc_decl_list__begin() {
		printRuleHeader(1214, "proc-decl-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void proc_decl_list(int count) {
		printRuleHeader(1214, "proc-decl-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R1217
	 * function_reference
	 */
	public void function_reference(boolean hasActualArgSpecList) {
		printRuleHeader(1217, "function-reference");
		printParameter(hasActualArgSpecList, "hasActualArgSpecList");
		printRuleTrailer();
	}

	/** R1218
	 * call_stmt
	 */
	public void call_stmt(Token label, boolean hasActualArgSpecList) {
		printRuleHeader(1218, "call-stmt");
		printParameter(hasActualArgSpecList, "hasActualArgSpecList");
		printRuleTrailer();
	}

	/** R1220
	 * actual_arg_spec
	 */
	public void actual_arg_spec(Token keyword) {
		printRuleHeader(1220, "actual-arg-spec");
		printParameter(keyword, "keyword");
		printRuleTrailer();
	}

	/** R1220 list
	 * actual_arg_spec_list
	 */
	public void actual_arg_spec_list__begin() {
		printRuleHeader(1220, "actual-arg-spec-list__begin", "list-begin");
		printRuleTrailer();
	}
	public void actual_arg_spec_list(int count) {
		printRuleHeader(1220, "actual-arg-spec-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

	/** R1221
	 * actual_arg
	 */
	public void actual_arg(boolean hasExpr, Token label) {
		printRuleHeader(1221, "actual-arg");
		printParameter(hasExpr, "hasExpr");
		printParameter(label, "label");
		printRuleTrailer();
	}

	/** R1233
	 * dummy_arg
	 */
	public void dummy_arg(Token dummy) {
		printRuleHeader(1233, "dummy-arg");
		printParameter(dummy, "dummy");
		printRuleTrailer();
	}

	/** R1233 list
	 * dummy_arg_list
     */
	public void dummy_arg_list__begin() {
		printRuleHeader(1233, "dummy-arg-list__begin", "list-begin");
		printRuleTrailer();	
	}
	public void dummy_arg_list(int count) {
		printRuleHeader(1233, "dummy-arg-list", "list");
		printParameter(count, "count");
		printRuleTrailer();
	}

}
