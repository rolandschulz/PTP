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
 * Created on Dec 1, 2004
 */
package org.eclipse.fdt.internal.core.dom.parser.cpp;

import org.eclipse.fdt.core.dom.ast.DOMException;
import org.eclipse.fdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTDeclarator;
import org.eclipse.fdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.fdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.fdt.core.dom.ast.IASTName;
import org.eclipse.fdt.core.dom.ast.IASTNode;
import org.eclipse.fdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.fdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.fdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.fdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.fdt.core.dom.ast.IBinding;
import org.eclipse.fdt.core.dom.ast.IFunction;
import org.eclipse.fdt.core.dom.ast.IFunctionType;
import org.eclipse.fdt.core.dom.ast.IParameter;
import org.eclipse.fdt.core.dom.ast.IScope;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.fdt.core.parser.util.ArrayUtil;
import org.eclipse.fdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPFunction implements IFunction, ICPPBinding {
    
    public static class CPPFunctionProblem extends ProblemBinding implements IFunction {
        public CPPFunctionProblem( int id, char[] arg ) {
            super( id, arg );
        }

        public IParameter[] getParameters() throws DOMException {
            throw new DOMException( this );
        }

        public IScope getFunctionScope() throws DOMException {
            throw new DOMException( this );
        }

        public IFunctionType getType() throws DOMException {
            throw new DOMException( this );
        }

        public boolean isStatic() throws DOMException {
            throw new DOMException( this );
        }
    }
    
	protected ICPPASTFunctionDeclarator [] declarations;
	protected ICPPASTFunctionDeclarator definition;
	protected IFunctionType type = null;
	
	private static final int FULLY_RESOLVED         = 1;
	private static final int RESOLUTION_IN_PROGRESS = 1 << 1;
	private static final int IS_STATIC              = 3 << 2;
	private int bits = 0;
	
	public CPPFunction( ICPPASTFunctionDeclarator declarator ){
	    if( declarator != null ) {
			IASTNode parent = declarator.getParent();
			if( parent instanceof IASTFunctionDefinition )
				definition = declarator;
			else
				declarations = new ICPPASTFunctionDeclarator [] { declarator };
	    
		    IASTName name = declarator.getName();
		    if( name instanceof ICPPASTQualifiedName ){
		        IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
		        name = ns[ ns.length - 1 ];
		    }
		    ((CPPASTName)name).setBinding( this );
	    }
	}
	
	private void resolveAllDeclarations(){
	    if( (bits & (FULLY_RESOLVED | RESOLUTION_IN_PROGRESS)) == 0 ){
	        bits |= RESOLUTION_IN_PROGRESS;
		    IASTTranslationUnit tu = null;
	        if( definition != null )
	            tu = definition.getTranslationUnit();
	        else if( declarations != null )
	            tu = declarations[0].getTranslationUnit();
	        else {
	            //implicit binding
	            IScope scope = getScope();
                try {
                    IASTNode node = scope.getPhysicalNode();
                    while( !( node instanceof IASTTranslationUnit) )
    	                node = node.getParent();
    	            tu = (IASTTranslationUnit) node;
                } catch ( DOMException e ) {
                }
	        }
	        if( tu != null ){
	            CPPVisitor.getDeclarations( tu, this );
	        }
	        declarations = (ICPPASTFunctionDeclarator[]) ArrayUtil.trim( ICPPASTFunctionDeclarator.class, declarations );
	        bits |= FULLY_RESOLVED;
	        bits &= ~RESOLUTION_IN_PROGRESS;
	    }
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.fdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return declarations;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.internal.core.dom.parser.cpp.ICPPBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        return definition;
    }
    
	public void addDefinition( ICPPASTFunctionDeclarator dtor ){
		updateParameterBindings( dtor );
		definition = dtor;
	}
	public void addDeclaration( ICPPASTFunctionDeclarator dtor ){
		updateParameterBindings( dtor );
		if( declarations == null ){
			declarations = new ICPPASTFunctionDeclarator [] { dtor };
			return;
		}
		for( int i = 0; i < declarations.length; i++ ){
		    if( declarations[i] == dtor ){
		        //already in
		        return;
		    } else if( declarations[i] == null ){
				declarations[i] = dtor;
				updateParameterBindings( dtor );
				return;
			}
		}
		ICPPASTFunctionDeclarator [] tmp = new ICPPASTFunctionDeclarator[ declarations.length * 2 ];
		System.arraycopy( declarations, 0, tmp, 0, declarations.length );
		tmp[ declarations.length ] = dtor;
		declarations = tmp;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IFunction#getParameters()
	 */
	public IParameter [] getParameters() {
	    IASTStandardFunctionDeclarator dtor = ( definition != null ) ? definition : declarations[0];
		IASTParameterDeclaration[] params = dtor.getParameters();
		int size = params.length;
		IParameter [] result = new IParameter[ size ];
		if( size > 0 ){
			for( int i = 0; i < size; i++ ){
				IASTParameterDeclaration p = params[i];
				result[i] = (IParameter) p.getDeclarator().getName().resolveBinding();
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IFunction#getFunctionScope()
	 */
	public IScope getFunctionScope() {
	    resolveAllDeclarations();
	    if( definition != null ){
			return definition.getFunctionScope();
	    } 
	        
	    return declarations[0].getFunctionScope();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
	    IASTName name = (definition != null ) ? definition.getName() : declarations[0].getName();
	    if( name instanceof ICPPASTQualifiedName ){
	        IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
	        name = ns[ ns.length - 1 ];
	    }
		return name.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
	    IASTName name = (definition != null ) ? definition.getName() : declarations[0].getName();
	    if( name instanceof ICPPASTQualifiedName ){
	        IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
	        name = ns[ ns.length - 1 ];
	    }
		return name.toCharArray();	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
	    ICPPASTDeclSpecifier declSpec = null;
	    if( definition != null ){
	    	IASTNode node = definition.getParent();
	    	while( node instanceof IASTDeclarator )
	    		node = node.getParent();
	        IASTFunctionDefinition def = (IASTFunctionDefinition) node;
		    declSpec = (ICPPASTDeclSpecifier) def.getDeclSpecifier();    
	    } else {
	    	IASTNode node = declarations[0].getParent();
	    	while( node instanceof IASTDeclarator )
	    		node = node.getParent();
	        IASTSimpleDeclaration decl = (IASTSimpleDeclaration)node; 
	        declSpec = (ICPPASTDeclSpecifier) decl.getDeclSpecifier();
	    }	

	    IScope scope = CPPVisitor.getContainingScope( definition != null ? definition : declarations[0] );
	    if( declSpec.isFriend() && scope instanceof ICPPClassScope ){
	        try {
                while( scope instanceof ICPPClassScope ){
	                scope = scope.getParent();
                }
	        } catch ( DOMException e ) {
            }
	    }
		return scope;
	}

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IFunction#getType()
     */
    public IFunctionType getType() {
        if( type == null )
            type = (IFunctionType) CPPVisitor.createType( ( definition != null ) ? definition : declarations[0] );
        return type;
    }

    public IBinding resolveParameter( IASTParameterDeclaration param ){
    	IASTName name = param.getDeclarator().getName();
    	IBinding binding = ((CPPASTName)name).getBinding();
    	if( binding != null )
    		return binding;
		
    	IASTStandardFunctionDeclarator fdtor = (IASTStandardFunctionDeclarator) param.getParent();
    	IASTParameterDeclaration [] ps = fdtor.getParameters();
    	int i = 0;
    	for( ; i < ps.length; i++ ){
    		if( param == ps[i] )
    			break;
    	}
    	
    	//create a new binding and set it for the corresponding parameter in all known defns and decls
    	binding = new CPPParameter( name );
    	IASTParameterDeclaration temp = null;
    	if( definition != null ){
    		temp = definition.getParameters()[i];
    		CPPASTName n = (CPPASTName)temp.getDeclarator().getName();
    		if( n != name ) {
    		    n.setBinding( binding );
    		    ((CPPParameter)binding).addDeclaration( n );
    		}
    	}
    	if( declarations != null ){
    		for( int j = 0; j < declarations.length && declarations[j] != null; j++ ){
    			temp = declarations[j].getParameters()[i];
        		CPPASTName n = (CPPASTName)temp.getDeclarator().getName();
        		if( n != name ) {
        		    n.setBinding( binding );
        		    ((CPPParameter)binding).addDeclaration( n );
        		}

    		}
    	}
    	return binding;
    }
    
    protected void updateParameterBindings( ICPPASTFunctionDeclarator fdtor ){
    	ICPPASTFunctionDeclarator orig = definition != null ? definition : declarations[0];
    	IASTParameterDeclaration [] ops = orig.getParameters();
    	IASTParameterDeclaration [] nps = fdtor.getParameters();
    	CPPParameter temp = null;
    	for( int i = 0; i < nps.length; i++ ){
    		temp = (CPPParameter) ((CPPASTName)ops[i].getDeclarator().getName()).getBinding();
    		if( temp != null ){
    		    CPPASTName name = (CPPASTName) nps[i].getDeclarator().getName();
    			name.setBinding( temp );
    			temp.addDeclaration( name );
    		}
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IFunction#isStatic()
     */
    public boolean isStatic() {
        if( (bits & FULLY_RESOLVED) == 0 ){
            resolveAllDeclarations();
        }

        //2 state bits, most significant = whether or not we've figure this out yet
        //least significant = whether or not we are static
        int state = ( bits & IS_STATIC ) >> 2;
        if( state > 1 ) return (state % 2 != 0);
        
        
        IASTFunctionDeclarator dtor = (IASTFunctionDeclarator) getDefinition();
        IASTDeclSpecifier declSpec = ((IASTFunctionDefinition)dtor.getParent()).getDeclSpecifier();
        if( declSpec.getStorageClass() == IASTDeclSpecifier.sc_static ){
            bits |= 3 << 2;
            return true;
        }
        
        IASTFunctionDeclarator[] dtors = (IASTFunctionDeclarator[]) getDeclarations();
        for( int i = 0; i < dtors.length; i++ ){
            IASTNode parent = dtors[i].getParent();
            declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
            if( declSpec.getStorageClass() == IASTDeclSpecifier.sc_static ){
                bits |= 3 << 2;
                return true;
            }
        }
        bits |= 2 << 2;
        return false;
    }
}
