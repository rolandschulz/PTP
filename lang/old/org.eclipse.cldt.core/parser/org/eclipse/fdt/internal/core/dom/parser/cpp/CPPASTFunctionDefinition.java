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

import org.eclipse.fdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.fdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.fdt.core.dom.ast.IASTStatement;
import org.eclipse.fdt.core.dom.ast.IScope;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

/**
 * @author jcamelon
 */
public class CPPASTFunctionDefinition extends CPPASTNode implements
        IASTFunctionDefinition {

    private IASTDeclSpecifier declSpecifier;
    private IASTFunctionDeclarator declarator;
    private IASTStatement bodyStatement;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFunctionDefinition#getDeclSpecifier()
     */
    public IASTDeclSpecifier getDeclSpecifier() {
        return declSpecifier;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFunctionDefinition#setDeclSpecifier(org.eclipse.fdt.core.dom.ast.IASTDeclSpecifier)
     */
    public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        declSpecifier = declSpec;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFunctionDefinition#getDeclarator()
     */
    public IASTFunctionDeclarator getDeclarator() {
        return declarator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFunctionDefinition#setDeclarator(org.eclipse.fdt.core.dom.ast.IASTFunctionDeclarator)
     */
    public void setDeclarator(IASTFunctionDeclarator declarator) {
        this.declarator = declarator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFunctionDefinition#getBody()
     */
    public IASTStatement getBody() {
        return bodyStatement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTFunctionDefinition#setBody(org.eclipse.fdt.core.dom.ast.IASTStatement)
     */
    public void setBody(IASTStatement statement) {
        bodyStatement = statement;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IASTFunctionDefinition#getScope()
	 */
	public IScope getScope() {
		return ((ICPPASTFunctionDeclarator)declarator).getFunctionScope();
	}

    
}
