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

import org.eclipse.fdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.fdt.core.dom.ast.IASTExpression;

/**
 * @author jcamelon
 */
public class CASTArrayModifier extends CASTNode implements IASTArrayModifier {

    private IASTExpression exp;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTArrayModifier#getConstantExpression()
     */
    public IASTExpression getConstantExpression() {
        return exp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTArrayModifier#setConstantExpression(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setConstantExpression(IASTExpression expression) {
        this.exp = expression;
    }

}
