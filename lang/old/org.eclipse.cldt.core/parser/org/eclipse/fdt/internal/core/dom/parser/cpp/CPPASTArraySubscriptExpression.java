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

import org.eclipse.fdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.fdt.core.dom.ast.IASTExpression;

/**
 * @author jcamelon
 */
public class CPPASTArraySubscriptExpression extends CPPASTNode implements
        IASTArraySubscriptExpression {

    private IASTExpression subscriptExp;
    private IASTExpression arrayExpression;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTArraySubscriptExpression#getArrayExpression()
     */
    public IASTExpression getArrayExpression() {
        return arrayExpression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTArraySubscriptExpression#setArrayExpression(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setArrayExpression(IASTExpression expression) {
        arrayExpression = expression;        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTArraySubscriptExpression#getSubscriptExpression()
     */
    public IASTExpression getSubscriptExpression() {
        return subscriptExp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTArraySubscriptExpression#setSubscriptExpression(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setSubscriptExpression(IASTExpression expression) {
        subscriptExp = expression;
    }

}
