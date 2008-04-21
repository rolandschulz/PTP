package org.eclipse.ptp.lang.fortran.core.parser;

import org.antlr.runtime.Token;

/* The following needed for OFP packaging scheme */
//import fortran.ofp.parser.java.IActionEnums;

public class FortranParserActionNull implements IFortranParserAction {

	public FortranParserActionNull(String[] args, FortranParser parser, String filename) {
		super();
	}

	public void generic_name_list__begin() {
		// Auto-generated method stub
	}

	public void generic_name_list(int count) {
		// Auto-generated method stub
	}

	public void generic_name_list_part(Token ident) {
		// Auto-generated method stub
	}

	public void specification_part(int numUseStmts, int numImportStmts,
														 int numDeclConstructs) {
		// Auto-generated method stub
	}

	public void declaration_construct() {
		// Auto-generated method stub
	}

	public void execution_part() {
		// Auto-generated method stub
	}

	public void execution_part_construct() {
		// Auto-generated method stub
	}

	public void internal_subprogram_part(int count) {
		// Auto-generated method stub
	}

	public void internal_subprogram() {
		// Auto-generated method stub
	}

	public void specification_stmt() {
		// Auto-generated method stub
	}

	public void executable_construct() {
		// Auto-generated method stub
	}

	public void action_stmt() {
		// Auto-generated method stub
	}

	public void keyword() {
		// Auto-generated method stub
	}

	public void name(Token id) {
		// Auto-generated method stub
	}

	public void constant(Token id) {
		// Auto-generated method stub
	}

	public void scalar_constant() {
		// Auto-generated method stub
	}

	public void literal_constant() {
		// Auto-generated method stub
	}

	public void int_constant(Token id) {
		// Auto-generated method stub
	}

	public void char_constant(Token id) {
		// Auto-generated method stub
	}

	public void intrinsic_operator() {
		// Auto-generated method stub
	}

	public void defined_operator(Token definedOp, boolean isExtended) {
		// Auto-generated method stub
	}

	public void extended_intrinsic_op() {
		// Auto-generated method stub
	}

	public void label(Token lbl) {
		// Auto-generated method stub
	}

	public void label_list__begin() {
		// Auto-generated method stub
	}

	public void label_list(int count) {
		// Auto-generated method stub
	}

	public void type_spec() {
		// Auto-generated method stub
	}

	public void type_param_value(boolean hasExpr, boolean hasAsterisk, 
													  boolean hasColon) {
		// Auto-generated method stub
	}

	public void intrinsic_type_spec(Token keyword1, Token keyword2, 
														  int type, boolean hasKindSelector) {
		// Auto-generated method stub
	}

	public void kind_selector(Token token1, Token token2, 
												  boolean hasExpression) {
		// Auto-generated method stub
	}

	 public void signed_int_literal_constant(Token sign) {
		// Auto-generated method stub
	}

	 public void int_literal_constant(Token digitString, 
															 Token kindParam) {
		// Auto-generated method stub
	}

	public void kind_param(Token kind) {
		// Auto-generated method stub
	}

	public void boz_literal_constant(Token constant) {
		// Auto-generated method stub
	}

	 public void signed_real_literal_constant(Token sign) {
		// Auto-generated method stub
	}

	 public void real_literal_constant(Token realConstant, 
                                                    Token kindParam) {
		// Auto-generated method stub
	}

	public void complex_literal_constant() {
		// Auto-generated method stub
	}

	public void real_part(boolean hasIntConstant, 
											 boolean hasRealConstant, Token id) {
		// Auto-generated method stub
	}

	public void imag_part(boolean hasIntConstant, 
											 boolean hasRealConstant, Token id) {
		// Auto-generated method stub
	}

	public void char_selector(Token tk1, Token tk2, int kindOrLen1, 
												  int kindOrLen2, boolean hasAsterisk) {
		// Auto-generated method stub
	}

	public void length_selector(Token len, int kindOrLen, 
													 boolean hasAsterisk) {
		// Auto-generated method stub
	}

	public void char_length(boolean hasTypeParamValue) {
		// Auto-generated method stub
	}

	public void scalar_int_literal_constant() {
		// Auto-generated method stub
	}

	public void char_literal_constant(Token digitString, Token id, 
															 Token str) {
		// Auto-generated method stub
	}

	public void logical_literal_constant(Token logicalValue, 
																 boolean isTrue, 
																 Token kindParam) {
		// Auto-generated method stub
	}

	public void derived_type_def() {
		// Auto-generated method stub
	}

	public void type_param_or_comp_def_stmt(Token eos, int type) {
		// Auto-generated method stub
	}

	public void type_param_or_comp_def_stmt_list() {
		// Auto-generated method stub
	}

	public void derived_type_stmt(Token label, Token keyword, Token id,
														Token eos, 
														boolean hasTypeAttrSpecList,
														boolean hasGenericNameList) {
		// Auto-generated method stub
	}

	public void type_attr_spec(Token keyword, Token id, int specType) {
		// Auto-generated method stub
	}

	public void type_attr_spec_list__begin() {
		// Auto-generated method stub
	}

	public void type_attr_spec_list(int count) {
		// Auto-generated method stub
	}

	public void private_or_sequence() {
		// Auto-generated method stub
	}

	public void end_type_stmt(Token label, Token endKeyword, 
												  Token typeKeyword, Token id, Token eos) {
		// Auto-generated method stub
	}

	public void sequence_stmt(Token label, Token sequenceKeyword, 
												  Token eos) {
		// Auto-generated method stub
	}

	public void type_param_decl(Token id, boolean hasInit) {
		// Auto-generated method stub
	}

	public void type_param_decl_list__begin() {
		// Auto-generated method stub
	}

	public void type_param_decl_list(int count) {
		// Auto-generated method stub
	}

	public void type_param_attr_spec(Token kindOrLen) {
		// Auto-generated method stub
	}

	public void component_def_stmt(int type) {
		// Auto-generated method stub
	}

	public void data_component_def_stmt(Token label, Token eos, 
																boolean hasSpec) {
		// Auto-generated method stub
	}

