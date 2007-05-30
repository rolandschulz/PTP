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

package org.eclipse.ptp.lang.fortran.internal.core.dom.parser;

import java.util.Iterator;

import org.antlr.runtime.Token;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

import org.eclipse.cdt.internal.core.dom.parser.c.CASTArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTArraySubscriptExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTConditionalExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDefaultStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDesignatedInitializer;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDoStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTEnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionList;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFieldDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFieldDesignator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTInitializerList;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTLabelStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTModifiedArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTNullStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTPointer;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTProblem;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTProblemDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTProblemExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTProblemStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeIdInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypedefNameSpecifier;

import org.eclipse.ptp.lang.fortran.core.parser.FortranParser;
import org.eclipse.ptp.lang.fortran.core.parser.IFortranParserAction;
import org.eclipse.ptp.lang.fortran.core.parser.IFortranParserAction.KindParam;
import org.eclipse.ptp.lang.fortran.core.parser.IFortranParserAction.LiteralConstant;


/**
 * Actions called by the parser to build an AST.
 * 
 * @author crasmussen
 */
public class FortranParserActionDom implements IFortranParserAction {

	private static final char[] EMPTY_CHAR_ARRAY = {};
	
	// Stack that holds the intermediate ASt
	private ASTStack astStack = new ASTStack();
	
	
	private FortranParser parser = null;
	
	/** The offset and length of the rule that is in the process of being consumed */
	protected int ruleOffset, ruleLength;
	
	
	public FortranParserActionDom(FortranParser parser) {
		this.parser = parser;
	}
	
	
	/**
	 * Returns an AST after a successful parse, null otherwise.
	 */
	public IASTTranslationUnit getAST() {
		if(astStack.isEmpty())
			return null;
		return (IASTTranslationUnit) astStack.peek();
	}
	
	
	/**
	 * Method that is called by the special <openscope> production
	 * in order to create a new scope in the AST stack.
	 * 
	 */
	protected void openASTScope() {
		astStack.openASTScope();
	}
	
	
	
	/**
	 * Creates a IASTName node from an identifier token.
	 * 
	 */
	private static FortranASTName createName(Token token) {
		FortranASTName name = new FortranASTName(token.toString().toCharArray());
		name.setOffsetAndLength(offset(token), length(token));
		return name;
	}
	
	
	
	public static int offset(Token token) {
		// TODO - fix line/character info;
		return 80*(token.getLine() - 1) + token.getCharPositionInLine();
	}
	
	private static int offset(IASTNode node) {
		return ((ASTNode)node).getOffset();
	}
	
	public static int length(Token token) {
		return token.getText().length();
	}
	
	private static int length(IASTNode node) {
		return ((ASTNode)node).getLength();
	}
	
	private static int endOffset(IASTNode node) {
		return offset(node) + length(node);
	}
	
	private static int endOffset(Token token) {
		// TODO - return token.getEndOffset() + 1;
		return 0;
	}
	
	/********************************************************************
	 * Start of semantic actions.
	 ********************************************************************/
	
	
	
