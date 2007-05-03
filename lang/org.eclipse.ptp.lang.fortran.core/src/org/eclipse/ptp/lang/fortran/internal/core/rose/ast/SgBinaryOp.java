package org.eclipse.ptp.lang.fortran.internal.core.rose.ast;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;

public abstract class SgBinaryOp extends SgExpression implements IASTBinaryExpression {

    // This is the operand associated with the lhs of the binary operator.
    // Every binary operator is applied to two operands, this variable stores the lhs operand to which the binary operator is applied (with the rhs).
	protected SgExpression p_lhs_operand_i;
	                                         	
    // This is the operand associated with the rhs of the binary operator.
    // Every binary operator is applied to two operands, this variable stores the rhs operand to which the binary operator is applied (with the lhs).
    protected SgExpression p_rhs_operand_i;
	                                             	
    // This SgType is the type of the operator (function type).
    protected SgType p_expression_type;
	
	public SgBinaryOp(SgExpression lhs_operand_i, SgExpression rhs_operand_i, SgType expression_type) {
		set_lhs_operand_i(lhs_operand_i);
		set_rhs_operand_i(rhs_operand_i);
		p_expression_type = expression_type;
	}


 	/** Returns SgExpression pointer to the operand associated with this binary operator. */ 
	public SgExpression get_lhs_operand_i() {
		return p_lhs_operand_i;
	}

 	/** This function allows the p_lhs_operand_i pointer to be set (used internally). */ 
	public void set_lhs_operand_i(SgExpression lhs_operand_i) {
		p_lhs_operand_i = lhs_operand_i;
	}

 	/** Returns SgExpression pointer to the operand associated with this binary operator. */ 
	public SgExpression get_rhs_operand_i() {
		return p_rhs_operand_i;
	}

 	/** This function allows the p_rhs_operand_i pointer to be set (used internally). */
 	void set_rhs_operand_i(SgExpression rhs_operand_i) {
		p_rhs_operand_i = rhs_operand_i;
	}
 
	public IASTExpression getOperand1() {
		return get_lhs_operand_i();
	}

	public IASTExpression getOperand2() {
		return get_rhs_operand_i();
	}

	// The should be overridden in subclass
	public abstract int getOperator();/* {
		return 0;
	}*/

	public void setOperand1(IASTExpression expression) {
		set_lhs_operand_i((SgExpression) expression);
	}

	public void setOperand2(IASTExpression expression) {
		set_rhs_operand_i((SgExpression) expression);		
	}

	// TODO - maybe need to have delegate so this is possible
	// Hopefully this is not needed
	public void setOperator(int op) {
	}

}