	public void component_attr_spec(Token attrKeyword, int specType) {
		// Auto-generated method stub
	}

	public void component_attr_spec_list__begin() {
		// Auto-generated method stub
	}

	public void component_attr_spec_list(int count) {
		// Auto-generated method stub
	}

	public void component_decl(Token id, 
													boolean hasComponentArraySpec, 
													boolean hasCoArraySpec, 
													boolean hasCharLength, 
													boolean hasComponentInitialization) {
		// Auto-generated method stub
	}

	public void component_decl_list__begin() {
		// Auto-generated method stub
	}

	public void component_decl_list(int count) {
		// Auto-generated method stub
	}

	public void component_array_spec(boolean isExplicit) {
		// Auto-generated method stub
	}

	public void deferred_shape_spec_list__begin() {
		// Auto-generated method stub
	}

	public void deferred_shape_spec_list(int count) {
		// Auto-generated method stub
	}

	public void component_initialization() {
		// Auto-generated method stub
	}

	public void proc_component_def_stmt(Token label, 
																Token procedureKeyword, 
																Token eos, 
																boolean hasInterface) {
		// Auto-generated method stub
	}

	public void proc_component_attr_spec(Token attrSpecKeyword, 
																 Token id, int specType) {
		// Auto-generated method stub
	}

	public void proc_component_attr_spec_list__begin() {
		// Auto-generated method stub
	}

	public void proc_component_attr_spec_list(int count) {
		// Auto-generated method stub
	}

	public void private_components_stmt(Token label, 
																Token privateKeyword, 
																Token eos) {
		// Auto-generated method stub
	}

	public 
	void type_bound_procedure_part(int count, boolean hasBindingPrivateStmt) {
		// Auto-generated method stub
	}

	public void binding_private_stmt(Token label, 
															Token privateKeyword, Token eos) {
		// Auto-generated method stub
	}

	public void proc_binding_stmt(Token label, int type, Token eos) {
		// Auto-generated method stub
	}

	public void specific_binding(Token procedureKeyword, 
													  Token interfaceName, 
													  Token bindingName, 
													  Token procedureName, 
													  boolean hasBindingAttrList) {
		// Auto-generated method stub
	}

	public void generic_binding(Token genericKeyword, 
													 boolean hasAccessSpec) {
		// Auto-generated method stub
	}

	public void binding_attr(Token bindingAttr, int attr, Token id) {
		// Auto-generated method stub
	}

	public void binding_attr_list__begin() {
		// Auto-generated method stub
	}

	public void binding_attr_list(int count) {
		// Auto-generated method stub
	}

	public void final_binding(Token finalKeyword) {
		// Auto-generated method stub
	}

	public void derived_type_spec(Token typeName, 
														boolean hasTypeParamSpecList) {
		// Auto-generated method stub
	}

	public void type_param_spec(Token keyword) {
		// Auto-generated method stub
	}

	public void type_param_spec_list__begin() {
		// Auto-generated method stub
	}

	public void type_param_spec_list(int count) {
		// Auto-generated method stub
	}

	public void structure_constructor(Token id) {
		// Auto-generated method stub
	}

	public void component_spec(Token id) {
		// Auto-generated method stub
	}

	public void component_spec_list__begin() {
		// Auto-generated method stub
	}

	public void component_spec_list(int count) {
		// Auto-generated method stub
	}

	public void component_data_source() {
		// Auto-generated method stub
	}

	public void enum_def(int numEls) {
		// Auto-generated method stub
	}

	public void enum_def_stmt(Token label, Token enumKeyword, 
												  Token bindKeyword, Token id, Token eos) {
		// Auto-generated method stub
	}

	public void enumerator_def_stmt(Token label, 
														  Token enumeratorKeyword, 
														  Token eos) {
		// Auto-generated method stub
	}

	public void enumerator(Token id, boolean hasExpr) {
		// Auto-generated method stub
	}

	public void enumerator_list__begin() {
		// Auto-generated method stub
	}

	public void enumerator_list(int count) {
		// Auto-generated method stub
	}

	public void end_enum_stmt(Token label, Token endKeyword, 
												  Token enumKeyword, Token eos) {
		// Auto-generated method stub
	}

	public void array_constructor() {
		// Auto-generated method stub
	}

	public void ac_spec() {
		// Auto-generated method stub
	}

	public void ac_value() {
		// Auto-generated method stub
	}

	public void ac_value_list__begin() {
		// Auto-generated method stub
	}

	public void ac_value_list(int count) {
		// Auto-generated method stub
	}

	public void ac_implied_do() {
		// Auto-generated method stub
	}

	public void ac_implied_do_control(boolean hasStride) {
		// Auto-generated method stub
	}

	public void scalar_int_variable() {
		// Auto-generated method stub
	}

	public void type_declaration_stmt(Token label, int numAttributes, 
															 Token eos) {
		// Auto-generated method stub
	}

	public void declaration_type_spec(Token udtKeyword, int type) {
		// Auto-generated method stub
	}

	public void attr_spec(Token attrKeyword, int attr) {
		// Auto-generated method stub
	}

	public void entity_decl(Token id) {
		// Auto-generated method stub
	}

	public void entity_decl_list__begin() {
		// Auto-generated method stub
	}

	public void entity_decl_list(int count) {
		// Auto-generated method stub
	}

	public void initialization(boolean hasExpr, boolean hasNullInit) {
		// Auto-generated method stub
	}

	public void null_init(Token id) {
		// Auto-generated method stub
	}

	public void access_spec(Token keyword, int type) {
		// Auto-generated method stub
	}

	public void language_binding_spec(Token keyword, Token id, 
															 boolean hasName) {
		// Auto-generated method stub
	}

	public void array_spec(int count) {
		// Auto-generated method stub
	}

	public void array_spec_element(int type) {
		// Auto-generated method stub
	}

	public void explicit_shape_spec(boolean hasUpperBound) {
		// Auto-generated method stub
	}

	public void explicit_shape_spec_list__begin() {
		// Auto-generated method stub
	}

