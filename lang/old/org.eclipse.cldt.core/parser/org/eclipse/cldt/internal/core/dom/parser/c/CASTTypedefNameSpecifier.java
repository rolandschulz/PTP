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
import org.eclipse.cldt.core.dom.ast.c.ICASTTypedefNameSpecifier;

/**
 * @author jcamelon
 */
public class CASTTypedefNameSpecifier extends CASTBaseDeclSpecifier implements
        ICASTTypedefNameSpecifier {

    private IASTName name;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypedefNameSpecifier#getName()
     */
    public IASTName getName() {
        return name;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypedefNameSpecifier#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

}