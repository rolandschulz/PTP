
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

import org.eclipse.cldt.core.dom.ast.DOMException;
import org.eclipse.cldt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cldt.core.dom.ast.IASTDeclarator;
import org.eclipse.cldt.core.dom.ast.IASTName;
import org.eclipse.cldt.core.dom.ast.IASTNode;
import org.eclipse.cldt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cldt.core.dom.ast.IScope;
import org.eclipse.cldt.core.dom.ast.IType;
import org.eclipse.cldt.core.dom.ast.IVariable;
import org.eclipse.cldt.core.parser.util.ArrayUtil;
import org.eclipse.cldt.internal.core.dom.parser.ProblemBinding;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CVariable implements IVariable, ICBinding {
    public static class CVariableProblem extends ProblemBinding implements IVariable {
        public CVariableProblem( int id, char[] arg ) {
            super( id, arg );
        }

        public IType getType() throws DOMException {
            throw new DOMException( this );
        }
        public boolean isStatic() throws DOMException {
            throw new DOMException( this );
        }
        
    }
	private IASTName [] declarations = null;
	private IType type = null;
	
	public CVariable( IASTName name ){
		declarations = new IASTName [] { name };
	}
    public IASTNode getPhysicalNode(){
        return declarations[0];
    }	

    public void addDeclaration( IASTName name ){
        declarations = (IASTName[]) ArrayUtil.append( IASTName.class, declarations, name );
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	public IType getType() {
		if (type == null)
			type = CVisitor.createType(declarations[0]);
		return type;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return declarations[0].toString();
	}
	public char[]getNameCharArray(){
	    return declarations[0].toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		IASTDeclarator declarator = (IASTDeclarator) declarations[0].getParent();
		return CVisitor.getContainingScope( declarator.getParent() );
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isStatic()
     */
    public boolean isStatic() {
        IASTDeclarator dtor = (IASTDeclarator) declarations[0].getParent();
        while( dtor.getParent() instanceof IASTDeclarator )
            dtor = (IASTDeclarator) dtor.getParent();
        
        IASTSimpleDeclaration simple = (IASTSimpleDeclaration) dtor.getParent();
        IASTDeclSpecifier declSpec = simple.getDeclSpecifier();
        return ( declSpec.getStorageClass() == IASTDeclSpecifier.sc_static );
    }
}
