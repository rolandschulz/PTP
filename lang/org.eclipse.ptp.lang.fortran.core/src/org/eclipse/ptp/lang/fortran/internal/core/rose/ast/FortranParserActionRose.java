package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.Token;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.ptp.lang.fortran.core.parser.FortranLexer;
import org.eclipse.ptp.lang.fortran.core.parser.FortranParser;
import org.eclipse.ptp.lang.fortran.core.parser.IActionEnums;
import org.eclipse.ptp.lang.fortran.core.parser.IFortranParserAction;
import org.eclipse.ptp.lang.fortran.internal.core.dom.parser.ASTStack;
import org.eclipse.ptp.lang.fortran.internal.core.dom.parser.FortranASTBinaryExpression;

public class FortranParserActionRose implements IFortranParserAction {

	// Stack that holds the intermediate AST
	private ASTStack astStack = new ASTStack();
	
	// Stack for the scopes
	// TODO - make type <SgScopeStatement>
	private ASTStack scopeStack = new ASTStack();
	
	private SgProject project;
	
	private FortranParser parser = null;
	
	void setSourcePosition(SgLocatedNode locatedNode) {
		// The SgLocatedNode has both a startOfConstruct and endOfConstruct source position.
		assert(locatedNode != null);
		assert(locatedNode.get_startOfConstruct() == null);
		assert(locatedNode.get_endOfConstruct() == null);

		locatedNode.set_startOfConstruct(Sg_File_Info.generateDefaultFileInfoForTransformationNode());
		locatedNode.set_endOfConstruct(Sg_File_Info.generateDefaultFileInfoForTransformationNode());
	}

	void setSourcePosition(SgInitializedName initializedName) {
		// The SgInitializedName only has a startOfConstruct source position.
		assert(initializedName != null);
		assert(initializedName.get_startOfConstruct() == null);
		initializedName.set_startOfConstruct(Sg_File_Info.generateDefaultFileInfoForTransformationNode());
	}

	// TODO - finish this implementation (perhaps not needed)
	void resetSourcePosition(SgLocatedNode locatedNode, ArrayList<Token> tokenList) {
	     System.out.println("In resetSourcePosition locatedNode = " + locatedNode + " " + locatedNode.class_name());

	     // The SgLocatedNode has both a startOfConstruct and endOfConstruct source position.
	     assert(locatedNode != null);

	     assert(locatedNode.get_startOfConstruct() != null);
	     assert(locatedNode.get_endOfConstruct() != null);

	     // Remove the existing Sg_File_Info objects, they will be reset below
	     // delete locatedNode->get_startOfConstruct();
	     // delete locatedNode->get_endOfConstruct();

	     // Get the first and last tokens from the token list (information about middle tokens 
	     // is held in the token stream to be attached to the AST).
	     //Token firstToken = tokenList.front();
	     //Token lastToken  = tokenList.back();

	     //assert(firstToken != null);
	     //assert(lastToken != null);

	     //assert(firstToken.get_startOfConstruct() != null);
	     //assert(firstToken.get_endOfConstruct()   != null);

	     //assert(lastToken.get_startOfConstruct() != null);
	     //assert(lastToken.get_endOfConstruct()   != null);

	     // Set these based on the source position information from the tokens
	     //locatedNode.set_startOfConstruct(new Sg_File_Info(*(firstToken.get_startOfConstruct())));
	     //locatedNode.set_endOfConstruct(new Sg_File_Info(*(lastToken.get_endOfConstruct  ())));
	}

	public FortranParserActionRose(FortranParser parser, String filename) {
		this.parser = parser;
		this.project = setup_AST_Skeleton(filename);

		// TODO - do the following lines need to be done, if so should go in setup_AST_Skeleton
		// globalScope.set_startOfConstruct(Sg_File_Info.generateDefaultFileInfoForTransformationNode());
		// globalScope.set_endOfConstruct(Sg_File_Info.generateDefaultFileInfoForTransformationNode());
		
		// TODO - create translation unit here?
	}

	/**
	 *  This function builds the basics required to support an AST (SgProject, SgFile, SgGlobal)
	 *  and then sets up the stack of scopes which will be used by the functions called directly 
	 *  by the parser.
	 */
	
	SgProject setup_AST_Skeleton(String filename) {
		
		// Build the SgProject IR node, the root of the AST (initialized to global variable "project")
		SgProject project = new SgProject();

		// Without setting the file list we get an error from the traversal.
		// Temp code, this detail will be buried into the IR in the near future.
		ArrayList<SgFile> fileList = new ArrayList<SgFile>();
		project.set_fileList(fileList);

		// Now build the SgFile IR node
		SgFile file = new SgFile();
	    project.get_fileList().add(file);

	    // Set the file names in the SgFile IR node for later use by the unparser at the end
	    // The first two might not be required, but the last one is (it is the name of the 
	    // generated source file).
	    file.set_sourceFileNameWithPath(filename);
	    file.set_sourceFileNameWithoutPath(filename);
	    file.set_unparse_output_filename("rose_" + filename);

	    // Parents are set within the AST post-processing (but we may not have that for awhile, so keep)
	    file.set_parent(project);

	    // The default Sg_File_Info built is not built correctly, so replace it with 
	    // another one. This is a detail that will be buried in ROSE a bit later.
	    file.set_file_info(Sg_File_Info.generateDefaultFileInfoForTransformationNode());
	    assert(file.get_file_info() != null);

	    // Set the parent so that the final graph will be better connected
	    SgNode.get_globalFunctionTypeTable().set_parent(file);

	    // Build the global scope IR node for the file
	    SgGlobal globalScope = new SgGlobal();
	    file.set_root(globalScope);
	    setSourcePosition(globalScope);

	    // Scopes should be pushed onto the stack
	    scopeStack.push(globalScope);

	    return project;
	}


	public void buildExpressionID() {
		// TODO Auto-generated method stub

	}

	/** R102 list
	 * generic_name (xyz-name)
	 * generic_name_list (xyz-list R101)
	 * 	:	T_IDENT ( T_COMMA T_IDENT )*
	 * 
	 * @param count The number of items in the list.
	 * @param ident The name of the item placed in the list.
	 */
	public void generic_name_list__begin() {
	}
	public void generic_name_list(int count) {
	}
	public void generic_name_list_part(Token ident) {
	}

	/** R210
	 * internal_subprogram_part
	 *	:	T_CONTAINS T_EOS internal_subprogram (internal_subprogram)*
	 *
	 * T_CONTAINS inlined for contains_stmt
	 *
	 * @param count The number of internal subprograms
	 */
	public void internal_subprogram_part(int count) {
	}

	/** R305
	 * constant
	 *	:	literal_constant
	 *	|	T_IDENT
	 * 
	 * ERR_CHK 305 named_constant replaced by T_IDENT
	 * 
	 * @param id The identifier representing the named constant if present, otherwise is a literal-constant
	 */
	public void constant(Token id) {
	}

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
	public void int_constant(Token id) {
	}

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
	public void char_constant(Token id) {
	}

	/** R311
	 * defined_operator
	 *	:	T_DEFINED_OP
	 *	|	extended_intrinsic_op
	 *
	 * removed defined_unary_op or defined_binary_op ambiguity with T_DEFINED_OP
	 * 
	 * @param definedOp The defined operator token if present, otherwise is an extended-intrinsic-op
	 */
	public void defined_operator(Token definedOp) {
	}

	/** R313
	 * label	:	T_DIGIT_STRING
	 *
	 * @param lbl The token containing the label
     */
	public void label(Token lbl) {
	}

	/** R313 list
	 * label	:	T_DIGIT_STRING
	 * label_list
	 *	:	label ( T_COMMA label )*
	 *
	 * // ERR_CHK 313 five characters or less
	 *
	 * @param count The number of items in the list.
     */
	public void label_list__begin() {
	}
	public void label_list(int count) {
	}

	/** R402
	 * type-param-value
	 *	: expr | T_ASTERISK | T_COLON
	 *
	 * @param hasExpr True if an expr is present
	 * @param hasAsterisk True if an '*' is present
	 * @param hasColon True if a ':' is present
	 */
	public void type_param_value(boolean hasExpr, boolean hasAsterisk, boolean hasColon) {
	}

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
	public void intrinsic_type_spec(int type, boolean hasKindSelector) {
		System.out.print("R 403:intrinsic-type-spec: type=" + type);
		System.out.println(" hasKindSelector=" + hasKindSelector);
		
		SgExpression kind_selector = null;
		if (hasKindSelector) {
			kind_selector = (SgExpression) astStack.pop();
		}

		SgType sg_type = null;
		// TODO - implement other types
		switch(type) {
		case IActionEnums.IntrinsicTypeSpec_INTEGER: sg_type = SgTypeInt.createType(); break;
		case IActionEnums.IntrinsicTypeSpec_REAL: break;
		case IActionEnums.IntrinsicTypeSpec_DOUBLEPRECISION: break;
		case IActionEnums.IntrinsicTypeSpec_COMPLEX: break;
		case IActionEnums.IntrinsicTypeSpec_CHARACTER: break;
		case IActionEnums.IntrinsicTypeSpec_LOGICAL: break;
		default: break; // error condition
		}
		astStack.push(sg_type);
		// TODO - handle kind_selector properly (add a component to the type?)
		if (hasKindSelector) {
			// sg_type.set_kindSelector(kind_selector);
		}
	}

	/** R405
	 * signed_int_literal_constant
	 *  : 	(T_PLUS|T_MINUS)? int_literal_constant
	 *
	 * @param sign The sign: positive, negative, or null.
	 */
	 public void signed_int_literal_constant(Token sign) {
		 
	 }

