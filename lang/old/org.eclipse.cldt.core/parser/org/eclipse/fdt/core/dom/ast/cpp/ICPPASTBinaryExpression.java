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
package org.eclipse.fdt.core.dom.ast.cpp;

import org.eclipse.fdt.core.dom.ast.IASTBinaryExpression;

/**
 * @author jcamelon
 */
public interface ICPPASTBinaryExpression extends IASTBinaryExpression {

    public static final int op_pmdot = IASTBinaryExpression.op_last + 1;
    public static final int op_pmarrow = IASTBinaryExpression.op_last + 2;
    public static final int op_last = op_pmarrow;
}
