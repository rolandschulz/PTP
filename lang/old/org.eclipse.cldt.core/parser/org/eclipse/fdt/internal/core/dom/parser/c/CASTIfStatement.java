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
import org.eclipse.fdt.core.dom.ast.IASTIfStatement;
import org.eclipse.fdt.core.dom.ast.IASTStatement;

/**
 * @author jcamelon
 */
public class CASTIfStatement extends CASTNode implements IASTIfStatement {

    private IASTExpression condition;
    private IASTStatement thenClause;
    private IASTStatement elseClause;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTIfStatement#getCondition()
     */
    public IASTExpression getCondition() {
        return condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTIfStatement#setCondition(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setCondition(IASTExpression condition) {
        this.condition = condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTIfStatement#getThenClause()
     */
    public IASTStatement getThenClause() {
        return thenClause;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTIfStatement#setThenClause(org.eclipse.fdt.core.dom.ast.IASTStatement)
     */
    public void setThenClause(IASTStatement thenClause) {
        this.thenClause = thenClause;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTIfStatement#getElseClause()
     */
    public IASTStatement getElseClause() {
        return elseClause;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTIfStatement#setElseClause(org.eclipse.fdt.core.dom.ast.IASTStatement)
     */
    public void setElseClause(IASTStatement elseClause) {
        this.elseClause = elseClause;
    }

}
