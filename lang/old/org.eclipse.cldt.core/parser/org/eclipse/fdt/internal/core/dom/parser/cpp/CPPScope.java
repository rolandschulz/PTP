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
import org.eclipse.fdt.core.dom.ast.IASTName;
import org.eclipse.fdt.core.dom.ast.IASTNode;
import org.eclipse.fdt.core.dom.ast.IBinding;
import org.eclipse.fdt.core.dom.ast.IScope;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPCompositeBinding;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.fdt.core.parser.util.ArrayUtil;
import org.eclipse.fdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.fdt.core.parser.util.ObjectSet;
import org.eclipse.fdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.fdt.internal.core.dom.parser.cpp.CPPSemantics.LookupData;

/**
 * @author aniefer
 */
abstract public class CPPScope implements ICPPScope{
    public static class CPPScopeProblem extends ProblemBinding implements ICPPScope {
        public CPPScopeProblem( int id, char[] arg ) {
            super( id, arg );
        }
        public void addName( IASTName name ) throws DOMException {
            throw new DOMException( this );
        }

        public IBinding getBinding( IASTName name, boolean resolve ) throws DOMException {
            throw new DOMException( this );
        }

        public IScope getParent() throws DOMException {
            throw new DOMException( this );
        }

        public IBinding[] find( String name ) throws DOMException {
            throw new DOMException( this );
        }
		public void setFullyCached(boolean b) {
		}
		public boolean isFullyCached() {
			return false;
		}
    }
    public static class CPPTemplateProblem extends CPPScopeProblem {
		public CPPTemplateProblem(int id, char[] arg) {
			super(id, arg);
		}
    }

    
	private IASTNode physicalNode;
	public CPPScope( IASTNode physicalNode ) {
		this.physicalNode = physicalNode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IScope#getParent()
	 */
	public IScope getParent() throws DOMException {
		return CPPVisitor.getContainingScope( physicalNode );
	}
	
	public IASTNode getPhysicalNode() throws DOMException{
		return physicalNode;
	}

	protected CharArrayObjectMap bindings = null;
	
	public void addName(IASTName name) {
		if( bindings == null )
			bindings = new CharArrayObjectMap(1);
		char [] c = name.toCharArray();
		Object o = bindings.get( c );
		if( o != null ){
		    if( o instanceof ObjectSet ){
		    	((ObjectSet)o).put( name );
		        //bindings.put( c, ArrayUtil.append( Object.class, (Object[]) o, name ) );
		    } else {
		    	ObjectSet temp = new ObjectSet( 2 );
		    	temp.put( o );
		    	temp.put( name );
		        bindings.put( c, temp );
		    }
		} else {
		    bindings.put( c, name );
		}
	}

	public IBinding getBinding(IASTName name, boolean forceResolve) throws DOMException {
	    char [] c = name.toCharArray();
	    if( bindings == null )
	        return null;
	    
	    Object obj = bindings.get( c );
	    if( obj != null ){
	        if( obj instanceof ObjectSet ) {
	        	if( forceResolve )
	        		return CPPSemantics.resolveAmbiguities( name,  ((ObjectSet) obj).keyArray() );
	        	IBinding [] bs = null;
        		Object [] os = ((ObjectSet) obj).keyArray();
        		for( int i = 0; i < os.length; i++ ){
        			if( os[i] instanceof IASTName ){
        				IASTName n = (IASTName) os[i];
        				if( n instanceof ICPPASTQualifiedName ){
        					IASTName [] ns = ((ICPPASTQualifiedName)n).getNames();
        					n = ns[ ns.length - 1 ];
        				}
        				bs = (IBinding[]) ArrayUtil.append( IBinding.class, bs, ((CPPASTName)n).getBinding() );
        			} else
						bs = (IBinding[]) ArrayUtil.append( IBinding.class, bs, os[i] );
        		}	    
        		return CPPSemantics.resolveAmbiguities( name,  bs );
	        } else if( obj instanceof IASTName ){
	        	IBinding binding = null;
	        	if( forceResolve && obj != name )
	        		binding = ((IASTName)obj).resolveBinding();
	        	else {
	        		IASTName n = (IASTName) obj;
    				if( n instanceof ICPPASTQualifiedName ){
    					IASTName [] ns = ((ICPPASTQualifiedName)n).getNames();
    					n = ns[ ns.length - 1 ];
    				}
	        		binding = ((CPPASTName)n).getBinding();
	        	}
	        	if( binding instanceof ICPPCompositeBinding ){
	        		return CPPSemantics.resolveAmbiguities( name, ((ICPPCompositeBinding)binding).getBindings() );
	        	}
	        	return binding;
	        }
	        return (IBinding) obj;
	    }
		return null;
	}
	
	boolean isfull = false;
	public void setFullyCached( boolean full ){
		isfull = full;
	}
	
	public boolean isFullyCached(){
		return isfull;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public IBinding[] find(String name) throws DOMException {
	    char [] n = name.toCharArray();
	    if( isFullyCached() ){
	    	if( bindings != null ) {
		        Object o = bindings.get( n );
		        if( o instanceof IBinding[] )
		            return (IBinding[]) ArrayUtil.trim( IBinding.class, (Object[]) o );
	            return new IBinding[] { (IBinding) o };
	    	}
	    } else {
		    LookupData data = new LookupData( n );
			try {
		        data.foundItems = CPPSemantics.lookupInScope( data, this, null, null );
		    } catch ( DOMException e ) {
		    }
		    
		    if( data.foundItems != null ){
		        IASTName [] ns = (IASTName[]) data.foundItems;
		        ObjectSet set = new ObjectSet( ns.length );
		        for( int i = 0; i < ns.length && ns[i] != null; i++ ){
		            set.put( ns[i].resolveBinding() );
		        }
		        return (IBinding[]) ArrayUtil.trim( IBinding.class, set.keyArray(), true );
		    }
	    }
	    
		return new IBinding[0];
	}
}
