package org.eclipse.ptp.lang.fortran.core.parser;

import org.antlr.runtime.Token;

public class FortranParserActionNull implements IFortranParserAction {

	protected FortranParserActionNull(String[] args, FortranParser parser, String filename) {
		super();
	}

	public void generic_name_list__begin() {
		//TODO Auto-generated method stub
	}

	public void generic_name_list(int count) {
		//TODO Auto-generated method stub
	}

	public void generic_name_list_part(Token ident) {
		//TODO Auto-generated method stub
	}

	public void internal_subprogram_part(int count) {
		//TODO Auto-generated method stub
	}

	public void constant(Token id) {
		//TODO Auto-generated method stub
	}

	public void int_constant(Token id) {
		//TODO Auto-generated method stub
	}

	public void char_constant(Token id) {
		//TODO Auto-generated method stub
	}

	public void defined_operator(Token definedOp) {
		//TODO Auto-generated method stub
	}

	public void label(Token lbl) {
		//TODO Auto-generated method stub
	}

	public void label_list__begin() {
		//TODO Auto-generated method stub
	}

	public void label_list(int count) {
		//TODO Auto-generated method stub
	}

	public void type_param_value(boolean hasExpr, boolean hasAsterisk, boolean hasColon) {
		//TODO Auto-generated method stub
	}

	public void intrinsic_type_spec(IntrinsicTypeSpec type, boolean hasKindSelector) {
		//TODO Auto-generated method stub
	}

	public void kind_selector(boolean hasExpression, Token typeSize) {
		//TODO Auto-generated method stub
	}

	 public void signed_int_literal_constant(Token sign) {
		//TODO Auto-generated method stub
	}

	 public void int_literal_constant(Token digitString, Token kindParam) {
		//TODO Auto-generated method stub
	}

	 public void signed_real_literal_constant(Token sign) {
		//TODO Auto-generated method stub
	}

	 public void real_literal_constant(Token digits, Token fractionExp, Token kindParam) {
		//TODO Auto-generated method stub
	}

	public void real_part(boolean hasIntConstant, boolean hasRealConstant, Token id) {
		//TODO Auto-generated method stub
	}

	public void imag_part(boolean hasIntConstant, boolean hasRealConstant, Token id) {
		//TODO Auto-generated method stub
	}

	public void char_selector(KindLenParam kindOrLen1, KindLenParam kindOrLen2, boolean hasAsterisk) {
		//TODO Auto-generated method stub
	}

	public void length_selector(KindLenParam kindOrLen, boolean hasAsterisk) {
		//TODO Auto-generated method stub
	}

	public void char_length(boolean hasTypeParamValue) {
		//TODO Auto-generated method stub
	}

	public void char_literal_constant(Token digitString, Token id, Token str) {
		//TODO Auto-generated method stub
	}

	public void logical_literal_constant(boolean isTrue, Token kindParam) {
		//TODO Auto-generated method stub
	}

	public void derived_type_stmt(Token label, Token id) {
		//TODO Auto-generated method stub
	}

	public void type_attr_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void type_attr_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void end_type_stmt(Token label, Token id) {
		//TODO Auto-generated method stub
	}

	public void sequence_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void type_param_decl_list__begin() {
		//TODO Auto-generated method stub
	}

	public void type_param_decl_list(int count) {
		//TODO Auto-generated method stub
	}

	public void data_component_def_stmt(Token label, boolean hasSpec) {
		//TODO Auto-generated method stub
	}

	public void component_attr_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void component_attr_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void component_decl_list__begin() {
		//TODO Auto-generated method stub
	}

	public void component_decl_list(int count) {
		//TODO Auto-generated method stub
	}

	public void deferred_shape_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void deferred_shape_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void proc_component_def_stmt(Token label, boolean hasInterface) {
		//TODO Auto-generated method stub
	}

	public void proc_component_attr_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void proc_component_attr_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void private_components_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void binding_private_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void proc_binding_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void binding_attr(AttrSpec attr) {
		//TODO Auto-generated method stub
	}

	public void binding_attr_list__begin() {
		//TODO Auto-generated method stub
	}

	public void binding_attr_list(int count) {
		//TODO Auto-generated method stub
	}

	public void derived_type_spec(Token typeName, boolean hasTypeParamSpecList) {
		//TODO Auto-generated method stub
	}

	public void type_param_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void type_param_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void component_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void component_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void enum_def_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void enumerator_def_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void enumerator_list__begin() {
		//TODO Auto-generated method stub
	}

	public void enumerator_list(int count) {
		//TODO Auto-generated method stub
	}

	public void end_enum_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void array_constructor() {
		//TODO Auto-generated method stub
	}

