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

import org.eclipse.fdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.fdt.core.dom.ast.IASTExpression;

/**
 * @author jcamelon
 */
public class CPPASTArrayModifier extends CPPASTNode implements
        IASTArrayModifier {

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
        exp = expression;
    }
}
