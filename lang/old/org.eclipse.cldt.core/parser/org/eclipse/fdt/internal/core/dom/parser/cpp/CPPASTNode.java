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

import org.eclipse.fdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.fdt.core.dom.ast.IASTNode;
import org.eclipse.fdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.fdt.internal.core.dom.parser.ASTNode;

/**
 * @author jcamelon
 */
public class CPPASTNode extends ASTNode implements IASTNode {

    private IASTNode parent;
    private ASTNodeProperty property;

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTNode#getTranslationUnit()
     */
    public IASTTranslationUnit getTranslationUnit() {
        if( this instanceof IASTTranslationUnit ) return (IASTTranslationUnit) this;
        IASTNode node = getParent();
        while( ! (node instanceof IASTTranslationUnit ))
        {
            node = node.getParent();
        }
        return (IASTTranslationUnit) node;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTNode#getParent()
     */
    public IASTNode getParent() {
        return parent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTNode#setParent(org.eclipse.fdt.core.dom.ast.IASTNode)
     */
    public void setParent(IASTNode node) {
        this.parent = node;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTNode#getPropertyInParent()
     */
    public ASTNodeProperty getPropertyInParent() {
        return property;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.dom.ast.IASTNode#setPropertyInParent(org.eclipse.fdt.core.dom.ast.ASTNodeProperty)
     */
    public void setPropertyInParent(ASTNodeProperty property) {
        this.property = property;
    }

}
