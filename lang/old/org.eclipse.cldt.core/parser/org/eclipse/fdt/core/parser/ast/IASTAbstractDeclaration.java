/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.core.parser.ast;

import java.util.Iterator;

import org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate;

/**
 * @author jcamelon
 *
 */
public interface IASTAbstractDeclaration extends IASTTypeSpecifierOwner, ISourceElementCallbackDelegate
{
	public boolean isConst();
	public boolean isVolatile();
	public Iterator getPointerOperators();
	public Iterator getArrayModifiers();
	public Iterator getParameters();
	public ASTPointerOperator getPointerToFunctionOperator();

}
