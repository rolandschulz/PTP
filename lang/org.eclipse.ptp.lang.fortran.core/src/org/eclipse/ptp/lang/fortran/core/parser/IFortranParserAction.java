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
	
	/** R306
	 * literal_constant
	 * :	int_literal_constant
	 * |	real_literal_constant
	 * |	complex_literal_constant
	 * |	logical_literal_constant
	 * |	char_literal_constant
	 * |	boz_literal_constant
	 * 
	 * @param kind The kind of literal constant
	 * @param cToken The literal constant token
	 * @param kindType The kind of kind-type parameter (const '_' kindType)
	 * @param ktToken The kind-type parameter token (digit string | id)
	 * @see IASTLiteralExpression
	 */
	public abstract void buildExpressionConstant(LiteralConstant kind, Token cToken, KindParam kindType, Token ktToken);
	
	/**
	 * primary_expression ::= 'identifier'
	 */
	public abstract void buildExpressionID();
	
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
	public abstract void buildExpressionBinaryOperator(int op);
	
}
