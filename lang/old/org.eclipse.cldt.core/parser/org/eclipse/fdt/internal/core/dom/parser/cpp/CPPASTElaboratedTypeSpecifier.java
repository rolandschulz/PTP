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
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;

/**
 * @author jcamelon
 */
public class CPPASTElaboratedTypeSpecifier extends CPPASTBaseDeclSpecifier
        implements ICPPASTElaboratedTypeSpecifier {

    private int kind;
    private IASTName name;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTElaboratedTypeSpecifier#getKind()
     */
    public int getKind() {
        return kind;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTElaboratedTypeSpecifier#setKind(int)
     */
    public void setKind(int value) {
        this.kind = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTElaboratedTypeSpecifier#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTElaboratedTypeSpecifier#setName(org.eclipse.fdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

}
