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
import org.eclipse.fdt.core.dom.ast.IASTStatement;
import org.eclipse.fdt.core.dom.ast.IASTSwitchStatement;

/**
 * @author jcamelon
 */
public class CASTSwitchStatement extends CASTNode implements
        IASTSwitchStatement {

    private IASTExpression controller;
    private IASTStatement body;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSwitchStatement#getController()
     */
    public IASTExpression getController() {
        return controller;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSwitchStatement#setController(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setController(IASTExpression controller) {
        this.controller = controller;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSwitchStatement#getBody()
     */
    public IASTStatement getBody() {
        return body;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSwitchStatement#setBody(org.eclipse.fdt.core.dom.ast.IASTStatement)
     */
    public void setBody(IASTStatement body) {
        this.body = body;
    }

}
