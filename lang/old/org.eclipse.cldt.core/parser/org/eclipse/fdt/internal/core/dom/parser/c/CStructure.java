
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
package org.eclipse.fdt.internal.core.dom.parser.c;

import org.eclipse.fdt.core.dom.ast.DOMException;
import org.eclipse.fdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTDeclaration;
import org.eclipse.fdt.core.dom.ast.IASTDeclarator;
import org.eclipse.fdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTNode;
import org.eclipse.fdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.fdt.core.dom.ast.IBinding;
import org.eclipse.fdt.core.dom.ast.ICompositeType;
import org.eclipse.fdt.core.dom.ast.IField;
import org.eclipse.fdt.core.dom.ast.IProblemBinding;
import org.eclipse.fdt.core.dom.ast.IScope;
import org.eclipse.fdt.core.dom.ast.IType;
import org.eclipse.fdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.fdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.fdt.core.dom.ast.c.ICCompositeTypeScope;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class CStructure implements ICompositeType, ICBinding {
	private IASTElaboratedTypeSpecifier[] declarations = null;
	private ICASTCompositeTypeSpecifier definition;
	
	public CStructure( IASTDeclSpecifier declSpec ){
	    if( declSpec instanceof IASTCompositeTypeSpecifier )
	        definition = (ICASTCompositeTypeSpecifier) declSpec;
	    else {
	        declarations = new IASTElaboratedTypeSpecifier[] { (IASTElaboratedTypeSpecifier) declSpec };
	    }
	}
	
    public IASTNode getPhysicalNode(){
        return ( definition != null ) ? (IASTNode)definition : (IASTNode)declarations[0];
    }
	private ICASTCompositeTypeSpecifier checkForDefinition( IASTElaboratedTypeSpecifier declSpec ){
		IASTDeclSpecifier spec = CVisitor.findDefinition( (ICASTElaboratedTypeSpecifier) declSpec );
		if( spec != null && spec instanceof ICASTCompositeTypeSpecifier ){
			ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) spec;
			((CASTName)compTypeSpec.getName()).setBinding( this );
			return compTypeSpec;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		if( definition != null )
			return definition.getName().toString();

		return declarations[0].getName().toString();
	}
	public char[] getNameCharArray() {
		if( definition != null )
			return ((CASTName)definition.getName()).toCharArray();

		return ((CASTName)declarations[0].getName()).toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() throws DOMException {
	    IASTDeclSpecifier declSpec = (IASTDeclSpecifier) ( ( definition != null ) ? (IASTNode)definition : declarations[0] );
		IScope scope = CVisitor.getContainingScope( declSpec );
		while( scope instanceof ICCompositeTypeScope ){
			scope = scope.getParent();
		}
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.ICompositeType#getFields()
	 */
	public IField[] getFields() {
	    if( definition == null ){
	        ICASTCompositeTypeSpecifier temp = checkForDefinition( declarations[0] );
	        if( temp == null )
	            return new IField [] { new CField.CFieldProblem( IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
	        definition = temp;
	    }

		IASTDeclaration[] members = definition.getMembers();
		int size = members.length;
		IField[] fields = new IField[ size ];
		if( size > 0 ){

			for( int i = 0; i < size; i++ ){
				IASTNode node = members[i];
				if( node instanceof IASTSimpleDeclaration ){
					IASTDeclarator[] declarators = ((IASTSimpleDeclaration)node).getDeclarators();
					for( int j = 0; j < declarators.length; j++ ){
						IASTDeclarator declarator = declarators[j];
						IBinding binding = declarator.getName().resolveBinding();
						if( binding != null )
							fields[i] =  (IField) binding;
					}
				}
			}
			
		}
		return fields;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.ICompositeType#findField(org.eclipse.fdt.core.dom.ast.IASTName)
	 */
	public IField findField(String name) {
	    if( definition == null ){
	        ICASTCompositeTypeSpecifier temp = checkForDefinition( declarations[0] );
	        if( temp == null )
	            return new CField.CFieldProblem( IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() );
	        definition = temp;
	    }

		IASTDeclaration[] members = definition.getMembers();
		int size = members.length;
		if( size > 0 ){
			for( int i = 0; i < size; i++ ){
				IASTNode node = members[i];
				if( node instanceof IASTSimpleDeclaration ){
					IASTDeclarator[] declarators = ((IASTSimpleDeclaration)node).getDeclarators();
					for( int j = 0; j < declarators.length; j++ ){
						IASTDeclarator declarator = declarators[j];
						if( name.equals( declarator.getName().toString() ) ){
							IBinding binding = declarator.getName().resolveBinding();
							if( binding instanceof IField )
								return (IField) binding;
						}
					}
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.ICompositeType#getKey()
	 */
	public int getKey() {
		return ( definition != null ) ? definition.getKey() : declarations[0].getKind();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() {
		return (definition != null ) ? definition.getScope() : null;
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

	/**
	 * @param compositeTypeSpec
	 */
	public void addDefinition(ICASTCompositeTypeSpecifier compositeTypeSpec) {
		definition = compositeTypeSpec;
		((CASTName)compositeTypeSpec.getName()).setBinding( this );
	}
}
