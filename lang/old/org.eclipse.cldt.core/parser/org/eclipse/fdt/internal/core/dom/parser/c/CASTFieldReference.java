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
import org.eclipse.fdt.core.dom.ast.IASTFieldReference;
import org.eclipse.fdt.core.dom.ast.IASTName;

/**
 * @author jcamelon
 */
public class CASTFieldReference extends CASTNode implements IASTFieldReference {

    private IASTExpression owner;
    private IASTName name;
    private boolean ptr;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFieldReference#getFieldOwner()
     */
    public IASTExpression getFieldOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFieldReference#setFieldOwner(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setFieldOwner(IASTExpression expression) {
        this.owner = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFieldReference#getFieldName()
     */
    public IASTName getFieldName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFieldReference#setFieldName(org.eclipse.fdt.core.dom.ast.IASTName)
     */
    public void setFieldName(IASTName name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFieldReference#isPointerDereference()
     */
    public boolean isPointerDereference() {
        return ptr;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFieldReference#setIsPointerDereference(boolean)
     */
    public void setIsPointerDereference(boolean value) {
        ptr = value;
    }

}
