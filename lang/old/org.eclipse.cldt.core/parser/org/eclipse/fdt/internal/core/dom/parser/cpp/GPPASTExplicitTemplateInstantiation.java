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

import org.eclipse.fdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation;

/**
 * @author jcamelon
 */
public class GPPASTExplicitTemplateInstantiation extends
        CPPASTExplicitTemplateInstantiation implements
        IGPPASTExplicitTemplateInstantiation {

    private int mod;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation#getModifier()
     */
    public int getModifier() {
        return mod;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation#setModifier(int)
     */
    public void setModifier(int value) {
        this.mod = value;
    }

}
