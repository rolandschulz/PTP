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

import org.eclipse.fdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.fdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;

/**
 * @author jcamelon
 */
public class CPPASTCompoundStatementExpression extends CPPASTNode implements
        IGNUASTCompoundStatementExpression {
    private IASTCompoundStatement statement;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.c.gcc.IGCCASTCompoundStatementExpression#getCompoundStatement()
     */
    public IASTCompoundStatement getCompoundStatement() {
        return statement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.c.gcc.IGCCASTCompoundStatementExpression#setCompoundStatement(org.eclipse.fdt.core.dom.ast.IASTCompoundStatement)
     */
    public void setCompoundStatement(IASTCompoundStatement statement) {
        this.statement = statement;
    }

}
