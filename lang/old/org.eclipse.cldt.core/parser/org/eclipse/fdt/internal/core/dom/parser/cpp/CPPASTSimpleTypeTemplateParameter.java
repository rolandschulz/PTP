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
import org.eclipse.fdt.core.dom.ast.IASTTypeId;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;

/**
 * @author jcamelon
 */
public class CPPASTSimpleTypeTemplateParameter extends CPPASTNode implements
        ICPPASTSimpleTypeTemplateParameter {

    private int type;
    private IASTName name;
    private IASTTypeId typeId;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter#getParameterType()
     */
    public int getParameterType() {
        return type;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter#setParameterType(int)
     */
    public void setParameterType(int value) {
        this.type = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter#setName(org.eclipse.fdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter#getDefaultType()
     */
    public IASTTypeId getDefaultType() {
        return typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter#setDefaultType(org.eclipse.fdt.core.dom.ast.IASTTypeId)
     */
    public void setDefaultType(IASTTypeId typeId) {
        this.typeId = typeId;
    }
}