	public void ac_value_list__begin() {
		//TODO Auto-generated method stub
	}

	public void ac_value_list(int count) {
		//TODO Auto-generated method stub
	}

	public void type_declaration_stmt__begin() {
		//TODO Auto-generated method stub
	}

	public void type_declaration_stmt(Token label, int numAttributes) {
		//TODO Auto-generated method stub
	}

	public void declaration_type_spec(DeclarationTypeSpec type) {
		//TODO Auto-generated method stub
	}

	public void attr_spec(AttrSpec attr) {
		//TODO Auto-generated method stub
	}

	public void entity_decl(Token id) {
		//TODO Auto-generated method stub
	}

	public void entity_decl_list__begin() {
		//TODO Auto-generated method stub
	}

	public void entity_decl_list(int count) {
		//TODO Auto-generated method stub
	}

	public void initialization(boolean hasExpr, boolean hasNullInit) {
		//TODO Auto-generated method stub
	}

	public void null_init(Token id) {
		//TODO Auto-generated method stub
	}

	public void language_binding_spec(Token id, boolean hasName) {
		//TODO Auto-generated method stub
	}

	public void array_spec__begin() {
		//TODO Auto-generated method stub
	}

	public void array_spec(int count) {
		//TODO Auto-generated method stub
	}

	public void array_spec_element(ArraySpecElement type) {
		//TODO Auto-generated method stub
	}

	public void explicit_shape_spec(boolean hasUpperBound) {
		//TODO Auto-generated method stub
	}

	public void explicit_shape_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void explicit_shape_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void intent_spec(IntentSpec intent) {
		//TODO Auto-generated method stub
	}

	public void access_stmt(Token label, boolean hasList) {
		//TODO Auto-generated method stub
	}

	public void deferred_co_shape_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void deferred_co_shape_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void access_id_list__begin() {
		//TODO Auto-generated method stub
	}

	public void access_id_list(int count) {
		//TODO Auto-generated method stub
	}

	public void asynchronous_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void bind_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void bind_entity(Token entity, boolean isCommonBlockName) {
		//TODO Auto-generated method stub
	}

	public void bind_entity_list__begin() {
		//TODO Auto-generated method stub
	}

	public void bind_entity_list(int count) {
		//TODO Auto-generated method stub
	}

	public void data_stmt__begin() {
		//TODO Auto-generated method stub
	}

	public void data_stmt(Token label, int count) {
		//TODO Auto-generated method stub
	}

	public void data_stmt_object_list__begin() {
		//TODO Auto-generated method stub
	}

	public void data_stmt_object_list(int count) {
		//TODO Auto-generated method stub
	}

	public void data_i_do_object_list__begin() {
		//TODO Auto-generated method stub
	}

	public void data_i_do_object_list(int count) {
		//TODO Auto-generated method stub
	}

	public void data_stmt_value_list__begin() {
		//TODO Auto-generated method stub
	}

	public void data_stmt_value_list(int count) {
		//TODO Auto-generated method stub
	}

	public void dimension_stmt__begin() {
		//TODO Auto-generated method stub
	}

	public void dimension_stmt(Token label, int count) {
		//TODO Auto-generated method stub
	}

	public void intent_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void optional_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void parameter_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void named_constant_def_list__begin() {
		//TODO Auto-generated method stub
	}

	public void named_constant_def_list(int count) {
		//TODO Auto-generated method stub
	}

	public void pointer_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void pointer_decl_list__begin() {
		//TODO Auto-generated method stub
	}

	public void pointer_decl_list(int count) {
		//TODO Auto-generated method stub
	}

	public void protected_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void save_stmt(Token label,boolean hasSavedEntityList) {
		//TODO Auto-generated method stub
	}

	public void saved_entity_list__begin() {
		//TODO Auto-generated method stub
	}

	public void saved_entity_list(int count) {
		//TODO Auto-generated method stub
	}

	public void target_stmt__begin() {
		//TODO Auto-generated method stub
	}

	public void target_stmt(Token label, int count) {
		//TODO Auto-generated method stub
	}

	public void value_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void volatile_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void implicit_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void implicit_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void implicit_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void letter_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void letter_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void namelist_group_object_list__begin() {
		//TODO Auto-generated method stub
	}

	public void namelist_group_object_list(int count) {
		//TODO Auto-generated method stub
	}

	public void equivalence_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void equivalence_set_list__begin() {
		//TODO Auto-generated method stub
	}

	public void equivalence_set_list(int count) {
		//TODO Auto-generated method stub
	}

	public void equivalence_object_list__begin() {
		//TODO Auto-generated method stub
	}

	public void equivalence_object_list(int count) {
		//TODO Auto-generated method stub
	}