	/** R406
	 * int_literal_constant
	 *	:	T_DIGIT_STRING (T_UNDERSCORE kind_param)?
	 *
	 * @param digitString The digit string representing the constant
	 * @param kindParam The kind parameter
	 */
	 public void int_literal_constant(Token digitString, Token kindParam) {
			System.out.print("R 406:int-literal-constant:");
			System.out.print(" digitString=" + digitString);
			System.out.println(" kindParam=" + kindParam);
			int val = Integer.parseInt(digitString.getText());
			SgIntVal sg_int_val = new SgIntVal(val, digitString.getText());
			if (kindParam != null) {
				// check token type to see if is a constant or a variable
				if (kindParam.getType() == FortranLexer.T_DIGIT_STRING) {
					
				} else {
					// type is T_IDENT so build a variable expression
					
				}
				// TODO - build expression and add to the type in sg_int_val
			}
			astStack.push(sg_int_val);
	 }

	/** R416
	 * signed_real_literal_constant
	 *  : 	(T_PLUS|T_MINUS)? real_literal_constant
	 *
	 * @param sign The sign: positive, negative, or null.
	 */
	 public void signed_real_literal_constant(Token sign) {
		 
	 }

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
	 public void real_literal_constant(Token digits, Token fractionExp, Token kindParam) {
		 
	 }

	/** R422
	 * real_part
	 * 
	 * ERR_CHK 422 named_constant replaced by T_IDENT
	 * 
	 * @param hasIntConstant True if signed-int-literal-constant is present
	 * @param hasRealConstant True if signed-real-literal-constant is present
	 * @param id The named-constant (optional)
	 */
	public void real_part(boolean hasIntConstant, boolean hasRealConstant, Token id) {
		
	}

	/** R423
	 * imag_part
	 * 
	 * ERR_CHK 423 named_constant replaced by T_IDENT
	 * 
	 * @param hasIntConstant True if signed-int-literal-constant is present
	 * @param hasRealConstant True if signed-real-literal-constant is present
	 * @param id The named-constant (optional)
	 */
	public void imag_part(boolean hasIntConstant, boolean hasRealConstant, Token id) {
		
	}

	/** R426
	 * char_length
	 *	:	T_LPAREN type_param_value T_RPAREN
	 *	|	scalar_int_literal_constant
	 *
	 * @param hasTypeParamValue True if a type-param-value is specified, otherwise is a scalar-int-literal-constant
	 */
	public void char_length(boolean hasTypeParamValue) {
		
	}

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
	public void char_literal_constant(Token digitString, Token id, Token str) {
		
	}

	/** R428
	 * logical_literal_constant
	 *	: T_TRUE | T_FALSE
	 *
	 * @param isTrue True if logical constant is true, false otherwise
	 * @param kindParam The kind parameter
	 */
	public void logical_literal_constant(boolean isTrue, Token kindParam) {
		
	}
		
	/** R431 list
	 * type_attr_spec
	 * type_attr_spec_list
	 * 	:	type_attr_spec ( T_COMMA type_attr_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void type_attr_spec_list__begin() {
		
	}
	public void type_attr_spec_list(int count) {
		
	}

	/** R436 list
	 * type_param_decl
	 * type_param_decl_list
	 *	:	type_param_decl ( T_COMMA type_param_decl )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void type_param_decl_list__begin() {
		
	}
	public void type_param_decl_list(int count) {
		
	}

	/** R441 list
	 * component_attr_spec
	 * component_attr_spec_list
	 * 	:	component_attr_spec ( T_COMMA component_attr_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void component_attr_spec_list__begin() {
		
	}
	public void component_attr_spec_list(int count) {
		
	}

	/** R442 list
	 * component_decl
	 * component_decl_list
	 *    :    component_decl ( T_COMMA component_decl )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void component_decl_list__begin() {
		
	}
	public void component_decl_list(int count) {
		
	}

	/** R443 list
	 * deferred_shape_spec_list
	 * 		T_COLON {count++;} ( T_COMMA T_COLON {count++;} )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void deferred_shape_spec_list__begin() {
		
	}
	public void deferred_shape_spec_list(int count) {
		
	}

	/** R446 list
	 * proc_component_attr_spec_list
	 * 		proc_component_attr_spec ( T_COMMA proc_component_attr_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void proc_component_attr_spec_list__begin() {
		
	}
	public void proc_component_attr_spec_list(int count) {
		
	}

	/** R453 list
	 * binding_attr_list
	 * 		binding_attr ( T_COMMA binding_attr )*
     *
	 * @param count The number of items in the list.
	 */
	public void binding_attr_list__begin() {
		
	}
	public void binding_attr_list(int count) {
		
	}

	/** R456 list
	 * type_param_spec_list
	 * 		type_param_spec ( T_COMMA type_param_spec )*
     *
	 * @param count The number of items in the list.
	 */
	public void type_param_spec_list__begin() {
		
	}
	public void type_param_spec_list(int count) {
		
	}

	/** R458 list
	 * component_spec_list
	 * 		component_spec ( T_COMMA component_spec )*
     *
	 * @param count The number of items in the list.
	 */
	public void component_spec_list__begin() {
		
	}
	public void component_spec_list(int count) {
		
	}

	/** R463 list
	 * enumerator_list
	 * 		enumerator ( T_COMMA enumerator )*
     *
	 * @param count The number of items in the list.
	 */
	public void enumerator_list__begin() {
		
	}
	public void enumerator_list(int count) {
		
	}

	/** R465
	 * array_constructor
	 *	:	T_LPAREN T_SLASH ac_spec T_SLASH T_RPAREN
	 *	|	T_LBRACKET ac_spec T_RBRACKET
	 */
	public void array_constructor() {
		
	}

	/** R469 list
	 * ac_value_list
	 * 		ac_value ( T_COMMA ac_value )*
	    *
	 * @param count The number of items in the list.
	 */
	public void ac_value_list__begin() {
		
	}
	public void ac_value_list(int count) {
		
	}

	/** R501
	 * type_declaration_stmt
	 * :    (label)? declaration_type_spec ( (T_COMMA attr_spec)* T_COLON_COLON )?
	 *      entity_decl_list T_EOS
	 *
	 * @param label Optional statement label
	 * @param numAttributes The number of attributes present
	 */
	public void type_declaration_stmt__begin() {
		
	}
	public void type_declaration_stmt(Token label, int numAttributes) {
		System.out.print("R 501:type-declaration-stmt:");
		if (label != null) System.out.print(" label=" + label);
		System.out.println(" numAttributes=" + numAttributes);
		
		ArrayList<Token> list = (ArrayList<Token>) astStack.pop();
		SgType type = (SgType) astStack.pop();
		
		// TODO - add label
		// TODO - add attributes
		
		for (int i = 0; i < list.size(); i++) {
			// TODO - add initializer (NULL for now)
			SgName sg_name = new SgName(list.get(i).getText());
			SgInitializedName variableName = new SgInitializedName(sg_name, type, null);
			setSourcePosition(variableName); // TODO - set correct source position
			
			 // Build the variable declaration IR node
		     SgVariableDeclaration variableDeclaration = new SgVariableDeclaration();
		     setSourcePosition(variableDeclaration);
		     
			 // NULL is used to indicate that there is no initializer.
		     variableDeclaration.append_variable(variableName, null);
		     SgBasicBlock basicBlock = (SgBasicBlock) scopeStack.peek();
		     assert(basicBlock != null);
		     
			 // Add the variable declaration to the function body's scope.
		     basicBlock.append_statement(variableDeclaration);
		     
			 // Build a symbol for the scope's symbol table, and insert the symbol.
		     SgVariableSymbol variableSymbol = new SgVariableSymbol(variableName);
		     basicBlock.insert_symbol(variableName.get_name(), variableSymbol);
		}
	}

	/** R504, R503-F2008
	 * entity_decl
	 *  : T_IDENT ( T_LPAREN array_spec T_RPAREN )?
	 *            ( T_LBRACKET co_array_spec T_RBRACKET )?
	 *            ( T_ASTERISK char_length )? ( initialization )? 
	 */
	public void entity_decl(Token id) {
		System.out.println("R 504:entity-decl: id=" + id);
		astStack.push(id);
	}

	/** R504 list
	 * entity_decl
	 * entity_decl_list
	 * 	:    entity_decl ( T_COMMA entity_decl )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void entity_decl_list__begin() {
		System.out.println("R 504:entity-decl-list: begin:");
	}
	public void entity_decl_list(int count) {
		System.out.println("R 504:entity-decl-list: count=" + count);
		ArrayList<Token> list = new ArrayList<Token>(count);
		for (int i = count-1; i >= 0; i--) {
			list.add(i, (Token) astStack.pop());
		}
		astStack.push(list);
	}

	
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
	public void initialization(boolean hasExpr, boolean hasNullInit) {

	}

	/** R507
	 * null_init
	 * 	:	T_IDENT //'NULL'// T_LPAREN T_RPAREN
	 *
	 * C506 The function-reference shall be a reference to the NULL intrinsic function with no arguments.
	 *
	 * @param id The function-reference
	 */
	public void null_init(Token id) {

	}

	/** R511
	 * explicit_shape_spec
 	 * expr ( T_COLON expr )?
	 * 
	 * @param hasUpperBound Whether the shape spec is of the form x:y.
	 */
	public void explicit_shape_spec(boolean hasUpperBound) {
	}

