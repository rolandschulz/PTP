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

import org.eclipse.fdt.core.dom.ast.IASTExpression;
import org.eclipse.fdt.core.dom.ast.c.ICASTArrayDesignator;

/**
 * @author jcamelon
 */
public class CASTArrayDesignator extends CASTNode implements
        ICASTArrayDesignator {

    private IASTExpression exp;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.c.ICASTArrayDesignator#getSubscriptExpression()
     */
    public IASTExpression getSubscriptExpression() {
        return exp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.c.ICASTArrayDesignator#setSubscriptExpression(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setSubscriptExpression(IASTExpression value) {
        exp = value;
    }

}
