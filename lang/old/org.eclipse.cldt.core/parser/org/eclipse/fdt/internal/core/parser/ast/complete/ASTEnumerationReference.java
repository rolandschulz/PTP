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
import org.eclipse.fdt.core.parser.ast.IASTEnumerationReference;
import org.eclipse.fdt.core.parser.ast.IASTEnumerationSpecifier;


public class ASTEnumerationReference extends ASTReference
		implements
			IASTEnumerationReference {
	private IASTEnumerationSpecifier referencedElement;
	/**
	 * @param offset
	 * @param specifier
	 */
	public ASTEnumerationReference(int offset,
			IASTEnumerationSpecifier specifier) {
		super(offset);
		referencedElement = specifier;
	}

	/**
	 * 
	 */
	public ASTEnumerationReference() {
		super( 0 );
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
			requestor.acceptEnumerationReference(this);
		} catch (Exception e) {
			/* do nothing */
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate)
	 */
	public void initialize(int o, ISourceElementCallbackDelegate re) {
		initialize(o);
		this.referencedElement = (IASTEnumerationSpecifier) re; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
	 */
	public void reset() {
		super.resetOffset();
		this.referencedElement = null;
	}
}
