/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.fdt.internal.core.dom.parser.cpp;

import org.eclipse.fdt.core.dom.ast.IASTExpression;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTBinaryExpression;

/**
 * @author jcamelon
 */
public class CPPASTBinaryExpression extends CPPASTNode implements
        ICPPASTBinaryExpression {

    private int op;
    private IASTExpression operand1;
    private IASTExpression operand2;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTBinaryExpression#getOperator()
     */
    public int getOperator() {
        return op;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTBinaryExpression#getOperand1()
     */
    public IASTExpression getOperand1() {
        return operand1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTBinaryExpression#getOperand2()
     */
    public IASTExpression getOperand2() {
        return operand2;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTBinaryExpression#setOperator(int)
     */
    public void setOperator(int op) {
        this.op = op;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTBinaryExpression#setOperand1(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setOperand1(IASTExpression expression) {
        operand1 = expression;   
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTBinaryExpression#setOperand2(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setOperand2(IASTExpression expression) {
        operand2 = expression;
    }

}
