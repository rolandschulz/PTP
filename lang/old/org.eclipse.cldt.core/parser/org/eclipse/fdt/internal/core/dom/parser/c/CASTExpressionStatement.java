/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.fdt.internal.core.dom.parser.c;

import org.eclipse.fdt.core.dom.ast.IASTExpression;
import org.eclipse.fdt.core.dom.ast.IASTExpressionStatement;

/**
 * @author jcamelon
 */
public class CASTExpressionStatement extends CASTNode implements
        IASTExpressionStatement {

    private IASTExpression expression;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTExpressionStatement#getExpression()
     */
    public IASTExpression getExpression() {
        return expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTExpressionStatement#setExpression(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setExpression(IASTExpression expression) {
        this.expression = expression;
    }

}
