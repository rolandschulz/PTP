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
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTPointerToMember;

/**
 * @author jcamelon
 */
public class CPPASTPointerToMember extends CPPASTPointer implements
        ICPPASTPointerToMember {

    private IASTName n;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTPointerToMember#setName(org.eclipse.fdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        n = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTPointerToMember#getName()
     */
    public IASTName getName() {
        return n;
    }

}
