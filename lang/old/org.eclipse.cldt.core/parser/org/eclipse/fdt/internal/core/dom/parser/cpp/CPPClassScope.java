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
 * Created on Nov 29, 2004
 */
package org.eclipse.fdt.internal.core.dom.parser.cpp;

import org.eclipse.fdt.core.dom.ast.DOMException;
import org.eclipse.fdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTDeclarator;
import org.eclipse.fdt.core.dom.ast.IASTName;
import org.eclipse.fdt.core.dom.ast.IASTNode;
import org.eclipse.fdt.core.dom.ast.IASTTypeId;
import org.eclipse.fdt.core.dom.ast.IBasicType;
import org.eclipse.fdt.core.dom.ast.IBinding;
import org.eclipse.fdt.core.dom.ast.IParameter;
import org.eclipse.fdt.core.dom.ast.IType;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.fdt.core.parser.util.ArrayUtil;
import org.eclipse.fdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.fdt.core.parser.util.CharArrayUtils;
import org.eclipse.fdt.core.parser.util.ObjectSet;

/**
 * @author aniefer
 */
public class CPPClassScope extends CPPScope implements ICPPClassScope {
    private ObjectSet constructorBindings = ObjectSet.EMPTY_SET;
    private ObjectSet constructorNames = ObjectSet.EMPTY_SET;
    
	public CPPClassScope( ICPPASTCompositeTypeSpecifier physicalNode ) {
		super( physicalNode );
		((CPPASTCompositeTypeSpecifier)physicalNode).setScope( this );
		createImplicitMembers();
	}

