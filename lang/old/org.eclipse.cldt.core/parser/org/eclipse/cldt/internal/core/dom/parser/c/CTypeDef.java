
/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cldt.internal.core.dom.parser.c;

import org.eclipse.cldt.core.dom.ast.IASTDeclarator;
import org.eclipse.cldt.core.dom.ast.IASTName;
import org.eclipse.cldt.core.dom.ast.IASTNode;
import org.eclipse.cldt.core.dom.ast.IScope;
import org.eclipse.cldt.core.dom.ast.IType;
import org.eclipse.cldt.core.dom.ast.ITypedef;
import org.eclipse.cldt.internal.core.dom.parser.ITypeContainer;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class CTypeDef implements ITypedef, ITypeContainer {
	private final IASTName name; 
	private IType type = null;
	
	public CTypeDef( IASTName name ){
		this.name = name;
	}
	
    public IASTNode getPhysicalNode(){
        return name;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ITypedef#getType()
	 */
	public IType getType() {
		if (type == null)
			type = CVisitor.createType(name);
		return type;
	}
	
	public void setType( IType t ){
	    type = t;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return name.toString();
	}
	public char[] getNameCharArray(){
	    return ((CASTName) name).toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		IASTDeclarator declarator = (IASTDeclarator) name.getParent();
		return CVisitor.getContainingScope( declarator.getParent() );
	}

    public Object clone(){
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch ( CloneNotSupportedException e ) {
            //not going to happen
        }
        return t;
    }
}
