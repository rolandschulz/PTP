/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.fdt.internal.core.dom.parser.c;

import org.eclipse.fdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTDeclaration;
import org.eclipse.fdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTName;
import org.eclipse.fdt.core.dom.ast.IASTNode;
import org.eclipse.fdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.fdt.core.dom.ast.IParameter;
import org.eclipse.fdt.core.dom.ast.IScope;
import org.eclipse.fdt.core.dom.ast.IType;
import org.eclipse.fdt.core.dom.ast.c.ICASTTypedefNameSpecifier;

/**
 * A K&R C parameter.
 *
 * @author dsteffle
 */
public class CKnRParameter implements IParameter {
	final private IASTDeclaration declaration;
	final private IASTName name;

	public CKnRParameter(IASTDeclaration declaration, IASTName name) {
		this.declaration = declaration;
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IVariable#getType()
	 */
	public IType getType() {
		IASTDeclSpecifier declSpec = null;
		if (declaration instanceof IASTSimpleDeclaration)
			declSpec = ((IASTSimpleDeclaration)declaration).getDeclSpecifier();
			
		if( declSpec != null && declSpec instanceof ICASTTypedefNameSpecifier ){
			ICASTTypedefNameSpecifier nameSpec = (ICASTTypedefNameSpecifier) declSpec;
			return (IType) nameSpec.getName().resolveBinding();
		} else if( declSpec != null && declSpec instanceof IASTElaboratedTypeSpecifier ){
			IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) declSpec;
			return (IType) elabTypeSpec.getName().resolveBinding();
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return name.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return name.toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CVisitor.getContainingScope( declaration );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IBinding#getPhysicalNode()
	 */
	public IASTNode getPhysicalNode() {
		return declaration;
	}

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IVariable#isStatic()
     */
    public boolean isStatic() {
        return false;
    }

}
