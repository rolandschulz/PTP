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
import org.eclipse.fdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.fdt.core.parser.ast.IASTNamespaceReference;


public class ASTNamespaceReference extends ASTReference
		implements
			IASTNamespaceReference {
	private IASTNamespaceDefinition reference;

	/**
	 * @param offset
	 * @param definition
	 */
	public ASTNamespaceReference(int offset,
			IASTNamespaceDefinition definition) {
		super(offset);
		reference = definition;
	}

	/**
	 * 
	 */
	public ASTNamespaceReference() {
		super(0);
		reference = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.core.parser.ast.IASTReference#getReferencedElement()
	 */
	public ISourceElementCallbackDelegate getReferencedElement() {
		return reference;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.fdt.core.parser.ISourceElementRequestor)
	 */
	public void acceptElement(ISourceElementRequestor requestor) {
		try {
			requestor.acceptNamespaceReference(this);
		} catch (Exception e) {
			/* do nothing */
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate)
	 */
	public void initialize(int o, ISourceElementCallbackDelegate referencedElement) {
		super.initialize(o);
		this.reference = (IASTNamespaceDefinition) referencedElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
	 */
	public void reset() {
		resetOffset();
		this.reference = null;
	}
}
