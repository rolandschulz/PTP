/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.internal.core.parser.ast.complete;

import org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.fdt.core.parser.ISourceElementRequestor;
import org.eclipse.fdt.core.parser.ast.IASTReference;
import org.eclipse.fdt.core.parser.ast.IASTVariable;
import org.eclipse.fdt.core.parser.ast.IASTVariableReference;


public class ASTVariableReference extends ASTReference
		implements
			IASTReference,
			IASTVariableReference {

	private IASTVariable referencedElement;
	/**
	 * @param offset
	 * @param variable
	 */
	public ASTVariableReference(int offset, IASTVariable variable) {
		super(offset);
		referencedElement = variable;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int)
	 */
	public void initialize(int o, ISourceElementCallbackDelegate var ) {
		super.initialize(o);
		referencedElement = (IASTVariable) var;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
	 */
	public void reset() {
		super.resetOffset();
		referencedElement = null;
	}
	/**
	 * 
	 */
	public ASTVariableReference() {
		super(0);
		referencedElement = null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.core.parser.ast.IASTReference#getReferencedElement()
	 */
	public ISourceElementCallbackDelegate getReferencedElement() {
		return referencedElement;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.fdt.core.parser.ISourceElementRequestor)
	 */
	public void acceptElement(ISourceElementRequestor requestor) {
		try {
			requestor.acceptVariableReference(this);
		} catch (Exception e) {
			/* do nothing */
		}
	}
}