	public void explicit_shape_spec_list(int count) {
		// Auto-generated method stub
	}

	public void co_array_spec() {
		// Auto-generated method stub
	}

	public void intent_spec(Token intentKeyword1, 
												Token intentKeyword2, int intent) {
		// Auto-generated method stub
	}

	public void access_stmt(Token label, Token eos, boolean hasList) {
		// Auto-generated method stub
	}

	public void deferred_co_shape_spec() {
		// Auto-generated method stub
	}

	public void deferred_co_shape_spec_list__begin() {
		// Auto-generated method stub
	}

	public void deferred_co_shape_spec_list(int count) {
		// Auto-generated method stub
	}

	public void explicit_co_shape_spec() {
		// Auto-generated method stub
	}

	public void explicit_co_shape_spec_suffix() {
		// Auto-generated method stub
	}

	public void access_id() {
		// Auto-generated method stub
	}

	public void access_id_list__begin() {
		// Auto-generated method stub
	}

	public void access_id_list(int count) {
		// Auto-generated method stub
	}

	public void allocatable_stmt(Token label, Token keyword, Token eos,
													  int count) {
		// Auto-generated method stub
	}

	public void allocatable_decl(Token id, boolean hasArraySpec, 
													  boolean hasCoArraySpec) {
		// Auto-generated method stub
	}

	public void asynchronous_stmt(Token label, Token keyword, 
														Token eos) {
		// Auto-generated method stub
	}

	public void bind_stmt(Token label, Token eos) {
		// Auto-generated method stub
	}

	public void bind_entity(Token entity, boolean isCommonBlockName) {
		// Auto-generated method stub
	}

	public void bind_entity_list__begin() {
		// Auto-generated method stub
	}

	public void bind_entity_list(int count) {
		// Auto-generated method stub
	}

	public void data_stmt(Token label, Token keyword, Token eos, 
											 int count) {
		// Auto-generated method stub
	}

	public void data_stmt_set() {
		// Auto-generated method stub
	}

	public void data_stmt_object() {
		// Auto-generated method stub
	}

	public void data_stmt_object_list__begin() {
		// Auto-generated method stub
	}

	public void data_stmt_object_list(int count) {
		// Auto-generated method stub
	}

	public void data_implied_do(Token id, boolean hasThirdExpr) {
		// Auto-generated method stub
	}

	public void data_i_do_object() {
		// Auto-generated method stub
	}

	public void data_i_do_object_list__begin() {
		// Auto-generated method stub
	}

	public void data_i_do_object_list(int count) {
		// Auto-generated method stub
	}

	public void data_stmt_value(Token asterisk) {
		// Auto-generated method stub
	}

	public void data_stmt_value_list__begin() {
		// Auto-generated method stub
	}

	public void data_stmt_value_list(int count) {
		// Auto-generated method stub
	}

	public void scalar_int_constant() {
		// Auto-generated method stub
	}

	public void hollerith_constant(Token hollerithConstant) {
		// Auto-generated method stub
	}

	public void data_stmt_constant() {
		// Auto-generated method stub
	}

	public void dimension_stmt(Token label, Token keyword, Token eos, 
													int count) {
		// Auto-generated method stub
	}

	public void dimension_decl(Token id, boolean hasArraySpec, 
													boolean hasCoArraySpec) {
		// Auto-generated method stub
	}

	public void dimension_spec(Token dimensionKeyword) {
		// Auto-generated method stub
	}

	public void intent_stmt(Token label, Token keyword, Token eos) {
		// Auto-generated method stub
	}

	public void optional_stmt(Token label, Token keyword, Token eos) {
		// Auto-generated method stub
	}

	public void parameter_stmt(Token label, Token keyword, Token eos) {
		// Auto-generated method stub
	}

	public void named_constant_def_list__begin() {
		// Auto-generated method stub
	}

	public void named_constant_def_list(int count) {
		// Auto-generated method stub
	}

	public void named_constant_def(Token id) {
		// Auto-generated method stub
	}

	public void pointer_stmt(Token label, Token keyword, Token eos) {
		// Auto-generated method stub
	}

	public void pointer_decl_list__begin() {
		// Auto-generated method stub
	}

	public void pointer_decl_list(int count) {
		// Auto-generated method stub
	}

	public void pointer_decl(Token id, boolean hasSpecList) {
		// Auto-generated method stub
	}

	public void protected_stmt(Token label, Token keyword, Token eos) {
		// Auto-generated method stub
	}

	public void save_stmt(Token label, Token keyword, Token eos, 
											 boolean hasSavedEntityList) {
		// Auto-generated method stub
	}

	public void saved_entity_list__begin() {
		// Auto-generated method stub
	}

	public void saved_entity_list(int count) {
		// Auto-generated method stub
	}

	public void saved_entity(Token id, boolean isCommonBlockName) {
		// Auto-generated method stub
	}

	public void target_stmt(Token label, Token keyword, Token eos, 
												int count) {
		// Auto-generated method stub
	}

	public void target_decl(Token id, boolean hasArraySpec, 
												boolean hasCoArraySpec) {
		// Auto-generated method stub
	}

	public void value_stmt(Token label, Token keyword, Token eos) {
		// Auto-generated method stub
	}

	public void volatile_stmt(Token label, Token keyword, Token eos) {
		// Auto-generated method stub
	}

	public void implicit_stmt(Token label, Token implicitKeyword, 
												  Token noneKeyword, Token eos, 
												  boolean hasImplicitSpecList) {
		// Auto-generated method stub
	}

	public void implicit_spec() {
		// Auto-generated method stub
	}

	public void implicit_spec_list__begin() {
		// Auto-generated method stub
	}

	public void implicit_spec_list(int count) {
		// Auto-generated method stub
	}

	public void letter_spec(Token id1, Token id2) {
		// Auto-generated method stub
	}

	public void letter_spec_list__begin() {
		// Auto-generated method stub
	}

	public void letter_spec_list(int count) {
		// Auto-generated method stub
	}

