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
import org.eclipse.fdt.core.dom.ast.IASTFunctionCallExpression;

/**
 * @author jcamelon
 */
public class CPPASTFunctionCallExpression extends CPPASTNode implements
        IASTFunctionCallExpression {
    private IASTExpression functionName;
    private IASTExpression parameter;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFunctionCallExpression#setFunctionNameExpression(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setFunctionNameExpression(IASTExpression expression) {
        this.functionName = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFunctionCallExpression#getFunctionNameExpression()
     */
    public IASTExpression getFunctionNameExpression() {
        return functionName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFunctionCallExpression#setParameterExpression(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setParameterExpression(IASTExpression expression) {
        this.parameter = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFunctionCallExpression#getParameterExpression()
     */
    public IASTExpression getParameterExpression() {
        return parameter;
    }

}
