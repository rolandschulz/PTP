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

import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;

/**
 * @author jcamelon
 */
public class CPPASTVisibilityLabel extends CPPASTNode implements
        ICPPASTVisiblityLabel {

    private int visibility;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTVisiblityLabel#getVisibility()
     */
    public int getVisibility() {
        return visibility;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTVisiblityLabel#setVisibility(int)
     */
    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

}
