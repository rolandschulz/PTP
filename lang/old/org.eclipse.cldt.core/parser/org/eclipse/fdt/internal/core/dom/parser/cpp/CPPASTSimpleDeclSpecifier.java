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

import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;

/**
 * @author jcamelon
 */
public class CPPASTSimpleDeclSpecifier extends CPPASTBaseDeclSpecifier
        implements ICPPASTSimpleDeclSpecifier {

    private int type;
    private boolean isSigned;
    private boolean isUnsigned;
    private boolean isShort;
    private boolean isLong;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSimpleDeclSpecifier#getType()
     */
    public int getType() {
        return type;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSimpleDeclSpecifier#setType(int)
     */
    public void setType(int type) {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSimpleDeclSpecifier#isSigned()
     */
    public boolean isSigned() {
        return isSigned;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSimpleDeclSpecifier#isUnsigned()
     */
    public boolean isUnsigned() {
        return isUnsigned;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSimpleDeclSpecifier#isShort()
     */
    public boolean isShort() {
        return isShort;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSimpleDeclSpecifier#isLong()
     */
    public boolean isLong() {
        return isLong;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSimpleDeclSpecifier#setSigned(boolean)
     */
    public void setSigned(boolean value) {
        isSigned = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSimpleDeclSpecifier#setUnsigned(boolean)
     */
    public void setUnsigned(boolean value) {
        isUnsigned = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSimpleDeclSpecifier#setLong(boolean)
     */
    public void setLong(boolean value) {
        isLong = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTSimpleDeclSpecifier#setShort(boolean)
     */
    public void setShort(boolean value) {
        isShort = value;
    }

}