	public void namelist_stmt(Token label, Token keyword, Token eos, 
												  int count) {
		// Auto-generated method stub
	}

	public void namelist_group_name(Token id) {
		// Auto-generated method stub
	}

	public void namelist_group_object(Token id) {
		// Auto-generated method stub
	}

	public void namelist_group_object_list__begin() {
		// Auto-generated method stub
	}

	public void namelist_group_object_list(int count) {
		// Auto-generated method stub
	}

	public void equivalence_stmt(Token label, 
													  Token equivalenceKeyword, Token eos) {
		// Auto-generated method stub
	}

	public void equivalence_set() {
		// Auto-generated method stub
	}

	public void equivalence_set_list__begin() {
		// Auto-generated method stub
	}

	public void equivalence_set_list(int count) {
		// Auto-generated method stub
	}

	public void equivalence_object() {
		// Auto-generated method stub
	}

	public void equivalence_object_list__begin() {
		// Auto-generated method stub
	}

	public void equivalence_object_list(int count) {
		// Auto-generated method stub
	}

	public void common_stmt(Token label, Token commonKeyword, 
												Token eos, int numBlocks) {
		// Auto-generated method stub
	}

	public void common_block_name(Token id) {
		// Auto-generated method stub
	}

	public void common_block_object_list__begin() {
		// Auto-generated method stub
	}

	public void common_block_object_list(int count) {
		// Auto-generated method stub
	}

	public void common_block_object(Token id, 
														  boolean hasShapeSpecList) {
		// Auto-generated method stub
	}

	public void variable() {
		// Auto-generated method stub
	}

	public void designator(boolean hasSubstringRange) {
		// Auto-generated method stub
	}

	public void designator_or_func_ref() {
		// Auto-generated method stub
	}

	public void substring_range_or_arg_list() {
		// Auto-generated method stub
	}

	public void substr_range_or_arg_list_suffix() {
		// Auto-generated method stub
	}

	public void logical_variable() {
		// Auto-generated method stub
	}

	public void default_logical_variable() {
		// Auto-generated method stub
	}

	public void scalar_default_logical_variable() {
		// Auto-generated method stub
	}

	public void char_variable() {
		// Auto-generated method stub
	}

	public void default_char_variable() {
		// Auto-generated method stub
	}

	public void scalar_default_char_variable() {
		// Auto-generated method stub
	}

	public void int_variable() {
		// Auto-generated method stub
	}

	public void substring(boolean hasSubstringRange) {
		// Auto-generated method stub
	}

	public void substring_range(boolean hasLowerBound, 
													 boolean hasUpperBound) {
		// Auto-generated method stub
	}

	public void data_ref(int numPartRef) {
		// Auto-generated method stub
	}

	public void part_ref(Token id, boolean hasSelectionSubscriptList, 
											boolean hasImageSelector) {
		// Auto-generated method stub
	}

	public void section_subscript(boolean hasLowerBound, 
														boolean hasUpperBound, 
														boolean hasStride, 
														boolean isAmbiguous) {
		// Auto-generated method stub
	}

	public void section_subscript_list__begin() {
		// Auto-generated method stub
	}

	public void section_subscript_list(int count) {
		// Auto-generated method stub
	}

	public void vector_subscript() {
		// Auto-generated method stub
	}

	public void allocate_stmt(Token label, Token allocateKeyword, 
												  Token eos, boolean hasTypeSpec, 
												  boolean hasAllocOptList) {
		// Auto-generated method stub
	}

	public void image_selector(int exprCount) {
		// Auto-generated method stub
	}

	public void alloc_opt(Token allocOpt) {
		// Auto-generated method stub
	}

	public void alloc_opt_list__begin() {
		// Auto-generated method stub
	}

	public void alloc_opt_list(int count) {
		// Auto-generated method stub
	}

	public void allocation(boolean hasAllocateShapeSpecList, 
											  boolean hasAllocateCoArraySpec) {
		// Auto-generated method stub
	}

	public void allocation_list__begin() {
		// Auto-generated method stub
	}

	public void allocation_list(int count) {
		// Auto-generated method stub
	}

	public void allocate_object() {
		// Auto-generated method stub
	}

	public void allocate_object_list__begin() {
		// Auto-generated method stub
	}

	public void allocate_object_list(int count) {
		// Auto-generated method stub
	}

	public void allocate_shape_spec(boolean hasLowerBound, 
														  boolean hasUpperBound) {
		// Auto-generated method stub
	}

	public void allocate_shape_spec_list__begin() {
		// Auto-generated method stub
	}

	public void allocate_shape_spec_list(int count) {
		// Auto-generated method stub
	}

	public void nullify_stmt(Token label, Token nullifyKeyword, 
												 Token eos) {
		// Auto-generated method stub
	}

	public void pointer_object() {
		// Auto-generated method stub
	}

	public void pointer_object_list__begin() {
		// Auto-generated method stub
	}

	public void pointer_object_list(int count) {
		// Auto-generated method stub
	}

	public void deallocate_stmt(Token label, Token deallocateKeyword, 
													 Token eos, boolean hasDeallocOptList) {
		// Auto-generated method stub
	}

	public void dealloc_opt(Token id) {
		// Auto-generated method stub
	}

	public void dealloc_opt_list__begin() {
		// Auto-generated method stub
	}

	public void dealloc_opt_list(int count) {
		// Auto-generated method stub
	}

	public void allocate_co_array_spec() {
		// Auto-generated method stub
	}

	public void allocate_co_shape_spec(boolean hasExpr) {
		// Auto-generated method stub
	}

	public void allocate_co_shape_spec_list__begin() {
		// Auto-generated method stub
	}

	public void allocate_co_shape_spec_list(int count) {
		// Auto-generated method stub
	}

	public void primary() {
		// Auto-generated method stub
	}

	public void level_1_expr(Token definedUnaryOp) {
		// Auto-generated method stub
	}

	public void defined_unary_op(Token definedOp) {
		// Auto-generated method stub
	}

	public void power_operand(boolean hasPowerOperand) {
		// Auto-generated method stub
	}

