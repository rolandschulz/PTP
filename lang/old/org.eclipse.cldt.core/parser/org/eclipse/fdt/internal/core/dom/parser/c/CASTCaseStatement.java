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

import org.eclipse.fdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.fdt.core.dom.ast.IASTExpression;

/**
 * @author jcamelon
 */
public class CASTCaseStatement extends CASTNode implements IASTCaseStatement {

    private IASTExpression expression;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTCaseStatement#getExpression()
     */
    public IASTExpression getExpression() {
        return expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTCaseStatement#setExpression(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setExpression(IASTExpression expression) {
        this.expression = expression;
    }

}
