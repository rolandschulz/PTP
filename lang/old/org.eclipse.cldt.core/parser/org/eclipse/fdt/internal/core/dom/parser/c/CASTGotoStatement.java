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

import org.eclipse.fdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.fdt.core.dom.ast.IASTName;

/**
 * @author jcamelon
 */
public class CASTGotoStatement extends CASTNode implements IASTGotoStatement {

    private IASTName name;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTGotoStatement#getName()
     */
    public IASTName getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTGotoStatement#setName(org.eclipse.fdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
       this.name = name;
    }

}
