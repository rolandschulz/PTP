/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Dec 14, 2004
 */
package org.eclipse.fdt.internal.core.dom.parser.cpp;

import org.eclipse.fdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTName;
import org.eclipse.fdt.core.dom.ast.IASTNode;
import org.eclipse.fdt.core.dom.ast.IEnumeration;
import org.eclipse.fdt.core.dom.ast.IEnumerator;
import org.eclipse.fdt.core.dom.ast.IScope;
import org.eclipse.fdt.core.dom.ast.IType;
import org.eclipse.fdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;

/**
 * @author aniefer
 */
public class CPPEnumerator implements IEnumerator, ICPPBinding {
    private IASTName enumName;
    /**
     * @param enumerator
     */
    public CPPEnumerator( IASTName enumerator ) {
        this.enumName = enumerator;
        ((CPPASTName)enumerator).setBinding( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.internal.core.dom.parser.cpp.ICPPBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        return enumName;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        return enumName.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return enumName.toCharArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return CPPVisitor.getContainingScope( enumName );
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IBinding#getPhysicalNode()
     */
    public IASTNode getPhysicalNode() {
        return enumName;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IEnumerator#getType()
	 */
	public IType getType() {
	    IASTEnumerator etor = (IASTEnumerator) enumName.getParent();
		IASTEnumerationSpecifier enumSpec = (IASTEnumerationSpecifier) etor.getParent();
		IEnumeration enumeration = (IEnumeration) enumSpec.getName().resolveBinding();
		return enumeration;
	}
}
