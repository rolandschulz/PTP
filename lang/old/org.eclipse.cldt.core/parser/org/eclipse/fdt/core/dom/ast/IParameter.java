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
 * Represents a parameter to a function. The scope of the parameter is
 * the function that declared this parameter.
 * 
 * @author Doug Schaefer
 */
public interface IParameter extends IVariable {
	public static final IParameter [] EMPTY_PARAMETER_ARRAY = new IParameter[0];
}