	/** R511 list
	 * explicit_shape_spec_list
	 *	:	explicit_shape_spec ( T_COMMA explicit_shape_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void explicit_shape_spec_list__begin() {
	}
	public void explicit_shape_spec_list(int count) {
	}

	/** R519-08 list
	 * deferred_co_shape_spec_list
	 *	:	T_COLON ( T_COMMA T_COLON )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void deferred_co_shape_spec_list__begin() {
	}
	public void deferred_co_shape_spec_list(int count) {
	}

	/** R519 list
	 * access_id_list
	 * 	:    access_id ( T_COMMA access_id )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void access_id_list__begin() {
	}
	public void access_id_list(int count) {
	}

	/** R523 list
	 * bind_entity_list
	 * 	:    bind_entity ( T_COMMA bind_entity )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void bind_entity_list__begin() {
	}
	public void bind_entity_list(int count) {
	}

	/** R526 list
	 * data_stmt_object_list
	 * 	:    data_stmt_object ( T_COMMA data_stmt_object )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void data_stmt_object_list__begin() {
	}
	public void data_stmt_object_list(int count) {
	}

	/** R528 list
	 * data_i_do_object_list
	 * 	:    data_i_do_object ( T_COMMA data_i_do_object )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void data_i_do_object_list__begin() {
	}
	public void data_i_do_object_list(int count) {
	}

	/** R530 list
	 * data_stmt_value_list
	 * 	:    data_stmt_value ( T_COMMA data_stmt_value )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void data_stmt_value_list__begin() {
	}
	public void data_stmt_value_list(int count) {
	}

	/** R539 list
	 * named_constant_def_list
	 * 	:    named_constant_def ( T_COMMA named_constant_def )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void named_constant_def_list__begin() {
	}
	public void named_constant_def_list(int count) {
	}

	/** R541 list
	 * pointer_decl_list
	 * 	:    pointer_decl ( T_COMMA pointer_decl )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void pointer_decl_list__begin() {
	}
	public void pointer_decl_list(int count) {
	}

	/** R544 list
	 * saved_entity_list
	 * 	:    saved_entity ( T_COMMA saved_entity )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void saved_entity_list__begin() {
	}
	public void saved_entity_list(int count) {
	}

	/** R550 list
	 * implicit_spec_list
	 * 	:    implicit_spec ( T_COMMA implicit_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void implicit_spec_list__begin() {
	}
	public void implicit_spec_list(int count) {
	}

	/** R551 list
	 * letter_spec_list
	 * 	:    letter_spec ( T_COMMA letter_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void letter_spec_list__begin() {
	}
	public void letter_spec_list(int count) {
	}

	/** R553 list
	 * namelist_group_object_list
	 * 	:    T_IDENT ( T_COMMA T_IDENT )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void namelist_group_object_list__begin() {
	}
	public void namelist_group_object_list(int count) {
	}

	/** R555 list
	 * equivalence_set_list
	 * 	:    equivalence_set ( T_COMMA equivalence_set )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void equivalence_set_list__begin() {
	}
	public void equivalence_set_list(int count) {
	}

	/** R556 list
	 * equivalence_object_list
	 * 	:    equivalence_object ( T_COMMA equivalence_object )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void equivalence_object_list__begin() {
	}
	public void equivalence_object_list(int count) {
	}

	/** R558 list
	 * common_block_object_list
	 * 	:    common_block_object ( T_COMMA common_block_object )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void common_block_object_list__begin() {
	}
	public void common_block_object_list(int count) {
	}

	/** R601
	 * variable
	 * :	designator
	 */
	public void variable() {
		System.out.println("R 601:variable:");
		// TODO - fix assumption that this is only an ident (variable-name?)
		Token tk = (Token) astStack.pop();
		SgInitializedName name = new SgInitializedName(new SgName(tk.getText()), null, null);
		SgVariableSymbol symbol = new SgVariableSymbol(name);
		SgVarRefExp var = new SgVarRefExp(symbol);
		astStack.push(var);
	}

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
	public void designator(boolean hasSubstringRange) {
		System.out.println("R 603:designator:");	
	}

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
	public void substring(boolean hasSubstringRange) {
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
	}

	/** R612
	 *	data_ref
	 *	:	part_ref (T_PERCENT part_ref)*
	 *
	 * @param numPartRef The number of optional part_refs
	 */
	public void data_ref(int numPartRef) {
	}

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
	public void part_ref(Token id, boolean hasSelectionSubscriptList, boolean hasImageSelector) {
		System.out.println("R 613:part-ref: id=" + id);
		// TODO - figure out what to do here
		astStack.push(id);
	}

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
	public void section_subscript(boolean hasLowerBound, boolean hasUpperBound, boolean hasStride, boolean isAmbiguous) {
		
	}

	/** R619 list
	 * section_subscript
	 * section_subscript_list
	 * 	:    section_subscript ( T_COMMA section_subscript )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void section_subscript_list__begin() {
	}
	public void section_subscript_list(int count) {
	}

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
	public void allocate_stmt(Token label, boolean hasTypeSpec, boolean hasAllocOptList) {
	}

	/** R624
	 * alloc_opt
	 *	:	T_IDENT			// {'STAT','ERRMSG'} are variables {SOURCE'} is expr
	 *		T_EQUALS expr
	 *
	 * @param allocOpt Identifier representing {'STAT','ERRMSG','SOURCE'}
	 */
	public void alloc_opt(Token allocOpt) {
	}
	
	/** R624 list
	 * alloc_opt_list
	 * 	:    alloc_opt ( T_COMMA alloc_opt )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void alloc_opt_list__begin() {
	}
	public void alloc_opt_list(int count) {
	}

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
	public void allocation(boolean hasAllocateShapeSpecList, boolean hasAllocateCoArraySpec) {
	}

	/** R628 list
	 * allocation_list
	 * 	:    allocation ( T_COMMA allocation )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void allocation_list__begin() {
	}
	public void allocation_list(int count) {
	}

	/** R629 list
	 * allocate_object_list
	 * 	:	allocate_object ( T_COMMA allocate_object )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void allocate_object_list__begin() {
	}
	public void allocate_object_list(int count) {
	}

	/** R630
	 * allocate_shape_spec
	 *	:	expr (T_COLON expr)?
	 *
	 * NOTE: not called by current parser, appears as R619 section-subscript instead
	 *
	 * @param hasLowerBound True if optional lower-bound-expr is present.
	 * @param hasUpperBound True if upper-bound-expr is present (note always true).
	 */
	public void allocate_shape_spec(boolean hasLowerBound, boolean hasUpperBound) {
	}

	/** R630 list
	 * allocate_shape_spec_list
	 * 	:    allocate_shape_spec ( T_COMMA allocate_shape_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void allocate_shape_spec_list__begin() {
	}
	public void allocate_shape_spec_list(int count) {
	}

	/** R633
	 *	nullify_stmt
	 *	:	(label)? T_NULLIFY T_LPAREN pointer_object_list T_RPAREN T_EOS
	 *
	 * @param label Optional statement label
	 */
	public void nullify_stmt(Token label) {
	}
	
	/** R634 list
	 * pointer_object_list
	 * 	:    pointer_object ( T_COMMA pointer_object )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void pointer_object_list__begin() {
	}
	public void pointer_object_list(int count) {
	}

	/** R636 list
	 * dealloc_opt_list
	 * 	:    dealloc_opt ( T_COMMA dealloc_opt )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void dealloc_opt_list__begin() {
	}
	public void dealloc_opt_list(int count) {
	}

	/** R637-F2008 list
	 * allocate_co_shape_spec_list
	 * 	:    allocate_co_shape_spec ( T_COMMA allocate_co_shape_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void allocate_co_shape_spec_list__begin() {
	}
	public void allocate_co_shape_spec_list(int count) {
	}

	/** R701
	 * primary
	 *	:	designator_or_func_ref
	 *	|	literal_constant
	 *	|	array_constructor
	 *	|	structure_constructor
	 *	|	T_LPAREN expr T_RPAREN
	 *
	 */
	public void primary() {
		// System.out.println("R 701:primary");
	}
	
	/** R702
	 * level_1_expr
	 *  : (defined_unary_op)? primary
	 */
	public void level_1_expr(Token definedUnaryOp) {
	}
	
	/** R704: note, inserted as R704 functionality
	 * power_operand
	 *	: level_1_expr (power_op power_operand)?
	 */
	public void power_operand(boolean hasPowerOperand) {
	}
	public void power_operand__power_op(Token powerOp) {
	}

	/** R704: note, see power_operand
	 * mult_operand
	 *  : power_operand (mult_op power_operand)*
	 *  
	 *  @param numMults The number of optional mult_ops
	 */
	public void mult_operand(int numMultOps) {
	}
	public void mult_operand__mult_op(Token multOp) {
	}

	/** R705: note, moved leading optionals to mult_operand
	 * add_operand
	 *  : (add_op)? mult_operand (add_op mult_operand)*
	 *  
	 * @param addOp Optional add_op for this operand
	 * @param numAddOps The number of optional add_ops
	 */
	public void add_operand(Token addOp, int numAddOps) {
	}
	public void add_operand__add_op(Token addOp) {
		System.out.println("R705a:add-operand__add-op: addOp=" + addOp);
		buildExpressionBinaryOperator(addOp);
	}

	/** R706: note, moved leading optionals to add_operand
	 * level_2_expr
	 *  : add_operand (concat_op add_operand)*
	 *  
	 *  @param numConcatOps The number of optional numConcatOps
	 */
	public void level_2_expr(int numConcatOps) {
	}


	/** R710: note, moved leading optional to level_2_expr
	 * level_3_expr
	 *  : level_2_expr (rel_op level_2_expr)?
	 *  
	 *  @param relOp The rel-op, if present, null otherwise
	 */
	public void level_3_expr(Token relOp) {
	}

	/** R714
	 * and_operand
	 *  :    (not_op)? level_3_expr (and_op level_3_expr)*
	 *
	 * @param hasNotOp True if optional not_op is present
	 * @param numAndOps The number of optional and_ops
	 */
	public void and_operand(boolean hasNotOp, int numAndOps) {
	}
	public void and_operand__not_op(boolean hasNotOp) {
	}

	/** R715: note, moved leading optional to or_operand
	 * or_operand
	 *    : and_operand (or_op and_operand)*
	 *
	 * @param numOrOps The number of optional or_ops
	 */
	public void or_operand(int numOrOps) {
	}