	public void power_operand__power_op(Token powerOp) {
		// Auto-generated method stub
	}

	public void mult_operand(int numMultOps) {
		// Auto-generated method stub
	}

	public void mult_operand__mult_op(Token multOp) {
		// Auto-generated method stub
	}

	public void add_operand(Token addOp, int numAddOps) {
		// Auto-generated method stub
	}

	public void add_operand__add_op(Token addOp) {
		// Auto-generated method stub
	}

	public void level_2_expr(int numConcatOps) {
		// Auto-generated method stub
	}

	public void power_op(Token powerKeyword) {
		// Auto-generated method stub
	}

	public void mult_op(Token multKeyword) {
		// Auto-generated method stub
	}

	public void add_op(Token addKeyword) {
		// Auto-generated method stub
	}

	public void level_3_expr(Token relOp) {
		// Auto-generated method stub
	}

	public void concat_op(Token concatKeyword) {
		// Auto-generated method stub
	}

	public void rel_op(Token relOp) {
		// Auto-generated method stub
	}

	public void and_operand(boolean hasNotOp, int numAndOps) {
		// Auto-generated method stub
	}

	public void and_operand__not_op(boolean hasNotOp) {
		// Auto-generated method stub
	}

	public void or_operand(int numOrOps) {
		// Auto-generated method stub
	}

	public void equiv_operand(int numEquivOps) {
		// Auto-generated method stub
	}

	public void equiv_operand__equiv_op(Token equivOp) {
		// Auto-generated method stub
	}

	public void level_5_expr(int numDefinedBinaryOps) {
		// Auto-generated method stub
	}

	public void level_5_expr__defined_binary_op(Token definedBinaryOp) {
		// Auto-generated method stub
	}

	public void not_op(Token notOp) {
		// Auto-generated method stub
	}

	public void and_op(Token andOp) {
		// Auto-generated method stub
	}

	public void or_op(Token orOp) {
		// Auto-generated method stub
	}

	public void equiv_op(Token equivOp) {
		// Auto-generated method stub
	}

	public void expr() {
		// Auto-generated method stub
	}

	public void defined_binary_op(Token binaryOp) {
		// Auto-generated method stub
	}

	public void assignment_stmt(Token label, Token eos) {
		// Auto-generated method stub
	}

	public void pointer_assignment_stmt(Token label, Token eos, 
																boolean hasBoundsSpecList, 
																boolean hasBRList) {
		// Auto-generated method stub
	}

	public void data_pointer_object() {
		// Auto-generated method stub
	}

	public void bounds_spec() {
		// Auto-generated method stub
	}

	public void bounds_spec_list__begin() {
		// Auto-generated method stub
	}

	public void bounds_spec_list(int count) {
		// Auto-generated method stub
	}

	public void bounds_remapping() {
		// Auto-generated method stub
	}

	public void bounds_remapping_list__begin() {
		// Auto-generated method stub
	}

	public void bounds_remapping_list(int count) {
		// Auto-generated method stub
	}

	public void proc_pointer_object() {
		// Auto-generated method stub
	}

	public void where_stmt__begin() {
		// Auto-generated method stub
	}

	public void where_stmt(Token label, Token whereKeyword) {
		// Auto-generated method stub
	}

	public void where_construct(int numConstructs, 
													 boolean hasMaskedElsewhere, 
													 boolean hasElsewhere) {
		// Auto-generated method stub
	}

	public void where_construct_stmt(Token id, Token whereKeyword, 
															Token eos) {
		// Auto-generated method stub
	}

	public void where_body_construct() {
		// Auto-generated method stub
	}

	public void masked_elsewhere_stmt(Token label, Token elseKeyword, 
															 Token whereKeyword, Token id,
															 Token eos) {
		// Auto-generated method stub
	}

    public void masked_elsewhere_stmt__end(int numBodyConstructs) {
		// Auto-generated method stub
	}

	public void elsewhere_stmt(Token label, Token elseKeyword, 
													Token whereKeyword, Token id, 
													Token eos) {
		// Auto-generated method stub
	}

    public void elsewhere_stmt__end(int numBodyConstructs) {
		// Auto-generated method stub
	}

	public void end_where_stmt(Token label, Token endKeyword, 
													Token whereKeyword, Token id, 
													Token eos) {
		// Auto-generated method stub
	}

	public void forall_construct() {
		// Auto-generated method stub
	}

	public void forall_construct_stmt(Token label, Token id, 
															 Token forallKeyword, Token eos) {
		// Auto-generated method stub
	}

	public void forall_header() {
		// Auto-generated method stub
	}

	public void forall_triplet_spec(Token id, boolean hasStride) {
		// Auto-generated method stub
	}

	public void forall_triplet_spec_list__begin() {
		// Auto-generated method stub
	}

	public void forall_triplet_spec_list(int count) {
		// Auto-generated method stub
	}

	public void forall_body_construct() {
		// Auto-generated method stub
	}

	public void forall_assignment_stmt(boolean isPointerAssignment) {
		// Auto-generated method stub
	}

	public void end_forall_stmt(Token label, Token endKeyword, 
													 Token forallKeyword, Token id, 
													 Token eos) {
		// Auto-generated method stub
	}

	public void forall_stmt__begin() {
		// Auto-generated method stub
	}
 
	public void forall_stmt(Token label, Token forallKeyword) {
		// Auto-generated method stub
	}

	public void block() {
		// Auto-generated method stub
	}

	public void if_construct() {
		// Auto-generated method stub
	}

	public void if_then_stmt(Token label, Token id, Token ifKeyword, 
												 Token thenKeyword, Token eos) {
		// Auto-generated method stub
	}

	public void else_if_stmt(Token label, Token elseKeyword, 
												 Token ifKeyword, Token thenKeyword, 
												 Token id, Token eos) {
		// Auto-generated method stub
	}

	public void else_stmt(Token label, Token elseKeyword, Token id, 
											 Token eos) {
		// Auto-generated method stub
	}

	public void end_if_stmt(Token label, Token endKeyword, 
												Token ifKeyword, Token id, Token eos) {
		// Auto-generated method stub
	}

