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
import org.eclipse.fdt.core.parser.ast.IASTMethod;
import org.eclipse.fdt.core.parser.ast.IASTMethodReference;


public class ASTMethodReference extends ASTReference
		implements
			IASTMethodReference {
	private IASTMethod method;
	/**
	 * @param offset
	 */
	public ASTMethodReference(int offset, IASTMethod method) {
		super(offset);
		this.method = method;
	}
	/**
	 * 
	 */
	public ASTMethodReference() {
		super(0);
		this.method = null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.core.parser.ast.IASTReference#getReferencedElement()
	 */
	public ISourceElementCallbackDelegate getReferencedElement() {
		return method;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.fdt.core.parser.ISourceElementRequestor)
	 */
	public void acceptElement(ISourceElementRequestor requestor) {
		try {
			requestor.acceptMethodReference(this);
		} catch (Exception e) {
			/* do nothing */
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate)
	 */
	public void initialize(int o, ISourceElementCallbackDelegate referencedElement) {
		super.initialize(o);
		this.method = (IASTMethod) referencedElement;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
	 */
	public void reset() {
		resetOffset();
		this.method= null;
	}
}
