/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.fdt.core.dom.ast.gnu.c;

import org.eclipse.fdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.fdt.core.dom.ast.IASTExpression;
import org.eclipse.fdt.core.dom.ast.c.ICASTDesignator;

/**
 * @author jcamelon
 */
public interface IGCCASTArrayRangeDesignator extends ICASTDesignator {

    public static final ASTNodeProperty SUBSCRIPT_FLOOR_EXPRESSION = new ASTNodeProperty( "Subscript Floor Expression"); //$NON-NLS-1$
    public static final ASTNodeProperty SUBSCRIPT_CEILING_EXPRESSION = new ASTNodeProperty( "Subscript Ceiling Expression"); //$NON-NLS-1$
    
    public IASTExpression getRangeFloor();
    public void setRangeFloor( IASTExpression expression );
    public IASTExpression getRangeCeiling();
    public void setRangeCeiling( IASTExpression expression );
}
