
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

import org.eclipse.cldt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cldt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cldt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cldt.core.dom.ast.IASTNode;
import org.eclipse.cldt.core.dom.ast.IASTStatement;
import org.eclipse.cldt.core.dom.ast.IBinding;
import org.eclipse.cldt.core.dom.ast.ILabel;
import org.eclipse.cldt.core.dom.ast.IScope;
import org.eclipse.cldt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cldt.core.dom.ast.c.ICScope;
import org.eclipse.cldt.core.dom.ast.c.ICASTVisitor.CBaseVisitorAction;
import org.eclipse.cldt.core.parser.util.ArrayUtil;
import org.eclipse.cldt.core.parser.util.CharArrayObjectMap;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class CFunctionScope implements ICFunctionScope {
	private final IASTFunctionDefinition function;
	private CharArrayObjectMap bindings = CharArrayObjectMap.EMPTY_MAP;
	
	public CFunctionScope( IASTFunctionDefinition function ){
		this.function = function;
	}
	
	public void addBinding( IBinding binding ) {
	    //only labels have function scope 
	    if( !(binding instanceof ILabel) )
	        return;
	    if( bindings == CharArrayObjectMap.EMPTY_MAP )
	        bindings = new CharArrayObjectMap(1);
	    bindings.put( binding.getNameCharArray(), binding );
    }
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICScope#getBinding(int, char[])
     */
    public IBinding getBinding( int namespaceType, char[] name ) {
        if( namespaceType == ICScope.NAMESPACE_TYPE_OTHER )
            return getBinding( name );
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICFunctionScope#getBinding(char[])
     */
    public IBinding getBinding( char[] name ) {
        return (IBinding) bindings.get( name );
    }

    
	public IScope getBodyScope(){
	    IASTStatement statement = function.getBody();
	    if( statement instanceof IASTCompoundStatement ){
	        return ((IASTCompoundStatement)statement).getScope();
	    }
	    return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
	 */
	public IScope getParent() {
		return function.getScope();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	public IBinding[] find(String name) {
		return null;
	}

	public ILabel[] getLabels(){
	    FindLabelsAction action = new FindLabelsAction();
	    
        function.getTranslationUnit().getVisitor().visitDeclaration( function, action );
	    
	    ILabel [] result = null;
	    if( action.labels != null ){
		    for( int i = 0; i < action.labels.length && action.labels[i] != null; i++ ){
		        IASTLabelStatement labelStatement = action.labels[i];
		        IBinding binding = labelStatement.getName().resolveBinding();
		        if( binding != null )
		            result = (ILabel[]) ArrayUtil.append( ILabel.class, result, binding );
		    }
	    }
	    return (ILabel[]) ArrayUtil.trim( ILabel.class, result );
	}
	
	static private class FindLabelsAction extends CBaseVisitorAction {
        public IASTLabelStatement [] labels = null;
        
        public FindLabelsAction(){
            processStatements = true;
        }
        
        public int processStatement( IASTStatement statement ) {
            if( statement instanceof IASTLabelStatement ){
               labels = (IASTLabelStatement[]) ArrayUtil.append( IASTLabelStatement.class, labels, statement );
            }
            return PROCESS_CONTINUE;
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.c.ICScope#removeBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public void removeBinding(IBinding binding) {
		if( bindings != CharArrayObjectMap.EMPTY_MAP ) {
			bindings.remove( binding.getNameCharArray(), 0, binding.getNameCharArray().length);
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getPhysicalNode()
     */
    public IASTNode getPhysicalNode() {
        return function;
    }
}
