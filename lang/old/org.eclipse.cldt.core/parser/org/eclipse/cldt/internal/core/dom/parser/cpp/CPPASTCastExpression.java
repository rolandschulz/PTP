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
package org.eclipse.cldt.internal.core.dom.parser.cpp;

import org.eclipse.cldt.core.dom.ast.IASTTypeId;
import org.eclipse.cldt.core.dom.ast.cpp.ICPPASTCastExpression;

/**
 * @author jcamelon
 */
public class CPPASTCastExpression extends CPPASTUnaryExpression implements
        ICPPASTCastExpression {
    private IASTTypeId typeId;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTUnaryTypeIdExpression#setTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
     */
    public void setTypeId(IASTTypeId typeId) {
        this.typeId = typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTUnaryTypeIdExpression#getTypeId()
     */
    public IASTTypeId getTypeId() {
        return typeId;
    }

}
