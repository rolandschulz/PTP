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

import org.eclipse.fdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;

/**
 * @author jcamelon
 */
public class GPPASTPointerToMember extends CPPASTPointerToMember implements
        IGPPASTPointerToMember {

    private boolean isRestrict;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.gnu.cpp.IGPPASTPointer#isRestrict()
     */
    public boolean isRestrict() {
        return isRestrict;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.gnu.cpp.IGPPASTPointer#setRestrict(boolean)
     */
    public void setRestrict(boolean value) {
        isRestrict = value;
    }

}