	public void if_stmt__begin() {
		// Auto-generated method stub
	}

	public void if_stmt(Token label, Token ifKeyword) {
		// Auto-generated method stub
	}

	public void case_construct() {
		// Auto-generated method stub
	}

	public void select_case_stmt(Token label, Token id, 
													  Token selectKeyword, 
													  Token caseKeyword, Token eos) {
		// Auto-generated method stub
	}

	public void case_stmt(Token label, Token caseKeyword, 
											 Token id, Token eos) {
		// Auto-generated method stub
	}

	public void end_select_stmt(Token label, Token endKeyword, 
													 Token selectKeyword, Token id, 
													 Token eos) {
		// Auto-generated method stub
	}

	public void case_selector(Token defaultToken) {
		// Auto-generated method stub
	}

	public void case_value_range() {
		// Auto-generated method stub
	}

	public void case_value_range_list__begin() {
		// Auto-generated method stub
	}

	public void case_value_range_list(int count) {
		// Auto-generated method stub
	}

	public void case_value_range_suffix() {
		// Auto-generated method stub
	}

	public void case_value() {
		// Auto-generated method stub
	}

	public void associate_construct() {
		// Auto-generated method stub
	}

	public void associate_stmt(Token label, Token id, 
													Token associateKeyword, Token eos) {
		// Auto-generated method stub
	}

	public void association_list__begin() {
		// Auto-generated method stub
	}

	public void association_list(int count) {
		// Auto-generated method stub
	}

	public void association(Token id) {
		// Auto-generated method stub
	}

	public void selector() {
		// Auto-generated method stub
	}

	public void end_associate_stmt(Token label, Token endKeyword, 
														 Token associateKeyword, Token id, 
														 Token eos) {
		// Auto-generated method stub
	}

	public void select_type_construct() {
		// Auto-generated method stub
	}

	public void select_type_stmt(Token label, 
													  Token selectConstructName, 
													  Token associateName, Token eos) {
		// Auto-generated method stub
	}

	public void select_type(Token selectKeyword, Token typeKeyword) {
		// Auto-generated method stub
	}

	public void type_guard_stmt(Token label, Token typeKeyword, 
													 Token isOrDefaultKeyword, 
													 Token selectConstructName, Token eos) {
		// Auto-generated method stub
	}

	public void end_select_type_stmt(Token label, Token endKeyword, 
															Token selectKeyword, Token id, 
															Token eos) {
		// Auto-generated method stub
	}

	public void do_construct() {
		// Auto-generated method stub
	}

	public void block_do_construct() {
		// Auto-generated method stub
	}

	public void do_stmt(Token label, Token id, Token doKeyword, 
										  Token digitString, Token eos, 
										  boolean hasLoopControl) {
		// Auto-generated method stub
	}

	public void label_do_stmt(Token label, Token id, Token doKeyword, 
												  Token digitString, Token eos, 
												  boolean hasLoopControl) {
		// Auto-generated method stub
	}

	public void loop_control(Token whileKeyword, boolean hasOptExpr) {
		// Auto-generated method stub
	}

	public void do_variable() {
		// Auto-generated method stub
	}

	public void end_do() {
		// Auto-generated method stub
	}

	public void end_do_stmt(Token label, Token endKeyword, 
												Token doKeyword, Token id, Token eos) {
		// Auto-generated method stub
	}

	public void do_term_action_stmt(Token label, Token endKeyword, 
														  Token doKeyword, Token id, 
														  Token eos) {
		// Auto-generated method stub
	}

	public void cycle_stmt(Token label, Token cycleKeyword, Token id, 
											  Token eos) {
		// Auto-generated method stub
	}

	public void exit_stmt(Token label, Token exitKeyword, Token id, 
											 Token eos) {
		// Auto-generated method stub
	}

	public void goto_stmt(Token goKeyword, Token toKeyword, 
											 Token label, Token eos) {
		// Auto-generated method stub
	}

	public void computed_goto_stmt(Token label, Token goKeyword, 
														 Token toKeyword, Token eos) {
		// Auto-generated method stub
	}

	public void assign_stmt(Token label1, Token assignKeyword, 
												Token label2, Token toKeyword, Token name, 
												Token eos) {
		// Auto-generated method stub
	}

	public void assigned_goto_stmt(Token label, Token goKeyword, 
														 Token toKeyword, Token name, 
														 Token eos) {
		// Auto-generated method stub
	}

	public void stmt_label_list() {
		// Auto-generated method stub
	}

	public void pause_stmt(Token label, Token pauseKeyword, 
											  Token constant, Token eos) {
		// Auto-generated method stub
	}

	public void arithmetic_if_stmt(Token label, Token ifKeyword, 
														 Token label1, Token label2, 
														 Token label3, Token eos) {
		// Auto-generated method stub
	}

	public void continue_stmt(Token label, Token continueKeyword, 
												  Token eos) {
		// Auto-generated method stub
	}

	public void stop_stmt(Token label, Token stopKeyword, 
											 Token eos, boolean hasStopCode) {
		// Auto-generated method stub
	}

	public void stop_code(Token digitString) {
		// Auto-generated method stub
	}

	public void scalar_char_constant() {
		// Auto-generated method stub
	}

	public void io_unit() {
		// Auto-generated method stub
	}

	public void file_unit_number() {
		// Auto-generated method stub
	}

	public void open_stmt(Token label, Token openKeyword, Token eos) {
		// Auto-generated method stub
	}

	public void connect_spec(Token id) {
		// Auto-generated method stub
	}

	public void connect_spec_list__begin() {
		// Auto-generated method stub
	}

	public void connect_spec_list(int count) {
		// Auto-generated method stub
	}

	public void close_stmt(Token label, Token closeKeyword, Token eos) {
		// Auto-generated method stub
	}

	public void close_spec(Token closeSpec) {
		// Auto-generated method stub
	}

	public void close_spec_list__begin() {
		// Auto-generated method stub
	}

	public void close_spec_list(int count) {
		// Auto-generated method stub
	}