	/**
	 * Special action that is always called before every build action.
	 */
	protected void beforeConsume() {
		// TODO ruleOffset = parser.getLeftIToken().getStartOffset();
		// TODO ruleLength = parser.getRightIToken().getEndOffset() + 1 - ruleOffset;
	}
	
	
	/**
	 * Consumes a name from an identifer.
	 * Used by several grammar rules.
	 * 
	 */
	protected void consumeName() {
		IASTName name = createName( parser.getRightIToken() );
		astStack.push(name);
	}
	
	
	/**
	 * Gets the current token and places it on the stack for later consumption.
	 */
	protected void consumeToken() {
		astStack.push(parser.getRightIToken());
	}
	
	
	/**
	 * @see org.eclipse.ptp.lang.fortran.core.parser.IFortranParserAction#buildExpressionConstant(LiteralConstant, Token, KindParam, Token)
	 */
	public void buildExpressionConstant(LiteralConstant kind, Token cToken, KindParam kindType, Token ktToken) {
		FortranASTLiteralExpression expr = new FortranASTLiteralExpression(kind, cToken, kindType, ktToken);
		astStack.push(expr);
	}
	
	
	/**
	 * primary_expression ::= 'identifier'
	 */
	public void buildExpressionID() {
		FortranASTIdExpression expr = new FortranASTIdExpression();
		FortranASTName name = createName(parser.getRightIToken());
		expr.setName(name);
		name.setParent(expr);
		name.setPropertyInParent(IASTIdExpression.ID_NAME);
        expr.setOffsetAndLength(name);
        astStack.push(expr);
	}
	
	
	/**
	 * multiplicative_expression ::= multiplicative_expression '*' cast_expression
	 * TODO - replace C grammar with Fortran
	 * 
	 * @param op Field from IASTBinaryExpression
	 */
	public void buildExpressionBinaryOperator(int op) {
		IASTExpression expr2 = (IASTExpression) astStack.pop();
		IASTExpression expr1 = (IASTExpression) astStack.pop();
		
		FortranASTBinaryExpression binExpr = new FortranASTBinaryExpression();
		binExpr.setOperator(op);
		
		binExpr.setOperand1(expr1);
		expr1.setParent(binExpr);
		expr1.setPropertyInParent(IASTBinaryExpression.OPERAND_ONE);
		
		binExpr.setOperand2(expr2);
		expr2.setParent(binExpr);
		expr2.setPropertyInParent(IASTBinaryExpression.OPERAND_TWO);
		
		binExpr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(binExpr);
	}
	
	
	/**
	 * conditional_expression ::= logical_OR_expression '?' expression ':' conditional_expression
	 */
	protected void buildExpressionConditional() {
		IASTExpression expr3 = (IASTExpression) astStack.pop();
		IASTExpression expr2 = (IASTExpression) astStack.pop();
		IASTExpression expr1 = (IASTExpression) astStack.pop();
		
		CASTConditionalExpression condExpr = new CASTConditionalExpression();
		
		condExpr.setLogicalConditionExpression(expr1);
		expr1.setParent(condExpr);
		expr1.setPropertyInParent(IASTConditionalExpression.LOGICAL_CONDITION);
		
		condExpr.setPositiveResultExpression(expr2);
		expr2.setParent(condExpr);
		expr2.setPropertyInParent(IASTConditionalExpression.POSITIVE_RESULT);
		
		condExpr.setNegativeResultExpression(expr3);
		expr3.setParent(condExpr);
		expr3.setPropertyInParent(IASTConditionalExpression.NEGATIVE_RESULT);
		
		condExpr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(condExpr);
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '[' expression ']'
	 */
	protected void buildExpressionArraySubscript() {
		CASTArraySubscriptExpression expr = new CASTArraySubscriptExpression();
		
		IASTExpression subscript = (IASTExpression) astStack.pop();
		IASTExpression arrayExpr = (IASTExpression) astStack.pop();
		
		expr.setArrayExpression(arrayExpr);
		arrayExpr.setParent(expr);
		arrayExpr.setPropertyInParent(IASTArraySubscriptExpression.ARRAY);
		
		expr.setSubscriptExpression(subscript);
		subscript.setParent(expr);
		arrayExpr.setPropertyInParent(IASTArraySubscriptExpression.SUBSCRIPT);
		
		expr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(expr);
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '(' argument_expression_list ')'
	 * postfix_expression ::= postfix_expression '(' ')'
	 */
	protected void buildExpressionFunctionCall(boolean hasArgs) {
		FortranASTFunctionCallExpression expr = new FortranASTFunctionCallExpression();
		
		if(hasArgs) {
			CASTExpressionList argList = (CASTExpressionList) astStack.pop();
			
			expr.setParameterExpression(argList);
			argList.setParent(expr);
			argList.setPropertyInParent(IASTFunctionCallExpression.PARAMETERS);
		}
		
		IASTExpression idExpr  = (IASTExpression) astStack.pop();
		
		expr.setFunctionNameExpression(idExpr);
		idExpr.setParent(expr);
		idExpr.setPropertyInParent(IASTFunctionCallExpression.FUNCTION_NAME);

		expr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(expr);
	}
	
	
	/**
	 * argument_expression_list
     *     ::= assignment_expression  -- base case
     *       | argument_expression_list ',' assignment_expression
	 * 
	 */
	protected void buildExpressionArgumentExpressionList(boolean baseCase) {
		IASTExpression argumentExpression = (IASTExpression) astStack.pop();
		
		CASTExpressionList argList;
		if(baseCase) {
			argList = new CASTExpressionList();
			argList.setOffset(ruleOffset);
			astStack.push(argList);
		}
		else {
			argList = (CASTExpressionList) astStack.peek();
		}
		
		argList.addExpression(argumentExpression);
		argumentExpression.setParent(argList);
		argumentExpression.setPropertyInParent(IASTExpressionList.NESTED_EXPRESSION);
		argList.setLength(ruleLength);
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '.' 'identifier'
	 * postfix_expression ::= postfix_expression '->' 'identifier'
	 */
	protected void buildExpressionFieldReference(boolean isPointerDereference) {
		IASTExpression idExpression = (IASTExpression) astStack.pop();
		
		CASTFieldReference expr = new CASTFieldReference();
		expr.setIsPointerDereference(isPointerDereference);
		
		Token identifier = parser.getRightIToken();
		IASTName name = createName(identifier);
		
		expr.setFieldName(name);
		name.setParent(expr);
		name.setPropertyInParent(IASTFieldReference.FIELD_NAME);
		
		expr.setFieldOwner(idExpression);
		idExpression.setParent(expr);
		idExpression.setPropertyInParent(IASTFieldReference.FIELD_OWNER);
		
		expr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(expr);
	}
	
	
	
	/**
	 * postfix_expression ::= postfix_expression '++'
	 * postfix_expression ::= postfix_expression '__'
	 * 
	 * unary_expression ::= '++' unary_expression
	 * unary_expression ::= '__' unary_expression
	 * unary_expression ::= '&' cast_expression
	 * unary_expression ::= '*' cast_expression
	 * unary_expression ::= '+' cast_expression
	 * unary_expression ::= '_' cast_expression
	 * unary_expression ::= '~' cast_expression
	 * unary_expression ::= '!' cast_expression
	 * unary_expression ::= 'sizeof' unary_expression
	 * 
	 * @param operator From IASTUnaryExpression
	 */
	protected void buildExpressionUnaryOperator(int operator) {
		IASTExpression operand = (IASTExpression) astStack.pop();
		
		FortranASTUnaryExpression expr = new FortranASTUnaryExpression();
		expr.setOperator(operator);
		
		expr.setOperand(operand);
		operand.setParent(expr);
		operand.setPropertyInParent(IASTUnaryExpression.OPERAND);
		
		expr.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(expr);
	}
	
	
	/**
	 * postfix_expression ::= '(' type_name ')' '{' <openscope> initializer_list '}'
	 * postfix_expression ::= '(' type_name ')' '{' <openscope> initializer_list ',' '}'            
	 */
	protected void buildExpressionTypeIdInitializer() {
		buildInitializerList(); // closes the scope
		
		IASTInitializerList list = (IASTInitializerList) astStack.pop();
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		
		CASTTypeIdInitializerExpression expr = new CASTTypeIdInitializerExpression();
		
		expr.setInitializer(list);
		list.setParent(expr);
		list.setPropertyInParent(ICASTTypeIdInitializerExpression.INITIALIZER);
		
		expr.setTypeId(typeId);
		typeId.setParent(expr);
		typeId.setPropertyInParent(ICASTTypeIdInitializerExpression.TYPE_ID);
	
		expr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(expr);
	}
	
	
	/**
	 * primary_expression ::= '(' expression ')'
	 * 
	 * TODO: should bracketed expressions cause a new node in the AST? whats the point?
	 */
	protected void buildExpressionBracketed() {
		FortranASTUnaryExpression expr = new FortranASTUnaryExpression();
		expr.setOperator(IASTUnaryExpression.op_bracketedPrimary);
		
		IASTExpression operand = (IASTExpression) astStack.pop();
		
		expr.setOperand(operand);
		operand.setParent(expr);
        operand.setPropertyInParent(IASTUnaryExpression.OPERAND);

        expr.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(expr);
	}
	
	
	
	/**
	 * expression ::= expression_list
	 * 
	 * In the case that an expression list consists of a single expression
	 * then discard the list.
	 */
	protected void buildExpression() {
		IASTExpressionList exprList = (IASTExpressionList) astStack.pop();
		IASTExpression[] expressions = exprList.getExpressions();
		if(expressions.length == 1) {
			astStack.push(expressions[0]);
		}
		else {
			astStack.push(exprList);
		}
	}
	
	
	
	/**
	 * expression_list
     *     ::= assignment_expression
     *       | expression_list ',' assignment_expression 
	 */
	protected void buildExpressionList(boolean baseCase) {
		IASTExpression expr = (IASTExpression) astStack.pop();
		
		CASTExpressionList exprList;
		if(baseCase) {
			exprList = new CASTExpressionList();
			exprList.setOffset(ruleOffset);
			astStack.push(exprList);
		}
		else {
			exprList = (CASTExpressionList) astStack.peek();
		}
		
		exprList.addExpression(expr);
		exprList.setLength(ruleLength);
		expr.setParent(exprList);
		expr.setPropertyInParent(IASTExpressionList.NESTED_EXPRESSION);
	}
	
	
	
	/**
	 * Sets a token specifier.
	 */
	private void setSpecifier(ICASTDeclSpecifier node, Token token) {
/*** TODO
		switch(token.getKind()){
			// storage_class_specifier
			case FortranParsersym.TK_typedef: 
				node.setStorageClass(IASTDeclSpecifier.sc_typedef); 
				return;
			case FortranParsersym.TK_extern: 
				node.setStorageClass(IASTDeclSpecifier.sc_extern); 
				return;
			case FortranParsersym.TK_static:
				node.setStorageClass(IASTDeclSpecifier.sc_static);
				return;
			case FortranParsersym.TK_auto:
				node.setStorageClass(IASTDeclSpecifier.sc_auto);
				return;
			case FortranParsersym.TK_register:
				node.setStorageClass(IASTDeclSpecifier.sc_register);
				return;
			// function_specifier
			case FortranParsersym.TK_inline:
				node.setInline(true);
				return;
			// type_qualifier
			case FortranParsersym.TK_const:
				node.setConst(true);
				return;
			case FortranParsersym.TK_restrict:
				node.setRestrict(true);
				return;
			case FortranParsersym.TK_volatile:
				node.setVolatile(true);
				return;
		}
***/
		
		// type_specifier
/*** TODO
		if(node instanceof ICASTSimpleDeclSpecifier)
		{
			ICASTSimpleDeclSpecifier n = (ICASTSimpleDeclSpecifier) node;
			switch(token.getKind()) {
				case FortranParsersym.TK_void:
					n.setType(IASTSimpleDeclSpecifier.t_void);
					break;
				case FortranParsersym.TK_char:
					n.setType(IASTSimpleDeclSpecifier.t_char);
					break;
				case FortranParsersym.TK_short:
					n.setShort(true);
					break;
				case FortranParsersym.TK_int:
					n.setType(IASTSimpleDeclSpecifier.t_int);
					break;
				case FortranParsersym.TK_long:
					boolean isLong = n.isLong();
					n.setLongLong(isLong);
					n.setLong(!isLong);
					break;
				case FortranParsersym.TK_float:
					n.setType(IASTSimpleDeclSpecifier.t_float);
					break;
				case FortranParsersym.TK_double:
					n.setType(IASTSimpleDeclSpecifier.t_double);
					break;
				case FortranParsersym.TK_signed:
					n.setSigned(true);
					break;
				case FortranParsersym.TK_unsigned:
					n.setUnsigned(true);
					break;
				case FortranParsersym.TK__Bool:
					n.setType(ICASTSimpleDeclSpecifier.t_Bool);
					break;
				case FortranParsersym.TK__Complex:
					n.setComplex(true);
					break;
				default:
					return;
			}
			//declSpecStack.setEncounteredType(true);
		}
***/
	}
	
	
	
	/**
	 * type_name ::= specifier_qualifier_list
     *             | specifier_qualifier_list abstract_declarator
	 */
	protected void buildTypeId(boolean hasDeclarator) {
		CASTTypeId typeId = new CASTTypeId();
		
		if(hasDeclarator) {
			IASTDeclarator declarator = (IASTDeclarator) astStack.pop();
			typeId.setAbstractDeclarator(declarator);
			declarator.setParent(typeId);
			declarator.setPropertyInParent(IASTTypeId.ABSTRACT_DECLARATOR);
		}
		
		IASTDeclSpecifier declSpecifier = (IASTDeclSpecifier) astStack.pop();
		
		typeId.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(typeId);
		declSpecifier.setPropertyInParent(IASTTypeId.DECL_SPECIFIER);
		
		typeId.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(typeId);
	}
	

	
	/**
	 * declarator ::= <openscope> pointer direct_declarator
     *              
     * abstract_declarator  -- a declarator that does not include an identifier
     *     ::= <openscope> pointer
     *       | <openscope> pointer direct_abstract_declarator 
	 */
	protected void buildDeclaratorWithPointer(boolean hasDeclarator) {
		CASTDeclarator decl;
		if(hasDeclarator) {	
			decl = (CASTDeclarator)astStack.pop();
		}
		else {
			decl = new CASTDeclarator();
			IASTName name = new FortranASTName();
			decl.setName(name);
			name.setParent(decl);
			name.setPropertyInParent(IASTDeclarator.DECLARATOR_NAME);
		}
		
		// add all the pointers to the declarator
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			ICASTPointer pointer = (ICASTPointer) iter.next();
			decl.addPointerOperator(pointer);
			pointer.setParent(decl);
			pointer.setPropertyInParent(IASTDeclarator.POINTER_OPERATOR);
			
		}
		astStack.closeASTScope();

		decl.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(decl);
	}
	
	
	
	/**
	 * Used by rules of the form:  direct_declarator ::= direct_declarator '[' < something > ']'
	 * Builds the direct_declarator part, the array modifier (the square bracket part) must be provided.
	 * Returns true if there is no problem.
	 * There will be an IASTArrayDeclarator on the stack if this method returns true.
	 *  
	 * __ 5 possibilities
     *    __ identifier
     *       __ create new array declarator
     *    __ nested declarator
     *       __ create new array declarator
     *    __ array declarator
     *       __ add this modifier to existing declarator
     *    __ function declarator
     *       __ problem
     *    __ problem
     *       __ problem
	 */
	private void buildDeclaratorArray(IASTArrayModifier arrayModifier) {
		ASTNode node = (ASTNode) astStack.pop();
		
		// Its a nested declarator so create an new ArrayDeclarator
		if(node.getPropertyInParent() == IASTDeclarator.NESTED_DECLARATOR) {
			FortranASTArrayDeclarator decl = new FortranASTArrayDeclarator();
			
			CASTDeclarator nested = (CASTDeclarator) node;
			decl.setNestedDeclarator(nested);
			nested.setParent(decl);
			//nested.setPropertyInParent(IASTArrayDeclarator.NESTED_DECLARATOR);
			
			int offset = nested.getOffset();
			int length = endOffset(arrayModifier) - offset;
			decl.setOffsetAndLength(offset, length);
			
			addArrayModifier(decl, arrayModifier);
			astStack.push(decl);
		}
		// There is already an array declarator so just add the modifier to it
		else if(node instanceof IASTArrayDeclarator) {
			FortranASTArrayDeclarator decl = (FortranASTArrayDeclarator) node;
			decl.setLength(endOffset(arrayModifier) - decl.getOffset());
			
			addArrayModifier(decl, arrayModifier);
			astStack.push(decl);
		}
		// The declarator is an identifier so create a new array declarator
		else if(node instanceof CASTDeclarator) {
			FortranASTArrayDeclarator decl = new FortranASTArrayDeclarator();
			
			FortranASTName name = (FortranASTName)((CASTDeclarator)node).getName();
			decl.setName(name);
			name.setParent(decl);
			name.setPropertyInParent(IASTArrayDeclarator.DECLARATOR_NAME);
		
			int offset = name.getOffset();
			int length = endOffset(arrayModifier) - offset;
			decl.setOffsetAndLength(offset, length);
			
			addArrayModifier(decl, arrayModifier);
			astStack.push(decl);
		}
		else {
			astStack.push(new CASTProblemDeclaration());
		}
	}
	
	
	private void addArrayModifier(IASTArrayDeclarator decl, IASTArrayModifier modifier) {
		decl.addArrayModifier(modifier);
		modifier.setParent(decl);
		modifier.setPropertyInParent(IASTArrayDeclarator.ARRAY_MODIFIER);
	}
	
	
	/**
	 * type_qualifier ::= const | restrict | volatile
	 */
	private void collectArrayModifierTypeQualifiers(CASTModifiedArrayModifier arrayModifier) {		
/*** TODO
		for (Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			Token token = (Token) iter.next();
			switch(token.getKind()) {
				case FortranParsersym.TK_const:
					arrayModifier.setConst(true);
					break;
				case FortranParsersym.TK_restrict:
					arrayModifier.setRestrict(true);
					break;
				case FortranParsersym.TK_volatile:
					arrayModifier.setVolatile(true);
					break;
			}
		}
***/
		astStack.closeASTScope();
	}
	
	
	/**
	 *  array_modifier 
     *      ::= '[' <openscope> type_qualifier_list ']'
     *        | '[' <openscope> type_qualifier_list assignment_expression ']'
     *        | '[' 'static' assignment_expression ']'
     *        | '[' 'static' <openscope> type_qualifier_list assignment_expression ']'
     *        | '[' <openscope> type_qualifier_list 'static' assignment_expression ']'
     *        | '[' '*' ']'
     *        | '[' <openscope> type_qualifier_list '*' ']'
     *        
     * The main reason to separate array_modifier into its own rule is to
     * make calculating the offset and length much easier.
	 */
	protected void buildDirectDeclaratorModifiedArrayModifier(boolean isStatic, 
			 boolean isVarSized, boolean hasTypeQualifierList, boolean hasAssignmentExpr) {
		
		assert isStatic || isVarSized || hasTypeQualifierList;
		
		CASTModifiedArrayModifier arrayModifier = new CASTModifiedArrayModifier();
		
		// build all the stuff between the square brackets into an array modifier
		arrayModifier.setStatic(isStatic);
		arrayModifier.setVariableSized(isVarSized);
		
		if(hasAssignmentExpr) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			arrayModifier.setConstantExpression(expr);
			expr.setParent(arrayModifier);
			expr.setPropertyInParent(ICASTArrayModifier.CONSTANT_EXPRESSION);
		}
		
		if(hasTypeQualifierList) {
			collectArrayModifierTypeQualifiers(arrayModifier);
		}

		arrayModifier.setOffsetAndLength(ruleOffset, ruleLength); // snap!
		astStack.push(arrayModifier);
	}
	
	
	/**
	 *  array_modifier 
	 *      ::= '[' ']' 
     *        | '[' assignment_expression ']'
     */        
	protected void buildDirectDeclaratorArrayModifier(boolean hasAssignmentExpr) {
		CASTArrayModifier arrayModifier = new CASTArrayModifier();
		
		if(hasAssignmentExpr) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			arrayModifier.setConstantExpression(expr);
			expr.setParent(arrayModifier);
			expr.setPropertyInParent(ICASTArrayModifier.CONSTANT_EXPRESSION);
		}
		
		arrayModifier.setOffsetAndLength(ruleOffset, ruleLength); // snap!
		astStack.push(arrayModifier);
	}
	
	
	/**
	 * direct_declarator ::= direct_declarator array_modifier
	 * 
	 * build the direct_declarator part and add the array modifier
	 */
	protected void buildDirectDeclaratorArrayDeclarator() {
		IASTArrayModifier arrayModifier = (IASTArrayModifier) astStack.pop();
		buildDeclaratorArray(arrayModifier);
	}
	
	
	/**
	 * direct_declarator ::= '(' declarator ')'
	 */
	protected void buildDirectDeclaratorBracketed() {
		IASTDeclarator decl = (IASTDeclarator) astStack.peek();
		decl.setPropertyInParent(IASTDeclarator.NESTED_DECLARATOR);
	}
	
	
	/**
	 * init_declarator ::= declarator '=' initializer
	 */
	protected void buildDeclaratorWithInitializer() {
		IASTInitializer expr = (IASTInitializer) astStack.pop();
		CASTDeclarator declarator = (CASTDeclarator) astStack.peek();
		
		declarator.setInitializer(expr);
		expr.setParent(declarator);
		expr.setPropertyInParent(IASTDeclarator.INITIALIZER);
		
		declarator.setLength(ruleLength);
	}
	
	
	/**
	 * direct_declarator ::= 'identifier'
	 */
	protected void buildDirectDeclaratorIdentifier() {
		FortranASTName name = createName(parser.getRightIToken());
		
		CASTDeclarator declarator = new CASTDeclarator();
		declarator.setName(name);
		name.setParent(declarator);
		name.setPropertyInParent(IASTDeclarator.DECLARATOR_NAME);
		
		declarator.setOffsetAndLength(name);
		
		astStack.push(declarator);
	}
	
	
	/**
	 * direct_declarator ::= direct_declarator '(' <openscope> parameter_type_list ')'
	 * direct_declarator ::= direct_declarator '(' ')'
	 */
	protected void buildDirectDeclaratorFunctionDeclarator(boolean hasParameters) {
		CASTFunctionDeclarator declarator = new CASTFunctionDeclarator();
		
		if(hasParameters) {
			for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
				CASTParameterDeclaration parameter = (CASTParameterDeclaration) iter.next();
				declarator.addParameterDeclaration(parameter);
				parameter.setParent(declarator);
				parameter.setPropertyInParent(IASTStandardFunctionDeclarator.FUNCTION_PARAMETER);
			}
			astStack.closeASTScope();
		}
		
