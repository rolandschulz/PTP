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
 * This interface represents a structural property in an IASTNode.
 * This is used to determine the relationship between a child node and
 * it's parent. This is especially important with rewrite since we need
 * to understand how to properly replace the child in the source.
 * 
 * @author Doug Schaefer
 */
public class ASTNodeProperty {

	private String name;

    /**
     * @param n
     */
    public ASTNodeProperty(String n) {
        this.name = n;
    }


    /**
	 * Each property has a name to help distinguish it from other
	 * properties of a node.
	 * 
	 * @return the name of the property
	 */
	public String getName()
	{
	    return name;
	}
	
	
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
}