	// 12.1 The default constructor, copy constructor, copy assignment operator, and destructor are
	//special member functions.  The implementation will implicitly declare these member functions
	//for a class type when the program does not declare them.
	private void createImplicitMembers(){
	    //create bindings for the implicit members, if the user declared them then those declarations
	    //will resolve to these bindings.
	    ICPPASTCompositeTypeSpecifier compTypeSpec;
        try {
            compTypeSpec = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
        } catch ( DOMException e ) {
            return;
        }
        
        IASTName name = compTypeSpec.getName();
        if( name instanceof ICPPASTQualifiedName ){
        	IASTName [] ns = ((ICPPASTQualifiedName) name).getNames();
        	name = ns[ ns.length - 1 ];
        }
        
        IBinding binding = compTypeSpec.getName().resolveBinding();
        if( !(binding instanceof ICPPClassType ) )
        	return;
        ICPPClassType clsType = (ICPPClassType) binding;
        
        char [] className = name.toCharArray();
               
        //default constructor: A()
	    addBinding( new CPPImplicitConstructor( this, className, IParameter.EMPTY_PARAMETER_ARRAY ) );
	    
	    //copy constructor: A( const A & )
	    IType pType = new CPPReferenceType( new CPPQualifierType( clsType, true, false ) );
	    IParameter [] ps = new IParameter [] { new CPPParameter( pType ) };
	    addBinding( new CPPImplicitConstructor( this, className, ps ) );
	    
	    //copy assignment operator: A& operator = ( const A & ) 
	    IType refType = new CPPReferenceType( clsType );
	    addBinding( new CPPImplicitMethod( this, "operator =".toCharArray(), refType, ps ) ); //$NON-NLS-1$
	    
	    //destructor: ~A()
	    char [] dtorName = CharArrayUtils.concat( "~".toCharArray(), className );  //$NON-NLS-1$
	    addBinding( new CPPImplicitMethod( this, dtorName, new CPPBasicType( IBasicType.t_unspecified, 0 ), IParameter.EMPTY_PARAMETER_ARRAY ) );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPScope#addBinding(org.eclipse.fdt.core.dom.ast.IBinding)
	 */
	public void addBinding(IBinding binding) {
	    if( binding instanceof ICPPConstructor ){
	        addConstructor( binding );
	        return;
	    }
		if( bindings == null )
			bindings = new CharArrayObjectMap(1);
		char [] c = binding.getNameCharArray();
		Object o = bindings.get( c );
		if( o != null ){
		    if( o instanceof Object[] ){
		        bindings.put( c, ArrayUtil.append( Object.class, (Object[]) o, binding ) );
		    } else {
		        bindings.put( c, new Object[] { o, binding } );
		    }
		} else {
		    bindings.put( c, binding );
		}
	}

	public void addName(IASTName name) {
		IASTNode parent = name.getParent();
		if( parent instanceof IASTDeclarator ){
			if( CPPVisitor.isConstructor( this, (IASTDeclarator) parent ) ){
				addConstructor( name );
				return;
			}
		}
		super.addName( name );
	}

	private void addConstructor( Object constructor ){
		if( constructor instanceof IBinding ){
		    if( constructorBindings == ObjectSet.EMPTY_SET )
		        constructorBindings = new ObjectSet( 2 );
		    
		    constructorBindings.put( constructor );
		} else {
			if( constructorNames == ObjectSet.EMPTY_SET )
				constructorNames = new ObjectSet( 2 );
		    
		    constructorNames.put( constructor );
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPScope#getBinding(int, char[])
	 */
	public IBinding getBinding( IASTName name, boolean resolve ) throws DOMException {
	    char [] c = name.toCharArray();
	
	    ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
	    IASTName compName = compType.getName();
	    if( compName instanceof ICPPASTQualifiedName ){
	    	IASTName [] ns = ((ICPPASTQualifiedName)compName).getNames();
	    	compName = ns[ ns.length - 1 ];
	    }
	    if( CharArrayUtils.equals( c, compName.toCharArray() ) ){
	        if( isConstructorReference( name ) ){
//	            if( constructors == null )
//	                return null;
	            return CPPSemantics.resolveAmbiguities( name, getConstructors( resolve ) );
	        }
            //9.2 ... The class-name is also inserted into the scope of the class itself
            return compType.getName().resolveBinding();
	    }
	    return super.getBinding( name, resolve );
	}

	protected ICPPConstructor [] getConstructors( boolean forceResolve ){
		if( forceResolve && constructorNames.size() > 0 ){
			Object [] names = constructorNames.keyArray();
			for( int i = 0; i < names.length; i++ ){
				ICPPConstructor ctor = (ICPPConstructor) ((IASTName)names[i]).resolveBinding();
				constructorBindings.put( ctor );
			}
			constructorNames.clear();
		}
	    return (ICPPConstructor[]) ArrayUtil.trim( ICPPConstructor.class, constructorBindings.keyArray(), true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public IBinding[] find(String name) throws DOMException {
	    char [] n = name.toCharArray();
	    ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
	    if( CharArrayUtils.equals( n, compType.getName().toCharArray() ) ){
	        return (IBinding[]) ArrayUtil.addAll( IBinding.class, null, getConstructors( true ) );
	    }
	    return super.find( name );
	}
	
	private boolean isConstructorReference( IASTName name ){
	    IASTNode node = name.getParent();
	    if( node instanceof ICPPASTQualifiedName ){
	    	IASTName [] ns = ((ICPPASTQualifiedName)node).getNames();
	    	if( ns[ ns.length - 1 ] == name )
	    		node = node.getParent();
	    }
	    if( node instanceof IASTDeclSpecifier ){
	        IASTNode parent = node.getParent();
	        if( parent instanceof IASTTypeId && parent.getParent() instanceof ICPPASTNewExpression )
	            return true;
	        return false;
	    }
	    return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPClassScope#getClassType()
	 */
	public ICPPClassType getClassType() {
		ICPPASTCompositeTypeSpecifier compSpec;
		try {
			compSpec = (ICPPASTCompositeTypeSpecifier) getPhysicalNode();
		} catch (DOMException e) {
			return null;
		}
		return (ICPPClassType) compSpec.getName().resolveBinding();
		
	}
}
