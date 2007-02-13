/**
 * 
 */
package org.eclipse.ptp.lang.fortran.core.parser;

import java.util.Iterator;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
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
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
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
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTArrayDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTArrayDesignator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTArraySubscriptExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBreakStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCaseStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCastExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTConditionalExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTContinueStatement;
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
import org.eclipse.cdt.internal.core.dom.parser.c.CASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTGotoStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTInitializerList;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTLabelStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTModifiedArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTName;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTNullStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTPointer;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTProblem;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTProblemDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTProblemExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTProblemStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeIdInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypedefNameSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTWhileStatement;

/**
 * TODO - change/add from C to Fortran grammar in comments and actions
 * The goal is to do this slowly, one action at a time
 */
public abstract class IFortranParserAction {


	/**
	 * constant ::= 'integer' | 'floating' | 'charconst' | 'stringlit'
	 * 
	 * @param kind One of the kind flags from IASTLiteralExpression
	 * @see IASTLiteralExpression
	 */
	public abstract void consumeExpressionConstant(int kind);
	
	/**
	 * primary_expression ::= 'identifier'
	 */
	public abstract void consumeExpressionID();
	
	/**
	 * multiplicative_expression ::= multiplicative_expression '*' cast_expression
	 * multiplicative_expression ::= multiplicative_expression '/' cast_expression
	 * multiplicative_expression ::= multiplicative_expression '%' cast_expression
	 * 
	 * additive_expression ::= additive_expression '+' multiplicative_expression
	 * additive_expression ::= additive_expression '_' multiplicative_expression
	 * 
	 * shift_expression ::= shift_expression '<<' additive_expression
	 * shift_expression ::= shift_expression '>>' additive_expression
	 * 
	 * relational_expression ::= relational_expression '<' shift_expression
	 * relational_expression ::= relational_expression '>' shift_expression
	 * relational_expression ::= relational_expression '<=' shift_expression
	 * relational_expression ::= relational_expression '>=' shift_expression
	 * 
	 * equality_expression ::= equality_expression '==' relational_expression
	 * equality_expression ::= equality_expression '!=' relational_expression
	 * 
	 * AND_expression ::= AND_expression '&' equality_expression
	 * 
	 * exclusive_OR_expression ::= exclusive_OR_expression '^' AND_expression
	 * 
	 * inclusive_OR_expression ::= inclusive_OR_expression '|' exclusive_OR_expression
	 * 
	 * logical_AND_expression ::= logical_AND_expression '&&' inclusive_OR_expression
	 * 
	 * logical_OR_expression ::= logical_OR_expression '||' logical_AND_expression
	 * 
	 * assignment_expression ::= unary_expression '='   assignment_expression
	 * assignment_expression ::= unary_expression '*='  assignment_expression
	 * assignment_expression ::= unary_expression '/='  assignment_expression
	 * assignment_expression ::= unary_expression '%='  assignment_expression
	 * assignment_expression ::= unary_expression '+='  assignment_expression
	 * assignment_expression ::= unary_expression '_='  assignment_expression
	 * assignment_expression ::= unary_expression '<<=' assignment_expression
	 * assignment_expression ::= unary_expression '>>=' assignment_expression
	 * assignment_expression ::= unary_expression '&='  assignment_expression
	 * assignment_expression ::= unary_expression '^='  assignment_expression
	 * assignment_expression ::= unary_expression '|='  assignment_expression
	 * 
	 * 
	 * @param op Field from IASTBinaryExpression
	 */
	public abstract void consumeExpressionBinaryOperator(int op);
	
}
