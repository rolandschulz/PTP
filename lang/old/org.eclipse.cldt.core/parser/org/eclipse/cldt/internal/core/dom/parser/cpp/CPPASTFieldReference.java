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

import org.eclipse.cldt.core.dom.ast.IASTExpression;
import org.eclipse.cldt.core.dom.ast.IASTName;
import org.eclipse.cldt.core.dom.ast.cpp.ICPPASTFieldReference;

/**
 * @author jcamelon
 */
public class CPPASTFieldReference extends CPPASTNode implements
        ICPPASTFieldReference {

    private boolean isTemplate;
    private IASTExpression owner;
    private IASTName name;
    private boolean isDeref;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference#isTemplate()
     */
    public boolean isTemplate() {
        return isTemplate;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference#setIsTemplate(boolean)
     */
    public void setIsTemplate(boolean value) {
        isTemplate = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFieldReference#getFieldOwner()
     */
    public IASTExpression getFieldOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFieldReference#setFieldOwner(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setFieldOwner(IASTExpression expression) {
        owner = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFieldReference#getFieldName()
     */
    public IASTName getFieldName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFieldReference#setFieldName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setFieldName(IASTName name) {
        this.name =name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFieldReference#isPointerDereference()
     */
    public boolean isPointerDereference() {
        return isDeref;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFieldReference#setIsPointerDereference(boolean)
     */
    public void setIsPointerDereference(boolean value) {
        isDeref = value;
    }

}
