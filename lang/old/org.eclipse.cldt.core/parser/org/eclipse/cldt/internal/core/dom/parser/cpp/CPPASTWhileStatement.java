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

import org.eclipse.cldt.core.dom.ast.IASTDeclaration;
import org.eclipse.cldt.core.dom.ast.IASTExpression;
import org.eclipse.cldt.core.dom.ast.IASTStatement;
import org.eclipse.cldt.core.dom.ast.cpp.ICPPASTWhileStatement;

/**
 * @author jcamelon
 */
public class CPPASTWhileStatement extends CPPASTNode implements
        ICPPASTWhileStatement {
    private IASTExpression condition;
    private IASTStatement body;
    private IASTDeclaration condition2;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTWhileStatement#getCondition()
     */
    public IASTExpression getCondition() {
        return condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTWhileStatement#setCondition(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setCondition(IASTExpression condition) {
        this.condition = condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTWhileStatement#getBody()
     */
    public IASTStatement getBody() {
        return body;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTWhileStatement#setBody(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setBody(IASTStatement body) {
        this.body = body;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement#getInitDeclaration()
	 */
	public IASTDeclaration getConditionDeclaration() {
		return condition2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement#setInit(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
	 */
	public void setConditionDeclaration(IASTDeclaration declaration) {
		condition2 = declaration;
	}

}
