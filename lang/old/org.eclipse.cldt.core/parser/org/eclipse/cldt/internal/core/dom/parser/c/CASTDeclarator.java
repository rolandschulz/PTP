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

import org.eclipse.cldt.core.dom.ast.IASTDeclarator;
import org.eclipse.cldt.core.dom.ast.IASTInitializer;
import org.eclipse.cldt.core.dom.ast.IASTName;
import org.eclipse.cldt.core.dom.ast.IASTPointerOperator;

/**
 * @author jcamelon
 */
public class CASTDeclarator extends CASTNode implements IASTDeclarator {

    private IASTInitializer initializer;
    private IASTName name;
    private IASTDeclarator nestedDeclarator;
    private IASTPointerOperator [] pointerOps = null;
    private static final int DEFAULT_PTROPS_LIST_SIZE = 2;
    private int currentIndex;

    private void removeNullPointers() {
        int nullCount = 0; 
        for( int i = 0; i < pointerOps.length; ++i )
            if( pointerOps[i] == null )
                ++nullCount;
        if( nullCount == 0 ) return;
        IASTPointerOperator [] old = pointerOps;
        int newSize = old.length - nullCount;
        pointerOps = new IASTPointerOperator[ newSize ];
        for( int i = 0; i < newSize; ++i )
            pointerOps[i] = old[i];
        currentIndex = newSize;
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#getPointerOperators()
     */
    public IASTPointerOperator[] getPointerOperators() {
        if( pointerOps == null ) return IASTPointerOperator.EMPTY_ARRAY;
        removeNullPointers();
        return pointerOps;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#getNestedDeclarator()
     */
    public IASTDeclarator getNestedDeclarator() {
        return nestedDeclarator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#getInitializer()
     */
    public IASTInitializer getInitializer() {
        return initializer;
    }

    /**
     * @param initializer
     */
    public void setInitializer(IASTInitializer initializer) {
        this.initializer = initializer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#addPointerOperator(org.eclipse.cdt.core.dom.ast.IASTPointerOperator)
     */
    public void addPointerOperator(IASTPointerOperator operator) {
        if( pointerOps == null )
        {
            pointerOps = new IASTPointerOperator[ DEFAULT_PTROPS_LIST_SIZE ];
            currentIndex = 0;
        }
        if( pointerOps.length == currentIndex )
        {
            IASTPointerOperator [] old = pointerOps;
            pointerOps = new IASTPointerOperator[ old.length * 2 ];
            for( int i = 0; i < old.length; ++i )
                pointerOps[i] = old[i];
        }
        pointerOps[ currentIndex++ ] = operator;    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#setNestedDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
     */
    public void setNestedDeclarator(IASTDeclarator nested) {
        this.nestedDeclarator = nested;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

}