	/** R716: note, moved leading optional to or_operand
	 * equiv_operand
	 *  : or_operand (equiv_op or_operand)*
	 *  
	 * @param numEquivOps The number of optional or_operands
	 * @param equivOp Optional equiv_op for this operand
	 */
	public void equiv_operand(int numEquivOps) {
	}
	public void equiv_operand__equiv_op(Token equivOp) {
	}

	/** R717: note, moved leading optional to equiv_operand
	 * level_5_expr
	 *  : equiv_operand (defined_binary_op equiv_operand)*
	 *  
	 * @param numDefinedBinaryOps The number of optional equiv_operands
	 * @param definedBinaryOp Optional defined_binary_op for this operand
	 */
	public void level_5_expr(int numDefinedBinaryOps) {
	}
	public void level_5_expr__defined_binary_op(Token definedBinaryOp) {
	}

	/** R722: note, moved leading optional to level_5_expr
	 * expr
	 *  : level_5_expr
	 */
	public void expr() {
	}

	/** R734
	 *	assignment_stmt 
	 *	:	(label)? T_ASSIGNMENT_STMT variable	T_EQUALS expr T_EOS
	 *
	 * @param label Optional statement label
	 */
	public void assignment_stmt(Token label) {
		System.out.print("R 734:assignment-stmt:");
		if (label != null) System.out.print(" label=" + label);
		System.out.println();
		
		SgExpression expr = (SgExpression) astStack.pop();
		SgExpression variable = (SgExpression) astStack.pop();
		
		SgAssignOp assign_op = new SgAssignOp(variable, expr, null);
		expr.set_parent(assign_op);
		variable.set_parent(assign_op);
		
		SgExpressionStatement assign_stmt = new SgExpressionStatement(assign_op);
	    SgBasicBlock basicBlock = (SgBasicBlock) scopeStack.peek();
	    basicBlock.append_statement(assign_stmt);
	}

	/** R737 list
	 * bounds_spec_list
	 * 	:    bounds_spec ( T_COMMA bounds_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void bounds_spec_list__begin() {
	}
	public void bounds_spec_list(int count) {
	}

	/** R738 list
	 * bounds_remapping_list
	 * 	:    bounds_remapping ( T_COMMA bounds_remapping )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void bounds_remapping_list__begin() {
	}
	public void bounds_remapping_list(int count) {
	}

	/** R755 list
	 * forall_triplet_spec_list
	 * 	:    forall_triplet_spec ( T_COMMA forall_triplet_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void forall_triplet_spec_list__begin() {
	}
	public void forall_triplet_spec_list(int count) {
	}

	/** R814 list
	 * case_value_range_list
	 * 	:    case_value_range ( T_COMMA case_value_range )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void case_value_range_list__begin() {
	}
	public void case_value_range_list(int count) {
	}

	/** R817 list
	 * association_list
	 * 	:    association ( T_COMMA association )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void association_list__begin() {
	}
	public void association_list(int count) {
	}

	/** R843
	 * cycle_stmt
	 *	:	(label)? T_CYCLE (T_IDENT)? T_EOS
	 * 
	 * T_IDENT inlined for do_construct_name
	 * 
	 * @param label Optional statement label
	 * @param id Optional do-construct-name
	 */
	public void cycle_stmt(Token label, Token id) {
	}

	/** R844
	 * exit_stmt
	 *	:	(label)? T_EXIT (T_IDENT)? T_EOS
	 *
	 * T_IDENT inlined for do_construct_name
	 * 
	 * @param label Optional statement label
	 * @param id Optional do-construct-name
	 */
	public void exit_stmt(Token label, Token id) {
	}

	/** R845
	 * goto_stmt
	 *	:	t_go_to label T_EOS
	 *
	 * @param label The branch target statement label
	 */
	public void goto_stmt(Token label) {
	}

	/** R846
	 * computed_goto_stmt
	 *	:	(label)? t_go_to T_LPAREN label_list T_RPAREN (T_COMMA)? expr T_EOS
	 *
	 * ERR_CHK 846 scalar_int_expr replaced by expr
	 * 
	 * @param label Optional statement label
	 */
	public void computed_goto_stmt(Token label) {
	}

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
	public void arithmetic_if_stmt(Token label, Token label1, Token label2, Token label3) {
	}

	/** R848
	 * continue_stmt
	 *	:	(label)? T_CONTINUE
	 * 
	 * @param label  Optional statement label
	 */
	public void continue_stmt(Token label) {
	}

	/** R849
	 * stop_stmt
	 *	:	(label)? T_STOP (stop_code)? T_EOS
	 *
	 *@param label Optional statement label
	 *@param hasStopCode True if the stop-code is present, false otherwise
	 */
	public void stop_stmt(Token label, boolean hasStopCode) {
	}

	/** R850
	 * stop_code
	 *	: scalar_char_constant
	 *	| T_DIGIT_STRING
	 * 
	 * ERR_CHK 850 T_DIGIT_STRING must be 5 digits or less
	 * 
	 * @param digitString The stop-code token, otherwise is a scalar-char-constant
	 */
	public void stop_code(Token digitString) {
	}

	/** R905 list
	 * connect_spec_list
	 * 	:    connect_spec ( T_COMMA connect_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void connect_spec_list__begin() {
	}
	public void connect_spec_list(int count) {
	}

	/** R909 list
	 * close_spec_list
	 * 	:    close_spec ( T_COMMA close_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void close_spec_list__begin() {
	}
	public void close_spec_list(int count) {
	}

	/** R913 list
	 * io_control_spec_list
	 * 	:    io_control_spec ( T_COMMA io_control_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void io_control_spec_list__begin() {
	}
	public void io_control_spec_list(int count) {
	}

	/** R915 list
	 * input_item_list
	 * 	:    input_item ( T_COMMA input_item )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void input_item_list__begin() {
	}
	public void input_item_list(int count) {
	}

	/** R916 list
	 * output_item_list
	 * 	:    output_item ( T_COMMA output_item )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void output_item_list__begin() {
	}
	public void output_item_list(int count) {
	}

	/** R922 list
	 * wait_spec_list
	 * 	:    wait_spec ( T_COMMA wait_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void wait_spec_list__begin() {
	}
	public void wait_spec_list(int count) {
	}

	/** R926 list
	 * position_spec_list
	 * 	:    position_spec ( T_COMMA position_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void position_spec_list__begin() {
	}
	public void position_spec_list(int count) {
	}

	/** R928 list
	 * flush_spec_list
	 * 	:    flush_spec ( T_COMMA flush_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void flush_spec_list__begin() {
	}
	public void flush_spec_list(int count) {
	}

	/** R930 list
	 * inquire_spec_list
	 * 	:    inquire_spec ( T_COMMA inquire_spec )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void inquire_spec_list__begin() {
	}
	public void inquire_spec_list(int count) {
	}

	/** R1003 list
	 * format_item_list
	 * 	:    format_item ( T_COMMA format_item )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void format_item_list__begin() {
	}
	public void format_item_list(int count) {
	}

	/** R1010 list
	 * v_list_part
	 * v_list
	 * 	:    (T_PLUS|T_MINUS)? T_DIGIT_STRING ( T_COMMA (T_PLUS|T_MINUS)? T_DIGIT_STRING )*
	 * 
	 * @param plus_minus Optional T_PLUSIT_MINUS token.
	 * @param digitString The digit string token.
	 * @param count The number of items in the list.
	 */
	public void v_list_part(Token plus_minus, Token digitString) {
	}
	public void v_list__begin() {
	}
	public void v_list(int count) {
	}

