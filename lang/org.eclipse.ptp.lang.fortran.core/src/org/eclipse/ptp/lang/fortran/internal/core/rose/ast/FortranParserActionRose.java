package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.Token;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.ptp.lang.fortran.core.parser.FortranLexer;
import org.eclipse.ptp.lang.fortran.core.parser.FortranParser;
import org.eclipse.ptp.lang.fortran.core.parser.IFortranParserAction;
import org.eclipse.ptp.lang.fortran.core.parser.IFortranParserAction.IntrinsicTypeSpec;
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

	/** R1101
	 * main_program
	 * 	:	( program_stmt )?
	 * 		specification_part
	 * 		( execution_part )?
	 * 		( internal_subprogram_part )?
	 * 		end_program_stmt
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


	/** R403
	 * intrinsic_type_spec
	 */
	public void intrinsic_type_spec(IntrinsicTypeSpec type, boolean hasKindSelector) {
		System.out.print("R 403:intrinsic-type-spec: type=" + type);
		System.out.println(" hasKindSelector=" + hasKindSelector);
		
		SgExpression kind_selector = null;
		if (hasKindSelector) {
			kind_selector = (SgExpression) astStack.pop();
		}

		SgType sg_type = null;
		// TODO - implement other types
		switch(type) {
		case INTEGER: sg_type = SgTypeInt.createType(); break;
		case REAL: break;
		case DOUBLEPRECISION: break;
		case COMPLEX: break;
		case CHARACTER: break;
		case LOGICAL: break;
		default: break; // error condition
		}
		astStack.push(sg_type);
		// TODO - handle kind_selector properly (add a component to the type?)
		if (hasKindSelector) {
			// sg_type.set_kindSelector(kind_selector);
		}
	}

	/** R406
	 * int_literal_constant
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

	/** R501
	 * type_declaration_stmt
	 */
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

	/**
	 * entity_decl_list
	 * 	:    entity_decl ( T_COMMA entity_decl )*
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
	 */
	public void designator() {
		System.out.println("R 603:designator:");	
	}
	
	/** R612
	 *	data_ref
	 */
	public void data_ref(int numPartRef) {
		System.out.println("R 612:data-ref: numPartRef=" + numPartRef);
	}

	/** R613, R613-F2008
	 * part_ref
	 */
	public void part_ref(Token id) {
		System.out.println("R 613:part-ref: id=" + id);
		// TODO - figure out what to do here
		astStack.push(id);
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
		if (definedUnaryOp != null) {
			System.out.println("R 702:level-1-expr: definedUnaryOp=" + definedUnaryOp);
		}
	}
	
	/** R704: note, inserted as R704 functionality
	 * power_operand
	 *	: level_1_expr (power_op power_operand)?
	 */
	public void power_operand(boolean hasPowerOperand) {
		if (hasPowerOperand) {
			System.out.println("R 704:power-operand: hasPowerOperand=" + hasPowerOperand);
		}		
	}

	/** R704: note, see power_operand
	 * mult_operand
	 *  : power_operand (mult_op power_operand)*
	 *  
	 *  @param numPowerOperands The number of optional power_operands
	 */
	public void mult_operand(int numPowerOperands) {
		if (numPowerOperands > 0) {
			System.out.println("R 704:mult-operand: numPowerOperands=" + numPowerOperands);
		}	
	}

	/** R705: note, moved leading optionals to mult_operand
	 * add_operand
	 *  : (add_op)? mult_operand (add_op mult_operand)*
	 *  
	 *  @param addOp Optional add_op for this operand
	 *  @param numMultOperands The number of optional mult_operands
	 */
	public void add_operand(Token addOp, int numMultOperands) {
		if (numMultOperands > 0) {
			System.out.print("R 705:add-operand:");
			System.out.print(" addOp=" + addOp);
			System.out.println(" numMultOperands=" + numMultOperands);
		}
	}

	public void add_operand__add_op(Token addOp) {
		System.out.println("R705a:add-operand__add-op: addOp=" + addOp);
		buildExpressionBinaryOperator(addOp);
	}

	/** R706: note, moved leading optionals to add_operand
	 * level_2_expr
	 *  : add_operand (concat_op add_operand)*
	 *  
	 *  @param numAddOperands The number of optional add_operands
	 */
	public void level_2_expr(int numAddOperands) {
		if (numAddOperands > 0) {
			System.out.println("R 706:level-2-expr: numMultOperands=" + numAddOperands);
		}
	}

	/** R710: note, moved leading optional to level_2_expr
	 * level_3_expr
	 *  : level_2_expr (rel_op level_2_expr)?
	 *  
	 *  @param hasLevel2Expr True if optional level_2_expr is present
	 */
	public void level_3_expr(boolean hasLevel2Expr) {
		if (hasLevel2Expr) {
			System.out.println("R 710:level-3-expr: hasLevel2Expr=" + hasLevel2Expr);
		}		
	}

	/** R714
	 * and_operand
	 *  :    (not_op)? level_4_expr (and_op level_4_expr)*
	 *
	 *  @param hasNotOp True if optional not_op is present
	 *  @param numLevel4Exprs The number of optional level_4_exprs
	 */
	public void and_operand(boolean hasNotOp, int numLevel4Exprs) {
		if (numLevel4Exprs > 0) {
			System.out.print("R 714:and-operand:");
			System.out.print(" hasNotOp=" + hasNotOp);
			System.out.println(" numLevel4Exprs=" + numLevel4Exprs);
		}
	}

	/** R715: note, moved leading optional to or_operand
	 * or_operand
	    : and_operand (or_op and_operand)*
	 */
	public void or_operand(int numAndOperands) {
		if (numAndOperands > 0) {
			System.out.println("R 715:or-operand: numAndOperands=" + numAndOperands);
		}	
	}

	/** R716: note, moved leading optional to or_operand
	 * equiv_operand
	 *  : or_operand (equiv_op or_operand)*
	 *  
	 *  @param numOrOperands The number of optional or_operands
	 */
	public void equiv_operand(int numOrOperands) {
		if (numOrOperands > 0) {
			System.out.println("R 716:equiv-operand: numOrOperands=" + numOrOperands);
		}
	}

	/** R717: note, moved leading optional to equiv_operand
	 * level_5_expr
	 *  : equiv_operand (defined_binary_op equiv_operand)*
	 *  
	 *  @param numEquivOperands The number of optional equiv_operands
	 */
	public void level_5_expr(int numEquivOperands) {
		if (numEquivOperands > 0) {
			System.out.println("R 717:level-5-expr: numEquivOperands=" + numEquivOperands);
		}
	}

	/** R722: note, moved leading optional to level_5_expr
	 * expr
	 *  : level_5_expr
	 */
	public void expr() {
		// System.out.println("R 722:expr:");
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

	/** R1102
	 * program_stmt
	 * 
	 * Could build a SgProgramHeaderStatement and push it on the stack.  But it has
	 * already been created by main_program__begin, so just push the two tokens.
	 */
	public void program_stmt(Token label, Token id) {
		astStack.push(label);
		astStack.push(id);
	}

	/** R1103
	 * end_program_stmt
	 * 
	 */
	public void end_program_stmt(Token label, Token id) {
		System.out.print("R1103:end-program-stmt:");
		if (label != null) System.out.print(" label=" + label);
		System.out.println();

		astStack.push(label);
		astStack.push(id);
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

	public void buildIntrinsicTypeSpec(IntrinsicTypeSpec type, KindSelector kind) {
		// TODO need to pop expr from stack if kind == expression
		IASTExpression expr2 = (IASTExpression) astStack.pop();
		
	}

	public void buildExpressionBinaryOperator(Token op) {
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

}
