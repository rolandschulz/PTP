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
import org.eclipse.cldt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;

/**
 * @author jcamelon
 */
public class GPPASTSimpleDeclSpecifier extends CPPASTSimpleDeclSpecifier
        implements IGPPASTSimpleDeclSpecifier {

    private boolean longLong;
    private boolean restrict;
    private IASTExpression typeOfExpression;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier#isLongLong()
     */
    public boolean isLongLong() {
        return longLong;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier#setLongLong(boolean)
     */
    public void setLongLong(boolean value) {
        longLong = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTDeclSpecifier#isRestrict()
     */
    public boolean isRestrict() {
        return restrict;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTDeclSpecifier#setRestrict(boolean)
     */
    public void setRestrict(boolean value) {
        restrict = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier#setTypeofExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setTypeofExpression(IASTExpression typeofExpression) {
        typeOfExpression = typeofExpression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier#getTypeofExpression()
     */
    public IASTExpression getTypeofExpression() {
        return typeOfExpression;
    }

}