		int endOffset = endOffset(parser.getRightIToken());
		buildDirectDeclaratorFunctionDeclarator(declarator, endOffset);
	}
	
	
	/**
	 * Pops a simple declarator from the stack, converts it into 
	 * a FunctionDeclator, then pushes it.
	 * 
	 * TODO: is this the best way of doing this?
	 * 
	 */
	private void buildDirectDeclaratorFunctionDeclarator(IASTFunctionDeclarator declarator, int endOffset) {
		IASTDeclarator decl = (IASTDeclarator) astStack.pop();
		 
		if(decl.getPropertyInParent() == IASTDeclarator.NESTED_DECLARATOR) {
			declarator.setNestedDeclarator(decl);
			decl.setParent(declarator);
			
			IASTName name = new FortranASTName();
			declarator.setName(name);
			name.setParent(declarator);
			name.setPropertyInParent(IASTFunctionDeclarator.DECLARATOR_NAME);
			
			int offset = ((ASTNode)decl).getOffset();
			((ASTNode)declarator).setOffsetAndLength(offset, endOffset - offset);
			astStack.push(declarator);
		}
		else if(decl instanceof CASTDeclarator) {
			FortranASTName name = (FortranASTName)((CASTDeclarator)decl).getName();
			declarator.setName(name);
			name.setParent(declarator);
			name.setPropertyInParent(IASTFunctionDeclarator.DECLARATOR_NAME);
			
			IASTPointerOperator[] pointers = decl.getPointerOperators();
			for(int i = 0; i < pointers.length; i++) {
				IASTPointerOperator pointer = pointers[i];
				declarator.addPointerOperator(pointer);
				pointer.setParent(declarator);
				pointer.setPropertyInParent(IASTFunctionDeclarator.POINTER_OPERATOR);
			}
			
			int offset = name.getOffset();
			((ASTNode)declarator).setOffsetAndLength(offset, endOffset - offset);
			
			astStack.push(declarator);
		}
		else {
			astStack.push(new CASTProblemDeclaration());
		}
	}
	
	
	/**
	 * pointer ::= '*'
     *           | pointer '*' 
     */ 
	protected void buildPointer() {
		CASTPointer pointer = new CASTPointer();
		Token star = parser.getRightIToken();
		pointer.setOffsetAndLength(offset(star), length(star));
		astStack.push(pointer);
	}
	
	
	/**
	 * pointer ::= '*' <openscope> type_qualifier_list
     *           | pointer '*' <openscope> type_qualifier_list
	 */
	protected void buildPointerTypeQualifierList() {
		CASTPointer pointer = new CASTPointer();

/*** TODO
		for (Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			Token token = (Token) iter.next();			
			switch(token.getKind()) {
				case FortranParsersym.TK_const:
					pointer.setConst(true);
					break;
				case FortranParsersym.TK_restrict:
					pointer.setRestrict(true);
					break;
				case FortranParsersym.TK_volatile:
					pointer.setVolatile(true);
					break;
			}
		}
***/
		astStack.closeASTScope();

		pointer.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(pointer);
	}
	
	
	/**
	 * parameter_declaration ::= declaration_specifiers declarator
     *                         | declaration_specifiers   
     *                         | declaration_specifiers abstract_declarator
	 */
	protected void buildParameterDeclaration(boolean hasDeclarator) {
		CASTParameterDeclaration declaration = new CASTParameterDeclaration();
		
		IASTDeclarator declarator;
		if(hasDeclarator) {
			declarator = (IASTDeclarator) astStack.pop();
		}
		else { // it appears that a declarator is always required in the AST here
			declarator = new CASTDeclarator();
			((ASTNode)declarator).setOffsetAndLength(ruleOffset + ruleLength, 0);
			FortranASTName name = new FortranASTName();
			name.setOffsetAndLength(ruleOffset + ruleLength, 0);
			declarator.setName(name);
			name.setParent(declarator);
			name.setPropertyInParent(IASTDeclarator.DECLARATOR_NAME);
		}
		
		declaration.setDeclarator(declarator);
		declarator.setParent(declaration);
		declarator.setPropertyInParent(IASTParameterDeclaration.DECLARATOR);
		
		IASTDeclSpecifier declSpecifier = (IASTDeclSpecifier) astStack.pop();
		declaration.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(declaration);
		declSpecifier.setPropertyInParent(IASTParameterDeclaration.DECL_SPECIFIER);
		
		declaration.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(declaration);
	}
	
	
	
	/**
	 * direct_abstract_declarator   
     *     ::= array_modifier
     *       | direct_abstract_declarator array_modifier
	 */
	protected void buildAbstractDeclaratorArrayModifier(boolean hasDeclarator) {
		IASTArrayModifier arrayModifier = (IASTArrayModifier) astStack.pop();
		
		if(hasDeclarator) {
			buildDeclaratorArray(arrayModifier);
		}
		else {
			FortranASTArrayDeclarator decl = new FortranASTArrayDeclarator();
			decl.addArrayModifier(arrayModifier);
			arrayModifier.setParent(decl);
			arrayModifier.setPropertyInParent(IASTArrayDeclarator.ARRAY_MODIFIER);
			
			decl.setOffsetAndLength(ruleOffset, ruleLength);
			astStack.push(decl);
		}
	}
	
	
	/**
	 * direct_abstract_declarator  
	 *     ::= '(' ')'
     *       | direct_abstract_declarator '(' ')'
     *       | '(' <openscope> parameter_type_list ')'
     *       | direct_abstract_declarator '(' <openscope> parameter_type_list ')'
	 */
	protected void buildAbstractDeclaratorFunctionDeclarator(boolean hasDeclarator, boolean hasParameters) {
		CASTFunctionDeclarator declarator = new CASTFunctionDeclarator();
		
		if(hasParameters) {
			for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
				CASTParameterDeclaration parameter = (CASTParameterDeclaration) iter.next();
				declarator.addParameterDeclaration(parameter);
				parameter.setParent(declarator);
				parameter.setPropertyInParent(IASTStandardFunctionDeclarator.FUNCTION_PARAMETER);
			}
			astStack.closeASTScope();
		}
		
		if(hasDeclarator) {
			buildDirectDeclaratorFunctionDeclarator(declarator, endOffset(parser.getRightIToken()));
		}
		else {
			declarator.setOffsetAndLength(ruleOffset, ruleLength);
			astStack.push(declarator);
		}
	}
	
	
	/**
	 * initializer ::= assignment_expression
	 */
	protected void buildInitializer() {
		IASTExpression assignmentExpression = (IASTExpression) astStack.pop();
		
		CASTInitializerExpression expr = new CASTInitializerExpression();
		
		expr.setExpression(assignmentExpression);
		assignmentExpression.setParent(expr);
        assignmentExpression.setPropertyInParent(IASTInitializerExpression.INITIALIZER_EXPRESSION);
        
        expr.setOffsetAndLength((ASTNode)assignmentExpression);
        
        astStack.push(expr);
	}
	
	
	/**
	 * initializer ::= '{' <openscope> initializer_list '}'
     *               | '{' <openscope> initializer_list ',' '}'
	 */
	protected void buildInitializerList() {
		CASTInitializerList list = new CASTInitializerList();
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTInitializer initializer = (IASTInitializer) iter.next();
			list.addInitializer(initializer);
			initializer.setParent(list);
			initializer.setPropertyInParent(IASTInitializerList.NESTED_INITIALIZER);
		}
		astStack.closeASTScope();
		
		list.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(list);
	}
	
	
	/**
	 * designated_initializer ::= <openscope> designation initializer
	 */
	protected void buildInitializerDesignated() {
		CASTDesignatedInitializer result = new CASTDesignatedInitializer();
		
		IASTInitializer initializer = (IASTInitializer)astStack.pop();
		result.setOperandInitializer(initializer);
		initializer.setParent(result);
		initializer.setPropertyInParent(ICASTDesignatedInitializer.OPERAND);
		
		// build the designation which is a list of designators
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			ICASTDesignator designator  = (ICASTDesignator)iter.next();
			result.addDesignator(designator);
			designator.setParent(result);
			designator.setPropertyInParent(ICASTDesignatedInitializer.DESIGNATOR);
		}
		astStack.closeASTScope();
		
		result.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(result);
	}
	
	
	/**
	 * designator ::= '[' constant_expression ']'
	 */
	protected void buildDesignatorArrayDesignator() {
		IASTExpression expr = (IASTExpression) astStack.pop();
		
		FortranASTArrayDesignator designator = new FortranASTArrayDesignator();
		designator.setSubscriptExpression(expr);
		expr.setParent(designator);
		expr.setPropertyInParent(ICASTArrayDesignator.SUBSCRIPT_EXPRESSION);
		
		designator.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(designator);
	}
	
	
	/**
	 *  designator ::= '.' 'identifier'
	 */
	protected void buildDesignatorFieldDesignator() {		
		CASTFieldDesignator designator = new CASTFieldDesignator();
		IASTName name = createName( parser.getRightIToken() );
		designator.setName(name);
		name.setParent(designator);
		name.setPropertyInParent(ICASTFieldDesignator.FIELD_NAME);
		
		designator.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(designator);
	}
	
	
	/**
	 * declaration_specifiers ::= <openscope> simple_declaration_specifiers
	 */
	protected void buildDeclarationSpecifiersSimple() {
		CASTSimpleDeclSpecifier declSpec = new CASTSimpleDeclSpecifier();
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			Token token = (Token) iter.next();
			setSpecifier(declSpec, token);
		}
		astStack.closeASTScope();
		
		declSpec.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(declSpec);
	}
	
	
	/**
	 * declaration_specifiers ::= <openscope> struct_or_union_declaration_specifiers
	 * declaration_specifiers ::= <openscope> enum_declaration_specifiers
	 */
	protected void buildDeclarationSpecifiersStructUnionEnum() {
		// There's already a composite type specifier somewhere on the stack, find it.
		ICASTDeclSpecifier declSpec = null;
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			Object o = iter.next();
			if(o instanceof ICASTDeclSpecifier) {
				declSpec = (ICASTDeclSpecifier) o;
				iter.remove();
				break;
			}
		}
		
		// now apply the rest of the specifiers
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			Token token = (Token) iter.next();
			setSpecifier(declSpec, token);
		}
		astStack.closeASTScope();
		
		((ASTNode)declSpec).setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(declSpec);
	}

	
	/**
	 * declaration ::= declaration_specifiers <openscope> init_declarator_list ';'
	 * declaration ::= declaration_specifiers  ';'
	 */
	protected void buildDeclaration(boolean hasDeclaratorList) {
		CASTSimpleDeclaration declaration = new CASTSimpleDeclaration();
		
		if(hasDeclaratorList) {
			for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
				IASTDeclarator declarator = (IASTDeclarator)iter.next();
				declaration.addDeclarator(declarator);
				declarator.setParent(declaration);
				declarator.setPropertyInParent(IASTSimpleDeclaration.DECLARATOR);
			}
			astStack.closeASTScope();
		}
		
		ICASTDeclSpecifier declSpecifier = (ICASTDeclSpecifier) astStack.pop();
		
		declaration.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(declaration);
		declSpecifier.setPropertyInParent(IASTSimpleDeclaration.DECL_SPECIFIER);
		
		declaration.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(declaration);
	}
	
	
	
	/**
	 * struct_declaration ::= specifier_qualifier_list <openscope> struct_declarator_list ';'
	 * 
	 * specifier_qualifier_list is a subset of declaration_specifiers,
	 * struct_declarators are declarators that are allowed inside a struct,
	 * a struct declarator is a regular declarator plus bit fields
	 */
	protected void buildStructDeclaration(boolean hasDeclaration) {
		buildDeclaration(hasDeclaration); // TODO this is ok as long as bit fields implement IASTDeclarator (see buildDeclaration())
	} 
	
	
	/**
	 * struct_declarator
     *     ::= ':' constant_expression  
     *       | declarator ':' constant_expression		
	 */
	protected void buildStructBitField(boolean hasDeclarator) {
		IASTExpression expr = (IASTExpression)astStack.pop();
		
		CASTFieldDeclarator fieldDecl = new CASTFieldDeclarator();
		fieldDecl.setBitFieldSize(expr);
		expr.setParent(fieldDecl);
		expr.setPropertyInParent(IASTFieldDeclarator.FIELD_SIZE);
		
		if(hasDeclarator) { // it should have been parsed into a regular declarator
			IASTDeclarator decl = (IASTDeclarator) astStack.pop();
			IASTName name = decl.getName();
			fieldDecl.setName(name);
			name.setParent(fieldDecl);
			name.setPropertyInParent(IASTFieldDeclarator.DECLARATOR_NAME);
		}
		
		fieldDecl.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(fieldDecl);
	}
	
	
	
	/**
	 * struct_or_union_specifier
     *     ::= 'struct' '{' <openscope> struct_declaration_list_opt '}'
     *       | 'union'  '{' <openscope> struct_declaration_list_opt '}'
     *       | 'struct' struct_or_union_identifier '{' <openscope> struct_declaration_list_opt '}'
     *       | 'union'  struct_or_union_identifier '{' <openscope> struct_declaration_list_opt '}'
	 * 
	 * @param key either k_struct or k_union from IASTCompositeTypeSpecifier
	 */
	protected void buildTypeSpecifierComposite(boolean hasName, int key) {
		CASTCompositeTypeSpecifier typeSpec = new CASTCompositeTypeSpecifier();
		typeSpec.setKey(key); // struct or union
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTDeclaration declaration = (IASTDeclaration)iter.next();
			typeSpec.addMemberDeclaration(declaration);
			declaration.setParent(typeSpec);
			declaration.setPropertyInParent(IASTCompositeTypeSpecifier.MEMBER_DECLARATION);
		}
		astStack.closeASTScope();
		
		IASTName name = (hasName) ? (IASTName)astStack.pop() : new FortranASTName();
		typeSpec.setName(name);
		name.setParent(typeSpec);
		name.setPropertyInParent(IASTCompositeTypeSpecifier.TYPE_NAME);
		
		typeSpec.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(typeSpec);
	}
	
	
	/**
	 * struct_or_union_specifier
     *     ::= 'struct' struct_or_union_identifier
     *       | 'union'  struct_or_union_identifier
     *       
     * enum_specifier ::= 'enum' enum_identifier     
	 */
	protected void buildTypeSpecifierElaborated(int kind) {
		CASTElaboratedTypeSpecifier typeSpec = new CASTElaboratedTypeSpecifier();
		typeSpec.setKind(kind);
		
		IASTName name = (IASTName)astStack.pop();
		typeSpec.setName(name);
		name.setParent(typeSpec);
		name.setPropertyInParent(IASTElaboratedTypeSpecifier.TYPE_NAME);
		
		typeSpec.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(typeSpec);
	}
	
	
	
	/**
	 * enum_specifier ::= 'enum' '{' <openscope> enumerator_list_opt '}'
     *                  | 'enum' enum_identifier '{' <openscope> enumerator_list_opt '}'
     *                  | 'enum' '{' <openscope> enumerator_list_opt ',' '}'
     *                  | 'enum' enum_identifier '{' <openscope> enumerator_list_opt ',' '}'
	 */
	protected void buildTypeSpecifierEnumeration(boolean hasIdentifier) {
		CASTEnumerationSpecifier enumSpec = new CASTEnumerationSpecifier();

		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTEnumerator enumerator = (IASTEnumerator)iter.next();
			enumSpec.addEnumerator(enumerator);
			enumerator.setParent(enumSpec);
			enumerator.setPropertyInParent(ICASTEnumerationSpecifier.ENUMERATOR);
		}
		astStack.closeASTScope();
		
		if(hasIdentifier) {
			IASTName name = (IASTName)astStack.pop();
			enumSpec.setName(name);
			name.setParent(enumSpec);
			name.setPropertyInParent(ICASTEnumerationSpecifier.ENUMERATION_NAME);
		}
		
		enumSpec.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(enumSpec);
	}
	
	
	
	/**
	 * enumerator ::= enum_identifier
     *              | enum_identifier '=' constant_expression
	 */
	protected void buildEnumerator(boolean hasInitializer) {
		CASTEnumerator enumerator = new CASTEnumerator();
		
		if(hasInitializer) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			enumerator.setValue(expr);
			expr.setParent(enumerator);
			expr.setPropertyInParent(IASTEnumerator.ENUMERATOR_VALUE);
		}
		
		IASTName name = (IASTName)astStack.pop();
		enumerator.setName(name);
		name.setParent(enumerator);
		name.setPropertyInParent(IASTEnumerator.ENUMERATOR_NAME);
		
		enumerator.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(enumerator);
	}
		
	
	/**
	 * compound_statement ::= <openscope> '{' block_item_list '}'
	 * 
	 * block_item_list ::= block_item | block_item_list block_item
	 */
	protected void buildStatementCompoundStatement() {
		CASTCompoundStatement block = new CASTCompoundStatement();
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTStatement statement = (IASTStatement)iter.next();
			block.addStatement(statement);
			statement.setParent(block);
			statement.setPropertyInParent(IASTCompoundStatement.NESTED_STATEMENT);
		}
		astStack.closeASTScope();
		
		block.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(block);
	}
	
	
	
	/**
	 * compound_statement ::= '{' '}' 
	 */
	protected void buildStatementEmptyCompoundStatement() {
		CASTCompoundStatement statement = new CASTCompoundStatement();
		statement.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(statement);
	}
	
	
	
	
	/**
	 * iteration_statement_matched
	 *     ::= 'for' '(' expression ';' expression ';' expression ')' matched_statement
     *       | 'for' '(' expression ';' expression ';'            ')' matched_statement
     *       | 'for' '(' expression ';'            ';' expression ')' matched_statement
     *       | 'for' '(' expression ';'            ';'            ')' matched_statement
     *       | 'for' '('            ';' expression ';' expression ')' matched_statement
     *       | 'for' '('            ';' expression ';'            ')' matched_statement
     *       | 'for' '('            ';'            ';' expression ')' matched_statement
     *       | 'for' '('            ';'            ';'            ')' matched_statement
     *       | 'for' '(' declaration expression ';' expression ')' matched_statement
     *       | 'for' '(' declaration expression ';'            ')' matched_statement
     *       | 'for' '(' declaration            ';' expression ')' matched_statement
     *       | 'for' '(' declaration            ';'            ')' matched_statement
     *       
	 */
	protected void buildStatementForLoop(boolean hasExpr1, boolean hasExpr2, boolean hasExpr3) {
		FortranASTDoStatement forStat = new FortranASTDoStatement();
		
		IASTStatement body = (IASTStatement) astStack.pop();
		forStat.setBody(body);
		body.setParent(forStat);
		body.setPropertyInParent(IASTForStatement.BODY);
		
		if(hasExpr3) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			forStat.setIterationExpression(expr);
			expr.setParent(forStat);
			expr.setPropertyInParent(IASTForStatement.ITERATION);
		}
		
		if(hasExpr2) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			forStat.setConditionExpression(expr);
			expr.setParent(forStat);
			expr.setPropertyInParent(IASTForStatement.CONDITION);
		}
		
		if(hasExpr1) { // may be an expression or a declaration
			IASTNode node = (IASTNode) astStack.pop();
			
			if(node instanceof IASTExpression) {
				IASTExpressionStatement stat = new CASTExpressionStatement();
				IASTExpression expr = (IASTExpression)node;
				stat.setExpression(expr);
				expr.setParent(stat);
				expr.setPropertyInParent(IASTExpressionStatement.EXPFRESSION);
				
				forStat.setInitializerStatement(stat);
				stat.setParent(forStat);
				stat.setPropertyInParent(IASTForStatement.INITIALIZER);
			}
			else if(node instanceof IASTDeclaration) {
				IASTDeclarationStatement stat = new CASTDeclarationStatement();
				IASTDeclaration declaration = (IASTDeclaration)node;
				stat.setDeclaration(declaration);
				declaration.setParent(stat);
				declaration.setPropertyInParent(IASTDeclarationStatement.DECLARATION);
				
				forStat.setInitializerStatement(stat);
				stat.setParent(forStat);
				stat.setPropertyInParent(IASTForStatement.INITIALIZER);
			}
		}
		else {
			forStat.setInitializerStatement(new CASTNullStatement());
		}
		
		forStat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(forStat);
	}
	
	
	/**
	 * iteration_statement_matched
	 *     ::= 'while' '(' expression ')' matched_statement
	 */
	protected void buildStatementWhileLoop() {
		FortranASTWhileStatement stat = new FortranASTWhileStatement();
		
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTExpression condition = (IASTExpression) astStack.pop();
		
		stat.setBody(body);
		body.setParent(stat);
		body.setPropertyInParent(IASTWhileStatement.BODY);
		
		stat.setCondition(condition);
		condition.setParent(stat);
		condition.setPropertyInParent(IASTWhileStatement.CONDITIONEXPRESSION);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	
	/**
	 * iteration_statement_matched
	 *     ::= 'do' statement 'while' '(' expression ')' ';'
	 */
	protected void buildStatementDoLoop() {
		CASTDoStatement stat = new CASTDoStatement();
		
		IASTExpression condition = (IASTExpression) astStack.pop();
		IASTStatement body = (IASTStatement) astStack.pop();
		
		stat.setCondition(condition);
		condition.setParent(stat);
		condition.setPropertyInParent(IASTDoStatement.CONDITION);
		
		stat.setBody(body);
		body.setParent(stat);
		body.setPropertyInParent(IASTDoStatement.BODY);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * block_item ::= declaration | statement 
	 * 
	 * Wrap a declaration in a DeclarationStatement.
	 * 
	 * Disambiguation:
	 * 
	 * x; // should be an expression statement
	 * 
	 */
	protected void buildStatementDeclaration() {
		IASTDeclaration decl = (IASTDeclaration) astStack.pop();
		
		// Kludgy way to disambiguate a certain case.
		// An identifier alone on a line will be parsed as a declaration
		// but it probably should be an expression.
		// eg) i;
		if(decl instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) decl;
			if(declaration.getDeclarators() == IASTDeclarator.EMPTY_DECLARATOR_ARRAY) {
				IASTDeclSpecifier declSpec = declaration.getDeclSpecifier();
				if(declSpec instanceof ICASTTypedefNameSpecifier) {
					CASTTypedefNameSpecifier typedefNameSpec = (CASTTypedefNameSpecifier) declSpec;
					FortranASTName name = (FortranASTName)typedefNameSpec.getName();
					
					if(name.getOffset() == typedefNameSpec.getOffset() &&
				       name.getLength() == typedefNameSpec.getLength()) {
						
						CASTExpressionStatement stat = new CASTExpressionStatement();
						FortranASTIdExpression idExpr = new FortranASTIdExpression();
						idExpr.setName(name);
						name.setParent(idExpr);
						name.setPropertyInParent(IASTIdExpression.ID_NAME);
						
						stat.setExpression(idExpr);
						idExpr.setParent(stat);
						idExpr.setPropertyInParent(IASTExpressionStatement.EXPFRESSION);
						
						stat.setOffsetAndLength(ruleOffset, ruleLength);
						astStack.push(stat);
						return;
					}
				}
			}
		}

		CASTDeclarationStatement stat = new CASTDeclarationStatement();
		
		stat.setDeclaration(decl);
		decl.setParent(stat);
		decl.setPropertyInParent(IASTDeclarationStatement.DECLARATION);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * jump_statement ::= goto 'identifier' ';'
	 */
	protected void buildStatementGoto() {
		IASTName name = createName(parser.getRhsIToken(2));
		FortranASTGotoStatement gotoStat = new FortranASTGotoStatement();
		
		gotoStat.setName(name);
		name.setParent(gotoStat);
		name.setPropertyInParent(IASTGotoStatement.NAME);
		
		gotoStat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(gotoStat);
	}
	
	
	/**
	 * jump_statement ::= continue ';'
	 */
	protected void buildStatementContinue() {  
		FortranASTContinueStatement stat = new FortranASTContinueStatement();
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * jump_statement ::= return ';'
	 * jump_statement ::= return expression ';'
	 */
	protected void buildStatementReturn(boolean hasExpression) {
		FortranASTReturnStatement returnStat = new FortranASTReturnStatement();
		
		if(hasExpression) {
			IASTExpression expr = (IASTExpression) astStack.pop();
			returnStat.setReturnValue(expr);
			expr.setParent(returnStat);
			expr.setPropertyInParent(IASTReturnStatement.RETURNVALUE);
		}
		
		returnStat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(returnStat);
	}
	
	
	/**
	 * expression_statement ::= ';'
	 */
	protected void buildStatementNull() {
		CASTNullStatement stat = new CASTNullStatement();
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * expression_statement ::= expression ';'
	 */
	protected void buildStatementExpression() {
		IASTExpression expr = (IASTExpression) astStack.pop();
		CASTExpressionStatement stat = new CASTExpressionStatement();
		stat.setExpression(expr);
		expr.setParent(stat);
		expr.setPropertyInParent(IASTExpressionStatement.EXPFRESSION);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * labeled_statement ::= label_identifier ':' statement
	 * label_identifier ::= identifier 
	 */
	protected void buildStatementLabeled() {
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTName label = (IASTName) astStack.pop();
		
		CASTLabelStatement stat = new CASTLabelStatement();
		
		stat.setNestedStatement(body);
		body.setParent(stat);
		body.setPropertyInParent(IASTLabelStatement.NESTED_STATEMENT);
		
		stat.setName(label);
		label.setParent(stat);
		label.setPropertyInParent(IASTLabelStatement.NAME);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * labeled_statement ::= case constant_expression ':' statement
	 */
	protected void buildStatementCase() { 
		IASTStatement body  = (IASTStatement)  astStack.pop();
		IASTExpression expr = (IASTExpression) astStack.pop();
		
		FortranASTCaseStatement stat = new FortranASTCaseStatement();
		
		stat.setExpression(expr);
		expr.setParent(stat);
		expr.setPropertyInParent(IASTCaseStatement.EXPRESSION);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
		astStack.push(body);
	}
	
	
	/**
	 * labeled_statement ::= default ':' statement
	 */
	protected void buildStatementDefault() {
		IASTStatement body = (IASTStatement) astStack.pop();
		
		CASTDefaultStatement stat = new CASTDefaultStatement();
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		
		astStack.push(stat);
		astStack.push(body);
	}
	
	
	/**
	 * selection_statement ::=  switch '(' expression ')' statement
	 */
	protected void buildStatementSwitch() {
		IASTStatement body  = (IASTStatement)  astStack.pop();
		IASTExpression expr = (IASTExpression) astStack.pop();
		
		FortranASTCaseConstruct stat = new FortranASTCaseConstruct();
		
		stat.setBody(body);
		body.setParent(stat);
		body.setPropertyInParent(IASTSwitchStatement.BODY);
		
		stat.setControllerExpression(expr);
		expr.setParent(stat);
		expr.setPropertyInParent(IASTSwitchStatement.CONTROLLER_EXP);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * if_then_statement ::= if '(' expression ')' statement
	 */
	protected void buildStatementIfThen() {
		IASTStatement thenClause = (IASTStatement) astStack.pop();
		IASTExpression condition = (IASTExpression) astStack.pop();
		
		FortranASTIfStatement stat = new FortranASTIfStatement();
		
		stat.setConditionExpression(condition);
		condition.setParent(stat);
		condition.setPropertyInParent(IASTIfStatement.CONDITION);
		
		stat.setThenClause(thenClause);
		thenClause.setParent(stat);
		thenClause.setPropertyInParent(IASTIfStatement.THEN);
		
		stat.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(stat);
	}
	
	
	/**
	 * if_then_else_matched_statement
     *     ::= if '(' expression ')' statement_no_short_if else statement_no_short_if
     *     
     * if_then_else_unmatched_statement
     *     ::= if '(' expression ')' statement_no_short_if else statement
	 */
	protected void buildStatementIfThenElse() { 
		IASTStatement elseClause = (IASTStatement) astStack.pop();
		
		buildStatementIfThen();
		FortranASTIfStatement stat = (FortranASTIfStatement) astStack.pop();
		
		stat.setElseClause(elseClause);
		elseClause.setParent(stat);
		elseClause.setPropertyInParent(IASTIfStatement.ELSE);
		
		// the offset and length is set in buildStatementIfThen()
		astStack.push(stat);
	}
	

	/**
	 * translation_unit ::= external_declaration_list
     *
     * external_declaration_list
     *    ::= external_declaration
     *      | external_declaration_list external_declaration
	 */
	protected void buildTranslationUnit() {
		FortranASTTranslationUnit tu = new FortranASTTranslationUnit();
		
		for(Iterator iter = astStack.topScopeIterator(); iter.hasNext();) {
			IASTDeclaration declaration = (IASTDeclaration) iter.next();
			tu.addDeclaration(declaration);
			declaration.setParent(tu);
			declaration.setPropertyInParent(IASTTranslationUnit.OWNED_DECLARATION);
		}
		
		tu.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(tu); 
	}
	
	
	/**
	 * function_definition
     *    ::= declaration_specifiers <openscope> declarator compound_statement
     */
	protected void buildFunctionDefinition() {
		FortranASTFunctionDefinition def = new FortranASTFunctionDefinition();
		
		IASTCompoundStatement  body = (IASTCompoundStatement)  astStack.pop();
		IASTFunctionDeclarator decl = (IASTFunctionDeclarator) astStack.pop();
		// The seemingly pointless <openscope> is just there to 
		// prevent a shift/reduce conflict in the grammar.
		astStack.closeASTScope();
		IASTDeclSpecifier declSpecifier = (IASTDeclSpecifier) astStack.pop();
		
		def.setBody(body);
		body.setParent(def);
		body.setPropertyInParent(IASTFunctionDefinition.FUNCTION_BODY);
		
		def.setDeclarator(decl);
		decl.setParent(def);
		decl.setPropertyInParent(IASTFunctionDefinition.DECLARATOR);
		
		def.setDeclSpecifier(declSpecifier);
		declSpecifier.setParent(def);
		declSpecifier.setPropertyInParent(IASTFunctionDefinition.DECL_SPECIFIER);
		
		def.setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(def);
	}
	
	
	/**
	 * statement ::= ERROR_TOKEN
	 */
	protected void buildStatementProblem() {
		buildProblem(new CASTProblemStatement());
	}
	
	
	/**
	 * assignment_expression ::= ERROR_TOKEN
	 * constant_expression ::= ERROR_TOKEN
	 */
	protected void buildExpressionProblem() {
		buildProblem(new CASTProblemExpression());
	}
	
	
	/**
	 * external_declaration ::= ERROR_TOKEN
	 */
	protected void buildDeclarationProblem() {
		buildProblem(new CASTProblemDeclaration());
	}
	
	
	private void buildProblem(IASTProblemHolder problemHolder) {
		CASTProblem problem = new CASTProblem(IASTProblem.SYNTAX_ERROR, EMPTY_CHAR_ARRAY, false, true);
		
		problemHolder.setProblem(problem);
		problem.setParent((IASTNode)problemHolder);
		problem.setPropertyInParent(IASTProblemStatement.PROBLEM);
		
		problem.setOffsetAndLength(ruleOffset, ruleLength);
		((ASTNode)problemHolder).setOffsetAndLength(ruleOffset, ruleLength);
		astStack.push(problemHolder);
	}


	public void ac_value_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void ac_value_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void access_id_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void access_id_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void actual_arg(boolean hasExpr, Token label) {
		// TODO Auto-generated method stub
		
	}


	public void actual_arg_spec(Token keyword) {
		// TODO Auto-generated method stub
		
	}


	public void actual_arg_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void actual_arg_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void add_operand(Token addOp, int numAddOps) {
		// TODO Auto-generated method stub
		
	}


	public void add_operand__add_op(Token addOp) {
		// TODO Auto-generated method stub
		
	}


	public void alloc_opt_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void alloc_opt_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void allocate_co_shape_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void allocate_co_shape_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void allocate_object_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void allocate_object_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void allocate_shape_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void allocate_shape_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void allocation_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void allocation_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void and_operand(boolean hasNotOp, int numAndOps) {
		// TODO Auto-generated method stub
		
	}


	public void and_operand__not_op(boolean hasNotOp) {
		// TODO Auto-generated method stub
		
	}


	public void arithmetic_if_stmt(Token label, Token label1, Token label2,
			Token label3) {
		// TODO Auto-generated method stub
		
	}


	public void assignment_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}


	public void association_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void association_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void bind_entity_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void bind_entity_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void binding_attr_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void binding_attr_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void bounds_remapping_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void bounds_remapping_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void bounds_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void bounds_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void call_stmt(Token label, boolean hasActualArgSpecList) {
		// TODO Auto-generated method stub
		
	}


	public void case_value_range_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void case_value_range_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void char_constant(Token id) {
		// TODO Auto-generated method stub
		
	}


	public void char_length(boolean hasTypeParamValue) {
		// TODO Auto-generated method stub
		
	}


	public void char_literal_constant(Token digitString, Token id, Token str) {
		// TODO Auto-generated method stub
		
	}


	public void char_selector(KindLenParam kindOrLen1, KindLenParam kindOrLen2,
			boolean hasAsterisk) {
		// TODO Auto-generated method stub
		
	}


	public void close_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void close_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void common_block_object_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void common_block_object_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void component_attr_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void component_attr_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void component_decl_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void component_decl_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void component_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void component_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void computed_goto_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}


	public void connect_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void connect_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void constant(Token id) {
		// TODO Auto-generated method stub
		
	}


	public void continue_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}


	public void cycle_stmt(Token label, Token id) {
		// TODO Auto-generated method stub
		
	}


	public void data_i_do_object_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void data_i_do_object_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void data_ref(int numPartRef) {
		// TODO Auto-generated method stub
		
	}


	public void data_stmt_object_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void data_stmt_object_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void data_stmt_value_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void data_stmt_value_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void dealloc_opt_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void dealloc_opt_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void deferred_co_shape_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void deferred_co_shape_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void deferred_shape_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void deferred_shape_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void defined_operator(Token definedOp) {
		// TODO Auto-generated method stub
		
	}


	public void designator(boolean hasSubstringRange) {
		// TODO Auto-generated method stub
		
	}


	public void dummy_arg_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void dummy_arg_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void end_program_stmt(Token label, Token id) {
		// TODO Auto-generated method stub
		
	}


	public void entity_decl(Token id) {
		// TODO Auto-generated method stub
		
	}


	public void entity_decl_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void entity_decl_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void enumerator_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void enumerator_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void equiv_operand(int numEquivOps) {
		// TODO Auto-generated method stub
		
	}


	public void equiv_operand__equiv_op(Token equivOp) {
		// TODO Auto-generated method stub
		
	}


	public void equivalence_object_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void equivalence_object_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void equivalence_set_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void equivalence_set_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void exit_stmt(Token label, Token id) {
		// TODO Auto-generated method stub
		
	}


	public void explicit_shape_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void explicit_shape_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void expr() {
		// TODO Auto-generated method stub
		
	}


	public void flush_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void flush_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void forall_triplet_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void forall_triplet_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void format_item_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void format_item_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void function_reference(boolean hasActualArgSpecList) {
		// TODO Auto-generated method stub
		
	}


	public void generic_name_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void generic_name_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void goto_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}


	public void imag_part(boolean hasIntConstant, boolean hasRealConstant,
			Token id) {
		// TODO Auto-generated method stub
		
	}


	public void implicit_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void implicit_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void initialization(boolean hasExpr, boolean hasNullInit) {
		// TODO Auto-generated method stub
		
	}


	public void input_item_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void input_item_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void inquire_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void inquire_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void int_constant(Token id) {
		// TODO Auto-generated method stub
		
	}


	public void int_literal_constant(Token digitString, Token kindParam) {
		// TODO Auto-generated method stub
		
	}


	public void internal_subprogram_part(int count) {
		// TODO Auto-generated method stub
		
	}


	public void intrinsic_type_spec(IntrinsicTypeSpec type,
			boolean hasKindSelector) {
		// TODO Auto-generated method stub
		
	}


	public void io_control_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void io_control_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void label(Token lbl) {
		// TODO Auto-generated method stub
		
	}


	public void label_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void label_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void length_selector(KindLenParam kindOrLen, boolean hasAsterisk) {
		// TODO Auto-generated method stub
		
	}


	public void letter_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void letter_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void level_1_expr(Token definedUnaryOp) {
		// TODO Auto-generated method stub
		
	}


	public void level_2_expr(int numConcatOps) {
		// TODO Auto-generated method stub
		
	}


	public void level_3_expr(Token relOp) {
		// TODO Auto-generated method stub
		
	}


	public void level_5_expr(int numDefinedBinaryOps) {
		// TODO Auto-generated method stub
		
	}


	public void level_5_expr__defined_binary_op(Token definedBinaryOp) {
		// TODO Auto-generated method stub
		
	}


	public void logical_literal_constant(boolean isTrue, Token kindParam) {
		// TODO Auto-generated method stub
		
	}


	public void main_program(boolean hasProgramStmt, boolean hasExecutionPart,
			boolean hasInternalSubprogramPart) {
		// TODO Auto-generated method stub
		
	}


	public void main_program__begin() {
		// TODO Auto-generated method stub
		
	}


	public void mult_operand(int numMultOps) {
		// TODO Auto-generated method stub
		
	}


	public void mult_operand__mult_op(Token multOp) {
		// TODO Auto-generated method stub
		
	}


	public void named_constant_def_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void named_constant_def_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void namelist_group_object_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void namelist_group_object_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void null_init(Token id) {
		// TODO Auto-generated method stub
		
	}


	public void nullify_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}


	public void only_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void only_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void or_operand(int numOrOps) {
		// TODO Auto-generated method stub
		
	}


	public void output_item_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void output_item_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void part_ref(Token id, boolean hasSelectionSubscriptList,
			boolean hasImageSelector) {
		// TODO Auto-generated method stub
		
	}


	public void pointer_decl_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void pointer_decl_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void pointer_object_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void pointer_object_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void position_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void position_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void power_operand(boolean hasPowerOperand) {
		// TODO Auto-generated method stub
		
	}


	public void power_operand__power_op(Token powerOp) {
		// TODO Auto-generated method stub
		
	}


	public void primary() {
		// TODO Auto-generated method stub
		
	}


	public void proc_component_attr_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void proc_component_attr_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void proc_decl_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void proc_decl_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void program_stmt(Token label, Token id) {
		// TODO Auto-generated method stub
		
	}


	public void real_literal_constant(Token digits, Token fractionExp,
			Token kindParam) {
		// TODO Auto-generated method stub
		
	}


	public void real_part(boolean hasIntConstant, boolean hasRealConstant,
			Token id) {
		// TODO Auto-generated method stub
		
	}


	public void rename_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void rename_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void saved_entity_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void saved_entity_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void section_subscript(boolean hasLowerBound, boolean hasUpperBound,
			boolean hasStride, boolean isAmbiguous) {
		// TODO Auto-generated method stub
		
	}


	public void section_subscript_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void section_subscript_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void signed_int_literal_constant(Token sign) {
		// TODO Auto-generated method stub
		
	}


	public void signed_real_literal_constant(Token sign) {
		// TODO Auto-generated method stub
		
	}


	public void stop_code(Token digitString) {
		// TODO Auto-generated method stub
		
	}


	public void stop_stmt(Token label, boolean hasStopCode) {
		// TODO Auto-generated method stub
		
	}


	public void substring(boolean hasSubstringRange) {
		// TODO Auto-generated method stub
		
	}


	public void substring_range(boolean hasLowerBound, boolean hasUpperBound) {
		// TODO Auto-generated method stub
		
	}


	public void type_attr_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void type_attr_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void type_declaration_stmt(Token label, int numAttributes) {
		// TODO Auto-generated method stub
		
	}


	public void type_declaration_stmt__begin() {
		// TODO Auto-generated method stub
		
	}


	public void type_param_decl_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void type_param_decl_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void type_param_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void type_param_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void type_param_value(boolean hasExpr, boolean hasAsterisk,
			boolean hasColon) {
		// TODO Auto-generated method stub
		
	}


	public void v_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void v_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void v_list_part(Token plus_minus, Token digitString) {
		// TODO Auto-generated method stub
		
	}


	public void variable() {
		// TODO Auto-generated method stub
		
	}


	public void wait_spec_list(int count) {
		// TODO Auto-generated method stub
		
	}


	public void wait_spec_list__begin() {
		// TODO Auto-generated method stub
		
	}


	public void access_stmt(Token label, boolean hasList) {
		// TODO Auto-generated method stub
		
	}


	public void alloc_opt(Token allocOpt) {
		// TODO Auto-generated method stub
		
	}


	public void allocate_shape_spec(boolean hasLowerBound, boolean hasUpperBound) {
		// TODO Auto-generated method stub
		
	}


	public void allocate_stmt(Token label, boolean hasTypeSpec,
			boolean hasAllocOptList) {
		// TODO Auto-generated method stub
		
	}


	public void allocation(boolean hasAllocateShapeSpecList,
			boolean hasAllocateCoArraySpec) {
		// TODO Auto-generated method stub
		
	}


	public void array_constructor() {
		// TODO Auto-generated method stub
		
	}


	public void array_spec(int count) {
		// TODO Auto-generated method stub
		
	}


	public void array_spec__begin() {
		// TODO Auto-generated method stub
		
	}


	public void array_spec_element(ArraySpecElement type) {
		// TODO Auto-generated method stub
		
	}


	public void asynchronous_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}


	public void attr_spec(AttrSpec attr) {
		// TODO Auto-generated method stub
		
	}


	public void bind_entity(Token entity, boolean isCommonBlockName) {
		// TODO Auto-generated method stub
		
	}


	public void bind_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}


	public void binding_attr(AttrSpec attr) {
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


	public void declaration_type_spec(DeclarationTypeSpec type) {
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


	public void dummy_arg(Token dummy) {
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


	public void explicit_shape_spec(boolean hasUpperBound) {
		// TODO Auto-generated method stub
		
	}


	public void forall_construct_stmt(Token label, Token id) {
		// TODO Auto-generated method stub
		
	}


	public void generic_name_list_part(Token ident) {
		// TODO Auto-generated method stub
		
	}


	public void implicit_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}


	public void intent_spec(IntentSpec intent) {
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


	public void proc_attr_spec(AttrSpec spec) {
		// TODO Auto-generated method stub
		
	}


	public void proc_binding_stmt(Token label) {
		// TODO Auto-generated method stub
		
	}


	public void proc_component_def_stmt(Token label, boolean hasInterface) {
		// TODO Auto-generated method stub
		
	}


	public void proc_decl(Token id, boolean hasNullInit) {
		// TODO Auto-generated method stub
		
	}


	public void proc_interface(Token id) {
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
}