	public void read_stmt(Token label, Token readKeyword, 
											 Token eos, boolean hasInputItemList) {
		// Auto-generated method stub
	}

	public void write_stmt(Token label, Token writeKeyword, 
											  Token eos, boolean hasOutputItemList) {
		// Auto-generated method stub
	}

	public void print_stmt(Token label, Token printKeyword, 
											  Token eos, boolean hasOutputItemList) {
		// Auto-generated method stub
	}

	 public void io_control_spec(boolean hasExpression, 
													  Token keyword, boolean hasAsterisk) {
		// Auto-generated method stub
	}

	public void io_control_spec_list__begin() {
		// Auto-generated method stub
	}

	public void io_control_spec_list(int count) {
		// Auto-generated method stub
	}

	public void format() {
		// Auto-generated method stub
	}

	public void input_item() {
		// Auto-generated method stub
	}

	public void input_item_list__begin() {
		// Auto-generated method stub
	}

	public void input_item_list(int count) {
		// Auto-generated method stub
	}

	public void output_item() {
		// Auto-generated method stub
	}

	public void output_item_list__begin() {
		// Auto-generated method stub
	}

	public void output_item_list(int count) {
		// Auto-generated method stub
	}

	public void io_implied_do() {
		// Auto-generated method stub
	}

	public void io_implied_do_object() {
		// Auto-generated method stub
	}

	public void io_implied_do_control() {
		// Auto-generated method stub
	}

	public void dtv_type_spec(Token typeKeyword) {
		// Auto-generated method stub
	}

	public void wait_stmt(Token label, Token waitKeyword, Token eos) {
		// Auto-generated method stub
	}

	public void wait_spec(Token id) {
		// Auto-generated method stub
	}

	public void wait_spec_list__begin() {
		// Auto-generated method stub
	}

	public void wait_spec_list(int count) {
		// Auto-generated method stub
	}

	public void backspace_stmt(Token label, Token backspaceKeyword, 
													Token eos, boolean hasPositionSpecList) {
		// Auto-generated method stub
	}

	public void endfile_stmt(Token label, Token endKeyword, 
												 Token fileKeyword, Token eos, 
												 boolean hasPositionSpecList) {
		// Auto-generated method stub
	}

	public void rewind_stmt(Token label, Token rewindKeyword, 
												Token eos, boolean hasPositionSpecList) {
		// Auto-generated method stub
	}

	public void position_spec(Token id) {
		// Auto-generated method stub
	}

	public void position_spec_list__begin() {
		// Auto-generated method stub
	}

	public void position_spec_list(int count) {
		// Auto-generated method stub
	}

	public void flush_stmt(Token label, Token flushKeyword, 
											  Token eos, boolean hasFlushSpecList) {
		// Auto-generated method stub
	}

	public void flush_spec(Token id) {
		// Auto-generated method stub
	}

	public void flush_spec_list__begin() {
		// Auto-generated method stub
	}

	public void flush_spec_list(int count) {
		// Auto-generated method stub
	}

	public void inquire_stmt(Token label, Token inquireKeyword, 
												 Token id, Token eos, boolean isType2) {
		// Auto-generated method stub
	}

	public void inquire_spec(Token id) {
		// Auto-generated method stub
	}

	public void inquire_spec_list__begin() {
		// Auto-generated method stub
	}

	public void inquire_spec_list(int count) {
		// Auto-generated method stub
	}

	public void format_stmt(Token label, Token formatKeyword, 
												Token eos) {
		// Auto-generated method stub
	}

	public void format_specification(boolean hasFormatItemList) {
		// Auto-generated method stub
	}

	public void format_item(Token descOrDigit, 
												boolean hasFormatItemList) {
		// Auto-generated method stub
	}

	public void format_item_list__begin() {
		// Auto-generated method stub
	}

	public void format_item_list(int count) {
		// Auto-generated method stub
	}

	public void v_list_part(Token plus_minus, Token digitString) {
		// Auto-generated method stub
	}

	public void v_list__begin() {
		// Auto-generated method stub
	}

	public void v_list(int count) {
		// Auto-generated method stub
	}

	public void main_program__begin() {
		// Auto-generated method stub
	}

	public void main_program(boolean hasProgramStmt, 
												 boolean hasExecutionPart, 
												 boolean hasInternalSubprogramPart) {
		// Auto-generated method stub
	}

	public void ext_function_subprogram(boolean hasPrefix) {
		// Auto-generated method stub
	}

	public void program_stmt(Token label, Token programKeyword, 
												 Token id, Token eos) {
		// Auto-generated method stub
	}

	public void end_program_stmt(Token label, Token endKeyword, 
													  Token programKeyword, Token id, 
													  Token eos) {
		// Auto-generated method stub
	}

	public void module() {
		// Auto-generated method stub
	}

	public void module_stmt__begin() {
		// Auto-generated method stub
	}

	public void module_stmt(Token label, Token moduleKeyword, 
												Token id, Token eos) {
		// Auto-generated method stub
	}

	public void end_module_stmt(Token label, Token endKeyword, 
													 Token moduleKeyword, Token id, 
													 Token eos) {
		// Auto-generated method stub
	}

	public void module_subprogram_part() {
		// Auto-generated method stub
	}

	public void module_subprogram(boolean hasPrefix) {
		// Auto-generated method stub
	}

	public void use_stmt(Token label, Token useKeyword, Token id, 
											Token onlyKeyword, Token eos, 
											boolean hasModuleNature,
											boolean hasRenameList, boolean hasOnly) {
		// Auto-generated method stub
	}

	public void module_nature(Token nature) {
		// Auto-generated method stub
	}

	public void rename(Token id1, Token id2, Token op1, Token defOp1, 
										 Token op2, Token defOp2) {
		// Auto-generated method stub
	}

	public void rename_list__begin() {
		// Auto-generated method stub
	}

	public void rename_list(int count) {
		// Auto-generated method stub
	}

	public void only() {
		// Auto-generated method stub
	}

	public void only_list__begin() {
		// Auto-generated method stub
	}