	/** R1101
	 * main_program
	 *	(program_stmt)?	specification_part (execution_part)? (internal_subprogram_part)?
	 *	end_program_stmt
	 * 
	 * @param hasProgramStmt Optional program-stmt
	 * @param hasExecutionPart Optional execution-part
	 * @param hasInternalSubprogramPart Optional internal-subprogram-part
	 */
	public void main_program__begin() {
		// This function builds the basics of a function in which to put the main fortran program.
		// These IR nodes will be marked as compiler generated and if we later see an explicit
		// program statement then we will reset these IR nodes to be non-compiler generated and
		// assign them source position information.

		// Get the current scope (assume SgGlobal for now)
		SgGlobal globalScope = (SgGlobal) scopeStack.pop();
		assert(globalScope != null);

		// This is a "C" language specific detail, for Fortran it is always false.
		boolean has_ellipse = false;
		SgFunctionType functionType = new SgFunctionType(SgTypeVoid.createType(), has_ellipse);

		// Build the function declaration, this will be a SgProgramHeaderStatement 
		// (a new Fortran specific IR node) a little later.
		SgProgramHeaderStatement functionDeclaration = new SgProgramHeaderStatement(new SgName(""), functionType, null);
		functionDeclaration.set_scope(globalScope);
		setSourcePosition(functionDeclaration);

		// Need to set the position of the parameter list explicitly, this should be buried in ROSE.
		setSourcePosition(functionDeclaration.get_parameterList());

		// Need to explicitly set the parent so that the functionDeclaration can be retrieved 
		// from the chain of parents starting as the scope on top of the stack.
		functionDeclaration.set_parent(globalScope);

		// Insert the function type into the global function type symbol table
		SgFunctionTypeSymbol functionTypeSymbol = new SgFunctionTypeSymbol(functionType.get_mangled(), functionType);

		// There is no interface function to insert an existing symbol!
		// SgNode::get_globalFunctionTypeTable()->insert_function_type(functionType->get_mangled(),functionTypeSymbol);
		assert(SgNode.get_globalFunctionTypeTable() != null);
		assert(SgNode.get_globalFunctionTypeTable().get_function_type_table() != null);
		SgNode.get_globalFunctionTypeTable().get_function_type_table().insert(functionType.get_mangled(),functionTypeSymbol);

		// Add the function declaration to the global scope
		globalScope.append_declaration(functionDeclaration);

		// Build the body of the function definition
		SgBasicBlock basicBlock = new SgBasicBlock();

		// DQ (3/22/2007): Setting the source position is required for the AST post-processing
		setSourcePosition(basicBlock);

		// Build the function definition
		SgFunctionDefinition functionDefinition = new SgFunctionDefinition(basicBlock);
		setSourcePosition(functionDefinition);

		// Need to explicitly set the parent so that the functionDeclaration can be retrieved 
		// from the chain of parents starting as the scope on top of the stack.
		basicBlock.set_parent(functionDefinition);

		// Set the associated definition in the function declaration
		functionDeclaration.set_definition(functionDefinition);

		// Need to explicitly set the parent so that the functionDeclaration can be retrieved 
		// from the chain of parents starting as the scope on top of the stack.
		functionDefinition.set_parent(functionDeclaration);

		// Scopes should push onto the front of the stack (the top is the front)
		// Push associated scopes onto the stack of scopes (functionDefinition is pushed for 
		// consistency). Note that two scopes must be popped off the stack at the end of a
		// function.
		scopeStack.push(functionDefinition);
		scopeStack.push(basicBlock);

		astStack.push(functionDeclaration);
	}
	public void main_program(boolean hasProgramStmt, boolean hasExecutionPart, boolean hasInternalSubprogramPart) {
		System.out.print("R1101:main-program: hasProgramStmt=" + hasProgramStmt);
		System.out.println(" hasExecutionPart=" + hasExecutionPart + " hasInternalSubprogramPart=" + hasInternalSubprogramPart);

		Token program_stmt_name = null;
		Token program_stmt_label = null;

		SgBasicBlock basicBlock = (SgBasicBlock) scopeStack.pop();
		SgFunctionDefinition functionDefinition = (SgFunctionDefinition) scopeStack.pop();
		
		// end_program_stmt
		Token end_program_stmt_name = (Token) astStack.pop();
		Token end_program_stmt_label = (Token) astStack.pop();

		// internal_subprogram_part
		if (hasInternalSubprogramPart) {
			// TODO - this is another scope
		}

		// execution_part
		if (hasExecutionPart) {
			// TODO what to do, nothing? Statements should have all been added to the basic block
		}
		
		// specification_part
		// TODO what to do, nothing? Statements should have all been added to the basic block
		
		// program_stmt
		if (hasProgramStmt) {
			program_stmt_name = (Token) astStack.pop();
			program_stmt_label = (Token) astStack.pop();
		}
		
		SgProgramHeaderStatement functionDecl = (SgProgramHeaderStatement) astStack.pop();

		if (program_stmt_name != null) {
			// Add name provided by the program_stmt, previously empty string
			functionDecl.set_name(new SgName(program_stmt_name.getText()));
			if (end_program_stmt_name != null) {
				assert(functionDecl.get_name().getString() == end_program_stmt_name.getText());
			}
		}

		if (program_stmt_label != null) {
			int numeric_label = Integer.parseInt(program_stmt_label.getText());
			functionDecl.set_numeric_label(numeric_label); 
		}

		// TODO - what do we do now, the function declaration is part of the global scope?
		// hand off as a translation unit?
		assert(astStack.isEmpty());
	}
	
	/** R1102
	 * program_stmt
	 * :	(label)? ...
	 * 
	 * @param label Optional statement label
	 * @param id Optional program name
	 */
	public void program_stmt(Token label, Token id) {
		astStack.push(label);
		astStack.push(id);
	}

	/** R1103
	 * end_program_stmt
	 * :	(label)? ...
	 * 
	 * @param label Optional statement label
	 * @param id Optional program name
	 */
	public void end_program_stmt(Token label, Token id) {
		System.out.print("R1103:end-program-stmt:");
		if (label != null) System.out.print(" label=" + label);
		System.out.println();

		astStack.push(label);
		astStack.push(id);
	}

	/** R1111 list
	 * rename_list
	 * 	:    rename ( T_COMMA rename )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void rename_list__begin() {
	}
	public void rename_list(int count) {
	}

	/** R1112 list
	 * only_list
	 * 	:    only ( T_COMMA only )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void only_list__begin() {
	}
	public void only_list(int count) {
	}

	/** R1212
	 * proc_interface
	 *	:	T_IDENT
	 *	|	declaration_type_spec
	 *
	 * @param id The interface name.
	 */
	public void proc_interface(Token id) {
	}

	/** R1214
	 * proc_decl
	 *    :	T_IDENT ( T_EQ_GT null_init {hasNullInit=true;} )?
	 *    
	 * @param id The name of the procedure.
	 * @param hasNullInit True if null-init is present.
	 */
	public void proc_decl(Token id, boolean hasNullInit) {
	}
	   
	/** R1214 list
	 * proc_decl_list
	 * 	:    proc_decl ( T_COMMA proc_decl )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void proc_decl_list__begin() {
	}
	public void proc_decl_list(int count) {
	}

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
	public void function_reference(boolean hasActualArgSpecList) {
	}

	/** R1218
	 * call_stmt
	 *	:	(label)? T_CALL procedure_designator
			( T_LPAREN (actual_arg_spec_list)? T_RPAREN )? T_EOS
	 * 
	 * @param label Optional statement label
	 * @param hasActionArgSpecList True if an actual-arg-spec-list is present
	 */
	public void call_stmt(Token label, boolean hasActualArgSpecList) {
	}

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
	public void actual_arg_spec(Token keyword) {
	}
	
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
	public void actual_arg_spec_list__begin() {
	}
	public void actual_arg_spec_list(int count) {
	}
	
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
	public void actual_arg(boolean hasExpr, Token label) {
	}
	
	/** R1233
	 * dummy_arg
	 *	:	T_IDENT | T_ASTERISK
	 *
	 * @param dummy The dummy argument token.
	 */
	public void dummy_arg(Token dummy) {
	}
	
	/** R1233 list
	 * dummy_arg_list
	 * 	:    dummy_arg ( T_COMMA dummy_arg )*
	 * 
	 * @param count The number of items in the list.
	 */
	public void dummy_arg_list__begin() {
	}
	public void dummy_arg_list(int count) {
	}
	
	private class DataRequiredForComputationOfSourcePositionInformation {
		
		private int startLine;
		private int startCol;
		private int endLine;
		private int endCol;
		
		private DataRequiredForComputationOfSourcePositionInformation(Token start, Token end) {
			startLine = start.getLine();
			startCol = start.getCharPositionInLine();
			endLine = end.getLine();
			endCol = end.getCharPositionInLine();
		}

		public void initializeSourcePositionInformation(SgNode node) {
			
		}
	}

	private void buildExpressionBinaryOperator(Token op) {
		SgExpression rhs = (SgExpression) astStack.pop();
		SgExpression lhs = (SgExpression) astStack.pop();

		// TODO - what to do with type, hopefully can clean up later
		SgBinaryOp binExpr = createBinaryOp(op, lhs, rhs, null);

		lhs.set_parent(binExpr);
		lhs.setPropertyInParent(IASTBinaryExpression.OPERAND_ONE);
		
		rhs.set_parent(binExpr);
		rhs.setPropertyInParent(IASTBinaryExpression.OPERAND_TWO);
		
		int ruleOffset = 0; // TODO - get from addOp token
		int ruleLength = 1; // TODO - get from addOp token
		binExpr.setOffsetAndLength(ruleOffset, ruleLength);

		// TODO - set ROSE location
		
		astStack.push(binExpr);
	}

	private SgBinaryOp createBinaryOp(Token op, SgExpression lhs, SgExpression rhs, SgType expression_type) {
		SgBinaryOp binOp = null;
		
		switch (op.getType()) {
		case FortranParser.T_PLUS: binOp = new SgAddOp(lhs, rhs, expression_type); break;
		}
		
		return binOp;
	}

	public void access_stmt(Token label, boolean hasList) {
		// TODO Auto-generated method stub
		
	}

	public void array_spec(int count) {
		// TODO Auto-generated method stub
		
	}

	public void array_spec__begin() {
		// TODO Auto-generated method stub
		
	}

	public void asynchronous_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void bind_entity(Token entity, boolean isCommonBlockName) {
		// TODO Auto-generated method stub
		
	}

	public void bind_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void binding_private_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void data_component_def_stmt(Token label, boolean hasSpec) {
		// TODO Auto-generated method stub
		
	}

	public void data_stmt(Token label, int count) {
		// TODO Auto-generated method stub
		
	}

	public void data_stmt__begin() {
		// TODO Auto-generated method stub
		
	}

	public void derived_type_spec(Token typeName, boolean hasTypeParamSpecList) {
		// TODO Auto-generated method stub
		
	}

	public void derived_type_stmt(Token label, Token id) {
		// TODO Auto-generated method stub
		
	}

	public void dimension_stmt(Token label, int count) {
		// TODO Auto-generated method stub
		
	}

	public void dimension_stmt__begin() {
		// TODO Auto-generated method stub
		
	}

	public void elsewhere_stmt(Token label, Token id) {
		// TODO Auto-generated method stub
		
	}

	public void end_enum_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void end_type_stmt(Token label, Token id) {
		// TODO Auto-generated method stub
		
	}

	public void end_where_stmt(Token label, Token id) {
		// TODO Auto-generated method stub
		
	}

	public void enum_def_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void enumerator_def_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void equivalence_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void forall_construct_stmt(Token label, Token id) {
		// TODO Auto-generated method stub
		
	}

	public void implicit_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void intent_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void io_control_spec(boolean hasExpression, Token keyword,
			boolean hasAsterisk) {
		// TODO Auto-generated method stub
		
	}

	public void kind_selector(boolean hasExpression, Token typeSize) {
		// TODO Auto-generated method stub
		
	}

	public void language_binding_spec(Token id, boolean hasName) {
		// TODO Auto-generated method stub
		
	}

	public void masked_elsewhere_stmt(Token label, Token id) {
		// TODO Auto-generated method stub
		
	}

