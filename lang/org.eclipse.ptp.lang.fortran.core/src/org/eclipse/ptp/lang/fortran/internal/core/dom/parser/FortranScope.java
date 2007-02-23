/*******************************************************************************
 * Copyright (c) 2005, 2006 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.ptp.lang.fortran.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;

/**
 * @author aniefer
 */
public class FortranScope implements ICScope, IASTInternalScope {
	/**
	 * ISO C:99 6.2.3 there are seperate namespaces for various categories of
	 * identifiers: - label names ( labels have ICFunctionScope ) - tags of
	 * structures or unions : NAMESPACE_TYPE_TAG - members of structures or
	 * unions ( members have ICCompositeTypeScope ) - all other identifiers :
	 * NAMESPACE_TYPE_OTHER
	 */
	public static final int NAMESPACE_TYPE_TAG = 0;
	public static final int NAMESPACE_TYPE_OTHER = 1;
	
    private IASTNode physicalNode = null;
    private boolean isFullyCached = false;
    
    private CharArrayObjectMap [] bindings = { CharArrayObjectMap.EMPTY_MAP, CharArrayObjectMap.EMPTY_MAP };
    
    public FortranScope( IASTNode physical ){
        physicalNode = physical;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
     */
    public IScope getParent() {
        return FortranVisitor.getContainingScope( physicalNode );
    }

    protected static class CollectNamesAction extends CASTVisitor {
        private char [] name;
        private IASTName [] result = null;
        CollectNamesAction( char [] n ){
            name = n;
            shouldVisitNames = true;
        }
        public int visit( IASTName n ){
            ASTNodeProperty prop = n.getPropertyInParent();
            if( prop == IASTElaboratedTypeSpecifier.TYPE_NAME ||
                prop == IASTCompositeTypeSpecifier.TYPE_NAME ||
                prop == IASTDeclarator.DECLARATOR_NAME )
            {
                if( CharArrayUtils.equals( n.toCharArray(), name ) )
                    result = (IASTName[]) ArrayUtil.append( IASTName.class, result, n );    
            }
            
            return PROCESS_CONTINUE; 
        }
        public int visit( IASTStatement statement ){
            if( statement instanceof IASTDeclarationStatement )
                return PROCESS_CONTINUE;
            return PROCESS_SKIP;
        }
        public IASTName [] getNames(){
            return (IASTName[]) ArrayUtil.trim( IASTName.class, result );
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    public IBinding[] find( String name ) throws DOMException {
    	return FortranVisitor.findBindings( this, name, false );
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    public IBinding[] find( String name, boolean prefixLookup ) throws DOMException {
    	return FortranVisitor.findBindings( this, name, prefixLookup );
    }

    public IBinding getBinding( int namespaceType, char [] name ){
        IASTName n = (IASTName) bindings[namespaceType].get( name );
        return ( n != null ) ? n.resolveBinding() : null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICScope#removeBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public void removeBinding(IBinding binding) {
        int type = ( binding instanceof ICompositeType || binding instanceof IEnumeration ) ? 
				NAMESPACE_TYPE_TAG : NAMESPACE_TYPE_OTHER;

		if( bindings[type] != CharArrayObjectMap.EMPTY_MAP ) {
			bindings[type].remove( binding.getNameCharArray(), 0, binding.getNameCharArray().length);
		}
		isFullyCached = false;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getPhysicalNode()
     */
    public IASTNode getPhysicalNode() {
        return physicalNode;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#addName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void addName( IASTName name ) {
        int type = getNamespaceType( name );
        if( bindings[type] == CharArrayObjectMap.EMPTY_MAP )
            bindings[type] = new CharArrayObjectMap(1);
        
        char [] n = name.toCharArray();
        IASTName current = (IASTName) bindings[type].get( n );
        if( current == null || ((FortranASTName)current).getOffset() > ((FortranASTName) name).getOffset() ){
            bindings[type].put( n, name );
        }
    }

    private int getNamespaceType( IASTName name ){
        ASTNodeProperty prop = name.getPropertyInParent();
        if( prop == IASTCompositeTypeSpecifier.TYPE_NAME ||
            prop == IASTElaboratedTypeSpecifier.TYPE_NAME ||
            prop == IASTEnumerationSpecifier.ENUMERATION_NAME ||
            prop == FortranVisitor.STRING_LOOKUP_TAGS_PROPERTY )
        {
            return NAMESPACE_TYPE_TAG;
        }
        
        return NAMESPACE_TYPE_OTHER;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#getBinding(org.eclipse.cdt.core.dom.ast.IASTName, boolean)
     */
    public IBinding getBinding( IASTName name, boolean resolve ) {
        char [] c = name.toCharArray();
        if( c.length == 0  ){
            return null;
        }
        
        int type = getNamespaceType( name );
        Object o = bindings[type].get( name.toCharArray() );
        
        if( o == null ) 
            return null;
        
        if( o instanceof IBinding )
            return (IBinding) o;

        if( (resolve || ((IASTName)o).getBinding() != null) && ( o != name ) )
            return ((IASTName)o).resolveBinding();

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#setFullyCached(boolean)
     */
    public void setFullyCached( boolean b ){
        isFullyCached = b;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#isFullyCached()
     */
    public boolean isFullyCached(){
        return isFullyCached;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getScopeName()
	 */
	public IName getScopeName() {
		if( physicalNode instanceof IASTCompositeTypeSpecifier ){
			return ((IASTCompositeTypeSpecifier) physicalNode).getName();
		}
		return null;
	}

	public void flushCache() {
		bindings[0].clear();
		bindings[1].clear();
		isFullyCached = false;
	}

	public void addBinding(IBinding binding) {
		int type = NAMESPACE_TYPE_OTHER;
        if (binding instanceof ICompositeType || binding instanceof IEnumeration) {
            type = NAMESPACE_TYPE_TAG;
        }
            
        if( bindings[type] == CharArrayObjectMap.EMPTY_MAP )
           bindings[type] = new CharArrayObjectMap(2);
        
		bindings[type].put(binding.getNameCharArray(), binding);
	}
}
