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
package org.eclipse.fdt.core.dom.ast;

/**
 * This is the root node in the physical AST. A physical node represents
 * a chunk of text in the source program.
 * 
 * @author Doug Schaefer
 */
public interface IASTNode {

    public IASTTranslationUnit getTranslationUnit();
    public IASTNodeLocation[] getNodeLocations();
    
	/**
	 * Get the parent node of this node in the tree.
	 * 
	 * @return the parent node of this node
	 */
	public IASTNode getParent();
	
	public void setParent( IASTNode node );

	/**
	 * In order to properly understand the relationship between this child
	 * node and it's parent, a node property object is used.
	 * 
	 * @return
	 */
	public ASTNodeProperty getPropertyInParent();
	
	public void setPropertyInParent( ASTNodeProperty property );
	

}