	public void optional_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void parameter_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void pointer_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void private_components_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void proc_binding_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void proc_component_def_stmt(Token label, boolean hasInterface) {
		// TODO Auto-generated method stub
		
	}

	public void protected_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void save_stmt(Token label, boolean hasSavedEntityList) {
		// TODO Auto-generated method stub
		
	}

	public void sequence_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void target_stmt(Token label, int count) {
		// TODO Auto-generated method stub
		
	}

	public void target_stmt__begin() {
		// TODO Auto-generated method stub
		
	}

	public void value_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void volatile_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void where_construct_stmt(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void where_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}

	public void write_stmt(Token label, boolean hasOutputList) {
		// TODO Auto-generated method stub
		
	}

	public void ac_implied_do() {
		// TODO Auto-generated method stub
		
	}

	public void ac_implied_do_control(boolean hasStride) {
		// TODO Auto-generated method stub
		
	}

	public void ac_spec() {
		// TODO Auto-generated method stub
		
	}

	public void ac_value() {
		// TODO Auto-generated method stub
		
	}

	public void access_id() {
		// TODO Auto-generated method stub
		
	}

	public void access_spec(Token keyword, int type) {
		// TODO Auto-generated method stub
		
	}

	public void access_stmt(Token label, Token eos, boolean hasList) {
		// TODO Auto-generated method stub
		
	}

	public void action_stmt() {
		// TODO Auto-generated method stub
		
	}

	public void add_op(Token addKeyword) {
		// TODO Auto-generated method stub
		
	}

	public void allocatable_decl(Token id, boolean hasArraySpec,
			boolean hasCoArraySpec) {
		// TODO Auto-generated method stub
		
	}

	public void allocatable_stmt(Token label, Token keyword, Token eos,
			int count) {
		// TODO Auto-generated method stub
		
	}

	public void allocate_co_array_spec() {
		// TODO Auto-generated method stub
		
	}

	public void allocate_co_shape_spec(boolean hasExpr) {
		// TODO Auto-generated method stub
		
	}

	public void allocate_object() {
		// TODO Auto-generated method stub
		
	}

	public void allocate_stmt(Token label, Token allocateKeyword, Token eos,
			boolean hasTypeSpec, boolean hasAllocOptList) {
		// TODO Auto-generated method stub
		
	}

	public void and_op(Token andOp) {
		// TODO Auto-generated method stub
		
	}

