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
import org.eclipse.cldt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cldt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cldt.core.dom.ast.IASTStatement;
import org.eclipse.cldt.core.dom.ast.IScope;
import org.eclipse.cldt.core.dom.ast.c.ICFunctionScope;

/**
 * @author jcamelon
 */
public class CASTFunctionDefinition extends CASTNode implements
        IASTFunctionDefinition {

    private IASTDeclSpecifier declSpecifier;
    private IASTFunctionDeclarator declarator;
    private IASTStatement bodyStatement;
    private ICFunctionScope scope;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#getDeclSpecifier()
     */
    public IASTDeclSpecifier getDeclSpecifier() {
        return declSpecifier;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#setDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
     */
    public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        declSpecifier = declSpec;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#getDeclarator()
     */
    public IASTFunctionDeclarator getDeclarator() {
        return declarator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#setDeclarator(org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator)
     */
    public void setDeclarator(IASTFunctionDeclarator declarator) {
        this.declarator = declarator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#getBody()
     */
    public IASTStatement getBody() {
        return bodyStatement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#setBody(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setBody(IASTStatement statement) {
        bodyStatement = statement;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition#getScope()
	 */
	public IScope getScope() {
		if( scope == null )
			scope = new CFunctionScope( this );
		return scope;
	}

}
