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
package org.eclipse.fdt.internal.core.parser.ast.complete;

import org.eclipse.fdt.core.parser.ITokenDuple;
import org.eclipse.fdt.internal.core.parser.pst.IContainerSymbol;

/**
 * @author jcamelon
 */
public class UnresolvedReferenceDuple {

	public UnresolvedReferenceDuple( IContainerSymbol scope, ITokenDuple name ){
		this.scope = scope;
		this.name = name;
	}
	
	private final IContainerSymbol scope;
	private final ITokenDuple name;

	public IContainerSymbol getScope()
	{
		return scope;
	}
	
	public ITokenDuple      getName()
	{
		return name;
	}
}