	public void only_list(int count) {
		// Auto-generated method stub
	}

	public void block_data() {
		// Auto-generated method stub
	}

	public void block_data_stmt__begin() {
		// Auto-generated method stub
	}

	public void block_data_stmt(Token label, Token blockKeyword, 
													 Token dataKeyword, Token id, 
													 Token eos) {
		// Auto-generated method stub
	}

	public void end_block_data_stmt(Token label, Token endKeyword, 
														  Token blockKeyword, 
														  Token dataKeyword, Token id, 
														  Token eos) {
		// Auto-generated method stub
	}

	public void interface_block() {
		// Auto-generated method stub
	}

	public void interface_specification() {
		// Auto-generated method stub
	}

	public void interface_stmt__begin() {
		// Auto-generated method stub
	}

	public void interface_stmt(Token label, Token abstractToken, 
													Token keyword, Token eos, 
													boolean hasGenericSpec) {
		// Auto-generated method stub
	}

	public void end_interface_stmt(Token label, Token kw1, Token kw2, 
														 Token eos, boolean hasGenericSpec) {
		// Auto-generated method stub
	}

	public void interface_body(boolean hasPrefix) {
		// Auto-generated method stub
	}

	public void procedure_stmt(Token label, Token module, 
													Token procedureKeyword, Token eos) {
		// Auto-generated method stub
	}

	 public void generic_spec(Token keyword, Token name, int type) {
		// Auto-generated method stub
	}

	public void dtio_generic_spec(Token rw, Token format, int type) {
		// Auto-generated method stub
	}

	public void import_stmt(Token label, Token importKeyword, 
												Token eos, boolean hasGenericNameList) {
		// Auto-generated method stub
	}

	public void external_stmt(Token label, Token externalKeyword, 
												  Token eos) {
		// Auto-generated method stub
	}

	public void procedure_declaration_stmt(Token label, 
																	Token procedureKeyword, 
																	Token eos, 
																	boolean hasProcInterface, 
																	int count) {
		// Auto-generated method stub
	}

	public void proc_interface(Token id) {
		// Auto-generated method stub
	}

	public void proc_attr_spec(Token attrKeyword, Token id, int spec) {
		// Auto-generated method stub
	}

	public void proc_decl(Token id, boolean hasNullInit) {
		// Auto-generated method stub
	}

	public void proc_decl_list__begin() {
		// Auto-generated method stub
	}

	public void proc_decl_list(int count) {
		// Auto-generated method stub
	}

	public void intrinsic_stmt(Token label, Token intrinsicToken, 
													Token eos) {
		// Auto-generated method stub
	}

	public void function_reference(boolean hasActualArgSpecList) {
		// Auto-generated method stub
	}

	public void call_stmt(Token label, Token callKeyword, 
											 Token eos, boolean hasActualArgSpecList) {
		// Auto-generated method stub
	}

	public void procedure_designator() {
		// Auto-generated method stub
	}

	public void actual_arg_spec(Token keyword) {
		// Auto-generated method stub
	}

	public void actual_arg_spec_list__begin() {
		// Auto-generated method stub
	}

	public void actual_arg_spec_list(int count) {
		// Auto-generated method stub
	}

	public void actual_arg(boolean hasExpr, Token label) {
		// Auto-generated method stub
	}

	public void function_subprogram(boolean hasExePart, 
														  boolean hasIntSubProg) {
		// Auto-generated method stub
	}

	public void function_stmt__begin() {
		// Auto-generated method stub
	}

	public void function_stmt(Token label, Token keyword, Token name, 
												  Token eos, boolean hasGenericNameList, 
												  boolean hasSuffix) {
		// Auto-generated method stub
	}

	public void proc_language_binding_spec() {
		// Auto-generated method stub
	}

	public void prefix(int specCount) {
		// Auto-generated method stub
	}

	public void t_prefix(int specCount) {
		// Auto-generated method stub
	}

	public void prefix_spec(boolean isDecTypeSpec) {
		// Auto-generated method stub
	}

	public void t_prefix_spec(Token spec) {
		// Auto-generated method stub
	}

	public void suffix(Token resultKeyword, 
										 boolean hasProcLangBindSpec) {
		// Auto-generated method stub
	}

	public void result_name() {
		// Auto-generated method stub
	}

	public void end_function_stmt(Token label, Token keyword1, 
														Token keyword2, Token name, 
														Token eos) {
		// Auto-generated method stub
	}

	public void subroutine_stmt__begin() {
		// Auto-generated method stub
	}

	public void subroutine_stmt(Token label, Token keyword, Token name,
													 Token eos,	boolean hasPrefix, 
													 boolean hasDummyArgList, 
													 boolean hasBindingSpec, 
													 boolean hasArgSpecifier) {
		// Auto-generated method stub
	}

	public void dummy_arg(Token dummy) {
		// Auto-generated method stub
	}

	public void dummy_arg_list__begin() {
		// Auto-generated method stub
	}

	public void dummy_arg_list(int count) {
		// Auto-generated method stub
	}

	public void end_subroutine_stmt(Token label, Token keyword1, 
														  Token keyword2, Token name, 
														  Token eos) {
		// Auto-generated method stub
	}

	public void entry_stmt(Token label, Token keyword, Token id,
											  Token eos, boolean hasDummyArgList, 
											  boolean hasSuffix) {
		// Auto-generated method stub
	}

	public void return_stmt(Token label, Token keyword, Token eos, 
												boolean hasScalarIntExpr) {
		// Auto-generated method stub
	}

	public void contains_stmt(Token label, Token keyword, Token eos) {
		// Auto-generated method stub
	}

	public void stmt_function_stmt(Token label, Token functionName, 
														 Token eos, 
														 boolean hasGenericNameList) {
		// Auto-generated method stub
	}

	public void end_of_stmt(Token eos) {
		// Auto-generated method stub
	}

	public void start_of_file(String fileName) {
		// Auto-generated method stub
	}

	public void end_of_file() {
		// Auto-generated method stub
	}

	public void cleanUp() {
		// Auto-generated method stub
	}


}
