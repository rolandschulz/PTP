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
import org.eclipse.fdt.core.dom.ast.IASTName;
import org.eclipse.fdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;

/**
 * @author jcamelon
 */
public class CASTEnumerator extends CASTNode implements IASTEnumerator {

    private IASTName name;
    private IASTExpression value;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator#setName(org.eclipse.fdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator#setValue(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setValue(IASTExpression expression) {
        this.value = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator#getValue()
     */
    public IASTExpression getValue() {
        return value;
    }

}
