/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cldt.internal.core.dom.parser.c;

import org.eclipse.cldt.core.dom.ast.IASTExpression;
import org.eclipse.cldt.core.dom.ast.IASTFieldDeclarator;

/**
 * @author jcamelon
 */
public class CASTFieldDeclarator extends CASTDeclarator implements
        IASTFieldDeclarator {

    private IASTExpression bitFieldSize;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator#getBitFieldSize()
     */
    public IASTExpression getBitFieldSize() {
        return bitFieldSize;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator#setBitFieldSize(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setBitFieldSize(IASTExpression size) {
        bitFieldSize = size;
    }


}
