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
package org.eclipse.fdt.internal.core.parser.ast.quick;

import org.eclipse.fdt.core.parser.ast.IASTDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 *
 */
public interface IASTQScope extends IASTScope {

	public void addDeclaration( IASTDeclaration declaration );
}