	public void arithmetic_if_stmt(Token label, Token ifKeyword, Token label1,
			Token label2, Token label3, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void array_spec_element(int type) {
		// TODO Auto-generated method stub
		
	}

	public void assign_stmt(Token label1, Token assignKeyword, Token label2,
			Token toKeyword, Token name, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void assigned_goto_stmt(Token label, Token goKeyword,
			Token toKeyword, Token name, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void assignment_stmt(Token label, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void associate_construct() {
		// TODO Auto-generated method stub
		
	}

	public void associate_stmt(Token label, Token id, Token associateKeyword,
			Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void association(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void asynchronous_stmt(Token label, Token keyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void attr_spec(Token attrKeyword, int attr) {
		// TODO Auto-generated method stub
		
	}

	public void backspace_stmt(Token label, Token backspaceKeyword, Token eos,
			boolean hasPositionSpecList) {
		// TODO Auto-generated method stub
		
	}

	public void bind_stmt(Token label, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void binding_attr(Token bindingAttr, int attr, Token id) {
		// TODO Auto-generated method stub
		
	}

	public void binding_private_stmt(Token label, Token privateKeyword,
			Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void block() {
		// TODO Auto-generated method stub
		
	}

	public void block_data() {
		// TODO Auto-generated method stub
		
	}

	public void block_data_stmt(Token label, Token blockKeyword,
			Token dataKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void block_data_stmt__begin() {
		// TODO Auto-generated method stub
		
	}

	public void block_do_construct() {
		// TODO Auto-generated method stub
		
	}

	public void bounds_remapping() {
		// TODO Auto-generated method stub
		
	}

	public void bounds_spec() {
		// TODO Auto-generated method stub
		
	}

	public void boz_literal_constant(Token constant) {
		// TODO Auto-generated method stub
		
	}

	public void call_stmt(Token label, Token callKeyword, Token eos,
			boolean hasActualArgSpecList) {
		// TODO Auto-generated method stub
		
	}

	public void case_construct() {
		// TODO Auto-generated method stub
		
	}

	public void case_selector(Token defaultToken) {
		// TODO Auto-generated method stub
		
	}

	public void case_stmt(Token label, Token caseKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void case_value() {
		// TODO Auto-generated method stub
		
	}

	public void case_value_range() {
		// TODO Auto-generated method stub
		
	}

	public void case_value_range_suffix() {
		// TODO Auto-generated method stub
		
	}

	public void char_selector(Token tk1, Token tk2, int kindOrLen1,
			int kindOrLen2, boolean hasAsterisk) {
		// TODO Auto-generated method stub
		
	}

	public void char_variable() {
		// TODO Auto-generated method stub
		
	}

	public void cleanUp() {
		// TODO Auto-generated method stub
		
	}

	public void close_spec(Token closeSpec) {
		// TODO Auto-generated method stub
		
	}

	public void close_stmt(Token label, Token closeKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void co_array_spec() {
		// TODO Auto-generated method stub
		
	}

	public void common_block_name(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void common_block_object(Token id, boolean hasShapeSpecList) {
		// TODO Auto-generated method stub
		
	}

	public void common_stmt(Token label, Token commonKeyword, Token eos,
			int numBlocks) {
		// TODO Auto-generated method stub
		
	}

	public void complex_literal_constant() {
		// TODO Auto-generated method stub
		
	}

	public void component_array_spec(boolean isExplicit) {
		// TODO Auto-generated method stub
		
	}

	public void component_attr_spec(Token attrKeyword, int specType) {
		// TODO Auto-generated method stub
		
	}

	public void component_data_source() {
		// TODO Auto-generated method stub
		
	}

	public void component_decl(Token id, boolean hasComponentArraySpec,
			boolean hasCoArraySpec, boolean hasCharLength,
			boolean hasComponentInitialization) {
		// TODO Auto-generated method stub
		
	}

	public void component_def_stmt(int type) {
		// TODO Auto-generated method stub
		
	}

	public void component_initialization() {
		// TODO Auto-generated method stub
		
	}

	public void component_spec(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void computed_goto_stmt(Token label, Token goKeyword,
			Token toKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void concat_op(Token concatKeyword) {
		// TODO Auto-generated method stub
		
	}

	public void connect_spec(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void contains_stmt(Token label, Token keyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void continue_stmt(Token label, Token continueKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void cycle_stmt(Token label, Token cycleKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void data_component_def_stmt(Token label, Token eos, boolean hasSpec) {
		// TODO Auto-generated method stub
		
	}

	public void data_i_do_object() {
		// TODO Auto-generated method stub
		
	}

	public void data_implied_do(Token id, boolean hasThirdExpr) {
		// TODO Auto-generated method stub
		
	}

	public void data_pointer_object() {
		// TODO Auto-generated method stub
		
	}

	public void data_stmt(Token label, Token keyword, Token eos, int count) {
		// TODO Auto-generated method stub
		
	}

	public void data_stmt_constant() {
		// TODO Auto-generated method stub
		
	}

	public void data_stmt_object() {
		// TODO Auto-generated method stub
		
	}

	public void data_stmt_set() {
		// TODO Auto-generated method stub
		
	}

	public void data_stmt_value(Token asterisk) {
		// TODO Auto-generated method stub
		
	}

	public void dealloc_opt(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void deallocate_stmt(Token label, Token deallocateKeyword,
			Token eos, boolean hasDeallocOptList) {
		// TODO Auto-generated method stub
		
	}

	public void declaration_construct() {
		// TODO Auto-generated method stub
		
	}

	public void declaration_type_spec(Token udtKeyword, int type) {
		// TODO Auto-generated method stub
		
	}

	public void default_char_variable() {
		// TODO Auto-generated method stub
		
	}

	public void default_logical_variable() {
		// TODO Auto-generated method stub
		
	}

	public void deferred_co_shape_spec() {
		// TODO Auto-generated method stub
		
	}

	public void defined_binary_op(Token binaryOp) {
		// TODO Auto-generated method stub
		
	}

	public void defined_operator(Token definedOp, boolean isExtended) {
		// TODO Auto-generated method stub
		
	}

	public void defined_unary_op(Token definedOp) {
		// TODO Auto-generated method stub
		
	}

	public void derived_type_def() {
		// TODO Auto-generated method stub
		
	}

	public void derived_type_stmt(Token label, Token keyword, Token id,
			Token eos, boolean hasTypeAttrSpecList, boolean hasGenericNameList) {
		// TODO Auto-generated method stub
		
	}

	public void designator_or_func_ref() {
		// TODO Auto-generated method stub
		
	}

	public void dimension_decl(Token id, boolean hasArraySpec,
			boolean hasCoArraySpec) {
		// TODO Auto-generated method stub
		
	}

	public void dimension_spec(Token dimensionKeyword) {
		// TODO Auto-generated method stub
		
	}

	public void dimension_stmt(Token label, Token keyword, Token eos, int count) {
		// TODO Auto-generated method stub
		
	}

	public void do_construct() {
		// TODO Auto-generated method stub
		
	}

	public void do_stmt(Token label, Token id, Token doKeyword,
			Token digitString, Token eos, boolean hasLoopControl) {
		// TODO Auto-generated method stub
		
	}

	public void do_term_action_stmt(Token label, Token endKeyword,
			Token doKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void do_variable() {
		// TODO Auto-generated method stub
		
	}

	public void dtio_generic_spec(Token rw, Token format, int type) {
		// TODO Auto-generated method stub
		
	}

	public void dtv_type_spec(Token typeKeyword) {
		// TODO Auto-generated method stub
		
	}

	public void else_if_stmt(Token label, Token elseKeyword, Token ifKeyword,
			Token thenKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void else_stmt(Token label, Token elseKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void elsewhere_stmt(Token label, Token elseKeyword,
			Token whereKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void elsewhere_stmt__end(int numBodyConstructs) {
		// TODO Auto-generated method stub
		
	}

	public void end_associate_stmt(Token label, Token endKeyword,
			Token associateKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_block_data_stmt(Token label, Token endKeyword,
			Token blockKeyword, Token dataKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_do() {
		// TODO Auto-generated method stub
		
	}

	public void end_do_stmt(Token label, Token endKeyword, Token doKeyword,
			Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_enum_stmt(Token label, Token endKeyword, Token enumKeyword,
			Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_forall_stmt(Token label, Token endKeyword,
			Token forallKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_function_stmt(Token label, Token keyword1, Token keyword2,
			Token name, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_if_stmt(Token label, Token endKeyword, Token ifKeyword,
			Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_interface_stmt(Token label, Token kw1, Token kw2,
			Token eos, boolean hasGenericSpec) {
		// TODO Auto-generated method stub
		
	}

	public void end_module_stmt(Token label, Token endKeyword,
			Token moduleKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_of_file() {
		// TODO Auto-generated method stub
		
	}

	public void end_of_stmt(Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_program_stmt(Token label, Token endKeyword,
			Token programKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_select_stmt(Token label, Token endKeyword,
			Token selectKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_select_type_stmt(Token label, Token endKeyword,
			Token selectKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_subroutine_stmt(Token label, Token keyword1,
			Token keyword2, Token name, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_type_stmt(Token label, Token endKeyword, Token typeKeyword,
			Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void end_where_stmt(Token label, Token endKeyword,
			Token whereKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void endfile_stmt(Token label, Token endKeyword, Token fileKeyword,
			Token eos, boolean hasPositionSpecList) {
		// TODO Auto-generated method stub
		
	}

	public void entry_stmt(Token label, Token keyword, Token id, Token eos,
			boolean hasDummyArgList, boolean hasSuffix) {
		// TODO Auto-generated method stub
		
	}

	public void enum_def(int numEls) {
		// TODO Auto-generated method stub
		
	}

	public void enum_def_stmt(Token label, Token enumKeyword,
			Token bindKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void enumerator(Token id, boolean hasExpr) {
		// TODO Auto-generated method stub
		
	}

	public void enumerator_def_stmt(Token label, Token enumeratorKeyword,
			Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void equiv_op(Token equivOp) {
		// TODO Auto-generated method stub
		
	}

	public void equivalence_object() {
		// TODO Auto-generated method stub
		
	}

	public void equivalence_set() {
		// TODO Auto-generated method stub
		
	}

	public void equivalence_stmt(Token label, Token equivalenceKeyword,
			Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void executable_construct() {
		// TODO Auto-generated method stub
		
	}

	public void execution_part() {
		// TODO Auto-generated method stub
		
	}

	public void execution_part_construct() {
		// TODO Auto-generated method stub
		
	}

	public void exit_stmt(Token label, Token exitKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void explicit_co_shape_spec() {
		// TODO Auto-generated method stub
		
	}

	public void explicit_co_shape_spec_suffix() {
		// TODO Auto-generated method stub
		
	}

	public void ext_function_subprogram(boolean hasPrefix) {
		// TODO Auto-generated method stub
		
	}

	public void extended_intrinsic_op() {
		// TODO Auto-generated method stub
		
	}

	public void external_stmt(Token label, Token externalKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void file_unit_number() {
		// TODO Auto-generated method stub
		
	}

	public void final_binding(Token finalKeyword) {
		// TODO Auto-generated method stub
		
	}

	public void flush_spec(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void flush_stmt(Token label, Token flushKeyword, Token eos,
			boolean hasFlushSpecList) {
		// TODO Auto-generated method stub
		
	}

	public void forall_assignment_stmt(boolean isPointerAssignment) {
		// TODO Auto-generated method stub
		
	}

	public void forall_body_construct() {
		// TODO Auto-generated method stub
		
	}

	public void forall_construct() {
		// TODO Auto-generated method stub
		
	}

	public void forall_construct_stmt(Token label, Token id,
			Token forallKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void forall_header() {
		// TODO Auto-generated method stub
		
	}

	public void forall_stmt(Token label, Token forallKeyword) {
		// TODO Auto-generated method stub
		
	}

	public void forall_stmt__begin() {
		// TODO Auto-generated method stub
		
	}

	public void forall_triplet_spec(Token id, boolean hasStride) {
		// TODO Auto-generated method stub
		
	}

	public void format() {
		// TODO Auto-generated method stub
		
	}

	public void format_item(Token descOrDigit, boolean hasFormatItemList) {
		// TODO Auto-generated method stub
		
	}

	public void format_specification(boolean hasFormatItemList) {
		// TODO Auto-generated method stub
		
	}

	public void format_stmt(Token label, Token formatKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void function_stmt(Token label, Token keyword, Token name,
			Token eos, boolean hasGenericNameList, boolean hasSuffix) {
		// TODO Auto-generated method stub
		
	}

	public void function_stmt__begin() {
		// TODO Auto-generated method stub
		
	}

	public void function_subprogram(boolean hasExePart, boolean hasIntSubProg) {
		// TODO Auto-generated method stub
		
	}

	public void generic_binding(Token genericKeyword, boolean hasAccessSpec) {
		// TODO Auto-generated method stub
		
	}

	public void generic_spec(Token keyword, Token name, int type) {
		// TODO Auto-generated method stub
		
	}

	public void goto_stmt(Token goKeyword, Token toKeyword, Token label,
			Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void hollerith_constant(Token hollerithConstant) {
		// TODO Auto-generated method stub
		
	}

	public void if_construct() {
		// TODO Auto-generated method stub
		
	}

	public void if_stmt(Token label, Token ifKeyword) {
		// TODO Auto-generated method stub
		
	}

	public void if_stmt__begin() {
		// TODO Auto-generated method stub
		
	}

	public void if_then_stmt(Token label, Token id, Token ifKeyword,
			Token thenKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void image_selector(int exprCount) {
		// TODO Auto-generated method stub
		
	}

	public void implicit_spec() {
		// TODO Auto-generated method stub
		
	}

	public void implicit_stmt(Token label, Token implicitKeyword,
			Token noneKeyword, Token eos, boolean hasImplicitSpecList) {
		// TODO Auto-generated method stub
		
	}

	public void import_stmt(Token label, Token importKeyword, Token eos,
			boolean hasGenericNameList) {
		// TODO Auto-generated method stub
		
	}

	public void input_item() {
		// TODO Auto-generated method stub
		
	}

	public void inquire_spec(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void inquire_stmt(Token label, Token inquireKeyword, Token id,
			Token eos, boolean isType2) {
		// TODO Auto-generated method stub
		
	}

	public void int_variable() {
		// TODO Auto-generated method stub
		
	}

	public void intent_spec(Token intentKeyword1, Token intentKeyword2,
			int intent) {
		// TODO Auto-generated method stub
		
	}

	public void intent_stmt(Token label, Token keyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void interface_block() {
		// TODO Auto-generated method stub
		
	}

	public void interface_body(boolean hasPrefix) {
		// TODO Auto-generated method stub
		
	}

	public void interface_specification() {
		// TODO Auto-generated method stub
		
	}

	public void interface_stmt(Token label, Token abstractToken, Token keyword,
			Token eos, boolean hasGenericSpec) {
		// TODO Auto-generated method stub
		
	}

	public void interface_stmt__begin() {
		// TODO Auto-generated method stub
		
	}

	public void internal_subprogram() {
		// TODO Auto-generated method stub
		
	}

	public void intrinsic_operator() {
		// TODO Auto-generated method stub
		
	}

	public void intrinsic_stmt(Token label, Token intrinsicToken, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void intrinsic_type_spec(Token keyword1, Token keyword2, int type,
			boolean hasKindSelector) {
		// TODO Auto-generated method stub
		
	}

	public void io_implied_do() {
		// TODO Auto-generated method stub
		
	}

	public void io_implied_do_control() {
		// TODO Auto-generated method stub
		
	}

	public void io_implied_do_object() {
		// TODO Auto-generated method stub
		
	}

	public void io_unit() {
		// TODO Auto-generated method stub
		
	}

	public void keyword() {
		// TODO Auto-generated method stub
		
	}

	public void kind_param(Token kind) {
		// TODO Auto-generated method stub
		
	}

	public void kind_selector(Token token1, Token token2, boolean hasExpression) {
		// TODO Auto-generated method stub
		
	}

	public void label_do_stmt(Token label, Token id, Token doKeyword,
			Token digitString, Token eos, boolean hasLoopControl) {
		// TODO Auto-generated method stub
		
	}

	public void language_binding_spec(Token keyword, Token id, boolean hasName) {
		// TODO Auto-generated method stub
		
	}

	public void length_selector(Token len, int kindOrLen, boolean hasAsterisk) {
		// TODO Auto-generated method stub
		
	}

	public void letter_spec(Token id1, Token id2) {
		// TODO Auto-generated method stub
		
	}

	public void literal_constant() {
		// TODO Auto-generated method stub
		
	}

	public void logical_literal_constant(Token logicalValue, boolean isTrue,
			Token kindParam) {
		// TODO Auto-generated method stub
		
	}

	public void logical_variable() {
		// TODO Auto-generated method stub
		
	}

	public void loop_control(Token whileKeyword, boolean hasOptExpr) {
		// TODO Auto-generated method stub
		
	}

	public void masked_elsewhere_stmt(Token label, Token elseKeyword,
			Token whereKeyword, Token id, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void masked_elsewhere_stmt__end(int numBodyConstructs) {
		// TODO Auto-generated method stub
		
	}

	public void module() {
		// TODO Auto-generated method stub
		
	}

	public void module_nature(Token nature) {
		// TODO Auto-generated method stub
		
	}

	public void module_stmt(Token label, Token moduleKeyword, Token id,
			Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void module_stmt__begin() {
		// TODO Auto-generated method stub
		
	}

	public void module_subprogram(boolean hasPrefix) {
		// TODO Auto-generated method stub
		
	}

	public void module_subprogram_part() {
		// TODO Auto-generated method stub
		
	}

	public void mult_op(Token multKeyword) {
		// TODO Auto-generated method stub
		
	}

	public void name(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void named_constant_def(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void namelist_group_name(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void namelist_group_object(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void namelist_stmt(Token label, Token keyword, Token eos, int count) {
		// TODO Auto-generated method stub
		
	}

	public void not_op(Token notOp) {
		// TODO Auto-generated method stub
		
	}

	public void nullify_stmt(Token label, Token nullifyKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void only() {
		// TODO Auto-generated method stub
		
	}

	public void open_stmt(Token label, Token openKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void optional_stmt(Token label, Token keyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void or_op(Token orOp) {
		// TODO Auto-generated method stub
		
	}

	public void output_item() {
		// TODO Auto-generated method stub
		
	}

	public void parameter_stmt(Token label, Token keyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void pause_stmt(Token label, Token pauseKeyword, Token constant,
			Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void pointer_assignment_stmt(Token label, Token eos,
			boolean hasBoundsSpecList, boolean hasBRList) {
		// TODO Auto-generated method stub
		
	}

	public void pointer_decl(Token id, boolean hasSpecList) {
		// TODO Auto-generated method stub
		
	}

	public void pointer_object() {
		// TODO Auto-generated method stub
		
	}

	public void pointer_stmt(Token label, Token keyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void position_spec(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void power_op(Token powerKeyword) {
		// TODO Auto-generated method stub
		
	}

	public void prefix(int specCount) {
		// TODO Auto-generated method stub
		
	}

	public void prefix_spec(boolean isDecTypeSpec) {
		// TODO Auto-generated method stub
		
	}

	public void print_stmt(Token label, Token printKeyword, Token eos,
			boolean hasOutputItemList) {
		// TODO Auto-generated method stub
		
	}

	public void private_components_stmt(Token label, Token privateKeyword,
			Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void private_or_sequence() {
		// TODO Auto-generated method stub
		
	}

	public void proc_attr_spec(Token attrKeyword, Token id, int spec) {
		// TODO Auto-generated method stub
		
	}

	public void proc_binding_stmt(Token label, int type, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void proc_component_attr_spec(Token attrSpecKeyword, Token id,
			int specType) {
		// TODO Auto-generated method stub
		
	}

	public void proc_component_def_stmt(Token label, Token procedureKeyword,
			Token eos, boolean hasInterface) {
		// TODO Auto-generated method stub
		
	}

	public void proc_language_binding_spec() {
		// TODO Auto-generated method stub
		
	}

	public void proc_pointer_object() {
		// TODO Auto-generated method stub
		
	}

	public void procedure_declaration_stmt(Token label, Token procedureKeyword,
			Token eos, boolean hasProcInterface, int count) {
		// TODO Auto-generated method stub
		
	}

	public void procedure_designator() {
		// TODO Auto-generated method stub
		
	}

	public void procedure_stmt(Token label, Token module,
			Token procedureKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void program_stmt(Token label, Token programKeyword, Token id,
			Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void protected_stmt(Token label, Token keyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void read_stmt(Token label, Token readKeyword, Token eos,
			boolean hasInputItemList) {
		// TODO Auto-generated method stub
		
	}

	public void real_literal_constant(Token realConstant, Token kindParam) {
		// TODO Auto-generated method stub
		
	}

	public void rel_op(Token relOp) {
		// TODO Auto-generated method stub
		
	}

	public void rename(Token id1, Token id2, Token op1, Token defOp1,
			Token op2, Token defOp2) {
		// TODO Auto-generated method stub
		
	}

	public void result_name() {
		// TODO Auto-generated method stub
		
	}

	public void return_stmt(Token label, Token keyword, Token eos,
			boolean hasScalarIntExpr) {
		// TODO Auto-generated method stub
		
	}

	public void rewind_stmt(Token label, Token rewindKeyword, Token eos,
			boolean hasPositionSpecList) {
		// TODO Auto-generated method stub
		
	}

	public void save_stmt(Token label, Token keyword, Token eos,
			boolean hasSavedEntityList) {
		// TODO Auto-generated method stub
		
	}

	public void saved_entity(Token id, boolean isCommonBlockName) {
		// TODO Auto-generated method stub
		
	}

	public void scalar_char_constant() {
		// TODO Auto-generated method stub
		
	}

	public void scalar_constant() {
		// TODO Auto-generated method stub
		
	}

	public void scalar_default_char_variable() {
		// TODO Auto-generated method stub
		
	}

	public void scalar_default_logical_variable() {
		// TODO Auto-generated method stub
		
	}

	public void scalar_int_constant() {
		// TODO Auto-generated method stub
		
	}

	public void scalar_int_literal_constant() {
		// TODO Auto-generated method stub
		
	}

	public void scalar_int_variable() {
		// TODO Auto-generated method stub
		
	}

	public void select_case_stmt(Token label, Token id, Token selectKeyword,
			Token caseKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void select_type(Token selectKeyword, Token typeKeyword) {
		// TODO Auto-generated method stub
		
	}

	public void select_type_construct() {
		// TODO Auto-generated method stub
		
	}

	public void select_type_stmt(Token label, Token selectConstructName,
			Token associateName, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void selector() {
		// TODO Auto-generated method stub
		
	}

	public void sequence_stmt(Token label, Token sequenceKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void specific_binding(Token procedureKeyword, Token interfaceName,
			Token bindingName, Token procedureName, boolean hasBindingAttrList) {
		// TODO Auto-generated method stub
		
	}

	public void specification_part(int numUseStmts, int numImportStmts,
			int numDeclConstructs) {
		// TODO Auto-generated method stub
		
	}

	public void specification_stmt() {
		// TODO Auto-generated method stub
		
	}

	public void start_of_file(String fileName) {
		// TODO Auto-generated method stub
		
	}

	public void stmt_function_stmt(Token label, Token functionName, Token eos,
			boolean hasGenericNameList) {
		// TODO Auto-generated method stub
		
	}

	public void stmt_label_list() {
		// TODO Auto-generated method stub
		
	}

	public void stop_stmt(Token label, Token stopKeyword, Token eos,
			boolean hasStopCode) {
		// TODO Auto-generated method stub
		
	}

	public void structure_constructor(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void subroutine_stmt(Token label, Token keyword, Token name,
			Token eos, boolean hasPrefix, boolean hasDummyArgList,
			boolean hasBindingSpec, boolean hasArgSpecifier) {
		// TODO Auto-generated method stub
		
	}

	public void subroutine_stmt__begin() {
		// TODO Auto-generated method stub
		
	}

	public void substr_range_or_arg_list_suffix() {
		// TODO Auto-generated method stub
		
	}

	public void substring_range_or_arg_list() {
		// TODO Auto-generated method stub
		
	}

	public void suffix(Token resultKeyword, boolean hasProcLangBindSpec) {
		// TODO Auto-generated method stub
		
	}

	public void t_prefix(int specCount) {
		// TODO Auto-generated method stub
		
	}

	public void t_prefix_spec(Token spec) {
		// TODO Auto-generated method stub
		
	}

	public void target_decl(Token id, boolean hasArraySpec,
			boolean hasCoArraySpec) {
		// TODO Auto-generated method stub
		
	}

	public void target_stmt(Token label, Token keyword, Token eos, int count) {
		// TODO Auto-generated method stub
		
	}

	public void type_attr_spec(Token keyword, Token id, int specType) {
		// TODO Auto-generated method stub
		
	}

	public void type_bound_procedure_part(int count,
			boolean hasBindingPrivateStmt) {
		// TODO Auto-generated method stub
		
	}

	public void type_declaration_stmt(Token label, int numAttributes, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void type_guard_stmt(Token label, Token typeKeyword,
			Token isOrDefaultKeyword, Token selectConstructName, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void type_param_attr_spec(Token kindOrLen) {
		// TODO Auto-generated method stub
		
	}

	public void type_param_decl(Token id, boolean hasInit) {
		// TODO Auto-generated method stub
		
	}

	public void type_param_or_comp_def_stmt(Token eos, int type) {
		// TODO Auto-generated method stub
		
	}

	public void type_param_or_comp_def_stmt_list() {
		// TODO Auto-generated method stub
		
	}

	public void type_param_spec(Token keyword) {
		// TODO Auto-generated method stub
		
	}

	public void type_spec() {
		// TODO Auto-generated method stub
		
	}

	public void use_stmt(Token label, Token useKeyword, Token id,
			Token onlyKeyword, Token eos, boolean hasModuleNature,
			boolean hasRenameList, boolean hasOnly) {
		// TODO Auto-generated method stub
		
	}

	public void value_stmt(Token label, Token keyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void vector_subscript() {
		// TODO Auto-generated method stub
		
	}

	public void volatile_stmt(Token label, Token keyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void wait_spec(Token id) {
		// TODO Auto-generated method stub
		
	}

	public void wait_stmt(Token label, Token waitKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void where_body_construct() {
		// TODO Auto-generated method stub
		
	}

	public void where_construct(int numConstructs, boolean hasMaskedElsewhere,
			boolean hasElsewhere) {
		// TODO Auto-generated method stub
		
	}

	public void where_construct_stmt(Token id, Token whereKeyword, Token eos) {
		// TODO Auto-generated method stub
		
	}

	public void where_stmt(Token label, Token whereKeyword) {
		// TODO Auto-generated method stub
		
	}

	public void where_stmt__begin() {
		// TODO Auto-generated method stub
		
	}

	public void write_stmt(Token label, Token writeKeyword, Token eos,
			boolean hasOutputItemList) {
		// TODO Auto-generated method stub
		
	}

}