/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.fdt.internal.core.dom.parser.c;

import org.eclipse.fdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTDeclarator;
import org.eclipse.fdt.core.dom.ast.IASTSimpleDeclaration;

/**
 * @author jcamelon
 */
public class CASTSimpleDeclaration extends CASTNode implements
        IASTSimpleDeclaration {

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSimpleDeclaration#getDeclSpecifier()
     */
    public IASTDeclSpecifier getDeclSpecifier() {
        return declSpecifier;
    }


    
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSimpleDeclaration#getDeclarators()
     */
    public IASTDeclarator[] getDeclarators() {
        if( declarators == null ) return IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
        removeNullDeclarators();
        return declarators;
    }
    
    public void addDeclarator( IASTDeclarator d )
    {
        if( declarators == null )
        {
            declarators = new IASTDeclarator[ DEFAULT_DECLARATORS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( declarators.length == currentIndex )
        {
            IASTDeclarator [] old = declarators;
            declarators = new IASTDeclarator[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                declarators[i] = old[i];
        }
        declarators[ currentIndex++ ] = d;
    }
    
    private void removeNullDeclarators() {
        int nullCount = 0; 
        for( int i = 0; i < declarators.length; ++i )
            if( declarators[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTDeclarator [] old = declarators;
        int newSize = old.length - nullCount;
        declarators = new IASTDeclarator[ newSize ];
        for( int i = 0; i < newSize; ++i )
            declarators[i] = old[i];
        currentIndex = newSize;
    }

    private int currentIndex = 0;    
    private IASTDeclarator [] declarators = null;
    private static final int DEFAULT_DECLARATORS_LIST_SIZE = 2;
    private IASTDeclSpecifier declSpecifier;

    /**
     * @param declSpecifier The declSpecifier to set.
     */
    public void setDeclSpecifier(IASTDeclSpecifier declSpecifier) {
        this.declSpecifier = declSpecifier;
    }
}
