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

import org.eclipse.fdt.core.dom.ast.IASTTypeId;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTCastExpression;

/**
 * @author jcamelon
 */
public class CPPASTCastExpression extends CPPASTUnaryExpression implements
        ICPPASTCastExpression {
    private IASTTypeId typeId;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTUnaryTypeIdExpression#setTypeId(org.eclipse.fdt.core.dom.ast.IASTTypeId)
     */
    public void setTypeId(IASTTypeId typeId) {
        this.typeId = typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTUnaryTypeIdExpression#getTypeId()
     */
    public IASTTypeId getTypeId() {
        return typeId;
    }

}
