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

import org.eclipse.cldt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cldt.core.dom.ast.IASTDeclarator;
import org.eclipse.cldt.core.dom.ast.IASTTypeId;

/**
 * @author jcamelon
 */
public class CASTTypeId extends CASTNode implements IASTTypeId {

    private IASTDeclSpecifier declSpecifier;
    private IASTDeclarator declarator;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypeId#getDeclSpecifier()
     */
    public IASTDeclSpecifier getDeclSpecifier() {
        return declSpecifier;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypeId#setDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
     */
    public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        this.declSpecifier = declSpec;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypeId#getAbstractDeclarator()
     */
    public IASTDeclarator getAbstractDeclarator() {
        return declarator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypeId#setAbstractDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
     */
    public void setAbstractDeclarator(IASTDeclarator abstractDeclarator) {
        declarator = abstractDeclarator;

    }

}
