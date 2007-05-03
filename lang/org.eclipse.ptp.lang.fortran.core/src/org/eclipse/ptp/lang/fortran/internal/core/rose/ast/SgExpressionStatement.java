package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

public class SgExpressionStatement extends SgStatement {

	protected SgExpression p_expression;
	
	public SgExpressionStatement(SgExpression expression) {
		p_expression = expression;
	}

}
