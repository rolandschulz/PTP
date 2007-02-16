/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Markus Schorn (Wind River Systems) 
 *******************************************************************************/

package org.eclipse.ptp.lang.fortran.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class FortranStructure extends PlatformObject implements ICompositeType, IFortranInternalBinding {
	
	public static class CStructureProblem extends ProblemBinding implements ICompositeType {
		public CStructureProblem(IASTNode node, int id, char[] arg) {
			super(node, id, arg);
		}
		public IField findField(String name) throws DOMException {
			throw new DOMException( this );
		}
		public IScope getCompositeScope() throws DOMException {
			throw new DOMException( this );
		}
		public IField[] getFields() throws DOMException {
			throw new DOMException( this );
		}
		public int getKey() throws DOMException {
			throw new DOMException( this );
		}
	}

	private IASTName [] declarations = null;
	private IASTName definition;
	
	public FortranStructure( IASTName name ){
	    if( name.getPropertyInParent() == IASTCompositeTypeSpecifier.TYPE_NAME )
	        definition = name;
	    else {
	        declarations = new IASTName[] { name };
	    }
	    name.setBinding( this );
	}
	
    public IASTNode getPhysicalNode(){
        return ( definition != null ) ? (IASTNode)definition : (IASTNode)declarations[0];
    }
	private ICASTCompositeTypeSpecifier checkForDefinition( IASTElaboratedTypeSpecifier declSpec ){
		IASTDeclSpecifier spec = FortranVisitor.findDefinition( (ICASTElaboratedTypeSpecifier) declSpec );
		if( spec != null && spec instanceof ICASTCompositeTypeSpecifier ){
			ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) spec;
			compTypeSpec.getName().setBinding( this );
			return compTypeSpec;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		if( definition != null )
			return definition.toString();

		return declarations[0].toString();
	}
	public char[] getNameCharArray() {
		if( definition != null )
			return definition.toCharArray();

		return declarations[0].toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() throws DOMException {
	    IASTDeclSpecifier declSpec = (IASTDeclSpecifier) ( ( definition != null ) ? (IASTNode)definition.getParent() : declarations[0].getParent() );
		IScope scope = FortranVisitor.getContainingScope( declSpec );
		while( scope instanceof ICCompositeTypeScope ){
			scope = scope.getParent();
		}
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public IField[] getFields() throws DOMException {
	    if( definition == null ){
	        ICASTCompositeTypeSpecifier temp = checkForDefinition( (IASTElaboratedTypeSpecifier) declarations[0].getParent() );
	        if( temp == null )
	            return new IField [] { new FortranField.FortranFieldProblem( declarations[0], IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() ) };
	        definition = temp.getName();
	    }
	    ICASTCompositeTypeSpecifier compSpec = (ICASTCompositeTypeSpecifier) definition.getParent();
		IASTDeclaration[] members = compSpec.getMembers();
		int size = members.length;
		IField[] fields = new IField[ size ];
		if( size > 0 ){
		    ICCompositeTypeScope scope = (ICCompositeTypeScope) getCompositeScope();
		    if( ASTInternal.isFullyCached(scope) )
		        scope = null;
			for( int i = 0; i < size; i++ ){
				IASTNode node = members[i];
				if( node instanceof IASTSimpleDeclaration ){
					IASTDeclarator[] declarators = ((IASTSimpleDeclaration)node).getDeclarators();
					for( int j = 0; j < declarators.length; j++ ){
						IASTDeclarator declarator = declarators[j];
						IASTName name = declarator.getName();
						IBinding binding = name.resolveBinding();
						if( scope != null )
						    ASTInternal.addName(scope, name );
						if( binding != null )
							fields = (IField[]) ArrayUtil.append( IField.class, fields, binding );
					}
				}
			}
			if( scope != null )
				ASTInternal.setFullyCached(scope, true);
		}
		return (IField[]) ArrayUtil.trim( IField.class, fields );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#findField(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public IField findField(String name) throws DOMException {
	    if( definition == null ){
	        ICASTCompositeTypeSpecifier temp = checkForDefinition( (IASTElaboratedTypeSpecifier) declarations[0].getParent() );
	        if( temp == null )
	            return new FortranField.FortranFieldProblem( declarations[0], IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, getNameCharArray() );
	        definition = temp.getName();
	    }
	    
	    ICCompositeTypeScope scope = (ICCompositeTypeScope) getCompositeScope();
	    if( scope != null && ASTInternal.isFullyCached(scope) ){
	        IBinding binding = scope.getBinding( name.toCharArray() );
	        if( binding instanceof IField )
	            return (IField) binding;
	    } else {
	        ICASTCompositeTypeSpecifier compSpec = (ICASTCompositeTypeSpecifier) definition.getParent();
	        ICASTCompositeTypeSpecifier [] specStack = null;
	        int stackIdx = -1;
	    	IASTDeclaration[] members = compSpec.getMembers();
	    	IField found = null;
	    	while( members != null ){
		    	int size = members.length;
	    		for( int i = 0; i < size; i++ ){
	    			IASTNode node = members[i];
	    			if( node instanceof IASTSimpleDeclaration ){
	    				IASTDeclarator[] declarators = ((IASTSimpleDeclaration)node).getDeclarators();
	    				for( int j = 0; j < declarators.length; j++ ){
	    					IASTDeclarator declarator = declarators[j];
	    					IASTName dtorName = declarator.getName();
	    					if( scope != null )
	    					    ASTInternal.addName( scope,  dtorName );
	    					if( name.equals( dtorName.toString() ) ){
	    						IBinding binding = dtorName.resolveBinding();
	    						if( binding instanceof IField )
	    							found = (IField) binding;
	    					}
	    				}
	    				//anonymous structurs and unions
	    				if( declarators.length == 0 && ((IASTSimpleDeclaration)node).getDeclSpecifier() instanceof IASTCompositeTypeSpecifier ){
	    				    IASTCompositeTypeSpecifier declSpec = (IASTCompositeTypeSpecifier) ((IASTSimpleDeclaration)node).getDeclSpecifier();
	    				    IASTName n = declSpec.getName();
	    				    if( n.toCharArray().length == 0 ){
	    				        specStack = (ICASTCompositeTypeSpecifier[])ArrayUtil.append( ICASTCompositeTypeSpecifier.class, specStack, declSpec );
	    				    }
	    				}
	    			}
	    		}
		    	if( specStack != null && ++stackIdx < specStack.length && specStack[stackIdx] != null ){
		    	    members = specStack[stackIdx].getMembers();
		    	} else {
		    	    members = null;
		    	}
	    	}
    		if( scope != null )
    		    ASTInternal.setFullyCached(scope, true);
    		if( found != null )
    		    return found;
	    }
	    
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
	 */
	public int getKey() {
		return ( definition != null ) ? ((IASTCompositeTypeSpecifier)definition.getParent()).getKey() 
		        					  : ((IASTElaboratedTypeSpecifier)declarations[0].getParent()).getKind();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() {
		return (definition != null ) ? ((IASTCompositeTypeSpecifier)definition.getParent()).getScope() : null;
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
		definition = compositeTypeSpec.getName();
		compositeTypeSpec.getName().setBinding( this );
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
