/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cldt.internal.core.dom.parser.c;

import org.eclipse.cldt.core.dom.ast.IASTName;
import org.eclipse.cldt.core.dom.ast.IBinding;

/**
 * @author jcamelon
 */
public class CASTName extends CASTNode implements IASTName {

    private final char[] name;
    private static final char[] EMPTY_CHAR_ARRAY = { };
    private IBinding binding = null;

    /**
     * @param name 
     */
    public CASTName(char [] name ) {
        this.name = name;
    }

    /**
     * 
     */
    public CASTName() {
        name = EMPTY_CHAR_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTName#resolveBinding()
     */
    public IBinding resolveBinding() {
    	if( binding == null )
    		CVisitor.createBinding( this ); 
    	
        return binding;
    }
    
	public IBinding[] resolvePrefix() {
		// TODO hook this up to the CVisitor
		return null;
	}
	
    protected boolean hasBinding(){
        return ( binding != null );
    }
    
    protected void setBinding( IBinding binding ){
    	this.binding = binding;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if( name == EMPTY_CHAR_ARRAY ) return null;
        return new String( name );
    }
    
    public char[] toCharArray() {
    	return name;
    }
}
