/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.fdt.internal.core.dom.parser.c;

import org.eclipse.fdt.core.dom.ast.IASTCastExpression;
import org.eclipse.fdt.core.dom.ast.IASTTypeId;

/**
 * @author jcamelon
 */
public class CASTCastExpression extends CASTUnaryExpression implements
        IASTCastExpression {

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
