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
 * Created on Jan 26, 2005
 */
package org.eclipse.fdt.internal.core.dom.parser.c;

import org.eclipse.fdt.core.dom.ast.IASTName;
import org.eclipse.fdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.fdt.core.dom.ast.IScope;
import org.eclipse.fdt.core.dom.ast.IType;
import org.eclipse.fdt.core.dom.ast.IVariable;
import org.eclipse.fdt.core.dom.ast.c.ICExternalBinding;

/**
 * @author aniefer
 */
public class CExternalVariable implements ICExternalBinding, IVariable {
    private IASTTranslationUnit tu;
    private IASTName name;
    /**
     * @param name
     */
    public CExternalVariable( IASTTranslationUnit tu, IASTName name ) {
        this.name = name;
        this.tu = tu;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        return name.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return name.toCharArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return tu.getScope();
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IVariable#getType()
     */
    public IType getType() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IVariable#isStatic()
     */
    public boolean isStatic() {
        return false;
    }
}
