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
import org.eclipse.fdt.core.dom.ast.IASTTypeIdExpression;

/**
 * @author jcamelon
 */
public class CPPASTTypeIdExpression extends CPPASTNode implements
        IASTTypeIdExpression {

    private int op;
    private IASTTypeId typeId;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTTypeIdExpression#getOperator()
     */
    public int getOperator() {
        return op;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTTypeIdExpression#setOperator(int)
     */
    public void setOperator(int value) {
        this.op = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTTypeIdExpression#setTypeId(org.eclipse.fdt.core.dom.ast.IASTTypeId)
     */
    public void setTypeId(IASTTypeId typeId) {
       this.typeId = typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTTypeIdExpression#getTypeId()
     */
    public IASTTypeId getTypeId() {
        return typeId;
    }

}
