/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.fdt.internal.core.dom.parser.cpp;

import org.eclipse.fdt.core.dom.ast.IASTName;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;

/**
 * @author jcamelon
 */
public class CPPASTNamespaceAlias extends CPPASTNode implements
        ICPPASTNamespaceAlias {

    private IASTName alias;
    private IASTName qualifiedName;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTNamespaceAlias#getAlias()
     */
    public IASTName getAlias() {
        return alias;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTNamespaceAlias#setAlias(org.eclipse.fdt.core.dom.ast.IASTName)
     */
    public void setAlias(IASTName name) {
        this.alias = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTNamespaceAlias#getQualifiedName()
     */
    public IASTName getQualifiedName() {
        return qualifiedName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTNamespaceAlias#setQualifiedName(org.eclipse.fdt.core.dom.ast.cpp.ICPPASTQualifiedName)
     */
    public void setQualifiedName(IASTName qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

}
