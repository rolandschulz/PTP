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
 * Created on Nov 23, 2004
 */
package org.eclipse.fdt.internal.core.dom.parser.c;

import org.eclipse.fdt.core.dom.ast.DOMException;
import org.eclipse.fdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTNode;
import org.eclipse.fdt.core.dom.ast.IEnumeration;
import org.eclipse.fdt.core.dom.ast.IEnumerator;
import org.eclipse.fdt.core.dom.ast.IScope;
import org.eclipse.fdt.core.dom.ast.IType;
import org.eclipse.fdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.fdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CEnumerator implements IEnumerator {
    public static class CEnumeratorProblem extends ProblemBinding implements IEnumerator {
        public CEnumeratorProblem( int id, char[] arg ) {
            super( id, arg );
        }

        public IType getType() throws DOMException {
            throw new DOMException( this );
        }
    }

    private final IASTEnumerator enumerator;
    public CEnumerator( IASTEnumerator enumtor ){
		this.enumerator= enumtor;
	}
    
    public IASTNode getPhysicalNode(){
        return enumerator;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        return enumerator.getName().toString();
    }
    public char[] getNameCharArray(){
        return ((CASTName) enumerator.getName()).toCharArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return CVisitor.getContainingScope( enumerator );
    }

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IEnumerator#getType()
	 */
	public IType getType() {
		IASTEnumerationSpecifier enumSpec = (IASTEnumerationSpecifier) enumerator.getParent();
		IEnumeration enumeration = (IEnumeration) enumSpec.getName().resolveBinding();
		return enumeration;
	}

}