	public void common_block_object_list__begin() {
		//TODO Auto-generated method stub
	}

	public void common_block_object_list(int count) {
		//TODO Auto-generated method stub
	}

	public void variable() {
		//TODO Auto-generated method stub
	}

	public void designator(boolean hasSubstringRange) {
		//TODO Auto-generated method stub
	}

	public void substring(boolean hasSubstringRange) {
		//TODO Auto-generated method stub
	}

	public void substring_range(boolean hasLowerBound, boolean hasUpperBound) {
		//TODO Auto-generated method stub
	}

	public void data_ref(int numPartRef) {
		//TODO Auto-generated method stub
	}

	public void part_ref(Token id, boolean hasSelectionSubscriptList, boolean hasImageSelector) {
		//TODO Auto-generated method stub
	}

	public void section_subscript(boolean hasLowerBound, boolean hasUpperBound, boolean hasStride, boolean isAmbiguous) {
		//TODO Auto-generated method stub
	}

	public void section_subscript_list__begin() {
		//TODO Auto-generated method stub
	}

	public void section_subscript_list(int count) {
		//TODO Auto-generated method stub
	}

	public void allocate_stmt(Token label, boolean hasTypeSpec, boolean hasAllocOptList) {
		//TODO Auto-generated method stub
	}

	public void alloc_opt(Token allocOpt) {
		//TODO Auto-generated method stub
	}

	public void alloc_opt_list__begin() {
		//TODO Auto-generated method stub
	}

	public void alloc_opt_list(int count) {
		//TODO Auto-generated method stub
	}

	public void allocation(boolean hasAllocateShapeSpecList, boolean hasAllocateCoArraySpec) {
		//TODO Auto-generated method stub
	}

	public void allocation_list__begin() {
		//TODO Auto-generated method stub
	}

	public void allocation_list(int count) {
		//TODO Auto-generated method stub
	}

	public void allocate_object_list__begin() {
		//TODO Auto-generated method stub
	}

	public void allocate_object_list(int count) {
		//TODO Auto-generated method stub
	}

	public void allocate_shape_spec(boolean hasLowerBound, boolean hasUpperBound) {
		//TODO Auto-generated method stub
	}

	public void allocate_shape_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void allocate_shape_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void nullify_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void pointer_object_list__begin() {
		//TODO Auto-generated method stub
	}

	public void pointer_object_list(int count) {
		//TODO Auto-generated method stub
	}

	public void dealloc_opt_list__begin() {
		//TODO Auto-generated method stub
	}

	public void dealloc_opt_list(int count) {
		//TODO Auto-generated method stub
	}

	public void allocate_co_shape_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void allocate_co_shape_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void primary() {
		//TODO Auto-generated method stub
	}

	public void level_1_expr(Token definedUnaryOp) {
		//TODO Auto-generated method stub
	}

	public void power_operand(boolean hasPowerOperand) {
		//TODO Auto-generated method stub
	}

	public void power_operand__power_op(Token powerOp) {
		//TODO Auto-generated method stub
	}

	public void mult_operand(int numMultOps) {
		//TODO Auto-generated method stub
	}

	public void mult_operand__mult_op(Token multOp) {
		//TODO Auto-generated method stub
	}

	public void add_operand(Token addOp, int numAddOps) {
		//TODO Auto-generated method stub
	}

	public void add_operand__add_op(Token addOp) {
		//TODO Auto-generated method stub
	}

	public void level_2_expr(int numConcatOps) {
		//TODO Auto-generated method stub
	}

	public void level_3_expr(Token relOp) {
		//TODO Auto-generated method stub
	}

	public void and_operand(boolean hasNotOp, int numAndOps) {
		//TODO Auto-generated method stub
	}

	public void and_operand__not_op(boolean hasNotOp) {
		//TODO Auto-generated method stub
	}

	public void or_operand(int numOrOps) {
		//TODO Auto-generated method stub
	}

	public void equiv_operand(int numEquivOps) {
		//TODO Auto-generated method stub
	}

	public void equiv_operand__equiv_op(Token equivOp) {
		//TODO Auto-generated method stub
	}

	public void level_5_expr(int numDefinedBinaryOps) {
		//TODO Auto-generated method stub
	}

	public void level_5_expr__defined_binary_op(Token definedBinaryOp) {
		//TODO Auto-generated method stub
	}

	public void expr() {
		//TODO Auto-generated method stub
	}

	public void assignment_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void bounds_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void bounds_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void bounds_remapping_list__begin() {
		//TODO Auto-generated method stub
	}

	public void bounds_remapping_list(int count) {
		//TODO Auto-generated method stub
	}

	public void where_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void where_construct_stmt(Token id) {
		//TODO Auto-generated method stub
	}

