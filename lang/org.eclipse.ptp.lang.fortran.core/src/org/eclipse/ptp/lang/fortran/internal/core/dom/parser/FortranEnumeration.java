/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

/*
 * Created on Nov 23, 2004
 */
package org.eclipse.ptp.lang.fortran.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.c.ICInternalBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.core.runtime.PlatformObject;

/**
 * @author aniefer
 */
public class FortranEnumeration extends PlatformObject implements IEnumeration, ICInternalBinding {

    private IASTName [] declarations = null;
    private IASTName definition = null;
    public FortranEnumeration( IASTName enumeration ){
        ASTNodeProperty prop = enumeration.getPropertyInParent();
        if( prop == IASTElaboratedTypeSpecifier.TYPE_NAME )
        	declarations = new IASTName[] { enumeration };
        else
            definition = enumeration;
        enumeration.setBinding( this );
	}
	
    public void addDeclaration( IASTName decl ){
        if( decl.getPropertyInParent() != IASTElaboratedTypeSpecifier.TYPE_NAME )
            return;
            
        decl.setBinding( this );
        if( declarations == null ){
            declarations = new IASTName[] { decl };
        	return;
        }
        for( int i = 0; i < declarations.length; i++ ){
            if( declarations[i] == null ){
                declarations[i] = decl;
                return;
            }
        }
        IASTName tmp [] = new IASTName [ declarations.length * 2 ];
        System.arraycopy( declarations, 0, tmp, 0, declarations.length );
        tmp[ declarations.length ] = decl;
        declarations = tmp;
    }
    
    public IASTNode getPhysicalNode(){
        if( definition != null )
            return definition;
        
        return declarations[0];
    }
    
	private void checkForDefinition(){
		IASTDeclSpecifier spec = FortranVisitor.findDefinition( (ICASTElaboratedTypeSpecifier) declarations[0].getParent() );
		if( spec != null && spec instanceof ICASTEnumerationSpecifier ){
		    ICASTEnumerationSpecifier enumSpec = (ICASTEnumerationSpecifier) spec;
		    
			enumSpec.getName().setBinding( this );
			definition = enumSpec.getName();
		}
		return;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        if( definition != null )
            return definition.toString();
        
        return declarations[0].toString();
    }
    public char[] getNameCharArray(){
        if( definition != null )
            return definition.toCharArray();
        
        return declarations[0].toCharArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return FortranVisitor.getContainingScope( definition != null ? definition : declarations[0].getParent() );
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IEnumeration#getEnumerators()
     */
    public IEnumerator[] getEnumerators() {
        if( definition == null ){
            checkForDefinition();
            if( definition == null )
                return new IEnumerator[] { new FortranEnumerator.CEnumeratorProblem( declarations[0], IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, declarations[0].toCharArray() ) };
        }
        
        IASTEnumerationSpecifier enumSpec = (IASTEnumerationSpecifier) definition.getParent();
        IASTEnumerationSpecifier.IASTEnumerator[] enums = enumSpec.getEnumerators();
        IEnumerator [] bindings = new IEnumerator [ enums.length ];
        
        for( int i = 0; i < enums.length; i++ ){
            bindings[i] = (IEnumerator) enums[i].getName().resolveBinding();
        }
        return bindings;
    }

    /**
     * @param name
     */
    public void addDefinition( IASTName name ) {
        definition = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    public boolean isSameType( IType type ) {
        if( type == this )
            return true;
        if( type instanceof ITypedef || type instanceof IIndexType)
            return type.isSameType( this );

        return false;
    }
    
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}
}
