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

import org.eclipse.fdt.core.dom.ast.IASTDeclaration;
import org.eclipse.fdt.core.dom.ast.IASTExpression;
import org.eclipse.fdt.core.dom.ast.IASTForStatement;
import org.eclipse.fdt.core.dom.ast.IASTStatement;
import org.eclipse.fdt.core.dom.ast.IScope;

/**
 * @author jcamelon
 */
public class CASTForStatement extends CASTNode implements IASTForStatement {
    private IScope scope = null;
    
    private IASTExpression initialExpression;
    private IASTDeclaration initDeclaration;
    private IASTExpression condition;
    private IASTExpression iterationExpression;
    private IASTStatement body;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTForStatement#getInitExpression()
     */
    public IASTExpression getInitExpression() {
        return initialExpression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTForStatement#setInit(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setInit(IASTExpression expression) {
        this.initialExpression = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTForStatement#getInitDeclaration()
     */
    public IASTDeclaration getInitDeclaration() {
        return initDeclaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTForStatement#setInit(org.eclipse.fdt.core.dom.ast.IASTDeclaration)
     */
    public void setInit(IASTDeclaration declaration) {
        this.initDeclaration = declaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTForStatement#getCondition()
     */
    public IASTExpression getCondition() {
        return condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTForStatement#setCondition(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setCondition(IASTExpression condition) {
        this.condition = condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTForStatement#getIterationExpression()
     */
    public IASTExpression getIterationExpression() {
        return iterationExpression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTForStatement#setIterationExpression(org.eclipse.fdt.core.dom.ast.IASTExpression)
     */
    public void setIterationExpression(IASTExpression iterator) {
        this.iterationExpression = iterator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTForStatement#getBody()
     */
    public IASTStatement getBody() {
        return body;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTForStatement#setBody(org.eclipse.fdt.core.dom.ast.IASTStatement)
     */
    public void setBody(IASTStatement statement) {
        body = statement;

    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTForStatement#getScope()
     */
    public IScope getScope() {
        if( scope == null )
            scope = new CScope( this );
        return scope;
    }

}