	public void masked_elsewhere_stmt(Token label, Token id) {
		//TODO Auto-generated method stub
	}

	public void elsewhere_stmt(Token label, Token id) {
		//TODO Auto-generated method stub
	}

	public void end_where_stmt(Token label, Token id) {
		//TODO Auto-generated method stub
	}

	public void forall_construct_stmt(Token label, Token id) {
		//TODO Auto-generated method stub
	}

	public void forall_triplet_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void forall_triplet_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void case_value_range_list__begin() {
		//TODO Auto-generated method stub
	}

	public void case_value_range_list(int count) {
		//TODO Auto-generated method stub
	}

	public void association_list__begin() {
		//TODO Auto-generated method stub
	}

	public void association_list(int count) {
		//TODO Auto-generated method stub
	}

	public void cycle_stmt(Token label, Token id) {
		//TODO Auto-generated method stub
	}

	public void exit_stmt(Token label, Token id) {
		//TODO Auto-generated method stub
	}

	public void goto_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void computed_goto_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void arithmetic_if_stmt(Token label, Token label1, Token label2, Token label3) {
		//TODO Auto-generated method stub
	}

	public void continue_stmt(Token label) {
		//TODO Auto-generated method stub
	}

	public void stop_stmt(Token label, boolean hasStopCode) {
		//TODO Auto-generated method stub
	}

	public void stop_code(Token digitString) {
		//TODO Auto-generated method stub
	}

	public void connect_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void connect_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void close_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void close_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void io_control_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void io_control_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void input_item_list__begin() {
		//TODO Auto-generated method stub
	}

	public void input_item_list(int count) {
		//TODO Auto-generated method stub
	}

	public void output_item_list__begin() {
		//TODO Auto-generated method stub
	}

	public void output_item_list(int count) {
		//TODO Auto-generated method stub
	}

	public void wait_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void wait_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void position_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void position_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void flush_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void flush_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void inquire_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void inquire_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void format_item_list__begin() {
		//TODO Auto-generated method stub
	}

	public void format_item_list(int count) {
		//TODO Auto-generated method stub
	}

	public void v_list_part(Token plus_minus, Token digitString) {
		//TODO Auto-generated method stub
	}

	public void v_list__begin() {
		//TODO Auto-generated method stub
	}

	public void v_list(int count) {
		//TODO Auto-generated method stub
	}

	public void main_program__begin() {
		//TODO Auto-generated method stub
	}

	public void main_program(boolean hasProgramStmt, boolean hasExecutionPart, boolean hasInternalSubprogramPart) {
		//TODO Auto-generated method stub
	}

	public void program_stmt(Token label, Token id) {
		//TODO Auto-generated method stub
	}

	public void end_program_stmt(Token label, Token id) {
		//TODO Auto-generated method stub
	}

	public void rename_list__begin() {
		//TODO Auto-generated method stub
	}

	public void rename_list(int count) {
		//TODO Auto-generated method stub
	}

	public void only_list__begin() {
		//TODO Auto-generated method stub
	}

	public void only_list(int count) {
		//TODO Auto-generated method stub
	}

	public void proc_interface(Token id) {
		//TODO Auto-generated method stub
	}

	public void proc_attr_spec(AttrSpec spec) {
		//TODO Auto-generated method stub
	}

	public void proc_decl(Token id, boolean hasNullInit) {
		//TODO Auto-generated method stub
	}

	public void proc_decl_list__begin() {
		//TODO Auto-generated method stub
	}

	public void proc_decl_list(int count) {
		//TODO Auto-generated method stub
	}

	public void function_reference(boolean hasActualArgSpecList) {
		//TODO Auto-generated method stub
	}

	public void call_stmt(Token label, boolean hasActualArgSpecList) {
		//TODO Auto-generated method stub
	}

	public void actual_arg_spec(Token keyword) {
		//TODO Auto-generated method stub
	}

	public void actual_arg_spec_list__begin() {
		//TODO Auto-generated method stub
	}

	public void actual_arg_spec_list(int count) {
		//TODO Auto-generated method stub
	}

	public void actual_arg(boolean hasExpr, Token label) {
		//TODO Auto-generated method stub
	}

	public void dummy_arg(Token dummy) {
		//TODO Auto-generated method stub
	}

	public void dummy_arg_list__begin() {
		//TODO Auto-generated method stub
	}

	public void dummy_arg_list(int count) {
		//TODO Auto-generated method stub
	}

	public void write_stmt(Token label, boolean hasOutputList) {
		// TODO Auto-generated method stub
	}

	public void io_control_spec(boolean hasExpression, Token keyword,
			boolean hasAsterisk) {
		// TODO Auto-generated method stub
		
	}


}