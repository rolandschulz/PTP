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
package org.eclipse.cldt.core.dom.ast.cpp;

import org.eclipse.cldt.core.dom.ast.IASTTypeIdExpression;

/**
 * @author jcamelon
 */
public interface ICPPASTTypeIdExpression extends IASTTypeIdExpression {

    public static final int op_typeid = IASTTypeIdExpression.op_last + 1;
    public static final int op_last = op_typeid;
}
