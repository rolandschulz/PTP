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
package org.eclipse.cldt.core.parser.ast;

import org.eclipse.cldt.core.parser.ISourceElementCallbackDelegate;

/**
 * @author jcamelon
 *
 */
public interface IASTEnumerator extends IASTOffsetableNamedElement, IASTNode, ISourceElementCallbackDelegate  {

	public IASTEnumerationSpecifier getOwnerEnumerationSpecifier(); 
	public IASTExpression getInitialValue();
	/**
	 * @param referenceManager
	 */
	public void freeReferences(); 
}