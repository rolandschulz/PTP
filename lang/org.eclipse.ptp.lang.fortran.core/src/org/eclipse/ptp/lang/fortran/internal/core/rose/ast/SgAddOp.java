package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;

public class SgAddOp extends SgBinaryOp {

	public SgAddOp(SgExpression	lhs_operand_i, SgExpression rhs_operand_i, SgType expression_type) {
		super(lhs_operand_i, rhs_operand_i, expression_type);
	}

	@Override
	public int getOperator() {
		return IASTBinaryExpression.op_plus;
	}

}
